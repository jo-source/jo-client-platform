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

package org.jowidgets.cap.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptorBuilder;
import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.entity.EntityLinkDescriptor;
import org.jowidgets.cap.common.api.entity.EntityLinkProperties;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptorBuilder;
import org.jowidgets.cap.common.api.entity.IEntityLinkProperties;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.ILinkCreatorService;
import org.jowidgets.cap.common.api.service.ILinkDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.adapter.ISyncCreatorService;
import org.jowidgets.cap.service.api.adapter.ISyncDeleterService;
import org.jowidgets.cap.service.api.adapter.ISyncReaderService;
import org.jowidgets.cap.service.api.adapter.ISyncRefreshService;
import org.jowidgets.cap.service.api.adapter.ISyncUpdaterService;
import org.jowidgets.cap.service.api.bean.BeanDtoFactory;
import org.jowidgets.cap.service.api.bean.BeanInitializer;
import org.jowidgets.cap.service.api.bean.BeanModifier;
import org.jowidgets.cap.service.api.creator.ICreatorServiceBuilder;
import org.jowidgets.cap.service.api.entity.IBeanEntityBluePrint;
import org.jowidgets.cap.service.api.entity.IBeanEntityLinkBluePrint;
import org.jowidgets.cap.service.api.entity.IBeanEntityServiceBuilder;
import org.jowidgets.cap.service.api.entity.IBeanServicesProviderBuilder;
import org.jowidgets.cap.service.api.factory.IBeanServiceFactory;
import org.jowidgets.cap.service.api.link.ILinkServicesBuilder;
import org.jowidgets.cap.service.api.link.LinkServicesBuilder;
import org.jowidgets.cap.service.api.plugin.IServiceIdDecoratorPlugin;
import org.jowidgets.cap.service.api.updater.IUpdaterServiceBuilder;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProperties;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServiceRegistry;
import org.jowidgets.service.tools.ServiceId;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IAdapterFactory;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.maybe.Nothing;
import org.jowidgets.util.maybe.Some;

final class BeanEntityServiceBuilderImpl extends EntityServiceBuilderImpl implements IBeanEntityServiceBuilder {

	private final IServiceRegistry serviceRegistry;
	private final IBeanServiceFactory beanServiceFactory;
	private final IServiceId<IEntityService> entityServiceId;

	private final List<BeanEntityBluePrintImpl> beanEntities;

	private boolean exhausted;

	BeanEntityServiceBuilderImpl(final IBeanServiceFactory beanServiceFactory, final IServiceRegistry serviceRegistry) {
		this(beanServiceFactory, serviceRegistry, IEntityService.ID);
	}

	BeanEntityServiceBuilderImpl(
		final IBeanServiceFactory beanServiceFactory,
		final IServiceRegistry serviceRegistry,
		final IServiceId<IEntityService> entityServiceId) {

		this.beanServiceFactory = beanServiceFactory;
		this.serviceRegistry = serviceRegistry;
		this.entityServiceId = entityServiceId;

		this.beanEntities = new LinkedList<BeanEntityBluePrintImpl>();

		this.exhausted = false;
	}

	@Override
	public IBeanEntityBluePrint addEntity() {
		checkExhausted();
		final BeanEntityBluePrintImpl result = new BeanEntityBluePrintImpl();
		beanEntities.add(result);
		return result;
	}

	@Override
	public IEntityService build() {
		checkExhausted();
		final Map<Object, BeanEntityPreBuild> prebuilds = createPrebuilds();
		for (final BeanEntityBluePrintImpl beanEntityBp : beanEntities) {
			final BeanEntityPreBuild preBuild = prebuilds.get(beanEntityBp.getEntityId());
			add(
					preBuild.getEntityId(),
					preBuild.getDtoDescriptor(),
					preBuild.getBeanServicesProvider(),
					beanEntityBp.getLinkDescriptors(prebuilds));
		}
		this.exhausted = true;
		return super.build();
	}

	private Map<Object, BeanEntityPreBuild> createPrebuilds() {
		final Map<Object, BeanEntityPreBuild> result = new HashMap<Object, BeanEntityPreBuild>();
		for (final BeanEntityBluePrintImpl beanEntityBp : beanEntities) {
			result.put(beanEntityBp.getEntityId(), beanEntityBp.createPreBuild());
		}
		return result;
	}

