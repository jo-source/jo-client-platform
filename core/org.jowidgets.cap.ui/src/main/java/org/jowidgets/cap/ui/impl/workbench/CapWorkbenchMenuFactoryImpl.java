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

package org.jowidgets.cap.ui.impl.workbench;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.model.item.IMenuBarModel;
import org.jowidgets.api.model.item.IMenuItemModel;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.api.model.item.ISeparatorItemModel;
import org.jowidgets.api.model.item.IToolBarItemModel;
import org.jowidgets.api.model.item.IToolBarModel;
import org.jowidgets.cap.ui.api.command.IDataModelAction;
import org.jowidgets.cap.ui.api.plugin.IWorkbenchMenuInterceptorPlugin;
import org.jowidgets.cap.ui.api.workbench.CapWorkbenchActionsProvider;
import org.jowidgets.cap.ui.api.workbench.ICapWorkbenchMenuFactory;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.tools.model.item.MenuBarModel;
import org.jowidgets.tools.model.item.MenuModel;
import org.jowidgets.tools.model.item.ToolBarModel;

final class CapWorkbenchMenuFactoryImpl implements ICapWorkbenchMenuFactory {

	@Override
	public IMenuBarModel menuBar() {
		final MenuBarModel result = new MenuBarModel();
		final IMenuModel dataMenu = dataMenu();
		if (dataMenu != null) {
			result.addMenu(dataMenu);
		}
		return result;
	}

	@Override
	public IMenuModel dataMenu() {
		//TODO i18n
		IMenuModel result = new MenuModel(Messages.getString("CapWorkbenchMenuFactoryImpl.data"));
		final IDataModelAction loadAction = CapWorkbenchActionsProvider.loadAction();
		boolean separator = false;
		if (loadAction != null) {
			result.addAction(loadAction);
			separator = true;
		}
		final IDataModelAction cancelAction = CapWorkbenchActionsProvider.cancelAction();
		if (cancelAction != null) {
			result.addAction(cancelAction);
			separator = true;
		}
		if (separator) {
			result.addSeparator();
			separator = false;
		}

		final IDataModelAction undoAction = CapWorkbenchActionsProvider.undoAction();
		if (undoAction != null) {
			result.addAction(undoAction);
			separator = true;
		}
		final IDataModelAction saveAction = CapWorkbenchActionsProvider.saveAction();
		if (saveAction != null) {
			result.addAction(saveAction);
			separator = true;
		}
		if (separator) {
			result.addSeparator();
		}
		final IAction refreshLookUpsAction = CapWorkbenchActionsProvider.refreshLookUpsAction();
		if (refreshLookUpsAction != null) {
			result.addAction(refreshLookUpsAction);
		}

		final int size = result.getChildren().size();
		if (size > 0) {
			final IMenuItemModel itemModel = result.getChildren().get(size - 1);
			if (itemModel instanceof ISeparatorItemModel) {
				result.removeItem(itemModel);
			}
		}

		//Modify with plugins
		for (final IWorkbenchMenuInterceptorPlugin plugin : PluginProvider.getPlugins(IWorkbenchMenuInterceptorPlugin.ID)) {
			result = plugin.getMenuInterceptor().dataMenuModel(result);
			if (result == null) {
				break;
			}
		}

		return result;
	}

	@Override
	public IToolBarModel toolBar() {
		IToolBarModel result = new ToolBarModel();
		final IDataModelAction loadAction = CapWorkbenchActionsProvider.loadAction();
		boolean separator = false;
		if (loadAction != null) {
			result.addAction(loadAction);
			separator = true;
		}
		final IDataModelAction cancelAction = CapWorkbenchActionsProvider.cancelAction();
		if (cancelAction != null) {
			result.addAction(cancelAction);
			separator = true;
		}
		if (separator) {
			result.addSeparator();
			separator = false;
		}

		final IDataModelAction undoAction = CapWorkbenchActionsProvider.undoAction();
		if (undoAction != null) {
			result.addAction(undoAction);
		}
		final IDataModelAction saveAction = CapWorkbenchActionsProvider.saveAction();
		if (saveAction != null) {
			result.addAction(saveAction);
		}

		final int size = result.getItems().size();
		if (size > 0) {
			final IToolBarItemModel itemModel = result.getItems().get(size - 1);
			if (itemModel instanceof ISeparatorItemModel) {
				result.removeItem(itemModel);
			}
		}

		//Modify with plugins
		for (final IWorkbenchMenuInterceptorPlugin plugin : PluginProvider.getPlugins(IWorkbenchMenuInterceptorPlugin.ID)) {
			result = plugin.getMenuInterceptor().toolBarModel(result);
			if (result == null) {
				break;
			}
		}
		if (result != null) {
			return result;
		}
		else {
			return new ToolBarModel();
		}
	}
}
