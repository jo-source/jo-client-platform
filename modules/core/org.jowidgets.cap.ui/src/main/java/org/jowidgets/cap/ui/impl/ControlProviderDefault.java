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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.convert.IObjectLabelConverter;
import org.jowidgets.api.convert.IObjectStringConverter;
import org.jowidgets.api.convert.IStringObjectConverter;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.api.widgets.blueprint.ICollectionInputFieldBluePrint;
import org.jowidgets.api.widgets.blueprint.IComboBoxBluePrint;
import org.jowidgets.api.widgets.blueprint.IComboBoxSelectionBluePrint;
import org.jowidgets.api.widgets.blueprint.IInputFieldBluePrint;
import org.jowidgets.api.widgets.blueprint.builder.IInputComponentSetupBuilder;
import org.jowidgets.cap.common.api.bean.IStaticValueRange;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.common.api.lookup.ILookUpValueRange;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.control.IDisplayFormat;
import org.jowidgets.cap.ui.api.control.IInputControlProvider;
import org.jowidgets.cap.ui.api.control.IInputControlSupport;
import org.jowidgets.cap.ui.tools.validation.ValueRangeValidator;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.factory.ICustomWidgetFactory;
import org.jowidgets.tools.converter.Converter;
import org.jowidgets.tools.converter.ObjectStringObjectLabelConverterAdapter;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.Assert;