	private void checkExhausted() {
		if (exhausted) {
			throw new IllegalStateException("Builder exhausted. The builder is a single use builder and can only be used once");
		}
	}

	private final class BeanEntityBluePrintImpl implements IBeanEntityBluePrint {

		private final List<BeanEntityLinkBluePrintImpl> linkBps;

		private Object entityId;
		private Class<? extends IBean> beanType;
		private Object beanTypeId;
		private IBeanDtoDescriptor dtoDescriptor;
		private Collection<String> properties;
		private IMaybe<IReaderService<Void>> readerService;
		private IMaybe<ICreatorService> creatorService;
		private IMaybe<IRefreshService> refreshService;
		private IMaybe<IUpdaterService> updaterService;
		private IMaybe<IDeleterService> deleterService;

		private BeanEntityBluePrintImpl() {
			this.linkBps = new LinkedList<BeanEntityLinkBluePrintImpl>();
		}

		@Override
		public IBeanEntityBluePrint setEntityId(final Object entityId) {
			checkExhausted();
			Assert.paramNotNull(entityId, "entityId");
			this.entityId = entityId;
			return this;
		}

		@Override
		public IBeanEntityBluePrint setBeanType(final Class<? extends IBean> beanType) {
			checkExhausted();
			Assert.paramNotNull(beanType, "beanType");
			this.beanType = beanType;
			return this;
		}

		@Override
		public IBeanEntityBluePrint setBeanTypeId(final Object beanTypeId) {
			checkExhausted();
			Assert.paramNotNull(beanTypeId, "beanTypeId");
			this.beanTypeId = beanTypeId;
			return this;
		}

		@Override
		public IBeanEntityBluePrint setDtoDescriptor(final IBeanDtoDescriptor descriptor) {
			checkExhausted();
			Assert.paramNotNull(descriptor, "descriptor");
			this.dtoDescriptor = descriptor;
			return this;
		}

		@Override
		public IBeanEntityBluePrint setDtoDescriptor(final IBeanDtoDescriptorBuilder builder) {
			checkExhausted();
			Assert.paramNotNull(builder, "builder");
			return setDtoDescriptor(builder.build());
		}

		@Override
		public IBeanEntityBluePrint setProperties(final Collection<String> properties) {
			checkExhausted();
			if (properties != null) {
				this.properties = new LinkedList<String>(properties);
			}
			else {
				this.properties = null;
			}
			return this;
		}

		@Override
		public IBeanEntityBluePrint setReaderService(final IReaderService<Void> readerService) {
			checkExhausted();
			if (readerService != null) {
				this.readerService = new Some<IReaderService<Void>>(readerService);
			}
			else {
				this.readerService = Nothing.getInstance();
			}
			return this;
		}

		@Override
		public IBeanEntityBluePrint setReaderService(final ISyncReaderService<Void> readerService) {
			checkExhausted();
			final IAdapterFactory<IReaderService<Void>, ISyncReaderService<Void>> readerAdapterFactory = CapServiceToolkit.adapterFactoryProvider().reader();
			return setReaderService(readerAdapterFactory.createAdapter(readerService));
		}

		@Override
		public IBeanEntityBluePrint setCreatorService(final ICreatorService creatorService) {
			checkExhausted();
			if (creatorService != null) {
				this.creatorService = new Some<ICreatorService>(creatorService);
			}
			else {
				this.creatorService = Nothing.getInstance();
			}
			return this;
		}

		@Override
		public IBeanEntityBluePrint setCreatorService(final ISyncCreatorService creatorService) {
			checkExhausted();
			if (creatorService != null) {
				return setCreatorService(CapServiceToolkit.adapterFactoryProvider().creator().createAdapter(creatorService));
			}
			else {
				return setCreatorService((ICreatorService) null);
			}
		}

		@Override
		public IBeanEntityBluePrint setRefreshService(final IRefreshService refreshService) {
			checkExhausted();
			if (refreshService != null) {
				this.refreshService = new Some<IRefreshService>(refreshService);
			}
			else {
				this.refreshService = Nothing.getInstance();
			}
			return this;
		}

		@Override
		public IBeanEntityBluePrint setRefreshService(final ISyncRefreshService refreshService) {
			checkExhausted();
			return setRefreshService(CapServiceToolkit.adapterFactoryProvider().refresh().createAdapter(refreshService));
		}

