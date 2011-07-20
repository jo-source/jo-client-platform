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

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.exception.ServiceCanceledException;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.service.api.ICapServiceToolkit;
import org.jowidgets.cap.service.api.adapter.IAdapterFactoryProvider;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.decorator.IDecoratorProviderFactory;
import org.jowidgets.cap.service.api.entity.IBeanServicesProviderBuilder;
import org.jowidgets.cap.service.api.entity.IEntityServiceBuilder;
import org.jowidgets.cap.service.api.executor.IExecutorServiceBuilder;
import org.jowidgets.cap.service.api.refresh.IRefreshServiceBuilder;
import org.jowidgets.cap.service.api.updater.IUpdaterServiceBuilder;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServiceRegistry;
import org.jowidgets.util.concurrent.DaemonThreadFactory;

public final class DefaultCapServiceToolkit implements ICapServiceToolkit {

	private final IAdapterFactoryProvider adapterFactoryProvider;
	private final IDecoratorProviderFactory decoratorProviderFactory;
	private final ScheduledExecutorService scheduledExecutorService;

	public DefaultCapServiceToolkit() {
		this.adapterFactoryProvider = new AdapterFactoryProviderImpl();
		this.decoratorProviderFactory = new DecoratorProviderFactoryImpl();
		this.scheduledExecutorService = Executors.newScheduledThreadPool(20, new DaemonThreadFactory());
	}

	@Override
	public IEntityServiceBuilder entityServiceBuilder() {
		return new EntityServiceBuilderImpl();
	}

	@Override
	public IBeanServicesProviderBuilder beanServicesProviderBuilder(
		final IServiceRegistry registry,
		final IServiceId<IEntityService> entityServiceId,
		final Object entityId) {
		return new BeanServicesProviderBuilderImpl(registry, entityServiceId, entityId);
	}

	@Override
	public <BEAN_TYPE extends IBean> IBeanDtoFactory<BEAN_TYPE> dtoFactory(
		final Class<? extends BEAN_TYPE> beanType,
		final List<String> propertyNames) {
		return new BeanDtoFactoryImpl<BEAN_TYPE>(beanType, propertyNames);
	}

	@Override
	public <BEAN_TYPE extends IBean> IBeanInitializer<BEAN_TYPE> beanInitializer(
		final Class<? extends BEAN_TYPE> beanType,
		final List<String> propertyNames) {
		return new BeanInitializerImpl<BEAN_TYPE>(beanType, propertyNames);
	}

	@Override
	public <BEAN_TYPE extends IBean, PARAM_TYPE> IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> executorServiceBuilder(
		final IBeanAccess<? extends BEAN_TYPE> beanAccess) {
		return new ExecutorServiceBuilderImpl<BEAN_TYPE, PARAM_TYPE>(beanAccess);
	}

	@Override
	public <BEAN_TYPE extends IBean> IUpdaterServiceBuilder<BEAN_TYPE> updaterServiceBuilder(
		final IBeanAccess<? extends BEAN_TYPE> beanAccess) {
		return new UpdaterServiceBuilderImpl<BEAN_TYPE>(beanAccess);
	}

	@Override
	public <BEAN_TYPE extends IBean> IRefreshServiceBuilder<BEAN_TYPE> refreshServiceBuilder(
		final IBeanAccess<? extends BEAN_TYPE> beanAccess) {
		return new RefreshServiceBuilderImpl<BEAN_TYPE>(beanAccess);
	}

	@Override
	public IExecutionCallback delayedExecutionCallback(
		final IExecutionCallback executionCallback,
		final ScheduledExecutorService scheduledExecutorService,
		final Long delay) {
		return new DelayedExecutionCallback(executionCallback, scheduledExecutorService, delay);
	}

	@Override
	public IExecutionCallback delayedExecutionCallback(final IExecutionCallback executionCallback) {
		return delayedExecutionCallback(executionCallback, scheduledExecutorService, null);
	}

	@Override
	public void checkCanceled(final IExecutionCallback executionCallback) {
		if (executionCallback != null && executionCallback.isCanceled()) {
			throw new ServiceCanceledException();
		}
	}

	@Override
	public IAdapterFactoryProvider adapterFactoryProvider() {
		return adapterFactoryProvider;
	}

	@Override
	public IDecoratorProviderFactory serviceDecoratorProvider() {
		return decoratorProviderFactory;
	}

}
