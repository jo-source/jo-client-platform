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

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanData;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.util.Assert;

final class BeanInitializerImpl<BEAN_TYPE extends IBean> implements IBeanInitializer<BEAN_TYPE> {

	private final Map<String, Method> methods;

	BeanInitializerImpl(final Class<? extends BEAN_TYPE> beanType, final Collection<String> propertyNames) {
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(propertyNames, "propertyNames");

		this.methods = new HashMap<String, Method>();

		try {
			final BeanInfo beanInfo = Introspector.getBeanInfo(beanType);
			for (final PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
				final String propertyName = propertyDescriptor.getName();
				if (propertyNames.contains(propertyName) && !propertyName.equals("version") && !propertyName.equals("id")) {
					final Method writeMethod = propertyDescriptor.getWriteMethod();
					if (writeMethod != null) {
						methods.put(propertyName, propertyDescriptor.getWriteMethod());
					}
				}
			}
		}
		catch (final IntrospectionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void initialize(final BEAN_TYPE bean, final IBeanData beanData) {
		Assert.paramNotNull(bean, "bean");
		Assert.paramNotNull(beanData, "beanData");

		for (final Entry<String, Method> entry : methods.entrySet()) {
			try {
				entry.getValue().invoke(bean, beanData.getValue(entry.getKey()));
			}
			catch (final Exception e) {
				throw new RuntimeException("Error while setting property '" + entry.getKey() + "' on bean '" + bean + "'.", e);
			}
		}
	}
}
