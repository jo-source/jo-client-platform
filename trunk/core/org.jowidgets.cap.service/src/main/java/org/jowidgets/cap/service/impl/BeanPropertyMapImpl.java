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

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.service.api.bean.IBeanPropertyMap;
import org.jowidgets.util.Assert;

final class BeanPropertyMapImpl implements IBeanPropertyMap {

	private final Object entityTypeId;
	private Object id;
	private long version;
	private final Map<String, Object> values;

	BeanPropertyMapImpl(final Object entityTypeId) {
		Assert.paramNotNull(entityTypeId, "entityTypeId");
		this.entityTypeId = entityTypeId;
		this.values = new HashMap<String, Object>();
	}

	@Override
	public Object getId() {
		return id;
	}

	@Override
	public void setId(final Object id) {
		this.id = id;
		values.put(ID_PROPERTY, id);
	}

	@Override
	public long getVersion() {
		return version;
	}

	@Override
	public void setVersion(final long version) {
		this.version = version;
		values.put(VERSION_PROPERTY, version);
	}

	@Override
	public Object getValue(final String propertyName) {
		Assert.paramNotEmpty(propertyName, "propertyName");
		return values.get(propertyName);
	}

	@Override
	public void setValue(final String propertyName, final Object value) {
		Assert.paramNotEmpty(propertyName, "propertyName");
		values.put(propertyName, value);
		if (IBean.ID_PROPERTY.equals(propertyName)) {
			setId(value);
		}
		else if (IBean.VERSION_PROPERTY.equals(propertyName)) {
			setVersion((Long) value);
		}
	}

	@Override
	public Object getEntityTypeId() {
		return entityTypeId;
	}

}
