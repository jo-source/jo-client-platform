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
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.util.Assert;

final class BeanListRefreshDelegate<BEAN_TYPE> {

	private static final IMessage REFRESH = Messages.getMessage("BeanListRefreshDelegate.Reload");;
	private static final IMessage REFRESH_FAILED = Messages.getMessage("BeanListRefreshDelegate.Reload_failed");

	private final IBeanListModel<BEAN_TYPE> listModel;
	private final IBeanExceptionConverter exceptionConverter;
	private final BeanExecutionPolicy beanExecutionPolicy;
	private final IRefreshService refreshService;

	BeanListRefreshDelegate(
		final IBeanListModel<BEAN_TYPE> listModel,
		final IBeanExceptionConverter exceptionConverter,
		final BeanExecutionPolicy beanExecutionPolicy,
		final IRefreshService refreshService) {

		Assert.paramNotNull(listModel, "listModel");
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");
		Assert.paramNotNull(beanExecutionPolicy, "beanExecutionPolicy");

		this.listModel = listModel;
		this.exceptionConverter = exceptionConverter;
		this.beanExecutionPolicy = beanExecutionPolicy;
		this.refreshService = refreshService;
	}

	void refresh(final Collection<IBeanProxy<BEAN_TYPE>> beans) {
		if (refreshService != null) {
			final BeanListExecutionHelper<BEAN_TYPE> executionHelper = new BeanListExecutionHelper<BEAN_TYPE>(
				REFRESH_FAILED.get(),
				listModel,
				beans,
				beanExecutionPolicy,
				exceptionConverter,
				false,
				true);

			for (final List<IBeanProxy<BEAN_TYPE>> preparedBeans : executionHelper.prepareExecutions(false)) {
				if (preparedBeans.size() > 0) {
					final IExecutionTask executionTask = preparedBeans.get(0).getExecutionTask();
					if (executionTask != null) {
						executionTask.setDescription(REFRESH.get());
						final IBeanKeyFactory beanKeyFactory = CapUiToolkit.beanKeyFactory();
						final List<IBeanKey> beanKeys = beanKeyFactory.createKeys(preparedBeans);
						final IResultCallback<List<IBeanDto>> helperCallback = executionHelper.createResultCallback(preparedBeans);
						refreshService.refresh(helperCallback, beanKeys, executionTask);
					}
				}
			}
		}
	}

}
