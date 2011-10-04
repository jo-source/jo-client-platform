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

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.ui.api.attribute.IAttributeBluePrint;
import org.jowidgets.cap.ui.api.attribute.IAttributeGroup;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.control.DisplayFormat;
import org.jowidgets.cap.ui.api.control.IDisplayFormat;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.util.Assert;

final class AttributeModifierBluePrint<ELEMENT_VALUE_TYPE> implements IAttributeBluePrint<ELEMENT_VALUE_TYPE> {

	private boolean exhausted;
	private IValueRange valueRange;
	private String label;
	private String labelLong;
	private DisplayFormat labelDisplayFormat;
	private String description;
	private Boolean visible;
	private Boolean mandatory;
	private Boolean editable;
	private Boolean readonly;
	private AlignmentHorizontal tableAlignment;
	private Integer tableColumnWidth;
	private IAttributeGroup attributeGroup;
	private Boolean sortable;
	private Boolean filterable;
	private IDisplayFormat displayFormat;

	@SuppressWarnings("rawtypes")
	private final List controlPanels;

	@SuppressWarnings("rawtypes")
	AttributeModifierBluePrint() {
		this.controlPanels = new LinkedList();
		this.exhausted = false;
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
		return setValueRange(CapCommonToolkit.staticValueRangeFactory().create(values, open));
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setValueRange(final Collection<? extends ELEMENT_VALUE_TYPE> values) {
		return setValueRange(values, false);
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setLabel(final String label) {
		Assert.paramNotEmpty(label, "label");
		checkExhausted();
		this.label = label;
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setLabelLong(final String labelLong) {
		checkExhausted();
		this.labelLong = labelLong;
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setLabelDisplayFormat(final DisplayFormat displayFormat) {
		checkExhausted();
		this.labelDisplayFormat = displayFormat;
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setDescription(final String description) {
		checkExhausted();
		this.description = description;
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setVisible(final boolean visible) {
		checkExhausted();
		this.visible = Boolean.valueOf(visible);
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setMandatory(final boolean mandatory) {
		checkExhausted();
		this.mandatory = Boolean.valueOf(mandatory);
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setEditable(final boolean editable) {
		checkExhausted();
		this.editable = Boolean.valueOf(editable);
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setReadonly(final boolean readonly) {
		checkExhausted();
		this.readonly = Boolean.valueOf(readonly);
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setTableAlignment(final AlignmentHorizontal tableAlignment) {
		Assert.paramNotNull(tableAlignment, "tableAlignment");
		checkExhausted();
		this.tableAlignment = tableAlignment;
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setTableColumnWidth(final int tableColumnWidth) {
		checkExhausted();
		this.tableColumnWidth = Integer.valueOf(tableColumnWidth);
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setGroup(final IAttributeGroup group) {
		checkExhausted();
		this.attributeGroup = group;
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setSortable(final boolean sortable) {
		checkExhausted();
		this.sortable = Boolean.valueOf(sortable);
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setFilterable(final boolean filterable) {
		checkExhausted();
		this.filterable = Boolean.valueOf(filterable);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> addControlPanel(
		final IControlPanelProvider<? extends ELEMENT_VALUE_TYPE> controlPanel) {
		Assert.paramNotNull(controlPanel, "controlPanel");
		checkExhausted();
		this.controlPanels.add(controlPanel);
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setDisplayFormat(final IDisplayFormat displayFormat) {
		checkExhausted();
		this.displayFormat = displayFormat;
		return this;
	}

	private void checkExhausted() {
		if (exhausted) {
			throw new IllegalStateException("This blueprint is exhausted and can not be used for any further modfications.");
		}
	}

	void setExhausted() {
		this.exhausted = true;
	}

	@SuppressWarnings("unchecked")
	void modify(final IAttributeBluePrint<ELEMENT_VALUE_TYPE> attributeBluePrint) {
		if (valueRange != null) {
			attributeBluePrint.setValueRange(valueRange);
		}
		if (label != null) {
			attributeBluePrint.setLabel(label);
		}
		if (labelLong != null) {
			attributeBluePrint.setLabelLong(labelLong);
		}
		if (labelDisplayFormat != null) {
			attributeBluePrint.setLabelDisplayFormat(labelDisplayFormat);
		}
		if (description != null) {
			attributeBluePrint.setDescription(description);
		}
		if (visible != null) {
			attributeBluePrint.setVisible(visible.booleanValue());
		}
		if (mandatory != null) {
			attributeBluePrint.setMandatory(mandatory.booleanValue());
		}
		if (editable != null) {
			attributeBluePrint.setEditable(editable.booleanValue());
		}
		if (readonly != null) {
			attributeBluePrint.setReadonly(readonly.booleanValue());
		}
		if (tableAlignment != null) {
			attributeBluePrint.setTableAlignment(tableAlignment);
		}
		if (tableColumnWidth != null) {
			attributeBluePrint.setTableColumnWidth(tableColumnWidth.intValue());
		}
		if (attributeGroup != null) {
			attributeBluePrint.setGroup(attributeGroup);
		}
		if (sortable != null) {
			attributeBluePrint.setSortable(sortable.booleanValue());
		}
		if (filterable != null) {
			attributeBluePrint.setFilterable(filterable.booleanValue());
		}
		if (displayFormat != null) {
			attributeBluePrint.setDisplayFormat(displayFormat);
		}
		for (final Object controlPanelProvider : controlPanels) {
			attributeBluePrint.addControlPanel((IControlPanelProvider<? extends ELEMENT_VALUE_TYPE>) controlPanelProvider);
		}
	}

}
