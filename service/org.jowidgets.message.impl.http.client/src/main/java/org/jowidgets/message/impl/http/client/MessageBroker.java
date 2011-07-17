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

package org.jowidgets.message.impl.http.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.jowidgets.message.api.IExceptionCallback;
import org.jowidgets.message.api.IMessageChannel;
import org.jowidgets.message.api.IMessageReceiver;
import org.jowidgets.message.api.MessageToolkit;
import org.jowidgets.util.concurrent.DaemonThreadFactory;

final class MessageBroker implements IMessageBroker, IMessageChannel {

	private static final class DeferredMessage {
		private final Object message;
		private final IExceptionCallback exceptionCallback;

		private DeferredMessage(final Object message, final IExceptionCallback exceptionCallback) {
			this.message = message;
			this.exceptionCallback = exceptionCallback;
		}
	}

	private final Object brokerId;
	private final String url;
	private final HttpClient httpClient;
	private final BlockingQueue<DeferredMessage> outQueue = new LinkedBlockingQueue<DeferredMessage>();
	private final Executor incomingExecutor = Executors.newFixedThreadPool(
			Runtime.getRuntime().availableProcessors() * 2,
			new DaemonThreadFactory());

	private IHttpRequestInitializer httpRequestInitializer;

	private volatile IMessageReceiver receiver;

	public MessageBroker(final Object brokerId, final String url, final HttpClient httpClient) {
		this.brokerId = brokerId;
		this.url = url;
		this.httpClient = httpClient;
		createSenderThread().start();
		createReceiverThread().start();
	}

	@Override
	public Object getBrokerId() {
		return brokerId;
	}

	@Override
	public IMessageChannel getChannel() {
		return this;
	}

	@Override
	public void setReceiver(final IMessageReceiver receiver) {
		this.receiver = receiver;
	}

	public void setHttpRequestInitializer(final IHttpRequestInitializer httpRequestInitializer) {
		this.httpRequestInitializer = httpRequestInitializer;
	}

	@Override
	public void send(final Object message, final IExceptionCallback exceptionCallback) {
		outQueue.add(new DeferredMessage(message, exceptionCallback));
	}

	private Thread createSenderThread() {
		final Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				sendMessages();
			}
		});
		thread.setDaemon(true);
		return thread;
	}

	private void sendMessages() {
		try {
			for (;;) {
				final DeferredMessage msg = outQueue.take();
				try {
					final HttpPost request = new HttpPost(url);
					if (httpRequestInitializer != null) {
						httpRequestInitializer.initialize(request);
					}
					final byte[] data = createMessageData(msg.message);
					request.setEntity(new ByteArrayEntity(data));
					try {
						final HttpResponse response = httpClient.execute(request);
						try {
							final StatusLine statusLine = response.getStatusLine();
							if (statusLine.getStatusCode() != 200) {
								throw new IOException("Invalid HTTP response: " + statusLine);
							}
						}
						finally {
							final HttpEntity entity = response.getEntity();
							if (entity != null) {
								entity.getContent().close();
							}
						}
					}
					catch (final IOException e) {
						if (msg.exceptionCallback != null) {
							msg.exceptionCallback.exception(e);
						}
						else {
							MessageToolkit.handleExceptions(brokerId, e);
						}
						// sleep more, because of network problems
						Thread.sleep(10000);
					}
				}
				catch (final Throwable t) {
					if (msg.exceptionCallback != null) {
						msg.exceptionCallback.exception(t);
					}
					else {
						MessageToolkit.handleExceptions(brokerId, t);
					}
					Thread.sleep(10);
				}
			}
		}
		catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private byte[] createMessageData(final Object data) throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		new ObjectOutputStream(bos).writeObject(data);
		return bos.toByteArray();
	}

	private Thread createReceiverThread() {
		final Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				receiveMessages();
			}
		});
		thread.setDaemon(true);
		return thread;
	}

	private void receiveMessages() {
		try {
			for (;;) {
				try {
					final HttpGet request = new HttpGet(url);
					if (httpRequestInitializer != null) {
						httpRequestInitializer.initialize(request);
					}
					final HttpResponse response = httpClient.execute(request);
					final StatusLine statusLine = response.getStatusLine();
					final HttpEntity entity = response.getEntity();
					if (entity != null) {
						final InputStream is = entity.getContent();
						try {
							if (statusLine.getStatusCode() != 200) {
								throw new IOException("Invalid HTTP response: " + statusLine);
							}
							final ObjectInputStream ois = new ObjectInputStream(is);
							final int num = ois.readInt();
							for (int i = 0; i < num; i++) {
								try {
									final Object msg = ois.readObject();
									incomingExecutor.execute(new Runnable() {
										@Override
										public void run() {
											if (receiver != null) {
												receiver.onMessage(msg, MessageBroker.this);
											}
										}
									});
								}
								catch (final ClassNotFoundException e) {
									MessageToolkit.handleExceptions(brokerId, e);
								}
							}
						}
						finally {
							is.close();
						}
					}
				}
				catch (final IOException e) {
					MessageToolkit.handleExceptions(brokerId, e);
					Thread.sleep(10000);
				}
			}
		}
		catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
