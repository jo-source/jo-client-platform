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

package org.jowidgets.cap.service.api.bean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.jowidgets.util.Assert;

public final class BeanDtoCustomFilterPluginRegistry {

	private static Map<String, IBeanDtoCustomFilterPlugin> plugins = loadPlugins();

	private BeanDtoCustomFilterPluginRegistry() {}

	private static Map<String, IBeanDtoCustomFilterPlugin> loadPlugins() {
		final ServiceLoader<IBeanDtoCustomFilterPlugin> toolkitProviderLoader = ServiceLoader.load(IBeanDtoCustomFilterPlugin.class);
		final Iterator<IBeanDtoCustomFilterPlugin> iterator = toolkitProviderLoader.iterator();

		final Map<String, IBeanDtoCustomFilterPlugin> result = new HashMap<String, IBeanDtoCustomFilterPlugin>();

		while (iterator.hasNext()) {
			final IBeanDtoCustomFilterPlugin plugin = iterator.next();
			if (!plugins.containsKey(plugin.getFilterType())) {
				result.put(plugin.getFilterType(), plugin);
			}
			else {
				throw new IllegalStateException("The filter plugin with the type '"
					+ plugin.getFilterType()
					+ "' must not exists more than once");
			}
		}

		return result;
	}

	public static void register(final String filterType, final IBeanDtoCustomFilterPlugin plugin) {
		Assert.paramNotNull(filterType, "filterType");
		Assert.paramNotNull(plugin, "plugin");
		plugins.put(filterType, plugin);
	}

	public static IBeanDtoCustomFilterPlugin getPlugin(final String filterType) {
		Assert.paramNotNull(filterType, "filterType");
		return plugins.get(filterType);
	}

}
