/*
 * Copyright (c) 2011, grossmann
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * * Neither the name of the jo-widgets.org nor the
 *   names of its contributors may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL jo-widgets.org BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package org.jowidgets.cap.ui.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IFrame;
import org.jowidgets.api.widgets.blueprint.IDialogBluePrint;
import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.entity.IEntityLinkProperties;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanExecptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionTable;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionTableBluePrint;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

//TODO MG remove this later
//CHECKSTYLE:OFF
@SuppressWarnings("unused")
final class BeanLinkCommand<BEAN_TYPE> implements ICommand, ICommandExecutor {

	//TODO i18n
	private final String nothingSelectedMessage = Messages.getString("BeanLinkCommand.nothing_selected");

	private final IBeanListModel<BEAN_TYPE> model;

	private final ICreatorService linkCreatorService;
	private final Object linkableTableEntityId;
	private final IReaderService<Void> linkableReaderService;
	private final List<IAttribute<Object>> linkableTableAttributes;
	private final String linkableTableLabel;
	private final IEntityLinkProperties sourceLinkProperties;
	private final IEntityLinkProperties destinationLinkProperties;

	private final IBeanExecptionConverter exceptionConverter;

	private final ExecutionObservable executionObservable;

	private final BeanListModelEnabledChecker<?> enabledChecker;

	BeanLinkCommand(
		final IBeanListModel<BEAN_TYPE> model,
		final ICreatorService linkCreatorService,
		final Object linkableTableEntityId,
		final IReaderService<Void> linkableReaderService,
		final List<IAttribute<Object>> linkableTableAttributes,
		final String linkableTableLabel,
		final IEntityLinkProperties sourceLinkProperties,
		final IEntityLinkProperties destinationLinkProperties,
		final List<IEnabledChecker> enabledCheckers,
		final List<IExecutableChecker<BEAN_TYPE>> executableCheckers,
		final boolean multiSelection,
		final BeanModificationStatePolicy beanModificationStatePolicy,
		final BeanMessageStatePolicy beanMessageStatePolicy,
		final List<IExecutionInterceptor> executionInterceptors,
		final IBeanExecptionConverter exceptionConverter) {

		Assert.paramNotNull(model, "model");
		Assert.paramNotNull(linkCreatorService, "linkCreatorService");
		Assert.paramNotNull(linkableReaderService, "linkableReaderService");
		Assert.paramNotNull(linkableTableAttributes, "linkableTableAttributes");

		this.enabledChecker = new BeanListModelEnabledChecker<BEAN_TYPE>(
			model,
			multiSelection ? BeanSelectionPolicy.MULTI_SELECTION : BeanSelectionPolicy.SINGLE_SELECTION,
			beanModificationStatePolicy,
			beanMessageStatePolicy,
			enabledCheckers,
			executableCheckers,
			false);

		this.model = model;
		this.linkCreatorService = linkCreatorService;
		this.linkableTableEntityId = linkableTableEntityId;
		this.linkableReaderService = linkableReaderService;
		this.linkableTableAttributes = linkableTableAttributes;
		this.linkableTableLabel = linkableTableLabel;
		this.sourceLinkProperties = sourceLinkProperties;
		this.destinationLinkProperties = destinationLinkProperties;
		this.executionObservable = new ExecutionObservable(executionInterceptors);
		this.exceptionConverter = exceptionConverter;
	}

	@Override
	public ICommandExecutor getCommandExecutor() {
		return this;
	}

	@Override
	public IEnabledChecker getEnabledChecker() {
		return enabledChecker;
	}

	@Override
	public IExceptionHandler getExceptionHandler() {
		return null;
	}

	@Override
	public void execute(final IExecutionContext executionContext) throws Exception {
		if (!executionObservable.fireBeforeExecution(executionContext)) {
			return;
		}

		final ArrayList<Integer> selection = model.getSelection();

		if (selection == null || selection.size() == 0) {
			Toolkit.getMessagePane().showWarning(executionContext, nothingSelectedMessage);
			return;
		}

		final List<IBeanProxy<?>> beansToLink = getBeansToLink(executionContext);
		if (!EmptyCheck.isEmpty(beansToLink)) {
			linkBeans(executionContext, selection, beansToLink);
		}

	}

	private void linkBeans(
		final IExecutionContext executionContext,
		final ArrayList<Integer> selection,
		final List<IBeanProxy<?>> beansToLink) {

		final IExecutionTask executionTask = CapUiToolkit.executionTaskFactory().create();

		final List<IBeanKey> beanKeys = new LinkedList<IBeanKey>();
		final List<IBeanProxy<BEAN_TYPE>> beans = new LinkedList<IBeanProxy<BEAN_TYPE>>();

		for (final Integer index : selection) {
			final IBeanProxy<BEAN_TYPE> bean = model.getBean(index.intValue());
			if (bean != null && !bean.isDummy() && !bean.isTransient()) {
				bean.setExecutionTask(executionTask);
				beans.add(bean);
			}
		}

		final IUiThreadAccess uiThreadAccess = Toolkit.getUiThreadAccess();
		executionTask.addExecutionCallbackListener(new IExecutionCallbackListener() {
			@Override
			public void canceled() {
				uiThreadAccess.invokeLater(new Runnable() {
					@Override
					public void run() {
						for (final IBeanProxy<BEAN_TYPE> bean : beans) {
							bean.setExecutionTask(null);
						}
						model.fireBeansChanged();
						executionObservable.fireAfterExecutionCanceled(executionContext);
					}
				});
			}
		});

		model.fireBeansChanged();
		executionObservable.fireAfterExecutionPrepared(executionContext);
	}

	private List<IBeanProxy<?>> getBeansToLink(final IExecutionContext executionContext) {
		final IAction action = executionContext.getAction();
		final IDialogBluePrint dialogBp = BPF.dialog().setTitle(action.getText()).setIcon(action.getIcon());
		//TODO this must be done better later
		dialogBp.setSize(new Dimension(1000, 600));
		final IFrame dialog = Toolkit.getActiveWindow().createChildWindow(dialogBp);

		dialog.setLayout(MigLayoutFactory.growingInnerCellLayout());

		final IBeanTableModelBuilder<IBean> modelBuilder = CapUiToolkit.beanTableModelBuilder(linkableTableEntityId);
		modelBuilder.setAttributes(linkableTableAttributes);
		modelBuilder.setParent(model, LinkType.SELECTION_ALL);

		final IBeanTableModel<IBean> linkableModel = modelBuilder.build();

		final IBeanSelectionTableBluePrint<?> tableBp = CapUiToolkit.bluePrintFactory().beanSelectionTable(linkableModel);

		final IBeanSelectionTable<?> selectionTable = dialog.add(tableBp, MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

		linkableModel.load();
		dialog.setVisible(true);

		return Collections.emptyList();
	}

	private final class ResultCallback implements IResultCallback<Void> {

		private final List<IBeanProxy<BEAN_TYPE>> beans;
		private final IExecutionContext executionContext;
		private final IUiThreadAccess uiThreadAccess;

		private ResultCallback(final IExecutionContext executionContext, final List<IBeanProxy<BEAN_TYPE>> beans) {
			this.beans = beans;
			this.uiThreadAccess = Toolkit.getUiThreadAccess();
			this.executionContext = executionContext;
		}

		@Override
		public void finished(final Void result) {
			onSuccessLater();
		}

		@Override
		public void exception(final Throwable exception) {
			onExceptionLater(exception);
		}

		@Override
		public void timeout() {
			exception(new TimeoutException("Timeout while deleting data"));
		}

		private void onSuccessLater() {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					onSuccess();
				}
			});
		}

		private void onSuccess() {
			for (final IBeanProxy<BEAN_TYPE> bean : beans) {
				bean.setExecutionTask(null);
			}

			model.fireBeansChanged();
			executionObservable.fireAfterExecutionSuccess(executionContext);
		}

		private void onExceptionLater(final Throwable exception) {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					onException(exception);
				}
			});
		}

		private void onException(final Throwable exception) {
			//CHECKSTYLE:OFF
			exception.printStackTrace();
			//CHECKSTYLE:ON

			for (final IBeanProxy<BEAN_TYPE> bean : beans) {
				bean.setExecutionTask(null);
				bean.addMessage(exceptionConverter.convert(beans, bean, exception));
			}

			model.fireBeansChanged();
			executionObservable.fireAfterExecutionError(executionContext, exception);
		}
	}
}