		@Override
		public IBeanEntityBluePrint setUpdaterService(final IUpdaterService updaterService) {
			checkExhausted();
			if (updaterService != null) {
				this.updaterService = new Some<IUpdaterService>(updaterService);
			}
			else {
				this.updaterService = Nothing.getInstance();
			}
			return this;
		}

		@Override
		public IBeanEntityBluePrint setUpdaterService(final ISyncUpdaterService updaterService) {
			checkExhausted();
			return setUpdaterService(CapServiceToolkit.adapterFactoryProvider().updater().createAdapter(updaterService));
		}

		@Override
		public IBeanEntityBluePrint setDeleterService(final IDeleterService deleterService) {
			checkExhausted();
			if (deleterService != null) {
				this.deleterService = new Some<IDeleterService>(deleterService);
			}
			else {
				this.deleterService = Nothing.getInstance();
			}
			return this;
		}

		@Override
		public IBeanEntityBluePrint setDeleterService(final ISyncDeleterService deleterService) {
			checkExhausted();
			if (deleterService != null) {
				setDeleterService(CapServiceToolkit.adapterFactoryProvider().deleter().createAdapter(deleterService));
			}
			else {
				setDeleterService((IDeleterService) null);
			}
			return this;
		}

		@Override
		public IBeanEntityBluePrint setReadonly() {
			checkExhausted();
			setUpdaterService((IUpdaterService) null);
			setCreatorService((ICreatorService) null);
			setDeleterService((IDeleterService) null);
			return this;
		}

		@Override
		public IBeanEntityLinkBluePrint addLink() {
			checkExhausted();
			final BeanEntityLinkBluePrintImpl result = new BeanEntityLinkBluePrintImpl(this);
			linkBps.add(result);
			return result;
		}

		Object getEntityId() {
			return entityId;
		}

		private Collection<String> getPropertyNames() {
			if (properties != null) {
				return properties;
			}
			else if (dtoDescriptor != null) {
				final List<String> result = new LinkedList<String>();
				for (final IProperty property : dtoDescriptor.getProperties()) {
					result.add(property.getName());
				}
				return result;
			}
			else {
				throw new IllegalStateException("Proprties could not be created");
			}
		}

		BeanEntityPreBuild createPreBuild() {
			final Collection<String> propertyNames = getPropertyNames();
			return new BeanEntityPreBuild(
				entityId,
				beanType,
				beanTypeId,
				dtoDescriptor,
				propertyNames,
				readerService,
				creatorService,
				refreshService,
				updaterService,
				deleterService);
		}

		Collection<? extends IEntityLinkDescriptor> getLinkDescriptors(final Map<Object, BeanEntityPreBuild> prebuilds) {
			final List<IEntityLinkDescriptor> result = new LinkedList<IEntityLinkDescriptor>();
			for (final BeanEntityLinkBluePrintImpl linkBp : linkBps) {
				result.add(linkBp.build(prebuilds));
			}
			return result;
		}

	}

	private final class BeanEntityPreBuild {

		private final Object entityId;
		private final Class<? extends IBean> beanType;
		private final Object beanTypeId;
		private final IBeanDtoDescriptor dtoDescriptor;
		private final Collection<String> propertyNames;
		private final IReaderService<Void> readerService;
		private final ICreatorService creatorService;
		private final IRefreshService refreshService;
		private final IUpdaterService updaterService;
		private final IDeleterService deleterService;
		private final IBeanServicesProvider beanServicesProvider;

		private BeanEntityPreBuild(
			final Object entityId,
			final Class<? extends IBean> beanType,
			final Object beanTypeId,
			final IBeanDtoDescriptor dtoDescriptor,
			final Collection<String> propertyNames,
			final IMaybe<IReaderService<Void>> readerServiceMaybe,
			final IMaybe<ICreatorService> creatorServiceMaybe,
			final IMaybe<IRefreshService> refreshServiceMaybe,
			final IMaybe<IUpdaterService> updaterServiceMaybe,
			final IMaybe<IDeleterService> deleterServiceMaybe) {

			this.entityId = entityId;
			this.beanType = beanType;
			this.beanTypeId = beanTypeId;
			this.propertyNames = propertyNames;
			this.dtoDescriptor = dtoDescriptor;
			this.readerService = getReaderService(readerServiceMaybe);
			this.creatorService = getCreatorService(creatorServiceMaybe);
			this.refreshService = getRefreshService(refreshServiceMaybe);
			this.updaterService = getUpdaterService(updaterServiceMaybe);
			this.deleterService = getDeleterService(deleterServiceMaybe);
			this.beanServicesProvider = createBeanServicesProvider(
					readerServiceMaybe,
					creatorServiceMaybe,
					refreshServiceMaybe,
					updaterServiceMaybe,
					deleterServiceMaybe,
					propertyNames);
		}

