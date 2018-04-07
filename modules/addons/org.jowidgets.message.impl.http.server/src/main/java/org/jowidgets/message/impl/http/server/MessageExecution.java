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

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.jowidgets.message.api.IMessageChannel;
import org.jowidgets.message.api.IMessageReceiver;
import org.jowidgets.util.Assert;
import org.jowidgets.util.ISystemTimeProvider;

public final class MessageExecution {

	private final MessageHandler messageHandler;
	private final AtomicReference<Long> canceledTimeMillis;
	private final AtomicBoolean canceled;
	private final ISystemTimeProvider systemTimeProvider;
	private final long creationTimeMillis;

	private final Future<?> submitionResult;

	MessageExecution(
		final Object message,
		final Collection<IExecutionInterceptor<Object>> interceptors,
		final ExecutorService executor,
		final IMessageReceiver receiver,
		final IMessageChannel replyChannel,
		final ISystemTimeProvider systemTimeProvider) {

		Assert.paramNotNull(interceptors, "interceptors");
		Assert.paramNotNull(executor, "executor");
		Assert.paramNotNull(receiver, "receiver");
		Assert.paramNotNull(replyChannel, "replyChannel");
		Assert.paramNotNull(systemTimeProvider, "systemTimeProvider");

		this.canceledTimeMillis = new AtomicReference<Long>();
		this.canceled = new AtomicBoolean(false);
		this.messageHandler = new MessageHandler(
			message,
			interceptors,
			receiver,
			replyChannel,
			systemTimeProvider,
			canceledTimeMillis);

		this.systemTimeProvider = systemTimeProvider;
		this.creationTimeMillis = systemTimeProvider.currentTimeMillis();

		this.submitionResult = executor.submit(messageHandler);
	}

	public synchronized void cancel() {
		if (canceledTimeMillis.compareAndSet(null, systemTimeProvider.currentTimeMillis())) {
			try {
				submitionResult.cancel(true);
			}
			finally {
				canceled.set(true);
			}
		}
	}

	public Thread getExecutingThread() {
		return messageHandler.getExecutingThread();
	}

	public long getCreationTimeMillis() {
		return creationTimeMillis;
	}

	public Object getMessage() {
		return messageHandler.getMessage();
	}

	public Long getHandlerStartTimeMillis() {
		return messageHandler.getStartTimeMillis();
	}

	public boolean isHandlerStarted() {
		return messageHandler.isStarted();
	}

	public boolean isHandlerTerminated() {
		return messageHandler.isTerminated();
	}

	public Long getHandlerRuntimeMillis() {
		return messageHandler.getRuntimeMillis();
	}

	public boolean isHandlerRunning() {
		return messageHandler.isRunning();
	}

	public Long getCanceledTimeMillis() {
		return canceledTimeMillis.get();
	}

	public boolean isCanceled() {
		return canceled.get();
	}

}
