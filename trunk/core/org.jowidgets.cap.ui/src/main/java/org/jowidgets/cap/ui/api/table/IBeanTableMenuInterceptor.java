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

import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.cap.ui.api.command.ICreatorActionBuilder;
import org.jowidgets.cap.ui.api.command.IDeleterActionBuilder;
import org.jowidgets.cap.ui.api.command.IExecutorActionBuilder;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;

public interface IBeanTableMenuInterceptor<BEAN_TYPE> {

	IMenuModel headerPopupMenu(IBeanTable<?> table, int columnIndex, IMenuModel menuModel);

	IMenuModel cellPopupMenu(IBeanTable<?> table, final IMenuModel headerPopupMenuModel, int columnIndex, IMenuModel menuModel);

	IMenuModel columnsVisibilityMenu(IBeanTableModel<?> model, IMenuModel menuModel);

	IMenuModel filterMenu(IBeanTable<?> table, IMenuModel menuModel);

	IMenuModel filterCellMenu(IBeanTable<?> table, int columnIndex, IMenuModel menuModel);

	IMenuModel filterHeaderMenu(IBeanTable<?> table, int columnIndex, IMenuModel menuModel);

	IMenuModel alignmentMenu(IBeanTableModel<?> model, int columnIndex, IMenuModel menuModel);

	IMenuModel headerFormatMenu(IBeanTableModel<?> model, int columnIndex, IMenuModel menuModel);

	IMenuModel contentFormatMenu(IBeanTableModel<?> model, int columnIndex, IMenuModel menuModel);

	IMenuModel currentSortMenu(IBeanTableModel<?> model, int columnIndex, IMenuModel menuModel);

	IMenuModel defaultSortMenu(IBeanTableModel<?> model, int columnIndex, IMenuModel menuModel);

	IActionBuilder settingsActionBuilder(IBeanTable<BEAN_TYPE> table, IActionBuilder builder);

	IExecutorActionBuilder<BEAN_TYPE, ICsvExportParameter> csvExportActionBuilder(
		IBeanTableModel<BEAN_TYPE> model,
		IExecutorActionBuilder<BEAN_TYPE, ICsvExportParameter> builder);

	IActionBuilder hideColumnActionBuilder(IBeanTable<BEAN_TYPE> table, int columnIndex, IActionBuilder builder);

	IActionBuilder showAllColumnsActionBuilder(IBeanTable<?> table, IActionBuilder builder);

	IActionBuilder packAllActionBuilder(IBeanTable<?> table, IActionBuilder builder);

	IActionBuilder packSelectedActionBuilder(IBeanTable<?> table, IActionBuilder builder);

	IActionBuilder clearCurrentSortActionBuilder(IBeanTableModel<?> model, IActionBuilder builder);

	IActionBuilder clearDefaultSortActionBuilder(IBeanTableModel<?> model, IActionBuilder builder);

	IActionBuilder addIncludingFilterActionBuilder(IBeanTableModel<?> model, int columnIndex, IActionBuilder builder);

	IActionBuilder addExcludingFilterActionBuilder(IBeanTableModel<?> model, int columnIndex, IActionBuilder builder);

	IActionBuilder addCustomFilterActionBuilder(IBeanTableModel<?> model, int columnIndex, IActionBuilder builder);

	IActionBuilder addFilterActionBuilder(
		IBeanTableModel<?> model,
		IFilterType filterType,
		int columnIndex,
		IActionBuilder builder);

	IActionBuilder editFilterActionBuilder(IBeanTableModel<?> model, IActionBuilder builder);

	IActionBuilder deleteFilterActionBuilder(IBeanTableModel<?> model, IActionBuilder builder);

	IActionBuilder deleteColumnFiltersActionBuilder(IBeanTableModel<?> model, int columnIndex, IActionBuilder builder);

	ICreatorActionBuilder<BEAN_TYPE> creatorActionBuilder(IBeanTable<BEAN_TYPE> table, ICreatorActionBuilder<BEAN_TYPE> builder);

	IDeleterActionBuilder<BEAN_TYPE> deleterActionBuilder(IBeanTable<BEAN_TYPE> table, IDeleterActionBuilder<BEAN_TYPE> builder);

}
