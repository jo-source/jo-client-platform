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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.lookup.ILookUpProperty;
import org.jowidgets.cap.common.api.lookup.ILookUpToolkit;
import org.jowidgets.cap.common.api.lookup.ILookUpValueRange;
import org.jowidgets.cap.common.api.lookup.ILookUpValueRangeBuilder;
import org.jowidgets.util.Assert;

public class LookUpValueRangeBuilderImpl implements ILookUpValueRangeBuilder {

	private Object lookUpId;
	private List<ILookUpProperty> lookUpProperties;
	private String defaultPropertyName;

	@Override
	public ILookUpValueRangeBuilder setLookUpId(final Object lookUpId) {
		Assert.paramNotNull(lookUpId, "lookUpId");
		this.lookUpId = lookUpId;
		return this;
	}

	@Override
	public ILookUpValueRangeBuilder setValueProperties(final List<? extends ILookUpProperty> lookUpProperties) {
		Assert.paramNotEmpty(lookUpProperties, "lookUpProperties");
		this.lookUpProperties = new LinkedList<ILookUpProperty>(lookUpProperties);
		return this;
	}

	@Override
	public ILookUpValueRangeBuilder addValueProperty(final ILookUpProperty property) {
		Assert.paramNotNull(property, "property");
		getLookUpPropertiesLazy().add(property);
		return this;
	}

	@Override
	public ILookUpValueRangeBuilder addValueProperty(
		final String name,
		final String displayFormatName,
		final String displayFormatDescription) {

		final ILookUpToolkit lookUpToolkit = CapCommonToolkit.lookUpToolkit();
		final ILookUpProperty property = lookUpToolkit.lookUpProperty(name, displayFormatName, displayFormatDescription);
		getLookUpPropertiesLazy().add(property);

		return this;
	}

	@Override
	public ILookUpValueRangeBuilder addValueProperty(final String name, final String displayFormatName) {

		final ILookUpToolkit lookUpToolkit = CapCommonToolkit.lookUpToolkit();
		final ILookUpProperty property = lookUpToolkit.lookUpProperty(name, displayFormatName);
		getLookUpPropertiesLazy().add(property);

		return this;
	}

	@Override
	public ILookUpValueRangeBuilder setDefaultValuePropertyName(final String defaultPropertyName) {
		this.defaultPropertyName = defaultPropertyName;
		return this;
	}

	@Override
	public ILookUpValueRange build() {
		return new LookUpValueRangeImpl(lookUpId, getLookUpProperties(), getDefaultValuePropertyName());
	}

	private String getDefaultValuePropertyName() {
		if (defaultPropertyName == null) {
			return getLookUpProperties().get(0).getName();
		}
		else {
			return defaultPropertyName;
		}
	}

	private List<ILookUpProperty> getLookUpProperties() {
		if (lookUpProperties == null) {
			return Collections.singletonList(CapCommonToolkit.lookUpToolkit().lookUpProperty());
		}
		else {
			return lookUpProperties;
		}
	}

	private List<ILookUpProperty> getLookUpPropertiesLazy() {
		if (lookUpProperties == null) {
			lookUpProperties = new LinkedList<ILookUpProperty>();
		}
		return lookUpProperties;
	}
}
