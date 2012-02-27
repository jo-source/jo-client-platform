/*
 * Copyright (c) 2011 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original Author: grossmann
 * Creation Date: 18.10.2011
 */

package org.jowidgets.cap.sample2.plugins.ui;

import org.jowidgets.cap.sample2.app.common.entity.EntityIds;
import org.jowidgets.cap.sample2.plugins.ui.table.PersonMenuContributionPlugin;
import org.jowidgets.cap.sample2.plugins.ui.table.PersonMenuInterceptorPlugin;
import org.jowidgets.cap.sample2.plugins.ui.table.RoleMenuInterceptorPlugin;
import org.jowidgets.cap.ui.api.plugin.IBeanTableMenuContributionPlugin;
import org.jowidgets.cap.ui.api.plugin.IBeanTableMenuInterceptorPlugin;
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

	}

}
