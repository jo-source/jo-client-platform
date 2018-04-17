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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;

/**
 * {@link IMessageExecutionWatchdogListener} implementation that logs states with info level if any execution is pending, running
 * or has an unfinished cancel.
 * 
 * If critical thresholds reached, warning will be logged.
 */
public final class LoggingWatchdogListener implements IMessageExecutionWatchdogListener {

	private static final ILogger LOGGER = LoggerProvider.get(LoggingWatchdogListener.class);

	private static final String NOT_AVAILABLE = "n.a.";

	private static final long PENDING_EXECUTIONS_WARN_THRESHOLD = 30 * 1000; // 30 seconds
	private static final long RUNNING_EXECUTIONS_WARN_THRESHOLD = 30 * 60 * 1000; // 30 minutes
	private static final long UNFINISHED_CANCEL_WARN_THRESHOLD = 10 * 1000; // 10 seconds

	@Override
	public void onExecutionsWatch(final WatchDogEvent event) {
		final MessageExecution maxPendingExecution = event.getMaxPendingExecution(true);
		if (maxPendingExecution != null) {
			final long delay = event.getWatchTimeMillis() - maxPendingExecution.getCreationTimeMillis();
			if (delay >= PENDING_EXECUTIONS_WARN_THRESHOLD) {
				LOGGER.warn("There are pending executions since " + getDurationInProperUnit(delay) + ", " + event);
			}
		}

		final MessageExecution maxRuntimeExecution = event.getMaxRuntimeExecution(true);
		if (maxRuntimeExecution != null) {
			final long delay = event.getWatchTimeMillis() - maxRuntimeExecution.getStartTimeMillis().longValue();
			if (delay >= RUNNING_EXECUTIONS_WARN_THRESHOLD) {
				LOGGER.warn("There are running executions since " + getDurationInProperUnit(delay) + ", " + event);
			}
		}

		final List<MessageExecution> unfinishedCancelExecutions = event.getUnfinishedCancelExecutions(
				UNFINISHED_CANCEL_WARN_THRESHOLD);
		if (unfinishedCancelExecutions.size() > 0) {
			LOGGER.warn("There are " + unfinishedCancelExecutions.size() + " unfinished cancel executions");
		}

		if (event.getPendingExecutions().size() > 0
			|| event.getRunningExecutions().size() > 0
			|| event.getUnfinishedCancelExecutions().size() > 0) {
			LOGGER.info(event.toString());
		}

	}

	@Override
	public void onExecutionRemove(final MessageExecution execution) {
		LOGGER.debug("Execution removed from watchdog: " + execution);
	}

	@Override
	public void onExecutionCancel(final Object message, final long cancelTimeMillis) {
		LOGGER.info("Message canceled by watchdog: " + message);
	}

	@Override
	public void onExecutionsHaraKiri(final WatchDogEvent event) {
		final String maxPending = getMaxPending(event);
		final String maxRunning = getMaxRunning(event);
		LOGGER.warn(
				"All executions canceled because server has no more threads for "
					+ maxPending
					+ ", max runtime: "
					+ maxRunning
					+ ", "
					+ event);

	}

	private String getMaxPending(final WatchDogEvent event) {
		final MessageExecution execution = event.getMaxPendingExecution();
		if (execution == null) {
			return NOT_AVAILABLE;
		}
		return getDuration(Long.valueOf(execution.getCreationTimeMillis()), event.getWatchTimeMillis());
	}

	private String getMaxRunning(final WatchDogEvent event) {
		final MessageExecution execution = event.getMaxRuntimeExecution();
		if (execution == null) {
			return NOT_AVAILABLE;
		}
		return getDuration(Long.valueOf(execution.getCreationTimeMillis()), event.getWatchTimeMillis());
	}

	private String getDuration(final Long sinceMillis, final long watchTimeMillis) {
		if (sinceMillis == null) {
			return NOT_AVAILABLE;
		}
		return getDurationInProperUnit(watchTimeMillis - sinceMillis.longValue());
	}

	private String getDurationInProperUnit(final long duration) {
		final long hours = TimeUnit.MILLISECONDS.toHours(duration);
		final long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
		final long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
		if (hours > 0) {
			return hours + " hours, " + (minutes - (hours * 60)) + " minutes";
		}
		else if (minutes > 0) {
			return minutes + " minutes, " + (seconds - (minutes * 60)) + " seconds";
		}
		else if (seconds > 0) {
			return seconds + " seconds";
		}
		else {
			return duration + " millis";
		}
	}

}
