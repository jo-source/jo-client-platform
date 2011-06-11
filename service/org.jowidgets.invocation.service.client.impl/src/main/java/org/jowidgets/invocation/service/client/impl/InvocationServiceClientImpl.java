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

package org.jowidgets.invocation.service.client.impl;

import org.jowidgets.invocation.client.api.IInvocationClient;
import org.jowidgets.invocation.client.api.InvocationClientToolkit;
import org.jowidgets.invocation.common.api.IServerMethod;
import org.jowidgets.invocation.service.client.api.IInvocationServiceClient;
import org.jowidgets.invocation.service.common.api.IInterimRequestCallback;
import org.jowidgets.invocation.service.common.api.IInterimResponseCallback;
import org.jowidgets.invocation.service.common.api.IInvocationCallback;
import org.jowidgets.invocation.service.common.api.IMethodInvocationService;
import org.jowidgets.util.Assert;

final class InvocationServiceClientImpl implements IInvocationServiceClient {

	private final long defaulTimeout;
	private final IInvocationClient invocationClient;
	private final InvocationCallbackServiceImpl invocationCallbackService;

	InvocationServiceClientImpl(final InvocationCallbackServiceImpl invocationCallbackService, final long defaulTimeout) {
		super();
		this.defaulTimeout = defaulTimeout;
		this.invocationCallbackService = invocationCallbackService;
		this.invocationClient = InvocationClientToolkit.getClient();
	}

	@Override
	public <RES, PROG, QUEST, QUEST_RES, PARAM> IMethodInvocationService<RES, PROG, QUEST, QUEST_RES, PARAM> getMethodService(
		final String methodName) {
		return getMethodService(methodName, defaulTimeout);
	}

	@Override
	public <RES, PROG, QUEST, QUEST_RES, PARAM> IMethodInvocationService<RES, PROG, QUEST, QUEST_RES, PARAM> getMethodService(
		final String methodName,
		final long timeout) {
		Assert.paramNotNull(methodName, "methodName");

		return new IMethodInvocationService<RES, PROG, QUEST, QUEST_RES, PARAM>() {

			@Override
			public void invoke(
				final IInvocationCallback<RES> invocationCallback,
				final IInterimResponseCallback<PROG> interimResponseCallback,
				final IInterimRequestCallback<QUEST, QUEST_RES> interimRequestCallback,
				final PARAM parameter) {

				final IServerMethod serverMethod = invocationClient.getMethod(methodName);
				if (serverMethod == null) {
					throw new IllegalArgumentException("No server method registered for method name '" + methodName + "'.");
				}
				else {
					final Object invocationId = invocationCallbackService.registerInvocation(
							invocationCallback,
							interimResponseCallback,
							interimRequestCallback,
							timeout,
							serverMethod.getServerId(),
							invocationClient);

					serverMethod.invoke(invocationId, parameter);
				}

			}
		};
	}

}
