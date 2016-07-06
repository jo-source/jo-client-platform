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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jowidgets.cap.common.api.exception.ServiceCanceledException;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.invocation.service.common.api.ICancelListener;
import org.jowidgets.invocation.service.common.api.IInvocationCallback;

final class SyncInvocationCallback<RESULT_TYPE> implements IInvocationCallback<RESULT_TYPE> {

	static final long DEFAULT_TIMEOUT = 100000000;

	private final IExecutionCallback executionCallback;

	private final long timeout;
	private final CountDownLatch latch;
	private final AtomicBoolean canceled;

	private RESULT_TYPE result;
	private Throwable exception;

	SyncInvocationCallback() {
		this(null, DEFAULT_TIMEOUT);
	}

	SyncInvocationCallback(final IExecutionCallback executionCallback) {
		this(executionCallback, DEFAULT_TIMEOUT);
	}

	SyncInvocationCallback(final long timeout) {
		this(null, timeout);
	}

	SyncInvocationCallback(final IExecutionCallback executionCallback, final long timeout) {
		this.timeout = timeout;
		this.latch = new CountDownLatch(1);
		this.canceled = new AtomicBoolean(false);
		this.executionCallback = executionCallback;
		if (executionCallback != null) {
			executionCallback.addExecutionCallbackListener(new IExecutionCallbackListener() {
				@Override
				public void canceled() {
					canceled.set(true);
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
				public void canceled() {
					cancelListener.canceled();
				}
			});
		}
	}

	@Override
	public void finished(final RESULT_TYPE result) {
		this.result = result;
		latch.countDown();
	}

	@Override
	public void exeption(final Throwable exception) {
		this.exception = exception;
		latch.countDown();
	}

	RESULT_TYPE getResultSynchronious() {

		final boolean hasTimeout;
		try {
			hasTimeout = !latch.await(timeout, TimeUnit.MILLISECONDS);
		}
		catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}

		if (canceled.get()) {
			throw new ServiceCanceledException();
		}
		else if (hasTimeout) {
			throw new RemotingTimeoutException("Timeout '" + timeout + " ms' while waiting on result");
		}
		else if (exception != null) {
			throw new RuntimeException(exception);
		}
		return result;
	}
}
