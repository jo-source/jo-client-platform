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

/**
 * Encapsulates the execution of a message that was received by the {@link MessageServlet}.
 * 
 * The execution will be done with the {@link MessageServlet}'s {@link IMessageReceiver} in the
 * {@link IMessageReceiver#onMessage(Object, IMessageChannel)} method.
 */
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

	/**
	 * Cancels the execution.
	 * 
	 * If the execution was not yet started (pending state), it will just be removed.
	 * 
	 * If the execution is running, the running thread will be interrupted.
	 * 
	 * If the execution has already been finished or is already canceled, nothing happens.
	 */
	public synchronized void cancel() {
		if (canceledTimeMillis.compareAndSet(null, systemTimeProvider.currentTimeMillis()) && !isTerminated()) {
			try {
				submitionResult.cancel(true);
			}
			finally {
				canceled.set(true);
			}
		}
	}

	/**
	 * Get's the thread that does the execution.
	 * 
	 * If the message is not in running state, null will be returned.
	 * 
	 * @return The execution thread or null if the execution is not running.
	 */
	public Thread getExecutingThread() {
		return messageHandler.getExecutingThread();
	}

	/**
	 * Get's the creation timestamp in millis of this execution
	 * 
	 * @return The creation timestmp
	 */
	public long getCreationTimeMillis() {
		return creationTimeMillis;
	}

	/**
	 * Get's the message this execution executes with the {@link IMessageReceiver} in the
	 * {@link IMessageReceiver#onMessage(Object, IMessageChannel)} method.
	 * 
	 * @return The message to execute
	 */
	public Object getMessage() {
		return messageHandler.getMessage();
	}

	/**
	 * Get's the start time of the execution if execution has been started or null if pending or canceled.
	 * 
	 * @return The start time or null if not yet started
	 */
	public Long getStartTimeMillis() {
		return messageHandler.getStartTimeMillis();
	}

	/**
	 * Checks if the execution has already been started.
	 * 
	 * @return True if started, false if pending or canceled
	 */
	public boolean isStarted() {
		return messageHandler.isStarted();
	}

	/**
	 * Checks if the execution has been terminated normally.
	 * 
	 * The execution has terminated if it has been started and finished or canceled.
	 * 
	 * @return True if execution has been terminated.
	 */
	public boolean isTerminated() {
		return messageHandler.isTerminated();
	}

	/**
	 * Get's the current runtime of the execution or null if execution has not started
	 * 
	 * @return The execution runtime or null
	 */
	public Long getRuntimeMillis() {
		return messageHandler.getRuntimeMillis();
	}

	/**
	 * Checks if the execution is currently running.
	 * 
	 * A execution is running if it has been started and not yet finished.
	 * 
	 * @return True if execution is currently running, false otherwise
	 */
	public boolean isRunning() {
		return messageHandler.isRunning();
	}

	/**
	 * Get's the timestamp the execution was canceled at or null, if execution has not yet canceled.
	 * 
	 * @return The canceled timestamp or null.
	 */
	public Long getCanceledTimeMillis() {
		return canceledTimeMillis.get();
	}

	/**
	 * Checks if the execution has been canceled.
	 * 
	 * Remark that a canceled execution may still be running of.
	 * 
	 * @return True if execution has been canceled, false otherwise
	 */
	public boolean isCanceled() {
		return canceled.get();
	}

	@Override
	public String toString() {
		return "MessageExecution [canceledTimeMillis="
			+ canceledTimeMillis
			+ ", canceled="
			+ canceled
			+ ", creationTimeMillis="
			+ creationTimeMillis
			+ ", getStartTimeMillis()="
			+ getStartTimeMillis()
			+ ", getRuntimeMillis()="
			+ getRuntimeMillis()
			+ "]";
	}

}
