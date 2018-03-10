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

	private final long watchTimeMillis;
	private final List<MessageExecution> running;
	private final List<MessageExecution> pending;
	private final List<MessageExecution> unfinishedCancel;

	WatchDogEvent(
		final long watchTimeMillis,
		final List<MessageExecution> running,
		final List<MessageExecution> pending,
		final List<MessageExecution> unfinishedCancel) {

		this.watchTimeMillis = watchTimeMillis;
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
		long min = 0;
		MessageExecution result = null;
		for (final MessageExecution execution : getPendingExecutions()) {
			if (!execution.isCanceled() && !execution.isTerminated()) {
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
	 * Get's the running execution that has the maximal runtime or null, if no execution is running.
	 * 
	 * @return The oldest running execution or null if no running execution exists
	 */
	public MessageExecution getMaxRuntimeExecution() {
		long min = 0;
		MessageExecution result = null;
		for (final MessageExecution execution : getRunningExecutions()) {
			if (execution.isRunning()) {
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
