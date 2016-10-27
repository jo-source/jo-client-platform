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
import java.util.HashMap;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IPropertyMap;
import org.jowidgets.cap.service.api.bean.IBeanIdentityResolver;
import org.jowidgets.cap.service.api.bean.IBeanPropertyAccessor;
import org.jowidgets.cap.service.tools.bean.DefaultBeanIdentityResolver;
import org.jowidgets.util.Assert;

final class BeanPropertyAccessorImpl<BEAN_TYPE> implements IBeanPropertyAccessor<BEAN_TYPE> {

	private final IBeanIdentityResolver<BEAN_TYPE> identityResolver;
	private final Class<?> beanType;
	private final Map<String, Method> methods;

	@SuppressWarnings({"unchecked", "rawtypes"})
	BeanPropertyAccessorImpl(final Class<? extends IBean> beanType) {
		this(new DefaultBeanIdentityResolver(beanType));
	}

	@SuppressWarnings("unchecked")
	BeanPropertyAccessorImpl(final IBeanIdentityResolver<? extends BEAN_TYPE> identityResolver) {

		Assert.paramNotNull(identityResolver, "identityResolver");

		this.identityResolver = (IBeanIdentityResolver<BEAN_TYPE>) identityResolver;
		this.beanType = identityResolver.getBeanType();
		this.methods = new HashMap<String, Method>();

		try {
			final BeanInfo beanInfo = Introspector.getBeanInfo(beanType);
			for (final PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
				final String propertyName = propertyDescriptor.getName();
				methods.put(propertyName, propertyDescriptor.getReadMethod());
			}
		}
		catch (final IntrospectionException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public Object getId(final BEAN_TYPE bean) {
		return identityResolver.getId(bean);
	}

	@Override
	public long getVersion(final BEAN_TYPE bean) {
		return identityResolver.getVersion(bean);
	}

	@Override
	public Object getValue(final BEAN_TYPE bean, final String propertyName) {
		Assert.paramNotNull(bean, "bean");
		final Method method = methods.get(propertyName);
		if (method != null) {
			try {
				return method.invoke(bean);
			}
			catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
		else if (bean instanceof IPropertyMap) {
			return ((IPropertyMap) bean).getValue(propertyName);
		}
		else {
			throw new IllegalArgumentException(
				"The property with the name '" + propertyName + "' cannot be accessed for the bean: " + bean);
		}
	}

}
