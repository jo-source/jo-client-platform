/*
 * Copyright (c) 2012, grossmann
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

package org.jowidgets.cap.common.tools.execution;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IUserQuestionCallback;
import org.jowidgets.cap.common.api.execution.UserQuestionResult;
import org.jowidgets.util.Assert;

public class DummyExecutionCallback implements IExecutionCallback {

	@Override
	public boolean isCanceled() {
		return false;
	}

	@Override
	public void setTotalStepCount(final int stepCount) {}

	@Override
	public void worked(final int stepCount) {}

	@Override
	public void workedOne() {}

	@Override
	public void setDescription(final String description) {}

	@Override
	public void finshed() {}

	@Override
	public UserQuestionResult userQuestion(final String question) {
		return UserQuestionResult.YES;
	}

	@Override
	public void userQuestion(final String question, final IUserQuestionCallback callback) {
		Assert.paramNotNull(callback, "callback");
		callback.questionAnswered(UserQuestionResult.YES);
	}

	@Override
	public IExecutionCallback createSubExecution(final int stepProportion) {
		return new DummyExecutionCallback();
	}

	@Override
	public void addExecutionCallbackListener(final IExecutionCallbackListener listener) {}

	@Override
	public void removeExecutionCallbackListener(final IExecutionCallbackListener listener) {}

}
