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

import org.jowidgets.api.convert.IConverter;
import org.jowidgets.api.convert.IObjectLabelConverter;
import org.jowidgets.api.convert.IObjectStringConverter;
import org.jowidgets.api.convert.IStringObjectConverter;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProviderBluePrint;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProviderBuilder;
import org.jowidgets.cap.ui.api.attribute.IGenericControlPanelProviderBluePrint;
import org.jowidgets.cap.ui.api.control.IDisplayFormat;
import org.jowidgets.cap.ui.api.filter.IFilterSupport;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.util.Assert;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.maybe.Nothing;
import org.jowidgets.util.maybe.Some;

final class ControlPanelProviderBluePrintImpl<ELEMENT_VALUE_TYPE> implements IControlPanelProviderBluePrint<ELEMENT_VALUE_TYPE> {

	private final IDisplayFormat displayFormat;

	private IMaybe<IObjectLabelConverter<? extends ELEMENT_VALUE_TYPE>> objectLabelConverter;
	private IMaybe<IObjectStringConverter<? extends ELEMENT_VALUE_TYPE>> objectStringConverter;
	private IMaybe<IStringObjectConverter<ELEMENT_VALUE_TYPE>> stringObjectConverter;
	private IMaybe<IConverter<? extends ELEMENT_VALUE_TYPE>> converter;
	private IMaybe<IFilterSupport<?>> filterSupport;
	private IMaybe<ICustomWidgetCreator<? extends IInputControl<? extends ELEMENT_VALUE_TYPE>>> controlCreator;
	private IMaybe<ICustomWidgetCreator<? extends IInputControl<? extends Collection<? extends ELEMENT_VALUE_TYPE>>>> collectionControlCreator;
	private IMaybe<ICustomWidgetCreator<? extends IInputControl<? extends Collection<? extends ELEMENT_VALUE_TYPE>>>> filterCollectionControlCreator;

	private boolean exhausted = false;

	ControlPanelProviderBluePrintImpl(final IDisplayFormat displayFormat) {
		Assert.paramNotNull(displayFormat, "displayFormat");

		this.displayFormat = displayFormat;
		this.objectLabelConverter = Nothing.getInstance();
		this.objectStringConverter = Nothing.getInstance();
		this.stringObjectConverter = Nothing.getInstance();
		this.converter = Nothing.getInstance();
		this.filterSupport = Nothing.getInstance();
		this.controlCreator = Nothing.getInstance();
		this.collectionControlCreator = Nothing.getInstance();
		this.filterCollectionControlCreator = Nothing.getInstance();
	}

	IDisplayFormat getDisplayFormat() {
		return displayFormat;
	}

	@Override
	public IControlPanelProviderBluePrint<ELEMENT_VALUE_TYPE> setObjectLabelConverter(
		final IObjectLabelConverter<? extends ELEMENT_VALUE_TYPE> objectLabelConverter) {
		checkExhausted();
		this.objectLabelConverter = new Some<IObjectLabelConverter<? extends ELEMENT_VALUE_TYPE>>(objectLabelConverter);
		this.objectStringConverter = Nothing.getInstance();
		this.converter = Nothing.getInstance();
		return this;
	}

	@Override
	public IControlPanelProviderBluePrint<ELEMENT_VALUE_TYPE> setObjectLabelConverter(
		final IObjectStringConverter<? extends ELEMENT_VALUE_TYPE> objectStringConverter) {
		checkExhausted();
		this.objectStringConverter = new Some<IObjectStringConverter<? extends ELEMENT_VALUE_TYPE>>(objectStringConverter);
		this.objectLabelConverter = Nothing.getInstance();
		this.converter = Nothing.getInstance();
		return this;
	}

	@Override
	public IControlPanelProviderBluePrint<ELEMENT_VALUE_TYPE> setStringObjectConverter(
		final IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter) {
		checkExhausted();
		this.stringObjectConverter = new Some<IStringObjectConverter<ELEMENT_VALUE_TYPE>>(stringObjectConverter);
		this.converter = Nothing.getInstance();
		return this;
	}

