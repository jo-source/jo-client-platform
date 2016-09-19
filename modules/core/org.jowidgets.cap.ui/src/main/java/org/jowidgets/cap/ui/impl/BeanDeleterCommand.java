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
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.exception.ServiceCanceledException;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.bean.IBeanSelection;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.plugin.IBeanProxyLabelRendererPlugin;
import org.jowidgets.cap.ui.tools.execution.AbstractUiResultCallback;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProperties;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.IDecorator;

final class BeanDeleterCommand<BEAN_TYPE> implements ICommand, ICommandExecutor {

	private static final IMessage SINGLE_DELETION_CONFIRM = Messages.getMessage(
			"BeanDeleterCommand.single_deletion_confirm_message");
	private static final IMessage SINGLE_DELETION_PATTERN_CONFIRM = Messages.getMessage(
			"BeanDeleterCommand.single_deletion_pattern_confirm_message");
	private static final IMessage MULTI_DELETION_CONFIRM = Messages.getMessage(
			"BeanDeleterCommand.multi_deletion_confirm_message");
	private static final IMessage CAN_NOT_BE_UNDONE = Messages.getMessage("BeanDeleterCommand.can_not_be_undone");
	private static final IMessage NOTHING_SELECTED = Messages.getMessage("BeanDeleterCommand.nothing_selected");
	private static final IMessage DELETION_FAILED = Messages.getMessage("BeanDeleterCommand.deletion_failed");

	private final IBeanListModel<BEAN_TYPE> model;
	private final IDeleterService deleterService;
	private final ExecutionObservable<Void> executionObservable;
	private final IBeanExceptionConverter exceptionConverter;

	private final BeanSelectionProviderEnabledChecker<BEAN_TYPE> enabledChecker;
	private final boolean autoSelection;
	private final boolean deletionConfirmDialog;

