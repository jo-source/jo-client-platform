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

package org.jowidgets.cap.remoting.server;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IUserQuestionCallback;
import org.jowidgets.cap.common.api.execution.UserQuestionResult;
import org.jowidgets.cap.remoting.common.Progress;
import org.jowidgets.cap.remoting.common.UserQuestionRequest;
import org.jowidgets.invocation.service.common.api.ICancelListener;
import org.jowidgets.invocation.service.common.api.IInterimRequestCallback;
import org.jowidgets.invocation.service.common.api.IInterimResponseCallback;
import org.jowidgets.invocation.service.common.api.IInvocationCallback;
import org.jowidgets.util.Assert;
import org.jowidgets.util.ValueHolder;

final class ServerExecutionCallback implements IExecutionCallback {

	private final IInterimResponseCallback<Object> interimResponseCallback;
	private final IInterimRequestCallback<Object, Object> interimRequestCallback;
	private final Set<IExecutionCallbackListener> executionCallbackListeners;

	private final ScheduledExecutorService scheduledExecutorService;
	private final long progressDelay;
	private final AtomicBoolean isProgressScheduled;

	private final List<ServerSubExecutionCallback> subCallbacks;

	private boolean canceled;
	private Integer totalStepCount;
	private Integer totalWorked;
	private String description;
	private boolean finished;

	ServerExecutionCallback(
		final ScheduledExecutorService scheduledExecutorService,
		final long progressDelay,
		final IInvocationCallback<Object> invocationCallback,
		final IInterimResponseCallback<Object> interimResponseCallback,
		final IInterimRequestCallback<Object, Object> interimRequestCallback) {

		Assert.paramNotNull(scheduledExecutorService, "scheduledExecutorService");
		Assert.paramNotNull(invocationCallback, "invocationCallback");
		Assert.paramNotNull(interimResponseCallback, "interimResponseCallback");

		this.progressDelay = progressDelay;
		this.scheduledExecutorService = scheduledExecutorService;
		this.interimResponseCallback = interimResponseCallback;
		this.interimRequestCallback = interimRequestCallback;
		this.executionCallbackListeners = new HashSet<IExecutionCallbackListener>();
		this.subCallbacks = new LinkedList<ServerSubExecutionCallback>();

		this.canceled = false;
		this.totalWorked = Integer.valueOf(0);

		invocationCallback.addCancelListener(new ICancelListener() {
			@Override
			public void canceled() {
				canceled = true;
				for (final IExecutionCallbackListener listener : new LinkedList<IExecutionCallbackListener>(
					executionCallbackListeners)) {
					listener.canceled();
				}
			}
		});

		this.isProgressScheduled = new AtomicBoolean(false);
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public synchronized void setTotalStepCount(final int stepCount) {
		this.totalStepCount = stepCount;
		setDirty();
	}

	@Override
	public synchronized void worked(final int stepCount) {
		this.totalWorked = this.totalWorked + stepCount;
		setDirty();
	}

	@Override
	public synchronized void workedOne() {
		worked(1);
	}

	@Override
	public synchronized void setDescription(final String description) {
		this.description = description;
		setDirty();
	}

	@Override
	public synchronized void finshed() {
		this.finished = true;
		setDirty();
	}

	@Override
	public UserQuestionResult userQuestion(final String question) {
		final ValueHolder<UserQuestionResult> result = new ValueHolder<UserQuestionResult>();
		final Lock lock = new ReentrantLock();
		final Condition condition = lock.newCondition();

		interimRequestCallback.request(new IInterimResponseCallback<Object>() {
			@Override
			public void response(final Object response) {
				final UserQuestionResult userQuestionResult = (UserQuestionResult) response;
				result.set(userQuestionResult);
				lock.lock();
				condition.signal();
				lock.unlock();
			}

		}, new UserQuestionRequest(question));

		lock.lock();
		condition.awaitUninterruptibly();
		lock.unlock();

		return result.get();
	}

	@Override
	public void userQuestion(final String question, final IUserQuestionCallback callback) {
		interimRequestCallback.request(new IInterimResponseCallback<Object>() {
			@Override
			public void response(final Object response) {
				final UserQuestionResult userQuestionResult = (UserQuestionResult) response;
				callback.questionAnswered(userQuestionResult);
			}
		}, new UserQuestionRequest(question));
	}

	@Override
	public IExecutionCallback createSubExecution(final int stepProportion) {
		final ServerSubExecutionCallback result = new ServerSubExecutionCallback(stepProportion, this, null);
		subCallbacks.add(result);
		return result;
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

	void setDirty() {
		if (isProgressScheduled.compareAndSet(false, true)) {
			scheduledExecutorService.schedule(new Runnable() {
				@Override
				public void run() {
					isProgressScheduled.set(false);
					final List<Progress> subProgressList = new LinkedList<Progress>();
					for (final ServerSubExecutionCallback subCallback : subCallbacks) {
						final Progress subProgress = subCallback.readProgress();
						if (subProgress != null) {
							subProgressList.add(subProgress);
						}
					}
					final Progress progress = new Progress(
						null,
						null,
						totalStepCount,
						totalWorked,
						description,
						finished,
						subProgressList);
					if (!finished && !canceled) {
						interimResponseCallback.response(progress);
					}
				}
			}, progressDelay, TimeUnit.MILLISECONDS);
		}
	}
}
