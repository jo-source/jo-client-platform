/*
 * Copyright (c) 2011, grossmann, Nikolaus Moll
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

package org.jowidgets.cap.common.impl;

import java.util.List;

import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.common.api.filter.BooleanOperator;
import org.jowidgets.cap.common.api.filter.IArithmeticFilter;
import org.jowidgets.cap.common.api.filter.IArithmeticFilterBuilder;
import org.jowidgets.cap.common.api.filter.IArithmeticPropertyFilter;
import org.jowidgets.cap.common.api.filter.IArithmeticPropertyFilterBuilder;
import org.jowidgets.cap.common.api.filter.IBooleanFilter;
import org.jowidgets.cap.common.api.filter.IBooleanFilterBuilder;
import org.jowidgets.cap.common.api.filter.ICustomFilter;
import org.jowidgets.cap.common.api.filter.ICustomFilterBuilder;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.filter.IFilterFactory;
import org.jowidgets.cap.common.api.filter.IOperator;

final class FilterFactoryImpl implements IFilterFactory {

	@Override
	public IBooleanFilterBuilder booleanFilterBuilder() {
		return new BooleanFilterBuilderImpl();
	}

	@Override
	public IBooleanFilter booleanFilter(final BooleanOperator operator, final List<? extends IFilter> filters) {
		return booleanFilterBuilder().setOperator(operator).setFilters(filters).build();
	}

	@Override
	public IArithmeticFilterBuilder arithmeticFilterBuilder() {
		return new ArithmeticFilterBuilderImpl();
	}

	@Override
	public IArithmeticFilter arithmeticFilter(
		final String propertyName,
		final ArithmeticOperator operator,
		final Object[] parameters) {
		return arithmeticFilterBuilder().setPropertyName(propertyName).setOperator(operator).setParameters(parameters).build();
	}

	@Override
	public IArithmeticFilter arithmeticFilter(final String propertyName, final ArithmeticOperator operator, final Object parameter) {
		return arithmeticFilterBuilder().setPropertyName(propertyName).setOperator(operator).setParameter(parameter).build();
	}

	@Override
	public IArithmeticFilter arithmeticFilter(final String propertyName, final ArithmeticOperator operator) {
		return arithmeticFilterBuilder().setPropertyName(propertyName).setOperator(operator).build();
	}

	@Override
	public IArithmeticPropertyFilterBuilder arithmeticPropertyFilterBuilder() {
		return new ArithmeticPropertyFilterBuilderImpl();
	}

	@Override
	public IArithmeticPropertyFilter arithmeticPropertyFilter(
		final String leftPropertyName,
		final ArithmeticOperator operator,
		final String[] rightPropertyNames) {
		return arithmeticPropertyFilterBuilder().setLeftHandPropertyName(leftPropertyName).setOperator(operator).setRightHandPropertyNames(
				rightPropertyNames).build();
	}

	@Override
	public IArithmeticPropertyFilter arithmeticPropertyFilter(
		final String leftPropertyName,
		final ArithmeticOperator operator,
		final String rightPropertyName) {
		return arithmeticPropertyFilterBuilder().setLeftHandPropertyName(leftPropertyName).setOperator(operator).setRightHandPropertyName(
				rightPropertyName).build();
	}

	@Override
	public ICustomFilterBuilder customFilterBuilder() {
		return new CustomFilterBuilderImpl();
	}

	@Override
	public ICustomFilter customFilter(
		final String filterType,
		final String propertyName,
		final IOperator operator,
		final Object value) {
		return customFilterBuilder().setFilterType(filterType).setPropertyName(propertyName).setOperator(operator).setValue(value).build();
	}

}
