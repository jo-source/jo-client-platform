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

package org.jowidgets.cap.sample1.service.reader;

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

public abstract class AbstractSyncReaderService implements ISyncReaderService<Void> {

	@Override
	public final List<IBeanDto> read(
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final List<? extends ISort> sorting,
		final int firstRow,
		final int maxRows,
		final Void param,
		final IExecutionCallback executionCallback) {

		List<IBeanDto> result = getFilteredResult(parentBeanKeys, filter, executionCallback);

		if (sorting != null && sorting.size() > 0) {
			result = CapServiceToolkit.beanDtoSorter().sort(result, sorting, executionCallback);
		}

		if (result.size() >= firstRow) {
			return new LinkedList<IBeanDto>(result.subList(firstRow, Math.min(firstRow + maxRows, result.size())));
		}
		else {
			return new LinkedList<IBeanDto>();
		}

	}

	@Override
	public final Integer count(
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final Void param,
		final IExecutionCallback executionCallback) {

		return Integer.valueOf(getFilteredResult(parentBeanKeys, filter, executionCallback).size());
	}

	private List<IBeanDto> getFilteredResult(
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final IExecutionCallback executionCallback) {

		final IBeanDtoFactory<?> dtoFactory = getDtoFactory();

		final List<IBeanDto> result = BeanDtoFactoryHelper.createDtos(
				dtoFactory,
				getData(parentBeanKeys, executionCallback),
				executionCallback);

		if (filter != null) {
			return CapServiceToolkit.beanDtoFilter().filter(result, filter, executionCallback);
		}
		else {
			return result;
		}
	}

	abstract IBeanDtoFactory<?> getDtoFactory();

	abstract List<IBean> getData(final List<? extends IBeanKey> parentBeanKeys, final IExecutionCallback executionCallback);

}
