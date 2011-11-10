/*
 * Copyright (c) 2011 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original Author: grossmann
 * Creation Date: 18.10.2011
 */

package org.jowidgets.cap.sample1.plugins.ui;

import org.jowidgets.cap.sample1.common.entity.EntityIds;
import org.jowidgets.cap.sample1.common.entity.IUser;
import org.jowidgets.cap.sample1.plugins.ui.table.UserMenuPlugin;
import org.jowidgets.cap.ui.api.plugin.IBeanTableMenuPlugin;
import org.jowidgets.plugin.tools.PluginProviderBuilder;

public final class Sample1PluginProviderBuilder extends PluginProviderBuilder {

	public Sample1PluginProviderBuilder() {
		addPlugin(
				IBeanTableMenuPlugin.ID,
				new UserMenuPlugin(),
				IBeanTableMenuPlugin.ENTITIY_ID_PROPERTY_KEY,
				IUser.class,
				EntityIds.VIRTUAL_USERS_OF_ROLES);
	}

}
