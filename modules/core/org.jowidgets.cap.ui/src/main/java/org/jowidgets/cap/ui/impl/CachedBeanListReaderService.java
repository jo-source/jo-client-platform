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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.exception.ServiceCanceledException;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.filter.IBeanDtoFilter;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.sort.BeanDtoComparator;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.tools.filter.FilterUtil;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.NullCompatibleEquivalence;
import org.jowidgets.util.maybe.IMaybe;

final class CachedBeanListReaderService<PARAM_TYPE, BEAN_TYPE> implements IReaderService<PARAM_TYPE> {

	private static final ILogger LOGGER = LoggerProvider.get(CachedBeanListReaderService.class);

	private static final IBeanKey EMPTY_PARENT_BEAN_KEY = new EmptyParentBeanKey();

	private final IReaderService<PARAM_TYPE> original;
	private final int pageSize;
	private final Map<IBeanKey, ReadCacheEntry> readCache;
	private final boolean useCache;

	CachedBeanListReaderService(final IReaderService<PARAM_TYPE> original, final int pageSize) {
		this(original, pageSize, false);
	}

	CachedBeanListReaderService(final IReaderService<PARAM_TYPE> original, final int pageSize, final boolean useCache) {
		Assert.paramNotNull(original, "original");
		this.original = original;
		this.pageSize = pageSize;
		this.useCache = useCache;
		this.readCache = new HashMap<IBeanKey, CachedBeanListReaderService.ReadCacheEntry>();
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

		if (!useCache) {
			original.read(result, parentBeanKeys, filter, sorting, firstRow, maxRows, parameter, executionCallback);
		}
		else if (!isReadFromCachePossible(parentBeanKeys, filter, firstRow, maxRows, parameter)) {
			clearCache(parentBeanKeys);
			final IResultCallback<List<IBeanDto>> decoratedResult = new ReadResultCallback(
				result,
				parentBeanKeys,
				filter,
				sorting,
				firstRow,
				maxRows);
			LOGGER.debug("Read from service");
			original.read(decoratedResult, parentBeanKeys, filter, sorting, firstRow, maxRows, parameter, executionCallback);
		}
		else {
			final ReadCacheEntry cacheEntry = readCache.get(getParentBeanKey(parentBeanKeys));
			if (cacheEntry != null) {
				try {
					LOGGER.debug("Read from cache");
					result.finished(cacheEntry.getResult(filter, sorting, firstRow, maxRows, executionCallback));
				}
				catch (final Exception e) {
					result.exception(e);
				}
			}
			else {
				final List<IBeanDto> emptyList = Collections.emptyList();
				result.finished(emptyList);
			}
		}
	}