	BeanDeleterCommand(
		final IBeanListModel<BEAN_TYPE> model,
		final List<IEnabledChecker> enabledCheckers,
		final List<IExecutableChecker<BEAN_TYPE>> executableCheckers,
		final IDeleterService deleterService,
		final List<IExecutionInterceptor<Void>> executionInterceptors,
		final boolean multiSelection,
		final BeanModificationStatePolicy beanModificationStatePolicy,
		final BeanMessageStatePolicy beanMessageStatePolicy,
		final IBeanExceptionConverter exceptionConverter,
		final boolean autoSelection,
		final boolean deletionConfirmDialog) {

		Assert.paramNotNull(model, "model");
		Assert.paramNotNull(deleterService, "deleterService");
		Assert.paramNotNull(executionInterceptors, "executionInterceptors");
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");

		this.enabledChecker = new BeanSelectionProviderEnabledChecker<BEAN_TYPE>(
			model,
			multiSelection ? BeanSelectionPolicy.MULTI_SELECTION : BeanSelectionPolicy.SINGLE_SELECTION,
			beanModificationStatePolicy,
			beanMessageStatePolicy,
			enabledCheckers,
			executableCheckers,
			false);

		this.model = model;
		this.deleterService = deleterService;
		this.executionObservable = new ExecutionObservable<Void>(executionInterceptors);
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

		final IBeanSelection<BEAN_TYPE> beanSelection = model.getBeanSelection();

		if (beanSelection == null || beanSelection.getSelection().size() == 0) {
			Toolkit.getMessagePane().showWarning(executionContext, NOTHING_SELECTED.get());
			return;
		}

		if (deletionConfirmDialog) {
			if (!showDeletionConfirmDialog(executionContext, beanSelection)) {
				executionObservable.fireAfterExecutionCanceled(executionContext);
				return;
			}
		}

		final IBeanKeyFactory beanKeyFactory = CapUiToolkit.beanKeyFactory();
		final IExecutionTask executionTask = CapUiToolkit.executionTaskFactory().create(executionContext);

		final List<IBeanKey> beanKeys = new LinkedList<IBeanKey>();
		final List<IBeanProxy<BEAN_TYPE>> beans = new LinkedList<IBeanProxy<BEAN_TYPE>>();
		final List<IBeanProxy<BEAN_TYPE>> transientBeans = new LinkedList<IBeanProxy<BEAN_TYPE>>();

		for (final IBeanProxy<BEAN_TYPE> bean : beanSelection.getSelection()) {
			if (bean != null && bean.isTransient()) {
				transientBeans.add(bean);
			}
			else if (bean != null && !bean.isDummy() && !bean.isLastRowDummy()) {
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
			final ArrayList<Integer> selection = model.getSelection();
			if (!EmptyCheck.isEmpty(selection)) {
				final int newSelectionIndex = selection.get(selection.size() - 1) + 1;
				if (newSelectionIndex >= 0 && newSelectionIndex < model.getSize()) {
					model.setSelection(Collections.singletonList(newSelectionIndex));
				}
			}
		}
		if (!transientBeans.isEmpty()) {
			model.removeBeans(transientBeans);
		}
		model.fireBeansChanged();
		executionObservable.fireAfterExecutionPrepared(executionContext);
		if (!beanKeys.isEmpty()) {
			deleterService.delete(new ResultCallback(executionContext, beans), beanKeys, executionTask);
		}
	}

	private boolean showDeletionConfirmDialog(
		final IExecutionContext executionContext,
		final IBeanSelection<BEAN_TYPE> selection) {
		final QuestionResult questionResult = Toolkit.getQuestionPane().askYesNoQuestion(
				executionContext,
				getConfirmationMessage(selection),
				QuestionResult.YES);
		return questionResult == QuestionResult.YES;
	}

	private String getConfirmationMessage(final IBeanSelection<BEAN_TYPE> selection) {
		final StringBuilder result = new StringBuilder();
		final int selectionCount = selection.getSelection().size();
		if (selectionCount == 1) {
			final String beanLabel = getBeanLabel(selection);
			if (!EmptyCheck.isEmpty(beanLabel)) {
				result.append(MessageReplacer.replace(SINGLE_DELETION_PATTERN_CONFIRM.get(), beanLabel));
			}
			else {
				result.append(SINGLE_DELETION_CONFIRM.get());
			}
		}
		else {
			result.append(MessageReplacer.replace(MULTI_DELETION_CONFIRM.get(), "" + selectionCount));
		}
		result.append("\n");
		result.append(CAN_NOT_BE_UNDONE.get());
		return result.toString();
	}

	private String getBeanLabel(final IBeanSelection<BEAN_TYPE> selection) {
		final IBeanProxyLabelRenderer<BEAN_TYPE> renderer = getRenderer(selection);
		if (renderer != null) {
			return renderer.getLabel(selection.getFirstSelected()).getText();
		}
		else {
			return null;
		}
	}

	private IBeanProxyLabelRenderer<BEAN_TYPE> getRenderer(final IBeanSelection<BEAN_TYPE> selection) {
		final IBeanProxyLabelRenderer<BEAN_TYPE> renderer = getPatternBasedRenderer(selection);
		if (renderer != null) {
			return getPluginDecoratedRenderer(selection.getEntityId(), selection.getBeanType(), renderer);
		}
		else {
			return null;
		}
	}

	private IBeanProxyLabelRenderer<BEAN_TYPE> getPatternBasedRenderer(final IBeanSelection<BEAN_TYPE> selection) {
		final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService != null) {
			final IBeanDtoDescriptor dtoDescriptor = entityService.getDescriptor(selection.getEntityId());
			if (dtoDescriptor != null) {
				final String renderingPattern = dtoDescriptor.getRenderingPattern().get();
				if (!EmptyCheck.isEmpty(renderingPattern)) {
					final Object iconDescriptor = dtoDescriptor.getIconDescriptor();
					return new BeanProxyLabelPatternRenderer<BEAN_TYPE>(
						renderingPattern,
						iconDescriptor,
						selection.getFirstSelected().getAttributes());
				}
			}
		}
		return null;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private IBeanProxyLabelRenderer<BEAN_TYPE> getPluginDecoratedRenderer(
		final Object entityId,
		final Class<?> beanType,
		final IBeanProxyLabelRenderer<BEAN_TYPE> renderer) {
		final IPluginPropertiesBuilder propertiesBuilder = PluginProperties.builder();
		propertiesBuilder.add(IBeanProxyLabelRendererPlugin.ENTITIY_ID_PROPERTY_KEY, entityId);
		propertiesBuilder.add(IBeanProxyLabelRendererPlugin.BEAN_TYPE_PROPERTY_KEY, beanType);
		final IPluginProperties properties = propertiesBuilder.build();
		IBeanProxyLabelRenderer result = renderer;
		for (final IBeanProxyLabelRendererPlugin plugin : PluginProvider.getPlugins(
				IBeanProxyLabelRendererPlugin.ID,
				properties)) {
			final IDecorator<IBeanProxyLabelRenderer<?>> decorator = plugin.getRendererDecorator(properties);
			if (decorator != null) {
				result = decorator.decorate(result);
			}
		}
		return result;
	}

	private final class ResultCallback extends AbstractUiResultCallback<Void> {

		private final List<IBeanProxy<BEAN_TYPE>> beans;
		private final IExecutionContext executionContext;

		private ResultCallback(final IExecutionContext executionContext, final List<IBeanProxy<BEAN_TYPE>> beans) {
			this.beans = beans;
			this.executionContext = executionContext;
		}

		@Override
		protected void finishedUi(final Void result) {
			for (final IBeanProxy<BEAN_TYPE> bean : beans) {
				bean.setExecutionTask(null);
			}
			model.removeBeans(beans);
			model.fireBeansChanged();
			executionObservable.fireAfterExecutionSuccess(executionContext, result);
		}

		@Override
		protected void exceptionUi(final Throwable exception) {

			for (final IBeanProxy<BEAN_TYPE> bean : beans) {
				bean.setExecutionTask(null);
				if (!(exception instanceof ServiceCanceledException)) {
					bean.addMessage(exceptionConverter.convert(DELETION_FAILED.get(), beans, bean, exception));
				}
			}

			model.fireBeansChanged();
			executionObservable.fireAfterExecutionError(executionContext, exception);
		}

	}
}
