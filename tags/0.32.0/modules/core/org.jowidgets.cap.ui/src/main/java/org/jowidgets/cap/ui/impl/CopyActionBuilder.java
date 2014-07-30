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

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.ui.api.clipboard.BeanSelectionTransferableFactory;
import org.jowidgets.cap.ui.api.clipboard.IBeanSelectionTransferableFactory;
import org.jowidgets.cap.ui.api.command.ICopyActionBuilder;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.common.types.VirtualKey;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class CopyActionBuilder<BEAN_TYPE> extends AbstractCapActionBuilderImpl<ICopyActionBuilder<BEAN_TYPE>> implements
		ICopyActionBuilder<BEAN_TYPE> {

	private final IBeanListModel<BEAN_TYPE> model;
	private final List<IEnabledChecker> enabledCheckers;
	private final List<IExecutableChecker<BEAN_TYPE>> executableCheckers;

	private final BeanModificationStatePolicy beanModificationStatePolicy;

	private String entityLabelSingular;
	private String entityLabelPlural;
	private IBeanSelectionTransferableFactory<BEAN_TYPE> transferableFactory;
	private boolean multiSelection;

	private BeanMessageStatePolicy beanMessageStatePolicy;

	CopyActionBuilder(final IBeanListModel<BEAN_TYPE> model) {
		checkExhausted();
		Assert.paramNotNull(model, "model");
		this.model = model;
		this.enabledCheckers = new LinkedList<IEnabledChecker>();
		this.executableCheckers = new LinkedList<IExecutableChecker<BEAN_TYPE>>();

		this.transferableFactory = BeanSelectionTransferableFactory.create();
		this.multiSelection = true;
		this.beanModificationStatePolicy = BeanModificationStatePolicy.ANY_MODIFICATION;
		this.beanMessageStatePolicy = BeanMessageStatePolicy.NO_ERROR;

		setAccelerator(VirtualKey.C, Modifier.CTRL);
		setIcon(IconsSmall.COPY);
	}

	@Override
	public ICopyActionBuilder<BEAN_TYPE> setEntityLabelSingular(final String label) {
		checkExhausted();
		this.entityLabelSingular = label;
		return this;
	}

	@Override
	public ICopyActionBuilder<BEAN_TYPE> setEntityLabelPlural(final String label) {
		checkExhausted();
		this.entityLabelPlural = label;
		return this;
	}

	@Override
	public ICopyActionBuilder<BEAN_TYPE> setTransferableFactory(
		final IBeanSelectionTransferableFactory<BEAN_TYPE> transferableFactory) {
		checkExhausted();
		Assert.paramNotNull(transferableFactory, "transferableFactory");
		this.transferableFactory = transferableFactory;
		return this;
	}

	@Override
	public ICopyActionBuilder<BEAN_TYPE> addEnabledChecker(final IEnabledChecker enabledChecker) {
		checkExhausted();
		Assert.paramNotNull(enabledChecker, "enabledChecker");
		enabledCheckers.add(enabledChecker);
		return this;
	}

	@Override
	public ICopyActionBuilder<BEAN_TYPE> setMultiSelectionPolicy(final boolean multiSelection) {
		checkExhausted();
		this.multiSelection = multiSelection;
		return this;
	}

	@Override
	public ICopyActionBuilder<BEAN_TYPE> setMessageStatePolicy(final BeanMessageStatePolicy policy) {
		checkExhausted();
		Assert.paramNotNull(policy, "policy");
		this.beanMessageStatePolicy = policy;
		return this;
	}

	@Override
	public ICopyActionBuilder<BEAN_TYPE> addExecutableChecker(final IExecutableChecker<BEAN_TYPE> executableChecker) {
		checkExhausted();
		Assert.paramNotNull(executableChecker, "executableChecker");
		executableCheckers.add(executableChecker);
		return this;
	}

	private void setDefaultTextIfNecessary() {
		if (EmptyCheck.isEmpty(getText())) {
			if (!multiSelection && !EmptyCheck.isEmpty(entityLabelSingular)) {
				final String message = Messages.getString("CopyActionBuilder.copy_single_var");
				setText(MessageReplacer.replace(message, entityLabelSingular));
			}
			else if (!EmptyCheck.isEmpty(entityLabelPlural) && multiSelection) {
				final String message = Messages.getString("CopyActionBuilder.copy_multi_var");
				setText(MessageReplacer.replace(message, entityLabelPlural));
			}
			else if (!multiSelection) {
				setText(Messages.getString("CopyActionBuilder.copy_single"));
			}
			else {
				setText(Messages.getString("CopyActionBuilder.copy_multi"));
			}
		}
	}

	private void setDefaultToolTipTextIfNecessary() {
		if (EmptyCheck.isEmpty(getToolTipText())) {
			if (!multiSelection) {
				setToolTipText(Messages.getString("CopyActionBuilder.copy_single_tooltip"));
			}
			else {
				setToolTipText(Messages.getString("CopyActionBuilder.copy_multi_tooltip"));
			}
		}
	}

	@Override
	public IAction doBuild() {
		setDefaultTextIfNecessary();
		setDefaultToolTipTextIfNecessary();

		final BeanCopyCommand<BEAN_TYPE> command = new BeanCopyCommand<BEAN_TYPE>(
			model,
			transferableFactory,
			enabledCheckers,
			executableCheckers,
			multiSelection,
			beanModificationStatePolicy,
			beanMessageStatePolicy);

		final IActionBuilder builder = getBuilder();
		builder.setCommand((ICommand) command);
		return builder.build();
	}

}
