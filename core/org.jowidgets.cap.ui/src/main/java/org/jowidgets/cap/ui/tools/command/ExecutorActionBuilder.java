/*
 * Copyright (c) 2012, grossmann
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

package org.jowidgets.cap.ui.tools.command;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.widgets.content.IInputContentCreator;
import org.jowidgets.api.widgets.descriptor.IInputDialogDescriptor;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.service.IExecutorService;
import org.jowidgets.cap.common.api.service.IParameterProviderService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.command.IExecutorActionBuilder;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.execution.IExecutor;
import org.jowidgets.cap.ui.api.execution.IParameterProvider;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.types.Accelerator;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.service.api.IServiceId;

public class ExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> implements IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> {

	private final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> original;

	public ExecutorActionBuilder(final IBeanListModel<BEAN_TYPE> model) {
		this.original = CapUiToolkit.actionFactory().executorActionBuilder(model);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setText(final String text) {
		return original.setText(text);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setToolTipText(final String toolTipText) {
		return original.setToolTipText(toolTipText);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setIcon(final IImageConstant icon) {
		return original.setIcon(icon);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setMnemonic(final Character mnemonic) {
		return original.setMnemonic(mnemonic);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setMnemonic(final char mnemonic) {
		return original.setMnemonic(mnemonic);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setAccelerator(final Accelerator accelerator) {
		return original.setAccelerator(accelerator);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setAccelerator(final char key, final Modifier... modifier) {
		return original.setAccelerator(key, modifier);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setDefaultParameter(final PARAM_TYPE defaultParameter) {
		return original.setDefaultParameter(defaultParameter);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(
		final IInputContentCreator<PARAM_TYPE> inputContentCreator) {
		return original.addParameterProvider(inputContentCreator);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(
		final IInputDialogDescriptor<PARAM_TYPE> inputDialogDescriptor) {
		return original.addParameterProvider(inputDialogDescriptor);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(
		final IParameterProvider<BEAN_TYPE, PARAM_TYPE> parameterProvider) {
		return original.addParameterProvider(parameterProvider);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(
		final IParameterProviderService<PARAM_TYPE> parameterProviderService) {
		return original.addParameterProvider(parameterProviderService);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(
		final IServiceId<IParameterProviderService<PARAM_TYPE>> parameterProviderService) {
		return original.addParameterProvider(parameterProviderService);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(final IExecutor<BEAN_TYPE, PARAM_TYPE> executor) {
		return original.setExecutor(executor);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(final IExecutorService<PARAM_TYPE> excecuterService) {
		return original.setExecutor(excecuterService);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(
		final IServiceId<IExecutorService<PARAM_TYPE>> excecuterServiceId) {
		return original.setExecutor(excecuterServiceId);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(final String excecuterServiceId) {
		return original.setExecutor(excecuterServiceId);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutionPolicy(final BeanExecutionPolicy policy) {
		return original.setExecutionPolicy(policy);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setSelectionPolicy(final BeanSelectionPolicy policy) {
		return original.setSelectionPolicy(policy);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setModificationPolicy(final BeanModificationStatePolicy policy) {
		return original.setModificationPolicy(policy);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setMessageStatePolicy(final BeanMessageStatePolicy policy) {
		return original.setMessageStatePolicy(policy);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addEnabledChecker(final IEnabledChecker enabledChecker) {
		return original.addEnabledChecker(enabledChecker);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addExecutableChecker(
		final IExecutableChecker<BEAN_TYPE> executableChecker) {
		return original.addExecutableChecker(executableChecker);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addExecutionInterceptor(final IExecutionInterceptor interceptor) {
		return original.addExecutionInterceptor(interceptor);
	}

	@Override
	public final IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExceptionConverter(
		final IBeanExceptionConverter exceptionConverter) {
		return original.setExceptionConverter(exceptionConverter);
	}

	@Override
	public final IAction build() {
		return original.build();
	}

}
