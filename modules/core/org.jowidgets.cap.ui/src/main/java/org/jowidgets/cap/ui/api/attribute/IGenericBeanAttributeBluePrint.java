/*
 * Copyright (c) 2014, grossmann
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

package org.jowidgets.cap.ui.api.attribute;

import java.util.Collection;

import org.jowidgets.cap.common.api.bean.Cardinality;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.ui.api.control.DisplayFormat;
import org.jowidgets.cap.ui.api.control.IDisplayFormat;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.validation.IValidator;

public interface IGenericBeanAttributeBluePrint<ELEMENT_VALUE_TYPE, BLUE_PRINT_TYPE> {

	BLUE_PRINT_TYPE setValueRange(boolean open, Collection<? extends ELEMENT_VALUE_TYPE> values);

	BLUE_PRINT_TYPE setValueRange(boolean open, ELEMENT_VALUE_TYPE... values);

	BLUE_PRINT_TYPE setLookUpValueRange(Object lookUpId);

	BLUE_PRINT_TYPE setElementValueType(Class<? extends ELEMENT_VALUE_TYPE> elementValueType);

	BLUE_PRINT_TYPE setLabel(IMessage label);

	BLUE_PRINT_TYPE setLabelLong(IMessage labelLong);

	BLUE_PRINT_TYPE setDescription(IMessage description);

	BLUE_PRINT_TYPE setLabel(String label);

	BLUE_PRINT_TYPE setLabelLong(String labelLong);

	BLUE_PRINT_TYPE setDescription(String description);

	BLUE_PRINT_TYPE setValueRange(IValueRange valueRange);

	BLUE_PRINT_TYPE setValueRange(Collection<? extends ELEMENT_VALUE_TYPE> values, boolean open);

	BLUE_PRINT_TYPE setValueRange(ELEMENT_VALUE_TYPE... values);

	BLUE_PRINT_TYPE setValueRange(Collection<? extends ELEMENT_VALUE_TYPE> values);

	BLUE_PRINT_TYPE setDefaultValue(Object value);

	BLUE_PRINT_TYPE setCardinality(Cardinality cardinality);

	BLUE_PRINT_TYPE setVisible(boolean visible);

	BLUE_PRINT_TYPE setEditable(boolean editable);

	BLUE_PRINT_TYPE setBatchEditable(boolean editable);

	BLUE_PRINT_TYPE setReadonly(boolean readonly);

	BLUE_PRINT_TYPE setMandatory(boolean mandatory);

	BLUE_PRINT_TYPE setTableAlignment(AlignmentHorizontal alignment);

	BLUE_PRINT_TYPE setTableColumnWidth(int width);

	BLUE_PRINT_TYPE setGroup(IAttributeGroup group);

	BLUE_PRINT_TYPE setSortable(boolean sortable);

	BLUE_PRINT_TYPE setFilterable(boolean filterable);

	BLUE_PRINT_TYPE setSearchable(boolean searchable);

	BLUE_PRINT_TYPE setLabelDisplayFormat(DisplayFormat displayFormat);

	BLUE_PRINT_TYPE addValidator(IValidator<? extends Object> validator);

	BLUE_PRINT_TYPE addElementTypeValidator(IValidator<ELEMENT_VALUE_TYPE> validator);

	/**
	 * Adds a bean property validator using bean validation (jsr 303)
	 * 
	 * @param beanType The type of the bean
	 * 
	 * @return This builder
	 */
	BLUE_PRINT_TYPE addBeanValidator(Class<?> beanType);

	/**
	 * Sets the display format.
	 * 
	 * If the display format is not set, the first added control panels id will be used.
	 * 
	 * @param displayFormat The display format to set. A control panel with the display format must exist
	 * 
	 * @return This builder
	 */
	BLUE_PRINT_TYPE setDisplayFormat(IDisplayFormat displayFormat);

	BLUE_PRINT_TYPE addControlPanel(IControlPanelProvider<? extends ELEMENT_VALUE_TYPE> controlPanel);

	IControlPanelProviderBluePrint<ELEMENT_VALUE_TYPE> addControlPanel(IDisplayFormat displayFormat);

	IControlPanelProviderBluePrint<ELEMENT_VALUE_TYPE> setControlPanel();

	BLUE_PRINT_TYPE setControlPanels(Collection<? extends IControlPanelProvider<? extends ELEMENT_VALUE_TYPE>> controlPanels);

}
