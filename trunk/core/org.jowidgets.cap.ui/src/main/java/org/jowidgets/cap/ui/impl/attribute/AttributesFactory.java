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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifier;
import org.jowidgets.cap.ui.api.attribute.IAttributeFilter;
import org.jowidgets.cap.ui.api.attribute.IAttributeModifier;
import org.jowidgets.util.Assert;

final class AttributesFactory {

	List<IAttribute<Object>> createAttributesCopy(
		final Collection<? extends IAttribute<?>> attributes,
		final IAttributeCollectionModifier attributeCollectionModifier) {
		Assert.paramNotNull(attributes, "attributes");
		final List<IAttribute<Object>> result = new LinkedList<IAttribute<Object>>();

		for (final IAttribute<?> attribute : attributes) {
			@SuppressWarnings("unchecked")
			final AttributeBuilderImpl<Object> attributeBuilder = new AttributeBuilderImpl<Object>((IAttribute<Object>) attribute);
			if (attributeCollectionModifier != null) {
				final IAttribute<Object> resultAttribute = applyCollectionModifier(
						createProperty(attribute),
						attributeBuilder,
						attributeCollectionModifier);
				if (resultAttribute != null) {
					result.add(resultAttribute);
				}
			}
			else {
				result.add(attributeBuilder.build());
			}
		}
		return result;
	}

	List<IAttribute<Object>> createAttributes(
		final Collection<? extends IProperty> properties,
		final IAttributeCollectionModifier attributeCollectionModifier) {
		Assert.paramNotNull(properties, "properties");

		final List<IAttribute<Object>> result = new LinkedList<IAttribute<Object>>();
		for (final IProperty property : properties) {
			final AttributeBuilderImpl<Object> attributeBuilder = new AttributeBuilderImpl<Object>(property);
			if (attributeCollectionModifier != null) {
				final IAttribute<Object> attribute = applyCollectionModifier(
						property,
						attributeBuilder,
						attributeCollectionModifier);
				if (attribute != null) {
					result.add(attribute);
				}
			}
			else {
				result.add(attributeBuilder.build());
			}
		}
		return result;
	}

	private IAttribute<Object> applyCollectionModifier(
		final IProperty property,
		final IAttributeBuilder<Object> attributeBuilder,
		final IAttributeCollectionModifier attributeCollectionModifier) {

		//apply default modifier
		for (final IAttributeModifier modifier : attributeCollectionModifier.getDefaultModifiers()) {
			modifier.modify(property, attributeBuilder);
		}

		//apply property modifier
		final List<IAttributeModifier> modifierList = attributeCollectionModifier.getModifiers().get(property.getName());
		if (modifierList != null) {
			for (final IAttributeModifier modifier : modifierList) {
				modifier.modify(property, attributeBuilder);
			}
		}

		final IAttribute<Object> result = attributeBuilder.build();

		//apply filter
		for (final IAttributeFilter filter : attributeCollectionModifier.getFilters()) {
			if (!filter.accept(result)) {
				return null;
			}
		}

		return result;
	}

	private IProperty createProperty(final IAttribute<?> attribute) {
		return new IProperty() {

			@Override
			public boolean isVisibleDefault() {
				return attribute.isVisible();
			}

			@Override
			public boolean isSortable() {
				return attribute.isSortable();
			}

			@Override
			public boolean isReadonly() {
				return attribute.isReadonly();
			}

			@Override
			public boolean isMandatoryDefault() {
				return attribute.isMandatory();
			}

			@Override
			public boolean isFilterable() {
				return attribute.isFilterable();
			}

			@Override
			public Class<?> getValueType() {
				return attribute.getValueType();
			}

			@Override
			public String getName() {
				return attribute.getPropertyName();
			}

			@Override
			public String getLabelLongDefault() {
				return attribute.getLabelLong();
			}

			@Override
			public String getLabelDefault() {
				return attribute.getLabel();
			}

			@Override
			public Class<?> getElementValueType() {
				return attribute.getElementValueType();
			}

			@Override
			public String getDescriptionDefault() {
				return attribute.getDescription();
			}
		};
	}
}
