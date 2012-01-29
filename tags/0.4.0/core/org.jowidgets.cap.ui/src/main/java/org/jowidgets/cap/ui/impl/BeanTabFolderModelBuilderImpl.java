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

package org.jowidgets.cap.ui.impl;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.cap.ui.api.sort.ISortModelConfig;
import org.jowidgets.cap.ui.api.tabfolder.IBeanTabFolderModel;
import org.jowidgets.cap.ui.api.tabfolder.IBeanTabFolderModelBuilder;
import org.jowidgets.cap.ui.api.tabfolder.IBeanTabFolderModelInterceptor;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IProvider;

final class BeanTabFolderModelBuilderImpl<BEAN_TYPE> implements IBeanTabFolderModelBuilder<BEAN_TYPE> {

	private final Class<? extends BEAN_TYPE> beanType;
	private final Object entityId;
	private final Set<IBeanValidator<BEAN_TYPE>> beanValidators;
	private final List<IBeanTabFolderModelInterceptor<BEAN_TYPE>> interceptors;

	private IReaderService<? extends Object> readerService;
	private IProvider<? extends Object> readerParameterProvider;
	private IBeanListModel<?> parent;
	private LinkType linkType;

	private ICreatorService creatorService;
	private IRefreshService refreshService;
	private IUpdaterService updaterService;
	private IDeleterService deleterService;

	private ISortModelConfig sortModelConfig;
	private List<String> propertyNames;

	private IBeanProxyLabelRenderer<BEAN_TYPE> renderer;

	@SuppressWarnings({"unchecked", "rawtypes"})
	BeanTabFolderModelBuilderImpl(final Object entityId, final Class<BEAN_TYPE> beanType) {
		Assert.paramNotNull(entityId, "entityId");
		Assert.paramNotNull(beanType, "beanType");

		this.beanValidators = new LinkedHashSet<IBeanValidator<BEAN_TYPE>>();
		this.interceptors = new LinkedList<IBeanTabFolderModelInterceptor<BEAN_TYPE>>();
		this.beanType = beanType;
		this.entityId = entityId;

		final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService != null) {
			final IBeanServicesProvider entityServicesProvider = entityService.getBeanServices(entityId);
			if (entityServicesProvider != null) {
				setEntityServices(entityServicesProvider);
			}
			final IBeanDtoDescriptor beanDtoDescriptor = entityService.getDescriptor(entityId);
			if (beanDtoDescriptor != null) {
				this.propertyNames = new LinkedList<String>();
				for (final IProperty property : beanDtoDescriptor.getProperties()) {
					propertyNames.add(property.getName());
				}
				for (final IBeanValidator<?> beanValidator : beanDtoDescriptor.getValidators()) {
					addBeanValidator((IBeanValidator) beanValidator);
				}
			}
		}

