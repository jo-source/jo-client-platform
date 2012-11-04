/*
 * Copyright (c) 2010, grossmann
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

package org.jowidgets.cap.ui.impl.widgets;

import java.util.Collection;

import org.jowidgets.api.convert.IConverter;
import org.jowidgets.api.convert.IObjectStringConverter;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.api.widgets.blueprint.ICollectionInputFieldBluePrint;
import org.jowidgets.cap.ui.api.widgets.ILookUpCollectionInputFieldBluePrint;
import org.jowidgets.common.widgets.factory.IWidgetFactory;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.Assert;

final class LookUpCollectionInputFieldFactory implements
		IWidgetFactory<IInputControl<Collection<Object>>, ILookUpCollectionInputFieldBluePrint<Object>> {

	@SuppressWarnings("unchecked")
	@Override
	public IInputControl<Collection<Object>> create(
		final Object parentUiReference,
		final ILookUpCollectionInputFieldBluePrint<Object> descriptor) {
		Assert.paramNotNull(descriptor.getConverter(), "descriptor.getConverter()");

		final Object converter = descriptor.getConverter();
		final ICollectionInputFieldBluePrint<Object> bluePrint;
		if (converter instanceof IConverter<?>) {
			bluePrint = BPF.collectionInputField((IConverter<Object>) converter);
		}
		else if (converter instanceof IObjectStringConverter<?>) {
			bluePrint = BPF.collectionInputField((IObjectStringConverter<Object>) converter);
		}
		else {
			throw new IllegalArgumentException("Converter type '" + converter.getClass().getName() + "' is not supoorted");
		}

		bluePrint.setSetup(descriptor);
		final IInputControl<Collection<Object>> inputControl = Toolkit.getWidgetFactory().create(parentUiReference, bluePrint);
		return new LookUpCollectionInputFieldImpl(inputControl, descriptor);
	}
}
