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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.Cardinality;
import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanPropertyBuilder;
import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidator;

final class BeanPropertyBuilderImpl implements IBeanPropertyBuilder {

	private final Class<?> beanType;
	private final PropertyBuilder propertyBuilder;

	BeanPropertyBuilderImpl(final Class<?> beanType, final String propertyName) {
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotEmpty(propertyName, "propertyName");

		this.beanType = beanType;
		this.propertyBuilder = new PropertyBuilder();

		propertyBuilder.setName(propertyName);

		BeanInfo beanInfo;

		if (IBean.ID_PROPERTY.equals(propertyName)) {
			propertyBuilder.setElementValueType(Object.class);
			propertyBuilder.setValueType(Object.class);
			propertyBuilder.setReadonly(true);
		}
		else if (IBean.VERSION_PROPERTY.equals(propertyName)) {
			propertyBuilder.setElementValueType(long.class);
			propertyBuilder.setValueType(long.class);
			propertyBuilder.setReadonly(true);
		}
		else {
			try {
				beanInfo = Introspector.getBeanInfo(beanType);
			}
			catch (final IntrospectionException e) {
				throw new RuntimeException(e);
			}
			for (final PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
				if (propertyDescriptor.getName().equals(propertyName)) {
					final Class<?> propertyType = propertyDescriptor.getPropertyType();
					propertyBuilder.setValueType(propertyType);
					if (boolean.class.isAssignableFrom(propertyType)) {
						propertyBuilder.setDefaultValue(false);
					}
					if (!Collection.class.isAssignableFrom(propertyType)) {
						propertyBuilder.setElementValueType(propertyType);
						propertyBuilder.setSortable(propertyType.isPrimitive() || Comparable.class.isAssignableFrom(propertyType));
					}
					else {
						final Type returnType = propertyDescriptor.getReadMethod().getGenericReturnType();
						if (returnType instanceof ParameterizedType) {
							final ParameterizedType paramType = (ParameterizedType) returnType;
							if (paramType.getActualTypeArguments().length == 1) {
								final Type typeArg = paramType.getActualTypeArguments()[0];
								if (typeArg instanceof Class<?>) {
									propertyBuilder.setElementValueType((Class<?>) typeArg);
								}
							}
						}
						propertyBuilder.setSortable(false);
					}
					propertyBuilder.setReadonly(propertyDescriptor.getWriteMethod() == null);
				}
			}
			//TODO MG take respect of the super classes and implemented interfaces
		}
	}

	@Override
	public IBeanPropertyBuilder setValueRange(final IValueRange valueRange) {
		propertyBuilder.setValueRange(valueRange);
		return this;
	}

	@Override
	public IBeanPropertyBuilder setValueRange(final boolean open, final Collection<? extends Object> values) {
		propertyBuilder.setValueRange(open, values);
		return this;
	}

	@Override
	public IBeanPropertyBuilder setValueRange(final Collection<? extends Object> values) {
		propertyBuilder.setValueRange(values);
		return this;
	}

	@Override
	public IBeanPropertyBuilder setValueRange(final boolean open, final Object... values) {
		return setValueRange(open, Arrays.asList(values));
	}

	@Override
	public IBeanPropertyBuilder setValueRange(final Object... values) {
		return setValueRange(Arrays.asList(values));
	}

	@Override
	public IBeanPropertyBuilder setLookUpValueRange(final Object lookUpId) {
		return setValueRange(CapCommonToolkit.lookUpToolkit().lookUpValueRange(lookUpId));
	}

	@Override
	public IBeanPropertyBuilder setDefaultValue(final Object value) {
		propertyBuilder.setDefaultValue(value);
		return this;
	}

	@Override
	public IBeanPropertyBuilder setCardinality(final Cardinality cardinality) {
		propertyBuilder.setCardinality(cardinality);
		return this;
	}

	@Override
	public IBeanPropertyBuilder setLabel(final String labelDefault) {
		propertyBuilder.setLabel(labelDefault);
		return this;
	}

	@Override
	public IBeanPropertyBuilder setLabelLong(final String labelLongDefault) {
		propertyBuilder.setLabelLong(labelLongDefault);
		return this;
	}

	@Override
	public IBeanPropertyBuilder setDescription(final String descriptionDefault) {
		propertyBuilder.setDescription(descriptionDefault);
		return this;
	}

	@Override
	public IBeanPropertyBuilder setVisible(final boolean visibleDefault) {
		propertyBuilder.setVisible(visibleDefault);
		return this;
	}

	@Override
	public IBeanPropertyBuilder setMandatory(final boolean mandatoryDefault) {
		propertyBuilder.setMandatory(mandatoryDefault);
		return this;
	}

	@Override
	public IBeanPropertyBuilder setEditable(final boolean editable) {
		propertyBuilder.setEditable(editable);
		return this;
	}

	@Override
	public IBeanPropertyBuilder setElementValueType(final Class<?> elementValueType) {
		propertyBuilder.setElementValueType(elementValueType);
		return this;
	}

	@Override
	public IBeanPropertyBuilder addValidator(final IValidator<? extends Object> validator) {
		propertyBuilder.addValidator(validator);
		return this;
	}

	@Override
	public IBeanPropertyBuilder addElementTypeValidator(final IValidator<? extends Object> validator) {
		propertyBuilder.addElementTypeValidator(validator);
		return this;
	}

	@Override
	public IBeanPropertyBuilder setSortable(final boolean sortable) {
		propertyBuilder.setSortable(sortable);
		return this;
	}

	@Override
	public IBeanPropertyBuilder setFilterable(final boolean filterable) {
		propertyBuilder.setFilterable(filterable);
		return this;
	}

	@Override
	public IProperty build() {
		if (!propertyBuilder.isBeanValidatorAdded()) {
			propertyBuilder.addBeanValidator(beanType);
		}
		return propertyBuilder.build();
	}
}
