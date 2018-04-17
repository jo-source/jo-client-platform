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
 * Allows to observe the message executions of the {@link MessageServlet}.
 */
public interface IMessageExecutionWatchdogListener {

	/**
	 * Will be invoked if a execution was removed from watchdog because it was terminated or canceled.
	 * 
	 * @param execution The execution that was removed
	 */
	void onExecutionRemove(final MessageExecution execution);

	/**
	 * This method will be invoked if a execution has been canceled by watchdog.
	 * 
	 * A execution may be canceled by several reasons, e.g. if the client is no longer active or because of server overload.
	 * 
	 * @param message The message that was canceled
	 * @param cancelTimeMillis The cancel timestamp in millis when cancel has been started
	 */
	void onExecutionCancel(Object message, long cancelTimeMillis);

	/**
	 * This method will be invoked if the watchdog cancels all executions due to kara-kiri conditions occured.
	 * 
	 * @param watchDogEvent The watch event that was crucial to do hara-kiri
	 */
	void onExecutionsHaraKiri(WatchDogEvent watchDogEvent);

	/**
	 * This method will be invoked every time the watchdog was executed
	 * 
	 * @param watchDogEvent The event of the watchdog execution
	 */
	void onExecutionsWatch(WatchDogEvent watchDogEvent);

}
