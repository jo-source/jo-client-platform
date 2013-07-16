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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.plugin.api.IPluginFilter;
import org.jowidgets.plugin.api.IPluginId;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.Tuple;

@SuppressWarnings({"rawtypes", "unchecked"})
final class PluginProviderImpl implements IPluginProvider {

	private final Map plugins;

	PluginProviderImpl(final Map plugins) {
		this.plugins = new LinkedHashMap(plugins);
	}

	@Override
	public <PLUGIN_TYPE> List<PLUGIN_TYPE> getPlugins(final IPluginId<PLUGIN_TYPE> id, final IPluginProperties properties) {
		Assert.paramNotNull(id, "id");
		Assert.paramNotNull(properties, "properties");

		final List<PLUGIN_TYPE> result = new LinkedList<PLUGIN_TYPE>();

		final List<Tuple> registerdPlugins = (List<Tuple>) plugins.get(id);
		if (registerdPlugins != null) {
			for (final Tuple tuple : registerdPlugins) {
				final PLUGIN_TYPE plugin = (PLUGIN_TYPE) tuple.getFirst();
				final IPluginFilter filter = (IPluginFilter) tuple.getSecond();
				if (filter.accept(properties)) {
					result.add(plugin);
				}
			}
		}

		return Collections.unmodifiableList(result);
	}

}
