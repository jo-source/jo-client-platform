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

package org.jowidgets.cap.remoting.client;

import java.util.HashMap;
import java.util.Map;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.remoting.common.Progress;
import org.jowidgets.invocation.service.common.api.IInterimResponseCallback;
import org.jowidgets.util.Assert;
import org.jowidgets.util.NullCompatibleEquivalence;
import org.jowidgets.util.Tuple;
import org.jowidgets.util.ValueHolder;

final class ProgressResponseCallback implements IInterimResponseCallback<Progress> {

	private final IExecutionCallback executionCallback;
	private final Map<Object, Tuple<ValueHolder<Progress>, IExecutionCallback>> subCallbacks;

	private Progress lastProgress;

	ProgressResponseCallback(final IExecutionCallback executionCallback) {
		Assert.paramNotNull(executionCallback, "executionCallback");
		this.executionCallback = executionCallback;
		this.subCallbacks = new HashMap<Object, Tuple<ValueHolder<Progress>, IExecutionCallback>>();
		this.lastProgress = new Progress();
	}

	@Override
	public void response(final Progress progress) {
		setSubProgress(progress, executionCallback);

		setProgress(progress, lastProgress, executionCallback);
		this.lastProgress = progress;
	}

	private void setSubProgress(final Progress progress, final IExecutionCallback executionCallback) {
		for (final Progress subProgress : progress.getSubProgressList()) {
			//set the subProgress recursively
			setSubProgress(subProgress, executionCallback);

			//check if the task id is already registered
			Tuple<ValueHolder<Progress>, IExecutionCallback> subCallback = subCallbacks.get(subProgress.getTaskId());

			//if not, create a sub execution
			if (subCallback == null) {
				final ValueHolder<Progress> newProgress = new ValueHolder<Progress>(new Progress());
				subCallback = new Tuple<ValueHolder<Progress>, IExecutionCallback>(
					newProgress,
					executionCallback.createSubExecution(subProgress.getStepProportion()));
				subCallbacks.put(subProgress.getTaskId(), subCallback);
			}

			//set the progress on the execution callback and set the last progress to the current progress
			setProgress(subProgress, subCallback.getFirst().get(), subCallback.getSecond());
			subCallback.getFirst().set(subProgress);
		}
	}

	private void setProgress(final Progress progress, final Progress lastProgress, final IExecutionCallback executionCallback) {
		if (!NullCompatibleEquivalence.equals(progress.getDescription(), lastProgress.getDescription())) {
			executionCallback.setDescription(progress.getDescription());
		}
		if (!NullCompatibleEquivalence.equals(progress.getTotalStepCount(), lastProgress.getTotalStepCount())) {
			executionCallback.setTotalStepCount(progress.getTotalStepCount());
		}
		if (progress.getTotalWorked() != null) {
			final int lastWorked = lastProgress.getTotalWorked() != null ? lastProgress.getTotalWorked().intValue() : 0;
			final int worked = progress.getTotalWorked() != null ? progress.getTotalWorked().intValue() : 0;
			if (worked > lastWorked) {
				executionCallback.worked(worked - lastWorked);
			}
		}
		if (progress.isFinished() != lastProgress.isFinished()) {
			executionCallback.finshed();
		}

	}

}
