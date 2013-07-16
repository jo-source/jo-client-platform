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

package org.jowidgets.plugin.spring.impl;

import org.jowidgets.plugin.api.IPluginProviderBuilder;
import org.jowidgets.plugin.api.IPluginProviderHolder;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.plugin.api.PluginToolkit;
import org.jowidgets.plugin.spring.api.IPluginDescriptor;
import org.jowidgets.plugin.spring.api.Plugin;
import org.jowidgets.plugin.spring.tools.PluginDescriptor;
import org.jowidgets.plugin.tools.PluginProviderHolder;
import org.jowidgets.util.reflection.AnnotationCache;
import org.springframework.beans.factory.config.BeanPostProcessor;

public final class PluginProviderPostProcessor implements BeanPostProcessor {

	public PluginProviderPostProcessor() {}

	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName) {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName) {
		if (bean instanceof IPluginProviderHolder) {
			PluginProvider.registerPluginProviderHolder((IPluginProviderHolder) bean);
		}
		else if (bean instanceof IPluginDescriptor<?>) {
			addPluginDescriptor((IPluginDescriptor<?>) bean);
		}
		else {
			final Plugin plugin = AnnotationCache.getTypeAnnotationFromHierarchy(bean.getClass(), Plugin.class);
			if (plugin != null) {
				addPluginDescriptor(new PluginDescriptor<Object>(bean, plugin.order()));
			}
		}
		return bean;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void addPluginDescriptor(final IPluginDescriptor pluginDescriptor) {
		final IPluginProviderBuilder builder = PluginToolkit.pluginProviderBuilder();
		builder.addPlugin(pluginDescriptor.getId(), pluginDescriptor.getPlugin(), pluginDescriptor.getFilter());
		//TODO MG do not create one holder for each plugin
		PluginProvider.registerPluginProviderHolder(new PluginProviderHolder(builder, pluginDescriptor.getOrder()));
	}

}
