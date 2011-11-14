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

import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanValidator;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.ValidationResult;

final class BeanValidatorImpl<BEAN_TYPE> implements IBeanValidator<BEAN_TYPE> {

	private final Map<String, IAttribute<?>> attributes;

	BeanValidatorImpl(final Collection<? extends IAttribute<?>> attributes) {
		this.attributes = new HashMap<String, IAttribute<?>>();
		for (final IAttribute<?> attribute : attributes) {
			this.attributes.put(attribute.getPropertyName(), attribute);
		}
	}

	@Override
	public IValidationResult validate(final IBeanProxy<BEAN_TYPE> bean) {
		// TODO MG bean validators must be considered
		return ValidationResult.ok();
	}

	@Override
	public IValidationResult validateProperty(final String propertyName, final Object value) {
		final IAttribute<?> attribute = attributes.get(propertyName);
		if (attribute != null) {
			final IValidator<Object> validator = attribute.getValidator();
			if (validator != null) {
				return validator.validate(value).withContext(attribute.getCurrentLabel());
			}
		}
		return ValidationResult.ok();
	}

}
