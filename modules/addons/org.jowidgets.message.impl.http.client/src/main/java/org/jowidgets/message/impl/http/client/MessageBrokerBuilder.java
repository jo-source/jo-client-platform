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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.jowidgets.util.Assert;
import org.jowidgets.util.concurrent.DaemonThreadFactory;

public final class MessageBrokerBuilder {

	private static final long DEFAULT_SLEEP_DURATION_AFTER_IO_EXCEPTION = 10000;

	private final Object brokerId;

	private String url;
	private HttpClient httpClient;
	private IHttpRequestInitializer httpRequestInitializer;
	private ExecutorService executorService;
	private long sleepDurationAfterIoException;

	public MessageBrokerBuilder(final Object brokerId) {
		Assert.paramNotNull(brokerId, "brokerId");
		this.brokerId = brokerId;
		this.sleepDurationAfterIoException = DEFAULT_SLEEP_DURATION_AFTER_IO_EXCEPTION;
	}

	public MessageBrokerBuilder setUrl(final String url) {
		Assert.paramNotNull(url, "url");
		this.url = url;
		return this;
	}

	public MessageBrokerBuilder setHttpClient(final HttpClient httpClient) {
		Assert.paramNotNull(httpClient, "httpClient");
		this.httpClient = httpClient;
		return this;
	}

	public MessageBrokerBuilder setHttpRequestInitializer(final IHttpRequestInitializer httpRequestInitializer) {
		Assert.paramNotNull(httpRequestInitializer, "httpRequestInitializer");
		this.httpRequestInitializer = httpRequestInitializer;
		return this;
	}

	public MessageBrokerBuilder setIncommingMessageExecutor(final ExecutorService executorService) {
		Assert.paramNotNull(executorService, "executorService");
		this.executorService = executorService;
		return this;
	}

	public MessageBrokerBuilder setSleepDurationMillisAfterIoException(final long sleepDurationAfterIoException) {
		this.sleepDurationAfterIoException = sleepDurationAfterIoException;
		return this;
	}

	private HttpClient getOrCreateHttpClient() {
		if (httpClient != null) {
			return httpClient;
		}
		else {
			return new DefaultHttpClient(new ThreadSafeClientConnManager());
		}
	}

	private ExecutorService getOrCreateExecutorService() {
		if (executorService != null) {
			return executorService;
		}
		else {
			return Executors.newFixedThreadPool(
					Runtime.getRuntime().availableProcessors() * 2,
					new DaemonThreadFactory(MessageBroker.class.getName() + "-incommingMessageExecutor-"));
		}
	}

	public IMessageBroker build() {
		if (url == null) {
			throw new IllegalStateException("url must be set");
		}
		return new MessageBroker(
			brokerId,
			url,
			getOrCreateHttpClient(),
			getOrCreateExecutorService(),
			httpRequestInitializer,
			sleepDurationAfterIoException);
	}

}
