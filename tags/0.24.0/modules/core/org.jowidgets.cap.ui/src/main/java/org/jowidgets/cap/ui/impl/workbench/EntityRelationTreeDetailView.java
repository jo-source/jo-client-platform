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

import java.util.Collection;
import java.util.List;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.plugin.IEntityComponentRelationTreeDetailViewPlugin;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTree;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeDetail;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeDetailBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProperties;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.workbench.api.IViewContext;
import org.jowidgets.workbench.tools.AbstractView;

final class EntityRelationTreeDetailView extends AbstractView {

	public static final String ID = EntityRelationTreeDetailView.class.getName();
	public static final IMessage DEFAULT_LABEL = Messages.getMessage("EntityRelationTreeDetailView.details");

	private final IEntityService entityService;
	private final IViewContext context;

	private boolean intialized;

	EntityRelationTreeDetailView(final IViewContext context) {

		this.context = context;

		this.entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService == null) {
			throw new IllegalStateException("No entity service found");
		}

		this.intialized = false;
	}

	void initialize(
		final IBeanTable<?> rootTable,
		final IBeanRelationTree<?> relationTree,
		final Collection<IAction> linkCreatorActions) {
		if (!intialized) {
			final IContainer container = context.getContainer();
			container.setLayout(MigLayoutFactory.growingInnerCellLayout());
			final ICapApiBluePrintFactory cbpf = CapUiToolkit.bluePrintFactory();
			final IBeanRelationTreeDetailBluePrint<?> treeDetailBp = cbpf.beanRelationTreeDetail(relationTree.getModel());

			final IBeanRelationTreeDetail<?> relationTreeDetail = container.add(
					treeDetailBp,
					MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

			final IBeanTableModel<?> rootTableModel = rootTable.getModel();
			final IPluginPropertiesBuilder propBuilder = PluginProperties.builder();
			propBuilder.add(IEntityComponentRelationTreeDetailViewPlugin.BEAN_TYPE_PROPERTY_KEY, rootTableModel.getBeanType());
			propBuilder.add(IEntityComponentRelationTreeDetailViewPlugin.ENTITIY_ID_PROPERTY_KEY, rootTableModel.getEntityId());
			final IPluginProperties pluginProperties = propBuilder.build();

			final List<IEntityComponentRelationTreeDetailViewPlugin> plugins = PluginProvider.getPlugins(
					IEntityComponentRelationTreeDetailViewPlugin.ID,
					pluginProperties);
			for (final IEntityComponentRelationTreeDetailViewPlugin plugin : plugins) {
				plugin.onInitialize(pluginProperties, context, rootTable, relationTree, relationTreeDetail, linkCreatorActions);
			}

			intialized = true;
		}
	}
}
