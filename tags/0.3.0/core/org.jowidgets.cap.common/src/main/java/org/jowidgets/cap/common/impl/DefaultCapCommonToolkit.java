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

import java.util.Collection;

import org.jowidgets.cap.common.api.ICapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBeanDataBuilder;
import org.jowidgets.cap.common.api.bean.IBeanDtoBuilder;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptorBuilder;
import org.jowidgets.cap.common.api.bean.IBeanKeyBuilder;
import org.jowidgets.cap.common.api.bean.IBeanModificationBuilder;
import org.jowidgets.cap.common.api.bean.IBeanPropertyBuilder;
import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.bean.IPropertyBuilder;
import org.jowidgets.cap.common.api.bean.IStaticValueRangeFactory;
import org.jowidgets.cap.common.api.entity.IEntityClassBuilder;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptorBuilder;
import org.jowidgets.cap.common.api.entity.IEntityLinkPropertiesBuilder;
import org.jowidgets.cap.common.api.filter.IFilterFactory;
import org.jowidgets.cap.common.api.lookup.ILookUpToolkit;
import org.jowidgets.cap.common.api.sort.ISortFactory;
import org.jowidgets.util.Assert;

public final class DefaultCapCommonToolkit implements ICapCommonToolkit {

	private final ISortFactory sortFactory;
	private final IFilterFactory filterFactory;
	private final IStaticValueRangeFactory valueRangeFactory;
	private final ILookUpToolkit lookUpToolkit;

	public DefaultCapCommonToolkit() {
		this.sortFactory = new SortFactoryImpl();
		this.filterFactory = new FilterFactoryImpl();
		this.valueRangeFactory = new StaticValueRangeFactoryImpl();
		this.lookUpToolkit = new LookUpToolkitImpl();
	}

	@Override
	public IEntityClassBuilder entityClassBuilder() {
		return new EntityClassBuilderImpl();
	}

	@Override
	public IEntityLinkPropertiesBuilder entityLinkPropertiesBuilder() {
		return new EntityLinkPropertiesBuilderImpl();
	}

	@Override
	public IEntityLinkDescriptorBuilder entityLinkDescriptorBuilder() {
		return new EntityLinkDescriptorBuilderImpl();
	}

	@Override
	public IPropertyBuilder propertyBuilder() {
		return new PropertyBuilder();
	}

	@Override
	public IBeanPropertyBuilder beanPropertyBuilder(final Class<?> beanType, final String propertyName) {
		return new BeanPropertyBuilderImpl(beanType, propertyName);
	}

	@Override
	public IBeanDtoBuilder dtoBuilder(final Object entityTypeId) {
		return new BeanDtoBuilderImpl(entityTypeId);
	}

	@Override
	public IBeanDtoDescriptor dtoDescriptor(final Collection<IProperty> properties) {
		Assert.paramNotNull(properties, "properties");
		return dtoDescriptor(properties, null, null, null);
	}

	@Override
	public IBeanDtoDescriptor dtoDescriptor(
		final Collection<IProperty> properties,
		final String labelSingular,
		final String labelPlural) {
		Assert.paramNotNull(properties, "properties");
		return dtoDescriptor(properties, labelSingular, labelPlural, null);
	}

	@Override
	public IBeanDtoDescriptor dtoDescriptor(
		final Collection<IProperty> properties,
		final String labelSingular,
		final String labelPlural,
		final String description) {
		Assert.paramNotNull(properties, "properties");
		return new BeanDtoDescriptorImpl(labelSingular, labelPlural, description, properties);
	}

	@Override
	public IBeanDtoDescriptorBuilder dtoDescriptorBuilder(final Class<?> beanType) {
		return new BeanDtoDescriptorBuilderImpl(beanType);
	}

	@Override
	public IBeanDataBuilder beanDataBuilder() {
		return new BeanDataBuilderImpl();
	}

	@Override
	public IBeanKeyBuilder beanKeyBuilder() {
		return new BeanKeyBuilderImpl();
	}

	@Override
	public IBeanModificationBuilder beanModificationBuilder() {
		return new BeanModificationBuilderImpl();
	}

	@Override
	public ISortFactory sortFactory() {
		return sortFactory;
	}

	@Override
	public IFilterFactory filterFactory() {
		return filterFactory;
	}

	@Override
	public IStaticValueRangeFactory staticValueRangeFactory() {
		return valueRangeFactory;
	}

	@Override
	public ILookUpToolkit lookUpToolkit() {
		return lookUpToolkit;
	}

}
