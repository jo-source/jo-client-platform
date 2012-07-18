/*
 * Copyright (c) 2012, grossmann
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

package org.jowidgets.cap.service.tools.factory;

import java.util.Collection;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.BeanDtoFactory;
import org.jowidgets.cap.service.api.bean.BeanInitializer;
import org.jowidgets.cap.service.api.bean.BeanModifier;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.bean.IBeanModifier;
import org.jowidgets.cap.service.api.entity.IBeanServicesProviderBuilder;
import org.jowidgets.cap.service.api.factory.IBeanServiceFactory;
import org.jowidgets.cap.service.api.refresh.IRefreshServiceBuilder;
import org.jowidgets.cap.service.api.updater.IUpdaterServiceBuilder;
import org.jowidgets.service.api.IServiceRegistry;

public abstract class AbstractBeanServiceFactory implements IBeanServiceFactory {

	@Override
	public <BEAN_TYPE extends IBean> IRefreshServiceBuilder<BEAN_TYPE> refreshServiceBuilder(
		final Class<? extends BEAN_TYPE> beanType) {
		return CapServiceToolkit.refreshServiceBuilder(beanAccess(beanType));
	}

	@Override
	public <BEAN_TYPE extends IBean> IUpdaterServiceBuilder<BEAN_TYPE> updaterServiceBuilder(
		final Class<? extends BEAN_TYPE> beanType) {
		return CapServiceToolkit.updaterServiceBuilder(beanAccess(beanType));
	}

	@Override
	public IBeanServicesProviderBuilder beanServicesBuilder(
		final IServiceRegistry registry,
		final Object entityId,
		final Class<? extends IBean> beanType,
		final Collection<String> propertyNames) {
		return beanServicesBuilder(
				registry,
				entityId,
				beanType,
				BeanDtoFactory.create(beanType, propertyNames),
				BeanInitializer.create(beanType, propertyNames),
				BeanModifier.create(beanType, propertyNames));
	}

	@Override
	public IBeanServicesProvider beanServices(
		final IServiceRegistry registry,
		final Object entityId,
		final Class<? extends IBean> beanType,
		final Collection<String> properties) {
		return beanServicesBuilder(registry, entityId, beanType, properties).build();
	}

	@Override
	public <BEAN_TYPE extends IBean> IBeanServicesProvider beanServices(
		final IServiceRegistry registry,
		final Object entityId,
		final Class<? extends BEAN_TYPE> beanType,
		final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory,
		final IBeanInitializer<BEAN_TYPE> beanInitializer,
		final IBeanModifier<BEAN_TYPE> beanModifier) {
		return beanServicesBuilder(registry, entityId, beanType, beanDtoFactory, beanInitializer, beanModifier).build();
	}

	@Override
	public ICreatorService creatorService(final Class<? extends IBean> beanType, final Collection<String> propertyNames) {
		return creatorServiceBuilder(beanType).setBeanDtoFactoryAndBeanInitializer(propertyNames).build();
	}

	@Override
	public <PARAM_TYPE> IReaderService<PARAM_TYPE> readerService(
		final Class<? extends IBean> beanType,
		final Collection<String> propertyNames) {
		return readerService(beanType, BeanDtoFactory.create(beanType, propertyNames));
	}

	@Override
	public IRefreshService refreshService(final Class<? extends IBean> beanType, final Collection<String> propertyNames) {
		return refreshServiceBuilder(beanType).setBeanDtoFactory(propertyNames).build();
	}

	@Override
	public IUpdaterService updaterService(final Class<? extends IBean> beanType, final Collection<String> propertyNames) {
		return updaterServiceBuilder(beanType).setBeanDtoFactoryAndBeanModifier(propertyNames).build();
	}

	@Override
	public IDeleterService deleterService(final Class<? extends IBean> beanType) {
		return deleterServiceBuilder(beanType).build();
	}

}
