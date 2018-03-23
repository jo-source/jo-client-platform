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
import java.net.ConnectException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;
import org.jowidgets.classloading.tools.SharedClassLoadingObjectInputStream;
import org.jowidgets.message.api.IExceptionCallback;
import org.jowidgets.message.api.IMessageChannel;
import org.jowidgets.message.api.IMessageReceiver;
import org.jowidgets.message.api.MessageToolkit;
import org.jowidgets.util.Assert;
import org.jowidgets.util.io.IoUtils;

final class MessageBroker implements IMessageBroker, IMessageChannel {

	private final Object brokerId;
	private final String url;
	private final IHttpRequestInitializer httpRequestInitializer;
	private final HttpClient httpClient;
	private final ExecutorService incommingMessageExecutor;
	private final long sleepDurationAfterIoException;

	private final BlockingQueue<DeferredMessage> messageQueue;
	private final CountDownLatch sessionInitialized;
	private final Thread senderThread;
	private final Thread receiverThread;

	private volatile IMessageReceiver receiver;

	MessageBroker(
		final Object brokerId,
		final String url,
		final HttpClient httpClient,
		final ExecutorService incommingMessageExecutor,
		final IHttpRequestInitializer httpRequestInitializer,
		final long sleepDurationAfterIoException) {

		Assert.paramNotNull(brokerId, "brokerId");
		Assert.paramNotNull(url, "url");
		Assert.paramNotNull(httpClient, "httpClient");
		Assert.paramNotNull(incommingMessageExecutor, "incommingMessageExecutor");

		this.brokerId = brokerId;
		this.url = url;
		this.httpClient = httpClient;
		this.incommingMessageExecutor = incommingMessageExecutor;
		this.httpRequestInitializer = httpRequestInitializer;
		this.sleepDurationAfterIoException = sleepDurationAfterIoException;

		this.messageQueue = new LinkedBlockingQueue<DeferredMessage>();
		this.sessionInitialized = new CountDownLatch(1);
		this.senderThread = createSenderThread();
		this.receiverThread = createReceiverThread();

		senderThread.start();
		receiverThread.start();
	}

	private Thread createSenderThread() {
		final Thread result = new Thread(new MessageSenderLoop(), MessageBroker.class.getName() + "-messageSender");
		result.setDaemon(true);
		return result;
	}

	private Thread createReceiverThread() {
		final Thread result = new Thread(new MessageReceiverLoop(), MessageBroker.class.getName() + "-messageReceiver");
		result.setDaemon(true);
		return result;
	}

