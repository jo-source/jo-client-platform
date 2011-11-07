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

package org.jowidgets.cap.ui.api.widgets;

import java.util.List;

import org.jowidgets.api.convert.IConverter;
import org.jowidgets.cap.common.api.lookup.ILookUpProperty;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;

public interface ICapApiBluePrintFactory {

	<BEAN_TYPE> IBeanTableBluePrint<BEAN_TYPE> beanTable();

	<BEAN_TYPE> IBeanTableBluePrint<BEAN_TYPE> beanTable(IBeanTableModel<BEAN_TYPE> model);

	<BEAN_TYPE> IBeanSelectionTableBluePrint<BEAN_TYPE> beanSelectionTable(IBeanTableModel<BEAN_TYPE> model);

	<BEAN_TYPE> IBeanSelectionDialogBluePrint<BEAN_TYPE> beanSelectionDialog();

	<BEAN_TYPE> IBeanSelectionDialogBluePrint<BEAN_TYPE> beanSelectionDialog(IBeanTableModel<BEAN_TYPE> model);

	IBeanTableSettingsDialogBluePrint beanTableSettingsDialog(IBeanTableModel<?> model);

	<BEAN_TYPE> IBeanFormBluePrint<BEAN_TYPE> beanForm(List<? extends IAttribute<?>> attributes);

	IBeanTablesFormBluePrint beanTablesForm();

	<BEAN_TYPE> IBeanDialogBluePrint<BEAN_TYPE> beanDialog(IBeanFormBluePrint<BEAN_TYPE> beanForm);

	<BEAN_TYPE> IBeanTableFormBluePrint<BEAN_TYPE> beanTableForm(IBeanTableModel<BEAN_TYPE> model);

	IAttributeFilterControlBluePrint attributeFilterControl(List<? extends IAttribute<?>> attributes);

	<KEY_TYPE> ILookUpComboBoxSelectionBluePrint<KEY_TYPE> lookUpComboBox(Object lookUpId, IConverter<KEY_TYPE> converter);

	<KEY_TYPE> ILookUpComboBoxSelectionBluePrint<KEY_TYPE> lookUpComboBox(Object lookUpId, ILookUpProperty lookUpProperty);

	<KEY_TYPE> ILookUpCollectionInputFieldBluePrint<KEY_TYPE> lookUpCollectionInputField(
		Object lookUpId,
		IConverter<KEY_TYPE> converter);

	<KEY_TYPE> ILookUpCollectionInputFieldBluePrint<KEY_TYPE> lookUpCollectionInputField(
		Object lookUpId,
		ILookUpProperty lookUpProperty);

}
