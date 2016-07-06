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

import java.io.Serializable;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.ValidationResult;

final class BeanPropertyValidatorAdapter implements IValidator<Object>, Serializable {

	private static final long serialVersionUID = 3669089126219297774L;

	private final Class<?> beanType;
	private final String propertyName;
	private boolean propertyValidatable;

	BeanPropertyValidatorAdapter(final Class<?> beanType, final String propertyName) {
		this.beanType = beanType;
		this.propertyName = propertyName;
		this.propertyValidatable = true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IValidationResult validate(final Object value) {
		if (propertyValidatable) {
			final Set<ConstraintViolation<Object>> beanValidationResult;
			try {
				final Validator beanValidator = CapCommonToolkit.beanValidator();
				beanValidationResult = beanValidator.validateValue((Class<Object>) beanType, propertyName, value);
			}
			catch (final Exception e) {
				propertyValidatable = false;
				return ValidationResult.ok();
			}
			for (final ConstraintViolation<Object> violation : beanValidationResult) {
				return ValidationResult.error(violation.getMessage());
			}
		}
		return ValidationResult.ok();
	}

	public static boolean isBeanPropertyConstrained(final Class<?> beanType, final String propertyName) {
		final Validator beanValidator = CapCommonToolkit.beanValidator();
		if (beanValidator != null) {
			final BeanDescriptor constraintsForClass = beanValidator.getConstraintsForClass(beanType);
			if (constraintsForClass != null) {
				final PropertyDescriptor constraintsForProperty = constraintsForClass.getConstraintsForProperty(propertyName);
				if (constraintsForProperty != null) {
					return constraintsForProperty.hasConstraints();
				}
			}
		}
		return false;
	}

}
