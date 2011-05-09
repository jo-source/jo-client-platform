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

package org.jowidgets.cap.ui.impl.bean;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.util.Assert;

public final class DataBeanFactory<BEAN_TYPE> implements IBeanProxyFactory<BEAN_TYPE> {

	private final Class<? extends BEAN_TYPE> beanType;

	public DataBeanFactory(final Class<? extends BEAN_TYPE> beanType) {
		this.beanType = beanType;
	}

	@Override
	public List<IBeanProxy<BEAN_TYPE>> createProxies(final Collection<? extends IBeanDto> beanDtos) {
		Assert.paramNotNull(beanDtos, "beanDtos");
		final List<IBeanProxy<BEAN_TYPE>> result = new LinkedList<IBeanProxy<BEAN_TYPE>>();
		for (final IBeanDto beanDto : beanDtos) {
			result.add(createProxy(beanDto));
		}
		return result;
	}

	@Override
	public IBeanProxy<BEAN_TYPE> createProxy(final IBeanDto beanDto) {
		return new BeanProxyImpl<BEAN_TYPE>(beanDto, beanType);
	}

	@Override
	public IBeanProxy<BEAN_TYPE> createProxy() {
		return new BeanProxyImpl<BEAN_TYPE>(new IBeanDto() {

			@Override
			public Object getValue(final String propertyName) {
				return null;
			}

			@Override
			public long getVersion() {
				return 0;
			}

			@Override
			public Object getId() {
				return null;
			}
		}, beanType);
	}

}
