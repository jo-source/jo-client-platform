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

package org.jowidgets.plugin.impl;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.plugin.api.IPluginFilter;
import org.jowidgets.plugin.api.IPluginFilterBuilder;
import org.jowidgets.plugin.api.IPluginId;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginProvider;
import org.jowidgets.plugin.api.IPluginProviderBuilder;
import org.jowidgets.plugin.api.PluginToolkit;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.ITypedKey;
import org.jowidgets.util.Tuple;
import org.jowidgets.util.builder.AbstractSingleUseBuilder;

@SuppressWarnings({"unchecked", "rawtypes"})
final class PluginProviderBuilderImpl extends AbstractSingleUseBuilder<IPluginProvider> implements IPluginProviderBuilder {

	private static final IPluginFilter ACCEPTING_ALL_FILTER = createAcceptionAllFilter();

	private final Map plugins;

	PluginProviderBuilderImpl() {
		this.plugins = new LinkedHashMap();
	}

	@Override
	protected IPluginProvider doBuild() {
		return new PluginProviderImpl(plugins);
	}

	@Override
	public <PLUGIN_TYPE> void addPlugin(final IPluginId<? extends PLUGIN_TYPE> id, final PLUGIN_TYPE plugin) {
		addPlugin(id, plugin, ACCEPTING_ALL_FILTER);
	}

	@Override
	public <PLUGIN_TYPE> void addPlugin(
		final IPluginId<? extends PLUGIN_TYPE> id,
		final PLUGIN_TYPE plugin,
		final IPluginFilter filter) {

		Assert.paramNotNull(id, "id");
		Assert.paramNotNull(plugin, "plugin");
		Assert.paramNotNull(filter, "filter");

		List<Tuple> pluginsList = (List<Tuple>) plugins.get(id);
		if (pluginsList == null) {
			pluginsList = new LinkedList<Tuple>();
			plugins.put(id, pluginsList);
		}
		pluginsList.add(new Tuple(plugin, filter));
	}

	@Override
	public <PLUGIN_TYPE, PROPERTY_VALUE_TYPE> void addPlugin(
		final IPluginId<? extends PLUGIN_TYPE> id,
		final PLUGIN_TYPE plugin,
		final ITypedKey<PROPERTY_VALUE_TYPE> key,
		final PROPERTY_VALUE_TYPE... propertyValues) {

		if (EmptyCheck.isEmpty(propertyValues)) {
			addPlugin(id, plugin);
		}
		else {
			final IPluginFilterBuilder filterBuilder = PluginToolkit.pluginFilterBuilderOr();
			for (final PROPERTY_VALUE_TYPE propertyValue : propertyValues) {
				filterBuilder.addCondition(key, propertyValue);
			}
			addPlugin(id, plugin, filterBuilder.build());
		}
	}

	private static IPluginFilter createAcceptionAllFilter() {
		return new IPluginFilter() {
			@Override
			public boolean accept(final IPluginProperties properties) {
				return true;
			}
		};
	}
}
