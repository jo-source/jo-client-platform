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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jowidgets.api.convert.IObjectLabelConverter;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.model.ILabelModel;
import org.jowidgets.tools.message.MessageReplacer;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.ITypedKey;

final class BeanProxyLabelPatternRenderer<BEAN_TYPE> implements IBeanProxyLabelRenderer<BEAN_TYPE> {

	private final String replacerPattern;
	private final Set<String> propertyDependencies;
	private final Set<? extends ITypedKey<?>> customPropertyDependencies;
	private final Map<String, IObjectLabelConverter<Object>> converterMap;

	@SuppressWarnings({"rawtypes", "unchecked"})
	BeanProxyLabelPatternRenderer(final String pattern, final Collection<? extends IAttribute<?>> attributes) {
		Assert.paramNotNull(attributes, "attributes");
		final HashSet<String> propertyDeps = new LinkedHashSet<String>();
		this.propertyDependencies = Collections.unmodifiableSet(propertyDeps);
		this.customPropertyDependencies = Collections.unmodifiableSet(new HashSet<ITypedKey<?>>());
		if (pattern != null) {
			final StringBuilder replacerPatternBuilder = new StringBuilder();
			int lastStart = -1;
			for (int i = 0; i < pattern.length(); i++) {
				final char c = pattern.charAt(i);
				if (c == '$') {
					if (lastStart == -1) {
						lastStart = i;
					}
					else {
						if (i - lastStart > 1) {
							final String propertyName = pattern.substring(lastStart + 1, i);
							propertyDeps.add(propertyName);
							replacerPatternBuilder.append("%" + propertyDeps.size());
							lastStart = -1;
						}
						else {
							replacerPatternBuilder.append('$');
						}
					}
				}
				else if (lastStart == -1) {
					replacerPatternBuilder.append(c);
				}
			}
			replacerPattern = replacerPatternBuilder.toString();
		}
		else {
			replacerPattern = null;
		}
		this.converterMap = new HashMap<String, IObjectLabelConverter<Object>>();
		if (!EmptyCheck.isEmpty(replacerPattern)) {
			for (final IAttribute attribute : attributes) {
				final String propertyName = attribute.getPropertyName();
				if (propertyDeps.contains(propertyName)) {
					final IControlPanelProvider<Object> controlPanel = attribute.getCurrentControlPanel();
					if (controlPanel != null) {
						final IObjectLabelConverter<Object> converter = controlPanel.getObjectLabelConverter();
						if (converter != null) {
							converterMap.put(propertyName, converter);
						}
						else {
							propertyDeps.remove(propertyName);
						}
					}
				}
			}
		}
	}

	@Override
	public ILabelModel getLabel(final IBeanProxy<BEAN_TYPE> bean) {
		if (EmptyCheck.isEmpty(replacerPattern)) {
			return new LabelModelImpl(bean.getId().toString());
		}
		else {
			final List<String> parameters = new LinkedList<String>();
			for (final String property : propertyDependencies) {
				String parameter = "";
				final IObjectLabelConverter<Object> converter = converterMap.get(property);
				if (converter != null) {
					final Object value = bean.getValue(property);
					if (value instanceof Collection) {
						final Collection<?> collection = (Collection<?>) value;
						if (collection.size() > 1) {
							parameter = converter.convertToString(collection.iterator().next()) + " [" + collection.size() + "]";
						}
						else if (collection.size() > 0) {
							parameter = converter.convertToString(collection.iterator().next());
						}
					}
					else if (value != null) {
						parameter = converter.convertToString(value);
					}
				}
				parameters.add(parameter);
			}
			return new LabelModelImpl(MessageReplacer.replace(replacerPattern, parameters));
		}
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