		private IReaderService<Void> getReaderService(final IMaybe<IReaderService<Void>> readerServiceMaybe) {
			if (readerServiceMaybe != null) {
				if (readerServiceMaybe.isNothing()) {
					return null;
				}
				else {
					return readerServiceMaybe.getValue();
				}
			}
			else {
				return beanServiceFactory.readerService(beanType, getBeanTypeId(), BeanDtoFactory.create(beanType, propertyNames));
			}
		}

		private ICreatorService getCreatorService(final IMaybe<ICreatorService> creatorServiceMaybe) {
			if (creatorServiceMaybe != null) {
				if (creatorServiceMaybe.isNothing()) {
					return null;
				}
				else {
					return creatorServiceMaybe.getValue();
				}
			}
			else {
				final ICreatorServiceBuilder<IBean> creatorServiceBuilder = beanServiceFactory.creatorServiceBuilder(
						beanType,
						getBeanTypeId());
				return creatorServiceBuilder.setBeanDtoFactoryAndBeanInitializer(propertyNames).build();
			}
		}

		private IRefreshService getRefreshService(final IMaybe<IRefreshService> refreshServiceMaybe) {
			if (refreshServiceMaybe != null) {
				if (refreshServiceMaybe.isNothing()) {
					return null;
				}
				else {
					return refreshServiceMaybe.getValue();
				}
			}
			else {
				return CapServiceToolkit.refreshServiceBuilder(beanServiceFactory.beanAccess(beanType, getBeanTypeId())).build();
			}
		}

		private IUpdaterService getUpdaterService(final IMaybe<IUpdaterService> updaterServiceMaybe) {
			if (updaterServiceMaybe != null) {
				if (updaterServiceMaybe.isNothing()) {
					return null;
				}
				else {
					return updaterServiceMaybe.getValue();
				}
			}
			else {
				final IUpdaterServiceBuilder<IBean> updaterServiceBuilder = CapServiceToolkit.updaterServiceBuilder(beanServiceFactory.beanAccess(
						beanType,
						getBeanTypeId()));
				return updaterServiceBuilder.setBeanDtoFactoryAndBeanModifier(propertyNames).build();
			}
		}

		private IDeleterService getDeleterService(final IMaybe<IDeleterService> deleterServiceMaybe) {
			if (deleterServiceMaybe != null) {
				if (deleterServiceMaybe.isNothing()) {
					return null;
				}
				else {
					return deleterServiceMaybe.getValue();
				}
			}
			else {
				return beanServiceFactory.deleterServiceBuilder(beanType, getBeanTypeId()).build();
			}
		}

		Object getEntityId() {
			return entityId;
		}

		Object getBeanTypeId() {
			if (beanTypeId != null) {
				return beanTypeId;
			}
			else {
				return beanType;
			}
		}

		IBeanDtoDescriptor getDtoDescriptor() {
			return dtoDescriptor;
		}

		ICreatorService getCreatorService() {
			return creatorService;
		}

		IReaderService<Void> getReaderService() {
			return readerService;
		}

		IDeleterService getDeleterService() {
			return deleterService;
		}

		IBeanServicesProvider getBeanServicesProvider() {
			return beanServicesProvider;
		}

		Class<? extends IBean> getBeanType() {
			return beanType;
		}

		Collection<String> getPropertyNames() {
			return propertyNames;
		}

