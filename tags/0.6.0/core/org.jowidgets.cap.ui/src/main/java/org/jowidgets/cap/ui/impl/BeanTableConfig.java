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

package org.jowidgets.cap.ui.impl;

import java.util.Map;

import org.jowidgets.cap.ui.api.attribute.IAttributeConfig;
import org.jowidgets.cap.ui.api.filter.IUiFilter;
import org.jowidgets.cap.ui.api.sort.ISortModelConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableConfig;

final class BeanTableConfig implements IBeanTableConfig {

	private final Map<String, IAttributeConfig> attributeConfigs;
	private final Boolean autoSelection;
	private final ISortModelConfig sortModelConfig;
	private final Map<String, IUiFilter> filters;

	BeanTableConfig(
		final Map<String, IAttributeConfig> attributeConfigs,
		final ISortModelConfig sortModelConfig,
		final Map<String, IUiFilter> filters,
		final Boolean autoSelection) {
		super();
		this.attributeConfigs = attributeConfigs;
		this.sortModelConfig = sortModelConfig;
		this.filters = filters;
		this.autoSelection = autoSelection;
	}

	@Override
	public ISortModelConfig getSortModelConfig() {
		return sortModelConfig;
	}

	@Override
	public Map<String, IUiFilter> getFilters() {
		return filters;
	}

	@Override
	public Map<String, IAttributeConfig> getAttributeConfigs() {
		return attributeConfigs;
	}

	@Override
	public Boolean isAutoSelection() {
		return autoSelection;
	}

	@Override
	public String toString() {
		return "BeanTableConfig [attributeConfigs="
			+ attributeConfigs
			+ ", autoSelection="
			+ autoSelection
			+ ", sortModelConfig="
			+ sortModelConfig
			+ ", filters="
			+ filters
			+ "]";
	}

}
