/*
 * Copyright (c) 2016, grossmann
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.IEntityInfo;
import org.jowidgets.cap.common.api.service.IEntityInfoBuilder;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class EntityInfoBuilderImpl implements IEntityInfoBuilder {

	private Object entityId;
	private IBeanDtoDescriptor descriptor;
	private IBeanServicesProvider beanServicesProvider;
	private List<IEntityLinkDescriptor> entityLinks;

	EntityInfoBuilderImpl() {
		this.entityLinks = Collections.emptyList();
	}

	@Override
	public IEntityInfoBuilder setEntityId(final Object entityId) {
		Assert.paramNotNull(entityId, "entityId");
		this.entityId = entityId;
		return this;
	}

	@Override
	public IEntityInfoBuilder setDescriptor(final IBeanDtoDescriptor descriptor) {
		this.descriptor = descriptor;
		return this;
	}

	@Override
	public IEntityInfoBuilder setBeanServices(final IBeanServicesProvider beanServicesProvider) {
		this.beanServicesProvider = beanServicesProvider;
		return this;
	}

	@Override
	public IEntityInfoBuilder setEntityLinks(final Collection<? extends IEntityLinkDescriptor> links) {
		if (EmptyCheck.isEmpty(links)) {
			this.entityLinks = Collections.emptyList();
		}
		else {
			this.entityLinks = Collections.unmodifiableList(new ArrayList<IEntityLinkDescriptor>(links));
		}
		return this;
	}

	protected IBeanServicesProvider getBeanServicesProvider() {
		return beanServicesProvider;
	}

	protected void setBeanServicesProvider(final IBeanServicesProvider beanServicesProvider) {
		this.beanServicesProvider = beanServicesProvider;
	}

	protected Object getEntityId() {
		return entityId;
	}

	protected IBeanDtoDescriptor getDescriptor() {
		return descriptor;
	}

	protected List<IEntityLinkDescriptor> getEntityLinks() {
		return entityLinks;
	}

	protected void setLinks(final List<IEntityLinkDescriptor> links) {
		this.entityLinks = links;
	}

	@Override
	public IEntityInfo build() {
		return new EntityInfoImpl(this);
	}

}
