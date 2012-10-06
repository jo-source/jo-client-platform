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

package org.jowidgets.cap.service.tools.reader;

import java.util.Collection;
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
import org.jowidgets.cap.service.tools.bean.BeanDtoFactoryHelper;
import org.jowidgets.util.Assert;

/**
 * An abstract implementation of the reader service that uses in memory sorting and filtering. This can only be used
 * for small data amounts because the whole data must be loaded into memory.
 * 
 * Remark: This implementation was not designed for production use
 */
public abstract class AbstractSimpleReaderService<BEAN_TYPE extends IBean, PARAM_TYPE> implements ISyncReaderService<PARAM_TYPE> {

	private final IBeanDtoFactory<? extends BEAN_TYPE> beanFactory;

	protected AbstractSimpleReaderService(final IBeanDtoFactory<? extends BEAN_TYPE> beanFactory) {
		Assert.paramNotNull(beanFactory, "beanFactory");
		this.beanFactory = beanFactory;
	}

	protected abstract List<? extends BEAN_TYPE> getAllBeans(
		List<? extends IBeanKey> parentBeans,
		PARAM_TYPE parameter,
		final IExecutionCallback executionCallback);

	@Override
	public final List<IBeanDto> read(
		final List<? extends IBeanKey> parentBeans,
		final IFilter filter,
		final List<? extends ISort> sortedProperties,
		final int firstRow,
		final int maxRows,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {

		if (filter == null && (sortedProperties == null || sortedProperties.size() == 0)) {
			final Collection<? extends BEAN_TYPE> beans = getBeans(parentBeans, parameter, firstRow, maxRows, executionCallback);
			return BeanDtoFactoryHelper.createDtos(beanFactory, beans, executionCallback);
		}
		else {
			List<IBeanDto> result = BeanDtoFactoryHelper.createDtos(
					beanFactory,
					getAllBeans(parentBeans, parameter, executionCallback),
					executionCallback);

			if (filter != null) {
				result = CapServiceToolkit.beanDtoCollectionFilter().filter(result, filter, executionCallback);
			}
			if (sortedProperties != null && sortedProperties.size() > 0) {
				result = CapServiceToolkit.beanDtoCollectionSorter().sort(result, sortedProperties, executionCallback);
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
	public final Integer count(
		final List<? extends IBeanKey> parentBeans,
		final IFilter filter,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {

		if (filter == null) {
			return Integer.valueOf(getAllBeans(parentBeans, parameter, executionCallback).size());
		}
		else {
			final List<IBeanDto> result = BeanDtoFactoryHelper.createDtos(
					beanFactory,
					getAllBeans(parentBeans, parameter, executionCallback),
					executionCallback);
			return Integer.valueOf(CapServiceToolkit.beanDtoCollectionFilter().filter(result, filter, executionCallback).size());
		}
	}

	protected List<? extends BEAN_TYPE> getBeans(
		final List<? extends IBeanKey> parentBeans,
		final PARAM_TYPE parameter,
		final int firstRow,
		final int maxRows,
		final IExecutionCallback executionCallback) {
		final List<? extends BEAN_TYPE> result = getAllBeans(parentBeans, parameter, executionCallback);
		if (result.size() >= firstRow) {
			return new LinkedList<BEAN_TYPE>(result.subList(firstRow, Math.min(firstRow + maxRows, result.size())));
		}
		else {
			return new LinkedList<BEAN_TYPE>();
		}
	}

}
