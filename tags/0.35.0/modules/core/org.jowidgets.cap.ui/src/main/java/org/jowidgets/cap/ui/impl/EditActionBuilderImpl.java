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
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.command.IEditActionBuilder;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IDataModel;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.common.types.VirtualKey;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class EditActionBuilderImpl<BEAN_TYPE> extends AbstractCapActionBuilderImpl<IEditActionBuilder<BEAN_TYPE>> implements
		IEditActionBuilder<BEAN_TYPE> {

	private final Object entityId;
	private final IBeanListModel<BEAN_TYPE> model;
	private final IDataModel dataModel;
	private final List<IEnabledChecker> enabledCheckers;

	private IBeanFormBluePrint<BEAN_TYPE> beanFormBp;

	EditActionBuilderImpl(final IBeanListModel<BEAN_TYPE> model, final IDataModel dataModel) {
		this(null, model, dataModel);
	}

	EditActionBuilderImpl(final Object entityId, final IBeanListModel<BEAN_TYPE> model, final IDataModel dataModel) {

		Assert.paramNotNull(model, "model");

		this.entityId = entityId;
		this.model = model;
		this.dataModel = dataModel;
		this.enabledCheckers = new LinkedList<IEnabledChecker>();

		setText(Messages.getString("BeanEditActionBuilderImpl.edit_text"));
		setToolTipText(Messages.getString("BeanEditActionBuilderImpl.edit_tooltip"));
		setAccelerator(VirtualKey.E, Modifier.CTRL);
		setIcon(IconsSmall.EDIT);

		if (entityId != null) {
			final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
			if (entityService != null) {
				final IBeanDtoDescriptor descriptor = entityService.getDescriptor(entityId);
				if (descriptor != null) {
					final IMessage label = descriptor.getLabelSingular();
					if (label != null && !EmptyCheck.isEmpty(label.get())) {
						setEntityLabelSingular(label.get());
					}
				}
			}
		}
	}

	@Override
	public IEditActionBuilder<BEAN_TYPE> setEntityLabelSingular(final String label) {
		checkExhausted();
		Assert.paramNotEmpty(label, "label");
		final String message = Messages.getString("BeanEditActionBuilderImpl.edit_text_var");
		setText(MessageReplacer.replace(message, label));
		return this;
	}

	@Override
	public IEditActionBuilder<BEAN_TYPE> setBeanForm(final IBeanFormBluePrint<BEAN_TYPE> beanForm) {
		checkExhausted();
		Assert.paramNotNull(beanForm, "beanForm");
		this.beanFormBp = beanForm;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IEditActionBuilder<BEAN_TYPE> setBeanForm(final Collection<? extends IAttribute<?>> attributes) {
		checkExhausted();
		Assert.paramNotNull(attributes, "attributes");
		return setBeanForm((IBeanFormBluePrint<BEAN_TYPE>) CapUiToolkit.bluePrintFactory().beanForm(entityId, attributes));
	}

	@Override
	public IEditActionBuilder<BEAN_TYPE> addEnabledChecker(final IEnabledChecker enabledChecker) {
		checkExhausted();
		Assert.paramNotNull(enabledChecker, "enabledChecker");
		enabledCheckers.add(enabledChecker);
		return this;
	}

	@SuppressWarnings("unchecked")
	private IBeanFormBluePrint<BEAN_TYPE> getBeanFormBp() {
		if (beanFormBp == null) {
			if (entityId != null) {
				return (IBeanFormBluePrint<BEAN_TYPE>) CapUiToolkit.bluePrintFactory().beanForm(entityId);
			}
		}
		return beanFormBp;
	}

	@Override
	public IAction doBuild() {
		final BeanEditCommand<BEAN_TYPE> command = new BeanEditCommand<BEAN_TYPE>(
			model,
			dataModel,
			getBeanFormBp(),
			enabledCheckers);
		final IActionBuilder builder = getBuilder();
		builder.setCommand((ICommand) command);
		return builder.build();
	}

}
