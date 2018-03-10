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

public interface IMessageExecutionWatchdogListener {

	/**
	 * This method will be invoked if a execution has been canceled by watchdog
	 * 
	 * @param message The message that was canceled
	 * @param cancelTimeMillis The cancel timestamp
	 */
	void onExecutionCancel(Object message, long cancelTimeMillis);

	/**
	 * This method will be invoked for all pending executions.
	 * 
	 * A pending execution is a execution that has been created but not yet started
	 * 
	 * @param message The message to execute
	 * @param cancelTimeMillis The timestamp the execution was created
	 * @param watchTimeMillis The timestamp the watchdog has checked the execution
	 */
	void onPendingExecutionWatch(Object message, long creationTimeMillis, long watchTimeMillis);

	/**
	 * This method will be invoked for all running executions.
	 * 
	 * A running execution is a execution that has been started and that was not yet terminated and that was not yet canceled
	 * 
	 * @param message The message to execute
	 * @param creationTimeMillis The timestamp the execution was created
	 * @param startTimeMillis The timestamp the execution has been started
	 * @param watchTimeMillis The timestamp the watchdog has checked the execution
	 */
	void onRunningExecutionWatch(Object message, long creationTimeMillis, long startTimeMillis, long watchTimeMillis);

	/**
	 * This method will be invoked if a execution has been started, was canceled afterwards and has not been terminated yet
	 * 
	 * @param message The message that was executed
	 * @param cancelTimeMillis The timestamp the execution was canceled at
	 * @param watchTimeMillis The timestamp the watchdog has checked the execution
	 */
	void onUnfinishedCancelExecutionWatch(
		Object message,
		long creationTimeMillis,
		long startTimeMillis,
		long cancelTimeMillis,
		long watchTimeMillis);

}
