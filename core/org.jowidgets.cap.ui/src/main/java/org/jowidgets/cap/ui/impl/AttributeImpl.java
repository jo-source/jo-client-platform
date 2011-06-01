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

import org.jowidgets.api.controler.IChangeListener;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeConfig;
import org.jowidgets.cap.ui.api.attribute.IAttributeGroup;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.tools.controler.ChangeObservable;
import org.jowidgets.util.Assert;


final class AttributeImpl<ELEMENT_VALUE_TYPE> implements IAttribute<ELEMENT_VALUE_TYPE> {

	private final ChangeObservable changeObservable;

	private final String propertyName;
	private final String label;
	private final String labelLong;
	private final String description;
	private final boolean mandatory;
	private final boolean editable;
	private final boolean readonly;
	private final IAttributeGroup attributeGroup;
	private final boolean sortable;
	private final boolean filterable;
	private final Class<?> valueType;
	private final Class<ELEMENT_VALUE_TYPE> elementValueType;
	private final IControlPanelProvider<ELEMENT_VALUE_TYPE> defaultControlPanel;
	private final List<IControlPanelProvider<ELEMENT_VALUE_TYPE>> controlPanels;

	private boolean visible;
	private AlignmentHorizontal tableAlignment;
	private int tableColumnWidth;
	private IControlPanelProvider<ELEMENT_VALUE_TYPE> currentControlPanel;

	@SuppressWarnings({"unchecked", "rawtypes"})
	AttributeImpl(
		final String propertyName,
		final String label,
		final String labelLong,
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
		final IControlPanelProvider<? extends ELEMENT_VALUE_TYPE> defaultControlPanel,
		final List<IControlPanelProvider<? extends ELEMENT_VALUE_TYPE>> controlPanels) {

		Assert.paramNotEmpty(propertyName, "propertyName");
		Assert.paramNotEmpty(label, "label");
		Assert.paramNotNull(tableAlignment, "tableAlignment");
		Assert.paramNotNull(valueType, "valueType");
		Assert.paramNotNull(elementValueType, "elementValueType");
		Assert.paramNotNull(defaultControlPanel, "defaultControlPanel");
		Assert.paramNotNull(controlPanels, "controlPanels");

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
		this.label = label;
		this.labelLong = labelLong;
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
		this.defaultControlPanel = (IControlPanelProvider<ELEMENT_VALUE_TYPE>) defaultControlPanel;
		this.currentControlPanel = this.defaultControlPanel;
		this.controlPanels = new LinkedList(controlPanels);

		if (!this.controlPanels.contains(defaultControlPanel)) {
			this.controlPanels.add((IControlPanelProvider<ELEMENT_VALUE_TYPE>) defaultControlPanel);
		}
	}

	@Override
	public String getPropertyName() {
		return propertyName;
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
	public boolean isCollectionType() {
		return Collection.class.isAssignableFrom(valueType);
	}

	@Override
	public IControlPanelProvider<ELEMENT_VALUE_TYPE> getDefaultControlPanel() {
		return defaultControlPanel;
	}

	@Override
	public List<IControlPanelProvider<ELEMENT_VALUE_TYPE>> getSupportedControlPanels() {
		return controlPanels;
	}

	@Override
	public String getDisplayFormatId() {
		return currentControlPanel.getDisplayFormatId();
	}

	@Override
	public void setVisible(final boolean visible) {
		this.visible = visible;
		changeObservable.fireChangedEvent();
	}

	@Override
	public void setDisplayFormatId(final String id) {
		setDisplayFormatToNewId(id);
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
			getDisplayFormatId(),
			tableAlignment,
			Integer.valueOf(tableColumnWidth));
	}

	@Override
	public void setConfig(final IAttributeConfig config) {
		Assert.paramNotNull(config, "config");
		if (config.isVisible() != null) {
			this.visible = config.isVisible();
		}
		if (config.getDisplayFormatId() != null) {
			setDisplayFormatToNewId(config.getDisplayFormatId());
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

	private void setDisplayFormatToNewId(final String id) {
		Assert.paramNotEmpty(id, "id");
		final IControlPanelProvider<ELEMENT_VALUE_TYPE> controlPanel = findControlPanel(id);
		if (controlPanel == null) {
			throw new IllegalArgumentException("The displayFormatId '" + id + "' is not a known display format.");
		}
		else {
			currentControlPanel = controlPanel;
		}
	}

	private IControlPanelProvider<ELEMENT_VALUE_TYPE> findControlPanel(final String displayFormatId) {
		for (final IControlPanelProvider<ELEMENT_VALUE_TYPE> controlPanelProvider : controlPanels) {
			if (controlPanelProvider.getDisplayFormatId().equals(displayFormatId)) {
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
			+ getDisplayFormatId()
			+ "]";
	}

}