		this.sortModelConfig = new SortModelConfigImpl();
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> setReaderService(final IReaderService<Void> readerService) {
		Assert.paramNotNull(readerService, "readerService");

		this.readerService = readerService;
		this.readerParameterProvider = new IProvider<Object>() {
			@Override
			public Object get() {
				return null;
			}
		};

		return this;
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> setReaderService(final IServiceId<IReaderService<Void>> readerServiceId) {
		Assert.paramNotNull(readerServiceId, "readerServiceId");
		return setReaderService(ServiceProvider.getService(readerServiceId));
	}

	@Override
	public <PARAM_TYPE> IBeanTabFolderModelBuilder<BEAN_TYPE> setReaderService(
		final IReaderService<PARAM_TYPE> readerService,
		final IProvider<PARAM_TYPE> paramProvider) {
		Assert.paramNotNull(readerService, "readerService");
		Assert.paramNotNull(paramProvider, "paramProvider");

		this.readerService = readerService;
		this.readerParameterProvider = paramProvider;
		return this;
	}

	@Override
	public <PARAM_TYPE> IBeanTabFolderModelBuilder<BEAN_TYPE> setReaderService(
		final IServiceId<IReaderService<PARAM_TYPE>> readerServiceId,
		final IProvider<PARAM_TYPE> paramProvider) {
		Assert.paramNotNull(readerServiceId, "readerServiceId");
		return setReaderService(ServiceProvider.getService(readerServiceId), paramProvider);
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> setEntityServices(final IBeanServicesProvider entityServicesProvider) {
		Assert.paramNotNull(entityServicesProvider, "entityServicesProvider");
		final IReaderService<Void> entitityReaderService = entityServicesProvider.readerService();
		if (entitityReaderService != null) {
			setReaderService(entitityReaderService);
		}
		this.creatorService = entityServicesProvider.creatorService();
		this.refreshService = entityServicesProvider.refreshService();
		this.updaterService = entityServicesProvider.updaterService();
		this.deleterService = entityServicesProvider.deleterService();
		return this;
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> setEntityServices(
		final IServiceId<IBeanServicesProvider> entityServicesProviderId) {
		Assert.paramNotNull(entityServicesProviderId, "entityServicesProviderId");
		return setEntityServices(ServiceProvider.getService(entityServicesProviderId));
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> setCreatorService(final ICreatorService creatorService) {
		this.creatorService = creatorService;
		return this;
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> setCreatorService(final IServiceId<ICreatorService> creatorServiceId) {
		Assert.paramNotNull(creatorServiceId, "creatorServiceId");
		return setCreatorService(ServiceProvider.getService(creatorServiceId));
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> setRefreshService(final IRefreshService refreshService) {
		this.refreshService = refreshService;
		return this;
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> setRefreshService(final IServiceId<IRefreshService> refreshServiceId) {
		Assert.paramNotNull(refreshServiceId, "refreshServiceId");
		return setRefreshService(ServiceProvider.getService(refreshServiceId));
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> setUpdaterService(final IUpdaterService updaterService) {
		this.updaterService = updaterService;
		return this;
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> setUpdaterService(final IServiceId<IUpdaterService> updaterServiceId) {
		Assert.paramNotNull(updaterServiceId, "updaterServiceId");
		return setUpdaterService(ServiceProvider.getService(updaterServiceId));
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> setDeleterService(final IDeleterService deleterService) {
		this.deleterService = deleterService;
		return this;
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> setDeleterService(final IServiceId<IDeleterService> deleterServiceId) {
		Assert.paramNotNull(deleterServiceId, "deleterServiceId");
		return setDeleterService(ServiceProvider.getService(deleterServiceId));
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> setParent(final IBeanListModel<?> parent, final LinkType linkType) {
		Assert.paramNotNull(parent, "parent");
		Assert.paramNotNull(linkType, "linkType");

		this.parent = parent;
		this.linkType = linkType;

		return this;
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> addBeanValidator(final IBeanValidator<BEAN_TYPE> beanValidator) {
		Assert.paramNotNull(beanValidator, "beanValidator");
		beanValidators.add(beanValidator);
		return this;
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> setSorting(final ISortModelConfig sorting) {
		Assert.paramNotNull(sorting, "sorting");
		this.sortModelConfig = sorting;
		return this;
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> setLabelRenderer(final IBeanProxyLabelRenderer<BEAN_TYPE> renderer) {
		Assert.paramNotNull(renderer, "renderer");
		this.renderer = renderer;
		return this;
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> setPropertyNames(final Collection<String> propertyNames) {
		Assert.paramNotNull(propertyNames, "propertyNames");
		this.propertyNames = new LinkedList<String>(propertyNames);
		return this;
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> addInterceptor(final IBeanTabFolderModelInterceptor<BEAN_TYPE> interceptor) {
		Assert.paramNotNull(interceptor, "interceptor");
		interceptors.add(interceptor);
		return this;
	}

	@Override
	public IBeanTabFolderModel<BEAN_TYPE> build() {
		return new BeanTabFolderModelImpl<BEAN_TYPE>(
			entityId,
			beanType,
			propertyNames,
			renderer,
			beanValidators,
			interceptors,
			sortModelConfig,
			readerService,
			readerParameterProvider,
			creatorService,
			refreshService,
			updaterService,
			deleterService,
			parent,
			linkType);
	}

}
