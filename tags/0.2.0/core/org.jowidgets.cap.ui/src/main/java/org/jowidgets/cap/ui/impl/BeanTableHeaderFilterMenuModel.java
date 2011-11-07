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

import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.tools.model.item.MenuModel;

final class BeanTableHeaderFilterMenuModel extends MenuModel {

	@SuppressWarnings({"unchecked", "rawtypes"})
	BeanTableHeaderFilterMenuModel(final IBeanTable<?> table, final int columnIndex) {
		super(Messages.getString("BeanTableHeaderFilterMenuModel.filter"), IconsSmall.FILTER); //$NON-NLS-1$

		final IBeanTableModel<?> model = table.getModel();

		final IBeanTableMenuFactory menuFactory = CapUiToolkit.beanTableMenuFactory();

		final IAttribute<Object> attribute = model.getAttribute(columnIndex);
		if (attribute.isFilterable()) {
			final List<IFilterType> filterTypes = attribute.getSupportedFilterTypes();
			if (filterTypes.size() > 0) {
				for (final IFilterType filterType : filterTypes) {
					if (attribute.getFilterPanelProvider(filterType).isApplicableWith((List) model.getAttributes())) {
						addAction(menuFactory.addFilterAction(model, filterType, columnIndex));
					}
				}
			}
			addAction(menuFactory.deleteColumnFiltersAction(model, columnIndex));
			addSeparator();
		}
		addAction(menuFactory.editFilterAction(model));
		addAction(menuFactory.deleteFilterAction(model));
		addSeparator();
		addItem(table.getSearchFilterItemModel());
	}

}
