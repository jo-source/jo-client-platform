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

import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanModification;
import org.jowidgets.cap.service.api.bean.IBeanModifier;
import org.jowidgets.cap.service.api.bean.IBeanPropertyMap;
import org.jowidgets.cap.service.api.plugin.IBeanModifierPlugin;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.plugin.api.PluginToolkit;
import org.jowidgets.util.EmptyCompatibleEquivalence;

final class BeanPropertyMapModifier implements IBeanModifier<IBeanPropertyMap> {

	private final IPluginProperties pluginProperties;

	BeanPropertyMapModifier() {
		this.pluginProperties = createPluginProperties();
	}

	private IPluginProperties createPluginProperties() {
		final IPluginPropertiesBuilder builder = PluginToolkit.pluginPropertiesBuilder();
		builder.add(IBeanModifierPlugin.BEAN_TYPE_PROPERTY_KEY, IBeanPropertyMap.class);
		return builder.build();
	}

	@Override
	public boolean isPropertyStale(final IBeanPropertyMap bean, final IBeanModification modification) {
		if (EmptyCompatibleEquivalence.equals(bean.getValue(modification.getPropertyName()), modification.getOldValue())) {
			return false;
		}
		else {
			return true;
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void modify(final IBeanPropertyMap bean, final IBeanModification modification) {

		//plugin before invocation
		final List<IBeanModifierPlugin<?>> plugins;
		plugins = PluginProvider.getPlugins(IBeanModifierPlugin.ID, pluginProperties);
		for (final IBeanModifierPlugin plugin : plugins) {
			plugin.beforeModification(bean, modification);
		}

		//do modification
		bean.setValue(modification.getPropertyName(), modification.getNewValue());

		//plugin after invocation
		for (final IBeanModifierPlugin plugin : plugins) {
			plugin.afterModification(bean, modification);
		}
	}

}
