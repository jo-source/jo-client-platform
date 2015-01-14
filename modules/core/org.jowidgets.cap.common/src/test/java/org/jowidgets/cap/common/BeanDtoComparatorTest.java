/*
 * Copyright (c) 2015, grossmann
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

package org.jowidgets.cap.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanDtoBuilder;
import org.jowidgets.cap.common.api.sort.BeanDtoComparator;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.sort.Sort;
import org.jowidgets.cap.common.api.sort.SortOrder;
import org.jowidgets.util.IConverter;
import org.junit.Assert;
import org.junit.Test;

public class BeanDtoComparatorTest {

	private static final String BEAN_TYPE_ID = "beanTypeID";

	private static final String NUMBER_PROPERTY_NAME = "numberProperty";
	private static final String STRING_PROPRTY_NAME = "stringProperty";

	private static final List<ISort> SORT_NUMBER_PROPERTY_ASC = Arrays.asList(Sort.create(NUMBER_PROPERTY_NAME));
	private static final List<ISort> SORT_STRING_PROPERTY_ASC = Arrays.asList(Sort.create(STRING_PROPRTY_NAME));

	private static final List<ISort> SORT_NUMBER_PROPERTY_DESC = Arrays.asList(Sort.create(NUMBER_PROPERTY_NAME, SortOrder.DESC));
	private static final List<ISort> SORT_STRING_PROPERTY_DESC = Arrays.asList(Sort.create(STRING_PROPRTY_NAME, SortOrder.DESC));

	private static final IConverter<String, Long> STRING_SORT_CONVERTER = new StringSortConverter();

	private static IBeanDto createBeanDto(final Long number, final String string) {
		final IBeanDtoBuilder builder = CapCommonToolkit.dtoBuilder(BEAN_TYPE_ID);
		builder.setId(UUID.randomUUID().toString());
		builder.setValue(NUMBER_PROPERTY_NAME, number);
		builder.setValue(STRING_PROPRTY_NAME, string);
		return builder.build();
	}

	@Test
	public void testBeanDtoComparatorSort() {
		final IBeanDto bean1 = createBeanDto(new Long(1), "2");
		final IBeanDto bean2 = createBeanDto(new Long(2), "12");

		final List<IBeanDto> beans = new ArrayList<IBeanDto>(2);
		beans.add(bean2);
		beans.add(bean1);

		Assert.assertSame(bean2, beans.get(0));
		Assert.assertSame(bean1, beans.get(1));

		Collections.sort(beans, BeanDtoComparator.create(SORT_NUMBER_PROPERTY_ASC));

		Assert.assertSame(bean1, beans.get(0));
		Assert.assertSame(bean2, beans.get(1));

		Collections.sort(beans, BeanDtoComparator.create(SORT_STRING_PROPERTY_ASC));

		Assert.assertSame(bean2, beans.get(0));
		Assert.assertSame(bean1, beans.get(1));

		Collections.sort(beans, BeanDtoComparator.create(SORT_STRING_PROPERTY_DESC));

		Assert.assertSame(bean1, beans.get(0));
		Assert.assertSame(bean2, beans.get(1));

		Collections.sort(beans, BeanDtoComparator.create(SORT_NUMBER_PROPERTY_DESC));

		Assert.assertSame(bean2, beans.get(0));
		Assert.assertSame(bean1, beans.get(1));
	}

	@Test
	public void testSortConverter() {
		final IBeanDto bean1 = createBeanDto(new Long(1), "2");
		final IBeanDto bean2 = createBeanDto(new Long(2), "12");

		final List<IBeanDto> beans = new ArrayList<IBeanDto>(2);
		beans.add(bean2);
		beans.add(bean1);

		Assert.assertSame(bean2, beans.get(0));
		Assert.assertSame(bean1, beans.get(1));

		Collections.sort(beans, createSortComparator(SORT_STRING_PROPERTY_ASC, STRING_PROPRTY_NAME, STRING_SORT_CONVERTER));

		Assert.assertSame(bean1, beans.get(0));
		Assert.assertSame(bean2, beans.get(1));

		Collections.sort(beans, createSortComparator(SORT_STRING_PROPERTY_DESC, STRING_PROPRTY_NAME, STRING_SORT_CONVERTER));

		Assert.assertSame(bean2, beans.get(0));
		Assert.assertSame(bean1, beans.get(1));
	}

	private Comparator<IBeanDto> createSortComparator(
		final Collection<ISort> sorting,
		final String propertyName,
		final IConverter<?, ?> converter) {
		return BeanDtoComparator.builder().setSorting(sorting).addPropertyComparator(propertyName, converter).build();
	}

	private static final class StringSortConverter implements IConverter<String, Long> {

		@Override
		public Long convert(final String source) {
			if (source != null) {
				return Long.parseLong(source);
			}
			return null;
		}

	}

}
