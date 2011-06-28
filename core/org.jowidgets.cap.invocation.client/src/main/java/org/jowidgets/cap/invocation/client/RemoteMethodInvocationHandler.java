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

package org.jowidgets.cap.invocation.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.execution.UserQuestionResult;
import org.jowidgets.cap.invocation.common.CapInvocationMethodNames;
import org.jowidgets.cap.invocation.common.Progress;
import org.jowidgets.cap.invocation.common.RemoteInvocationParameter;
import org.jowidgets.invocation.service.client.api.IInvocationServiceClient;
import org.jowidgets.invocation.service.client.api.InvocationServiceClientToolkit;
import org.jowidgets.invocation.service.common.api.IInterimRequestCallback;
import org.jowidgets.invocation.service.common.api.IInterimResponseCallback;
import org.jowidgets.invocation.service.common.api.IInvocationCallback;
import org.jowidgets.invocation.service.common.api.IMethodInvocationService;
import org.jowidgets.service.tools.ServiceId;
import org.jowidgets.util.Assert;

final class RemoteMethodInvocationHandler implements InvocationHandler {

	private final ServiceId<?> serviceId;
	private final IInvocationServiceClient invocationServiceClient;

	RemoteMethodInvocationHandler(final ServiceId<?> serviceId) {
		Assert.paramNotNull(serviceId, "serviceId");
		this.serviceId = serviceId;
		this.invocationServiceClient = InvocationServiceClientToolkit.getClient();
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		try {
			return doInvoke(proxy, method, args);
		}
		catch (final Exception e) {
			throw new RuntimeException("Error while invoking method '" + method.getName() + "' on '" + proxy + "'", e);
		}
	}

	private Object doInvoke(final Object proxy, final Method method, final Object[] args) {
		if ("toString".equals(method.getName())) {
			return "Proxy for service: " + serviceId;
		}
		if ("equals".equals(method.getName())) {
			if (args == null || args.length != 1) {
				throw new IllegalStateException("Method equals must have exactly one argument");
			}
			if (args[0] instanceof RemoteMethodInvocationHandler) {
				return serviceId.equals(((RemoteMethodInvocationHandler) args[0]).serviceId);
			}
			return false;
		}
		if ("hashCode".equals(method.getName())) {
			return serviceId.hashCode();
		}
		else {
			return invokeRemoteMethod(proxy, method, args);
		}
	}

	private Object invokeRemoteMethod(final Object proxy, final Method method, final Object[] args) {
		final Class<?>[] parameterTypes = method.getParameterTypes();

		final IResultCallback<Object> resultCallback = getResultCallback(parameterTypes, args);
		final IExecutionCallback executionCallback = getExecutionCallback(parameterTypes, args);

		final IInterimResponseCallback<Progress> interimResponseCallback;
		final IInterimRequestCallback<String, UserQuestionResult> interimRequestCallback;
		if (executionCallback != null) {
			interimResponseCallback = new ProgressResponseCallback(executionCallback);
			interimRequestCallback = new UserQuestionRequestCallback(executionCallback);
		}
		else {
			interimResponseCallback = new DummyProgressResponseCallback();
			interimRequestCallback = new DummyUserQuestionRequestCallback(resultCallback);
		}

		final RemoteInvocationParameter parameter = new RemoteInvocationParameter(
			serviceId,
			method.getName(),
			parameterTypes,
			getFilteredArgs(args));

		if (resultCallback != null) {
			invokeAsync(resultCallback, interimResponseCallback, interimRequestCallback, parameter, executionCallback);
			return null;
		}
		else {
			return invokeSync(interimResponseCallback, interimRequestCallback, parameter, executionCallback);
		}
	}

	private Object invokeSync(
		final IInterimResponseCallback<Progress> interimResponseCallback,
		final IInterimRequestCallback<String, UserQuestionResult> interimRequestCallback,
		final RemoteInvocationParameter parameter,
		final IExecutionCallback executionCallback) {

		final SyncInvocationCallback<Object> syncInvocationCallback = new SyncInvocationCallback<Object>(executionCallback);
		invokeMethod(syncInvocationCallback, interimResponseCallback, interimRequestCallback, parameter);
		return syncInvocationCallback.getResultSynchronious();
	}

	private void invokeAsync(
		final IResultCallback<Object> resultCallback,
		final IInterimResponseCallback<Progress> interimResponseCallback,
		final IInterimRequestCallback<String, UserQuestionResult> interimRequestCallback,
		final RemoteInvocationParameter parameter,
		final IExecutionCallback executionCallback) {

		final IInvocationCallback<Object> invocationCallback = new InvocationCallback<Object>(resultCallback, executionCallback);
		invokeMethod(invocationCallback, interimResponseCallback, interimRequestCallback, parameter);
	}

	private void invokeMethod(
		final IInvocationCallback<Object> invocationCallback,
		final IInterimResponseCallback<Progress> interimResponseCallback,
		final IInterimRequestCallback<String, UserQuestionResult> interimRequestCallback,
		final RemoteInvocationParameter parameter) {

		final IMethodInvocationService<Object, Progress, String, UserQuestionResult, RemoteInvocationParameter> methodService;
		methodService = invocationServiceClient.getMethodService(CapInvocationMethodNames.GENERIC_REMOTE_METHOD_NAME);
		methodService.invoke(invocationCallback, interimResponseCallback, interimRequestCallback, parameter);
	}

	@SuppressWarnings("unchecked")
	private IResultCallback<Object> getResultCallback(final Class<?>[] parameterTypes, final Object[] args) {
		final int index = getFirstMatchingIndex(IResultCallback.class, parameterTypes);
		return (IResultCallback<Object>) (index != -1 ? args[index] : null);
	}

	private IExecutionCallback getExecutionCallback(final Class<?>[] parameterTypes, final Object[] args) {
		final int index = getFirstMatchingIndex(IExecutionCallback.class, parameterTypes);
		return (IExecutionCallback) (index != -1 ? args[index] : null);
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

	/**
	 * Filter the callback arguments
	 * 
	 * @param args The args to filter
	 * @return the filtered args
	 */
	private Object[] getFilteredArgs(final Object[] args) {
		if (args != null) {
			final Object[] result = new Object[args.length];
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof IResultCallback || args[i] instanceof IExecutionCallback) {
					result[i] = null;
				}
				else {
					result[i] = args[i];
				}
			}
			return result;
		}
		else {
			return new Object[0];
		}
	}
}
