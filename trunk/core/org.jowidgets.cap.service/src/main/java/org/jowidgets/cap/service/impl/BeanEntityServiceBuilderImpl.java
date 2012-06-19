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

import java.io.Serializable;
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
import org.jowidgets.cap.service.api.entity.IBeanEntityBluePrint;
import org.jowidgets.cap.service.api.entity.IBeanEntityLinkBluePrint;
import org.jowidgets.cap.service.api.entity.IBeanEntityServiceBuilder;
import org.jowidgets.cap.service.api.entity.IBeanServicesProviderBuilder;
import org.jowidgets.cap.service.api.factory.IBeanServiceFactory;
import org.jowidgets.cap.service.api.link.ILinkServicesBuilder;
import org.jowidgets.cap.service.api.link.LinkServicesBuilder;
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
			return setCreatorService(CapServiceToolkit.adapterFactoryProvider().creator().createAdapter(creatorService));
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
			return setDeleterService(CapServiceToolkit.adapterFactoryProvider().deleter().createAdapter(deleterService));
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
			final IBeanDtoDescriptor dtoDescriptor,
			final Collection<String> propertyNames,
			final IMaybe<IReaderService<Void>> readerService,
			final IMaybe<ICreatorService> creatorService,
			final IMaybe<IRefreshService> refreshService,
			final IMaybe<IUpdaterService> updaterService,
			final IMaybe<IDeleterService> deleterService) {

			this.entityId = entityId;
			this.beanType = beanType;
			this.propertyNames = propertyNames;
			this.dtoDescriptor = dtoDescriptor;
			this.readerService = getReaderService(readerService);
			this.creatorService = getCreatorService(creatorService);
			this.refreshService = getRefreshService(refreshService);
			this.updaterService = getUpdaterService(updaterService);
			this.deleterService = getDeleterService(deleterService);
			this.beanServicesProvider = createBeanServicesProvider(propertyNames);
		}

		private IReaderService<Void> getReaderService(final IMaybe<IReaderService<Void>> readerService) {
			if (readerService != null) {
				if (readerService.isNothing()) {
					return null;
				}
				else {
					return readerService.getValue();
				}
			}
			else {
				return beanServiceFactory.readerService(beanType, BeanDtoFactory.create(beanType, propertyNames));
			}
		}

		private ICreatorService getCreatorService(final IMaybe<ICreatorService> creatorService) {
			if (creatorService != null) {
				if (creatorService.isNothing()) {
					return null;
				}
				else {
					return creatorService.getValue();
				}
			}
			else {
				return beanServiceFactory.creatorService(
						beanType,
						BeanDtoFactory.create(beanType, propertyNames),
						BeanInitializer.create(beanType, propertyNames));
			}
		}

		private IRefreshService getRefreshService(final IMaybe<IRefreshService> refreshService) {
			if (refreshService != null) {
				if (refreshService.isNothing()) {
					return null;
				}
				else {
					return refreshService.getValue();
				}
			}
			else {
				return CapServiceToolkit.refreshServiceBuilder(beanServiceFactory.beanAccess(beanType)).build();
			}
		}

		private IUpdaterService getUpdaterService(final IMaybe<IUpdaterService> updaterService) {
			if (updaterService != null) {
				if (updaterService.isNothing()) {
					return null;
				}
				else {
					return updaterService.getValue();
				}
			}
			else {
				return CapServiceToolkit.updaterServiceBuilder(beanServiceFactory.beanAccess(beanType)).build();
			}
		}

		private IDeleterService getDeleterService(final IMaybe<IDeleterService> deleterService) {
			if (deleterService != null) {
				if (deleterService.isNothing()) {
					return null;
				}
				else {
					return deleterService.getValue();
				}
			}
			else {
				return beanServiceFactory.deleterService(beanType, true, true);
			}
		}

		Object getEntityId() {
			return entityId;
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

		private IBeanServicesProvider createBeanServicesProvider(final Collection<String> propertyNames) {

			final IBeanServicesProviderBuilder builder = beanServiceFactory.beanServicesBuilder(
					serviceRegistry,
					entityId,
					beanType,
					BeanDtoFactory.create(beanType, propertyNames),
					BeanInitializer.create(beanType, propertyNames),
					BeanModifier.create(beanType, propertyNames));

			builder.setCreatorService(creatorService);
			builder.setReaderService(readerService);
			builder.setRefreshService(refreshService);
			builder.setUpdaterService(updaterService);
			builder.setDeleterService(deleterService);

			return builder.build();
		}

	}

	private final class BeanEntityLinkBluePrintImpl implements IBeanEntityLinkBluePrint {

		private final BeanEntityBluePrintImpl linkSource;

		@SuppressWarnings("rawtypes")
		private ILinkServicesBuilder linkServicesBuilder;

		private Object linkEntityId;
		private Class<? extends IBean> linkBeanType;
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

		IEntityLinkDescriptor build(final Map<Object, BeanEntityPreBuild> prebuilds) {
			if (sourceProperties == null || linkEntityId == null || linkedEntityId == null) {
				throw new IllegalStateException("Missiing mandatory parameters: sourceProperties='"
					+ sourceProperties
					+ "', destinationProperties = '"
					+ destinationProperties
					+ "', linkEntityId = '"
					+ linkEntityId
					+ "', linkableEntityId = '"
					+ linkableEntityId
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
			else {
				builder.setLinkCreatorService(createCreatorService(prebuilds));
			}
			if (deleterService != null) {
				if (deleterService.isSomething()) {
					builder.setLinkDeleterService(deleterService.getValue());
				}
			}
			else {
				builder.setLinkDeleterService(createDeleterService(prebuilds));
			}
			return builder.build();
		}

		private IServiceId<ILinkCreatorService> createCreatorService(final Map<Object, BeanEntityPreBuild> prebuilds) {
			final ILinkCreatorService service = getLinkServicesBuilder(prebuilds).tryBuildCreatorService();
			if (service != null) {
				final Id id = new Id(
					entityServiceId,
					linkSource.getEntityId(),
					linkEntityId,
					linkedEntityId,
					linkableEntityId,
					ILinkCreatorService.class.getName());
				final ServiceId<ILinkCreatorService> result = new ServiceId<ILinkCreatorService>(id, ILinkCreatorService.class);
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
				final Id id = new Id(
					entityServiceId,
					linkSource.getEntityId(),
					linkEntityId,
					linkedEntityId,
					linkableEntityId,
					ILinkDeleterService.class.getName());
				final ServiceId<ILinkDeleterService> result = new ServiceId<ILinkDeleterService>(id, ILinkDeleterService.class);
				serviceRegistry.addService(result, service);
				return result;
			}
			else {
				return null;
			}
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
							linkPrebuild.getPropertyNames()));
				}
				if (linkPrebuild.getDeleterService() != null) {
					builder.setLinkDeleterService(linkPrebuild.getDeleterService());
				}
				else {
					builder.setLinkDeleterService(createDeleterService(linkPrebuild.getBeanType()));
				}
				if (linkPrebuild.getReaderService() != null) {
					builder.setAllLinksReaderService(linkPrebuild.getReaderService());
				}
				else {
					builder.setAllLinksReaderService(createReaderService(
							linkPrebuild.getBeanType(),
							linkPrebuild.getPropertyNames()));
				}
				builder.setLinkBeanType(linkPrebuild.getBeanType());
			}
			else if (linkBeanType != null) {
				final List<String> linkPropertyNames = new LinkedList<String>();
				linkPropertyNames.add(sourceProperties.getForeignKeyPropertyName());
				linkPropertyNames.add(destinationProperties.getForeignKeyPropertyName());
				builder.setLinkBeanType(linkBeanType);
				builder.setLinkCreatorService(createCreatorService(linkBeanType, linkPropertyNames));
				builder.setLinkDeleterService(createDeleterService(linkBeanType));
				builder.setAllLinksReaderService(createReaderService(linkBeanType, linkPropertyNames));
			}

			final BeanEntityPreBuild linkedPrebuild = prebuilds.get(linkedEntityId);
			if (linkedPrebuild != null) {
				final Class<? extends IBean> linkedBeanType = linkedPrebuild.getBeanType();
				final Collection<String> linkedProperties = linkedPrebuild.getPropertyNames();
				builder.setLinkedBeanAccess(beanServiceFactory.beanAccess(linkedBeanType));
				builder.setLinkedDtoFactory(linkedBeanType, linkedProperties);
			}

			final BeanEntityPreBuild linkablePrebuild = prebuilds.get(linkableEntityId);
			if (linkablePrebuild != null) {
				final Class<? extends IBean> linkableBeanType = linkablePrebuild.getBeanType();
				final Collection<String> linkableProperties = linkablePrebuild.getPropertyNames();
				builder.setLinkableCreatorService(createCreatorService(linkableBeanType, linkableProperties));
				builder.setLinkableDeleterService(createDeleterService(linkableBeanType));
			}

			return builder;
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		private ICreatorService createCreatorService(final Class beanType, final Collection<String> properties) {
			return beanServiceFactory.creatorService(
					beanType,
					BeanDtoFactory.create(beanType, properties),
					BeanInitializer.create(beanType, properties));
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		private IDeleterService createDeleterService(final Class beanType) {
			return beanServiceFactory.deleterService(beanType, true, true);
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		private IReaderService<Void> createReaderService(final Class beanType, final Collection<String> properties) {
			return beanServiceFactory.readerService(beanType, BeanDtoFactory.create(beanType, properties));
		}
	}

	private static final class Id implements Serializable {

		private static final long serialVersionUID = -1013030060315128693L;

		private final IServiceId<IEntityService> entityServiceId;
		private final Object sourceEntityId;
		private final Object linkEntityId;
		private final Object linkedEntityId;
		private final Object linkableEntityId;
		private final String service;

		private Id(
			final IServiceId<IEntityService> entityServiceId,
			final Object sourceEntityId,
			final Object linkEntityId,
			final Object linkedEntityId,
			final Object linkableEntityId,
			final String service) {

			this.entityServiceId = entityServiceId;
			this.sourceEntityId = sourceEntityId;
			this.linkEntityId = linkEntityId;
			this.linkedEntityId = linkedEntityId;
			this.linkableEntityId = linkableEntityId;
			this.service = service;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((entityServiceId == null) ? 0 : entityServiceId.hashCode());
			result = prime * result + ((linkEntityId == null) ? 0 : linkEntityId.hashCode());
			result = prime * result + ((linkableEntityId == null) ? 0 : linkableEntityId.hashCode());
			result = prime * result + ((linkedEntityId == null) ? 0 : linkedEntityId.hashCode());
			result = prime * result + ((service == null) ? 0 : service.hashCode());
			result = prime * result + ((sourceEntityId == null) ? 0 : sourceEntityId.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Id)) {
				return false;
			}
			final Id other = (Id) obj;

			if (entityServiceId == null) {
				if (other.entityServiceId != null) {
					return false;
				}
			}
			else if (!entityServiceId.equals(other.entityServiceId)) {
				return false;
			}
			if (linkEntityId == null) {
				if (other.linkEntityId != null) {
					return false;
				}
			}
			else if (!linkEntityId.equals(other.linkEntityId)) {
				return false;
			}
			if (linkableEntityId == null) {
				if (other.linkableEntityId != null) {
					return false;
				}
			}
			else if (!linkableEntityId.equals(other.linkableEntityId)) {
				return false;
			}
			if (linkedEntityId == null) {
				if (other.linkedEntityId != null) {
					return false;
				}
			}
			else if (!linkedEntityId.equals(other.linkedEntityId)) {
				return false;
			}
			if (service == null) {
				if (other.service != null) {
					return false;
				}
			}
			else if (!service.equals(other.service)) {
				return false;
			}
			if (sourceEntityId == null) {
				if (other.sourceEntityId != null) {
					return false;
				}
			}
			else if (!sourceEntityId.equals(other.sourceEntityId)) {
				return false;
			}
			return true;
		}

	}
}