	@Override
	public IControlPanelProviderBluePrint<ELEMENT_VALUE_TYPE> setConverter(
		final IConverter<? extends ELEMENT_VALUE_TYPE> converter) {
		checkExhausted();
		this.converter = new Some<IConverter<? extends ELEMENT_VALUE_TYPE>>(converter);
		this.objectStringConverter = Nothing.getInstance();
		this.objectLabelConverter = Nothing.getInstance();
		this.stringObjectConverter = Nothing.getInstance();
		return this;
	}

	@Override
	public IControlPanelProviderBluePrint<ELEMENT_VALUE_TYPE> setFilterSupport(final IFilterSupport<?> filterSupport) {
		checkExhausted();
		this.filterSupport = new Some<IFilterSupport<?>>(filterSupport);
		return this;
	}

	@Override
	public IControlPanelProviderBluePrint<ELEMENT_VALUE_TYPE> setControlCreator(
		final ICustomWidgetCreator<? extends IInputControl<? extends ELEMENT_VALUE_TYPE>> controlCreator) {
		checkExhausted();
		this.controlCreator = new Some<ICustomWidgetCreator<? extends IInputControl<? extends ELEMENT_VALUE_TYPE>>>(
			controlCreator);
		return this;
	}

	@Override
	public IControlPanelProviderBluePrint<ELEMENT_VALUE_TYPE> setCollectionControlCreator(
		final ICustomWidgetCreator<? extends IInputControl<? extends Collection<? extends ELEMENT_VALUE_TYPE>>> collectionControlCreator) {
		checkExhausted();
		this.collectionControlCreator = new Some<ICustomWidgetCreator<? extends IInputControl<? extends Collection<? extends ELEMENT_VALUE_TYPE>>>>(
			collectionControlCreator);
		return this;
	}

	@Override
	public IControlPanelProviderBluePrint<ELEMENT_VALUE_TYPE> setFilterCollectionControlCreator(
		final ICustomWidgetCreator<? extends IInputControl<? extends Collection<? extends ELEMENT_VALUE_TYPE>>> collectionControlCreator) {
		this.filterCollectionControlCreator = new Some<ICustomWidgetCreator<? extends IInputControl<? extends Collection<? extends ELEMENT_VALUE_TYPE>>>>(
			collectionControlCreator);
		return this;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	IControlPanelProvider<ELEMENT_VALUE_TYPE> create(
		final String propertyName,
		final Class valueType,
		final Class elementValueType,
		final IValueRange valueRange) {

		final IAttributeToolkit attributeToolkit = CapUiToolkit.attributeToolkit();
		final IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> builder;
		if (valueType == null) {
			builder = attributeToolkit.createControlPanelProviderBuilder(propertyName, elementValueType, valueRange);
		}
		else {
			builder = attributeToolkit.createControlPanelProviderBuilder(propertyName, valueType, elementValueType, valueRange);
		}

		builder.setDisplayFormat(displayFormat);

		modifyBluePrint(builder);

		final IControlPanelProvider<ELEMENT_VALUE_TYPE> result = builder.build();
		this.exhausted = true;
		return result;
	}

	void modifyBluePrint(final IGenericControlPanelProviderBluePrint<ELEMENT_VALUE_TYPE, ?> bluePrint) {
		if (objectLabelConverter.isSomething()) {
			bluePrint.setObjectLabelConverter(objectLabelConverter.getValue());
		}
		if (objectStringConverter.isSomething()) {
			bluePrint.setObjectLabelConverter(objectStringConverter.getValue());
		}
		if (stringObjectConverter.isSomething()) {
			bluePrint.setStringObjectConverter(stringObjectConverter.getValue());
		}
		if (converter.isSomething()) {
			bluePrint.setConverter(converter.getValue());
		}
		if (filterSupport.isSomething()) {
			bluePrint.setFilterSupport(filterSupport.getValue());
		}
		if (controlCreator.isSomething()) {
			bluePrint.setControlCreator(controlCreator.getValue());
		}
		if (collectionControlCreator.isSomething()) {
			bluePrint.setCollectionControlCreator(collectionControlCreator.getValue());
		}
		if (filterCollectionControlCreator.isSomething()) {
			bluePrint.setFilterCollectionControlCreator(filterCollectionControlCreator.getValue());
		}
	}

	private void checkExhausted() {
		if (exhausted) {
			throw new IllegalStateException("The builder is exhausted. ");
		}
	}

}
