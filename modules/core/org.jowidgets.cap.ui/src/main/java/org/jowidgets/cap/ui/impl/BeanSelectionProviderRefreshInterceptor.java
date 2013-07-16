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

import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelection;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.tools.execution.ExecutionInterceptorAdapter;
import org.jowidgets.common.types.IVetoable;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;

final class BeanSelectionProviderRefreshInterceptor<BEAN_TYPE, RESULT_TYPE> extends ExecutionInterceptorAdapter<RESULT_TYPE> {

	private final IBeanSelectionProvider<BEAN_TYPE> selectionProvider;
	private final IEntityService entityService;

	private IBeanSelection<BEAN_TYPE> lastSelection;

	BeanSelectionProviderRefreshInterceptor(final IBeanSelectionProvider<BEAN_TYPE> selectionProvider) {
		Assert.paramNotNull(selectionProvider, "selectionProvider");
		this.selectionProvider = selectionProvider;
		this.entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService == null) {
			throw new IllegalStateException("Entity Service is not available");
		}
	}

	@Override
	public void beforeExecution(final IExecutionContext executionContext, final IVetoable continueExecution) {
		lastSelection = selectionProvider.getBeanSelection();
	}

	@Override
	public void onExecutionVeto(final IExecutionContext executionContext) {
		lastSelection = null;
	}

	@Override
	public void afterExecutionSuccess(final IExecutionContext executionContext, final RESULT_TYPE result) {
		final IRefreshService refreshService = getRefreshService();
		if (refreshService != null) {
			final BeanListRefreshDelegate<BEAN_TYPE> refreshDelegate = new BeanListRefreshDelegate<BEAN_TYPE>(
				getListModel(),
				CapUiToolkit.defaultExceptionConverter(),
				BeanExecutionPolicy.BATCH,
				refreshService);
			refreshDelegate.refresh(lastSelection.getSelection());
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private IBeanListModel<BEAN_TYPE> getListModel() {
		if (selectionProvider instanceof IBeanListModel<?>) {
			return (IBeanListModel<BEAN_TYPE>) selectionProvider;
		}
		else {
			final IBeanTableModelBuilder builder = CapUiToolkit.beanTableModelBuilder(
					lastSelection.getEntityId(),
					lastSelection.getBeanType());
			final IBeanTableModel<BEAN_TYPE> result = builder.build();
			for (final IBeanProxy<BEAN_TYPE> bean : lastSelection.getSelection()) {
				result.addBean(bean);
			}
			return result;
		}
	}

	private IRefreshService getRefreshService() {
		if (lastSelection != null) {
			final Object entityId = lastSelection.getEntityId();
			if (entityId != null) {
				final IBeanServicesProvider beanServices = entityService.getBeanServices(entityId);
				if (beanServices != null) {
					return beanServices.refreshService();
				}
			}
		}
		return null;
	}

}
