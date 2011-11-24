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

package org.jowidgets.cap.service.jpa.api;

import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.bean.IBeanModifier;
import org.jowidgets.cap.service.api.entity.IBeanServicesProviderBuilder;
import org.jowidgets.cap.service.jpa.api.query.IQueryCreator;
import org.jowidgets.service.api.IServiceRegistry;

public interface IJpaServiceFactory {

	<BEAN_TYPE extends IBean> IBeanAccess<BEAN_TYPE> beanAccess(Class<? extends BEAN_TYPE> beanType);

	IBeanServicesProvider beanServices(IServiceRegistry registry, Class<? extends IBean> beanType, List<String> properties);

	IBeanServicesProviderBuilder beanServicesBuilder(
		IServiceRegistry registry,
		Class<? extends IBean> beanType,
		List<String> properties);

	IBeanServicesProvider beanServices(
		IServiceRegistry registry,
		Object entityTypeId,
		Class<? extends IBean> beanType,
		List<String> properties);

	IBeanServicesProviderBuilder beanServicesBuilder(
		IServiceRegistry registry,
		Object entityTypeId,
		Class<? extends IBean> beanType,
		List<String> properties);

	<BEAN_TYPE extends IBean> IBeanServicesProvider beanServices(
		IServiceRegistry registry,
		Object entityTypeId,
		Class<? extends BEAN_TYPE> beanType,
		IBeanDtoFactory<BEAN_TYPE> beanDtoFactory,
		IBeanInitializer<BEAN_TYPE> beanInitializer,
		IBeanModifier<BEAN_TYPE> beanModifier);

	<BEAN_TYPE extends IBean> IBeanServicesProviderBuilder beanServicesBuilder(
		IServiceRegistry registry,
		Object entityTypeId,
		Class<? extends BEAN_TYPE> beanType,
		IBeanDtoFactory<BEAN_TYPE> beanDtoFactory,
		IBeanInitializer<BEAN_TYPE> beanInitializer,
		IBeanModifier<BEAN_TYPE> beanModifier);

	ICreatorService creatorService(Class<? extends IBean> beanType, final List<String> propertyNames);

	<BEAN_TYPE extends IBean> ICreatorService creatorService(
		Class<? extends BEAN_TYPE> beanType,
		IBeanDtoFactory<BEAN_TYPE> beanDtoFactory,
		IBeanInitializer<BEAN_TYPE> beanInitializer);

	<PARAM_TYPE> IReaderService<PARAM_TYPE> readerService(Class<? extends IBean> beanType, final List<String> propertyNames);

	<PARAM_TYPE> IReaderService<PARAM_TYPE> readerService(
		Class<? extends IBean> beanType,
		IQueryCreator<PARAM_TYPE> queryCreator,
		final List<String> propertyNames);

	<BEAN_TYPE extends IBean, PARAM_TYPE> IReaderService<PARAM_TYPE> readerService(
		IBeanAccess<? extends BEAN_TYPE> beanAccess,
		IBeanDtoFactory<BEAN_TYPE> beanDtoFactory);

	<BEAN_TYPE extends IBean, PARAM_TYPE> IReaderService<PARAM_TYPE> readerService(
		IBeanAccess<? extends BEAN_TYPE> beanAccess,
		IQueryCreator<PARAM_TYPE> queryCreator,
		IBeanDtoFactory<BEAN_TYPE> beanDtoFactory);

	IDeleterService deleterService(Class<? extends IBean> beanType);

	IDeleterService deleterService(Class<? extends IBean> beanType, boolean allowDeletedData, boolean allowStaleData);

	IDeleterService deleterService(IBeanAccess<? extends IBean> beanAccess);

	IDeleterService deleterService(IBeanAccess<? extends IBean> beanAccess, boolean allowDeletedData, boolean allowStaleData);

}