		private IBeanServicesProvider createBeanServicesProvider(
			final IMaybe<IReaderService<Void>> readerServiceMaybe,
			final IMaybe<ICreatorService> creatorServiceMaybe,
			final IMaybe<IRefreshService> refreshServiceMaybe,
			final IMaybe<IUpdaterService> updaterServiceMaybe,
			final IMaybe<IDeleterService> deleterServiceMaybe,
			final Collection<String> propertyNames) {

			final IBeanServicesProviderBuilder builder = beanServiceFactory.beanServicesBuilder(
					serviceRegistry,
					entityId,
					beanType,
					getBeanTypeId(),
					BeanDtoFactory.create(beanType, propertyNames),
					BeanInitializer.create(beanType, propertyNames),
					BeanModifier.create(beanType, propertyNames));

			if (creatorServiceMaybe != null) {
				builder.setCreatorService(creatorService);
			}
			if (readerServiceMaybe != null) {
				builder.setReaderService(readerService);
			}
			if (refreshServiceMaybe != null) {
				builder.setRefreshService(refreshService);
			}
			if (updaterServiceMaybe != null) {
				builder.setUpdaterService(updaterService);
			}
			if (deleterServiceMaybe != null) {
				builder.setDeleterService(deleterService);
			}

			return builder.build();
		}

	}

	private final class BeanEntityLinkBluePrintImpl implements IBeanEntityLinkBluePrint {

		private final BeanEntityBluePrintImpl linkSource;

		@SuppressWarnings("rawtypes")
		private ILinkServicesBuilder linkServicesBuilder;

		private Object linkEntityId;
		private Class<? extends IBean> linkBeanType;
		private Object linkBeanTypeId;
		private Object linkedEntityId;
		private Object linkableEntityId;
		private IEntityLinkProperties sourceProperties;
		private IEntityLinkProperties destinationProperties;
		private IMaybe<IServiceId<ILinkCreatorService>> creatorService;
		private IMaybe<IServiceId<ILinkDeleterService>> deleterService;
		private boolean symmetric;

		BeanEntityLinkBluePrintImpl(final BeanEntityBluePrintImpl linkSource) {
			this.symmetric = false;
			this.linkSource = linkSource;
		}

		@Override
		public IBeanEntityLinkBluePrint setLinkEntityId(final Object id) {
			checkExhausted();
			this.linkEntityId = id;
			return this;
		}

		@Override
		public IBeanEntityLinkBluePrint setLinkBeanType(final Class<? extends IBean> beanType) {
			checkExhausted();
			this.linkBeanType = beanType;
			return this;
		}

		@Override
		public IBeanEntityLinkBluePrint setLinkBeanTypeId(final Object beanTypeId) {
			checkExhausted();
			this.linkBeanTypeId = beanTypeId;
			return this;
		}

		@Override
		public IBeanEntityLinkBluePrint setLinkedEntityId(final Object id) {
			checkExhausted();
			this.linkedEntityId = id;
			return this;
		}

		@Override
		public IBeanEntityLinkBluePrint setLinkableEntityId(final Object id) {
			checkExhausted();
			this.linkableEntityId = id;
			return this;
		}

		@Override
		public IBeanEntityLinkBluePrint setSymmetric(final boolean symmetric) {
			checkExhausted();
			this.symmetric = symmetric;
			return this;
		}

		@Override
		public IBeanEntityLinkBluePrint setSourceProperties(final IEntityLinkProperties properties) {
			checkExhausted();
			this.sourceProperties = properties;
			return this;
		}

		@Override
		public IBeanEntityLinkBluePrint setSourceProperties(final String keyPropertyName, final String foreignKeyPropertyName) {
			checkExhausted();
			return setSourceProperties(EntityLinkProperties.create(keyPropertyName, foreignKeyPropertyName));
		}

		@Override
		public IBeanEntityLinkBluePrint setSourceProperties(final String foreignKeyPropertyName) {
			checkExhausted();
			return setSourceProperties(EntityLinkProperties.create(foreignKeyPropertyName));
		}

		@Override
		public IBeanEntityLinkBluePrint setDestinationProperties(final IEntityLinkProperties properties) {
			checkExhausted();
			this.destinationProperties = properties;
			return this;
		}

		@Override
		public IBeanEntityLinkBluePrint setDestinationProperties(final String keyPropertyName, final String foreignKeyPropertyName) {
			checkExhausted();
			return setDestinationProperties(EntityLinkProperties.create(keyPropertyName, foreignKeyPropertyName));
		}

		@Override
		public IBeanEntityLinkBluePrint setDestinationProperties(final String foreignKeyPropertyName) {
			checkExhausted();
			return setDestinationProperties(EntityLinkProperties.create(foreignKeyPropertyName));
		}

