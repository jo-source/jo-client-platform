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

package org.jowidgets.invocation.impl.local;

import org.jowidgets.invocation.client.api.IClientServiceRegistry;
import org.jowidgets.invocation.client.api.IInvocationClient;
import org.jowidgets.invocation.common.api.ICancelService;
import org.jowidgets.invocation.common.api.IInvocationCallbackService;
import org.jowidgets.invocation.common.api.IServerMethod;
import org.jowidgets.invocation.common.api.IResponseService;
import org.jowidgets.util.Assert;

final class InvocationClient implements IInvocationClient, IClientServiceRegistry {

	private static final InvocationClient INSTANCE = new InvocationClient();

	private IInvocationCallbackService callbackService;

	private InvocationClient() {}

	@Override
	public void register(final IInvocationCallbackService callbackService) {
		Assert.paramNotNull(callbackService, "callbackService");
		this.callbackService = callbackService;
	}

	@Override
	public IServerMethod getMethod(final String methodName) {
		return InvocationServer.getInstance().getMethod(methodName);
	}

	@Override
	public ICancelService getCancelService(final Object serverId) {
		return InvocationServer.getInstance().getCancelService();
	}

	@Override
	public IResponseService getResponseService(final Object serverId) {
		return InvocationServer.getInstance().getResponseService();
	}

	IInvocationCallbackService getCallbackService() {
		return callbackService;
	}

	public static InvocationClient getInstance() {
		return INSTANCE;
	}

}
