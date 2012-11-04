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
import org.jowidgets.api.convert.IStringObjectConverter;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.api.widgets.blueprint.builder.IInputComponentSetupBuilder;
import org.jowidgets.cap.common.api.bean.IStaticValueRange;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.common.api.lookup.ILookUpProperty;
import org.jowidgets.cap.common.api.lookup.ILookUpValueRange;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.control.IDisplayFormat;
import org.jowidgets.cap.ui.api.control.IInputControlProvider;
import org.jowidgets.cap.ui.api.widgets.ILookUpCollectionInputFieldBluePrint;
import org.jowidgets.cap.ui.api.widgets.ILookUpComboBoxSelectionBluePrint;
import org.jowidgets.cap.ui.tools.validation.ValueRangeValidator;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.factory.ICustomWidgetFactory;
import org.jowidgets.tools.converter.Converter;
import org.jowidgets.tools.converter.ObjectStringObjectLabelConverterAdapter;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.Assert;

class ControlProviderLookUpDefault<ELEMENT_VALUE_TYPE> implements IInputControlProvider<ELEMENT_VALUE_TYPE> {

	private final Object lookUpId;
	private final ILookUpProperty lookUpProperty;
	private final IDisplayFormat displayFormat;

	ControlProviderLookUpDefault(final Object lookUpId, final ILookUpProperty lookUpProperty) {
		Assert.paramNotNull(lookUpId, "lookUpId");
		Assert.paramNotNull(lookUpProperty, "lookUpProperty");

		this.lookUpId = lookUpId;
		this.lookUpProperty = lookUpProperty;
		this.displayFormat = LookUpDisplayFormatFactory.create(lookUpProperty);
	}

	@Override
	public IDisplayFormat getDisplayFormat() {
		return displayFormat;
	}

	@Override
	public IObjectLabelConverter<ELEMENT_VALUE_TYPE> getObjectLabelConverter(final IValueRange valueRange) {
		Assert.paramHasType(valueRange, ILookUpValueRange.class, "valueRange");
		return new ObjectStringObjectLabelConverterAdapter<ELEMENT_VALUE_TYPE>(getConverter(valueRange));
	}

	@Override
	public IStringObjectConverter<ELEMENT_VALUE_TYPE> getStringObjectConverter(final IValueRange valueRange) {
		Assert.paramHasType(valueRange, ILookUpValueRange.class, "valueRange");
		return getConverter(valueRange);
	}

	private IConverter<ELEMENT_VALUE_TYPE> getConverter(final IValueRange valueRange) {
		return CapUiToolkit.converterFactory().lookUpConverter(lookUpId, lookUpProperty);
	}

	@Override
	public ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> getControlCreator(
		final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverter,
		final IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter,
		final IValueRange valueRange) {
		Assert.paramNotNull(objectLabelConverter, "objectLabelConverter");
		Assert.paramNotNull(stringObjectConverter, "stringObjectConverter");
		Assert.paramHasType(valueRange, ILookUpValueRange.class, "valueRange");

		final IConverter<ELEMENT_VALUE_TYPE> converter = new Converter<ELEMENT_VALUE_TYPE>(
			objectLabelConverter,
			stringObjectConverter);

		return new ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>>() {
			@Override
			public IInputControl<ELEMENT_VALUE_TYPE> create(final ICustomWidgetFactory widgetFactory) {
				final ILookUpComboBoxSelectionBluePrint<ELEMENT_VALUE_TYPE> bluePrint;

				bluePrint = CapUiToolkit.bluePrintFactory().lookUpComboBox(lookUpId, converter);
				addValueRangeValidator(bluePrint, valueRange);
				return widgetFactory.create(bluePrint);
			}
		};
	}

	@Override
	public ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> getCollectionControlCreator(
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> elementControlCreator,
		final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverter,
		final IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter,
		final IValueRange valueRange) {
		Assert.paramNotNull(elementControlCreator, "elementControlCreator");
		Assert.paramNotNull(objectLabelConverter, "objectLabelConverter");
		Assert.paramNotNull(stringObjectConverter, "stringObjectConverter");
		Assert.paramHasType(valueRange, ILookUpValueRange.class, "valueRange");

		final IConverter<ELEMENT_VALUE_TYPE> converter = new Converter<ELEMENT_VALUE_TYPE>(
			objectLabelConverter,
			stringObjectConverter);

		return new ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>>() {
			@Override
			public IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>> create(final ICustomWidgetFactory widgetFactory) {
				final ILookUpCollectionInputFieldBluePrint<ELEMENT_VALUE_TYPE> bluePrint;
				bluePrint = CapUiToolkit.bluePrintFactory().lookUpCollectionInputField(lookUpId, converter);

				bluePrint.setElementValidator(new ValueRangeValidator<ELEMENT_VALUE_TYPE>(valueRange));
				if (elementControlCreator != null) {
					bluePrint.setCollectionInputDialogSetup(BPF.collectionInputDialog(elementControlCreator));
				}
				return widgetFactory.create(bluePrint);
			}
		};
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
