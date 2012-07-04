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

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.bean.BeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionObservable;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.model.IBeanModelBuilder;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IProvider;

abstract class AbstractBeanModelBuilderImpl<BEAN_TYPE, INSTANCE_TYPE> implements IBeanModelBuilder<BEAN_TYPE, INSTANCE_TYPE> {

	private final Class<? extends BEAN_TYPE> beanType;
	private final Object entityId;
	private final Set<IBeanValidator<BEAN_TYPE>> beanValidators;

	private IReaderService<? extends Object> readerService;
	private IProvider<? extends Object> readerParameterProvider;
	private IBeanSelectionObservable<?> parent;
	private LinkType linkType;
	private Long listenerDelay;
	private List<IAttribute<Object>> attributes;
	private String[] metaPropertyNames;

	private ICreatorService creatorService;
	private IRefreshService refreshService;
	private IUpdaterService updaterService;
	private IDeleterService deleterService;

	private IBeanExceptionConverter exceptionConverter;

	@SuppressWarnings({"unchecked", "rawtypes"})
	AbstractBeanModelBuilderImpl(final Object entityId, final Class<BEAN_TYPE> beanType) {
		Assert.paramNotNull(entityId, "entityId");
		Assert.paramNotNull(beanType, "beanType");

		this.beanValidators = new LinkedHashSet<IBeanValidator<BEAN_TYPE>>();
		this.beanType = beanType;
		this.entityId = entityId;
		this.exceptionConverter = BeanExceptionConverter.get();
		this.metaPropertyNames = new String[] {IBeanProxy.META_PROPERTY_PROGRESS, IBeanProxy.META_PROPERTY_MESSAGES};

		final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService != null) {
			final IBeanServicesProvider entityServicesProvider = entityService.getBeanServices(entityId);
			if (entityServicesProvider != null) {
				setEntityServices(entityServicesProvider);
			}
			final IBeanDtoDescriptor beanDtoDescriptor = entityService.getDescriptor(entityId);
			if (beanDtoDescriptor != null) {
				this.attributes = CapUiToolkit.attributeToolkit().createAttributes(beanDtoDescriptor.getProperties());

				for (final IBeanValidator<?> beanValidator : beanDtoDescriptor.getValidators()) {
					addBeanValidator((IBeanValidator) beanValidator);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE setReaderService(final IReaderService<Void> readerService) {
		Assert.paramNotNull(readerService, "readerService");

		this.readerService = readerService;
		this.readerParameterProvider = new IProvider<Object>() {
			@Override
			public Object get() {
				return null;
			}
		};

		return (INSTANCE_TYPE) this;
	}

	@Override
	public INSTANCE_TYPE setReaderService(final IServiceId<IReaderService<Void>> readerServiceId) {
		Assert.paramNotNull(readerServiceId, "readerServiceId");
		return setReaderService(ServiceProvider.getService(readerServiceId));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <PARAM_TYPE> INSTANCE_TYPE setReaderService(
		final IReaderService<PARAM_TYPE> readerService,
		final IProvider<PARAM_TYPE> paramProvider) {
		Assert.paramNotNull(readerService, "readerService");
		Assert.paramNotNull(paramProvider, "paramProvider");

		this.readerService = readerService;
		this.readerParameterProvider = paramProvider;
		return (INSTANCE_TYPE) this;
	}

	@Override
	public <PARAM_TYPE> INSTANCE_TYPE setReaderService(
		final IServiceId<IReaderService<PARAM_TYPE>> readerServiceId,
		final IProvider<PARAM_TYPE> paramProvider) {
		Assert.paramNotNull(readerServiceId, "readerServiceId");
		return setReaderService(ServiceProvider.getService(readerServiceId), paramProvider);
	}

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE setEntityServices(final IBeanServicesProvider entityServicesProvider) {
		Assert.paramNotNull(entityServicesProvider, "entityServicesProvider");
		final IReaderService<Void> entitityReaderService = entityServicesProvider.readerService();
		if (entitityReaderService != null) {
			setReaderService(entitityReaderService);
		}
		this.creatorService = entityServicesProvider.creatorService();
		this.refreshService = entityServicesProvider.refreshService();
		this.updaterService = entityServicesProvider.updaterService();
		this.deleterService = entityServicesProvider.deleterService();
		return (INSTANCE_TYPE) this;
	}

	@Override
	public INSTANCE_TYPE setEntityServices(final IServiceId<IBeanServicesProvider> entityServicesProviderId) {
		Assert.paramNotNull(entityServicesProviderId, "entityServicesProviderId");
		return setEntityServices(ServiceProvider.getService(entityServicesProviderId));
	}

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE setCreatorService(final ICreatorService creatorService) {
		this.creatorService = creatorService;
		return (INSTANCE_TYPE) this;
	}

	@Override
	public INSTANCE_TYPE setCreatorService(final IServiceId<ICreatorService> creatorServiceId) {
		Assert.paramNotNull(creatorServiceId, "creatorServiceId");
		return setCreatorService(ServiceProvider.getService(creatorServiceId));
	}

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE setRefreshService(final IRefreshService refreshService) {
		this.refreshService = refreshService;
		return (INSTANCE_TYPE) this;
	}

	@Override
	public INSTANCE_TYPE setRefreshService(final IServiceId<IRefreshService> refreshServiceId) {
		Assert.paramNotNull(refreshServiceId, "refreshServiceId");
		return setRefreshService(ServiceProvider.getService(refreshServiceId));
	}

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE setUpdaterService(final IUpdaterService updaterService) {
		this.updaterService = updaterService;
		return (INSTANCE_TYPE) this;
	}

	@Override
	public INSTANCE_TYPE setUpdaterService(final IServiceId<IUpdaterService> updaterServiceId) {
		Assert.paramNotNull(updaterServiceId, "updaterServiceId");
		return setUpdaterService(ServiceProvider.getService(updaterServiceId));
	}

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE setDeleterService(final IDeleterService deleterService) {
		this.deleterService = deleterService;
		return (INSTANCE_TYPE) this;
	}

	@Override
	public INSTANCE_TYPE setDeleterService(final IServiceId<IDeleterService> deleterServiceId) {
		Assert.paramNotNull(deleterServiceId, "deleterServiceId");
		return setDeleterService(ServiceProvider.getService(deleterServiceId));
	}

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE setExceptionConverter(final IBeanExceptionConverter exceptionConverter) {
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");
		this.exceptionConverter = exceptionConverter;
		return (INSTANCE_TYPE) this;
	}

	@Override
	public INSTANCE_TYPE setParent(final IBeanSelectionProvider<?> parent, final LinkType linkType) {
		return setParentImpl(parent, linkType, null);
	}

	@Override
	public INSTANCE_TYPE setParent(final IBeanSelectionProvider<?> parent, final LinkType linkType, final long listenerDelay) {
		return setParentImpl(parent, linkType, Long.valueOf(listenerDelay));
	}

	@SuppressWarnings("unchecked")
	private INSTANCE_TYPE setParentImpl(final IBeanSelectionProvider<?> parent, final LinkType linkType, final Long listenerDelay) {
		Assert.paramNotNull(parent, "parent");
		Assert.paramNotNull(linkType, "linkType");

		this.parent = parent;
		this.linkType = linkType;
		this.listenerDelay = listenerDelay;

		return (INSTANCE_TYPE) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE setAttributes(final List<? extends IAttribute<?>> attributes) {
		Assert.paramNotNull(attributes, "attributes");
		this.attributes = (List<IAttribute<Object>>) attributes;
		return (INSTANCE_TYPE) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE setMetaAttributes(final String... metaPropertyNames) {
		this.metaPropertyNames = metaPropertyNames;
		return (INSTANCE_TYPE) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE addBeanValidator(final IBeanValidator<BEAN_TYPE> beanValidator) {
		Assert.paramNotNull(beanValidator, "beanValidator");
		beanValidators.add(beanValidator);
		return (INSTANCE_TYPE) this;
	}

	protected List<IAttribute<Object>> getAttributes() {
		final List<IAttribute<Object>> result = new LinkedList<IAttribute<Object>>(attributes);
		if (metaPropertyNames != null) {
			final IAttributeToolkit attributeToolkit = CapUiToolkit.attributeToolkit();
			for (final String metaPropertyName : metaPropertyNames) {
				result.add(attributeToolkit.createMetaAttribute(metaPropertyName));
			}
		}
		return result;
	}

	protected Class<? extends BEAN_TYPE> getBeanType() {
		return beanType;
	}

	protected Object getEntityId() {
		return entityId;
	}

	protected Set<IBeanValidator<BEAN_TYPE>> getBeanValidators() {
		return beanValidators;
	}

	protected IReaderService<? extends Object> getReaderService() {
		return readerService;
	}

	protected IProvider<? extends Object> getReaderParameterProvider() {
		return readerParameterProvider;
	}

	@SuppressWarnings("unchecked")
	protected IBeanSelectionProvider<Object> getParent() {
		return (IBeanSelectionProvider<Object>) parent;
	}

	protected LinkType getLinkType() {
		return linkType;
	}

	protected Long getListenerDelay() {
		return listenerDelay;
	}

	protected ICreatorService getCreatorService() {
		return creatorService;
	}

	protected IRefreshService getRefreshService() {
		return refreshService;
	}

	protected IUpdaterService getUpdaterService() {
		return updaterService;
	}

	protected IDeleterService getDeleterService() {
		return deleterService;
	}

	protected IBeanExceptionConverter getExceptionConverter() {
		return exceptionConverter;
	}

}
