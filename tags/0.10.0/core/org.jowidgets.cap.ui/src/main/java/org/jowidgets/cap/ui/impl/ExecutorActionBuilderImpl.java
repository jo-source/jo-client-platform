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
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IInputDialog;
import org.jowidgets.api.widgets.IWindow;
import org.jowidgets.api.widgets.blueprint.IInputDialogBluePrint;
import org.jowidgets.api.widgets.content.IInputContentCreator;
import org.jowidgets.api.widgets.descriptor.IInputDialogDescriptor;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.service.IExecutorService;
import org.jowidgets.cap.common.api.service.IParameterProviderService;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.command.IExecutorActionBuilder;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.execution.IExecutor;
import org.jowidgets.cap.ui.api.execution.IParameterProvider;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.service.tools.ServiceId;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IProvider;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.maybe.Nothing;
import org.jowidgets.util.maybe.Some;

final class ExecutorActionBuilderImpl<BEAN_TYPE, PARAM_TYPE> extends
		AbstractCapActionBuilderImpl<IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE>> implements
		IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> {

	private final IBeanListModel<BEAN_TYPE> listModel;
	private final List<IExecutableChecker<Object>> executableCheckers;
	private final List<IEnabledChecker> enabledCheckers;
	private final List<Object> parameterProviders;
	private final List<IExecutionInterceptor<List<IBeanDto>>> executionInterceptors;

	private PARAM_TYPE defaultParameter;
	private Object executor;
	private IBeanExceptionConverter exceptionConverter;
	private BeanExecutionPolicy beanListExecutionPolicy;
	private BeanSelectionPolicy beanSelectionPolicy;
	private BeanModificationStatePolicy beanModificationStatePolicy;
	private BeanMessageStatePolicy beanMessageStatePolicy;

	ExecutorActionBuilderImpl(final IBeanListModel<BEAN_TYPE> listModel) {
		Assert.paramNotNull(listModel, "listModel");
		this.listModel = listModel;
		this.executableCheckers = new LinkedList<IExecutableChecker<Object>>();
		this.enabledCheckers = new LinkedList<IEnabledChecker>();
		this.parameterProviders = new LinkedList<Object>();
		this.executionInterceptors = new LinkedList<IExecutionInterceptor<List<IBeanDto>>>();

		this.beanSelectionPolicy = BeanSelectionPolicy.SINGLE_SELECTION;
		this.beanModificationStatePolicy = BeanModificationStatePolicy.NO_MODIFICATION;
		this.beanMessageStatePolicy = BeanMessageStatePolicy.NO_WARNING_OR_ERROR;
		this.beanListExecutionPolicy = BeanExecutionPolicy.BATCH;

		this.exceptionConverter = new DefaultBeanExceptionConverter();
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setDefaultParameter(final PARAM_TYPE defaultParameter) {
		checkExhausted();
		this.defaultParameter = defaultParameter;
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(
		final IParameterProvider<BEAN_TYPE, PARAM_TYPE> parameterProvider) {
		checkExhausted();
		Assert.paramNotNull(parameterProvider, "parameterProvider");
		parameterProviders.add(parameterProvider);
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(
		final IParameterProviderService<PARAM_TYPE> parameterProviderService) {
		checkExhausted();
		Assert.paramNotNull(parameterProviderService, "parameterProviderService");
		parameterProviders.add(parameterProviderService);
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(
		final IServiceId<IParameterProviderService<PARAM_TYPE>> serviceId) {
		checkExhausted();
		Assert.paramNotNull(serviceId, "serviceId");
		addParameterProvider(ServiceProvider.getService(serviceId));
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(
		final IInputContentCreator<PARAM_TYPE> contentCreator) {
		checkExhausted();
		Assert.paramNotNull(contentCreator, "contentCreator");
		return addParameterProvider(new IProvider<IInputDialogDescriptor<PARAM_TYPE>>() {
			@Override
			public IInputDialogDescriptor<PARAM_TYPE> get() {
				final IInputDialogBluePrint<PARAM_TYPE> bp = Toolkit.getBluePrintFactory().inputDialog(contentCreator);
				return bp.setTitle(getText()).setIcon(getIcon()).setCloseable(false);
			}
		});
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(
		final IInputDialogDescriptor<PARAM_TYPE> inputDialogDescriptor) {
		checkExhausted();
		Assert.paramNotNull(inputDialogDescriptor, "inputDialogDescriptor");
		addParameterProvider(new IProvider<IInputDialogDescriptor<PARAM_TYPE>>() {
			@Override
			public IInputDialogDescriptor<PARAM_TYPE> get() {
				return inputDialogDescriptor;
			}
		});
		return this;
	}

	private IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(
		final IProvider<IInputDialogDescriptor<PARAM_TYPE>> inputDialogDescriptorProvider) {
		addParameterProvider(new IParameterProvider<BEAN_TYPE, PARAM_TYPE>() {
			@Override
			public IMaybe<PARAM_TYPE> getParameter(
				final IExecutionContext executionContext,
				final List<IBeanProxy<BEAN_TYPE>> beans,
				final PARAM_TYPE defaultParameter) throws Exception {

				final IWindow activeWindow = Toolkit.getActiveWindow();
				final IInputDialog<PARAM_TYPE> inputDialog;
				if (activeWindow != null) {
					inputDialog = activeWindow.createChildWindow(inputDialogDescriptorProvider.get());
				}
				else {
					inputDialog = Toolkit.getWidgetFactory().create(inputDialogDescriptorProvider.get());
				}

				inputDialog.setValue(defaultParameter);
				inputDialog.setVisible(true);

				IMaybe<PARAM_TYPE> result;
				if (inputDialog.isOkPressed()) {
					result = new Some<PARAM_TYPE>(inputDialog.getValue());
				}
				else {
					result = Nothing.getInstance();
				}

				inputDialog.dispose();
				return result;
			}
		});
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(final IExecutor<BEAN_TYPE, PARAM_TYPE> executor) {
		checkExhausted();
		Assert.paramNotNull(executor, "executor");
		this.executor = executor;
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(final IExecutorService<PARAM_TYPE> excecuterService) {
		checkExhausted();
		Assert.paramNotNull(excecuterService, "excecuterService");
		this.executor = excecuterService;
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(
		final IServiceId<IExecutorService<PARAM_TYPE>> excecuterServiceId) {
		checkExhausted();
		Assert.paramNotNull(excecuterServiceId, "excecuterServiceId");
		setExecutor(ServiceProvider.getService(excecuterServiceId));
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(final String excecuterServiceId) {
		checkExhausted();
		Assert.paramNotNull(excecuterServiceId, "excecuterServiceId");
		setExecutor(new ServiceId<IExecutorService<PARAM_TYPE>>(excecuterServiceId, IExecutorService.class));
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutionPolicy(final BeanExecutionPolicy policy) {
		checkExhausted();
		Assert.paramNotNull(policy, "policy");
		this.beanListExecutionPolicy = policy;
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setSelectionPolicy(final BeanSelectionPolicy policy) {
		checkExhausted();
		Assert.paramNotNull(policy, "policy");
		this.beanSelectionPolicy = policy;
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setModificationPolicy(final BeanModificationStatePolicy policy) {
		checkExhausted();
		Assert.paramNotNull(policy, "policy");
		this.beanModificationStatePolicy = policy;
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setMessageStatePolicy(final BeanMessageStatePolicy policy) {
		checkExhausted();
		Assert.paramNotNull(policy, "policy");
		this.beanMessageStatePolicy = policy;
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addEnabledChecker(final IEnabledChecker enabledChecker) {
		checkExhausted();
		Assert.paramNotNull(enabledChecker, "enabledChecker");
		enabledCheckers.add(enabledChecker);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addExecutableChecker(
		final IExecutableChecker<BEAN_TYPE> executableChecker) {
		checkExhausted();
		Assert.paramNotNull(executableChecker, "executableChecker");
		executableCheckers.add((IExecutableChecker<Object>) executableChecker);
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addExecutionInterceptor(
		final IExecutionInterceptor<List<IBeanDto>> interceptor) {
		checkExhausted();
		Assert.paramNotNull(interceptor, "interceptor");
		executionInterceptors.add(interceptor);
		return this;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExceptionConverter(final IBeanExceptionConverter exceptionConverter) {
		checkExhausted();
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");
		this.exceptionConverter = exceptionConverter;
		return this;
	}

	@Override
	public IAction doBuild() {
		final ExecutorCommand command = new ExecutorCommand(
			listModel,
			beanListExecutionPolicy,
			beanSelectionPolicy,
			beanModificationStatePolicy,
			beanMessageStatePolicy,
			enabledCheckers,
			executableCheckers,
			exceptionConverter,
			parameterProviders,
			executionInterceptors,
			defaultParameter,
			executor);
		final IActionBuilder builder = getBuilder();
		builder.setCommand((ICommand) command);
		return builder.build();
	}
}
