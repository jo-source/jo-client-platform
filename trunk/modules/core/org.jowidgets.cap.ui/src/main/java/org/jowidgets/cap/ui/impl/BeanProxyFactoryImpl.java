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

package org.jowidgets.cap.ui.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.ui.api.attribute.IAttributeSet;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.util.Assert;

final class BeanProxyFactoryImpl<BEAN_TYPE> implements IBeanProxyFactory<BEAN_TYPE> {

	private static final Object DUMMY_ID = new Object() {};

	private final Object beanTypeId;
	private final Class<? extends BEAN_TYPE> beanType;

	BeanProxyFactoryImpl(final Object beanTypeId, final Class<? extends BEAN_TYPE> beanType) {
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(beanType, "beanType");
		this.beanTypeId = beanTypeId;
		this.beanType = beanType;
	}

	@Override
	public List<IBeanProxy<BEAN_TYPE>> createProxies(final Collection<? extends IBeanDto> beanDtos, final IAttributeSet attributes) {
		Assert.paramNotNull(beanDtos, "beanDtos");
		Assert.paramNotNull(attributes, "attributes");
		final List<IBeanProxy<BEAN_TYPE>> result = new LinkedList<IBeanProxy<BEAN_TYPE>>();
		for (final IBeanDto beanDto : beanDtos) {
			result.add(createProxy(beanDto, attributes));
		}
		return result;
	}

	@Override
	public IBeanProxy<BEAN_TYPE> createProxy(final IBeanDto beanDto, final IAttributeSet attributes) {
		Assert.paramNotNull(beanDto, "beanDto");
		Assert.paramNotNull(attributes, "attributes");
		return new BeanProxyImpl<BEAN_TYPE>(beanDto, beanTypeId, beanType, attributes, false, false, false);
	}

	@Override
	public IBeanProxy<BEAN_TYPE> createTransientProxy(final IAttributeSet attributes) {
		return createTransientProxy(attributes, new HashMap<String, Object>());
	}

	@Override
	public IBeanProxy<BEAN_TYPE> createTransientProxy(final IAttributeSet attributes, Map<String, Object> defaultValues) {
		if (defaultValues == null) {
			defaultValues = new HashMap<String, Object>();
		}
		return new BeanProxyImpl<BEAN_TYPE>(new BeanDto(defaultValues), beanTypeId, beanType, attributes, false, true, false);
	}

	@Override
	public IBeanProxy<BEAN_TYPE> createLastRowDummyProxy(final IAttributeSet attributes) {
		return new BeanProxyImpl<BEAN_TYPE>(
			new BeanDto(new HashMap<String, Object>()),
			beanTypeId,
			beanType,
			attributes,
			false,
			true,
			true);
	}

	@Override
	public IBeanProxy<BEAN_TYPE> createDummyProxy(final IAttributeSet attributes) {
		Assert.paramNotNull(attributes, "attributes");
		return new BeanProxyImpl<BEAN_TYPE>(new DummyBeanDto(), beanTypeId, beanType, attributes, true, false, false);
	}

	private static class DummyBeanDto implements IBeanDto {

		@Override
		public Object getValue(final String propertyName) {
			return null;
		}

		@Override
		public Object getId() {
			return DUMMY_ID;
		}

		@Override
		public long getVersion() {
			return 0;
		}

	}

	private static final class BeanDto implements IBeanDto, Serializable {

		private static final long serialVersionUID = 8721566850623754973L;

		private final Object id;
		private final Map<String, Object> defaultValues;

		private BeanDto(final Map<String, Object> defaultValues) {
			this.id = UUID.randomUUID();
			this.defaultValues = defaultValues;
		}

		@Override
		public Object getValue(final String propertyName) {
			return defaultValues.get(propertyName);
		}

		@Override
		public long getVersion() {
			return 0;
		}

		@Override
		public Object getId() {
			return id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
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
			if (getClass() != obj.getClass()) {
				return false;
			}
			final BeanDto other = (BeanDto) obj;
			if (id == null) {
				if (other.id != null) {
					return false;
				}
			}
			else if (!id.equals(other.id)) {
				return false;
			}
			return true;
		}

	}
}
