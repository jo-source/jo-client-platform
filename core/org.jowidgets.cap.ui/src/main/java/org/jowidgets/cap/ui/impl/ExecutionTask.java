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

package org.jowidgets.cap.ui.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IUserQuestionCallback;
import org.jowidgets.cap.common.api.execution.UserQuestionResult;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.execution.IExecutionTaskListener;
import org.jowidgets.cap.ui.api.execution.IUserAnswerCallback;
import org.jowidgets.util.Assert;

public final class ExecutionTask implements IExecutionTask, IUserAnswerCallback, Serializable {

	private static final long serialVersionUID = 5949541700454226560L;

	private final String id;
	private final List<ExecutionTask> subExecutions;
	private final Integer stepProportion;
	private final boolean cancelable;

	private final Set<IExecutionCallbackListener> executionCallbackListeners;
	private final Set<IExecutionTaskListener> executionTaskListeners;

	private boolean canceled;
	private Integer totalStepCount;
	private int worked;
	private String description;
	private boolean finished;
	private String userQuestion;
	private IUserQuestionCallback userQuestionCallback;
	private UserQuestionResult userQuestionResult;
	private Thread userQuestionWaitThread;

	ExecutionTask() {
		this(true, null);
	}

	ExecutionTask(final boolean cancelable, final Integer stepProportion) {
		this.id = UUID.randomUUID().toString();
		this.executionCallbackListeners = new HashSet<IExecutionCallbackListener>();
		this.executionTaskListeners = new HashSet<IExecutionTaskListener>();
		this.subExecutions = new LinkedList<ExecutionTask>();
		this.stepProportion = stepProportion;
		this.cancelable = cancelable;
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public void setTotalStepCount(final int stepCount) {
		this.totalStepCount = Integer.valueOf(stepCount);
		fireTotalStepCountChanged();
	}

	@Override
	public void worked(final int stepCount) {
		this.worked = worked + stepCount;
		fireWorked();
	}

	@Override
	public void workedOne() {
		worked(1);
	}

	@Override
	public void setDescription(final String descritpion) {
		this.description = descritpion;
		fireDescriptionChanged();
	}

	@Override
	public void finshed() {
		this.finished = true;
		fireFinished();
	}

	@Override
	public UserQuestionResult userQuestion(final String question) {
		Assert.paramNotNull(question, "question");
		this.userQuestion = question;
		userQuestionWaitThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(10000);
					}
					catch (final InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
		});

		userQuestionWaitThread.setDaemon(true);
		userQuestionWaitThread.start();
		fireUserQuestionAsked();
		try {
			userQuestionWaitThread.join();
		}
		catch (final Exception e) {
		}

		final UserQuestionResult result = userQuestionResult;
		userQuestionResult = null;

		return result;
	}

	@Override
	public void userQuestion(final String userQuestion, final IUserQuestionCallback userQuestionCallback) {
		Assert.paramNotNull(userQuestion, "userQuestion");
		Assert.paramNotNull(userQuestionCallback, "userQuestionCallback");

		this.userQuestion = userQuestion;
		this.userQuestionCallback = userQuestionCallback;

		fireUserQuestionAsked();
	}

	@Override
	public void setQuestionResult(final UserQuestionResult result) {
		this.userQuestionResult = result;
		this.userQuestion = null;

		if (userQuestionCallback != null) {
			userQuestionCallback.questionAnswered(result);
			//TODO MG is this necessary?
			userQuestionCallback = null;
		}
		else if (userQuestionWaitThread != null) {
			userQuestionWaitThread.interrupt();
			//TODO MG is this necessary?
			userQuestionWaitThread = null;
		}
		else {
			throw new IllegalStateException("No user question asked before");
		}
	}

	@Override
	public IExecutionCallback createSubExecution(final int stepProportion) {
		final ExecutionTask result = new ExecutionTask(false, Integer.valueOf(stepProportion));
		subExecutions.add(result);
		fireSubExecutionAdded(result);
		return result;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Integer getStepProportion() {
		return stepProportion;
	}

	@Override
	public boolean isCancelable() {
		return cancelable;
	}

	@Override
	public void cancel() {
		this.canceled = true;
		fireCanceled();
		for (final IExecutionTask subExecution : new LinkedList<IExecutionTask>(subExecutions)) {
			subExecution.cancel();
		}
	}

	@Override
	public Integer getTotalStepCount() {
		return totalStepCount;
	}

	@Override
	public int getWorked() {
		return worked;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isFinshed() {
		return finished;
	}

	@Override
	public List<IExecutionTask> getSubExecutions() {
		return new LinkedList<IExecutionTask>(subExecutions);
	}

	@Override
	public void addExecutionCallbackListener(final IExecutionCallbackListener listener) {
		executionCallbackListeners.add(listener);
	}

	@Override
	public void removeExecutionCallbackListener(final IExecutionCallbackListener listener) {
		executionCallbackListeners.remove(listener);
	}

	@Override
	public void addExecutionTaskListener(final IExecutionTaskListener listener) {
		executionTaskListeners.add(listener);
	}

	@Override
	public void removeExecutionTaskListener(final IExecutionTaskListener listener) {
		executionTaskListeners.remove(listener);
	}

	private void fireCanceled() {
		for (final IExecutionCallbackListener listener : executionCallbackListeners) {
			listener.canceled();
		}
	}

	void fireDescriptionChanged() {
		for (final IExecutionTaskListener listener : executionTaskListeners) {
			listener.descriptionChanged(getDescription());
		}
	}

	void fireTotalStepCountChanged() {
		for (final IExecutionTaskListener listener : executionTaskListeners) {
			listener.totalStepCountChanged(getTotalStepCount());
		}
	}

	void fireWorked() {
		for (final IExecutionTaskListener listener : executionTaskListeners) {
			listener.worked(getWorked());
		}
	}

	private void fireFinished() {
		for (final IExecutionTaskListener listener : executionTaskListeners) {
			listener.finished();
		}
	}

	private void fireUserQuestionAsked() {
		for (final IExecutionTaskListener listener : executionTaskListeners) {
			listener.userQuestionAsked(userQuestion, this);
		}
	}

	private void fireSubExecutionAdded(final IExecutionTask executionTask) {
		for (final IExecutionTaskListener listener : executionTaskListeners) {
			listener.subExecutionAdded(executionTask);
		}
	}

}
