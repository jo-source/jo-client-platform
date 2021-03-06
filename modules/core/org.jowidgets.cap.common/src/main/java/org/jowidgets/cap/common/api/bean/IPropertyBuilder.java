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

package org.jowidgets.cap.common.api.bean;

import java.util.Collection;

import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.validation.IValidator;

public interface IPropertyBuilder {

	IPropertyBuilder setName(String name);

	IPropertyBuilder setLabel(String labelDefault);

	IPropertyBuilder setLabel(IMessage labelDefault);

	IPropertyBuilder setLabelLong(String labelLongDefault);

	IPropertyBuilder setLabelLong(IMessage labelLongDefault);

	IPropertyBuilder setDescription(String descriptionDefault);

	IPropertyBuilder setDescription(IMessage descriptionDefault);

	IPropertyBuilder setVisible(boolean visibleDefault);

	IPropertyBuilder setMandatory(boolean mandatoryDefault);

	IPropertyBuilder setMandatoryValidator(IValidator<? extends Object> validator);

	IPropertyBuilder setValueType(Class<?> valueType);

	IPropertyBuilder setElementValueType(Class<?> elementValueType);

	IPropertyBuilder addValidator(IValidator<? extends Object> validator);

	IPropertyBuilder addElementTypeValidator(IValidator<? extends Object> validator);

	/**
	 * Adds a bean property validator using bean validation (jsr 303)
	 * 
	 * @param beanType The type of the bean
	 * 
	 * @return This builder
	 */
	IPropertyBuilder addBeanValidator(Class<?> beanType);

	IPropertyBuilder setCardinality(Cardinality cardinality);

	IPropertyBuilder setReadonly(boolean readonly);

	IPropertyBuilder setEditable(boolean editable);

	IPropertyBuilder setBatchEditable(boolean editable);

	IPropertyBuilder setValueRange(IValueRange valueRange);

	IPropertyBuilder setValueRange(boolean open, Collection<? extends Object> values);

	IPropertyBuilder setValueRange(Collection<? extends Object> values);

	IPropertyBuilder setValueRange(boolean open, Object... values);

	IPropertyBuilder setValueRange(Object... values);

	IPropertyBuilder setLookUpValueRange(Object lookUpId);

	IPropertyBuilder setDefaultValue(Object value);

	IPropertyBuilder setSortable(boolean sortable);

	IPropertyBuilder setFilterable(boolean filterable);

	IPropertyBuilder setSearchable(boolean searchable);

	IProperty build();

}
