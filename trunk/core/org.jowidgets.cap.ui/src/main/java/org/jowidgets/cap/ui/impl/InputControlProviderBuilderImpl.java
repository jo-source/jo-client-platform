/*
 * Copyright (c) 2012, grossmann
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

import org.jowidgets.api.convert.IConverter;
import org.jowidgets.api.convert.IObjectLabelConverter;
import org.jowidgets.api.convert.IStringObjectConverter;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.control.IDisplayFormat;
import org.jowidgets.cap.ui.api.control.IInputControlProvider;
import org.jowidgets.cap.ui.api.control.IInputControlProviderBuilder;
import org.jowidgets.cap.ui.api.control.IInputControlSupport;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.tools.converter.ObjectStringObjectLabelConverterAdapter;
import org.jowidgets.util.Assert;

final class InputControlProviderBuilderImpl<ELEMENT_VALUE_TYPE> implements IInputControlProviderBuilder<ELEMENT_VALUE_TYPE> {

	private final Class<ELEMENT_VALUE_TYPE> elementValueType;

	private IDisplayFormat displayFormat;
	private IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverter;
	private IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter;
	private ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator;
	private ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator;

	public InputControlProviderBuilderImpl(final Class<ELEMENT_VALUE_TYPE> elementValueType) {
		Assert.paramNotNull(elementValueType, "elementValueType");
		this.elementValueType = elementValueType;
		this.displayFormat = CapUiToolkit.displayFormatFactory().getDefault();
	}

	@Override
	public IInputControlProviderBuilder<ELEMENT_VALUE_TYPE> setDisplayFormat(final IDisplayFormat displayFormat) {
		this.displayFormat = displayFormat;
		return this;
	}

	@Override
	public IInputControlProviderBuilder<ELEMENT_VALUE_TYPE> setObjectLabelConverter(
		final IObjectLabelConverter<ELEMENT_VALUE_TYPE> converter) {
		this.objectLabelConverter = converter;
		return this;
	}

	@Override
	public IInputControlProviderBuilder<ELEMENT_VALUE_TYPE> setStringObjectConverter(
		final IStringObjectConverter<ELEMENT_VALUE_TYPE> converter) {
		this.stringObjectConverter = converter;
		return this;
	}

	@Override
	public IInputControlProviderBuilder<ELEMENT_VALUE_TYPE> setConverter(final IConverter<ELEMENT_VALUE_TYPE> converter) {
		setObjectLabelConverter(new ObjectStringObjectLabelConverterAdapter<ELEMENT_VALUE_TYPE>(converter));
		setStringObjectConverter(converter);
		return this;
	}

	@Override
	public IInputControlProviderBuilder<ELEMENT_VALUE_TYPE> setControlCreator(
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator) {
		this.controlCreator = controlCreator;
		return this;
	}

	@Override
	public IInputControlProviderBuilder<ELEMENT_VALUE_TYPE> setCollectionControlCreator(
		final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator) {
		this.collectionControlCreator = collectionControlCreator;
		return this;
	}

	@Override
	public IInputControlSupport<ELEMENT_VALUE_TYPE> buildInputControlSupport() {
		return buildImpl();
	}

	@Override
	public IInputControlProvider<ELEMENT_VALUE_TYPE> build() {
		return buildImpl();
	}

	private ControlProviderImpl<ELEMENT_VALUE_TYPE> buildImpl() {
		return new ControlProviderImpl<ELEMENT_VALUE_TYPE>(
			elementValueType,
			displayFormat,
			objectLabelConverter,
			stringObjectConverter,
			controlCreator,
			collectionControlCreator);
	}

}
