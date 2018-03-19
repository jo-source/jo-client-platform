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

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.jowidgets.logging.tools.JUnitLogger;
import org.jowidgets.logging.tools.JUnitLoggerProvider;
import org.jowidgets.message.api.IExceptionCallback;
import org.jowidgets.message.api.IMessageReceiver;
import org.jowidgets.message.api.MessageToolkit;
import org.jowidgets.util.mock.ScheduledExecutorServiceMock;
import org.junit.Test;
import org.mockito.Mockito;

import junit.framework.Assert;

public class MessageBrokerTest {

	private static long SYNC_TIMEOUT = 10000;

	private static final String BROKER_ID = "BROKER_ID";
	private static final String DEFAULT_URL = "http://127.0.0.1/remoting";

	@Test
	public void testSendMessages() throws ClientProtocolException, IOException, InterruptedException {
		final String expectedResultMessage = "RESULT_MESSAGE";
		final MessageStub requestMessage = new MessageStub(expectedResultMessage);

		final int messageCount = 5;

		final HttpClientStub httpClient = new HttpClientStub(messageCount, 0);

		final IHttpRequestInitializer httpRequestInitializer = Mockito.mock(IHttpRequestInitializer.class);
		final ScheduledExecutorServiceMock executor = new ScheduledExecutorServiceMock();

		final MessageBrokerBuilder builder = new MessageBrokerBuilder(BROKER_ID);
		builder.setUrl(DEFAULT_URL);
		builder.setHttpClient(httpClient);
		builder.setHttpRequestInitializer(httpRequestInitializer);
		builder.setIncommingMessageExecutor(executor);

		final IMessageBroker messageBroker = builder.build();

		final IMessageReceiver receiver = Mockito.mock(IMessageReceiver.class);
		messageBroker.setReceiver(receiver);

		for (int i = 0; i < messageCount; i++) {
			messageBroker.getChannel().send(requestMessage, null);
		}

		Assert.assertTrue(httpClient.awaitMessagesConsumed(SYNC_TIMEOUT, TimeUnit.MILLISECONDS));

		executor.executeEvents();

		Mockito.verify(receiver, Mockito.times(messageCount)).onMessage(expectedResultMessage, messageBroker.getChannel());

		Assert.assertTrue(messageBroker.shutdown(SYNC_TIMEOUT));

		Mockito.verify(httpClient.getConnectionManager(), Mockito.times(1)).shutdown();

		final int postCount = httpClient.getPostInvocationCount();
		final int getCount = httpClient.getGetInvocationCount();

		Assert.assertEquals(messageCount, postCount);
		//get count must be 1 initial get and at least one get request read the result messages
		Assert.assertTrue(getCount >= 2);
		Mockito.verify(httpRequestInitializer, Mockito.times(postCount + getCount)).initialize(Mockito.any(HttpRequest.class));

	}

	@Test
	public void testGetWithServerError() throws ClientProtocolException, IOException, InterruptedException {

		final int errorCount = 5;

		final HttpClientStub httpClient = new HttpClientStub(0, errorCount);
		httpClient.setGetStatus(HttpClientStub.INTERNAL_SERVER_ERROR_STATUS_LINE);

		final ScheduledExecutorServiceMock executor = new ScheduledExecutorServiceMock();

		final AtomicReference<JUnitLogger> loggerRef = new AtomicReference<JUnitLogger>(null);

		final MessageBrokerBuilder builder = new MessageBrokerBuilder(BROKER_ID);
		builder.setUrl(DEFAULT_URL);
		builder.setHttpClient(httpClient);
		builder.setIncommingMessageExecutor(executor);
		builder.setSleepDurationMillisAfterIoException(0);
		builder.setHttpRequestInitializer(new IHttpRequestInitializer() {
			@Override
			public void initialize(final HttpRequest httpRequest) {
				if (Thread.currentThread().getName().contains("messageReceiver")) {
					prepareLoggerForThreadAndDisableConsole(loggerRef);
				}
			}
		});

		final IMessageBroker messageBroker = builder.build();

		final IMessageReceiver receiver = Mockito.mock(IMessageReceiver.class);
		messageBroker.setReceiver(receiver);

		Assert.assertTrue(httpClient.awaitErrorsConsumed(SYNC_TIMEOUT, TimeUnit.MILLISECONDS));

		executor.executeEvents();

		Mockito.verify(receiver, Mockito.never()).onMessage(Mockito.any(), Mockito.eq(messageBroker.getChannel()));

		Assert.assertTrue(messageBroker.shutdown(SYNC_TIMEOUT));

		Mockito.verify(httpClient.getConnectionManager(), Mockito.times(1)).shutdown();
		Assert.assertEquals(0, httpClient.getPostInvocationCount());
		Assert.assertTrue(httpClient.getGetInvocationCount() > errorCount);

		Assert.assertTrue(loggerRef.get().getMessageCount() >= errorCount);
	}

