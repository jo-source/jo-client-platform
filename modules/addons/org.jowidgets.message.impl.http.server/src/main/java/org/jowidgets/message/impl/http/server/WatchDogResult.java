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

public final class WatchDogResult {

	private final long watchTimeMillis;
	private final List<MessageExecution> running;
	private final List<MessageExecution> pending;
	private final List<MessageExecution> unfinishedCancel;

	WatchDogResult(
		final long watchTimeMillis,
		final List<MessageExecution> running,
		final List<MessageExecution> pending,
		final List<MessageExecution> unfinishedCancel) {

		this.watchTimeMillis = watchTimeMillis;
		this.running = Collections.unmodifiableList(new ArrayList<MessageExecution>(running));
		this.pending = Collections.unmodifiableList(new ArrayList<MessageExecution>(pending));
		this.unfinishedCancel = Collections.unmodifiableList(new ArrayList<MessageExecution>(unfinishedCancel));
	}

	public long getWatchTimeMillis() {
		return watchTimeMillis;
	}

	public List<MessageExecution> getRunningExecutions(final long minRuntime) {
		final List<MessageExecution> result = new LinkedList<MessageExecution>();
		for (final MessageExecution execution : getRunningExecutions()) {
			if ((watchTimeMillis - execution.getHandlerStartTimeMillis().longValue()) >= minRuntime) {
				result.add(execution);
			}
		}
		return Collections.unmodifiableList(result);
	}

	public List<MessageExecution> getRunningExecutions() {
		return running;
	}

	public List<MessageExecution> getPendingExecutions() {
		return pending;
	}

	public List<MessageExecution> getPendingExecutions(final long minPendingDuration) {
		final List<MessageExecution> result = new LinkedList<MessageExecution>();
		for (final MessageExecution execution : getPendingExecutions()) {
			if ((watchTimeMillis - execution.getCreationTimeMillis()) >= minPendingDuration) {
				result.add(execution);
			}
		}
		return Collections.unmodifiableList(result);
	}

	public List<MessageExecution> getUnfinishedCancelExecutions() {
		return unfinishedCancel;
	}

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
