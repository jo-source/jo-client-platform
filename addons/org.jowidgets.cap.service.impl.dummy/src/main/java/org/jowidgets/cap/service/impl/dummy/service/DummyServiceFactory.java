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
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.bean.IBeanModifier;
import org.jowidgets.cap.service.api.bean.IBeanPropertyMap;
import org.jowidgets.cap.service.impl.dummy.datastore.IEntityData;
import org.jowidgets.service.api.IServiceRegistry;

public final class DummyServiceFactory {

	private static final IDummyServiceFactory INSTANCE = createInstance();

	private DummyServiceFactory() {}

	public static IDummyServiceFactory getInstance() {
		return INSTANCE;
	}

	public static IBeanServicesProvider beanServices(
		final IServiceRegistry registry,
		final IEntityData<? extends IBean> data,
		final List<String> properties) {
		return getInstance().beanServices(registry, data, properties);
	}

	public static <BEAN_TYPE extends IBean> IBeanServicesProvider beanServices(
		final IServiceRegistry registry,
		final IEntityData<? extends BEAN_TYPE> data,
		final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory,
		final IBeanInitializer<BEAN_TYPE> beanInitializer,
		final IBeanModifier<BEAN_TYPE> beanModifier) {
		return getInstance().beanServices(registry, data, beanDtoFactory, beanInitializer, beanModifier);
	}

	public static IBeanServicesProvider beanPropertyMapServices(
		final IServiceRegistry registry,
		final IEntityData<? extends IBeanPropertyMap> data,
		final List<String> propertyNames) {
		return getInstance().beanPropertyMapServices(registry, data, propertyNames);
	}

	public static ICreatorService creatorService(final IEntityData<? extends IBean> data, final List<String> propertyNames) {
		return getInstance().creatorService(data, propertyNames);
	}

	public static <BEAN_TYPE extends IBean> ICreatorService creatorService(
		final IEntityData<? extends BEAN_TYPE> data,
		final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory,
		final IBeanInitializer<BEAN_TYPE> beanInitializer) {
		return getInstance().creatorService(data, beanDtoFactory, beanInitializer);
	}

	public static IReaderService<Void> readerService(final IEntityData<? extends IBean> data, final List<String> propertyNames) {
		return getInstance().readerService(data, propertyNames);
	}

	public static <BEAN_TYPE extends IBean> IReaderService<Void> readerService(
		final IEntityData<? extends BEAN_TYPE> data,
		final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory) {
		return getInstance().readerService(data, beanDtoFactory);
	}

	public static IDeleterService deleterService(final IEntityData<? extends IBean> data) {
		return getInstance().deleterService(data);
	}

	public static IDeleterService deleterService(
		final IEntityData<? extends IBean> data,
		final boolean allowDeletedData,
		final boolean allowStaleData) {
		return getInstance().deleterService(data, allowDeletedData, allowStaleData);
	}

	private static IDummyServiceFactory createInstance() {
		return new DummyServiceFactoryImpl();
	}

}
