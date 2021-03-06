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
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.IExecutorService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
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
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.util.IFilter;
import org.jowidgets.util.ValueHolder;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.maybe.Nothing;
import org.jowidgets.util.maybe.Some;

@SuppressWarnings({"rawtypes", "unchecked"})
final class ExecutorCommand<BEAN_TYPE> implements ICommand, ICommandExecutor {

	private static final IMessage SHORT_ERROR = Messages.getMessage("ExecutorCommand.short_error_message");

	private final BeanSelectionProviderEnabledChecker enabledChecker;

	private final IBeanListModel<BEAN_TYPE> listModel;
	private final List<Object> parameterProviders;
	private final IFilter<IBeanProxy<BEAN_TYPE>> selectionFilter;
	private final ExecutionObservable<List<IBeanDto>> executionObservable;
	private final BeanExecutionPolicy beanListExecutionPolicy;

	private final Object defaultParameter;
	private final Object executor;
	private final IBeanExceptionConverter beanExceptionConverter;

	ExecutorCommand(
		final IBeanListModel<BEAN_TYPE> listModel,
		final IFilter<IBeanProxy<BEAN_TYPE>> selectionFilter,
		final BeanExecutionPolicy beanListExecutionPolicy,
		final BeanSelectionPolicy beanSelectionPolicy,
		final BeanModificationStatePolicy beanModificationStatePolicy,
		final BeanMessageStatePolicy beanMessageStatePolicy,
		final List<IEnabledChecker> enabledCheckers,
		final List<IExecutableChecker<Object>> executableCheckers,
		final IBeanExceptionConverter beanExceptionConverter,
		final List<Object> parameterProviders,
		final List<IExecutionInterceptor<List<IBeanDto>>> executionInterceptors,
		final Object defaultParameter,
		final Object executor) {
		super();

		this.enabledChecker = new BeanSelectionProviderEnabledChecker(
			listModel,
			beanSelectionPolicy,
			beanModificationStatePolicy,
			beanMessageStatePolicy,
			enabledCheckers,
			executableCheckers,
			false);

		this.listModel = listModel;
		this.beanListExecutionPolicy = beanListExecutionPolicy;
		this.beanExceptionConverter = beanExceptionConverter;
		this.parameterProviders = parameterProviders;
		this.selectionFilter = selectionFilter;
		this.executionObservable = new ExecutionObservable<List<IBeanDto>>(executionInterceptors);
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
		if (!executionObservable.fireBeforeExecution(executionContext)) {
			return;
		}

		final List<IBeanProxy> beans = new LinkedList<IBeanProxy>();
		for (final Integer index : listModel.getSelection()) {
			final IBeanProxy<BEAN_TYPE> bean = listModel.getBean(index.intValue());
			if (bean != null && !bean.isDummy() && !bean.isLastRowDummy() && selectionFilter.accept(bean)) {
				beans.add(bean);
			}
		}

		final BeanListExecutionHelper executionHelper = new BeanListExecutionHelper(
			getShortErrorMessage(executionContext),
			listModel,
			beans,
			beanListExecutionPolicy,
			beanExceptionConverter,
			false,
			true);

		final List<List<IBeanProxy<?>>> preparedExecutions = executionHelper.prepareExecutions(false, executionContext);
		executionObservable.fireAfterExecutionPrepared(executionContext);

		for (final List<IBeanProxy<?>> preparedBeans : preparedExecutions) {
			new Execution(preparedBeans, executionContext, executionHelper).execute();
		}

	}

	private String getShortErrorMessage(final IExecutionContext executionContext) {
		final String actionText = executionContext.getAction().getText().replaceAll("\\.", "").trim();
		return MessageReplacer.replace(SHORT_ERROR.get(), actionText);
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
				executionTask = executionHelper.createExecutionTask(executionContext);
			}

			executionTask.addExecutionCallbackListener(new IExecutionCallbackListener() {
				@Override
				public void canceled() {
					uiThreadAccess.invokeLater(new Runnable() {
						@Override
						public void run() {
							executionHelper.afterExecution(beans, null);
							executionObservable.fireAfterExecutionCanceled(executionContext);
						}
					});
				}
			});
		}

		void execute() {
			Object parameter = defaultParameter;
			for (final Object parameterProvider : parameterProviders) {
				final IMaybe paramResult = getParameter(parameterProvider, parameter, beans);
				if (paramResult.isNothing() || executionTask.isCanceled()) {
					executionHelper.afterExecution(beans, null);
					executionObservable.fireAfterExecutionCanceled(executionContext);
					return;
				}
				else {
					parameter = paramResult.getValue();
				}
			}

			if (executionTask.isCanceled()) {
				executionHelper.afterExecution(beans, null);
				executionObservable.fireAfterExecutionCanceled(executionContext);
				return;
			}
			if (executor instanceof IExecutor) {
				final IExecutor theExecutor = (IExecutor) executor;
				try {
					theExecutor.execute(executionContext, beans, parameter);
					executionHelper.afterExecution(beans, null);
					executionObservable.fireAfterExecutionSuccess(executionContext, createBeanDtos(beans));
				}
				catch (final Exception exception) {
					executionHelper.onExecption(beans, exception);
					executionObservable.fireAfterExecutionError(executionContext, exception);
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
						uiThreadAccess.invokeLater(new Runnable() {
							@Override
							public void run() {
								executionObservable.fireAfterExecutionSuccess(executionContext, result);
							}
						});
					}

					@Override
					public void exception(final Throwable exception) {
						helperCallback.exception(exception);
						uiThreadAccess.invokeLater(new Runnable() {
							@Override
							public void run() {
								executionObservable.fireAfterExecutionError(executionContext, exception);
							}
						});
					}

				};

				executorService.execute(resultCallback, keys, parameter, executionTask);
			}
		}

		List<IBeanDto> createBeanDtos(final List<IBeanProxy<?>> beans) {
			final List<IBeanDto> result = new LinkedList<IBeanDto>();
			for (final IBeanProxy<?> bean : beans) {
				result.add(bean.createCopy());
			}
			return result;
		}

		private IMaybe getParameter(
			final Object parameterProvider,
			final Object defaultParameter,
			final List<IBeanProxy<?>> beans) {
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
