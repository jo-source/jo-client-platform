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

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.remoting.common.InputStreamDummy;
import org.jowidgets.cap.remoting.common.RemoteInvocationParameter;
import org.jowidgets.invocation.service.common.api.IInterimRequestCallback;
import org.jowidgets.invocation.service.common.api.IInterimResponseCallback;
import org.jowidgets.invocation.service.common.api.IInvocationCallback;
import org.jowidgets.invocation.service.common.api.IMethodInvocationService;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.io.LazyBufferedInputStream;

final class GenericRemoteMethod implements IMethodInvocationService<Object, Object, Object, Object, RemoteInvocationParameter> {

	private final long progressDelay;
	private final ScheduledExecutorService scheduledExecutorService;

	GenericRemoteMethod(final ScheduledExecutorService scheduledExecutorService, final long progressDelay) {
		this.scheduledExecutorService = scheduledExecutorService;
		this.progressDelay = progressDelay;
	}

	@Override
	public void invoke(
		final IInvocationCallback<Object> invocationCallback,
		final IInterimResponseCallback<Object> interimResponseCallback,
		final IInterimRequestCallback<Object, Object> interimRequestCallback,
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
					throw new IllegalArgumentException(
						"No method found for '"
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

	@SuppressWarnings("unchecked")
	public void invokeMethodOnService(
		final Object service,
		final Method method,
		final IInvocationCallback<Object> invocationCallback,
		final IInterimResponseCallback<Object> interimResponseCallback,
		final IInterimRequestCallback<Object, Object> interimRequestCallback,
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

		if (parameterTypes != null) {
			int inputStreamIndex = 0;
			for (int i = 0; i < parameterTypes.length; i++) {
				if (InputStream.class.isAssignableFrom(parameterTypes[i])) {
					arguments[i] = createServerInputStream(inputStreamIndex, interimRequestCallback);
					inputStreamIndex++;
				}
				else if (InputStream[].class.isAssignableFrom(parameterTypes[i])) {
					final InputStreamDummy[] inputStreamDummies = (InputStreamDummy[]) arguments[i];
					if (inputStreamDummies != null) {
						final InputStream[] inputStreams = new InputStream[inputStreamDummies.length];
						for (int j = 0; j < inputStreams.length; j++) {
							if (inputStreamDummies[j] != null) {
								inputStreams[j] = createServerInputStream(inputStreamIndex, interimRequestCallback);
								inputStreamIndex++;
							}
						}
						arguments[i] = inputStreams;
					}
				}
				else if (Collection.class.isAssignableFrom(parameterTypes[i])) {
					final Collection<?> collection = (Collection<?>) arguments[i];
					if (collection != null && isIterableOfType(collection, InputStreamDummy.class)) {
						final Collection<InputStream> inputStreams;
						try {
							inputStreams = collection.getClass().newInstance();
						}
						catch (final Exception e) {
							throw new IllegalArgumentException(
								"Collections that holds input streams must have a public default constructor",
								e);
						}
						for (final Object element : collection) {
							if (element instanceof InputStreamDummy) {
								inputStreams.add(createServerInputStream(inputStreamIndex, interimRequestCallback));
								inputStreamIndex++;
							}
							else if (element == null) {
								inputStreams.add(null);
							}
							else {
								throw new IllegalArgumentException("Collections with mixed types must not contain InputStreams");
							}
						}
						arguments[i] = inputStreams;
					}
				}
			}
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

	private InputStream createServerInputStream(
		final int inputStreamIndex,
		final IInterimRequestCallback<Object, Object> interimRequestCallback) {
		return new LazyBufferedInputStream(new ServerInputStream(inputStreamIndex, interimRequestCallback), 1024000);
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

}
