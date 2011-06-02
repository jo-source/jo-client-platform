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
import java.util.Collection;
import java.util.Collections;
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
import org.jowidgets.api.controler.IChangeListener;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.types.QuestionResult;
import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutableState;
import org.jowidgets.cap.common.api.execution.IExecutionTask;
import org.jowidgets.cap.common.api.execution.IExecutionTaskListener;
import org.jowidgets.cap.common.api.execution.UserQuestionResult;
import org.jowidgets.cap.common.api.service.IExecutorService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IProcessStateListener;
import org.jowidgets.cap.ui.api.executor.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.executor.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.executor.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.executor.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.executor.IExecutor;
import org.jowidgets.cap.ui.api.executor.IExecutorJob;
import org.jowidgets.cap.ui.api.executor.IParameterProvider;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.model.IModificationStateListener;
import org.jowidgets.tools.controler.ChangeObservable;
import org.jowidgets.util.ValueHolder;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.maybe.Nothing;
import org.jowidgets.util.maybe.Some;

@SuppressWarnings({"rawtypes", "unchecked"})
final class ExecutorCommand extends ChangeObservable implements ICommand, ICommandExecutor, IEnabledChecker, IExceptionHandler {

	//TODO i18n
	private static final IEnabledState IS_IN_PROCESS_STATE = EnabledState.disabled("There is some other execution in process");
	private static final IEnabledState SINGLE_SELECTION_STATE = EnabledState.disabled("There must be selected exactly one record");
	private static final IEnabledState MULTI_SELECTION_STATE = EnabledState.disabled("There must be selected at least one record");
	private static final IEnabledState NO_SELECTION_STATE = EnabledState.disabled("There must not be selected any record");
	private static final IEnabledState UNSAVED_DATA_STATE = EnabledState.disabled("There record has unsaved data");

	private final IBeanListModel<Object> listModel;
	private final List<IExecutableChecker<Object>> executableCheckers;
	private final List<IEnabledChecker> enabledCheckers;
	private final IExceptionHandler exceptionHandler;
	private final List<Object> parameterProviders;
	private final List<IExecutionInterceptor> executionInterceptors;
	private final BeanExecutionPolicy beanListExecutionPolicy;
	private final BeanSelectionPolicy beanSelectionPolicy;
	private final BeanModificationStatePolicy beanModificationStatePolicy;

	private final Object defaultParameter;
	private final Object executor;

	private List<IBeanProxy> lastSelection;

	ExecutorCommand(
		final IBeanListModel listModel,
		final BeanExecutionPolicy beanListExecutionPolicy,
		final BeanSelectionPolicy beanSelectionPolicy,
		final BeanModificationStatePolicy beanModificationStatePolicy,
		final List<IEnabledChecker> enabledCheckers,
		final List<IExecutableChecker<Object>> executableCheckers,
		final IExceptionHandler exceptionHandler,
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
		this.exceptionHandler = exceptionHandler;
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

		final IModificationStateListener modificationStateListener = new IModificationStateListener() {
			@Override
			public void modificationStateChanged() {
				fireChangedEvent();
			}
		};

		final IProcessStateListener processStateListener = new IProcessStateListener() {
			@Override
			public void processStateChanged() {
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
		return this;
	}

	@Override
	public void handleException(final IExecutionContext executionContext, final Exception exception) throws Exception {
		exceptionHandler.handleException(executionContext, exception);
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

		if (BeanExecutionPolicy.SERIAL == beanListExecutionPolicy) {
			final IExecutionTask executionTask = createExecutionTask();
			for (final IBeanProxy bean : beans) {
				bean.setExecutionTask(executionTask);
			}
			listModel.fireBeansChanged();
			final Thread thread = new Thread(new ExecutorRunnable(beans, executionContext, executionTask));
			thread.setDaemon(true);
			thread.start();
		}
		else {
			for (final IBeanProxy bean : beans) {
				final IExecutionTask executionTask = createExecutionTask();
				bean.setExecutionTask(executionTask);
				final Thread thread = new Thread(new ExecutorRunnable(
					Collections.singletonList(bean),
					executionContext,
					executionTask));
				thread.setDaemon(true);
				thread.start();
				listModel.fireBeansChanged();
			}
		}
	}

	private IExecutionTask createExecutionTask() {
		final IUiThreadAccess uiThreadAccess = Toolkit.getUiThreadAccess();
		final IExecutionTask executionTask = CapCommonToolkit.executionTaskFactory().create();

		//TODO MG remove this later
		executionTask.addExecutionTaskListener(new IExecutionTaskListener() {

			@Override
			public void worked() {
				//CHECKSTYLE:OFF
				System.out.println("WORKED " + executionTask.getWorked());
				//CHECKSTYLE:ON
			}

			@Override
			public void userQuestionAsked() {
				final ValueHolder<QuestionResult> resultHolder = new ValueHolder<QuestionResult>();
				try {
					uiThreadAccess.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							final QuestionResult result = Toolkit.getQuestionPane().askYesNoQuestion(
									executionTask.getUserQuestion());
							resultHolder.set(result);
						}
					});
				}
				catch (final InterruptedException e) {
					executionTask.setQuestionResult(UserQuestionResult.NO);
				}

				if (QuestionResult.YES == resultHolder.get()) {
					executionTask.setQuestionResult(UserQuestionResult.YES);
				}
				else {
					executionTask.setQuestionResult(UserQuestionResult.NO);
				}
			}

			@Override
			public void totalStepCountChanged() {}

			@Override
			public void subExecutionAdded(final IExecutionTask executionTask) {}

			@Override
			public void finished() {
				//CHECKSTYLE:OFF
				System.out.println("FINISHED " + executionTask.isFinshed());
				//CHECKSTYLE:ON
			}

			@Override
			public void descriptionChanged() {
				//CHECKSTYLE:OFF
				System.out.println("DESCRIPTION CHANGED " + executionTask.getDescription());
				//CHECKSTYLE:ON
			}
		});

