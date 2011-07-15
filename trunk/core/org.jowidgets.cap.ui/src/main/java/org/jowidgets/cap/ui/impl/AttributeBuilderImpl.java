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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeBluePrint;
import org.jowidgets.cap.ui.api.attribute.IAttributeBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeGroup;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.util.Assert;

final class AttributeBuilderImpl<ELEMENT_VALUE_TYPE> implements IAttributeBuilder<ELEMENT_VALUE_TYPE> {

	private String propertyName;
	private IValueRange valueRange;
	private String label;
	private String labelLong;
	private String description;
	private boolean visible;
	private boolean mandatory;
	private boolean editable;
	private boolean readonly;
	private AlignmentHorizontal tableAlignment;
	private int tableColumnWidth;
	private IAttributeGroup attributeGroup;
	private boolean sortable;
	private boolean filterable;
	private Class<?> valueType;
	private Class<? extends ELEMENT_VALUE_TYPE> elementValueType;
	private String displayFormatId;

	@SuppressWarnings("rawtypes")
	private List controlPanels;

	AttributeBuilderImpl(final Class<? extends ELEMENT_VALUE_TYPE> elementValueType) {
		this();
		Assert.paramNotNull(elementValueType, "elementValueType");
		if (Collection.class.isAssignableFrom(elementValueType)) {
			throw new IllegalArgumentException("The parameter '" + elementValueType + "' must not be a 'Collection'");
		}
		this.elementValueType = elementValueType;
		this.valueType = elementValueType;
	}

	public AttributeBuilderImpl(final Class<?> valueType, final Class<? extends ELEMENT_VALUE_TYPE> elementValueType) {
		this();
		Assert.paramNotNull(valueType, "valueType");
		Assert.paramNotNull(elementValueType, "elementValueType");
		this.valueType = elementValueType;
		this.elementValueType = elementValueType;
	}

	public AttributeBuilderImpl(final IAttribute<ELEMENT_VALUE_TYPE> attribute) {
		this();
		Assert.paramNotNull(attribute, "attribute");
		this.propertyName = attribute.getPropertyName();
		this.valueRange = attribute.getValueRange();
		this.label = attribute.getLabel();
		this.labelLong = attribute.getLabelLong();
		this.description = attribute.getDescription();
		this.visible = attribute.isVisible();
		this.mandatory = attribute.isMandatory();
		this.editable = attribute.isEditable();
		this.readonly = attribute.isReadonly();
		this.tableAlignment = attribute.getTableAlignment();
		this.tableColumnWidth = attribute.getTableWidth();
		this.attributeGroup = attribute.getGroup();
		this.sortable = attribute.isSortable();
		this.filterable = attribute.isFilterable();
		this.valueType = attribute.getValueType();
		this.elementValueType = attribute.getElementValueType();
		this.displayFormatId = attribute.getDisplayFormatId();
		this.controlPanels = attribute.getControlPanels();
	}

	@SuppressWarnings("unchecked")
	public AttributeBuilderImpl(final IProperty property) {
		this();
		Assert.paramNotNull(property, "property");
		this.valueType = property.getValueType();
		this.elementValueType = (Class<? extends ELEMENT_VALUE_TYPE>) property.getElementValueType();
		this.propertyName = property.getName();
		this.valueRange = property.getValueRange();
		this.label = property.getLabelDefault();
		this.labelLong = property.getLabelLongDefault();
		this.description = property.getDescriptionDefault();
		this.visible = property.isVisibleDefault();
		this.mandatory = property.isMandatoryDefault();
		this.editable = !property.isReadonly();
		this.readonly = property.isReadonly();
		this.sortable = property.isSortable();
		this.filterable = property.isFilterable();
	}

	private AttributeBuilderImpl() {
		this.controlPanels = new LinkedList<IControlPanelProvider<? extends ELEMENT_VALUE_TYPE>>();
		this.valueRange = CapCommonToolkit.valueRangeFactory().create();
		this.visible = true;
		this.mandatory = false;
		this.editable = true;
		this.readonly = false;
		this.tableAlignment = AlignmentHorizontal.LEFT;
		this.tableColumnWidth = 100;
		this.sortable = true;
		this.filterable = true;
	}

