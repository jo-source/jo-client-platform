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
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanModification;
import org.jowidgets.cap.common.api.exception.ServiceException;
import org.jowidgets.cap.service.api.bean.IBeanModifier;
import org.jowidgets.util.NullCompatibleEquivalence;

final class BeanModifierImpl<BEAN_TYPE extends IBean> implements IBeanModifier<BEAN_TYPE> {

	private final Map<String, Method> writeMethods;
	private final Map<String, Method> readMethods;

	BeanModifierImpl(final Class<? extends BEAN_TYPE> beanType, final Collection<String> propertyNames) {
		this.writeMethods = new HashMap<String, Method>();
		this.readMethods = new HashMap<String, Method>();

		try {
			final BeanInfo beanInfo = Introspector.getBeanInfo(beanType);
			for (final PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
				final String propertyName = propertyDescriptor.getName();
				if (propertyNames.contains(propertyName)) {
					writeMethods.put(propertyName, propertyDescriptor.getWriteMethod());
					readMethods.put(propertyName, propertyDescriptor.getReadMethod());
				}
			}
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isPropertyStale(final BEAN_TYPE bean, final IBeanModification modification) {
		final Method readMethod = readMethods.get(modification.getPropertyName());
		if (readMethod != null) {
			try {
				final Object value = readMethod.invoke(bean);
				if (NullCompatibleEquivalence.equals(value, modification.getOldValue())) {
					return false;
				}
				else {
					return true;
				}
			}
			catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
		else {
			throw new ServiceException(modification, "Tryed to get the property '"
				+ modification.getPropertyName()
				+ "', but the property is not defined / allowed for this bean.");
		}
	}

	@Override
	public void modify(final BEAN_TYPE bean, final IBeanModification modification) {
		final Method writeMethod = writeMethods.get(modification.getPropertyName());
		if (writeMethod != null) {
			try {
				writeMethod.invoke(bean, modification.getNewValue());
			}
			catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
		else {
			throw new ServiceException(modification, "Tryed to set the property '"
				+ modification.getPropertyName()
				+ "', but the property is not defined / allowed for this bean.");
		}
	}

}