		@Override
		public IBeanEntityLinkBluePrint setLinkCreatorService(final IServiceId<ILinkCreatorService> serviceId) {
			checkExhausted();
			if (serviceId != null) {
				creatorService = new Some<IServiceId<ILinkCreatorService>>(serviceId);
			}
			else {
				creatorService = Nothing.getInstance();
			}
			return this;
		}

		@Override
		public IBeanEntityLinkBluePrint setLinkDeleterService(final IServiceId<ILinkDeleterService> serviceId) {
			checkExhausted();
			if (serviceId != null) {
				deleterService = new Some<IServiceId<ILinkDeleterService>>(serviceId);
			}
			else {
				deleterService = Nothing.getInstance();
			}
			return this;
		}

		private Object getLinkBeanTypeId() {
			if (linkBeanTypeId != null) {
				return linkBeanTypeId;
			}
			else {
				return linkBeanType;
			}
		}

		IEntityLinkDescriptor build(final Map<Object, BeanEntityPreBuild> prebuilds) {
			if (linkedEntityId == null) {
				throw new IllegalStateException("Missing mandatory parameters: "
					+ "linkEntityId = '"
					+ linkEntityId
					+ "', linkedEntityId = '"
					+ linkedEntityId);
			}
			final IEntityLinkDescriptorBuilder builder = EntityLinkDescriptor.builder();
			builder.setLinkEntityId(linkEntityId).setLinkedEntityId(linkedEntityId).setLinkableEntityId(linkableEntityId);
			builder.setSourceProperties(sourceProperties);
			builder.setDestinationProperties(destinationProperties);

			if (creatorService != null) {
				if (creatorService.isSomething()) {
					builder.setLinkCreatorService(creatorService.getValue());
				}
			}
			else if (sourceProperties != null) {
				builder.setLinkCreatorService(createCreatorService(prebuilds));
			}
			if (deleterService != null) {
				if (deleterService.isSomething()) {
					builder.setLinkDeleterService(deleterService.getValue());
				}
			}
			else if (sourceProperties != null) {
				builder.setLinkDeleterService(createDeleterService(prebuilds));
			}
			return builder.build();
		}

		private IServiceId<ILinkCreatorService> createCreatorService(final Map<Object, BeanEntityPreBuild> prebuilds) {
			final ILinkCreatorService service = getLinkServicesBuilder(prebuilds).tryBuildCreatorService();
			if (service != null) {
				final IServiceId<ILinkCreatorService> result = createServiceId(ILinkCreatorService.class);
				serviceRegistry.addService(result, service);
				return result;
			}
			else {
				return null;
			}
		}

		private IServiceId<ILinkDeleterService> createDeleterService(final Map<Object, BeanEntityPreBuild> prebuilds) {
			final ILinkDeleterService service = getLinkServicesBuilder(prebuilds).tryBuildDeleterService();
			if (service != null) {
				final IServiceId<ILinkDeleterService> result = createServiceId(ILinkDeleterService.class);
				serviceRegistry.addService(result, service);
				return result;
			}
			else {
				return null;
			}
		}

		private <SERVICE_TYPE> IServiceId<SERVICE_TYPE> createServiceId(final Class<SERVICE_TYPE> serviceType) {
			final List<Object> id = new LinkedList<Object>();
			id.add(entityServiceId);
			id.add(linkSource.getEntityId());
			id.add(linkEntityId);
			id.add(linkedEntityId);
			id.add(linkableEntityId);
			id.add(serviceType.getName());
			final ServiceId<SERVICE_TYPE> result = new ServiceId<SERVICE_TYPE>(id, serviceType);
			return decorateServiceId(result, entityServiceId, linkBeanType, linkEntityId, serviceType);
		}

		private <SERVICE_TYPE> IServiceId<SERVICE_TYPE> decorateServiceId(
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
				result = plugin.decorateServiceId(defaultId, entityServiceId, beanType, entityId, serviceType);
			}

			return result;
		}

