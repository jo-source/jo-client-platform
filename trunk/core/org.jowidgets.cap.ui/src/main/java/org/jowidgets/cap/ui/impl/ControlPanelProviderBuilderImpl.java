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
import java.util.List;

import org.jowidgets.api.convert.IConverter;
import org.jowidgets.api.convert.IConverterProvider;
import org.jowidgets.api.convert.IObjectLabelConverter;
import org.jowidgets.api.convert.IObjectStringConverter;
import org.jowidgets.api.convert.IStringObjectConverter;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.api.widgets.blueprint.ICheckBoxBluePrint;
import org.jowidgets.api.widgets.blueprint.ICollectionInputFieldBluePrint;
import org.jowidgets.api.widgets.blueprint.IComboBoxBluePrint;
import org.jowidgets.api.widgets.blueprint.IComboBoxSelectionBluePrint;
import org.jowidgets.api.widgets.blueprint.IInputFieldBluePrint;
import org.jowidgets.api.widgets.blueprint.builder.IInputComponentSetupBuilder;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.DisplayFormat;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProviderBuilder;
import org.jowidgets.cap.ui.api.filter.IFilterSupport;
import org.jowidgets.cap.ui.tools.validation.ValueRangeValidator;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.factory.ICustomWidgetFactory;
import org.jowidgets.tools.converter.AbstractObjectLabelConverter;
import org.jowidgets.tools.converter.Converter;
import org.jowidgets.util.Assert;

final class ControlPanelProviderBuilderImpl<ELEMENT_VALUE_TYPE> implements IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> {

	private static final String DEFAULT_DISPLAY_FORMAT_ID = DisplayFormat.DEFAULT.getId();
	private static final String DEFAULT_DISPLAY_NAME = DisplayFormat.DEFAULT.getName();

	private final IValueRange valueRange;

	@SuppressWarnings("unused")
	private Class<?> valueType;
	private Class<? extends ELEMENT_VALUE_TYPE> elementValueType;

	private String displayFormatId;
	private String displayFormatName;
	private String displayFormatDescription;
	private IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverter;
	private IObjectStringConverter<ELEMENT_VALUE_TYPE> objectStringConverter;
	private IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter;
	private IFilterSupport<?> filterSupport;
	private ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator;
	private ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator;

	ControlPanelProviderBuilderImpl(final Class<? extends ELEMENT_VALUE_TYPE> elementValueType, final IValueRange valueRange) {
		this(valueRange);
		Assert.paramNotNull(elementValueType, "elementValueType");
		if (Collection.class.isAssignableFrom(elementValueType)) {
			throw new IllegalArgumentException("The parameter 'elementValueType' must not be a 'Collection'");
		}
		this.valueType = elementValueType;
		this.elementValueType = elementValueType;
	}

	ControlPanelProviderBuilderImpl(
		final Class<?> valueType,
		final Class<? extends ELEMENT_VALUE_TYPE> elementValueType,
		final IValueRange valueRange) {
		this(valueRange);
		Assert.paramNotNull(valueType, "valueType");
		Assert.paramNotNull(elementValueType, "elementValueType");
		this.valueType = valueType;
		this.elementValueType = elementValueType;
	}

