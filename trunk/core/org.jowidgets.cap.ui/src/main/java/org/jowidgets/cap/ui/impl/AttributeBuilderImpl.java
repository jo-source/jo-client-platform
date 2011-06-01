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

import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeGroup;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.util.Assert;

final class AttributeBuilderImpl<ELEMENT_VALUE_TYPE> implements IAttributeBuilder<ELEMENT_VALUE_TYPE> {

	private String propertyName;
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
	private IControlPanelProvider<? extends ELEMENT_VALUE_TYPE> defaultControlPanel;

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

	public AttributeBuilderImpl(
		final Class<? extends Collection<? extends ELEMENT_VALUE_TYPE>> valueType,
		final Class<? extends ELEMENT_VALUE_TYPE> elementValueType) {
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
		this.defaultControlPanel = attribute.getDefaultControlPanel();
		this.controlPanels = attribute.getSupportedControlPanels();
	}

	@SuppressWarnings("unchecked")
	public AttributeBuilderImpl(final IProperty property) {
		this();
		Assert.paramNotNull(property, "property");
		this.valueType = property.getValueType();
		this.elementValueType = (Class<? extends ELEMENT_VALUE_TYPE>) property.getElementValueType();
		this.propertyName = property.getName();
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

	@Override
	public IAttributeBuilder<ELEMENT_VALUE_TYPE> setDefaultControlPanel(
		final IControlPanelProvider<? extends ELEMENT_VALUE_TYPE> controlPanel) {
		this.defaultControlPanel = controlPanel;
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

	@SuppressWarnings("unchecked")
	private IControlPanelProvider<? extends ELEMENT_VALUE_TYPE> getDefaultControlPanel() {
		if (defaultControlPanel == null) {
			if (Collection.class.isAssignableFrom(valueType)) {
				return CapUiToolkit.getAttributeToolkit().createControlPanelProvider(
						(Class<? extends Collection<? extends ELEMENT_VALUE_TYPE>>) valueType,
						elementValueType);
			}
			else {
				return CapUiToolkit.getAttributeToolkit().createControlPanelProvider(elementValueType);
			}
		}
		return defaultControlPanel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IAttribute<ELEMENT_VALUE_TYPE> build() {
		return new AttributeImpl<ELEMENT_VALUE_TYPE>(
			propertyName,
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
			getDefaultControlPanel(),
			controlPanels);
	}

}
