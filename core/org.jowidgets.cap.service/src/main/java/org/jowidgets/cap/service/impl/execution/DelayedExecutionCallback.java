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

package org.jowidgets.cap.service.impl.execution;

import java.util.HashSet;
import java.util.Set;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IUserQuestionCallback;
import org.jowidgets.cap.common.api.execution.UserQuestionResult;
import org.jowidgets.util.Assert;

public class DelayedExecutionCallback implements IExecutionCallback, IExecutionCallbackListener {

	private static final long DEFAULT_DELAY = 200;

	private final IExecutionCallback executionCallback;
	private final long delay;
	private final Set<IExecutionCallbackListener> executionCallbackListeners;
	private final Thread updaterThread;

	private boolean canceled;
	private boolean finished;
	private boolean disposed;

	private Integer lastTotalStepCount;
	private Integer totalStepCount;

	private Integer lastWorked;
	private Integer worked;

	private String lastDescription;
	private String description;

	public DelayedExecutionCallback(final IExecutionCallback executionCallback) {
		this(executionCallback, null);
	}

	public DelayedExecutionCallback(final IExecutionCallback executionCallback, final Long delay) {
		Assert.paramNotNull(executionCallback, "executionCallback");
		this.executionCallback = executionCallback;
		this.executionCallbackListeners = new HashSet<IExecutionCallbackListener>();
		this.canceled = executionCallback.isCanceled();

		if (delay != null) {
			this.delay = delay.longValue();
		}
		else {
			this.delay = DEFAULT_DELAY;
		}

		this.updaterThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true && !finished && !canceled && !disposed) {

					try {
						Thread.sleep(DelayedExecutionCallback.this.delay);
					}
					catch (final InterruptedException e) {
						break;
					}

					if (description != null && !description.equals(lastDescription)) {
						executionCallback.setDescription(description);
						lastDescription = description;
					}
					if (totalStepCount != null && !totalStepCount.equals(lastTotalStepCount)) {
						executionCallback.setTotalStepCount(totalStepCount.intValue());
						lastTotalStepCount = totalStepCount;
					}
					if (worked != null && !worked.equals(lastWorked)) {
						final int last = lastWorked != null ? lastWorked.intValue() : 0;
						executionCallback.worked(worked.intValue() - last);
						lastWorked = worked;
					}

				}
			}
		});

		this.updaterThread.setDaemon(true);
		this.updaterThread.start();

		this.executionCallback.addExecutionCallbackListener(this);
	}

	@Override
	public void executionCanceled() {
		canceled = true;
		fireCanceled();
		updaterThread.interrupt();
	}

	@Override
	public void onDispose() {
		disposed = true;
		fireOnDispose();
		executionCallback.removeExecutionCallbackListener(this);
		updaterThread.interrupt();
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public void setTotalStepCount(final int stepCount) {
		this.totalStepCount = Integer.valueOf(stepCount);
	}

	@Override
	public void worked(final int stepCount) {
		if (worked == null) {
			this.worked = Integer.valueOf(stepCount);
		}
		else {
			this.worked = Integer.valueOf(stepCount + worked.intValue());
		}
	}

	@Override
	public void workedOne() {
		worked(1);
	}

	@Override
	public void setDescription(final String descritpion) {
		this.description = descritpion;
	}

	@Override
	public void finshed() {
		this.finished = true;
		updaterThread.interrupt();
		executionCallback.finshed();
	}

	@Override
	public UserQuestionResult userQuestion(final String question) {
		return executionCallback.userQuestion(question);
	}

	@Override
	public void userQuestion(final String question, final IUserQuestionCallback callback) {
		executionCallback.userQuestion(question, callback);
	}

	@Override
	public IExecutionCallback createSubExecution(final int stepProportion, final boolean cancelable) {
		final IExecutionCallback callback = executionCallback.createSubExecution(stepProportion, cancelable);
		return new DelayedExecutionCallback(callback, delay);
	}

	@Override
	public void addExecutionCallbackListener(final IExecutionCallbackListener listener) {
		executionCallbackListeners.add(listener);
	}

	@Override
	public void removeExecutionCallbackListener(final IExecutionCallbackListener listener) {
		executionCallbackListeners.remove(listener);
	}

	public void fireCanceled() {
		for (final IExecutionCallbackListener listener : executionCallbackListeners) {
			listener.executionCanceled();
		}
	}

	public void fireOnDispose() {
		for (final IExecutionCallbackListener listener : executionCallbackListeners) {
			listener.onDispose();
		}
	}

}
