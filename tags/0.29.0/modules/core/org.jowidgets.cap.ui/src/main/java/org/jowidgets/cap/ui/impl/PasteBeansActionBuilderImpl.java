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
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.controller.IDisposeObservable;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.command.IPasteBeansActionBuilder;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.common.types.VirtualKey;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.util.Assert;

final class PasteBeansActionBuilderImpl<BEAN_TYPE> extends AbstractCapActionBuilderImpl<IPasteBeansActionBuilder<BEAN_TYPE>> implements
		IPasteBeansActionBuilder<BEAN_TYPE> {

	private final Object beanTypeId;
	private final Class<? extends BEAN_TYPE> beanType;
	private final IBeanListModel<BEAN_TYPE> model;
	private final IDisposeObservable disposeObservable;
	private final List<IEnabledChecker> enabledCheckers;
	private boolean anySelection;

	private List<IAttribute<?>> attributes;

	PasteBeansActionBuilderImpl(
		final Object beanTypeId,
		final Class<? extends BEAN_TYPE> beanType,
		final IBeanListModel<BEAN_TYPE> model,
		final IDisposeObservable disposeObservable) {

		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(model, "model");
		Assert.paramNotNull(disposeObservable, "disposeObservable");

		this.beanTypeId = beanTypeId;
		this.beanType = beanType;
		this.model = model;
		this.disposeObservable = disposeObservable;
		this.enabledCheckers = new LinkedList<IEnabledChecker>();

		this.anySelection = true;

		setText(Messages.getString("PasteBeansActionBuilder.paste_data_sets"));
		setToolTipText(Messages.getString("PasteBeansActionBuilder.paste_data_sets_tooltip"));
		setAccelerator(VirtualKey.V, Modifier.CTRL);
		setIcon(IconsSmall.PASTE);

	}

	@Override
	public IPasteBeansActionBuilder<BEAN_TYPE> setEntityLabelPlural(final String label) {
		checkExhausted();
		Assert.paramNotEmpty(label, "label");
		final String message = Messages.getString("PasteBeansActionBuilder.paste_data_sets_with_var");
		setText(MessageReplacer.replace(message, label));
		return this;
	}

	@Override
	public IPasteBeansActionBuilder<BEAN_TYPE> setAnySelection(final boolean anySelection) {
		checkExhausted();
		this.anySelection = anySelection;
		return this;
	}

	@Override
	public IPasteBeansActionBuilder<BEAN_TYPE> setAttributes(final Collection<? extends IAttribute<?>> attributes) {
		checkExhausted();
		Assert.paramNotNull(attributes, "attributes");
		this.attributes = new LinkedList<IAttribute<?>>(attributes);
		return this;
	}

	@Override
	public IPasteBeansActionBuilder<BEAN_TYPE> addEnabledChecker(final IEnabledChecker enabledChecker) {
		checkExhausted();
		Assert.paramNotNull(enabledChecker, "enabledChecker");
		enabledCheckers.add(enabledChecker);
		return this;
	}

	@Override
	public IAction doBuild() {
		final BeanPasteCommand<BEAN_TYPE> command = new BeanPasteCommand<BEAN_TYPE>(
			beanTypeId,
			beanType,
			model,
			disposeObservable,
			attributes,
			enabledCheckers,
			anySelection);

		final IActionBuilder builder = getBuilder();
		builder.setCommand((ICommand) command);
		return builder.build();
	}

}
