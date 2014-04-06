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

package org.jowidgets.cap.ui.tools.workbench;

import java.util.Collection;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.api.model.item.IToolBarModel;
import org.jowidgets.cap.ui.api.plugin.IEntityComponentMasterTableViewPlugin;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.workbench.api.IViewContext;

public final class EntityComponentMasterTableToolBarPlugin implements IEntityComponentMasterTableViewPlugin {

	@Override
	public void onInitialize(
		final IPluginProperties properties,
		final IViewContext context,
		final IBeanTable<?> table,
		final Collection<IAction> linkCreatorActions) {

		final IToolBarModel toolBar = context.getToolBar();
		if (table.getDefaultCreatorAction() != null) {
			toolBar.addAction(table.getDefaultCreatorAction());
		}

		final IMenuModel toolBarMenu = context.getToolBarMenu();
		for (final IAction action : linkCreatorActions) {
			toolBarMenu.addAction(action);
		}

	}

}
