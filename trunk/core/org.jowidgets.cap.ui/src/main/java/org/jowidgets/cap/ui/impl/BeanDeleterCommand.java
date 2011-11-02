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

import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanExecptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class BeanDeleterCommand<BEAN_TYPE> implements ICommand, ICommandExecutor {

	private final IBeanListModel<BEAN_TYPE> model;

	private final IDeleterService deleterService;

	@SuppressWarnings("unused")
	private final List<IExecutionInterceptor> executionInterceptors;

	@SuppressWarnings("unused")
	private final BeanExecutionPolicy beanExecutionPolicy;

	@SuppressWarnings("unused")
	private final IBeanExecptionConverter exceptionConverter;

	private final BeanListModelEnabledChecker<BEAN_TYPE> enabledChecker;

	BeanDeleterCommand(
		final IBeanListModel<BEAN_TYPE> model,
		final List<IEnabledChecker> enabledCheckers,
		final List<IExecutableChecker<BEAN_TYPE>> executableCheckers,
		final IDeleterService deleterService,
		final List<IExecutionInterceptor> executionInterceptors,
		final boolean multiSelection,
		final BeanExecutionPolicy beanExecutionPolicy,
		final BeanModificationStatePolicy beanModificationStatePolicy,
		final BeanMessageStatePolicy beanMessageStatePolicy,
		final IBeanExecptionConverter exceptionConverter) {

		Assert.paramNotNull(model, "model");
		Assert.paramNotNull(deleterService, "deleterService");
		Assert.paramNotNull(executionInterceptors, "executionInterceptors");
		Assert.paramNotNull(beanExecutionPolicy, "beanExecutionPolicy");
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
		this.executionInterceptors = new LinkedList<IExecutionInterceptor>(executionInterceptors);
		this.beanExecutionPolicy = beanExecutionPolicy;
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
		final IExecutionTask executionTask = CapUiToolkit.executionTaskFactory().create();
		final List<IBeanKey> beanKeys = new LinkedList<IBeanKey>();
		final IBeanKeyFactory beanKeyFactory = CapUiToolkit.beanKeyFactory();
		final List<IBeanProxy<BEAN_TYPE>> beans = new LinkedList<IBeanProxy<BEAN_TYPE>>();
		for (final Integer index : model.getSelection()) {
			final IBeanProxy<BEAN_TYPE> bean = model.getBean(index.intValue());
			if (bean != null && !bean.isDummy() && !bean.isTransient()) {
				bean.setExecutionTask(executionTask);
				beanKeys.add(beanKeyFactory.createKey(bean));
				beans.add(bean);
			}
		}
		final ArrayList<Integer> selection = model.getSelection();
		if (!EmptyCheck.isEmpty(selection)) {
			model.setSelection(Collections.singletonList(selection.get(selection.size() - 1) + 1));
		}
		model.fireBeansChanged();
		deleterService.delete(new ResultCallback(beans), beanKeys, executionTask);
	}

	private final class ResultCallback implements IResultCallback<Void> {

		private final List<IBeanProxy<BEAN_TYPE>> beans;
		private final IUiThreadAccess uiThreadAccess;

		private ResultCallback(final List<IBeanProxy<BEAN_TYPE>> beans) {
			this.beans = beans;
			this.uiThreadAccess = Toolkit.getUiThreadAccess();
		}

		@Override
		public void finished(final Void result) {
			onSuccessLater();
		}

		@Override
		public void exception(final Throwable exception) {
			//CHECKSTYLE:OFF
			exception.printStackTrace();
			//CHECKSTYLE:ON
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
			model.removeBeans(beans);
		}
	}
}
