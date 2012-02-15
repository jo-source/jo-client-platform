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

package org.jowidgets.cap.ui.impl;

import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.types.QuestionResult;
import org.jowidgets.cap.common.api.execution.UserQuestionResult;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.execution.IExecutionTaskFactory;
import org.jowidgets.cap.ui.api.execution.IExecutionTaskListener;
import org.jowidgets.cap.ui.api.execution.IUserAnswerCallback;
import org.jowidgets.util.ValueHolder;

final class ExecutionTaskFactory implements IExecutionTaskFactory {

	@Override
	public IExecutionTask create() {
		final ExecutionTask result = new ExecutionTask();
		result.addExecutionTaskListener(new ExecutionTaskMonitor(result));
		return result;
	}

	//TODO MG enhance this , maybe a monitoring context could be given by the create method
	private class ExecutionTaskMonitor implements IExecutionTaskListener {

		private final IExecutionTask executionTask;
		private final IUiThreadAccess uiThreadAccess;

		ExecutionTaskMonitor(final IExecutionTask executionTask) {
			this.executionTask = executionTask;
			this.uiThreadAccess = Toolkit.getUiThreadAccess();
		}

		@Override
		public void worked(final int totalWorked) {
			//CHECKSTYLE:OFF
			System.out.println("WORKED " + totalWorked);
			//CHECKSTYLE:ON
		}

		@Override
		public void userQuestionAsked(final String question, final IUserAnswerCallback callback) {
			final ValueHolder<QuestionResult> resultHolder = new ValueHolder<QuestionResult>();
			try {
				uiThreadAccess.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						final QuestionResult result = Toolkit.getQuestionPane().askYesNoQuestion(question);
						resultHolder.set(result);
					}
				});
			}
			catch (final InterruptedException e) {
				callback.setQuestionResult(UserQuestionResult.NO);
			}

			if (QuestionResult.YES == resultHolder.get()) {
				callback.setQuestionResult(UserQuestionResult.YES);
			}
			else {
				callback.setQuestionResult(UserQuestionResult.NO);
			}

		}

		@Override
		public void totalStepCountChanged(final int totalStepCount) {
			//CHECKSTYLE:OFF
			System.out.println("TOTAL STEP COUNT " + totalStepCount);
			//CHECKSTYLE:ON
		}

		@Override
		public void subExecutionAdded(final IExecutionTask executionTask) {
			//CHECKSTYLE:OFF
			System.out.println("SUB EXECUTION TASK ADDED ");
			//CHECKSTYLE:ON
			executionTask.addExecutionTaskListener(new ExecutionTaskMonitor(executionTask));
		}

		@Override
		public void finished() {
			//CHECKSTYLE:OFF
			System.out.println("FINISHED " + executionTask.isFinshed());
			//CHECKSTYLE:ON
		}

		@Override
		public void descriptionChanged(final String description) {
			//CHECKSTYLE:OFF
			System.out.println("DESCRIPTION CHANGED " + description);
			//CHECKSTYLE:ON
		}

	}
}
