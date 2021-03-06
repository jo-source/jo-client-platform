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

public interface IBeanPropertyBluePrint {

	IBeanPropertyBluePrint setLabel(IMessage labelDefault);

	IBeanPropertyBluePrint setLabel(String labelDefault);

	IBeanPropertyBluePrint setLabelLong(IMessage labelLongDefault);

	IBeanPropertyBluePrint setLabelLong(String labelLongDefault);

	IBeanPropertyBluePrint setDescription(IMessage descriptionDefault);

	IBeanPropertyBluePrint setDescription(String descriptionDefault);

	IBeanPropertyBluePrint setValueRange(IValueRange valueRange);

	IBeanPropertyBluePrint setValueRange(boolean open, Collection<? extends Object> values);

	IBeanPropertyBluePrint setValueRange(Collection<? extends Object> values);

	IBeanPropertyBluePrint setValueRange(boolean open, Object... values);

	IBeanPropertyBluePrint setValueRange(Object... values);

	IBeanPropertyBluePrint setValueType(Class<?> valueType);

	IBeanPropertyBluePrint setLookUpValueRange(Object lookUpId);

	IBeanPropertyBluePrint setDefaultValue(Object value);

	IBeanPropertyBluePrint setCardinality(Cardinality cardinality);

	IBeanPropertyBluePrint setVisible(boolean visibleDefault);

	IBeanPropertyBluePrint setMandatory(boolean mandatoryDefault);

	IBeanPropertyBluePrint setMandatoryValidator(IValidator<? extends Object> validator);

	IBeanPropertyBluePrint setEditable(boolean editable);

	IBeanPropertyBluePrint setBatchEditable(boolean editable);

	IBeanPropertyBluePrint setElementValueType(Class<?> elementValueType);

	IBeanPropertyBluePrint addValidator(IValidator<? extends Object> validator);

	IBeanPropertyBluePrint addElementTypeValidator(IValidator<? extends Object> validator);

	IBeanPropertyBluePrint setSortable(boolean sortable);

	IBeanPropertyBluePrint setFilterable(boolean filterable);

	IBeanPropertyBluePrint setSearchable(boolean searchable);

}
