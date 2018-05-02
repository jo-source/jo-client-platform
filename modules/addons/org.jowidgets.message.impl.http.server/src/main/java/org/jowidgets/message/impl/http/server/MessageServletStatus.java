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

import java.util.Date;

import org.jowidgets.util.Assert;

public final class MessageServletStatus implements IMessageServletStatusMXBean {

	private Date lastExecutionWatch;

	private int threadCount;
	private int activeSessionCount;

	private int runningExecutionsCount;
	private int pendingExecutionsCount;
	private int unfinishedCancelExecutionsCount;

	private long lastMaxRuntimeInSeconds;
	private long lastMaxPendingDurationInSeconds;
	private long lastMaxUnfinishedCancelDurationInSeconds;

	private long maxRuntimeInSeconds;
	private long maxPendingDurationInSeconds;
	private long maxUnfinishedCancelDurationSeconds;

	private long terminationRuntime;
	private long terminatedExecutionCount;

	private int haraKiriCount;
	private Date lastHaraKiri;

	@Override
	public Date getLastExecutionWatch() {
		return lastExecutionWatch;
	}

	@Override
	public int getThreadCount() {
		return threadCount;
	}

	@Override
	public int getAvailableThreadCount() {
		return threadCount - (runningExecutionsCount + unfinishedCancelExecutionsCount);
	}

	@Override
	public int getActiveSessionCount() {
		return activeSessionCount;
	}

	@Override
	public int getRunningExecutionsCount() {
		return runningExecutionsCount;
	}

	@Override
	public int getPendingExecutionsCount() {
		return pendingExecutionsCount;
	}

	@Override
	public int getUnfinishedCancelExecutionsCount() {
		return unfinishedCancelExecutionsCount;
	}

	@Override
	public long getLastMaxRuntimeInSeconds() {
		return lastMaxRuntimeInSeconds;
	}

	@Override
	public long getLastMaxPendingDurationInSeconds() {
		return lastMaxPendingDurationInSeconds;
	}

	@Override
	public long getLastMaxUnfinishedCancelDurationInSeconds() {
		return lastMaxUnfinishedCancelDurationInSeconds;
	}

	@Override
	public long getMaxRuntimeInSeconds() {
		return maxRuntimeInSeconds;
	}

	@Override
	public long getMaxPendingDurationInSeconds() {
		return maxPendingDurationInSeconds;
	}

	@Override
	public long getMaxUnfinishedCancelDurationSeconds() {
		return maxUnfinishedCancelDurationSeconds;
	}

	@Override
	public long getAverageRuntimeInMillis() {
		if (terminatedExecutionCount == 0) {
			return 0;
		}
		else {
			return terminationRuntime / terminatedExecutionCount;
		}
	}

	@Override
	public int getHaraKiriCount() {
		return haraKiriCount;
	}

	@Override
	public Date getLastHaraKiri() {
		return lastHaraKiri;
	}

	@Override
	public void resetMaxRuntime() {
		maxRuntimeInSeconds = 0;
	}

	@Override
	public void resetMaxPendingDuration() {
		maxPendingDurationInSeconds = 0;
	}

	@Override
	public void resetMaxUnfinishedCancelDuration() {
		maxUnfinishedCancelDurationSeconds = 0;
	}

	@Override
	public void resetAverageRuntime() {
		terminationRuntime = 0;
		terminatedExecutionCount = 0;
	}

	@Override
	public void resetHaraKiriCount() {
		haraKiriCount = 0;
	}

	@Override
	public void resetLastHaraKiri() {
		setLastHaraKiri(null);
	}

	@Override
	public void resetAll() {
		lastExecutionWatch = null;
		threadCount = 0;
		activeSessionCount = 0;
		runningExecutionsCount = 0;
		pendingExecutionsCount = 0;
		unfinishedCancelExecutionsCount = 0;
		lastMaxRuntimeInSeconds = 0;
		lastMaxPendingDurationInSeconds = 0;
		lastMaxUnfinishedCancelDurationInSeconds = 0;
		maxRuntimeInSeconds = 0;
		maxPendingDurationInSeconds = 0;
		maxUnfinishedCancelDurationSeconds = 0;
		terminationRuntime = 0;
		terminatedExecutionCount = 0;
		haraKiriCount = 0;
		lastHaraKiri = null;
	}

	void setLastExecutionWatch(final Date lastExecutionWatch) {
		this.lastExecutionWatch = lastExecutionWatch;
	}

	void setThreadCount(final int threadCount) {
		this.threadCount = threadCount;
	}

	void setActiveSessionCount(final int activeSessionCount) {
		this.activeSessionCount = activeSessionCount;
	}

	void setRunningExecutionsCount(final int runningExecutionsCount) {
		this.runningExecutionsCount = runningExecutionsCount;
	}

	void setPendingExecutionsCount(final int pendingExecutionsCount) {
		this.pendingExecutionsCount = pendingExecutionsCount;
	}

	void setUnfinishedCancelExecutionsCount(final int unfinishedCancelExecutionsCount) {
		this.unfinishedCancelExecutionsCount = unfinishedCancelExecutionsCount;
	}

	void setLastMaxRuntimeInSeconds(final long currentMaxRuntimeInSeconds) {
		this.lastMaxRuntimeInSeconds = currentMaxRuntimeInSeconds;
		if (maxRuntimeInSeconds < currentMaxRuntimeInSeconds) {
			maxRuntimeInSeconds = currentMaxRuntimeInSeconds;
		}
	}

	void setLastMaxPendingDurationInSeconds(final long currentMaxPendingDurationInSeconds) {
		this.lastMaxPendingDurationInSeconds = currentMaxPendingDurationInSeconds;
		if (maxPendingDurationInSeconds < currentMaxPendingDurationInSeconds) {
			maxPendingDurationInSeconds = currentMaxPendingDurationInSeconds;
		}
	}

	void setLastMaxUnfinishedCancelDurationInSeconds(final long currentMaxUnfinishedCancelDurationInSeconds) {
		this.lastMaxUnfinishedCancelDurationInSeconds = currentMaxUnfinishedCancelDurationInSeconds;
		if (maxUnfinishedCancelDurationSeconds < currentMaxUnfinishedCancelDurationInSeconds) {
			maxUnfinishedCancelDurationSeconds = currentMaxUnfinishedCancelDurationInSeconds;
		}
	}

	void calculateAverageRuntimeInMillis(final MessageExecution execution) {
		Assert.paramNotNull(execution, "execution");
		if (execution.isTerminated()) {
			terminationRuntime += execution.getRuntimeMillis().longValue();
			terminatedExecutionCount++;
		}
	}

	void incrementHaraKiriCount() {
		this.haraKiriCount++;
	}

	void setLastHaraKiri(final Date lastHaraKiri) {
		this.lastHaraKiri = lastHaraKiri;
	}

}
