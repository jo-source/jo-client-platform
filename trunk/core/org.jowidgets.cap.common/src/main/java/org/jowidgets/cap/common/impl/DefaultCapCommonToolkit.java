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

import org.jowidgets.cap.common.api.ICapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDataBuilder;
import org.jowidgets.cap.common.api.bean.IBeanDtoBuilder;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptorBuilder;
import org.jowidgets.cap.common.api.bean.IBeanKeyBuilder;
import org.jowidgets.cap.common.api.bean.IBeanModificationBuilder;
import org.jowidgets.cap.common.api.bean.IBeanPropertyBuilder;
import org.jowidgets.cap.common.api.bean.IPropertyBuilder;
import org.jowidgets.cap.common.api.bean.IValueRangeFactory;
import org.jowidgets.cap.common.api.sort.ISortFactory;

public final class DefaultCapCommonToolkit implements ICapCommonToolkit {

	private final ISortFactory sortFactory;
	private final IValueRangeFactory valueRangeFactory;

	public DefaultCapCommonToolkit() {
		this.sortFactory = new SortFactoryImpl();
		this.valueRangeFactory = new ValueRangeFactoryImpl();
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
	public IBeanDtoBuilder dtoBuilder() {
		return new BeanDtoBuilderImpl();
	}

	@Override
	public <BEAN_TYPE extends IBean> IBeanDtoDescriptorBuilder<BEAN_TYPE> dtoDescriptorBuilder(
		final Class<? extends BEAN_TYPE> beanType) {
		return new BeanDtoDescriptorBuilderImpl<BEAN_TYPE>(beanType);
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
	public IValueRangeFactory valueRangeFactory() {
		return valueRangeFactory;
	}

}
