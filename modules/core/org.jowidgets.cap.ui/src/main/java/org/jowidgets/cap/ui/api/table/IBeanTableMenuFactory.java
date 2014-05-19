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

package org.jowidgets.cap.ui.api.table;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.cap.ui.api.command.ICopyActionBuilder;
import org.jowidgets.cap.ui.api.command.ICreatorActionBuilder;
import org.jowidgets.cap.ui.api.command.IDeleterActionBuilder;
import org.jowidgets.cap.ui.api.command.IExecutorActionBuilder;
import org.jowidgets.cap.ui.api.command.IPasteBeansActionBuilder;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;

public interface IBeanTableMenuFactory<BEAN_TYPE> {

	IMenuModel headerPopupMenu(IBeanTable<BEAN_TYPE> table, int columnIndex);

	IMenuModel cellPopupMenu(IBeanTable<BEAN_TYPE> table, final IMenuModel headerPopupMenuModel, int columnIndex);

	IMenuModel columnsVisibilityMenu(IBeanTableModel<BEAN_TYPE> model);

	IMenuModel filterMenu(IBeanTable<BEAN_TYPE> table);

	IMenuModel filterCellMenu(IBeanTable<BEAN_TYPE> table, int columnIndex);

	IMenuModel filterHeaderMenu(IBeanTable<BEAN_TYPE> table, int columnIndex);

	IMenuModel editMenu(IBeanTable<BEAN_TYPE> table, int columnIndex);

	IMenuModel alignmentMenu(IBeanTableModel<BEAN_TYPE> model, int columnIndex);

	/**
	 * @return The menu for the header format or null, if the header format could not be switched for the column
	 */
	IMenuModel headerFormatMenu(IBeanTableModel<BEAN_TYPE> model, int columnIndex);

	/**
	 * @return The menu for the content display format or null, if the content format could not be switched for the column
	 */
	IMenuModel contentFormatMenu(IBeanTableModel<BEAN_TYPE> model, int columnIndex);

	/**
	 * @return The menu for the sorting or null, if the column is not sortable
	 */
	IMenuModel currentSortMenu(IBeanTableModel<BEAN_TYPE> model, int columnIndex);

	/**
	 * @return The menu for the sorting or null, if the column is not sortable
	 */
	IMenuModel defaultSortMenu(IBeanTableModel<BEAN_TYPE> model, int columnIndex);

	IActionBuilder settingsActionBuilder(IBeanTable<BEAN_TYPE> table);

	IAction settingsAction(IBeanTable<BEAN_TYPE> table);

	IExecutorActionBuilder<BEAN_TYPE, ICsvExportParameter> csvExportActionBuilder(IBeanTableModel<BEAN_TYPE> model);

	IAction csvExportAction(IBeanTableModel<BEAN_TYPE> model);

	IActionBuilder hideColumnActionBuilder(IBeanTable<BEAN_TYPE> table, int columnIndex);

	IAction hideColumnAction(IBeanTable<BEAN_TYPE> table, int columnIndex);

	IActionBuilder showAllColumnsActionBuilder(IBeanTable<BEAN_TYPE> table);

	IAction showAllColumnsAction(IBeanTable<BEAN_TYPE> table);

	IActionBuilder packAllActionBuilder(IBeanTable<BEAN_TYPE> table);

	IAction packAllAction(IBeanTable<BEAN_TYPE> table);

	IActionBuilder packSelectedActionBuilder(IBeanTable<BEAN_TYPE> table);

	IAction packSelectedAction(IBeanTable<BEAN_TYPE> table);

	IActionBuilder clearCurrentSortActionBuilder(IBeanTableModel<BEAN_TYPE> model);

	IAction clearCurrentSortAction(IBeanTableModel<BEAN_TYPE> model);

	IActionBuilder clearDefaultSortActionBuilder(IBeanTableModel<BEAN_TYPE> model);

	IAction clearDefaultSortAction(IBeanTableModel<BEAN_TYPE> model);

	IActionBuilder setToAllActionBuilder(IBeanTable<BEAN_TYPE> table, int columnIndex);

	IAction setToAllAction(IBeanTable<BEAN_TYPE> table, int columnIndex);

	IActionBuilder addIncludingFilterActionBuilder(IBeanTable<BEAN_TYPE> table, int columnIndex);

	IAction addIncludingFilterAction(IBeanTable<BEAN_TYPE> table, int columnIndex);

	IActionBuilder addExcludingFilterActionBuilder(IBeanTable<BEAN_TYPE> table, int columnIndex);

	IAction addExcludingFilterAction(IBeanTable<BEAN_TYPE> table, int columnIndex);

	IActionBuilder addCustomFilterActionBuilder(IBeanTable<BEAN_TYPE> table, int columnIndex);

	IAction addCustomFilterAction(IBeanTable<BEAN_TYPE> table, int columnIndex);

	IActionBuilder addFilterActionBuilder(IBeanTableModel<BEAN_TYPE> model, IFilterType filterType, int columnIndex);

	IAction addFilterAction(IBeanTableModel<BEAN_TYPE> model, IFilterType filterType, int columnIndex);

	IActionBuilder editFilterActionBuilder(IBeanTableModel<BEAN_TYPE> model);

	IAction editFilterAction(IBeanTableModel<BEAN_TYPE> model);

	IActionBuilder deleteFilterActionBuilder(IBeanTableModel<BEAN_TYPE> model);

	IAction deleteFilterAction(IBeanTableModel<BEAN_TYPE> model);

	IActionBuilder deleteColumnFiltersActionBuilder(IBeanTableModel<BEAN_TYPE> model, int columnIndex);

	IAction deleteColumnFiltersAction(IBeanTableModel<BEAN_TYPE> model, int columnIndex);

	ICreatorActionBuilder<BEAN_TYPE> creatorActionBuilder(IBeanTable<BEAN_TYPE> table);

	IAction creatorAction(IBeanTable<BEAN_TYPE> table);

	IDeleterActionBuilder<BEAN_TYPE> deleterActionBuilder(IBeanTable<BEAN_TYPE> table);

	IAction deleterAction(IBeanTable<BEAN_TYPE> table);

	ICopyActionBuilder<BEAN_TYPE> copyActionBuilder(IBeanTable<BEAN_TYPE> table);

	IAction copyAction(IBeanTable<BEAN_TYPE> table);

	IPasteBeansActionBuilder<BEAN_TYPE> pasteBeansActionBuilder(IBeanTable<BEAN_TYPE> table);

	IAction pasteBeansAction(IBeanTable<BEAN_TYPE> table);

}
