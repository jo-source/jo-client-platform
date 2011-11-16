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

package org.jowidgets.cap.common.tools.validation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.validation.IBeanValidationResult;
import org.jowidgets.cap.common.api.validation.IBeanValidationResultListBuilder;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.MessageType;

public abstract class AbstractSingleConcernBeanValidator<BEAN_TYPE> implements IBeanValidator<BEAN_TYPE>, Serializable {

	private static final long serialVersionUID = -1186654852604095665L;

	private final Set<String> properties;
	private final boolean madatory;

	public AbstractSingleConcernBeanValidator(final String... properties) {
		this(false, Arrays.asList(properties));
	}

	public AbstractSingleConcernBeanValidator(final Collection<String> properties) {
		this(false, properties);
	}

	public AbstractSingleConcernBeanValidator(final boolean mandatory, final String... properties) {
		this(mandatory, Arrays.asList(properties));
	}

	public AbstractSingleConcernBeanValidator(final boolean mandatory, final Collection<String> properties) {
		this.properties = Collections.unmodifiableSet(new LinkedHashSet<String>(properties));
		this.madatory = mandatory;
	}

	public abstract IValidationResult validateBean(BEAN_TYPE bean);

	@Override
	public final Collection<IBeanValidationResult> validate(final BEAN_TYPE bean) {

		final IBeanValidationResultListBuilder builder = CapCommonToolkit.beanValidationResultListBuilder();
		if (madatory || bean != null) {
			final IValidationResult validationResult = validateBean(bean);
			if (MessageType.OK != validationResult.getWorstFirst().getType()) {
				builder.addResult(validationResult, properties);
			}
		}
		return builder.build();
	}

	@Override
	public final Set<String> getPropertyDependencies() {
		return properties;
	}

}
