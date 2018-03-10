/*
 * Copyright (c) 2018, grossmann
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

package org.jowidgets.message.impl.mock;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jowidgets.message.api.IExceptionCallback;
import org.jowidgets.message.api.IMessageChannel;
import org.jowidgets.message.api.IMessageChannelBroker;
import org.jowidgets.message.api.IMessageReceiver;

public final class MessageChannelBrokerMock implements IMessageChannelBroker, IMessageChannel {

	private final MessageReceiverBrokerMock messageReceiverBroker;
	private final BlockingQueue<QueuedMessage> returnedMessages;

	private IMessageReceiver receiver;

	MessageChannelBrokerMock(final Object brokerId) {
		this.messageReceiverBroker = new MessageReceiverBrokerMock(brokerId);
		this.returnedMessages = new LinkedBlockingQueue<QueuedMessage>();
	}

	@Override
	public void send(final Object message, final IExceptionCallback exceptionCallback) {
		messageReceiverBroker.addMessage(message, new IMessageChannel() {
			@Override
			public void send(final Object message, final IExceptionCallback exceptionCallback) {
				returnedMessages.add(new QueuedMessage(message, MessageChannelBrokerMock.this));
			}
		});
	}

	@Override
	public Object getBrokerId() {
		return messageReceiverBroker.getBrokerId();
	}

	MessageReceiverBrokerMock getMessageReceiverBroker() {
		return messageReceiverBroker;
	}

	@Override
	public void setReceiver(final IMessageReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public IMessageChannel getChannel() {
		return this;
	}

	public void dispatchReturnedMessages() {
		while (returnedMessages.size() > 0) {
			final QueuedMessage message = returnedMessages.poll();
			if (receiver != null) {
				receiver.onMessage(message.getMessage(), message.getReplyChannel());
			}
		}
	}

}
