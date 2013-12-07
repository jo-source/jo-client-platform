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

package org.jowidgets.cap.common.api;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.validation.Validator;

import org.jowidgets.cap.common.api.bean.IBeanDataBuilder;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanDtoBuilder;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptorBuilder;
import org.jowidgets.cap.common.api.bean.IBeanFormInfoDescriptorBuilder;
import org.jowidgets.cap.common.api.bean.IBeanKeyBuilder;
import org.jowidgets.cap.common.api.bean.IBeanModificationBuilder;
import org.jowidgets.cap.common.api.bean.IBeanPropertyBuilder;
import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.bean.IPropertyBuilder;
import org.jowidgets.cap.common.api.bean.IPropertyValidatorBuilder;
import org.jowidgets.cap.common.api.bean.IStaticValueRangeFactory;
import org.jowidgets.cap.common.api.entity.IEntityApplicationNodeBuilder;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptorBuilder;
import org.jowidgets.cap.common.api.entity.IEntityLinkPropertiesBuilder;
import org.jowidgets.cap.common.api.execution.IExecutableCheckerCompositeBuilder;
import org.jowidgets.cap.common.api.filter.IBeanDtoFilter;
import org.jowidgets.cap.common.api.filter.IFilterFactory;
import org.jowidgets.cap.common.api.link.ILinkCreationBuilder;
import org.jowidgets.cap.common.api.link.ILinkDeletionBuilder;
import org.jowidgets.cap.common.api.lookup.ILookUpToolkit;
import org.jowidgets.cap.common.api.service.IBeanServicesProviderFactory;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.IEntityServiceCompositeBuilder;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.sort.ISortFactory;
import org.jowidgets.cap.common.api.validation.IBeanValidationResultListBuilder;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.common.impl.DefaultCapCommonToolkit;
import org.jowidgets.service.api.IRedundantServiceResolver;

public final class CapCommonToolkit {

	private static ICapCommonToolkit instance;

	private CapCommonToolkit() {}

	public static ICapCommonToolkit getInstance() {
		if (instance == null) {
			instance = new DefaultCapCommonToolkit();
		}
		return instance;
	}

	public static IEntityApplicationNodeBuilder entityApplicationNodeBuilder() {
		return getInstance().entityApplicationNodeBuilder();
	}

	public static IEntityLinkPropertiesBuilder entityLinkPropertiesBuilder() {
		return getInstance().entityLinkPropertiesBuilder();
	}

	public static IEntityLinkDescriptorBuilder entityLinkDescriptorBuilder() {
		return getInstance().entityLinkDescriptorBuilder();
	}

	public static IBeanFormInfoDescriptorBuilder beanFormInfoDescriptorBuilder() {
		return getInstance().beanFormInfoDescriptorBuilder();
	}

	public static IPropertyBuilder propertyBuilder() {
		return getInstance().propertyBuilder();
	}

	public static IBeanPropertyBuilder beanPropertyBuilder(final Class<?> beanType, final String propertyName) {
		return getInstance().beanPropertyBuilder(beanType, propertyName);
	}

	public static ILinkCreationBuilder linkCreationBuilder() {
		return getInstance().linkCreationBuilder();
	}

	public static ILinkDeletionBuilder linkDeletionBuilder() {
		return getInstance().linkDeletionBuilder();
	}

	public static IStaticValueRangeFactory staticValueRangeFactory() {
		return getInstance().staticValueRangeFactory();
	}

	public static IBeanDtoDescriptorBuilder dtoDescriptorBuilder(final Class<?> beanType) {
		return getInstance().dtoDescriptorBuilder(beanType);
	}

	public static IBeanDtoDescriptorBuilder dtoDescriptorBuilder() {
		return getInstance().dtoDescriptorBuilder();
	}

	public static IBeanDtoDescriptor dtoDescriptor(final List<IProperty> properties) {
		return getInstance().dtoDescriptor(properties);
	}

	public static IBeanDtoDescriptor dtoDescriptor(
		final Collection<IProperty> properties,
		final String labelSingular,
		final String labelPlural) {
		return getInstance().dtoDescriptor(properties, labelSingular, labelPlural);
	}

	public static IBeanDtoDescriptor dtoDescriptor(
		final Collection<IProperty> properties,
		final String labelSingular,
		final String labelPlural,
		final String description) {
		return getInstance().dtoDescriptor(properties, labelSingular, labelPlural, description);
	}

	public static IBeanDtoDescriptor dtoDescriptor(
		final Collection<IProperty> properties,
		final String labelSingular,
		final String labelPlural,
		final String description,
		final Collection<? extends IBeanValidator<?>> validators) {
		return getInstance().dtoDescriptor(properties, labelSingular, labelPlural, description, validators);
	}

	public static IBeanDtoDescriptor dtoDescriptor(
		final Collection<IProperty> properties,
		final String labelSingular,
		final String labelPlural,
		final String description,
		final String renderingPattern,
		final Collection<? extends IBeanValidator<?>> validators) {
		return getInstance().dtoDescriptor(properties, labelSingular, labelPlural, description, renderingPattern, validators);
	}

	public static IBeanDtoDescriptor dtoDescriptor(
		final Collection<IProperty> properties,
		final Collection<ISort> defaultSorting,
		final String labelSingular,
		final String labelPlural,
		final String description,
		final String renderingPattern,
		final Collection<? extends IBeanValidator<?>> beanValidators) {
		return getInstance().dtoDescriptor(
				properties,
				defaultSorting,
				labelSingular,
				labelPlural,
				description,
				renderingPattern,
				beanValidators);
	}

	public static IBeanServicesProviderFactory beanServicesProviderFactory() {
		return getInstance().beanServicesProviderFactory();
	}

	public static IBeanDtoBuilder dtoBuilder(final Object beanTypeId) {
		return getInstance().dtoBuilder(beanTypeId);
	}

	public static IBeanDataBuilder beanDataBuilder() {
		return getInstance().beanDataBuilder();
	}

	public static IBeanKeyBuilder beanKeyBuilder() {
		return getInstance().beanKeyBuilder();
	}

	public static IBeanModificationBuilder beanModificationBuilder() {
		return getInstance().beanModificationBuilder();
	}

	public static ISortFactory sortFactory() {
		return getInstance().sortFactory();
	}

	public static IFilterFactory filterFactory() {
		return getInstance().filterFactory();
	}

	public static IBeanDtoFilter beanDtoFilter() {
		return getInstance().beanDtoFilter();
	}

	public static Comparator<IBeanDto> beanDtoComparator(final Collection<? extends ISort> sorting) {
		return getInstance().beanDtoComparator(sorting);
	}

	public static ILookUpToolkit lookUpToolkit() {
		return getInstance().lookUpToolkit();
	}

	public static IPropertyValidatorBuilder propertyValidatorBuilder() {
		return getInstance().propertyValidatorBuilder();
	}

	public static Validator beanValidator() {
		return getInstance().beanValidator();
	}

	public static IBeanValidationResultListBuilder beanValidationResultListBuilder() {
		return getInstance().beanValidationResultListBuilder();
	}

	public static <BEAN_TYPE> IExecutableCheckerCompositeBuilder<BEAN_TYPE> executableCheckerCompositeBuilder() {
		return getInstance().executableCheckerCompositeBuilder();
	}

	public static IEntityServiceCompositeBuilder entityServiceCompositeBuilder() {
		return getInstance().entityServiceCompositeBuilder();
	}

	public static IRedundantServiceResolver<IEntityService> entityServiceResolver() {
		return getInstance().entityServiceResolver();
	}
}
