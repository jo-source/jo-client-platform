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

package org.jowidgets.message.impl.socket;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jowidgets.message.api.IMessageChannelBroker;
import org.jowidgets.message.api.IMessageReceiverBroker;
import org.jowidgets.util.Assert;

public final class MessageBrokerBuilder {

	private static final Set<Object> KNOWN_RECEIVER_BROKERS = new HashSet<Object>();

	private final Object brokerId;

	private String host;
	private int port;
	private String receiverHost;
	private int receiverPort;
	private Executor sendExecutor;
	private Executor receiveExecutor;

	public MessageBrokerBuilder(final Object brokerId) {
		Assert.paramNotNull(brokerId, "brokerId");
		this.brokerId = brokerId;
		this.port = -1;
		this.receiverPort = -1;
		this.sendExecutor = Executors.newFixedThreadPool(50);
		this.receiveExecutor = Executors.newFixedThreadPool(50);
	}

	public MessageBrokerBuilder setHost(final String host) {
		Assert.paramNotNull(host, "host");
		this.host = host;
		return this;
	}

	public MessageBrokerBuilder setPort(final int port) {
		Assert.paramNotNull(port, "port");
		this.port = port;
		return this;
	}

	public MessageBrokerBuilder setReceiverHost(final String host) {
		Assert.paramNotNull(host, "host");
		this.receiverHost = host;
		return this;
	}

	public MessageBrokerBuilder setReceiverPort(final int port) {
		Assert.paramNotNull(port, "port");
		this.receiverPort = port;
		return this;
	}

	public MessageBrokerBuilder setSendExecutor(final Executor executor) {
		Assert.paramNotNull(executor, "executor");
		this.sendExecutor = executor;
		return this;
	}

	public MessageBrokerBuilder setReceiveExecutor(final Executor executor) {
		Assert.paramNotNull(executor, "executor");
		this.receiveExecutor = executor;
		return this;
	}

	public IMessageChannelBroker buildChannel() {
		return new MessageChannelBroker(
			brokerId,
			new Peer(host, port),
			new Peer(receiverHost, receiverPort),
			sendExecutor,
			new MessageReceiverBroker(brokerId, new Peer(host, port), sendExecutor, receiveExecutor));
	}

	public synchronized IMessageReceiverBroker buildReceiver() {
		if (KNOWN_RECEIVER_BROKERS.contains(brokerId)) {
			throw new IllegalStateException("An receiver broker with the id '" + brokerId + "' was already created");
		}
		final IMessageReceiverBroker result = new MessageReceiverBroker(
			brokerId,
			new Peer(host, port),
			sendExecutor,
			receiveExecutor);
		KNOWN_RECEIVER_BROKERS.add(brokerId);
		return result;
	}
}
