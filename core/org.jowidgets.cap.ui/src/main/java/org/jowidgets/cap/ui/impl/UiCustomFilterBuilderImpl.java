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

import org.jowidgets.cap.common.api.filter.IOperator;
import org.jowidgets.cap.common.impl.FilterBuilderImpl;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.cap.ui.api.filter.IUiConfigurableFilterBuilder;
import org.jowidgets.cap.ui.api.filter.IUiCustomFilter;
import org.jowidgets.cap.ui.api.filter.IUiCustomFilterBuilder;

final class UiCustomFilterBuilderImpl<CONFIG_TYPE> extends FilterBuilderImpl<IUiCustomFilterBuilder<CONFIG_TYPE>> implements
		IUiCustomFilterBuilder<CONFIG_TYPE> {

	private String filterType;
	private String propertyName;
	private IOperator operator;
	private Object value;
	private CONFIG_TYPE config;
	private IFilterType type;

	@Override
	public IUiConfigurableFilterBuilder<IUiCustomFilterBuilder<CONFIG_TYPE>, CONFIG_TYPE> setConfig(final CONFIG_TYPE config) {
		this.config = config;
		return this;
	}

	@Override
	public IUiConfigurableFilterBuilder<IUiCustomFilterBuilder<CONFIG_TYPE>, CONFIG_TYPE> setType(final IFilterType type) {
		this.type = type;
		return this;
	}

	@Override
	public IUiCustomFilterBuilder<CONFIG_TYPE> setFilterType(final String filterType) {
		this.filterType = filterType;
		return this;
	}

	@Override
	public IUiCustomFilterBuilder<CONFIG_TYPE> setPropertyName(final String propertyName) {
		this.propertyName = propertyName;
		return this;
	}

	@Override
	public IUiCustomFilterBuilder<CONFIG_TYPE> setOperator(final IOperator operator) {
		this.operator = operator;
		return this;
	}

	@Override
	public IUiCustomFilterBuilder<CONFIG_TYPE> setValue(final Object value) {
		this.value = value;
		return this;
	}

	@Override
	public IUiCustomFilter<CONFIG_TYPE> build() {
		return new UiCustomFilterImpl<CONFIG_TYPE>(filterType, propertyName, operator, value, isInverted(), config, type);
	}

}
