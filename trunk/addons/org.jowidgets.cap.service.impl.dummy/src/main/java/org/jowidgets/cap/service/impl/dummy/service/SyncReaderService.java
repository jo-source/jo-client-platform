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

package org.jowidgets.cap.service.impl.dummy.service;

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.adapter.ISyncReaderService;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.impl.dummy.datastore.IEntityData;
import org.jowidgets.cap.service.tools.bean.BeanDtoFactoryHelper;

final class SyncReaderService<BEAN_TYPE extends IBean> implements ISyncReaderService<Void> {

	private final IBeanDtoFactory<BEAN_TYPE> beanFactory;
	private final IEntityData<? extends BEAN_TYPE> data;

	SyncReaderService(final IEntityData<? extends BEAN_TYPE> data, final IBeanDtoFactory<BEAN_TYPE> beanFactory) {
		this.beanFactory = beanFactory;
		this.data = data;
	}

	@Override
	public List<IBeanDto> read(
		final List<? extends IBeanKey> parentBeans,
		final IFilter filter,
		final List<? extends ISort> sortedProperties,
		final int firstRow,
		final int maxRows,
		final Void parameter,
		IExecutionCallback executionCallback) {

		executionCallback = CapServiceToolkit.delayedExecutionCallback(executionCallback);

		if (filter == null && (sortedProperties == null || sortedProperties.size() == 0)) {
			return BeanDtoFactoryHelper.createDtos(beanFactory, data.getAllData(firstRow, maxRows), executionCallback);
		}
		else {
			List<IBeanDto> result = BeanDtoFactoryHelper.createDtos(beanFactory, data.getAllData(), executionCallback);

			if (filter != null) {
				result = CapServiceToolkit.beanDtoFilter().filter(result, filter, executionCallback);
			}
			if (sortedProperties != null && sortedProperties.size() > 0) {
				result = CapServiceToolkit.beanDtoSorter().sort(result, sortedProperties, executionCallback);
			}

			if (result.size() >= firstRow) {
				return new LinkedList<IBeanDto>(result.subList(firstRow, Math.min(firstRow + maxRows, result.size())));
			}
			else {
				return new LinkedList<IBeanDto>();
			}
		}

	}

	@Override
	public Integer count(
		final List<? extends IBeanKey> parentBeans,
		final IFilter filter,
		final Void parameter,
		final IExecutionCallback executionCallback) {

		//TODO apply filter

		return Integer.valueOf(data.getAllData().size());
	}

}
