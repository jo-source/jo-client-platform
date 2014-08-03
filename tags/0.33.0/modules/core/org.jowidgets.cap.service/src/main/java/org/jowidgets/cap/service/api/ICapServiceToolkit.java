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

package org.jowidgets.cap.service.api;

import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.service.api.adapter.IAdapterFactoryProvider;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoCollectionFilter;
import org.jowidgets.cap.service.api.bean.IBeanDtoCollectionSorter;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanIdentityResolver;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.bean.IBeanModifier;
import org.jowidgets.cap.service.api.bean.IBeanPropertyMap;
import org.jowidgets.cap.service.api.bean.IUniqueConstraintChecker;
import org.jowidgets.cap.service.api.bean.IUniqueConstraintCheckerBuilder;
import org.jowidgets.cap.service.api.decorator.IDecoratorProviderFactory;
import org.jowidgets.cap.service.api.entity.IBeanEntityServiceBuilder;
import org.jowidgets.cap.service.api.entity.IBeanServicesProviderBuilder;
import org.jowidgets.cap.service.api.entity.IEntityApplicationServiceBuilder;
import org.jowidgets.cap.service.api.entity.IEntityServiceBuilder;
import org.jowidgets.cap.service.api.executor.IExecutorServiceBuilder;
import org.jowidgets.cap.service.api.factory.IBeanServiceFactory;
import org.jowidgets.cap.service.api.link.ILinkServicesBuilder;
import org.jowidgets.cap.service.api.refresh.IRefreshServiceBuilder;
import org.jowidgets.cap.service.api.updater.IUpdaterServiceBuilder;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServiceRegistry;

public interface ICapServiceToolkit {

	IEntityServiceBuilder entityServiceBuilder();

	IBeanEntityServiceBuilder beanEntityServiceBuilder(IBeanServiceFactory beanServiceFactory, IServiceRegistry serviceRegistry);

	<SOURCE_BEAN_TYPE extends IBean, LINKED_BEAN_TYPE extends IBean> ILinkServicesBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> linkServicesBuilder();

	IEntityApplicationServiceBuilder entityApplicationServiceBuilder();

	IBeanServicesProviderBuilder beanServicesProviderBuilder(
		IServiceRegistry registry,
		IServiceId<IEntityService> entityServiceId,
		Class<? extends IBean> beanType,
		Object entityId);

	<BEAN_TYPE extends IBean> IBeanDtoFactory<BEAN_TYPE> dtoFactory(
		Class<? extends BEAN_TYPE> beanType,
		Collection<String> propertyNames);

	<BEAN_TYPE> IBeanDtoFactory<BEAN_TYPE> dtoFactory(
		IBeanIdentityResolver<? extends BEAN_TYPE> identityResolver,
		Collection<String> propertyNames);

	IBeanDtoCollectionSorter beanDtoCollectionSorter();

	IBeanDtoCollectionFilter beanDtoCollectionFilter();

	<BEAN_TYPE> IBeanInitializer<BEAN_TYPE> beanInitializer(Class<? extends BEAN_TYPE> beanType, Collection<String> propertyNames);

	<BEAN_TYPE> IBeanModifier<BEAN_TYPE> beanModifier(Class<? extends BEAN_TYPE> beanType, Collection<String> propertyNames);

	IBeanPropertyMap beanPropertyMap(Object entityTypeId);

	IBeanDtoFactory<IBeanPropertyMap> beanPropertyMapDtoFactory(Collection<String> propertyNames);

	IBeanInitializer<IBeanPropertyMap> beanPropertyMapInitializer(Collection<String> propertyNames);

	IBeanModifier<IBeanPropertyMap> beanPropertyMapModifier();

	IAdapterFactoryProvider adapterFactoryProvider();

	IDecoratorProviderFactory serviceDecoratorProvider();

	<BEAN_TYPE, PARAM_TYPE> IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> executorServiceBuilder(
		IBeanAccess<? extends BEAN_TYPE> beanAccess);

	<BEAN_TYPE> IUpdaterServiceBuilder<BEAN_TYPE> updaterServiceBuilder(IBeanAccess<? extends BEAN_TYPE> beanAccess);

	<BEAN_TYPE> IRefreshServiceBuilder<BEAN_TYPE> refreshServiceBuilder(IBeanAccess<? extends BEAN_TYPE> beanAccess);

	IExecutionCallback delayedExecutionCallback(
		IExecutionCallback executionCallback,
		final ScheduledExecutorService scheduledExecutorService,
		Long delay);

	IExecutionCallback delayedExecutionCallback(IExecutionCallback executionCallback);

	void checkCanceled(IExecutionCallback executionCallback);

	IUniqueConstraintCheckerBuilder uniqueConstraintCheckerBuilder(
		IBeanServiceFactory serviceFactory,
		Class<? extends IBean> beanType,
		Object beanTypeId);

	IUniqueConstraintChecker uniqueConstraintChecker(
		IBeanServiceFactory serviceFactory,
		Class<? extends IBean> beanType,
		Object beanTypeId,
		String... propertyNames);

	IUniqueConstraintChecker uniqueConstraintChecker(
		IBeanServiceFactory serviceFactory,
		Class<? extends IBean> beanType,
		String... propertyNames);

}