	@Test
	public void testPostWithServerError() throws ClientProtocolException, IOException, InterruptedException {
		final String expectedResultMessage = "RESULT_MESSAGE";
		final MessageStub requestMessage = new MessageStub(expectedResultMessage);

		final int postErrorCount = 5;
		final int messageCount = 5;

		final HttpClientStub httpClient = new HttpClientStub(messageCount, 0);
		httpClient.setPostStatus(HttpClientStub.INTERNAL_SERVER_ERROR_STATUS_LINE);

		final ScheduledExecutorServiceMock executor = new ScheduledExecutorServiceMock();

		final AtomicReference<JUnitLogger> senderThreadLoggerRef = new AtomicReference<JUnitLogger>(null);
		final AtomicReference<JUnitLogger> receiverThreadLoggerRef = new AtomicReference<JUnitLogger>(null);

		final MessageBrokerBuilder builder = new MessageBrokerBuilder(BROKER_ID);
		builder.setUrl(DEFAULT_URL);
		builder.setHttpClient(httpClient);
		builder.setIncommingMessageExecutor(executor);
		builder.setSleepDurationMillisAfterIoException(0);
		builder.setHttpRequestInitializer(new IHttpRequestInitializer() {
			@Override
			public void initialize(final HttpRequest httpRequest) {
				if (Thread.currentThread().getName().contains("messageSender")) {
					prepareLoggerForThreadAndDisableConsole(senderThreadLoggerRef);
				}
				else {
					prepareLoggerForThreadAndDisableConsole(receiverThreadLoggerRef);
				}
			}
		});

		final IMessageBroker messageBroker = builder.build();

		final IMessageReceiver receiver = Mockito.mock(IMessageReceiver.class);
		messageBroker.setReceiver(receiver);

		for (int i = 0; i < postErrorCount; i++) {
			messageBroker.getChannel().send(requestMessage, null);
		}

		while (httpClient.getPostInvocationCount() < postErrorCount) {
			//do nothing
		}

		final IExceptionCallback exceptionCallback = Mockito.mock(IExceptionCallback.class);
		for (int i = 0; i < postErrorCount; i++) {
			messageBroker.getChannel().send(requestMessage, exceptionCallback);
		}

		while (httpClient.getPostInvocationCount() < postErrorCount * 2) {
			//do nothing
		}

		//now fix error and send some messages with success
		httpClient.setPostStatus(HttpClientStub.OK_STATUS_LINE);
		for (int i = 0; i < messageCount; i++) {
			messageBroker.getChannel().send(requestMessage, exceptionCallback);
		}

		Assert.assertTrue(httpClient.awaitMessagesConsumed(SYNC_TIMEOUT, TimeUnit.MILLISECONDS));

		executor.executeEvents();

		Mockito.verify(receiver, Mockito.times(messageCount)).onMessage(expectedResultMessage, messageBroker.getChannel());

		Assert.assertTrue(messageBroker.shutdown(SYNC_TIMEOUT));

		Mockito.verify(httpClient.getConnectionManager(), Mockito.times(1)).shutdown();
		Assert.assertEquals(2 * postErrorCount + messageCount, httpClient.getPostInvocationCount());
		Assert.assertEquals(postErrorCount, senderThreadLoggerRef.get().getMessageCount());
		Mockito.verify(exceptionCallback, Mockito.times(postErrorCount)).exception(Mockito.any(Exception.class));
		Assert.assertEquals(0, receiverThreadLoggerRef.get().getMessageCount());
	}

