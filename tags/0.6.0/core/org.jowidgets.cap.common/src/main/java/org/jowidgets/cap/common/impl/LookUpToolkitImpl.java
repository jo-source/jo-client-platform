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

package org.jowidgets.cap.common.impl;

import org.jowidgets.cap.common.api.lookup.ILookUpEntry;
import org.jowidgets.cap.common.api.lookup.ILookUpEntryBuilder;
import org.jowidgets.cap.common.api.lookup.ILookUpProperty;
import org.jowidgets.cap.common.api.lookup.ILookUpPropertyBuilder;
import org.jowidgets.cap.common.api.lookup.ILookUpToolkit;
import org.jowidgets.cap.common.api.lookup.ILookUpValueRange;
import org.jowidgets.cap.common.api.lookup.ILookUpValueRangeBuilder;

final class LookUpToolkitImpl implements ILookUpToolkit {

	@Override
	public ILookUpPropertyBuilder lookUpPropertyBuilder() {
		return new LookUpPropertyBuilderImpl();
	}

	@Override
	public ILookUpProperty lookUpProperty(final String name, final String displayFormatName, final String displayFormatDescription) {
		final ILookUpPropertyBuilder builder = lookUpPropertyBuilder();
		builder.setName(name).setDisplayFormatName(displayFormatName).setDisplayFormatDescription(displayFormatDescription);
		return builder.build();
	}

	@Override
	public ILookUpProperty lookUpProperty(final String name, final String displayFormatName) {
		final ILookUpPropertyBuilder builder = lookUpPropertyBuilder();
		builder.setName(name).setDisplayFormatName(displayFormatName);
		return builder.build();
	}

	@Override
	public ILookUpProperty lookUpProperty() {
		return lookUpPropertyBuilder().build();
	}

	@Override
	public ILookUpEntryBuilder lookUpEntryBuilder() {
		return new LookUpEntryBuilderImpl();
	}

	@Override
	public ILookUpEntry lookUpEntry(final Object key, final Object value, final String description) {
		return lookUpEntryBuilder().setKey(key).setValue(value).setDescription(description).build();
	}

	@Override
	public ILookUpEntry lookUpEntry(final Object key, final Object value) {
		return lookUpEntryBuilder().setKey(key).setValue(value).build();
	}

	@Override
	public ILookUpValueRangeBuilder lookUpValueRangeBuilder() {
		return new LookUpValueRangeBuilderImpl();
	}

	@Override
	public ILookUpValueRange lookUpValueRange(final Object lookUpId) {
		return lookUpValueRangeBuilder().setLookUpId(lookUpId).build();
	}

}
