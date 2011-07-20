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

package org.jowidgets.cap.service.impl;

import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.adapter.ISyncCreatorService;
import org.jowidgets.cap.service.api.adapter.ISyncDeleterService;
import org.jowidgets.cap.service.api.adapter.ISyncReaderService;
import org.jowidgets.cap.service.api.adapter.ISyncRefreshService;
import org.jowidgets.cap.service.api.adapter.ISyncUpdaterService;
import org.jowidgets.cap.service.api.entity.IBeanServicesProviderBuilder;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServiceRegistry;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IAdapterFactory;

final class BeanServicesProviderBuilderImpl implements IBeanServicesProviderBuilder {

	private final IServiceRegistry registry;
	private final IServiceId<IEntityService> entityServiceId;
	private final Object entityId;

	private IReaderService<Void> readerService;
	private ICreatorService creatorService;
	private IRefreshService refreshService;
	private IUpdaterService updaterService;
	private IDeleterService deleterService;

	BeanServicesProviderBuilderImpl(
		final IServiceRegistry registry,
		final IServiceId<IEntityService> entityServiceId,
		final Object entityId) {
		Assert.paramNotNull(registry, "registry");
		this.registry = registry;
		this.entityServiceId = entityServiceId;
		this.entityId = entityId;
	}

	@Override
	public IBeanServicesProviderBuilder setReaderService(final IReaderService<Void> readerService) {
		Assert.paramNotNull(readerService, "readerService");
		this.readerService = readerService;
		return this;
	}

	@Override
	public IBeanServicesProviderBuilder setReaderService(final ISyncReaderService<Void> readerService) {
		Assert.paramNotNull(readerService, "readerService");
		final IAdapterFactory<IReaderService<Void>, ISyncReaderService<Void>> readerAdapterFactory = CapServiceToolkit.adapterFactoryProvider().reader();
		return setReaderService(readerAdapterFactory.createAdapter(readerService));
	}

	@Override
	public IBeanServicesProviderBuilder setCreatorService(final ICreatorService creatorService) {
		Assert.paramNotNull(creatorService, "creatorService");
		this.creatorService = creatorService;
		return this;
	}

	@Override
	public IBeanServicesProviderBuilder setCreatorService(final ISyncCreatorService creatorService) {
		Assert.paramNotNull(creatorService, "creatorService");
		return setCreatorService(CapServiceToolkit.adapterFactoryProvider().creator().createAdapter(creatorService));
	}

	@Override
	public IBeanServicesProviderBuilder setRefreshService(final IRefreshService refreshService) {
		Assert.paramNotNull(refreshService, "refreshService");
		this.refreshService = refreshService;
		return this;
	}

	@Override
	public IBeanServicesProviderBuilder setRefreshService(final ISyncRefreshService refreshService) {
		Assert.paramNotNull(refreshService, "refreshService");
		return setRefreshService(CapServiceToolkit.adapterFactoryProvider().refresh().createAdapter(refreshService));
	}

	@Override
	public IBeanServicesProviderBuilder setUpdaterService(final IUpdaterService updaterService) {
		Assert.paramNotNull(updaterService, "updaterService");
		this.updaterService = updaterService;
		return this;
	}

	@Override
	public IBeanServicesProviderBuilder setUpdaterService(final ISyncUpdaterService updaterService) {
		Assert.paramNotNull(updaterService, "updaterService");
		return setUpdaterService(CapServiceToolkit.adapterFactoryProvider().updater().createAdapter(updaterService));
	}

	@Override
	public IBeanServicesProviderBuilder setDeleterService(final IDeleterService deleterService) {
		Assert.paramNotNull(deleterService, "deleterService");
		this.deleterService = deleterService;
		return this;
	}

	@Override
	public IBeanServicesProviderBuilder setDeleterService(final ISyncDeleterService deleterService) {
		Assert.paramNotNull(deleterService, "deleterService");
		return setDeleterService(CapServiceToolkit.adapterFactoryProvider().deleter().createAdapter(deleterService));
	}

	@Override
	public IBeanServicesProvider build() {
		return new BeanServicesProviderImpl(
			registry,
			entityServiceId,
			entityId,
			readerService,
			creatorService,
			refreshService,
			updaterService,
			deleterService);
	}

}
