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

package org.jowidgets.cap.ui.api.filter;

import java.util.Collection;

import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.ui.api.attribute.IAttributeFilter;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;

public interface IFilterToolkit {

	IUiFilterFactory filterFactory();

	IOperatorProvider<ArithmeticOperator> arithmeticOperatorProvider(Class<?> type, Class<?> elementValueType);

	IOperatorProvider<ArithmeticOperator> arithmeticPropertyOperatorProvider(Class<?> type, Class<?> elementValueType);

	IAttributeFilter arithmeticPropertyAttributeFilter(Class<?> type, Class<?> elementValueType);

	<VALUE_TYPE> IIncludingFilterFactory<VALUE_TYPE> includingFilterFactory(
		Class<? extends VALUE_TYPE> type,
		Class<?> elementValueType);

	<ELEMENT_VALUE_TYPE> IFilterSupport<?> filterSupport(
		Class<?> type,
		Class<?> elementValueType,
		ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator,
		ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator);

	<ELEMENT_VALUE_TYPE, VALUE_TYPE> IFilterSupport<?> filterSupport(
		IOperatorProvider<ArithmeticOperator> arithmeticOperatorProvider,
		IOperatorProvider<ArithmeticOperator> arithmeticPropertyOperatorProvider,
		IAttributeFilter attributeFilter,
		IIncludingFilterFactory<VALUE_TYPE> includingFilterFactory,
		ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator,
		ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator);

	<ELEMENT_VALUE_TYPE> IFilterPanelProvider<ArithmeticOperator> arithmeticFilterPanel(
		IOperatorProvider<ArithmeticOperator> operatorProvider,
		ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator,
		ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator);

	<ELEMENT_VALUE_TYPE> IFilterPanelProvider<ArithmeticOperator> arithmeticPropertyFilterPanel(
		IOperatorProvider<ArithmeticOperator> arithmeticPropertyOperatorProvider,
		IAttributeFilter attributeFilter,
		ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator,
		ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator);

}
