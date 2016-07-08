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

package org.jowidgets.cap.ui.api.bean;

import java.util.Collection;
import java.util.Map;

import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.ui.api.attribute.IAttributeSet;

public interface IBeanProxyFactoryBuilder<BEAN_TYPE> {

	/**
	 * Sets the bean type id to use for the created beans.
	 * 
	 * @param beanTypeId The bean type id to set
	 * 
	 * @return This builder
	 */
	IBeanProxyFactoryBuilder<BEAN_TYPE> setBeanTypeId(Object beanTypeId);

	/**
	 * Sets the attributes that will be used for the create bean proxies
	 * 
	 * @param attributes The attributes to use
	 */
	IBeanProxyFactoryBuilder<BEAN_TYPE> setAttributes(IAttributeSet attributes);

	/**
	 * Adds a bean property validator that will be added to the created beans
	 * 
	 * @param validator The validator to add
	 */
	IBeanProxyFactoryBuilder<BEAN_TYPE> addBeanPropertyValidator(IBeanPropertyValidator<BEAN_TYPE> validator);

	/**
	 * Adds bean property validators that will be added to the created beans
	 * 
	 * @param validators The validators to add
	 */
	IBeanProxyFactoryBuilder<BEAN_TYPE> addBeanPropertyValidators(
		Collection<? extends IBeanPropertyValidator<BEAN_TYPE>> validators);

	/**
	 * Sets bean property validators that will be added to the created beans
	 * 
	 * Remark: This be remove all already set validators
	 * 
	 * @param validators The validators to add
	 */
	IBeanProxyFactoryBuilder<BEAN_TYPE> setBeanPropertyValidators(
		Collection<? extends IBeanPropertyValidator<BEAN_TYPE>> validators);

	/**
	 * Sets the default values that should be used for transient bean proxies
	 * 
	 * @param defaultValues The default values to use
	 */
	IBeanProxyFactoryBuilder<BEAN_TYPE> setDefaultValuesForTransientBeans(Map<String, Object> defaultValues);

	/**
	 * Sets the default values that should be used for transient bean proxies created from the given attributes
	 * 
	 * @param defaultValues The attributes to use
	 */
	IBeanProxyFactoryBuilder<BEAN_TYPE> setDefaultValuesForTransientBeans(IAttributeSet attributes);

	/**
	 * Sets the validate unmodified beans flag.
	 * 
	 * If set to true, beans will be validated even if they are unmodified
	 * 
	 * @param validateUnmodified The flag to set
	 * 
	 * @return This builder
	 */
	IBeanProxyFactoryBuilder<BEAN_TYPE> setValidateUnmodifiedBeans(boolean validateUnmodified);

	/**
	 * Adds a bean property validator created from the given attributes
	 * 
	 * @param attributes The attributes to create the validators from
	 */
	IBeanProxyFactoryBuilder<BEAN_TYPE> addBeanPropertyValidator(IAttributeSet attributes);

	/**
	 * Adds a bean property validator adapted from an {@link IBeanValidator}
	 * 
	 * @param beanValidator The bean validator to add
	 */
	IBeanProxyFactoryBuilder<BEAN_TYPE> addBeanPropertyValidator(IBeanValidator<BEAN_TYPE> beanValidator);

	/**
	 * Adds bean property validators adapted from a collection of {@link IBeanValidator}
	 * 
	 * @param beanValidators The bean validators to add
	 */
	IBeanProxyFactoryBuilder<BEAN_TYPE> addBeanPropertyValidator(Collection<? extends IBeanValidator<BEAN_TYPE>> beanValidators);

	/**
	 * This configures the builder from the entity service.
	 * 
	 * If the entity is found, the following will be set:
	 * 
	 * beanTypeId
	 * attributes
	 * beanPropertyValidators
	 * defaultValues for transient beans
	 * 
	 * If the entity is not found, an {@link IllegalArgumentException} is thrown
	 * 
	 * @param entityId The entity id to use
	 * 
	 * @return This builder
	 * 
	 * @throws IllegalArgumentException
	 */
	IBeanProxyFactoryBuilder<BEAN_TYPE> configureFromEntityService(Object entityId);

	/**
	 * Builds a new bean proxy factory
	 * 
	 * @return The new bean proxy factory
	 */
	IBeanProxyFactory<BEAN_TYPE> build();

}
