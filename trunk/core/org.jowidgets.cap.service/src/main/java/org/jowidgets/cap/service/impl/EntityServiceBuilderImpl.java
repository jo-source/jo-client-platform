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

package org.jowidgets.cap.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.service.api.entity.IEntityServiceBuilder;
import org.jowidgets.util.Assert;

final class EntityServiceBuilderImpl implements IEntityServiceBuilder {

	private final Map<Class<?>, IBeanDtoDescriptor<?>> descriptors;
	private final Map<Class<?>, IBeanServicesProvider<?>> beanServices;

	EntityServiceBuilderImpl() {
		this.descriptors = new HashMap<Class<?>, IBeanDtoDescriptor<?>>();
		this.beanServices = new HashMap<Class<?>, IBeanServicesProvider<?>>();
	}

	@Override
	public IEntityService build() {
		return new EntityServiceImpl(descriptors, beanServices);
	}

	@Override
	public <BEAN_TYPE> IEntityServiceBuilder add(
		final IBeanDtoDescriptor<BEAN_TYPE> descriptor,
		final IBeanServicesProvider<BEAN_TYPE> beanServicesProvider) {
		Assert.paramNotNull(descriptor, "descriptor");
		Assert.paramNotNull(descriptor.getBeanType(), "descriptor.getBeanType()");
		Assert.paramNotNull(beanServicesProvider, "beanServicesProvider");

		descriptors.put(descriptor.getBeanType(), descriptor);
		beanServices.put(descriptor.getBeanType(), beanServicesProvider);
		return this;
	}

}
