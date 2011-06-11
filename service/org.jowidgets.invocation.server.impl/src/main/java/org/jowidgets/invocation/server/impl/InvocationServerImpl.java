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

package org.jowidgets.invocation.server.impl;

import org.jowidgets.invocation.common.api.IInvocationCallbackService;
import org.jowidgets.invocation.common.impl.ExceptionMessage;
import org.jowidgets.invocation.common.impl.FinishedMessage;
import org.jowidgets.invocation.common.impl.InterimRequestMessage;
import org.jowidgets.invocation.common.impl.InterimResponseMessage;
import org.jowidgets.invocation.common.impl.MessageBrokerId;
import org.jowidgets.invocation.server.api.IInvocationServer;
import org.jowidgets.message.api.IMessageChannel;
import org.jowidgets.message.api.IMessageClient;
import org.jowidgets.message.api.MessageToolkit;

public class InvocationServerImpl implements IInvocationServer {

	private final IMessageClient messageClient;

	InvocationServerImpl() {
		this.messageClient = MessageToolkit.getClient(MessageBrokerId.INVOCATION_IMPL_BROKER_ID);
	}

	@Override
	public IInvocationCallbackService getInvocationCallback(final Object clientId) {
		final IMessageChannel messageChannel = messageClient.getMessageChannel(clientId);
		return new IInvocationCallbackService() {

			@Override
			public void interimResponse(final Object invocationId, final Object response) {
				messageChannel.send(new InterimResponseMessage(invocationId, response), null);
			}

			@Override
			public void interimRequest(final Object invocationId, final Object requestId, final Object request) {
				messageChannel.send(new InterimRequestMessage(invocationId, requestId, request), null);
			}

			@Override
			public void finished(final Object invocationId, final Object result) {
				messageChannel.send(new FinishedMessage(invocationId, result), null);
			}

			@Override
			public void exeption(final Object invocationId, final Throwable exception) {
				messageChannel.send(new ExceptionMessage(invocationId, exception), null);
			}
		};
	}

}
