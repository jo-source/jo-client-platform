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
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.types.QuestionResult;
import org.jowidgets.cap.common.api.exception.ServiceCanceledException;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.link.ILinkDeletion;
import org.jowidgets.cap.common.api.link.LinkDeletion;
import org.jowidgets.cap.common.api.service.ILinkDeleterService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.tools.execution.AbstractUiExecutionCallbackListener;
import org.jowidgets.cap.ui.tools.execution.AbstractUiResultCallback;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.tools.command.EnabledCheckerCompositeBuilder;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class BeanLinkDeleterCommand<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> implements ICommand, ICommandExecutor {

	private static final IMessage SINGLE_DELETION_CONFIRM = Messages.getMessage("BeanLinkDeleterCommand.single_deletion_confirm_message");
	private static final IMessage MULTI_DELETION_CONFIRM = Messages.getMessage("BeanLinkDeleterCommand.multi_deletion_confirm_message");
	private static final IMessage CAN_NOT_BE_UNDONE = Messages.getMessage("BeanLinkDeleterCommand.can_not_be_undone");
	private static final IMessage NOTHING_SELECTED = Messages.getMessage("BeanLinkDeleterCommand.nothing_selected");
	private static final IMessage SHORT_ERROR = Messages.getMessage("BeanLinkDeleterCommand.short_error_message");

	private final ILinkDeleterService linkDeleterService;
	private final IBeanSelectionProvider<SOURCE_BEAN_TYPE> source;
	private final IBeanListModel<LINKED_BEAN_TYPE> linkedModel;
	private final IBeanExceptionConverter exceptionConverter;
	private final IEnabledChecker enabledChecker;
	private final ExecutionObservable<Void> executionObservable;
	private final boolean autoSelection;
	private final boolean deletionConfirmDialog;

	BeanLinkDeleterCommand(
		final ILinkDeleterService linkDeleterService,
		final boolean deletionConfirmDialog,
		final IBeanSelectionProvider<SOURCE_BEAN_TYPE> source,
		final boolean sourceSelectionAutoRefresh,
		final boolean sourceMultiSelection,
		final BeanModificationStatePolicy sourceModificationPolicy,
		final BeanMessageStatePolicy sourceMessageStatePolicy,
		final List<IExecutableChecker<SOURCE_BEAN_TYPE>> sourceExecutableCheckers,
		final IBeanListModel<LINKED_BEAN_TYPE> linkedModel,
		final boolean linkedMultiSelection,
		final BeanModificationStatePolicy linkedModificationPolicy,
		final BeanMessageStatePolicy linkedMessageStatePolicy,
		final List<IExecutableChecker<LINKED_BEAN_TYPE>> linkedExecutableCheckers,
		final List<IEnabledChecker> enabledCheckers,
		final boolean autoSelection,
		final List<IExecutionInterceptor<Void>> executionInterceptors,
		final IBeanExceptionConverter exceptionConverter) {

		Assert.paramNotNull(linkDeleterService, "linkDeleterService");
		Assert.paramNotNull(sourceModificationPolicy, "sourceModificationPolicy");
		Assert.paramNotNull(sourceMessageStatePolicy, "sourceMessageStatePolicy");
		Assert.paramNotNull(sourceExecutableCheckers, "sourceExecutableCheckers");
		Assert.paramNotNull(linkedModificationPolicy, "linkedModificationPolicy");
		Assert.paramNotNull(linkedMessageStatePolicy, "linkedMessageStatePolicy");
		Assert.paramNotNull(linkedExecutableCheckers, "linkedExecutableCheckers");
		Assert.paramNotNull(enabledCheckers, "enabledCheckers");
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");

		final EnabledCheckerCompositeBuilder enabledCheckerBuilder = new EnabledCheckerCompositeBuilder();

		enabledCheckerBuilder.add(new BeanSelectionProviderEnabledChecker<SOURCE_BEAN_TYPE>(
			source,
			sourceMultiSelection ? BeanSelectionPolicy.MULTI_SELECTION : BeanSelectionPolicy.SINGLE_SELECTION,
			sourceModificationPolicy,
			sourceMessageStatePolicy,
			enabledCheckers,
			sourceExecutableCheckers,
			false));

		enabledCheckerBuilder.add(new BeanSelectionProviderEnabledChecker<LINKED_BEAN_TYPE>(
			linkedModel,
			linkedMultiSelection ? BeanSelectionPolicy.MULTI_SELECTION : BeanSelectionPolicy.SINGLE_SELECTION,
			linkedModificationPolicy,
			linkedMessageStatePolicy,
			new LinkedList<IEnabledChecker>(),
			linkedExecutableCheckers,
			false));

		this.enabledChecker = enabledCheckerBuilder.build();

		this.source = source;
		this.linkDeleterService = linkDeleterService;
		this.linkedModel = linkedModel;

		this.executionObservable = new ExecutionObservable<Void>(executionInterceptors);
		this.exceptionConverter = exceptionConverter;
		this.deletionConfirmDialog = deletionConfirmDialog;
		this.autoSelection = autoSelection;

		if (sourceSelectionAutoRefresh) {
			BeanSelectionProviderRefreshInterceptor<SOURCE_BEAN_TYPE, Void> refreshInterceptor;
			refreshInterceptor = new BeanSelectionProviderRefreshInterceptor<SOURCE_BEAN_TYPE, Void>(source);
			executionObservable.addExecutionInterceptor(refreshInterceptor);
		}
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

		final List<IBeanProxy<SOURCE_BEAN_TYPE>> sourceSelection = source.getBeanSelection().getSelection();
		if (!checkSelection(sourceSelection, executionContext)) {
			return;
		}

		final List<IBeanProxy<LINKED_BEAN_TYPE>> linkedSelection = linkedModel.getBeanSelection().getSelection();
		if (!checkSelection(linkedSelection, executionContext)) {
			return;
		}

		if (deletionConfirmDialog) {
			if (!showDeletionConfirmDialog(executionContext, linkedSelection.size())) {
				return;
			}
		}

		new LinkDeleter(executionContext, sourceSelection, linkedSelection).delete();
	}

	private boolean checkSelection(final List<?> selection, final IExecutionContext executionContext) {
		if (EmptyCheck.isEmpty(selection)) {
			Toolkit.getMessagePane().showWarning(executionContext, NOTHING_SELECTED.get());
			return false;
		}
		return true;
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
			result.append(SINGLE_DELETION_CONFIRM.get());
		}
		else {
			result.append(MessageReplacer.replace(MULTI_DELETION_CONFIRM.get(), "" + selectionCount));
		}
		result.append("\n");
		result.append(CAN_NOT_BE_UNDONE.get());
		return result.toString();
	}

	private final class LinkDeleter {

		private final IExecutionContext executionContext;
		private final List<IBeanProxy<LINKED_BEAN_TYPE>> linkedSelection;

		private final IExecutionTask executionTask;
		private final List<ILinkDeletion> linkDeletions;

		LinkDeleter(
			final IExecutionContext executionContext,
			final List<IBeanProxy<SOURCE_BEAN_TYPE>> sourceSelection,
			final List<IBeanProxy<LINKED_BEAN_TYPE>> linkedSelection) {

			this.executionContext = executionContext;
			this.linkedSelection = linkedSelection;

			final IBeanKeyFactory beanKeyFactory = CapUiToolkit.beanKeyFactory();
			this.executionTask = CapUiToolkit.executionTaskFactory().create();

			this.linkDeletions = new LinkedList<ILinkDeletion>();
			boolean firstIteration = true;
			for (final IBeanProxy<SOURCE_BEAN_TYPE> sourceBean : sourceSelection) {
				for (final IBeanProxy<LINKED_BEAN_TYPE> linkedBean : linkedSelection) {
					if (firstIteration) {
						linkedBean.setExecutionTask(executionTask);
					}
					linkDeletions.add(LinkDeletion.create(
							beanKeyFactory.createKey(sourceBean),
							beanKeyFactory.createKey(linkedBean)));
				}
				firstIteration = false;
			}

			executionTask.addExecutionCallbackListener(new AbstractUiExecutionCallbackListener() {
				@Override
				protected void canceledUi() {
					setExecutionTaskNull();
					linkedModel.fireBeansChanged();
					executionObservable.fireAfterExecutionCanceled(executionContext);
				}
			});

			if (autoSelection) {
				final ArrayList<Integer> selection = linkedModel.getSelection();
				if (!EmptyCheck.isEmpty(linkedSelection)) {
					final int newSelectionIndex = selection.get(selection.size() - 1) + 1;
					if (newSelectionIndex >= 0 && newSelectionIndex < linkedModel.getSize()) {
						linkedModel.setSelection(Collections.singletonList(newSelectionIndex));
					}
				}
			}
			linkedModel.fireBeansChanged();

			executionObservable.fireAfterExecutionPrepared(executionContext);

		}

		void delete() {
			final IResultCallback<Void> resultCallback = new AbstractUiResultCallback<Void>() {
				@Override
				protected void finishedUi(final Void result) {
					setExecutionTaskNull();
					linkedModel.removeBeans(linkedSelection);
					linkedModel.fireBeansChanged();
					executionObservable.fireAfterExecutionSuccess(executionContext, null);
				}

				@Override
				protected void exceptionUi(final Throwable exception) {
					//CHECKSTYLE:OFF
					exception.printStackTrace();
					//CHECKSTYLE:ON

					onError(exception);

					linkedModel.fireBeansChanged();
					executionObservable.fireAfterExecutionError(executionContext, exception);
				}
			};
			linkDeleterService.delete(resultCallback, linkDeletions, executionTask);
		}

		private void onError(final Throwable exception) {
			for (final IBeanProxy<?> bean : linkedSelection) {
				if (!(exception instanceof ServiceCanceledException)) {
					bean.addMessage(exceptionConverter.convert(getShortErrorMessage(), linkedSelection, bean, exception));
				}
				bean.setExecutionTask(null);
			}
		}

		private String getShortErrorMessage() {
			final String actionText = executionContext.getAction().getText().replaceAll("\\.", "").trim();
			return MessageReplacer.replace(SHORT_ERROR.get(), actionText);
		}

		private void setExecutionTaskNull() {
			for (final IBeanProxy<LINKED_BEAN_TYPE> bean : linkedSelection) {
				bean.setExecutionTask(null);
			}
		}

	}

}