class ControlProviderDefault<ELEMENT_VALUE_TYPE> implements
		IInputControlProvider<ELEMENT_VALUE_TYPE>,
		IInputControlSupport<ELEMENT_VALUE_TYPE> {

	private final Class<? extends ELEMENT_VALUE_TYPE> elementValueType;

	ControlProviderDefault(final Class<? extends ELEMENT_VALUE_TYPE> elementValueType) {
		this.elementValueType = elementValueType;
	}

	@Override
	public IDisplayFormat getDisplayFormat() {
		return CapUiToolkit.displayFormatFactory().getDefault();
	}

	@Override
	public IObjectLabelConverter<ELEMENT_VALUE_TYPE> getObjectLabelConverter(final IValueRange valueRange) {
		return Toolkit.getConverterProvider().getObjectLabelConverter(elementValueType);
	}

	@Override
	public IStringObjectConverter<ELEMENT_VALUE_TYPE> getStringObjectConverter(final IValueRange valueRange) {
		return Toolkit.getConverterProvider().getStringObjectConverter(elementValueType);
	}

	@Override
	public ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> getControlCreator(
		final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverter,
		final IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter,
		final IValueRange valueRange) {
		Assert.paramNotNull(valueRange, "valueRange");
		if (valueRange instanceof IStaticValueRange) {
			return getControlCreator(objectLabelConverter, stringObjectConverter, (IStaticValueRange) valueRange);
		}
		else if (valueRange instanceof ILookUpValueRange) {
			return getControlCreator(objectLabelConverter, stringObjectConverter, (ILookUpValueRange) valueRange);
		}
		else {
			throw new IllegalArgumentException("Value range of type '" + valueRange.getClass().getName() + "' is not supoorted");
		}
	}

	private ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> getControlCreator(
		final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverter,
		final IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter,
		final IStaticValueRange valueRange) {

		final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverterNotNull = getObjectLabelConverterNotNull(objectLabelConverter);

		if (valueRange.getValues().isEmpty()) {
			final IInputFieldBluePrint<ELEMENT_VALUE_TYPE> inputFieldBp;
			if (stringObjectConverter != null) {
				inputFieldBp = BPF.inputField(new Converter<ELEMENT_VALUE_TYPE>(
					objectLabelConverterNotNull,
					stringObjectConverter));
			}
			else {
				inputFieldBp = BPF.inputField(objectLabelConverterNotNull);
			}
			addValueRangeValidator(inputFieldBp, valueRange);
			return new ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>>() {
				@Override
				public IInputControl<ELEMENT_VALUE_TYPE> create(final ICustomWidgetFactory widgetFactory) {
					return widgetFactory.create(inputFieldBp);
				}
			};
		}
		else {
			return new ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>>() {
				@SuppressWarnings("unchecked")
				@Override
				public IInputControl<ELEMENT_VALUE_TYPE> create(final ICustomWidgetFactory widgetFactory) {
					if (valueRange.isOpen() && stringObjectConverter != null) {
						final Converter<ELEMENT_VALUE_TYPE> converter = new Converter<ELEMENT_VALUE_TYPE>(
							objectLabelConverterNotNull,
							stringObjectConverter);
						final IComboBoxBluePrint<ELEMENT_VALUE_TYPE> comboBp = BPF.comboBox(converter);
						addValueRangeValidator(comboBp, valueRange);
						comboBp.setElements((List<ELEMENT_VALUE_TYPE>) valueRange.getValues());
						return widgetFactory.create(comboBp);
					}
					else {
						final IComboBoxSelectionBluePrint<ELEMENT_VALUE_TYPE> comboBp = BPF.comboBoxSelection(objectLabelConverterNotNull);
						addValueRangeValidator(comboBp, valueRange);
						comboBp.setElements((List<ELEMENT_VALUE_TYPE>) valueRange.getValues());
						comboBp.setLenient(true);
						return widgetFactory.create(comboBp);
					}
				}
			};
		}
	}

	private ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> getControlCreator(
		final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverter,
		final IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter,
		final ILookUpValueRange lookUpValueRange) {
		final IInputControlSupport<ELEMENT_VALUE_TYPE> controls;
		controls = CapUiToolkit.inputControlRegistry().getControls(lookUpValueRange);
		final IInputControlProvider<ELEMENT_VALUE_TYPE> defaultControl = getDefaultControl(controls);
		if (defaultControl != null) {
			final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverterNotNull = getObjectLabelConverterNotNull(objectLabelConverter);
			return defaultControl.getControlCreator(objectLabelConverterNotNull, stringObjectConverter, lookUpValueRange);
		}
		else {
			return null;
		}
	}

	@Override
	public ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> getCollectionControlCreator(
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> elementControlCreator,
		final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverter,
		final IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter,
		final IValueRange valueRange) {

		final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverterNotNull = getObjectLabelConverterNotNull(objectLabelConverter);

		final ICollectionInputFieldBluePrint<ELEMENT_VALUE_TYPE> inputFieldBp;
		if (stringObjectConverter != null) {
			inputFieldBp = BPF.collectionInputField(new Converter<ELEMENT_VALUE_TYPE>(
				objectLabelConverterNotNull,
				stringObjectConverter));
		}
		else {
			inputFieldBp = BPF.collectionInputField(objectLabelConverterNotNull);
		}

		return new ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>>() {
			@Override
			public IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>> create(final ICustomWidgetFactory widgetFactory) {
				if (valueRange instanceof IStaticValueRange && !((IStaticValueRange) valueRange).isOpen()) {
					inputFieldBp.setElementValidator(new ValueRangeValidator<ELEMENT_VALUE_TYPE>(valueRange));
				}
				if (elementControlCreator != null) {
					inputFieldBp.setCollectionInputDialogSetup(BPF.collectionInputDialog(elementControlCreator));
				}
				if (inputFieldBp.getSeparator() == null
					&& (Double.class.isAssignableFrom(elementValueType) || Float.class.isAssignableFrom(elementValueType))) {
					inputFieldBp.setSeparator(Character.valueOf(';'));
				}
				return widgetFactory.create(inputFieldBp);
			}
		};
	}

	@Override
	public IDisplayFormat getDefaultDisplayFormat() {
		return CapUiToolkit.displayFormatFactory().getDefault();
	}

	@Override
	public List<IInputControlProvider<ELEMENT_VALUE_TYPE>> getControls() {
		final List<IInputControlProvider<ELEMENT_VALUE_TYPE>> result = new LinkedList<IInputControlProvider<ELEMENT_VALUE_TYPE>>();
		result.add(this);
		return Collections.unmodifiableList(result);
	}

	private IObjectLabelConverter<ELEMENT_VALUE_TYPE> getObjectLabelConverterNotNull(
		final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverter) {
		if (objectLabelConverter != null) {
			return objectLabelConverter;
		}
		else {
			final IObjectStringConverter<ELEMENT_VALUE_TYPE> objectStringConverter = Toolkit.getConverterProvider().toStringConverter();
			return new ObjectStringObjectLabelConverterAdapter<ELEMENT_VALUE_TYPE>(objectStringConverter);
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

	void addValueRangeValidator(
		final IInputComponentSetupBuilder<?, ELEMENT_VALUE_TYPE> setupBuilder,
		final IValueRange valueRange) {
		if ((valueRange instanceof IStaticValueRange && !((IStaticValueRange) valueRange).isOpen())
			|| valueRange instanceof ILookUpValueRange) {
			setupBuilder.setValidator(new ValueRangeValidator<ELEMENT_VALUE_TYPE>(valueRange));
		}
	}

}
