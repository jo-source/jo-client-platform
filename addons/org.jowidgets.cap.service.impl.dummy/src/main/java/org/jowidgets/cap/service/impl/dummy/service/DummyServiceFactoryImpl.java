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

package org.jowidgets.cap.service.impl.dummy.service;

import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.adapter.ISyncDeleterService;
import org.jowidgets.cap.service.api.adapter.ISyncReaderService;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.bean.IBeanModifier;
import org.jowidgets.cap.service.api.bean.IBeanPropertyMap;
import org.jowidgets.cap.service.impl.dummy.datastore.IEntityData;
import org.jowidgets.service.api.IServiceRegistry;
import org.jowidgets.util.IAdapterFactory;

final class DummyServiceFactoryImpl implements IDummyServiceFactory {

	DummyServiceFactoryImpl() {}

	@Override
	public IBeanServicesProvider beanServices(
		final IServiceRegistry registry,
		final IEntityData<? extends IBean> data,
		final List<String> properties) {
		return beanServices(
				registry,
				data,
				CapServiceToolkit.dtoFactory(data.getBeanType(), properties),
				CapServiceToolkit.beanInitializer(data.getBeanType(), properties),
				CapServiceToolkit.beanModifier(data.getBeanType(), properties));
	}

	@Override
	public <BEAN_TYPE extends IBean> IBeanServicesProvider beanServices(
		final IServiceRegistry registry,
		final IEntityData<? extends BEAN_TYPE> data,
		final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory,
		final IBeanInitializer<BEAN_TYPE> beanInitializer,
		final IBeanModifier<BEAN_TYPE> beanModifier) {
		return new BeanServicesProviderCreator<BEAN_TYPE>(registry, data, beanDtoFactory, beanInitializer, beanModifier).getServices();
	}

	@Override
	public IBeanServicesProvider beanPropertyMapServices(
		final IServiceRegistry registry,
		final IEntityData<? extends IBeanPropertyMap> data,
		final List<String> propertyNames) {
		return beanServices(
				registry,
				data,
				CapServiceToolkit.beanPropertyMapDtoFactory(propertyNames),
				CapServiceToolkit.beanPropertyMapInitializer(propertyNames),
				CapServiceToolkit.beanPropertyMapModifier());
	}

	@Override
	public ICreatorService creatorService(final IEntityData<? extends IBean> data, final List<String> properties) {
		return creatorService(
				data,
				CapServiceToolkit.dtoFactory(data.getBeanType(), properties),
				CapServiceToolkit.beanInitializer(data.getBeanType(), properties));
	}

	@Override
	public <BEAN_TYPE extends IBean> ICreatorService creatorService(
		final IEntityData<? extends BEAN_TYPE> data,
		final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory,
		final IBeanInitializer<BEAN_TYPE> beanInitializer) {

		final SyncCreatorService<BEAN_TYPE> syncCreatorService = new SyncCreatorService<BEAN_TYPE>(
			data,
			beanDtoFactory,
			beanInitializer);

		return CapServiceToolkit.adapterFactoryProvider().creator().createAdapter(syncCreatorService);
	}

	@Override
	public IReaderService<Void> readerService(final IEntityData<? extends IBean> data, final List<String> properties) {
		return readerService(data, CapServiceToolkit.dtoFactory(data.getBeanType(), properties));
	}

	@Override
	public <BEAN_TYPE extends IBean> IReaderService<Void> readerService(
		final IEntityData<? extends BEAN_TYPE> data,
		final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory) {

		final ISyncReaderService<Void> syncReaderService = new SyncReaderService<BEAN_TYPE>(data, beanDtoFactory);
		final IAdapterFactory<IReaderService<Void>, ISyncReaderService<Void>> adapterFactory = CapServiceToolkit.adapterFactoryProvider().reader();
		return adapterFactory.createAdapter(syncReaderService);
	}

	@Override
	public IDeleterService deleterService(final IEntityData<? extends IBean> data) {
		return deleterService(data, true, false);
	}

	@Override
	public IDeleterService deleterService(
		final IEntityData<? extends IBean> data,
		final boolean allowDeletedData,
		final boolean allowStaleData) {

		final ISyncDeleterService syncDeleterService = new SyncDeleterService(data, allowDeletedData, allowStaleData);

		return CapServiceToolkit.adapterFactoryProvider().deleter().createAdapter(syncDeleterService);
	}

}
