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

package org.jowidgets.cap.service.neo4J.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.execution.IUpdatableResultCallback;
import org.jowidgets.cap.common.tools.proxy.AbstractCapServiceInvocationHandler;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.exception.IServiceExceptionLogger;
import org.jowidgets.cap.service.neo4j.api.GraphDBConfig;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.IExceptionLogger;
import org.neo4j.graphdb.Transaction;

final class Neo4JServicesDecoratorProviderImpl implements IServicesDecoratorProvider {

	private final Set<Class<?>> transactionalServices;
	private final List<IDecorator<Throwable>> exceptionDecorators;
	private final IServiceExceptionLogger exceptionLogger;
	private final IExceptionLogger deprecatedExceptionLogger;
	private final int order;

	Neo4JServicesDecoratorProviderImpl(
		final Set<Class<?>> transactionalServices,
		final List<IDecorator<Throwable>> exceptionDecorators,
		final IExceptionLogger deprecatedExceptionLogger,
		final IServiceExceptionLogger exceptionLogger,
		final int order) {

		Assert.paramNotNull(transactionalServices, "transactionalServices");
		Assert.paramNotNull(exceptionDecorators, "exceptionDecorators");
		Assert.paramNotNull(deprecatedExceptionLogger, "deprecatedExceptionLogger");
		Assert.paramNotNull(exceptionLogger, "exceptionLogger");

		this.transactionalServices = new HashSet<Class<?>>(transactionalServices);
		this.exceptionDecorators = new LinkedList<IDecorator<Throwable>>(exceptionDecorators);
		this.deprecatedExceptionLogger = deprecatedExceptionLogger;
		this.exceptionLogger = exceptionLogger;
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
				final InvocationHandler invocationHandler = new Neo4JInvocationHandler(serviceType, original);
				return (SERVICE_TYPE) Proxy.newProxyInstance(
						serviceType.getClassLoader(),
						new Class[] {serviceType},
						invocationHandler);
			}
		};
	}

	@Override
	public int getOrder() {
		return order;
	}

	private final class Neo4JInvocationHandler extends AbstractCapServiceInvocationHandler {

		private final Class<?> serviceType;
		private final Object original;
		private final boolean transactionalService;

		private Neo4JInvocationHandler(final Class<?> serviceType, final Object original) {
			this.serviceType = serviceType;
			this.original = original;
			this.transactionalService = transactionalServices.contains(serviceType);
		}

		@Override
		protected Object invokeSyncSignature(final Method method, final Object[] args, final IExecutionCallback executionCallback)
				throws Throwable {
			if (transactionalService) {
				final Transaction tx = GraphDBConfig.getGraphDbService().beginTx();
				try {
					CapServiceToolkit.checkCanceled(executionCallback);
					final Object result = method.invoke(original, args);
					CapServiceToolkit.checkCanceled(executionCallback);
					tx.success();
					return result;
				}
				catch (final Throwable e) {
					tx.failure();
					throw decorateException(e, executionCallback);
				}
				finally {
					tx.finish();
				}
			}
			else {
				try {
					CapServiceToolkit.checkCanceled(executionCallback);
					final Object result = method.invoke(original, args);
					CapServiceToolkit.checkCanceled(executionCallback);
					return result;
				}
				catch (final Throwable e) {
					throw decorateException(e, executionCallback);
				}
			}
		}

		@Override
		protected Object invokeAsyncSignature(
			final Method method,
			final Object[] args,
			final int resultCallbackIndex,
			final IResultCallback<Object> resultCallback,
			final IExecutionCallback executionCallback) {

			final Transaction tx;
			if (transactionalService) {
				tx = GraphDBConfig.getGraphDbService().beginTx();
			}
			else {
				tx = null;
			}

			final IResultCallback<Object> decoratedResultCallback = createDecoratedResultCallback(
					resultCallback,
					executionCallback,
					tx);

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

		@SuppressWarnings("unchecked")
		private IResultCallback<Object> createDecoratedResultCallback(
			final IResultCallback<Object> original,
			final IExecutionCallback executionCallback,
			final Transaction tx) {
			if (original instanceof IUpdatableResultCallback<?, ?>) {
				return new DecoratedUpdatableResultCallback(
					(IUpdatableResultCallback<Object, Object>) original,
					executionCallback,
					tx);
			}
			else {
				return new DecoratedResultCallback(original, executionCallback, tx);
			}
		}

		private Throwable decorateException(final Throwable exception, final IExecutionCallback executionCallback) {
			if (executionCallback == null || !executionCallback.isCanceled()) {
				deprecatedExceptionLogger.log(exception);
			}
			Throwable result = exception;
			for (final IDecorator<Throwable> exceptionDecorator : exceptionDecorators) {
				result = exceptionDecorator.decorate(result);
			}
			exceptionLogger.log(serviceType, exception, result);
			return result;
		}

		private class DecoratedResultCallback implements IResultCallback<Object> {

			private final IResultCallback<Object> original;
			private final IExecutionCallback executionCallback;
			private final Transaction tx;

			DecoratedResultCallback(
				final IResultCallback<Object> original,
				final IExecutionCallback executionCallback,
				final Transaction tx) {

				Assert.paramNotNull(original, "original");
				Assert.paramNotNull(executionCallback, "executionCallback");

				this.original = original;
				this.executionCallback = executionCallback;
				this.tx = tx;
			}

			@Override
			public final void finished(final Object result) {
				try {
					CapServiceToolkit.checkCanceled(executionCallback);
					if (tx != null) {
						tx.success();
					}
				}
				catch (final Exception e) {
					exception(e);
					return;
				}
				finally {
					if (tx != null) {
						tx.finish();
					}
				}
				original.finished(result);
			}

			@Override
			public final void exception(final Throwable exception) {
				try {
					if (tx != null) {
						tx.failure();
					}
				}
				finally {
					if (tx != null) {
						tx.finish();
					}
				}
				original.exception(decorateException(exception, executionCallback));
			}

		}

		private final class DecoratedUpdatableResultCallback extends DecoratedResultCallback
				implements IUpdatableResultCallback<Object, Object> {

			private final IUpdatableResultCallback<Object, Object> original;

			DecoratedUpdatableResultCallback(
				final IUpdatableResultCallback<Object, Object> original,
				final IExecutionCallback executionCallback,
				final Transaction tx) {
				super(original, executionCallback, tx);
				this.original = original;
			}

			@Override
			public void update(final Object result) {
				original.update(result);
			}

			@Override
			public void updatesFinished() {
				// TODO Auto-generated method stub

			}

			@Override
			public void exceptionOnUpdate(final Throwable exception) {
				// TODO Auto-generated method stub
			}

		}

	}

}
