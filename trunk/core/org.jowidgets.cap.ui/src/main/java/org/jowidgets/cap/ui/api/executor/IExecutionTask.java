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

package org.jowidgets.cap.ui.api.executor;

import java.util.List;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.UserQuestionResult;


public interface IExecutionTask extends IExecutionCallback {

	/**
	 * Gets a unique identifier of this task.
	 * 
	 * @return the id, never null and unique for all created tasks in the same vm
	 */
	String getId();

	/**
	 * @return The number of steps this (sub) execution task contributes to its parent execution task or null,
	 *         if this execution task is the root task
	 */
	Integer getStepProportion();

	/**
	 * @return True if this execution task could be canceled by the user, false otherwise
	 */
	boolean isCancelable();

	/**
	 * Cancel the task.
	 */
	void cancel();

	/**
	 * Disposes the task.
	 */
	void dispose();

	/**
	 * Gets the total number of steps this execution has. If this value is not set, the
	 * execution is indeterminate (leads to an indeterminate progress bar)
	 * 
	 * @return The total number of steps
	 */
	Integer getTotalStepCount();

	/**
	 * Gets the number of steps that was worked by the executor (relative to the total step count)
	 * 
	 * @return stepCount The number of worked steps
	 */
	int getWorked();

	/**
	 * Gets the description of what this executor will currently do
	 * 
	 * @result The description of what this executor will currently do
	 */
	String getDescription();

	/**
	 * @return True if the execution was finished
	 */
	boolean isFinshed();

	/**
	 * Gets a question that should be offered to the user the execution ask.
	 * Questions should be answered by the user and the result should be set with
	 * {@link IExecutionTask#setQuestionResult(UserQuestionResult)}.
	 * 
	 * @return A question to the user or null, if no question should be offered to the user
	 */
	String getUserQuestion();

	/**
	 * Sets the result of a user question. After that, the method {@link IExecutionTask#getUserQuestion()} will return null.
	 * 
	 * @param result The result of the user question
	 */
	void setQuestionResult(UserQuestionResult result);

	/**
	 * @return all sub execution task of this task
	 */
	List<IExecutionTask> getSubExecutions();

	void addExecutionTaskListener(IExecutionTaskListener listener);

	void removeExecutionTaskListener(IExecutionTaskListener listener);

}
