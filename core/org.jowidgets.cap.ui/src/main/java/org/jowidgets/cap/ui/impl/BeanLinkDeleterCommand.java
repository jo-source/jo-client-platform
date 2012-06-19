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

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
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
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class BeanLinkDeleterCommand<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> implements ICommand, ICommandExecutor {

	private final String nothingSelectedMessage = Messages.getString("BeanLinkDeleterCommand.nothing_selected");

	private final ILinkDeleterService linkDeleterService;
	private final IBeanSelectionProvider<SOURCE_BEAN_TYPE> source;
	private final IBeanListModel<LINKED_BEAN_TYPE> linkedModel;
	private final IBeanExceptionConverter exceptionConverter;

	private final BeanSelectionProviderEnabledChecker<SOURCE_BEAN_TYPE> enabledChecker;
	private final ExecutionObservable<List<IBeanDto>> executionObservable;

	BeanLinkDeleterCommand(
		final ILinkDeleterService linkDeleterService,
		final IBeanSelectionProvider<SOURCE_BEAN_TYPE> source,
		final boolean sourceMultiSelection,
		final BeanModificationStatePolicy sourceModificationPolicy,
		final BeanMessageStatePolicy sourceMessageStatePolicy,
		final List<IExecutableChecker<SOURCE_BEAN_TYPE>> sourceExecutableCheckers,
		final IBeanListModel<LINKED_BEAN_TYPE> linkedModel,
		final List<IEnabledChecker> enabledCheckers,
		final List<IExecutionInterceptor<List<IBeanDto>>> executionInterceptors,
		final IBeanExceptionConverter exceptionConverter) {

		Assert.paramNotNull(linkDeleterService, "linkDeleterService");
		Assert.paramNotNull(sourceModificationPolicy, "sourceModificationPolicy");
		Assert.paramNotNull(sourceMessageStatePolicy, "sourceMessageStatePolicy");
		Assert.paramNotNull(sourceExecutableCheckers, "sourceExecutableCheckers");
		Assert.paramNotNull(enabledCheckers, "enabledCheckers");
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");

		this.enabledChecker = new BeanSelectionProviderEnabledChecker<SOURCE_BEAN_TYPE>(
			source,
			sourceMultiSelection ? BeanSelectionPolicy.MULTI_SELECTION : BeanSelectionPolicy.SINGLE_SELECTION,
			sourceModificationPolicy,
			sourceMessageStatePolicy,
			enabledCheckers,
			sourceExecutableCheckers,
			false);

		this.source = source;
		this.linkDeleterService = linkDeleterService;
		this.linkedModel = linkedModel;

		this.executionObservable = new ExecutionObservable<List<IBeanDto>>(executionInterceptors);
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

		final List<IBeanProxy<SOURCE_BEAN_TYPE>> sourceSelection = source.getBeanSelection().getSelection();
		if (!checkSelection(sourceSelection, executionContext)) {
			return;
		}

		final List<IBeanProxy<LINKED_BEAN_TYPE>> linkedSelection = linkedModel.getBeanSelection().getSelection();
		if (!checkSelection(linkedSelection, executionContext)) {
			return;
		}

		new LinkDeleter(executionContext, sourceSelection, linkedSelection).delete();
	}

	private boolean checkSelection(final List<?> selection, final IExecutionContext executionContext) {
		if (EmptyCheck.isEmpty(selection)) {
			Toolkit.getMessagePane().showWarning(executionContext, nothingSelectedMessage);
			return false;
		}
		return true;
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

			executionObservable.fireAfterExecutionPrepared(executionContext);

		}

		void delete() {
			final IResultCallback<Void> resultCallback = new AbstractUiResultCallback<Void>() {
				@Override
				protected void finishedUi(final Void result) {
					setExecutionTaskNull();
					linkedModel.removeBeans(linkedSelection);
					linkedModel.fireBeansChanged();
					executionObservable.fireAfterExecutionSuccess(executionContext, new LinkedList<IBeanDto>());
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

				bean.addMessage(exceptionConverter.convert(linkedSelection, bean, exception));
				bean.setExecutionTask(null);
			}
		}

		private void setExecutionTaskNull() {
			for (final IBeanProxy<LINKED_BEAN_TYPE> bean : linkedSelection) {
				bean.setExecutionTask(null);
			}
		}

	}

}
