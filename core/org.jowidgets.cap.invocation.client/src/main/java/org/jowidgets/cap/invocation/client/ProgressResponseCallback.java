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

package org.jowidgets.cap.invocation.client;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.invocation.common.Progress;
import org.jowidgets.invocation.service.common.api.IInterimResponseCallback;
import org.jowidgets.util.Assert;
import org.jowidgets.util.NullCompatibleEquivalence;

final class ProgressResponseCallback implements IInterimResponseCallback<Progress> {

	private final IExecutionCallback executionCallback;

	private Progress lastProgress;

	ProgressResponseCallback(final IExecutionCallback executionCallback) {
		Assert.paramNotNull(executionCallback, "executionCallback");
		this.executionCallback = executionCallback;

		this.lastProgress = new Progress();
	}

	@Override
	public void response(final Progress progress) {

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

		this.lastProgress = progress;

	}
}
