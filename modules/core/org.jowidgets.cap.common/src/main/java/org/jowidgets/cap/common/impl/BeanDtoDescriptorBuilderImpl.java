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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jowidgets.cap.common.api.bean.BeanFormInfoDescriptor;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptorBuilder;
import org.jowidgets.cap.common.api.bean.IBeanFormInfoDescriptor;
import org.jowidgets.cap.common.api.bean.IBeanPropertyBluePrint;
import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.common.tools.annotation.ValidatorAnnotationUtil;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.tools.StaticMessage;
import org.jowidgets.util.Assert;

final class BeanDtoDescriptorBuilderImpl implements IBeanDtoDescriptorBuilder {

	private final Object beanTypeId;
	private final Class<?> beanType;
	private final List<BeanPropertyBluePrintImpl> bluePrints;
	private final List<IProperty> properties;
	private final List<ISort> defaultSorting;
	private final Set<IBeanValidator<?>> beanValidators;

	private IMessage labelSingular;
	private IMessage labelPlural;
	private IMessage description;
	private IMessage renderingPattern;
	private IBeanFormInfoDescriptor createFormInfo;
	private IBeanFormInfoDescriptor editFormInfo;
	private Object iconDescriptor;
	private Object createIconDescriptor;
	private Object deleteIconDescriptor;
	private Object createLinkIconDescriptor;
	private Object deleteLinkIconDescriptor;

	BeanDtoDescriptorBuilderImpl() {
		this(IBeanDto.class);
	}

	BeanDtoDescriptorBuilderImpl(final Object beanTypeId) {
		this(beanTypeId, IBeanDto.class);
	}

	BeanDtoDescriptorBuilderImpl(final Class<?> beanType) {
		this(beanType.getName(), beanType);
	}

	BeanDtoDescriptorBuilderImpl(final Object beanTypeId, final Class<?> beanType) {
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(beanType, "beanType");

		this.beanTypeId = beanTypeId;
		this.beanType = beanType;
		this.bluePrints = new LinkedList<BeanPropertyBluePrintImpl>();
		this.properties = new LinkedList<IProperty>();
		this.beanValidators = new LinkedHashSet<IBeanValidator<?>>();
		this.defaultSorting = new LinkedList<ISort>();

		beanValidators.addAll(ValidatorAnnotationUtil.getBeanValidators(beanType));
	}

