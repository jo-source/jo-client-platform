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

package org.jowidgets.cap.service.jpa.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.jpa.api.EntityManagerFactoryProvider;
import org.jowidgets.cap.service.jpa.api.EntityManagerHolder;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.IExceptionLogger;
import org.jowidgets.util.Tuple;

final class JpaServicesDecoratorProviderImpl implements IServicesDecoratorProvider {

	private final EntityManagerFactory entityManagerFactory;
	private final Set<Class<?>> entityManagerServices;
	private final Set<Class<?>> transactionalServices;
	private final IDecorator<Throwable> exceptionDecorator;
	private final IExceptionLogger exceptionLogger;
	private final int order;

	JpaServicesDecoratorProviderImpl(
		final String persistenceUnitName,
		final Set<Class<?>> entityManagerServices,
		final Set<Class<?>> transactionalServices,
		final IDecorator<Throwable> exceptionDecorator,
		final IExceptionLogger exceptionLogger,
		final int order) {

		Assert.paramNotNull(persistenceUnitName, "persistenceUnitName");
		Assert.paramNotNull(entityManagerServices, "entityManagerServices");
		Assert.paramNotNull(transactionalServices, "transactionalServices");
		Assert.paramNotNull(exceptionDecorator, "exceptionDecorator");
		Assert.paramNotNull(exceptionLogger, "exceptionLogger");

		this.entityManagerFactory = EntityManagerFactoryProvider.get(persistenceUnitName);
		if (entityManagerFactory == null && !entityManagerServices.isEmpty()) {
			throw new IllegalArgumentException("Could not create an EntityManagerFactory for persistence unit name '"
				+ persistenceUnitName
				+ "'.");
		}

		this.entityManagerServices = new HashSet<Class<?>>(entityManagerServices);
		this.transactionalServices = new HashSet<Class<?>>(transactionalServices);
		this.exceptionDecorator = exceptionDecorator;
		this.exceptionLogger = exceptionLogger;
		this.order = order;
	}

	@Override
	public IDecorator<Object> getDefaultDecorator() {
		return null;
	}

