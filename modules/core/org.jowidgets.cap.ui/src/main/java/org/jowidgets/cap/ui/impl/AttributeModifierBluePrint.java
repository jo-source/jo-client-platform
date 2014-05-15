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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.Cardinality;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.ui.api.attribute.IAttributeBluePrint;
import org.jowidgets.cap.ui.api.attribute.IAttributeGroup;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProviderBluePrint;
import org.jowidgets.cap.ui.api.attribute.IGenericControlPanelProviderBluePrint;
import org.jowidgets.cap.ui.api.control.DisplayFormat;
import org.jowidgets.cap.ui.api.control.IDisplayFormat;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.tools.StaticMessage;
import org.jowidgets.util.Assert;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.maybe.Nothing;
import org.jowidgets.util.maybe.Some;
import org.jowidgets.validation.IValidator;

final class AttributeModifierBluePrint<ELEMENT_VALUE_TYPE> implements IAttributeBluePrint<ELEMENT_VALUE_TYPE> {

	private final List<IValidator<ELEMENT_VALUE_TYPE>> elementTypeValidators;
	private final List<IValidator<? extends Object>> validators;
	private final List<Class<?>> addedBeanValidators;

	private boolean exhausted;
	private IValueRange valueRange;
	private IMaybe<Object> defaultValue;
	private Cardinality cardinality;
	private boolean cardinalitySet;
	private IMessage label;
	private IMessage labelLong;
	private IMessage description;
	private DisplayFormat labelDisplayFormat;
	private Boolean visible;
	private Boolean mandatory;
	private Boolean editable;
	private Boolean readonly;
	private AlignmentHorizontal tableAlignment;
	private Integer tableColumnWidth;
	private IAttributeGroup attributeGroup;
	private Boolean sortable;
	private Boolean filterable;
	private Boolean searchable;
	private IDisplayFormat displayFormat;

	private boolean controlPanelsCleared = false;

	@SuppressWarnings("rawtypes")
	private final List controlPanels;

