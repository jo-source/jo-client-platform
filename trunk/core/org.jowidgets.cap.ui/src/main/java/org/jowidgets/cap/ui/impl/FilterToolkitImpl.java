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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.ui.api.attribute.IAttributeFilter;
import org.jowidgets.cap.ui.api.filter.IFilterPanelProvider;
import org.jowidgets.cap.ui.api.filter.IFilterSupport;
import org.jowidgets.cap.ui.api.filter.IFilterToolkit;
import org.jowidgets.cap.ui.api.filter.IIncludingFilterFactory;
import org.jowidgets.cap.ui.api.filter.IOperatorProvider;
import org.jowidgets.cap.ui.api.filter.IUiFilterFactory;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;

final class FilterToolkitImpl implements IFilterToolkit {

	private final IUiFilterFactory filterFactory;

	FilterToolkitImpl() {
		this.filterFactory = new UiFilterFactoryImpl();
	}

	@Override
	public IUiFilterFactory filterFactory() {
		return filterFactory;
	}

	@Override
	public IOperatorProvider<ArithmeticOperator> arithmeticOperatorProvider(
		final Class<?> type,
		final Class<?> elementValueType,
		final IValueRange valueRange) {
		return DefaultOperatorProvider.getArithmeticOperatorProvider(type, elementValueType, valueRange);
	}

	@Override
	public IOperatorProvider<ArithmeticOperator> arithmeticPropertyOperatorProvider(
		final Class<?> type,
		final Class<?> elementValueType,
		final IValueRange valueRange) {
		return DefaultOperatorProvider.getArithmeticPropertyOperatorProvider(type, elementValueType, valueRange);
	}

	@Override
	public IAttributeFilter arithmeticPropertyAttributeFilter(final Class<?> type, final Class<?> elementValueType) {
		return new DefaultArithmeticPropertyAttributeFilter(type, elementValueType);
	}

	@Override
	public <VALUE_TYPE> IIncludingFilterFactory<VALUE_TYPE> includingFilterFactory(
		final String propertyName,
		final Class<? extends VALUE_TYPE> type) {
		return new DefaultIncludingFilterFactory<VALUE_TYPE>(propertyName, type);
	}

	@Override
	public <ELEMENT_VALUE_TYPE> IFilterSupport<?> filterSupport(
		final String propertyName,
		final Class<?> type,
		final Class<?> elementValueType,
		final IValueRange valueRange,
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator,
		final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator) {

		final IOperatorProvider<ArithmeticOperator> arithmeticOperatorProvider = arithmeticOperatorProvider(
				type,
				elementValueType,
				valueRange);

		final IOperatorProvider<ArithmeticOperator> arithmeticPropertyOperatorProvider = arithmeticPropertyOperatorProvider(
				type,
				elementValueType,
				valueRange);

		final IAttributeFilter arithmeticPropertyAttributeFilter = arithmeticPropertyAttributeFilter(type, elementValueType);

		return filterSupport(
				propertyName,
				type,
				elementValueType,
				arithmeticOperatorProvider,
				arithmeticPropertyOperatorProvider,
				arithmeticPropertyAttributeFilter,
				includingFilterFactory(propertyName, type),
				controlCreator,
				collectionControlCreator);
	}

	@Override
	public <ELEMENT_VALUE_TYPE, VALUE_TYPE> IFilterSupport<?> filterSupport(
		final String propertyName,
		final Class<?> type,
		final Class<?> elementValueType,
		final IOperatorProvider<ArithmeticOperator> arithmeticOperatorProvider,
		final IOperatorProvider<ArithmeticOperator> arithmeticPropertyOperatorProvider,
		final IAttributeFilter attributeFilter,
		final IIncludingFilterFactory<VALUE_TYPE> includingFilterFactory,
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator,
		final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator) {

		final List<IFilterPanelProvider<?>> result = new LinkedList<IFilterPanelProvider<?>>();

		if (arithmeticOperatorProvider != null) {
			result.add(arithmeticFilterPanel(
					propertyName,
					type,
					elementValueType,
					arithmeticOperatorProvider,
					controlCreator,
					collectionControlCreator));
		}

		if (arithmeticPropertyOperatorProvider != null) {
			result.add(arithmeticPropertyFilterPanel(
					propertyName,
					type,
					elementValueType,
					arithmeticPropertyOperatorProvider,
					attributeFilter));
		}

		return new IFilterSupport<VALUE_TYPE>() {
			@Override
			public List<IFilterPanelProvider<?>> getFilterPanels() {
				return result;
			}

			@Override
			public IIncludingFilterFactory<VALUE_TYPE> getIncludingFilterFactory() {
				return includingFilterFactory;
			}
		};

	}

	@Override
	public <ELEMENT_VALUE_TYPE> IFilterPanelProvider<ArithmeticOperator> arithmeticFilterPanel(
		final String propertyName,
		final Class<?> type,
		final Class<?> elementValueType,
		final IOperatorProvider<ArithmeticOperator> operatorProvider,
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator,
		final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator) {
		return new ArithmeticFilterPanelProviderImpl<ELEMENT_VALUE_TYPE>(
			propertyName,
			elementValueType,
			operatorProvider,
			controlCreator,
			collectionControlCreator);
	}

	@Override
	public <ELEMENT_VALUE_TYPE> IFilterPanelProvider<ArithmeticOperator> arithmeticPropertyFilterPanel(
		final String propertyName,
		final Class<?> type,
		final Class<?> elementValueType,
		final IOperatorProvider<ArithmeticOperator> arithmeticPropertyOperatorProvider,
		final IAttributeFilter attributeFilter) {
		// TODO MG implement arithmeticPropertyFilterPanel
		return null;
	}

}
