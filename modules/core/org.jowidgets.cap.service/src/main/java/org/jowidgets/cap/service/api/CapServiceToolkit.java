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
import org.jowidgets.cap.common.api.ordered.IOrderedBean;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.sort.ISortConverterMap;
import org.jowidgets.cap.service.api.adapter.IAdapterFactoryProvider;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoCollectionFilter;
import org.jowidgets.cap.service.api.bean.IBeanDtoCollectionSorter;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanIdentityResolver;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.bean.IBeanModifier;
import org.jowidgets.cap.service.api.bean.IBeanPropertyAccessor;
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
import org.jowidgets.cap.service.api.ordered.IOrderedBeanCrudServiceInterceptorBuilder;
import org.jowidgets.cap.service.api.refresh.IRefreshServiceBuilder;
import org.jowidgets.cap.service.api.updater.IUpdaterServiceBuilder;
import org.jowidgets.cap.service.impl.DefaultCapServiceToolkit;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServiceRegistry;

public final class CapServiceToolkit {

	private static ICapServiceToolkit dataServiceToolkit;

	private CapServiceToolkit() {}

	public static ICapServiceToolkit getInstance() {
		if (dataServiceToolkit == null) {
			dataServiceToolkit = new DefaultCapServiceToolkit();
		}
		return dataServiceToolkit;
	}

	public static IEntityServiceBuilder entityServiceBuilder() {
		return getInstance().entityServiceBuilder();
	}

	public static IBeanEntityServiceBuilder beanEntityServiceBuilder(
		final IBeanServiceFactory beanServiceFactory,
		final IServiceRegistry serviceRegistry) {
		return getInstance().beanEntityServiceBuilder(beanServiceFactory, serviceRegistry);
	}

	public static <SOURCE_BEAN_TYPE extends IBean, LINKED_BEAN_TYPE extends IBean> ILinkServicesBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> linkServicesBuilder() {
		return getInstance().linkServicesBuilder();
	}

	public static IEntityApplicationServiceBuilder entityApplicationServiceBuilder() {
		return getInstance().entityApplicationServiceBuilder();
	}

	public static IBeanServicesProviderBuilder beanServicesProviderBuilder(
		final IServiceRegistry registry,
		final IServiceId<IEntityService> entityServiceId,
		final Class<? extends IBean> beanType,
		final Object entityId) {
		return getInstance().beanServicesProviderBuilder(registry, entityServiceId, beanType, entityId);
	}

	public static <BEAN_TYPE> IBeanInitializer<BEAN_TYPE> beanInitializer(
		final Class<? extends BEAN_TYPE> beanType,
		final Collection<String> propertyNames) {
		return getInstance().beanInitializer(beanType, propertyNames);
	}

	public static <BEAN_TYPE> IBeanModifier<BEAN_TYPE> beanModifier(
		final Class<? extends BEAN_TYPE> beanType,
		final Collection<String> propertyNames) {
		return getInstance().beanModifier(beanType, propertyNames);
	}

	public static <BEAN_TYPE extends IBean> IBeanDtoFactory<BEAN_TYPE> dtoFactory(
		final Class<? extends BEAN_TYPE> beanType,
		final Collection<String> propertyNames) {
		return getInstance().dtoFactory(beanType, propertyNames);
	}

	public static <BEAN_TYPE> IBeanDtoFactory<BEAN_TYPE> dtoFactory(
		final IBeanIdentityResolver<? extends BEAN_TYPE> identityResolver,
		final Collection<String> propertyNames) {
		return getInstance().dtoFactory(identityResolver, propertyNames);
	}

	public static <BEAN_TYPE> IBeanPropertyAccessor<BEAN_TYPE> beanPropertyAccessor(
		final IBeanIdentityResolver<? extends BEAN_TYPE> identityResolver) {
		return getInstance().beanPropertyAccessor(identityResolver);
	}

	public static <BEAN_TYPE extends IBean> IBeanPropertyAccessor<BEAN_TYPE> beanPropertyAccessor(
		final Class<? extends BEAN_TYPE> beanType) {
		return getInstance().beanPropertyAccessor(beanType);
	}

	public static IBeanDtoCollectionSorter beanDtoCollectionSorter() {
		return getInstance().beanDtoCollectionSorter();
	}

