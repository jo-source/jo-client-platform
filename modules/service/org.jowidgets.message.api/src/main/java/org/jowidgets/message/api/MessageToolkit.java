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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jowidgets.classloading.api.SharedClassLoader;
import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;

public final class MessageToolkit {

	private static final ILogger LOGGER = LoggerProvider.get(MessageToolkit.class);

	private static final IMessageToolkit INSTANCE = new MessageToolkitImpl();

	private static Map<Object, IMessageChannel> messageChannelBrokers = getChannels();
	private static Map<Object, IMessageReceiverBroker> messageReceiverBrokers = getReceivers();
	private static Map<Object, Set<IExceptionCallback>> exceptionCallbacks = new ConcurrentHashMap<Object, Set<IExceptionCallback>>();

	private MessageToolkit() {}

	public static IMessageToolkit getInstance() {
		return INSTANCE;
	}

	public static synchronized void addChannelBroker(final IMessageChannelBroker channelBroker) {
		if (channelBroker == null) {
			throw new IllegalArgumentException("Parameter 'channelBroker' must not be null");
		}
		messageChannelBrokers.put(channelBroker.getBrokerId(), channelBroker.getChannel());
	}

	public static synchronized void addReceiverBroker(final IMessageReceiverBroker receiver) {
		if (receiver == null) {
			throw new IllegalArgumentException("Parameter 'receiver' must not be null");
		}
		messageReceiverBrokers.put(receiver.getBrokerId(), receiver);
	}

	@Deprecated
	/**
	 * @deprecated jowidgets logging api will be used by default from now
	 * 
	 * Adds a exception callback for a defined broker id, this will not remove the other exception callbacks
	 * added before to this broker
	 * 
	 * @param brokerId The broker to set the callback for
	 * @param exceptionCallback The callback to set
	 */
	public static synchronized void addExceptionCallback(final Object brokerId, final IExceptionCallback exceptionCallback) {
		if (exceptionCallback == null) {
			throw new IllegalArgumentException("Parameter 'exceptionCallback' must not be null");
		}
		if (brokerId == null) {
			throw new IllegalArgumentException("Parameter 'brokerId' must not be null");
		}
		Set<IExceptionCallback> callbacks = exceptionCallbacks.get(brokerId);
		if (callbacks == null) {
			callbacks = new LinkedHashSet<IExceptionCallback>();
			exceptionCallbacks.put(brokerId, callbacks);
		}
		callbacks.add(exceptionCallback);
	}

	@Deprecated
	/**
	 * @deprecated jowidgets logging api will be used by default from now
	 * 
	 * Sets the exception callback for a defined broker id, this will remove all other exception callbacks for this
	 * broker
	 * 
	 * @param brokerId The broker to set the callback for
	 * @param exceptionCallback The callback to set
	 */
	public static synchronized void setExceptionCallback(final Object brokerId, final IExceptionCallback exceptionCallback) {
		if (exceptionCallback == null) {
			throw new IllegalArgumentException("Parameter 'exceptionCallback' must not be null");
		}
		if (brokerId == null) {
			throw new IllegalArgumentException("Parameter 'brokerId' must not be null");
		}

		final Set<IExceptionCallback> callbacks = new HashSet<IExceptionCallback>();
		callbacks.add(exceptionCallback);
		exceptionCallbacks.put(brokerId, callbacks);
	}

	@Deprecated
	/**
	 * @deprecated jowidgets logging api will be used by default from now
	 * 
	 * Removes a earlier registered exception callback for a given broker id
	 * 
	 * @param brokerId
	 * @param exceptionCallback
	 */
	public static synchronized void removeExceptionCallback(final Object brokerId, final IExceptionCallback exceptionCallback) {
		if (exceptionCallback == null) {
			throw new IllegalArgumentException("Parameter 'exceptionCallback' must not be null");
		}
		if (brokerId == null) {
			throw new IllegalArgumentException("Parameter 'brokerId' must not be null");
		}
		final Set<IExceptionCallback> callbacks = exceptionCallbacks.get(brokerId);
		if (callbacks != null) {
			callbacks.remove(exceptionCallback);
		}
	}

