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

import org.jowidgets.cap.common.api.bean.Cardinality;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeGroup;
import org.jowidgets.cap.ui.api.attribute.IBeanAttributeBluePrint;
import org.jowidgets.cap.ui.api.attribute.IBeanAttributeBuilder;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProviderBluePrint;
import org.jowidgets.cap.ui.api.control.DisplayFormat;
import org.jowidgets.cap.ui.api.control.IDisplayFormat;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidator;

final class BeanAttributeBluePrintImpl<ELEMENT_VALUE_TYPE> implements IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> {

	private final IBeanAttributeBuilder<ELEMENT_VALUE_TYPE> attributeBuilder;

	BeanAttributeBluePrintImpl(final Class<?> beanType, final String propertyName) {
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(propertyName, "propertyName");

		this.attributeBuilder = new BeanAttributeBuilderImpl<ELEMENT_VALUE_TYPE>(beanType, propertyName);
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setValueRange(
		final boolean open,
		final Collection<? extends ELEMENT_VALUE_TYPE> values) {
		attributeBuilder.setValueRange(open, values);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setValueRange(final boolean open, final ELEMENT_VALUE_TYPE... values) {
		attributeBuilder.setValueRange(open, values);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setLookUpValueRange(final Object lookUpId) {
		attributeBuilder.setLookUpValueRange(lookUpId);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setElementValueType(
		final Class<? extends ELEMENT_VALUE_TYPE> elementValueType) {
		attributeBuilder.setElementValueType(elementValueType);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setLabel(final IMessage label) {
		attributeBuilder.setLabel(label);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setLabelLong(final IMessage labelLong) {
		attributeBuilder.setLabelLong(labelLong);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setDescription(final IMessage description) {
		attributeBuilder.setDescription(description);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setLabel(final String label) {
		attributeBuilder.setLabel(label);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setLabelLong(final String labelLong) {
		attributeBuilder.setLabelLong(labelLong);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setDescription(final String description) {
		attributeBuilder.setDescription(description);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setValueRange(final IValueRange valueRange) {
		attributeBuilder.setValueRange(valueRange);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setValueRange(
		final Collection<? extends ELEMENT_VALUE_TYPE> values,
		final boolean open) {
		attributeBuilder.setValueRange(open, values);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setValueRange(final ELEMENT_VALUE_TYPE... values) {
		attributeBuilder.setValueRange(values);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setValueRange(final Collection<? extends ELEMENT_VALUE_TYPE> values) {
		attributeBuilder.setValueRange(values);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setDefaultValue(final Object value) {
		attributeBuilder.setDefaultValue(value);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setCardinality(final Cardinality cardinality) {
		attributeBuilder.setCardinality(cardinality);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setVisible(final boolean visible) {
		attributeBuilder.setVisible(visible);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setEditable(final boolean editable) {
		attributeBuilder.setEditable(editable);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setReadonly(final boolean readonly) {
		attributeBuilder.setReadonly(readonly);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setMandatory(final boolean mandatory) {
		attributeBuilder.setMandatory(mandatory);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setTableAlignment(final AlignmentHorizontal alignment) {
		attributeBuilder.setTableAlignment(alignment);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setTableColumnWidth(final int width) {
		attributeBuilder.setTableColumnWidth(width);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setGroup(final IAttributeGroup group) {
		attributeBuilder.setGroup(group);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setSortable(final boolean sortable) {
		attributeBuilder.setSortable(sortable);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setFilterable(final boolean filterable) {
		attributeBuilder.setFilterable(filterable);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setSearchable(final boolean searchable) {
		attributeBuilder.setSearchable(searchable);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setLabelDisplayFormat(final DisplayFormat displayFormat) {
		attributeBuilder.setLabelDisplayFormat(displayFormat);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> addValidator(final IValidator<? extends Object> validator) {
		attributeBuilder.addValidator(validator);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> addElementTypeValidator(final IValidator<ELEMENT_VALUE_TYPE> validator) {
		attributeBuilder.addElementTypeValidator(validator);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> addBeanValidator(final Class<?> beanType) {
		attributeBuilder.addBeanValidator(beanType);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setDisplayFormat(final IDisplayFormat displayFormat) {
		attributeBuilder.setDisplayFormat(displayFormat);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> addControlPanel(
		final IControlPanelProvider<? extends ELEMENT_VALUE_TYPE> controlPanel) {
		attributeBuilder.addControlPanel(controlPanel);
		return this;
	}

	@Override
	public IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> setControlPanels(
		final Collection<? extends IControlPanelProvider<? extends ELEMENT_VALUE_TYPE>> controlPanels) {
		attributeBuilder.setControlPanels(controlPanels);
		return this;
	}

	@Override
	public IControlPanelProviderBluePrint<ELEMENT_VALUE_TYPE> addControlPanel(final IDisplayFormat displayFormat) {
		return attributeBuilder.addControlPanel(displayFormat);
	}

	@Override
	public IControlPanelProviderBluePrint<ELEMENT_VALUE_TYPE> setControlPanel() {
		return attributeBuilder.setControlPanel();
	}

	public IAttribute<ELEMENT_VALUE_TYPE> build() {
		return attributeBuilder.build();
	}

}
