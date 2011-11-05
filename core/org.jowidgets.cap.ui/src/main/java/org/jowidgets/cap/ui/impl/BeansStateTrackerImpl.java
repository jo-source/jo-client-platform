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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jowidgets.cap.ui.api.bean.IBeanModificationStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProcessStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeansStateTracker;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.model.IModificationStateListener;
import org.jowidgets.cap.ui.api.model.IProcessStateListener;
import org.jowidgets.cap.ui.tools.model.ModificationStateObservable;
import org.jowidgets.cap.ui.tools.model.ProcessStateObservable;
import org.jowidgets.util.Assert;

final class BeansStateTrackerImpl<BEAN_TYPE> implements IBeansStateTracker<BEAN_TYPE> {

	private final Set<IBeanProxy<BEAN_TYPE>> modifiedBeans;
	private final Set<IBeanProxy<BEAN_TYPE>> processingBeans;

	private final ModificationStateObservable modificationStateObservable;
	private final ProcessStateObservable processStateObservable;

	private final IBeanModificationStateListener<BEAN_TYPE> modificationStateListener;
	private final IBeanProcessStateListener<BEAN_TYPE> processStateListener;

	BeansStateTrackerImpl() {
		this.modifiedBeans = new HashSet<IBeanProxy<BEAN_TYPE>>();
		this.processingBeans = new HashSet<IBeanProxy<BEAN_TYPE>>();

		this.modificationStateObservable = new ModificationStateObservable();
		this.processStateObservable = new ProcessStateObservable();

		this.modificationStateListener = new IBeanModificationStateListener<BEAN_TYPE>() {
			@Override
			public void modificationStateChanged(final IBeanProxy<BEAN_TYPE> bean) {
				if (bean.hasModifications()) {
					addModifiedBean(bean);
				}
				else {
					removeUnmodifiedBean(bean);
				}
			}
		};

		this.processStateListener = new IBeanProcessStateListener<BEAN_TYPE>() {
			@Override
			public void processStateChanged(final IBeanProxy<BEAN_TYPE> bean) {
				if (bean.hasExecution()) {
					addProcessingBean(bean);
				}
				else {
					removeUnprocessingBean(bean);
				}
			}
		};
	}

	@Override
	public void dispose() {
		modificationStateObservable.dispose();
		processStateObservable.dispose();
	}

	@Override
	public void register(final IBeanProxy<BEAN_TYPE> bean) {
		Assert.paramNotNull(bean, "bean");
		bean.addModificationStateListener(modificationStateListener);
		if (bean.hasModifications()) {
			addModifiedBean(bean);
		}

		bean.addProcessStateListener(processStateListener);
		if (bean.hasExecution()) {
			addProcessingBean(bean);
		}
	}

	@Override
	public void unregister(final IBeanProxy<BEAN_TYPE> bean) {
		Assert.paramNotNull(bean, "bean");
		bean.removeModificationStateListener(modificationStateListener);
		removeUnmodifiedBean(bean);
		bean.removeProcessStateListener(processStateListener);
		removeUnprocessingBean(bean);
	}

	@Override
	public void undoModifications() {
		for (final IBeanProxy<BEAN_TYPE> bean : modifiedBeans) {
			bean.undoModifications();
		}
	}

	@Override
	public Set<IBeanProxy<BEAN_TYPE>> getModifiedBeans() {
		return Collections.unmodifiableSet(modifiedBeans);
	}

	@Override
	public boolean hasModifiedBeans() {
		return !modifiedBeans.isEmpty();
	}

	@Override
	public Set<IBeanProxy<BEAN_TYPE>> getExecutingBeans() {
		return Collections.unmodifiableSet(processingBeans);
	}

	@Override
	public boolean hasExecutingBeans() {
		return !processingBeans.isEmpty();
	}

	@Override
	public void cancelExecutions() {
		final Set<IExecutionTask> tasks = new HashSet<IExecutionTask>();
		for (final IBeanProxy<BEAN_TYPE> bean : new HashSet<IBeanProxy<BEAN_TYPE>>(processingBeans)) {
			tasks.add(bean.getExecutionTask());
			bean.setExecutionTask(null);
		}
		processingBeans.clear();

		for (final IExecutionTask task : tasks) {
			if (task != null && !task.isCanceled() && !task.isFinshed()) {
				final Runnable runnable = new Runnable() {
					@Override
					public void run() {
						task.cancel();
					}
				};
				final Thread thread = new Thread(runnable);
				thread.setDaemon(true);
				thread.start();
			}
		}

	}

	@Override
	public void clearModifications() {
		modifiedBeans.clear();
	}

	@Override
	public void clearAll() {
		clearModifications();
		processingBeans.clear();
	}

	@Override
	public void addModificationStateListener(final IModificationStateListener listener) {
		modificationStateObservable.addModificationStateListener(listener);
	}

	@Override
	public void removeModificationStateListener(final IModificationStateListener listener) {
		modificationStateObservable.removeModificationStateListener(listener);
	}

	@Override
	public void addProcessStateListener(final IProcessStateListener listener) {
		processStateObservable.addProcessStateListener(listener);
	}

	@Override
	public void removeProcessStateListener(final IProcessStateListener listener) {
		processStateObservable.removeProcessStateListener(listener);
	}

	private void addModifiedBean(final IBeanProxy<BEAN_TYPE> bean) {
		final boolean lastHasModifications = hasModifiedBeans();
		modifiedBeans.add(bean);
		if (lastHasModifications != hasModifiedBeans()) {
			modificationStateObservable.fireModificationStateChanged();
		}
	}

	private void removeUnmodifiedBean(final IBeanProxy<BEAN_TYPE> bean) {
		final boolean lastHasModifications = hasModifiedBeans();
		modifiedBeans.remove(bean);
		if (lastHasModifications != hasModifiedBeans()) {
			modificationStateObservable.fireModificationStateChanged();
		}
	}

	private void addProcessingBean(final IBeanProxy<BEAN_TYPE> bean) {
		final boolean lastHasProccesingBeans = hasExecutingBeans();
		processingBeans.add(bean);
		if (lastHasProccesingBeans != hasExecutingBeans()) {
			processStateObservable.fireProcessStateChanged();
		}
	}

	private void removeUnprocessingBean(final IBeanProxy<BEAN_TYPE> bean) {
		final boolean lastHasProccesingBeans = hasExecutingBeans();
		processingBeans.remove(bean);
		if (lastHasProccesingBeans != hasExecutingBeans()) {
			processStateObservable.fireProcessStateChanged();
		}
	}

}
