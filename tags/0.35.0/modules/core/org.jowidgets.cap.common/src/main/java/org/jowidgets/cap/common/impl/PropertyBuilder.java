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

package org.jowidgets.cap.common.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.Cardinality;
import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.bean.IPropertyBuilder;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.tools.StaticMessage;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.tools.MandatoryValidator;
import org.jowidgets.validation.tools.MandatoryValidator.MandatoryMessageType;

final class PropertyBuilder implements IPropertyBuilder {

	private final PropertyValidatorBuilder validatorBuilder;
	private final List<Class<?>> addedBeanValidators;

	private String name;
	private IValueRange valueRange;
	private Object defaultValue;
	private Cardinality cardinality;
	private IMessage labelDefault;
	private IMessage labelLongDefault;
	private IMessage descriptionDefault;
	private boolean visibleDefault;
	private boolean mandatoryDefault;
	private IValidator<? extends Object> mandatoryValidator;
	private Class<?> valueType;
	private Class<?> elementValueType;
	private boolean readonly;
	private Boolean editable;
	private Boolean batchEditable;
	private boolean sortable;
	private boolean filterable;
	private Boolean searchable;
	private boolean beanValidatorAdded;

	PropertyBuilder() {
		this.visibleDefault = true;
		this.mandatoryDefault = false;
		this.readonly = false;
		this.sortable = true;
		this.filterable = true;
		this.valueRange = new StaticValueRangeImpl(Collections.emptyList(), true);
		this.validatorBuilder = new PropertyValidatorBuilder();
		this.addedBeanValidators = new LinkedList<Class<?>>();
		this.beanValidatorAdded = false;
		this.mandatoryValidator = new MandatoryValidator<Object>(MandatoryMessageType.INFO_ERROR);
	}

	@Override
	public IPropertyBuilder setName(final String name) {
		Assert.paramNotEmpty(name, "name");
		this.name = name;
		return this;
	}

	@Override
	public IPropertyBuilder setValueRange(final IValueRange valueRange) {
		Assert.paramNotNull(valueRange, "valueRange");
		this.valueRange = valueRange;
		return this;
	}

	@Override
	public IPropertyBuilder setValueRange(final boolean open, final Collection<? extends Object> values) {
		return setValueRange(new StaticValueRangeImpl(values, open));
	}

	@Override
	public IPropertyBuilder setValueRange(final Collection<? extends Object> values) {
		return setValueRange(false, values);
	}

	@Override
	public IPropertyBuilder setValueRange(final boolean open, final Object... values) {
		return setValueRange(open, Arrays.asList(values));
	}

	@Override
	public IPropertyBuilder setValueRange(final Object... values) {
		return setValueRange(Arrays.asList(values));
	}

	@Override
	public IPropertyBuilder setLookUpValueRange(final Object lookUpId) {
		return setValueRange(CapCommonToolkit.lookUpToolkit().lookUpValueRange(lookUpId));
	}

	@Override
	public IPropertyBuilder setDefaultValue(final Object value) {
		this.defaultValue = value;
		return this;
	}

	@Override
	public IPropertyBuilder setCardinality(final Cardinality cardinality) {
		this.cardinality = cardinality;
		return this;
	}

	@Override
	public IPropertyBuilder setLabel(final IMessage labelDefault) {
		this.labelDefault = labelDefault;
		return this;
	}

	@Override
	public IPropertyBuilder setLabel(final String labelDefault) {
		this.labelDefault = new StaticMessage(labelDefault);
		return this;
	}

	@Override
	public IPropertyBuilder setLabelLong(final IMessage labelLongDefault) {
		this.labelLongDefault = labelLongDefault;
		return this;
	}

	@Override
	public IPropertyBuilder setLabelLong(final String labelLongDefault) {
		this.labelLongDefault = new StaticMessage(labelLongDefault);
		return this;
	}

	@Override
	public IPropertyBuilder setDescription(final IMessage descriptionDefault) {
		this.descriptionDefault = descriptionDefault;
		return this;
	}

	@Override
	public IPropertyBuilder setDescription(final String descriptionDefault) {
		this.descriptionDefault = new StaticMessage(descriptionDefault);
		return this;
	}

