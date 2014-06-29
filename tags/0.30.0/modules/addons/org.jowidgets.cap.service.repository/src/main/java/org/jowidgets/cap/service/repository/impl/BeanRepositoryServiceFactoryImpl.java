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

import java.util.Collection;
import java.util.LinkedList;
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
import org.jowidgets.cap.service.repository.api.IDeleteSupportBeanRepository;
import org.jowidgets.cap.service.repository.api.IUpdateSupportBeanRepository;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.service.tools.ServiceId;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IAdapterFactory;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.reflection.BeanUtils;

final class BeanRepositoryServiceFactoryImpl<BEAN_TYPE> implements IBeanRepositoryServiceFactory<BEAN_TYPE> {

	private static final IAdapterFactory<IReaderService<Void>, ISyncReaderService<Void>> READER_ADAPTER_FACTORY = CapServiceToolkit.adapterFactoryProvider().reader();

	private static final IServiceId<IReaderService<Void>> READER_SERVICE_ID = new ServiceId<IReaderService<Void>>(
		"BeanRepositoryServiceFactoryImpl",
		IReaderService.class);

	private static final IServiceId<ICreatorService> CREATOR_SERVICE_ID = new ServiceId<ICreatorService>(
		"BeanRepositoryServiceFactoryImpl",
		ICreatorService.class);

	private static final IServiceId<IUpdaterService> UPDATER_SERVICE_ID = new ServiceId<IUpdaterService>(
		"BeanRepositoryServiceFactoryImpl",
		IUpdaterService.class);

	private static final IServiceId<IRefreshService> REFRESH_SERVICE_ID = new ServiceId<IRefreshService>(
		"BeanRepositoryServiceFactoryImpl",
		IRefreshService.class);

	private static final IServiceId<IDeleterService> DELETER_SERVICE_ID = new ServiceId<IDeleterService>(
		"BeanRepositoryServiceFactoryImpl",
		IDeleterService.class);

	private final IBeanRepository<BEAN_TYPE> repository;
	private final BeanRepositoryBeanAccess<BEAN_TYPE> beanAccess;
	private final List<String> readableProperties;
	private final List<String> allProperties;

	private final IDecorator<IReaderService<Void>> readerDecorator;
	private final IDecorator<ICreatorService> creatorDecorator;
	private final IDecorator<IUpdaterService> updaterDecorator;
	private final IDecorator<IRefreshService> refreshDecorator;
	private final IDecorator<IDeleterService> deleterDecorator;

	BeanRepositoryServiceFactoryImpl(
		final IBeanRepository<BEAN_TYPE> repositiory,
		final IServicesDecoratorProvider serviceDecoratorProvider) {
		this(
			repositiory,
			BeanUtils.getReadableProperties(repositiory.getBeanType()),
			BeanUtils.getProperties(repositiory.getBeanType()),
			serviceDecoratorProvider);
	}

	BeanRepositoryServiceFactoryImpl(
		final IBeanRepository<BEAN_TYPE> repositiory,
		final Collection<String> properties,
		final IServicesDecoratorProvider serviceDecoratorProvider) {
		this(repositiory, properties, properties, serviceDecoratorProvider);
	}

	BeanRepositoryServiceFactoryImpl(
		final IBeanRepository<BEAN_TYPE> repositiory,
		final Collection<String> readableProperties,
		final Collection<String> allProperties,
		final IServicesDecoratorProvider serviceDecoratorProvider) {
		Assert.paramNotNull(repositiory, "repositiory");
		Assert.paramNotNull(readableProperties, "readableProperties");
		Assert.paramNotNull(allProperties, "allProperties");
		Assert.paramNotNull(serviceDecoratorProvider, "serviceDecoratorProvider");

		this.repository = repositiory;
		this.beanAccess = new BeanRepositoryBeanAccess<BEAN_TYPE>(repository);
		this.readableProperties = new LinkedList<String>(readableProperties);
		this.allProperties = new LinkedList<String>(allProperties);

		this.readerDecorator = serviceDecoratorProvider.getDecorator(READER_SERVICE_ID);
		this.creatorDecorator = serviceDecoratorProvider.getDecorator(CREATOR_SERVICE_ID);
		this.updaterDecorator = serviceDecoratorProvider.getDecorator(UPDATER_SERVICE_ID);
		this.refreshDecorator = serviceDecoratorProvider.getDecorator(REFRESH_SERVICE_ID);
		this.deleterDecorator = serviceDecoratorProvider.getDecorator(DELETER_SERVICE_ID);
	}

	@Override
	public IBeanServicesProvider beanServices() {
		return new BeanServicesProvider(readerService(), creatorService(), refreshService(), updaterService(), deleterService());
	}

	@Override
	public ICreatorServiceBuilder<BEAN_TYPE> creatorServiceBuilder() {
		if (repository instanceof ICreateSupportBeanRepository) {
			return new BeanRepositoryCreatorServiceBuilderImpl<BEAN_TYPE>(
				(ICreateSupportBeanRepository<BEAN_TYPE>) repository,
				creatorDecorator,
				allProperties);
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
		return readerDecorator.decorate(READER_ADAPTER_FACTORY.createAdapter(new SyncBeanRepositoryReaderService<BEAN_TYPE>(
			repository,
			readableProperties)));
	}

	@Override
	public IRefreshServiceBuilder<BEAN_TYPE> refreshServiceBuilder() {
		return new BeanRepositoryRefreshServiceBuilderImpl<BEAN_TYPE>(beanAccess, allProperties, refreshDecorator);
	}

	@Override
	public IRefreshService refreshService() {
		return refreshServiceBuilder().build();
	}

	@Override
	public IDeleterServiceBuilder<BEAN_TYPE> deleterServiceBuilder() {
		if (repository instanceof IDeleteSupportBeanRepository) {
			return new BeanRepositoryDeleterServiceBuilderImpl<BEAN_TYPE>(
				beanAccess,
				(IDeleteSupportBeanRepository<BEAN_TYPE>) repository,
				deleterDecorator);
		}
		else {
			return null;
		}
	}

	@Override
	public IDeleterService deleterService() {
		final IDeleterServiceBuilder<BEAN_TYPE> builder = deleterServiceBuilder();
		if (builder != null) {
			return builder.build();
		}
		else {
			return null;
		}
	}

	@Override
	public IUpdaterServiceBuilder<BEAN_TYPE> updaterServiceBuilder() {
		if (repository instanceof IUpdateSupportBeanRepository) {
			return new BeanRepositoryUpdaterServiceBuilderImpl<BEAN_TYPE>(
				(IUpdateSupportBeanRepository<BEAN_TYPE>) repository,
				beanAccess,
				allProperties,
				updaterDecorator);
		}
		else {
			return null;
		}
	}

	@Override
	public IUpdaterService updaterService() {
		final IUpdaterServiceBuilder<BEAN_TYPE> builder = updaterServiceBuilder();
		if (builder != null) {
			return builder.build();
		}
		else {
			return null;
		}
	}

}
