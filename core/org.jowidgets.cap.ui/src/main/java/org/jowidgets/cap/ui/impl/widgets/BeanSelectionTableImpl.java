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

package org.jowidgets.cap.ui.impl.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jowidgets.api.model.item.ICheckedItemModel;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionTable;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.common.types.Position;
import org.jowidgets.common.types.TablePackPolicy;
import org.jowidgets.common.types.TableSelectionPolicy;
import org.jowidgets.common.widgets.controller.ITableCellEditorListener;
import org.jowidgets.common.widgets.controller.ITableCellListener;
import org.jowidgets.common.widgets.controller.ITableCellPopupDetectionListener;
import org.jowidgets.common.widgets.controller.ITableColumnListener;
import org.jowidgets.common.widgets.controller.ITableColumnPopupDetectionListener;
import org.jowidgets.common.widgets.controller.ITableSelectionListener;
import org.jowidgets.tools.widgets.wrapper.AbstractInputControl;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.EmptyCompatibleEquivalence;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.ValidationResult;

final class BeanSelectionTableImpl<BEAN_TYPE> extends AbstractInputControl<List<IBeanProxy<BEAN_TYPE>>> implements
		IBeanSelectionTable<BEAN_TYPE> {

	private final String nothingSelectedErrorMessage;
	private final String unloadedSelectionErrorMessage;
	private final String unhandledErrorMessagesErrorMessage;

	private final IBeanTable<BEAN_TYPE> table;
	private final Set<IBeanProxy<BEAN_TYPE>> lastValue;
	private final boolean mandatorySelectionValidator;

	BeanSelectionTableImpl(final IBeanTable<BEAN_TYPE> table, final IBeanSelectionTableBluePrint<BEAN_TYPE> bluePrint) {
		super(table);
		this.table = table;
		this.mandatorySelectionValidator = bluePrint.hasMandatorySelectionValidator();

		if (bluePrint.getValidator() != null) {
			addValidator(bluePrint.getValidator());
		}

		if (bluePrint.getSelectionPolicy() == TableSelectionPolicy.SINGLE_ROW_SELECTION) {
			nothingSelectedErrorMessage = Messages.getString("BeanSelectionTableImpl.one_dataset_must_be_selected");
		}
		else {
			nothingSelectedErrorMessage = Messages.getString("BeanSelectionTableImpl.at_least_one_dataset_must_be_selected");
		}
		unloadedSelectionErrorMessage = Messages.getString("BeanSelectionTableImpl.selection_contains_unloaded_data");
		unhandledErrorMessagesErrorMessage = Messages.getString("BeanSelectionTableImpl.unhandeled_error_messages");

		table.getModel().addBeanListModelListener(new IBeanListModelListener() {

			@Override
			public void selectionChanged() {
				fireInputChanged();
				setValidationCacheDirty();
			}

			@Override
			public void beansChanged() {
				setValidationCacheDirty();
			}
		});

		this.lastValue = new HashSet<IBeanProxy<BEAN_TYPE>>();
	}

	@Override
	protected IValidationResult createValidationResult() {
		final IBeanTableModel<BEAN_TYPE> model = table.getModel();
		final ArrayList<Integer> selection = model.getSelection();
		if (mandatorySelectionValidator && EmptyCheck.isEmpty(selection)) {
			return ValidationResult.error(nothingSelectedErrorMessage);
		}
		for (final Integer index : selection) {
			final IBeanProxy<BEAN_TYPE> bean = model.getBean(index.intValue());
			if (bean == null) {
				return ValidationResult.error(unloadedSelectionErrorMessage);
			}
			else if (bean.hasErrors()) {
				return ValidationResult.error(unhandledErrorMessagesErrorMessage);
			}
			else if (bean.isDummy()) {
				return ValidationResult.error(unloadedSelectionErrorMessage);
			}
		}
		return ValidationResult.ok();
	}

	@Override
	public void setValue(final List<IBeanProxy<BEAN_TYPE>> value) {
		if (value != null) {
			table.getModel().setSelectedBeans(value);
		}
		else {
			final List<? extends IBeanProxy<BEAN_TYPE>> emptyList = Collections.emptyList();
			table.getModel().setSelectedBeans(emptyList);
		}
	}

	@Override
	public List<IBeanProxy<BEAN_TYPE>> getValue() {
		return table.getModel().getSelectedBeans();
	}

	@Override
	public boolean hasModifications() {
		return !EmptyCompatibleEquivalence.equals(lastValue, new HashSet<IBeanProxy<BEAN_TYPE>>(getValue()));
	}

	@Override
	public void resetModificationState() {
		lastValue.clear();
		final List<IBeanProxy<BEAN_TYPE>> value = getValue();
		if (value != null) {
			lastValue.addAll(getValue());
		}
	}

	@Override
	public void setEditable(final boolean editable) {
		table.setEnabled(editable);
	}

	@Override
	public IBeanTableModel<BEAN_TYPE> getModel() {
		return table.getModel();
	}

	@Override
	public void showSettingsDialog() {
		table.showSettingsDialog();
	}

	@Override
	public void setSearchFilterToolbarVisible(final boolean visible) {
		table.setSearchFilterToolbarVisible(visible);
	}

	@Override
	public void setFilterToolbarVisible(final boolean visible) {
		table.setFilterToolbarVisible(visible);
	}

	@Override
	public void setStatusBarVisible(final boolean visible) {
		table.setStatusBarVisible(visible);
	}

	@Override
	public ICheckedItemModel getSearchFilterToolbarItemModel() {
		return table.getSearchFilterToolbarItemModel();
	}

	@Override
	public ICheckedItemModel getFilterToolbarItemModel() {
		return table.getFilterToolbarItemModel();
	}

	@Override
	public ICheckedItemModel getStatusBarItemModel() {
		return table.getStatusBarItemModel();
	}

	@Override
	public IMenuModel getTablePopupMenu() {
		return table.getTablePopupMenu();
	}

	@Override
	public IMenuModel getCellPopMenu() {
		return table.getCellPopMenu();
	}

	@Override
	public IMenuModel getHeaderPopMenu() {
		return table.getHeaderPopMenu();
	}

	@Override
	public void resetFromModel() {
		table.resetFromModel();
	}

	@Override
	public Position getCellPosition(final int rowIndex, final int columnIndex) {
		return table.getCellPosition(rowIndex, columnIndex);
	}

	@Override
	public Dimension getCellSize(final int rowIndex, final int columnIndex) {
		return table.getCellSize(rowIndex, columnIndex);
	}

	@Override
	public ArrayList<Integer> getColumnPermutation() {
		return table.getColumnPermutation();
	}

	@Override
	public void setColumnPermutation(final List<Integer> permutation) {
		table.setColumnPermutation(permutation);
	}

	@Override
	public ArrayList<Integer> getSelection() {
		return table.getSelection();
	}

	@Override
	public void setSelection(final List<Integer> selection) {
		table.setSelection(selection);
	}

	@Override
	public void showSelection() {
		table.showSelection();
	}

	@Override
	public void pack(final TablePackPolicy policy) {
		table.pack(policy);
	}

	@Override
	public void pack(final int columnIndex, final TablePackPolicy policy) {
		table.pack(columnIndex, policy);
	}

	@Override
	public void addTableCellPopupDetectionListener(final ITableCellPopupDetectionListener listener) {
		table.addTableCellPopupDetectionListener(listener);
	}

	@Override
	public void removeTableCellPopupDetectionListener(final ITableCellPopupDetectionListener listener) {
		table.removeTableCellPopupDetectionListener(listener);
	}

	@Override
	public void addTableColumnPopupDetectionListener(final ITableColumnPopupDetectionListener listener) {
		table.addTableColumnPopupDetectionListener(listener);
	}

	@Override
	public void removeTableColumnPopupDetectionListener(final ITableColumnPopupDetectionListener listener) {
		table.removeTableColumnPopupDetectionListener(listener);
	}

	@Override
	public void addTableCellEditorListener(final ITableCellEditorListener listener) {
		table.addTableCellEditorListener(listener);
	}

	@Override
	public void removeTableCellEditorListener(final ITableCellEditorListener listener) {
		table.removeTableCellEditorListener(listener);
	}

	@Override
	public void addTableSelectionListener(final ITableSelectionListener listener) {
		table.addTableSelectionListener(listener);
	}

	@Override
	public void removeTableSelectionListener(final ITableSelectionListener listener) {
		table.removeTableSelectionListener(listener);
	}

	@Override
	public void addTableCellListener(final ITableCellListener listener) {
		table.addTableCellListener(listener);
	}

	@Override
	public void removeTableCellListener(final ITableCellListener listener) {
		table.removeTableCellListener(listener);
	}

	@Override
	public void pack() {
		table.pack();
	}

	@Override
	public void pack(final int columnIndex) {
		table.pack(columnIndex);
	}

	@Override
	public int getRowCount() {
		return table.getRowCount();
	}

	@Override
	public int getColumnCount() {
		return table.getColumnCount();
	}

	@Override
	public int convertColumnIndexToView(final int modelIndex) {
		return table.convertColumnIndexToView(modelIndex);
	}

	@Override
	public int convertColumnIndexToModel(final int viewIndex) {
		return table.convertColumnIndexToModel(viewIndex);
	}

	@Override
	public void moveColumn(final int oldViewIndex, final int newViewIndex) {
		table.moveColumn(oldViewIndex, newViewIndex);
	}

	@Override
	public void resetColumnPermutation() {
		table.resetColumnPermutation();
	}

	@Override
	public void addTableColumnListener(final ITableColumnListener listener) {
		table.addTableColumnListener(listener);
	}

	@Override
	public void removeTableColumnListener(final ITableColumnListener listener) {
		table.removeTableColumnListener(listener);
	}

}
