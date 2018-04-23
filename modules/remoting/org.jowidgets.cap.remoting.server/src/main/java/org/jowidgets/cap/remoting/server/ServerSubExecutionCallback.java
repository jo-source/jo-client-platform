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

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IUserQuestionCallback;
import org.jowidgets.cap.common.api.execution.UserQuestionResult;
import org.jowidgets.cap.remoting.common.Progress;
import org.jowidgets.util.Assert;

final class ServerSubExecutionCallback implements IExecutionCallback {

	private final Object id;

	private final ServerExecutionCallback rootCallback;
	private final ServerSubExecutionCallback parentCallback;
	private final List<ServerSubExecutionCallback> subCallbacks;
	private final int stepProportion;

	private final AtomicBoolean dirty;

	private Integer totalStepCount;
	private Integer totalWorked;
	private String description;
	private boolean finished;

	ServerSubExecutionCallback(
		final int stepProportion,
		final ServerExecutionCallback rootCallback,
		final ServerSubExecutionCallback parentCallback) {
		Assert.paramNotNull(rootCallback, "rootCallback");
		this.stepProportion = stepProportion;
		this.rootCallback = rootCallback;
		this.parentCallback = parentCallback;
		this.id = UUID.randomUUID();
		this.dirty = new AtomicBoolean();
		this.subCallbacks = new LinkedList<ServerSubExecutionCallback>();
		this.totalWorked = Integer.valueOf(0);
	}

	@Override
	public boolean isCanceled() {
		return rootCallback.isCanceled();
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
	public UserQuestionResult userQuestion(final String question) throws InterruptedException {
		return rootCallback.userQuestion(question);
	}

	@Override
	public void userQuestion(final String question, final IUserQuestionCallback callback) {
		rootCallback.userQuestion(question, callback);
	}

	@Override
	public IExecutionCallback createSubExecution(final int stepProportion) {
		final ServerSubExecutionCallback result = new ServerSubExecutionCallback(stepProportion, rootCallback, this);
		subCallbacks.add(result);
		return result;
	}

	@Override
	public void addExecutionCallbackListener(final IExecutionCallbackListener listener) {
		rootCallback.addExecutionCallbackListener(listener);
	}

	@Override
	public void removeExecutionCallbackListener(final IExecutionCallbackListener listener) {
		rootCallback.removeExecutionCallbackListener(listener);
	}

	/**
	 * Reads the progress and sets the dirty flag to false.
	 * 
	 * @return the current progress
	 */
	Progress readProgress() {
		if (dirty.compareAndSet(true, false)) {
			final List<Progress> subProgressList = new LinkedList<Progress>();
			for (final ServerSubExecutionCallback subCallback : subCallbacks) {
				final Progress subProgress = subCallback.readProgress();
				if (subProgress != null) {
					subProgressList.add(subProgress);
				}
			}
			return new Progress(id, stepProportion, totalStepCount, totalWorked, description, finished, subProgressList);
		}
		else {
			return null;
		}
	}

	private void setDirty() {
		dirty.set(true);
		if (parentCallback != null) {
			parentCallback.setDirty();
		}
		rootCallback.setDirty();
	}

}
