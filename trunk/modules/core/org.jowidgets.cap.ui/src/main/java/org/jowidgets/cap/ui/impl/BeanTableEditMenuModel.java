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
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.tools.model.item.MenuModel;

final class BeanTableEditMenuModel<BEAN_TYPE> extends MenuModel {

	BeanTableEditMenuModel(
		final IBeanTable<BEAN_TYPE> table,
		final int columnIndex,
		final IBeanTableMenuFactory<BEAN_TYPE> menuFactory) {
		super(Messages.getString("BeanTableEditMenuModel.edit"), IconsSmall.EDIT);

		final IBeanTableModel<BEAN_TYPE> model = table.getModel();

		final IAttribute<Object> attribute = model.getAttribute(columnIndex);
		if (attribute.isBatchEditable()) {
			if (hasPropertyEditor(attribute)) {
				tryAddAction(menuFactory.editAllAction(table, columnIndex));
			}
			tryAddAction(menuFactory.setToAllAction(table, columnIndex));

		}
	}

	private static boolean hasPropertyEditor(final IAttribute<?> attribute) {
		final IControlPanelProvider<?> controlPanel = attribute.getCurrentControlPanel();
		if (controlPanel != null) {
			if (attribute.isCollectionType()) {
				return controlPanel.getCollectionControlCreator() != null;
			}
			else {
				return controlPanel.getControlCreator() != null;
			}
		}
		return false;
	}

	private void tryAddAction(final IAction action) {
		if (action != null) {
			addAction(action);
		}
	}

}
