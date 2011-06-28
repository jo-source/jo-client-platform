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

package org.jowidgets.cap.invocation.server;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IUserQuestionCallback;
import org.jowidgets.cap.common.api.execution.UserQuestionResult;
import org.jowidgets.cap.invocation.common.Progress;
import org.jowidgets.invocation.service.common.api.ICancelListener;
import org.jowidgets.invocation.service.common.api.IInterimResponseCallback;
import org.jowidgets.invocation.service.common.api.IInvocationCallback;
import org.jowidgets.util.Assert;

final class ServerExecutionCallback implements IExecutionCallback {

	private final IInvocationCallback<Object> invocationCallback;
	private final IInterimResponseCallback<Progress> interimResponseCallback;
	private final Set<IExecutionCallbackListener> executionCallbackListeners;

	private boolean canceled;

	ServerExecutionCallback(
		final IInvocationCallback<Object> invocationCallback,
		final IInterimResponseCallback<Progress> interimResponseCallback) {

		Assert.paramNotNull(invocationCallback, "invocationCallback");
		Assert.paramNotNull(interimResponseCallback, "interimResponseCallback");
		this.invocationCallback = invocationCallback;
		this.interimResponseCallback = interimResponseCallback;
		this.executionCallbackListeners = new HashSet<IExecutionCallbackListener>();

		this.canceled = false;

		invocationCallback.addCancelListener(new ICancelListener() {
			@Override
			public void canceled() {
				canceled = true;
				for (final IExecutionCallbackListener listener : new LinkedList<IExecutionCallbackListener>(
					executionCallbackListeners)) {
					listener.executionCanceled();
				}
			}
		});

	}

	@Override
	public boolean isCanceled() {
		return canceled;
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
		return null;
	}

	@Override
	public void userQuestion(final String question, final IUserQuestionCallback callback) {}

	@Override
	public IExecutionCallback createSubExecution(final int stepProportion, final boolean cancelable) {
		return new ServerExecutionCallback(invocationCallback, interimResponseCallback);
	}

	@Override
	public void addExecutionCallbackListener(final IExecutionCallbackListener listener) {
		Assert.paramNotNull(listener, "listener");
		executionCallbackListeners.add(listener);
	}

	@Override
	public void removeExecutionCallbackListener(final IExecutionCallbackListener listener) {
		Assert.paramNotNull(listener, "listener");
		executionCallbackListeners.remove(listener);
	}

}
