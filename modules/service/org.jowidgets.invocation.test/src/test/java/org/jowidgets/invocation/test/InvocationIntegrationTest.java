/*
 * Copyright (c) 2018, herrg
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

package org.jowidgets.invocation.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.jowidgets.invocation.client.api.IInvocationClient;
import org.jowidgets.invocation.client.api.InvocationClientToolkit;
import org.jowidgets.invocation.common.api.ICancelService;
import org.jowidgets.invocation.common.api.IInvocationCallbackService;
import org.jowidgets.invocation.common.api.IMethod;
import org.jowidgets.invocation.common.api.IResponseService;
import org.jowidgets.invocation.server.api.InvocationServerToolkit;
import org.jowidgets.logging.tools.JUnitLogger;
import org.jowidgets.logging.tools.JUnitLoggerProvider;
import org.jowidgets.logging.tools.LogLevel;
import org.jowidgets.logging.tools.LogMessage;
import org.jowidgets.message.impl.mock.MockMessaging;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class InvocationIntegrationTest {
	private static final String BROKER_ID = "MOCK_MESSAGING_BROKER_ID";

	private MockMessaging messaging;
	private JUnitLogger loggerMock;

	@Before
	public void setUp() {
		JUnitLoggerProvider.reset();
		loggerMock = JUnitLoggerProvider.getGlobalLogger();
		InvocationClientToolkit.dispose();
		InvocationServerToolkit.dispose();
		messaging = new MockMessaging(BROKER_ID);
	}

	@After
	public void tearDown() {
		JUnitLoggerProvider.reset();
		messaging.dispose();
		InvocationClientToolkit.dispose();
		InvocationServerToolkit.dispose();
	}

	@Test
	public void testInvokeMethod() {

		final String methodName = "Method";
		final Integer invocationId = Integer.valueOf(0);
		final String invocationParameter = "InvocationParameter";

		//setup server
		final IMethod serverMethodMock = Mockito.mock(IMethod.class);
		InvocationServerToolkit.getRegistry(BROKER_ID).register(methodName, serverMethodMock);

		//setup client
		final IMethod clientMethod = InvocationClientToolkit.getClient(BROKER_ID).getMethod(methodName);

		//invoke method on client
		clientMethod.invoke(invocationId, invocationParameter);

		Mockito.verify(serverMethodMock, Mockito.never()).invoke(invocationId, invocationParameter);
		messaging.getMessageReceiverBroker().dispatchMessages();
		Mockito.verify(serverMethodMock, Mockito.times(1)).invoke(invocationId, invocationParameter);

		assertNoLogErrorOrWarning();
	}

	@Test
	public void testInvokeMethodWithResult() {

		final String methodName = "Method";
		final String methodResult = "Result";
		final Integer invocationId = Integer.valueOf(0);
		final String invocationParameter = "InvocationParameter";

		//setup server
		final IInvocationCallbackService serverInvocationCallback = InvocationServerToolkit.getServer(
				BROKER_ID).getInvocationCallbackService();
		final IMethod serverMethodMock = Mockito.mock(IMethod.class);
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Integer invocationIdArg = invocation.getArgumentAt(0, Integer.class);
				final String invocationParameterArg = invocation.getArgumentAt(1, String.class);
				serverInvocationCallback.finished(invocationIdArg, invocationParameterArg + methodResult);
				return null;
			}
		}).when(serverMethodMock).invoke(Mockito.any(), Mockito.any());
		InvocationServerToolkit.getRegistry(BROKER_ID).register(methodName, serverMethodMock);

		//setup client
		final IInvocationCallbackService clientInvocationCallback = Mockito.mock(IInvocationCallbackService.class);
		InvocationClientToolkit.getRegistry(BROKER_ID).register(clientInvocationCallback);

		final IMethod clientMethod = InvocationClientToolkit.getClient(BROKER_ID).getMethod(methodName);

		//invoke method on client
		clientMethod.invoke(invocationId, invocationParameter);

		Mockito.verify(serverMethodMock, Mockito.never()).invoke(invocationId, invocationParameter);
		messaging.getMessageReceiverBroker().dispatchMessages();
		Mockito.verify(serverMethodMock, Mockito.times(1)).invoke(invocationId, invocationParameter);

		Mockito.verify(clientInvocationCallback, Mockito.never()).finished(invocationId, invocationParameter + methodResult);
		messaging.getMessageChannelBroker().dispatchReturnedMessages();
		Mockito.verify(clientInvocationCallback, Mockito.times(1)).finished(invocationId, invocationParameter + methodResult);

		assertNoLogErrorOrWarning();
	}

	@Test
	public void testException() {

		final String methodName = "Method";
		final String methodResult = "Result";
		final RuntimeException expectedException = new RuntimeException("Something goes wrong");
		final Integer invocationId = Integer.valueOf(0);
		final String invocationParameter = "InvocationParameter";

		//setup server
		final IInvocationCallbackService serverInvocationCallback = InvocationServerToolkit.getServer(
				BROKER_ID).getInvocationCallbackService();
		final IMethod serverMethodMock = Mockito.mock(IMethod.class);
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Integer invocationIdArg = invocation.getArgumentAt(0, Integer.class);

				try {
					throwException();
					serverInvocationCallback.finished(invocationIdArg, methodResult);
				}
				catch (final Exception e) {
					serverInvocationCallback.exeption(invocationIdArg, e);
				}

				return null;
			}

			private void throwException() {
				throw expectedException;
			}

		}).when(serverMethodMock).invoke(Mockito.any(), Mockito.any());
		InvocationServerToolkit.getRegistry(BROKER_ID).register(methodName, serverMethodMock);

		//setup client
		final IInvocationCallbackService clientInvocationCallback = Mockito.mock(IInvocationCallbackService.class);
		InvocationClientToolkit.getRegistry(BROKER_ID).register(clientInvocationCallback);

		final IMethod clientMethod = InvocationClientToolkit.getClient(BROKER_ID).getMethod(methodName);

		//invoke method on client
		clientMethod.invoke(invocationId, invocationParameter);

		Mockito.verify(serverMethodMock, Mockito.never()).invoke(invocationId, invocationParameter);
		messaging.getMessageReceiverBroker().dispatchMessages();
		Mockito.verify(serverMethodMock, Mockito.times(1)).invoke(invocationId, invocationParameter);

		Mockito.verify(clientInvocationCallback, Mockito.never()).exeption(invocationId, expectedException);
		Mockito.verify(clientInvocationCallback, Mockito.never()).finished(invocationId, methodResult);
		messaging.getMessageChannelBroker().dispatchReturnedMessages();
		Mockito.verify(clientInvocationCallback, Mockito.times(1)).exeption(invocationId, expectedException);
		Mockito.verify(clientInvocationCallback, Mockito.never()).finished(invocationId, methodResult);

		assertNoLogErrorOrWarning();
	}

	@Test
	public void testExceptionBeforeFinsihed() {

		JUnitLoggerProvider.getConsoleLoggerEnablement().setEnabled(false);

		final String methodName = "Method";
		final String methodResult = "Result";
		final RuntimeException expectedException = new RuntimeException("Something goes wrong");
		final Integer invocationId = Integer.valueOf(0);
		final String invocationParameter = "InvocationParameter";

		//setup server
		final IInvocationCallbackService serverInvocationCallback = InvocationServerToolkit.getServer(
				BROKER_ID).getInvocationCallbackService();

		final IMethod serverMethodMock = Mockito.mock(IMethod.class);
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Integer invocationIdArg = invocation.getArgumentAt(0, Integer.class);

				try {
					throw expectedException;
				}
				catch (final Exception e) {
					serverInvocationCallback.exeption(invocationIdArg, e);
				}
				serverInvocationCallback.finished(invocationIdArg, methodResult);

				return null;
			}
		}).when(serverMethodMock).invoke(Mockito.any(), Mockito.any());
		InvocationServerToolkit.getRegistry(BROKER_ID).register(methodName, serverMethodMock);

		//setup client
		final IInvocationCallbackService clientInvocationCallback = Mockito.mock(IInvocationCallbackService.class);
		InvocationClientToolkit.getRegistry(BROKER_ID).register(clientInvocationCallback);

		final IMethod clientMethod = InvocationClientToolkit.getClient(BROKER_ID).getMethod(methodName);

		//invoke method on client
		clientMethod.invoke(invocationId, invocationParameter);

		messaging.getMessageReceiverBroker().dispatchMessages();
		messaging.getMessageChannelBroker().dispatchReturnedMessages();
		Mockito.verify(clientInvocationCallback, Mockito.times(1)).exeption(invocationId, expectedException);
		Mockito.verify(clientInvocationCallback, Mockito.never()).finished(invocationId, methodResult);

		assertLogError();
	}

	@Test
	public void testExceptionAfterFinsihed() {

		JUnitLoggerProvider.getConsoleLoggerEnablement().setEnabled(false);

		final String methodName = "Method";
		final String methodResult = "Result";
		final RuntimeException expectedException = new RuntimeException("Something goes wrong");
		final Integer invocationId = Integer.valueOf(0);
		final String invocationParameter = "InvocationParameter";

		//setup server
		final IInvocationCallbackService serverInvocationCallback = InvocationServerToolkit.getServer(
				BROKER_ID).getInvocationCallbackService();

		final IMethod serverMethodMock = Mockito.mock(IMethod.class);
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Integer invocationIdArg = invocation.getArgumentAt(0, Integer.class);

				try {
					serverInvocationCallback.finished(invocationIdArg, methodResult);
					throw expectedException;
				}
				catch (final Exception e) {
					serverInvocationCallback.exeption(invocationIdArg, e);
				}

				return null;
			}

		}).when(serverMethodMock).invoke(Mockito.any(), Mockito.any());
		InvocationServerToolkit.getRegistry(BROKER_ID).register(methodName, serverMethodMock);

		//setup client
		final IInvocationCallbackService clientInvocationCallback = Mockito.mock(IInvocationCallbackService.class);
		InvocationClientToolkit.getRegistry(BROKER_ID).register(clientInvocationCallback);

		final IMethod clientMethod = InvocationClientToolkit.getClient(BROKER_ID).getMethod(methodName);

		//invoke method on client
		clientMethod.invoke(invocationId, invocationParameter);

		messaging.getMessageReceiverBroker().dispatchMessages();
		messaging.getMessageChannelBroker().dispatchReturnedMessages();
		Mockito.verify(clientInvocationCallback, Mockito.never()).exeption(invocationId, expectedException);
		Mockito.verify(clientInvocationCallback, Mockito.times(1)).finished(invocationId, methodResult);

		assertNoLogErrorOrWarning();
	}

	@Test
	public void testCancelBeforeMethodInvocation() {

		final String methodName = "Method";
		final Integer invocationId = Integer.valueOf(0);
		final String invocationParameter = "InvocationParameter";

		//setup server
		final ICancelService serverCancelService = Mockito.mock(ICancelService.class);
		InvocationServerToolkit.getRegistry(BROKER_ID).register(serverCancelService);

		final IMethod serverMethodMock = Mockito.mock(IMethod.class);
		InvocationServerToolkit.getRegistry(BROKER_ID).register(methodName, serverMethodMock);

		//setup client
		final IInvocationCallbackService clientInvocationCallback = Mockito.mock(IInvocationCallbackService.class);
		InvocationClientToolkit.getRegistry(BROKER_ID).register(clientInvocationCallback);

		final IInvocationClient client = InvocationClientToolkit.getClient(BROKER_ID);
		final IMethod clientMethod = client.getMethod(methodName);
		final ICancelService cancelService = client.getCancelService();

		//invoke methods on client
		clientMethod.invoke(invocationId, invocationParameter);
		cancelService.canceled(invocationId);

		Mockito.verify(serverMethodMock, Mockito.never()).invoke(invocationId, invocationParameter);
		Mockito.verify(serverCancelService, Mockito.never()).canceled(invocationId);

		messaging.getMessageReceiverBroker().dispatchMessages();
		Mockito.verify(serverMethodMock, Mockito.times(1)).invoke(invocationId, invocationParameter);
		Mockito.verify(serverCancelService, Mockito.times(1)).canceled(invocationId);

		assertNoLogErrorOrWarning();
	}

	@Test
	public void testCancelAfterMessageInvocation() {

		final String methodName = "Method";
		final Integer invocationId = Integer.valueOf(0);
		final String invocationParameter = "InvocationParameter";

		//setup server
		final ICancelService serverCancelService = Mockito.mock(ICancelService.class);
		InvocationServerToolkit.getRegistry(BROKER_ID).register(serverCancelService);

		final IMethod serverMethodMock = Mockito.mock(IMethod.class);
		InvocationServerToolkit.getRegistry(BROKER_ID).register(methodName, serverMethodMock);

		//setup client
		final IInvocationCallbackService clientInvocationCallback = Mockito.mock(IInvocationCallbackService.class);
		InvocationClientToolkit.getRegistry(BROKER_ID).register(clientInvocationCallback);

		final IInvocationClient client = InvocationClientToolkit.getClient(BROKER_ID);
		final IMethod clientMethod = client.getMethod(methodName);
		final ICancelService clientCancelService = client.getCancelService();

		//invoke methods on client
		clientMethod.invoke(invocationId, invocationParameter);

		Mockito.verify(serverMethodMock, Mockito.never()).invoke(invocationId, invocationParameter);
		messaging.getMessageReceiverBroker().dispatchMessages();
		Mockito.verify(serverMethodMock, Mockito.times(1)).invoke(invocationId, invocationParameter);

		clientCancelService.canceled(invocationId);
		Mockito.verify(serverCancelService, Mockito.never()).canceled(invocationId);
		messaging.getMessageReceiverBroker().dispatchMessages();
		Mockito.verify(serverCancelService, Mockito.times(1)).canceled(invocationId);

		assertNoLogErrorOrWarning();
	}

	@Test
	public void testInterimServerResponse() {

		final String methodName = "Method";
		final Integer invocationId = Integer.valueOf(0);
		final String invocationParameter = "InvocationParameter";
		final Integer interimResponse1 = Integer.valueOf(1);
		final Integer interimResponse2 = Integer.valueOf(2);
		final Integer interimResponse3 = Integer.valueOf(3);
		final String methodResult = "Result";

		//setup server
		final IInvocationCallbackService serverInvocationCallback = InvocationServerToolkit.getServer(
				BROKER_ID).getInvocationCallbackService();
		final IMethod serverMethod = Mockito.mock(IMethod.class);
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Integer invocationIdArg = invocation.getArgumentAt(0, Integer.class);
				serverInvocationCallback.interimResponse(invocationIdArg, interimResponse1);
				serverInvocationCallback.interimResponse(invocationIdArg, interimResponse2);
				serverInvocationCallback.interimResponse(invocationIdArg, interimResponse3);
				serverInvocationCallback.finished(invocationIdArg, methodResult);
				return null;
			}
		}).when(serverMethod).invoke(Mockito.any(), Mockito.any());
		InvocationServerToolkit.getRegistry(BROKER_ID).register(methodName, serverMethod);

		//setup client
		final IInvocationCallbackService clientInvocationCallback = Mockito.mock(IInvocationCallbackService.class);
		InvocationClientToolkit.getRegistry(BROKER_ID).register(clientInvocationCallback);

		final IMethod clientMethod = InvocationClientToolkit.getClient(BROKER_ID).getMethod(methodName);

		//invoke method on client
		clientMethod.invoke(invocationId, invocationParameter);

		Mockito.verify(serverMethod, Mockito.never()).invoke(invocationId, invocationParameter);
		messaging.getMessageReceiverBroker().dispatchMessages();
		Mockito.verify(serverMethod, Mockito.times(1)).invoke(invocationId, invocationParameter);

		Mockito.verify(clientInvocationCallback, Mockito.never()).interimResponse(invocationId, interimResponse1);
		Mockito.verify(clientInvocationCallback, Mockito.never()).interimResponse(invocationId, interimResponse2);
		Mockito.verify(clientInvocationCallback, Mockito.never()).interimResponse(invocationId, interimResponse3);
		Mockito.verify(clientInvocationCallback, Mockito.never()).finished(invocationId, methodResult);
		messaging.getMessageChannelBroker().dispatchReturnedMessages();
		Mockito.verify(clientInvocationCallback, Mockito.times(1)).interimResponse(invocationId, interimResponse1);
		Mockito.verify(clientInvocationCallback, Mockito.times(1)).interimResponse(invocationId, interimResponse2);
		Mockito.verify(clientInvocationCallback, Mockito.times(1)).interimResponse(invocationId, interimResponse3);
		Mockito.verify(clientInvocationCallback, Mockito.times(1)).finished(invocationId, methodResult);

		assertNoLogErrorOrWarning();
	}

	@Test
	public void testRequestResponse() {

		final String methodName = "Method";
		final Integer invocationId = Integer.valueOf(0);
		final Integer requestId = Integer.valueOf(1);
		final String invocationParameter = "InvocationParameter";
		final String request = "Request";
		final String response = "Response";
		final String methodResult = "Result";

		//setup server
		final IInvocationCallbackService serverInvocationCallback = InvocationServerToolkit.getServer(
				BROKER_ID).getInvocationCallbackService();
		final IMethod serverMethod = Mockito.mock(IMethod.class);
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				serverInvocationCallback.interimRequest(invocationId, requestId, request);
				return null;
			}
		}).when(serverMethod).invoke(Mockito.any(), Mockito.any());
		InvocationServerToolkit.getRegistry(BROKER_ID).register(methodName, serverMethod);

		final IResponseService serverResponseService = Mockito.mock(IResponseService.class);
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final String responseArg = invocation.getArgumentAt(1, String.class);
				serverInvocationCallback.finished(invocationId, responseArg + methodResult);
				return null;
			}
		}).when(serverResponseService).response(Mockito.any(), Mockito.any());
		InvocationServerToolkit.getRegistry(BROKER_ID).register(serverResponseService);

		//setup client
		final IInvocationCallbackService clientInvocationCallback = Mockito.mock(IInvocationCallbackService.class);
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				final Integer requestIdArg = invocation.getArgumentAt(1, Integer.class);
				final String requestArg = invocation.getArgumentAt(2, String.class);
				InvocationClientToolkit.getClient(BROKER_ID).getResponseService().response(requestIdArg, requestArg + response);
				return null;
			}
		}).when(clientInvocationCallback).interimRequest(Mockito.any(), Mockito.any(), Mockito.any());
		InvocationClientToolkit.getRegistry(BROKER_ID).register(clientInvocationCallback);

		final IMethod clientMethod = InvocationClientToolkit.getClient(BROKER_ID).getMethod(methodName);

		//invoke method on client
		clientMethod.invoke(invocationId, invocationParameter);

		Mockito.verify(serverMethod, Mockito.never()).invoke(invocationId, invocationParameter);
		messaging.getMessageReceiverBroker().dispatchMessages();
		Mockito.verify(serverMethod, Mockito.times(1)).invoke(invocationId, invocationParameter);

		Mockito.verify(clientInvocationCallback, Mockito.never()).interimRequest(invocationId, requestId, request);
		messaging.getMessageChannelBroker().dispatchReturnedMessages();
		Mockito.verify(clientInvocationCallback, Mockito.times(1)).interimRequest(invocationId, requestId, request);

		Mockito.verify(serverResponseService, Mockito.never()).response(requestId, request + response);
		messaging.getMessageReceiverBroker().dispatchMessages();
		Mockito.verify(serverResponseService, Mockito.times(1)).response(requestId, request + response);

		Mockito.verify(clientInvocationCallback, Mockito.never()).finished(invocationId, request + response + methodResult);
		messaging.getMessageChannelBroker().dispatchReturnedMessages();
		Mockito.verify(clientInvocationCallback, Mockito.times(1)).finished(invocationId, request + response + methodResult);

		assertNoLogErrorOrWarning();
	}

	@Test
	public void testCancelExceptionAfterCancel() {

		final String methodName = "Method";
		final Integer invocationId = Integer.valueOf(0);
		final String invocationParameter = "InvocationParameter";
		final RuntimeException cancelException = new RuntimeException("Service was canceled");

		final CountDownLatch cancelLatch = new CountDownLatch(1);
		final CountDownLatch serverFinsihedLatch = new CountDownLatch(1);

		//setup server
		final ICancelService serverCancelService = Mockito.mock(ICancelService.class);
		InvocationServerToolkit.getRegistry(BROKER_ID).register(serverCancelService);

		final IInvocationCallbackService serverInvocationCallback = InvocationServerToolkit.getServer(
				BROKER_ID).getInvocationCallbackService();

		final IMethod serverMethodMock = Mockito.mock(IMethod.class);
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				//wait until service was canceled
				cancelLatch.await();

				//after service was canceled return a cancel exception
				serverInvocationCallback.exeption(invocationId, cancelException);
				return null;
			}

		}).when(serverMethodMock).invoke(Mockito.any(), Mockito.any());
		InvocationServerToolkit.getRegistry(BROKER_ID).register(methodName, serverMethodMock);

		//setup client
		final IInvocationCallbackService clientInvocationCallback = Mockito.mock(IInvocationCallbackService.class);
		InvocationClientToolkit.getRegistry(BROKER_ID).register(clientInvocationCallback);

		final IInvocationClient client = InvocationClientToolkit.getClient(BROKER_ID);
		final IMethod clientMethod = client.getMethod(methodName);
		final ICancelService cancelService = client.getCancelService();

		//invoke methods on client
		clientMethod.invoke(invocationId, invocationParameter);

		//dispatch method on server
		final AtomicReference<JUnitLogger> serverMethodThreadLogger = new AtomicReference<JUnitLogger>();
		new Thread(new Runnable() {
			@Override
			public void run() {
				serverMethodThreadLogger.set(JUnitLoggerProvider.getGlobalLogger());
				messaging.getMessageReceiverBroker().dispatchMessages();
				serverFinsihedLatch.countDown();
			}
		}).start();

		//invoke cancel on client
		cancelService.canceled(invocationId);

		//dispatch cancel message on server
		final AtomicReference<JUnitLogger> serverCancelThreadLogger = new AtomicReference<JUnitLogger>();
		new Thread(new Runnable() {
			@Override
			public void run() {
				serverCancelThreadLogger.set(JUnitLoggerProvider.getGlobalLogger());
				messaging.getMessageReceiverBroker().dispatchMessages();
				cancelLatch.countDown();
			}
		}).start();

		try {
			serverFinsihedLatch.await();
		}
		catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}

		messaging.getMessageChannelBroker().dispatchReturnedMessages();

		Mockito.verify(serverCancelService, Mockito.times(1)).canceled(invocationId);
		Mockito.verify(clientInvocationCallback, Mockito.never()).exeption(invocationId, cancelException);
		Assert.assertFalse(serverMethodThreadLogger.get().hasMessage());
		Assert.assertFalse(serverCancelThreadLogger.get().hasMessage());
		assertNoLogErrorOrWarning();
	}

	private void assertLogError() {
		final LogMessage logMessage = loggerMock.peekLastMessage();
		Assert.assertNotNull(logMessage);
		Assert.assertEquals(LogLevel.ERROR, logMessage.getLevel());
	}

	private void assertNoLogErrorOrWarning() {
		final LogMessage logMessage = loggerMock.peekLastMessage();
		Assert.assertNull(logMessage);
	}

}
