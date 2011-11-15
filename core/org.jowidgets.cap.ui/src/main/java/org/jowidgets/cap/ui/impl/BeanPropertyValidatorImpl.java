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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidationResultBuilder;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.ValidationResult;

final class BeanPropertyValidatorImpl<BEAN_TYPE> implements IBeanPropertyValidator<BEAN_TYPE> {

	private final Map<String, IAttribute<?>> attributes;
	private final List<IBeanValidator<BEAN_TYPE>> beanValidators;
	private final Map<String, List<IBeanValidator<BEAN_TYPE>>> propertyDependendBeanValidators;

	BeanPropertyValidatorImpl(final Collection<? extends IAttribute<?>> attributes) {
		this.attributes = new HashMap<String, IAttribute<?>>();
		this.beanValidators = new LinkedList<IBeanValidator<BEAN_TYPE>>();
		this.propertyDependendBeanValidators = new HashMap<String, List<IBeanValidator<BEAN_TYPE>>>();
		for (final IAttribute<?> attribute : attributes) {
			this.attributes.put(attribute.getPropertyName(), attribute);
		}
	}

	@Override
	public IValidationResult validateProperty(final IBeanProxy<BEAN_TYPE> bean, final String propertyName) {
		final IValidationResultBuilder builder = ValidationResult.builder();
		final IAttribute<?> attribute = attributes.get(propertyName);
		if (attribute != null) {
			final IValidator<Object> validator = attribute.getValidator();
			if (validator != null) {
				final IValidationResult validationResult = validator.validate(bean.getValue(propertyName));
				if (!validationResult.isValid()) {
					return validationResult.withContext(attribute.getCurrentLabel());
				}
				else {
					builder.addResult(validationResult.withContext(attribute.getCurrentLabel()));
				}
			}
		}
		final List<IBeanValidator<BEAN_TYPE>> validatorList = propertyDependendBeanValidators.get(propertyName);
		if (validatorList != null) {
			for (final IBeanValidator<BEAN_TYPE> validator : validatorList) {
				final IValidationResult validationResult = validator.validate(bean.getBean());
				if (!validationResult.isValid()) {
					return validationResult;
				}
				else {
					builder.addResult(validationResult);
				}
			}
		}

		for (final IBeanValidator<BEAN_TYPE> validator : beanValidators) {
			final IValidationResult validationResult = validator.validate(bean.getBean());
			if (!validationResult.isValid()) {
				return validationResult;
			}
			else {
				builder.addResult(validationResult);
			}
		}

		return builder.build();
	}

	public void addBeanValidator(final IBeanValidator<BEAN_TYPE> beanValidator) {
		Assert.paramNotNull(beanValidator, "beanValidator");
		final Set<String> propertyDependencies = beanValidator.getPropertyDependencies();
		if (EmptyCheck.isEmpty(propertyDependencies)) {
			beanValidators.add(beanValidator);
		}
		else {
			for (final String propertyName : propertyDependencies) {
				getBeanValidators(propertyName).add(beanValidator);
			}
		}
	}

	private List<IBeanValidator<BEAN_TYPE>> getBeanValidators(final String propertyName) {
		List<IBeanValidator<BEAN_TYPE>> result = propertyDependendBeanValidators.get(propertyName);
		if (result == null) {
			result = new LinkedList<IBeanValidator<BEAN_TYPE>>();
			propertyDependendBeanValidators.put(propertyName, result);
		}
		return result;
	}
}
