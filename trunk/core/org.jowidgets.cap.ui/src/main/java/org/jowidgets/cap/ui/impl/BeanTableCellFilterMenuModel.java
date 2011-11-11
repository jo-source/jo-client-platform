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

import java.util.List;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.model.item.IMenuItemModel;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.filter.IFilterSupport;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.cap.ui.api.filter.IIncludingFilterFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.tools.model.item.MenuModel;

final class BeanTableCellFilterMenuModel<BEAN_TYPE> extends MenuModel {

	@SuppressWarnings({"unchecked", "rawtypes"})
	BeanTableCellFilterMenuModel(
		final IBeanTable<BEAN_TYPE> table,
		final int columnIndex,
		final IBeanTableMenuFactory<BEAN_TYPE> menuFactory) {
		super(Messages.getString("BeanTableCellFilterMenuModel.filter"), IconsSmall.FILTER); //$NON-NLS-1$

		final IBeanTableModel<BEAN_TYPE> model = table.getModel();

		final IAttribute<Object> attribute = model.getAttribute(columnIndex);
		if (attribute.isFilterable()) {
			tryAddAction(menuFactory.addIncludingFilterAction(model, columnIndex));
			tryAddAction(menuFactory.addExcludingFilterAction(model, columnIndex));
			if (hasCustomFilterSupport(attribute)) {
				tryAddAction(menuFactory.addCustomFilterAction(model, columnIndex));
			}
			final List<IFilterType> filterTypes = attribute.getSupportedFilterTypes();
			if (filterTypes.size() > 0) {
				boolean separatorAdded = false;
				for (final IFilterType filterType : filterTypes) {
					if (attribute.getFilterPanelProvider(filterType).isApplicableWith((List) model.getAttributes())) {
						if (!separatorAdded) {
							addSeparator();
							separatorAdded = true;
						}
						addAction(menuFactory.addFilterAction(model, filterType, columnIndex));
					}
				}
			}
			addSeparator();
		}
		tryAddAction(menuFactory.editFilterAction(model));
		tryAddAction(menuFactory.deleteFilterAction(model));
		addSeparator();
		tryAddItem(table.getFilterToolbarItemModel());
		tryAddItem(table.getSearchFilterToolbarItemModel());
	}

	private static boolean hasCustomFilterSupport(final IAttribute<?> attribute) {
		final IFilterSupport<Object> filterSupport = attribute.getCurrentControlPanel().getFilterSupport();
		final IIncludingFilterFactory<Object> includingFilterFactory = filterSupport.getIncludingFilterFactory();
		return attribute.getSupportedFilterTypes().contains(includingFilterFactory.getFilterType());
	}

	private void tryAddAction(final IAction action) {
		if (action != null) {
			addAction(action);
		}
	}

	private void tryAddItem(final IMenuItemModel item) {
		if (item != null) {
			addItem(item);
		}
	}

}
