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

package org.jowidgets.cap.common.tools.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;

public abstract class AbstractCapServiceInvocationHandler implements InvocationHandler {

	@Override
	public final Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		final Class<?>[] parameterTypes = method.getParameterTypes();

		final IExecutionCallback executionCallback = getExecutionCallback(parameterTypes, args);

		final int resultCallbackIndex = getFirstMatchingIndex(IResultCallback.class, parameterTypes);
		if (resultCallbackIndex == -1) {
			return invokeSyncSignature(method, args, executionCallback);
		}
		else {
			@SuppressWarnings("unchecked")
			final IResultCallback<Object> resultCallback = (IResultCallback<Object>) args[resultCallbackIndex];
			return invokeAsyncSignature(method, args, resultCallbackIndex, resultCallback, executionCallback);
		}
	}

	/**
	 * Invokes sync signature method (method has no result callback)
	 * 
	 * @param method The method to invoke
	 * @param args The methods args
	 * @param executionCallback The execution callback (may be null)
	 * 
	 * @return The invocation result
	 * @throws Throwable
	 */
	protected abstract Object invokeSyncSignature(final Method method, final Object[] args, IExecutionCallback executionCallback) throws Throwable;

	/**
	 * Invokes an async signature method (return type is void, method has a result callback)
	 * 
	 * @param method
	 * @param args The methods args
	 * @param resultCallbackIndex The index of the result callback arg (useful to decorate it)
	 * @param resultCallback The result callback (never null)
	 * @param executionCallback The execution callback, may be null
	 * 
	 * @return Normally null is returned, because the method is async (has void as result type=
	 */
	protected abstract Object invokeAsyncSignature(
		final Method method,
		final Object[] args,
		final int resultCallbackIndex,
		final IResultCallback<Object> resultCallback,
		final IExecutionCallback executionCallback);

	private IExecutionCallback getExecutionCallback(final Class<?>[] parameterTypes, final Object[] args) {
		final int executionCallbackIndex = getFirstMatchingIndex(IExecutionCallback.class, parameterTypes);
		if (executionCallbackIndex != -1) {
			return (IExecutionCallback) args[executionCallbackIndex];
		}
		else {
			return null;
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
