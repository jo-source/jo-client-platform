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

/**
 * MBean interface for execution status of {@link MessageServlet}
 */
public interface IMessageServletStatusMXBean {

	/**
	 * Get's the last date the executions was watched by watchdog
	 * 
	 * @return The last watch date or null if no watch still happened
	 */
	Date getLastExecutionWatch();

	/**
	 * @return The number of all executor threads the {@link MessageServlet} has in pool
	 */
	int getThreadCount();

	/**
	 * @return The number of threads available for message executions ({@link #getThreadCount()-#getRunningExecutionsCount()})
	 */
	int getAvailableThreadCount();

	/**
	 * Get's the number of http sessions currently active.
	 * 
	 * Active means here, that there are running, pending or unfinished cancel executions at the moment.
	 * 
	 * @return the number of http sessions currently active.
	 */
	int getActiveSessionCount();

	/**
	 * @return The number of the currently running executions
	 */
	int getRunningExecutionsCount();

	/**
	 * @return The number of the currently pending executions
	 */
	int getPendingExecutionsCount();

	/**
	 * @return The number of the executions that was canceled but not yet finished
	 */
	int getUnfinishedCancelExecutionsCount();

	/**
	 * @return The maximal runtime of the running executions at last execution watch
	 */
	long getLastMaxRuntimeInSeconds();

	/**
	 * @return The maximal pending duration of the pending executions at last execution watch
	 */
	long getLastMaxPendingDurationInSeconds();

	/**
	 * @return The maximal unfinished cancel duration of the unfinished canceled executions at last execution watch
	 */
	long getLastMaxUnfinishedCancelDurationInSeconds();

	/**
	 * @return The maximal runtime since server start or invocation of {@link #resetMaxRuntime()}
	 */
	long getMaxRuntimeInSeconds();

	/**
	 * @return The maximal pending duration since server start or invocation of {@link #resetMaxPendingDuration()}
	 */
	long getMaxPendingDurationInSeconds();

	/**
	 * @return The maximal unfinished cancel duration since server start or invocation of
	 *         {@link #resetMaxUnfinishedCancelDuration()}
	 */
	long getMaxUnfinishedCancelDurationSeconds();

	/**
	 * @return The average runtime of all terminated executions since server start or invocation of
	 *         {@link #resetAverageTerminatedRuntime()}
	 */
	long getAverageTerminationRuntimeInMillis();

	/**
	 * @return The number of hara-kiri invocations since server start or invocation of {@link #resetHaraKiriCount()}
	 */
	int getHaraKiriCount();

	/**
	 * @return The date the last hara-kiri invocation has been occurred or null if no such event happened or the
	 *         {@link #resetLastHaraKiri()} method was invoked after the last hara-kiri event.
	 */
	Date getLastHaraKiri();

	/**
	 * Resets the maximal runtime value to 0
	 */
	void resetMaxRuntime();

	/**
	 * Resets the maximal pending duration value to 0
	 */
	void resetMaxPendingDuration();

	/**
	 * Resets the maximal unfinished cancel duration value to 0
	 */
	void resetMaxUnfinishedCancelDuration();

	/**
	 * Resets the average termination runtime value to 0
	 */
	void resetAverageTerminatedRuntime();

	/**
	 * Resets the hara-kiri count to 0
	 */
	void resetHaraKiriCount();

	/**
	 * Resets the last hara-kri date
	 */
	void resetLastHaraKiri();

}
