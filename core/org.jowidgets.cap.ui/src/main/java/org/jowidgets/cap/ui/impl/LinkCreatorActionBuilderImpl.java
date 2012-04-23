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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.entity.EntityLinkProperties;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkProperties;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.ILinkCreatorService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifierBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.command.ILinkCreatorActionBuilder;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.tools.message.MessageReplacer;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class LinkCreatorActionBuilderImpl<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> extends
		AbstractCapActionBuilderImpl<ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE>> implements
		ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> {

	private final List<IExecutableChecker<SOURCE_BEAN_TYPE>> sourceExecutableCheckers;
	private final List<IEnabledChecker> enabledCheckers;
	private final List<IExecutionInterceptor> executionInterceptors;

	private IEntityLinkProperties sourceProperties;
	private IEntityLinkProperties destinationProperties;
	private ILinkCreatorService linkCreatorService;
	private IBeanSelectionProvider<SOURCE_BEAN_TYPE> source;
	private boolean sourceMultiSelection;
	private BeanModificationStatePolicy sourceModificationPolicy;
	private BeanMessageStatePolicy sourceMessageStatePolicy;
	private IBeanListModel<LINKABLE_BEAN_TYPE> linkedModel;
	private String linkedEntityLabelPlural;
	private Class<? extends LINK_BEAN_TYPE> linkBeanType;
	private Class<? extends LINKABLE_BEAN_TYPE> linkableBeanType;
	private IBeanFormBluePrint<LINK_BEAN_TYPE> linkBeanForm;
	private IBeanFormBluePrint<LINKABLE_BEAN_TYPE> linkableBeanForm;
	private IBeanTableBluePrint<LINKABLE_BEAN_TYPE> linkableTable;
	private IBeanExceptionConverter exceptionConverter;

	@SuppressWarnings({"rawtypes", "unchecked"})
	LinkCreatorActionBuilderImpl(final IBeanSelectionProvider<SOURCE_BEAN_TYPE> source, final IEntityLinkDescriptor linkDescriptor) {
		this();
		Assert.paramNotNull(source, "source");
		Assert.paramNotNull(linkDescriptor, "linkDescriptor");

		setSource(source);

		setSourceProperties(linkDescriptor.getSourceProperties());
		setDestinationProperties(linkDescriptor.getDestinationProperties());
		setLinkCreatorService(linkDescriptor.getLinkCreatorService());

		final ICapApiBluePrintFactory cbpf = CapUiToolkit.bluePrintFactory();

		final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService != null) {
			final Object linkEntityId = linkDescriptor.getLinkEntityId();
			if (linkEntityId != null) {
				final IBeanDtoDescriptor descriptor = entityService.getDescriptor(linkEntityId);
				if (descriptor != null && hasAdditionalProperties(descriptor, linkDescriptor)) {
					final List<IAttribute<Object>> attributes = createAttributes(
							descriptor,
							sourceProperties.getForeignKeyPropertyName(),
							destinationProperties.getForeignKeyPropertyName());

					final IBeanFormBluePrint beanFormBp = cbpf.beanForm(linkEntityId, attributes);
					final Class beanType = descriptor.getBeanType();
					setLinkBeanType(beanType);
					setLinkBeanForm(beanFormBp);
				}
			}

			final Object linkableEntityId = linkDescriptor.getLinkableEntityId();
			if (linkableEntityId != null) {
				final IBeanDtoDescriptor descriptor = entityService.getDescriptor(linkableEntityId);
				if (descriptor != null) {

					final Class linkableType = descriptor.getBeanType();
					setLinkableBeanType(linkableType);

					final IBeanFormBluePrint beanFormBp = cbpf.beanForm(linkableEntityId, createAttributes(descriptor));
					setLinkableBeanForm(beanFormBp);

					final IBeanTableModelBuilder<?> linkableModelBuilder;
					linkableModelBuilder = CapUiToolkit.beanTableModelBuilder(linkableEntityId, linkableType);
					linkableModelBuilder.setParent(source, LinkType.SELECTION_ALL);
					final IBeanTableModel linkableModel = linkableModelBuilder.build();
					setLinkableTable(cbpf.beanTable(linkableModel));

					if (!EmptyCheck.isEmpty(descriptor.getLabelPlural())) {
						setLinkedEntityLabelPlural(descriptor.getLabelPlural());
					}
				}
			}

		}
		else {
			throw new IllegalStateException("No EntityService found!");
		}
	}

	LinkCreatorActionBuilderImpl() {
		super();
		this.sourceExecutableCheckers = new LinkedList<IExecutableChecker<SOURCE_BEAN_TYPE>>();
		this.enabledCheckers = new LinkedList<IEnabledChecker>();
		this.executionInterceptors = new LinkedList<IExecutionInterceptor>();

		this.sourceMultiSelection = false;
		this.sourceModificationPolicy = BeanModificationStatePolicy.NO_MODIFICATION;
		this.sourceMessageStatePolicy = BeanMessageStatePolicy.NO_WARNING_OR_ERROR;
		this.exceptionConverter = new DefaultBeanExceptionConverter();
	}

	private static List<IAttribute<Object>> createAttributes(
		final IBeanDtoDescriptor descriptor,
		final String... filteredProperties) {
		final IAttributeToolkit attributeToolkit = CapUiToolkit.attributeToolkit();
		final IAttributeCollectionModifierBuilder modifierBuilder = attributeToolkit.createAttributeCollectionModifierBuilder();
		modifierBuilder.addAcceptEditableAttributesFilter();
		if (!EmptyCheck.isEmpty(filteredProperties)) {
			modifierBuilder.addBlackListFilter(filteredProperties);
		}
		return attributeToolkit.createAttributes(descriptor.getProperties(), modifierBuilder.build());
	}

	private static boolean hasAdditionalProperties(final IBeanDtoDescriptor descriptor, final IEntityLinkDescriptor linkDescriptor) {
		final Set<String> ignoreProperties = new HashSet<String>();
		ignoreProperties.add(IBean.ID_PROPERTY);
		ignoreProperties.add(IBean.VERSION_PROPERTY);
		ignoreProperties.add(linkDescriptor.getSourceProperties().getForeignKeyPropertyName());
		ignoreProperties.add(linkDescriptor.getDestinationProperties().getForeignKeyPropertyName());
		for (final IProperty property : descriptor.getProperties()) {
			if (property.isEditable() && !ignoreProperties.contains(property.getName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setSource(
		final IBeanSelectionProvider<SOURCE_BEAN_TYPE> selectionProvider) {
		checkExhausted();
		Assert.paramNotNull(selectionProvider, "selectionProvider");
		this.source = selectionProvider;
		return this;
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setSourceMultiSelection(
		final boolean multiSelection) {
		checkExhausted();
		this.sourceMultiSelection = multiSelection;
		return this;
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setSourceModificationPolicy(
		final BeanModificationStatePolicy policy) {
		checkExhausted();
		Assert.paramNotNull(policy, "policy");
		this.sourceModificationPolicy = policy;
		return this;
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setSourceMessageStatePolicy(
		final BeanMessageStatePolicy policy) {
		checkExhausted();
		Assert.paramNotNull(policy, "policy");
		this.sourceMessageStatePolicy = policy;
		return this;
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> addSourceExecutableChecker(
		final IExecutableChecker<SOURCE_BEAN_TYPE> executableChecker) {
		checkExhausted();
		Assert.paramNotNull(executableChecker, "executableChecker");
		sourceExecutableCheckers.add(executableChecker);
		return this;
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkedEntityLabelPlural(
		final String label) {
		checkExhausted();
		this.linkedEntityLabelPlural = label;
		return this;
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkCreatorService(
		final ILinkCreatorService creatorService) {
		checkExhausted();
		Assert.paramNotNull(creatorService, "creatorService");
		this.linkCreatorService = creatorService;
		return this;
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkBeanForm(
		final IBeanFormBluePrint<LINK_BEAN_TYPE> beanForm) {
		checkExhausted();
		this.linkBeanForm = beanForm;
		return this;
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkableBeanForm(
		final IBeanFormBluePrint<LINKABLE_BEAN_TYPE> beanForm) {
		checkExhausted();
		this.linkableBeanForm = beanForm;
		return this;
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkBeanType(
		final Class<? extends LINK_BEAN_TYPE> beanType) {
		checkExhausted();
		this.linkBeanType = beanType;
		return this;
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkableBeanType(
		final Class<? extends LINKABLE_BEAN_TYPE> beanType) {
		checkExhausted();
		this.linkableBeanType = beanType;
		return this;
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkableTable(
		final IBeanTableBluePrint<LINKABLE_BEAN_TYPE> table) {
		checkExhausted();
		this.linkableTable = table;
		return this;
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setSourceProperties(
		final IEntityLinkProperties properties) {
		checkExhausted();
		Assert.paramNotNull(properties, "properties");
		this.sourceProperties = properties;
		return this;
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setSourceProperties(
		final String keyPropertyName,
		final String foreignKeyPropertyName) {
		checkExhausted();
		return setSourceProperties(EntityLinkProperties.create(keyPropertyName, foreignKeyPropertyName));
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setDestinationProperties(
		final IEntityLinkProperties properties) {
		checkExhausted();
		Assert.paramNotNull(properties, "properties");
		this.destinationProperties = properties;
		return this;
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setDestinationProperties(
		final String keyPropertyName,
		final String foreignKeyPropertyName) {
		checkExhausted();
		return setDestinationProperties(EntityLinkProperties.create(keyPropertyName, foreignKeyPropertyName));
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> addEnabledChecker(
		final IEnabledChecker enabledChecker) {
		checkExhausted();
		Assert.paramNotNull(enabledChecker, "enabledChecker");
		this.enabledCheckers.add(enabledChecker);
		return this;
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setExceptionConverter(
		final IBeanExceptionConverter exceptionConverter) {
		checkExhausted();
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");
		this.exceptionConverter = exceptionConverter;
		return this;
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> addExecutionInterceptor(
		final IExecutionInterceptor interceptor) {
		checkExhausted();
		Assert.paramNotNull(interceptor, "interceptor");
		this.executionInterceptors.add(interceptor);
		return this;
	}

	@Override
	public ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkedModel(
		final IBeanListModel<LINKABLE_BEAN_TYPE> model) {
		checkExhausted();
		this.linkedModel = model;
		return this;
	}

	private void setDefaultTextIfNecessary() {
		if (EmptyCheck.isEmpty(getText()) && !EmptyCheck.isEmpty(linkedEntityLabelPlural)) {
			final String message = Messages.getString("LinkActionBuilderImpl.link_var");
			setText(MessageReplacer.replace(message, linkedEntityLabelPlural));
		}

	}

	@Override
	protected IAction doBuild() {
		setDefaultTextIfNecessary();
		final ICommand command = new BeanLinkCreatorCommand<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE>(
			sourceProperties,
			destinationProperties,
			linkCreatorService,
			source,
			sourceMultiSelection,
			sourceModificationPolicy,
			sourceMessageStatePolicy,
			sourceExecutableCheckers,
			linkedModel,
			linkBeanType,
			linkBeanForm,
			linkableBeanType,
			linkableBeanForm,
			linkableTable,
			enabledCheckers,
			executionInterceptors,
			exceptionConverter);

		final IActionBuilder builder = getBuilder();
		builder.setCommand(command);

		return builder.build();
	}
}