	@Test
	public void testSucessfulGetRequiredBeforePost() throws ClientProtocolException, IOException, InterruptedException {
		final String expectedResultMessage = "RESULT_MESSAGE";
		final MessageStub requestMessage = new MessageStub(expectedResultMessage);

		final int minGetBeforeFixError = 100;
		final int messageCount = 5;

		final HttpClientStub httpClient = new HttpClientStub(messageCount, 0);
		httpClient.setFirstGetStatus(HttpClientStub.INTERNAL_SERVER_ERROR_STATUS_LINE);

		final ScheduledExecutorServiceMock executor = new ScheduledExecutorServiceMock();

		final AtomicReference<JUnitLogger> senderThreadLoggerRef = new AtomicReference<JUnitLogger>(null);
		final AtomicReference<JUnitLogger> receiverThreadLoggerRef = new AtomicReference<JUnitLogger>(null);
		final AtomicLong postCount = new AtomicLong();

		final MessageBrokerBuilder builder = new MessageBrokerBuilder(BROKER_ID);
		builder.setUrl(DEFAULT_URL);
		builder.setHttpClient(httpClient);
		builder.setIncommingMessageExecutor(executor);
		builder.setSleepDurationMillisAfterIoException(0);
		builder.setHttpRequestInitializer(new IHttpRequestInitializer() {
			@Override
			public void initialize(final HttpRequest httpRequest) {
				if (httpRequest instanceof HttpPost) {
					postCount.incrementAndGet();
				}
				if (Thread.currentThread().getName().contains("messageSender")) {
					prepareLoggerForThreadAndDisableConsole(senderThreadLoggerRef);
				}
				else {
					prepareLoggerForThreadAndDisableConsole(receiverThreadLoggerRef);
				}
			}
		});

		final IMessageBroker messageBroker = builder.build();

		final IMessageReceiver receiver = Mockito.mock(IMessageReceiver.class);
		messageBroker.setReceiver(receiver);

		for (int i = 0; i < messageCount; i++) {
			messageBroker.getChannel().send(requestMessage, null);
		}

		while (httpClient.getGetInvocationCount() < minGetBeforeFixError) {
			//wait for some get() failures 
		}

		//no posts has been send until first get() is successful
		executor.executeEvents();
		Assert.assertEquals(0, httpClient.getPostInvocationCount());
		Mockito.verify(receiver, Mockito.never()).onMessage(expectedResultMessage, messageBroker.getChannel());

		//now fix the fist get
		httpClient.setFirstGetStatus(HttpClientStub.OK_STATUS_LINE);

		//and wait that previous send messages has been consumed
		Assert.assertTrue(httpClient.awaitMessagesConsumed(SYNC_TIMEOUT, TimeUnit.MILLISECONDS));

		executor.executeEvents();
		//now all messages has been received
		Mockito.verify(receiver, Mockito.times(messageCount)).onMessage(expectedResultMessage, messageBroker.getChannel());

		Assert.assertTrue(messageBroker.shutdown(SYNC_TIMEOUT));

		Mockito.verify(httpClient.getConnectionManager(), Mockito.times(1)).shutdown();
		Assert.assertEquals(messageCount, httpClient.getPostInvocationCount());
		Assert.assertEquals(0, senderThreadLoggerRef.get().getMessageCount());
		Assert.assertTrue(receiverThreadLoggerRef.get().getMessageCount() >= minGetBeforeFixError);
	}

	private void prepareLoggerForThreadAndDisableConsole(final AtomicReference<JUnitLogger> loggerRef) {
		if (loggerRef.compareAndSet(null, JUnitLoggerProvider.getLogger(MessageToolkit.class))) {
			JUnitLoggerProvider.getConsoleLoggerEnablement().setEnabled(false);
			loggerRef.get().reset();
			loggerRef.get().setTraceEnabled(false);
			loggerRef.get().setDebugEnabled(false);
			loggerRef.get().setInfoEnabled(false);
		}
	}

}
