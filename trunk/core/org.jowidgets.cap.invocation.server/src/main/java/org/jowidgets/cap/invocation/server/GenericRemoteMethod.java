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

package org.jowidgets.cap.invocation.server;

import java.lang.reflect.Method;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.invocation.common.Progress;
import org.jowidgets.cap.invocation.common.RemoteInvocationParameter;
import org.jowidgets.invocation.service.common.api.IInterimRequestCallback;
import org.jowidgets.invocation.service.common.api.IInterimResponseCallback;
import org.jowidgets.invocation.service.common.api.IInvocationCallback;
import org.jowidgets.invocation.service.common.api.IMethodInvocationService;
import org.jowidgets.service.api.ServiceProvider;

final class GenericRemoteMethod implements IMethodInvocationService<Object, Progress, Void, Void, RemoteInvocationParameter> {

	@Override
	public void invoke(
		final IInvocationCallback<Object> invocationCallback,
		final IInterimResponseCallback<Progress> interimResponseCallback,
		final IInterimRequestCallback<Void, Void> interimRequestCallback,
		final RemoteInvocationParameter parameter) {
		try {
			final Object service = ServiceProvider.getService(parameter.getServiceId());
			if (service != null) {
				final Method method = service.getClass().getMethod(parameter.getMethodName(), parameter.getParameterTypes());
				if (method != null) {
					if (method.getReturnType() != void.class) {
						final Object result = method.invoke(service, parameter.getArguments());
						invocationCallback.finished(result);
					}
					else {
						final int resultCallbackIndex = getFirstResultCallbackIndex(parameter.getParameterTypes());
						if (resultCallbackIndex != -1) {
							parameter.getArguments()[resultCallbackIndex] = new IResultCallback<Object>() {

								@Override
								public void finished(final Object result) {
									invocationCallback.finished(result);
								}

								@Override
								public void exception(final Throwable exception) {
									invocationCallback.exeption(exception);
								}

								@Override
								public void timeout() {
									invocationCallback.exeption(new RuntimeException("Timeout exception"));
								}
							};
						}

						final int executionCallbackIndex = getFirstExecutionCallbackIndex(parameter.getParameterTypes());
						if (executionCallbackIndex != -1) {
							parameter.getArguments()[executionCallbackIndex] = new ServerExecutionCallback(
								invocationCallback,
								interimResponseCallback);
						}

						method.invoke(service, parameter.getArguments());
					}
				}
				else {
					throw new IllegalArgumentException("No method found for '"
						+ parameter.getMethodName()
						+ "' with parameters '"
						+ parameter.getParameterTypes()
						+ "'");
				}
			}
			else {
				throw new IllegalArgumentException("No service found for the id '" + parameter.getServiceId() + "'");
			}
		}
		catch (final Exception exception) {
			invocationCallback.exeption(exception);
		}

	}

	private int getFirstResultCallbackIndex(final Class<?>[] paramTypes) {
		if (paramTypes != null) {
			for (int i = 0; i < paramTypes.length; i++) {
				if (IResultCallback.class.isAssignableFrom(paramTypes[i])) {
					return i;
				}
			}
		}
		return -1;
	}

	private int getFirstExecutionCallbackIndex(final Class<?>[] paramTypes) {
		if (paramTypes != null) {
			for (int i = 0; i < paramTypes.length; i++) {
				if (IExecutionCallback.class.isAssignableFrom(paramTypes[i])) {
					return i;
				}
			}
		}
		return -1;
	}
}
