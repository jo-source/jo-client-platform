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
import java.util.Set;

import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IExecutorService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.service.jpa.api.IJpaServicesDecoratorProviderBuilder;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.IExceptionLogger;

final class JpaServicesDecoratorProviderBuilder implements IJpaServicesDecoratorProviderBuilder {

	private final String persistenceUnitName;
	private final Set<Class<?>> entityManagerServices;
	private final Set<Class<?>> transactionalServices;

	private IDecorator<Throwable> exceptionDecorator;
	private IExceptionLogger exceptionLogger;

	private int order;

	JpaServicesDecoratorProviderBuilder(final String persistenceUnitName) {
		Assert.paramNotEmpty(persistenceUnitName, "persistenceUnitName");
		this.persistenceUnitName = persistenceUnitName;
		this.order = IJpaServicesDecoratorProviderBuilder.DEFAULT_ORDER;
		this.entityManagerServices = new HashSet<Class<?>>();
		this.transactionalServices = new HashSet<Class<?>>();
		this.exceptionDecorator = new DefaultJpaExceptionDecorator();
		this.exceptionLogger = new DefaultExceptionLogger();

		addEntityManagerServices(
				ICreatorService.class,
				IReaderService.class,
				IRefreshService.class,
				IUpdaterService.class,
				IExecutorService.class,
				IDeleterService.class);

		addTransactionalServices(ICreatorService.class, IUpdaterService.class, IExecutorService.class, IDeleterService.class);
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
	public IJpaServicesDecoratorProviderBuilder setExceptionDecorator(final IDecorator<Throwable> decorator) {
		Assert.paramNotNull(decorator, "decorator");
		this.exceptionDecorator = decorator;
		return this;
	}

	@Override
	public IJpaServicesDecoratorProviderBuilder setExceptionLogger(final IExceptionLogger exceptionLogger) {
		Assert.paramNotNull(exceptionLogger, "logger");
		this.exceptionLogger = exceptionLogger;
		return this;
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
			exceptionDecorator,
			exceptionLogger,
			order);
	}

}
