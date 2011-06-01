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
import org.jowidgets.api.convert.IConverterProvider;
import org.jowidgets.api.convert.IObjectLabelConverter;
import org.jowidgets.api.convert.IObjectStringConverter;
import org.jowidgets.api.convert.IStringObjectConverter;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.ui.api.attribute.IArithmeticOperatorProvider;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProviderBuilder;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.tools.converter.AbstractObjectLabelConverter;
import org.jowidgets.util.Assert;

final class ControlPanelProviderBuilderImpl<ELEMENT_VALUE_TYPE> implements IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> {

	private static final String DEFAULT_DISPLAY_FORMAT_ID = ControlPanelProviderBuilderImpl.class.getName() + "_DEFAULT_ID";
	private static final String DEFAULT_DISPLAY_NAME = "Default";

	@SuppressWarnings("unused")
	private Class<?> valueType;
	private Class<? extends ELEMENT_VALUE_TYPE> elementValueType;

	private String displayFormatId;
	private String displayFormatName;
	private String displayFormatDescription;
	private IArithmeticOperatorProvider arithmeticOperatorProvider;
	private IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverter;
	private IObjectStringConverter<ELEMENT_VALUE_TYPE> objectStringConverter;
	private IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter;
	private ICustomWidgetCreator<IInputControl<? extends IFilter>> filterControlCreator;
	private ICustomWidgetCreator<IInputControl<? extends ELEMENT_VALUE_TYPE>> controlCreator;
	private ICustomWidgetCreator<IInputControl<Collection<? extends ELEMENT_VALUE_TYPE>>> collectionControlCreator;

	ControlPanelProviderBuilderImpl(final Class<? extends ELEMENT_VALUE_TYPE> elementValueType) {
		this();
		Assert.paramNotNull(elementValueType, "elementValueType");
		if (Collection.class.isAssignableFrom(elementValueType)) {
			throw new IllegalArgumentException("The parameter 'elementValueType' must not be a 'Collection'");
		}
		this.valueType = elementValueType;
		this.elementValueType = elementValueType;
	}

	ControlPanelProviderBuilderImpl(
		final Class<? extends Collection<? extends ELEMENT_VALUE_TYPE>> valueType,
		final Class<? extends ELEMENT_VALUE_TYPE> elementValueType) {
		this();
		Assert.paramNotNull(valueType, "valueType");
		Assert.paramNotNull(elementValueType, "elementValueType");
		this.valueType = valueType;
		this.elementValueType = elementValueType;
	}

