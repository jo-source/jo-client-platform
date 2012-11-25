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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.BeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.command.ICreatorActionBuilder;
import org.jowidgets.cap.ui.api.command.ICreatorInterceptor;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.image.ImageResolver;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.plugin.IServiceActionDecoratorPlugin;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.common.image.IImageProvider;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.common.types.VirtualKey;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.PluginProperties;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.service.tools.ServiceId;
import org.jowidgets.util.Assert;

final class CreatorActionBuilderImpl<BEAN_TYPE> extends AbstractCapActionBuilderImpl<ICreatorActionBuilder<BEAN_TYPE>> implements
		ICreatorActionBuilder<BEAN_TYPE> {

	private final Object entityId;
	private final Class<? extends BEAN_TYPE> beanType;
	private final IBeanListModel<BEAN_TYPE> model;
	private final List<IEnabledChecker> enabledCheckers;
	private final List<IExecutionInterceptor<List<IBeanDto>>> executionInterceptors;
	private final List<ICreatorInterceptor<BEAN_TYPE>> creatorInterceptors;
	private final List<IBeanPropertyValidator<BEAN_TYPE>> beanPropertyValidators;
	private boolean anySelection;

	private ICreatorService creatorService;
	private IBeanFormBluePrint<BEAN_TYPE> beanFormBp;
	private List<IAttribute<?>> attributes;
	private IBeanExceptionConverter exceptionConverter;

	CreatorActionBuilderImpl(
		final Object entityId,
		final Class<? extends BEAN_TYPE> beanType,
		final IBeanListModel<BEAN_TYPE> model) {
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(model, "model");
		this.entityId = entityId;
		this.beanType = beanType;
		this.model = model;
		this.enabledCheckers = new LinkedList<IEnabledChecker>();
		this.executionInterceptors = new LinkedList<IExecutionInterceptor<List<IBeanDto>>>();
		this.exceptionConverter = BeanExceptionConverter.get();
		this.beanPropertyValidators = new LinkedList<IBeanPropertyValidator<BEAN_TYPE>>();
		this.creatorInterceptors = new LinkedList<ICreatorInterceptor<BEAN_TYPE>>();

		this.anySelection = true;

		setText(Messages.getString("CreatorActionBuilder.create_data_set"));
		setToolTipText(Messages.getString("CreatorActionBuilder.create_data_set_tooltip"));
		setAccelerator(VirtualKey.N, Modifier.CTRL);
		setIcon(IconsSmall.ADD);

		if (entityId != null) {
			final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
			if (entityService != null) {
				final IBeanDtoDescriptor descriptor = entityService.getDescriptor(entityId);
				if (descriptor != null) {
					final Object icon = descriptor.getCreateIconDescriptor();
					if (icon != null) {
						final IImageProvider imageProvider = ImageResolver.resolve(icon);
						if (imageProvider != null) {
							setIcon(imageProvider);
						}
					}
				}
			}
		}
	}

	@Override
	public ICreatorActionBuilder<BEAN_TYPE> setEntityLabelSingular(final String label) {
		checkExhausted();
		Assert.paramNotEmpty(label, "label");
		final String message = Messages.getString("CreatorActionBuilder.create_with_var");
		setText(MessageReplacer.replace(message, label));
		return this;
	}

	@Override
	public ICreatorActionBuilder<BEAN_TYPE> setAttributes(final Collection<? extends IAttribute<?>> attributes) {
		checkExhausted();
		Assert.paramNotNull(attributes, "attributes");
		this.attributes = new LinkedList<IAttribute<?>>(attributes);
		return this;
	}

	@Override
	public ICreatorActionBuilder<BEAN_TYPE> setBeanForm(final IBeanFormBluePrint<BEAN_TYPE> beanForm) {
		checkExhausted();
		Assert.paramNotNull(beanForm, "beanForm");
		this.beanFormBp = beanForm;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ICreatorActionBuilder<BEAN_TYPE> setBeanForm(final Collection<? extends IAttribute<?>> attributes) {
		checkExhausted();
		Assert.paramNotNull(attributes, "attributes");
		return setBeanForm((IBeanFormBluePrint<BEAN_TYPE>) CapUiToolkit.bluePrintFactory().beanForm(entityId, attributes));
	}

	@Override
	public ICreatorActionBuilder<BEAN_TYPE> setCreatorService(final ICreatorService creatorService) {
		checkExhausted();
		Assert.paramNotNull(creatorService, "creatorService");
		this.creatorService = creatorService;
		return this;
	}

	@Override
	public ICreatorActionBuilder<BEAN_TYPE> setCreatorService(final IServiceId<ICreatorService> creatorServiceId) {
		checkExhausted();
		Assert.paramNotNull(creatorServiceId, "creatorServiceId");
		final ICreatorService service = ServiceProvider.getService(creatorServiceId);
		if (service == null) {
			throw new IllegalArgumentException("No creator service found for the id '" + creatorServiceId + "'.");
		}
		return setCreatorService(service);
	}

	@Override
	public ICreatorActionBuilder<BEAN_TYPE> setCreatorService(final String creatorServiceId) {
		checkExhausted();
		Assert.paramNotNull(creatorServiceId, "creatorServiceId");
		return setCreatorService(new ServiceId<ICreatorService>(creatorServiceId, ICreatorService.class));
	}

	@Override
	public ICreatorActionBuilder<BEAN_TYPE> setAnySelection(final boolean anySelection) {
		checkExhausted();
		this.anySelection = anySelection;
		return this;
	}

	@Override
	public ICreatorActionBuilder<BEAN_TYPE> addEnabledChecker(final IEnabledChecker enabledChecker) {
		checkExhausted();
		Assert.paramNotNull(enabledChecker, "enabledChecker");
		enabledCheckers.add(enabledChecker);
		return this;
	}

	@Override
	public ICreatorActionBuilder<BEAN_TYPE> addExecutionInterceptor(final IExecutionInterceptor<List<IBeanDto>> interceptor) {
		checkExhausted();
		Assert.paramNotNull(interceptor, "interceptor");
		executionInterceptors.add(interceptor);
		return this;
	}

	@Override
	public ICreatorActionBuilder<BEAN_TYPE> addCreatorInterceptor(final ICreatorInterceptor<BEAN_TYPE> interceptor) {
		checkExhausted();
		Assert.paramNotNull(interceptor, "interceptor");
		creatorInterceptors.add(interceptor);
		return this;
	}

	@Override
	public ICreatorActionBuilder<BEAN_TYPE> setExceptionConverter(final IBeanExceptionConverter exceptionConverter) {
		checkExhausted();
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");
		this.exceptionConverter = exceptionConverter;
		return this;
	}

	@Override
	public ICreatorActionBuilder<BEAN_TYPE> setBeanPropertyValidators(
		final List<? extends IBeanPropertyValidator<BEAN_TYPE>> validators) {
		checkExhausted();
		Assert.paramNotNull(validators, "validators");
		beanPropertyValidators.clear();
		beanPropertyValidators.addAll(validators);
		return this;
	}

	@Override
	public ICreatorActionBuilder<BEAN_TYPE> addBeanPropertyValidator(final IBeanPropertyValidator<BEAN_TYPE> validator) {
		checkExhausted();
		Assert.paramNotNull(validator, "validator");
		beanPropertyValidators.add(validator);
		return this;
	}

	@SuppressWarnings("unchecked")
	private IBeanFormBluePrint<BEAN_TYPE> getBeanFormBp() {
		if (beanFormBp == null) {
			if (attributes != null) {
				return (IBeanFormBluePrint<BEAN_TYPE>) CapUiToolkit.bluePrintFactory().beanForm(entityId, attributes);
			}
			else if (entityId != null) {
				return (IBeanFormBluePrint<BEAN_TYPE>) CapUiToolkit.bluePrintFactory().beanForm(entityId);
			}
		}
		return beanFormBp;
	}

	@Override
	public IAction doBuild() {
		return decorateActionWithPlugins(buildAction());
	}

	private IAction decorateActionWithPlugins(final IAction action) {
		IAction result = action;
		final IPluginProperties properties = PluginProperties.create(
				IServiceActionDecoratorPlugin.SERVICE_TYPE_PROPERTY_KEY,
				ICreatorService.class);
		final List<IServiceActionDecoratorPlugin> plugins = PluginProvider.getPlugins(
				IServiceActionDecoratorPlugin.ID,
				properties);
		for (final IServiceActionDecoratorPlugin plugin : plugins) {
			result = plugin.decorate(result, creatorService);
			if (result == null) {
				return null;
			}
		}
		return result;
	}

	private IAction buildAction() {
		final IBeanFormBluePrint<BEAN_TYPE> formBp = getBeanFormBp();
		Collection<IAttribute<?>> attr = attributes;
		if (attr == null && formBp != null) {
			attr = formBp.getCreateModeAttributes();
		}
		final BeanCreatorCommand<BEAN_TYPE> command = new BeanCreatorCommand<BEAN_TYPE>(
			beanType,
			beanPropertyValidators,
			model,
			attr,
			formBp,
			enabledCheckers,
			anySelection,
			creatorService,
			exceptionConverter,
			executionInterceptors,
			creatorInterceptors);

		final IActionBuilder builder = getBuilder();
		builder.setCommand((ICommand) command);
		return builder.build();
	}

}
