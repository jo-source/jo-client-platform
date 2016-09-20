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
import java.util.Map;
import java.util.Set;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.validation.IBeanValidationResult;
import org.jowidgets.cap.common.api.validation.IBeanValidationResultListBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidationResultBuilder;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.ValidationResult;
import org.jowidgets.validation.Validator;

final class AttributesBeanPropertyValidator<BEAN_TYPE> implements IBeanPropertyValidator<BEAN_TYPE> {

	private final Map<String, IValidator<Object>> validators;

	AttributesBeanPropertyValidator(final Collection<? extends IAttribute<?>> attributes) {
		this.validators = new HashMap<String, IValidator<Object>>();
		for (final IAttribute<?> attribute : attributes) {
			final IValidator<Object> validator = attribute.getValidator();
			if (validator != null && !Validator.okValidator().equals(validator)) {
				validators.put(attribute.getPropertyName(), validator);
			}
		}
	}

	@Override
	public Collection<IBeanValidationResult> validateProperty(final IBeanProxy<BEAN_TYPE> bean, final String propertyName) {
		final IBeanValidationResultListBuilder builder = CapCommonToolkit.beanValidationResultListBuilder();
		builder.addResult(validatePropertyImpl(bean, propertyName), propertyName);
		return builder.build();
	}

	private IValidationResult validatePropertyImpl(final IBeanProxy<BEAN_TYPE> bean, final String propertyName) {
		final IValidationResultBuilder builder = ValidationResult.builder();
		final IValidator<Object> validator = validators.get(propertyName);
		if (validator != null) {
			final IValidationResult validationResult = validator.validate(bean.getValue(propertyName));
			if (!validationResult.isValid()) {
				return validationResult;
			}
			else if (!validationResult.isOk()) {
				builder.addResult(validationResult);
			}
		}

		return builder.build();
	}

	boolean hasValidators() {
		return validators.size() > 0;
	}

	@Override
	public Set<String> getPropertyDependencies() {
		return validators.keySet();
	}

	@Override
	public boolean isSymmetric() {
		return false;
	}

}
