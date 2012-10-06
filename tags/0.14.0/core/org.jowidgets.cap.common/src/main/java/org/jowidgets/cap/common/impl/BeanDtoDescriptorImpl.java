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
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.tools.StaticMessage;

final class BeanDtoDescriptorImpl implements IBeanDtoDescriptor, Serializable {

	private static final long serialVersionUID = 4875055093925862277L;

	private static final List<ISort> EMPTY_SORT = Collections.emptyList();

	private final Class<?> beanType;
	private final IMessage labelSingular;
	private final IMessage labelPlural;
	private final IMessage description;
	private final IMessage renderingPattern;
	private final List<IProperty> unodifiableProperties;
	private final Set<IBeanValidator<?>> unmodifieableBeanValidators;
	private final List<ISort> unmodifieableDefaultSorting;

	@SuppressWarnings({"unchecked", "rawtypes"})
	BeanDtoDescriptorImpl(final Collection<IProperty> properties) {
		this(
			IBeanDto.class,
			new StaticMessage(null),
			new StaticMessage(null),
			new StaticMessage(null),
			properties,
			(Set) Collections.emptySet());
	}

	BeanDtoDescriptorImpl(
		final String labelSingular,
		final String labelPlural,
		final String description,
		final Collection<IProperty> properties) {
		this(new StaticMessage(labelSingular), new StaticMessage(labelPlural), new StaticMessage(description), properties);
	}

	BeanDtoDescriptorImpl(
		final Class<?> beanType,
		final String labelSingular,
		final String labelPlural,
		final String description,
		final Collection<IProperty> properties,
		final Collection<? extends IBeanValidator<?>> beanValidators) {
		this(
			beanType,
			new StaticMessage(labelSingular),
			new StaticMessage(labelPlural),
			new StaticMessage(description),
			properties,
			beanValidators);
	}

	BeanDtoDescriptorImpl(
		final Class<?> beanType,
		final String labelSingular,
		final String labelPlural,
		final String description,
		final String renderingPattern,
		final Collection<IProperty> properties,
		final Collection<? extends IBeanValidator<?>> beanValidators) {
		this(
			beanType,
			new StaticMessage(labelSingular),
			new StaticMessage(labelPlural),
			new StaticMessage(description),
			new StaticMessage(renderingPattern),
			properties,
			beanValidators);
	}

	BeanDtoDescriptorImpl(
		final Class<?> beanType,
		final String labelSingular,
		final String labelPlural,
		final String description,
		final String renderingPattern,
		final Collection<IProperty> properties,
		final Collection<ISort> defaultSorting,
		final Collection<? extends IBeanValidator<?>> beanValidators) {
		this(
			beanType,
			new StaticMessage(labelSingular),
			new StaticMessage(labelPlural),
			new StaticMessage(description),
			new StaticMessage(renderingPattern),
			properties,
			defaultSorting,
			beanValidators);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	BeanDtoDescriptorImpl(
		final IMessage labelSingular,
		final IMessage labelPlural,
		final IMessage description,
		final Collection<IProperty> properties) {
		this(IBeanDto.class, labelSingular, labelPlural, description, properties, (Set) Collections.emptySet());
	}

	BeanDtoDescriptorImpl(
		final Class<?> beanType,
		final IMessage labelSingular,
		final IMessage labelPlural,
		final IMessage description,
		final Collection<IProperty> properties,
		final Collection<? extends IBeanValidator<?>> beanValidators) {
		this(beanType, labelSingular, labelPlural, description, null, properties, beanValidators);
	}

	BeanDtoDescriptorImpl(
		final Class<?> beanType,
		final IMessage labelSingular,
		final IMessage labelPlural,
		final IMessage description,
		final IMessage renderingPattern,
		final Collection<IProperty> properties,
		final Collection<? extends IBeanValidator<?>> beanValidators) {
		this(beanType, labelSingular, labelPlural, description, renderingPattern, properties, EMPTY_SORT, beanValidators);
	}

	BeanDtoDescriptorImpl(
		final Class<?> beanType,
		final IMessage labelSingular,
		final IMessage labelPlural,
		final IMessage description,
		final IMessage renderingPattern,
		final Collection<IProperty> properties,
		final Collection<ISort> defaultSorting,
		final Collection<? extends IBeanValidator<?>> beanValidators) {
		this.beanType = beanType;
		if (labelSingular != null) {
			this.labelSingular = labelSingular;
		}
		else {
			this.labelSingular = new StaticMessage();
		}
		if (labelPlural != null) {
			this.labelPlural = labelPlural;
		}
		else {
			this.labelPlural = new StaticMessage();
		}
		if (description != null) {
			this.description = description;
		}
		else {
			this.description = new StaticMessage();
		}
		if (renderingPattern != null) {
			this.renderingPattern = renderingPattern;
		}
		else {
			this.renderingPattern = new StaticMessage();
		}
		this.unodifiableProperties = Collections.unmodifiableList(new LinkedList<IProperty>(properties));
		this.unmodifieableDefaultSorting = Collections.unmodifiableList(new LinkedList<ISort>(defaultSorting));
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
	public List<ISort> getDefaultSorting() {
		return unmodifieableDefaultSorting;
	}

	@Override
	public Set<IBeanValidator<?>> getValidators() {
		return unmodifieableBeanValidators;
	}

	@Override
	public IMessage getLabelSingular() {
		return labelSingular;
	}

	@Override
	public IMessage getLabelPlural() {
		return labelPlural;
	}

	@Override
	public IMessage getDescription() {
		return description;
	}

	@Override
	public IMessage getRenderingPattern() {
		return renderingPattern;
	}

}
