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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a snapshot of all unfinished {@link MessageExecution} objects.
 * 
 * Remark that the state of the executions may have been changed since last watchdog execution.
 */
public final class WatchDogEvent {

	private final int threadCount;
	private final long watchTimeMillis;
	private final int activeSessionCount;;
	private final List<MessageExecution> running;
	private final List<MessageExecution> pending;
	private final List<MessageExecution> unfinishedCancel;

	WatchDogEvent(
		final int threadCount,
		final long watchTimeMillis,
		final int activeSessionCount,
		final List<MessageExecution> running,
		final List<MessageExecution> pending,
		final List<MessageExecution> unfinishedCancel) {

		this.threadCount = threadCount;
		this.watchTimeMillis = watchTimeMillis;
		this.activeSessionCount = activeSessionCount;
		this.running = Collections.unmodifiableList(new ArrayList<MessageExecution>(running));
		this.pending = Collections.unmodifiableList(new ArrayList<MessageExecution>(pending));
		this.unfinishedCancel = Collections.unmodifiableList(new ArrayList<MessageExecution>(unfinishedCancel));
	}

	/**
	 * Gets the timestamp in millis the executions was watched.
	 * 
	 * @return The timestamp of the watch
	 */
	public long getWatchTimeMillis() {
		return watchTimeMillis;
	}

	/**
	 * Gets the number of threads used for message execution by {@link MessageServlet}
	 * 
	 * @return The number of threads
	 */
	public int getThreadCount() {
		return threadCount;
	}

	/**
	 * Get's the number of currently active http sessions.
	 * 
	 * @return The number of active sessions
	 */
	public int getActiveSessionCount() {
		return activeSessionCount;
	}

	/**
	 * Get's all running executions.
	 * 
	 * A execution is running if it was scheduled and started but not yet finished.
	 * 
	 * @return A list of running executions, may be empty but never null
	 */
	public List<MessageExecution> getRunningExecutions() {
		return running;
	}

