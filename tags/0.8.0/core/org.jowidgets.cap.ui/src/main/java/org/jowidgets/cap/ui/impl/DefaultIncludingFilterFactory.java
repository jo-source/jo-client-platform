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

package org.jowidgets.cap.ui.impl;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import org.jowidgets.cap.common.api.bean.Cardinality;
import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.filter.FilterType;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.cap.ui.api.filter.IIncludingFilterFactory;
import org.jowidgets.cap.ui.api.filter.IUiArithmeticFilterBuilder;
import org.jowidgets.cap.ui.api.filter.IUiConfigurableFilter;
import org.jowidgets.cap.ui.api.filter.IUiFilterFactory;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class DefaultIncludingFilterFactory<VALUE_TYPE> implements IIncludingFilterFactory<VALUE_TYPE> {

	private final Class<?> type;
	private final Cardinality cardinality;
	private final String propertyName;
	private final IUiFilterFactory filterFactory;

	DefaultIncludingFilterFactory(final String propertyName, final Class<? extends VALUE_TYPE> type, final Cardinality cardinality) {
		this.propertyName = propertyName;
		this.type = type;
		this.cardinality = cardinality;
		this.filterFactory = CapUiToolkit.filterToolkit().filterFactory();
	}

	@Override
	public IFilterType getFilterType() {
		return FilterType.ARITHMETIC_FILTER;
	}

	@Override
	public IUiConfigurableFilter<?> getIncludingFilter(final VALUE_TYPE attributeValue) {
		if (EmptyCheck.isEmpty(attributeValue)) {
			return filterFactory.arithmeticFilter(propertyName, ArithmeticOperator.EMPTY);
		}
		else if (Collection.class.isAssignableFrom(type)) {
			Assert.paramHasType(attributeValue, Collection.class, "attributeValue");

			final IUiArithmeticFilterBuilder<Object> builder = filterFactory.arithmeticFilterBuilder();
			builder.setPropertyName(propertyName);
			if (Cardinality.GREATER_OR_EQUAL_ZERO == cardinality) {
				builder.setOperator(ArithmeticOperator.CONTAINS_ANY);
			}
			else {
				builder.setOperator(ArithmeticOperator.EQUAL);
			}

			final Collection<?> collection = (Collection<?>) attributeValue;
			for (final Object elementValue : collection) {
				builder.addParameter(elementValue);
			}
			return builder.build();
		}
		else if (attributeValue instanceof Date) {
			final Date date = (Date) attributeValue;

			final Calendar firstOperand = new GregorianCalendar();
			firstOperand.setTime(date);
			firstOperand.set(Calendar.MINUTE, 0);
			firstOperand.set(Calendar.SECOND, 0);
			firstOperand.set(Calendar.MILLISECOND, 0);
			firstOperand.set(Calendar.HOUR_OF_DAY, 0);

			final Calendar secondOperand = new GregorianCalendar();
			secondOperand.setTime(firstOperand.getTime());
			secondOperand.add(Calendar.DAY_OF_MONTH, 1);
			secondOperand.add(Calendar.MILLISECOND, -1);

			return filterFactory.arithmeticFilter(propertyName, ArithmeticOperator.BETWEEN, new Date[] {
					firstOperand.getTime(), secondOperand.getTime()});
		}
		else {
			return filterFactory.arithmeticFilter(propertyName, ArithmeticOperator.EQUAL, attributeValue);
		}

	}
}
