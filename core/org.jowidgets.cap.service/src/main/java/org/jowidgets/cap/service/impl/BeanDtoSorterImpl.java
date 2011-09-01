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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.sort.SortOrder;
import org.jowidgets.cap.service.api.bean.IBeanDtoSorter;

final class BeanDtoSorterImpl implements IBeanDtoSorter {

	@Override
	public List<IBeanDto> sort(final Collection<? extends IBeanDto> beanDtos, final List<? extends ISort> sorting) {
		final List<IBeanDto> result = new LinkedList<IBeanDto>(beanDtos);
		Collections.sort(result, new BeanDtoComparator(sorting));
		return result;
	}

	private class BeanDtoComparator implements Comparator<IBeanDto> {

		private final List<? extends ISort> sorting;

		public BeanDtoComparator(final List<? extends ISort> sorting) {
			this.sorting = sorting;
		}

		@Override
		public int compare(final IBeanDto firstBeanDto, final IBeanDto secondBeanDto) {
			int result = 0;
			for (final ISort sort : sorting) {
				final String propertyName = sort.getPropertyName();
				final SortOrder sortOrder = sort.getSortOrder();
				if (null != sortOrder) {
					if (sortOrder.equals(SortOrder.ASC)) {
						result = compareWithCast(firstBeanDto, secondBeanDto, result, propertyName);
					}
					else if (sortOrder.equals(SortOrder.DESC)) {
						result = -1 * compareWithCast(firstBeanDto, secondBeanDto, result, propertyName);
					}
				}
				if (result != 0) {
					return result;
				}

			}
			return result;
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		private int compareWithCast(
			final IBeanDto firstBeanDto,
			final IBeanDto secondBeanDto,
			int result,
			final String propertyName) {
			final Object firstValue = firstBeanDto.getValue(propertyName);
			final Object secondValue = secondBeanDto.getValue(propertyName);
			if (firstValue != null && secondValue != null) {
				if (firstValue instanceof Comparable<?> && secondValue instanceof Comparable<?>) {
					result = ((Comparable) firstValue).compareTo(secondValue);
				}
				else {
					throw new IllegalArgumentException("The datatype of the property '" + propertyName + "' is not comparable");
				}
			}
			else if (firstValue == null && secondValue == null) {
				result = 0;
			}
			else if (firstValue == null) {
				result = 1;
			}
			else {
				result = -1;
			}
			return result;
		}
	}

}
