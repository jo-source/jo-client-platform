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

package org.jowidgets.plugin.tools;

import org.jowidgets.plugin.api.IPluginFilter;
import org.jowidgets.plugin.api.IPluginFilterBuilder;
import org.jowidgets.plugin.api.IPluginId;
import org.jowidgets.plugin.api.IPluginProvider;
import org.jowidgets.plugin.api.IPluginProviderBuilder;
import org.jowidgets.plugin.api.PluginToolkit;
import org.jowidgets.util.Assert;
import org.jowidgets.util.ITypedKey;

public class PluginProviderBuilder implements IPluginProviderBuilder {

	private final IPluginProviderBuilder builder;

	public PluginProviderBuilder() {
		this.builder = PluginToolkit.pluginProviderBuilder();
	}

	@Override
	public final <SERVICE_TYPE> void addPlugin(final IPluginId<? extends SERVICE_TYPE> id, final SERVICE_TYPE service) {
		builder.addPlugin(id, service);
	}

	@Override
	public <PLUGIN_TYPE> void addPlugin(
		final IPluginId<? extends PLUGIN_TYPE> id,
		final PLUGIN_TYPE plugin,
		final IPluginFilter filter) {
		builder.addPlugin(id, plugin, filter);
	}

	public <PLUGIN_TYPE, PROPERTY_VALUE_TYPE> void addPlugin(
		final IPluginId<? extends PLUGIN_TYPE> id,
		final PLUGIN_TYPE plugin,
		final ITypedKey<PROPERTY_VALUE_TYPE> key,
		final PROPERTY_VALUE_TYPE... propertyValues) {
		Assert.paramNotEmpty(propertyValues, "propertyValues");

		final IPluginFilterBuilder filterBuilder = PluginToolkit.pluginFilterBuilderOr();
		for (final PROPERTY_VALUE_TYPE propertyValue : propertyValues) {
			filterBuilder.addCondition(key, propertyValue);
		}

		builder.addPlugin(id, plugin, filterBuilder.build());
	}

	@Override
	public final IPluginProvider build() {
		return builder.build();
	}

}
