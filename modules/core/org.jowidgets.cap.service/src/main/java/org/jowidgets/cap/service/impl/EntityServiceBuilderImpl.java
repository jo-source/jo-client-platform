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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.service.EntityInfo;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.IEntityInfo;
import org.jowidgets.cap.common.api.service.IEntityInfoBuilder;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.service.api.entity.IEntityServiceBuilder;
import org.jowidgets.util.Assert;

class EntityServiceBuilderImpl implements IEntityServiceBuilder {

	private final Map<Object, IEntityInfo> entityInfos;

	EntityServiceBuilderImpl() {
		this.entityInfos = new LinkedHashMap<Object, IEntityInfo>();
	}

	@Override
	public IEntityService build() {
		return new EntityServiceImpl(entityInfos);
	}

	@Override
	public final <BEAN_TYPE> IEntityServiceBuilder add(
		final Object entityId,
		final IBeanDtoDescriptor descriptor,
		final IBeanServicesProvider beanServicesProvider) {
		Assert.paramNotNull(entityId, "entityId");
		Assert.paramNotNull(descriptor, "descriptor");
		Assert.paramNotNull(beanServicesProvider, "beanServicesProvider");

		final IEntityInfoBuilder builder = EntityInfo.builder();
		builder.setEntityId(entityId);
		builder.setDescriptor(descriptor);
		builder.setBeanServices(beanServicesProvider);
		entityInfos.put(entityId, builder.build());

		return this;
	}

	@Override
	public final <BEAN_TYPE> IEntityServiceBuilder add(
		final Object entityId,
		final IBeanDtoDescriptor descriptor,
		final IBeanServicesProvider beanServicesProvider,
		final Collection<? extends IEntityLinkDescriptor> entityLinks) {

		Assert.paramNotNull(entityId, "entityId");
		Assert.paramNotNull(descriptor, "descriptor");
		Assert.paramNotNull(beanServicesProvider, "beanServicesProvider");
		Assert.paramNotNull(entityLinks, "entityLinks");

		final IEntityInfoBuilder builder = EntityInfo.builder();
		builder.setEntityId(entityId);
		builder.setDescriptor(descriptor);
		builder.setBeanServices(beanServicesProvider);
		builder.setEntityLinks(entityLinks);
		entityInfos.put(entityId, builder.build());

		return this;
	}
}
