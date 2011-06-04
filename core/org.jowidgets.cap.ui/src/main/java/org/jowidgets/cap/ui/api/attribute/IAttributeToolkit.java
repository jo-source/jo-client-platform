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

package org.jowidgets.cap.ui.api.attribute;

import java.util.Collection;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IProperty;

public interface IAttributeToolkit {

	<ELEMENT_VALUE_TYPE> IAttributeBuilder<ELEMENT_VALUE_TYPE> createAttributeBuilder(
		Class<? extends ELEMENT_VALUE_TYPE> elementValueType);

	<ELEMENT_VALUE_TYPE> IAttributeBuilder<ELEMENT_VALUE_TYPE> createAttributeBuilder(
		Class<? extends Collection<? extends ELEMENT_VALUE_TYPE>> valueType,
		Class<? extends ELEMENT_VALUE_TYPE> elementValueType);

	<ELEMENT_VALUE_TYPE> IAttributeBuilder<ELEMENT_VALUE_TYPE> createAttributeBuilder(IProperty property);

	IAttributeCollectionModifierBuilder createAttributeCollectionModifierBuilder();

	List<IAttribute<Object>> createAttributes(
		Collection<? extends IProperty> properties,
		IAttributeCollectionModifier attributeCollectionModifier);

	List<IAttribute<Object>> createAttributes(Collection<? extends IProperty> properties);

	List<IAttribute<Object>> createAttributesCopy(
		Collection<? extends IAttribute<?>> attributes,
		IAttributeCollectionModifier attributeCollectionModifier);

	List<IAttribute<Object>> createAttributesCopy(Collection<? extends IAttribute<?>> attributes);

	IAttribute<Object> createMetaAttribute(String propertyName);

	IAttributeBuilder<Object> createMetaAttributeBuilder(String propertyName);

	<ELEMENT_VALUE_TYPE> IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> createControlPanelProviderBuilder(
		Class<? extends ELEMENT_VALUE_TYPE> elementValueType);

	<ELEMENT_VALUE_TYPE> IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> createControlPanelProviderBuilder(
		Class<? extends Collection<? extends ELEMENT_VALUE_TYPE>> valueType,
		Class<? extends ELEMENT_VALUE_TYPE> elementValueType);

	<ELEMENT_VALUE_TYPE> IControlPanelProvider<ELEMENT_VALUE_TYPE> createControlPanelProvider(
		Class<? extends ELEMENT_VALUE_TYPE> elementValueType);

	<ELEMENT_VALUE_TYPE> IControlPanelProvider<ELEMENT_VALUE_TYPE> createControlPanelProvider(
		Class<? extends Collection<? extends ELEMENT_VALUE_TYPE>> valueType,
		Class<? extends ELEMENT_VALUE_TYPE> elementValueType);

}
