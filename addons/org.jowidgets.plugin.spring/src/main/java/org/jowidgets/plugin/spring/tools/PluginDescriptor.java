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

package org.jowidgets.plugin.spring.tools;

import org.jowidgets.plugin.api.IPluginFilter;
import org.jowidgets.plugin.api.IPluginId;
import org.jowidgets.plugin.spring.api.IPluginDescriptor;
import org.jowidgets.plugin.tools.AcceptAllPluginFilter;
import org.jowidgets.util.Assert;

public final class PluginDescriptor<PLUGIN_TYPE> implements IPluginDescriptor<PLUGIN_TYPE> {

	public static final int DEFAULT_ORDER = 2;

	private final IPluginId<PLUGIN_TYPE> id;
	private final PLUGIN_TYPE plugin;
	private final IPluginFilter filter;
	private final int order;

	public PluginDescriptor(final IPluginId<PLUGIN_TYPE> id, final PLUGIN_TYPE plugin) {
		this(id, plugin, new AcceptAllPluginFilter(), DEFAULT_ORDER);
	}

	public PluginDescriptor(final IPluginId<PLUGIN_TYPE> id, final PLUGIN_TYPE plugin, final IPluginFilter filter) {
		this(id, plugin, filter, DEFAULT_ORDER);
	}

	public PluginDescriptor(final IPluginId<PLUGIN_TYPE> id, final PLUGIN_TYPE plugin, final int order) {
		this(id, plugin, new AcceptAllPluginFilter(), order);
	}

	public PluginDescriptor(final IPluginId<PLUGIN_TYPE> id, final PLUGIN_TYPE plugin, final IPluginFilter filter, final int order) {
		Assert.paramNotNull(id, "id");
		Assert.paramNotNull(plugin, "plugin");
		Assert.paramNotNull(filter, "filter");

		this.id = id;
		this.plugin = plugin;
		this.filter = filter;
		this.order = order;
	}

	@Override
	public IPluginId<PLUGIN_TYPE> getId() {
		return id;
	}

	@Override
	public PLUGIN_TYPE getPlugin() {
		return plugin;
	}

	@Override
	public IPluginFilter getFilter() {
		return filter;
	}

	@Override
	public int getOrder() {
		return order;
	}

}
