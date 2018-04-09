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

package org.jowidgets.message.impl.http.server;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

import org.jowidgets.message.api.IExceptionCallback;
import org.jowidgets.message.api.IMessageChannel;
import org.jowidgets.message.api.IMessageReceiver;
import org.jowidgets.util.Assert;
import org.jowidgets.util.ISystemTimeProvider;

final class MessageChannel implements IMessageChannel {

	private final HttpSession session;
	private final MessageExecutionsWatchdog watchdog;
	private final IMessageReceiver receiver;
	private final ExecutorService executor;

	private final BlockingQueue<Object> queue;
	private final ISystemTimeProvider systemTimeProvider;

	MessageChannel(
		final IMessageReceiver receiver,
		final ExecutorService executor,
		final HttpSession session,
		final MessageExecutionsWatchdog watchdog,
		final ISystemTimeProvider systemTimeProvider) {

		Assert.paramNotNull(receiver, "receiver");
		Assert.paramNotNull(executor, "executor");
		Assert.paramNotNull(session, "session");
		Assert.paramNotNull(watchdog, "watchdog");
		Assert.paramNotNull(systemTimeProvider, "systemTimeProvider");

		this.receiver = receiver;
		this.executor = executor;
		this.session = session;
		this.watchdog = watchdog;

		this.queue = new LinkedBlockingQueue<Object>();
		this.systemTimeProvider = systemTimeProvider;
	}

	void onMessage(final Object message, final Collection<IExecutionInterceptor<Object>> interceptors) {
		watchdog.addExecution(session, new MessageExecution(message, interceptors, executor, receiver, this, systemTimeProvider));
	}

	List<Object> pollMessages(final long pollInterval) {
		final List<Object> msgs = new LinkedList<Object>();
		if (queue.drainTo(msgs) == 0) {
			try {
				final Object msg = queue.poll(pollInterval, TimeUnit.MILLISECONDS);
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

	@Override
	public void send(final Object message, final IExceptionCallback exceptionCallback) {
		queue.add(message);
	}

}
