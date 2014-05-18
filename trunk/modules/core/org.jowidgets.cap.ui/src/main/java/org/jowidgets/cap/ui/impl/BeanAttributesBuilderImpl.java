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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IBeanAttributeBluePrint;
import org.jowidgets.cap.ui.api.attribute.IBeanAttributesBuilder;
import org.jowidgets.util.Assert;

final class BeanAttributesBuilderImpl implements IBeanAttributesBuilder {

	private final Class<?> beanType;
	private final Map<String, BeanAttributeBluePrintImpl<Object>> attributes;

	BeanAttributesBuilderImpl(final Class<?> beanType) {
		Assert.paramNotNull(beanType, "beanType");
		this.beanType = beanType;
		this.attributes = new LinkedHashMap<String, BeanAttributeBluePrintImpl<Object>>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <ELEMENT_VALUE_TYPE> IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE> add(final String propertyName) {
		Assert.paramNotNull(propertyName, "propertyName");
		if (attributes.containsKey(propertyName)) {
			throw new IllegalArgumentException("The property with the name '" + propertyName + "' was already added");
		}
		final BeanAttributeBluePrintImpl<Object> result = new BeanAttributeBluePrintImpl<Object>(beanType, propertyName);
		attributes.put(propertyName, result);
		return (IBeanAttributeBluePrint<ELEMENT_VALUE_TYPE>) result;
	}

	@Override
	public List<IAttribute<Object>> build() {
		final List<IAttribute<Object>> result = new LinkedList<IAttribute<Object>>();
		for (final BeanAttributeBluePrintImpl<Object> builder : attributes.values()) {
			result.add(builder.build());
		}
		return Collections.unmodifiableList(result);
	}

}
