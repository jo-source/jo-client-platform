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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import org.jowidgets.util.Tuple;

class InvocationClientImpl implements IInvocationClient {

	//TODO MG remove invocations after defined timeout
	private final Map<Object, TimeStampedObject<Object>> invokedInvocations;
	private final Map<Object, TimeStampedObject<Object>> canceledInvocations;
	private final Map<Object, TimeStampedObject<IMessageChannel>> acknowledgedInvocations;
	private final Map<Object, TimeStampedObject<Tuple<Object, IMessageChannel>>> interimRequests;

	private final InvocationClientServiceRegistryImpl invocationClientServiceRegistry;
	private final IMessageChannel messageChannel;

	InvocationClientImpl(final Object brokerId, final InvocationClientServiceRegistryImpl invocationClientServiceRegistry) {
		Assert.paramNotNull(brokerId, "brokerId");
		this.invokedInvocations = new ConcurrentHashMap<Object, TimeStampedObject<Object>>();
		this.canceledInvocations = new ConcurrentHashMap<Object, TimeStampedObject<Object>>();
		this.acknowledgedInvocations = new ConcurrentHashMap<Object, TimeStampedObject<IMessageChannel>>();
		this.interimRequests = new ConcurrentHashMap<Object, TimeStampedObject<Tuple<Object, IMessageChannel>>>();

		this.messageChannel = MessageToolkit.getChannel(brokerId);
		this.invocationClientServiceRegistry = invocationClientServiceRegistry;
	}

	@Override
	public IMethod getMethod(final String methodName) {
		return new IMethod() {
			@Override
			public void invoke(final Object invocationId, final Object parameter) {
				final MethodInvocationMessage message = new MethodInvocationMessage(invocationId, methodName, parameter);
				invokedInvocations.put(invocationId, new TimeStampedObject<Object>(invocationId));
				messageChannel.send(message, new ExceptionCallback(invocationClientServiceRegistry, invocationId));
			}
		};
	}

	@Override
	public ICancelService getCancelService() {
		return new ICancelService() {
			@Override
			public void canceled(final Object invocationId) {
				Assert.paramNotNull(invocationId, "invocationId");
				final TimeStampedObject<IMessageChannel> ackInvocation = acknowledgedInvocations.get(invocationId);
				if (ackInvocation != null) {
					final CancelMessage message = new CancelMessage(invocationId);
					ackInvocation.getObject().send(message, new ExceptionCallback(invocationClientServiceRegistry, invocationId));
					canceledInvocations.remove(invocationId);
				}
				else {
					canceledInvocations.put(invocationId, new TimeStampedObject<Object>(invocationId));
				}
			}
		};
	}

	@Override
	public IResponseService getResponseService() {
		return new IResponseService() {
			@Override
			public void response(final Object requestId, final Object response) {
				Assert.paramNotNull(requestId, "requestId");
				final TimeStampedObject<Tuple<Object, IMessageChannel>> request = interimRequests.remove(requestId);
				if (request != null) {
					final Tuple<Object, IMessageChannel> tuple = request.getObject();
					final ResponseMessage message = new ResponseMessage(requestId, response);
					tuple.getSecond().send(message, new ExceptionCallback(invocationClientServiceRegistry, tuple.getFirst()));
				}
				else {
					throw new IllegalStateException("The request id '" + requestId + "' is not known");
				}
			}
		};
	}

	void registerAcknowledge(final Object invocationId, final IMessageChannel replyChannel) {
		if (canceledInvocations.remove(invocationId) != null) {
			final CancelMessage message = new CancelMessage(invocationId);
			replyChannel.send(message, new ExceptionCallback(invocationClientServiceRegistry, invocationId));
		}
		else {
			acknowledgedInvocations.put(invocationId, new TimeStampedObject<IMessageChannel>(replyChannel));
		}
	}

	void registerInterimRequest(final Object invocationId, final Object requestId, final IMessageChannel replyChannel) {
		final Tuple<Object, IMessageChannel> tuple = new Tuple<Object, IMessageChannel>(invocationId, replyChannel);
		interimRequests.put(requestId, new TimeStampedObject<Tuple<Object, IMessageChannel>>(tuple));
	}

}
