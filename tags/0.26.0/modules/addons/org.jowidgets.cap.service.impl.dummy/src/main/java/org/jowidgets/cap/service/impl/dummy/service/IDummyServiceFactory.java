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
import org.jowidgets.cap.service.api.entity.IBeanServicesProviderBuilder;
import org.jowidgets.cap.service.impl.dummy.datastore.IEntityData;
import org.jowidgets.service.api.IServiceRegistry;

public interface IDummyServiceFactory {

	IBeanServicesProvider beanServices(
		IServiceRegistry registry,
		Object entityTypeId,
		IEntityData<? extends IBean> data,
		List<String> properties);

	IBeanServicesProviderBuilder beanServicesBuilder(
		IServiceRegistry registry,
		Object entityTypeId,
		IEntityData<? extends IBean> data,
		List<String> properties);

	<BEAN_TYPE extends IBean> IBeanServicesProvider beanServices(
		IServiceRegistry registry,
		Object entityTypeId,
		IEntityData<? extends BEAN_TYPE> data,
		IBeanDtoFactory<BEAN_TYPE> beanDtoFactory,
		IBeanInitializer<BEAN_TYPE> beanInitializer,
		IBeanModifier<BEAN_TYPE> beanModifier);

	<BEAN_TYPE extends IBean> IBeanServicesProviderBuilder beanServicesBuilder(
		IServiceRegistry registry,
		Object entityTypeId,
		IEntityData<? extends BEAN_TYPE> data,
		IBeanDtoFactory<BEAN_TYPE> beanDtoFactory,
		IBeanInitializer<BEAN_TYPE> beanInitializer,
		IBeanModifier<BEAN_TYPE> beanModifier);

	IBeanServicesProvider beanPropertyMapServices(
		IServiceRegistry registry,
		Object entityTypeId,
		IEntityData<? extends IBeanPropertyMap> data,
		List<String> propertyNames);

	IBeanServicesProviderBuilder beanPropertyMapServicesBuilder(
		IServiceRegistry registry,
		Object entityTypeId,
		IEntityData<? extends IBeanPropertyMap> data,
		List<String> propertyNames);

	ICreatorService creatorService(final IEntityData<? extends IBean> data, final List<String> propertyNames);

	<BEAN_TYPE extends IBean> ICreatorService creatorService(
		IEntityData<? extends BEAN_TYPE> data,
		IBeanDtoFactory<BEAN_TYPE> beanDtoFactory,
		IBeanInitializer<BEAN_TYPE> beanInitializer);

	IReaderService<Void> readerService(final IEntityData<? extends IBean> data, final List<String> propertyNames);

	<BEAN_TYPE extends IBean> IReaderService<Void> readerService(
		IEntityData<? extends BEAN_TYPE> data,
		IBeanDtoFactory<BEAN_TYPE> beanDtoFactory);

	IDeleterService deleterService(final IEntityData<? extends IBean> data);

	IDeleterService deleterService(IEntityData<? extends IBean> data, boolean allowDeletedData, boolean allowStaleData);

}
