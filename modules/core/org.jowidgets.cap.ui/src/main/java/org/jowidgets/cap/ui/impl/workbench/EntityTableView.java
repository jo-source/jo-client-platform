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

package org.jowidgets.cap.ui.impl.workbench;

import java.util.Collection;
import java.util.List;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.plugin.IEntityComponentMasterTableViewPlugin;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProperties;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.workbench.api.IViewContext;
import org.jowidgets.workbench.tools.AbstractView;

class EntityTableView extends AbstractView {

	private static final IMessage ADD = Messages.getMessage("EntityTableView.add");

	private final IBeanTable<?> table;

	EntityTableView(final IViewContext context, final IBeanTableModel<?> tableModel, final Collection<IAction> linkCreatorActions) {

		final IContainer container = context.getContainer();
		container.setLayout(MigLayoutFactory.growingInnerCellLayout());

		final IBeanTableBluePrint<?> tableBp = CapUiToolkit.bluePrintFactory().beanTable(tableModel);
		this.table = container.add(tableBp, MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

		final IPluginPropertiesBuilder propBuilder = PluginProperties.builder();
		propBuilder.add(IEntityComponentMasterTableViewPlugin.BEAN_TYPE_PROPERTY_KEY, tableModel.getBeanType());
		propBuilder.add(IEntityComponentMasterTableViewPlugin.ENTITIY_ID_PROPERTY_KEY, tableModel.getEntityId());
		final IPluginProperties pluginProperties = propBuilder.build();

		final List<IEntityComponentMasterTableViewPlugin> plugins = PluginProvider.getPlugins(
				IEntityComponentMasterTableViewPlugin.ID,
				pluginProperties);
		for (final IEntityComponentMasterTableViewPlugin plugin : plugins) {
			plugin.onInitialize(pluginProperties, context, table, linkCreatorActions);
		}

		addLinkActions(table, linkCreatorActions);

		tableModel.load();
	}

	private void addLinkActions(final IBeanTable<?> table, final Collection<IAction> actions) {
		IMenuModel menu = table.getCellPopMenu();
		if (actions.size() > 3) {
			menu = menu.addMenu(ADD.get());
		}
		for (final IAction action : actions) {
			menu.addAction(action);
		}
	}

	IBeanTable<?> getTable() {
		return table;
	}

}
