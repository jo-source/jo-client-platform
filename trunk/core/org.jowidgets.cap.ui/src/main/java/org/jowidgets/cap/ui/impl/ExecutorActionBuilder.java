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

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.exception.ServiceException;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.service.IExecutorService;
import org.jowidgets.cap.common.api.service.IParameterProviderService;
import org.jowidgets.cap.ui.api.command.IExecutorActionBuilder;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.execution.IExecutor;
import org.jowidgets.cap.ui.api.execution.IExecutorJob;
import org.jowidgets.cap.ui.api.execution.IParameterProvider;
import org.jowidgets.cap.ui.api.execution.IParameterProviderJob;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.types.Accelerator;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.builder.AbstractSingleUseBuilder;

final class ExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> extends AbstractSingleUseBuilder<IAction> implements
		IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> {

	private final IBeanListModel<BEAN_TYPE> listModel;
	private final IActionBuilder builder;
	private final List<IExecutableChecker<Object>> executableCheckers;
	private final List<IEnabledChecker> enabledCheckers;
	private final List<Object> parameterProviders;
	private final List<IExecutionInterceptor> executionInterceptors;

	private PARAM_TYPE defaultParameter;
	private Object executor;
	private IExceptionHandler exceptionHandler;
	private BeanExecutionPolicy beanListExecutionPolicy;
	private BeanSelectionPolicy beanSelectionPolicy;
	private BeanModificationStatePolicy beanModificationStatePolicy;

	ExecutorActionBuilder(final IBeanListModel<BEAN_TYPE> listModel) {
		Assert.paramNotNull(listModel, "listModel");
		this.listModel = listModel;
		this.builder = Toolkit.getActionBuilderFactory().create();
		this.executableCheckers = new LinkedList<IExecutableChecker<Object>>();
		this.enabledCheckers = new LinkedList<IEnabledChecker>();
		this.parameterProviders = new LinkedList<Object>();
		this.executionInterceptors = new LinkedList<IExecutionInterceptor>();

		this.beanSelectionPolicy = BeanSelectionPolicy.SINGLE_SELECTION;
		this.beanModificationStatePolicy = BeanModificationStatePolicy.NO_MODIFICATION;
		this.beanListExecutionPolicy = BeanExecutionPolicy.PARALLEL;

		//TODO MG add better default exception handler
		this.exceptionHandler = new IExceptionHandler() {
			@Override
			public void handleException(final IExecutionContext executionContext, final Exception exception) throws Exception {
				final IAction action = executionContext.getAction();
				if (exception instanceof ServiceException) {
					Toolkit.getMessagePane().showError(
							action.getText(),
							action.getIcon(),
							((ServiceException) exception).getUserMessage());
				}
				else {
					Toolkit.getMessagePane().showError(action.getText(), action.getIcon(), exception.getMessage());
					//CHECKSTYLE:OFF
					exception.printStackTrace();
					//CHECKSTYLE:ON
				}
			}
		};
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setText(final String text) {
		builder.setText(text);
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setToolTipText(final String toolTipText) {
		builder.setToolTipText(toolTipText);
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setIcon(final IImageConstant icon) {
		builder.setIcon(icon);
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setMnemonic(final Character mnemonic) {
		builder.setMnemonic(mnemonic);
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setMnemonic(final char mnemonic) {
		builder.setMnemonic(mnemonic);
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setAccelerator(final Accelerator accelerator) {
		builder.setAccelerator(accelerator);
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setAccelerator(final char key, final Modifier... modifier) {
		builder.setAccelerator(key, modifier);
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setDefaultParameter(final PARAM_TYPE defaultParameter) {
		this.defaultParameter = defaultParameter;
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(
		final IParameterProvider<BEAN_TYPE, PARAM_TYPE> parameterProvider) {
		Assert.paramNotNull(parameterProvider, "parameterProvider");
		parameterProviders.add(parameterProvider);
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(
		final IParameterProviderJob<BEAN_TYPE, PARAM_TYPE> parameterProviderJob) {
		Assert.paramNotNull(parameterProviderJob, "parameterProviderJob");
		parameterProviders.add(parameterProviderJob);
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(
		final IParameterProviderService<PARAM_TYPE> parameterProviderService) {
		Assert.paramNotNull(parameterProviderService, "parameterProviderService");
		parameterProviders.add(parameterProviderService);
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(
		final IServiceId<IParameterProviderService<PARAM_TYPE>> serviceId) {
		Assert.paramNotNull(serviceId, "serviceId");
		addParameterProvider(ServiceProvider.getService(serviceId));
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(final IExecutor<BEAN_TYPE, PARAM_TYPE> executor) {
		Assert.paramNotNull(executor, "executor");
		this.executor = executor;
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(final IExecutorJob<BEAN_TYPE, PARAM_TYPE> executorJob) {
		Assert.paramNotNull(executorJob, "executorJob");
		this.executor = executorJob;
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(final IExecutorService<PARAM_TYPE> excecuterService) {
		Assert.paramNotNull(excecuterService, "excecuterService");
		this.executor = excecuterService;
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(
		final IServiceId<IExecutorService<PARAM_TYPE>> excecuterServiceId) {
		Assert.paramNotNull(excecuterServiceId, "excecuterServiceId");
		setExecutor(ServiceProvider.getService(excecuterServiceId));
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutionPolicy(final BeanExecutionPolicy policy) {
		Assert.paramNotNull(policy, "policy");
		this.beanListExecutionPolicy = policy;
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setSelectionPolicy(final BeanSelectionPolicy policy) {
		Assert.paramNotNull(policy, "policy");
		this.beanSelectionPolicy = policy;
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setModificationPolicy(final BeanModificationStatePolicy policy) {
		Assert.paramNotNull(policy, "policy");
		this.beanModificationStatePolicy = policy;
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addEnabledChecker(final IEnabledChecker enabledChecker) {
		enabledCheckers.add(enabledChecker);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addExecutableChecker(
		final IExecutableChecker<BEAN_TYPE> executableChecker) {
		executableCheckers.add((IExecutableChecker<Object>) executableChecker);
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addExecutionInterceptor(final IExecutionInterceptor interceptor) {
		executionInterceptors.add(interceptor);
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExceptionHandler(final IExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
		return this;
	}

	@Override
	public IAction doBuild() {
		final ExecutorCommand command = new ExecutorCommand(
			listModel,
			beanListExecutionPolicy,
			beanSelectionPolicy,
			beanModificationStatePolicy,
			enabledCheckers,
			executableCheckers,
			exceptionHandler,
			parameterProviders,
			executionInterceptors,
			defaultParameter,
			executor);
		builder.setCommand((ICommand) command);
		return builder.build();
	}
}
