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
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.plugin.IEntityComponentRelationTreeViewPlugin;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeMenuInterceptor;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModel;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTree;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProperties;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.workbench.api.IViewContext;
import org.jowidgets.workbench.tools.AbstractView;

class EntityRelationTreeView extends AbstractView {

	static final String ID = EntityRelationTreeView.class.getName();

	private final IViewContext context;

	private IBeanRelationTree<?> tree;

	private boolean intialized;

	EntityRelationTreeView(final IViewContext context) {
		this.context = context;
		this.intialized = false;
	}

	void initialize(
		final IBeanTable<?> rootTable,
		final IBeanRelationTreeModel<?> parentModel,
		final IBeanRelationTreeMenuInterceptor treeMenuInterceptor,
		final Collection<IAction> linkCreatorActions) {

		if (!intialized) {
			final IContainer container = context.getContainer();
			container.setLayout(MigLayoutFactory.growingInnerCellLayout());
			final IBeanRelationTreeBluePrint<?> beanRelationTreeBp = CapUiToolkit.bluePrintFactory().beanRelationTree(parentModel);
			beanRelationTreeBp.setRootCreatorAction(rootTable.getDefaultCreatorAction());
			beanRelationTreeBp.addMenuInterceptor(treeMenuInterceptor);
			this.tree = container.add(beanRelationTreeBp, MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

			final IBeanTableModel<?> rootTableModel = rootTable.getModel();
			final IPluginPropertiesBuilder propBuilder = PluginProperties.builder();
			propBuilder.add(IEntityComponentRelationTreeViewPlugin.BEAN_TYPE_PROPERTY_KEY, rootTableModel.getBeanType());
			propBuilder.add(IEntityComponentRelationTreeViewPlugin.ENTITIY_ID_PROPERTY_KEY, rootTableModel.getEntityId());
			final IPluginProperties pluginProperties = propBuilder.build();

			final List<IEntityComponentRelationTreeViewPlugin> plugins = PluginProvider.getPlugins(
					IEntityComponentRelationTreeViewPlugin.ID,
					pluginProperties);
			for (final IEntityComponentRelationTreeViewPlugin plugin : plugins) {
				plugin.onInitialize(pluginProperties, context, rootTable, tree, linkCreatorActions);
			}

			intialized = true;
		}
	}

	IBeanRelationTree<?> getTree() {
		return tree;
	}
}
