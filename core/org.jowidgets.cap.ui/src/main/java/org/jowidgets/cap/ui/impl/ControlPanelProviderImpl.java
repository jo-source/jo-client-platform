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
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.control.IDisplayFormat;
import org.jowidgets.cap.ui.api.filter.IFilterSupport;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.util.Assert;

final class ControlPanelProviderImpl<ELEMENT_VALUE_TYPE> implements IControlPanelProvider<ELEMENT_VALUE_TYPE> {

	private final IDisplayFormat displayFormat;
	private final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverter;
	private final IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter;
	private final IFilterSupport<?> filterSupport;
	private final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator;
	private final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator;

	ControlPanelProviderImpl(
		final IDisplayFormat displayFormat,
		final IObjectLabelConverter<ELEMENT_VALUE_TYPE> objectLabelConverter,
		final IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter,
		final IFilterSupport<?> filterSupport,
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator,
		final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator) {

		Assert.paramNotNull(displayFormat, "displayFormat");
		Assert.paramNotNull(objectLabelConverter, "objectLabelConverter");

		this.displayFormat = displayFormat;
		this.objectLabelConverter = objectLabelConverter;
		this.stringObjectConverter = stringObjectConverter;
		this.filterSupport = filterSupport;
		this.controlCreator = controlCreator;
		this.collectionControlCreator = collectionControlCreator;
	}

	@Override
	public IDisplayFormat getDisplayFormat() {
		return displayFormat;
	}

	@Override
	public IObjectLabelConverter<ELEMENT_VALUE_TYPE> getObjectLabelConverter() {
		return objectLabelConverter;
	}

	@Override
	public IStringObjectConverter<ELEMENT_VALUE_TYPE> getStringObjectConverter() {
		return stringObjectConverter;
	}

	@Override
	public ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> getControlCreator() {
		return controlCreator;
	}

	@Override
	public IFilterSupport<?> getFilterSupport() {
		return filterSupport;
	}

	@Override
	public ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> getCollectionControlCreator() {
		return collectionControlCreator;
	}

}
