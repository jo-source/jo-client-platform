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

package org.jowidgets.cap.sample.app.server.starter;

import org.jowidgets.cap.sample.app.common.message.BrokerIds;
import org.jowidgets.message.api.IMessageClient;
import org.jowidgets.message.api.IMessageReceiver;
import org.jowidgets.message.api.MessageToolkit;
import org.jowidgets.message.impl.p2p.simple.MessageBrokerBuilder;

public final class SampleServerStarter {

	private SampleServerStarter() {}

	public static void main(final String[] args) {
		final MessageBrokerBuilder builder = new MessageBrokerBuilder(BrokerIds.DEFAULT_MESSAGE_BROKER_ID);
		builder.setHost("127.0.0.1");
		builder.setPort(5660);
		MessageToolkit.addBrokerClient(builder.buildClient());
		MessageToolkit.addBrokerServer(builder.buildServer());

		final IMessageClient client = MessageToolkit.getClient(BrokerIds.DEFAULT_MESSAGE_BROKER_ID);

		MessageToolkit.setReceiver(BrokerIds.DEFAULT_MESSAGE_BROKER_ID, new IMessageReceiver() {
			@Override
			public void onMessage(final Object message, final Object replyPeerId) {
				//CHECKSTYLE:OFF
				System.out.println(message);
				//CHECKSTYLE:ON
				client.getMessageChannel(replyPeerId).send("Response on: " + message, null);
			}
		});

		//CHECKSTYLE:OFF
		System.out.println("Sample server started");
		//CHECKSTYLE:ON
		while (true) {
			try {
				Thread.sleep(1000000000);
			}
			catch (final InterruptedException e) {
			}
		}
	}
}
