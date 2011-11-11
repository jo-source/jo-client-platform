/*
 * Copyright (c) 2011, grossmann
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

package org.jowidgets.cap.ui.api.execution;

import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.common.types.IVetoable;

/**
 * Execution interceptor to intercept into executions.
 * 
 * Remark: All methods will be invoked in the UiThread,
 */
public interface IExecutionInterceptor {

	/**
	 * Will be invoked before the execution occurs
	 * 
	 * @param vetoable Veto could be set, if the execution should not continue
	 * @param executionContext
	 */
	void beforeExecution(IExecutionContext executionContext, IVetoable continueExecution);

	/**
	 * Will be invoked if any interceptor has set a veto in the {@link #beforeExecution(IExecutionContext, IVetoable)} method.
	 * 
	 * @param executionContext
	 */
	void onExecutionVeto(IExecutionContext executionContext);

	/**
	 * Will be invoked after the execution was prepared and before (potential asynchronous) execution
	 * service(s) will be started.
	 * 
	 * @param executionContext
	 */
	void afterExecutionPrepared(IExecutionContext executionContext);

	/**
	 * Will be invoked, after the execution has been finished with success.
	 * 
	 * Remark: For executions not running in batch mode, this may be invoked for each sub execution!
	 * 
	 * @param executionContext
	 */
	void afterExecutionSuccess(IExecutionContext executionContext);

	/**
	 * Will be invoked, after the execution has been finished with an error
	 * 
	 * Remark: For executions not running in batch mode, this may be invoked for each sub execution!
	 * 
	 * @param executionContext
	 * @param error The error or null
	 */
	void afterExecutionError(IExecutionContext executionContext, Throwable error);

	/**
	 * Will be invoked, after the execution has been canceled by the user
	 * 
	 * Remark: For executions not running in batch mode, this may be invoked for each sub execution!
	 * 
	 * @param executionContext
	 */
	void afterExecutionUserCanceled(IExecutionContext executionContext);

}
