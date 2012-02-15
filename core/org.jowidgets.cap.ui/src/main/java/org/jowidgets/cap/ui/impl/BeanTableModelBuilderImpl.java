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
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.cap.ui.api.sort.ISortModelConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.api.table.IReaderParameterProvider;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class BeanTableModelBuilderImpl<BEAN_TYPE> implements IBeanTableModelBuilder<BEAN_TYPE> {

	private final Class<? extends BEAN_TYPE> beanType;
	private final Object entityId;
	private final Set<IBeanValidator<BEAN_TYPE>> beanValidators;

	private String entityLabelSingular;
	private String entityLabelPlural;

	private IReaderService<? extends Object> readerService;
	private IReaderParameterProvider<? extends Object> readerParameterProvider;
	private IBeanListModel<?> parent;
	private LinkType linkType;
	private boolean autoRowCount;
	private boolean autoSelection;
	private boolean clearOnEmptyFilter;
	private Boolean clearOnEmptyParentBeans;
	private List<IAttribute<Object>> attributes;
	private String[] metaPropertyNames;

	private ICreatorService creatorService;
	private IRefreshService refreshService;
	private IUpdaterService updaterService;
	private IDeleterService deleterService;

	private IBeanExceptionConverter exceptionConverter;

	private ISortModelConfig sortModelConfig;

	@SuppressWarnings({"unchecked", "rawtypes"})
	BeanTableModelBuilderImpl(final Object entityId, final Class<BEAN_TYPE> beanType) {
		Assert.paramNotNull(entityId, "entityId");
		Assert.paramNotNull(beanType, "beanType");

		this.beanValidators = new LinkedHashSet<IBeanValidator<BEAN_TYPE>>();
		this.beanType = beanType;
		this.entityId = entityId;
		this.autoRowCount = true;
		this.autoSelection = true;
		this.clearOnEmptyFilter = false;
		this.exceptionConverter = new DefaultBeanExceptionConverter();

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
				final String labelSingular = beanDtoDescriptor.getLabelSingular();
				if (!EmptyCheck.isEmpty(labelSingular)) {
					this.entityLabelSingular = labelSingular;
				}
				final String labelPlural = beanDtoDescriptor.getLabelPlural();
				if (!EmptyCheck.isEmpty(labelPlural)) {
					this.entityLabelPlural = labelPlural;
				}
				for (final IBeanValidator<?> beanValidator : beanDtoDescriptor.getValidators()) {
					addBeanValidator((IBeanValidator) beanValidator);
				}
			}
		}

		this.sortModelConfig = new SortModelConfigImpl();
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setEntityLabelSingular(final String label) {
		this.entityLabelSingular = label;
		return this;
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setEntityLabelPlural(final String label) {
		this.entityLabelPlural = label;
		return this;
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setReaderService(final IReaderService<Void> readerService) {
		Assert.paramNotNull(readerService, "readerService");

		this.readerService = readerService;
		this.readerParameterProvider = new IReaderParameterProvider<Object>() {
			@Override
			public Object getParameter() {
				return null;
			}
		};

		return this;
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setReaderService(final IServiceId<IReaderService<Void>> readerServiceId) {
		Assert.paramNotNull(readerServiceId, "readerServiceId");
		return setReaderService(ServiceProvider.getService(readerServiceId));
	}

	@Override
	public <PARAM_TYPE> IBeanTableModelBuilder<BEAN_TYPE> setReaderService(
		final IReaderService<PARAM_TYPE> readerService,
		final IReaderParameterProvider<PARAM_TYPE> paramProvider) {
		Assert.paramNotNull(readerService, "readerService");
		Assert.paramNotNull(paramProvider, "paramProvider");

		this.readerService = readerService;
		this.readerParameterProvider = paramProvider;
		return this;
	}

	@Override
	public <PARAM_TYPE> IBeanTableModelBuilder<BEAN_TYPE> setReaderService(
		final IServiceId<IReaderService<PARAM_TYPE>> readerServiceId,
		final IReaderParameterProvider<PARAM_TYPE> paramProvider) {
		Assert.paramNotNull(readerServiceId, "readerServiceId");
		return setReaderService(ServiceProvider.getService(readerServiceId), paramProvider);
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setEntityServices(final IBeanServicesProvider entityServicesProvider) {
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
	public IBeanTableModelBuilder<BEAN_TYPE> setEntityServices(final IServiceId<IBeanServicesProvider> entityServicesProviderId) {
		Assert.paramNotNull(entityServicesProviderId, "entityServicesProviderId");
		return setEntityServices(ServiceProvider.getService(entityServicesProviderId));
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setCreatorService(final ICreatorService creatorService) {
		this.creatorService = creatorService;
		return this;
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setCreatorService(final IServiceId<ICreatorService> creatorServiceId) {
		Assert.paramNotNull(creatorServiceId, "creatorServiceId");
		return setCreatorService(ServiceProvider.getService(creatorServiceId));
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setRefreshService(final IRefreshService refreshService) {
		this.refreshService = refreshService;
		return this;
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setRefreshService(final IServiceId<IRefreshService> refreshServiceId) {
		Assert.paramNotNull(refreshServiceId, "refreshServiceId");
		return setRefreshService(ServiceProvider.getService(refreshServiceId));
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setUpdaterService(final IUpdaterService updaterService) {
		this.updaterService = updaterService;
		return this;
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setUpdaterService(final IServiceId<IUpdaterService> updaterServiceId) {
		Assert.paramNotNull(updaterServiceId, "updaterServiceId");
		return setUpdaterService(ServiceProvider.getService(updaterServiceId));
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setDeleterService(final IDeleterService deleterService) {
		this.deleterService = deleterService;
		return this;
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setDeleterService(final IServiceId<IDeleterService> deleterServiceId) {
		Assert.paramNotNull(deleterServiceId, "deleterServiceId");
		return setDeleterService(ServiceProvider.getService(deleterServiceId));
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setExceptionConverter(final IBeanExceptionConverter exceptionConverter) {
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");
		this.exceptionConverter = exceptionConverter;
		return this;
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setParent(final IBeanListModel<?> parent, final LinkType linkType) {
		Assert.paramNotNull(parent, "parent");
		Assert.paramNotNull(linkType, "linkType");

		this.parent = parent;
		this.linkType = linkType;

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setAttributes(final List<? extends IAttribute<?>> attributes) {
		Assert.paramNotNull(attributes, "attributes");
		this.attributes = (List<IAttribute<Object>>) attributes;
		return this;
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setMetaAttributes(final String... metaPropertyNames) {
		this.metaPropertyNames = metaPropertyNames;
		return this;
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> addBeanValidator(final IBeanValidator<BEAN_TYPE> beanValidator) {
		Assert.paramNotNull(beanValidator, "beanValidator");
		beanValidators.add(beanValidator);
		return this;
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setSorting(final ISortModelConfig sorting) {
		Assert.paramNotNull(sorting, "sorting");
		this.sortModelConfig = sorting;
		return this;
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setAutoRowCount(final boolean autoRowCount) {
		this.autoRowCount = autoRowCount;
		return this;
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setAutoSelection(final boolean autoSelection) {
		this.autoSelection = autoSelection;
		return this;
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setClearOnEmptyFilter(final boolean clearOnEmptyFilter) {
		this.clearOnEmptyFilter = clearOnEmptyFilter;
		return this;
	}

	@Override
	public IBeanTableModelBuilder<BEAN_TYPE> setClearOnEmptyParentBeans(final boolean clearOnEmptyParentBeans) {
		this.clearOnEmptyParentBeans = Boolean.valueOf(clearOnEmptyParentBeans);
		return this;
	}

	private List<IAttribute<Object>> getAttributes() {
		final List<IAttribute<Object>> result = new LinkedList<IAttribute<Object>>(attributes);
		if (metaPropertyNames != null) {
			final IAttributeToolkit attributeToolkit = CapUiToolkit.attributeToolkit();
			for (final String metaPropertyName : metaPropertyNames) {
				result.add(attributeToolkit.createMetaAttribute(metaPropertyName));
			}
		}
		return result;
	}

	private String getEntityLabelSingular() {
		if (EmptyCheck.isEmpty(entityLabelSingular)) {
			entityLabelSingular = Messages.getString("BeanTableModelBuilderImpl.dataset");
		}
		return entityLabelSingular;
	}

	private String getEntityLabelPlural() {
		if (EmptyCheck.isEmpty(entityLabelPlural)) {
			entityLabelPlural = Messages.getString("BeanTableModelBuilderImpl.datasets");
		}
		return entityLabelPlural;
	}

	private boolean getClearOnEmptyParentBeans() {
		if (clearOnEmptyParentBeans != null) {
			return clearOnEmptyParentBeans.booleanValue();
		}
		else {
			return parent != null;
		}
	}

	@Override
	public IBeanTableModel<BEAN_TYPE> build() {
		return new BeanTableModelImpl<BEAN_TYPE>(
			entityId,
			beanType,
			beanValidators,
			getEntityLabelSingular(),
			getEntityLabelPlural(),
			getAttributes(),
			sortModelConfig,
			readerService,
			readerParameterProvider,
			creatorService,
			refreshService,
			updaterService,
			deleterService,
			exceptionConverter,
			parent,
			linkType,
			autoRowCount,
			autoSelection,
			clearOnEmptyFilter,
			getClearOnEmptyParentBeans());
	}

}
