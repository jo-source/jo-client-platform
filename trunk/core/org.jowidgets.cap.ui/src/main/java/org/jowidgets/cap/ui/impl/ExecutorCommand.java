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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.command.EnabledState;
import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IEnabledState;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.controler.IChangeListener;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutableState;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.IExecutorService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanExecptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanModificationStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProcessStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.execution.IExecutor;
import org.jowidgets.cap.ui.api.execution.IParameterProvider;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.tools.controler.ChangeObservable;
import org.jowidgets.util.ValueHolder;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.maybe.Nothing;
import org.jowidgets.util.maybe.Some;

@SuppressWarnings({"rawtypes", "unchecked"})
final class ExecutorCommand extends ChangeObservable implements ICommand, ICommandExecutor, IEnabledChecker {

	//TODO i18n
	private static final IEnabledState IS_IN_PROCESS_STATE = EnabledState.disabled("There is some other execution in process");
	private static final IEnabledState SINGLE_SELECTION_STATE = EnabledState.disabled("There must be selected exactly one record");
	private static final IEnabledState MULTI_SELECTION_STATE = EnabledState.disabled("There must be selected at least one record");
	private static final IEnabledState NO_SELECTION_STATE = EnabledState.disabled("There must not be selected any record");
	private static final IEnabledState UNSAVED_DATA_STATE = EnabledState.disabled("There record has unsaved data");

	private final IBeanListModel<Object> listModel;
	private final List<IExecutableChecker<Object>> executableCheckers;
	private final List<IEnabledChecker> enabledCheckers;
	private final List<Object> parameterProviders;
	private final List<IExecutionInterceptor> executionInterceptors;
	private final BeanExecutionPolicy beanListExecutionPolicy;
	private final BeanSelectionPolicy beanSelectionPolicy;
	private final BeanModificationStatePolicy beanModificationStatePolicy;

	private final Object defaultParameter;
	private final Object executor;
	private final IBeanExecptionConverter beanExceptionConverter;

	private List<IBeanProxy> lastSelection;

	ExecutorCommand(
		final IBeanListModel listModel,
		final BeanExecutionPolicy beanListExecutionPolicy,
		final BeanSelectionPolicy beanSelectionPolicy,
		final BeanModificationStatePolicy beanModificationStatePolicy,
		final List<IEnabledChecker> enabledCheckers,
		final List<IExecutableChecker<Object>> executableCheckers,
		final IBeanExecptionConverter beanExceptionConverter,
		final List<Object> parameterProviders,
		final List<IExecutionInterceptor> executionInterceptors,
		final Object defaultParameter,
		final Object executor) {
		super();
		this.listModel = listModel;
		this.beanListExecutionPolicy = beanListExecutionPolicy;
		this.beanSelectionPolicy = beanSelectionPolicy;
		this.beanModificationStatePolicy = beanModificationStatePolicy;
		this.enabledCheckers = enabledCheckers;
		this.executableCheckers = executableCheckers;
		this.beanExceptionConverter = beanExceptionConverter;
		this.parameterProviders = parameterProviders;
		this.executionInterceptors = executionInterceptors;
		this.defaultParameter = defaultParameter;
		this.executor = executor;
		this.lastSelection = getSelectedBeans();

		final IChangeListener changeListener = new IChangeListener() {
			@Override
			public void changedEvent() {
				fireChangedEvent();
			}
		};

		for (final IEnabledChecker enabledChecker : enabledCheckers) {
			enabledChecker.addChangeListener(changeListener);
		}

		//TODO MG enabled checks must be done better performance
		final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				fireChangedEvent();
			}
		};

		final IBeanModificationStateListener modificationStateListener = new IBeanModificationStateListener<Object>() {
			@Override
			public void modificationStateChanged(final IBeanProxy<Object> bean) {
				fireChangedEvent();
			}
		};

		final IBeanProcessStateListener processStateListener = new IBeanProcessStateListener<Object>() {
			@Override
			public void processStateChanged(final IBeanProxy<Object> bean) {
				fireChangedEvent();
			}
		};

		listModel.addBeanListModelListener(new IBeanListModelListener() {

			@Override
			public void selectionChanged() {

				for (final IBeanProxy bean : lastSelection) {
					bean.removeProcessStateListener(processStateListener);
					bean.removePropertyChangeListener(propertyChangeListener);
					bean.removeModificationStateListener(modificationStateListener);
				}

				final List<IBeanProxy> selectedBeans = getSelectedBeans();

				for (final IBeanProxy bean : selectedBeans) {
					bean.addProcessStateListener(processStateListener);
					bean.addPropertyChangeListener(propertyChangeListener);
					bean.addModificationStateListener(modificationStateListener);
				}

				lastSelection = selectedBeans;

				fireChangedEvent();
			}

			@Override
			public void beansChanged() {}
		});
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
		return null;
	}

	@Override
	public IEnabledState getEnabledState() {
		//TODO MG enabled checks must be done better performance
		if (BeanSelectionPolicy.SINGLE_SELECTION == beanSelectionPolicy && lastSelection.size() != 1) {
			return SINGLE_SELECTION_STATE;
		}
		else if (BeanSelectionPolicy.MULTI_SELECTION == beanSelectionPolicy && lastSelection.size() < 1) {
			return MULTI_SELECTION_STATE;
		}
		else if (BeanSelectionPolicy.NO_SELECTION == beanSelectionPolicy && lastSelection.size() > 0) {
			return NO_SELECTION_STATE;
		}
		for (final IEnabledChecker enabledChecker : enabledCheckers) {
			final IEnabledState result = enabledChecker.getEnabledState();
			if (!result.isEnabled()) {
				return result;
			}
		}
		for (final IBeanProxy bean : lastSelection) {
			if (bean.getExecutionTask() != null) {
				return IS_IN_PROCESS_STATE;
			}
			else if (BeanModificationStatePolicy.NO_MODIFICATION == beanModificationStatePolicy && bean.hasModifications()) {
				return UNSAVED_DATA_STATE;
			}
			for (final IExecutableChecker executableChecker : executableCheckers) {
				final IExecutableState result = executableChecker.getExecutableState(bean.getBean());
				if (!result.isExecutable()) {
					return EnabledState.disabled(result.getReason());
				}
			}
		}

		return EnabledState.ENABLED;
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

	private List<IBeanProxy> getSelectedBeans() {
		final List<IBeanProxy> result = new LinkedList<IBeanProxy>();
		for (final Integer index : listModel.getSelection()) {
			result.add(listModel.getBean(index.intValue()));
		}
		return result;
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
				final IBeanKeyFactory beanKeyFactory = CapUiToolkit.getBeanKeyFactory();
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
