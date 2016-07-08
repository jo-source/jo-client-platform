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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.controller.IDisposeObservable;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.cap.common.api.bean.Cardinality;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.ILinkCreatorService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.AttributeSet;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeSet;
import org.jowidgets.cap.ui.api.bean.BeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.command.IPasteLinkActionBuilder;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.plugin.IServiceActionDecoratorPlugin;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.common.types.VirtualKey;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.PluginProperties;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.IFactory;

final class PasteLinkActionBuilderImpl<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> extends
		AbstractCapActionBuilderImpl<IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE>> implements
		IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> {

	private final Cardinality linkedCardinality;

	private final List<IExecutableChecker<SOURCE_BEAN_TYPE>> sourceExecutableCheckers;
	private final List<IEnabledChecker> enabledCheckers;
	private final List<IExecutionInterceptor<List<IBeanDto>>> executionInterceptors;
	private final IDisposeObservable disposeObservable;

	private ILinkCreatorService linkCreatorService;
	private IBeanSelectionProvider<SOURCE_BEAN_TYPE> source;
	private boolean sourceMultiSelection;
	private boolean sourceSelectionAutoRefresh;
	private BeanModificationStatePolicy sourceModificationPolicy;
	private BeanMessageStatePolicy sourceMessageStatePolicy;
	private IBeanListModel<LINKABLE_BEAN_TYPE> linkedModel;
	private String linkedEntityLabel;
	private Object linkBeanTypeId;
	private Class<? extends LINK_BEAN_TYPE> linkBeanType;
	private IFactory<IBeanProxy<LINK_BEAN_TYPE>> linkDefaultFactory;
	private Object linkableBeanTypeId;
	private Class<? extends LINKABLE_BEAN_TYPE> linkableBeanType;
	private IBeanExceptionConverter exceptionConverter;
	private boolean serviceBasedEnabledChecking;

	@SuppressWarnings({"rawtypes", "unchecked"})
	PasteLinkActionBuilderImpl(
		final IBeanSelectionProvider<SOURCE_BEAN_TYPE> source,
		final IEntityLinkDescriptor linkDescriptor,
		final IDisposeObservable disposeObservable) {
		this(linkDescriptor.getLinkedCardinality(), disposeObservable);
		Assert.paramNotNull(source, "source");
		Assert.paramNotNull(linkDescriptor, "linkDescriptor");

		this.serviceBasedEnabledChecking = true;

		setSource(source);

		setLinkCreatorService(linkDescriptor.getLinkCreatorService());
		final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService != null) {
			final Object linkEntityId = linkDescriptor.getLinkEntityId();
			if (linkEntityId != null) {
				final IBeanDtoDescriptor descriptor = entityService.getDescriptor(linkEntityId);
				if (descriptor != null) {
					final Class beanType = descriptor.getBeanType();
					setLinkBeanType(beanType);
					setLinkBeanTypeId(descriptor.getBeanTypeId());
					final List<IAttribute<Object>> attributes = createAttributes(descriptor);
					final HashMap<String, Object> defaultValues = new HashMap<String, Object>();
					for (final IAttribute<?> attribute : attributes) {
						final String propertyName = attribute.getPropertyName();
						final Object defaultValue = attribute.getDefaultValue();
						if (defaultValue != null) {
							defaultValues.put(propertyName, defaultValue);
						}
					}
					if (!defaultValues.isEmpty()) {
						setLinkDefaultFactory(new IFactory<IBeanProxy<LINK_BEAN_TYPE>>() {

							private final IAttributeSet attributeSet = AttributeSet.create(attributes);

							@Override
							public IBeanProxy<LINK_BEAN_TYPE> create() {
								final IBeanProxyFactory<LINK_BEAN_TYPE> proxyFactory = CapUiToolkit.beanProxyFactory(
										linkBeanTypeId,
										linkBeanType,
										attributeSet);
								return proxyFactory.createTransientProxy(defaultValues);
							}
						});
					}
				}
			}

			final Object linkableEntityId = linkDescriptor.getLinkableEntityId();
			if (linkableEntityId != null) {
				final IBeanDtoDescriptor descriptor = entityService.getDescriptor(linkableEntityId);
				if (descriptor != null) {

					final Class linkableType = descriptor.getBeanType();
					setLinkableBeanType(linkableType);
					setLinkableBeanTypeId(descriptor.getBeanTypeId());

					//this may be overridden when linked entity id will be extracted
					if (!EmptyCheck.isEmpty(descriptor.getLabelPlural().get())) {
						setLinkedEntityLabel(descriptor.getLabelPlural().get());
					}
				}
			}

			final Object linkedEntityId = linkDescriptor.getLinkedEntityId();
			if (linkedEntityId != null) {
				final IBeanDtoDescriptor descriptor = entityService.getDescriptor(linkedEntityId);
				if (descriptor != null) {
					if (Cardinality.GREATER_OR_EQUAL_ZERO.equals(linkedCardinality)
						&& !EmptyCheck.isEmpty(descriptor.getLabelPlural())) {
						setLinkedEntityLabel(descriptor.getLabelPlural().get());
					}
					else if (Cardinality.LESS_OR_EQUAL_ONE.equals(linkedCardinality)
						&& !EmptyCheck.isEmpty(descriptor.getLabelSingular())) {
						setLinkedEntityLabel(descriptor.getLabelSingular().get());
					}
				}
			}

		}
		else {
			throw new IllegalStateException("No EntityService found!");
		}
	}

	PasteLinkActionBuilderImpl(final IDisposeObservable disposeObservable) {
		this(Cardinality.GREATER_OR_EQUAL_ZERO, disposeObservable);
	}

	PasteLinkActionBuilderImpl(final Cardinality linkedCardinality, final IDisposeObservable disposeObservable) {
		super();
		Assert.paramNotNull(linkedCardinality, "linkedCardinality");
		this.linkedCardinality = linkedCardinality;
		this.sourceExecutableCheckers = new LinkedList<IExecutableChecker<SOURCE_BEAN_TYPE>>();
		this.enabledCheckers = new LinkedList<IEnabledChecker>();
		this.executionInterceptors = new LinkedList<IExecutionInterceptor<List<IBeanDto>>>();

		this.sourceMultiSelection = false;
		this.sourceSelectionAutoRefresh = true;
		this.sourceModificationPolicy = BeanModificationStatePolicy.NO_MODIFICATION;
		this.sourceMessageStatePolicy = BeanMessageStatePolicy.NO_WARNING_OR_ERROR;
		this.exceptionConverter = BeanExceptionConverter.get();
		this.disposeObservable = disposeObservable;

		setIcon(IconsSmall.PASTE);
		setAccelerator(VirtualKey.V, Modifier.CTRL);
	}

	private static List<IAttribute<Object>> createAttributes(final IBeanDtoDescriptor descriptor) {
		return CapUiToolkit.attributeToolkit().createAttributes(descriptor.getProperties());
	}

	@Override
	public IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setSource(
		final IBeanSelectionProvider<SOURCE_BEAN_TYPE> selectionProvider) {
		checkExhausted();
		Assert.paramNotNull(selectionProvider, "selectionProvider");
		this.source = selectionProvider;
		return this;
	}

	@Override
	public IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setSourceMultiSelection(
		final boolean multiSelection) {
		checkExhausted();
		this.sourceMultiSelection = multiSelection;
		return this;
	}

	@Override
	public IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setSourceModificationPolicy(
		final BeanModificationStatePolicy policy) {
		checkExhausted();
		Assert.paramNotNull(policy, "policy");
		this.sourceModificationPolicy = policy;
		return this;
	}

	@Override
	public IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setSourceMessageStatePolicy(
		final BeanMessageStatePolicy policy) {
		checkExhausted();
		Assert.paramNotNull(policy, "policy");
		this.sourceMessageStatePolicy = policy;
		return this;
	}

	@Override
	public IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> addSourceExecutableChecker(
		final IExecutableChecker<SOURCE_BEAN_TYPE> executableChecker) {
		checkExhausted();
		Assert.paramNotNull(executableChecker, "executableChecker");
		sourceExecutableCheckers.add(executableChecker);
		return this;
	}

	@Override
	public IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setSourceSelectionAutoRefresh(
		final boolean autoRefresh) {
		checkExhausted();
		this.sourceSelectionAutoRefresh = autoRefresh;
		return this;
	}

	@Override
	public IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkedEntityLabel(final String label) {
		checkExhausted();
		this.linkedEntityLabel = label;
		return this;
	}

	@Override
	public IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkCreatorService(
		final ILinkCreatorService creatorService) {
		checkExhausted();
		Assert.paramNotNull(creatorService, "creatorService");
		this.linkCreatorService = creatorService;
		return this;
	}

	@Override
	public IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkDefaultFactory(
		final IFactory<IBeanProxy<LINK_BEAN_TYPE>> defaultFactory) {
		checkExhausted();
		this.linkDefaultFactory = defaultFactory;
		return this;
	}

	@Override
	public IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkBeanTypeId(final Object beanTypeId) {
		checkExhausted();
		this.linkBeanTypeId = beanTypeId;
		return this;
	}

	@Override
	public IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkBeanType(
		final Class<? extends LINK_BEAN_TYPE> beanType) {
		checkExhausted();
		this.linkBeanType = beanType;
		return this;
	}

	@Override
	public IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkableBeanTypeId(
		final Object linkableBeanTypeId) {
		checkExhausted();
		this.linkableBeanTypeId = linkableBeanTypeId;
		return this;
	}

	@Override
	public IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkableBeanType(
		final Class<? extends LINKABLE_BEAN_TYPE> beanType) {
		checkExhausted();
		this.linkableBeanType = beanType;
		return this;
	}

	@Override
	public IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> addEnabledChecker(
		final IEnabledChecker enabledChecker) {
		checkExhausted();
		Assert.paramNotNull(enabledChecker, "enabledChecker");
		this.enabledCheckers.add(enabledChecker);
		return this;
	}

	@Override
	public IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setServiceBasedEnabledChecking(
		final boolean serviceBasedEnabledChecking) {
		this.serviceBasedEnabledChecking = serviceBasedEnabledChecking;
		return this;
	}

	@Override
	public IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setExceptionConverter(
		final IBeanExceptionConverter exceptionConverter) {
		checkExhausted();
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");
		this.exceptionConverter = exceptionConverter;
		return this;
	}

	@Override
	public IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> addExecutionInterceptor(
		final IExecutionInterceptor<List<IBeanDto>> interceptor) {
		checkExhausted();
		Assert.paramNotNull(interceptor, "interceptor");
		this.executionInterceptors.add(interceptor);
		return this;
	}

	@Override
	public IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkedModel(
		final IBeanListModel<LINKABLE_BEAN_TYPE> model) {
		checkExhausted();
		this.linkedModel = model;
		return this;
	}

	private void setDefaultTextIfNecessary() {
		if (EmptyCheck.isEmpty(getText()) && !EmptyCheck.isEmpty(linkedEntityLabel)) {
			final String message = Messages.getString("PasteLinkActionBuilderImpl.paste");
			setText(MessageReplacer.replace(message, linkedEntityLabel));
		}
	}

	@Override
	public IAction doBuild() {
		return decorateActionWithPlugins(buildAction());
	}

	private IAction decorateActionWithPlugins(final IAction action) {
		IAction result = action;
		final IPluginProperties properties = PluginProperties.create(
				IServiceActionDecoratorPlugin.SERVICE_TYPE_PROPERTY_KEY,
				ILinkCreatorService.class);
		final List<IServiceActionDecoratorPlugin> plugins = PluginProvider.getPlugins(
				IServiceActionDecoratorPlugin.ID,
				properties);
		for (final IServiceActionDecoratorPlugin plugin : plugins) {
			result = plugin.decorate(result, linkCreatorService);
			if (result == null) {
				return null;
			}
		}
		return result;
	}

	private IAction buildAction() {
		setDefaultTextIfNecessary();
		final ICommand command = new PasteLinkCommand<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE>(
			linkCreatorService,
			source,
			sourceSelectionAutoRefresh,
			sourceMultiSelection,
			sourceModificationPolicy,
			sourceMessageStatePolicy,
			sourceExecutableCheckers,
			linkedModel,
			linkedCardinality,
			linkBeanTypeId,
			linkBeanType,
			linkDefaultFactory,
			linkableBeanTypeId,
			linkableBeanType,
			enabledCheckers,
			serviceBasedEnabledChecking,
			executionInterceptors,
			exceptionConverter,
			disposeObservable);
		final IActionBuilder builder = getBuilder();
		builder.setCommand(command);

		return builder.build();
	}
}
