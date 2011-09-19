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
	public IOperatorProvider<ArithmeticOperator> arithmeticOperatorProvider(final Class<?> type, final Class<?> elementValueType) {
		// TODO MG implement filter stuff
		return null;
	}

	@Override
	public IOperatorProvider<ArithmeticOperator> arithmeticPropertyOperatorProvider(
		final Class<?> type,
		final Class<?> elementValueType) {
		// TODO MG implement filter stuff
		return null;
	}

	@Override
	public IAttributeFilter arithmeticPropertyAttributeFilter(final Class<?> type, final Class<?> elementValueType) {
		// TODO MG implement filter stuff
		return null;
	}

	@Override
	public <VALUE_TYPE> IIncludingFilterFactory<VALUE_TYPE> includingFilterFactory(
		final Class<? extends VALUE_TYPE> type,
		final Class<?> elementValueType) {
		// TODO MG implement filter stuff
		return null;
	}

	@Override
	public <ELEMENT_VALUE_TYPE> IFilterSupport<?> filterSupport(
		final Class<?> type,
		final Class<?> elementValueType,
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator,
		final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator) {

		final IOperatorProvider<ArithmeticOperator> arithmeticOperatorProvider = arithmeticOperatorProvider(
				type,
				elementValueType);

		final IOperatorProvider<ArithmeticOperator> arithmeticPropertyOperatorProvider = arithmeticPropertyOperatorProvider(
				type,
				elementValueType);

		final IAttributeFilter arithmeticPropertyAttributeFilter = arithmeticPropertyAttributeFilter(type, elementValueType);

		return filterSupport(
				arithmeticOperatorProvider,
				arithmeticPropertyOperatorProvider,
				arithmeticPropertyAttributeFilter,
				includingFilterFactory(type, elementValueType),
				controlCreator,
				collectionControlCreator);
	}

	@Override
	public <ELEMENT_VALUE_TYPE, VALUE_TYPE> IFilterSupport<?> filterSupport(
		final IOperatorProvider<ArithmeticOperator> arithmeticOperatorProvider,
		final IOperatorProvider<ArithmeticOperator> arithmeticPropertyOperatorProvider,
		final IAttributeFilter attributeFilter,
		final IIncludingFilterFactory<VALUE_TYPE> includingFilterFactory,
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator,
		final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator) {

		final List<IFilterPanelProvider<?>> result = new LinkedList<IFilterPanelProvider<?>>();

		result.add(arithmeticFilterPanel(arithmeticOperatorProvider, controlCreator, collectionControlCreator));
		result.add(arithmeticPropertyFilterPanel(
				arithmeticPropertyOperatorProvider,
				attributeFilter,
				controlCreator,
				collectionControlCreator));

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
		final IOperatorProvider<ArithmeticOperator> operatorProvider,
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator,
		final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator) {
		// TODO MG implement filter stuff
		return null;
	}

	@Override
	public <ELEMENT_VALUE_TYPE> IFilterPanelProvider<ArithmeticOperator> arithmeticPropertyFilterPanel(
		final IOperatorProvider<ArithmeticOperator> arithmeticPropertyOperatorProvider,
		final IAttributeFilter attributeFilter,
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator,
		final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator) {
		// TODO MG implement filter stuff
		return null;
	}

}
