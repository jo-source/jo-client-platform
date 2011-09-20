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

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class BeanTableMenuFactoryImpl implements IBeanTableMenuFactory {

	BeanTableMenuFactoryImpl() {}

	@Override
	public IActionBuilder settingsActionBuilder(final IBeanTable<?> table) {
		Assert.paramNotNull(table, "table");
		return new BeanTableSettingsActionBuilder(table);
	}

	@Override
	public IAction settingsAction(final IBeanTable<?> table) {
		return settingsActionBuilder(table).build();
	}

	@Override
	public IActionBuilder hideColumnActionBuilder(final IBeanTable<?> table) {
		Assert.paramNotNull(table, "table");
		return new BeanTableHideColumnActionBuilder(table);
	}

	@Override
	public IAction hideColumnAction(final IBeanTable<?> table) {
		return hideColumnActionBuilder(table).build();
	}

	@Override
	public IActionBuilder showAllColumnsActionBuilder(final IBeanTable<?> table) {
		Assert.paramNotNull(table, "table");
		return new BeanTableShowAllColumnsActionBuilder(table);
	}

	@Override
	public IAction showAllColumnsAction(final IBeanTable<?> table) {
		return showAllColumnsActionBuilder(table).build();
	}

	@Override
	public IActionBuilder packAllActionBuilder(final IBeanTable<?> table) {
		Assert.paramNotNull(table, "table");
		return new BeanTablePackAllActionBuilder(table);
	}

	@Override
	public IAction packAllAction(final IBeanTable<?> table) {
		return packAllActionBuilder(table).build();
	}

	@Override
	public IActionBuilder packSelectedActionBuilder(final IBeanTable<?> table) {
		Assert.paramNotNull(table, "table");
		return new BeanTablePackSelectedActionBuilder(table);
	}

	@Override
	public IAction packSelectedAction(final IBeanTable<?> table) {
		return packSelectedActionBuilder(table).build();
	}

	@Override
	public IActionBuilder clearCurrentSortActionBuilder(final IBeanTableModel<?> model) {
		Assert.paramNotNull(model, "model");
		return new BeanTableClearCurrentSortActionBuilder(model);
	}

	@Override
	public IAction clearCurrentSortAction(final IBeanTableModel<?> model) {
		return clearCurrentSortActionBuilder(model).build();
	}

	@Override
	public IActionBuilder clearDefaultSortActionBuilder(final IBeanTableModel<?> model) {
		Assert.paramNotNull(model, "model");
		return new BeanTableClearDefaultSortActionBuilder(model);
	}

	@Override
	public IAction clearDefaultSortAction(final IBeanTableModel<?> model) {
		return clearDefaultSortActionBuilder(model).build();
	}

	@Override
	public IActionBuilder includingFilterActionBuilder(final IBeanTableModel<?> model) {
		return new BeanTableIncludingFilterActionBuilder(model);
	}

	@Override
	public IAction includingFilterAction(final IBeanTableModel<?> model) {
		return includingFilterActionBuilder(model).build();
	}

	@Override
	public IActionBuilder excludingFilterActionBuilder(final IBeanTableModel<?> model) {
		return new BeanTableExcludingFilterActionBuilder(model);
	}

	@Override
	public IAction excludingFilterAction(final IBeanTableModel<?> model) {
		return excludingFilterActionBuilder(model).build();
	}

	@Override
	public IActionBuilder customFilterActionBuilder(final IBeanTableModel<?> model) {
		return new BeanTableCustomFilterActionBuilder(model);
	}

	@Override
	public IAction customFilterAction(final IBeanTableModel<?> model) {
		return customFilterActionBuilder(model).build();
	}

	@Override
	public IActionBuilder editFilterActionBuilder(final IBeanTableModel<?> model) {
		return new BeanTableEditFilterActionBuilder(model);
	}

	@Override
	public IAction editFilterAction(final IBeanTableModel<?> model) {
		return editFilterActionBuilder(model).build();
	}

	@Override
	public IActionBuilder deleteFilterActionBuilder(final IBeanTableModel<?> model) {
		return new BeanTableDeleteFilterActionBuilder(model);
	}

	@Override
	public IAction deleteFilterAction(final IBeanTableModel<?> model) {
		return deleteFilterActionBuilder(model).build();
	}

	@Override
	public IActionBuilder deleteColumnFiltersActionBuilder(final IBeanTableModel<?> model) {
		return new BeanTableDeleteColumnFiltersActionBuilder(model);
	}

	@Override
	public IAction deleteColumnFiltersAction(final IBeanTableModel<?> model) {
		return deleteColumnFiltersActionBuilder(model).build();
	}

	@Override
	public IMenuModel headerPopupMenu(final IBeanTable<?> table, final int columnIndex) {
		Assert.paramNotNull(table, "table");
		return new BeanTableHeaderMenuModel(table, columnIndex);
	}

	@Override
	public IMenuModel cellPopupMenu(final IBeanTable<?> table, final IMenuModel headerPopupMenuModel, final int columnIndex) {
		Assert.paramNotNull(table, "table");
		return new BeanTableCellMenuModel(table, headerPopupMenuModel, columnIndex);
	}

	@Override
	public IMenuModel columnsVisibilityMenu(final IBeanTableModel<?> model) {
		Assert.paramNotNull(model, "model");
		return new BeanTableColumnsVisibilityMenuModel(model);
	}

	@Override
	public IMenuModel filterMenu(final IBeanTableModel<?> model) {
		Assert.paramNotNull(model, "model");
		return new BeanTableFilterMenuModel(model);
	}

	@Override
	public IMenuModel filterCellMenu(final IBeanTableModel<?> model) {
		Assert.paramNotNull(model, "model");
		return new BeanTableCellFilterMenuModel(model);
	}

	@Override
	public IMenuModel filterHeaderMenu(final IBeanTableModel<?> model) {
		Assert.paramNotNull(model, "model");
		return new BeanTableHeaderFilterMenuModel(model);
	}

	@Override
	public IMenuModel alignmentMenu(final IBeanTableModel<?> model, final int columnIndex) {
		Assert.paramNotNull(model, "model");
		return new BeanTableAlignmentMenuModel(model, columnIndex);
	}

	@Override
	public IMenuModel headerFormatMenu(final IBeanTableModel<?> model, final int columnIndex) {
		Assert.paramNotNull(model, "model");
		if (EmptyCheck.isEmpty(model.getAttribute(columnIndex).getLabelLong())) {
			return null;
		}
		else {
			return new BeanTableHeaderFormatMenuModel(model, columnIndex);
		}
	}

	@Override
	public IMenuModel contentFormatMenu(final IBeanTableModel<?> model, final int columnIndex) {
		Assert.paramNotNull(model, "model");
		if (model.getAttribute(columnIndex).getControlPanels().size() > 1) {
			return new BeanTableContentFormatMenuModel(model, columnIndex);
		}
		else {
			return null;
		}
	}

	@Override
	public IMenuModel currentSortMenu(final IBeanTableModel<?> model, final int columnIndex) {
		Assert.paramNotNull(model, "model");
		if (model.getAttribute(columnIndex).isSortable()) {
			return new BeanTableCurrentSortMenuModel(model, columnIndex);
		}
		else {
			return null;
		}
	}

	@Override
	public IMenuModel defaultSortMenu(final IBeanTableModel<?> model, final int columnIndex) {
		Assert.paramNotNull(model, "model");
		if (model.getAttribute(columnIndex).isSortable()) {
			return new BeanTableDefaultSortMenuModel(model, columnIndex);
		}
		else {
			return null;
		}
	}

}
