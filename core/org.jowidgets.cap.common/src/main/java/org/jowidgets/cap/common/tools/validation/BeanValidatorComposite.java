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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.validation.IBeanValidationResult;
import org.jowidgets.cap.common.api.validation.IBeanValidationResultListBuilder;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.util.Assert;

public final class BeanValidatorComposite<BEAN_TYPE> implements IBeanValidator<BEAN_TYPE> {

	private final Collection<IBeanValidator<BEAN_TYPE>> validators;
	private final Set<String> propertyDependencies;

	public BeanValidatorComposite(final Collection<IBeanValidator<BEAN_TYPE>> validators) {
		Assert.paramNotNull(validators, "validators");
		this.validators = new LinkedList<IBeanValidator<BEAN_TYPE>>(validators);
		this.propertyDependencies = createPropertyDependencies(validators);
	}

	private Set<String> createPropertyDependencies(final Collection<IBeanValidator<BEAN_TYPE>> validators) {
		final Set<String> result = new HashSet<String>();
		for (final IBeanValidator<BEAN_TYPE> validator : validators) {
			result.addAll(validator.getPropertyDependencies());
		}
		return Collections.unmodifiableSet(result);
	}

	@Override
	public Collection<IBeanValidationResult> validate(final BEAN_TYPE bean) {
		final IBeanValidationResultListBuilder builder = CapCommonToolkit.beanValidationResultListBuilder();
		for (final IBeanValidator<BEAN_TYPE> validator : validators) {
			builder.addResult(validator.validate(bean));
		}
		return builder.build();
	}

	@Override
	public Set<String> getPropertyDependencies() {
		return propertyDependencies;
	}

}
