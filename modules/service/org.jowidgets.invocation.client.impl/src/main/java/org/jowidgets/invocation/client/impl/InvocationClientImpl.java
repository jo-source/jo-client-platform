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

package org.jowidgets.invocation.client.impl;

import org.jowidgets.invocation.client.api.IInvocationClient;
import org.jowidgets.invocation.common.api.ICancelService;
import org.jowidgets.invocation.common.api.IMethod;
import org.jowidgets.invocation.common.api.IResponseService;
import org.jowidgets.invocation.common.impl.CancelMessage;
import org.jowidgets.invocation.common.impl.MethodInvocationMessage;
import org.jowidgets.invocation.common.impl.ResponseMessage;
import org.jowidgets.message.api.IMessageChannel;
import org.jowidgets.message.api.MessageToolkit;
import org.jowidgets.util.Assert;

class InvocationClientImpl implements IInvocationClient {

	private final InvocationClientServiceRegistryImpl invocationClientServiceRegistry;
	private final IMessageChannel messageChannel;
	private final ICancelService cancelService;
	private final IResponseService responseService;

	InvocationClientImpl(final Object brokerId, final InvocationClientServiceRegistryImpl invocationClientServiceRegistry) {
		this.messageChannel = MessageToolkit.getChannel(brokerId);
		this.invocationClientServiceRegistry = invocationClientServiceRegistry;
		this.cancelService = new CancelService();
		this.responseService = new ResponseService();
	}

	@Override
	public IMethod getMethod(final String methodName) {
		return new IMethod() {
			@Override
			public void invoke(final Object invocationId, final Object parameter) {
				final MethodInvocationMessage message = new MethodInvocationMessage(invocationId, methodName, parameter);
				messageChannel.send(message, new ExceptionCallback(invocationClientServiceRegistry, invocationId));
			}
		};
	}

	@Override
	public ICancelService getCancelService() {
		return cancelService;
	}

	@Override
	public IResponseService getResponseService() {
		return responseService;
	}

	private class CancelService implements ICancelService {
		@Override
		public void canceled(final Object invocationId) {
			Assert.paramNotNull(invocationId, "invocationId");
			final CancelMessage message = new CancelMessage(invocationId);
			messageChannel.send(message, new ExceptionCallback(invocationClientServiceRegistry, invocationId));
		}
	}

	private class ResponseService implements IResponseService {
		@Override
		public void response(final Object requestId, final Object response) {
			Assert.paramNotNull(requestId, "requestId");
			final ResponseMessage message = new ResponseMessage(requestId, response);
			messageChannel.send(message, new ExceptionCallback(invocationClientServiceRegistry, requestId));
		}
	}

}
