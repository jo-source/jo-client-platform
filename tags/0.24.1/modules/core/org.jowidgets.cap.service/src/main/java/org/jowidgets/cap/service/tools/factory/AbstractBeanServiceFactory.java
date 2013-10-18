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
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.BeanDtoFactory;
import org.jowidgets.cap.service.api.bean.BeanInitializer;
import org.jowidgets.cap.service.api.bean.BeanModifier;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.bean.IBeanModifier;
import org.jowidgets.cap.service.api.creator.ICreatorServiceBuilder;
import org.jowidgets.cap.service.api.deleter.IDeleterServiceBuilder;
import org.jowidgets.cap.service.api.entity.IBeanServicesProviderBuilder;
import org.jowidgets.cap.service.api.factory.IBeanServiceFactory;
import org.jowidgets.cap.service.api.refresh.IRefreshServiceBuilder;
import org.jowidgets.cap.service.api.updater.IUpdaterServiceBuilder;
import org.jowidgets.service.api.IServiceRegistry;
import org.jowidgets.util.Assert;

public abstract class AbstractBeanServiceFactory implements IBeanServiceFactory {

	@Override
	public <BEAN_TYPE extends IBean> IBeanServicesProviderBuilder beanServicesBuilder(
		final IServiceRegistry registry,
		final Object entityId,
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId,
		final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory,
		final IBeanInitializer<BEAN_TYPE> beanInitializer,
		final IBeanModifier<BEAN_TYPE> beanModifier) {

		Assert.paramNotNull(registry, "registry");
		Assert.paramNotNull(entityId, "entityId");
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(beanDtoFactory, "beanDtoFactory");
		Assert.paramNotNull(beanInitializer, "beanInitializer");
		Assert.paramNotNull(beanModifier, "beanModifier");

		final IBeanServicesProviderBuilder builder;
		builder = CapServiceToolkit.beanServicesProviderBuilder(registry, IEntityService.ID, beanType, entityId);

		//create bean access
		final IBeanAccess<BEAN_TYPE> beanAccess = beanAccess(beanType, beanTypeId);

		//set creator service
		final ICreatorServiceBuilder<BEAN_TYPE> creatorServiceBuilder = creatorServiceBuilder(beanType, beanTypeId);
		creatorServiceBuilder.setBeanDtoFactory(beanDtoFactory).setBeanInitializer(beanInitializer);
		builder.setCreatorService(creatorServiceBuilder.build());

		//set reader service
		final IReaderService<Void> readerService = readerService(beanType, beanTypeId, beanDtoFactory);
		builder.setReaderService(readerService);

		//set refresh
		builder.setRefreshService(CapServiceToolkit.refreshServiceBuilder(beanAccess).setBeanDtoFactory(beanDtoFactory).build());

		//set updater service
		final IUpdaterServiceBuilder<BEAN_TYPE> updaterServiceBuilder = CapServiceToolkit.updaterServiceBuilder(beanAccess);
		updaterServiceBuilder.setBeanDtoFactory(beanDtoFactory).setBeanModifier(beanModifier);
		builder.setUpdaterService(updaterServiceBuilder.build());

		//set deleter service
		builder.setDeleterService(deleterService(beanType, beanTypeId));

		return builder;
	}

	@Override
	public <BEAN_TYPE extends IBean> IBeanAccess<BEAN_TYPE> beanAccess(final Class<? extends BEAN_TYPE> beanType) {
		return beanAccess(beanType, beanType);
	}

	@Override
	public <BEAN_TYPE extends IBean> IBeanServicesProviderBuilder beanServicesBuilder(
		final IServiceRegistry registry,
		final Object entityId,
		final Class<? extends BEAN_TYPE> beanType,
		final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory,
		final IBeanInitializer<BEAN_TYPE> beanInitializer,
		final IBeanModifier<BEAN_TYPE> beanModifier) {
		return beanServicesBuilder(registry, entityId, beanType, beanType, beanDtoFactory, beanInitializer, beanModifier);
	}

	@Override
	public <BEAN_TYPE extends IBean> ICreatorServiceBuilder<BEAN_TYPE> creatorServiceBuilder(
		final Class<? extends BEAN_TYPE> beanType) {
		Assert.paramNotNull(beanType, "beanType");
		return creatorServiceBuilder(beanType, beanType);
	}

	@Override
	public <BEAN_TYPE extends IBean, PARAM_TYPE> IReaderService<PARAM_TYPE> readerService(
		final Class<? extends BEAN_TYPE> beanType,
		final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory) {
		return readerService(beanType, beanType, beanDtoFactory);
	}

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
	public <BEAN_TYPE extends IBean> IRefreshServiceBuilder<BEAN_TYPE> refreshServiceBuilder(
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId) {
		return CapServiceToolkit.refreshServiceBuilder(beanAccess(beanType, beanTypeId));
	}

	@Override
	public <BEAN_TYPE extends IBean> IUpdaterServiceBuilder<BEAN_TYPE> updaterServiceBuilder(
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId) {
		return CapServiceToolkit.updaterServiceBuilder(beanAccess(beanType, beanTypeId));
	}

	@Override
	public <BEAN_TYPE extends IBean> IDeleterServiceBuilder<BEAN_TYPE> deleterServiceBuilder(
		final Class<? extends BEAN_TYPE> beanType) {
		return deleterServiceBuilder(beanType, beanType);
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

	@Override
	public ICreatorService creatorService(
		final Class<? extends IBean> beanType,
		final Object beanTypeId,
		final Collection<String> propertyNames) {
		return creatorServiceBuilder(beanType, beanTypeId).setBeanDtoFactoryAndBeanInitializer(propertyNames).build();
	}

	@Override
	public <PARAM_TYPE> IReaderService<PARAM_TYPE> readerService(
		final Class<? extends IBean> beanType,
		final Object beanTypeId,
		final Collection<String> propertyNames) {
		return readerService(beanType, beanTypeId, BeanDtoFactory.create(beanType, propertyNames));
	}

	@Override
	public IRefreshService refreshService(
		final Class<? extends IBean> beanType,
		final Object beanTypeId,
		final Collection<String> propertyNames) {
		return refreshServiceBuilder(beanType, beanTypeId).setBeanDtoFactory(propertyNames).build();
	}

	@Override
	public IUpdaterService updaterService(
		final Class<? extends IBean> beanType,
		final Object beanTypeId,
		final Collection<String> propertyNames) {
		return updaterServiceBuilder(beanType, beanTypeId).setBeanDtoFactoryAndBeanModifier(propertyNames).build();
	}

	@Override
	public IDeleterService deleterService(final Class<? extends IBean> beanType, final Object beanTypeId) {
		return deleterServiceBuilder(beanType, beanTypeId).build();
	}

}
