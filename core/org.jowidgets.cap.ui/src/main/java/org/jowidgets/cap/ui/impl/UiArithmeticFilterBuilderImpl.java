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

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.common.impl.FilterBuilderImpl;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.cap.ui.api.filter.IUiArithmeticFilter;
import org.jowidgets.cap.ui.api.filter.IUiArithmeticFilterBuilder;

final class UiArithmeticFilterBuilderImpl<CONFIG_TYPE> extends FilterBuilderImpl<IUiArithmeticFilterBuilder<CONFIG_TYPE>> implements
		IUiArithmeticFilterBuilder<CONFIG_TYPE> {

	private final List<Object> parameters;
	private CONFIG_TYPE config;
	private IFilterType filterType;
	private ArithmeticOperator operator;
	private String propertyName;

	UiArithmeticFilterBuilderImpl() {
		parameters = new LinkedList<Object>();
	}

	@Override
	public void setConfig(final CONFIG_TYPE config) {
		this.config = config;
		// TODO MG,NM review: return this;
	}

	@Override
	public void setType(final IFilterType filterType) {
		this.filterType = filterType;
		// TODO MG,NM review: return this;
	}

	@Override
	public IUiArithmeticFilterBuilder<CONFIG_TYPE> setPropertyName(final String propertyName) {
		this.propertyName = propertyName;
		return this;
	}

	@Override
	public IUiArithmeticFilterBuilder<CONFIG_TYPE> setOperator(final ArithmeticOperator operator) {
		this.operator = operator;
		return this;
	}

	@Override
	public IUiArithmeticFilterBuilder<CONFIG_TYPE> addParameter(final Object parameter) {
		this.parameters.add(parameter);
		return this;
	}

	@Override
	public IUiArithmeticFilterBuilder<CONFIG_TYPE> setParameter(final Object parameter) {
		this.parameters.clear();
		this.parameters.add(parameter);
		return this;
	}

	@Override
	public IUiArithmeticFilterBuilder<CONFIG_TYPE> setParameters(final Object[] parameters) {
		this.parameters.clear();
		for (final Object parameter : parameters) {
			this.parameters.add(parameter);
		}
		return this;
	}

	@Override
	public IUiArithmeticFilter<CONFIG_TYPE> build() {
		return new UiArithmeticFilterImpl<CONFIG_TYPE>(
			propertyName,
			operator,
			parameters.toArray(),
			isInverted(),
			config,
			filterType);
	}

}
