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
import java.util.List;

import org.jowidgets.api.convert.IConverter;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.common.api.lookup.ILookUpProperty;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionObservable;
import org.jowidgets.cap.ui.api.form.IBeanFormLayout;
import org.jowidgets.cap.ui.api.form.IBeanFormToolkit;
import org.jowidgets.cap.ui.api.model.ISingleBeanModel;
import org.jowidgets.cap.ui.api.tabfolder.IBeanTabFolderModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModel;
import org.jowidgets.cap.ui.api.widgets.IAttributeFilterControlBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTabFolderBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.cap.ui.api.widgets.ILookUpCollectionInputFieldBluePrint;
import org.jowidgets.cap.ui.api.widgets.ILookUpComboBoxSelectionBluePrint;
import org.jowidgets.cap.ui.api.widgets.ISingleBeanFormBluePrint;
import org.jowidgets.util.Assert;

final class CapApiBluePrintFactory implements ICapApiBluePrintFactory {

	private final IBluePrintFactory bluePrintFactory;

	CapApiBluePrintFactory() {
		this.bluePrintFactory = Toolkit.getBluePrintFactory();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <BEAN_TYPE> IBeanTableBluePrint<BEAN_TYPE> beanTable() {
		return bluePrintFactory.bluePrint(IBeanTableBluePrint.class);
	}

	@Override
	public <BEAN_TYPE> IBeanTableBluePrint<BEAN_TYPE> beanTable(final IBeanTableModel<BEAN_TYPE> model) {
		Assert.paramNotNull(model, "model");
		final IBeanTableBluePrint<BEAN_TYPE> result = beanTable();
		result.setModel(model);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <CHILD_BEAN_TYPE> IBeanRelationTreeBluePrint<CHILD_BEAN_TYPE> beanRelationTree() {
		return bluePrintFactory.bluePrint(IBeanRelationTreeBluePrint.class);
	}

	@Override
	public <CHILD_BEAN_TYPE> IBeanRelationTreeBluePrint<CHILD_BEAN_TYPE> beanRelationTree(
		final IBeanRelationTreeModel<CHILD_BEAN_TYPE> model) {
		Assert.paramNotNull(model, "model");
		final IBeanRelationTreeBluePrint<CHILD_BEAN_TYPE> result = beanRelationTree();
		result.setModel(model);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <BEAN_TYPE> IBeanSelectionTableBluePrint<BEAN_TYPE> beanSelectionTable(final IBeanTableModel<BEAN_TYPE> model) {
		Assert.paramNotNull(model, "model");
		final IBeanSelectionTableBluePrint<BEAN_TYPE> result = bluePrintFactory.bluePrint(IBeanSelectionTableBluePrint.class);
		result.setModel(model);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <BEAN_TYPE> IBeanSelectionDialogBluePrint<BEAN_TYPE> beanSelectionDialog() {
		return bluePrintFactory.bluePrint(IBeanSelectionDialogBluePrint.class);
	}

	@Override
	public <BEAN_TYPE> IBeanSelectionDialogBluePrint<BEAN_TYPE> beanSelectionDialog(final IBeanTableModel<BEAN_TYPE> model) {
		Assert.paramNotNull(model, "model");
		final IBeanSelectionDialogBluePrint<BEAN_TYPE> result = beanSelectionDialog();
		result.setBeanSelectionTable(beanSelectionTable(model));
		return result;
	}

	@Override
	public IBeanTableSettingsDialogBluePrint beanTableSettingsDialog(final IBeanTableModel<?> model) {
		Assert.paramNotNull(model, "model");
		final IBeanTableSettingsDialogBluePrint result = bluePrintFactory.bluePrint(IBeanTableSettingsDialogBluePrint.class);
		result.setModel(model);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <BEAN_TYPE> IBeanFormBluePrint<BEAN_TYPE> beanForm() {
		return bluePrintFactory.bluePrint(IBeanFormBluePrint.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <BEAN_TYPE> IBeanFormBluePrint<BEAN_TYPE> beanForm(final Object entityId) {
		final List<IAttribute<Object>> attributes = EntityServiceAttributesFactory.createAttributes(entityId);
		if (attributes != null) {
			return beanForm(entityId, attributes);
		}
		else {
			return bluePrintFactory.bluePrint(IBeanFormBluePrint.class).setEntityId(entityId);
		}
	}

	@Override
	public <BEAN_TYPE> IBeanFormBluePrint<BEAN_TYPE> beanForm(final Collection<? extends IAttribute<?>> attributes) {
		return beanForm(null, attributes);
	}

	@Override
	public <BEAN_TYPE> IBeanFormBluePrint<BEAN_TYPE> beanForm(
		final Object entityId,
		final Collection<? extends IAttribute<?>> attributes) {
		Assert.paramNotNull(attributes, "attributes");
		final IBeanFormBluePrint<BEAN_TYPE> result = beanForm();
		result.setEntityId(entityId);
		result.setAttributes(attributes);
		final IBeanFormToolkit beanFormToolkit = CapUiToolkit.beanFormToolkit();
		final IBeanFormLayout layout = CapUiToolkit.beanFormToolkit().layoutBuilder().addGroups(attributes).build();
		result.setLayouter(beanFormToolkit.layouter(layout));
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <BEAN_TYPE> IBeanDialogBluePrint<BEAN_TYPE> beanDialog(final IBeanFormBluePrint<BEAN_TYPE> beanForm) {
		Assert.paramNotNull(beanForm, "beanForm");
		final IBeanDialogBluePrint<BEAN_TYPE> result = bluePrintFactory.bluePrint(IBeanDialogBluePrint.class);
		result.setBeanForm(beanForm);
		return result;
	}

	@Override
	public IBeanSelectionFormBluePrint beanSelectionForm() {
		return bluePrintFactory.bluePrint(IBeanSelectionFormBluePrint.class);
	}

	@Override
	public IBeanSelectionFormBluePrint beanSelectionForm(final IBeanSelectionObservable<?> observable) {
		final IBeanSelectionFormBluePrint result = beanSelectionForm();
		result.setSelectionObservable(observable);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <BEAN_TYPE> ISingleBeanFormBluePrint<BEAN_TYPE> singleBeanForm(final ISingleBeanModel<BEAN_TYPE> model) {
		Assert.paramNotNull(model, "model");
		final ISingleBeanFormBluePrint<BEAN_TYPE> result = bluePrintFactory.bluePrint(ISingleBeanFormBluePrint.class);
		result.setModel(model);
		final IBeanFormBluePrint<BEAN_TYPE> beanForm = beanForm(model.getEntityId(), model.getAttributes());
		result.setBeanForm(beanForm);
		return result;
	}

	@Override
	public IAttributeFilterControlBluePrint attributeFilterControl(final List<? extends IAttribute<?>> attributes) {
		Assert.paramNotNull(attributes, "attributes");
		final IAttributeFilterControlBluePrint result = bluePrintFactory.bluePrint(IAttributeFilterControlBluePrint.class);
		result.setAttributes(attributes);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <KEY_TYPE> ILookUpComboBoxSelectionBluePrint<KEY_TYPE> lookUpComboBox(
		final Object lookUpId,
		final IConverter<KEY_TYPE> converter) {
		Assert.paramNotNull(lookUpId, "lookUpId");
		Assert.paramNotNull(converter, "converter");

		final ILookUpComboBoxSelectionBluePrint<KEY_TYPE> result = bluePrintFactory.bluePrint(ILookUpComboBoxSelectionBluePrint.class);
		result.setObjectStringConverter(converter).setLookUpId(lookUpId);
		result.setLenient(true).autoSelectionOff();

		return result;
	}

	@Override
	public <KEY_TYPE> ILookUpComboBoxSelectionBluePrint<KEY_TYPE> lookUpComboBox(
		final Object lookUpId,
		final ILookUpProperty lookUpProperty) {
		Assert.paramNotNull(lookUpId, "lookUpId");
		Assert.paramNotNull(lookUpProperty, "lookUpProperty");

		final IConverter<KEY_TYPE> converter = CapUiToolkit.converterFactory().lookUpConverter(lookUpId, lookUpProperty);
		return lookUpComboBox(lookUpId, converter);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <KEY_TYPE> ILookUpCollectionInputFieldBluePrint<KEY_TYPE> lookUpCollectionInputField(
		final Object lookUpId,
		final IConverter<KEY_TYPE> converter) {
		final ILookUpCollectionInputFieldBluePrint<KEY_TYPE> result = bluePrintFactory.bluePrint(ILookUpCollectionInputFieldBluePrint.class);
		result.setConverter(converter).setLookUpId(lookUpId);
		return result;
	}

	@Override
	public <KEY_TYPE> ILookUpCollectionInputFieldBluePrint<KEY_TYPE> lookUpCollectionInputField(
		final Object lookUpId,
		final ILookUpProperty lookUpProperty) {

		final IConverter<KEY_TYPE> converter = CapUiToolkit.converterFactory().lookUpConverter(lookUpId, lookUpProperty);
		return lookUpCollectionInputField(lookUpId, converter);
	}

	@Override
	public <BEAN_TYPE> IBeanTabFolderBluePrint<BEAN_TYPE> beanTabFolder(final IBeanTabFolderModel<BEAN_TYPE> model) {
		Assert.paramNotNull(model, "model");
		@SuppressWarnings("unchecked")
		final IBeanTabFolderBluePrint<BEAN_TYPE> result = bluePrintFactory.bluePrint(IBeanTabFolderBluePrint.class);
		result.setModel(model);
		final IBeanFormBluePrint<BEAN_TYPE> beanForm = beanForm(model.getEntityId());
		if (beanForm.getEditModeAttributes() != null && beanForm.getCreateModeAttributes() != null) {
			result.setTabFactory(beanForm);
		}
		return result;
	}
}