	public static IMessageChannel getChannel(final Object brokerId) {
		return getInstance().getChannel(brokerId);
	}

	public static void setReceiver(final Object brokerId, final IMessageReceiver receiver) {
		getInstance().setReceiver(brokerId, receiver);
	}

	public static void handleExceptions(final Object brokerId, final Throwable throwable) {
		if (brokerId == null) {
			throw new IllegalArgumentException("Parameter 'brokerId' must not be null");
		}
		if (throwable == null) {
			throw new IllegalArgumentException("Parameter 'throwable' must not be null");
		}
		final Set<IExceptionCallback> callbacks = getExceptionCallbacks(brokerId);
		if (callbacks.isEmpty()) {
			LOGGER.error("Error on message broker '" + brokerId + "'", throwable);
		}
		else {
			for (final IExceptionCallback exceptionCallback : callbacks) {
				exceptionCallback.exception(throwable);
			}
		}
	}

	private static Map<Object, IMessageChannel> getChannels() {
		final Map<Object, IMessageChannel> result = new ConcurrentHashMap<Object, IMessageChannel>();
		final ServiceLoader<IMessageChannelBroker> widgetServiceLoader = ServiceLoader.load(
				IMessageChannelBroker.class,
				SharedClassLoader.getCompositeClassLoader());
		final Iterator<IMessageChannelBroker> iterator = widgetServiceLoader.iterator();
		while (iterator.hasNext()) {
			final IMessageChannelBroker messageChannelBroker = iterator.next();
			result.put(messageChannelBroker.getBrokerId(), messageChannelBroker.getChannel());
		}
		return result;
	}

	private static Map<Object, IMessageReceiverBroker> getReceivers() {
		final Map<Object, IMessageReceiverBroker> result = new ConcurrentHashMap<Object, IMessageReceiverBroker>();
		final ServiceLoader<IMessageReceiverBroker> widgetServiceLoader = ServiceLoader.load(
				IMessageReceiverBroker.class,
				SharedClassLoader.getCompositeClassLoader());
		final Iterator<IMessageReceiverBroker> iterator = widgetServiceLoader.iterator();
		while (iterator.hasNext()) {
			final IMessageReceiverBroker messageReceiverBroker = iterator.next();
			result.put(messageReceiverBroker.getBrokerId(), messageReceiverBroker);
		}
		return result;
	}

	private static Set<IExceptionCallback> getExceptionCallbacks(final Object brokerId) {
		if (brokerId == null) {
			throw new IllegalArgumentException("Parameter 'brokerId' must not be null");
		}
		final Set<IExceptionCallback> result = new HashSet<IExceptionCallback>();
		final Set<IExceptionCallback> callbacks = exceptionCallbacks.get(brokerId);
		if (callbacks != null) {
			result.addAll(callbacks);
		}
		return result;
	}

	private static class MessageToolkitImpl implements IMessageToolkit {

		@Override
		public IMessageChannel getChannel(final Object brokerId) {
			if (brokerId == null) {
				throw new IllegalArgumentException("Parameter 'brokerId' must not be null");
			}
			return messageChannelBrokers.get(brokerId);
		}

		@Override
		public void setReceiver(final Object brokerId, final IMessageReceiver receiver) {
			if (brokerId == null) {
				throw new IllegalArgumentException("Parameter 'brokerId' must not be null");
			}
			if (receiver == null) {
				throw new IllegalArgumentException("Parameter 'receiver' must not be null");
			}
			final IMessageReceiverBroker messageBrokerServer = messageReceiverBrokers.get(brokerId);
			if (messageBrokerServer != null) {
				messageBrokerServer.setReceiver(receiver);
			}
			else {
				throw new IllegalArgumentException("No broker server found for parameter 'brokerId' with value '"
					+ brokerId
					+ "'");
			}
		}

	}
}
