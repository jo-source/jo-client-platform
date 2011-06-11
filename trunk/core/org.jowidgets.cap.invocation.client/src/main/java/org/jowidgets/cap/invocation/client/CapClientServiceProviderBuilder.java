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

package org.jowidgets.cap.invocation.client;

import java.util.List;
import java.util.Set;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.UserQuestionResult;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.IExecutorService;
import org.jowidgets.cap.invocation.common.CapInvocationMethodNames;
import org.jowidgets.invocation.service.client.api.IInvocationServiceClient;
import org.jowidgets.invocation.service.client.api.InvocationServiceClientToolkit;
import org.jowidgets.invocation.service.common.api.IMethodInvocationService;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.tools.ServiceProviderBuilder;

final class CapClientServiceProviderBuilder extends ServiceProviderBuilder {

	public CapClientServiceProviderBuilder() {
		super();
		final IInvocationServiceClient invocationServiceClient = InvocationServiceClientToolkit.getClient();
		final IMethodInvocationService<Set<IServiceId<?>>, Void, Void, Void, Void> methodService;
		methodService = invocationServiceClient.getMethodService(CapInvocationMethodNames.SERVICE_LOCATOR_METHOD_NAME);

		final SyncInvocationCallback<Set<IServiceId<?>>> invocationCallback = new SyncInvocationCallback<Set<IServiceId<?>>>();

		methodService.invoke(invocationCallback, null, null, null);

		addServices(invocationCallback.getResultSynchronious());
	}

	private void addServices(final Set<IServiceId<?>> serviceIds) {
		for (final IServiceId<?> serviceId : serviceIds) {
			addService(serviceId);
		}
	}

	private void addService(final IServiceId<?> serviceId) {
		final Class<?> serviceType = serviceId.getServiceType();
		final Object service = getService(serviceType);
		addService(serviceId, service);
	}

	private Object getService(final Class<?> serviceType) {
		//TODO only for test purpose
		if (serviceType.isAssignableFrom(IExecutorService.class)) {
			return new IExecutorService<Object>() {
				@Override
				public List<IBeanDto> execute(
					final List<? extends IBeanKey> beanKeys,
					final Object parameter,
					final IExecutionCallback executionCallback) {

					final IInvocationServiceClient invocationServiceClient = InvocationServiceClientToolkit.getClient();
					final IMethodInvocationService<List<IBeanDto>, Void, String, UserQuestionResult, Object> methodService;
					methodService = invocationServiceClient.getMethodService(CapInvocationMethodNames.EXECUTOR_SERVICE_EXECUTE);

					final SyncInvocationCallback<List<IBeanDto>> invocationCallback = new SyncInvocationCallback<List<IBeanDto>>();
					methodService.invoke(invocationCallback, null, null, parameter);

					return invocationCallback.getResultSynchronious();
				}
			};
		}
		else if (serviceType.isAssignableFrom(IEntityService.class)) {
			return new IEntityService() {

				@Override
				public <BEAN_TYPE> IBeanDtoDescriptor<BEAN_TYPE> getDescriptor(final Class<BEAN_TYPE> beanType) {
					return null;
				}

				@Override
				public <BEAN_TYPE> IBeanServicesProvider<BEAN_TYPE> getBeanServices(final Class<BEAN_TYPE> beanType) {
					return null;
				}
			};
		}
		return new Object();
	}
}
