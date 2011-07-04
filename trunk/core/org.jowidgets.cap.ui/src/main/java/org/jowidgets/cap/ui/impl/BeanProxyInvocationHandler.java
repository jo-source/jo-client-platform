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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.jowidgets.cap.common.api.bean.IBean;

final class BeanProxyInvocationHandler implements InvocationHandler {

	private final BeanProxyImpl<?> dataBean;
	private final Class<?> beanType;

	BeanProxyInvocationHandler(final BeanProxyImpl<?> dataBean, final Class<?> beanType) {
		super();
		this.dataBean = dataBean;
		this.beanType = beanType;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		try {
			return doInvoke(proxy, method, args);
		}
		catch (final Exception e) {
			throw new RuntimeException("Error while invoking method '" + method.getName() + "' on '" + proxy + "'", e);
		}

	}

	private Object doInvoke(final Object proxy, final Method method, final Object[] args) {
		if (method.getName().equals("toString")) {
			return invokeToString();
		}
		if (method.getName().equals("equals")) {
			if (args == null || args.length != 1) {
				throw new IllegalStateException("Method equals must have exactly one argument");
			}
			if (args[0] instanceof IBean) {
				return dataBean.getId().equals(((IBean) args[0]).getId());
			}
			else {
				return false;
			}
		}
		if (method.getName().equals("hashCode")) {
			return dataBean.hashCode();
		}
		else if (method.getName().startsWith("get")) {
			return dataBean.getValue(toPropertyName(method.getName(), 3));
		}
		else if (method.getName().startsWith("has")) {
			return dataBean.getValue(toPropertyName(method.getName(), 3));
		}
		else if (method.getName().startsWith("is")) {
			return dataBean.getValue(toPropertyName(method.getName(), 2));
		}
		else if (method.getName().startsWith("set")) {
			if (args == null || args.length != 1) {
				throw new IllegalStateException("Setter must have exactly one argument");
			}
			dataBean.setValue(toPropertyName(method.getName(), 3), args[0]);
			return null;
		}
		else {
			throw new IllegalStateException(
				"Method name start with 'get', 'has', 'is', 'set' or must be 'toString', 'equals', 'hashCode'.");
		}
	}

	private String toPropertyName(final String methodName, final int prefixLength) {
		return methodName.substring(prefixLength, prefixLength + 1).toLowerCase() + methodName.substring(prefixLength + 1);
	}

	private String invokeToString() {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Proxy for type: " + beanType.getName() + "\n");
		stringBuilder.append("DataBean: " + dataBean + "\n");
		return stringBuilder.toString();
	}

}
