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

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.invocation.service.common.api.ICancelListener;
import org.jowidgets.invocation.service.common.api.IInvocationCallback;

final class InvocationCallback<RESULT_TYPE> implements IInvocationCallback<RESULT_TYPE> {

	private final IResultCallback<RESULT_TYPE> resultCallback;
	private final IExecutionCallback executionCallback;

	InvocationCallback(final IResultCallback<RESULT_TYPE> resultCallback, final IExecutionCallback executionCallback) {
		this.resultCallback = resultCallback;
		this.executionCallback = executionCallback;
	}

	@Override
	public void addCancelListener(final ICancelListener cancelListener) {
		if (executionCallback != null) {
			executionCallback.addExecutionCallbackListener(new IExecutionCallbackListener() {
				@Override
				public void onDispose() {
					cancelListener.canceled();
				}

				@Override
				public void canceled() {
					cancelListener.canceled();
				}
			});
		}
	}

	@Override
	public void finished(final RESULT_TYPE result) {
		if (resultCallback != null) {
			resultCallback.finished(result);
		}
		if (executionCallback != null) {
			executionCallback.finshed();
		}
	}

	@Override
	public void exeption(final Throwable exception) {
		if (resultCallback != null) {
			resultCallback.exception(exception);
		}
		if (executionCallback != null) {
			executionCallback.finshed();
		}
	}

	@Override
	public void timeout() {
		if (resultCallback != null) {
			resultCallback.timeout();
		}
		if (executionCallback != null) {
			executionCallback.finshed();
		}
	}
}
