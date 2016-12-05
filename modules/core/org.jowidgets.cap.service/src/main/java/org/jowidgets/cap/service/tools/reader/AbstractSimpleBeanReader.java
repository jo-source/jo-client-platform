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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.filter.BooleanOperator;
import org.jowidgets.cap.common.api.filter.IBooleanFilterBuilder;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.sort.ISortConverterMap;
import org.jowidgets.cap.common.api.sort.SortConverterMap;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.BeanDtoCollectionSorter;
import org.jowidgets.cap.service.api.bean.IBeanDtoCollectionSorter;
import org.jowidgets.cap.service.api.bean.IBeanPropertyAccessor;
import org.jowidgets.cap.service.api.bean.IBeanReader;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

/**
 * An abstract implementation of the bean reader that uses in memory sorting and filtering. This can only be used
 * for small data amounts because the whole data must be loaded into memory.
 * 
 * Remark: This implementation was not designed for production use
 */
public abstract class AbstractSimpleBeanReader<BEAN_TYPE, PARAM_TYPE> implements IBeanReader<BEAN_TYPE, PARAM_TYPE> {

	private final IBeanDtoCollectionSorter collectionSorter;
	private final IBeanPropertyAccessor<BEAN_TYPE> propertyAccessor;
	private final Collection<IFilter> additionalFilters;

	protected AbstractSimpleBeanReader(
		final Class<?> beanType,
		final IBeanPropertyAccessor<? extends BEAN_TYPE> propertyAccessor) {
		this(beanType != null ? SortConverterMap.create(beanType) : null, propertyAccessor, null);
	}

	protected AbstractSimpleBeanReader(final IBeanPropertyAccessor<? extends BEAN_TYPE> propertyAccessor) {
		this(propertyAccessor, null);
	}

	protected AbstractSimpleBeanReader(
		final Class<?> beanType,
		final IBeanPropertyAccessor<? extends BEAN_TYPE> propertyAccessor,
		final Collection<IFilter> additionalFilters) {
		this(beanType != null ? SortConverterMap.create(beanType) : null, propertyAccessor, additionalFilters);
	}

	protected AbstractSimpleBeanReader(
		final IBeanPropertyAccessor<? extends BEAN_TYPE> propertyAccessor,
		final Collection<IFilter> additionalFilters) {
		this((ISortConverterMap) null, propertyAccessor, additionalFilters);
	}

	@SuppressWarnings("unchecked")
	protected AbstractSimpleBeanReader(
		final ISortConverterMap sortConverters,
		final IBeanPropertyAccessor<? extends BEAN_TYPE> propertyAccessor,
		final Collection<IFilter> additionalFilters) {
		Assert.paramNotNull(propertyAccessor, "propertyAccessor");

		this.collectionSorter = getCollectionSorter(sortConverters);
		this.propertyAccessor = (IBeanPropertyAccessor<BEAN_TYPE>) propertyAccessor;
		if (!EmptyCheck.isEmpty(additionalFilters)) {
			this.additionalFilters = new LinkedList<IFilter>(additionalFilters);
		}
		else {
			this.additionalFilters = null;
		}
	}

	private static IBeanDtoCollectionSorter getCollectionSorter(final ISortConverterMap sortConverters) {
		if (sortConverters != null) {
			return BeanDtoCollectionSorter.create(sortConverters);
		}
		else {
			return BeanDtoCollectionSorter.create();
		}
	}

	protected abstract List<BEAN_TYPE> getAllBeans(
		List<? extends IBeanKey> parentBeans,
		PARAM_TYPE parameter,
		IExecutionCallback executionCallback);

