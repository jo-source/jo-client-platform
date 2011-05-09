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

package org.jowidgets.cap.ui.impl.attribute;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifier;
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifierBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeFilter;
import org.jowidgets.cap.ui.api.attribute.IAttributeModifier;
import org.jowidgets.util.Assert;

final class AttributeCollectionModifierBuilderImpl implements IAttributeCollectionModifierBuilder {

	private final Set<IAttributeFilter> filters;
	private final List<IAttributeModifier> defaultModifiers;
	private final Map<String, List<IAttributeModifier>> modifiers;

	AttributeCollectionModifierBuilderImpl() {
		this.filters = new HashSet<IAttributeFilter>();
		this.defaultModifiers = new LinkedList<IAttributeModifier>();
		this.modifiers = new HashMap<String, List<IAttributeModifier>>();
	}

	@Override
	public IAttributeCollectionModifierBuilder addFilter(final IAttributeFilter filter) {
		Assert.paramNotNull(filter, "filter");
		this.filters.add(filter);
		return this;
	}

	@Override
	public IAttributeCollectionModifierBuilder addBlackListFilter(final List<String> propertyNames) {
		Assert.paramNotEmpty(propertyNames, "propertyNames");
		addFilter(new IAttributeFilter() {
			@Override
			public boolean accept(final IAttribute<?> attribute) {
				if (propertyNames.contains(attribute.getPropertyName())) {
					return false;
				}
				else {
					return true;
				}
			}
		});
		return this;
	}

	@Override
	public IAttributeCollectionModifierBuilder addBlackListFilter(final String... propertyNames) {
		Assert.paramNotEmpty(propertyNames, "propertyNames");
		addBlackListFilter(Arrays.asList(propertyNames));
		return this;
	}

	@Override
	public IAttributeCollectionModifierBuilder addWhiteListFilter(final List<String> propertyNames) {
		Assert.paramNotEmpty(propertyNames, "propertyNames");
		addFilter(new IAttributeFilter() {
			@Override
			public boolean accept(final IAttribute<?> attribute) {
				if (propertyNames.contains(attribute.getPropertyName())) {
					return true;
				}
				else {
					return false;
				}
			}
		});
		return this;
	}

	@Override
	public IAttributeCollectionModifierBuilder addWhiteListFilter(final String... propertyNames) {
		Assert.paramNotEmpty(propertyNames, "propertyNames");
		addWhiteListFilter(Arrays.asList(propertyNames));
		return this;
	}

	@Override
	public IAttributeCollectionModifierBuilder addAcceptEditableAttributesFilter() {
		addFilter(new IAttributeFilter() {
			@Override
			public boolean accept(final IAttribute<?> attribute) {
				if (attribute.isEditable()) {
					return true;
				}
				else {
					return false;
				}
			}
		});
		return this;
	}

	@Override
	public IAttributeCollectionModifierBuilder addDefaultModifier(final IAttributeModifier modifier) {
		Assert.paramNotNull(modifier, "modifier");
		defaultModifiers.add(modifier);
		return this;
	}

	@Override
	public IAttributeCollectionModifierBuilder addDefaultEditableModifier(final boolean editable) {
		addDefaultModifier(new IAttributeModifier() {
			@Override
			public void modify(final IProperty sourceProperty, final IAttributeBuilder<?> attributeBuilder) {
				if (editable) {
					attributeBuilder.setEditable(!sourceProperty.isReadonly());
				}
				else {
					attributeBuilder.setEditable(false);
				}
			}
		});
		return this;
	}

	@Override
	public IAttributeCollectionModifierBuilder addDefaultVisibleModifier(final boolean visible) {
		addDefaultModifier(new IAttributeModifier() {
			@Override
			public void modify(final IProperty sourceProperty, final IAttributeBuilder<?> attributeBuilder) {
				attributeBuilder.setVisible(visible);
			}
		});
		return this;
	}

	@Override
	public IAttributeCollectionModifierBuilder addModifier(final String propertyName, final IAttributeModifier modifier) {
		Assert.paramNotEmpty(propertyName, "propertyName");
		Assert.paramNotNull(modifier, "modifier");
		List<IAttributeModifier> modifierList = modifiers.get(propertyName);
		if (modifierList == null) {
			modifierList = new LinkedList<IAttributeModifier>();
			modifiers.put(propertyName, modifierList);
		}
		modifierList.add(modifier);
		return this;
	}

	@Override
	public IAttributeCollectionModifier build() {
		return new AttributeCollectionModifierImpl(filters, defaultModifiers, modifiers);
	}

}
