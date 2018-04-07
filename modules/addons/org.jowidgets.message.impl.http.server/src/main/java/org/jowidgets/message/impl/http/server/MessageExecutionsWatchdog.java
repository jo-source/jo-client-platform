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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpSession;

import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.DefaultSystemTimeProvider;
import org.jowidgets.util.ISystemTimeProvider;

final class MessageExecutionsWatchdog {

	private static final ILogger LOGGER = LoggerProvider.get(MessageExecutionsWatchdog.class);

	private static final long DEFAULT_SESSION_INACTIVITY_TIMEOUT = 60 * 1000; // 1 minute

	private final ISystemTimeProvider systemTimeProvider;
	private final long sessionInactivityTimeout;

	private final ConcurrentHashMap<HttpSession, List<MessageExecution>> executionsMap;

	private final CopyOnWriteArraySet<IMessageExecutionWatchdogListener> watchdogListeners;

	private final AtomicReference<WatchDogResult> lastWatchDogResult;

	MessageExecutionsWatchdog() {
		this(DefaultSystemTimeProvider.getInstance(), DEFAULT_SESSION_INACTIVITY_TIMEOUT);
	}

	MessageExecutionsWatchdog(final ISystemTimeProvider systemTimeProvider, final long sessionInactivityTimeout) {

		Assert.paramNotNull(systemTimeProvider, "systemTimeProvider");

		this.systemTimeProvider = systemTimeProvider;
		this.sessionInactivityTimeout = sessionInactivityTimeout;

		this.executionsMap = new ConcurrentHashMap<HttpSession, List<MessageExecution>>();
		this.watchdogListeners = new CopyOnWriteArraySet<IMessageExecutionWatchdogListener>();

		this.lastWatchDogResult = new AtomicReference<WatchDogResult>(new WatchDogResultBuilder().build());
	}

	WatchDogResult getLastWatchResult() {
		return lastWatchDogResult.get();
	}

	void watchExecutions() {
		final WatchDogResultBuilder resultBuilder = new WatchDogResultBuilder();
		for (final HttpSession session : executionsMap.keySet()) {
			synchronized (session) {
				watchExecutionsOfSession(session, resultBuilder);
			}
		}
		final WatchDogResult watchDogResult = resultBuilder.build();
		lastWatchDogResult.set(watchDogResult);
		onExecutionsWatch(watchDogResult);
	}

	private void onExecutionsWatch(final WatchDogResult watchDogResult) {
		for (final IMessageExecutionWatchdogListener listener : watchdogListeners) {
			listener.onExecutionsWatch(watchDogResult);
		}
	}

	private void watchExecutionsOfSession(final HttpSession session, final WatchDogResultBuilder resultBuilder) {
		final List<MessageExecution> executions = executionsMap.get(session);
		removeCompletedExecutions(executions);
		if (executions == null || executions.isEmpty()) {
			executionsMap.remove(session);
			LOGGER.info("Session has no executions and was removed from watchdog: " + session);
		}
		else {
			if (isSessionInactive(session)) {
				if (!cancelMessageExecutions(executions)) {
					//watch if no execution was canceled
					watchMessageExecutions(executions, resultBuilder);
				}
			}
			else {
				watchMessageExecutions(executions, resultBuilder);
			}
		}
	}

	private void removeCompletedExecutions(final List<MessageExecution> executions) {
		if (executions == null) {
			return;
		}
		final Iterator<MessageExecution> iterator = executions.iterator();
		while (iterator.hasNext()) {
			final MessageExecution execution = iterator.next();
			if (isExecutionCompleted(execution)) {
				iterator.remove();
			}
		}
	}

	private boolean isExecutionCompleted(final MessageExecution execution) {
		if (execution.isHandlerTerminated()) {
			return true;
		}
		else if (execution.isCanceled() && !execution.isHandlerStarted()) {
			return true;
		}
		else {
			return false;
		}
	}

	private boolean isSessionInactive(final HttpSession session) {
		return systemTimeProvider.currentTimeMillis() - session.getLastAccessedTime() > sessionInactivityTimeout;
	}

	private boolean cancelMessageExecutions(final Collection<MessageExecution> executions) {
		boolean result = false;
		for (final MessageExecution execution : executions) {
			result = cancelMessageExecution(execution) || result;
		}
		return result;
	}

	private boolean cancelMessageExecution(final MessageExecution execution) {
		if (!execution.isCanceled()) {
			execution.cancel();
			onExecutionCancel(execution);
			return true;
		}
		else {
			return false;
		}
	}

	private void onExecutionCancel(final MessageExecution execution) {
		final Object message = execution.getMessage();
		final long cancelTimeMillis = execution.getCanceledTimeMillis().longValue();
		for (final IMessageExecutionWatchdogListener listener : watchdogListeners) {
			listener.onExecutionCancel(message, cancelTimeMillis);
		}
	}

	private void watchMessageExecutions(
		final Collection<MessageExecution> executions,
		final WatchDogResultBuilder resultBuilder) {
		for (final MessageExecution execution : executions) {
			watchMessageExecution(execution, resultBuilder);
		}
	}

	private void watchMessageExecution(final MessageExecution execution, final WatchDogResultBuilder resultBuilder) {
		if (!execution.isCanceled()) {
			if (execution.isHandlerStarted()) {
				resultBuilder.addRunningExecution(execution);
			}
			else {
				resultBuilder.addPendingExecution(execution);
			}
		}
		else if (execution.isHandlerStarted() && !execution.isHandlerTerminated()) {
			resultBuilder.addUnfinishedCancelExecution(execution);
		}
	}

	void addExecution(final HttpSession session, final MessageExecution execution) {
		synchronized (session) {
			List<MessageExecution> executions = executionsMap.get(session);
			if (executions == null) {
				executions = new LinkedList<MessageExecution>();
				executionsMap.put(session, executions);
			}
			executions.add(execution);
		}
	}

	void addWatchdogListener(final IMessageExecutionWatchdogListener listener) {
		Assert.paramNotNull(listener, "listener");
		watchdogListeners.add(listener);
	}

	void removeWatchdogListener(final IMessageExecutionWatchdogListener listener) {
		Assert.paramNotNull(listener, "listener");
		watchdogListeners.remove(listener);
	}

	class WatchDogResultBuilder {

		private final long watchTimeMillis = systemTimeProvider.currentTimeMillis();
		private final List<MessageExecution> running = new LinkedList<MessageExecution>();
		private final List<MessageExecution> pending = new LinkedList<MessageExecution>();
		private final List<MessageExecution> unfinishedCancel = new LinkedList<MessageExecution>();

		WatchDogResultBuilder addRunningExecution(final MessageExecution execution) {
			running.add(execution);
			return this;
		}

		WatchDogResultBuilder addPendingExecution(final MessageExecution execution) {
			pending.add(execution);
			return this;
		}

		WatchDogResultBuilder addUnfinishedCancelExecution(final MessageExecution execution) {
			unfinishedCancel.add(execution);
			return this;
		}

		WatchDogResult build() {
			return new WatchDogResult(watchTimeMillis, running, pending, unfinishedCancel);
		}

	}
}