	@Override
	public IAttributeBuilder<ELEMENT_VALUE_TYPE> setPropertyName(final String propertyName) {
		Assert.paramNotEmpty(propertyName, "propertyName");
		this.propertyName = propertyName;
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setValueRange(final IValueRange valueRange) {
		Assert.paramNotNull(valueRange, "valueRange");
		this.valueRange = valueRange;
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setValueRange(
		final Collection<? extends ELEMENT_VALUE_TYPE> values,
		final boolean open) {
		Assert.paramNotNull(values, "values");
		return setValueRange(CapCommonToolkit.valueRangeFactory().create(values, open));
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setValueRange(final Collection<? extends ELEMENT_VALUE_TYPE> values) {
		return setValueRange(values, false);
	}

	@Override
	public IAttributeBuilder<ELEMENT_VALUE_TYPE> setLabel(final String label) {
		Assert.paramNotEmpty(label, "label");
		this.label = label;
		return this;
	}

	@Override
	public IAttributeBuilder<ELEMENT_VALUE_TYPE> setLabelLong(final String labelLong) {
		this.labelLong = labelLong;
		return this;
	}

	@Override
	public IAttributeBuilder<ELEMENT_VALUE_TYPE> setDescription(final String description) {
		this.description = description;
		return this;
	}

	@Override
	public IAttributeBuilder<ELEMENT_VALUE_TYPE> setVisible(final boolean visible) {
		this.visible = visible;
		return this;
	}

	@Override
	public IAttributeBuilder<ELEMENT_VALUE_TYPE> setMandatory(final boolean mandatory) {
		this.mandatory = mandatory;
		return this;
	}

	@Override
	public IAttributeBuilder<ELEMENT_VALUE_TYPE> setEditable(final boolean editable) {
		this.editable = editable;
		return this;
	}

	@Override
	public IAttributeBuilder<ELEMENT_VALUE_TYPE> setReadonly(final boolean readonly) {
		this.readonly = readonly;
		return this;
	}

	@Override
	public IAttributeBuilder<ELEMENT_VALUE_TYPE> setTableAlignment(final AlignmentHorizontal tableAlignment) {
		Assert.paramNotNull(tableAlignment, "tableAlignment");
		this.tableAlignment = tableAlignment;
		return this;
	}

	@Override
	public IAttributeBuilder<ELEMENT_VALUE_TYPE> setTableColumnWidth(final int tableColumnWidth) {
		this.tableColumnWidth = tableColumnWidth;
		return this;
	}

	@Override
	public IAttributeBuilder<ELEMENT_VALUE_TYPE> setGroup(final IAttributeGroup group) {
		this.attributeGroup = group;
		return this;
	}

	@Override
	public IAttributeBuilder<ELEMENT_VALUE_TYPE> setSortable(final boolean sortable) {
		this.sortable = sortable;
		return this;
	}

	@Override
	public IAttributeBuilder<ELEMENT_VALUE_TYPE> setFilterable(final boolean filterable) {
		this.filterable = filterable;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IAttributeBuilder<ELEMENT_VALUE_TYPE> addControlPanel(
		final IControlPanelProvider<? extends ELEMENT_VALUE_TYPE> controlPanel) {
		Assert.paramNotNull(controlPanel, "controlPanel");
		this.controlPanels.add(controlPanel);
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setDisplayFormatId(final String displayFormatId) {
		this.displayFormatId = displayFormatId;
		return this;
	}

	@SuppressWarnings("rawtypes")
	private List getControlPanels() {
		if (controlPanels.isEmpty()) {
			if (Collection.class.isAssignableFrom(valueType)) {
				return Collections.singletonList(CapUiToolkit.getAttributeToolkit().createControlPanelProvider(
						valueType,
						elementValueType,
						valueRange));
			}
			else {
				return Collections.singletonList(CapUiToolkit.getAttributeToolkit().createControlPanelProvider(
						elementValueType,
						valueRange));
			}
		}
		return controlPanels;
	}

	private String getDisplayFormatId(final List<IControlPanelProvider<? extends ELEMENT_VALUE_TYPE>> controlPanels) {
		if (displayFormatId == null) {
			return controlPanels.get(0).getDisplayFormatId();
		}
		else {
			return displayFormatId;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public IAttribute<ELEMENT_VALUE_TYPE> build() {
		final List<IControlPanelProvider<? extends ELEMENT_VALUE_TYPE>> panels = getControlPanels();
		final String displayFormat = getDisplayFormatId(panels);

		return new AttributeImpl<ELEMENT_VALUE_TYPE>(
			propertyName,
			valueRange,
			label,
			labelLong,
			description,
			visible,
			mandatory,
			editable,
			readonly,
			tableAlignment,
			tableColumnWidth,
			attributeGroup,
			sortable,
			filterable,
			valueType,
			elementValueType,
			panels,
			displayFormat);
	}
}
