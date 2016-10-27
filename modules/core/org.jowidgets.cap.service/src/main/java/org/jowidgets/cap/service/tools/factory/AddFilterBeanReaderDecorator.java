/*
 * Copyright (c) 2016, grossmann
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

package org.jowidgets.cap.service.tools.factory;

import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.filter.BooleanFilter;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.service.api.bean.IBeanReader;
import org.jowidgets.util.Assert;

final class AddFilterBeanReaderDecorator<BEAN_TYPE, PARAM_TYPE> implements IBeanReader<BEAN_TYPE, PARAM_TYPE> {

	private final IBeanReader<BEAN_TYPE, PARAM_TYPE> original;
	private final IFilter filter;

	AddFilterBeanReaderDecorator(final IBeanReader<BEAN_TYPE, PARAM_TYPE> original, final IFilter filter) {
		Assert.paramNotNull(original, "original");
		this.original = original;
		this.filter = filter;
	}

	@Override
	public List<BEAN_TYPE> read(
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final List<? extends ISort> sorting,
		final int firstRow,
		final int maxRows,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {
		return original.read(
				parentBeanKeys,
				getDecoratedFilter(filter),
				sorting,
				firstRow,
				maxRows,
				parameter,
				executionCallback);
	}

	@Override
	public Integer count(
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {
		return original.count(parentBeanKeys, filter, parameter, executionCallback);
	}

	private IFilter getDecoratedFilter(final IFilter filterToDecorate) {
		if (filterToDecorate == null) {
			return filter;
		}
		else if (filter == null) {
			return filterToDecorate;
		}
		else {
			return BooleanFilter.builder().addFilter(filter).addFilter(filterToDecorate).build();
		}
	}
}