	@Override
	public <SERVICE_TYPE> IDecorator<SERVICE_TYPE> getDecorator(final Class<? extends SERVICE_TYPE> serviceType) {
		return new IDecorator<SERVICE_TYPE>() {
			@SuppressWarnings("unchecked")
			@Override
			public SERVICE_TYPE decorate(final SERVICE_TYPE original) {
				if (entityManagerServices.contains(serviceType) || transactionalServices.contains(serviceType)) {
					final InvocationHandler invocationHandler = new JpaInvocationHandler(serviceType, original);
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

	private final class JpaInvocationHandler implements InvocationHandler {

		private final Object original;
		private final boolean entityManagerService;
		private final boolean transactionalService;

		private JpaInvocationHandler(final Class<?> serviceType, final Object original) {
			this.original = original;
			this.entityManagerService = entityManagerServices.contains(serviceType);
			this.transactionalService = transactionalServices.contains(serviceType);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			final Class<?>[] parameterTypes = method.getParameterTypes();

			final int resultCallbackIndex = getFirstMatchingIndex(IResultCallback.class, parameterTypes);

			if (resultCallbackIndex == -1) {
				return invokeSyncSignature(method, args);
			}
			else {
				final int executionCallbackIndex = getFirstMatchingIndex(IExecutionCallback.class, parameterTypes);
				final IExecutionCallback executionCallback;
				if (executionCallbackIndex != -1) {
					executionCallback = (IExecutionCallback) args[executionCallbackIndex];
				}
				else {
					executionCallback = null;
				}
				return invokeAsyncSignature(method, args, resultCallbackIndex, executionCallback);
			}
		}

		private Object invokeSyncSignature(final Method method, final Object[] args) throws Throwable {
			Tuple<EntityManager, Boolean> entityManagerTuple = null;
			Tuple<EntityTransaction, Boolean> transactionTuple = null;
			try {
				entityManagerTuple = entityManagerBegin();
				transactionTuple = transactionBegin();
				final Object result = method.invoke(original, args);
				transactionCommit(transactionTuple);
				return result;
			}
			catch (final Throwable e) {
				throw decorateException(e);
			}
			finally {
				try {
					transactionRollback(transactionTuple);
				}
				catch (final Exception e) {
				}
				entityManagerEnd(entityManagerTuple);
			}
		}

		private Object invokeAsyncSignature(
			final Method method,
			final Object[] args,
			final int resultCallbackIndex,
			final IExecutionCallback executionCallback) throws Exception {

			@SuppressWarnings("unchecked")
			final IResultCallback<Object> resultCallback = (IResultCallback<Object>) args[resultCallbackIndex];

			final Tuple<EntityManager, Boolean> entityManagerTuple;
			final Tuple<EntityTransaction, Boolean> transactionTuple;

			try {
				entityManagerTuple = entityManagerBegin();
				transactionTuple = transactionBegin();
			}
			catch (final Exception exception) {
				resultCallback.exception(decorateException(exception));
				return null;
			}

			final IResultCallback<Object> decoratedResultCallback = new IResultCallback<Object>() {

				@Override
				public void finished(final Object result) {
					try {
						CapServiceToolkit.checkCanceled(executionCallback);
						transactionCommit(transactionTuple);
					}
					catch (final Exception e) {
						exception(e);
						return;
					}
					finally {
						try {
							transactionRollback(transactionTuple);
						}
						catch (final Exception e) {
							exception(e);
							return;
						}
						finally {
							entityManagerEnd(entityManagerTuple);
						}
					}
					CapServiceToolkit.checkCanceled(executionCallback);
					resultCallback.finished(result);
				}

				@Override
				public void exception(final Throwable exception) {
					resultCallback.exception(decorateException(exception));
					try {
						transactionRollback(transactionTuple);
					}
					catch (final Exception e) {
						//ignore this exception, because earlier an exception was thrown
						//what may be the cause of this exception
					}
					finally {
						entityManagerEnd(entityManagerTuple);
					}
				}

			};

			args[resultCallbackIndex] = decoratedResultCallback;

			try {
				CapServiceToolkit.checkCanceled(executionCallback);
				return method.invoke(original, args);
			}
			catch (final Exception exception) {
				decoratedResultCallback.exception(exception);
				return null;
			}
		}

		private int getFirstMatchingIndex(final Class<?> interfaceType, final Class<?>[] paramTypes) {
			if (paramTypes != null) {
				for (int i = 0; i < paramTypes.length; i++) {
					if (interfaceType.isAssignableFrom(paramTypes[i])) {
						return i;
					}
				}
			}
			return -1;
		}

		private Throwable decorateException(final Throwable exception) {
			exceptionLogger.log(exception);
			return exceptionDecorator.decorate(exception);
		}

		private Tuple<EntityTransaction, Boolean> transactionBegin() {
			boolean started = false;
			final EntityManager entityManager = EntityManagerHolder.get();
			EntityTransaction tx = null;
			if (transactionalService && entityManager != null) {
				tx = entityManager.getTransaction();
				if (tx != null && !tx.isActive()) {
					tx.begin();
					started = true;
				}
			}
			return new Tuple<EntityTransaction, Boolean>(tx, Boolean.valueOf(started));
		}

		private void transactionCommit(final Tuple<EntityTransaction, Boolean> transactionTuple) throws Exception {
			final EntityTransaction tx = transactionTuple.getFirst();
			//only commit the transaction if it is active and started by this decorator
			if (transactionalService && tx != null && tx.isActive() && transactionTuple.getSecond().booleanValue()) {
				tx.commit();
			}
		}

		private void transactionRollback(final Tuple<EntityTransaction, Boolean> transactionTuple) throws Exception {
			final EntityTransaction tx = transactionTuple.getFirst();
			//only rollback the transaction if it is active and started by this decorator
			if (transactionalService && tx != null && tx.isActive() && transactionTuple.getSecond().booleanValue()) {
				tx.rollback();
			}
		}

		private Tuple<EntityManager, Boolean> entityManagerBegin() {
			boolean created = false;
			EntityManager entityManager = EntityManagerHolder.get();
			if (entityManagerService && entityManager == null) {
				entityManager = entityManagerFactory.createEntityManager();
				EntityManagerHolder.set(entityManager);
				created = true;
			}
			return new Tuple<EntityManager, Boolean>(entityManager, Boolean.valueOf(created));
		}

		private void entityManagerEnd(final Tuple<EntityManager, Boolean> entityManagerTuple) {
			//only close the entity manager if it was created by this decorator
			final EntityManager entityManager = entityManagerTuple.getFirst();
			if (entityManagerService && entityManager != null && entityManager.isOpen() && entityManagerTuple.getSecond()) {
				entityManagerTuple.getFirst().close();
				EntityManagerHolder.set(null);
			}
		}

	}

}
