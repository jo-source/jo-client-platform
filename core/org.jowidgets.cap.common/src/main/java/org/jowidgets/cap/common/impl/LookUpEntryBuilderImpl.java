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

import java.util.HashMap;
import java.util.Map;

import org.jowidgets.cap.common.api.lookup.ILookUpEntry;
import org.jowidgets.cap.common.api.lookup.ILookUpEntryBuilder;
import org.jowidgets.cap.common.api.lookup.ILookUpProperty;
import org.jowidgets.util.Assert;

final class LookUpEntryBuilderImpl implements ILookUpEntryBuilder {

	private final Map<String, Object> values;

	private Object key;
	private String description;
	private boolean valid;

	LookUpEntryBuilderImpl() {
		this.valid = true;
		this.values = new HashMap<String, Object>();
	}

	@Override
	public ILookUpEntryBuilder setKey(final Object key) {
		Assert.paramNotNull(key, "key");
		this.key = key;
		return this;
	}

	@Override
	public ILookUpEntryBuilder setValue(final String propertyName, final Object value) {
		Assert.paramNotNull(propertyName, "propertyName");
		Assert.paramNotNull(value, "value");
		values.put(propertyName, value);
		return this;
	}

	@Override
	public ILookUpEntryBuilder setValue(final Object value) {
		return setValue(ILookUpProperty.DEFAULT_NAME, value);
	}

	@Override
	public ILookUpEntryBuilder setDescription(final String description) {
		this.description = description;
		return this;
	}

	@Override
	public ILookUpEntryBuilder setValid(final boolean valid) {
		this.valid = valid;
		return this;
	}

	@Override
	public ILookUpEntry build() {
		return new LookUpEntryImpl(key, values, description, valid);
	}

}
