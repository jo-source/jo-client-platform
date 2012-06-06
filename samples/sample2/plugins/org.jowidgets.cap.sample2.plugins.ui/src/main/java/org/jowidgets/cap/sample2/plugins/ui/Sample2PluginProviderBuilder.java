/*
 * Copyright (c) 2011 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original Author: grossmann
 * Creation Date: 18.10.2011
 */

package org.jowidgets.cap.sample2.plugins.ui;

import org.jowidgets.cap.sample2.app.common.bean.IPerson;
import org.jowidgets.cap.sample2.app.common.bean.IRole;
import org.jowidgets.cap.sample2.app.common.entity.EntityIds;
import org.jowidgets.cap.sample2.plugins.ui.bean.PersonLabelRendererPlugin;
import org.jowidgets.cap.sample2.plugins.ui.bean.RoleLabelRendererPlugin;
import org.jowidgets.cap.sample2.plugins.ui.table.PersonMenuContributionPlugin;
import org.jowidgets.cap.sample2.plugins.ui.table.PersonMenuInterceptorPlugin;
import org.jowidgets.cap.sample2.plugins.ui.table.PersonTablePlugin;
import org.jowidgets.cap.sample2.plugins.ui.table.RoleMenuInterceptorPlugin;
import org.jowidgets.cap.sample2.plugins.ui.tree.RoleRelationTreePlugin;
import org.jowidgets.cap.ui.api.plugin.IBeanProxyLabelRendererPlugin;
import org.jowidgets.cap.ui.api.plugin.IBeanRelationTreePlugin;
import org.jowidgets.cap.ui.api.plugin.IBeanTableMenuContributionPlugin;
import org.jowidgets.cap.ui.api.plugin.IBeanTableMenuInterceptorPlugin;
import org.jowidgets.cap.ui.api.plugin.IBeanTablePlugin;
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
	}

}
