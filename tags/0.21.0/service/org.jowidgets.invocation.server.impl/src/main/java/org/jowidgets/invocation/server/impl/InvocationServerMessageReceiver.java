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

import org.jowidgets.invocation.common.impl.AcknowledgeMessage;
import org.jowidgets.invocation.common.impl.CancelMessage;
import org.jowidgets.invocation.common.impl.MethodInvocationMessage;
import org.jowidgets.invocation.common.impl.ResponseMessage;
import org.jowidgets.invocation.server.api.IInvocationServer;
import org.jowidgets.invocation.server.api.IInvocationServerServiceRegistry;
import org.jowidgets.message.api.IExceptionCallback;
import org.jowidgets.message.api.IMessageChannel;
import org.jowidgets.message.api.IMessageReceiver;

final class InvocationServerMessageReceiver implements IMessageReceiver {

	private final InvocationServerServiceRegistryImpl invocationServerServiceRegistry;
	private final InvocationServerImpl invocationServer;

	InvocationServerMessageReceiver(
		final InvocationServerImpl invocationServer,
		final InvocationServerServiceRegistryImpl invocationServerServiceRegistry) {
		this.invocationServerServiceRegistry = invocationServerServiceRegistry;
		this.invocationServer = invocationServer;
	}

	@Override
	public void onMessage(final Object message, final IMessageChannel replyChannel) {
		if (message instanceof MethodInvocationMessage) {
			final MethodInvocationMessage invocationMessage = (MethodInvocationMessage) message;
			final Object invocationId = invocationMessage.getInvocationId();
			invocationServer.registerInvocation(invocationId, replyChannel);

			final IExceptionCallback exceptionCallback = new IExceptionCallback() {
				@Override
				public void exception(final Throwable throwable) {
					invocationServer.unregisterInvocation(invocationId);
					invocationServerServiceRegistry.onCancel(invocationId);
				}
			};

			replyChannel.send(new AcknowledgeMessage(invocationId), exceptionCallback);
			invocationServerServiceRegistry.onMethodInvocation((MethodInvocationMessage) message);
		}
		else if (message instanceof CancelMessage) {
			final CancelMessage cancelMessage = (CancelMessage) message;
			invocationServerServiceRegistry.onCancel(cancelMessage);
			invocationServer.unregisterInvocation(cancelMessage.getInvocationId());
		}
		else if (message instanceof ResponseMessage) {
			invocationServerServiceRegistry.onResponse((ResponseMessage) message);
		}
	}

	IInvocationServer getInvocationServer() {
		return invocationServer;
	}

	IInvocationServerServiceRegistry getInvocationServerServiceRegistry() {
		return invocationServerServiceRegistry;
	}

}
