/*
 * Copyright (c) 2011, grossmann, Nikolaus Moll
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

import org.jowidgets.cap.common.api.bean.Cardinality;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeConfig;
import org.jowidgets.cap.ui.api.attribute.IAttributeGroup;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.control.DisplayFormat;
import org.jowidgets.cap.ui.api.control.IDisplayFormat;
import org.jowidgets.cap.ui.api.filter.IFilterPanelProvider;
import org.jowidgets.cap.ui.api.filter.IFilterSupport;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.util.Assert;
import org.jowidgets.util.event.ChangeObservable;
import org.jowidgets.util.event.IChangeListener;

final class AttributeImpl<ELEMENT_VALUE_TYPE> implements IAttribute<ELEMENT_VALUE_TYPE> {

	private final ChangeObservable changeObservable;

	private final String propertyName;
	private final IValueRange valueRange;
	private final Object defaultValue;
	private final String label;
	private final String labelLong;
	private DisplayFormat labelDisplayFormat;
	private final String description;
	private final boolean mandatory;
	private final boolean editable;
	private final boolean readonly;
	private final IAttributeGroup attributeGroup;
	private final boolean sortable;
	private final boolean filterable;
	private final Class<?> valueType;
	private final Class<ELEMENT_VALUE_TYPE> elementValueType;
	private final Cardinality cardinality;
	private final List<IControlPanelProvider<ELEMENT_VALUE_TYPE>> controlPanels;

	private boolean visible;
	private AlignmentHorizontal tableAlignment;
	private int tableColumnWidth;
	private IControlPanelProvider<ELEMENT_VALUE_TYPE> currentControlPanel;

	@SuppressWarnings({"unchecked", "rawtypes"})
	AttributeImpl(
		final String propertyName,
		final IValueRange valueRange,
		final Object defaultValue,
		final String label,
		final String labelLong,
		final DisplayFormat labelDisplayFormat,
		final String description,
		final boolean visible,
		final boolean mandatory,
		final boolean editable,
		final boolean readonly,
		final AlignmentHorizontal tableAlignment,
		final int tableColumnWidth,
		final IAttributeGroup attributeGroup,
		final boolean sortable,
		final boolean filterable,
		final Class<?> valueType,
		final Class<? extends ELEMENT_VALUE_TYPE> elementValueType,
		final Cardinality cardinality,
		final List<IControlPanelProvider<? extends ELEMENT_VALUE_TYPE>> controlPanels,
		final IDisplayFormat displayFormat) {

		Assert.paramNotEmpty(propertyName, "propertyName");
		Assert.paramNotNull(valueRange, "valueRange");
		Assert.paramNotEmpty(label, "label");
		Assert.paramNotNull(labelDisplayFormat, "labelDisplayFormat");
		Assert.paramNotNull(tableAlignment, "tableAlignment");
		Assert.paramNotNull(valueType, "valueType");
		Assert.paramNotNull(elementValueType, "elementValueType");
		Assert.paramNotNull(cardinality, "cardinality");
		Assert.paramNotNull(controlPanels, "controlPanels");
		Assert.paramNotNull(displayFormat, "displayFormat");

		if (Collections.class.isAssignableFrom(elementValueType)) {
			throw new IllegalArgumentException("The parameter 'elementValueType' must not be a collection but has the type '"
				+ elementValueType.getClass().getName()
				+ "'.");
		}
		if (!elementValueType.equals(valueType) && !Collection.class.isAssignableFrom(valueType)) {
			throw new IllegalArgumentException(
				"If the 'elementValueType' differs from the 'valueType', the value type must be a 'Collection'");
		}
		if (readonly && editable) {
			throw new IllegalArgumentException("The attribute must not be 'readonly' and 'editable'");
		}

		this.changeObservable = new ChangeObservable();

		this.propertyName = propertyName;
		this.valueRange = valueRange;
		this.defaultValue = defaultValue;
		this.label = label;
		this.labelLong = labelLong;
		this.labelDisplayFormat = labelDisplayFormat;
		this.description = description;
		this.visible = visible;
		this.mandatory = mandatory;
		this.editable = editable;
		this.readonly = readonly;
		this.tableAlignment = tableAlignment;
		this.tableColumnWidth = tableColumnWidth;
		this.attributeGroup = attributeGroup;
		this.sortable = sortable;
		this.filterable = filterable;
		this.valueType = valueType;
		this.elementValueType = (Class<ELEMENT_VALUE_TYPE>) elementValueType;
		this.cardinality = cardinality;
		this.controlPanels = new LinkedList(controlPanels);

		setDisplayFormatImpl(displayFormat);
	}

	@Override
	public String getPropertyName() {
		return propertyName;
	}

	@Override
	public IValueRange getValueRange() {
		return valueRange;
	}

	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}

	@Override
	public boolean isSortable() {
		return sortable;
	}

	@Override
	public boolean isFilterable() {
		return filterable;
	}

	@Override
	public AlignmentHorizontal getTableAlignment() {
		return tableAlignment;
	}

	@Override
	public int getTableWidth() {
		return tableColumnWidth;
	}

	@Override
	public boolean isMandatory() {
		return mandatory;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public boolean isEditable() {
		return editable;
	}

	@Override
	public boolean isReadonly() {
		return readonly;
	}

	@Override
	public IAttributeGroup getGroup() {
		return attributeGroup;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getLabelLong() {
		return labelLong;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Class<?> getValueType() {
		return valueType;
	}

	@Override
	public Class<ELEMENT_VALUE_TYPE> getElementValueType() {
		return elementValueType;
	}

	@Override
	public Cardinality getCardinality() {
		return cardinality;
	}

	@Override
	public boolean isCollectionType() {
		return Collection.class.isAssignableFrom(valueType);
	}

	@Override
	public IControlPanelProvider<ELEMENT_VALUE_TYPE> getCurrentControlPanel() {
		return currentControlPanel;
	}

	@Override
	public List<IControlPanelProvider<ELEMENT_VALUE_TYPE>> getControlPanels() {
		return controlPanels;
	}

	@Override
	public List<IFilterType> getSupportedFilterTypes() {
		final List<IFilterType> result = new LinkedList<IFilterType>();
		for (final IControlPanelProvider<ELEMENT_VALUE_TYPE> controlPanelProvider : controlPanels) {
			final IFilterSupport<?> filterSupport = controlPanelProvider.getFilterSupport();
			if (filterSupport != null) {
				for (final IFilterPanelProvider<?> filterPanelProvider : filterSupport.getFilterPanels()) {
					final IFilterType filterType = filterPanelProvider.getType();
					if (!result.contains(filterType)) {
						result.add(filterType);
					}
				}
			}
		}
		return result;
	}

	@Override
	public IFilterPanelProvider<?> getFilterPanelProvider(final IFilterType filterType) {
		for (final IControlPanelProvider<ELEMENT_VALUE_TYPE> controlPanelProvider : controlPanels) {
			final IFilterSupport<?> filterSupport = controlPanelProvider.getFilterSupport();
			if (filterSupport != null) {
				for (final IFilterPanelProvider<?> filterPanelProvider : filterSupport.getFilterPanels()) {
					if (filterType.equals(filterPanelProvider.getType())) {
						return filterPanelProvider;
					}
				}
			}
		}
		return null;
	}

	@Override
	public DisplayFormat getLabelDisplayFormat() {
		return labelDisplayFormat;
	}

	@Override
	public IDisplayFormat getDisplayFormat() {
		return currentControlPanel.getDisplayFormat();
	}

	@Override
	public void setVisible(final boolean visible) {
		if (this.visible != visible) {
			this.visible = visible;
			changeObservable.fireChangedEvent();
		}
	}

	@Override
	public void setLabelDisplayFormat(final DisplayFormat displayFormat) {
		this.labelDisplayFormat = displayFormat;
		changeObservable.fireChangedEvent();
	}

	@Override
	public void setDisplayFormat(final IDisplayFormat displayFormat) {
		setDisplayFormatImpl(displayFormat);
		changeObservable.fireChangedEvent();
	}

	@Override
	public void setTableAlignment(final AlignmentHorizontal alignment) {
		this.tableAlignment = alignment;
		changeObservable.fireChangedEvent();
	}

	@Override
	public void setTableWidth(final int width) {
		this.tableColumnWidth = width;
		changeObservable.fireChangedEvent();
	}

	@Override
	public IAttributeConfig getConfig() {
		return new AttributeConfigImpl(
			Boolean.valueOf(visible),
			labelDisplayFormat,
			getDisplayFormat(),
			tableAlignment,
			Integer.valueOf(tableColumnWidth));
	}

	@Override
	public void setConfig(final IAttributeConfig config) {
		Assert.paramNotNull(config, "config");
		if (config.isVisible() != null) {
			this.visible = config.isVisible();
		}
		if (config.getLabelDisplayFormat() != null) {
			this.labelDisplayFormat = config.getLabelDisplayFormat();
		}
		if (config.getDisplayFormat() != null) {
			setDisplayFormatImpl(config.getDisplayFormat());
		}
		if (config.getTableAlignment() != null) {
			this.tableAlignment = config.getTableAlignment();
		}
		if (config.getTableWidth() != null) {
			this.tableColumnWidth = config.getTableWidth();
		}
		changeObservable.fireChangedEvent();
	}

	@Override
	public void addChangeListener(final IChangeListener listener) {
		changeObservable.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(final IChangeListener listener) {
		changeObservable.removeChangeListener(listener);
	}

	private void setDisplayFormatImpl(final IDisplayFormat displayFormat) {
		Assert.paramNotNull(displayFormat, "displayFormat");
		final IControlPanelProvider<ELEMENT_VALUE_TYPE> controlPanel = findControlPanel(displayFormat);
		if (controlPanel == null) {
			throw new IllegalArgumentException("The displayFormat '" + displayFormat + "' is not a known display format.");
		}
		else {
			currentControlPanel = controlPanel;
		}
	}

	private IControlPanelProvider<ELEMENT_VALUE_TYPE> findControlPanel(final IDisplayFormat displayFormat) {
		for (final IControlPanelProvider<ELEMENT_VALUE_TYPE> controlPanelProvider : controlPanels) {
			if (controlPanelProvider.getDisplayFormat().getId().equals(displayFormat.getId())) {
				return controlPanelProvider;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "AttributeImpl [propertyName="
			+ propertyName
			+ ", label="
			+ label
			+ ", valueType="
			+ valueType
			+ ", elementValueType="
			+ elementValueType
			+ ", visible="
			+ visible
			+ ", getDisplayFormatId()="
			+ getDisplayFormat()
			+ "]";
	}

}
