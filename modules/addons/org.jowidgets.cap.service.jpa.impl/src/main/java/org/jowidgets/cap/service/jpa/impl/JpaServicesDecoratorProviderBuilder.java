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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IExecutorService;
import org.jowidgets.cap.common.api.service.ILinkCreatorService;
import org.jowidgets.cap.common.api.service.ILinkDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.service.api.exception.IServiceExceptionLogger;
import org.jowidgets.cap.service.jpa.api.IJpaServicesDecoratorProviderBuilder;
import org.jowidgets.cap.service.tools.exception.DefaultServiceExceptionLogger;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.IExceptionLogger;

final class JpaServicesDecoratorProviderBuilder implements IJpaServicesDecoratorProviderBuilder {

	private static final IServiceExceptionLogger DEFAULT_SERVICE_EXCEPTION_LOGGER = new DefaultServiceExceptionLogger(
		JpaServicesDecoratorProviderImpl.class.getName());

	private final String persistenceUnitName;
	private final Set<Class<?>> entityManagerServices;
	private final Set<Class<?>> transactionalServices;
	private final List<IDecorator<Throwable>> exceptionDecorators;

	private IExceptionLogger deprecatedExceptionLogger;
	private IServiceExceptionLogger exceptionLogger;

	private int order;

	JpaServicesDecoratorProviderBuilder(final String persistenceUnitName) {
		Assert.paramNotEmpty(persistenceUnitName, "persistenceUnitName");
		this.persistenceUnitName = persistenceUnitName;
		this.order = IJpaServicesDecoratorProviderBuilder.DEFAULT_ORDER;
		this.entityManagerServices = new HashSet<Class<?>>();
		this.transactionalServices = new HashSet<Class<?>>();
		this.exceptionDecorators = new LinkedList<IDecorator<Throwable>>();
		exceptionDecorators.add(new DefaultJpaExceptionDecorator());
		this.deprecatedExceptionLogger = new DefaultLegacyExceptionLogger();
		this.exceptionLogger = DEFAULT_SERVICE_EXCEPTION_LOGGER;

		addEntityManagerServices(
				ICreatorService.class,
				IReaderService.class,
				IRefreshService.class,
				IUpdaterService.class,
				IExecutorService.class,
				IDeleterService.class,
				ILinkCreatorService.class,
				ILinkDeleterService.class);

		addTransactionalServices(
				ICreatorService.class,
				IUpdaterService.class,
				IExecutorService.class,
				IDeleterService.class,
				ILinkCreatorService.class,
				ILinkDeleterService.class);
	}

	@Override
	public IJpaServicesDecoratorProviderBuilder setEntityManagerServices(final Collection<? extends Class<?>> services) {
		Assert.paramNotNull(services, "services");
		entityManagerServices.clear();
		entityManagerServices.addAll(services);
		return this;
	}

	@Override
	public IJpaServicesDecoratorProviderBuilder setTransactionalServices(final Collection<? extends Class<?>> services) {
		Assert.paramNotNull(services, "services");
		transactionalServices.clear();
		transactionalServices.addAll(services);
		return this;
	}

	@Override
	public IJpaServicesDecoratorProviderBuilder addEntityManagerServices(final Class<?>... services) {
		Assert.paramNotNull(services, "services");
		entityManagerServices.addAll(Arrays.asList(services));
		return this;
	}

	@Override
	public IJpaServicesDecoratorProviderBuilder addTransactionalServices(final Class<?>... services) {
		Assert.paramNotNull(services, "services");
		transactionalServices.addAll(Arrays.asList(services));
		return this;
	}

	@Override
	public IJpaServicesDecoratorProviderBuilder setExceptionDecorators(
		final Collection<? extends IDecorator<Throwable>> decorators) {
		Assert.paramNotNull(decorators, "decorators");
		exceptionDecorators.clear();
		for (final IDecorator<Throwable> decorator : decorators) {
			exceptionDecorators.add(0, decorator);
		}
		return this;
	}

	@Override
	public IJpaServicesDecoratorProviderBuilder addExceptionDecorator(final IDecorator<Throwable> decorator) {
		Assert.paramNotNull(decorator, "decorator");
		exceptionDecorators.add(0, decorator);
		return this;
	}

	@Override
	public IJpaServicesDecoratorProviderBuilder setExceptionLogger(final IExceptionLogger exceptionLogger) {
		Assert.paramNotNull(exceptionLogger, "logger");
		this.deprecatedExceptionLogger = exceptionLogger;
		return this;
	}

	@Override
	public IJpaServicesDecoratorProviderBuilder setExceptionLogger(final IServiceExceptionLogger exceptionLogger) {
		if (exceptionLogger != null) {
			this.exceptionLogger = exceptionLogger;
		}
		else {
			this.exceptionLogger = DEFAULT_SERVICE_EXCEPTION_LOGGER;
		}
		return null;
	}

	@Override
	public IJpaServicesDecoratorProviderBuilder setOrder(final int order) {
		this.order = order;
		return this;
	}

	@Override
	public IServicesDecoratorProvider build() {
		return new JpaServicesDecoratorProviderImpl(
			persistenceUnitName,
			entityManagerServices,
			transactionalServices,
			exceptionDecorators,
			deprecatedExceptionLogger,
			exceptionLogger,
			order);
	}

}
