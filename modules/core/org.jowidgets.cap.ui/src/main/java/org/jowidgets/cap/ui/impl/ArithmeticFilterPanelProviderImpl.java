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

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.filter.FilterType;
import org.jowidgets.cap.ui.api.filter.IFilterControl;
import org.jowidgets.cap.ui.api.filter.IFilterControlCreator;
import org.jowidgets.cap.ui.api.filter.IFilterPanelProvider;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.cap.ui.api.filter.IOperatorProvider;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.factory.ICustomWidgetFactory;

final class ArithmeticFilterPanelProviderImpl<ELEMENT_VALUE_TYPE> implements
		IFilterPanelProvider<ArithmeticOperator>,
		IFilterControlCreator<ArithmeticOperator> {

	private final String propertyName;
	private final Class<?> elementValueType;
	private final IOperatorProvider<ArithmeticOperator> operatorProvider;
	private final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator;
	private final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator;

	ArithmeticFilterPanelProviderImpl(
		final String propertyName,
		final Class<?> elementValueType,
		final IOperatorProvider<ArithmeticOperator> operatorProvider,
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator,
		final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator) {
		this.propertyName = propertyName;
		this.elementValueType = elementValueType;
		this.operatorProvider = operatorProvider;
		this.controlCreator = controlCreator;
		this.collectionControlCreator = collectionControlCreator;
	}

	@Override
	public IFilterType getType() {
		return FilterType.ARITHMETIC_FILTER;
	}

	@Override
	public IFilterControlCreator<ArithmeticOperator> getFilterControlCreator() {
		return this;
	}

	@Override
	public IOperatorProvider<ArithmeticOperator> getOperatorProvider() {
		return operatorProvider;
	}

	@Override
	public boolean isApplicableWith(final Collection<IAttribute<?>> attributes) {
		return true;
	}

	@Override
	public IFilterControl<ArithmeticOperator, ?, ?> create(
		final ICustomWidgetFactory widgetFactory,
		final Collection<? extends IAttribute<?>> attributes) {
		final IComposite composite = widgetFactory.create(Toolkit.getBluePrintFactory().composite());
		return new DefaultArithmeticFilterControl<ELEMENT_VALUE_TYPE>(
			propertyName,
			elementValueType,
			operatorProvider,
			controlCreator,
			collectionControlCreator,
			composite);
	}

}
