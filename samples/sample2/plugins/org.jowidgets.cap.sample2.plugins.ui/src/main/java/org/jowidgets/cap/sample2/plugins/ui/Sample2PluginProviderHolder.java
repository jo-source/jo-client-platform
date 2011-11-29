/*
 * Copyright (c) 2011 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original Author: grossmann
 * Creation Date: 18.10.2011
 */

package org.jowidgets.cap.sample2.plugins.ui;

import org.jowidgets.plugin.tools.PluginProviderHolder;

public class Sample2PluginProviderHolder extends PluginProviderHolder {

	public Sample2PluginProviderHolder() {
		super(new Sample2PluginProviderBuilder(), 2);
	}

}
