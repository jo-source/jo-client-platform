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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;

import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServiceProvider;
import org.jowidgets.service.api.IServiceProviderBuilder;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.builder.AbstractSingleUseBuilder;

@SuppressWarnings({"unchecked", "rawtypes"})
final class ServiceProviderBuilderImpl extends AbstractSingleUseBuilder<IServiceProvider> implements IServiceProviderBuilder {

	private final Map<IServiceId<? extends Object>, Object> services;
	private final List<IServicesDecoratorProvider> serviceDecorators;

	ServiceProviderBuilderImpl() {
		this.services = new HashMap<IServiceId<? extends Object>, Object>();
		this.serviceDecorators = getRegisteredServicesDecorators();
		sortDecorators();
	}

	@Override
	public IServiceProviderBuilder addServiceDecorator(final IServicesDecoratorProvider serviceDecorator) {
		Assert.paramNotNull(serviceDecorator, "serviceDecorator");
		serviceDecorators.add(serviceDecorator);
		sortDecorators();
		return this;
	}

	@Override
	public <SERVICE_TYPE> void addService(final IServiceId<? extends SERVICE_TYPE> id, final SERVICE_TYPE service) {
		Assert.paramNotNull(id, "id");
		Assert.paramNotNull(service, "service");
		services.put(id, service);
	}

	@Override
	protected IServiceProvider doBuild() {
		final Map<IServiceId<?>, Object> decoratedServices = new HashMap<IServiceId<?>, Object>();
		for (final Entry<IServiceId<? extends Object>, Object> entry : services.entrySet()) {
			final IServiceId<? extends Object> id = entry.getKey();
			decoratedServices.put(id, getDecoratedService(id, entry.getValue()));
		}
		return new ServiceProviderImpl(decoratedServices);
	}

	private Object getDecoratedService(final IServiceId id, Object result) {
		if (result != null) {
			for (final IServicesDecoratorProvider serviceDecorator : new LinkedList<IServicesDecoratorProvider>(serviceDecorators)) {
				final IDecorator<Object> defaultDecorator = serviceDecorator.getDefaultDecorator();
				if (defaultDecorator != null) {
					result = defaultDecorator.decorate(result);
				}
				final IDecorator<Object> decorator = serviceDecorator.getDecorator(id.getServiceType());
				if (decorator != null) {
					result = decorator.decorate(result);
				}
			}
		}
		return result;
	}

	private List<IServicesDecoratorProvider> getRegisteredServicesDecorators() {
		final List<IServicesDecoratorProvider> result = new LinkedList<IServicesDecoratorProvider>();
		final ServiceLoader<IServicesDecoratorProvider> widgetServiceLoader = ServiceLoader.load(IServicesDecoratorProvider.class);
		if (widgetServiceLoader != null) {
			final Iterator<IServicesDecoratorProvider> iterator = widgetServiceLoader.iterator();
			while (iterator.hasNext()) {
				result.add(iterator.next());
			}
		}
		return result;
	}

	private void sortDecorators() {
		Collections.sort(serviceDecorators, new Comparator<IServicesDecoratorProvider>() {
			@Override
			public int compare(final IServicesDecoratorProvider provider1, final IServicesDecoratorProvider provider2) {
				if (provider1 != null && provider2 != null) {
					return provider1.getOrder() - provider2.getOrder();
				}
				return 0;
			}
		});
	}
}
