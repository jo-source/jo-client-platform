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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.jowidgets.invocation.common.api.IInvocationCallbackService;
import org.jowidgets.invocation.common.api.IMethod;
import org.jowidgets.invocation.server.api.IInvocationServer;
import org.jowidgets.invocation.service.common.api.ICancelListener;
import org.jowidgets.invocation.service.common.api.IInterimRequestCallback;
import org.jowidgets.invocation.service.common.api.IInterimResponseCallback;
import org.jowidgets.invocation.service.common.api.IInvocationCallback;
import org.jowidgets.invocation.service.common.api.IMethodInvocationService;
import org.jowidgets.util.Assert;

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

		final InterimRequestCallback interimRequestCallback = new InterimRequestCallback(invocationId);
		final InterimResponseCallback interimResponseCallback = new InterimResponseCallback(invocationId);
		final MethodInvocationCallback invocationCallback = new MethodInvocationCallback(
			invocationId,
			interimRequestCallback,
			interimResponseCallback);

		cancelService.registerInvocation(invocationId);
		cancelService.registerCancelListener(invocationId, new ICancelListener() {
			@Override
			public void canceled() {
				invocationCallback.dispose();
				interimResponseCallback.dispose();
				interimRequestCallback.dispose();
			}
		});

		try {
			methodInvocationService.invoke(invocationCallback, interimResponseCallback, interimRequestCallback, parameter);
		}
		catch (final Exception e) {
			invocationCallback.exeption(e);
		}
	}

	private abstract class AbstractCancelableCallback {

		private final AtomicBoolean disposed;

		AbstractCancelableCallback() {
			this.disposed = new AtomicBoolean(false);
		}

		final boolean isDisposed() {
			return disposed.get();
		}

		void dispose() {
			disposed.set(true);
		}

		IInvocationCallbackService getInvocationCallbackService() {
			return invocationServer.getInvocationCallbackService();
		}
	}

	private class MethodInvocationCallback extends AbstractCancelableCallback implements IInvocationCallback<Object> {

		private final Object invocationId;
		private final InterimRequestCallback interimRequestCallback;
		private final InterimResponseCallback interimResponseCallback;

		MethodInvocationCallback(
			final Object invocationId,
			final InterimRequestCallback interimRequestCallback,
			final InterimResponseCallback interimResponseCallback) {
			Assert.paramNotNull(invocationId, "invocationId");
			Assert.paramNotNull(interimRequestCallback, "interimRequestCallback");
			Assert.paramNotNull(interimResponseCallback, "interimResponseCallback");

			this.invocationId = invocationId;
			this.interimRequestCallback = interimRequestCallback;
			this.interimResponseCallback = interimResponseCallback;
		}

		@Override
		public void addCancelListener(final ICancelListener cancelListener) {
			cancelService.registerCancelListener(invocationId, cancelListener);
		}

		@Override
		public void finished(final Object result) {
			if (!isDisposed()) {
				interimRequestCallback.dispose();
				interimResponseCallback.dispose();
				getInvocationCallbackService().finished(invocationId, result);
				cancelService.unregisterInvocation(invocationId);
			}
			//else the cancel service will unregister on first cancel by itself
		}

		@Override
		public void exeption(final Throwable exception) {
			if (!isDisposed()) {
				interimRequestCallback.dispose();
				interimResponseCallback.dispose();
				getInvocationCallbackService().exeption(invocationId, exception);
				cancelService.unregisterInvocation(invocationId);
			}
			//else 
			// - Canceled method invoker is no longer interested in response
			// - The cancel service will unregister on first cancel by itself
		}

	}

	private class InterimResponseCallback extends AbstractCancelableCallback implements IInterimResponseCallback<Object> {

		private final Object invocationId;

		InterimResponseCallback(final Object invocationId) {
			this.invocationId = invocationId;
		}

		@Override
		public void response(final Object response) {
			if (!isDisposed()) {
				getInvocationCallbackService().interimResponse(invocationId, response);
			}
			//canceled method invoker is no longer interested in response
		}

	}

	private class InterimRequestCallback extends AbstractCancelableCallback implements IInterimRequestCallback<Object, Object> {

		private final Object invocationId;
		private final List<Object> requestIds;

		InterimRequestCallback(final Object invocationId) {
			this.invocationId = invocationId;
			this.requestIds = new CopyOnWriteArrayList<Object>();
		}

		@Override
		public void request(final IInterimResponseCallback<Object> callback, final Object request) {
			if (!isDisposed()) {
				final AtomicReference<Object> requestIdRef = new AtomicReference<Object>();
				final Object requestId = responseService.register(new IInterimResponseCallback<Object>() {
					@Override
					public void response(final Object response) {
						try {
							final Object requestIdForResponse = requestIdRef.get();
							if (requestIdForResponse != null) {
								requestIds.remove(requestIdForResponse);
							}
						}
						finally {
							callback.response(response);
						}
					}
				});
				getInvocationCallbackService().interimRequest(invocationId, requestId, request);
			}
		}

		@Override
		void dispose() {
			super.dispose();
			for (final Object requestId : new ArrayList<Object>(requestIds)) {
				responseService.unregister(requestId);
			}
		}

	}

}
