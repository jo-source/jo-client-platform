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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.clipboard.Clipboard;
import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IEnabledState;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.controller.IDisposeListener;
import org.jowidgets.api.controller.IDisposeObservable;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.clipboard.IBeanSelectionClipboard;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.util.Assert;

final class BeanPasteCommand<BEAN_TYPE> implements ICommand, ICommandExecutor {

	private final IBeanListModel<BEAN_TYPE> model;
	private final BeanSelectionProviderEnabledChecker<BEAN_TYPE> enabledChecker;
	private final List<IAttribute<?>> attributes;

	BeanPasteCommand(
		final Object beanTypeId,
		final Class<? extends BEAN_TYPE> beanType,
		final IBeanListModel<BEAN_TYPE> model,
		final IDisposeObservable disposeObservable,
		final List<IAttribute<?>> attributes,
		final List<IEnabledChecker> enabledCheckers,
		final boolean anySelection) {

		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(model, "model");
		Assert.paramNotNull(disposeObservable, "disposeObservable");
		Assert.paramNotNull(attributes, "attributes");
		Assert.paramNotNull(enabledCheckers, "enabledCheckers");
		Assert.paramNotNull(anySelection, "anySelection");

		this.model = model;
		this.attributes = new LinkedList<IAttribute<?>>(attributes);

		final ClipboardSelectionEnabledChecker clipboardEnabledChecker = new ClipboardSelectionEnabledChecker(
			beanTypeId,
			beanType);

		final List<IEnabledChecker> checkers = new LinkedList<IEnabledChecker>(enabledCheckers);
		checkers.add(clipboardEnabledChecker);

		this.enabledChecker = new BeanSelectionProviderEnabledChecker<BEAN_TYPE>(
			model,
			anySelection ? BeanSelectionPolicy.ANY_SELECTION : BeanSelectionPolicy.NO_SELECTION,
			BeanModificationStatePolicy.ANY_MODIFICATION,
			null,
			checkers,
			null,
			true);

		final IDisposeListener disposeListener = new IDisposeListener() {
			@Override
			public void onDispose() {
				clipboardEnabledChecker.dispose();
			}
		};

		disposeObservable.addDisposeListener(disposeListener);
	}

	@Override
	public ICommandExecutor getCommandExecutor() {
		return this;
	}

	@Override
	public IEnabledChecker getEnabledChecker() {
		return enabledChecker;
	}

	@Override
	public IExceptionHandler getExceptionHandler() {
		return null;
	}

	@Override
	public void execute(final IExecutionContext executionContext) throws Exception {
		final IEnabledState enabledState = enabledChecker.getEnabledState();
		if (enabledState.isEnabled()) {
			doExecution();
		}
		else {
			Toolkit.getMessagePane().showInfo(executionContext, enabledState.getReason());
		}
	}

	private void doExecution() {
		final IBeanSelectionClipboard selectionClipboard = Clipboard.getData(IBeanSelectionClipboard.TRANSFER_TYPE);
		if (selectionClipboard != null) {
			for (final IBeanDto beanDto : selectionClipboard.getBeans()) {
				final IBeanProxy<BEAN_TYPE> transientBean = model.addTransientBean();
				for (final IAttribute<?> attribute : attributes) {
					final String propertyName = attribute.getPropertyName();
					if (!propertyName.equals(IBean.ID_PROPERTY)
						&& !propertyName.equals(IBean.VERSION_PROPERTY)
						&& !IBeanProxy.ALL_META_ATTRIBUTES.contains(propertyName)) {
						transientBean.setValue(propertyName, beanDto.getValue(propertyName));
					}
				}
				final IBeanProxy<BEAN_TYPE> unmodifiedCopy = transientBean.createUnmodifiedCopy();
				model.removeBeans(Collections.singleton(transientBean));
				model.addBean(unmodifiedCopy);
			}
		}
	}

}