	public static IBeanDtoCollectionSorter beanDtoCollectionSorter(final Class<?> beanType) {
		return getInstance().beanDtoCollectionSorter(beanType);
	}

	public static IBeanDtoCollectionSorter beanDtoCollectionSorter(final ISortConverterMap sortConveters) {
		return getInstance().beanDtoCollectionSorter(sortConveters);
	}

	public static IBeanDtoCollectionFilter beanDtoCollectionFilter() {
		return getInstance().beanDtoCollectionFilter();
	}

	public static IBeanPropertyMap beanPropertyMap(final Object entityTypeId) {
		return getInstance().beanPropertyMap(entityTypeId);
	}

	public static IBeanDtoFactory<IBeanPropertyMap> beanPropertyMapDtoFactory(final Collection<String> propertyNames) {
		return getInstance().beanPropertyMapDtoFactory(propertyNames);
	}

	public static IBeanInitializer<IBeanPropertyMap> beanPropertyMapInitializer(final Collection<String> propertyNames) {
		return getInstance().beanPropertyMapInitializer(propertyNames);
	}

	public static IBeanModifier<IBeanPropertyMap> beanPropertyMapModifier() {
		return getInstance().beanPropertyMapModifier();
	}

	public static <BEAN_TYPE, PARAM_TYPE> IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> executorServiceBuilder(
		final IBeanAccess<? extends BEAN_TYPE> beanAccess) {
		return getInstance().executorServiceBuilder(beanAccess);
	}

	public static IAdapterFactoryProvider adapterFactoryProvider() {
		return getInstance().adapterFactoryProvider();
	}

	public static IDecoratorProviderFactory serviceDecoratorProvider() {
		return getInstance().serviceDecoratorProvider();
	}

	public static <BEAN_TYPE> IUpdaterServiceBuilder<BEAN_TYPE> updaterServiceBuilder(
		final IBeanAccess<? extends BEAN_TYPE> beanAccess) {
		return getInstance().updaterServiceBuilder(beanAccess);
	}

	public static <BEAN_TYPE> IRefreshServiceBuilder<BEAN_TYPE> refreshServiceBuilder(
		final IBeanAccess<? extends BEAN_TYPE> beanAccess) {
		return getInstance().refreshServiceBuilder(beanAccess);
	}

	public static IExecutionCallback delayedExecutionCallback(
		final IExecutionCallback executionCallback,
		final ScheduledExecutorService scheduledExecutorService,
		final Long delay) {
		return getInstance().delayedExecutionCallback(executionCallback, scheduledExecutorService, delay);
	}

	public static IExecutionCallback delayedExecutionCallback(final IExecutionCallback executionCallback) {
		return getInstance().delayedExecutionCallback(executionCallback);
	}

	public static void checkCanceled(final IExecutionCallback executionCallback) {
		getInstance().checkCanceled(executionCallback);
	}

	public static IUniqueConstraintCheckerBuilder uniqueConstraintCheckerBuilder(
		final IBeanServiceFactory serviceFactory,
		final Class<? extends IBean> beanType,
		final Object beanTypeId) {
		return getInstance().uniqueConstraintCheckerBuilder(serviceFactory, beanType, beanTypeId);
	}

	public static IUniqueConstraintChecker uniqueConstraintChecker(
		final IBeanServiceFactory serviceFactory,
		final Class<? extends IBean> beanType,
		final Object beanTypeId,
		final String... propertyNames) {
		return getInstance().uniqueConstraintChecker(serviceFactory, beanType, beanTypeId, propertyNames);
	}

	public static IUniqueConstraintChecker uniqueConstraintChecker(
		final IBeanServiceFactory serviceFactory,
		final Class<? extends IBean> beanType,
		final String... propertyNames) {
		return getInstance().uniqueConstraintChecker(serviceFactory, beanType, propertyNames);
	}

	public static <BEAN_TYPE extends IOrderedBean, GROUP_TYPE> IOrderedBeanCrudServiceInterceptorBuilder<BEAN_TYPE, GROUP_TYPE> orderedBeanCrudInterceptorBuilder() {
		return getInstance().orderedBeanCrudInterceptorBuilder();
	}

}
