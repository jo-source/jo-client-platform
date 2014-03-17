/*
 * Copyright (c) 2014, grossmann
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

import java.util.Collection;

import org.jowidgets.api.command.EnabledState;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IEnabledState;
import org.jowidgets.cap.common.api.execution.IExecutableState;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.link.ILinkCreation;
import org.jowidgets.cap.common.api.service.ILinkCreatorService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionEvent;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.tools.execution.AbstractUiResultCallback;
import org.jowidgets.cap.ui.tools.model.BeanListModelListenerAdapter;
import org.jowidgets.tools.command.AbstractEnabledChecker;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IProvider;
import org.jowidgets.util.event.IChangeListener;

final class PasteBeanLinkEnabledChecker<SOURCE_BEAN_TYPE, LINKABLE_BEAN_TYPE> extends AbstractEnabledChecker implements
		IEnabledChecker {

	private final ILinkCreatorService linkCreatorService;
	private final IProvider<Collection<ILinkCreation>> linkProvider;
	private final IBeanSelectionProvider<SOURCE_BEAN_TYPE> source;
	private final IBeanListModel<LINKABLE_BEAN_TYPE> linkedModel;
	private final boolean serviceBasedEnabledChecking;
	private final ClipboardSelectionEnabledChecker clipboardEnabledChecker;
	private final IBeanSelectionListener<SOURCE_BEAN_TYPE> sourceListener;
	private final BeanListModelListenerAdapter<LINKABLE_BEAN_TYPE> linkedModelListener;

	private IExecutionTask executionTask;
	private IEnabledState enabledState;
	private boolean conditionsChanged;
	private boolean disposed;

	PasteBeanLinkEnabledChecker(
		final ILinkCreatorService linkCreatorService,
		final IProvider<Collection<ILinkCreation>> linkProvider,
		final IBeanSelectionProvider<SOURCE_BEAN_TYPE> source,
		final IBeanListModel<LINKABLE_BEAN_TYPE> linkedModel,
		final Object expectedBeanTypeId,
		final Class<?> expectedBeanType,
		final boolean serviceBasedEnabledChecking) {

		Assert.paramNotNull(linkCreatorService, "linkCreatorService");
		Assert.paramNotNull(linkProvider, "linkProvider");
		Assert.paramNotNull(source, "source");
		Assert.paramNotNull(expectedBeanTypeId, "expectedBeanTypeId");
		Assert.paramNotNull(expectedBeanType, "expectedBeanType");

		this.linkCreatorService = linkCreatorService;
		this.linkProvider = linkProvider;
		this.source = source;
		this.linkedModel = linkedModel;
		this.serviceBasedEnabledChecking = serviceBasedEnabledChecking;
		this.disposed = false;
		this.conditionsChanged = true;

		this.sourceListener = new IBeanSelectionListener<SOURCE_BEAN_TYPE>() {
			@Override
			public void selectionChanged(final IBeanSelectionEvent<SOURCE_BEAN_TYPE> selectionEvent) {
				onConditionsChanged();
			}
		};
		source.addBeanSelectionListener(sourceListener);

		this.linkedModelListener = new BeanListModelListenerAdapter<LINKABLE_BEAN_TYPE>() {
			@Override
			public void beansChanged() {
				onConditionsChanged();
			}
		};
		if (linkedModel != null) {
			linkedModel.addBeanListModelListener(linkedModelListener);
		}

		this.clipboardEnabledChecker = new ClipboardSelectionEnabledChecker(expectedBeanTypeId, expectedBeanType);
		clipboardEnabledChecker.addChangeListener(new IChangeListener() {
			@Override
			public void changed() {
				onConditionsChanged();
			}
		});

	}

	@Override
	public IEnabledState getEnabledState() {
		if (enabledState == null) {
			if (conditionsChanged) {
				onStateConditionChanged();
			}
			return clipboardEnabledChecker.getEnabledState();
		}
		else {
			return enabledState;
		}
	}

	private void onConditionsChanged() {
		this.conditionsChanged = true;
		this.enabledState = null;
		fireEnabledStateChanged();
	}

	private void onStateConditionChanged() {
		enabledState = null;
		if (serviceBasedEnabledChecking && clipboardEnabledChecker.getEnabledState().isEnabled()) {
			//only do service based checkings, if clipbaord checker allows operation
			if (executionTask != null && !executionTask.isCanceled()) {
				executionTask.cancel();
			}

			final Collection<ILinkCreation> linkCreations = linkProvider.get();
			if (!linkCreations.isEmpty()) {
				this.executionTask = CapUiToolkit.executionTaskFactory().create();
				linkCreatorService.getExecutableState(createResultCallback(executionTask), linkCreations, executionTask);
			}

		}
		conditionsChanged = false;
	}

	private IResultCallback<IExecutableState> createResultCallback(final IExecutionTask executionTask) {
		return new AbstractUiResultCallback<IExecutableState>() {

			@Override
			protected void finishedUi(final IExecutableState result) {
				if (!disposed && !executionTask.isCanceled()) {
					if (!result.isExecutable()) {
						enabledState = EnabledState.disabled(result.getReason());
						fireEnabledStateChanged();
					}
				}
			}

			@Override
			protected void exceptionUi(final Throwable exception) {
				//Ignore errors for enabled checking
			}

		};
	}

	@Override
	public void dispose() {
		disposed = true;
		if (executionTask != null && !executionTask.isCanceled()) {
			executionTask.cancel();
		}
		clipboardEnabledChecker.dispose();
		source.removeBeanSelectionListener(sourceListener);
		if (linkedModel != null) {
			linkedModel.removeBeanListModelListener(linkedModelListener);
		}
		super.dispose();
	}
}
