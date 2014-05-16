/*
 * Copyright (c) 2014, grossmann
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

package org.jowidgets.cap.service.repository.impl;

import java.util.List;

import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.adapter.ISyncReaderService;
import org.jowidgets.cap.service.api.creator.ICreatorServiceBuilder;
import org.jowidgets.cap.service.api.deleter.IDeleterServiceBuilder;
import org.jowidgets.cap.service.api.refresh.IRefreshServiceBuilder;
import org.jowidgets.cap.service.api.updater.IUpdaterServiceBuilder;
import org.jowidgets.cap.service.repository.api.IBeanRepository;
import org.jowidgets.cap.service.repository.api.IBeanRepositoryServiceFactory;
import org.jowidgets.cap.service.repository.api.ICreateSupportBeanRepository;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.service.tools.ServiceId;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IAdapterFactory;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.reflection.BeanUtils;

final class BeanRepositoryServiceFactoryImpl<BEAN_TYPE> implements IBeanRepositoryServiceFactory<BEAN_TYPE> {

	private static final long ASYNC_CALLBACK_DELAY = 200L;

	private static final IServicesDecoratorProvider ASYNC_DECORATOR_PROVIDER = CapServiceToolkit.serviceDecoratorProvider().asyncDecoratorProvider(
			ASYNC_CALLBACK_DELAY);

	private static final IServiceId<IReaderService<Void>> READER_SERVICE_ID = new ServiceId<IReaderService<Void>>(
		"BeanRepositoryServiceFactoryImpl",
		IReaderService.class);
	private static final IDecorator<IReaderService<Void>> ASYNC_READER_DECORATOR = ASYNC_DECORATOR_PROVIDER.getDecorator(READER_SERVICE_ID);

	private static final IAdapterFactory<IReaderService<Void>, ISyncReaderService<Void>> READER_ADAPTER_FACTORY = CapServiceToolkit.adapterFactoryProvider().reader();

	private final IBeanRepository<BEAN_TYPE> repository;
	private final List<String> readableProperties;

	BeanRepositoryServiceFactoryImpl(final IBeanRepository<BEAN_TYPE> repositiory) {
		Assert.paramNotNull(repositiory, "repositiory");
		this.repository = repositiory;
		this.readableProperties = BeanUtils.getReadableProperties(repositiory.getBeanType());
	}

	@Override
	public IBeanServicesProvider beanServices() {
		return new BeanServicesProvider(readerService(), creatorService(), refreshService(), updaterService(), deleterService());
	}

	@Override
	public ICreatorServiceBuilder<BEAN_TYPE> creatorServiceBuilder() {
		if (repository instanceof ICreateSupportBeanRepository) {
			return new BeanRepositoryCreatorServiceBuilderImpl<BEAN_TYPE>((ICreateSupportBeanRepository<BEAN_TYPE>) repository);
		}
		else {
			return null;
		}
	}

	@Override
	public ICreatorService creatorService() {
		final ICreatorServiceBuilder<BEAN_TYPE> builder = creatorServiceBuilder();
		if (builder != null) {
			return builder.build();
		}
		else {
			return null;
		}
	}

	@Override
	public IReaderService<Void> readerService() {
		return ASYNC_READER_DECORATOR.decorate(READER_ADAPTER_FACTORY.createAdapter(new SyncBeanRepositoryReaderService<BEAN_TYPE>(
			repository,
			readableProperties)));
	}

	@Override
	public IRefreshServiceBuilder<BEAN_TYPE> refreshServiceBuilder() {
		return null;
	}

	@Override
	public IRefreshService refreshService() {
		return null;
	}

	@Override
	public IDeleterServiceBuilder<BEAN_TYPE> deleterServiceBuilder() {
		return null;
	}

	@Override
	public IDeleterService deleterService() {
		return null;
	}

	@Override
	public IUpdaterServiceBuilder<BEAN_TYPE> updaterServiceBuilder() {
		return null;
	}

	@Override
	public IUpdaterService updaterService() {
		return null;
	}

}
