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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import org.jowidgets.message.api.IMessageReceiver;
import org.jowidgets.message.api.IMessageReceiverBroker;
import org.jowidgets.message.api.MessageToolkit;

public class MessageReceiverBroker implements IMessageReceiverBroker {

	private final Object brokerId;
	private final Peer peer;
	private final Executor receiveExecutor;
	private final Executor sendExecutor;
	private final BlockingQueue<QueuedMessage> messages;

	private IMessageReceiver receiver;

	MessageReceiverBroker(final Object brokerId, final Peer peer, final Executor sendExecutor, final Executor receiveExecutor) {
		super();
		this.brokerId = brokerId;
		this.peer = peer;
		this.sendExecutor = sendExecutor;
		this.receiveExecutor = receiveExecutor;
		this.messages = new LinkedBlockingQueue<QueuedMessage>();
		start();
	}

	@Override
	public Object getBrokerId() {
		return brokerId;
	}

	@Override
	public void setReceiver(final IMessageReceiver receiver) {
		this.receiver = receiver;
	}

	private void start() {
		receiveExecutor.execute(createCommunicationRunnable());
		receiveExecutor.execute(createMessageDispatcherRunnable());
	}

	private Runnable createMessageDispatcherRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						final QueuedMessage message = messages.take();
						receiveExecutor.execute(new Runnable() {
							@Override
							public void run() {
								if (receiver != null) {
									receiver.onMessage(message.getMessage(), message.getReplyChannel());
								}
							}
						});
					}
					catch (final InterruptedException e) {
					}
				}
			}
		};
	}

	private Runnable createCommunicationRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				ServerSocket serverSocket = null;
				try {
					serverSocket = new ServerSocket(peer.getPort());
					while (true) {
						Socket client = null;
						ObjectInputStream ooi = null;
						try {
							client = serverSocket.accept();
							ooi = new ObjectInputStream(client.getInputStream());
							final Object object = ooi.readObject();

							if (object instanceof Message) {
								final Message message = (Message) object;
								final InetAddress inetAddress = client.getInetAddress();
								final Peer replyPeer = new Peer(inetAddress.getHostName(), message.getReplyPort());
								final MessageChannel replyChannel = new MessageChannel(peer, replyPeer, sendExecutor);
								messages.add(new QueuedMessage(message.getContent(), replyChannel));
							}

						}
						catch (final Exception e) {
							MessageToolkit.handleExceptions(brokerId, e);
						}
						finally {
							try {
								client.close();
							}
							catch (final Exception exception) {
							}
							try {
								ooi.close();
							}
							catch (final Exception exception) {
							}
						}
					}
				}
				catch (final IOException e) {
					throw new RuntimeException(e);
				}
				finally {
					try {
						serverSocket.close();
					}
					catch (final Exception exception) {
					}
				}
			}
		};
	}
}
