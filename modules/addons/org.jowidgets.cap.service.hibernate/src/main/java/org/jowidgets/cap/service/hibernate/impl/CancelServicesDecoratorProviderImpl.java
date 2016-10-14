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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.jowidgets.cap.common.api.exception.ServiceCanceledException;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.execution.IUpdatableResultCallback;
import org.jowidgets.cap.common.tools.proxy.AbstractCapServiceInvocationHandler;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.jpa.api.EntityManagerFactoryProvider;
import org.jowidgets.cap.service.jpa.api.EntityManagerHolder;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;

final class CancelServicesDecoratorProviderImpl implements IServicesDecoratorProvider {

	private final EntityManagerFactory entityManagerFactory;
	private final Set<Class<?>> services;
	private final int order;

	CancelServicesDecoratorProviderImpl(final String persistenceUnitName, final Set<Class<?>> services, final int order) {

		Assert.paramNotNull(persistenceUnitName, "persistenceUnitName");
		Assert.paramNotNull(services, "services");

		this.entityManagerFactory = EntityManagerFactoryProvider.get(persistenceUnitName);
		if (entityManagerFactory == null && !services.isEmpty()) {
			throw new IllegalArgumentException(
				"Could not create an EntityManagerFactory for persistence unit name '" + persistenceUnitName + "'.");
		}

		this.services = new HashSet<Class<?>>(services);
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

			return new CancelableInvoker(original, method, args, resultCallbackIndex, resultCallback, executionCallback).invoke();
		}

	}

	private final class CancelableInvoker {

		private final Object original;
		private final Method method;
		private final Object[] args;
		private final int resultCallbackIndex;
		private final IResultCallback<Object> resultCallback;
		private final IExecutionCallback executionCallback;
		private final AtomicBoolean isRunning;
		private final AtomicReference<Session> canceledSessionHolder;

		private CancelableInvoker(
			final Object original,
			final Method method,
			final Object[] args,
			final int resultCallbackIndex,
			final IResultCallback<Object> resultCallback,
			final IExecutionCallback executionCallback) {

			this.isRunning = new AtomicBoolean(false);
			this.canceledSessionHolder = new AtomicReference<Session>();
			this.original = original;
			this.method = method;
			this.args = args;
			this.resultCallbackIndex = resultCallbackIndex;
			this.resultCallback = resultCallback;
			this.executionCallback = executionCallback;
		}

		private Object invoke() {

			final IResultCallback<Object> decoratedResultCallback = createDecoratedResultCallback();

			args[resultCallbackIndex] = decoratedResultCallback;

			try {
				addCancelListener(executionCallback);
				CapServiceToolkit.checkCanceled(executionCallback);
				isRunning.set(true);
				final Object result = method.invoke(original, args);
				return result;
			}
			catch (final Exception e) {
				decoratedResultCallback.exception(e);
				return null;
			}

		}

		private IResultCallback<Object> createDecoratedResultCallback() {
			if (resultCallback instanceof IUpdatableResultCallback<?, ?>) {
				return new DecoratedUpdateCallback();
			}
			else {
				return new DecoratedResultCallback();
			}
		}

		private void checkCanceled() {
			final Session canceledSession = canceledSessionHolder.get();
			if (canceledSession != null && canceledSession.isOpen()) {
				try {
					//If the session was canceled without any hibernate exception,
					//the connection is in a dirty state what may lead
					//to a timeout exception in future when connection will be recycled from
					//another session.
					//To avoid this, the connection will be rolled back. This only
					//works if the release mode (hibernate.connection.release_mode) is set to 'on_close'
					canceledSession.doWork(new Work() {
						@Override
						public void execute(final Connection connection) throws SQLException {
							connection.rollback();
						}
					});
				}
				catch (final Exception e) {
					//this exception can be ignored, because the service was already canceled
				}
			}
		}

		private void addCancelListener(final IExecutionCallback executionCallback) {
			final EntityManager em = EntityManagerHolder.get();
			if (executionCallback != null && em != null) {
				final Session session = em.unwrap(Session.class);
				executionCallback.addExecutionCallbackListener(new IExecutionCallbackListener() {
					@Override
					public void canceled() {
						try {
							if (session.isOpen() && isRunning.get()) {
								canceledSessionHolder.set(session);
								session.cancelQuery();
							}
						}
						catch (final Exception e) {
						}
					}
				});
			}
		}

		private class DecoratedResultCallback implements IResultCallback<Object> {

			@Override
			public final void finished(final Object result) {
				isRunning.set(false);
				checkCanceled();
				resultCallback.finished(result);
			}

			@Override
			public final void exception(final Throwable exception) {
				isRunning.set(false);
				checkCanceled();
				if (executionCallback != null
					&& executionCallback.isCanceled()
					&& !(exception instanceof ServiceCanceledException)) {
					resultCallback.exception(new ServiceCanceledException());
				}
				else {
					resultCallback.exception(exception);
				}
			}
		}

		private class DecoratedUpdateCallback extends DecoratedResultCallback
				implements IUpdatableResultCallback<Object, Object> {

			@SuppressWarnings("unchecked")
			@Override
			public void update(final Object result) {
				checkCanceled();
				((IUpdatableResultCallback<Object, Object>) resultCallback).update(result);
			}

			@SuppressWarnings("unchecked")
			@Override
			public void updatesFinished() {
				checkCanceled();
				((IUpdatableResultCallback<Object, Object>) resultCallback).updatesFinished();
			}

			@SuppressWarnings("unchecked")
			@Override
			public void exceptionOnUpdate(final Throwable exception) {
				checkCanceled();
				((IUpdatableResultCallback<Object, Object>) resultCallback).exceptionOnUpdate(exception);
			}

		}

	}

}
