/*
 * Copyright (c) 2012, grossmann
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

package org.jowidgets.cap.ui.impl;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.ui.api.types.IEntityTypeId;

final class EntityTypeIdImpl<BEAN_TYPE> implements IEntityTypeId<BEAN_TYPE> {

	private final Object entityId;
	private final Object beanTypeId;
	private final Class<BEAN_TYPE> beanType;

	@SuppressWarnings("unchecked")
	EntityTypeIdImpl(Object entityId, Object beanTypeId, Class<? extends BEAN_TYPE> beanType) {
		if (entityId == null && beanTypeId == null && beanType == null) {
			throw new IllegalArgumentException(
				"At least one of the parameters 'entityId', 'beanTypeId' or 'beanType' must not be null");
		}

		if (entityId == null) { //then beanTypeId or beanType must be != null
			if (beanTypeId == null) {
				beanTypeId = beanType.getName();
				entityId = beanType;
			}
			else if (beanType == null) {
				beanType = (Class<? extends BEAN_TYPE>) IBeanDto.class;
				entityId = beanTypeId;
			}
		}
		else {//entityId is not null
			if (beanType == null) {
				beanType = (Class<? extends BEAN_TYPE>) EntityServiceHelper.getBeanType(entityId);
				if (beanType == null) {
					beanType = (Class<? extends BEAN_TYPE>) IBeanDto.class;
				}
			}
			//from here beanType is not null
			if (beanTypeId == null) {
				beanTypeId = EntityServiceHelper.getBeanTypeId(entityId);
				if (beanTypeId == null) {
					beanTypeId = beanType.getName();
				}
			}
		}

		this.entityId = entityId;
		this.beanTypeId = beanTypeId;
		this.beanType = (Class<BEAN_TYPE>) beanType;
	}

	@Override
	public Object getEntityId() {
		return entityId;
	}

	@Override
	public Object getBeanTypeId() {
		return beanTypeId;
	}

	@Override
	public Class<BEAN_TYPE> getBeanType() {
		return beanType;
	}

	@Override
	public String toString() {
		return "EntityTypeIdImpl [entityId=" + entityId + ", entityTypeId=" + beanTypeId + ", beanType=" + beanType + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((beanType == null) ? 0 : beanType.hashCode());
		result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
		result = prime * result + ((beanTypeId == null) ? 0 : beanTypeId.hashCode());
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
		if (!(obj instanceof IEntityTypeId)) {
			return false;
		}
		final IEntityTypeId<?> other = (IEntityTypeId<?>) obj;
		if (beanType == null) {
			if (other.getBeanType() != null) {
				return false;
			}
		}
		else if (!beanType.equals(other.getBeanType())) {
			return false;
		}
		if (entityId == null) {
			if (other.getEntityId() != null) {
				return false;
			}
		}
		else if (!entityId.equals(other.getEntityId())) {
			return false;
		}
		if (beanTypeId == null) {
			if (other.getBeanTypeId() != null) {
				return false;
			}
		}
		else if (!beanTypeId.equals(other.getBeanTypeId())) {
			return false;
		}
		return true;
	}

}
