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

package org.jowidgets.invocation.service.server.impl;

import java.util.concurrent.TimeoutException;

import org.jowidgets.invocation.common.api.IInvocationCallbackService;
import org.jowidgets.invocation.common.api.IMethod;
import org.jowidgets.invocation.server.api.IInvocationServer;
import org.jowidgets.invocation.service.common.api.ICancelListener;
import org.jowidgets.invocation.service.common.api.IInterimRequestCallback;
import org.jowidgets.invocation.service.common.api.IInterimResponseCallback;
import org.jowidgets.invocation.service.common.api.IInvocationCallback;
import org.jowidgets.invocation.service.common.api.IMethodInvocationService;

@SuppressWarnings({"rawtypes", "unchecked"})
public class MethodImpl implements IMethod {

	private final IInvocationServer invocationServer;
	private final CancelServiceImpl cancelService;
	private final ResponseServiceImpl responseService;
	private final IMethodInvocationService methodInvocationService;

	MethodImpl(
		final IInvocationServer invocationServer,
		final CancelServiceImpl cancelService,
		final ResponseServiceImpl responseService,
		final IMethodInvocationService methodInvocationService) {
		super();
		this.invocationServer = invocationServer;
		this.cancelService = cancelService;
		this.responseService = responseService;
		this.methodInvocationService = methodInvocationService;
	}

	@Override
	public void invoke(final Object invocationId, final Object parameter) {

		final IInvocationCallbackService invocationCallbackService = invocationServer.getInvocationCallbackService();

		final IInvocationCallback<Object> invocationCallback = new IInvocationCallback<Object>() {

			@Override
			public void addCancelListener(final ICancelListener cancelListener) {
				cancelService.registerCancelListener(invocationId, cancelListener);
			}

			@Override
			public void finished(final Object result) {
				invocationCallbackService.finished(invocationId, result);
				cancelService.unregisterInvocation(invocationId);
			}

			@Override
			public void exeption(final Throwable exception) {
				invocationCallbackService.exeption(invocationId, exception);
				cancelService.unregisterInvocation(invocationId);
			}

			@Override
			public void timeout() {
				invocationCallbackService.exeption(invocationId, new TimeoutException());
				cancelService.unregisterInvocation(invocationId);
			}
		};

		final IInterimResponseCallback<Object> interimResponseCallback = new IInterimResponseCallback<Object>() {
			@Override
			public void response(final Object progress) {
				invocationCallbackService.interimResponse(invocationId, progress);
			}
		};

		final IInterimRequestCallback<Object, Object> interimRequestCallback = new IInterimRequestCallback<Object, Object>() {
			@Override
			public void request(final IInterimResponseCallback<Object> callback, final Object question) {
				final Object requestId = responseService.register(callback);
				invocationCallbackService.interimRequest(invocationId, requestId, question);
			}
		};

		try {
			methodInvocationService.invoke(invocationCallback, interimResponseCallback, interimRequestCallback, parameter);
		}
		catch (final Exception e) {
			invocationCallback.exeption(e);
		}
	}

}
