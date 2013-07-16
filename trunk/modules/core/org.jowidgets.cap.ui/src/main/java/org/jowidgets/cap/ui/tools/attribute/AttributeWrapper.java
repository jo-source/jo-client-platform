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

package org.jowidgets.cap.ui.tools.attribute;

import java.util.List;

import org.jowidgets.cap.common.api.bean.Cardinality;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.common.api.filter.IOperator;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeConfig;
import org.jowidgets.cap.ui.api.attribute.IAttributeGroup;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.control.DisplayFormat;
import org.jowidgets.cap.ui.api.control.IDisplayFormat;
import org.jowidgets.cap.ui.api.filter.IFilterPanelProvider;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.util.Assert;
import org.jowidgets.util.event.IChangeListener;
import org.jowidgets.validation.IValidator;

public class AttributeWrapper<ELEMENT_VALUE_TYPE> implements IAttribute<ELEMENT_VALUE_TYPE> {

	private final IAttribute<ELEMENT_VALUE_TYPE> original;

	public AttributeWrapper(final IAttribute<ELEMENT_VALUE_TYPE> original) {
		Assert.paramNotNull(original, "original");
		this.original = original;
	}

	@Override
	public void addChangeListener(final IChangeListener listener) {
		original.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(final IChangeListener listener) {
		original.removeChangeListener(listener);
	}

	@Override
	public String getPropertyName() {
		return original.getPropertyName();
	}

	@Override
	public IValueRange getValueRange() {
		return original.getValueRange();
	}

	@Override
	public Object getDefaultValue() {
		return original.getDefaultValue();
	}

	@Override
	public boolean isSortable() {
		return original.isSortable();
	}

	@Override
	public boolean isFilterable() {
		return original.isFilterable();
	}

	@Override
	public boolean isSearchable() {
		return original.isSearchable();
	}

	@Override
	public boolean isMandatory() {
		return original.isMandatory();
	}

	@Override
	public boolean isEditable() {
		return original.isEditable();
	}

	@Override
	public boolean isReadonly() {
		return original.isReadonly();
	}

	@Override
	public IAttributeGroup getGroup() {
		return original.getGroup();
	}

	@Override
	public IMessage getLabel() {
		return original.getLabel();
	}

	@Override
	public IMessage getLabelLong() {
		return original.getLabelLong();
	}

	@Override
	public String getCurrentLabel() {
		return original.getCurrentLabel();
	}

	@Override
	public String getValueAsString(final Object value) {
		return original.getValueAsString(value);
	}

	@Override
	public IMessage getDescription() {
		return original.getDescription();
	}

	@Override
	public DisplayFormat getLabelDisplayFormat() {
		return original.getLabelDisplayFormat();
	}

	@Override
	public Class<?> getValueType() {
		return original.getValueType();
	}

	@Override
	public Class<ELEMENT_VALUE_TYPE> getElementValueType() {
		return original.getElementValueType();
	}

	@Override
	public IValidator<Object> getValidator() {
		return original.getValidator();
	}

	@Override
	public Cardinality getCardinality() {
		return original.getCardinality();
	}

	@Override
	public boolean isCollectionType() {
		return original.isCollectionType();
	}

	@Override
	public List<IControlPanelProvider<ELEMENT_VALUE_TYPE>> getControlPanels() {
		return original.getControlPanels();
	}

	@Override
	public List<IFilterType> getSupportedFilterTypes() {
		return original.getSupportedFilterTypes();
	}

	@Override
	public IFilterPanelProvider<IOperator> getFilterPanelProvider(final IFilterType filterType) {
		return original.getFilterPanelProvider(filterType);
	}

	@Override
	public IControlPanelProvider<ELEMENT_VALUE_TYPE> getCurrentFilterControlPanel(final IFilterType filterType) {
		return original.getCurrentFilterControlPanel(filterType);
	}

	@Override
	public IControlPanelProvider<ELEMENT_VALUE_TYPE> getCurrentIncludingFilterControlPanel(final IFilterType filterType) {
		return original.getCurrentIncludingFilterControlPanel(filterType);
	}

	@Override
	public IControlPanelProvider<ELEMENT_VALUE_TYPE> getCurrentIncludingFilterControlPanel() {
		return original.getCurrentIncludingFilterControlPanel();
	}

	@Override
	public boolean isVisible() {
		return original.isVisible();
	}

	@Override
	public IDisplayFormat getDisplayFormat() {
		return original.getDisplayFormat();
	}

	@Override
	public AlignmentHorizontal getTableAlignment() {
		return original.getTableAlignment();
	}

	@Override
	public IControlPanelProvider<ELEMENT_VALUE_TYPE> getCurrentControlPanel() {
		return original.getCurrentControlPanel();
	}

	@Override
	public int getTableWidth() {
		return original.getTableWidth();
	}

	@Override
	public void setVisible(final boolean visible) {
		original.setVisible(visible);
	}

	@Override
	public void setLabelDisplayFormat(final DisplayFormat displayFormat) {
		original.setLabelDisplayFormat(displayFormat);
	}

	@Override
	public void setDisplayFormat(final IDisplayFormat displayFormat) {
		original.setDisplayFormat(displayFormat);
	}

	@Override
	public void setTableAlignment(final AlignmentHorizontal alignment) {
		original.setTableAlignment(alignment);
	}

	@Override
	public void setTableWidth(final int width) {
		original.setTableWidth(width);
	}

	@Override
	public IAttributeConfig getConfig() {
		return original.getConfig();
	}

	@Override
	public void setConfig(final IAttributeConfig config) {
		original.setConfig(config);
	}

}
