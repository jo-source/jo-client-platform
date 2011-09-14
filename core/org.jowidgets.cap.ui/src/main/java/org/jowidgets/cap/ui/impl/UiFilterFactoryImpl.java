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

package org.jowidgets.cap.ui.impl;

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.common.api.filter.BooleanOperator;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.filter.IFilterFactory;
import org.jowidgets.cap.ui.api.filter.IUiArithmeticFilter;
import org.jowidgets.cap.ui.api.filter.IUiArithmeticFilterBuilder;
import org.jowidgets.cap.ui.api.filter.IUiArithmeticPropertyFilter;
import org.jowidgets.cap.ui.api.filter.IUiArithmeticPropertyFilterBuilder;
import org.jowidgets.cap.ui.api.filter.IUiBooleanFilter;
import org.jowidgets.cap.ui.api.filter.IUiBooleanFilterBuilder;
import org.jowidgets.cap.ui.api.filter.IUiFilter;
import org.jowidgets.cap.ui.api.filter.IUiFilterFactory;

final class UiFilterFactoryImpl implements IUiFilterFactory {

	@Override
	public IUiBooleanFilterBuilder booleanFilterBuilder() {
		return new UiBooleanFilterBuilderImpl();
	}

	@Override
	public IUiBooleanFilter booleanFilter(final BooleanOperator operator, final List<? extends IUiFilter> filters) {
		return booleanFilterBuilder().setOperator(operator).setFilters(filters).build();
	}

	@Override
	public <CONFIG_TYPE> IUiArithmeticFilterBuilder<CONFIG_TYPE> arithmeticFilterBuilder() {
		return new UiArithmeticFilterBuilderImpl<CONFIG_TYPE>();
	}

	@Override
	public <CONFIG_TYPE> IUiArithmeticFilter<CONFIG_TYPE> arithmeticFilter(
		final String propertyName,
		final ArithmeticOperator operator,
		final Object[] parameters) {
		// TODO MG,NM review (method call instead, generics)
		return new UiArithmeticFilterBuilderImpl<CONFIG_TYPE>().setPropertyName(propertyName).setOperator(operator).setParameters(
				parameters).build();
	}

	@Override
	public <CONFIG_TYPE> IUiArithmeticFilter<CONFIG_TYPE> arithmeticFilter(
		final String propertyName,
		final ArithmeticOperator operator,
		final Object parameter) {
		// TODO MG,NM review (method call instead, generics)
		return new UiArithmeticFilterBuilderImpl<CONFIG_TYPE>().setPropertyName(propertyName).setOperator(operator).setParameter(
				parameter).build();
	}

	@Override
	public <CONFIG_TYPE> IUiArithmeticFilter<CONFIG_TYPE> arithmeticFilter(
		final String propertyName,
		final ArithmeticOperator operator) {
		// TODO MG,NM review (method call instead, generics)
		return new UiArithmeticFilterBuilderImpl<CONFIG_TYPE>().setPropertyName(propertyName).setOperator(operator).build();
	}

	@Override
	public <CONFIG_TYPE> IUiArithmeticPropertyFilterBuilder<CONFIG_TYPE> arithmeticPropertyFilterBuilder() {
		return new UiArithmeticPropertyFilterBuilderImpl<CONFIG_TYPE>();
	}

	@Override
	public <CONFIG_TYPE> IUiArithmeticPropertyFilter<CONFIG_TYPE> arithmeticPropertyFilter(
		final String leftPropertyName,
		final ArithmeticOperator operator,
		final String[] rightPropertyNames) {
		// TODO MG,NM review (method call instead, generics)
		return new UiArithmeticPropertyFilterBuilderImpl<CONFIG_TYPE>().setLeftHandPropertyName(leftPropertyName).setOperator(
				operator).setRightHandPropertyNames(rightPropertyNames).build();
	}

	@Override
	public <CONFIG_TYPE> IUiArithmeticPropertyFilter<CONFIG_TYPE> arithmeticPropertyFilter(
		final String leftPropertyName,
		final ArithmeticOperator operator,
		final String rightPropertyName) {
		// TODO MG,NM review (method call instead, generics)
		return new UiArithmeticPropertyFilterBuilderImpl<CONFIG_TYPE>().setLeftHandPropertyName(leftPropertyName).setOperator(
				operator).setRightHandPropertyName(rightPropertyName).build();
	}

	@Override
	public IFilter convert(final IUiFilter uiFilter) {
		final IFilterFactory fab = CapCommonToolkit.filterFactory();
		if (uiFilter instanceof IUiBooleanFilter) {
			final IUiBooleanFilter uiBooleanFilter = (IUiBooleanFilter) uiFilter;
			final List<IFilter> filters = new LinkedList<IFilter>();
			for (final IUiFilter uiF : uiBooleanFilter.getFilters()) {
				filters.add(convert(uiF));
			}
			return fab.booleanFilter(uiBooleanFilter.getOperator(), filters);
		}
		else if (uiFilter instanceof IUiArithmeticFilter<?>) {
			final IUiArithmeticFilter<?> uiArithmeticFilter = (IUiArithmeticFilter<?>) uiFilter;
			return fab.arithmeticFilter(
					uiArithmeticFilter.getPropertyName(),
					uiArithmeticFilter.getOperator(),
					uiArithmeticFilter.getParameters());
		}
		else if (uiFilter instanceof IUiArithmeticPropertyFilter<?>) {
			final IUiArithmeticPropertyFilter<?> uiArithmeticPropertyFilter = (IUiArithmeticPropertyFilter<?>) uiFilter;
			return fab.arithmeticPropertyFilter(
					uiArithmeticPropertyFilter.getLeftHandPropertyName(),
					uiArithmeticPropertyFilter.getOperator(),
					uiArithmeticPropertyFilter.getRightHandPropertyNames());
		}
		else {
			throw new IllegalStateException("Cannot convert unkown filter class '" + uiFilter.getClass().getName() + "'.");
		}
	}

}
