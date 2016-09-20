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

package org.jowidgets.service.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jowidgets.classloading.api.SharedClassLoader;
import org.jowidgets.util.Assert;

public final class ServiceProvider {

	private static CompositeServiceProviderHolder compositeServiceProviderHolder;
	private static List<IServiceProviderDecoratorHolder> serviceProviderDecorators;
	private static Map<IServiceId<?>, IRedundantServiceResolver<?>> redundantServiceResolvers;

	private ServiceProvider() {}

	/**
	 * Resets the service provider.
	 * 
	 * Use this method with care because this will remove all registered services, decorators and resolvers.
	 * 
	 * The method was designed to be used in junit tests only.
	 */
	public static synchronized void reset() {
		compositeServiceProviderHolder = null;
		serviceProviderDecorators = null;
		redundantServiceResolvers = null;
	}

	public static synchronized void registerServiceProviderHolder(final IServiceProviderHolder serviceProviderHolder) {
		Assert.paramNotNull(serviceProviderHolder, "serviceProviderHolder");
		getCompositeServiceProviderHolder().add(serviceProviderHolder);
	}

	public static synchronized void registerServiceProviderDecorator(final IServiceProviderDecoratorHolder decorator) {
		Assert.paramNotNull(decorator, "decorator");
		final List<IServiceProviderDecoratorHolder> decorators = getServiceProviderDecorators();
		decorators.add(decorator);
		sortDecorators(decorators);
	}

	public static synchronized void registerRedundantServiceResolver(final IRedundantServiceResolver<?> resolver) {
		Assert.paramNotNull(resolver, "resolver");
		Assert.paramNotNull(resolver.getServiceId(), "resolver.getServiceId()");
		final Map<IServiceId<?>, IRedundantServiceResolver<?>> serviceResolvers = getRedunantServiceResolvers();
		final IServiceId<?> serviceId = resolver.getServiceId();
		if (!serviceResolvers.containsKey(serviceId)) {
			serviceResolvers.put(serviceId, resolver);
		}
		else {
			throw new IllegalStateException("There is already a service resolver registred for the id '" + serviceId + "'");
		}
	}

	private static synchronized CompositeServiceProviderHolder getCompositeServiceProviderHolder() {
		if (compositeServiceProviderHolder == null) {
			compositeServiceProviderHolder = new CompositeServiceProviderHolder();
			final ServiceLoader<IServiceProviderHolder> widgetServiceLoader = ServiceLoader.load(
					IServiceProviderHolder.class,
					SharedClassLoader.getCompositeClassLoader());
			final Iterator<IServiceProviderHolder> iterator = widgetServiceLoader.iterator();
			while (iterator.hasNext()) {
				compositeServiceProviderHolder.add(iterator.next());
			}
		}
		return compositeServiceProviderHolder;
	}

	public static IServiceProvider getInstance() {
		return getDecoratedInstance();
	}

	public static Set<IServiceId<?>> getAvailableServices() {
		return getInstance().getAvailableServices();
	}

	public static <SERVICE_TYPE> SERVICE_TYPE getService(final IServiceId<SERVICE_TYPE> id) {
		return getInstance().get(id);
	}

	private static IServiceProvider getDecoratedInstance() {
		IServiceProvider result = getUndecoratedInstance();
		for (final IServiceProviderDecoratorHolder decorator : getServiceProviderDecorators()) {
			result = decorator.getDecorator().decorate(result);
			if (result == null) {
				throw new IllegalStateException("Decorator must not return null: " + decorator.getClass().getName());
			}
		}
		return result;
	}

	private static IServiceProvider getUndecoratedInstance() {
		return getCompositeServiceProviderHolder().getServiceProvider();
	}

	private static List<IServiceProviderDecoratorHolder> getServiceProviderDecorators() {
		if (serviceProviderDecorators == null) {
			serviceProviderDecorators = createServiceProviderDecorators();
		}
		return serviceProviderDecorators;
	}

	private static Map<IServiceId<?>, IRedundantServiceResolver<?>> getRedunantServiceResolvers() {
		if (redundantServiceResolvers == null) {
			redundantServiceResolvers = createRedunantServiceResolvers();
		}
		return redundantServiceResolvers;
	}

