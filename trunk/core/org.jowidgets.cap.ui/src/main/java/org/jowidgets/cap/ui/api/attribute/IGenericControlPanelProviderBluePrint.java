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

package org.jowidgets.cap.ui.api.attribute;

import java.util.Collection;

import org.jowidgets.api.convert.IConverter;
import org.jowidgets.api.convert.IObjectLabelConverter;
import org.jowidgets.api.convert.IObjectStringConverter;
import org.jowidgets.api.convert.IStringObjectConverter;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.cap.ui.api.filter.IFilterSupport;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;

public interface IGenericControlPanelProviderBluePrint<ELEMENT_VALUE_TYPE, INSTANCE_TYPE> {

	INSTANCE_TYPE setObjectLabelConverter(IObjectLabelConverter<? extends ELEMENT_VALUE_TYPE> objectLabelConverter);

	INSTANCE_TYPE setObjectLabelConverter(IObjectStringConverter<? extends ELEMENT_VALUE_TYPE> objectStringConverter);

	INSTANCE_TYPE setStringObjectConverter(IStringObjectConverter<ELEMENT_VALUE_TYPE> stringObjectConverter);

	INSTANCE_TYPE setConverter(final IConverter<? extends ELEMENT_VALUE_TYPE> converter);

	INSTANCE_TYPE setFilterSupport(IFilterSupport<?> filterSupport);

	INSTANCE_TYPE setFilterSupport(IConverter<ELEMENT_VALUE_TYPE> elementValueConverter);

	INSTANCE_TYPE setControlCreator(ICustomWidgetCreator<? extends IInputControl<? extends ELEMENT_VALUE_TYPE>> controlCreator);

	INSTANCE_TYPE setCollectionControlCreator(
		ICustomWidgetCreator<? extends IInputControl<? extends Collection<? extends ELEMENT_VALUE_TYPE>>> collectionControlCreator);

	INSTANCE_TYPE setFilterCollectionControlCreator(
		ICustomWidgetCreator<? extends IInputControl<? extends Collection<? extends ELEMENT_VALUE_TYPE>>> collectionControlCreator);

}
