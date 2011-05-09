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

package org.jowidgets.cap.service.impl.service;

import java.util.Collections;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.refresh.IRefreshServiceBuilder;
import org.jowidgets.util.Assert;

public final class RefreshServiceBuilder<BEAN_TYPE extends IBean> implements IRefreshServiceBuilder<BEAN_TYPE> {

	private final Class<? extends BEAN_TYPE> beanType;
	private final IBeanAccess<? extends BEAN_TYPE> beanAccess;

	private List<String> propertyNames;
	private boolean allowDeletedBeans;

	public RefreshServiceBuilder(final IBeanAccess<? extends BEAN_TYPE> beanAccess) {
		Assert.paramNotNull(beanAccess, "beanAccess");
		Assert.paramNotNull(beanAccess.getBeanType(), "beanAccess.getBeanType()");

		this.beanType = beanAccess.getBeanType();
		this.beanAccess = beanAccess;

		this.allowDeletedBeans = true;
	}

	@Override
	public IRefreshServiceBuilder<BEAN_TYPE> setPropertyNames(final List<String> propertyNames) {
		this.propertyNames = propertyNames;
		return this;
	}

	@Override
	public IRefreshServiceBuilder<BEAN_TYPE> setAllowDeletedBeans(final boolean allowDeletedBeans) {
		this.allowDeletedBeans = allowDeletedBeans;
		return this;
	}

	protected List<String> getPropertyNames() {
		if (propertyNames == null) {
			return Collections.emptyList();
		}
		else {
			return propertyNames;
		}
	}

	@Override
	public IRefreshService build() {
		return new RefreshServiceImpl<IBean>(beanType, beanAccess, getPropertyNames(), allowDeletedBeans);
	}

}
