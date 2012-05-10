/*
 * Copyright (c) 2011, nimoll
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

package org.jowidgets.cap.ui.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeConfig;
import org.jowidgets.cap.ui.api.filter.IUiFilter;
import org.jowidgets.cap.ui.api.sort.ISortModelConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableModelConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableModelConfigBuilder;
import org.jowidgets.util.Assert;

final class BeanTableModelConfigBuilderImpl implements IBeanTableModelConfigBuilder {

	private ISortModelConfig sortModelConfig;
	private Map<String, IUiFilter> filters;
	private Map<String, IAttributeConfig> attributeConfigs;
	private Boolean autoSelection;

	@Override
	public IBeanTableModelConfigBuilder setSortModelConfig(final ISortModelConfig sortModelConfig) {
		this.sortModelConfig = sortModelConfig;
		return this;
	}

	@Override
	public IBeanTableModelConfigBuilder setFilters(final Map<String, IUiFilter> filters) {
		Assert.paramNotNull(filters, "filters");

		ensureFiltersMap();
		this.filters.clear();
		for (final Entry<String, IUiFilter> entry : filters.entrySet()) {
			this.filters.put(entry.getKey(), entry.getValue());
		}
		return this;
	}

	@Override
	public IBeanTableModelConfigBuilder addFilter(final String id, final IUiFilter filter) {
		Assert.paramNotEmpty(id, "id");
		Assert.paramNotNull(filter, "filter");

		ensureFiltersMap();
		filters.put(id, filter);
		return this;
	}

	@Override
	public IBeanTableModelConfigBuilder setAttributeConfigs(final Map<String, IAttributeConfig> attributeConfigs) {
		Assert.paramNotNull(attributeConfigs, "attributeConfigs");

		ensureAttributeConfigsMap();
		this.attributeConfigs.clear();
		for (final Entry<String, IAttributeConfig> entry : attributeConfigs.entrySet()) {
			addToAttributeConfigsMap(entry.getKey(), entry.getValue());
		}
		return this;
	}

	@Override
	public IBeanTableModelConfigBuilder addAttributeConfig(final String propertyName, final IAttributeConfig attributeConfig) {
		Assert.paramNotEmpty(propertyName, "propertyName");
		Assert.paramNotNull(attributeConfig, "attributeConfig");

		ensureAttributeConfigsMap();
		addToAttributeConfigsMap(propertyName, attributeConfig);
		return this;
	}

	@Override
	public IBeanTableModelConfigBuilder addAttributeConfig(final IAttribute<?> attribute) {
		Assert.paramNotNull(attribute, "attribute");

		ensureAttributeConfigsMap();
		addToAttributeConfigsMap(attribute.getPropertyName(), attribute.getConfig());
		return this;
	}

	@Override
	public IBeanTableModelConfigBuilder setAutoSelection(final boolean autoSelection) {
		this.autoSelection = autoSelection;
		return this;
	}

	@Override
	public IBeanTableModelConfig build() {
		return new BeanTableModelConfigImpl(attributeConfigs, sortModelConfig, filters, autoSelection);
	}

	private void addToAttributeConfigsMap(final String propertyName, final IAttributeConfig attributeConfig) {
		attributeConfigs.put(propertyName, attributeConfig);
	}

	private void ensureFiltersMap() {
		if (filters == null) {
			filters = new HashMap<String, IUiFilter>();
		}
	}

	private void ensureAttributeConfigsMap() {
		if (attributeConfigs == null) {
			attributeConfigs = new HashMap<String, IAttributeConfig>();
		}
	}
}
