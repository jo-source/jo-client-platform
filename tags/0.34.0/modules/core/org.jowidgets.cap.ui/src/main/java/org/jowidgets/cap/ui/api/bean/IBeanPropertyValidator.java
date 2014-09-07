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

package org.jowidgets.cap.ui.api.bean;

import java.util.Collection;
import java.util.Set;

import org.jowidgets.cap.common.api.validation.IBeanValidationResult;

public interface IBeanPropertyValidator<BEAN_TYPE> {

	/**
	 * Validates a bean property.
	 * 
	 * @param bean The bean to validate
	 * @param propertyName The property to validate
	 * 
	 * @return The validation result.
	 */
	Collection<IBeanValidationResult> validateProperty(IBeanProxy<BEAN_TYPE> bean, String propertyName);

	/**
	 * Gets the properties, this validation depends on. If the result is null or empty, the
	 * validation depends on all properties of the given bean.
	 * Property dependencies could be used to reduce validation calculations.
	 * If no property dependencies are set, each time a property changes on the bean, all validations
	 * will be (re-)calculated, even if the validation calculation does not depend on the
	 * property change.
	 * 
	 * @return The properties, this validation depends on.
	 */
	Set<String> getPropertyDependencies();

	/**
	 * If a property validator is symmetric, the validateProperty method get's the same result
	 * for each dependent property if the bean is constant.
	 * 
	 * This is relevant for example for cross property validation.
	 * 
	 * Example: The BMI of a person should be validated. This depends on the persons 'weight' and
	 * 'height' properties. The dependent properties are [weight, height]. The validation is symmetric.
	 * 
	 * If the validator only depends on one property, the symetric flag is not relevant (its inherent symmetric).
	 * 
	 * If no cross property validation will be done, the validator is probably not symmetric.
	 * 
	 * Even though if more than one concern will be validated with one validator, it can probably not be symmetric.
	 * 
	 * Remark: To define a validator symmetric is an performance issue to avoid unneeded validations.
	 * If not sure, return false. This will work always correctly.
	 * 
	 * @return true if the validator is symmetric, false otherwise
	 */
	boolean isSymmetric();

}
