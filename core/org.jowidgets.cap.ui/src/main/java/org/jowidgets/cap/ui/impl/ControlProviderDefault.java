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
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.convert.IConverter;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.api.widgets.blueprint.ICollectionInputFieldBluePrint;
import org.jowidgets.api.widgets.blueprint.IComboBoxBluePrint;
import org.jowidgets.api.widgets.blueprint.IComboBoxSelectionBluePrint;
import org.jowidgets.api.widgets.blueprint.IInputFieldBluePrint;
import org.jowidgets.api.widgets.blueprint.builder.IInputComponentSetupBuilder;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.common.api.bean.ILookUpValueRange;
import org.jowidgets.cap.common.api.bean.IStaticValueRange;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.control.IDisplayFormat;
import org.jowidgets.cap.ui.api.control.IInputControlProvider;
import org.jowidgets.cap.ui.api.control.IInputControlSupport;
import org.jowidgets.cap.ui.tools.validation.ValueRangeValidator;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.factory.ICustomWidgetFactory;

class ControlProviderDefault<ELEMENT_VALUE_TYPE> implements
		IInputControlProvider<ELEMENT_VALUE_TYPE>,
		IInputControlSupport<ELEMENT_VALUE_TYPE> {

	private final Class<? extends ELEMENT_VALUE_TYPE> elementValueType;
	private final IBluePrintFactory bpf;

	ControlProviderDefault(final Class<? extends ELEMENT_VALUE_TYPE> elementValueType) {
		this.elementValueType = elementValueType;
		this.bpf = Toolkit.getBluePrintFactory();
	}

	@Override
	public IDisplayFormat getDisplayFormat() {
		return CapUiToolkit.displayFormatFactory().getDefault();
	}

	@Override
	public IConverter<ELEMENT_VALUE_TYPE> getConverter(final IValueRange valueRange) {
		return Toolkit.getConverterProvider().getConverter(elementValueType);
	}

	@Override
	public ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> getControlCreator(
		final IConverter<ELEMENT_VALUE_TYPE> converter,
		final IValueRange valueRange) {
		if (valueRange instanceof IStaticValueRange) {
			return getControlCreator(converter, (IStaticValueRange) valueRange);
		}
		else {
			// TODO MG handle ILookUpValueRange
			return null;
		}
	}

	private ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> getControlCreator(
		final IConverter<ELEMENT_VALUE_TYPE> converter,
		final IStaticValueRange valueRange) {
		if (valueRange.getValues().isEmpty()) {
			return new ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>>() {
				@Override
				public IInputControl<ELEMENT_VALUE_TYPE> create(final ICustomWidgetFactory widgetFactory) {
					final IInputFieldBluePrint<ELEMENT_VALUE_TYPE> inputFieldBp = bpf.inputField(converter);
					addValueRangeValidator(inputFieldBp, valueRange);
					return widgetFactory.create(inputFieldBp);
				}
			};
		}
		else {
			return new ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>>() {
				@SuppressWarnings("unchecked")
				@Override
				public IInputControl<ELEMENT_VALUE_TYPE> create(final ICustomWidgetFactory widgetFactory) {
					if (valueRange.isOpen()) {
						final IComboBoxBluePrint<ELEMENT_VALUE_TYPE> comboBp = bpf.comboBox(converter);
						addValueRangeValidator(comboBp, valueRange);
						comboBp.setElements((List<ELEMENT_VALUE_TYPE>) valueRange.getValues());
						return widgetFactory.create(comboBp);
					}
					else {
						final IComboBoxSelectionBluePrint<ELEMENT_VALUE_TYPE> comboBp = bpf.comboBoxSelection(converter);
						addValueRangeValidator(comboBp, valueRange);
						comboBp.setElements((List<ELEMENT_VALUE_TYPE>) valueRange.getValues());
						comboBp.setLenient(true);
						return widgetFactory.create(comboBp);
					}
				}
			};
		}
	}

	@Override
	public ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> getCollectionControlCreator(
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> elementControlCreator,
		final IConverter<ELEMENT_VALUE_TYPE> converter,
		final IValueRange valueRange) {

		if (valueRange instanceof IStaticValueRange) {
			return getCollectionControlCreator(elementControlCreator, converter, (IStaticValueRange) valueRange);
		}
		else {
			// TODO MG handle ILookUpValueRange
			return null;
		}
	}

	private ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> getCollectionControlCreator(
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> elementControlCreator,
		final IConverter<ELEMENT_VALUE_TYPE> converter,
		final IStaticValueRange valueRange) {

		return new ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>>() {
			@Override
			public IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>> create(final ICustomWidgetFactory widgetFactory) {
				final ICollectionInputFieldBluePrint<ELEMENT_VALUE_TYPE> inputFieldBp = bpf.collectionInputField(converter);
				if (!valueRange.isOpen()) {
					inputFieldBp.setElementValidator(new ValueRangeValidator<ELEMENT_VALUE_TYPE>(valueRange));
				}
				if (elementControlCreator != null) {
					inputFieldBp.setCollectionInputDialogSetup(bpf.collectionInputDialog(elementControlCreator));
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
		return result;
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
