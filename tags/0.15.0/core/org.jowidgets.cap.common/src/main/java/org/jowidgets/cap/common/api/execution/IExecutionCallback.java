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

package org.jowidgets.cap.common.api.execution;

public interface IExecutionCallback {

	/**
	 * Informs an executor that this execution or one of its parent executions was canceled by the user.
	 * 
	 * @return True, if the user has canceled the execution, false otherwise.
	 */
	boolean isCanceled();

	/**
	 * Sets the total number of steps this execution has. If this value is not set, the
	 * execution is indeterminate (leads to an indeterminate progress bar)
	 * 
	 * @param stepCount The total number of steps to set
	 */
	void setTotalStepCount(int stepCount);

	/**
	 * Sets the number of steps that was worked by the executor (relative to the total step count)
	 * 
	 * @param stepCount The number of worked steps
	 */
	void worked(int stepCount);

	/**
	 * One step of the total step count was worked by the executor.
	 * 
	 * This is a convenience method and does the same than worked(1);
	 * 
	 * @see IExecutionCallback#worked(int)
	 */
	void workedOne();

	/**
	 * Sets the description of what this executor will do now
	 * 
	 * @param description The description of what this executor will do now
	 */
	void setDescription(String description);

	/**
	 * Marks this execution as finished. After that it can no longer be canceled.
	 */
	void finshed();

	/**
	 * Offers a question to the user.
	 * 
	 * Remark: This method blocks, until the user has answered the
	 * given question.
	 * 
	 * @param question The question to ask the user.
	 * 
	 * @return The result of the user question
	 */
	UserQuestionResult userQuestion(String question);

	/**
	 * Offers a question to the user.
	 * 
	 * Remark: This method does not block, but therefore the given call back
	 * will be invoked, after the user has answered the question.
	 * 
	 * Implementors can assume, that the call back will be invoked in any
	 * usual case where no exception has been occurred (e.g. client server problems)
	 * 
	 * @param question The question to ask the user.
	 * @param callback The call back that will be invoked, after the user has answered the question
	 * 
	 */
	void userQuestion(String question, IUserQuestionCallback callback);

	/**
	 * Creates a sub execution from this execution.
	 * 
	 * @param stepProportion The number of steps the sub execution contributes to this execution.
	 * @return The execution handle of the sub execution
	 */
	IExecutionCallback createSubExecution(int stepProportion);

	void addExecutionCallbackListener(IExecutionCallbackListener listener);

	void removeExecutionCallbackListener(IExecutionCallbackListener listener);

}
