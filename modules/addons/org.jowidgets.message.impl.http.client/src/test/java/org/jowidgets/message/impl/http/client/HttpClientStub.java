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

package org.jowidgets.message.impl.http.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.mockito.Mockito;

final class HttpClientStub implements HttpClient {

	static final StatusLine OK_STATUS_LINE = new BasicStatusLine(new ProtocolVersion("http", 1, 1), 200, "ok");
	static final StatusLine INTERNAL_SERVER_ERROR_STATUS_LINE = new BasicStatusLine(
		new ProtocolVersion("http", 1, 1),
		500,
		"Internal server error");

	private static final int POLL_INTERVAL = 10000;

	private final int messagesToConsume;
	private final int getErrorsToConsume;
	private final CountDownLatch messagesConsumedLatch;
	private final CountDownLatch errorsConsumedLatch;
	private final AtomicBoolean firstGetWithSuccess;
	private final AtomicInteger createdMessages;
	private final AtomicInteger createdGetErrors;
	private final AtomicInteger postInvocations;
	private final AtomicInteger getInvocations;

	private final ClientConnectionManager connectionManager;
	private final BlockingQueue<MessageStub> queue;

	private final AtomicReference<StatusLine> postStatus;
	private final AtomicReference<StatusLine> getStatus;
	private final AtomicReference<StatusLine> firstGetStatus;

	HttpClientStub(final int messagesToConsume, final int getErrorsToConsume) {
		this.messagesToConsume = messagesToConsume;
		this.getErrorsToConsume = getErrorsToConsume;

		this.connectionManager = Mockito.mock(ClientConnectionManager.class);
		this.queue = new LinkedBlockingQueue<MessageStub>();

		this.firstGetWithSuccess = new AtomicBoolean(true);
		this.createdMessages = new AtomicInteger(0);
		this.createdGetErrors = new AtomicInteger(0);
		this.postInvocations = new AtomicInteger(0);
		this.getInvocations = new AtomicInteger(0);
		this.messagesConsumedLatch = new CountDownLatch(messagesToConsume);
		this.errorsConsumedLatch = new CountDownLatch(getErrorsToConsume);

		this.postStatus = new AtomicReference<StatusLine>(OK_STATUS_LINE);
		this.getStatus = new AtomicReference<StatusLine>(OK_STATUS_LINE);
		this.firstGetStatus = new AtomicReference<StatusLine>(OK_STATUS_LINE);
	}

	/**
	 * Wait until all messages was consumed or timeout occurred
	 * 
	 * @param timeout the maximum time to wait
	 * @param unit the time unit of the timeout argument
	 * 
	 * @return true if the the errors consumed, false if the timeout has been reached
	 * 
	 * @throws InterruptedException If the waiting thread is interrupted
	 */
	boolean awaitMessagesConsumed(final long timeout, final TimeUnit unit) throws InterruptedException {
		return messagesConsumedLatch.await(timeout, unit);
	}

	/**
	 * Wait until all errors was consumed or timeout occurred
	 * 
	 * @param timeout the maximum time to wait
	 * @param unit the time unit of the timeout argument
	 * 
	 * @return true if the the errors consumed, false if the timeout has been reached
	 * 
	 * @throws InterruptedException If the waiting thread is interrupted
	 */
	boolean awaitErrorsConsumed(final long timeout, final TimeUnit unit) throws InterruptedException {
		return errorsConsumedLatch.await(timeout, unit);
	}

	int getGetInvocationCount() {
		return getInvocations.get();
	}

	int getPostInvocationCount() {
		return postInvocations.get();
	}

	void setPostStatus(final StatusLine status) {
		this.postStatus.set(status);
	}

	void setFirstGetStatus(final StatusLine status) {
		this.firstGetStatus.set(status);
	}

	void setGetStatus(final StatusLine status) {
		this.getStatus.set(status);
	}

	@Override
	public HttpParams getParams() {
		return null;
	}

	@Override
	public ClientConnectionManager getConnectionManager() {
		return connectionManager;
	}

	@Override
	public HttpResponse execute(final HttpUriRequest request) throws IOException, ClientProtocolException {
		if (request instanceof HttpPost) {
			return post((HttpPost) request);
		}
		if (request instanceof HttpGet) {
			return get((HttpGet) request);
		}
		throw new UnsupportedOperationException();
	}

