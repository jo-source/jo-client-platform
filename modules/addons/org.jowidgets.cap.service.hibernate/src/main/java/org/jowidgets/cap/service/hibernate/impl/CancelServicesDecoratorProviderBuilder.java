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

package org.jowidgets.cap.service.hibernate.impl;

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
import org.jowidgets.cap.service.hibernate.api.ICancelServicesDecoratorProviderBuilder;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.concurrent.IThreadInterruptObservable;

final class CancelServicesDecoratorProviderBuilder implements ICancelServicesDecoratorProviderBuilder {

	private final String persistenceUnitName;
	private final IThreadInterruptObservable threadInterruptObservable;
	private final Set<Class<?>> services;

	private Long killAfterMillis = Long.valueOf(60000);
	private Long minQueryRuntimeMillis = Long.valueOf(25);

	private int order;

	CancelServicesDecoratorProviderBuilder(
		final String persistenceUnitName,
		final IThreadInterruptObservable threadInterruptObservable) {

		Assert.paramNotEmpty(persistenceUnitName, "persistenceUnitName");
		Assert.paramNotNull(threadInterruptObservable, "threadInterruptObservable");

		this.persistenceUnitName = persistenceUnitName;
		this.threadInterruptObservable = threadInterruptObservable;
		this.order = ICancelServicesDecoratorProviderBuilder.DEFAULT_ORDER;
		this.services = new HashSet<Class<?>>();

		addServices(
				ICreatorService.class,
				IReaderService.class,
				IRefreshService.class,
				IUpdaterService.class,
				IExecutorService.class,
				IDeleterService.class);
	}

	@Override
	public ICancelServicesDecoratorProviderBuilder setServices(final Collection<? extends Class<?>> services) {
		Assert.paramNotNull(services, "services");
		this.services.clear();
		this.services.addAll(services);
		return this;
	}

	@Override
	public ICancelServicesDecoratorProviderBuilder addServices(final Class<?>... services) {
		Assert.paramNotNull(services, "services");
		this.services.addAll(Arrays.asList(services));
		return this;
	}

	@Override
	public ICancelServicesDecoratorProviderBuilder setKillSessionAfterMillis(final Long killAfterMillis) {
		this.killAfterMillis = killAfterMillis;
		return this;
	}

	@Override
	public ICancelServicesDecoratorProviderBuilder setMinQueryRuntimeMillis(final Long minQueryRuntimeMillis) {
		this.minQueryRuntimeMillis = minQueryRuntimeMillis;
		return this;
	}

	@Override
	public ICancelServicesDecoratorProviderBuilder setOrder(final int order) {
		this.order = order;
		return this;
	}

	@Override
	public IServicesDecoratorProvider build() {
		return new CancelServicesDecoratorProviderImpl(
			persistenceUnitName,
			threadInterruptObservable,
			services,
			minQueryRuntimeMillis,
			killAfterMillis,
			order);
	}

}
