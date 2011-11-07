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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.common.api.lookup.ILookUpEntry;
import org.jowidgets.cap.ui.api.lookup.ILookUp;
import org.jowidgets.util.Assert;

final class LookUpImpl implements ILookUp {

	private final Map<Object, ILookUpEntry> map;
	private final List<Object> keys;
	private final List<Object> keysView;
	private final List<Object> validKeys;
	private final List<Object> validKeysView;
	private final List<ILookUpEntry> entries;
	private final List<ILookUpEntry> entriesView;

	private Map<String, Map<Object, Object>> valueToKeys;

	LookUpImpl(final List<ILookUpEntry> entries) {
		Assert.paramNotNull(entries, "entries");

		this.map = new HashMap<Object, ILookUpEntry>();

		this.keys = new LinkedList<Object>();
		this.keysView = Collections.unmodifiableList(keys);

		this.validKeys = new LinkedList<Object>();
		this.validKeysView = Collections.unmodifiableList(validKeys);

		this.entries = new LinkedList<ILookUpEntry>();
		this.entriesView = Collections.unmodifiableList(entries);

		for (final ILookUpEntry lookUpEntry : entries) {
			map.put(lookUpEntry.getKey(), lookUpEntry);
			keys.add(lookUpEntry.getKey());
			if (lookUpEntry.isValid()) {
				validKeys.add(lookUpEntry.getKey());
			}
			this.entries.add(lookUpEntry);
		}
	}

	@Override
	public ILookUpEntry getEntry(final Object key) {
		Assert.paramNotNull(key, "key");
		return map.get(key);
	}

	@Override
	public Object getValue(final Object key, final String propertyName) {
		Assert.paramNotNull(key, "key");
		Assert.paramNotNull(propertyName, "propertyName");
		final ILookUpEntry lookUpEntry = map.get(key);
		if (lookUpEntry != null) {
			return lookUpEntry.getValue(propertyName);
		}
		else {
			return null;
		}
	}

	@Override
	public Object getKey(Object value, final String propertyName) {
		if (value instanceof String) {
			value = ((String) value).toLowerCase();
		}
		return getValueToKeys(propertyName).get(value);
	}

	@Override
	public String getDescription(final Object key) {
		Assert.paramNotNull(key, "key");
		final ILookUpEntry lookUpEntry = map.get(key);
		if (lookUpEntry != null) {
			return lookUpEntry.getDescription();
		}
		else {
			return null;
		}
	}

	@Override
	public List<Object> getValidKeys() {
		return validKeysView;
	}

	@Override
	public List<Object> getKeys() {
		return keysView;
	}

	@Override
	public List<ILookUpEntry> getEntries() {
		return entriesView;
	}

	private Map<Object, Object> getValueToKeys(final String propertyName) {
		if (valueToKeys == null) {
			valueToKeys = new HashMap<String, Map<Object, Object>>();
		}
		Map<Object, Object> result = valueToKeys.get(propertyName);
		if (result == null) {
			result = new HashMap<Object, Object>();
			for (final ILookUpEntry lookUpEntry : entries) {
				Object value = lookUpEntry.getValue(propertyName);
				if (value instanceof String) {
					value = ((String) value).toLowerCase();
				}
				result.put(value, lookUpEntry.getKey());
			}
			valueToKeys.put(propertyName, result);
		}
		return result;
	}

}
