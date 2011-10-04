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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.exception.ServiceCanceledException;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.lookup.ILookUpEntry;
import org.jowidgets.cap.common.api.service.ILookUpService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.lookup.ILookUp;
import org.jowidgets.cap.ui.api.lookup.ILookUpAccess;
import org.jowidgets.cap.ui.api.lookup.ILookUpCallback;
import org.jowidgets.cap.ui.api.lookup.ILookUpListener;
import org.jowidgets.cap.ui.api.lookup.LookUpServiceProvider;
import org.jowidgets.util.Assert;

final class LookUpAccessImpl implements ILookUpAccess {

	private final Object lookUpId;
	private final Set<ILookUpCallback> callbacks;
	private final Set<ILookUpListener> listeners;
	private final IUiThreadAccess uiThreadAccess;
	private final IResultCallback<List<ILookUpEntry>> lookUpCallback;

	private ILookUpService lookUpService;
	private ILookUp lookUp;
	private IExecutionTask executionTask;

	LookUpAccessImpl(final Object lookUpId) {
		Assert.paramNotNull(lookUpId, "lookUpId");
		this.lookUpId = lookUpId;
		this.callbacks = new HashSet<ILookUpCallback>();
		this.listeners = new HashSet<ILookUpListener>();
		this.lookUpCallback = new LookUpReaderCallback();
		this.uiThreadAccess = Toolkit.getUiThreadAccess();

		checkThread();
	}

	@Override
	public void addCallback(final ILookUpCallback callback) {
		Assert.paramNotNull(callback, "callback");
		checkThread();
		callbacks.add(callback);
		if (lookUp != null) {
			callback.beforeChange();
			callback.onChange(lookUp);
		}
		else {
			readLookUp();
		}
	}

	@Override
	public void removeCallback(final ILookUpCallback callback) {
		Assert.paramNotNull(callback, "callback");
		checkThread();
		callbacks.remove(callback);
	}

	@Override
	public void initialize() {
		checkThread();
		if (!isInitialized()) {
			readLookUp();
		}
	}

	@Override
	public boolean isInitialized() {
		return lookUp != null;
	}

	@Override
	public ILookUp getCurrentLookUp() {
		checkThread();
		return lookUp;
	}

	@Override
	public void addLookUpListener(final ILookUpListener listener) {
		Assert.paramNotNull(listener, "listener");
		checkThread();
		listeners.add(listener);
	}

	@Override
	public void removeLookUpListener(final ILookUpListener listener) {
		Assert.paramNotNull(listener, "listener");
		checkThread();
		listeners.remove(listener);
	}

	public void clearCache() {
		checkThread();
		if (executionTask != null) {
			executionTask.cancel();
			executionTask = null;
		}
		readLookUp();
	}

	private void readLookUp() {
		if (executionTask == null) {
			for (final ILookUpCallback callback : callbacks) {
				callback.beforeChange();
			}
			executionTask = CapUiToolkit.executionTaskFactory().create();
			fireTaskCreated(executionTask);
			final ILookUpService lazyLookUpService = getLookUpServiceLazy();
			lazyLookUpService.readValues(lookUpCallback, executionTask);
		}
	}

	private void fireTaskCreated(final IExecutionTask task) {
		for (final ILookUpListener listener : listeners) {
			listener.taskCreated(task);
		}
	}

	private void fireAfterLookUpChanged() {
		for (final ILookUpListener listener : listeners) {
			listener.afterLookUpChanged();
		}
	}

	private ILookUpService getLookUpServiceLazy() {
		if (lookUpService == null) {
			lookUpService = LookUpServiceProvider.getLookUpService(lookUpId);
		}
		return lookUpService;
	}

	private void checkThread() {
		if (!uiThreadAccess.isUiThread()) {
			throw new IllegalStateException("The accessing thread must be the ui thread");
		}
	}

	private class LookUpReaderCallback implements IResultCallback<List<ILookUpEntry>> {

		@Override
		public void finished(final List<ILookUpEntry> result) {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					lookUp = new LookUpImpl(result);
					for (final ILookUpCallback callback : callbacks) {
						callback.onChange(lookUp);
					}
					executionTask = null;
					fireAfterLookUpChanged();
				}
			});
		}

		@Override
		public void exception(final Throwable exception) {
			if (!(exception instanceof ServiceCanceledException)) {
				uiThreadAccess.invokeLater(new Runnable() {
					@Override
					public void run() {
						for (final ILookUpCallback callback : callbacks) {
							callback.onException(exception);
						}
						executionTask = null;
					}
				});
			}
		}

		@Override
		public void timeout() {
			exception(new TimeoutException("Timout while loading lookup!"));
		}
	}

}
