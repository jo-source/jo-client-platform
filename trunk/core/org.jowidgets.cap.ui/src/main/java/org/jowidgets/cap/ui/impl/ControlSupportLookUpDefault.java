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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.bean.ILookUpValueRange;
import org.jowidgets.cap.common.api.lookup.ILookUpProperty;
import org.jowidgets.cap.ui.api.control.IDisplayFormat;
import org.jowidgets.cap.ui.api.control.IInputControlProvider;
import org.jowidgets.cap.ui.api.control.IInputControlSupport;
import org.jowidgets.util.Assert;

final class ControlSupportLookUpDefault implements IInputControlSupport<Object> {

	private final IDisplayFormat defaultDisplayFormat;
	private final List<IInputControlProvider<Object>> controls;
	private final List<IInputControlProvider<Object>> controlsView;

	ControlSupportLookUpDefault(final ILookUpValueRange valueRange) {
		Assert.paramNotNull(valueRange, "valueRange");

		final ILookUpProperty defaultProperty = getDefaultProperty(valueRange);
		if (defaultProperty == null) {
			throw new IllegalArgumentException("The value range has no convertable property");
		}
		else {
			this.defaultDisplayFormat = LookUpDisplayFormatFactory.create(defaultProperty);
		}

		this.controls = new LinkedList<IInputControlProvider<Object>>();
		this.controlsView = Collections.unmodifiableList(controls);
		for (final ILookUpProperty lookUpProperty : valueRange.getValueProperties()) {
			if (hasConverter(lookUpProperty)) {
				controls.add(new ControlProviderLookUpDefault<Object>(valueRange.getLookUpId(), lookUpProperty));
			}
		}
	}

	/**
	 * Gets the default property if the default property has a generic converter,
	 * else the first property with a generic converter will be returned
	 * 
	 * @param valueRange The value range to get the default property for
	 * 
	 * @return The default property or null, if there is no default property
	 */
	private static ILookUpProperty getDefaultProperty(final ILookUpValueRange valueRange) {
		final String defaultPropertyName = valueRange.getDefaultValuePropertyName();
		Assert.paramNotNull(defaultPropertyName, "valueRange.getDefaultPropertyName()");
		ILookUpProperty result = null;
		for (final ILookUpProperty lookUpProperty : valueRange.getValueProperties()) {
			final boolean hasConverter = hasConverter(lookUpProperty);
			if (defaultPropertyName.equals(lookUpProperty.getName()) && hasConverter) {
				return lookUpProperty;
			}
			else if (result == null && hasConverter) {
				result = lookUpProperty;
			}
		}
		return result;
	}

	private static boolean hasConverter(final ILookUpProperty lookUpProperty) {
		return Toolkit.getConverterProvider().getConverter(lookUpProperty.getValueType()) != null;
	}

	@Override
	public IDisplayFormat getDefaultDisplayFormat() {
		return defaultDisplayFormat;
	}

	@Override
	public List<IInputControlProvider<Object>> getControls() {
		return controlsView;
	}

}
