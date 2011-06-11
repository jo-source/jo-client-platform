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

import org.jowidgets.cap.invocation.common.CapInvocationMethodNames;
import org.jowidgets.invocation.service.client.api.IInvocationServiceClient;
import org.jowidgets.invocation.service.client.api.InvocationServiceClientToolkit;
import org.jowidgets.invocation.service.common.api.IMethodInvocationService;
import org.jowidgets.service.tools.ServiceId;
import org.jowidgets.service.tools.ServiceProviderBuilder;

final class CapClientServiceProviderBuilder extends ServiceProviderBuilder {

	public CapClientServiceProviderBuilder() {
		super();
		final IInvocationServiceClient invocationServiceClient = InvocationServiceClientToolkit.getClient();
		final IMethodInvocationService<List<ServiceId<Object>>, Void, Void, Void, Void> methodService;
		methodService = invocationServiceClient.getMethodService(CapInvocationMethodNames.SERVICE_LOCATOR_METHOD_NAME);

		final SyncInvocationCallback<List<ServiceId<Object>>> invocationCallback = new SyncInvocationCallback<List<ServiceId<Object>>>();

		methodService.invoke(invocationCallback, null, null, null);

		addServices(invocationCallback.getResultSynchronious());
	}

	private void addServices(final List<ServiceId<Object>> serviceIds) {
		for (final ServiceId<Object> serviceId : serviceIds) {
			addService(serviceId);
		}
	}

	private void addService(final ServiceId<Object> serviceId) {
		final Class<?> serviceType = serviceId.getServiceType();
		final Object service = getService(serviceType);
		addService(serviceId, service);
	}

	private Object getService(final Class<?> serviceType) {
		//TODO
		return new Object();
	}
}
