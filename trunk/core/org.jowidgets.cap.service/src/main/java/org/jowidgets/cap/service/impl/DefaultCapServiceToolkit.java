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

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.service.api.ICapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.entity.IBeanServicesProviderBuilder;
import org.jowidgets.cap.service.api.entity.IEntityServiceBuilder;
import org.jowidgets.cap.service.api.executor.IExecutorServiceBuilder;
import org.jowidgets.cap.service.api.refresh.IRefreshServiceBuilder;
import org.jowidgets.cap.service.api.updater.IUpdaterServiceBuilder;
import org.jowidgets.cap.service.impl.bean.BeanDtoFactory;
import org.jowidgets.cap.service.impl.bean.BeanInitializer;
import org.jowidgets.cap.service.impl.entity.BeanServicesProviderBuilder;
import org.jowidgets.cap.service.impl.entity.EntityServiceBuilder;
import org.jowidgets.cap.service.impl.execution.DelayedExecutionCallback;
import org.jowidgets.cap.service.impl.service.ExecutorServiceBuilder;
import org.jowidgets.cap.service.impl.service.RefreshServiceBuilder;
import org.jowidgets.cap.service.impl.service.UpdaterServiceBuilder;
import org.jowidgets.service.api.IServiceRegistry;

public final class DefaultCapServiceToolkit implements ICapServiceToolkit {

	@Override
	public IEntityServiceBuilder createEntityServiceBuilder() {
		return new EntityServiceBuilder();
	}

	@Override
	public <BEAN_TYPE> IBeanServicesProviderBuilder<BEAN_TYPE> createBeanServicesProviderBuilder(final IServiceRegistry registry) {
		return new BeanServicesProviderBuilder<BEAN_TYPE>(registry);
	}

	@Override
	public <BEAN_TYPE extends IBean> IBeanDtoFactory<BEAN_TYPE> createDtoFactory(
		final Class<? extends BEAN_TYPE> beanType,
		final List<String> propertyNames) {
		return new BeanDtoFactory<BEAN_TYPE>(beanType, propertyNames);
	}

	@Override
	public <BEAN_TYPE extends IBean> IBeanInitializer<BEAN_TYPE> createBeanInitializer(
		final Class<? extends BEAN_TYPE> beanType,
		final List<String> propertyNames) {
		return new BeanInitializer<BEAN_TYPE>(beanType, propertyNames);
	}

	@Override
	public <BEAN_TYPE extends IBean, PARAM_TYPE> IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> createExecutorServiceBuilder(
		final IBeanAccess<? extends BEAN_TYPE> beanAccess) {
		return new ExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE>(beanAccess);
	}

	@Override
	public <BEAN_TYPE extends IBean> IUpdaterServiceBuilder<BEAN_TYPE> createUpdaterServiceBuilder(
		final IBeanAccess<? extends BEAN_TYPE> beanAccess) {
		return new UpdaterServiceBuilder<BEAN_TYPE>(beanAccess);
	}

	@Override
	public <BEAN_TYPE extends IBean> IRefreshServiceBuilder<BEAN_TYPE> createRefreshServiceBuilder(
		final IBeanAccess<? extends BEAN_TYPE> beanAccess) {
		return new RefreshServiceBuilder<BEAN_TYPE>(beanAccess);
	}

	@Override
	public IExecutionCallback createDelayedExecutionCallback(final IExecutionCallback executionCallback, final Long delay) {
		return new DelayedExecutionCallback(executionCallback, delay);
	}

	@Override
	public IExecutionCallback createDelayedExecutionCallback(final IExecutionCallback executionCallback) {
		return new DelayedExecutionCallback(executionCallback);
	}

}
