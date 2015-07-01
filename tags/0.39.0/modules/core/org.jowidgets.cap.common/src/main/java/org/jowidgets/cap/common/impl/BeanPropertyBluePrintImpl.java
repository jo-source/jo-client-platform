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

import java.util.Collection;

import org.jowidgets.cap.common.api.bean.Cardinality;
import org.jowidgets.cap.common.api.bean.IBeanPropertyBluePrint;
import org.jowidgets.cap.common.api.bean.IBeanPropertyBuilder;
import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.validation.IValidator;

final class BeanPropertyBluePrintImpl implements IBeanPropertyBluePrint {

	private final IBeanPropertyBuilder builder;

	private boolean exhausted;

	BeanPropertyBluePrintImpl(final Class<?> beanType, final String propertyName) {
		this.builder = new BeanPropertyBuilderImpl(beanType, propertyName);
		this.exhausted = false;
	}

	@Override
	public IBeanPropertyBluePrint setLabel(final IMessage labelDefault) {
		checkExhausted();
		builder.setLabel(labelDefault);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setLabel(final String labelDefault) {
		checkExhausted();
		builder.setLabel(labelDefault);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setLabelLong(final IMessage labelLongDefault) {
		checkExhausted();
		builder.setLabelLong(labelLongDefault);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setLabelLong(final String labelLongDefault) {
		checkExhausted();
		builder.setLabelLong(labelLongDefault);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setDescription(final IMessage descriptionDefault) {
		checkExhausted();
		builder.setDescription(descriptionDefault);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setDescription(final String descriptionDefault) {
		checkExhausted();
		builder.setDescription(descriptionDefault);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setValueRange(final IValueRange valueRange) {
		checkExhausted();
		builder.setValueRange(valueRange);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setValueRange(final boolean open, final Collection<? extends Object> values) {
		checkExhausted();
		builder.setValueRange(open, values);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setValueRange(final Collection<? extends Object> values) {
		checkExhausted();
		builder.setValueRange(values);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setValueRange(final boolean open, final Object... values) {
		checkExhausted();
		builder.setValueRange(open, values);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setValueRange(final Object... values) {
		checkExhausted();
		builder.setValueRange(values);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setLookUpValueRange(final Object lookUpId) {
		checkExhausted();
		builder.setLookUpValueRange(lookUpId);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setDefaultValue(final Object value) {
		checkExhausted();
		builder.setDefaultValue(value);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setCardinality(final Cardinality cardinality) {
		checkExhausted();
		builder.setCardinality(cardinality);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setVisible(final boolean visibleDefault) {
		checkExhausted();
		builder.setVisible(visibleDefault);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setMandatory(final boolean mandatoryDefault) {
		checkExhausted();
		builder.setMandatory(mandatoryDefault);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setMandatoryValidator(final IValidator<? extends Object> validator) {
		checkExhausted();
		builder.setMandatoryValidator(validator);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setEditable(final boolean editable) {
		checkExhausted();
		builder.setEditable(editable);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setBatchEditable(final boolean editable) {
		checkExhausted();
		builder.setBatchEditable(editable);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setElementValueType(final Class<?> elementValueType) {
		checkExhausted();
		builder.setElementValueType(elementValueType);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint addValidator(final IValidator<? extends Object> validator) {
		checkExhausted();
		builder.addValidator(validator);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint addElementTypeValidator(final IValidator<? extends Object> validator) {
		checkExhausted();
		builder.addElementTypeValidator(validator);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setSortable(final boolean sortable) {
		checkExhausted();
		builder.setSortable(sortable);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setFilterable(final boolean filterable) {
		checkExhausted();
		builder.setFilterable(filterable);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint setSearchable(final boolean searchable) {
		checkExhausted();
		builder.setSearchable(searchable);
		return this;
	}

	IProperty build() {
		checkExhausted();
		this.exhausted = true;
		return builder.build();
	}

	private void checkExhausted() {
		if (exhausted) {
			throw new IllegalStateException("Builder is exhausted");
		}
	}

}
