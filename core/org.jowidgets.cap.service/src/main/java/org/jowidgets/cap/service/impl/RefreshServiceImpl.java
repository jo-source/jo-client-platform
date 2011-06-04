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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.exception.DeletedBeanException;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;

public final class RefreshServiceImpl<BEAN_TYPE extends IBean> implements IRefreshService {

	private final IBeanAccess<BEAN_TYPE> beanAccess;
	private final IBeanDtoFactory<BEAN_TYPE> dtoFactory;
	private final boolean allowDeletedBeans;

	@SuppressWarnings("unchecked")
	RefreshServiceImpl(
		final Class<? extends BEAN_TYPE> beanType,
		final IBeanAccess<? extends BEAN_TYPE> beanAccess,
		final List<String> propertyNames,
		final boolean allowDeletedBeans) {

		this.beanAccess = (IBeanAccess<BEAN_TYPE>) beanAccess;
		this.allowDeletedBeans = allowDeletedBeans;
		this.dtoFactory = CapServiceToolkit.dtoFactory(beanType, propertyNames);
	}

	@Override
	public List<IBeanDto> refresh(final Collection<? extends IBeanKey> beanKeys, final IExecutionCallback executionCallback) {

		final List<BEAN_TYPE> beans = beanAccess.getBeans(beanKeys, executionCallback);

		if (!allowDeletedBeans && beans.size() != beanKeys.size()) {
			checkBeans(beanKeys, beans);
		}
		return dtoFactory.createDtos(beans);
	}

	private void checkBeans(final Collection<? extends IBeanKey> beanKeys, final List<BEAN_TYPE> beans) {
		//put beans into map to access them faster at the next step
		final Map<Object, IBeanKey> beanMap = new HashMap<Object, IBeanKey>();
		for (final IBeanKey key : beanKeys) {
			beanMap.put(key.getId(), key);
		}

		//check if beans are deleted or stale
		for (final IBean bean : beans) {
			final IBeanKey key = beanMap.get(bean.getId());
			if (!allowDeletedBeans && key == null) {
				throw new DeletedBeanException(key);
			}
		}
	}

}
