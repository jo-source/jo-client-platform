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

import javax.validation.Validator;

import org.jowidgets.cap.common.api.bean.IBeanDataBuilder;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanDtoBuilder;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptorBuilder;
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
import org.jowidgets.cap.common.api.link.ILinkDataBuilder;
import org.jowidgets.cap.common.api.link.ILinkDeletionBuilder;
import org.jowidgets.cap.common.api.lookup.ILookUpToolkit;
import org.jowidgets.cap.common.api.service.IBeanServicesProviderFactory;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.IEntityServiceCompositeBuilder;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.sort.ISortFactory;
import org.jowidgets.cap.common.api.validation.IBeanValidationResultListBuilder;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.service.api.IRedundantServiceResolver;

public interface ICapCommonToolkit {

	IEntityApplicationNodeBuilder entityApplicationNodeBuilder();

	IEntityLinkPropertiesBuilder entityLinkPropertiesBuilder();

	IEntityLinkDescriptorBuilder entityLinkDescriptorBuilder();

	IPropertyBuilder propertyBuilder();

	IBeanPropertyBuilder beanPropertyBuilder(Class<?> beanType, String propertyName);

	IStaticValueRangeFactory staticValueRangeFactory();

	ILinkDataBuilder linkDataBuilder();

	ILinkDeletionBuilder linkDeletionBuilder();

	IBeanDtoDescriptorBuilder dtoDescriptorBuilder(Class<?> beanType);

	//TODO MG allow to build generic / untyped descriptors with help of a builder to avoid n*m factory methods 
	IBeanDtoDescriptor dtoDescriptor(Collection<IProperty> properties);

	IBeanDtoDescriptor dtoDescriptor(Collection<IProperty> properties, String labelSingular, String labelPlural);

	IBeanDtoDescriptor dtoDescriptor(
		Collection<IProperty> properties,
		String labelSingular,
		String labelPlural,
		String description);

	IBeanDtoDescriptor dtoDescriptor(
		Collection<IProperty> properties,
		String labelSingular,
		String labelPlural,
		String description,
		Collection<? extends IBeanValidator<?>> validators);

	IBeanDtoDescriptor dtoDescriptor(
		Collection<IProperty> properties,
		String labelSingular,
		String labelPlural,
		String description,
		String renderingPattern,
		Collection<? extends IBeanValidator<?>> beanValidators);

	IBeanDtoDescriptor dtoDescriptor(
		Collection<IProperty> properties,
		Collection<ISort> defaultSorting,
		String labelSingular,
		String labelPlural,
		String description,
		String renderingPattern,
		Collection<? extends IBeanValidator<?>> beanValidators);

	IBeanServicesProviderFactory beanServicesProviderFactory();

	IBeanDtoBuilder dtoBuilder(Object beanTypeId);

	IBeanDataBuilder beanDataBuilder();

	IBeanKeyBuilder beanKeyBuilder();

	IBeanModificationBuilder beanModificationBuilder();

	ISortFactory sortFactory();

	IFilterFactory filterFactory();

	IBeanDtoFilter beanDtoFilter();

	Comparator<IBeanDto> beanDtoComparator(Collection<? extends ISort> sorting);

	ILookUpToolkit lookUpToolkit();

	IPropertyValidatorBuilder propertyValidatorBuilder();

	Validator beanValidator();

	IBeanValidationResultListBuilder beanValidationResultListBuilder();

	<BEAN_TYPE> IExecutableCheckerCompositeBuilder<BEAN_TYPE> executableCheckerCompositeBuilder();

	IEntityServiceCompositeBuilder entityServiceCompositeBuilder();

	IRedundantServiceResolver<IEntityService> entityServiceResolver();

}
