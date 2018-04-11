/*
 * Copyright (c) 2013, grossmann
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

package org.jowidgets.cap.tools.starter.client.remoting;

import org.jowidgets.cap.remoting.common.RemotingBrokerId;
import org.jowidgets.message.api.MessageToolkit;
import org.jowidgets.message.impl.http.client.HttpRequestInitializerComposite;
import org.jowidgets.message.impl.http.client.IHttpRequestInitializer;
import org.jowidgets.message.impl.http.client.IMessageBroker;
import org.jowidgets.message.impl.http.client.MessageBrokerBuilder;
import org.jowidgets.message.impl.http.client.UserLocaleHttpRequestInitializer;
import org.jowidgets.security.impl.http.client.BasicAuthenticationInitializer;

public class MessagingInitializer {

	private final Object brokerId;
	private final String serverDefaultHost;
	private boolean messagingInitialized;

	public MessagingInitializer(final String serverDefaultHost) {
		this(RemotingBrokerId.DEFAULT_BROKER_ID, serverDefaultHost);
	}

	public MessagingInitializer(final Object brokerId, final String serverDefaultHost) {
		this.brokerId = brokerId != null ? brokerId : RemotingBrokerId.DEFAULT_BROKER_ID;
		this.serverDefaultHost = serverDefaultHost;
		this.messagingInitialized = false;
	}

	public synchronized void initializeMessaging() {
		if (!messagingInitialized) {
			final MessageBrokerBuilder builder = new MessageBrokerBuilder(brokerId);
			builder.setUrl(getUrl(serverDefaultHost));
			final IHttpRequestInitializer httpRequestInitializer = new HttpRequestInitializerComposite(
				BasicAuthenticationInitializer.getInstance(),
				UserLocaleHttpRequestInitializer.getInstance());
			builder.setHttpRequestInitializer(httpRequestInitializer);
			final IMessageBroker messageBroker = builder.build();
			MessageToolkit.addChannelBroker(messageBroker);
			MessageToolkit.addReceiverBroker(messageBroker);
			messagingInitialized = true;
		}
	}

	private static String getUrl(final String serverDefaultHost) {
		String result = System.getProperty("server.url");
		if (result == null) {
			result = System.getProperty("jnlp.server.url");
		}
		if (result == null) {
			result = serverDefaultHost;
		}
		return result;
	}
}
