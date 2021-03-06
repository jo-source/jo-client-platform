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

package org.jowidgets.cap.remoting.client;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.execution.UserQuestionResult;
import org.jowidgets.cap.remoting.common.CapInvocationMethodNames;
import org.jowidgets.cap.remoting.common.InputStreamDummy;
import org.jowidgets.cap.remoting.common.Progress;
import org.jowidgets.cap.remoting.common.RemoteInvocationParameter;
import org.jowidgets.cap.remoting.common.UserQuestionRequest;
import org.jowidgets.invocation.service.client.api.IInvocationServiceClient;
import org.jowidgets.invocation.service.client.api.InvocationServiceClientToolkit;
import org.jowidgets.invocation.service.common.api.IInterimRequestCallback;
import org.jowidgets.invocation.service.common.api.IInterimResponseCallback;
import org.jowidgets.invocation.service.common.api.IInvocationCallback;
import org.jowidgets.invocation.service.common.api.IMethodInvocationService;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.io.IoUtils;

final class RemoteMethodInvocationHandler implements InvocationHandler {

	private final IServiceId<?> serviceId;
	private final IInvocationServiceClient invocationServiceClient;

	RemoteMethodInvocationHandler(final Object brokerId, final IServiceId<?> serviceId) {
		Assert.paramNotNull(serviceId, "serviceId");
		this.serviceId = serviceId;
		this.invocationServiceClient = InvocationServiceClientToolkit.getClient(brokerId);
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
		final ArrayList<InputStream> inputStreams = getInputStreams(parameterTypes, args);

		final IInterimResponseCallback<Progress> interimResponseCallback;
		final IInterimRequestCallback<UserQuestionRequest, UserQuestionResult> userQuestionRequestCallback;
		if (executionCallback != null) {
			interimResponseCallback = new ProgressResponseCallback(executionCallback);
			userQuestionRequestCallback = new UserQuestionRequestCallback(executionCallback);
		}
		else {
			interimResponseCallback = new DummyProgressResponseCallback();
			userQuestionRequestCallback = new DummyUserQuestionRequestCallback(resultCallback);
		}

		final InputStreamRequestCallback inputStreamRequestCallback;
		if (!EmptyCheck.isEmpty(inputStreams)) {
			inputStreamRequestCallback = new InputStreamRequestCallback(inputStreams);
		}
		else {
			inputStreamRequestCallback = null;
		}
		final GenericInterimRequestCallback interimRequestCallback = new GenericInterimRequestCallback(
			userQuestionRequestCallback,
			inputStreamRequestCallback);

		final RemoteInvocationParameter parameter = new RemoteInvocationParameter(
			serviceId,
			method.getName(),
			parameterTypes,
			getFilteredArgs(args, parameterTypes));

		if (resultCallback != null) {
			invokeAsync(
					resultCallback,
					interimResponseCallback,
					interimRequestCallback,
					parameter,
					executionCallback,
					inputStreams);
			return null;
		}
		else {
			return invokeSync(interimResponseCallback, interimRequestCallback, parameter, executionCallback, inputStreams);
		}
	}

	private Object invokeSync(
		final IInterimResponseCallback<Progress> interimResponseCallback,
		final GenericInterimRequestCallback interimRequestCallback,
		final RemoteInvocationParameter parameter,
		final IExecutionCallback executionCallback,
		final ArrayList<InputStream> inputStreams) {

		final SyncInvocationCallback<Object> syncInvocationCallback = new SyncInvocationCallback<Object>(executionCallback);
		invokeMethod(syncInvocationCallback, interimResponseCallback, interimRequestCallback, parameter);
		final Object result = syncInvocationCallback.getResultSynchronious();
		for (final InputStream inputStream : inputStreams) {
			IoUtils.tryCloseSilent(inputStream);
		}
		return result;
	}

	private void invokeAsync(
		final IResultCallback<Object> resultCallback,
		final IInterimResponseCallback<Progress> interimResponseCallback,
		final GenericInterimRequestCallback interimRequestCallback,
		final RemoteInvocationParameter parameter,
		final IExecutionCallback executionCallback,
		final ArrayList<InputStream> inputStreams) {

		final IInvocationCallback<Object> invocationCallback = new InvocationCallback<Object>(
			resultCallback,
			executionCallback,
			inputStreams);
		invokeMethod(invocationCallback, interimResponseCallback, interimRequestCallback, parameter);
	}

