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
import org.jowidgets.invocation.common.api.IResponseService;
import org.jowidgets.invocation.common.api.IServerMethod;
import org.jowidgets.invocation.common.impl.CancelMessage;
import org.jowidgets.invocation.common.impl.MessageBrokerId;
import org.jowidgets.invocation.common.impl.MethodInvocationMessage;
import org.jowidgets.invocation.common.impl.ResponseMessage;
import org.jowidgets.message.api.IMessageChannel;
import org.jowidgets.message.api.IMessageClient;
import org.jowidgets.message.api.MessageToolkit;

class InvocationClientImpl implements IInvocationClient {

	private final InvocationClientServiceRegistryImpl invocationClientServiceRegistry;
	private final IMessageClient messageClient;

	InvocationClientImpl(final InvocationClientServiceRegistryImpl invocationClientServiceRegistry) {
		this.messageClient = MessageToolkit.getClient(MessageBrokerId.INVOCATION_IMPL_BROKER_ID);
		this.invocationClientServiceRegistry = invocationClientServiceRegistry;
	}

	@Override
	public IServerMethod getMethod(final String methodName) {
		final IMessageChannel messageChannel = messageClient.getMessageChannel();
		return new IServerMethod() {

			@Override
			public void invoke(final Object clientId, final Object invocationId, final Object parameter) {
				final MethodInvocationMessage message = new MethodInvocationMessage(clientId, invocationId, methodName, parameter);
				messageChannel.send(message, new ExceptionCallback(invocationClientServiceRegistry, invocationId));
			}

			@Override
			public Object getServerId() {
				return messageChannel.getServerPeer();
			}
		};
	}

	@Override
	public ICancelService getCancelService(final Object serverId) {
		final IMessageChannel messageChannel = messageClient.getMessageChannel(serverId);
		return new ICancelService() {
			@Override
			public void canceled(final Object invocationId) {
				final CancelMessage message = new CancelMessage(invocationId);
				messageChannel.send(message, new ExceptionCallback(invocationClientServiceRegistry, invocationId));
			}
		};
	}

	@Override
	public IResponseService getResponseService(final Object serverId) {
		final IMessageChannel messageChannel = messageClient.getMessageChannel(serverId);
		return new IResponseService() {
			@Override
			public void response(final Object invocationId, final Object requestId, final Object response) {
				final ResponseMessage message = new ResponseMessage(invocationId, requestId, response);
				messageChannel.send(message, new ExceptionCallback(invocationClientServiceRegistry, invocationId));
			}
		};
	}

}
