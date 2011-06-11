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

package org.jowidgets.message.api;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public final class MessageToolkit {

	private static final IMessageToolkit INSTANCE = new MessageToolkitImpl();

	private static Map<Object, IMessageClient> messageBrokerClients = getClients();
	private static Map<Object, IMessageBrokerServer> messageBrokerServers = getServer();

	private MessageToolkit() {}

	public static IMessageToolkit getInstance() {
		return INSTANCE;
	}

	public static synchronized void addBrokerClient(final IMessageBrokerClient client) {
		if (client == null) {
			throw new IllegalArgumentException("Parameter 'client' must not be null");
		}
		messageBrokerClients.put(client.getBrokerId(), client.getClient());
	}

	public static synchronized void addBrokerServer(final IMessageBrokerServer server) {
		if (server == null) {
			throw new IllegalArgumentException("Parameter 'server' must not be null");
		}
		messageBrokerServers.put(server.getBrokerId(), server);
	}

	public static IMessageClient getClient(final Object brokerId) {
		return getInstance().getClient(brokerId);
	}

	public static void setReceiver(final Object brokerId, final IMessageReceiver receiver) {
		getInstance().setReceiver(brokerId, receiver);
	}

	private static Map<Object, IMessageClient> getClients() {
		final Map<Object, IMessageClient> result = new ConcurrentHashMap<Object, IMessageClient>();
		final ServiceLoader<IMessageBrokerClient> widgetServiceLoader = ServiceLoader.load(IMessageBrokerClient.class);
		final Iterator<IMessageBrokerClient> iterator = widgetServiceLoader.iterator();
		while (iterator.hasNext()) {
			final IMessageBrokerClient messageBrokerClient = iterator.next();
			result.put(messageBrokerClient.getBrokerId(), messageBrokerClient.getClient());
		}
		return result;
	}

	private static Map<Object, IMessageBrokerServer> getServer() {
		final Map<Object, IMessageBrokerServer> result = new ConcurrentHashMap<Object, IMessageBrokerServer>();
		final ServiceLoader<IMessageBrokerServer> widgetServiceLoader = ServiceLoader.load(IMessageBrokerServer.class);
		final Iterator<IMessageBrokerServer> iterator = widgetServiceLoader.iterator();
		while (iterator.hasNext()) {
			final IMessageBrokerServer messageBrokerServer = iterator.next();
			result.put(messageBrokerServer.getBrokerId(), messageBrokerServer);
		}
		return result;
	}

	private static class MessageToolkitImpl implements IMessageToolkit {

		@Override
		public IMessageClient getClient(final Object brokerId) {
			if (brokerId == null) {
				throw new IllegalArgumentException("Parameter 'brokerId' must not be null");
			}
			return messageBrokerClients.get(brokerId);
		}

		@Override
		public void setReceiver(final Object brokerId, final IMessageReceiver receiver) {
			if (brokerId == null) {
				throw new IllegalArgumentException("Parameter 'brokerId' must not be null");
			}
			if (receiver == null) {
				throw new IllegalArgumentException("Parameter 'receiver' must not be null");
			}
			final IMessageBrokerServer messageBrokerServer = messageBrokerServers.get(brokerId);
			if (messageBrokerServer != null) {
				messageBrokerServer.setMessageReceiver(receiver);
			}
		}
	}
}
