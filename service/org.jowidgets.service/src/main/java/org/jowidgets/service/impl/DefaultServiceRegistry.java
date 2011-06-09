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

package org.jowidgets.service.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.jowidgets.service.api.IServiceDecoratorPlugin;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServiceProvider;
import org.jowidgets.service.api.IServiceRegistry;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;

public class DefaultServiceRegistry implements IServiceRegistry, IServiceProvider {

	private final Map<IServiceId<?>, Object> services;
	private final Map<IServiceId<?>, Object> decoratedServices;
	private final List<IServiceDecoratorPlugin> serviceDecorators;

	public DefaultServiceRegistry() {
		this.services = new HashMap<IServiceId<?>, Object>();
		this.decoratedServices = new HashMap<IServiceId<?>, Object>();
		this.serviceDecorators = getRegisteredServicesDecorators();
	}

	@Override
	public synchronized void addServiceDecorator(final IServiceDecoratorPlugin serviceDecoratorPlugin) {
		serviceDecorators.add(serviceDecoratorPlugin);
		decoratedServices.clear();
	}

	@Override
	public final <SERVICE_TYPE> SERVICE_TYPE get(final IServiceId<SERVICE_TYPE> id) {
		return getDecoratedService(id);
	}

	@Override
	public final <SERVICE_TYPE> void register(final IServiceId<? extends SERVICE_TYPE> id, final SERVICE_TYPE service) {
		Assert.paramNotNull(id, "id");
		Assert.paramNotNull(service, "service");
		services.put(id, service);

	}

	@Override
	public final Set<IServiceId<?>> getAvailableServices() {
		return services.keySet();
	}

	@SuppressWarnings("unchecked")
	private <SERVICE_TYPE> SERVICE_TYPE getDecoratedService(final IServiceId<SERVICE_TYPE> id) {
		Assert.paramNotNull(id, "id");
		SERVICE_TYPE result = (SERVICE_TYPE) decoratedServices.get(id);
		if (result == null) {
			result = (SERVICE_TYPE) services.get(id);
			if (result != null) {
				for (final IServiceDecoratorPlugin serviceDecorator : new LinkedList<IServiceDecoratorPlugin>(serviceDecorators)) {
					final IDecorator<Object> defaultDecorator = serviceDecorator.getDefaultDecorator();
					if (defaultDecorator != null) {
						defaultDecorator.decorate(result);
					}
					final IDecorator<SERVICE_TYPE> decorator = serviceDecorator.getDecorator(id.getServiceType());
					if (decorator != null) {
						result = decorator.decorate(result);
					}
				}
			}
			decoratedServices.put(id, result);
		}
		return result;
	}

	private List<IServiceDecoratorPlugin> getRegisteredServicesDecorators() {
		final List<IServiceDecoratorPlugin> result = new LinkedList<IServiceDecoratorPlugin>();
		final ServiceLoader<IServiceDecoratorPlugin> widgetServiceLoader = ServiceLoader.load(IServiceDecoratorPlugin.class);
		if (widgetServiceLoader != null) {
			final Iterator<IServiceDecoratorPlugin> iterator = widgetServiceLoader.iterator();
			while (iterator.hasNext()) {
				result.add(iterator.next());
			}
		}
		return result;
	}

}
