/*
 * Copyright (c) 2011, H.Westphal
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

package org.jowidgets.message.impl.akka;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.jowidgets.message.api.IExceptionCallback;
import org.jowidgets.message.api.IMessageChannel;
import org.jowidgets.message.api.IMessageReceiver;
import org.jowidgets.message.api.MessageToolkit;
import org.junit.Assert;
import org.junit.Test;

public class MessageBrokerTest {

	@Test(timeout = 5000)
	public void testPingPong() throws InterruptedException {
		final List<Throwable> throwables = new LinkedList<Throwable>();
		final StringBuilder result = new StringBuilder();
		final CountDownLatch latch = new CountDownLatch(4);

		final MessageBroker server = MessageBroker.create("server");
		server.getActorRef().start();
		MessageToolkit.addReceiverBroker(server);

		final MessageBroker client = MessageBroker.create("client", server.getActorRef());
		client.getActorRef().start();
		MessageToolkit.addChannelBroker(client);
		MessageToolkit.addReceiverBroker(client);

		MessageToolkit.setReceiver("server", new IMessageReceiver() {
			@Override
			public void onMessage(final Object message, final IMessageChannel replyChannel) {
				if ("ping".equals(message)) {
					result.append(message);
					replyChannel.send("pong", new IExceptionCallback() {
						@Override
						public void exception(final Throwable throwable) {
							throwables.add(throwable);
						}
					});
				}
			}
		});

		MessageToolkit.setReceiver("client", new IMessageReceiver() {
			@Override
			public void onMessage(final Object message, final IMessageChannel replyChannel) {
				if ("pong".equals(message)) {
					result.append(message);
					latch.countDown();
					replyChannel.send("ping", new IExceptionCallback() {
						@Override
						public void exception(final Throwable throwable) {
							throwables.add(throwable);
						}
					});
				}
			}
		});

		MessageToolkit.getChannel("client").send("ping", new IExceptionCallback() {
			@Override
			public void exception(final Throwable throwable) {
				throwables.add(throwable);
			}
		});

		latch.await();
		Assert.assertEquals(0, throwables.size());
		Assert.assertEquals("pingpongpingpongpingpongpingpong", result.toString());
	}
}
