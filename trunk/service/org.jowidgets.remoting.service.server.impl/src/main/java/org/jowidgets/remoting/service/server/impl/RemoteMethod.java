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

package org.jowidgets.remoting.service.server.impl;

import java.util.concurrent.TimeoutException;

import org.jowidgets.remoting.common.api.IInvocationCallbackService;
import org.jowidgets.remoting.common.api.IMethod;
import org.jowidgets.remoting.server.api.IRemoteServer;
import org.jowidgets.remoting.service.common.api.ICancelListener;
import org.jowidgets.remoting.service.common.api.IInvocationResultCallback;
import org.jowidgets.remoting.service.common.api.IProgressCallback;
import org.jowidgets.remoting.service.common.api.IRemoteMethodService;
import org.jowidgets.remoting.service.common.api.IUserQuestionCallback;
import org.jowidgets.remoting.service.common.api.IUserQuestionResultCallback;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RemoteMethod implements IMethod {

	private final IRemoteServer remoteServer;
	private final CancelService cancelService;
	private final UserQuestionResultService userQuestionResultService;
	private final IRemoteMethodService remoteMethodService;

	RemoteMethod(
		final IRemoteServer remoteServer,
		final CancelService cancelService,
		final UserQuestionResultService userQuestionResultService,
		final IRemoteMethodService remoteMethodService) {
		super();
		this.remoteServer = remoteServer;
		this.cancelService = cancelService;
		this.userQuestionResultService = userQuestionResultService;
		this.remoteMethodService = remoteMethodService;
	}

	@Override
	public void invoke(final Object clientId, final Object invocationId, final Object parameter) {

		final IInvocationCallbackService invocationCallbackService = remoteServer.getInvocationCallback(clientId);

		final IInvocationResultCallback<Object> resultCallback = new IInvocationResultCallback<Object>() {

			@Override
			public void finished(final Object result) {
				invocationCallbackService.finished(invocationId, result);
			}

			@Override
			public void exeption(final Throwable exception) {
				invocationCallbackService.exeption(invocationId, exception);
			}

			@Override
			public void timeout() {
				invocationCallbackService.exeption(invocationId, new TimeoutException());
			}
		};

		final IProgressCallback<Object> progressCallback = new IProgressCallback<Object>() {
			@Override
			public void addCancelListener(final ICancelListener cancelListener) {
				cancelService.registerCancelListener(invocationId, cancelListener);
			}

			@Override
			public void setProgress(final Object progress) {
				invocationCallbackService.setProgress(invocationId, progress);
			}
		};

		final IUserQuestionCallback<Object, Object> userQuestionCallback = new IUserQuestionCallback<Object, Object>() {
			@Override
			public void userQuestion(final IUserQuestionResultCallback<Object> callback, final Object question) {
				final Object userQuestionId = userQuestionResultService.register(callback);
				invocationCallbackService.userQuestion(invocationId, userQuestionId, question);
			}
		};

		remoteMethodService.invoke(resultCallback, progressCallback, userQuestionCallback, parameter);

		cancelService.unregisterInvocation(invocationId);
	}

}
