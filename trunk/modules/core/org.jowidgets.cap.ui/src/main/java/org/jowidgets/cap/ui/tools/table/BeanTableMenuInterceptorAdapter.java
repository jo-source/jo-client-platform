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

package org.jowidgets.cap.ui.tools.table;

import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.cap.ui.api.command.ICopyActionBuilder;
import org.jowidgets.cap.ui.api.command.ICreatorActionBuilder;
import org.jowidgets.cap.ui.api.command.IDeleterActionBuilder;
import org.jowidgets.cap.ui.api.command.IExecutorActionBuilder;
import org.jowidgets.cap.ui.api.command.IPasteBeansActionBuilder;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuInterceptor;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.ICsvExportParameter;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;

public class BeanTableMenuInterceptorAdapter<BEAN_TYPE> implements IBeanTableMenuInterceptor<BEAN_TYPE> {

	@Override
	public IMenuModel headerPopupMenu(final IBeanTable<?> table, final int columnIndex, final IMenuModel menuModel) {
		return menuModel;
	}

	@Override
	public IMenuModel cellPopupMenu(
		final IBeanTable<?> table,
		final IMenuModel headerPopupMenuModel,
		final int columnIndex,
		final IMenuModel menuModel) {
		return menuModel;
	}

	@Override
	public IMenuModel columnsVisibilityMenu(final IBeanTableModel<?> model, final IMenuModel menuModel) {
		return menuModel;
	}

	@Override
	public IMenuModel filterMenu(final IBeanTable<?> table, final IMenuModel menuModel) {
		return menuModel;
	}

	@Override
	public IMenuModel filterCellMenu(final IBeanTable<?> table, final int columnIndex, final IMenuModel menuModel) {
		return menuModel;
	}

	@Override
	public IMenuModel filterHeaderMenu(final IBeanTable<?> table, final int columnIndex, final IMenuModel menuModel) {
		return menuModel;
	}

	@Override
	public IMenuModel editMenu(final IBeanTable<BEAN_TYPE> table, final int columnIndex, final IMenuModel menuModel) {
		return menuModel;
	}

	@Override
	public IMenuModel alignmentMenu(final IBeanTableModel<?> model, final int columnIndex, final IMenuModel menuModel) {
		return menuModel;
	}

	@Override
	public IMenuModel headerFormatMenu(final IBeanTableModel<?> model, final int columnIndex, final IMenuModel menuModel) {
		return menuModel;
	}

	@Override
	public IMenuModel contentFormatMenu(final IBeanTableModel<?> model, final int columnIndex, final IMenuModel menuModel) {
		return menuModel;
	}

	@Override
	public IMenuModel currentSortMenu(final IBeanTableModel<?> model, final int columnIndex, final IMenuModel menuModel) {
		return menuModel;
	}

	@Override
	public IMenuModel defaultSortMenu(final IBeanTableModel<?> model, final int columnIndex, final IMenuModel menuModel) {
		return menuModel;
	}

	@Override
	public IActionBuilder settingsActionBuilder(final IBeanTable<BEAN_TYPE> table, final IActionBuilder builder) {
		return builder;
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, ICsvExportParameter> csvExportActionBuilder(
		final IBeanTableModel<BEAN_TYPE> model,
		final IExecutorActionBuilder<BEAN_TYPE, ICsvExportParameter> builder) {
		return builder;
	}

	@Override
	public IActionBuilder hideColumnActionBuilder(
		final IBeanTable<BEAN_TYPE> table,
		final int columnIndex,
		final IActionBuilder builder) {
		return builder;
	}

	@Override
	public IActionBuilder showAllColumnsActionBuilder(final IBeanTable<?> table, final IActionBuilder builder) {
		return builder;
	}

	@Override
	public IActionBuilder packAllActionBuilder(final IBeanTable<?> table, final IActionBuilder builder) {
		return builder;
	}

	@Override
	public IActionBuilder packSelectedActionBuilder(final IBeanTable<?> table, final IActionBuilder builder) {
		return builder;
	}

	@Override
	public IActionBuilder clearCurrentSortActionBuilder(final IBeanTableModel<?> model, final IActionBuilder builder) {
		return builder;
	}

	@Override
	public IActionBuilder clearDefaultSortActionBuilder(final IBeanTableModel<?> model, final IActionBuilder builder) {
		return builder;
	}

	@Override
	public IActionBuilder addIncludingFilterActionBuilder(
		final IBeanTable<?> table,
		final int columnIndex,
		final IActionBuilder builder) {
		return builder;
	}

	@Override
	public IActionBuilder addExcludingFilterActionBuilder(
		final IBeanTable<?> table,
		final int columnIndex,
		final IActionBuilder builder) {
		return builder;
	}

	@Override
	public IActionBuilder addCustomFilterActionBuilder(
		final IBeanTable<?> table,
		final int columnIndex,
		final IActionBuilder builder) {
		return builder;
	}

	@Override
	public IActionBuilder addFilterActionBuilder(
		final IBeanTableModel<?> model,
		final IFilterType filterType,
		final int columnIndex,
		final IActionBuilder builder) {
		return builder;
	}

	@Override
	public IActionBuilder editFilterActionBuilder(final IBeanTableModel<?> model, final IActionBuilder builder) {
		return builder;
	}

	@Override
	public IActionBuilder deleteFilterActionBuilder(final IBeanTableModel<?> model, final IActionBuilder builder) {
		return builder;
	}

	@Override
	public IActionBuilder deleteColumnFiltersActionBuilder(
		final IBeanTableModel<?> model,
		final int columnIndex,
		final IActionBuilder builder) {
		return builder;
	}

	@Override
	public ICreatorActionBuilder<BEAN_TYPE> creatorActionBuilder(
		final IBeanTable<BEAN_TYPE> table,
		final ICreatorActionBuilder<BEAN_TYPE> builder) {
		return builder;
	}

	@Override
	public IDeleterActionBuilder<BEAN_TYPE> deleterActionBuilder(
		final IBeanTable<BEAN_TYPE> table,
		final IDeleterActionBuilder<BEAN_TYPE> builder) {
		return builder;
	}

	@Override
	public ICopyActionBuilder<BEAN_TYPE> copyActionBuilder(
		final IBeanTable<BEAN_TYPE> table,
		final ICopyActionBuilder<BEAN_TYPE> builder) {
		return builder;
	}

	@Override
	public IPasteBeansActionBuilder<BEAN_TYPE> pasteBeansActionBuilder(
		final IBeanTable<BEAN_TYPE> table,
		final IPasteBeansActionBuilder<BEAN_TYPE> builder) {
		return builder;
	}

}
