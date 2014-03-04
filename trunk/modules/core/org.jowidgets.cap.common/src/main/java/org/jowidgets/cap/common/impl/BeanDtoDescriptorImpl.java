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

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IBeanFormInfoDescriptor;
import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.tools.StaticMessage;

final class BeanDtoDescriptorImpl implements IBeanDtoDescriptor, Serializable {

	private static final long serialVersionUID = 4875055093925862277L;

	private final Object beanTypeId;
	private final Class<?> beanType;
	private final IMessage labelSingular;
	private final IMessage labelPlural;
	private final IMessage description;
	private final IMessage renderingPattern;
	private final Object iconDescriptor;
	private final Object createIconDescriptor;
	private final Object deleteIconDescriptor;
	private final Object createLinkIconDescriptor;
	private final Object deleteLinkIconDescriptor;
	private final List<IProperty> unodifiableProperties;
	private final Set<IBeanValidator<?>> unmodifieableBeanValidators;
	private final List<ISort> unmodifieableDefaultSorting;
	private final IBeanFormInfoDescriptor createFormInfo;
	private final IBeanFormInfoDescriptor editFormInfo;

	BeanDtoDescriptorImpl(
		final Object beanTypeId,
		final Class<?> beanType,
		final IMessage labelSingular,
		final IMessage labelPlural,
		final IMessage description,
		final IMessage renderingPattern,
		final Object iconDescriptor,
		final Object createIconDescriptor,
		final Object deleteIconDescriptor,
		final Object createLinkIconDescriptor,
		final Object deleteLinkIconDescriptor,
		final Collection<IProperty> properties,
		final Collection<ISort> defaultSorting,
		final IBeanFormInfoDescriptor createFormInfo,
		final IBeanFormInfoDescriptor editFormInfo,
		final Collection<? extends IBeanValidator<?>> beanValidators) {

		this.beanTypeId = beanTypeId;
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
		this.createFormInfo = createFormInfo;
		this.editFormInfo = editFormInfo;
		this.iconDescriptor = iconDescriptor;
		this.createIconDescriptor = createIconDescriptor;
		this.deleteIconDescriptor = deleteIconDescriptor;
		this.createLinkIconDescriptor = createLinkIconDescriptor;
		this.deleteLinkIconDescriptor = deleteLinkIconDescriptor;
		this.unodifiableProperties = Collections.unmodifiableList(new LinkedList<IProperty>(properties));
		this.unmodifieableDefaultSorting = Collections.unmodifiableList(new LinkedList<ISort>(defaultSorting));
		this.unmodifieableBeanValidators = Collections.unmodifiableSet(new LinkedHashSet<IBeanValidator<?>>(beanValidators));
	}

	@Override
	public Object getBeanTypeId() {
		return beanTypeId;
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
	public IBeanFormInfoDescriptor getCreateModeFormInfo() {
		return createFormInfo;
	}

	@Override
	public IBeanFormInfoDescriptor getEditModeFormInfo() {
		return editFormInfo;
	}

	@Override
	public IMessage getRenderingPattern() {
		return renderingPattern;
	}

	@Override
	public Object getIconDescriptor() {
		return iconDescriptor;
	}

	@Override
	public Object getCreateIconDescriptor() {
		return createIconDescriptor;
	}

	@Override
	public Object getDeleteIconDescriptor() {
		return deleteIconDescriptor;
	}

	@Override
	public Object getCreateLinkIconDescriptor() {
		return createLinkIconDescriptor;
	}

	@Override
	public Object getDeleteLinkIconDescriptor() {
		return deleteLinkIconDescriptor;
	}

}
