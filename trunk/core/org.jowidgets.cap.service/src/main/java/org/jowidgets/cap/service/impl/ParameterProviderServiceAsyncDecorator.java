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

package org.jowidgets.cap.service.impl;

import java.util.List;
import java.util.concurrent.Executor;

import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.IParameterProviderService;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;

final class ParameterProviderServiceAsyncDecorator implements IDecorator<IParameterProviderService<Object>> {

	private final Executor executor;

	ParameterProviderServiceAsyncDecorator(final Executor executor) {
		this.executor = executor;
	}

	@Override
	public IParameterProviderService<Object> decorate(final IParameterProviderService<Object> original) {
		Assert.paramNotNull(original, "original");
		return new IParameterProviderService<Object>() {
			@Override
			public void getParameter(
				final IResultCallback<Object> result,
				final List<? extends IBeanKey> beanKeys,
				final Object defaultParameter,
				final IExecutionCallback executionCallback) {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							original.getParameter(result, beanKeys, defaultParameter, executionCallback);
						}
						catch (final Exception exception) {
							result.exception(exception);
						}
					}
				});
			}
		};
	}
}
