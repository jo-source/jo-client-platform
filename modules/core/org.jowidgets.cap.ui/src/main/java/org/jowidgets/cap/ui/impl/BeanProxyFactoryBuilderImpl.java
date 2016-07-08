/*
 * Copyright (c) 2016, Grossmann
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.AttributeSet;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeSet;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactoryBuilder;
import org.jowidgets.util.Assert;

final class BeanProxyFactoryBuilderImpl<BEAN_TYPE> implements IBeanProxyFactoryBuilder<BEAN_TYPE> {

	private final Class<BEAN_TYPE> beanType;
	private final Collection<IBeanPropertyValidator<BEAN_TYPE>> validators;

	private Object beanTypeId;
	private IAttributeSet attributes;
	private Map<String, Object> defaultValues;
	private boolean validateUnmodified;

	@SuppressWarnings("unchecked")
	BeanProxyFactoryBuilderImpl(final Class<? extends BEAN_TYPE> beanType) {
		Assert.paramNotNull(beanType, "beanType");
		this.beanType = (Class<BEAN_TYPE>) beanType;
		this.beanTypeId = CapUiToolkit.entityTypeId(beanType).getBeanTypeId();
		this.validateUnmodified = true;
		this.defaultValues = new HashMap<String, Object>();
		this.validators = new LinkedList<IBeanPropertyValidator<BEAN_TYPE>>();
	}

	@Override
	public IBeanProxyFactoryBuilder<BEAN_TYPE> setBeanTypeId(final Object beanTypeId) {
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		this.beanTypeId = beanTypeId;
		return this;
	}

	@Override
	public IBeanProxyFactoryBuilder<BEAN_TYPE> setAttributes(final IAttributeSet attributes) {
		Assert.paramNotNull(attributes, "attributes");
		this.attributes = attributes;
		return this;
	}

	@Override
	public IBeanProxyFactoryBuilder<BEAN_TYPE> addBeanPropertyValidator(final IBeanPropertyValidator<BEAN_TYPE> validator) {
		Assert.paramNotNull(validator, "validator");
		this.validators.add(validator);
		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public IBeanProxyFactoryBuilder<BEAN_TYPE> addBeanPropertyValidators(final Collection validators) {
		Assert.paramNotNull(validators, "validator");
		this.validators.addAll(validators);
		return this;
	}

	@Override
	public IBeanProxyFactoryBuilder<BEAN_TYPE> setBeanPropertyValidators(
		final Collection<? extends IBeanPropertyValidator<BEAN_TYPE>> validators) {
		this.validators.clear();
		addBeanPropertyValidators(validators);
		return this;
	}

	@Override
	public IBeanProxyFactoryBuilder<BEAN_TYPE> setDefaultValuesForTransientBeans(final Map<String, Object> defaultValues) {
		if (defaultValues != null) {
			this.defaultValues = new HashMap<String, Object>(defaultValues);
		}
		else {
			this.defaultValues = new HashMap<String, Object>();
		}
		return this;
	}

	@Override
	public IBeanProxyFactoryBuilder<BEAN_TYPE> setDefaultValuesForTransientBeans(final IAttributeSet attributes) {
		setDefaultValuesForTransientBeans(createDefaultValues(attributes));
		return this;
	}

	@Override
	public IBeanProxyFactoryBuilder<BEAN_TYPE> setValidateUnmodifiedBeans(final boolean validateUnmodified) {
		this.validateUnmodified = validateUnmodified;
		return this;
	}

	@Override
	public IBeanProxyFactoryBuilder<BEAN_TYPE> addBeanPropertyValidator(final IAttributeSet attributes) {
		addBeanPropertyValidator(new AttributesBeanPropertyValidator<BEAN_TYPE>(attributes.getAttributes()));
		return this;
	}

	@Override
	public IBeanProxyFactoryBuilder<BEAN_TYPE> addBeanPropertyValidator(final IBeanValidator<BEAN_TYPE> beanValidator) {
		addBeanPropertyValidator(new BeanPropertyValidatorAdapter<BEAN_TYPE>(beanValidator));
		return this;
	}

	@Override
	public IBeanProxyFactoryBuilder<BEAN_TYPE> addBeanPropertyValidator(
		final Collection<? extends IBeanValidator<BEAN_TYPE>> beanValidators) {
		for (final IBeanValidator<BEAN_TYPE> beanValidator : beanValidators) {
			addBeanPropertyValidator(new BeanPropertyValidatorAdapter<BEAN_TYPE>(beanValidator));
		}
		return this;
	}

	@Override
	public IBeanProxyFactoryBuilder<BEAN_TYPE> configureFromEntityService(final Object entityId) {
		final IBeanDtoDescriptor dtoDescriptor = EntityServiceHelper.getDtoDescriptor(entityId);
		final List<IAttribute<Object>> entityAttributes = EntityServiceHelper.createAttributes(entityId);
		if (dtoDescriptor != null && entityAttributes != null) {
			final Class<?> descritporBeanType = dtoDescriptor.getBeanType();
			if (descritporBeanType == null || !descritporBeanType.isAssignableFrom(beanType)) {
				throw new IllegalArgumentException(
					"The entity id '"
						+ entityId
						+ "' has a bean type '"
						+ descritporBeanType
						+ "' that is not compatible with the bean type '"
						+ beanType
						+ "' of this builder");
			}
			setBeanTypeId(dtoDescriptor.getBeanTypeId());
			final IAttributeSet attributeSet = AttributeSet.create(entityAttributes);
			setAttributes(attributeSet);
			addValidators(dtoDescriptor, attributeSet);
			setDefaultValuesForTransientBeans(attributeSet);
		}
		else {
			throw new IllegalArgumentException("TFor the entity id '" + entityId + "' no descriptor or attributes was found");
		}
		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void addValidators(final IBeanDtoDescriptor dtoDescriptor, final IAttributeSet attributes) {
		addBeanPropertyValidator(attributes);
		final Set beanValidators = dtoDescriptor.getValidators();
		addBeanPropertyValidator(beanValidators);
	}

	private Map<String, Object> createDefaultValues(final IAttributeSet attributes) {
		final Map<String, Object> result = new HashMap<String, Object>();
		for (final IAttribute<?> attribute : attributes) {
			final String propertyName = attribute.getPropertyName();
			final Object defaultValue = attribute.getDefaultValue();
			if (defaultValue != null) {
				defaultValues.put(propertyName, defaultValue);
			}
		}
		return result;
	}

	Class<BEAN_TYPE> getBeanType() {
		return beanType;
	}

	Object getBeanTypeId() {
		return beanTypeId;
	}

	IAttributeSet getAttributes() {
		return attributes;
	}

	Collection<IBeanPropertyValidator<BEAN_TYPE>> getValidators() {
		return validators;
	}

	Map<String, Object> getDefaultValues() {
		return defaultValues;
	}

	boolean isValidateUnmodified() {
		return validateUnmodified;
	}

	@Override
	public IBeanProxyFactory<BEAN_TYPE> build() {
		return new BeanProxyFactoryImpl<BEAN_TYPE>(this);
	}

}
