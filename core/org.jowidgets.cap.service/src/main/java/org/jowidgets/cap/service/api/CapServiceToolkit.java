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

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.service.api.adapter.IAdapterFactoryProvider;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanDtoSorter;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.bean.IBeanModifier;
import org.jowidgets.cap.service.api.bean.IBeanPropertyMap;
import org.jowidgets.cap.service.api.decorator.IDecoratorProviderFactory;
import org.jowidgets.cap.service.api.entity.IBeanServicesProviderBuilder;
import org.jowidgets.cap.service.api.entity.IEntityClassProviderServiceBuilder;
import org.jowidgets.cap.service.api.entity.IEntityServiceBuilder;
import org.jowidgets.cap.service.api.executor.IExecutorServiceBuilder;
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

	public static IEntityClassProviderServiceBuilder entityClassProviderServiceBuilder() {
		return getInstance().entityClassProviderServiceBuilder();
	}

	public static IBeanServicesProviderBuilder beanServicesProviderBuilder(
		final IServiceRegistry registry,
		final IServiceId<IEntityService> entityServiceId,
		final Object entityId) {
		return getInstance().beanServicesProviderBuilder(registry, entityServiceId, entityId);
	}

	public static <BEAN_TYPE extends IBean> IBeanInitializer<BEAN_TYPE> beanInitializer(
		final Class<? extends BEAN_TYPE> beanType,
		final List<String> propertyNames) {
		return getInstance().beanInitializer(beanType, propertyNames);
	}

	public static <BEAN_TYPE extends IBean> IBeanModifier<BEAN_TYPE> beanModifier(
		final Class<? extends BEAN_TYPE> beanType,
		final List<String> propertyNames) {
		return getInstance().beanModifier(beanType, propertyNames);
	}

	public static <BEAN_TYPE extends IBean> IBeanDtoFactory<BEAN_TYPE> dtoFactory(
		final Class<? extends BEAN_TYPE> beanType,
		final List<String> propertyNames) {
		return getInstance().dtoFactory(beanType, propertyNames);
	}

	public static IBeanDtoSorter beanDtoSorter() {
		return getInstance().beanDtoSorter();
	}

	public static IBeanPropertyMap beanPropertyMap(final Object entityTypeId) {
		return getInstance().beanPropertyMap(entityTypeId);
	}

	public static IBeanDtoFactory<IBeanPropertyMap> beanPropertyMapDtoFactory(final List<String> propertyNames) {
		return getInstance().beanPropertyMapDtoFactory(propertyNames);
	}

	public static IBeanInitializer<IBeanPropertyMap> beanPropertyMapInitializer(final List<String> propertyNames) {
		return getInstance().beanPropertyMapInitializer(propertyNames);
	}

	public static IBeanModifier<IBeanPropertyMap> beanPropertyMapModifier() {
		return getInstance().beanPropertyMapModifier();
	}

	public static <BEAN_TYPE extends IBean, PARAM_TYPE> IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> executorServiceBuilder(
		final IBeanAccess<? extends BEAN_TYPE> beanAccess) {
		return getInstance().executorServiceBuilder(beanAccess);
	}

	public static IAdapterFactoryProvider adapterFactoryProvider() {
		return getInstance().adapterFactoryProvider();
	}

	public static IDecoratorProviderFactory serviceDecoratorProvider() {
		return getInstance().serviceDecoratorProvider();
	}

	public static <BEAN_TYPE extends IBean> IUpdaterServiceBuilder<BEAN_TYPE> updaterServiceBuilder(
		final IBeanAccess<? extends BEAN_TYPE> beanAccess) {
		return getInstance().updaterServiceBuilder(beanAccess);
	}

	public static <BEAN_TYPE extends IBean> IRefreshServiceBuilder<BEAN_TYPE> refreshServiceBuilder(
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
}
