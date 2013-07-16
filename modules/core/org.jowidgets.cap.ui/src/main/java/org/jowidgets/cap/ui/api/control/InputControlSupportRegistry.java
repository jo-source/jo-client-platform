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

package org.jowidgets.cap.ui.api.control;

import org.jowidgets.cap.common.api.lookup.ILookUpValueRange;
import org.jowidgets.cap.ui.api.CapUiToolkit;

public final class InputControlSupportRegistry {

	private InputControlSupportRegistry() {}

	public static IInputControlSupportRegistry getInstance() {
		return CapUiToolkit.inputControlRegistry();
	}

	public static <ELEMENT_VALUE_TYPE> IInputControlSupport<ELEMENT_VALUE_TYPE> getControls(
		final Class<? extends ELEMENT_VALUE_TYPE> type) {
		return getInstance().getControls(type);
	}

	public static <ELEMENT_VALUE_TYPE> IInputControlSupport<ELEMENT_VALUE_TYPE> getControls(final ILookUpValueRange valueRange) {
		return getInstance().getControls(valueRange);
	}

	public static void setControls(final Object lookUpId, final IInputControlSupport<?> controlSupport) {
		getInstance().setControls(lookUpId, controlSupport);
	}

	public static <ELEMENT_VALUE_TYPE> void setControls(
		final Class<? extends ELEMENT_VALUE_TYPE> type,
		final IInputControlSupport<ELEMENT_VALUE_TYPE> controlSupport) {
		getInstance().setControls(type, controlSupport);
	}

	public static <ELEMENT_VALUE_TYPE> void setControl(
		final Class<? extends ELEMENT_VALUE_TYPE> type,
		final IInputControlProvider<ELEMENT_VALUE_TYPE> controlProvider) {
		getInstance().setControl(type, controlProvider);
	}

}
