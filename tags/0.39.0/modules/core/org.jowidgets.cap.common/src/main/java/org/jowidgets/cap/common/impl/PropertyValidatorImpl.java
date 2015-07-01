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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.util.EmptyCheck;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidationResultBuilder;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.ValidationResult;

@SuppressWarnings({"rawtypes", "unchecked"})
final class PropertyValidatorImpl implements IValidator<Object>, Serializable {

	private static final long serialVersionUID = 5661213393690831021L;

	private final Class<?> valueType;
	private final List<IValidator> elementTypeValidators;
	private final List<IValidator> validators;

	PropertyValidatorImpl(
		final Class<?> valueType,
		final List<IValidator<? extends Object>> elementTypeValidators,
		final List<IValidator<? extends Object>> validators) {
		this.valueType = valueType;
		this.validators = new LinkedList<IValidator>(validators);
		this.elementTypeValidators = new LinkedList<IValidator>();
		if (Collection.class.isAssignableFrom(valueType)) {
			this.elementTypeValidators.addAll(elementTypeValidators);
		}
		else {
			this.validators.addAll(elementTypeValidators);
		}
	}

	@Override
	public IValidationResult validate(final Object value) {
		final IValidationResultBuilder builder = ValidationResult.builder();
		if (Collection.class.isAssignableFrom(valueType) && !EmptyCheck.isEmpty(value) && value instanceof Collection) {
			for (final Object elementValue : (Collection<Object>) value) {
				for (final IValidator<Object> elementTypeValidator : elementTypeValidators) {
					final IValidationResult validationResult = elementTypeValidator.validate(elementValue);
					if (!validationResult.isValid()) {
						return validationResult;
					}
					else {
						builder.addResult(validationResult);
					}
				}
			}
		}

		for (final IValidator<Object> validator : validators) {
			final IValidationResult validationResult = validator.validate(value);
			if (!validationResult.isValid()) {
				return validationResult;
			}
			else {
				builder.addResult(validationResult);
			}
		}

		return builder.build();
	}

	@Override
	public String toString() {
		return "PropertyValidatorImpl [valueType="
			+ valueType
			+ ", elementTypeValidators="
			+ elementTypeValidators
			+ ", validators="
			+ validators
			+ "]";
	}

}
