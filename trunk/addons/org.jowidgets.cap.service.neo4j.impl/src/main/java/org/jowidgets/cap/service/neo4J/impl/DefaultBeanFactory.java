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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.service.api.bean.IBeanPropertyMap;
import org.jowidgets.cap.service.neo4j.api.IBeanFactory;
import org.jowidgets.cap.service.neo4j.api.INodeBean;
import org.jowidgets.cap.service.neo4j.api.IRelationshipBean;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

final class DefaultBeanFactory implements IBeanFactory {

	@Override
	public <BEAN_TYPE extends IBean> BEAN_TYPE createNodeBean(
		final Class<BEAN_TYPE> beanType,
		final Object beanTypeId,
		final Node node) {
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(node, "node");

		if (beanType.isInterface()) {
			return createNodeBeanProxy(beanType, beanTypeId, node);
		}
		else {
			return createNodeBeanInstance(beanType, beanTypeId, node);
		}
	}

	@Override
	public <BEAN_TYPE extends IBean> BEAN_TYPE createRelationshipBean(
		final Class<BEAN_TYPE> beanType,
		final Object beanTypeId,
		final Relationship relationship) {
		if (!beanType.isInterface()) {
			return createRelationshipBeanInstance(beanType, beanTypeId, relationship);
		}
		else {
			throw new IllegalArgumentException("Bean type must be a class for relationship beans");
		}
	}

	@Override
	public <BEAN_TYPE extends IBean> boolean isNodeBean(final Class<BEAN_TYPE> beanType, final Object beanTypeId) {
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		return INodeBean.class.isAssignableFrom(beanType);
	}

	@Override
	public <BEAN_TYPE extends IBean> boolean isRelationshipBean(final Class<BEAN_TYPE> beanType, final Object beanTypeId) {
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		return IRelationshipBean.class.isAssignableFrom(beanType);
	}

	private <BEAN_TYPE extends IBean> BEAN_TYPE createNodeBeanInstance(
		final Class<BEAN_TYPE> beanType,
		final Object beanTypeId,
		final Node node) {
		try {
			final Constructor<BEAN_TYPE> constructor = beanType.getConstructor(Node.class);
			return constructor.newInstance(node);
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private <BEAN_TYPE extends IBean> BEAN_TYPE createRelationshipBeanInstance(
		final Class<BEAN_TYPE> beanType,
		final Object beanTypeId,
		final Relationship relationship) {
		try {
			final Constructor<BEAN_TYPE> constructor = beanType.getConstructor(Relationship.class);
			return constructor.newInstance(relationship);
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <BEAN_TYPE extends IBean> BEAN_TYPE createNodeBeanProxy(
		final Class<BEAN_TYPE> beanType,
		final Object beanTypeId,
		final Node node) {
		return (BEAN_TYPE) Proxy.newProxyInstance(beanType.getClassLoader(), new Class[] {
				beanType, INodeBean.class, IBeanPropertyMap.class}, new ProxyInvocationHandler(node, beanType, beanTypeId));
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
				return getProperty(node, IBean.ID_PROPERTY);
			}
			else if (isGetVersionMethod(method)) {
				if (node.hasProperty(IBean.VERSION_PROPERTY)) {
					return node.getProperty(IBean.VERSION_PROPERTY);
				}
				else {
					return -1;
				}
			}
			else if (isGetValueMethod(method)) {
				if (!EmptyCheck.isEmpty(args)) {
					return getProperty(node, ((String) args[0]));
				}
				else {
					return null;
				}
			}
			else if (isSetValueMethod(method)) {
				if (!EmptyCheck.isEmpty(args) && args.length == 2) {
					setProperty(node, (String) args[0], args[1]);
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
				setProperty(node, toPropertyName(method.getName(), 3), args[0]);
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

		private void setProperty(final Node node, final String propertyName, final Object value) {
			if (value != null) {
				node.setProperty(propertyName, value);
			}
			else {
				node.removeProperty(propertyName);
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
