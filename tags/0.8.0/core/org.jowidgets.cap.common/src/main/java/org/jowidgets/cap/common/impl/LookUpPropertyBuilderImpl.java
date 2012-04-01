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

import org.jowidgets.cap.common.api.lookup.ILookUpProperty;
import org.jowidgets.cap.common.api.lookup.ILookUpPropertyBuilder;
import org.jowidgets.util.Assert;

final class LookUpPropertyBuilderImpl implements ILookUpPropertyBuilder {

	private String name;
	private Object displayFormatId;
	private String displayFormatName;
	private String displayFormatDescription;
	private Class<?> valueType;

	LookUpPropertyBuilderImpl() {
		this.valueType = String.class;
	}

	@Override
	public ILookUpPropertyBuilder setName(final String name) {
		Assert.paramNotNull(name, "name");
		this.name = name;
		return this;
	}

	@Override
	public ILookUpPropertyBuilder setDisplayFormatId(final Object displayFormatId) {
		Assert.paramNotNull(displayFormatId, "displayFormatId");
		this.displayFormatId = displayFormatId;
		return this;
	}

	@Override
	public ILookUpPropertyBuilder setDisplayFormatName(final String displayFormatName) {
		Assert.paramNotNull(displayFormatName, "displayFormatName");
		this.displayFormatName = displayFormatName;
		return this;
	}

	@Override
	public ILookUpPropertyBuilder setDisplayFormatDescription(final String displayFormatDescription) {
		this.displayFormatDescription = displayFormatDescription;
		return this;
	}

	@Override
	public ILookUpPropertyBuilder setValueType(final Class<?> valueType) {
		Assert.paramNotNull(valueType, "valueType");
		this.valueType = valueType;
		return this;
	}

	@Override
	public ILookUpProperty build() {
		return new LookUpPropertyImpl(
			getName(),
			getDisplayFormatId(),
			getDisplayFormatName(),
			displayFormatDescription,
			valueType);
	}

	private String getName() {
		if (name != null) {
			return name;
		}
		else {
			return ILookUpProperty.DEFAULT_NAME;
		}
	}

	private Object getDisplayFormatId() {
		if (displayFormatId != null) {
			return displayFormatId;
		}
		else {
			return getDisplayFormatName();
		}
	}

	private String getDisplayFormatName() {
		if (displayFormatName != null) {
			return displayFormatName;
		}
		else {
			return ILookUpProperty.DEFAULT_DISPLAY_FORMAT_NAME;
		}
	}

}