	@SuppressWarnings("rawtypes")
	private static Map<IServiceId<?>, IRedundantServiceResolver<?>> createRedunantServiceResolvers() {
		final Map<IServiceId<?>, IRedundantServiceResolver<?>> result = new HashMap<IServiceId<?>, IRedundantServiceResolver<?>>();

		final ServiceLoader<IRedundantServiceResolver> service = ServiceLoader.load(
				IRedundantServiceResolver.class,
				SharedClassLoader.getCompositeClassLoader());
		if (service != null) {
			final Iterator<IRedundantServiceResolver> iterator = service.iterator();
			while (iterator.hasNext()) {
				final IRedundantServiceResolver serviceResolver = iterator.next();
				final IServiceId serviceId = serviceResolver.getServiceId();
				if (serviceId != null) {
					if (!result.containsKey(serviceId)) {
						result.put(serviceId, serviceResolver);
					}
					else {
						throw new IllegalStateException(
							"There is already a service resolver registred for the id '" + serviceId + "'");
					}
				}
				else {
					throw new IllegalStateException("The registered service resolver has no service id.");
				}
			}
		}
		return result;
	}

	private static List<IServiceProviderDecoratorHolder> createServiceProviderDecorators() {
		final List<IServiceProviderDecoratorHolder> result = getRegisteredDecorators();
		sortDecorators(result);
		return result;
	}

	private static List<IServiceProviderDecoratorHolder> getRegisteredDecorators() {
		final List<IServiceProviderDecoratorHolder> result = new LinkedList<IServiceProviderDecoratorHolder>();
		final ServiceLoader<IServiceProviderDecoratorHolder> service = ServiceLoader.load(
				IServiceProviderDecoratorHolder.class,
				SharedClassLoader.getCompositeClassLoader());
		if (service != null) {
			final Iterator<IServiceProviderDecoratorHolder> iterator = service.iterator();
			while (iterator.hasNext()) {
				result.add(iterator.next());
			}
		}
		return result;
	}

	private static void sortDecorators(final List<IServiceProviderDecoratorHolder> decorators) {
		Collections.sort(decorators, new Comparator<IServiceProviderDecoratorHolder>() {
			@Override
			public int compare(
				final IServiceProviderDecoratorHolder decorator1,
				final IServiceProviderDecoratorHolder decorator2) {
				if (decorator1 != null && decorator2 != null) {
					return decorator2.getOrder() - decorator1.getOrder();
				}
				return 0;
			}
		});
	}

	private static class CompositeServiceProviderHolder implements IServiceProviderHolder {

		private final Set<IServiceProviderHolder> serviceProviderHolders;

		private final IServiceProvider serviceProvider;

		CompositeServiceProviderHolder() {
			this.serviceProviderHolders = new CopyOnWriteArraySet<IServiceProviderHolder>();

			this.serviceProvider = new IServiceProvider() {

				@Override
				public Set<IServiceId<?>> getAvailableServices() {
					final Set<IServiceId<?>> result = new HashSet<IServiceId<?>>();
					for (final IServiceProviderHolder serviceProviderHolder : serviceProviderHolders) {
						result.addAll(serviceProviderHolder.getServiceProvider().getAvailableServices());
					}
					return result;
				}

				@SuppressWarnings({"rawtypes", "unchecked"})
				@Override
				public <SERVICE_TYPE> SERVICE_TYPE get(final IServiceId<SERVICE_TYPE> id) {
					final Collection<SERVICE_TYPE> result = new LinkedList<SERVICE_TYPE>();
					for (final IServiceProviderHolder serviceProviderHolder : serviceProviderHolders) {
						final IServiceProvider provider = serviceProviderHolder.getServiceProvider();
						if (provider.getAvailableServices().contains(id)) {
							result.add(provider.get(id));
						}
					}
					if (result.size() == 0) {
						return null;
					}
					else if (result.size() == 1) {
						return result.iterator().next();
					}
					else {
						final IRedundantServiceResolver resolver = getRedunantServiceResolvers().get(id);
						if (resolver != null) {
							return (SERVICE_TYPE) resolver.resolve(result);
						}
						else {
							throw new IllegalStateException(
								"There is more than one service registered for the id'"
									+ id
									+ "'. Register the '"
									+ IRedundantServiceResolver.class
									+ "' do revolve the conflict");
						}
					}
				}
			};
		}

		void add(final IServiceProviderHolder holder) {
			serviceProviderHolders.add(holder);
		}

		@Override
		public IServiceProvider getServiceProvider() {
			return serviceProvider;
		}
	}
}
