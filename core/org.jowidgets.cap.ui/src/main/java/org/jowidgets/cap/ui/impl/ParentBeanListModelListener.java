/*
 * Copyright (c) 2012, grossmann
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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.model.IDataModel;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCompatibleEquivalence;
import org.jowidgets.util.IProvider;
import org.jowidgets.util.concurrent.DaemonThreadFactory;

final class ParentBeanListModelListener implements IBeanListModelListener {

	private static final int LISTENER_DELAY = 100;

	private final IDataModel dataModel;
	private final long listenerDelay;
	private final IProvider<Object> reloadConditionProvider;

	private ScheduledExecutorService executorService;
	private ScheduledFuture<?> schedule;
	private Object lastLoadCondition;

	ParentBeanListModelListener(final IDataModel dataModel, final IProvider<Object> reloadConditionProvider) {
		this(dataModel, reloadConditionProvider, LISTENER_DELAY);
	}

	ParentBeanListModelListener(
		final IDataModel dataModel,
		final IProvider<Object> reloadConditionProvider,
		final long listenerDelay) {
		Assert.paramNotNull(dataModel, "dataModel");

		this.dataModel = dataModel;
		this.reloadConditionProvider = reloadConditionProvider;
		this.listenerDelay = listenerDelay;
	}

	@Override
	public void selectionChanged() {
		if (!EmptyCompatibleEquivalence.equals(lastLoadCondition, reloadConditionProvider.get())) {
			loadScheduled();
		}
	}

	@Override
	public void beansChanged() {
		if (!EmptyCompatibleEquivalence.equals(lastLoadCondition, reloadConditionProvider.get())) {
			loadScheduled();
		}
	}

	private void loadScheduled() {
		dataModel.clear();
		if (schedule != null) {
			schedule.cancel(false);
		}
		final IUiThreadAccess uiThreadAccess = Toolkit.getUiThreadAccess();
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				uiThreadAccess.invokeLater(new Runnable() {
					@Override
					public void run() {
						schedule = null;
						lastLoadCondition = reloadConditionProvider.get();
						dataModel.load();
					}
				});
			}
		};

		schedule = getExecutorService().schedule(runnable, listenerDelay, TimeUnit.MILLISECONDS);
	}

	private ScheduledExecutorService getExecutorService() {
		if (executorService == null) {
			executorService = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
		}
		return executorService;
	}
}
