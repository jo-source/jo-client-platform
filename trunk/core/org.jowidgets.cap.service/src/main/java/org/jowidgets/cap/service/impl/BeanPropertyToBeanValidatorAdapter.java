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

package org.jowidgets.cap.service.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.validation.IBeanValidationResult;
import org.jowidgets.cap.common.api.validation.IBeanValidationResultListBuilder;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidator;

final class BeanPropertyToBeanValidatorAdapter<BEAN_TYPE> implements IBeanValidator<BEAN_TYPE> {

	private final List<IValidator<? extends Object>> propertyValidators;
	private final String propertyName;
	private final Set<String> propertyDependecies;
	private final Method readMethod;

	public BeanPropertyToBeanValidatorAdapter(
		final Class<? extends BEAN_TYPE> beanType,
		final String propertyName,
		final Collection<IValidator<? extends Object>> propertyValidators) {
		Assert.paramNotEmpty(propertyName, "propertyName");
		Assert.paramNotNull(propertyValidators, "propertyValidators");
		this.propertyValidators = new LinkedList<IValidator<? extends Object>>(propertyValidators);
		this.propertyName = propertyName;
		this.propertyDependecies = Collections.singleton(propertyName);
		this.readMethod = getReadMethod(beanType, propertyName);
	}

	private static Method getReadMethod(final Class<?> beanType, final String propertyName) {
		try {
			final BeanInfo beanInfo = Introspector.getBeanInfo(beanType);
			for (final PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
				final String decriptorPropertyName = propertyDescriptor.getName();
				if (decriptorPropertyName.equals(propertyName)) {
					return propertyDescriptor.getReadMethod();
				}
			}
		}
		catch (final IntrospectionException e) {
			throw new RuntimeException(e);
		}
		throw new IllegalArgumentException("Could not find property '" + propertyName + "' for bean type '" + beanType + "'");
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public Collection<IBeanValidationResult> validate(final BEAN_TYPE bean) {
		final IBeanValidationResultListBuilder builder = CapCommonToolkit.beanValidationResultListBuilder();
		if (bean != null) {
			Object propertyValue;
			try {
				propertyValue = readMethod.invoke(bean);
			}
			catch (final Exception e) {
				throw new RuntimeException(e);
			}
			for (final IValidator validator : propertyValidators) {
				final IValidationResult validationResult = validator.validate(propertyValue);
				if (!validationResult.isValid()) {
					builder.addResult(validationResult, propertyName);
				}
			}
		}
		return builder.build();
	}

	@Override
	public Set<String> getPropertyDependencies() {
		return propertyDependecies;
	}

}
