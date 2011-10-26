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
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.IExecutorService;
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
import org.jowidgets.cap.ui.api.execution.IExecutor;
import org.jowidgets.cap.ui.api.execution.IParameterProvider;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.util.ValueHolder;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.maybe.Nothing;
import org.jowidgets.util.maybe.Some;

@SuppressWarnings({"rawtypes", "unchecked"})
final class ExecutorCommand implements ICommand, ICommandExecutor {

	private final BeanListModelEnabledChecker enabledChecker;

	private final IBeanListModel<Object> listModel;
	private final List<Object> parameterProviders;
	private final List<IExecutionInterceptor> executionInterceptors;
	private final BeanExecutionPolicy beanListExecutionPolicy;

	private final Object defaultParameter;
	private final Object executor;
	private final IBeanExecptionConverter beanExceptionConverter;

	ExecutorCommand(
		final IBeanListModel listModel,
		final BeanExecutionPolicy beanListExecutionPolicy,
		final BeanSelectionPolicy beanSelectionPolicy,
		final BeanModificationStatePolicy beanModificationStatePolicy,
		final BeanMessageStatePolicy beanMessageStatePolicy,
		final List<IEnabledChecker> enabledCheckers,
		final List<IExecutableChecker<Object>> executableCheckers,
		final IBeanExecptionConverter beanExceptionConverter,
		final List<Object> parameterProviders,
		final List<IExecutionInterceptor> executionInterceptors,
		final Object defaultParameter,
		final Object executor) {
		super();

		this.enabledChecker = new BeanListModelEnabledChecker(
			listModel,
			beanSelectionPolicy,
			beanModificationStatePolicy,
			beanMessageStatePolicy,
			enabledCheckers,
			executableCheckers);

		this.listModel = listModel;
		this.beanListExecutionPolicy = beanListExecutionPolicy;
		this.beanExceptionConverter = beanExceptionConverter;
		this.parameterProviders = parameterProviders;
		this.executionInterceptors = executionInterceptors;
		this.defaultParameter = defaultParameter;
		this.executor = executor;

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
		for (final IExecutionInterceptor interceptor : executionInterceptors) {
			if (!interceptor.beforeExecution(executionContext)) {
				return;
			}
		}

		final List<IBeanProxy> beans = new LinkedList<IBeanProxy>();
		for (final Integer index : listModel.getSelection()) {
			final IBeanProxy<Object> bean = listModel.getBean(index.intValue());
			beans.add(bean);
		}

		final BeanListExecutionHelper executionHelper = new BeanListExecutionHelper(
			listModel,
			beans,
			beanListExecutionPolicy,
			beanExceptionConverter);

		for (final List<IBeanProxy<?>> preparedBeans : executionHelper.prepareExecutions()) {
			new Execution(preparedBeans, executionContext, executionHelper).execute();
		}

	}

	private class Execution {

		private final IExecutionContext executionContext;
		private final List<IBeanProxy<?>> beans;
		private final BeanListExecutionHelper executionHelper;
		private final IExecutionTask executionTask;
		private final IUiThreadAccess uiThreadAccess;

		Execution(
			final List<IBeanProxy<?>> beans,
			final IExecutionContext executionContext,
			final BeanListExecutionHelper executionHelper) {

			this.beans = beans;
			this.executionContext = executionContext;
			this.executionHelper = executionHelper;
			this.uiThreadAccess = Toolkit.getUiThreadAccess();

			if (beans.size() > 0) {
				executionTask = beans.get(0).getExecutionTask();
			}
			else {
				executionTask = executionHelper.createExecutionTask();
			}
		}

		public void execute() {
			Object parameter = defaultParameter;
			for (final Object parameterProvider : parameterProviders) {
				final IMaybe paramResult = getParameter(parameterProvider, parameter, beans);
				if (paramResult.isNothing() || executionTask.isCanceled()) {
					executionHelper.afterExecution(beans, null);
					return;
				}
				else {
					parameter = paramResult.getValue();
				}
			}

			if (executionTask.isCanceled()) {
				executionHelper.afterExecution(beans, null);
				return;
			}
			if (executor instanceof IExecutor) {
				final IExecutor theExecutor = (IExecutor) executor;
				try {
					theExecutor.execute(executionContext, beans, parameter);
					executionHelper.afterExecution(beans, null);
				}
				catch (final Exception exception) {
					executionHelper.onExecption(beans, exception);
				}
			}
			else if (executor instanceof IExecutorService) {
				final IExecutorService executorService = (IExecutorService) executor;
				final IBeanKeyFactory beanKeyFactory = CapUiToolkit.beanKeyFactory();
				final List<IBeanKey> keys = beanKeyFactory.createKeys(beans);

				final IResultCallback<List<IBeanDto>> helperCallback = executionHelper.createResultCallback(beans);
				final IResultCallback<List<IBeanDto>> resultCallback = new IResultCallback<List<IBeanDto>>() {

					@Override
					public void finished(final List<IBeanDto> result) {
						helperCallback.finished(result);
						for (final IExecutionInterceptor interceptor : executionInterceptors) {
							helperCallback.finished(result);
							uiThreadAccess.invokeLater(new Runnable() {
								@Override
								public void run() {
									interceptor.afterExecution(executionContext);
								}
							});
						}
					}

					@Override
					public void exception(final Throwable exception) {
						helperCallback.exception(exception);
					}

					@Override
					public void timeout() {
						helperCallback.timeout();
					}

				};

				executorService.execute(resultCallback, keys, parameter, executionTask);
			}
		}

		private IMaybe getParameter(final Object parameterProvider, final Object defaultParameter, final List<IBeanProxy<?>> beans) {
			if (parameterProvider instanceof IParameterProvider) {
				final IParameterProvider theParameterProvider = (IParameterProvider) parameterProvider;
				final ValueHolder<IMaybe> result = new ValueHolder<IMaybe>(Nothing.getInstance());
				try {
					result.set(theParameterProvider.getParameter(executionContext, beans, defaultParameter));
				}
				catch (final Exception e) {
					executionHelper.onExecption(beans, e);
				}
				return result.get();
			}
			//TODO MG else if IParameterProviderService 
			return new Some(defaultParameter);
		}

	}

}
