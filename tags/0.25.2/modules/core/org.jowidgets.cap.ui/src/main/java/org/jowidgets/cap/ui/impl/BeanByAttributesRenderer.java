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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jowidgets.api.convert.IObjectLabelConverter;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.model.ILabelModel;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.ITypedKey;

final class BeanByAttributesRenderer<BEAN_TYPE> implements IBeanProxyLabelRenderer<BEAN_TYPE> {

	private final List<IAttribute<Object>> attributes;
	private final Set<String> propertyDependencies;
	private final Set<? extends ITypedKey<?>> customPropertyDependencies;

	BeanByAttributesRenderer(final List<IAttribute<Object>> attributes) {
		this.attributes = new LinkedList<IAttribute<Object>>(attributes);
		this.propertyDependencies = new HashSet<String>();
		this.customPropertyDependencies = Collections.emptySet();
		for (final IAttribute<Object> attribute : attributes) {
			propertyDependencies.add(attribute.getPropertyName());
		}
	}

	@Override
	public ILabelModel getLabel(final IBeanProxy<BEAN_TYPE> bean) {
		final StringBuilder result = new StringBuilder();
		for (final IAttribute<Object> attribute : attributes) {
			if (attribute.isVisible() && !attribute.isCollectionType()) {
				final Object value = bean.getValue(attribute.getPropertyName());
				if (!EmptyCheck.isEmpty(value)) {
					final IControlPanelProvider<Object> controlPanel = attribute.getCurrentControlPanel();
					if (controlPanel != null) {
						final IObjectLabelConverter<Object> converter = controlPanel.getObjectLabelConverter();
						if (converter != null && !(value instanceof Collection)) {
							result.append(converter.convertToString(value) + " ");
						}
					}
				}
			}
		}
		return new LabelModelImpl(result.toString(), null, null);
	}

	@Override
	public Set<String> getPropertyDependencies() {
		return propertyDependencies;
	}

	@Override
	public Set<? extends ITypedKey<?>> getCustomPropertyDependencies() {
		return customPropertyDependencies;
	}

}
