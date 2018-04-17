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

/**
 * Jmx interface for MessageServlet
 */
public interface IMessageServletMXBean {

	/**
	 * @return The number of executor threads the servlet uses
	 */
	int getThreadCount();

	/**
	 * Get's the duration to wait for messages to receive with the http get method.
	 * 
	 * @return The poll interval
	 */
	long getPollIntervalMillis();

	/**
	 * @return The interval in millis the executions will be watched by the watchdog
	 */
	long getWatchDogIntervalMillis();

	/**
	 * Set's the watchdog interval in millis
	 * 
	 * @param interval The interval to set
	 */
	void setWatchDogIntervalMillis(long interval);

	/**
	 * Get's the timeout in millis.
	 * 
	 * If no get or post will occur for this duration, the session will be assumed to be inactive and all executions of a inactive
	 * session will be canceled then.
	 * 
	 * Remark: This value should be at least 2 times higher than {@link #getPollIntervalMillis()} to avoid active sessions will be
	 * canceled.
	 * 
	 * @return The timeout
	 */
	long getSessionInactivityTimeoutMillis();

	/**
	 * Set's the session inactivity timeout value.
	 * 
	 * If no get or post will occur for this duration, the session will be assumed to be inactive and all executions of a inactive
	 * session will be canceled then.
	 * 
	 * Remark: This value should be at least 2 times higher than {@link #getPollIntervalMillis()} to avoid active sessions will be
	 * canceled.
	 * 
	 * @param timeout The timeout to set
	 */
	void setSessionInactivityTimeoutMillis(long timeout);

	/**
	 * Get's the deny request pending timeout in millis.
	 * 
	 * If there are no free execution threads and if at least one pending thread exists since this given timeout, further messages
	 * will be rejected with a 503 (service currently unavailable). In this case, all running and pending executions of the
	 * requesting sessions will be canceled.
	 * 
	 * @return the deny request pending timeout in millis
	 */
	long getDenyRequestPendingTimeoutMillis();

	/**
	 * Set's the deny request pending timeout in millis.
	 * 
	 * If there are no free execution threads and if at least one pending thread exists since this given timeout, further messages
	 * will be rejected with a 503 (service currently unavailable). In this case, all running and pending executions of the
	 * requesting sessions will be canceled.
	 * 
	 * @param timeout The timeout to set
	 */
	void setDenyRequestPendingTimeoutMillis(long timeout);

	/**
	 * Get's the hara-kiri timeout in millis.
	 * 
	 * If no execution threads are available for the given time, all executions will be canceled to make the system available
	 * again.
	 * 
	 * @return The timeout or null, if no timeout is set
	 */
	Long getHaraKiriTimeoutMillis();

	/**
	 * Set's the hara-kiri timeout in millis.
	 * 
	 * If no execution threads are available for the given time, all executions will be canceled to make the system available
	 * again.
	 * 
	 * @param timeout The timeout to set or null to disable hara-kiri timeout
	 */
	void setHaraKiriTimeoutMillis(Long timeout);

	/**
	 * Get's the hara-kiri pending threshold.
	 * 
	 * If the pending thread count reaches the given value,, all executions will be canceled to make the system available
	 * again.
	 * 
	 * @return The timeout or null, if no timeout is set
	 */
	Long getHaraKiriPendingThreshold();

	/**
	 * Set's the hara-kiri pending threshold.
	 * 
	 * If the pending thread count reaches the given value,, all executions will be canceled to make the system available
	 * again.
	 * 
	 * @param threshold The threshold to set or null to disable hara-kiri pending threshold
	 */
	void setHaraKiriPendingThreshold(Long threshold);

	/**
	 * Disables the hara-kiri feature by setting the timeout and threshold to null.
	 */
	void disableHaraKiri();

	/**
	 * Cancels all running an pending executions of all active sessions.
	 * 
	 * Remark: Currently only session's with running executions be get a feedback, client's with pending executions will get no
	 * exception on reply channel. Maybe this will be fixed in further release, or feel free to submit a fix for this issue.
	 */
	void cancelAllExecutions();

	/**
	 * Cancels the execution with the largest runtime.
	 */
	void cancelMaxRuntimeExecution();

}
