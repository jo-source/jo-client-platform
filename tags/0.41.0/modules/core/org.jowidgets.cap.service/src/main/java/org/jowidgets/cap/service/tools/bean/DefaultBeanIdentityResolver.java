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

package org.jowidgets.cap.service.tools.bean;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.service.api.bean.IBeanIdentityResolver;
import org.jowidgets.util.Assert;

public class DefaultBeanIdentityResolver<BEAN_TYPE extends IBean> implements IBeanIdentityResolver<BEAN_TYPE> {

	private final Class<BEAN_TYPE> beanType;
	private final Object beanTypeId;

	public DefaultBeanIdentityResolver(final Class<? extends BEAN_TYPE> beanType) {
		this(beanType, beanType);
	}

	@SuppressWarnings("unchecked")
	public DefaultBeanIdentityResolver(final Class<? extends BEAN_TYPE> beanType, final Object beanTypeId) {
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		this.beanType = (Class<BEAN_TYPE>) beanType;
		this.beanTypeId = beanTypeId;
	}

	@Override
	public final Class<BEAN_TYPE> getBeanType() {
		return beanType;
	}

	@Override
	public final Object getBeanTypeId() {
		return beanTypeId;
	}

	@Override
	public final Object getId(final BEAN_TYPE bean) {
		return bean.getId();
	}

	@Override
	public final long getVersion(final BEAN_TYPE bean) {
		return bean.getVersion();
	}

}
