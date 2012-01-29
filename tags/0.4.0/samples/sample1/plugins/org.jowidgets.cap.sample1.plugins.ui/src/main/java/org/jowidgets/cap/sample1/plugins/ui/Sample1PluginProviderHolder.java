/*
 * Copyright (c) 2011 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original Author: grossmann
 * Creation Date: 18.10.2011
 */

package org.jowidgets.cap.sample1.plugins.ui;

import org.jowidgets.plugin.tools.PluginProviderHolder;

public class Sample1PluginProviderHolder extends PluginProviderHolder {

	public Sample1PluginProviderHolder() {
		super(new Sample1PluginProviderBuilder(), 2);
	}

}
