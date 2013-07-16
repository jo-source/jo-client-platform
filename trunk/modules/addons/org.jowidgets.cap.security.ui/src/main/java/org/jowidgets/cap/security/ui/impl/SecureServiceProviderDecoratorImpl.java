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

package org.jowidgets.cap.security.ui.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.exception.AuthorizationFailedException;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.tools.proxy.AbstractCapServiceInvocationHandler;
import org.jowidgets.cap.security.common.api.IAuthorizationChecker;
import org.jowidgets.cap.security.common.api.ISecureObject;
import org.jowidgets.cap.security.common.api.ISecureServiceId;
import org.jowidgets.cap.security.ui.api.DecorationStrategy;
import org.jowidgets.cap.security.ui.api.IDecorationStrategySelector;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServiceProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;

final class SecureServiceProviderDecoratorImpl<AUTHORIZATION_TYPE> implements IDecorator<IServiceProvider> {

	private final IAuthorizationChecker<AUTHORIZATION_TYPE> authorizationChecker;
	private final IDecorationStrategySelector decorationStrategySelector;
	private final boolean filterUnreadableLinkDescriptors;

	SecureServiceProviderDecoratorImpl(
		final IDecorationStrategySelector decorationStrategySelector,
		final IAuthorizationChecker<AUTHORIZATION_TYPE> authorizationChecker,
		final boolean filterUnreadableLinkDescriptors) {

		this.decorationStrategySelector = decorationStrategySelector;
		this.authorizationChecker = authorizationChecker;
		this.filterUnreadableLinkDescriptors = filterUnreadableLinkDescriptors;
	}

	@Override
	public IServiceProvider decorate(final IServiceProvider original) {
		return new DecoratedServiceProvider(original);
	}

	private final class DecoratedServiceProvider implements IServiceProvider {

		private final IServiceProvider original;

		private DecoratedServiceProvider(final IServiceProvider original) {
			Assert.paramNotNull(original, "original");
			this.original = original;
		}

		@Override
		public Set<IServiceId<?>> getAvailableServices() {
			final Set<IServiceId<?>> result = new HashSet<IServiceId<?>>();
			for (final IServiceId<?> id : original.getAvailableServices()) {
				if (get(id) != null) {
					result.add(id);
				}
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <SERVICE_TYPE> SERVICE_TYPE get(final IServiceId<SERVICE_TYPE> id) {
			final SERVICE_TYPE originalService = original.get(id);
			if (originalService instanceof IEntityService && filterUnreadableLinkDescriptors) {
				return getDecoratedService(id, (SERVICE_TYPE) new DecoratedEntityService((IEntityService) originalService));
			}
			else if (originalService != null) {
				return getDecoratedService(id, originalService);
			}
			else {
				return null;
			}
		}

		private <SERVICE_TYPE> SERVICE_TYPE getDecoratedService(
			final IServiceId<SERVICE_TYPE> id,
			final SERVICE_TYPE originalService) {

			final DecorationStrategy strategy = decorationStrategySelector.getStrategy(id, originalService);
			if (DecorationStrategy.FILTER == strategy) {
				return getFilteredService(id, originalService);
			}
			else if (DecorationStrategy.ADD_AUTHORIZATION == strategy) {
				return getAuthorizationAddedService(id, originalService);
			}
			else if (null == strategy) {
				throw new IllegalArgumentException("Decoration strategy must not be null");
			}
			else {
				throw new IllegalArgumentException("DecorationStrategy '" + strategy + "' is not supported");
			}

		}

		@SuppressWarnings("unchecked")
		private <SERVICE_TYPE> SERVICE_TYPE getAuthorizationAddedService(
			final IServiceId<SERVICE_TYPE> id,
			final SERVICE_TYPE service) {
			if (id instanceof ISecureServiceId<?, ?>) {
				final Object authorization = ((ISecureServiceId<?, ?>) id).getAuthorization();
				if (!isAuthorized(authorization)) {
					final Class<SERVICE_TYPE> serviceType = id.getServiceType();
					final Class<?>[] interfaces = new Class[] {serviceType, ISecureObject.class};
					final InvocationHandler invocationHandler = new AddAuthorizationInvocationHandler(original);
					return (SERVICE_TYPE) Proxy.newProxyInstance(serviceType.getClassLoader(), interfaces, invocationHandler);
				}
			}
			return service;
		}

		private <SERVICE_TYPE> SERVICE_TYPE getFilteredService(final IServiceId<SERVICE_TYPE> id, final SERVICE_TYPE service) {
			if (acceptService(id)) {
				return service;
			}
			else {
				return null;
			}
		}

		private boolean acceptService(final IServiceId<?> id) {
			if (id instanceof ISecureServiceId<?, ?>) {
				return isAuthorized(((ISecureServiceId<?, ?>) id).getAuthorization());
			}
			else {
				return true;
			}
		}

		@SuppressWarnings("unchecked")
		private boolean isAuthorized(final Object authorization) {
			return authorizationChecker.hasAuthorization((AUTHORIZATION_TYPE) authorization);
		}
	}

	private final class AddAuthorizationInvocationHandler extends AbstractCapServiceInvocationHandler {

		private final Object authorization;

		private AddAuthorizationInvocationHandler(final Object authorization) {
			this.authorization = authorization;
		}

		@Override
		protected Object invokeSyncSignature(final Method method, final Object[] args, final IExecutionCallback executionCallback) throws Throwable {
			if ("getAuthorization".equals(method.getName())) {
				return authorization;
			}
			else {
				throw createAuthorizationFailedException();
			}
		}

		@Override
		protected Object invokeAsyncSignature(
			final Method method,
			final Object[] args,
			final int resultCallbackIndex,
			final IResultCallback<Object> resultCallback,
			final IExecutionCallback executionCallback) {

			if (resultCallback != null) {
				resultCallback.exception(createAuthorizationFailedException());
				return null;
			}
			else {
				throw createAuthorizationFailedException();
			}
		}

		private AuthorizationFailedException createAuthorizationFailedException() {
			return new AuthorizationFailedException(authorization);
		}

	}

	private final class DecoratedEntityService implements IEntityService {

		private final IEntityService original;

		public DecoratedEntityService(final IEntityService original) {
			this.original = original;
		}

		@Override
		public IBeanDtoDescriptor getDescriptor(final Object entityId) {
			return original.getDescriptor(entityId);
		}

		@Override
		public IBeanServicesProvider getBeanServices(final Object entityId) {
			return original.getBeanServices(entityId);
		}

		@Override
		public List<IEntityLinkDescriptor> getEntityLinks(final Object entityId) {
			final List<IEntityLinkDescriptor> links = original.getEntityLinks(entityId);
			if (links != null) {
				final List<IEntityLinkDescriptor> result = new LinkedList<IEntityLinkDescriptor>();
				for (final IEntityLinkDescriptor link : links) {
					if (!filter(link)) {
						result.add(link);
					}
				}
				return result;
			}
			return links;
		}

		@SuppressWarnings("unchecked")
		private boolean filter(final IEntityLinkDescriptor link) {
			final Object linkedEntityId = link.getLinkedEntityId();
			final IBeanServicesProvider beanServices = getBeanServices(linkedEntityId);
			if (beanServices != null) {
				final IReaderService<Void> readerService = beanServices.readerService();
				if (readerService != null) {
					if (readerService instanceof ISecureObject) {
						return !authorizationChecker.hasAuthorization((AUTHORIZATION_TYPE) ((ISecureObject<?>) readerService).getAuthorization());
					}
				}
			}
			return false;
		}
	}
}