	private void invokeMethod(
		final IInvocationCallback<Object> invocationCallback,
		final IInterimResponseCallback<Progress> interimResponseCallback,
		final GenericInterimRequestCallback interimRequestCallback,
		final RemoteInvocationParameter parameter) {

		final IMethodInvocationService<Object, Progress, Object, Object, RemoteInvocationParameter> methodService;
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

	private ArrayList<InputStream> getInputStreams(final Class<?>[] parameterTypes, final Object[] args) {
		final ArrayList<InputStream> result = new ArrayList<InputStream>();
		if (parameterTypes != null) {
			for (int i = 0; i < parameterTypes.length; i++) {
				if (InputStream.class.isAssignableFrom(parameterTypes[i])) {
					result.add((InputStream) args[i]);
				}
				else if (InputStream[].class.isAssignableFrom(parameterTypes[i])) {
					final InputStream[] inputStreams = (InputStream[]) args[i];
					if (inputStreams != null) {
						for (final InputStream element : inputStreams) {
							result.add(element);
						}
					}
				}
				else if (Iterable.class.isAssignableFrom(parameterTypes[i])) {
					final Iterable<?> iterable = (Iterable<?>) args[i];
					if (iterable != null) {
						for (final Object element : iterable) {
							if (element instanceof InputStream) {
								result.add((InputStream) element);
							}
						}
					}
				}
			}
		}
		return result;
	}

	private int getFirstMatchingIndex(final Class<?> type, final Class<?>[] paramTypes) {
		if (paramTypes != null) {
			for (int i = 0; i < paramTypes.length; i++) {
				if (type.isAssignableFrom(paramTypes[i])) {
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
	 * @param parameterTypes
	 * @return the filtered args
	 */
	@SuppressWarnings("unchecked")
	private Object[] getFilteredArgs(final Object[] args, final Class<?>[] parameterTypes) {
		if (args != null) {
			final Object[] result = new Object[args.length];
			for (int i = 0; i < args.length; i++) {
				final Object object = args[i];
				if (object instanceof IResultCallback<?>) {
					result[i] = null;
				}
				else if (object instanceof IExecutionCallback) {
					result[i] = null;
				}
				else if (object instanceof InputStream) {
					result[i] = new InputStreamDummy();
				}
				else if (InputStream[].class.isAssignableFrom(parameterTypes[i])) {
					final InputStream[] inputStreams = (InputStream[]) object;
					if (inputStreams != null) {
						final InputStreamDummy[] inputStreamDummies = new InputStreamDummy[inputStreams.length];
						for (int j = 0; j < inputStreams.length; j++) {
							if (inputStreams[j] != null) {
								inputStreamDummies[j] = new InputStreamDummy();
							}
							else {
								inputStreamDummies[j] = null;
							}
						}
						result[i] = inputStreamDummies;
					}
					else {
						result[i] = null;
					}
				}
				else if (Iterable.class.isAssignableFrom(parameterTypes[i])) {
					final Iterable<?> iterable = (Iterable<?>) object;
					if (iterable != null && isIterableOfType(iterable, InputStream.class)) {
						if (!Collection.class.isAssignableFrom(parameterTypes[i])) {
							throw new IllegalArgumentException("Iterables that hold InputStreams must be Collections");
						}
						final Collection<InputStreamDummy> inputStreamDummies;
						try {
							inputStreamDummies = (Collection<InputStreamDummy>) iterable.getClass().newInstance();
						}
						catch (final Exception e) {
							throw new IllegalArgumentException(
								"Collections that holds input streams must have a public default constructor",
								e);
						}
						for (final Object element : iterable) {
							if (element instanceof InputStream) {
								inputStreamDummies.add(new InputStreamDummy());
							}
							else if (element != null) {
								throw new IllegalArgumentException("Collections with mixed types must not contain InputStreams");
							}
							else {
								inputStreamDummies.add(null);
							}
						}
						result[i] = inputStreamDummies;
					}
					else {
						result[i] = object;
					}
				}
				else {
					result[i] = object;
				}
			}
			return result;
		}
		else {
			return new Object[0];
		}
	}

	private boolean isIterableOfType(final Iterable<?> iterable, final Class<?> type) {
		for (final Object element : iterable) {
			if (element != null) {
				if (type.isAssignableFrom(element.getClass())) {
					return true;
				}
				else {
					return false;
				}
			}
		}
		return false;
	}

}
