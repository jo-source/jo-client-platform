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

import java.util.concurrent.atomic.AtomicInteger;

import org.jowidgets.message.api.IMessageChannel;
import org.jowidgets.message.api.IMessageReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class MockMessagingTest {

	private static final String MOCK_MESSAGING_BROKER_ID = "MOCK_MESSAGING_BROKER_ID";

	private MockMessaging messaging;
	private IMessageReceiver messageReceiver;
	private IMessageReceiver messageChannelMessageReceiver;

	@Before
	public void setUp() {
		messaging = new MockMessaging(MOCK_MESSAGING_BROKER_ID);

		messageReceiver = Mockito.mock(IMessageReceiver.class);
		messaging.getMessageReceiverBroker().setReceiver(messageReceiver);

		messageChannelMessageReceiver = Mockito.mock(IMessageReceiver.class);
		messaging.getMessageChannelBroker().setReceiver(messageChannelMessageReceiver);
	}

	@After
	public void tearDown() {
		messaging.dispose();
	}

	@Test
	public void testSendAndReceiveMessages() {
		final Object message = "foo";
		final Object message2 = "foo2";
		final Object replyMessage = "bar";

		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation) {
				final String messageArg = invocation.getArgumentAt(0, String.class);
				final IMessageChannel replyChannel = invocation.getArgumentAt(1, IMessageChannel.class);
				if (message.equals(messageArg)) {
					replyChannel.send(replyMessage, null);
				}
				return null;
			}
		}).when(messageReceiver).onMessage(Mockito.any(Object.class), Mockito.any(IMessageChannel.class));

		final MessageChannelBrokerMock channel = messaging.getMessageChannelBroker();
		channel.send(message, null);
		channel.send(message2, null);

		messaging.getMessageReceiverBroker().dispatchMessages();
		messaging.getMessageChannelBroker().dispatchReturnedMessages();

		Mockito.verify(messageReceiver, Mockito.times(1)).onMessage(Mockito.eq(message), Mockito.any(IMessageChannel.class));
		Mockito.verify(messageReceiver, Mockito.times(1)).onMessage(Mockito.eq(message2), Mockito.any(IMessageChannel.class));
		Mockito.verify(messageChannelMessageReceiver, Mockito.times(1)).onMessage(
				Mockito.eq(replyMessage),
				Mockito.any(IMessageChannel.class));
	}

	@Test
	public void testrequestResponseFeedback() {
		final int maxCount = 8;
		final AtomicInteger count = new AtomicInteger(0);

		final Object message = "foo";

		final Answer<Void> answer = new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation) {
				final IMessageChannel replyChannel = invocation.getArgumentAt(1, IMessageChannel.class);
				if (count.incrementAndGet() < maxCount) {
					replyChannel.send(message, null);
				}
				return null;
			}
		};
		Mockito.doAnswer(answer).when(messageReceiver).onMessage(Mockito.any(Object.class), Mockito.any(IMessageChannel.class));
		Mockito.doAnswer(answer).when(messageChannelMessageReceiver).onMessage(
				Mockito.any(Object.class),
				Mockito.any(IMessageChannel.class));

		messaging.getMessageChannelBroker().send(message, null);

		for (int i = 0; i < maxCount; i++) {
			messaging.getMessageReceiverBroker().dispatchMessages();
			messaging.getMessageChannelBroker().dispatchReturnedMessages();
		}

		Mockito.verify(messageReceiver, Mockito.times(maxCount / 2)).onMessage(
				Mockito.eq(message),
				Mockito.any(IMessageChannel.class));
		Mockito.verify(messageChannelMessageReceiver, Mockito.times(maxCount / 2)).onMessage(
				Mockito.eq(message),
				Mockito.any(IMessageChannel.class));

	}

}
