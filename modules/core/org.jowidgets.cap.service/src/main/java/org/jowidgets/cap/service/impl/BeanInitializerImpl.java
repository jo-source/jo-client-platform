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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanData;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.plugin.IBeanInitializerPlugin;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.plugin.api.PluginToolkit;
import org.jowidgets.util.Assert;

final class BeanInitializerImpl<BEAN_TYPE> implements IBeanInitializer<BEAN_TYPE> {

	private final Map<String, Method> methods;
	private final IPluginProperties pluginProperties;

	BeanInitializerImpl(final Class<? extends BEAN_TYPE> beanType, final Collection<String> propertyNames) {
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(propertyNames, "propertyNames");

		final Map<String, Method> unsortedMap = new HashMap<String, Method>();

		try {
			final BeanInfo beanInfo = Introspector.getBeanInfo(beanType);
			for (final PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
				final String propertyName = propertyDescriptor.getName();
				if (propertyNames.contains(propertyName)
					&& !propertyName.equals(IBean.VERSION_PROPERTY)
					&& !propertyName.equals(IBean.ID_PROPERTY)) {
					final Method writeMethod = propertyDescriptor.getWriteMethod();
					if (writeMethod != null) {
						unsortedMap.put(propertyName, propertyDescriptor.getWriteMethod());
					}
				}
			}
		}
		catch (final IntrospectionException e) {
			throw new RuntimeException(e);
		}

		//add the methods in that order they should be invoked when bean will be initialized
		this.methods = new LinkedHashMap<String, Method>();
		for (final String propertyName : propertyNames) {
			final Method method = unsortedMap.get(propertyName);
			if (method != null) {
				methods.put(propertyName, method);
			}
		}

		this.pluginProperties = createPluginProperties(beanType);
	}

	private IPluginProperties createPluginProperties(final Class<? extends BEAN_TYPE> beanType) {
		final IPluginPropertiesBuilder builder = PluginToolkit.pluginPropertiesBuilder();
		builder.add(IBeanInitializerPlugin.BEAN_TYPE_PROPERTY_KEY, beanType);
		return builder.build();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void initialize(final BEAN_TYPE bean, final IBeanData beanData) {
		Assert.paramNotNull(bean, "bean");
		Assert.paramNotNull(beanData, "beanData");

		//plugin before invocation
		final List<IBeanInitializerPlugin<?>> plugins;
		plugins = PluginProvider.getPlugins(IBeanInitializerPlugin.ID, pluginProperties);
		for (final IBeanInitializerPlugin plugin : plugins) {
			plugin.beforeInitialize(bean, beanData);
		}

		//set the values
		for (final Entry<String, Method> entry : methods.entrySet()) {
			try {
				entry.getValue().invoke(bean, beanData.getValue(entry.getKey()));
			}
			catch (final Exception e) {
				throw new RuntimeException("Error while setting property '" + entry.getKey() + "' on bean '" + bean + "'.", e);
			}
		}

		//plugin after invocation
		for (final IBeanInitializerPlugin plugin : plugins) {
			plugin.afterInitialize(bean, beanData);
		}
	}
}
