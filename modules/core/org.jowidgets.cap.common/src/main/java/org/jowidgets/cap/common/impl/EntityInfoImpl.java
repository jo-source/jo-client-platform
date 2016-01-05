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

import java.io.Serializable;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.IEntityInfo;
import org.jowidgets.util.Assert;

final class EntityInfoImpl implements IEntityInfo, Serializable {

	private static final long serialVersionUID = 2176954708694507797L;

	private final Object entityId;
	private final IBeanDtoDescriptor descriptor;
	private final IBeanServicesProvider beanServicesProvider;
	private final List<IEntityLinkDescriptor> entityLinks;

	EntityInfoImpl(final EntityInfoBuilderImpl builder) {
		Assert.paramNotNull(builder.getEntityId(), "builder.getEntityId()");
		Assert.paramNotNull(builder.getEntityLinks(), "builder.getEntityLinks()");

		this.entityId = builder.getEntityId();
		this.descriptor = builder.getDescriptor();
		this.beanServicesProvider = builder.getBeanServicesProvider();
		this.entityLinks = builder.getEntityLinks();
	}

	@Override
	public Object getEntityId() {
		return entityId;
	}

	@Override
	public IBeanDtoDescriptor getDescriptor() {
		return descriptor;
	}

	@Override
	public IBeanServicesProvider getBeanServices() {
		return beanServicesProvider;
	}

	@Override
	public List<IEntityLinkDescriptor> getEntityLinks() {
		return entityLinks;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof EntityInfoImpl)) {
			return false;
		}
		final EntityInfoImpl other = (EntityInfoImpl) obj;
		if (entityId == null) {
			if (other.entityId != null) {
				return false;
			}
		}
		else if (!entityId.equals(other.entityId)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "EntityInfoImpl [entityId=" + entityId + ", descriptor=" + descriptor + "]";
	}

}
