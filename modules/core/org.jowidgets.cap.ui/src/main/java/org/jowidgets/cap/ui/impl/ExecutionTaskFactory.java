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

final class ExecutionTaskFactory implements IExecutionTaskFactory {

	@Override
	public IExecutionTask create() {
		final ExecutionTask result = new ExecutionTask();
		result.addExecutionTaskListener(new ExecutionTaskMonitor(result, Toolkit.getUiThreadAccess()));
		return result;
	}

	//TODO MG enhance this , maybe a monitoring context could be given by the create method
	private class ExecutionTaskMonitor implements IExecutionTaskListener {

		private final IUiThreadAccess uiThreadAccess;

		ExecutionTaskMonitor(final IExecutionTask executionTask, final IUiThreadAccess uiThreadAccess) {
			this.uiThreadAccess = uiThreadAccess;
		}

		@Override
		public void worked(final int totalWorked) {}

		@Override
		public void userQuestionAsked(final String question, final IUserAnswerCallback callback) {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					final QuestionResult result = Toolkit.getQuestionPane().askYesNoQuestion(question);

					if (QuestionResult.YES == result) {
						callback.setQuestionResult(UserQuestionResult.YES);
					}
					else {
						callback.setQuestionResult(UserQuestionResult.NO);
					}
				}
			});
		}

		@Override
		public void totalStepCountChanged(final int totalStepCount) {}

		@Override
		public void subExecutionAdded(final IExecutionTask executionTask) {
			executionTask.addExecutionTaskListener(new ExecutionTaskMonitor(executionTask, uiThreadAccess));
		}

		@Override
		public void finished() {}

		@Override
		public void descriptionChanged(final String description) {}

	}
}
