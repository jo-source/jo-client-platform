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

package org.jowidgets.remoting.service.client.impl;

import java.util.concurrent.ExecutorService;

import org.jowidgets.remoting.client.api.IRemoteClient;
import org.jowidgets.remoting.client.api.RemoteClientToolkit;
import org.jowidgets.remoting.common.api.IRemoteMethod;
import org.jowidgets.remoting.service.client.api.IRemoteServiceClient;
import org.jowidgets.remoting.service.common.api.IInvocationResultCallback;
import org.jowidgets.remoting.service.common.api.IProgressCallback;
import org.jowidgets.remoting.service.common.api.IRemoteMethodService;
import org.jowidgets.remoting.service.common.api.IUserQuestionCallback;
import org.jowidgets.util.Assert;

final class RemoteServiceClient implements IRemoteServiceClient {

	private final long defaulTimeout;
	private final IRemoteClient remoteClient;
	private final Object clientId;
	private final InvocationCallbackService invocationCallbackService;
	private final ExecutorService executorService;

	RemoteServiceClient(
		final Object clientId,
		final InvocationCallbackService invocationCallbackService,
		final ExecutorService executorService,
		final long defaulTimeout) {
		super();
		this.defaulTimeout = defaulTimeout;
		this.clientId = clientId;
		this.invocationCallbackService = invocationCallbackService;
		this.executorService = executorService;
		this.remoteClient = RemoteClientToolkit.getClient();
	}

	@Override
	public <RES, PROG, QUEST, QUEST_RES, PARAM> IRemoteMethodService<RES, PROG, QUEST, QUEST_RES, PARAM> getMethodService(
		final String methodName) {
		return getMethodService(methodName, defaulTimeout);
	}

	@Override
	public <RES, PROG, QUEST, QUEST_RES, PARAM> IRemoteMethodService<RES, PROG, QUEST, QUEST_RES, PARAM> getMethodService(
		final String methodName,
		final long timeout) {
		Assert.paramNotNull(methodName, "methodName");

		return new IRemoteMethodService<RES, PROG, QUEST, QUEST_RES, PARAM>() {

			@Override
			public void invoke(
				final IInvocationResultCallback<RES> resultCallback,
				final IProgressCallback<PROG> progressCallback,
				final IUserQuestionCallback<QUEST, QUEST_RES> userQuestionCallback,
				final PARAM parameter) {

				executorService.execute(new Runnable() {
					@Override
					public void run() {
						final IRemoteMethod remoteMethod = remoteClient.getMethod(methodName);
						if (remoteMethod == null) {
							if (resultCallback != null) {
								resultCallback.exeption(new IllegalArgumentException(
									"No remote method registered for method name '" + methodName + "'."));
							}
						}
						else {
							final Object invocationId = invocationCallbackService.registerInvocation(
									resultCallback,
									progressCallback,
									userQuestionCallback,
									timeout,
									remoteMethod.getServerId(),
									remoteClient);

							remoteMethod.invoke(clientId, invocationId, parameter);
						}
					}
				});

			}
		};
	}

}
