/*
 * Copyright (c) 2014, grossmann
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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.ui.api.clipboard.IBeanSelectionClipboard;
import org.jowidgets.util.Assert;

final class BeanSelectionClipboardImpl implements IBeanSelectionClipboard {

	private static final long serialVersionUID = 2250159601546437673L;

	private final Object entityId;
	private final Object beanTypeId;
	private final Class<?> beanType;
	private final Collection<IBeanDto> beans;

	private final String beanTypeName;
	private final Object beanTypeIdComparable;
	private final Object entityIdComparable;

	BeanSelectionClipboardImpl(
		final Object entityId,
		final Object beanTypeId,
		final Class<?> beanType,
		final Collection<IBeanDto> beans) {

		Assert.paramNotNull(entityId, "entityId");
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(beans, "beans");

		this.entityId = entityId;
		this.beanTypeId = beanTypeId;
		this.beanType = beanType;
		this.beans = Collections.unmodifiableList(new LinkedList<IBeanDto>(beans));

		this.beanTypeName = beanType.getName();
		this.beanTypeIdComparable = createIdComparable(beanTypeId);
		this.entityIdComparable = createIdComparable(entityId);
	}

	private static Object createIdComparable(final Object entityId) {
		if (entityId instanceof Class<?>) {
			return ((Class<?>) entityId).getName();
		}
		else {
			return entityId;
		}
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
	public Class<?> getBeanType() {
		return beanType;
	}

	@Override
	public Collection<IBeanDto> getBeans() {
		return beans;
	}

	@Override
	public String toString() {
		return "BeanSelectionClipboardImpl [entityId=" + entityId + ", beanType=" + beanType + ", beans=" + beans + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((beanTypeIdComparable == null) ? 0 : beanTypeIdComparable.hashCode());
		result = prime * result + ((beanTypeName == null) ? 0 : beanTypeName.hashCode());
		result = prime * result + ((beans == null) ? 0 : beans.hashCode());
		result = prime * result + ((entityIdComparable == null) ? 0 : entityIdComparable.hashCode());
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
		if (!(obj instanceof BeanSelectionClipboardImpl)) {
			return false;
		}
		final BeanSelectionClipboardImpl other = (BeanSelectionClipboardImpl) obj;
		if (beanTypeIdComparable == null) {
			if (other.beanTypeIdComparable != null) {
				return false;
			}
		}
		else if (!beanTypeIdComparable.equals(other.beanTypeIdComparable)) {
			return false;
		}
		if (beanTypeName == null) {
			if (other.beanTypeName != null) {
				return false;
			}
		}
		else if (!beanTypeName.equals(other.beanTypeName)) {
			return false;
		}
		if (beans == null) {
			if (other.beans != null) {
				return false;
			}
		}
		else if (!beans.equals(other.beans)) {
			return false;
		}
		if (entityIdComparable == null) {
			if (other.entityIdComparable != null) {
				return false;
			}
		}
		else if (!entityIdComparable.equals(other.entityIdComparable)) {
			return false;
		}
		return true;
	}

}
