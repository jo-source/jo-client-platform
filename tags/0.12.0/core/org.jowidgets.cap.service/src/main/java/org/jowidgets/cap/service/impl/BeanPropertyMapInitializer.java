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

package org.jowidgets.cap.service.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanData;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.bean.IBeanPropertyMap;
import org.jowidgets.cap.service.api.plugin.IBeanInitializerPlugin;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.plugin.api.PluginToolkit;
import org.jowidgets.util.Assert;

final class BeanPropertyMapInitializer implements IBeanInitializer<IBeanPropertyMap> {

	private final List<String> propertyNames;
	private final IPluginProperties pluginProperties;

	BeanPropertyMapInitializer(final Collection<String> propertyNames) {
		Assert.paramNotNull(propertyNames, "propertyNames");
		this.propertyNames = new LinkedList<String>(propertyNames);
		this.pluginProperties = createPluginProperties();
	}

	private IPluginProperties createPluginProperties() {
		final IPluginPropertiesBuilder builder = PluginToolkit.pluginPropertiesBuilder();
		builder.add(IBeanInitializerPlugin.BEAN_TYPE_PROPERTY_KEY, IBeanPropertyMap.class);
		return builder.build();
	}

	@Override
	public void initialize(final IBeanPropertyMap bean, final IBeanData beanData) {
		//plugin before invocation
		final List<IBeanInitializerPlugin<IBean>> plugins;
		plugins = PluginProvider.getPlugins(IBeanInitializerPlugin.ID, pluginProperties);
		for (final IBeanInitializerPlugin<IBean> plugin : plugins) {
			plugin.beforeInitialize(bean, beanData);
		}

		//set the values
		for (final String propertyName : propertyNames) {
			bean.setValue(propertyName, beanData.getValue(propertyName));
		}

		//plugin after invocation
		for (final IBeanInitializerPlugin<IBean> plugin : plugins) {
			plugin.afterInitialize(bean, beanData);
		}

	}

}
