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

package org.jowidgets.cap.ui.impl.command;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.api.command.EnabledState;
import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IEnabledState;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionTask;
import org.jowidgets.cap.common.api.service.IExecutorService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.executor.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.executor.IExecutor;
import org.jowidgets.cap.ui.api.executor.IExecutorJob;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.tools.controler.ChangeObservable;

@SuppressWarnings({"rawtypes", "unchecked"})
final class ExecutorCommand extends ChangeObservable implements ICommand, ICommandExecutor, IEnabledChecker, IExceptionHandler {

	private final IBeanListModel<Object> listModel;
	private final IExceptionHandler exceptionHandler;
	private final List<Object> parameterProviders;
	private final List<IExecutionInterceptor> executionInterceptors;

	private final Object defaultParameter;
	private final Object executor;

	ExecutorCommand(
		final IBeanListModel listModel,
		final IExceptionHandler exceptionHandler,
		final List<Object> parameterProviders,
		final List<IExecutionInterceptor> executionInterceptors,
		final Object defaultParameter,
		final Object executor) {
		super();
		this.listModel = listModel;
		this.exceptionHandler = exceptionHandler;
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
		return this;
	}

	@Override
	public IExceptionHandler getExceptionHandler() {
		return this;
	}

	@Override
	public void handleException(final IExecutionContext executionContext, final Exception exception) throws Exception {
		exceptionHandler.handleException(executionContext, exception);
	}

	@Override
	public IEnabledState getEnabledState() {
		//TODO MG
		return EnabledState.ENABLED;
	}

	@Override
	public void execute(final IExecutionContext executionContext) throws Exception {
		for (final IExecutionInterceptor interceptor : executionInterceptors) {
			if (!interceptor.beforeExecution()) {
				return;
			}
		}

		final List<IBeanProxy> beans = new LinkedList<IBeanProxy>();
		for (final Integer index : listModel.getSelection()) {
			final IBeanProxy<Object> bean = listModel.getBean(index.intValue());
			beans.add(bean);
			bean.setInProcess(true);
		}

		final Thread thread = new Thread(new ExecutorRunnable(beans, executionContext));
		thread.setDaemon(true);
		thread.start();
	}

	private class ExecutorRunnable implements Runnable {

		private final IExecutionContext executionContext;
		private final IUiThreadAccess uiThreadAccess;
		private final List<IBeanProxy> beans;
		private final IExecutionTask executionTask;

		ExecutorRunnable(final List<IBeanProxy> beans, final IExecutionContext executionContext) {
			super();
			this.uiThreadAccess = Toolkit.getUiThreadAccess();
			this.executionTask = CapUiToolkit.getExecutionTaskFactory().create();
			this.beans = beans;
			this.executionContext = executionContext;
		}

		@Override
		public void run() {
			Object parameter = defaultParameter;
			for (final Object parameterProvider : parameterProviders) {
				parameter = getParameter(parameterProvider, parameter, beans);
			}

			final Object executionParameter = parameter;
			if (executor instanceof IExecutor) {
				uiThreadAccess.invokeLater(new Runnable() {
					@Override
					public void run() {
						final IExecutor theExecutor = (IExecutor) executor;
						try {
							theExecutor.execute(executionContext, beans, executionParameter);
						}
						catch (final Exception exception) {
							onExecption(exception);
						}
						afterExecution(null);
					}
				});
			}
			else if (executor instanceof IExecutorService) {
				final IExecutorService executorService = (IExecutorService) executor;
				final IBeanKeyFactory beanKeyFactory = CapUiToolkit.getBeanKeyFactory();
				final List<IBeanKey> keys = beanKeyFactory.createKeys((Collection<? extends IBeanProxy<?>>) beans);
				List<IBeanDto> exeuctionResult = null;
				try {
					exeuctionResult = executorService.execute(keys, executionParameter, executionTask);
					invokeAfterExecutionLater(exeuctionResult);
				}
				catch (final Exception e) {
					invokeOnExceptionLater(e);
				}
				finally {
					invokeAfterExecutionLater(exeuctionResult);
				}
			}
			else if (executor instanceof IExecutorJob) {
				final IExecutorJob executorJob = (IExecutorJob) executor;
				try {
					executorJob.execute(executionContext, beans, parameter, executionTask);
				}
				catch (final Exception e) {
					invokeOnExceptionLater(e);
				}
				finally {
					invokeAfterExecutionLater(null);
				}
			}
		}

		private Object getParameter(final Object parameterProvider, final Object defaultParameter, final List<IBeanProxy> beans) {
			//TODO MG implement getParameter
			return defaultParameter;
		}

		private void invokeOnExceptionLater(final Exception exception) {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					onExecption(exception);
				}
			});
		}

		private void onExecption(final Exception exception) {
			try {
				handleException(executionContext, exception);
			}
			catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}

		private void invokeAfterExecutionLater(final List<IBeanDto> result) {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					afterExecution(result);
				}
			});
		}

		private void afterExecution(final List<IBeanDto> result) {

			for (final IBeanProxy bean : beans) {
				bean.setInProcess(false);
			}

			if (result != null) {
				final Map<Object, IBeanDto> resultMap = new HashMap<Object, IBeanDto>();
				for (final IBeanDto beanDto : result) {
					resultMap.put(beanDto.getId(), beanDto);
				}

				for (final IBeanProxy bean : beans) {
					final IBeanDto updatedBean = resultMap.get(bean.getId());
					if (updatedBean != null) {
						bean.update(updatedBean);
					}
					//TODO MG //else {}
				}
			}

			for (final IExecutionInterceptor interceptor : executionInterceptors) {
				interceptor.afterExecution();
			}

			listModel.fireBeansChanged();

		}

	}

}
