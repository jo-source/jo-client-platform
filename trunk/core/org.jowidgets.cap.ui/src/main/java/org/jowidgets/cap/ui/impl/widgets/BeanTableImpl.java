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
import java.util.List;

import org.jowidgets.api.convert.IStringObjectConverter;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.api.widgets.ITable;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.sort.ISortModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.common.types.Position;
import org.jowidgets.common.types.TablePackPolicy;
import org.jowidgets.common.widgets.controler.ITableCellEditEvent;
import org.jowidgets.common.widgets.controler.ITableCellEditorListener;
import org.jowidgets.common.widgets.controler.ITableCellListener;
import org.jowidgets.common.widgets.controler.ITableCellPopupDetectionListener;
import org.jowidgets.common.widgets.controler.ITableColumnListener;
import org.jowidgets.common.widgets.controler.ITableColumnMouseEvent;
import org.jowidgets.common.widgets.controler.ITableColumnPopupDetectionListener;
import org.jowidgets.common.widgets.controler.ITableSelectionListener;
import org.jowidgets.tools.controler.TableCellEditorAdapter;
import org.jowidgets.tools.controler.TableColumnAdapter;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;

final class BeanTableImpl<BEAN_TYPE> extends ControlWrapper implements IBeanTable<BEAN_TYPE> {

	private final IBeanTableModel<BEAN_TYPE> model;

	BeanTableImpl(final ITable table, final IBeanTableBluePrint<BEAN_TYPE> bluePrint) {
		super(table);
		this.model = bluePrint.getModel();

		table.addTableCellEditorListener(new TableCellEditorAdapter() {

			@Override
			public void editFinished(final ITableCellEditEvent event) {
				final IBeanProxy<BEAN_TYPE> bean = model.getBean(event.getRowIndex());
				final IAttribute<Object> attribute = model.getAttribute(event.getColumnIndex());
				if (bean != null && attribute != null && !attribute.isCollectionType()) {
					final IStringObjectConverter<Object> converter = attribute.getCurrentControlPanel().getStringObjectConverter();
					if (converter != null) {
						if (converter.getStringValidator() == null
							|| converter.getStringValidator().validate(event.getCurrentText()).isValid()) {
							final Object value = converter.convertToObject(event.getCurrentText());
							bean.setValue(attribute.getPropertyName(), value);
						}
					}
				}
			}

		});

		table.addTableColumnListener(new TableColumnAdapter() {
			@Override
			public void mouseClicked(final ITableColumnMouseEvent event) {
				final int modelColumn = event.getColumnIndex();
				if (modelColumn < 0) {
					return;
				}

				final IAttribute<?> attribute = model.getAttribute(modelColumn);
				if (attribute != null && attribute.isSortable()) {
					final ISortModel sortModel = model.getSortModel();
					final String propertyName = attribute.getPropertyName();
					if (event.getModifiers().contains(Modifier.CTRL)) {
						sortModel.addOrToggleCurrentProperty(propertyName);
					}
					else {
						sortModel.setOrToggleCurrentProperty(propertyName);
					}
				}
			}
		});

	}

	@Override
	protected ITable getWidget() {
		return (ITable) super.getWidget();
	}

	@Override
	public void pack() {
		getWidget().pack();
	}

	@Override
	public void pack(final int columnIndex) {
		getWidget().pack(columnIndex);
	}

	@Override
	public int getRowCount() {
		return getWidget().getRowCount();
	}

	@Override
	public int getColumnCount() {
		return getWidget().getColumnCount();
	}

	@Override
	public int convertColumnIndexToView(final int modelIndex) {
		return getWidget().convertColumnIndexToView(modelIndex);
	}

	@Override
	public int convertColumnIndexToModel(final int viewIndex) {
		return getWidget().convertColumnIndexToModel(viewIndex);
	}

	@Override
	public void moveColumn(final int oldViewIndex, final int newViewIndex) {
		getWidget().moveColumn(oldViewIndex, newViewIndex);
	}

	@Override
	public void resetColumnPermutation() {
		getWidget().resetColumnPermutation();
	}

	@Override
	public void resetFromModel() {
		getWidget().resetFromModel();
	}

	@Override
	public Position getCellPosition(final int rowIndex, final int columnIndex) {
		return getWidget().getCellPosition(rowIndex, columnIndex);
	}

	@Override
	public Dimension getCellSize(final int rowIndex, final int columnIndex) {
		return getWidget().getCellSize(rowIndex, columnIndex);
	}

	@Override
	public ArrayList<Integer> getColumnPermutation() {
		return getWidget().getColumnPermutation();
	}

	@Override
	public void setColumnPermutation(final List<Integer> permutation) {
		getWidget().setColumnPermutation(permutation);
	}

	@Override
	public ArrayList<Integer> getSelection() {
		return getWidget().getSelection();
	}

	@Override
	public void setSelection(final List<Integer> selection) {
		getWidget().setSelection(selection);
	}

	@Override
	public void pack(final TablePackPolicy policy) {
		getWidget().pack(policy);
	}

	@Override
	public void pack(final int columnIndex, final TablePackPolicy policy) {
		getWidget().pack(columnIndex, policy);
	}

	@Override
	public void addTableSelectionListener(final ITableSelectionListener listener) {
		getWidget().addTableSelectionListener(listener);
	}

	@Override
	public void removeTableSelectionListener(final ITableSelectionListener listener) {
		getWidget().removeTableSelectionListener(listener);
	}

	@Override
	public void addTableCellListener(final ITableCellListener listener) {
		getWidget().addTableCellListener(listener);
	}

	@Override
	public void removeTableCellListener(final ITableCellListener listener) {
		getWidget().removeTableCellListener(listener);
	}

	@Override
	public void addTableCellPopupDetectionListener(final ITableCellPopupDetectionListener listener) {
		getWidget().addTableCellPopupDetectionListener(listener);
	}

	@Override
	public void removeTableCellPopupDetectionListener(final ITableCellPopupDetectionListener listener) {
		getWidget().removeTableCellPopupDetectionListener(listener);
	}

	@Override
	public void addTableCellEditorListener(final ITableCellEditorListener listener) {
		getWidget().addTableCellEditorListener(listener);
	}

	@Override
	public void removeTableCellEditorListener(final ITableCellEditorListener listener) {
		getWidget().removeTableCellEditorListener(listener);
	}

	@Override
	public void addTableColumnListener(final ITableColumnListener listener) {
		getWidget().addTableColumnListener(listener);
	}

	@Override
	public void removeTableColumnListener(final ITableColumnListener listener) {
		getWidget().removeTableColumnListener(listener);
	}

	@Override
	public void addTableColumnPopupDetectionListener(final ITableColumnPopupDetectionListener listener) {
		getWidget().addTableColumnPopupDetectionListener(listener);
	}

	@Override
	public void removeTableColumnPopupDetectionListener(final ITableColumnPopupDetectionListener listener) {
		getWidget().removeTableColumnPopupDetectionListener(listener);
	}

	@Override
	public IMenuModel getMenu() {
		return null;
	}
}
