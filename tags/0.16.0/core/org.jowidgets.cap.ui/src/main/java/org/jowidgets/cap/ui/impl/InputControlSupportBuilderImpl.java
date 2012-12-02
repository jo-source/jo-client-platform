/*
 * Copyright (c) 2012, grossmann
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

import org.jowidgets.cap.ui.api.control.IDisplayFormat;
import org.jowidgets.cap.ui.api.control.IInputControlProvider;
import org.jowidgets.cap.ui.api.control.IInputControlSupport;
import org.jowidgets.cap.ui.api.control.IInputControlSupportBuilder;
import org.jowidgets.util.Assert;

final class InputControlSupportBuilderImpl<ELEMENT_VALUE_TYPE> implements IInputControlSupportBuilder<ELEMENT_VALUE_TYPE> {

	private final List<IInputControlProvider<ELEMENT_VALUE_TYPE>> controls;

	private IDisplayFormat defaultDisplayFormat;

	InputControlSupportBuilderImpl() {
		this.controls = new LinkedList<IInputControlProvider<ELEMENT_VALUE_TYPE>>();
	}

	@Override
	public IInputControlSupportBuilder<ELEMENT_VALUE_TYPE> setDefaultDisplayFormat(final IDisplayFormat displayFormat) {
		this.defaultDisplayFormat = displayFormat;
		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public IInputControlSupportBuilder<ELEMENT_VALUE_TYPE> setControls(final Collection controls) {
		this.controls.clear();
		if (controls != null) {
			this.controls.addAll(controls);
		}
		return this;
	}

	@Override
	public IInputControlSupportBuilder<ELEMENT_VALUE_TYPE> addControl(final IInputControlProvider<ELEMENT_VALUE_TYPE> control) {
		Assert.paramNotNull(control, "control");
		this.controls.add(control);
		return null;
	}

	private IDisplayFormat getDefaultDisplayFormat() {
		if (defaultDisplayFormat == null) {
			return controls.iterator().next().getDisplayFormat();
		}
		else {
			return defaultDisplayFormat;
		}
	}

	@Override
	public IInputControlSupport<ELEMENT_VALUE_TYPE> build() {
		if (controls.size() == 0) {
			throw new IllegalStateException("The builder has no control provider added");
		}
		return new InputControlSupportImpl<ELEMENT_VALUE_TYPE>(getDefaultDisplayFormat(), controls);
	}

}
