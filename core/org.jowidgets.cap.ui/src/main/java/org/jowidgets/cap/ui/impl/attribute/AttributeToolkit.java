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

package org.jowidgets.cap.ui.impl.attribute;

import java.util.Collection;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifier;
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifierBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProviderBuilder;


public final class AttributeToolkit implements IAttributeToolkit {

	private final AttributesFactory attributesFactory;

	public AttributeToolkit() {
		this.attributesFactory = new AttributesFactory();
	}

	@Override
	public <ELEMENT_VALUE_TYPE> IAttributeBuilder<ELEMENT_VALUE_TYPE> createAttributeBuilder(
		final Class<? extends ELEMENT_VALUE_TYPE> elementValueType) {
		return new AttributeBuilderImpl<ELEMENT_VALUE_TYPE>(elementValueType);
	}

	@Override
	public <ELEMENT_VALUE_TYPE> IAttributeBuilder<ELEMENT_VALUE_TYPE> createAttributeBuilder(
		final Class<? extends Collection<? extends ELEMENT_VALUE_TYPE>> valueType,
		final Class<? extends ELEMENT_VALUE_TYPE> elementValueType) {
		return new AttributeBuilderImpl<ELEMENT_VALUE_TYPE>(valueType, elementValueType);
	}

	@Override
	public <ELEMENT_VALUE_TYPE> IAttributeBuilder<ELEMENT_VALUE_TYPE> createAttributeBuilder(final IProperty property) {
		return new AttributeBuilderImpl<ELEMENT_VALUE_TYPE>(property);
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
	public <ELEMENT_VALUE_TYPE> IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> createControlPanelProviderBuilder(
		final Class<? extends ELEMENT_VALUE_TYPE> elementValueType) {
		return new ControlPanelProviderBuilderImpl<ELEMENT_VALUE_TYPE>(elementValueType);
	}

	@Override
	public <ELEMENT_VALUE_TYPE> IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> createControlPanelProviderBuilder(
		final Class<? extends Collection<? extends ELEMENT_VALUE_TYPE>> valueType,
		final Class<? extends ELEMENT_VALUE_TYPE> elementValueType) {
		return new ControlPanelProviderBuilderImpl<ELEMENT_VALUE_TYPE>(valueType, elementValueType);
	}

	@Override
	public <ELEMENT_VALUE_TYPE> IControlPanelProvider<ELEMENT_VALUE_TYPE> createControlPanelProvider(
		final Class<? extends ELEMENT_VALUE_TYPE> elementValueType) {
		return new ControlPanelProviderBuilderImpl<ELEMENT_VALUE_TYPE>(elementValueType).build();
	}

	@Override
	public <ELEMENT_VALUE_TYPE> IControlPanelProvider<ELEMENT_VALUE_TYPE> createControlPanelProvider(
		final Class<? extends Collection<? extends ELEMENT_VALUE_TYPE>> valueType,
		final Class<? extends ELEMENT_VALUE_TYPE> elementValueType) {
		return new ControlPanelProviderBuilderImpl<ELEMENT_VALUE_TYPE>(valueType, elementValueType).build();
	}

}
