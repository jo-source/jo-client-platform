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
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.cap.common.api.bean.Cardinality;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.common.api.lookup.ILookUpValueRange;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProviderBuilder;
import org.jowidgets.cap.ui.api.control.DisplayFormat;
import org.jowidgets.cap.ui.api.control.IDisplayFormat;
import org.jowidgets.cap.ui.api.control.IInputControlProvider;
import org.jowidgets.cap.ui.api.control.IInputControlSupport;
import org.jowidgets.cap.ui.api.filter.IFilterSupport;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.tools.converter.ObjectStringObjectLabelConverterAdapter;
import org.jowidgets.util.Assert;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.maybe.Some;

final class ControlPanelProviderBuilderImpl<ELEMENT_VALUE_TYPE> implements IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> {

	static final Object DEFAULT_DISPLAY_FORMAT_ID = DisplayFormat.SHORT.getId();
	static final String DEFAULT_DISPLAY_NAME = DisplayFormat.SHORT.getName();

	private final String propertyName;
	private final IValueRange valueRange;
	private final Cardinality cardinality;

	private Class<?> valueType;
	private Class<ELEMENT_VALUE_TYPE> elementValueType;

	private Object displayFormatId;
	private String displayFormatName;
	private String displayFormatDescription;
	private IMaybe<IObjectLabelConverter<ELEMENT_VALUE_TYPE>> objectLabelConverter;
	private IMaybe<IObjectStringConverter<ELEMENT_VALUE_TYPE>> objectStringConverter;
	private IMaybe<IStringObjectConverter<ELEMENT_VALUE_TYPE>> stringObjectConverter;
	private IMaybe<IFilterSupport<?>> filterSupport;
	private IMaybe<ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>>> controlCreator;
	private IMaybe<ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>>> collectionControlCreator;
	private IMaybe<ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>>> filterCollectionControlCreator;

	@SuppressWarnings("unchecked")
	ControlPanelProviderBuilderImpl(
		final String propertyName,
		final Class<? extends ELEMENT_VALUE_TYPE> elementValueType,
		final IValueRange valueRange,
		final Cardinality cardinality) {
		this(propertyName, valueRange, cardinality);
		Assert.paramNotNull(elementValueType, "elementValueType");
		if (Collection.class.isAssignableFrom(elementValueType)) {
			throw new IllegalArgumentException("The parameter 'elementValueType' must not be a 'Collection'");
		}
		this.valueType = elementValueType;
		this.elementValueType = (Class<ELEMENT_VALUE_TYPE>) elementValueType;
	}

	@SuppressWarnings("unchecked")
	ControlPanelProviderBuilderImpl(
		final String propertyName,
		final Class<?> valueType,
		final Class<? extends ELEMENT_VALUE_TYPE> elementValueType,
		final IValueRange valueRange,
		final Cardinality cardinality) {
		this(propertyName, valueRange, cardinality);
		Assert.paramNotNull(valueType, "valueType");
		Assert.paramNotNull(elementValueType, "elementValueType");
		this.valueType = valueType;
		this.elementValueType = (Class<ELEMENT_VALUE_TYPE>) elementValueType;
	}

