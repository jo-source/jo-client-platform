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
import org.jowidgets.api.model.item.IActionItemModel;
import org.jowidgets.api.model.item.IItemModelFactory;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuFactory;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.tools.model.item.MenuModel;
import org.jowidgets.tools.model.item.SeparatorItemModel;

final class BeanTableHeaderMenuModel extends MenuModel {

	BeanTableHeaderMenuModel(final IBeanTable<?> table, final int columnIndex) {
		//TODO i18n
		super("Header");

		final IBeanTableMenuFactory menuFactory = CapUiToolkit.beanTableMenuFactory();
		final IAction clearCurrentSortAction = menuFactory.clearCurrentSortAction(table.getModel());
		final IAction clearDefaultSortAction = menuFactory.clearDefaultSortAction(table.getModel());
		final IAction packAllAction = menuFactory.packAllAction(table);
		final IAction packSelectedAction = menuFactory.packSelectedAction(table);
		final IAction hideColumnAction = menuFactory.hideColumnAction(table, columnIndex);
		final IMenuModel columnsVisibilityMenu = menuFactory.columnsVisibilityMenu(table.getModel());
		final IAction showAllColumnsAction = menuFactory.showAllColumnsAction(table);

		addAction(hideColumnAction);
		addItem(columnsVisibilityMenu);
		addAction(showAllColumnsAction);
		addSeparator();
		addItem(menuFactory.filterHeaderMenu(table.getModel(), columnIndex));

		//add separator
		addItem(new SeparatorItemModel());

		final IMenuModel currentSortMenu = menuFactory.currentSortMenu(table.getModel(), columnIndex);
		if (currentSortMenu != null) {
			addItem(currentSortMenu);
		}

		final IItemModelFactory itemModelFactory = Toolkit.getModelFactoryProvider().getItemModelFactory();

		final IActionItemModel clearCurrentSortingModel = itemModelFactory.actionItem();
		clearCurrentSortingModel.setAction(clearCurrentSortAction);
		addItem(clearCurrentSortingModel);

		addItem(new SeparatorItemModel());

		final IMenuModel defaultSortMenu = menuFactory.defaultSortMenu(table.getModel(), columnIndex);
		if (defaultSortMenu != null) {
			addItem(defaultSortMenu);
		}

		final IActionItemModel clearDefaultSortingModel = itemModelFactory.actionItem();
		clearDefaultSortingModel.setAction(clearDefaultSortAction);
		addItem(clearDefaultSortingModel);

		addItem(new SeparatorItemModel());

		//add alignment menu
		final IMenuModel alignmentMenu = menuFactory.alignmentMenu(table.getModel(), columnIndex);
		addItem(alignmentMenu);

		//add display format menus
		final IMenuModel headerFormatMenu = menuFactory.headerFormatMenu(table.getModel(), columnIndex);
		final IMenuModel contentFormatMenu = menuFactory.contentFormatMenu(table.getModel(), columnIndex);

		if (headerFormatMenu != null) {
			addItem(headerFormatMenu);
		}

		if (contentFormatMenu != null) {
			addItem(contentFormatMenu);
		}

		addSeparator();
		addAction(packSelectedAction);
		addAction(packAllAction);

	}

}
