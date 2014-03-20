/*
 * Copyright (c) 2012, grossmann
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

final class BeanTableMenuInterceptorComposite {

	private BeanTableMenuInterceptorComposite() {}

	static <BEAN_TYPE> IBeanTableMenuInterceptor<BEAN_TYPE> create(
		final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor1,
		final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor2) {

		if (interceptor1 != null && interceptor2 != null) {
			return new BeanTableMenuInterceptorImpl<BEAN_TYPE>(interceptor1, interceptor2);
		}
		else if (interceptor1 != null) {
			return interceptor1;
		}
		else if (interceptor2 != null) {
			return interceptor2;
		}
		else {
			return null;
		}
	}

	private static final class BeanTableMenuInterceptorImpl<BEAN_TYPE> implements IBeanTableMenuInterceptor<BEAN_TYPE> {

		private final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor1;
		private final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor2;

		private BeanTableMenuInterceptorImpl(
			final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor1,
			final IBeanTableMenuInterceptor<BEAN_TYPE> interceptor2) {
			this.interceptor1 = interceptor1;
			this.interceptor2 = interceptor2;
		}

		@Override
		public IMenuModel headerPopupMenu(final IBeanTable<?> table, final int columnIndex, final IMenuModel menuModel) {
			IMenuModel result = interceptor1.headerPopupMenu(table, columnIndex, menuModel);
			if (result != null) {
				result = interceptor2.headerPopupMenu(table, columnIndex, result);
			}
			return result;
		}

		@Override
		public IMenuModel cellPopupMenu(
			final IBeanTable<?> table,
			final IMenuModel headerPopupMenuModel,
			final int columnIndex,
			final IMenuModel menuModel) {
			IMenuModel result = interceptor1.cellPopupMenu(table, headerPopupMenuModel, columnIndex, menuModel);
			if (result != null) {
				result = interceptor2.cellPopupMenu(table, headerPopupMenuModel, columnIndex, result);
			}
			return result;
		}

		@Override
		public IMenuModel columnsVisibilityMenu(final IBeanTableModel<?> model, final IMenuModel menuModel) {
			IMenuModel result = interceptor1.columnsVisibilityMenu(model, menuModel);
			if (result != null) {
				result = interceptor2.columnsVisibilityMenu(model, result);
			}
			return result;
		}

		@Override
		public IMenuModel filterMenu(final IBeanTable<?> table, final IMenuModel menuModel) {
			IMenuModel result = interceptor1.filterMenu(table, menuModel);
			if (result != null) {
				result = interceptor2.filterMenu(table, result);
			}
			return result;
		}

		@Override
		public IMenuModel filterCellMenu(final IBeanTable<?> table, final int columnIndex, final IMenuModel menuModel) {
			IMenuModel result = interceptor1.filterCellMenu(table, columnIndex, menuModel);
			if (result != null) {
				result = interceptor2.filterCellMenu(table, columnIndex, result);
			}
			return result;
		}

		@Override
		public IMenuModel filterHeaderMenu(final IBeanTable<?> table, final int columnIndex, final IMenuModel menuModel) {
			IMenuModel result = interceptor1.filterHeaderMenu(table, columnIndex, menuModel);
			if (result != null) {
				result = interceptor2.filterHeaderMenu(table, columnIndex, result);
			}
			return result;
		}

		@Override
		public IMenuModel alignmentMenu(final IBeanTableModel<?> model, final int columnIndex, final IMenuModel menuModel) {
			IMenuModel result = interceptor1.alignmentMenu(model, columnIndex, menuModel);
			if (result != null) {
				result = interceptor2.alignmentMenu(model, columnIndex, result);
			}
			return result;
		}

		@Override
		public IMenuModel headerFormatMenu(final IBeanTableModel<?> model, final int columnIndex, final IMenuModel menuModel) {
			IMenuModel result = interceptor1.headerFormatMenu(model, columnIndex, menuModel);
			if (result != null) {
				result = interceptor2.alignmentMenu(model, columnIndex, result);
			}
			return result;
		}

		@Override
		public IMenuModel contentFormatMenu(final IBeanTableModel<?> model, final int columnIndex, final IMenuModel menuModel) {
			IMenuModel result = interceptor1.contentFormatMenu(model, columnIndex, menuModel);
			if (result != null) {
				result = interceptor2.contentFormatMenu(model, columnIndex, result);
			}
			return result;
		}

		@Override
		public IMenuModel currentSortMenu(final IBeanTableModel<?> model, final int columnIndex, final IMenuModel menuModel) {
			IMenuModel result = interceptor1.currentSortMenu(model, columnIndex, menuModel);
			if (result != null) {
				result = interceptor2.currentSortMenu(model, columnIndex, result);
			}
			return result;
		}

		@Override
		public IMenuModel defaultSortMenu(final IBeanTableModel<?> model, final int columnIndex, final IMenuModel menuModel) {
			IMenuModel result = interceptor1.defaultSortMenu(model, columnIndex, menuModel);
			if (result != null) {
				result = interceptor2.defaultSortMenu(model, columnIndex, result);
			}
			return result;
		}

		@Override
		public IActionBuilder settingsActionBuilder(final IBeanTable<BEAN_TYPE> table, final IActionBuilder builder) {
			IActionBuilder result = interceptor1.settingsActionBuilder(table, builder);
			if (result != null) {
				result = interceptor2.settingsActionBuilder(table, result);
			}
			return result;
		}

		@Override
		public IExecutorActionBuilder<BEAN_TYPE, ICsvExportParameter> csvExportActionBuilder(
			final IBeanTableModel<BEAN_TYPE> model,
			final IExecutorActionBuilder<BEAN_TYPE, ICsvExportParameter> builder) {
			IExecutorActionBuilder<BEAN_TYPE, ICsvExportParameter> result = interceptor1.csvExportActionBuilder(model, builder);
			if (result != null) {
				result = interceptor2.csvExportActionBuilder(model, result);
			}
			return result;
		}

		@Override
		public IActionBuilder hideColumnActionBuilder(
			final IBeanTable<BEAN_TYPE> table,
			final int columnIndex,
			final IActionBuilder builder) {
			IActionBuilder result = interceptor1.hideColumnActionBuilder(table, columnIndex, builder);
			if (result != null) {
				result = interceptor2.hideColumnActionBuilder(table, columnIndex, result);
			}
			return result;
		}

		@Override
		public IActionBuilder showAllColumnsActionBuilder(final IBeanTable<?> table, final IActionBuilder builder) {
			IActionBuilder result = interceptor1.showAllColumnsActionBuilder(table, builder);
			if (result != null) {
				result = interceptor2.showAllColumnsActionBuilder(table, result);
			}
			return result;
		}

		@Override
		public IActionBuilder packAllActionBuilder(final IBeanTable<?> table, final IActionBuilder builder) {
			IActionBuilder result = interceptor1.packAllActionBuilder(table, builder);
			if (result != null) {
				result = interceptor2.packAllActionBuilder(table, result);
			}
			return result;
		}

		@Override
		public IActionBuilder packSelectedActionBuilder(final IBeanTable<?> table, final IActionBuilder builder) {
			IActionBuilder result = interceptor1.packSelectedActionBuilder(table, builder);
			if (result != null) {
				result = interceptor2.packSelectedActionBuilder(table, result);
			}
			return result;
		}

		@Override
		public IActionBuilder clearCurrentSortActionBuilder(final IBeanTableModel<?> model, final IActionBuilder builder) {
			IActionBuilder result = interceptor1.clearCurrentSortActionBuilder(model, builder);
			if (result != null) {
				result = interceptor2.clearCurrentSortActionBuilder(model, result);
			}
			return result;
		}

		@Override
		public IActionBuilder clearDefaultSortActionBuilder(final IBeanTableModel<?> model, final IActionBuilder builder) {
			IActionBuilder result = interceptor1.clearDefaultSortActionBuilder(model, builder);
			if (result != null) {
				result = interceptor2.clearDefaultSortActionBuilder(model, result);
			}
			return result;
		}

		@Override
		public IActionBuilder addIncludingFilterActionBuilder(
			final IBeanTable<?> table,
			final int columnIndex,
			final IActionBuilder builder) {
			IActionBuilder result = interceptor1.addIncludingFilterActionBuilder(table, columnIndex, builder);
			if (result != null) {
				result = interceptor2.addIncludingFilterActionBuilder(table, columnIndex, result);
			}
			return result;
		}

		@Override
		public IActionBuilder addExcludingFilterActionBuilder(
			final IBeanTable<?> table,
			final int columnIndex,
			final IActionBuilder builder) {
			IActionBuilder result = interceptor1.addExcludingFilterActionBuilder(table, columnIndex, builder);
			if (result != null) {
				result = interceptor2.addExcludingFilterActionBuilder(table, columnIndex, result);
			}
			return result;
		}

		@Override
		public IActionBuilder addCustomFilterActionBuilder(
			final IBeanTable<?> table,
			final int columnIndex,
			final IActionBuilder builder) {
			IActionBuilder result = interceptor1.addCustomFilterActionBuilder(table, columnIndex, builder);
			if (result != null) {
				result = interceptor2.addCustomFilterActionBuilder(table, columnIndex, result);
			}
			return result;
		}

		@Override
		public IActionBuilder addFilterActionBuilder(
			final IBeanTableModel<?> model,
			final IFilterType filterType,
			final int columnIndex,
			final IActionBuilder builder) {
			IActionBuilder result = interceptor1.addFilterActionBuilder(model, filterType, columnIndex, builder);
			if (result != null) {
				result = interceptor2.addFilterActionBuilder(model, filterType, columnIndex, result);
			}
			return result;
		}

		@Override
		public IActionBuilder editFilterActionBuilder(final IBeanTableModel<?> model, final IActionBuilder builder) {
			IActionBuilder result = interceptor1.editFilterActionBuilder(model, builder);
			if (result != null) {
				result = interceptor2.editFilterActionBuilder(model, result);
			}
			return result;
		}

		@Override
		public IActionBuilder deleteFilterActionBuilder(final IBeanTableModel<?> model, final IActionBuilder builder) {
			IActionBuilder result = interceptor1.deleteFilterActionBuilder(model, builder);
			if (result != null) {
				result = interceptor2.deleteFilterActionBuilder(model, result);
			}
			return result;
		}

		@Override
		public IActionBuilder deleteColumnFiltersActionBuilder(
			final IBeanTableModel<?> model,
			final int columnIndex,
			final IActionBuilder builder) {
			IActionBuilder result = interceptor1.deleteColumnFiltersActionBuilder(model, columnIndex, builder);
			if (result != null) {
				result = interceptor2.deleteColumnFiltersActionBuilder(model, columnIndex, result);
			}
			return result;
		}

		@Override
		public ICreatorActionBuilder<BEAN_TYPE> creatorActionBuilder(
			final IBeanTable<BEAN_TYPE> table,
			final ICreatorActionBuilder<BEAN_TYPE> builder) {
			ICreatorActionBuilder<BEAN_TYPE> result = interceptor1.creatorActionBuilder(table, builder);
			if (result != null) {
				result = interceptor2.creatorActionBuilder(table, result);
			}
			return result;
		}

		@Override
		public IDeleterActionBuilder<BEAN_TYPE> deleterActionBuilder(
			final IBeanTable<BEAN_TYPE> table,
			final IDeleterActionBuilder<BEAN_TYPE> builder) {
			IDeleterActionBuilder<BEAN_TYPE> result = interceptor1.deleterActionBuilder(table, builder);
			if (result != null) {
				result = interceptor2.deleterActionBuilder(table, result);
			}
			return result;
		}

		@Override
		public ICopyActionBuilder<BEAN_TYPE> copyActionBuilder(
			final IBeanTable<BEAN_TYPE> table,
			final ICopyActionBuilder<BEAN_TYPE> builder) {
			ICopyActionBuilder<BEAN_TYPE> result = interceptor1.copyActionBuilder(table, builder);
			if (result != null) {
				result = interceptor2.copyActionBuilder(table, result);
			}
			return result;
		}

		@Override
		public IPasteBeansActionBuilder<BEAN_TYPE> pasteBeansActionBuilder(
			final IBeanTable<BEAN_TYPE> table,
			final IPasteBeansActionBuilder<BEAN_TYPE> builder) {
			IPasteBeansActionBuilder<BEAN_TYPE> result = interceptor1.pasteBeansActionBuilder(table, builder);
			if (result != null) {
				result = interceptor2.pasteBeansActionBuilder(table, result);
			}
			return result;
		}

	}

}
