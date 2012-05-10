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

package org.jowidgets.plugin.api;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import org.jowidgets.util.Assert;

public final class PluginProvider {

	private static CompositePluginProviderHolder compositePluginProviderHolder;

	private PluginProvider() {}

	public static synchronized void registerPluginProviderHolder(final IPluginProviderHolder pluginProviderHolder) {
		Assert.paramNotNull(pluginProviderHolder, "pluginProviderHolder");
		getCompositePluginProviderHolder().add(pluginProviderHolder);
	}

	private static synchronized CompositePluginProviderHolder getCompositePluginProviderHolder() {
		if (compositePluginProviderHolder == null) {
			compositePluginProviderHolder = new CompositePluginProviderHolder();
			final ServiceLoader<IPluginProviderHolder> serviceLoader = ServiceLoader.load(IPluginProviderHolder.class);
			final Iterator<IPluginProviderHolder> iterator = serviceLoader.iterator();
			while (iterator.hasNext()) {
				compositePluginProviderHolder.add(iterator.next());
			}
		}
		return compositePluginProviderHolder;
	}

	public static IPluginProvider getInstance() {
		return getCompositePluginProviderHolder().getPluginProvider();
	}

	public static <PLUGIN_TYPE> List<PLUGIN_TYPE> getPlugins(final IPluginId<PLUGIN_TYPE> id) {
		return getInstance().getPlugins(id, PluginToolkit.pluginPropertiesBuilder().build());
	}

	public static <PLUGIN_TYPE> List<PLUGIN_TYPE> getPlugins(final IPluginId<PLUGIN_TYPE> id, final IPluginProperties properties) {
		return getInstance().getPlugins(id, properties);
	}

	private static class CompositePluginProviderHolder implements IPluginProviderHolder {

		private final List<IPluginProviderHolder> pluginProviderHolders;

		private final IPluginProvider pluginProvider;

		CompositePluginProviderHolder() {
			this.pluginProviderHolders = new LinkedList<IPluginProviderHolder>();

			this.pluginProvider = new IPluginProvider() {

				@Override
				public <PLUGIN_TYPE> List<PLUGIN_TYPE> getPlugins(
					final IPluginId<PLUGIN_TYPE> id,
					final IPluginProperties properties) {
					final List<PLUGIN_TYPE> result = new LinkedList<PLUGIN_TYPE>();
					for (final IPluginProviderHolder pluginProviderHolder : pluginProviderHolders) {
						final IPluginProvider provider = pluginProviderHolder.getPluginProvider();
						final List<PLUGIN_TYPE> plugins = provider.getPlugins(id, properties);
						if (plugins != null && !plugins.isEmpty()) {
							result.addAll(plugins);
						}
					}
					return Collections.unmodifiableList(result);
				}
			};
		}

		void add(final IPluginProviderHolder holder) {
			pluginProviderHolders.add(holder);
			Collections.sort(pluginProviderHolders, new Comparator<IPluginProviderHolder>() {
				@Override
				public int compare(final IPluginProviderHolder provider1, final IPluginProviderHolder provider2) {
					if (provider1 != null && provider2 != null) {
						return provider1.getOrder() - provider2.getOrder();
					}
					return 0;
				}
			});
		}

		@Override
		public IPluginProvider getPluginProvider() {
			return pluginProvider;
		}

		@Override
		public int getOrder() {
			return 0;
		}
	}
}
