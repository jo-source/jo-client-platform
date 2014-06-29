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

package org.jowidgets.cap.common.api.lookup;

import org.jowidgets.cap.common.api.CapCommonToolkit;

public final class LookUpToolkit {

	private LookUpToolkit() {}

	public static ILookUpPropertyBuilder lookUpPropertyBuilder() {
		return CapCommonToolkit.lookUpToolkit().lookUpPropertyBuilder();
	}

	public static ILookUpProperty lookUpProperty(
		final String name,
		final String displayFormatName,
		final String displayFormatDescription) {
		return CapCommonToolkit.lookUpToolkit().lookUpProperty(name, displayFormatName, displayFormatDescription);
	}

	public static ILookUpProperty lookUpProperty(final String name, final String displayFormatName) {
		return CapCommonToolkit.lookUpToolkit().lookUpProperty(name, displayFormatName);
	}

	public static ILookUpProperty lookUpProperty() {
		return CapCommonToolkit.lookUpToolkit().lookUpProperty();
	}

	public static ILookUpEntryBuilder lookUpEntryBuilder() {
		return CapCommonToolkit.lookUpToolkit().lookUpEntryBuilder();
	}

	public static ILookUpEntry lookUpEntry(final Object key, final Object value, final String description) {
		return CapCommonToolkit.lookUpToolkit().lookUpEntry(key, value, description);
	}

	public static ILookUpEntry lookUpEntry(final Object key, final Object value) {
		return CapCommonToolkit.lookUpToolkit().lookUpEntry(key, value);
	}

	public static ILookUpValueRangeBuilder lookUpValueRangeBuilder() {
		return CapCommonToolkit.lookUpToolkit().lookUpValueRangeBuilder();
	}

	public static ILookUpValueRange lookUpValueRange(final Object lookUpId) {
		return CapCommonToolkit.lookUpToolkit().lookUpValueRange(lookUpId);
	}

}
