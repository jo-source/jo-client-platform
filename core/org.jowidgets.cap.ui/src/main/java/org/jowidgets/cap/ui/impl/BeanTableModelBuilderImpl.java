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

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.ui.api.sort.ISortModelConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.api.table.IReaderParameterProvider;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.IProvider;

@SuppressWarnings("deprecation")
final class BeanTableModelBuilderImpl<BEAN_TYPE> extends
		AbstractBeanModelBuilderImpl<BEAN_TYPE, IBeanTableModelBuilder<BEAN_TYPE>> implements IBeanTableModelBuilder<BEAN_TYPE> {

	private String entityLabelSingular;
	private String entityLabelPlural;

	private boolean autoRowCount;
	private boolean autoSelection;
	private boolean clearOnEmptyFilter;
	private Boolean clearOnEmptyParentBeans;

	private ISortModelConfig sortModelConfig;

	BeanTableModelBuilderImpl(final Object entityId, final Class<BEAN_TYPE> beanType) {
		super(entityId, beanType);
		Assert.paramNotNull(entityId, "entityId");
		Assert.paramNotNull(beanType, "beanType");

		this.autoRowCount = true;
		this.autoSelection = true;
		this.clearOnEmptyFilter = false;

		final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService != null) {
			final IBeanDtoDescriptor beanDtoDescriptor = entityService.getDescriptor(entityId);
			if (beanDtoDescriptor != null) {
				final String labelSingular = beanDtoDescriptor.getLabelSingular();
				if (!EmptyCheck.isEmpty(labelSingular)) {
					this.entityLabelSingular = labelSingular;
				}
				final String labelPlural = beanDtoDescriptor.getLabelPlural();
				if (!EmptyCheck.isEmpty(labelPlural)) {
					this.entityLabelPlural = labelPlural;
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
	public <PARAM_TYPE> IBeanTableModelBuilder<BEAN_TYPE> setReaderService(
		final IReaderService<PARAM_TYPE> readerService,
		final IReaderParameterProvider<PARAM_TYPE> paramProvider) {
		Assert.paramNotNull(readerService, "readerService");
		Assert.paramNotNull(paramProvider, "paramProvider");

		setReaderService(readerService, new IProvider<PARAM_TYPE>() {
			@Override
			public PARAM_TYPE get() {
				return paramProvider.getParameter();
			}
		});
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
			return getParent() != null;
		}
	}

	@Override
	public IBeanTableModel<BEAN_TYPE> build() {
		return new BeanTableModelImpl<BEAN_TYPE>(
			getEntityId(),
			getBeanType(),
			getBeanValidators(),
			getEntityLabelSingular(),
			getEntityLabelPlural(),
			getAttributes(),
			sortModelConfig,
			getReaderService(),
			getReaderParameterProvider(),
			getCreatorService(),
			getRefreshService(),
			getUpdaterService(),
			getDeleterService(),
			getExceptionConverter(),
			getParent(),
			getLinkType(),
			autoRowCount,
			autoSelection,
			clearOnEmptyFilter,
			getClearOnEmptyParentBeans());
	}

}