	@SuppressWarnings("rawtypes")
	AttributeModifierBluePrint() {
		this.elementTypeValidators = new LinkedList<IValidator<ELEMENT_VALUE_TYPE>>();
		this.validators = new LinkedList<IValidator<? extends Object>>();
		this.addedBeanValidators = new LinkedList<Class<?>>();
		this.controlPanels = new LinkedList();
		this.exhausted = false;
		this.cardinalitySet = false;
		this.defaultValue = Nothing.getInstance();
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setValueRange(final IValueRange valueRange) {
		Assert.paramNotNull(valueRange, "valueRange");
		checkExhausted();
		this.valueRange = valueRange;
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setValueRange(
		final boolean open,
		final Collection<? extends ELEMENT_VALUE_TYPE> values) {
		Assert.paramNotNull(values, "values");
		return setValueRange(CapCommonToolkit.staticValueRangeFactory().create(values, open));
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setValueRange(final boolean open, final ELEMENT_VALUE_TYPE... values) {
		Assert.paramNotNull(values, "values");
		return setValueRange(CapCommonToolkit.staticValueRangeFactory().create(open, values));
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setLookUpValueRange(final Object lookUpId) {
		return setValueRange(CapCommonToolkit.lookUpToolkit().lookUpValueRange(lookUpId));
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setValueRange(final Collection<? extends ELEMENT_VALUE_TYPE> values) {
		return setValueRange(false, values);
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setValueRange(final ELEMENT_VALUE_TYPE... values) {
		Assert.paramNotNull(values, "values");
		return setValueRange(Arrays.asList(values));
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setDefaultValue(final Object value) {
		this.defaultValue = new Some<Object>(value);
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setCardinality(final Cardinality cardinality) {
		checkExhausted();
		this.cardinality = cardinality;
		this.cardinalitySet = true;
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setLabel(final IMessage label) {
		Assert.paramNotNull(label, "label");
		checkExhausted();
		this.label = label;
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setLabel(final String label) {
		Assert.paramNotEmpty(label, "label");
		checkExhausted();
		this.label = new StaticMessage(label);
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setLabelLong(final IMessage labelLong) {
		Assert.paramNotNull(labelLong, "labelLong");
		checkExhausted();
		this.labelLong = labelLong;
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setLabelLong(final String labelLong) {
		checkExhausted();
		this.labelLong = new StaticMessage(labelLong);
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setDescription(final IMessage description) {
		Assert.paramNotNull(description, "description");
		checkExhausted();
		this.description = description;
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setDescription(final String description) {
		checkExhausted();
		this.description = new StaticMessage(description);
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setLabelDisplayFormat(final DisplayFormat displayFormat) {
		checkExhausted();
		this.labelDisplayFormat = displayFormat;
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

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setSearchable(final boolean searchable) {
		checkExhausted();
		this.searchable = Boolean.valueOf(searchable);
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> addValidator(final IValidator<? extends Object> validator) {
		checkExhausted();
		Assert.paramNotNull(validator, "validator");
		validators.add(validator);
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> addElementTypeValidator(final IValidator<ELEMENT_VALUE_TYPE> validator) {
		checkExhausted();
		Assert.paramNotNull(validator, "validator");
		elementTypeValidators.add(validator);
		return this;
	}

	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> addBeanValidator(final Class<?> beanType) {
		checkExhausted();
		Assert.paramNotNull(beanType, "beanType");
		addedBeanValidators.add(beanType);
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

	@SuppressWarnings("unchecked")
	@Override
	public IAttributeBluePrint<ELEMENT_VALUE_TYPE> setControlPanels(
		final Collection<? extends IControlPanelProvider<? extends ELEMENT_VALUE_TYPE>> controlPanels) {
		checkExhausted();
		this.controlPanels.clear();
		controlPanelsCleared = true;
		if (controlPanels != null) {
			this.controlPanels.addAll(controlPanels);
		}
		this.displayFormat = null;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IControlPanelProviderBluePrint<ELEMENT_VALUE_TYPE> addControlPanel(final IDisplayFormat displayFormat) {
		Assert.paramNotNull(displayFormat, "displayFormat");
		checkExhausted();
		final ControlPanelProviderBluePrintImpl<ELEMENT_VALUE_TYPE> result = new ControlPanelProviderBluePrintImpl<ELEMENT_VALUE_TYPE>(
			displayFormat);
		controlPanels.add(result);
		return result;
	}

	@Override
	public IControlPanelProviderBluePrint<ELEMENT_VALUE_TYPE> setControlPanel() {
		checkExhausted();
		this.controlPanels.clear();
		controlPanelsCleared = true;
		return addControlPanel(new DisplayFormatImpl(
			ControlPanelProviderBuilderImpl.DEFAULT_DISPLAY_FORMAT_ID,
			ControlPanelProviderBuilderImpl.DEFAULT_DISPLAY_NAME,
			null));
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

	@SuppressWarnings({"unchecked", "rawtypes"})
	void modify(final IAttributeBluePrint<ELEMENT_VALUE_TYPE> attributeBluePrint) {
		if (valueRange != null) {
			attributeBluePrint.setValueRange(valueRange);
		}
		if (defaultValue.isSomething()) {
			attributeBluePrint.setDefaultValue(defaultValue.getValue());
		}
		if (cardinalitySet) {
			attributeBluePrint.setCardinality(cardinality);
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
		if (searchable != null) {
			attributeBluePrint.setSearchable(searchable.booleanValue());
		}
		if (displayFormat != null) {
			attributeBluePrint.setDisplayFormat(displayFormat);
		}
		for (final IValidator<? extends Object> validator : validators) {
			attributeBluePrint.addValidator(validator);
		}
		for (final IValidator<ELEMENT_VALUE_TYPE> validator : elementTypeValidators) {
			attributeBluePrint.addElementTypeValidator(validator);
		}
		for (final Class<?> addedBeanValidator : addedBeanValidators) {
			attributeBluePrint.addBeanValidator(addedBeanValidator);
		}
		if (controlPanelsCleared) {
			final List emptyList = Collections.emptyList();
			attributeBluePrint.setControlPanels(emptyList);
		}
		for (final Object controlPanel : controlPanels) {
			if (controlPanel instanceof IControlPanelProvider) {
				attributeBluePrint.addControlPanel((IControlPanelProvider<? extends ELEMENT_VALUE_TYPE>) controlPanel);
			}
			else if (controlPanel instanceof ControlPanelProviderBluePrintImpl) {
				final ControlPanelProviderBluePrintImpl<?> bluePrint = (ControlPanelProviderBluePrintImpl<?>) controlPanel;
				final IControlPanelProviderBluePrint<ELEMENT_VALUE_TYPE> addedBluePrint = attributeBluePrint.addControlPanel(bluePrint.getDisplayFormat());
				//The following may seem to be wrong, but its correct!
				//The added blue print will be modified by this invocation and this is desired!
				bluePrint.modifyBluePrint((IGenericControlPanelProviderBluePrint) addedBluePrint);
			}
			else {
				throw new IllegalStateException("Control panel type '"
					+ controlPanel.getClass().getName()
					+ "' is not supported. This seems to be a bug.");
			}
		}
	}

}
