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

package org.jowidgets.cap.service.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IUserQuestionCallback;
import org.jowidgets.cap.common.api.execution.UserQuestionResult;
import org.jowidgets.util.Assert;

final class DelayedExecutionCallback implements IExecutionCallback {

	private static final long DEFAULT_DELAY = 200;

	private final IExecutionCallback executionCallback;
	private final long delay;
	private final Set<IExecutionCallbackListener> executionCallbackListeners;
	private final IExecutionCallbackListener executionCallbackListener;
	private final ScheduledExecutorService scheduledExecutorService;
	private final AtomicBoolean isProgressScheduled;

	private boolean canceled;

	private Integer lastTotalStepCount;
	private Integer totalStepCount;

	private Integer lastWorked;
	private Integer worked;

	private String lastDescription;
	private String description;

	DelayedExecutionCallback(
		final IExecutionCallback executionCallback,
		final ScheduledExecutorService scheduledExecutorService) {
		this(executionCallback, scheduledExecutorService, null);
	}

	DelayedExecutionCallback(
		final IExecutionCallback executionCallback,
		final ScheduledExecutorService scheduledExecutorService,
		final Long delay) {
		Assert.paramNotNull(executionCallback, "executionCallback");
		Assert.paramNotNull(scheduledExecutorService, "scheduledExecutorService");
		this.executionCallback = executionCallback;
		this.executionCallbackListeners = new HashSet<IExecutionCallbackListener>();
		this.canceled = executionCallback.isCanceled();

		if (delay != null) {
			this.delay = delay.longValue();
		}
		else {
			this.delay = DEFAULT_DELAY;
		}

		this.scheduledExecutorService = scheduledExecutorService;
		this.isProgressScheduled = new AtomicBoolean(false);

		this.executionCallbackListener = new ExecutionCallbackListener();
		this.executionCallback.addExecutionCallbackListener(executionCallbackListener);
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public synchronized void setTotalStepCount(final int stepCount) {
		this.totalStepCount = Integer.valueOf(stepCount);
		setDirty();
	}

	@Override
	public synchronized void worked(final int stepCount) {
		if (worked == null) {
			this.worked = Integer.valueOf(stepCount);
		}
		else {
			this.worked = Integer.valueOf(stepCount + worked.intValue());
		}
		setDirty();
	}

	@Override
	public void workedOne() {
		worked(1);
	}

	@Override
	public synchronized void setDescription(final String descritpion) {
		this.description = descritpion;
		setDirty();
	}

	@Override
	public void finshed() {
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
	public IExecutionCallback createSubExecution(final int stepProportion) {
		final IExecutionCallback callback = executionCallback.createSubExecution(stepProportion);
		return new DelayedExecutionCallback(callback, scheduledExecutorService, delay);
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
			listener.canceled();
		}
	}

	void setDirty() {
		if (isProgressScheduled.compareAndSet(false, true)) {
			scheduledExecutorService.schedule(new Runnable() {
				@Override
				public void run() {
					isProgressScheduled.set(false);
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
			}, delay, TimeUnit.MILLISECONDS);
		}
	}

	public final class ExecutionCallbackListener implements IExecutionCallbackListener {
		@Override
		public void canceled() {
			canceled = true;
			fireCanceled();
		}
	}

}
