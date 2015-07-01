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

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.sort.ISortModelConfig;
import org.jowidgets.cap.ui.api.tabfolder.IBeanTabFolderModel;
import org.jowidgets.cap.ui.api.tabfolder.IBeanTabFolderModelBuilder;
import org.jowidgets.cap.ui.api.tabfolder.IBeanTabFolderModelInterceptor;
import org.jowidgets.util.Assert;

final class BeanTabFolderModelBuilderImpl<BEAN_TYPE> extends
		AbstractBeanModelBuilderImpl<BEAN_TYPE, IBeanTabFolderModelBuilder<BEAN_TYPE>> implements
		IBeanTabFolderModelBuilder<BEAN_TYPE> {

	private final List<IBeanTabFolderModelInterceptor<BEAN_TYPE>> interceptors;

	private boolean clearOnEmptyFilter;
	private Boolean clearOnEmptyParentBeans;
	private ISortModelConfig sortModelConfig;
	private IBeanProxyLabelRenderer<BEAN_TYPE> renderer;

	BeanTabFolderModelBuilderImpl(final Object entityId, final Object beanTypeId, final Class<BEAN_TYPE> beanType) {
		super(entityId, beanTypeId, beanType);

		this.interceptors = new LinkedList<IBeanTabFolderModelInterceptor<BEAN_TYPE>>();
		this.clearOnEmptyFilter = false;

		this.sortModelConfig = new SortModelConfigImpl();
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
	public IBeanTabFolderModelBuilder<BEAN_TYPE> addInterceptor(final IBeanTabFolderModelInterceptor<BEAN_TYPE> interceptor) {
		Assert.paramNotNull(interceptor, "interceptor");
		interceptors.add(interceptor);
		return this;
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> setClearOnEmptyFilter(final boolean clearOnEmptyFilter) {
		this.clearOnEmptyFilter = clearOnEmptyFilter;
		return this;
	}

	@Override
	public IBeanTabFolderModelBuilder<BEAN_TYPE> setClearOnEmptyParentBeans(final boolean clearOnEmptyParentBeans) {
		this.clearOnEmptyParentBeans = Boolean.valueOf(clearOnEmptyParentBeans);
		return this;
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
	public IBeanTabFolderModel<BEAN_TYPE> build() {
		return new BeanTabFolderModelImpl<BEAN_TYPE>(
			getEntityId(),
			getBeanTypeId(),
			getBeanType(),
			getAttributes(),
			renderer,
			getBeanValidators(),
			interceptors,
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
			getListenerDelay(),
			clearOnEmptyFilter,
			getClearOnEmptyParentBeans(),
			getBeanProxyContext(),
			getDataModelContext());
	}

}
