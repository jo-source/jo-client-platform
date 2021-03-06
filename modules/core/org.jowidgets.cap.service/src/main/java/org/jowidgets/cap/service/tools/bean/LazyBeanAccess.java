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

package org.jowidgets.cap.service.tools.bean;

import java.util.Collection;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IProvider;

public final class LazyBeanAccess<BEAN_TYPE extends IBean> implements IBeanAccess<BEAN_TYPE> {

	private final IProvider<IBeanAccess<BEAN_TYPE>> beanAccessProvider;

	public LazyBeanAccess(final IProvider<IBeanAccess<BEAN_TYPE>> beanAccessProvider) {
		Assert.paramNotNull(beanAccessProvider, "beanAccessProvider");
		this.beanAccessProvider = beanAccessProvider;
	}

	@Override
	public Object getId(final BEAN_TYPE bean) {
		return bean.getId();
	}

	@Override
	public long getVersion(final BEAN_TYPE bean) {
		return bean.getVersion();
	}

	@Override
	public List<BEAN_TYPE> getBeans(final Collection<? extends IBeanKey> keys, final IExecutionCallback executionCallback) {
		return beanAccessProvider.get().getBeans(keys, executionCallback);
	}

	@Override
	public Class<BEAN_TYPE> getBeanType() {
		return beanAccessProvider.get().getBeanType();
	}

	@Override
	public Object getBeanTypeId() {
		return beanAccessProvider.get().getBeanTypeId();
	}

	@Override
	public void flush() {
		beanAccessProvider.get().flush();
	}

}
