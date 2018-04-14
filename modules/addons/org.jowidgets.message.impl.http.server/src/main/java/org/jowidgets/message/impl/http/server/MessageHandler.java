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

package org.jowidgets.message.impl.http.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;
import org.jowidgets.message.api.IMessageChannel;
import org.jowidgets.message.api.IMessageReceiver;
import org.jowidgets.util.Assert;
import org.jowidgets.util.ISystemTimeProvider;

final class MessageHandler implements Runnable {

	private static final ILogger LOGGER = LoggerProvider.get(MessageHandler.class);

	private final Object message;
	private final List<IExecutionInterceptor<Object>> interceptors;
	private final List<Object> executionContexts;
	private final IMessageReceiver receiver;
	private final IMessageChannel replyChannel;
	private final ISystemTimeProvider systemTimeProvider;
	private final AtomicReference<Long> canceled;
	private final AtomicReference<Long> started;
	private final AtomicReference<Thread> executingThread;
	private final AtomicBoolean terminated;

	MessageHandler(
		final Object message,
		final Collection<IExecutionInterceptor<Object>> interceptors,
		final IMessageReceiver receiver,
		final IMessageChannel replyChannel,
		final ISystemTimeProvider systemTimeProvider,
		final AtomicReference<Long> canceled) {

		Assert.paramNotNull(interceptors, "interceptors");
		Assert.paramNotNull(receiver, "receiver");
		Assert.paramNotNull(replyChannel, "replyChannel");
		Assert.paramNotNull(systemTimeProvider, "systemTimeProvider");
		Assert.paramNotNull(canceled, "canceled");

		this.message = message;
		this.interceptors = new ArrayList<IExecutionInterceptor<Object>>(interceptors);
		this.executionContexts = readExecutionContexts(interceptors);
		this.receiver = receiver;
		this.replyChannel = replyChannel;
		this.systemTimeProvider = systemTimeProvider;
		this.canceled = canceled;

		this.started = new AtomicReference<Long>();
		this.executingThread = new AtomicReference<Thread>(null);
		this.terminated = new AtomicBoolean(false);
	}

	private static List<Object> readExecutionContexts(final Collection<IExecutionInterceptor<Object>> interceptors) {
		final ArrayList<Object> result = new ArrayList<Object>(interceptors.size());
		for (final IExecutionInterceptor<Object> interceptor : interceptors) {
			result.add(interceptor.getExecutionContext());
		}
		return result;
	}

	@Override
	public void run() {
		try {
			if (canceled.get() == null && started.compareAndSet(null, Long.valueOf(systemTimeProvider.currentTimeMillis()))) {
				executingThread.set(Thread.currentThread());
				doRun(interceptors, executionContexts);
			}
		}
		finally {
			executingThread.set(null);
			terminated.set(true);
		}
	}

	private void doRun(
		final List<IExecutionInterceptor<Object>> uncalledInterceptors,
		final List<Object> uncalledExecutionContexts) {
		if (uncalledInterceptors.isEmpty()) {
			if (Thread.currentThread().isInterrupted()) {
				LOGGER.debug(
						"Message will not handled because the executor thread was already interrupted, message is: " + message);
			}
			else {
				LOGGER.debug("Before handle message: " + message);
				receiver.onMessage(message, replyChannel);
				LOGGER.debug("After handle message: " + message);
			}
			return;
		}
		final IExecutionInterceptor<Object> uncalledInterceptor = uncalledInterceptors.get(0);
		uncalledInterceptor.beforeExecution(uncalledExecutionContexts.get(0));
		try {
			doRun(
					uncalledInterceptors.subList(1, uncalledInterceptors.size()),
					uncalledExecutionContexts.subList(1, uncalledExecutionContexts.size()));
		}
		finally {
			uncalledInterceptor.afterExecution();
		}
	}

	Object getMessage() {
		return message;
	}

	Thread getExecutingThread() {
		return executingThread.get();
	}

	Long getStartTimeMillis() {
		return started.get();
	}

	boolean isStarted() {
		return started.get() != null;
	}

	boolean isTerminated() {
		return terminated.get();
	}

	Long getRuntimeMillis() {
		if (isRunning()) {
			return Long.valueOf(systemTimeProvider.currentTimeMillis() - started.get().longValue());
		}
		else {
			return null;
		}
	}

	boolean isRunning() {
		return isStarted() && !isTerminated();
	}
}
