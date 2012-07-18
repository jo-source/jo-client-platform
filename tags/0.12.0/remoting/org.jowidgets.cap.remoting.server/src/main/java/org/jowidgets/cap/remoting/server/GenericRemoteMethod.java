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

package org.jowidgets.cap.remoting.server;

import java.lang.reflect.Method;
import java.util.concurrent.ScheduledExecutorService;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.execution.UserQuestionResult;
import org.jowidgets.cap.remoting.common.Progress;
import org.jowidgets.cap.remoting.common.RemoteInvocationParameter;
import org.jowidgets.invocation.service.common.api.IInterimRequestCallback;
import org.jowidgets.invocation.service.common.api.IInterimResponseCallback;
import org.jowidgets.invocation.service.common.api.IInvocationCallback;
import org.jowidgets.invocation.service.common.api.IMethodInvocationService;
import org.jowidgets.service.api.ServiceProvider;

final class GenericRemoteMethod implements
		IMethodInvocationService<Object, Progress, String, UserQuestionResult, RemoteInvocationParameter> {

	private final long progressDelay;
	private final ScheduledExecutorService scheduledExecutorService;

	GenericRemoteMethod(final ScheduledExecutorService scheduledExecutorService, final long progressDelay) {
		this.scheduledExecutorService = scheduledExecutorService;
		this.progressDelay = progressDelay;
	}

	@Override
	public void invoke(
		final IInvocationCallback<Object> invocationCallback,
		final IInterimResponseCallback<Progress> interimResponseCallback,
		final IInterimRequestCallback<String, UserQuestionResult> interimRequestCallback,
		final RemoteInvocationParameter parameter) {
		try {
			final Object service = ServiceProvider.getService(parameter.getServiceId());
			if (service != null) {
				final Method method = service.getClass().getMethod(parameter.getMethodName(), parameter.getParameterTypes());
				if (method != null) {
					invokeMethodOnService(
							service,
							method,
							invocationCallback,
							interimResponseCallback,
							interimRequestCallback,
							parameter);
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

	public void invokeMethodOnService(
		final Object service,
		final Method method,
		final IInvocationCallback<Object> invocationCallback,
		final IInterimResponseCallback<Progress> interimResponseCallback,
		final IInterimRequestCallback<String, UserQuestionResult> interimRequestCallback,
		final RemoteInvocationParameter parameter) throws Exception {

		final Class<?>[] parameterTypes = parameter.getParameterTypes();
		final Object[] arguments = parameter.getArguments();

		final int executionCallbackIndex = getFirstMatchingIndex(IExecutionCallback.class, parameterTypes);
		if (executionCallbackIndex != -1) {
			arguments[executionCallbackIndex] = new ServerExecutionCallback(
				scheduledExecutorService,
				progressDelay,
				invocationCallback,
				interimResponseCallback,
				interimRequestCallback);
		}

		final int resultCallbackIndex = getFirstMatchingIndex(IResultCallback.class, parameterTypes);
		if (resultCallbackIndex == -1) {
			final Object result = method.invoke(service, arguments);
			invocationCallback.finished(result);
		}
		else {
			arguments[resultCallbackIndex] = new ServerResultCallback(invocationCallback);
			method.invoke(service, arguments);
		}
	}

	private int getFirstMatchingIndex(final Class<?> interfaceType, final Class<?>[] paramTypes) {
		if (paramTypes != null) {
			for (int i = 0; i < paramTypes.length; i++) {
				if (interfaceType.isAssignableFrom(paramTypes[i])) {
					return i;
				}
			}
		}
		return -1;
	}

}