	@Override
	public boolean shutdown(final long timeout) throws InterruptedException {
		final long startTime = System.currentTimeMillis();

		if (senderThread.isAlive()) {
			senderThread.interrupt();
		}

		if (receiverThread.isAlive()) {
			receiverThread.interrupt();
		}

		senderThread.join(timeout);

		if (timeout == 0) {
			receiverThread.join(timeout);
		}
		else {
			final long elapsedTime = System.currentTimeMillis() - startTime;
			final long residualTimeout = timeout - elapsedTime;
			if (residualTimeout > 0) {
				receiverThread.join(residualTimeout);
			}
		}

		httpClient.getConnectionManager().shutdown();

		return !senderThread.isAlive() && !receiverThread.isAlive();
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
	public HttpClient getHttpClient() {
		return httpClient;
	}

	@Override
	public void setReceiver(final IMessageReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void send(final Object message, final IExceptionCallback exceptionCallback) {
		messageQueue.add(new DeferredMessage(message, exceptionCallback));
	}

	private void initializeHttpRequest(final HttpRequest request) {
		if (httpRequestInitializer != null) {
			httpRequestInitializer.initialize(request);
		}
	}

	private void checkStatusLine(final HttpResponse response) throws IOException {
		final StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() != 200) {
			throw new UnexpectedHttpStatusException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
		}
	}

	private static final class DeferredMessage {

		private final Object message;
		private final IExceptionCallback exceptionCallback;

		DeferredMessage(final Object message, final IExceptionCallback exceptionCallback) {
			this.message = message;
			this.exceptionCallback = exceptionCallback;
		}

		Object getMessage() {
			return message;
		}

		IExceptionCallback getExceptionCallback() {
			return exceptionCallback;
		}

	}

	private class MessageSenderLoop implements Runnable {

		@Override
		public void run() {
			try {
				sessionInitialized.await();
				while (!Thread.interrupted()) {
					trySendMessage();
				}
			}
			catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		private void trySendMessage() throws InterruptedException {
			final DeferredMessage message = messageQueue.take();
			try {
				sendMessage(message);
			}
			catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new InterruptedException("Send message was interrupted.");
			}
			catch (final Throwable t) {
				handleException(t, message.getExceptionCallback());
			}
		}

		private void sendMessage(final DeferredMessage message) throws IOException, InterruptedException {
			HttpPost request = null;
			HttpResponse response = null;
			try {
				request = createHttpRequest(message);
				response = httpClient.execute(request);
				checkStatusLine(response);
			}
			catch (final ConnectException e) {
				throw new MessageServerConnectException(tryExtractHost(request), e);
			}
			catch (final RuntimeException e) {
				if (request != null) {
					request.abort();
				}
				throw e;
			}
			finally {
				if (response != null) {
					EntityUtils.consume(response.getEntity());
				}
			}
		}

		private HttpHost tryExtractHost(final HttpPost request) {
			if (request != null) {
				try {
					return URIUtils.extractHost(request.getURI());
				}
				catch (final Exception e) {
					// ignore
				}
			}
			return null;
		}

		private HttpPost createHttpRequest(final DeferredMessage message) throws IOException {
			final HttpPost result = new HttpPost(url);
			initializeHttpRequest(result);
			result.setEntity(createMessageEntity(message.getMessage()));
			return result;
		}

		private ByteArrayEntity createMessageEntity(final Object message) throws IOException {
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			new ObjectOutputStream(byteArrayOutputStream).writeObject(message);
			return new ByteArrayEntity(byteArrayOutputStream.toByteArray());
		}

		private void handleException(final Throwable throwable, final IExceptionCallback exceptionCallback) {
			if (exceptionCallback != null) {
				exceptionCallback.exception(throwable);
			}
			else {
				MessageToolkit.handleExceptions(brokerId, throwable);
			}
		}
	}

	private class MessageReceiverLoop implements Runnable {

		@Override
		public void run() {
			try {
				while (!Thread.interrupted()) {
					tryReceiveMessages();
				}
			}
			catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		private void tryReceiveMessages() throws InterruptedException {
			try {
				receiveMessages();
			}
			catch (final IOException e) {
				MessageToolkit.handleExceptions(brokerId, e);
				// sleep more, because of network problems
				Thread.sleep(sleepDurationAfterIoException);
			}
			catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new InterruptedException("Receive messages was interrupted.");
			}
			catch (final Throwable t) {
				MessageToolkit.handleExceptions(brokerId, t);
			}
		}

		private void receiveMessages() throws IOException, InterruptedException {
			HttpGet request = null;
			HttpResponse response = null;
			try {
				request = createHttpRequest();
				response = httpClient.execute(request);

				checkStatusLine(response);
				sessionInitialized.countDown();

				final HttpEntity entity = response.getEntity();
				if (entity != null && entity.isStreaming()) {
					executeMessagesFromStream(entity.getContent());
				}
			}
			catch (final RuntimeException e) {
				if (request != null) {
					request.abort();
				}
				throw e;
			}
			finally {
				if (response != null) {
					EntityUtils.consume(response.getEntity());
				}
			}
		}

		private HttpGet createHttpRequest() {
			final HttpGet result = new HttpGet(url);
			initializeHttpRequest(result);
			return result;
		}

		private void executeMessagesFromStream(final InputStream inputStream) throws IOException {
			final ObjectInputStream objectInputStream = new SharedClassLoadingObjectInputStream(inputStream);
			try {
				final int objectCount = objectInputStream.readInt();
				for (int i = 0; i < objectCount; i++) {
					try {
						executeMessage(objectInputStream.readObject());
					}
					catch (final ClassNotFoundException e) {
						MessageToolkit.handleExceptions(brokerId, e);
					}
				}
			}
			finally {
				IoUtils.tryCloseSilent(objectInputStream);
			}
		}

		private void executeMessage(final Object message) {
			incommingMessageExecutor.execute(new Runnable() {
				@Override
				public void run() {
					final IMessageReceiver currentReceiver = receiver;
					if (currentReceiver != null) {
						currentReceiver.onMessage(message, MessageBroker.this);
					}
				}
			});
		}

	}

}
