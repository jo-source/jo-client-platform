/*
 * Copyright (c) 2014, MGrossmann
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

package org.jowidgets.cap.ui.tools.widgets;

import java.util.Collection;
import java.util.List;

import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.convert.IConverter;
import org.jowidgets.cap.common.api.lookup.ILookUpProperty;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionObservable;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.model.ISingleBeanModel;
import org.jowidgets.cap.ui.api.tabfolder.IBeanTabFolderModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModel;
import org.jowidgets.cap.ui.api.widgets.IAttributeFilterControlBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanLinkDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanLinkPanelBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeDetailBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTabFolderBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IExecutionTaskDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.ILookUpCollectionInputFieldBluePrint;
import org.jowidgets.cap.ui.api.widgets.ILookUpComboBoxSelectionBluePrint;
import org.jowidgets.cap.ui.api.widgets.ISingleBeanFormBluePrint;

public final class CBPF {

	private CBPF() {}

	public static <BEAN_TYPE> IBeanTableBluePrint<BEAN_TYPE> beanTable() {
		return CapUiToolkit.bluePrintFactory().beanTable();
	}

	public static <BEAN_TYPE> IBeanTableBluePrint<BEAN_TYPE> beanTable(final IBeanTableModel<BEAN_TYPE> model) {
		return CapUiToolkit.bluePrintFactory().beanTable(model);
	}

	public static <CHILD_BEAN_TYPE> IBeanRelationTreeBluePrint<CHILD_BEAN_TYPE> beanRelationTree() {
		return CapUiToolkit.bluePrintFactory().beanRelationTree();
	}

	public static <CHILD_BEAN_TYPE> IBeanRelationTreeBluePrint<CHILD_BEAN_TYPE> beanRelationTree(
		final IBeanRelationTreeModel<CHILD_BEAN_TYPE> model) {
		return CapUiToolkit.bluePrintFactory().beanRelationTree(model);
	}

	public static <CHILD_BEAN_TYPE> IBeanRelationTreeDetailBluePrint<CHILD_BEAN_TYPE> beanRelationTreeDetail() {
		return CapUiToolkit.bluePrintFactory().beanRelationTreeDetail();
	}

	public static <CHILD_BEAN_TYPE> IBeanRelationTreeDetailBluePrint<CHILD_BEAN_TYPE> beanRelationTreeDetail(
		final IBeanRelationTreeModel<CHILD_BEAN_TYPE> model) {
		return CapUiToolkit.bluePrintFactory().beanRelationTreeDetail(model);
	}

	public static <BEAN_TYPE> IBeanSelectionTableBluePrint<BEAN_TYPE> beanSelectionTable(final IBeanTableModel<BEAN_TYPE> model) {
		return CapUiToolkit.bluePrintFactory().beanSelectionTable(model);
	}

	public static <BEAN_TYPE> IBeanSelectionDialogBluePrint<BEAN_TYPE> beanSelectionDialog() {
		return CapUiToolkit.bluePrintFactory().beanSelectionDialog();
	}

	public static <BEAN_TYPE> IBeanSelectionDialogBluePrint<BEAN_TYPE> beanSelectionDialog(final IBeanTableModel<BEAN_TYPE> model) {
		return CapUiToolkit.bluePrintFactory().beanSelectionDialog(model);
	}

	public static IBeanTableSettingsDialogBluePrint beanTableSettingsDialog(final IBeanTable<?> table) {
		return CapUiToolkit.bluePrintFactory().beanTableSettingsDialog(table);
	}

	public static <LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> IBeanLinkPanelBluePrint<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> beanLinkPanel() {
		return CapUiToolkit.bluePrintFactory().beanLinkPanel();
	}

	public static <LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> IBeanLinkDialogBluePrint<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> beanLinkDialog(
		final IBeanLinkPanelBluePrint<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> beanLinkPanel) {
		return CapUiToolkit.bluePrintFactory().beanLinkDialog(beanLinkPanel);
	}

	public static <BEAN_TYPE> IBeanFormBluePrint<BEAN_TYPE> beanForm() {
		return CapUiToolkit.bluePrintFactory().beanForm();
	}

	public static <BEAN_TYPE> IBeanFormBluePrint<BEAN_TYPE> beanForm(final Object entityId) {
		return CapUiToolkit.bluePrintFactory().beanForm();
	}

	public static <BEAN_TYPE> IBeanFormBluePrint<BEAN_TYPE> beanForm(final Collection<? extends IAttribute<?>> attributes) {
		return CapUiToolkit.bluePrintFactory().beanForm(attributes);
	}

	public static <BEAN_TYPE> IBeanFormBluePrint<BEAN_TYPE> beanForm(
		final Object entityId,
		final Collection<? extends IAttribute<?>> attributes) {
		return CapUiToolkit.bluePrintFactory().beanForm(entityId, attributes);
	}

	public static IBeanSelectionFormBluePrint beanSelectionForm() {
		return CapUiToolkit.bluePrintFactory().beanSelectionForm();
	}

	public static IBeanSelectionFormBluePrint beanSelectionForm(final IBeanSelectionObservable<?> observable) {
		return CapUiToolkit.bluePrintFactory().beanSelectionForm(observable);
	}

	public static <BEAN_TYPE> IBeanDialogBluePrint<BEAN_TYPE> beanDialog(final IBeanFormBluePrint<BEAN_TYPE> beanForm) {
		return CapUiToolkit.bluePrintFactory().beanDialog(beanForm);
	}

	public static <BEAN_TYPE> ISingleBeanFormBluePrint<BEAN_TYPE> singleBeanForm(final ISingleBeanModel<BEAN_TYPE> model) {
		return CapUiToolkit.bluePrintFactory().singleBeanForm(model);
	}

	public static IAttributeFilterControlBluePrint attributeFilterControl(final List<? extends IAttribute<?>> attributes) {
		return CapUiToolkit.bluePrintFactory().attributeFilterControl(attributes);
	}

	public static <KEY_TYPE> ILookUpComboBoxSelectionBluePrint<KEY_TYPE> lookUpComboBox(
		final Object lookUpId,
		final IConverter<KEY_TYPE> converter) {
		return CapUiToolkit.bluePrintFactory().lookUpComboBox(lookUpId, converter);
	}

	public static <KEY_TYPE> ILookUpComboBoxSelectionBluePrint<KEY_TYPE> lookUpComboBox(
		final Object lookUpId,
		final ILookUpProperty lookUpProperty) {
		return CapUiToolkit.bluePrintFactory().lookUpComboBox(lookUpId, lookUpProperty);
	}

	public static <KEY_TYPE> ILookUpComboBoxSelectionBluePrint<KEY_TYPE> lookUpComboBox(final Object lookUpId) {
		return CapUiToolkit.bluePrintFactory().lookUpComboBox(lookUpId);
	}

	public static <KEY_TYPE> ILookUpCollectionInputFieldBluePrint<KEY_TYPE> lookUpCollectionInputField(
		final Object lookUpId,
		final IConverter<KEY_TYPE> converter) {
		return CapUiToolkit.bluePrintFactory().lookUpCollectionInputField(lookUpId, converter);
	}

	public static <KEY_TYPE> ILookUpCollectionInputFieldBluePrint<KEY_TYPE> lookUpCollectionInputField(
		final Object lookUpId,
		final ILookUpProperty lookUpProperty) {
		return CapUiToolkit.bluePrintFactory().lookUpCollectionInputField(lookUpId, lookUpProperty);
	}

	public static <BEAN_TYPE> IBeanTabFolderBluePrint<BEAN_TYPE> beanTabFolder(final IBeanTabFolderModel<BEAN_TYPE> model) {
		return CapUiToolkit.bluePrintFactory().beanTabFolder(model);
	}

	public static IExecutionTaskDialogBluePrint executionTaskDialog(final IExecutionTask executionTask) {
		return CapUiToolkit.bluePrintFactory().executionTaskDialog(executionTask);
	}

	public static IExecutionTaskDialogBluePrint executionTaskDialog(
		final IExecutionContext executionContext,
		final IExecutionTask executionTask) {
		return CapUiToolkit.bluePrintFactory().executionTaskDialog(executionContext, executionTask);
	}

}
