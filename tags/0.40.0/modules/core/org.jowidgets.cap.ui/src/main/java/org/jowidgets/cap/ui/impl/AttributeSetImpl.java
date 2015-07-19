/*
 * Copyright (c) 2014, grossmann
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeFilter;
import org.jowidgets.cap.ui.api.attribute.IAttributeSet;
import org.jowidgets.util.collection.UnmodifiableArray;
import org.jowidgets.util.collection.UnmodifieableArrayWrapper;

final class AttributeSetImpl extends UnmodifieableArrayWrapper<IAttribute<Object>> implements IAttributeSet {

	private final Map<String, IAttribute<Object>> attributesMap;

	AttributeSetImpl(final Collection<IAttribute<Object>> attributes) {
		super(UnmodifiableArray.create(attributes));
		this.attributesMap = new LinkedHashMap<String, IAttribute<Object>>();
		for (final IAttribute<Object> attribute : attributes) {
			attributesMap.put(attribute.getPropertyName(), attribute);
		}
	}

	@Override
	public IAttribute<Object> getAttribute(final int index) {
		return get(index);
	}

	@Override
	public IAttribute<Object> getAttribute(final String propertyName) {
		return attributesMap.get(propertyName);
	}

	@Override
	public Collection<IAttribute<Object>> getAttributes() {
		return attributesMap.values();
	}

	@Override
	public Collection<IAttribute<Object>> getAttributes(final IAttributeFilter filter) {
		if (filter == null) {
			return getAttributes();
		}
		else {
			final List<IAttribute<Object>> result = new LinkedList<IAttribute<Object>>();
			for (final IAttribute<Object> attribute : this) {
				if (filter.accept(attribute)) {
					result.add(attribute);
				}
			}
			return Collections.unmodifiableList(result);
		}
	}

	@Override
	public Collection<String> getPropertyNames() {
		return attributesMap.keySet();
	}

}
