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

package org.jowidgets.cap.common.tools.validation;

import java.util.Collection;

import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidationResultBuilder;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.ValidationResult;

public final class ElementTypeValidationDecorator<VALUE_TYPE> implements IValidator<VALUE_TYPE> {

	private final IValidator<VALUE_TYPE> elementTypeValidator;

	public ElementTypeValidationDecorator(final IValidator<VALUE_TYPE> elementTypeValidator) {
		Assert.paramNotNull(elementTypeValidator, "elementTypeValidator");
		this.elementTypeValidator = elementTypeValidator;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IValidationResult validate(final VALUE_TYPE value) {
		if (value instanceof Collection<?>) {
			final IValidationResultBuilder builder = ValidationResult.builder();
			for (final Object elementValue : (Collection<?>) value) {
				final IValidationResult elementResult = elementTypeValidator.validate((VALUE_TYPE) elementValue);
				if (!elementResult.isOk()) {
					builder.addResult(elementResult);
				}
			}
			return builder.build();
		}
		else {
			return elementTypeValidator.validate(value);
		}
	}
}
