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
import org.jowidgets.cap.ui.api.filter.FilterType;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.cap.ui.api.filter.IUiArithmeticPropertyFilter;
import org.jowidgets.cap.ui.api.filter.IUiArithmeticPropertyFilterBuilder;

final class UiArithmeticPropertyFilterBuilderImpl<CONFIG_TYPE> extends
		UiFilterBuilderImpl<IUiArithmeticPropertyFilterBuilder<CONFIG_TYPE>> implements
		IUiArithmeticPropertyFilterBuilder<CONFIG_TYPE> {

	private CONFIG_TYPE config;
	private IFilterType filterType;
	private String leftHandPropertyName;
	private final List<String> rightHandPropertyNames;
	private ArithmeticOperator operator;

	UiArithmeticPropertyFilterBuilderImpl() {
		rightHandPropertyNames = new LinkedList<String>();
		this.filterType = FilterType.ARITHMETIC_PROPERTY_FILTER;
	}

	@Override
	public IUiArithmeticPropertyFilterBuilder<CONFIG_TYPE> setConfig(final CONFIG_TYPE config) {
		this.config = config;
		return this;
	}

	@Override
	public IUiArithmeticPropertyFilterBuilder<CONFIG_TYPE> setType(final IFilterType filterType) {
		this.filterType = filterType;
		return this;
	}

	@Override
	public IUiArithmeticPropertyFilterBuilder<CONFIG_TYPE> setLeftHandPropertyName(final String propertyName) {
		this.leftHandPropertyName = propertyName;
		return this;
	}

	@Override
	public IUiArithmeticPropertyFilterBuilder<CONFIG_TYPE> setRightHandPropertyNames(final String[] propertyNames) {
		this.rightHandPropertyNames.clear();
		for (final String propertyName : propertyNames) {
			this.rightHandPropertyNames.add(propertyName);
		}
		return this;
	}

	@Override
	public IUiArithmeticPropertyFilterBuilder<CONFIG_TYPE> setRightHandPropertyName(final String propertyName) {
		this.rightHandPropertyNames.clear();
		this.rightHandPropertyNames.add(propertyName);
		return this;
	}

	@Override
	public IUiArithmeticPropertyFilterBuilder<CONFIG_TYPE> addRightHandPropertyName(final String propertyName) {
		this.rightHandPropertyNames.add(propertyName);
		return this;
	}

	@Override
	public IUiArithmeticPropertyFilterBuilder<CONFIG_TYPE> setOperator(final ArithmeticOperator operator) {
		this.operator = operator;
		return this;
	}

	@Override
	public IUiArithmeticPropertyFilter<CONFIG_TYPE> build() {
		return new UiArithmeticPropertyFilterImpl<CONFIG_TYPE>(
			leftHandPropertyName,
			operator,
			rightHandPropertyNames.toArray(new String[rightHandPropertyNames.size()]),
			isInverted(),
			config,
			filterType);
	}

}
