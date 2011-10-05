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
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.api.widgets.blueprint.ICollectionInputFieldBluePrint;
import org.jowidgets.api.widgets.blueprint.builder.IInputComponentSetupBuilder;
import org.jowidgets.cap.common.api.bean.IStaticValueRange;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.common.api.lookup.ILookUpProperty;
import org.jowidgets.cap.common.api.lookup.ILookUpValueRange;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.control.IDisplayFormat;
import org.jowidgets.cap.ui.api.control.IInputControlProvider;
import org.jowidgets.cap.ui.api.widgets.ILookUpComboBoxSelectionBluePrint;
import org.jowidgets.cap.ui.tools.validation.ValueRangeValidator;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.factory.ICustomWidgetFactory;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.Assert;

class ControlProviderLookUpDefault<ELEMENT_VALUE_TYPE> implements IInputControlProvider<ELEMENT_VALUE_TYPE> {

	private final Object lookUpId;
	private final ILookUpProperty lookUpProperty;

	ControlProviderLookUpDefault(final Object lookUpId, final ILookUpProperty lookUpProperty) {
		Assert.paramNotNull(lookUpId, "lookUpId");
		Assert.paramNotNull(lookUpProperty, "lookUpProperty");

		this.lookUpId = lookUpId;
		this.lookUpProperty = lookUpProperty;
	}

	@Override
	public IDisplayFormat getDisplayFormat() {
		return CapUiToolkit.displayFormatFactory().getDefault();
	}

	@Override
	public IConverter<ELEMENT_VALUE_TYPE> getConverter(final IValueRange valueRange) {
		Assert.paramHasType(valueRange, ILookUpValueRange.class, "valueRange");
		return CapUiToolkit.converterFactory().lookUpConverter(lookUpId, lookUpProperty);
	}

	@Override
	public ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> getControlCreator(
		final IConverter<ELEMENT_VALUE_TYPE> converter,
		final IValueRange valueRange) {
		Assert.paramNotNull(converter, "converter");
		Assert.paramHasType(valueRange, ILookUpValueRange.class, "valueRange");

		return new ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>>() {
			@Override
			public IInputControl<ELEMENT_VALUE_TYPE> create(final ICustomWidgetFactory widgetFactory) {
				final ILookUpComboBoxSelectionBluePrint<ELEMENT_VALUE_TYPE> lookUpComboBox;
				lookUpComboBox = CapUiToolkit.bluePrintFactory().lookUpComboBox(lookUpId, lookUpProperty);
				lookUpComboBox.setObjectStringConverter(converter);
				addValueRangeValidator(lookUpComboBox, valueRange);
				return widgetFactory.create(lookUpComboBox);
			}
		};

	}

	@Override
	public ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> getCollectionControlCreator(
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> elementControlCreator,
		final IConverter<ELEMENT_VALUE_TYPE> converter,
		final IValueRange valueRange) {
		Assert.paramNotNull(elementControlCreator, "elementControlCreator");
		Assert.paramNotNull(converter, "converter");
		Assert.paramHasType(valueRange, ILookUpValueRange.class, "valueRange");

		//TODO MG use a special look up input field that changes the value when lookup changes
		return new ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>>() {
			@Override
			public IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>> create(final ICustomWidgetFactory widgetFactory) {
				final ICollectionInputFieldBluePrint<ELEMENT_VALUE_TYPE> inputFieldBp = BPF.collectionInputField(converter);
				inputFieldBp.setElementValidator(new ValueRangeValidator<ELEMENT_VALUE_TYPE>(valueRange));
				if (elementControlCreator != null) {
					inputFieldBp.setCollectionInputDialogSetup(BPF.collectionInputDialog(elementControlCreator));
				}
				return widgetFactory.create(inputFieldBp);
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
