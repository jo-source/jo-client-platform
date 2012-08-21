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

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.service.api.plugin.IServiceIdDecoratorPlugin;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProperties;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServiceRegistry;
import org.jowidgets.service.tools.ServiceId;
import org.jowidgets.util.Assert;

final class BeanServicesProviderBuilderHelper {

	private BeanServicesProviderBuilderHelper() {}

	static IBeanServicesProvider create(
		final IServiceRegistry serviceRegistry,
		final IServiceId<IEntityService> entityServiceId,
		final Class<? extends IBean> beanType,
		final Object entityId,
		final IReaderService<Void> readerService,
		final ICreatorService creatorService,
		final IRefreshService refreshService,
		final IUpdaterService updaterService,
		final IDeleterService deleterService) {

		Assert.paramNotNull(serviceRegistry, "serviceRegistry");
		Assert.paramNotNull(entityServiceId, "entityServiceId");
		Assert.paramNotNull(entityId, "entityId");

		final IServiceId<IReaderService<Void>> readerServiceId = addService(
				serviceRegistry,
				entityServiceId,
				beanType,
				entityId,
				IReaderService.class,
				readerService);

		final IServiceId<ICreatorService> creatorServiceId = addService(
				serviceRegistry,
				entityServiceId,
				beanType,
				entityId,
				ICreatorService.class,
				creatorService);

		final IServiceId<IRefreshService> refreshServiceId = addService(
				serviceRegistry,
				entityServiceId,
				beanType,
				entityId,
				IRefreshService.class,
				refreshService);

		final IServiceId<IUpdaterService> updaterServiceId = addService(
				serviceRegistry,
				entityServiceId,
				beanType,
				entityId,
				IUpdaterService.class,
				updaterService);

		final IServiceId<IDeleterService> deleterServiceId = addService(
				serviceRegistry,
				entityServiceId,
				beanType,
				entityId,
				IDeleterService.class,
				deleterService);

		return CapCommonToolkit.beanServicesProviderFactory().create(
				readerServiceId,
				creatorServiceId,
				refreshServiceId,
				updaterServiceId,
				deleterServiceId);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static <SERVICE_TYPE> IServiceId<SERVICE_TYPE> addService(
		final IServiceRegistry serviceRegistry,
		final IServiceId<IEntityService> entityServiceId,
		final Class<? extends IBean> beanType,
		final Object entityId,
		final Class serviceType,
		final SERVICE_TYPE service) {

		if (service != null) {
			final IServiceId<SERVICE_TYPE> serviceId = createServiceId(entityServiceId, beanType, entityId, serviceType);
			serviceRegistry.addService(serviceId, service);
			return serviceId;
		}
		else {
			return null;
		}
	}

	private static <SERVICE_TYPE> IServiceId<SERVICE_TYPE> createServiceId(
		final IServiceId<IEntityService> entityServiceId,
		final Class<? extends IBean> beanType,
		final Object entityId,
		final Class<SERVICE_TYPE> serviceType) {
		final List<Object> id = new LinkedList<Object>();
		id.add(entityServiceId);
		id.add(entityId);
		id.add(serviceType.getName());
		final ServiceId<SERVICE_TYPE> result = new ServiceId<SERVICE_TYPE>(id, serviceType);
		return decorateServiceId(result, entityServiceId, beanType, entityId, serviceType);
	}

	private static <SERVICE_TYPE> IServiceId<SERVICE_TYPE> decorateServiceId(
		final IServiceId<SERVICE_TYPE> defaultId,
		final IServiceId<IEntityService> entityServiceId,
		final Class<? extends IBean> beanType,
		final Object entityId,
		final Class<SERVICE_TYPE> serviceType) {

		IServiceId<SERVICE_TYPE> result = defaultId;

		final IPluginPropertiesBuilder propertiesBuilder = PluginProperties.builder();
		propertiesBuilder.add(IServiceIdDecoratorPlugin.BEAN_TYPE_PROPERTY_KEY, beanType);
		propertiesBuilder.add(IServiceIdDecoratorPlugin.ENTITIY_ID_PROPERTY_KEY, entityId);
		for (final IServiceIdDecoratorPlugin plugin : PluginProvider.getPlugins(
				IServiceIdDecoratorPlugin.ID,
				propertiesBuilder.build())) {
			result = plugin.decorateServiceId(result, entityServiceId, beanType, entityId, serviceType);
		}

		return result;
	}

}
