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

import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.types.QuestionResult;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanExecptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.tools.message.MessageReplacer;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class BeanDeleterCommand<BEAN_TYPE> implements ICommand, ICommandExecutor {

	private final String singleDeletionConfirmMessage = Messages.getString("BeanDeleterCommand.single_deletion_confirm_message");
	private final String multiDeletionConfirmMessage = Messages.getString("BeanDeleterCommand.multi_deletion_confirm_message");
	private final String couldNotBeUndoneMessage = Messages.getString("BeanDeleterCommand.can_not_be_undone");
	private final String nothingSelectedMessage = Messages.getString("BeanDeleterCommand.nothing_selected");

	private final IBeanListModel<BEAN_TYPE> model;
	private final IDeleterService deleterService;
	private final ExecutionObservable executionObservable;
	private final IBeanExecptionConverter exceptionConverter;

	private final BeanListModelEnabledChecker<BEAN_TYPE> enabledChecker;
	private final boolean autoSelection;
	private final boolean deletionConfirmDialog;

	BeanDeleterCommand(
		final IBeanListModel<BEAN_TYPE> model,
		final List<IEnabledChecker> enabledCheckers,
		final List<IExecutableChecker<BEAN_TYPE>> executableCheckers,
		final IDeleterService deleterService,
		final List<IExecutionInterceptor> executionInterceptors,
		final boolean multiSelection,
		final BeanModificationStatePolicy beanModificationStatePolicy,
		final BeanMessageStatePolicy beanMessageStatePolicy,
		final IBeanExecptionConverter exceptionConverter,
		final boolean autoSelection,
		final boolean deletionConfirmDialog) {

		Assert.paramNotNull(model, "model");
		Assert.paramNotNull(deleterService, "deleterService");
		Assert.paramNotNull(executionInterceptors, "executionInterceptors");
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");

		this.enabledChecker = new BeanListModelEnabledChecker<BEAN_TYPE>(
			model,
			multiSelection ? BeanSelectionPolicy.MULTI_SELECTION : BeanSelectionPolicy.SINGLE_SELECTION,
			beanModificationStatePolicy,
			beanMessageStatePolicy,
			enabledCheckers,
			executableCheckers,
			false);

		this.model = model;
		this.deleterService = deleterService;
		this.executionObservable = new ExecutionObservable(executionInterceptors);
		this.exceptionConverter = exceptionConverter;
		this.autoSelection = autoSelection;
		this.deletionConfirmDialog = deletionConfirmDialog;
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

		if (deletionConfirmDialog) {
			if (!showDeletionConfirmDialog(executionContext, selection.size())) {
				return;
			}
		}

		final IBeanKeyFactory beanKeyFactory = CapUiToolkit.beanKeyFactory();
		final IExecutionTask executionTask = CapUiToolkit.executionTaskFactory().create();

		final List<IBeanKey> beanKeys = new LinkedList<IBeanKey>();
		final List<IBeanProxy<BEAN_TYPE>> beans = new LinkedList<IBeanProxy<BEAN_TYPE>>();

		for (final Integer index : selection) {
			final IBeanProxy<BEAN_TYPE> bean = model.getBean(index.intValue());
			if (bean != null && !bean.isDummy() && !bean.isTransient()) {
				bean.setExecutionTask(executionTask);
				beanKeys.add(beanKeyFactory.createKey(bean));
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

		if (autoSelection) {
			if (!EmptyCheck.isEmpty(selection)) {
				model.setSelection(Collections.singletonList(selection.get(selection.size() - 1) + 1));
			}
		}
		model.fireBeansChanged();
		executionObservable.fireAfterExecutionPrepared(executionContext);
		deleterService.delete(new ResultCallback(executionContext, beans), beanKeys, executionTask);
	}

	private boolean showDeletionConfirmDialog(final IExecutionContext executionContext, final int selectionCount) {
		final QuestionResult questionResult = Toolkit.getQuestionPane().askYesNoQuestion(
				executionContext,
				getConfirmationMessage(selectionCount),
				QuestionResult.YES);
		return questionResult == QuestionResult.YES;
	}

	private String getConfirmationMessage(final int selectionCount) {
		final StringBuilder result = new StringBuilder();
		if (selectionCount == 1) {
			result.append(singleDeletionConfirmMessage);
		}
		else {
			result.append(MessageReplacer.replace(multiDeletionConfirmMessage, "" + selectionCount));
		}
		result.append("\n");
		result.append(couldNotBeUndoneMessage);
		return result.toString();
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
			model.removeBeans(beans);
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