	private ControlPanelProviderBuilderImpl() {
		super();
		this.displayFormatId = DEFAULT_DISPLAY_FORMAT_ID;
		this.displayFormatName = DEFAULT_DISPLAY_NAME;
	}

	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setDisplayFormatId(final String id) {
		Assert.paramNotEmpty(id, "id");
		this.displayFormatId = id;
		return this;
	}

	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setDisplayFormatName(final String name) {
		Assert.paramNotEmpty(name, "name");
		this.displayFormatName = name;
		return this;
	}

	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setDisplayFormatDescription(final String description) {
		this.displayFormatDescription = description;
		return this;
	}

	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setObjectLabelConverter(
		final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverter) {
		this.objectStringConverter = null;
		this.objectLabelConverter = objectLabelConverter;
		return this;
	}

	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setObjectLabelConverter(
		final IObjectStringConverter<ELEMENT_VALUE_TYPE> objectStringConverter) {
		this.objectLabelConverter = null;
		this.objectStringConverter = objectStringConverter;
		return null;
	}

	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setStringObjectConverter(
		final IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter) {
		this.stringObjectConverter = stringObjectConverter;
		return this;
	}

	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setConverter(final IConverter<ELEMENT_VALUE_TYPE> converter) {
		this.objectLabelConverter = null;
		this.objectStringConverter = converter;
		this.stringObjectConverter = converter;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setFilterControlCreator(
		final ICustomWidgetCreator<? extends IInputControl<? extends IFilter>> filterControlCreator) {
		this.arithmeticOperatorProvider = null;
		this.filterControlCreator = (ICustomWidgetCreator<IInputControl<? extends IFilter>>) filterControlCreator;
		return this;
	}

	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setControlCreator(
		final IArithmeticOperatorProvider arithmeticOperatorProvider) {
		this.filterControlCreator = null;
		this.arithmeticOperatorProvider = arithmeticOperatorProvider;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setControlCreator(
		final ICustomWidgetCreator<? extends IInputControl<? extends ELEMENT_VALUE_TYPE>> controlCreator) {
		this.controlCreator = (ICustomWidgetCreator<IInputControl<? extends ELEMENT_VALUE_TYPE>>) controlCreator;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setCollectionControlCreator(
		final ICustomWidgetCreator<? extends IInputControl<Collection<? extends ELEMENT_VALUE_TYPE>>> collectionControlCreator) {
		this.collectionControlCreator = (ICustomWidgetCreator<IInputControl<Collection<? extends ELEMENT_VALUE_TYPE>>>) collectionControlCreator;
		return this;
	}

	private IObjectStringConverter<ELEMENT_VALUE_TYPE> getObjectStringConverter() {
		if (objectStringConverter == null) {
			final IConverterProvider converterProvider = Toolkit.getConverterProvider();
			final IConverter<ELEMENT_VALUE_TYPE> converter = converterProvider.getConverter(elementValueType);

			if (converter != null) {
				objectStringConverter = converter;
			}
			else {
				objectStringConverter = converterProvider.toStringConverter();
			}
		}
		return objectStringConverter;
	}

	private IObjectLabelConverter<ELEMENT_VALUE_TYPE> getObjectLabelConverter() {
		if (objectLabelConverter == null) {
			final IObjectStringConverter<ELEMENT_VALUE_TYPE> objStringConverter = getObjectStringConverter();

			objectLabelConverter = new AbstractObjectLabelConverter<ELEMENT_VALUE_TYPE>() {

				@Override
				public String convertToString(final ELEMENT_VALUE_TYPE value) {
					return objStringConverter.convertToString(value);
				}

				@Override
				public String getDescription(final ELEMENT_VALUE_TYPE value) {
					return objStringConverter.getDescription(value);
				}

			};
		}
		return objectLabelConverter;
	}

	protected IStringObjectConverter<ELEMENT_VALUE_TYPE> getStringObjectConverter() {
		if (stringObjectConverter == null) {
			final IConverterProvider converterProvider = Toolkit.getConverterProvider();
			stringObjectConverter = converterProvider.getConverter(elementValueType);
		}
		return stringObjectConverter;
	}

	private ICustomWidgetCreator<IInputControl<? extends ELEMENT_VALUE_TYPE>> getControlCreator() {
		//		if (controlCreator == null) {
		//			//TODO create by element value type
		//		}
		return controlCreator;
	}

	@SuppressWarnings("unused")
	private IArithmeticOperatorProvider getArithmeticOperatorProvider() {
		//		if (arithmeticOperatorProvider == null) {
		//			//TODO create by element value type
		//		}
		return arithmeticOperatorProvider;
	}

	private ICustomWidgetCreator<IInputControl<? extends IFilter>> getFilterControlCreator() {
		//		if (filterControlCreator == null) {
		//			//TODO create by control factory and arithmetic operator provider
		//		}
		return filterControlCreator;
	}

	private ICustomWidgetCreator<IInputControl<Collection<? extends ELEMENT_VALUE_TYPE>>> getCollectionControlCreator() {
		//		if (collectionControlCreator == null) {
		//			//TODO create by element value type and value type
		//		}
		return collectionControlCreator;
	}

	@Override
	public IControlPanelProvider<ELEMENT_VALUE_TYPE> build() {
		return new ControlPanelProviderImpl<ELEMENT_VALUE_TYPE>(
			displayFormatId,
			displayFormatName,
			displayFormatDescription,
			getObjectLabelConverter(),
			getStringObjectConverter(),
			getFilterControlCreator(),
			getControlCreator(),
			getCollectionControlCreator());
	}

}
