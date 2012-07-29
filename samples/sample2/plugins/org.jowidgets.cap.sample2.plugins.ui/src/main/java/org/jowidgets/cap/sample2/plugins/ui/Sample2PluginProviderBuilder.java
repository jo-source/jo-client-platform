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

package org.jowidgets.cap.sample2.plugins.ui;

import org.jowidgets.cap.sample2.app.common.bean.IPerson;
import org.jowidgets.cap.sample2.app.common.bean.IRole;
import org.jowidgets.cap.sample2.app.common.entity.EntityIds;
import org.jowidgets.cap.sample2.plugins.ui.bean.PersonLabelRendererPlugin;
import org.jowidgets.cap.sample2.plugins.ui.bean.RoleLabelRendererPlugin;
import org.jowidgets.cap.sample2.plugins.ui.selection.PersonSelectionProvider;
import org.jowidgets.cap.sample2.plugins.ui.table.PersonMenuContributionPlugin;
import org.jowidgets.cap.sample2.plugins.ui.table.PersonMenuInterceptorPlugin;
import org.jowidgets.cap.sample2.plugins.ui.table.PersonTablePlugin;
import org.jowidgets.cap.sample2.plugins.ui.table.RoleMenuInterceptorPlugin;
import org.jowidgets.cap.sample2.plugins.ui.tree.RoleRelationTreePlugin;
import org.jowidgets.cap.ui.api.plugin.IBeanProxyLabelRendererPlugin;
import org.jowidgets.cap.ui.api.plugin.IBeanRelationTreePlugin;
import org.jowidgets.cap.ui.api.plugin.IBeanSelectionProviderPlugin;
import org.jowidgets.cap.ui.api.plugin.IBeanTableMenuContributionPlugin;
import org.jowidgets.cap.ui.api.plugin.IBeanTableMenuInterceptorPlugin;
import org.jowidgets.cap.ui.api.plugin.IBeanTablePlugin;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModel;
import org.jowidgets.plugin.api.IPluginFilter;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.tools.PluginProviderBuilder;

public final class Sample2PluginProviderBuilder extends PluginProviderBuilder {

	public Sample2PluginProviderBuilder() {
		addPlugin(
				IBeanTableMenuInterceptorPlugin.ID,
				new PersonMenuInterceptorPlugin(),
				IBeanTableMenuInterceptorPlugin.ENTITIY_ID_PROPERTY_KEY,
				EntityIds.PERSON,
				EntityIds.LINKED_PERSONS_OF_ROLES);

		addPlugin(
				IBeanTableMenuContributionPlugin.ID,
				new PersonMenuContributionPlugin(),
				IBeanTableMenuContributionPlugin.ENTITIY_ID_PROPERTY_KEY,
				EntityIds.PERSON,
				EntityIds.LINKED_PERSONS_OF_ROLES);

		addPlugin(
				IBeanTableMenuInterceptorPlugin.ID,
				new RoleMenuInterceptorPlugin(),
				IBeanTableMenuInterceptorPlugin.ENTITIY_ID_PROPERTY_KEY,
				EntityIds.ROLE);

		addPlugin(
				IBeanProxyLabelRendererPlugin.ID,
				new PersonLabelRendererPlugin(),
				IBeanProxyLabelRendererPlugin.BEAN_TYPE_PROPERTY_KEY,
				IPerson.class);

		addPlugin(
				IBeanProxyLabelRendererPlugin.ID,
				new RoleLabelRendererPlugin(),
				IBeanProxyLabelRendererPlugin.ENTITIY_ID_PROPERTY_KEY,
				EntityIds.ROLE,
				EntityIds.LINKED_ROLES_OF_PERSONS);

		addPlugin(IBeanTablePlugin.ID, new PersonTablePlugin(), IBeanTablePlugin.ENTITIY_ID_PROPERTY_KEY, EntityIds.PERSON);

		addPlugin(
				IBeanRelationTreePlugin.ID,
				new RoleRelationTreePlugin<IRole>(),
				IBeanRelationTreePlugin.ENTITIY_ID_PROPERTY_KEY,
				EntityIds.ROLE);

		addPlugin(IBeanSelectionProviderPlugin.ID, new PersonSelectionProvider(), new IPluginFilter() {
			@Override
			public boolean accept(final IPluginProperties properties) {
				if (!properties.getValue(IBeanSelectionProviderPlugin.SELECTION_EMPTY_PROPERTY_KEY)
					&& properties.getValue(IBeanSelectionProviderPlugin.BEAN_TYPE_PROPERTY_KEY) == IPerson.class
					&& (!IBeanRelationNodeModel.class.isAssignableFrom(properties.getValue(IBeanSelectionProviderPlugin.SELECTION_SOURCE_TYPE_PROPERTY_KEY)))) {
					return true;
				}
				return false;
			}
		});
	}
}
