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
import org.jowidgets.cap.ui.api.bean.IBeanSelection;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionEvent;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.model.IDataModel;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCompatibleEquivalence;
import org.jowidgets.util.concurrent.DaemonThreadFactory;

final class ParentSelectionListener<BEAN_TYPE> implements IBeanSelectionListener<BEAN_TYPE> {

	private static final long LISTENER_DELAY = 200;

	private final IDataModel childModel;
	private final long listenerDelay;
	private final IBeanSelectionProvider<?> parent;

	private ScheduledExecutorService executorService;
	private ScheduledFuture<?> schedule;
	private IBeanSelection<?> lastSelection;

	ParentSelectionListener(final IBeanSelectionProvider<?> parent, final IDataModel childModel, final Long listenerDelay) {
		Assert.paramNotNull(parent, "parent");
		Assert.paramNotNull(childModel, "childModel");

		this.childModel = childModel;
		this.parent = parent;
		this.listenerDelay = listenerDelay != null ? listenerDelay.longValue() : LISTENER_DELAY;
	}

	@Override
	public void selectionChanged(final IBeanSelectionEvent<BEAN_TYPE> selectionEvent) {
		if (!EmptyCompatibleEquivalence.equals(lastSelection, parent.getBeanSelection())) {
			if (listenerDelay > 0) {
				loadScheduled();
			}
			else {
				load();
			}
		}
	}

	private void loadScheduled() {
		if (listenerDelay > 0) {
			childModel.clear();
		}
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
						load();
					}
				});
			}
		};

		schedule = getExecutorService().schedule(runnable, listenerDelay, TimeUnit.MILLISECONDS);
	}

	private void load() {
		lastSelection = parent.getBeanSelection();
		childModel.load();
	}

	private ScheduledExecutorService getExecutorService() {
		if (executorService == null) {
			executorService = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
		}
		return executorService;
	}
}