	@Override
	public final List<BEAN_TYPE> read(
		final List<? extends IBeanKey> parentBeans,
		final IFilter filter,
		final List<? extends ISort> sortedProperties,
		final int firstRow,
		final int maxRows,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {

		final IFilter decoratedFilter = getDecoratedFilter(filter);

		if (decoratedFilter == null && (sortedProperties == null || sortedProperties.size() == 0)) {
			return getBeans(parentBeans, parameter, firstRow, maxRows, executionCallback);
		}
		else {
			final List<BEAN_TYPE> beans = getBeans(parentBeans, parameter, firstRow, maxRows, executionCallback);
			ArrayList<IBeanDto> result = createBeanDtos(beans, executionCallback);

			if (decoratedFilter != null) {
				result = CapServiceToolkit.beanDtoCollectionFilter().filter(result, decoratedFilter, executionCallback);
			}
			if (sortedProperties != null && sortedProperties.size() > 0) {
				result = collectionSorter.sort(result, sortedProperties, executionCallback);
			}

			if (result.size() >= firstRow) {
				return createBeansSubList(result, firstRow, Math.min(firstRow + maxRows, result.size()), executionCallback);
			}
			else {
				return Collections.emptyList();
			}
		}
	}

	private ArrayList<IBeanDto> createBeanDtos(final List<BEAN_TYPE> beans, final IExecutionCallback executionCallback) {
		final ArrayList<IBeanDto> result = new ArrayList<IBeanDto>(beans.size());
		for (final BEAN_TYPE bean : beans) {
			checkCanceled(executionCallback);
			result.add(new BeanDtoWithBeanReference<BEAN_TYPE>(bean, propertyAccessor));
		}
		return result;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private List<BEAN_TYPE> createBeansSubList(
		final ArrayList<IBeanDto> beanDtos,
		final int fromIndex,
		final int toIndex,
		final IExecutionCallback executionCallback) {
		final List<BEAN_TYPE> result = new ArrayList<BEAN_TYPE>(toIndex - fromIndex);
		for (int i = fromIndex; i < toIndex; i++) {
			checkCanceled(executionCallback);
			result.add((BEAN_TYPE) ((BeanDtoWithBeanReference) beanDtos.get(i)).getBeanReference());
		}
		return result;
	}

	private static void checkCanceled(final IExecutionCallback executionCallback) {
		if (executionCallback != null) {
			CapServiceToolkit.checkCanceled(executionCallback);
		}
	}

	@Override
	public Integer count(
		final List<? extends IBeanKey> parentBeans,
		final IFilter filter,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {

		final IFilter decoratedFilter = getDecoratedFilter(filter);

		if (decoratedFilter == null) {
			return Integer.valueOf(getAllBeans(parentBeans, parameter, executionCallback).size());
		}
		else {
			final List<IBeanDto> result = createBeanDtos(
					getAllBeans(parentBeans, parameter, executionCallback),
					executionCallback);
			return Integer.valueOf(
					CapServiceToolkit.beanDtoCollectionFilter().filter(result, decoratedFilter, executionCallback).size());
		}
	}

	protected List<BEAN_TYPE> getBeans(
		final List<? extends IBeanKey> parentBeans,
		final PARAM_TYPE parameter,
		final int firstRow,
		final int maxRows,
		final IExecutionCallback executionCallback) {
		final List<BEAN_TYPE> result = getAllBeans(parentBeans, parameter, executionCallback);
		if (result.size() >= firstRow) {
			return new LinkedList<BEAN_TYPE>(result.subList(firstRow, Math.min(firstRow + maxRows, result.size())));
		}
		else {
			return new LinkedList<BEAN_TYPE>();
		}
	}

	IFilter getDecoratedFilter(final IFilter filter) {
		if (EmptyCheck.isEmpty(additionalFilters)) {
			return filter;
		}
		else if (additionalFilters.size() == 1 && filter == null) {
			return additionalFilters.iterator().next();
		}
		else {
			final IBooleanFilterBuilder builder = CapCommonToolkit.filterFactory().booleanFilterBuilder();
			builder.setOperator(BooleanOperator.AND);
			for (final IFilter additionalFilter : additionalFilters) {
				builder.addFilter(additionalFilter);
			}
			if (filter != null) {
				builder.addFilter(filter);
			}
			return builder.build();
		}
	}
}
