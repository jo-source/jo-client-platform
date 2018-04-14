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

	private final ISystemTimeProvider systemTimeProvider;
	private final ConcurrentHashMap<HttpSession, List<MessageExecution>> executionsMap;
	private final CopyOnWriteArraySet<IMessageExecutionWatchdogListener> watchdogListeners;

	private final AtomicReference<WatchDogEvent> lastWatchDogResult;

	private long sessionInactivityTimeout;
	private Long haraKiriTimeout;
	private Long haraKiriPendingThreshold;

	MessageExecutionsWatchdog(final long sessionInactivityTimeout) {
		this(DefaultSystemTimeProvider.getInstance(), sessionInactivityTimeout);
	}

	MessageExecutionsWatchdog(final ISystemTimeProvider systemTimeProvider, final long sessionInactivityTimeout) {
		Assert.paramNotNull(systemTimeProvider, "systemTimeProvider");

		this.systemTimeProvider = systemTimeProvider;
		this.sessionInactivityTimeout = sessionInactivityTimeout;

		this.executionsMap = new ConcurrentHashMap<HttpSession, List<MessageExecution>>();
		this.watchdogListeners = new CopyOnWriteArraySet<IMessageExecutionWatchdogListener>();
		this.lastWatchDogResult = new AtomicReference<WatchDogEvent>(new WatchDogEventBuilder().build());
	}

	void setSessionInactivityTimeout(final long sessionInactivityTimeout) {
		this.sessionInactivityTimeout = sessionInactivityTimeout;
	}

	void setHaraKiriTimeout(final Long haraKiriTimeout) {
		this.haraKiriTimeout = haraKiriTimeout;
	}

	void setHaraKiriPendingThreshold(final Long haraKiriPendingThreshold) {
		this.haraKiriPendingThreshold = haraKiriPendingThreshold;
	}

	WatchDogEvent getLastWatchEvent() {
		return lastWatchDogResult.get();
	}

	void watchExecutions() {
		final WatchDogEventBuilder eventBuilder = new WatchDogEventBuilder();
		for (final HttpSession session : executionsMap.keySet()) {
			synchronized (session) {
				watchExecutionsOfSession(session, eventBuilder);
			}
		}
		final WatchDogEvent watchDogEvent = eventBuilder.build();
		if (checkIfHaraKiriIsNecessary(watchDogEvent)) {
			doHaraKiri(watchDogEvent);
			//watch again after hara kiri without and omit listener events
			watchExecutions();
		}
		else {
			lastWatchDogResult.set(watchDogEvent);
			onExecutionsWatch(watchDogEvent);
		}
	}

	void cancelExecutionsOfSession(final HttpSession session) {
		synchronized (session) {
			final List<MessageExecution> executions = executionsMap.remove(session);
			if (executions != null) {
				cancelMessageExecutions(executions);
			}
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

	private void onExecutionsWatch(final WatchDogEvent watchDogResult) {
		for (final IMessageExecutionWatchdogListener listener : watchdogListeners) {
			listener.onExecutionsWatch(watchDogResult);
		}
	}

	private void doHaraKiri(final WatchDogEvent event) {
		cancelMessageExecutions(event.getPendingExecutions());
		cancelMessageExecutions(event.getRunningExecutions());
		onHaraKiri(event);
	}

	private boolean checkIfHaraKiriIsNecessary(final WatchDogEvent event) {
		return isHaraKiriPendingThresholdReached(event) || isHaraKiriTimeoutReached(event);
	}

	private boolean isHaraKiriPendingThresholdReached(final WatchDogEvent event) {
		final Long haraKiriPendingThreshholdCopy = haraKiriPendingThreshold;
		if (haraKiriPendingThreshholdCopy == null) {
			return false;
		}
		if (event.getPendingExecutions().size() >= haraKiriPendingThreshholdCopy.longValue()) {
			LOGGER.warn("Max pending threshhold '" + haraKiriPendingThreshholdCopy + "' reached, try to cancel all executions.");
			return true;
		}
		return false;
	}

	private boolean isHaraKiriTimeoutReached(final WatchDogEvent event) {
		final Long haraKiriTimeoutCopy = haraKiriTimeout;
		if (haraKiriTimeoutCopy == null) {
			return false;
		}
		final MessageExecution maxPendingExecution = event.getMaxPendingExecution(true);
		if (maxPendingExecution != null) {
			final long creationTime = maxPendingExecution.getCreationTimeMillis();
			final long duration = systemTimeProvider.currentTimeMillis() - creationTime;
			if (duration >= haraKiriTimeoutCopy.longValue()) {
				LOGGER.warn("Max pending timeout '" + haraKiriTimeoutCopy + "' reached, try to cancel all executions.");
				return true;
			}
		}
		return false;
	}

	private void watchExecutionsOfSession(final HttpSession session, final WatchDogEventBuilder eventBuilder) {
		final List<MessageExecution> executions = executionsMap.get(session);
		removeCompletedExecutions(executions);
		if (executions == null || executions.isEmpty()) {
			executionsMap.remove(session);
			LOGGER.debug("Session has no executions and was removed from watchdog: " + session);
		}
		else {
			if (isSessionInactive(session)) {
				//avoid memory leak caused by out-dated result messages of inactive client
				removeMessageChannelAttribute(session);
				if (!cancelMessageExecutions(executions)) {
					//watch if no execution was canceled
					watchMessageExecutions(executions, eventBuilder);
				}
			}
			else {
				watchMessageExecutions(executions, eventBuilder);
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
		if (execution.isTerminated()) {
			return true;
		}
		else if (execution.isCanceled() && !execution.isStarted()) {
			return true;
		}
		else {
			return false;
		}
	}

	private boolean isSessionInactive(final HttpSession session) {
		try {
			return systemTimeProvider.currentTimeMillis() - session.getLastAccessedTime() > sessionInactivityTimeout;
		}
		catch (final IllegalStateException e) {
			//Session.getLastAccessedTime() throws an illegalStateException if session is invalid.
			//In this case session is no longer active.
			//Logging is omitted with purpose here because client stops the application is a normal use case that should not
			//produce warning or errors in log-file.
			//Unfortunately the interface does not allow to check the validity state of session
			return true;
		}
		catch (final Exception e) {
			LOGGER.error("Exception when checking sessions last accessed time", e);
			return true;
		}
	}

	private void removeMessageChannelAttribute(final HttpSession session) {
		try {
			session.removeAttribute(MessageServlet.MESSAGE_CHANNEL_ATTRIBUTE_NAME);
		}
		catch (final IllegalStateException e) {
			//ignore, probably session is invalid and will no longer be referenced is this case
		}
		catch (final Exception e) {
			LOGGER.error("Exception when remove attribute from session", e);
		}
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

	private void onHaraKiri(final WatchDogEvent event) {
		Assert.paramNotNull(event, "event");
		for (final IMessageExecutionWatchdogListener listener : watchdogListeners) {
			listener.onExecutionsHaraKiri(event);
		}
	}

	private void watchMessageExecutions(final Collection<MessageExecution> executions, final WatchDogEventBuilder eventBuilder) {
		for (final MessageExecution execution : executions) {
			watchMessageExecution(execution, eventBuilder);
		}
	}

	private void watchMessageExecution(final MessageExecution execution, final WatchDogEventBuilder eventBuilder) {
		if (!execution.isCanceled()) {
			if (execution.isStarted()) {
				final Thread executingThread = execution.getExecutingThread();
				if (executingThread != null) {
					//set interrupted executions on canceled to allow to watch unfinished canceled executions
					if (executingThread.isInterrupted()) {
						execution.cancel();
						//do not add the execution to the running executions
						return;
					}
				}
				eventBuilder.addRunningExecution(execution);
			}
			else {
				eventBuilder.addPendingExecution(execution);
			}
		}
		else if (execution.isStarted() && !execution.isTerminated()) {
			eventBuilder.addUnfinishedCancelExecution(execution);
		}
	}

	class WatchDogEventBuilder {

		private final long watchTimeMillis = systemTimeProvider.currentTimeMillis();
		private final List<MessageExecution> running = new LinkedList<MessageExecution>();
		private final List<MessageExecution> pending = new LinkedList<MessageExecution>();
		private final List<MessageExecution> unfinishedCancel = new LinkedList<MessageExecution>();

		WatchDogEventBuilder addRunningExecution(final MessageExecution execution) {
			running.add(execution);
			return this;
		}

		WatchDogEventBuilder addPendingExecution(final MessageExecution execution) {
			pending.add(execution);
			return this;
		}

		WatchDogEventBuilder addUnfinishedCancelExecution(final MessageExecution execution) {
			unfinishedCancel.add(execution);
			return this;
		}

		WatchDogEvent build() {
			return new WatchDogEvent(watchTimeMillis, running, pending, unfinishedCancel);
		}

	}
}
