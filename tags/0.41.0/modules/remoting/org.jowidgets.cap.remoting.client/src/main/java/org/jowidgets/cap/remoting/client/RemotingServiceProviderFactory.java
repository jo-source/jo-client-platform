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

package org.jowidgets.cap.remoting.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Set;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IUserQuestionCallback;
import org.jowidgets.cap.common.api.execution.UserQuestionResult;
import org.jowidgets.cap.remoting.common.CapInvocationMethodNames;
import org.jowidgets.cap.remoting.common.RemotingBrokerId;
import org.jowidgets.invocation.service.client.api.IInvocationServiceClient;
import org.jowidgets.invocation.service.client.api.InvocationServiceClientToolkit;
import org.jowidgets.invocation.service.common.api.IMethodInvocationService;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServiceProvider;
import org.jowidgets.service.tools.ServiceProviderBuilder;
import org.jowidgets.util.Assert;
import org.jowidgets.util.ICancelCallback;
import org.jowidgets.util.collection.IObserverSet;
import org.jowidgets.util.collection.IObserverSetFactory.Strategy;
import org.jowidgets.util.collection.ObserverSetFactory;
import org.jowidgets.util.event.ICancelListener;

public final class RemotingServiceProviderFactory {

	private RemotingServiceProviderFactory() {}

	/**
	 * Creates the service provider for the remote services of the default broker
	 * 
	 * @return The ServiceProvider
	 * 
	 * @throws RemotingTimeoutException if the timeout elapsed before initialization
	 */
	public static IServiceProvider create() {
		return create(RemotingBrokerId.DEFAULT_BROKER_ID);
	}

	/**
	 * Creates the service provider for the remote services defined by the broker id
	 * 
	 * @param brokerId The broker id to get the remote services for
	 * 
	 * @return The ServiceProvider
	 * 
	 * @throws RemotingTimeoutException if the timeout elapsed before initialization
	 */
	public static IServiceProvider create(final Object brokerId) {
		return create(brokerId, SyncInvocationCallback.DEFAULT_TIMEOUT);
	}

	/**
	 * Creates the service provider for the remote services defined by the broker id
	 * 
	 * @param brokerId The broker id to get the remote services for
	 * @param timeout The timeout after giving up, e.g. if server is not available
	 * 
	 * @return The ServiceProvider
	 * 
	 * @throws RemotingTimeoutException if the timeout elapsed before initialization
	 */
	public static IServiceProvider create(final Object brokerId, final long timeout) {
		return create(brokerId, timeout, null);
	}

	/**
	 * Creates the service provider for the remote services defined by the broker id
	 * 
	 * @param brokerId The broker id to get the remote services for
	 * @param timeout The timeout after giving up, e.g. if server is not available
	 * @param cancelCallback A cancel callback that can be used to cancel the creation
	 * 
	 * @return The ServiceProvider
	 * 
	 * @throws RemotingTimeoutException if the timeout elapsed before initialization
	 */
	public static IServiceProvider create(final Object brokerId, final long timeout, final ICancelCallback cancelCallback) {
		final ServiceProviderBuilder builder = new ServiceProviderBuilder();

		final IInvocationServiceClient invocationServiceClient = InvocationServiceClientToolkit.getClient(brokerId);
		final IMethodInvocationService<Set<? extends IServiceId<?>>, Void, Void, Void, Void> methodService;
		methodService = invocationServiceClient.getMethodService(CapInvocationMethodNames.SERVICE_LOCATOR_METHOD_NAME);
		final SyncInvocationCallback<Set<? extends IServiceId<?>>> invocationCallback;

		invocationCallback = new SyncInvocationCallback<Set<? extends IServiceId<?>>>(
			getExecutionCallback(cancelCallback),
			timeout);

		methodService.invoke(invocationCallback, null, null, null);
		addServices(builder, brokerId, invocationCallback.getResultSynchronious());

		return builder.build();
	}

	private static IExecutionCallback getExecutionCallback(final ICancelCallback cancelCallback) {
		if (cancelCallback != null) {
			return new CancelCallbackToExecutionCallbackAdapter(cancelCallback);
		}
		else {
			return null;
		}
	}

	private static void addServices(
		final ServiceProviderBuilder builder,
		final Object brokerId,
		final Set<? extends IServiceId<?>> serviceIds) {
		for (final IServiceId<?> serviceId : serviceIds) {
			addService(builder, brokerId, serviceId);
		}
	}

	private static void addService(final ServiceProviderBuilder builder, final Object brokerId, final IServiceId<?> serviceId) {
		builder.addService(serviceId, getService(serviceId, brokerId));
	}

	private static Object getService(final IServiceId<?> serviceId, final Object brokerId) {
		final Class<?> serviceType = serviceId.getServiceType();
		final InvocationHandler invocationHandler = new RemoteMethodInvocationHandler(brokerId, serviceId);
		return Proxy.newProxyInstance(serviceType.getClassLoader(), new Class[] {serviceType}, invocationHandler);
	}

	private static final class CancelCallbackToExecutionCallbackAdapter implements IExecutionCallback {

		private final ICancelCallback cancelCallback;
		private final IObserverSet<IExecutionCallbackListener> executionCallbackListeners;

		private CancelCallbackToExecutionCallbackAdapter(final ICancelCallback cancelCallback) {
			Assert.paramNotNull(cancelCallback, "cancelCallback");
			this.cancelCallback = cancelCallback;
			this.executionCallbackListeners = ObserverSetFactory.create(Strategy.HIGH_PERFORMANCE);

			cancelCallback.addCancelListener(new ICancelListener() {
				@Override
				public void canceled() {
					for (final IExecutionCallbackListener listener : executionCallbackListeners) {
						listener.canceled();
					}
				}
			});
		}

		@Override
		public boolean isCanceled() {
			return cancelCallback.isCanceled();
		}

		@Override
		public void addExecutionCallbackListener(final IExecutionCallbackListener listener) {
			executionCallbackListeners.add(listener);
		}

		@Override
		public void removeExecutionCallbackListener(final IExecutionCallbackListener listener) {
			executionCallbackListeners.remove(listener);
		}

		@Override
		public void setTotalStepCount(final int stepCount) {}

		@Override
		public void worked(final int stepCount) {}

		@Override
		public void workedOne() {}

		@Override
		public void setDescription(final String description) {}

		@Override
		public void finshed() {}

		@Override
		public UserQuestionResult userQuestion(final String question) {
			return UserQuestionResult.NO;
		}

		@Override
		public void userQuestion(final String question, final IUserQuestionCallback callback) {
			callback.questionAnswered(UserQuestionResult.NO);
		}

		@Override
		public IExecutionCallback createSubExecution(final int stepProportion) {
			return null;
		}

	}
}