	@Override
	public IPropertyBuilder setVisible(final boolean visibleDefault) {
		this.visibleDefault = visibleDefault;
		return this;
	}

	@Override
	public IPropertyBuilder setMandatory(final boolean mandatoryDefault) {
		this.mandatoryDefault = mandatoryDefault;
		return this;
	}

	@Override
	public IPropertyBuilder setMandatoryValidator(final IValidator<? extends Object> validator) {
		this.mandatoryValidator = validator;
		return this;
	}

	@Override
	public IPropertyBuilder setValueType(final Class<?> valueType) {
		this.valueType = valueType;
		return this;
	}

	@Override
	public IPropertyBuilder setElementValueType(final Class<?> elementValueType) {
		this.elementValueType = elementValueType;
		return this;
	}

	@Override
	public IPropertyBuilder addValidator(final IValidator<? extends Object> validator) {
		validatorBuilder.addValidator(validator);
		return this;
	}

	@Override
	public IPropertyBuilder addElementTypeValidator(final IValidator<? extends Object> validator) {
		validatorBuilder.addElementTypeValidator(validator);
		return this;
	}

	@Override
	public IPropertyBuilder addBeanValidator(final Class<?> beanType) {
		Assert.paramNotNull(beanType, "beanType");
		addedBeanValidators.add(beanType);
		this.beanValidatorAdded = true;
		return this;
	}

	@Override
	public IPropertyBuilder setReadonly(final boolean readonly) {
		this.readonly = readonly;
		return this;
	}

	@Override
	public IPropertyBuilder setEditable(final boolean editable) {
		this.editable = Boolean.valueOf(editable);
		return this;
	}

	@Override
	public IPropertyBuilder setBatchEditable(final boolean editable) {
		this.batchEditable = Boolean.valueOf(editable);
		return this;
	}

	@Override
	public IPropertyBuilder setSortable(final boolean sortable) {
		this.sortable = sortable;
		return this;
	}

	@Override
	public IPropertyBuilder setFilterable(final boolean filterable) {
		this.filterable = filterable;
		return this;
	}

	@Override
	public IPropertyBuilder setSearchable(final boolean searchable) {
		this.searchable = Boolean.valueOf(searchable);
		return this;
	}

	boolean isBeanValidatorAdded() {
		return beanValidatorAdded;
	}

	private IMessage getLabelDefault() {
		if (labelDefault == null) {
			return new StaticMessage(name);
		}
		else {
			return labelDefault;
		}
	}

	private Class<?> getElementValueType() {
		if (elementValueType != null) {
			return elementValueType;
		}
		else if (valueType != null && !Collection.class.isAssignableFrom(valueType)) {
			return valueType;
		}
		else {
			return null;
		}
	}

	private Cardinality getCardinality() {
		if (cardinality != null) {
			return cardinality;
		}
		else if (valueType != null && Collection.class.isAssignableFrom(valueType)) {
			return Cardinality.GREATER_OR_EQUAL_ZERO;
		}
		else {
			return Cardinality.LESS_OR_EQUAL_ONE;
		}
	}

	private boolean getSearchable() {
		if (searchable != null) {
			return searchable.booleanValue();
		}
		else {
			return filterable;
		}
	}

	private boolean getBatchEditable() {
		if (batchEditable != null) {
			return batchEditable.booleanValue();
		}
		else if (editable != null) {
			return editable.booleanValue();
		}
		else {
			return readonly;
		}
	}

	@Override
	public IProperty build() {
		if (mandatoryDefault && mandatoryValidator != null) {
			validatorBuilder.addValidator(mandatoryValidator);
		}

		for (final Class<?> beanValidatorClass : addedBeanValidators) {
			validatorBuilder.addBeanValidator(beanValidatorClass, name);
		}
		addedBeanValidators.clear();

		return new PropertyImpl(
			name,
			valueRange,
			defaultValue,
			getLabelDefault(),
			labelLongDefault,
			descriptionDefault,
			visibleDefault,
			mandatoryDefault,
			valueType,
			getElementValueType(),
			validatorBuilder.build(valueType, true),
			getCardinality(),
			readonly,
			editable != null ? editable.booleanValue() : !readonly,
			getBatchEditable(),
			sortable,
			filterable,
			getSearchable());
	}
}