		@SuppressWarnings("rawtypes")
		private ILinkServicesBuilder getLinkServicesBuilder(final Map<Object, BeanEntityPreBuild> prebuilds) {
			if (linkServicesBuilder == null) {
				linkServicesBuilder = createLinkServicesBuilder(prebuilds);
			}
			return linkServicesBuilder;
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		private ILinkServicesBuilder createLinkServicesBuilder(final Map<Object, BeanEntityPreBuild> prebuilds) {
			final ILinkServicesBuilder builder = LinkServicesBuilder.create();
			builder.setSymmetric(symmetric);
			builder.setSourceProperties(sourceProperties);
			builder.setDestinationProperties(destinationProperties);

			final BeanEntityPreBuild sourcePrebuild = prebuilds.get(linkSource.getEntityId());
			if (sourcePrebuild != null) {
				builder.setSourceCreatorService(sourcePrebuild.getCreatorService());
				builder.setSourceDeleterService(sourcePrebuild.getDeleterService());
			}

			final BeanEntityPreBuild linkPrebuild = prebuilds.get(linkEntityId);
			if (linkPrebuild != null) {
				if (linkPrebuild.getCreatorService() != null) {
					builder.setLinkCreatorService(linkPrebuild.getCreatorService());
				}
				else {
					builder.setLinkCreatorService(createCreatorService(
							linkPrebuild.getBeanType(),
							linkPrebuild.getBeanTypeId(),
							linkPrebuild.getPropertyNames()));
				}
				if (linkPrebuild.getDeleterService() != null) {
					builder.setLinkDeleterService(linkPrebuild.getDeleterService());
				}
				else {
					builder.setLinkDeleterService(createDeleterService(linkPrebuild.getBeanType(), linkPrebuild.getBeanTypeId()));
				}
				if (linkPrebuild.getReaderService() != null) {
					builder.setAllLinksReaderService(linkPrebuild.getReaderService());
				}
				else {
					builder.setAllLinksReaderService(createReaderService(
							linkPrebuild.getBeanType(),
							linkPrebuild.getBeanTypeId(),
							linkPrebuild.getPropertyNames()));
				}
				builder.setLinkBeanType(linkPrebuild.getBeanType());
			}
			else if (linkBeanType != null) {
				final List<String> linkPropertyNames = new LinkedList<String>();
				linkPropertyNames.add(sourceProperties.getForeignKeyPropertyName());
				linkPropertyNames.add(destinationProperties.getForeignKeyPropertyName());
				builder.setLinkBeanType(linkBeanType);
				builder.setLinkCreatorService(createCreatorService(linkBeanType, getLinkBeanTypeId(), linkPropertyNames));
				builder.setLinkDeleterService(createDeleterService(linkBeanType, getLinkBeanTypeId()));
				builder.setAllLinksReaderService(createReaderService(linkBeanType, getLinkBeanTypeId(), linkPropertyNames));
			}

			final BeanEntityPreBuild linkedPrebuild = prebuilds.get(linkedEntityId);
			if (linkedPrebuild != null) {
				final Class<? extends IBean> linkedBeanType = linkedPrebuild.getBeanType();
				final Object linkedBeanTypeId = linkedPrebuild.getBeanTypeId();
				final Collection<String> linkedProperties = linkedPrebuild.getPropertyNames();
				builder.setLinkedBeanAccess(beanServiceFactory.beanAccess(linkedBeanType, linkedBeanTypeId));
				builder.setLinkedDtoFactory(linkedBeanType, linkedProperties);
			}

			final BeanEntityPreBuild linkablePrebuild = prebuilds.get(linkableEntityId);
			if (linkablePrebuild != null) {
				final Class<? extends IBean> linkableBeanType = linkablePrebuild.getBeanType();
				final Object linkableBeanTypeId = linkablePrebuild.getBeanTypeId();
				final Collection<String> linkableProperties = linkablePrebuild.getPropertyNames();
				builder.setLinkableCreatorService(createCreatorService(linkableBeanType, linkableBeanTypeId, linkableProperties));
				builder.setLinkableDeleterService(createDeleterService(linkableBeanType, linkableBeanTypeId));
			}

			return builder;
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		private ICreatorService createCreatorService(
			final Class beanType,
			final Object beanTypeId,
			final Collection<String> properties) {
			return beanServiceFactory.creatorService(beanType, beanTypeId, properties);
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		private IDeleterService createDeleterService(final Class beanType, final Object beanTypeId) {
			return beanServiceFactory.deleterService(beanType, beanTypeId);
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		private IReaderService<Void> createReaderService(
			final Class beanType,
			final Object beanTypeId,
			final Collection<String> properties) {
			return beanServiceFactory.readerService(beanType, beanTypeId, BeanDtoFactory.create(beanType, properties));
		}
	}

}
