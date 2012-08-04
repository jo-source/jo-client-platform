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

package org.jowidgets.cap.service.neo4J.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.service.api.bean.IBeanPropertyMap;
import org.jowidgets.cap.service.neo4j.api.IBeanFactory;
import org.jowidgets.cap.service.neo4j.api.INodeBean;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.neo4j.graphdb.Node;

final class DefaultBeanFactory implements IBeanFactory {

	@SuppressWarnings("unchecked")
	@Override
	public <BEAN_TYPE extends IBean> BEAN_TYPE create(final Class<BEAN_TYPE> beanType, final Object beanTypeId, final Node node) {
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(node, "node");
		return (BEAN_TYPE) Proxy.newProxyInstance(beanType.getClassLoader(), new Class[] {
				beanType, IBeanDto.class, INodeBean.class, IBeanPropertyMap.class}, new ProxyInvocationHandler(
			node,
			beanType,
			beanTypeId));
	}

	final class ProxyInvocationHandler implements InvocationHandler {

		private final Node node;
		private final Class<?> beanType;
		private final Object beanTypeId;

		ProxyInvocationHandler(final Node node, final Class<?> beanType, final Object beanTypeId) {
			this.node = node;
			this.beanType = beanType;
			this.beanTypeId = beanTypeId;
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
					return node.getProperty(IBean.ID_PROPERTY).equals(((IBean) args[0]).getId());
				}
				else {
					return false;
				}
			}
			if (method.getName().equals("hashCode")) {
				return node.hashCode();
			}
			else if (isGetTypeMethod(method)) {
				return BeanTypeIdUtil.toString(beanTypeId);
			}
			else if (isGetNodeMethod(method)) {
				return node;
			}
			else if (isGetBeanTypeIdMethod(method)) {
				return beanTypeId;
			}
			else if (isGetIdMethod(method)) {
				return node.getId();
			}
			else if (isGetVersionMethod(method)) {
				return node.getProperty(IBean.VERSION_PROPERTY);
			}
			else if (isGetValueMethod(method)) {
				if (!EmptyCheck.isEmpty(args)) {
					return node.getProperty((String) args[0]);
				}
				else {
					return null;
				}
			}
			else if (isSetValueMethod(method)) {
				if (!EmptyCheck.isEmpty(args) && args.length == 2) {
					node.setProperty((String) args[0], args[1]);
				}
				return null;
			}
			else if (method.getName().startsWith("get")) {
				return getProperty(node, toPropertyName(method.getName(), 3));
			}
			else if (method.getName().startsWith("has")) {
				return getProperty(node, toPropertyName(method.getName(), 3));
			}
			else if (method.getName().startsWith("is")) {
				return getProperty(node, toPropertyName(method.getName(), 2));
			}
			else if (method.getName().startsWith("set")) {
				if (args == null || args.length != 1) {
					throw new IllegalStateException("Setter must have exactly one argument");
				}
				node.setProperty(toPropertyName(method.getName(), 3), args[0]);
				return null;
			}
			else {
				throw new IllegalStateException(
					"Method name start with 'get', 'has', 'is', 'set' or must be 'toString', 'equals', 'hashCode'.");
			}
		}

		private Object getProperty(final Node node, final String propertyName) {
			if (node.hasProperty(propertyName)) {
				return node.getProperty(propertyName);
			}
			else {
				return null;
			}
		}

		private String toPropertyName(final String methodName, final int prefixLength) {
			return methodName.substring(prefixLength, prefixLength + 1).toLowerCase() + methodName.substring(prefixLength + 1);
		}

		private boolean isGetNodeMethod(final Method method) {
			if (method.getName().equals("getNode")) {
				final Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes == null || parameterTypes.length == 0) {
					return true;
				}
			}
			return false;
		}

		private boolean isGetTypeMethod(final Method method) {
			if (method.getName().equals("getType")) {
				final Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes == null || parameterTypes.length == 0) {
					return true;
				}
			}
			return false;
		}

		private boolean isGetBeanTypeIdMethod(final Method method) {
			if (method.getName().equals("getBeanTypeId")) {
				final Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes == null || parameterTypes.length == 0) {
					return true;
				}
			}
			return false;
		}

		private boolean isGetValueMethod(final Method method) {
			if (method.getName().equals("getValue")) {
				final Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes != null && parameterTypes.length == 1 && parameterTypes[0] == String.class) {
					return true;
				}
			}
			return false;
		}

		private boolean isGetIdMethod(final Method method) {
			if (method.getName().equals("getId")) {
				final Class<?>[] parameterTypes = method.getParameterTypes();
				if (EmptyCheck.isEmpty(parameterTypes)) {
					return true;
				}
			}
			return false;
		}

		private boolean isGetVersionMethod(final Method method) {
			if (method.getName().equals("getVersion")) {
				final Class<?>[] parameterTypes = method.getParameterTypes();
				if (EmptyCheck.isEmpty(parameterTypes)) {
					return true;
				}
			}
			return false;
		}

		private boolean isSetValueMethod(final Method method) {
			if (method.getName().equals("setValue")) {
				final Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes != null
					&& parameterTypes.length == 2
					&& parameterTypes[0] == String.class
					&& parameterTypes[1] == Object.class) {
					return true;
				}
			}
			return false;
		}

		private String invokeToString() {
			final StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Proxy for type: " + beanType.getName() + "\n");
			stringBuilder.append("Node: " + node + "\n");
			return stringBuilder.toString();
		}

	}
}