		return executionTask;
	}

	private List<IBeanProxy> getSelectedBeans() {
		final List<IBeanProxy> result = new LinkedList<IBeanProxy>();
		for (final Integer index : listModel.getSelection()) {
			result.add(listModel.getBean(index.intValue()));
		}
		return result;
	}

	private class ExecutorRunnable implements Runnable {

		private final IExecutionContext executionContext;
		private final IUiThreadAccess uiThreadAccess;
		private final List<IBeanProxy> beans;
		private final IExecutionTask executionTask;

		ExecutorRunnable(
			final List<IBeanProxy> beans,
			final IExecutionContext executionContext,
			final IExecutionTask executionTask) {
			super();
			this.uiThreadAccess = Toolkit.getUiThreadAccess();

			this.beans = beans;
			this.executionContext = executionContext;
			this.executionTask = executionTask;
		}

		@Override
		public void run() {
			Object parameter = defaultParameter;
			for (final Object parameterProvider : parameterProviders) {
				final IMaybe paramResult = getParameter(parameterProvider, parameter, beans);
				if (paramResult.isNothing()) {
					invokeAfterExecutionLater(null);
					return;
				}
				else {
					parameter = paramResult.getValue();
				}
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
				final List<IBeanKey> keys = beanKeyFactory.createKeys((Collection) beans);
				List<IBeanDto> executionResult = null;
				try {
					executionResult = executorService.execute(keys, executionParameter, executionTask);
					invokeAfterExecutionLater(executionResult);
				}
				catch (final Exception e) {
					invokeOnExceptionLater(e);
				}
				finally {
					if (!executionTask.isCanceled()) {
						executionTask.dispose();
					}
					invokeAfterExecutionLater(executionResult);
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
					if (!executionTask.isCanceled()) {
						executionTask.dispose();
					}
					invokeAfterExecutionLater(null);
				}
			}
		}

		private IMaybe getParameter(final Object parameterProvider, final Object defaultParameter, final List<IBeanProxy> beans) {
			if (parameterProvider instanceof IParameterProvider) {
				final IParameterProvider theParameterProvider = (IParameterProvider) parameterProvider;
				try {
					final ValueHolder<IMaybe> result = new ValueHolder<IMaybe>(Nothing.getInstance());
					uiThreadAccess.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							try {
								result.set(theParameterProvider.getParameter(executionContext, beans, defaultParameter));
							}
							catch (final Exception e) {
								onExecption(e);
							}
						}
					});
					return result.get();
				}
				catch (final InterruptedException e) {
					invokeOnExceptionLater(e);
				}
			}
			//TODO MG else if IParameterProviderService or IParameterProviderJob
			return new Some(defaultParameter);
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

			for (final IBeanProxy bean : beans) {
				bean.setExecutionTask(null);
			}

			for (final IExecutionInterceptor interceptor : executionInterceptors) {
				interceptor.afterExecution(executionContext);
			}

			listModel.fireBeansChanged();

		}

	}

}
