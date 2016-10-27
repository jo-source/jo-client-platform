/*
 * Copyright (c) 2011, Benjamin Marstaller, Michael Grossmann
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

package org.jowidgets.cap.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.sort.BeanDtoComparator;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.sort.ISortConverterMap;
import org.jowidgets.cap.common.api.sort.SortConverterMap;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanDtoCollectionSorter;

final class BeanDtoCollectionSorterImpl implements IBeanDtoCollectionSorter {

	private final ISortConverterMap sortConverters;

	BeanDtoCollectionSorterImpl() {
		this((ISortConverterMap) null);
	}

	BeanDtoCollectionSorterImpl(final Class<?> beanType) {
		this(beanType != null ? SortConverterMap.create(beanType) : null);
	}

	BeanDtoCollectionSorterImpl(final ISortConverterMap sortConverters) {
		this.sortConverters = sortConverters;
	}

	@Override
	public ArrayList<IBeanDto> sort(
		final Collection<? extends IBeanDto> beanDtos,
		final List<? extends ISort> sorting,
		final IExecutionCallback executionCallback) {
		final ArrayList<IBeanDto> result = new ArrayList<IBeanDto>(beanDtos);
		Collections.sort(result, new BeanDtoComparatorDecorator(sorting, executionCallback));
		return result;
	}

	private class BeanDtoComparatorDecorator implements Comparator<IBeanDto> {

		private final Comparator<IBeanDto> original;
		private final IExecutionCallback executionCallback;

		BeanDtoComparatorDecorator(final List<? extends ISort> sorting, final IExecutionCallback executionCallback) {
			if (sortConverters != null) {
				this.original = BeanDtoComparator.create(sortConverters, sorting);
			}
			else {
				this.original = BeanDtoComparator.create(sorting);
			}
			this.executionCallback = executionCallback;
		}

		@Override
		public int compare(final IBeanDto firstBeanDto, final IBeanDto secondBeanDto) {
			CapServiceToolkit.checkCanceled(executionCallback);
			return original.compare(firstBeanDto, secondBeanDto);
		}

	}

}
