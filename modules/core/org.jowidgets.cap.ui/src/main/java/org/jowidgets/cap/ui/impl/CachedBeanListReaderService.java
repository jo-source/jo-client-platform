/*
 * Copyright (c) 2014, MGrossmann
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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.util.Assert;

//CHECKSTYLE:OFF
@SuppressWarnings("unused")
final class CachedBeanListReaderService<PARAM_TYPE, BEAN_TYPE> implements IReaderService<PARAM_TYPE> {

	private final IReaderService<PARAM_TYPE> original;
	private final int pageSize;

	private boolean cacheAvailable;

	CachedBeanListReaderService(final IReaderService<PARAM_TYPE> original, final int pageSize) {
		Assert.paramNotNull(original, "original");
		this.original = original;
		this.pageSize = pageSize;

		this.cacheAvailable = false;
	}

	@Override
	public void read(
		final IResultCallback<List<IBeanDto>> result,
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final List<? extends ISort> sorting,
		final int firstRow,
		final int maxRows,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {

		if (!isReadFromCachePossible(parentBeanKeys, filter, sorting, firstRow, maxRows, parameter)) {
			clearCache(parentBeanKeys);
			final IResultCallback<List<IBeanDto>> decoratedResult = new ReadResultCallback(result, parentBeanKeys, filter);
			original.read(decoratedResult, parentBeanKeys, filter, sorting, firstRow, maxRows, parameter, executionCallback);
		}
		else {
			//TODO read from cache
		}
	}

	@Override
	public void count(
		final IResultCallback<Integer> result,
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {
		// TODO Auto-generated method stub

	}

	void setTransientBeans(final List<? extends IBeanKey> parentBeanKeys, final Set<IBeanProxy<BEAN_TYPE>> transientBeans) {
		cacheAvailable = true;
		//add to cache
	}

	void setModifiedBeans(final List<? extends IBeanKey> parentBeanKeys, final Set<IBeanProxy<BEAN_TYPE>> modifiedBeans) {
		//TODO handle modifications different, e.g. with bean context
		cacheAvailable = true;
		//exchange in cache
	}

	void beansDeleted(final List<? extends IBeanKey> parentBeanKeys, final Set<IBeanProxy<BEAN_TYPE>> deletedBeans) {

	}

	void clearCache(final List<? extends IBeanKey> parentBeanKeys) {
		cacheAvailable = false;
	}

	void clearCache() {
		cacheAvailable = false;
	}

	boolean isReadFromCachePossible(
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final List<? extends ISort> sorting,
		final int firstRow,
		final int maxRows,
		final PARAM_TYPE parameter) {

		if (firstRow > 0 || maxRows > pageSize) {
			return false;
		}

		return false;
	}

	private final class ReadResultCallback implements IResultCallback<List<IBeanDto>> {

		private final IResultCallback<List<IBeanDto>> original;
		private final List<? extends IBeanKey> parentBeanKeys;
		private final IFilter filter;

		private ReadResultCallback(
			final IResultCallback<List<IBeanDto>> original,
			final List<? extends IBeanKey> parentBeanKeys,
			final IFilter filter) {

			this.original = original;
			this.parentBeanKeys = parentBeanKeys;
			this.filter = filter;
		}

		@Override
		public void finished(final List<IBeanDto> result) {
			//TODO put data into cache
			original.finished(result);
		}

		@Override
		public void exception(final Throwable exception) {
			original.exception(exception);
		}

	}

	private static final class ReadCacheEntry {

		private final IBeanKey parent;
		private final Set<IFilter> usedFilters;
		private final Set<IBeanDto> beans;

		private IFilter lastFilter;
		private List<? extends ISort> lastSort;
		private List<IBeanDto> lastResult;

		private ReadCacheEntry(final IBeanKey parent, final Set<? extends IBeanDto> beans) {
			super();
			this.parent = parent;
			this.beans = new LinkedHashSet<IBeanDto>(beans);

			this.usedFilters = new HashSet<IFilter>();

		}

	}

}