	@Override
	public void count(
		final IResultCallback<Integer> result,
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {

		if (!useCache) {
			original.count(result, parentBeanKeys, filter, parameter, executionCallback);
		}
		else if (!isReadFromCachePossible(parentBeanKeys, filter, 0, 1, parameter)) {
			original.count(result, parentBeanKeys, filter, parameter, executionCallback);
		}
		else {
			final ReadCacheEntry cacheEntry = readCache.get(getParentBeanKey(parentBeanKeys));
			if (cacheEntry != null) {
				try {
					result.finished(cacheEntry.getResult(filter, null, 0, Integer.MAX_VALUE, executionCallback).size());
				}
				catch (final Exception e) {
					result.exception(e);
				}
			}
			else {
				result.finished(null);
			}
		}

	}

	void addBean(final List<? extends IBeanKey> parentBeanKeys, final IBeanProxy<BEAN_TYPE> bean) {
		if (!useCache) {
			return;
		}
		addBeans(parentBeanKeys, Collections.singleton(bean));
	}

	void addBeans(final List<? extends IBeanKey> parentBeanKeys, final Set<IBeanProxy<BEAN_TYPE>> beans) {
		if (!useCache) {
			return;
		}
		final IBeanKey parentBeanKey = getParentBeanKey(parentBeanKeys);
		ReadCacheEntry readCacheEntry = readCache.get(parentBeanKey);
		if (readCacheEntry == null) {
			readCacheEntry = new ReadCacheEntry(beans);
			readCache.put(parentBeanKey, readCacheEntry);
		}
		else {
			readCacheEntry.addBeans(beans);
		}
	}

	void removeBean(final List<? extends IBeanKey> parentBeanKeys, final IBeanProxy<BEAN_TYPE> bean) {
		if (!useCache) {
			return;
		}
		removeBeans(parentBeanKeys, Collections.singleton(bean));
	}

	void removeBeans(final List<? extends IBeanKey> parentBeanKeys, final Iterable<? extends IBeanProxy<BEAN_TYPE>> bean) {
		if (!useCache) {
			return;
		}
		final ReadCacheEntry readCacheEntry = readCache.get(getParentBeanKey(parentBeanKeys));
		if (readCacheEntry != null) {
			readCacheEntry.removeBeans(bean);
		}
	}

	void clearCache(final List<? extends IBeanKey> parentBeanKeys) {
		if (!useCache) {
			return;
		}
		readCache.remove(getParentBeanKey(parentBeanKeys));
	}

	void clearCache() {
		if (!useCache) {
			return;
		}
		readCache.clear();
	}

	boolean isReadFromCachePossible(final IFilter filter, final PARAM_TYPE parameter) {
		if (!useCache || readCache.isEmpty()) {
			return false;
		}
		else {
			for (final ReadCacheEntry chacheEntry : readCache.values()) {
				if (!isReadFromCachePossible(filter, chacheEntry, parameter)) {
					return false;
				}
			}
			return true;
		}
	}

	boolean isReadFromCachePossible(
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final int firstRow,
		final int maxRows,
		final PARAM_TYPE parameter) {

		final ReadCacheEntry cacheEntry = readCache.get(getParentBeanKey(parentBeanKeys));
		if (firstRow > 0) {
			return false;
		}
		else {
			return isReadFromCachePossible(filter, cacheEntry, parameter);
		}
	}

	boolean isReadFromCachePossible(final IFilter filter, final ReadCacheEntry cacheEntry, final PARAM_TYPE parameter) {

		if (cacheEntry == null || cacheEntry.getBeansSize() > pageSize) {
			return false;
		}
		else {
			//because the relation hasLessOrEqualResults is transitive, you can show with help of mathematical induction 
			//that the new filter has less or equal results if this is true for at least one last filter if you assume
			//that all used filters have less or equal results.
			//Because the implementation of the method may return "Nothing" the test will be made for all used filters, because
			//you can not assume that the result Nothing implies that the result is greater
			for (final IFilter lastFilter : cacheEntry.getUsedFilters()) {
				final IMaybe<Boolean> lessOrEqualResults = FilterUtil.hasLessOrEqualResults(lastFilter, filter);
				if (lessOrEqualResults.isSomething() && lessOrEqualResults.getValue().booleanValue()) {
					return true;
				}
			}

			return false;
		}
	}

	private IBeanKey getParentBeanKey(final List<? extends IBeanKey> parentBeanKeys) {
		if (EmptyCheck.isEmpty(parentBeanKeys)) {
			return EMPTY_PARENT_BEAN_KEY;
		}
		else {
			return parentBeanKeys.iterator().next();
		}
	}

	private final class ReadResultCallback implements IResultCallback<List<IBeanDto>> {

		private final IResultCallback<List<IBeanDto>> original;
		private final IBeanKey parentBeanKey;
		private final IFilter filter;
		private final List<? extends ISort> sort;
		private final int firstRow;
		private final int maxRows;

		private ReadResultCallback(
			final IResultCallback<List<IBeanDto>> original,
			final List<? extends IBeanKey> parentBeanKeys,
			final IFilter filter,
			final List<? extends ISort> sort,
			final int firstRow,
			final int maxRows) {

			this.original = original;
			this.parentBeanKey = getParentBeanKey(parentBeanKeys);
			this.filter = filter;
			this.sort = sort;
			this.firstRow = firstRow;
			this.maxRows = maxRows;
		}

		@Override
		public void finished(final List<IBeanDto> result) {
			final ReadCacheEntry cacheEntry = new ReadCacheEntry(result, filter, sort, firstRow, maxRows);
			readCache.put(parentBeanKey, cacheEntry);
			original.finished(result);
		}

		@Override
		public void exception(final Throwable exception) {
			readCache.remove(parentBeanKey);
			original.exception(exception);
		}

	}

	private static final class ReadCacheEntry {

		private final Set<IFilter> usedFilters;
		private final Set<IBeanDto> beans;

		private IFilter lastFilter;
		private List<? extends ISort> lastSort;
		private List<IBeanDto> lastResult;
		private int lastFirstRow;
		private int lastMaxRows;

		private ReadCacheEntry(final Collection<? extends IBeanDto> beans) {
			this(beans, null, null, -1, -1);
		}

		private ReadCacheEntry(
			final Collection<? extends IBeanDto> beans,
			final IFilter filter,
			final List<? extends ISort> sort,
			final int firstRow,
			final int maxRows) {

			this.beans = new LinkedHashSet<IBeanDto>(beans);
			this.lastResult = new LinkedList<IBeanDto>(beans);
			this.lastFilter = filter;
			this.lastSort = sort != null ? new LinkedList<ISort>(sort) : null;
			this.lastFirstRow = firstRow;
			this.lastMaxRows = maxRows;

			this.usedFilters = new LinkedHashSet<IFilter>();
			usedFilters.add(filter);
		}

		private List<IBeanDto> getResult(
			final IFilter filter,
			final List<? extends ISort> sort,
			final int firstRow,
			final int maxRows,
			final IExecutionCallback executionCallback) {

			if (lastResult != null
				&& lastFirstRow == firstRow
				&& lastMaxRows == maxRows
				&& NullCompatibleEquivalence.equals(lastFilter, filter)
				&& NullCompatibleEquivalence.equals(lastSort, sort)) {
				LOGGER.debug("Read from last result");
				return lastResult;
			}
			else {
				if (filter == null && EmptyCheck.isEmpty(sort)) {
					LOGGER.debug("Read from all");
					lastResult = new LinkedList<IBeanDto>(beans);
				}
				else {
					LOGGER.debug("Sort and filter");
					List<IBeanDto> result = new LinkedList<IBeanDto>(beans);

					if (!FilterUtil.isEmpty(filter)) {
						result = filter(result, filter, executionCallback);
					}

					if (!EmptyCheck.isEmpty(sort)) {
						Collections.sort(result, new BeanDtoComparatorDecorator(sort, executionCallback));
					}

					if (result.size() >= firstRow) {
						lastResult = new LinkedList<IBeanDto>(
							result.subList(firstRow, Math.min(firstRow + maxRows, result.size())));
					}
					else {
						lastResult = new LinkedList<IBeanDto>();
					}
				}
				lastFilter = filter;

				lastSort = new LinkedList<ISort>(sort);
				lastFirstRow = firstRow;
				lastMaxRows = maxRows;
				usedFilters.add(filter);
				return lastResult;
			}
		}

		private List<IBeanDto> filter(
			final List<IBeanDto> source,
			final IFilter filter,
			final IExecutionCallback executionCallback) {
			final List<IBeanDto> result = new LinkedList<IBeanDto>();
			final IBeanDtoFilter dtoFilter = CapCommonToolkit.beanDtoFilter();
			for (final IBeanDto sourceBean : source) {
				if (executionCallback.isCanceled()) {
					throw new ServiceCanceledException();
				}
				if (dtoFilter.accept(sourceBean, filter)) {
					result.add(sourceBean);
				}
			}
			return result;
		}

		private void addBeans(final Collection<? extends IBeanDto> beans) {
			clearLastResult();
			this.beans.addAll(beans);
		}

		private void removeBeans(final Iterable<? extends IBeanDto> beans) {
			clearLastResult();
			if (beans instanceof Collection<?>) {
				this.beans.removeAll((Collection<?>) beans);
			}
			else {
				for (final IBeanDto bean : beans) {
					this.beans.remove(bean);
				}
			}
		}

		private Set<IFilter> getUsedFilters() {
			return usedFilters;
		}

		private int getBeansSize() {
			return beans.size();
		}

		private void clearLastResult() {
			this.lastFilter = null;
			this.lastSort = null;
			this.lastResult = null;
			this.lastFirstRow = -1;
			this.lastMaxRows = -1;
		}

	}

	private static final class EmptyParentBeanKey implements IBeanKey {

		@Override
		public Object getId() {
			return this;
		}

		@Override
		public long getVersion() {
			return 0;
		}

	}

	private static class BeanDtoComparatorDecorator implements Comparator<IBeanDto> {

		private final Comparator<IBeanDto> original;
		private final IExecutionCallback executionCallback;

		BeanDtoComparatorDecorator(final List<? extends ISort> sorting, final IExecutionCallback executionCallback) {
			this.original = BeanDtoComparator.create(sorting);
			this.executionCallback = executionCallback;
		}

		@Override
		public int compare(final IBeanDto firstBeanDto, final IBeanDto secondBeanDto) {
			if (executionCallback.isCanceled()) {
				throw new ServiceCanceledException();
			}
			return original.compare(firstBeanDto, secondBeanDto);
		}

	}

}
