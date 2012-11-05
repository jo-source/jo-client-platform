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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanDtoBuilder;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.util.Assert;

final class BeanDtoFactoryImpl<BEAN_TYPE extends IBean> implements IBeanDtoFactory<BEAN_TYPE> {

	private final Class<? extends IBean> beanType;
	private final Map<String, Method> methods;

	BeanDtoFactoryImpl(final Class<? extends IBean> beanType, final Collection<String> propertyNames) {

		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(propertyNames, "propertyNames");

		this.beanType = beanType;
		this.methods = new HashMap<String, Method>();

		try {
			final BeanInfo beanInfo = Introspector.getBeanInfo(beanType);
			for (final PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
				final String propertyName = propertyDescriptor.getName();
				if (propertyNames.contains(propertyName)) {
					methods.put(propertyName, propertyDescriptor.getReadMethod());
				}
			}
		}
		catch (final IntrospectionException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public IBeanDto createDto(final BEAN_TYPE bean) {
		Assert.paramNotNull(bean, "bean");
		final IBeanDtoBuilder builder = CapCommonToolkit.dtoBuilder(beanType);
		builder.setId(bean.getId());
		builder.setVersion(bean.getVersion());
		for (final Entry<String, Method> methodEntry : methods.entrySet()) {
			try {
				builder.setValue(methodEntry.getKey(), methodEntry.getValue().invoke(bean));
			}
			catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
		return builder.build();
	}
}