	@Override
	public IBeanDtoDescriptorBuilder setLabelSingular(final IMessage label) {
		this.labelSingular = label;
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setLabelPlural(final IMessage label) {
		this.labelPlural = label;
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setDescription(final IMessage description) {
		this.description = description;
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setRenderingPattern(final IMessage pattern) {
		this.renderingPattern = pattern;
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setLabelSingular(final String label) {
		this.labelSingular = new StaticMessage(label);
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setLabelPlural(final String label) {
		this.labelPlural = new StaticMessage(label);
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setDescription(final String description) {
		this.description = new StaticMessage(description);
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setRenderingPattern(final String renderingPattern) {
		this.renderingPattern = new StaticMessage(renderingPattern);
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setCreateFormInfo(final IBeanFormInfoDescriptor info) {
		this.createFormInfo = info;
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setCreateFormInfo(final IMessage infoText) {
		return setCreateFormInfo(BeanFormInfoDescriptor.create(infoText));
	}

	@Override
	public IBeanDtoDescriptorBuilder setCreateFormInfo(final String infoText) {
		return setCreateFormInfo(BeanFormInfoDescriptor.create(infoText));
	}

	@Override
	public IBeanDtoDescriptorBuilder setEditFormInfo(final IBeanFormInfoDescriptor info) {
		this.editFormInfo = info;
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setEditFormInfo(final IMessage infoText) {
		return setEditFormInfo(BeanFormInfoDescriptor.create(infoText));
	}

	@Override
	public IBeanDtoDescriptorBuilder setEditFormInfo(final String infoText) {
		return setEditFormInfo(BeanFormInfoDescriptor.create(infoText));
	}

	@Override
	public IBeanDtoDescriptorBuilder setFormInfo(final IBeanFormInfoDescriptor info) {
		setCreateFormInfo(info);
		setEditFormInfo(info);
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setFormInfo(final IMessage infoText) {
		setCreateFormInfo(infoText);
		setEditFormInfo(infoText);
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setFormInfo(final String infoText) {
		setCreateFormInfo(infoText);
		setEditFormInfo(infoText);
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setIconDescriptor(final Object iconDescriptor) {
		this.iconDescriptor = iconDescriptor;
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setCreateIconDescriptor(final Object iconDescriptor) {
		this.createIconDescriptor = iconDescriptor;
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setDeleteIconDescriptor(final Object iconDescriptor) {
		this.deleteIconDescriptor = iconDescriptor;
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setCreateLinkIconDescriptor(final Object iconDescriptor) {
		this.createLinkIconDescriptor = iconDescriptor;
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setDeleteLinkIconDescriptor(final Object iconDescriptor) {
		this.deleteLinkIconDescriptor = iconDescriptor;
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setDefaultSorting(final ISort... defaultSorting) {
		Assert.paramNotNull(defaultSorting, "defaultSorting");
		setDefaultSorting(Arrays.asList(defaultSorting));
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setDefaultSorting(final Collection<ISort> defaultSorting) {
		Assert.paramNotNull(defaultSorting, "defaultSorting");
		this.defaultSorting.clear();
		this.defaultSorting.addAll(defaultSorting);
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder addValidator(final IBeanValidator<?> validator) {
		Assert.paramNotNull(validator, "validator");
		beanValidators.add(validator);
		return this;
	}

	@Override
	public IBeanDtoDescriptorBuilder setValidators(final Collection<? extends IBeanValidator<?>> validators) {
		Assert.paramNotNull(validators, "validators");
		this.beanValidators.clear();
		this.beanValidators.addAll(validators);
		return this;
	}

	@Override
	public IBeanPropertyBluePrint addProperty(final String propertyName) {
		Assert.paramNotEmpty(propertyName, "propertyName");
		if (!properties.isEmpty()) {
			throw new IllegalStateException("After properties was set with setProperties(), no properties can be added. "
				+ "Feel free to create a patch that fixes this issue.");
		}
		if (IBeanDto.class.isAssignableFrom(beanType)) {
			throw new IllegalStateException("Bean property blueprints can not be used for generic bean type '"
				+ IBeanDto.class
				+ "'. Uses setProperties instead");
		}
		final BeanPropertyBluePrintImpl bluePrint = new BeanPropertyBluePrintImpl(beanType, propertyName);
		bluePrints.add(bluePrint);
		return bluePrint;
	}

	@Override
	public IBeanDtoDescriptorBuilder setProperties(final Collection<? extends IProperty> properties) {
		Assert.paramNotNull(properties, "properties");
		bluePrints.clear();
		this.properties.clear();
		this.properties.addAll(properties);
		return this;
	}

	@Override
	public IBeanDtoDescriptor build() {
		final List<IProperty> props;
		if (!bluePrints.isEmpty()) {
			props = new LinkedList<IProperty>();
			for (final BeanPropertyBluePrintImpl bluePrint : bluePrints) {
				props.add(bluePrint.build());
			}
		}
		else {
			props = properties;
		}

		return new BeanDtoDescriptorImpl(
			beanTypeId,
			beanType,
			labelSingular,
			labelPlural,
			description,
			renderingPattern,
			iconDescriptor,
			createIconDescriptor,
			deleteIconDescriptor,
			createLinkIconDescriptor,
			deleteLinkIconDescriptor,
			props,
			defaultSorting,
			createFormInfo,
			editFormInfo,
			beanValidators);
	}

}
