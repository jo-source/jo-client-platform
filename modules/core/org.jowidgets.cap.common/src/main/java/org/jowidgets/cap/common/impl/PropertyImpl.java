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

import java.io.Serializable;
import java.util.Collection;

import org.jowidgets.cap.common.api.bean.Cardinality;
import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.tools.StaticMessage;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidator;

final class PropertyImpl implements IProperty, Serializable {

	private static final long serialVersionUID = -6994592225239239349L;

	private final String name;
	private final IValueRange valueRange;
	private final IValidator<Object> validator;
	private final Object defaultValue;
	private final IMessage labelDefault;
	private final IMessage labelLongDefault;
	private final IMessage descriptionDefault;
	private final boolean visibleDefault;
	private final boolean mandatoryDefault;
	private final Class<?> valueType;
	private final Class<?> elementValueType;
	private final Cardinality cardinality;
	private final boolean readonly;
	private final boolean editable;
	private final boolean sortable;
	private final boolean filterable;
	private final boolean searchable;

	PropertyImpl(
		final String name,
		final IValueRange valueRange,
		final Object defaultValue,
		final IMessage labelDefault,
		final IMessage labelLongDefault,
		final IMessage descriptionDefault,
		final boolean visibleDefault,
		final boolean mandatoryDefault,
		final Class<?> valueType,
		final Class<?> elementValueType,
		final IValidator<Object> validator,
		final Cardinality cardinality,
		final boolean readonly,
		final boolean editable,
		final boolean sortable,
		final boolean filterable,
		final boolean searchable) {

		Assert.paramNotEmpty(name, "name");
		Assert.paramNotNull(valueRange, "valueRange");
		Assert.paramNotNull(labelDefault, "labelDefault");
		Assert.paramNotNull(valueType, "valueType");
		Assert.paramNotNull(elementValueType, "elementValueType");
		Assert.paramNotNull(cardinality, "cardinality");

		if (Collection.class.isAssignableFrom(elementValueType)) {
			throw new IllegalArgumentException("The element type must not be a collection");
		}

		if (!filterable && searchable) {
			throw new IllegalArgumentException("A property that is not filterable could not be searchable");
		}

		this.name = name;
		this.valueRange = valueRange;
		this.defaultValue = defaultValue;
		this.labelDefault = labelDefault;
		if (labelLongDefault != null) {
			this.labelLongDefault = labelLongDefault;
		}
		else {
			this.labelLongDefault = new StaticMessage();
		}
		if (descriptionDefault != null) {
			this.descriptionDefault = descriptionDefault;
		}
		else {
			this.descriptionDefault = new StaticMessage();
		}
		this.visibleDefault = visibleDefault;
		this.mandatoryDefault = mandatoryDefault;
		this.valueType = valueType;
		this.elementValueType = elementValueType;
		this.validator = validator;
		this.cardinality = cardinality;
		this.readonly = readonly;
		this.editable = editable;
		this.sortable = sortable;
		this.filterable = filterable;
		this.searchable = searchable;
	}

	@Override
	public String getName() {
		return name;
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
	public IMessage getLabelDefault() {
		return labelDefault;
	}

	@Override
	public IMessage getLabelLongDefault() {
		return labelLongDefault;
	}

	@Override
	public IMessage getDescriptionDefault() {
		return descriptionDefault;
	}

	@Override
	public boolean isVisibleDefault() {
		return visibleDefault;
	}

	@Override
	public boolean isMandatoryDefault() {
		return mandatoryDefault;
	}

	@Override
	public Class<?> getValueType() {
		return valueType;
	}

	@Override
	public Class<?> getElementValueType() {
		return elementValueType;
	}

	@Override
	public IValidator<Object> getValidator() {
		return validator;
	}

	@Override
	public Cardinality getCardinality() {
		return cardinality;
	}

	@Override
	public boolean isReadonly() {
		return readonly;
	}

	@Override
	public boolean isEditable() {
		return editable;
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
	public boolean isSearchable() {
		return searchable;
	}

	@Override
	public String toString() {
		return "PropertyImpl [name="
			+ name
			+ ", valueRange="
			+ valueRange
			+ ", defaultValue="
			+ defaultValue
			+ ", labelDefault="
			+ labelDefault
			+ ", valueType="
			+ valueType
			+ ", elementValueType="
			+ elementValueType
			+ ", cardinality="
			+ cardinality
			+ ", readonly="
			+ readonly
			+ ", sortable="
			+ sortable
			+ ", filterable="
			+ filterable
			+ "]";
	}

}
