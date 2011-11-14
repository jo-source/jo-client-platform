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

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IPropertyValidatorBuilder;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidator;

final class PropertyValidatorBuilder implements IPropertyValidatorBuilder {

	private final List<IValidator<? extends Object>> elementTypeValidators;
	private final List<IValidator<? extends Object>> validators;

	PropertyValidatorBuilder() {
		this.elementTypeValidators = new LinkedList<IValidator<? extends Object>>();
		this.validators = new LinkedList<IValidator<? extends Object>>();
	}

	@Override
	public IPropertyValidatorBuilder addValidator(final IValidator<? extends Object> validator) {
		validators.add(validator);
		return this;
	}

	@Override
	public IPropertyValidatorBuilder addElementTypeValidator(final IValidator<? extends Object> validator) {
		elementTypeValidators.add(validator);
		return this;
	}

	@Override
	public IPropertyValidatorBuilder addBeanValidator(final Class<?> beanType, final String propertyName) {
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(propertyName, "propertyName");
		validators.add(new BeanPropertyValidatorAdapter(beanType, propertyName));
		return this;
	}

	@Override
	public IValidator<Object> build(final Class<?> propertyValueType) {
		return new PropertyValidatorImpl(propertyValueType, elementTypeValidators, validators);
	}

}