	private ControlPanelProviderBuilderImpl(final IValueRange valueRange) {
		super();
		Assert.paramNotNull(valueRange, "valueRange");
		this.valueRange = valueRange;
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
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setDisplayFormat(final DisplayFormat displayFormat) {
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
		this.objectLabelConverter = objectLabelConverter;
		return this;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setObjectLabelConverter(
		final IObjectStringConverter objectStringConverter) {
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

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setConverter(final IConverter converter) {
		this.objectLabelConverter = null;
		this.objectStringConverter = converter;
		this.stringObjectConverter = converter;
		return this;
	}

	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setFilterSupport(final IFilterSupport<?> filterSupport) {
		this.filterSupport = filterSupport;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setControlCreator(
		final ICustomWidgetCreator<? extends IInputControl<? extends ELEMENT_VALUE_TYPE>> controlCreator) {
		this.controlCreator = (ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>>) controlCreator;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IControlPanelProviderBuilder<ELEMENT_VALUE_TYPE> setCollectionControlCreator(
		final ICustomWidgetCreator<? extends IInputControl<? extends Collection<? extends ELEMENT_VALUE_TYPE>>> collectionControlCreator) {
		this.collectionControlCreator = (ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>>) collectionControlCreator;
		return this;
	}

	private IObjectStringConverter<ELEMENT_VALUE_TYPE> getObjectStringConverter() {
		if (objectStringConverter == null) {
			objectStringConverter = Toolkit.getConverterProvider().getObjectStringConverter(elementValueType);
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

	@SuppressWarnings({"rawtypes", "unchecked"})
	private ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> getControlCreator() {
		if (controlCreator == null) {
			final IBluePrintFactory bpf = Toolkit.getBluePrintFactory();
			final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConv = getObjectLabelConverter();
			final IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConv = getStringObjectConverter();

			if (elementValueType.equals(boolean.class)) {
				controlCreator = new ICustomWidgetCreator() {
					@Override
					public IInputControl create(final ICustomWidgetFactory widgetFactory) {
						final ICheckBoxBluePrint checkBoxBp = bpf.checkBox();
						addValueRangeValidator(checkBoxBp);
						return widgetFactory.create(checkBoxBp);
					}
				};
			}
			else if (elementValueType.equals(Boolean.class)) {
				controlCreator = new ICustomWidgetCreator() {
					@Override
					public IInputControl create(final ICustomWidgetFactory widgetFactory) {
						final IComboBoxSelectionBluePrint<Boolean> cmbBp = bpf.comboBoxSelection(Toolkit.getConverterProvider().boolYesNoLong());
						cmbBp.setElements(null, Boolean.TRUE, Boolean.FALSE);
						addValueRangeValidator(cmbBp);
						return widgetFactory.create(cmbBp);
					}
				};
			}
			else if (objectLabelConv != null && stringObjectConv != null) {
				if (valueRange.getValues().isEmpty()) {
					controlCreator = new ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>>() {
						@Override
						public IInputControl<ELEMENT_VALUE_TYPE> create(final ICustomWidgetFactory widgetFactory) {
							final IConverter<ELEMENT_VALUE_TYPE> converter = new Converter<ELEMENT_VALUE_TYPE>(
								objectLabelConv,
								stringObjectConv);
							final IInputFieldBluePrint<ELEMENT_VALUE_TYPE> inputFieldBp = bpf.inputField(converter);
							addValueRangeValidator(inputFieldBp);
							return widgetFactory.create(inputFieldBp);
						}
					};
				}
				else {
					controlCreator = new ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>>() {
						@Override
						public IInputControl<ELEMENT_VALUE_TYPE> create(final ICustomWidgetFactory widgetFactory) {
							final IConverter<ELEMENT_VALUE_TYPE> converter = new Converter<ELEMENT_VALUE_TYPE>(
								objectLabelConv,
								stringObjectConv);
							if (valueRange.isOpen()) {
								final IComboBoxBluePrint<ELEMENT_VALUE_TYPE> comboBp = bpf.comboBox(converter);
								addValueRangeValidator(comboBp);
								comboBp.setElements((List<ELEMENT_VALUE_TYPE>) valueRange.getValues());
								return widgetFactory.create(comboBp);
							}
							else {
								final IComboBoxSelectionBluePrint<ELEMENT_VALUE_TYPE> comboBp = bpf.comboBoxSelection(converter);
								addValueRangeValidator(comboBp);
								comboBp.setElements((List<ELEMENT_VALUE_TYPE>) valueRange.getValues());
								comboBp.setLenient(true);
								return widgetFactory.create(comboBp);
							}
						}
					};
				}
			}
		}
		return controlCreator;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void addValueRangeValidator(final IInputComponentSetupBuilder setupBuilder) {
		if (!valueRange.isOpen()) {
			setupBuilder.setValidator(new ValueRangeValidator(valueRange));
		}
	}

	private ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> getCollectionControlCreator() {
		if (collectionControlCreator == null) {
			final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConv = getObjectLabelConverter();
			final IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConv = getStringObjectConverter();
			if (objectLabelConv != null && stringObjectConv != null) {
				final IConverter<ELEMENT_VALUE_TYPE> converter = new Converter<ELEMENT_VALUE_TYPE>(
					objectLabelConv,
					stringObjectConv);

				final IBluePrintFactory bpf = Toolkit.getBluePrintFactory();
				collectionControlCreator = new ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>>() {
					@Override
					public IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>> create(final ICustomWidgetFactory widgetFactory) {
						final ICollectionInputFieldBluePrint<ELEMENT_VALUE_TYPE> inputFieldBp = bpf.collectionInputField(converter);
						if (!valueRange.isOpen()) {
							inputFieldBp.setElementValidator(new ValueRangeValidator<ELEMENT_VALUE_TYPE>(valueRange));
						}
						final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> elementControlCreator = getControlCreator();
						if (elementControlCreator != null) {
							inputFieldBp.setCollectionInputDialogSetup(bpf.collectionInputDialog(elementControlCreator));
						}
						return widgetFactory.create(inputFieldBp);
					}
				};
			}
		}
		return collectionControlCreator;
	}

	private IFilterSupport<?> getFilterSupport() {
		if (filterSupport == null) {
			filterSupport = CapUiToolkit.filterToolkit().filterSupport(elementValueType);
		}
		return filterSupport;
	}

	@Override
	public IControlPanelProvider<ELEMENT_VALUE_TYPE> build() {
		return new ControlPanelProviderImpl<ELEMENT_VALUE_TYPE>(
			displayFormatId,
			displayFormatName,
			displayFormatDescription,
			getObjectLabelConverter(),
			getStringObjectConverter(),
			getFilterSupport(),
			getControlCreator(),
			getCollectionControlCreator());
	}

}