	/**
	 * Get's the running executions with a given minimal runtime.
	 * 
	 * A execution is running if it was scheduled and started but not yet finished.
	 * 
	 * @param minRuntime The minimal runtime each returned execution has
	 * 
	 * @return A list of running executions, may be empty but never null
	 */
	public List<MessageExecution> getRunningExecutions(final long minRuntime) {
		final List<MessageExecution> result = new LinkedList<MessageExecution>();
		for (final MessageExecution execution : getRunningExecutions()) {
			if ((watchTimeMillis - execution.getStartTimeMillis().longValue()) >= minRuntime) {
				result.add(execution);
			}
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * Get's all pending executions.
	 * 
	 * A execution is pending, if it was scheduled but not yet started.
	 * 
	 * @return A list of pending executions, may be empty but never null
	 */
	public List<MessageExecution> getPendingExecutions() {
		return pending;
	}

	/**
	 * Get's the pending executions with a given minimal pending duration.
	 * 
	 * A execution is pending, if it was scheduled but not yet started.
	 * 
	 * @param minPendingDuration The minimal pending duration each returned execution has
	 * 
	 * @return A list of pending executions, may be empty but never null
	 */
	public List<MessageExecution> getPendingExecutions(final long minPendingDuration) {
		final List<MessageExecution> result = new LinkedList<MessageExecution>();
		for (final MessageExecution execution : getPendingExecutions()) {
			if ((watchTimeMillis - execution.getCreationTimeMillis()) >= minPendingDuration) {
				result.add(execution);
			}
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * Get's the pending execution that has the maximal pending duration or null, if no execution is pending.
	 * 
	 * The max pending execution duration represents the duration the messaging can not handle events since.
	 * 
	 * @return The oldest pending execution or null if no pending execution exists
	 */
	public MessageExecution getMaxPendingExecution() {
		return getMaxPendingExecution(false);
	}

	/**
	 * Get's the pending execution that has the maximal pending duration or null, if no execution is pending.
	 * 
	 * The max pending execution duration represents the duration the messaging can not handle events since.
	 * 
	 * @param omitCanceledOrTerminated If true, executions already canceled or terminated will be filtered from result
	 * 
	 * @return The oldest pending execution or null if no pending execution exists
	 */
	public MessageExecution getMaxPendingExecution(final boolean filterCanceledOrTerminated) {
		long min = 0;
		MessageExecution result = null;
		for (final MessageExecution execution : getPendingExecutions()) {
			if (!filterCanceledOrTerminated || (!execution.isCanceled() && !execution.isTerminated())) {
				if (result == null) {
					result = execution;
					min = execution.getCreationTimeMillis();
				}
				else {
					final long creationTimeMillis = execution.getCreationTimeMillis();
					if (creationTimeMillis < min) {
						result = execution;
						min = creationTimeMillis;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Gets the duration in millis of the max pending execution
	 * 
	 * @return The duration of the max pending execution in millis
	 */
	public long getMaxPendingDurationMillis() {
		final MessageExecution execution = getMaxPendingExecution();
		if (execution != null) {
			return watchTimeMillis - execution.getCreationTimeMillis();
		}
		else {
			return 0;
		}
	}

	/**
	 * Get's the running execution that has the maximal runtime or null, if no execution is running.
	 * 
	 * @return The oldest running execution or null if no running execution exists
	 */
	public MessageExecution getMaxRuntimeExecution() {
		return getMaxRuntimeExecution(false);
	}

	/**
	 * Get's the running execution that has the maximal runtime or null, if no execution is running.
	 * 
	 * @param filterNotRunning If true, executions no longer running will be filtered from result
	 * 
	 * @return The oldest running execution or null if no running execution exists
	 */
	public MessageExecution getMaxRuntimeExecution(final boolean filterNotRunning) {
		long min = 0;
		MessageExecution result = null;
		for (final MessageExecution execution : getRunningExecutions()) {
			if (!filterNotRunning || execution.isRunning()) {
				if (result == null) {
					result = execution;
					min = execution.getStartTimeMillis().longValue();
				}
				else {
					final long startTimeMillis = execution.getStartTimeMillis().longValue();
					if (startTimeMillis < min) {
						result = execution;
						min = startTimeMillis;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Gets the duration in millis of the max max runtime execution execution
	 * 
	 * @return The duration of the max runtime execution in millis
	 */
	public long getMaxRuntimeMillis() {
		final MessageExecution execution = getMaxRuntimeExecution(false);
		if (execution != null) {
			return watchTimeMillis - execution.getStartTimeMillis().longValue();
		}
		else {
			return 0;
		}
	}

	/**
	 * Get's all canceled executions where cancel was not yet completed.
	 * 
	 * A execution is in state unfinished cancel if it was canceled but the execution thread has not yet been successfully
	 * interrupted.
	 * 
	 * @return A list of unfinished canceled executions, may be empty but never null
	 */
	public List<MessageExecution> getUnfinishedCancelExecutions() {
		return unfinishedCancel;
	}

	/**
	 * Get's the canceled executions where cancel was not yet completed with a given cancel duration.
	 * 
	 * A execution is in state unfinished cancel if it was canceled but the execution thread has not yet been successfully
	 * interrupted.
	 * 
	 * @param minCancelDuration The minimal cancel duration each returned execution has
	 * 
	 * @return A list of unfinished canceled executions, may be empty but never null
	 */
	public List<MessageExecution> getUnfinishedCancelExecutions(final long minCancelDuration) {
		final List<MessageExecution> result = new LinkedList<MessageExecution>();
		for (final MessageExecution execution : getUnfinishedCancelExecutions()) {
			if ((watchTimeMillis - execution.getCanceledTimeMillis().longValue()) >= minCancelDuration) {
				result.add(execution);
			}
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * @return The unfinished cancel execution that has the maximal cancel duration or null, if no such execution exists
	 */
	public MessageExecution getMaxUnfinishedCancelExecution() {
		long min = 0;
		MessageExecution result = null;
		for (final MessageExecution execution : getUnfinishedCancelExecutions()) {
			if (result == null) {
				result = execution;
				min = execution.getCanceledTimeMillis().longValue();
			}
			else {
				final long cancelTime = execution.getCanceledTimeMillis().longValue();
				if (cancelTime < min) {
					result = execution;
					min = cancelTime;
				}
			}
		}
		return result;
	}

	/**
	 * @return The duration in millis of the unfinished cancel execution that has the maximal cancel duration
	 */
	public long getMaxUnfinishedCancelDurationMillis() {
		final MessageExecution execution = getMaxUnfinishedCancelExecution();
		if (execution != null) {
			return watchTimeMillis - execution.getCanceledTimeMillis().longValue();
		}
		else {
			return 0;
		}
	}

	@Override
	public String toString() {
		return "WatchDogResult [running="
			+ running.size()
			+ ", pending="
			+ pending.size()
			+ ", unfinishedCancel="
			+ unfinishedCancel.size()
			+ "]";
	}

}
