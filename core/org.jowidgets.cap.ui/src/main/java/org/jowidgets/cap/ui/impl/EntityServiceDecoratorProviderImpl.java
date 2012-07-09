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
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.tools.proxy.AbstractCapServiceInvocationHandler;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;

final class EntityServiceDecoratorProviderImpl implements IServicesDecoratorProvider {

	private static final String GET_DESCRIPTOR_METHOD_NAME = "getDescriptor";
	private static final String GET_BEAN_SERVICES_METHOD_NAME = "getBeanServices";
	private static final String GET_ENTITY_LINKS_METHOD_NAME = "getEntityLinks";

	private final int order;

	private final Method getDescriptorMethod;
	private final Method getBeanServicesMethod;
	private final Method getEntityLinksMethod;

	private final Map<Object, EntityInfo> entityInfos;

	EntityServiceDecoratorProviderImpl(final int order) {
		this.order = order;
		this.entityInfos = new HashMap<Object, EntityServiceDecoratorProviderImpl.EntityInfo>();
		try {
			this.getDescriptorMethod = IEntityService.class.getMethod(GET_DESCRIPTOR_METHOD_NAME, Object.class);
			this.getBeanServicesMethod = IEntityService.class.getMethod(GET_BEAN_SERVICES_METHOD_NAME, Object.class);
			this.getEntityLinksMethod = IEntityService.class.getMethod(GET_ENTITY_LINKS_METHOD_NAME, Object.class);
		}
		catch (final Exception e) {
			throw new RuntimeException("Could not create entity service access methods. Maybe interface '"
				+ IEntityService.class.getName()
				+ "' has beeen changed", e);
		}
	}

	@Override
	public <SERVICE_TYPE> IDecorator<SERVICE_TYPE> getDecorator(final IServiceId<SERVICE_TYPE> id) {
		Assert.paramNotNull(id, "id");
		final Class<? extends SERVICE_TYPE> serviceType = id.getServiceType();
		return new IDecorator<SERVICE_TYPE>() {
			@SuppressWarnings("unchecked")
			@Override
			public SERVICE_TYPE decorate(final SERVICE_TYPE original) {
				if (IEntityService.class.equals(serviceType)) {
					final InvocationHandler invocationHandler = new EntityServiceDecoratorInvocationHandler(original);
					return (SERVICE_TYPE) Proxy.newProxyInstance(
							serviceType.getClassLoader(),
							new Class[] {serviceType},
							invocationHandler);
				}
				else {
					return original;
				}
			}
		};
	}

	@Override
	public int getOrder() {
		return order;
	}

	private final class EntityServiceDecoratorInvocationHandler extends AbstractCapServiceInvocationHandler {

		private final Object original;

		private EntityServiceDecoratorInvocationHandler(final Object original) {
			this.original = original;
		}

		@Override
		protected Object invokeSyncSignature(final Method method, final Object[] args, final IExecutionCallback executionCallback) throws Throwable {
			try {
				final Object entityId = args[0];
				final EntityInfo entityInfo = getEntityInfo(entityId);
				if (method.getName().equals(GET_DESCRIPTOR_METHOD_NAME)) {
					return entityInfo.getDescriptor();
				}
				else if (method.getName().equals(GET_BEAN_SERVICES_METHOD_NAME)) {
					return entityInfo.getBeanServices();
				}
				else if (method.getName().equals(GET_ENTITY_LINKS_METHOD_NAME)) {
					return entityInfo.getEntityLinks();
				}
				else {
					return method.invoke(original, args);
				}
			}
			catch (final Throwable e) {
				throw e;
			}
		}

		private EntityInfo getEntityInfo(final Object entityId) {
			EntityInfo result = entityInfos.get(entityId);
			if (result == null) {
				result = createEntityInfo(entityId);
				entityInfos.put(entityId, result);
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		private EntityInfo createEntityInfo(final Object entityId) {
			try {
				final IBeanDtoDescriptor descriptor = (IBeanDtoDescriptor) getDescriptorMethod.invoke(original, entityId);
				final IBeanServicesProvider beanServices = (IBeanServicesProvider) getBeanServicesMethod.invoke(
						original,
						entityId);
				final List<IEntityLinkDescriptor> entityLinks = (List<IEntityLinkDescriptor>) getEntityLinksMethod.invoke(
						original,
						entityId);
				return new EntityInfo(descriptor, beanServices, entityLinks);
			}
			catch (final Exception e) {
				throw new RuntimeException("Error while invoking method on entity service", e);
			}
		}

		@Override
		protected Object invokeAsyncSignature(
			final Method method,
			final Object[] args,
			final int resultCallbackIndex,
			final IResultCallback<Object> resultCallback,
			final IExecutionCallback executionCallback) {

			throw new IllegalStateException("The '"
				+ IEntityService.class.getName()
				+ "' had no async methods when this decorator was designed");

		}

	}

	private final class EntityInfo {

		private final IBeanDtoDescriptor descriptor;
		private final IBeanServicesProvider beanServices;
		private final List<IEntityLinkDescriptor> entityLinks;

		private EntityInfo(
			final IBeanDtoDescriptor descriptor,
			final IBeanServicesProvider beanServices,
			final List<IEntityLinkDescriptor> entityLinks) {
			super();
			this.descriptor = descriptor;
			this.beanServices = beanServices;
			if (entityLinks == null) {
				this.entityLinks = Collections.emptyList();
			}
			else {
				this.entityLinks = Collections.unmodifiableList(new LinkedList<IEntityLinkDescriptor>(entityLinks));
			}
		}

		private IBeanDtoDescriptor getDescriptor() {
			return descriptor;
		}

		private IBeanServicesProvider getBeanServices() {
			return beanServices;
		}

		private List<IEntityLinkDescriptor> getEntityLinks() {
			return entityLinks;
		}

	}

}
