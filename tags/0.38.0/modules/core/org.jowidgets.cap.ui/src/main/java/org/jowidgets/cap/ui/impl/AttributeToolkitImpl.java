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
import java.util.List;

import org.jowidgets.cap.common.api.bean.Cardinality;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifier;
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifierBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeFilterFactory;
import org.jowidgets.cap.ui.api.attribute.IAttributeSet;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.attribute.IBeanAttributeBuilder;
import org.jowidgets.cap.ui.api.attribute.IBeanAttributesBuilder;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProviderBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;

final class AttributeToolkitImpl implements IAttributeToolkit {

	private final AttributesFactory attributesFactory;
	private final IAttributeFilterFactory attributeFilterFactory;

	AttributeToolkitImpl() {
		this.attributesFactory = new AttributesFactory();
		this.attributeFilterFactory = new AttributeFilterFactoryImpl();
	}

	@Override
	public IAttributeFilterFactory attributeFilterFactory() {
		return attributeFilterFactory;
	}

	@Override
	public <ELEMENT_VALUE_TYPE> IAttributeBuilder<ELEMENT_VALUE_TYPE> createAttributeBuilder(
		final Class<? extends ELEMENT_VALUE_TYPE> elementValueType) {
		return new AttributeBuilderImpl<ELEMENT_VALUE_TYPE>(elementValueType);
	}

	@Override
	public <ELEMENT_VALUE_TYPE> IAttributeBuilder<ELEMENT_VALUE_TYPE> createAttributeBuilder(
		final Class<?> valueType,
		final Class<? extends ELEMENT_VALUE_TYPE> elementValueType) {
		return new AttributeBuilderImpl<ELEMENT_VALUE_TYPE>(valueType, elementValueType);
	}

	@Override
	public <ELEMENT_VALUE_TYPE> IAttributeBuilder<ELEMENT_VALUE_TYPE> createAttributeBuilder(final IProperty property) {
		return new AttributeBuilderImpl<ELEMENT_VALUE_TYPE>(property);
	}

	@Override
	public <ELEMENT_VALUE_TYPE> IBeanAttributeBuilder<ELEMENT_VALUE_TYPE> createBeanAttributeBuilder(
		final Class<?> beanType,
		final String propertyName) {
		return new BeanAttributeBuilderImpl<ELEMENT_VALUE_TYPE>(beanType, propertyName);
	}

	@Override
	public IBeanAttributesBuilder createBeanAttributesBuilder(final Class<?> beanType) {
		return new BeanAttributesBuilderImpl(beanType);
	}

	@Override
	public IAttributeCollectionModifierBuilder createAttributeCollectionModifierBuilder() {
		return new AttributeCollectionModifierBuilderImpl();
	}

	@Override
	public List<IAttribute<Object>> createAttributes(
		final Collection<? extends IProperty> properties,
		final IAttributeCollectionModifier attributeCollectionModifier) {
		return attributesFactory.createAttributes(properties, attributeCollectionModifier);
	}

	@Override
	public List<IAttribute<Object>> createAttributes(final Collection<? extends IProperty> properties) {
		return attributesFactory.createAttributes(properties, null);
	}

	@Override
	public List<IAttribute<Object>> createAttributes(
		final Object entityID,
		final IAttributeCollectionModifier attributeCollectionModifier) {
		Assert.paramNotNull(entityID, "entityID");

		final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService != null) {
			final IBeanDtoDescriptor descriptor = entityService.getDescriptor(entityID);
			if (descriptor != null) {
				final List<IProperty> properties = descriptor.getProperties();
				if (properties != null) {
					return createAttributes(properties, attributeCollectionModifier);
				}
			}
		}
		throw new IllegalArgumentException("Could not retrieve properties for entityId '" + entityID + "'");
	}

	@Override
	public List<IAttribute<Object>> createAttributes(final Object entityID) {
		return createAttributes(entityID, null);
	}

	@Override
	public List<IAttribute<Object>> createAttributesCopy(
		final Collection<? extends IAttribute<?>> attributes,
		final IAttributeCollectionModifier attributeCollectionModifier) {
		return attributesFactory.createAttributesCopy(attributes, attributeCollectionModifier);
	}

	@Override
	public List<IAttribute<Object>> createAttributesCopy(final Collection<? extends IAttribute<?>> attributes) {
		return attributesFactory.createAttributesCopy(attributes, null);
	}

	@Override
	public IAttribute<Object> createMetaAttribute(final String propertyName) {
		return createMetaAttributeBuilder(propertyName).build();
	}

	@SuppressWarnings("unchecked")
	@Override
	public IAttributeBuilder<Object> createMetaAttributeBuilder(final String propertyName) {
		Assert.paramNotNull(propertyName, "propertyName");

		if (IBeanProxy.META_PROPERTY_PROGRESS.equals(propertyName)) {
			@SuppressWarnings("rawtypes")
			final IAttributeBuilder builder = new AttributeBuilderImpl(String.class);
			builder.setPropertyName(IBeanProxy.META_PROPERTY_PROGRESS);
			builder.setLabel(Messages.getString("AttributeToolkitImpl.progress"));
			builder.setDescription(Messages.getString("AttributeToolkitImpl.progress_description"));
			builder.setEditable(false).setSortable(false).setFilterable(false);
			return builder;
		}
		else if (IBeanProxy.META_PROPERTY_MESSAGES.equals(propertyName)) {
			@SuppressWarnings("rawtypes")
			final IAttributeBuilder builder = new AttributeBuilderImpl(List.class, IBeanMessage.class);
			builder.setPropertyName(IBeanProxy.META_PROPERTY_MESSAGES);
			builder.setLabel(Messages.getString("AttributeToolkitImpl.messages"));
			builder.setDescription(Messages.getString("AttributeToolkitImpl.messages_description"));
			builder.setEditable(false).setSortable(false).setFilterable(false);
			return builder;
		}
		throw new IllegalArgumentException("The meta property name '" + propertyName + "' is not known");
	}

	@Override
	public <ELEMENT_VALUE_TYPE> IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> createControlPanelProviderBuilder(
		final String propertyName,
		final Class<? extends ELEMENT_VALUE_TYPE> elementValueType,
		final IValueRange valueRange,
		final Cardinality cardinality) {
		return new ControlPanelProviderBuilderImpl<ELEMENT_VALUE_TYPE>(propertyName, elementValueType, valueRange, cardinality);
	}

	@Override
	public <ELEMENT_VALUE_TYPE> IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> createControlPanelProviderBuilder(
		final String propertyName,
		final Class<?> valueType,
		final Class<? extends ELEMENT_VALUE_TYPE> elementValueType,
		final IValueRange valueRange,
		final Cardinality cardinality) {
		return new ControlPanelProviderBuilderImpl<ELEMENT_VALUE_TYPE>(
			propertyName,
			valueType,
			elementValueType,
			valueRange,
			cardinality);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public IAttributeSet attributeSet(final Collection attributes) {
		Assert.paramNotNull(attributes, "attributes");
		return new AttributeSetImpl(attributes);
	}

}
