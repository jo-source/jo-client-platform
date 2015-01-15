/*
 * Copyright (c) 2014, Michael
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

package org.jowidgets.cap.common.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.sort.IBeanDtoComparatorBuilder;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.sort.ISortConverterMap;
import org.jowidgets.cap.common.api.sort.SortConverterMap;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IConverter;

final class BeanDtoComparatorBuilderImpl implements IBeanDtoComparatorBuilder {

	private final Map<String, Comparator<?>> comparators;

	private Collection<? extends ISort> sorting;

	BeanDtoComparatorBuilderImpl() {
		this.comparators = new HashMap<String, Comparator<?>>();
	}

	@Override
	public IBeanDtoComparatorBuilder setSorting(final Collection<? extends ISort> sorting) {
		Assert.paramNotNull(sorting, "sorting");
		this.sorting = sorting;
		return this;
	}

	@Override
	public IBeanDtoComparatorBuilder setPropertyComparator(final String propertyName, final Comparator<?> comparator) {
		Assert.paramNotNull(propertyName, "propertyName");
		Assert.paramNotNull(comparator, "comparator");
		comparators.put(propertyName, comparator);
		return this;
	}

	@Override
	public IBeanDtoComparatorBuilder setPropertyComparator(final String propertyName, final IConverter<?, ?> converter) {
		Assert.paramNotNull(propertyName, "propertyName");
		Assert.paramNotNull(converter, "converter");
		return setPropertyComparator(propertyName, new ConverterBasedComparator(converter, propertyName));
	}

	@Override
	public IBeanDtoComparatorBuilder setPropertyComparators(final ISortConverterMap sortConverterMap) {
		Assert.paramNotNull(sortConverterMap, "sortConverterMap");
		for (final String propertyName : sortConverterMap.getProperties()) {
			setPropertyComparator(propertyName, sortConverterMap.getConverter(propertyName));
		}
		return this;
	}

	@Override
	public IBeanDtoComparatorBuilder setPropertyComparators(final Class<?> beanType) {
		Assert.paramNotNull(beanType, "beanType");
		setPropertyComparators(SortConverterMap.create(beanType));
		return this;
	}

	@Override
	public Comparator<IBeanDto> build() {
		Assert.paramNotNull(sorting, "sorting");
		return new BeanDtoComparatorImpl(sorting, comparators);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static final class ConverterBasedComparator implements Comparator<Object> {

		private final IConverter converter;
		private final String propertyName;

		private ConverterBasedComparator(final IConverter converter, final String propertyName) {
			this.converter = converter;
			this.propertyName = propertyName;
		}

		@Override
		public int compare(final Object object1, final Object object2) {
			if (object1 == null && object2 == null) {
				return 0;
			}
			else if (object1 == null) {// only one of them is null
				return -1;
			}
			else if (object2 == null) {// only one of them is null
				return 1;
			}
			else {
				final Object converted1 = converter.convert(object1);
				final Object converted2 = converter.convert(object2);
				if (converted1 == null && converted2 == null) {
					return 0;
				}
				else if (converted1 == null) {// only one of them is null
					return -1;
				}
				else if (converted2 == null) {// only one of them is null
					return 1;
				}
				else if (converted1 instanceof Comparable<?> && converted2 instanceof Comparable<?>) {
					return ((Comparable) converted1).compareTo(converted2);
				}
				else {
					throw new IllegalArgumentException("The converted data of the property '"
						+ propertyName
						+ "' with the type '"
						+ converted1.getClass()
						+ "' is not comparable");
				}
			}
		}
	}
}
