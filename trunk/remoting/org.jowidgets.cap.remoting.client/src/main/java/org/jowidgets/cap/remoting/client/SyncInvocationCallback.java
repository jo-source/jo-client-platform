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

import org.jowidgets.cap.common.api.exception.ServiceCanceledException;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.invocation.service.common.api.ICancelListener;
import org.jowidgets.invocation.service.common.api.IInvocationCallback;

final class SyncInvocationCallback<RESULT_TYPE> implements IInvocationCallback<RESULT_TYPE> {

	private final IExecutionCallback executionCallback;

	private RESULT_TYPE result;
	private Throwable exception;
	private boolean timeout;

	private Thread currentThread;

	SyncInvocationCallback() {
		this(null);
	}

	SyncInvocationCallback(final IExecutionCallback executionCallback) {
		this.executionCallback = executionCallback;
		if (executionCallback != null) {
			executionCallback.addExecutionCallbackListener(new IExecutionCallbackListener() {
				@Override
				public void onDispose() {
					exeption(new ServiceCanceledException());
				}

				@Override
				public void executionCanceled() {
					exeption(new ServiceCanceledException());
				}
			});
		}
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
				public void executionCanceled() {
					cancelListener.canceled();
				}
			});
		}
	}

	@Override
	public void finished(final RESULT_TYPE result) {
		this.result = result;
		unblock();
	}

	@Override
	public void exeption(final Throwable exception) {
		this.exception = exception;
		unblock();
	}

	@Override
	public void timeout() {
		this.timeout = true;
		unblock();
	}

	private void unblock() {
		if (currentThread != null) {
			currentThread.interrupt();
		}
	}

	RESULT_TYPE getResultSynchronious() {
		this.currentThread = Thread.currentThread();
		try {
			Thread.sleep(100000000);
		}
		catch (final InterruptedException e) {

		}
		if (exception != null) {
			throw new RuntimeException(exception);
		}
		if (timeout) {
			throw new RuntimeException("Timeout while waiting on result");
		}
		return result;
	}
}