	private ControlPanelProviderBuilderImpl(final String propertyName, final IValueRange valueRange, final Cardinality cardinality) {
		super();
		Assert.paramNotNull(propertyName, "propertyName");
		Assert.paramNotNull(valueRange, "valueRange");
		Assert.paramNotNull(cardinality, "cardinality");
		this.propertyName = propertyName;
		this.valueRange = valueRange;
		this.cardinality = cardinality;
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
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setDisplayFormat(final IDisplayFormat displayFormat) {
		Assert.paramNotNull(displayFormat, "displayFormat");
		this.displayFormatId = displayFormat.getId();
		this.displayFormatName = displayFormat.getName();
		this.displayFormatDescription = displayFormat.getDescription();
		return this;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setObjectLabelConverter(
		final IObjectLabelConverter objectLabelConverter) {
		this.objectStringConverter = null;
		this.objectLabelConverter = new Some(objectLabelConverter);
		return this;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setObjectLabelConverter(
		final IObjectStringConverter objectStringConverter) {
		if (objectStringConverter instanceof IObjectLabelConverter<?>) {
			setObjectLabelConverter((IObjectLabelConverter<?>) objectStringConverter);
		}
		else {
			this.objectLabelConverter = null;
			this.objectStringConverter = new Some(objectStringConverter);
		}
		return null;
	}

	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setStringObjectConverter(
		final IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter) {
		this.stringObjectConverter = new Some<IStringObjectConverter<ELEMENT_VALUE_TYPE>>(stringObjectConverter);
		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setConverter(final IConverter converter) {
		setObjectLabelConverter(converter);
		setStringObjectConverter(converter);
		return this;
	}

	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setFilterSupport(final IFilterSupport<?> filterSupport) {
		this.filterSupport = new Some<IFilterSupport<?>>(filterSupport);
		return this;
	}

	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setFilterSupport(
		final IConverter<ELEMENT_VALUE_TYPE> elementValueConverter) {
		Assert.paramNotNull(elementValueConverter, "elementValueConverter");
		setFilterSupport(CapUiToolkit.filterToolkit().filterSupport(
				propertyName,
				valueType,
				elementValueType,
				valueRange,
				cardinality,
				elementValueConverter));
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setControlCreator(
		final ICustomWidgetCreator<? extends IInputControl<? extends ELEMENT_VALUE_TYPE>> controlCreator) {
		this.controlCreator = new Some<ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>>>(
			(ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>>) controlCreator);
		return this;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setCollectionControlCreator(
		final ICustomWidgetCreator collectionControlCreator) {
		this.collectionControlCreator = new Some(collectionControlCreator);
		return this;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setFilterCollectionControlCreator(
		final ICustomWidgetCreator collectionControlCreator) {
		this.filterCollectionControlCreator = new Some(collectionControlCreator);
		return this;
	}

	private IObjectStringConverter<ELEMENT_VALUE_TYPE> getObjectStringConverter() {
		if (objectStringConverter == null) {
			return Toolkit.getConverterProvider().getObjectStringConverter(elementValueType);
		}
		else {
			return objectStringConverter.getValue();
		}
	}

	private IObjectLabelConverter<ELEMENT_VALUE_TYPE> getObjectLabelConverter() {
		if (objectLabelConverter == null) {
			final IObjectStringConverter<ELEMENT_VALUE_TYPE> objStringConv = getObjectStringConverter();
			if (objStringConv != null) {
				return new ObjectStringObjectLabelConverterAdapter<ELEMENT_VALUE_TYPE>(objStringConv);
			}
			else {
				return null;
			}
		}
		else {
			return objectLabelConverter.getValue();
		}
	}

	protected IStringObjectConverter<ELEMENT_VALUE_TYPE> getStringObjectConverter() {
		if (stringObjectConverter == null) {
			return Toolkit.getConverterProvider().getConverter(elementValueType);
		}
		else {
			return stringObjectConverter.getValue();
		}
	}

	private ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> getControlCreator() {
		if (controlCreator == null) {
			final IInputControlProvider<ELEMENT_VALUE_TYPE> defaultControl = createDefaultControlProvider();
			if (defaultControl != null) {
				return defaultControl.getControlCreator(
						getObjectLabelConverter(defaultControl),
						getStringObjectConverter(defaultControl),
						valueRange);
			}
			else {
				return null;
			}
		}
		else {
			return controlCreator.getValue();
		}
	}

	private ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> getCollectionControlCreator() {
		if (collectionControlCreator == null) {
			final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionCreator;
			collectionCreator = createCollectionControlCreator();
			final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> elementCreator = getControlCreator();
			if (Collection.class.isAssignableFrom(valueType)
				&& Cardinality.LESS_OR_EQUAL_ONE == cardinality
				&& collectionCreator != null
				&& elementCreator != null) {
				return new CombinedCollectionControlCreator<ELEMENT_VALUE_TYPE>(elementCreator, collectionCreator);
			}
			else {
				return collectionCreator;
			}
		}
		else {
			return collectionControlCreator.getValue();
		}
	}

	private ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> getFilterCollectionControlCreator() {
		if (filterCollectionControlCreator == null) {
			return createCollectionControlCreator();
		}
		else {
			return filterCollectionControlCreator.getValue();
		}
	}

	private ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> createCollectionControlCreator() {
		final IInputControlProvider<ELEMENT_VALUE_TYPE> defaultControl = createDefaultControlProvider();
		if (defaultControl != null) {
			return defaultControl.getCollectionControlCreator(
					getControlCreator(defaultControl),
					getObjectLabelConverter(defaultControl),
					getStringObjectConverter(defaultControl),
					valueRange);
		}
		else {
			return null;
		}
	}

	private IInputControlProvider<ELEMENT_VALUE_TYPE> createDefaultControlProvider() {
		if (valueRange instanceof ILookUpValueRange) {
			final IInputControlSupport<ELEMENT_VALUE_TYPE> controls;
			controls = CapUiToolkit.inputControlRegistry().getControls((ILookUpValueRange) valueRange);
			return getDefaultControl(controls);
		}
		else {
			return getDefaultControl(CapUiToolkit.inputControlRegistry().getControls(elementValueType));
		}
	}

	private IInputControlProvider<ELEMENT_VALUE_TYPE> getDefaultControl(final IInputControlSupport<ELEMENT_VALUE_TYPE> controls) {
		if (controls != null) {
			final IDisplayFormat defaultDisplayFormat = controls.getDefaultDisplayFormat();
			if (defaultDisplayFormat != null && defaultDisplayFormat.getId() != null) {
				for (final IInputControlProvider<ELEMENT_VALUE_TYPE> controlProvider : controls.getControls()) {
					if (defaultDisplayFormat.getId().equals(controlProvider.getDisplayFormat().getId())) {
						return controlProvider;
					}
				}
			}
			else if (controls.getControls().size() > 0) {
				return controls.getControls().get(0);
			}
		}
		return null;
	}

	private IStringObjectConverter<ELEMENT_VALUE_TYPE> getStringObjectConverter(
		final IInputControlProvider<ELEMENT_VALUE_TYPE> defaultControl) {
		if (stringObjectConverter == null) {
			return defaultControl.getStringObjectConverter(valueRange);
		}
		else {
			return getStringObjectConverter();
		}
	}

	private IObjectLabelConverter<ELEMENT_VALUE_TYPE> getObjectLabelConverter(
		final IInputControlProvider<ELEMENT_VALUE_TYPE> defaultControl) {
		if (objectLabelConverter == null) {
			final IObjectLabelConverter<ELEMENT_VALUE_TYPE> defaultControlObjectLabelConverter = defaultControl.getObjectLabelConverter(valueRange);
			if (defaultControlObjectLabelConverter != null) {
				return defaultControlObjectLabelConverter;
			}
			else {
				return getObjectLabelConverter();
			}
		}
		else {
			return getObjectLabelConverter();
		}
	}

	private ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> getControlCreator(
		final IInputControlProvider<ELEMENT_VALUE_TYPE> defaultControl) {
		if (controlCreator == null) {
			return defaultControl.getControlCreator(
					getObjectLabelConverter(defaultControl),
					getStringObjectConverter(defaultControl),
					valueRange);
		}
		else {
			return controlCreator.getValue();
		}
	}

	private IFilterSupport<?> getFilterSupport() {
		if (filterSupport == null) {
			final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> contrCreator = getControlCreator();
			final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> filterCollectionContrCreator = getFilterCollectionControlCreator();
			if (contrCreator != null && filterCollectionContrCreator != null) {
				return CapUiToolkit.filterToolkit().filterSupport(
						propertyName,
						valueType,
						elementValueType,
						valueRange,
						cardinality,
						contrCreator,
						filterCollectionContrCreator);
			}
			else {
				return null;
			}
		}
		else {
			return filterSupport.getValue();
		}
	}

	@Override
	public IControlPanelProvider<ELEMENT_VALUE_TYPE> build() {
		return new ControlPanelProviderImpl<ELEMENT_VALUE_TYPE>(
			new DisplayFormatImpl(displayFormatId, displayFormatName, displayFormatDescription),
			getObjectLabelConverter(),
			getStringObjectConverter(),
			getFilterSupport(),
			getControlCreator(),
			getCollectionControlCreator());
	}
}
