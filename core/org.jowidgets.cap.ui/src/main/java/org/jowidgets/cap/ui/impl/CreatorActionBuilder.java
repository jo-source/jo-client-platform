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
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanExecptionConverter;
import org.jowidgets.cap.ui.api.command.ICreatorActionBuilder;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.types.Accelerator;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.service.tools.ServiceId;
import org.jowidgets.util.Assert;
import org.jowidgets.util.builder.AbstractSingleUseBuilder;

final class CreatorActionBuilder<BEAN_TYPE> extends AbstractSingleUseBuilder<IAction> implements ICreatorActionBuilder {

	private final Class<? extends BEAN_TYPE> beanType;
	private final IBeanListModel<BEAN_TYPE> model;
	private final IActionBuilder builder;
	private final List<IEnabledChecker> enabledCheckers;
	private boolean anySelection;

	private ICreatorService creatorService;
	private IBeanFormBluePrint<BEAN_TYPE> beanFormBp;
	private IBeanExecptionConverter exceptionConverter;

	CreatorActionBuilder(final Class<? extends BEAN_TYPE> beanType, final IBeanListModel<BEAN_TYPE> model) {
		checkExhausted();
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(model, "model");
		this.beanType = beanType;
		this.model = model;
		this.builder = Toolkit.getActionBuilderFactory().create();
		this.enabledCheckers = new LinkedList<IEnabledChecker>();
		this.exceptionConverter = new DefaultBeanExceptionConverter();

		this.anySelection = true;

		builder.setText(Messages.getString("CreatorActionBuilder.create_data_set"));
		builder.setToolTipText(Messages.getString("CreatorActionBuilder.create_data_set_tooltip"));
		builder.setIcon(IconsSmall.ADD);
	}

	@Override
	public ICreatorActionBuilder setText(final String text) {
		checkExhausted();
		builder.setText(text);
		return this;
	}

	@Override
	public ICreatorActionBuilder setToolTipText(final String toolTipText) {
		checkExhausted();
		builder.setToolTipText(toolTipText);
		return this;
	}

	@Override
	public ICreatorActionBuilder setIcon(final IImageConstant icon) {
		checkExhausted();
		builder.setIcon(icon);
		return this;
	}

	@Override
	public ICreatorActionBuilder setMnemonic(final Character mnemonic) {
		checkExhausted();
		builder.setMnemonic(mnemonic);
		return this;
	}

	@Override
	public ICreatorActionBuilder setMnemonic(final char mnemonic) {
		checkExhausted();
		builder.setMnemonic(mnemonic);
		return this;
	}

	@Override
	public ICreatorActionBuilder setAccelerator(final Accelerator accelerator) {
		checkExhausted();
		builder.setAccelerator(accelerator);
		return this;
	}

	@Override
	public ICreatorActionBuilder setAccelerator(final char key, final Modifier... modifier) {
		checkExhausted();
		builder.setAccelerator(key, modifier);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ICreatorActionBuilder setBeanForm(final IBeanFormBluePrint<?> beanForm) {
		checkExhausted();
		Assert.paramNotNull(beanForm, "beanForm");
		this.beanFormBp = (IBeanFormBluePrint<BEAN_TYPE>) beanForm;
		return this;
	}

	@Override
	public ICreatorActionBuilder setBeanForm(final List<? extends IAttribute<?>> attributes) {
		checkExhausted();
		Assert.paramNotNull(attributes, "attributes");
		return setBeanForm(CapUiToolkit.bluePrintFactory().beanForm(attributes));
	}

	@Override
	public ICreatorActionBuilder setCreatorService(final ICreatorService creatorService) {
		checkExhausted();
		Assert.paramNotNull(creatorService, "creatorService");
		this.creatorService = creatorService;
		return this;
	}

	@Override
	public ICreatorActionBuilder setCreatorService(final IServiceId<ICreatorService> creatorServiceId) {
		checkExhausted();
		Assert.paramNotNull(creatorServiceId, "creatorServiceId");
		final ICreatorService service = ServiceProvider.getService(creatorServiceId);
		if (service == null) {
			throw new IllegalArgumentException("No creator service found for the id '" + creatorServiceId + "'.");
		}
		return setCreatorService(service);
	}

	@Override
	public ICreatorActionBuilder setCreatorService(final String creatorServiceId) {
		checkExhausted();
		Assert.paramNotNull(creatorServiceId, "creatorServiceId");
		return setCreatorService(new ServiceId<ICreatorService>(creatorServiceId, ICreatorService.class));
	}

	@Override
	public ICreatorActionBuilder setAnySelection(final boolean anySelection) {
		checkExhausted();
		this.anySelection = anySelection;
		return this;
	}

	@Override
	public ICreatorActionBuilder addEnabledChecker(final IEnabledChecker enabledChecker) {
		checkExhausted();
		Assert.paramNotNull(enabledChecker, "enabledChecker");
		enabledCheckers.add(enabledChecker);
		return this;
	}

	@Override
	public ICreatorActionBuilder setExceptionConverter(final IBeanExecptionConverter exceptionConverter) {
		checkExhausted();
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");
		this.exceptionConverter = exceptionConverter;
		return this;
	}

	@Override
	protected IAction doBuild() {
		final BeanCreatorCommand<BEAN_TYPE> command = new BeanCreatorCommand<BEAN_TYPE>(
			beanType,
			model,
			beanFormBp,
			enabledCheckers,
			anySelection,
			creatorService,
			exceptionConverter);
		builder.setCommand((ICommand) command);
		return builder.build();
	}
}
