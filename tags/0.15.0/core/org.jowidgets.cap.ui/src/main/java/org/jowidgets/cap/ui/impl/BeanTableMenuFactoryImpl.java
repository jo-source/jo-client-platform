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
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.cap.ui.api.command.ICreatorActionBuilder;
import org.jowidgets.cap.ui.api.command.IDeleterActionBuilder;
import org.jowidgets.cap.ui.api.command.IExecutorActionBuilder;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuInterceptor;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.ICsvExportParameter;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.NullCompatibleEquivalence;

final class BeanTableMenuFactoryImpl<BEAN_TYPE> implements IBeanTableMenuFactory<BEAN_TYPE> {

	private final List<IBeanTableMenuInterceptor<BEAN_TYPE>> interceptors;

	BeanTableMenuFactoryImpl(final Collection<IBeanTableMenuInterceptor<BEAN_TYPE>> interceptors) {
		this.interceptors = new LinkedList<IBeanTableMenuInterceptor<BEAN_TYPE>>(interceptors);
	}

	@Override
	public IMenuModel headerPopupMenu(final IBeanTable<BEAN_TYPE> table, final int columnIndex) {
		Assert.paramNotNull(table, "table");
		IMenuModel menuModel = new BeanTableHeaderMenuModel<BEAN_TYPE>(table, columnIndex, this);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (menuModel != null) {
				menuModel = interceptor.headerPopupMenu(table, columnIndex, menuModel);
			}
			else {
				break;
			}
		}
		return menuModel;
	}

	@Override
	public IMenuModel cellPopupMenu(
		final IBeanTable<BEAN_TYPE> table,
		final IMenuModel headerPopupMenuModel,
		final int columnIndex) {
		Assert.paramNotNull(table, "table");
		IMenuModel menuModel = new BeanTableCellMenuModel<BEAN_TYPE>(table, headerPopupMenuModel, columnIndex, this);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (menuModel != null) {
				menuModel = interceptor.cellPopupMenu(table, headerPopupMenuModel, columnIndex, menuModel);
			}
			else {
				break;
			}
		}
		return menuModel;
	}

	@Override
	public IMenuModel columnsVisibilityMenu(final IBeanTableModel<BEAN_TYPE> model) {
		Assert.paramNotNull(model, "model");
		IMenuModel menuModel = new BeanTableColumnsVisibilityMenuModel(model);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (menuModel != null) {
				menuModel = interceptor.columnsVisibilityMenu(model, menuModel);
			}
			else {
				break;
			}
		}
		return menuModel;
	}

	@Override
	public IMenuModel filterMenu(final IBeanTable<BEAN_TYPE> table) {
		Assert.paramNotNull(table, "table");
		IMenuModel menuModel = new BeanTableFilterMenuModel<BEAN_TYPE>(table, this);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (menuModel != null) {
				menuModel = interceptor.filterMenu(table, menuModel);
			}
			else {
				break;
			}
		}
		return menuModel;
	}

	@Override
	public IMenuModel filterCellMenu(final IBeanTable<BEAN_TYPE> table, final int columnIndex) {
		Assert.paramNotNull(table, "table");
		IMenuModel menuModel = new BeanTableCellFilterMenuModel<BEAN_TYPE>(table, columnIndex, this);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (menuModel != null) {
				menuModel = interceptor.filterCellMenu(table, columnIndex, menuModel);
			}
			else {
				break;
			}
		}
		return menuModel;
	}

	@Override
	public IMenuModel filterHeaderMenu(final IBeanTable<BEAN_TYPE> table, final int columnIndex) {
		Assert.paramNotNull(table, "table");
		IMenuModel menuModel = new BeanTableHeaderFilterMenuModel<BEAN_TYPE>(table, columnIndex, this);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (menuModel != null) {
				menuModel = interceptor.filterHeaderMenu(table, columnIndex, menuModel);
			}
			else {
				break;
			}
		}
		return menuModel;
	}

	@Override
	public IMenuModel alignmentMenu(final IBeanTableModel<BEAN_TYPE> model, final int columnIndex) {
		Assert.paramNotNull(model, "model");
		IMenuModel menuModel = new BeanTableAlignmentMenuModel(model, columnIndex);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (menuModel != null) {
				menuModel = interceptor.alignmentMenu(model, columnIndex, menuModel);
			}
			else {
				break;
			}
		}
		return menuModel;
	}

	@Override
	public IMenuModel headerFormatMenu(final IBeanTableModel<BEAN_TYPE> model, final int columnIndex) {
		Assert.paramNotNull(model, "model");
		if (EmptyCheck.isEmpty(model.getAttribute(columnIndex).getLabelLong().get())) {
			return null;
		}
		else if (NullCompatibleEquivalence.equals(
				model.getAttribute(columnIndex).getLabel().get(),
				model.getAttribute(columnIndex).getLabelLong().get())) {
			return null;
		}
		else {
			IMenuModel menuModel = new BeanTableHeaderFormatMenuModel(model, columnIndex);
			for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
				if (menuModel != null) {
					menuModel = interceptor.headerFormatMenu(model, columnIndex, menuModel);
				}
				else {
					break;
				}
			}
			return menuModel;
		}
	}

	@Override
	public IMenuModel contentFormatMenu(final IBeanTableModel<BEAN_TYPE> model, final int columnIndex) {
		Assert.paramNotNull(model, "model");
		if (model.getAttribute(columnIndex).getControlPanels().size() > 1) {
			IMenuModel menuModel = new BeanTableContentFormatMenuModel(model, columnIndex);
			for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
				if (menuModel != null) {
					menuModel = interceptor.contentFormatMenu(model, columnIndex, menuModel);
				}
				else {
					break;
				}
			}
			return menuModel;
		}
		else {
			return null;
		}
	}

	@Override
	public IMenuModel currentSortMenu(final IBeanTableModel<BEAN_TYPE> model, final int columnIndex) {
		Assert.paramNotNull(model, "model");
		if (model.getAttribute(columnIndex).isSortable()) {
			IMenuModel menuModel = new BeanTableCurrentSortMenuModel(model, columnIndex);
			for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
				if (menuModel != null) {
					menuModel = interceptor.currentSortMenu(model, columnIndex, menuModel);
				}
				else {
					break;
				}
			}
			return menuModel;
		}
		else {
			return null;
		}
	}

	@Override
	public IMenuModel defaultSortMenu(final IBeanTableModel<BEAN_TYPE> model, final int columnIndex) {
		Assert.paramNotNull(model, "model");
		if (model.getAttribute(columnIndex).isSortable()) {
			IMenuModel menuModel = new BeanTableDefaultSortMenuModel(model, columnIndex);
			for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
				if (menuModel != null) {
					menuModel = interceptor.defaultSortMenu(model, columnIndex, menuModel);
				}
				else {
					break;
				}
			}
			return menuModel;
		}
		else {
			return null;
		}
	}

	@Override
	public IActionBuilder settingsActionBuilder(final IBeanTable<BEAN_TYPE> table) {
		Assert.paramNotNull(table, "table");
		IActionBuilder builder = new BeanTableSettingsActionBuilder(table);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (builder != null) {
				builder = interceptor.settingsActionBuilder(table, builder);
			}
			else {
				break;
			}
		}
		return builder;
	}

	@Override
	public IAction settingsAction(final IBeanTable<BEAN_TYPE> table) {
		return settingsActionBuilder(table).build();
	}

	@Override
	public IExecutorActionBuilder<BEAN_TYPE, ICsvExportParameter> csvExportActionBuilder(final IBeanTableModel<BEAN_TYPE> model) {
		Assert.paramNotNull(model, "model");
		IExecutorActionBuilder<BEAN_TYPE, ICsvExportParameter> builder = new BeanTableCsvExportActionBuilder<BEAN_TYPE>(model);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (builder != null) {
				builder = interceptor.csvExportActionBuilder(model, builder);
			}
			else {
				break;
			}
		}
		return builder;
	}

	@Override
	public IAction csvExportAction(final IBeanTableModel<BEAN_TYPE> model) {
		return csvExportActionBuilder(model).build();
	}

	@Override
	public IActionBuilder hideColumnActionBuilder(final IBeanTable<BEAN_TYPE> table, final int columnIndex) {
		Assert.paramNotNull(table, "table");
		IActionBuilder builder = new BeanTableHideColumnActionBuilder(table, columnIndex);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (builder != null) {
				builder = interceptor.hideColumnActionBuilder(table, columnIndex, builder);
			}
			else {
				break;
			}
		}
		return builder;
	}

	@Override
	public IAction hideColumnAction(final IBeanTable<BEAN_TYPE> table, final int columnIndex) {
		return hideColumnActionBuilder(table, columnIndex).build();
	}

	@Override
	public IActionBuilder showAllColumnsActionBuilder(final IBeanTable<BEAN_TYPE> table) {
		Assert.paramNotNull(table, "table");
		IActionBuilder builder = new BeanTableShowAllColumnsActionBuilder(table);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (builder != null) {
				builder = interceptor.showAllColumnsActionBuilder(table, builder);
			}
			else {
				break;
			}
		}
		return builder;
	}

	@Override
	public IAction showAllColumnsAction(final IBeanTable<BEAN_TYPE> table) {
		return showAllColumnsActionBuilder(table).build();
	}

	@Override
	public IActionBuilder packAllActionBuilder(final IBeanTable<BEAN_TYPE> table) {
		Assert.paramNotNull(table, "table");
		IActionBuilder builder = new BeanTablePackAllActionBuilder(table);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (builder != null) {
				builder = interceptor.packAllActionBuilder(table, builder);
			}
			else {
				break;
			}
		}
		return builder;
	}

	@Override
	public IAction packAllAction(final IBeanTable<BEAN_TYPE> table) {
		return packAllActionBuilder(table).build();
	}

	@Override
	public IActionBuilder packSelectedActionBuilder(final IBeanTable<BEAN_TYPE> table) {
		Assert.paramNotNull(table, "table");
		IActionBuilder builder = new BeanTablePackSelectedActionBuilder(table);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (builder != null) {
				builder = interceptor.packSelectedActionBuilder(table, builder);
			}
			else {
				break;
			}
		}
		return builder;
	}

	@Override
	public IAction packSelectedAction(final IBeanTable<BEAN_TYPE> table) {
		return packSelectedActionBuilder(table).build();
	}

	@Override
	public IActionBuilder clearCurrentSortActionBuilder(final IBeanTableModel<BEAN_TYPE> model) {
		Assert.paramNotNull(model, "model");
		IActionBuilder builder = new BeanTableClearCurrentSortActionBuilder(model);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (builder != null) {
				builder = interceptor.clearCurrentSortActionBuilder(model, builder);
			}
			else {
				break;
			}
		}
		return builder;
	}

	@Override
	public IAction clearCurrentSortAction(final IBeanTableModel<BEAN_TYPE> model) {
		return clearCurrentSortActionBuilder(model).build();
	}

	@Override
	public IActionBuilder clearDefaultSortActionBuilder(final IBeanTableModel<BEAN_TYPE> model) {
		Assert.paramNotNull(model, "model");
		IActionBuilder builder = new BeanTableClearDefaultSortActionBuilder(model);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (builder != null) {
				builder = interceptor.clearDefaultSortActionBuilder(model, builder);
			}
			else {
				break;
			}
		}
		return builder;
	}

	@Override
	public IAction clearDefaultSortAction(final IBeanTableModel<BEAN_TYPE> model) {
		return clearDefaultSortActionBuilder(model).build();
	}

	@Override
	public IActionBuilder addIncludingFilterActionBuilder(final IBeanTable<BEAN_TYPE> table, final int columnIndex) {
		IActionBuilder builder = new BeanTableAddIncludingFilterActionBuilder(table, columnIndex);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (builder != null) {
				builder = interceptor.addIncludingFilterActionBuilder(table, columnIndex, builder);
			}
			else {
				break;
			}
		}
		return builder;
	}

	@Override
	public IAction addIncludingFilterAction(final IBeanTable<BEAN_TYPE> table, final int columnIndex) {
		return addIncludingFilterActionBuilder(table, columnIndex).build();
	}

	@Override
	public IActionBuilder addExcludingFilterActionBuilder(final IBeanTable<BEAN_TYPE> table, final int columnIndex) {
		IActionBuilder builder = new BeanTableAddExcludingFilterActionBuilder(table, columnIndex);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (builder != null) {
				builder = interceptor.addExcludingFilterActionBuilder(table, columnIndex, builder);
			}
			else {
				break;
			}
		}
		return builder;
	}

	@Override
	public IAction addExcludingFilterAction(final IBeanTable<BEAN_TYPE> table, final int columnIndex) {
		return addExcludingFilterActionBuilder(table, columnIndex).build();
	}

	@Override
	public IActionBuilder addCustomFilterActionBuilder(final IBeanTable<BEAN_TYPE> table, final int columnIndex) {
		IActionBuilder builder = new BeanTableAddCustomFilterActionBuilder(table, columnIndex);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (builder != null) {
				builder = interceptor.addCustomFilterActionBuilder(table, columnIndex, builder);
			}
			else {
				break;
			}
		}
		return builder;
	}

	@Override
	public IAction addCustomFilterAction(final IBeanTable<BEAN_TYPE> table, final int columnIndex) {
		return addCustomFilterActionBuilder(table, columnIndex).build();
	}

	@Override
	public IActionBuilder addFilterActionBuilder(
		final IBeanTableModel<BEAN_TYPE> model,
		final IFilterType filterType,
		final int columnIndex) {
		IActionBuilder builder = new BeanTableAddFilterActionBuilder(model, filterType, columnIndex);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (builder != null) {
				builder = interceptor.addFilterActionBuilder(model, filterType, columnIndex, builder);
			}
			else {
				break;
			}
		}
		return builder;
	}

	@Override
	public IAction addFilterAction(final IBeanTableModel<BEAN_TYPE> model, final IFilterType filterType, final int columnIndex) {
		return addFilterActionBuilder(model, filterType, columnIndex).build();
	}

	@Override
	public IActionBuilder editFilterActionBuilder(final IBeanTableModel<BEAN_TYPE> model) {
		IActionBuilder builder = new BeanTableEditFilterActionBuilder(model);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (builder != null) {
				builder = interceptor.editFilterActionBuilder(model, builder);
			}
			else {
				break;
			}
		}
		return builder;
	}

	@Override
	public IAction editFilterAction(final IBeanTableModel<BEAN_TYPE> model) {
		return editFilterActionBuilder(model).build();
	}

	@Override
	public IActionBuilder deleteFilterActionBuilder(final IBeanTableModel<BEAN_TYPE> model) {
		IActionBuilder builder = new BeanTableDeleteFilterActionBuilder(model);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (builder != null) {
				builder = interceptor.deleteFilterActionBuilder(model, builder);
			}
			else {
				break;
			}
		}
		return builder;
	}

	@Override
	public IAction deleteFilterAction(final IBeanTableModel<BEAN_TYPE> model) {
		return deleteFilterActionBuilder(model).build();
	}

	@Override
	public IActionBuilder deleteColumnFiltersActionBuilder(final IBeanTableModel<BEAN_TYPE> model, final int columnIndex) {
		IActionBuilder builder = new BeanTableDeleteColumnFiltersActionBuilder(model, columnIndex);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (builder != null) {
				builder = interceptor.deleteColumnFiltersActionBuilder(model, columnIndex, builder);
			}
			else {
				break;
			}
		}
		return builder;
	}

	@Override
	public IAction deleteColumnFiltersAction(final IBeanTableModel<BEAN_TYPE> model, final int columnIndex) {
		return deleteColumnFiltersActionBuilder(model, columnIndex).build();
	}

	@Override
	public ICreatorActionBuilder<BEAN_TYPE> creatorActionBuilder(final IBeanTable<BEAN_TYPE> table) {
		Assert.paramNotNull(table, "table");
		Assert.paramNotNull(table.getModel(), "table.getModel()");
		Assert.paramNotNull(table.getModel().getCreatorService(), "table.getModel().getCreatorService()");
		ICreatorActionBuilder<BEAN_TYPE> builder = BeanTableCreatorActionBuilderFactory.createBuilder(table);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (builder != null) {
				builder = interceptor.creatorActionBuilder(table, builder);
			}
			else {
				break;
			}
		}
		return builder;
	}

	@Override
	public IAction creatorAction(final IBeanTable<BEAN_TYPE> table) {
		final ICreatorActionBuilder<BEAN_TYPE> builder = creatorActionBuilder(table);
		if (builder != null) {
			return builder.build();
		}
		else {
			return null;
		}
	}

	@Override
	public IDeleterActionBuilder<BEAN_TYPE> deleterActionBuilder(final IBeanTable<BEAN_TYPE> table) {
		Assert.paramNotNull(table, "table");
		Assert.paramNotNull(table.getModel(), "table.getModel()");
		Assert.paramNotNull(table.getModel().getDeleterService(), "table.getModel().getDeleterService()");
		IDeleterActionBuilder<BEAN_TYPE> builder = BeanTableDeleterActionBuilderFactory.createBuilder(table);
		for (final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor : interceptors) {
			if (builder != null) {
				builder = interceptor.deleterActionBuilder(table, builder);
			}
			else {
				break;
			}
		}
		return builder;
	}

	@Override
	public IAction deleterAction(final IBeanTable<BEAN_TYPE> table) {
		final IDeleterActionBuilder<BEAN_TYPE> builder = deleterActionBuilder(table);
		if (builder != null) {
			return builder.build();
		}
		else {
			return null;
		}
	}

}
