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

import org.jowidgets.api.convert.IObjectLabelConverter;
import org.jowidgets.api.convert.IStringObjectConverter;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.ui.api.control.IDisplayFormat;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;

final class ControlProviderImpl<ELEMENT_VALUE_TYPE> extends ControlProviderDefault<ELEMENT_VALUE_TYPE> {

	private final IDisplayFormat displayFormat;
	private final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverter;
	private final IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter;
	private final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator;
	private final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator;

	ControlProviderImpl(
		final Class<? extends ELEMENT_VALUE_TYPE> elementValueType,
		final IDisplayFormat displayFormat,
		final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverter,
		final IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter,
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator,
		final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator) {
		super(elementValueType);

		this.displayFormat = displayFormat;
		this.objectLabelConverter = objectLabelConverter;
		this.stringObjectConverter = stringObjectConverter;
		this.controlCreator = controlCreator;
		this.collectionControlCreator = collectionControlCreator;
	}

	@Override
	public IDisplayFormat getDisplayFormat() {
		if (displayFormat != null) {
			return displayFormat;
		}
		else {
			return super.getDisplayFormat();
		}
	}

	@Override
	public IObjectLabelConverter<ELEMENT_VALUE_TYPE> getObjectLabelConverter(final IValueRange valueRange) {
		if (objectLabelConverter != null) {
			return objectLabelConverter;
		}
		else {
			return super.getObjectLabelConverter(valueRange);
		}
	}

	@Override
	public IStringObjectConverter<ELEMENT_VALUE_TYPE> getStringObjectConverter(final IValueRange valueRange) {
		if (stringObjectConverter != null) {
			return stringObjectConverter;
		}
		else {
			return super.getStringObjectConverter(valueRange);
		}
	}

	@Override
	public ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> getControlCreator(
		final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverter,
		final IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter,
		final IValueRange valueRange) {
		if (controlCreator != null) {
			return controlCreator;
		}
		else {
			return super.getControlCreator(objectLabelConverter, stringObjectConverter, valueRange);
		}
	}

	@Override
	public ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> getCollectionControlCreator(
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> elementControlCreator,
		final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverter,
		final IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter,
		final IValueRange valueRange) {
		if (collectionControlCreator != null) {
			return collectionControlCreator;
		}
		else {
			return super.getCollectionControlCreator(
					elementControlCreator,
					objectLabelConverter,
					stringObjectConverter,
					valueRange);
		}
	}

	@Override
	public IDisplayFormat getDefaultDisplayFormat() {
		return getDisplayFormat();
	}

}
