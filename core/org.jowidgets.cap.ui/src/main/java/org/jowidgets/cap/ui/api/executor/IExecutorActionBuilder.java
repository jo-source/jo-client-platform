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

package org.jowidgets.cap.ui.api.executor;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.service.IExecutorService;
import org.jowidgets.cap.common.api.service.IParameterProviderService;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.types.Accelerator;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.service.api.IServiceId;


public interface IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> {

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setText(String text);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setToolTipText(final String toolTipText);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setIcon(IImageConstant icon);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setMnemonic(final Character mnemonic);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setMnemonic(final char mnemonic);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setAccelerator(Accelerator accelerator);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setAccelerator(final char key, final Modifier... modifier);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setDefaultParameter(PARAM_TYPE defaultParameter);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(IParameterProvider<BEAN_TYPE, PARAM_TYPE> parameterProvider);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(
		IParameterProviderJob<BEAN_TYPE, PARAM_TYPE> parameterProviderJob);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(
		IParameterProviderService<PARAM_TYPE> parameterProviderService);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addParameterProvider(
		IServiceId<IParameterProviderService<PARAM_TYPE>> parameterProviderService);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(IExecutor<BEAN_TYPE, PARAM_TYPE> executor);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(IExecutorJob<BEAN_TYPE, PARAM_TYPE> executorJob);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(IExecutorService<PARAM_TYPE> excecuterService);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(IServiceId<IExecutorService<PARAM_TYPE>> excecuterServiceId);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addEnabledChecker(IEnabledChecker enabledChecker);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> addEnabledChecker(IExecutableChecker<BEAN_TYPE> executableChecker);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExecutionCallback(IExecutionResultCallback<BEAN_TYPE> executionCallback);

	IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> setExceptionHandler(IExceptionHandler exceptionHandler);

	IAction build();

}
