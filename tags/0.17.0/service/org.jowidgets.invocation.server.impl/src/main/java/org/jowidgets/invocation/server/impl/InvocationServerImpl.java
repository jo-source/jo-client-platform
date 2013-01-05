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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jowidgets.invocation.common.api.IInvocationCallbackService;
import org.jowidgets.invocation.common.impl.ExceptionMessage;
import org.jowidgets.invocation.common.impl.FinishedMessage;
import org.jowidgets.invocation.common.impl.InterimRequestMessage;
import org.jowidgets.invocation.common.impl.InterimResponseMessage;
import org.jowidgets.invocation.common.impl.MessageBrokerId;
import org.jowidgets.invocation.server.api.IInvocationServer;
import org.jowidgets.message.api.IExceptionCallback;
import org.jowidgets.message.api.IMessageChannel;
import org.jowidgets.message.api.MessageToolkit;
import org.jowidgets.util.Assert;

public final class InvocationServerImpl implements IInvocationServer {

	//TODO MG remove invocations with timeout from the map
	private final Map<Object, MethodInvocation> methodInvocations;
	private final IInvocationCallbackService invocationCallbackService;

	InvocationServerImpl() {
		this.methodInvocations = new ConcurrentHashMap<Object, MethodInvocation>();
		this.invocationCallbackService = new IInvocationCallbackService() {

			@Override
			public void interimResponse(final Object invocationId, final Object response) {
				getMessageChannel(invocationId).send(new InterimResponseMessage(invocationId, response), null);
			}

			@Override
			public void interimRequest(final Object invocationId, final Object requestId, final Object request) {
				getMessageChannel(invocationId).send(new InterimRequestMessage(invocationId, requestId, request), null);
			}

			@Override
			public void finished(final Object invocationId, final Object result) {
				getMessageChannel(invocationId).send(new FinishedMessage(invocationId, result), null);
				methodInvocations.remove(invocationId);
			}

			@Override
			public void exeption(final Object invocationId, final Throwable exception) {
				getMessageChannel(invocationId).send(new ExceptionMessage(invocationId, exception), null);
				methodInvocations.remove(invocationId);
			}
		};
	}

	@Override
	public IInvocationCallbackService getInvocationCallbackService() {
		return invocationCallbackService;
	}

	void registerInvocation(final Object invocationId, final IMessageChannel replyChannel) {
		Assert.paramNotNull(invocationId, "invocationId");
		Assert.paramNotNull(replyChannel, "replyChannel");
		methodInvocations.put(invocationId, new MethodInvocation(invocationId, replyChannel));
	}

	void unregisterInvocation(final Object invocationId) {
		Assert.paramNotNull(invocationId, "invocationId");
		methodInvocations.remove(invocationId);
	}

	IMessageChannel getMessageChannel(final Object invocationId) {
		final MethodInvocation methodInvocation = methodInvocations.get(invocationId);
		if (methodInvocation != null) {
			return methodInvocation.getReplyChannel();
		}
		else {
			return new IMessageChannel() {
				@Override
				public void send(final Object message, final IExceptionCallback exceptionCallback) {
					if (message instanceof ExceptionMessage) {
						final Throwable throwable = ((ExceptionMessage) message).getException();
						if (exceptionCallback != null) {
							exceptionCallback.exception(new IllegalStateException(
								"No message channel is registered for invocationId '" + invocationId + "'",
								throwable));
						}
						else {
							MessageToolkit.handleExceptions(MessageBrokerId.INVOCATION_IMPL_BROKER_ID, new IllegalStateException(
								"No message channel is registered for invocationId '" + invocationId + "'",
								throwable));
						}
					}
					else if (exceptionCallback != null) {
						exceptionCallback.exception(new IllegalStateException(
							"No message channel is registered for invocationId '" + invocationId + "'"));
					}
					else {
						MessageToolkit.handleExceptions(MessageBrokerId.INVOCATION_IMPL_BROKER_ID, new IllegalStateException(
							"No message channel is registered for invocationId '" + invocationId + "'"));
					}
				}
			};
		}
	}

}