	private HttpResponse get(final HttpGet request) throws IOException, ClientProtocolException {
		getInvocations.incrementAndGet();

		final boolean firstGet = firstGetWithSuccess.get();
		if (firstGet) {
			final StatusLine status = firstGetStatus.get();
			final BasicHttpResponse result = new BasicHttpResponse(status);
			if (status.getStatusCode() == 200) {
				return firstGetWithSuccess(request, result);
			}
			else {
				return firstGetWithError(request, result);
			}
		}
		else {
			final StatusLine status = getStatus.get();
			final BasicHttpResponse result = new BasicHttpResponse(status);
			if (status.getStatusCode() == 200) {
				return getWithSuccess(request, result);
			}
			else {
				return getWithError(request, result);
			}
		}
	}

	private HttpResponse firstGetWithSuccess(final HttpGet request, final BasicHttpResponse result)
			throws IOException, ClientProtocolException {

		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream(os);

		if (firstGetWithSuccess.compareAndSet(true, false)) {
			// return immediately to send new session id to client
			oos.writeInt(0);
			oos.flush();
		}

		return result;
	}

	private HttpResponse firstGetWithError(final HttpGet request, final BasicHttpResponse result)
			throws IOException, ClientProtocolException {

		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream(os);

		// return immediately to send new session id to client
		oos.flush();

		return result;
	}

	private HttpResponse getWithSuccess(final HttpGet request, final BasicHttpResponse result)
			throws IOException, ClientProtocolException {

		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream(os);

		if (createdMessages.get() < messagesToConsume) {
			final List<MessageStub> messages = pollMessages(POLL_INTERVAL);
			createdMessages.getAndAdd(messages.size());
			oos.writeInt(messages.size());
			for (final MessageStub msg : messages) {
				oos.flush();
				oos.writeObject(msg.getResult());
			}
			oos.flush();
			result.setEntity(new HttpEntityStub(os.toByteArray(), messages.size(), messagesConsumedLatch));
		}
		else {
			//after all expected messages was created, sleep until interrupt
			try {
				Thread.sleep(Long.MAX_VALUE);
			}
			catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		return result;
	}

	private HttpResponse getWithError(final HttpGet request, final BasicHttpResponse result)
			throws IOException, ClientProtocolException {

		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream(os);

		if (createdGetErrors.incrementAndGet() <= getErrorsToConsume) {
			//do not write anything in error case
			//oos.writeInt(0);
			oos.flush();
			result.setEntity(new HttpEntityStub(os.toByteArray(), 1, errorsConsumedLatch));
		}

		return result;
	}

	List<MessageStub> pollMessages(final long pollInterval) {
		final List<MessageStub> msgs = new LinkedList<MessageStub>();
		if (queue.drainTo(msgs) == 0) {
			try {
				final MessageStub msg = queue.poll(pollInterval, TimeUnit.MILLISECONDS);
				if (msg != null) {
					msgs.add(msg);
				}
			}
			catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return msgs;
	}

	private HttpResponse post(final HttpPost request) throws IOException, ClientProtocolException {
		postInvocations.incrementAndGet();
		final StatusLine status = postStatus.get();
		if (status.getStatusCode() == 200) {
			try {
				queue.put((MessageStub) new ObjectInputStream(request.getEntity().getContent()).readObject());
			}
			catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
		return new BasicHttpResponse(status);
	}

	@Override
	public HttpResponse execute(final HttpUriRequest request, final HttpContext context)
			throws IOException, ClientProtocolException {
		throw new UnsupportedOperationException();
	}

	@Override
	public HttpResponse execute(final HttpHost target, final HttpRequest request) throws IOException, ClientProtocolException {
		throw new UnsupportedOperationException();
	}

	@Override
	public HttpResponse execute(final HttpHost target, final HttpRequest request, final HttpContext context)
			throws IOException, ClientProtocolException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T execute(final HttpUriRequest request, final ResponseHandler<? extends T> responseHandler)
			throws IOException, ClientProtocolException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T execute(
		final HttpUriRequest request,
		final ResponseHandler<? extends T> responseHandler,
		final HttpContext context) throws IOException, ClientProtocolException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T execute(final HttpHost target, final HttpRequest request, final ResponseHandler<? extends T> responseHandler)
			throws IOException, ClientProtocolException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T execute(
		final HttpHost target,
		final HttpRequest request,
		final ResponseHandler<? extends T> responseHandler,
		final HttpContext context) throws IOException, ClientProtocolException {
		throw new UnsupportedOperationException();
	}

}
