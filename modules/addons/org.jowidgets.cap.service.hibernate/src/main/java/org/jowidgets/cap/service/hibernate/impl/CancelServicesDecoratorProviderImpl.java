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

package org.jowidgets.cap.service.hibernate.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.tools.proxy.AbstractCapServiceInvocationHandler;
import org.jowidgets.cap.service.jpa.api.EntityManagerFactoryProvider;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.concurrent.IThreadInterruptObservable;

final class CancelServicesDecoratorProviderImpl implements IServicesDecoratorProvider {

	private final IThreadInterruptObservable threadInterruptObservable;
	private final EntityManagerFactory entityManagerFactory;
	private final Set<Class<?>> services;
	private final Long killAfterMillis;
	private final Long minQueryRuntimeMillis;
	private final long waitForCancelSleepMillis;
	private final int order;

	CancelServicesDecoratorProviderImpl(
		final String persistenceUnitName,
		final IThreadInterruptObservable threadInterruptObservable,
		final Set<Class<?>> services,
		final Long minQueryRuntimeMillis,
		final Long killAfterMillis,
		final long waitForCancelSleepMillis,
		final int order) {

		Assert.paramNotNull(threadInterruptObservable, "threadInterruptObservable");
		Assert.paramNotNull(persistenceUnitName, "persistenceUnitName");
		Assert.paramNotNull(services, "services");

		this.threadInterruptObservable = threadInterruptObservable;
		this.entityManagerFactory = EntityManagerFactoryProvider.get(persistenceUnitName);
		if (entityManagerFactory == null && !services.isEmpty()) {
			throw new IllegalArgumentException(
				"Could not create an EntityManagerFactory for persistence unit name '" + persistenceUnitName + "'.");
		}

		this.services = new HashSet<Class<?>>(services);
		this.minQueryRuntimeMillis = minQueryRuntimeMillis;
		this.killAfterMillis = killAfterMillis;
		this.waitForCancelSleepMillis = waitForCancelSleepMillis;
		this.order = order;
	}

	@Override
	public <SERVICE_TYPE> IDecorator<SERVICE_TYPE> getDecorator(final IServiceId<SERVICE_TYPE> id) {
		Assert.paramNotNull(id, "id");
		final Class<? extends SERVICE_TYPE> serviceType = id.getServiceType();
		return new IDecorator<SERVICE_TYPE>() {
			@SuppressWarnings("unchecked")
			@Override
			public SERVICE_TYPE decorate(final SERVICE_TYPE original) {
				if (services.contains(serviceType)) {
					final InvocationHandler invocationHandler = new CancelInvocationHandler(original);
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

	private final class CancelInvocationHandler extends AbstractCapServiceInvocationHandler {

		private final Object original;

		private CancelInvocationHandler(final Object original) {
			this.original = original;
		}

		@Override
		protected Object invokeSyncSignature(final Method method, final Object[] args, final IExecutionCallback executionCallback)
				throws Throwable {
			try {
				return method.invoke(original, args);
			}
			catch (final Throwable e) {
				throw e;
			}
		}

		@Override
		protected Object invokeAsyncSignature(
			final Method method,
			final Object[] args,
			final int resultCallbackIndex,
			final IResultCallback<Object> resultCallback,
			final IExecutionCallback executionCallback) {

			return new CancelableInvoker(
				threadInterruptObservable,
				entityManagerFactory,
				original,
				method,
				args,
				resultCallbackIndex,
				resultCallback,
				executionCallback,
				minQueryRuntimeMillis,
				killAfterMillis,
				waitForCancelSleepMillis).invoke();
		}
	}

}
