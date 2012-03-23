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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.validation.IBeanValidator;

final class BeanDtoDescriptorImpl implements IBeanDtoDescriptor, Serializable {

	private static final long serialVersionUID = 4875055093925862277L;

	private final Class<?> beanType;
	private final String labelSingular;
	private final String labelPlural;
	private final String description;
	private final String renderingPattern;
	private final List<IProperty> unodifiableProperties;
	private final Set<IBeanValidator<?>> unmodifieableBeanValidators;

	@SuppressWarnings({"unchecked", "rawtypes"})
	BeanDtoDescriptorImpl(final Collection<IProperty> properties) {
		this(IBeanDto.class, null, null, null, properties, (Set) Collections.emptySet());
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	BeanDtoDescriptorImpl(
		final String labelSingular,
		final String labelPlural,
		final String description,
		final Collection<IProperty> properties) {
		this(IBeanDto.class, labelSingular, labelPlural, description, properties, (Set) Collections.emptySet());
	}

	BeanDtoDescriptorImpl(
		final Class<?> beanType,
		final String labelSingular,
		final String labelPlural,
		final String description,
		final Collection<IProperty> properties,
		final Collection<? extends IBeanValidator<?>> beanValidators) {
		this(beanType, labelSingular, labelPlural, description, null, properties, beanValidators);
	}

	BeanDtoDescriptorImpl(
		final Class<?> beanType,
		final String labelSingular,
		final String labelPlural,
		final String description,
		final String renderingPattern,
		final Collection<IProperty> properties,
		final Collection<? extends IBeanValidator<?>> beanValidators) {
		this.beanType = beanType;
		this.labelSingular = labelSingular;
		this.labelPlural = labelPlural;
		this.description = description;
		this.renderingPattern = renderingPattern;
		this.unodifiableProperties = Collections.unmodifiableList(new LinkedList<IProperty>(properties));
		this.unmodifieableBeanValidators = Collections.unmodifiableSet(new LinkedHashSet<IBeanValidator<?>>(beanValidators));
	}

	@Override
	public Class<?> getBeanType() {
		return beanType;
	}

	@Override
	public List<IProperty> getProperties() {
		return unodifiableProperties;
	}

	@Override
	public Set<IBeanValidator<?>> getValidators() {
		return unmodifieableBeanValidators;
	}

	@Override
	public String getLabelSingular() {
		return labelSingular;
	}

	@Override
	public String getLabelPlural() {
		return labelPlural;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getRenderingPattern() {
		return renderingPattern;
	}

}
