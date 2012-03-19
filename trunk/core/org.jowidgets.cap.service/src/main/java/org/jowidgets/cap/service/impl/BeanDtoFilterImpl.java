/*
 * Copyright (c) 2011, Michael Grossmann, Nikolaus Moll
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
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.common.api.filter.BooleanOperator;
import org.jowidgets.cap.common.api.filter.IArithmeticFilter;
import org.jowidgets.cap.common.api.filter.IArithmeticPropertyFilter;
import org.jowidgets.cap.common.api.filter.IBooleanFilter;
import org.jowidgets.cap.common.api.filter.ICustomFilter;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.BeanDtoCustomFilterPluginRegistry;
import org.jowidgets.cap.service.api.bean.IBeanDtoCustomFilterPlugin;
import org.jowidgets.cap.service.api.bean.IBeanDtoFilter;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.NullCompatibleEquivalence;

final class BeanDtoFilterImpl implements IBeanDtoFilter {

	@Override
	public List<IBeanDto> filter(
		final Collection<? extends IBeanDto> beanDtos,
		final IFilter filter,
		final IExecutionCallback executionCallback) {

		if (filter instanceof IBooleanFilter) {
			return booleanFilter(beanDtos, (IBooleanFilter) filter, executionCallback);
		}
		else if (filter instanceof IArithmeticFilter) {
			return arithmeticFilter(beanDtos, (IArithmeticFilter) filter, executionCallback);
		}
		else if (filter instanceof IArithmeticFilter) {
			return arithmeticPropertyFilter(beanDtos, (IArithmeticPropertyFilter) filter, executionCallback);
		}
		else if (filter instanceof ICustomFilter) {
			return customFilter(beanDtos, (ICustomFilter) filter, executionCallback);
		}
		else {
			throw new IllegalArgumentException("Unkown filter type '" + filter.getClass().getName() + "'.");
		}
	}

	private List<IBeanDto> booleanFilter(
		final Collection<? extends IBeanDto> beanDtos,
		final IBooleanFilter filter,
		final IExecutionCallback executionCallback) {
		final LinkedList<IBeanDto> result = new LinkedList<IBeanDto>();
		for (final IBeanDto beanDto : beanDtos) {
			CapServiceToolkit.checkCanceled(executionCallback);
			if (acceptBoolean(beanDto, filter)) {
				result.add(beanDto);
			}
		}
		return result;
	}

	private List<IBeanDto> arithmeticFilter(
		final Collection<? extends IBeanDto> beanDtos,
		final IArithmeticFilter filter,
		final IExecutionCallback executionCallback) {
		final LinkedList<IBeanDto> result = new LinkedList<IBeanDto>();
		final String propertyName = filter.getPropertyName();
		if (ArithmeticOperator.EMPTY.equals(filter.getOperator())) {
			for (final IBeanDto beanDto : beanDtos) {
				CapServiceToolkit.checkCanceled(executionCallback);
				if (EmptyCheck.isEmpty(beanDto.getValue(propertyName))) {
					result.add(beanDto);
				}
			}
		}
		else {
			for (final IBeanDto beanDto : beanDtos) {
				CapServiceToolkit.checkCanceled(executionCallback);
				if (acceptArithmetic(beanDto, filter)) {
					result.add(beanDto);
				}
			}
		}
		return result;
	}

	private List<IBeanDto> arithmeticPropertyFilter(
		final Collection<? extends IBeanDto> beanDtos,
		final IArithmeticPropertyFilter filter,
		final IExecutionCallback executionCallback) {
		final LinkedList<IBeanDto> result = new LinkedList<IBeanDto>();
		final String propertyName = filter.getPropertyName();
		if (ArithmeticOperator.EMPTY.equals(filter.getOperator())) {
			for (final IBeanDto beanDto : beanDtos) {
				CapServiceToolkit.checkCanceled(executionCallback);
				if (EmptyCheck.isEmpty(beanDto.getValue(propertyName))) {
					result.add(beanDto);
				}
			}
		}
		else {
			for (final IBeanDto beanDto : beanDtos) {
				CapServiceToolkit.checkCanceled(executionCallback);
				if (acceptArithmeticProperty(beanDto, filter)) {
					result.add(beanDto);
				}
			}
		}
		return result;
	}

	private List<IBeanDto> customFilter(
		final Collection<? extends IBeanDto> beanDtos,
		final ICustomFilter filter,
		final IExecutionCallback executionCallback) {
		final LinkedList<IBeanDto> result = new LinkedList<IBeanDto>();
		for (final IBeanDto beanDto : beanDtos) {
			CapServiceToolkit.checkCanceled(executionCallback);
			if (acceptCustom(beanDto, filter)) {
				result.add(beanDto);
			}
		}
		return result;
	}

	private boolean acceptBoolean(final IBeanDto beanDto, final IBooleanFilter filter) {
		final List<IFilter> filters = filter.getFilters();
		if (BooleanOperator.AND.equals(filter.getOperator())) {
			for (final IFilter currentFilter : filters) {
				if (!accept(beanDto, currentFilter)) {
					return invert(false, filter.isInverted());
				}
			}
			return invert(true, filter.isInverted());
		}
		else if (BooleanOperator.OR.equals(filter.getOperator())) {
			for (final IFilter currentFilter : filters) {
				if (accept(beanDto, currentFilter)) {
					return invert(true, filter.isInverted());
				}
			}
			return invert(false, filter.isInverted());
		}
		else {
			throw new IllegalArgumentException("Unknown boolean operator '" + filter.getOperator().getClass().getName() + "'.");
		}
	}

	private boolean accept(final IBeanDto beanDto, final IFilter filter) {
		if (filter instanceof IBooleanFilter) {
			return acceptBoolean(beanDto, (IBooleanFilter) filter);
		}
		else if (filter instanceof IArithmeticFilter) {
			return acceptArithmetic(beanDto, (IArithmeticFilter) filter);
		}
		else if (filter instanceof IArithmeticPropertyFilter) {
			return acceptArithmeticProperty(beanDto, (IArithmeticPropertyFilter) filter);
		}
		else if (filter instanceof ICustomFilter) {
			return acceptCustom(beanDto, (ICustomFilter) filter);
		}
		else {
			throw new IllegalArgumentException("Unkown filter type '" + filter.getClass().getName() + "'.");
		}
	}

	private boolean acceptCustom(final IBeanDto beanDto, final ICustomFilter filter) {
		final IBeanDtoCustomFilterPlugin filterPlugin = BeanDtoCustomFilterPluginRegistry.getPlugin(filter.getFilterType());
		// plug in has to negate result itself if filter is inverted
		return filterPlugin.accept(beanDto, filter);
	}

	private boolean acceptArithmetic(final IBeanDto beanDto, final IArithmeticFilter filter) {
		final Object value = beanDto.getValue(filter.getPropertyName());
		if (value instanceof Collection<?>) {
			return acceptArithmetic((Collection<?>) value, filter); // Collection or Set
		}
		else {
			return acceptArithmetic(value, filter); // concrete value or null
		}
	}

	private boolean acceptArithmetic(final Object value, final IArithmeticFilter filter) {
		final boolean accept;
		switch (filter.getOperator()) {
			case EMPTY:
				accept = EmptyCheck.isEmpty(value);
				break;

			case EQUAL:
				accept = isEqual(getParameter(filter, 0), value);
				break;

			case LESS:
				accept = isLess(getParameter(filter, 0), value);
				break;

			case LESS_EQUAL:
				accept = isEqual(getParameter(filter, 0), value) || isLess(getParameter(filter, 0), value);
				break;

			case GREATER:
				accept = isGreater(getParameter(filter, 0), value);
				break;

			case GREATER_EQUAL:
				accept = isEqual(getParameter(filter, 0), value) || isGreater(getParameter(filter, 0), value);
				break;

			case BETWEEN:
				accept = (isEqual(getParameter(filter, 0), value) || isGreater(getParameter(filter, 0), value))
					&& (isEqual(getParameter(filter, 1), value) || isLess(getParameter(filter, 1), value));
				break;

			case CONTAINS_ANY:
				accept = containsAny(getParameters(filter), value);
				break;

			case CONTAINS_ALL:
				throw new IllegalArgumentException("Arithmetic operator "
					+ filter.getOperator()
					+ " cannot be used with a non-collection value.");

			default:
				throw new IllegalArgumentException("Unknown arithmetic operator '" + filter.getOperator() + ".'");
		}

		return invert(accept, filter.isInverted());
	}

	private boolean acceptArithmetic(final Collection<?> collection, final IArithmeticFilter filter) {
		final boolean accept;
		switch (filter.getOperator()) {
			case EMPTY:
				accept = EmptyCheck.isEmpty(collection);
				break;

			case EQUAL:
				accept = isEqual(getParameter(filter, 0), collection);
				break;

			case LESS:
				accept = isLess(getParameter(filter, 0), collection);
				break;

			case LESS_EQUAL:
				accept = isEqual(getParameter(filter, 0), collection) || isLess(getParameter(filter, 0), collection);
				break;

			case GREATER:
				accept = isGreater(getParameter(filter, 0), collection);
				break;

			case GREATER_EQUAL:
				accept = isEqual(getParameter(filter, 0), collection) || isGreater(getParameter(filter, 0), collection);
				break;

			case BETWEEN:
				accept = (isEqual(getParameter(filter, 0), collection) || isGreater(getParameter(filter, 0), collection))
					&& (isEqual(getParameter(filter, 1), collection) || isLess(getParameter(filter, 1), collection));
				break;

			case CONTAINS_ANY:
				accept = containsAny(getParameters(filter), collection);
				break;

			case CONTAINS_ALL:
				accept = containsAll(getParameters(filter), collection);
				break;

			default:
				throw new IllegalArgumentException("Unknown arithmetic operator '" + filter.getOperator() + ".'");
		}

		return invert(accept, filter.isInverted());
	}

	private boolean acceptArithmeticProperty(final IBeanDto beanDto, final IArithmeticPropertyFilter filter) {
		final Object value = beanDto.getValue(filter.getPropertyName());
		if (value instanceof Collection<?>) {
			return acceptArithmeticProperty((Collection<?>) value, beanDto, filter); // Collection or Set
		}
		else {
			return acceptArithmeticProperty(value, beanDto, filter); // concrete value or null
		}
	}

	private boolean acceptArithmeticProperty(final Object value, final IBeanDto beanDto, final IArithmeticPropertyFilter filter) {
		final boolean accept;
		switch (filter.getOperator()) {
			case EMPTY:
				accept = EmptyCheck.isEmpty(value);
				break;

			case EQUAL:
				accept = isEqual(getParameter(filter, beanDto, 0), value);
				break;

			case LESS:
				accept = isLess(getParameter(filter, beanDto, 0), value);
				break;

			case LESS_EQUAL:
				accept = isEqual(getParameter(filter, beanDto, 0), value) || isLess(getParameter(filter, beanDto, 0), value);
				break;

			case GREATER:
				accept = isGreater(getParameter(filter, beanDto, 0), value);
				break;

			case GREATER_EQUAL:
				accept = isEqual(getParameter(filter, beanDto, 0), value) || isGreater(getParameter(filter, beanDto, 0), value);
				break;

			case BETWEEN:
				accept = (isEqual(getParameter(filter, beanDto, 0), value) || isGreater(getParameter(filter, beanDto, 0), value))
					&& (isEqual(getParameter(filter, beanDto, 1), value) || isLess(getParameter(filter, beanDto, 1), value));
				break;

			case CONTAINS_ANY:
				accept = containsAny(getParameters(filter, beanDto), value);
				break;

			case CONTAINS_ALL:
				throw new IllegalArgumentException("Arithmetic operator "
					+ filter.getOperator()
					+ " cannot be used with a non-collection value.");

			default:
				throw new IllegalArgumentException("Unknown arithmetic operator '" + filter.getOperator() + ".'");
		}

		return invert(accept, filter.isInverted());
	}

	private boolean acceptArithmeticProperty(
		final Collection<?> collection,
		final IBeanDto beanDto,
		final IArithmeticPropertyFilter filter) {
		final boolean accept;
		switch (filter.getOperator()) {
			case EMPTY:
				accept = EmptyCheck.isEmpty(collection);
				break;

			case EQUAL:
				accept = isEqual(getParameter(filter, beanDto, 0), collection);
				break;

			case LESS:
				accept = isLess(getParameter(filter, beanDto, 0), collection);
				break;

			case LESS_EQUAL:
				accept = isEqual(getParameter(filter, beanDto, 0), collection)
					|| isLess(getParameter(filter, beanDto, 0), collection);
				break;

			case GREATER:
				accept = isGreater(getParameter(filter, beanDto, 0), collection);
				break;

			case GREATER_EQUAL:
				accept = isEqual(getParameter(filter, beanDto, 0), collection)
					|| isGreater(getParameter(filter, beanDto, 0), collection);
				break;

			case BETWEEN:
				accept = (isEqual(getParameter(filter, beanDto, 0), collection) || isGreater(
						getParameter(filter, beanDto, 0),
						collection))
					&& (isEqual(getParameter(filter, beanDto, 1), collection) || isLess(
							getParameter(filter, beanDto, 1),
							collection));
				break;

			case CONTAINS_ANY:
				accept = containsAny(getParameters(filter, beanDto), collection);
				break;

			case CONTAINS_ALL:
				throw new IllegalArgumentException("Arithmetic operator "
					+ filter.getOperator()
					+ " cannot be used with a non-collection value.");

			default:
				throw new IllegalArgumentException("Unknown arithmetic operator '" + filter.getOperator() + ".'");
		}

		return invert(accept, filter.isInverted());
	}

	private boolean invert(final boolean value, final boolean invert) {
		if (invert) {
			return !value;
		}
		else {
			return value;
		}
	}

	private boolean isEqual(final Object object, final Object value) {
		if (object instanceof Collection<?>) {
			return isEqualToList((Collection<?>) object, value);
		}
		else {
			return isEqualToObject(object, value);
		}
	}

	private boolean isEqual(final Object object, final Collection<?> value) {
		if (object instanceof Collection<?>) {
			return isEqualToList((Collection<?>) object, value);
		}
		else {
			return isEqualToObject(object, value);
		}
	}

	private boolean isEqualToObject(final Object object, final Object value) {
		if (object instanceof String && value instanceof String) {
			final String regex = getRegex((String) object);
			final String stringValue = ((String) value).toLowerCase();
			return stringValue.matches(regex);
		}
		else {
			return NullCompatibleEquivalence.equals(object, value);
		}
	}

	private boolean isEqualToObject(final Object object, final Collection<?> values) {
		for (final Object value : values) {
			if (isEqual(object, value)) {
				return true;
			}
		}

		return false;
	}

	private boolean isEqualToList(final Collection<?> list, final Object value) {
		for (final Object object : list) {
			if (isEqualToObject(object, value)) {
				return true;
			}
		}
		return false;
	}

	// any from values is equal to any from list
	private boolean isEqualToList(final Collection<?> list, final Collection<?> values) {
		for (final Object value : values) {
			for (final Object object : list) {
				if (isEqualToObject(object, value)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean isLess(final Object object, final Object value) {
		if (object instanceof Collection<?>) {
			return isLessToList((Collection<?>) object, value);
		}
		else {
			return isLessToObject(object, value);
		}
	}

	private boolean isLess(final Object object, final Collection<?> values) {
		if (object instanceof Collection<?>) {
			return isLessToList((Collection<?>) object, values);
		}
		else {
			return isLessToObject(object, values);
		}
	}

	@SuppressWarnings("unchecked")
	private boolean isLessToObject(final Object object, final Object value) {
		if (value != null) {
			final Comparable<Object> cvalue = (Comparable<Object>) value;
			return cvalue.compareTo(object) < 0;
		}
		else {
			return false;
		}
	}

	private boolean isLessToObject(final Object object, final Collection<?> values) {
		for (final Object value : values) {
			if (isLessToObject(object, value)) {
				return true;
			}
		}

		return false;
	}

	private boolean isLessToList(final Collection<?> list, final Object value) {
		for (final Object object : list) {
			if (isLessToObject(object, value)) {
				return true;
			}
		}
		return false;
	}

	private boolean isLessToList(final Collection<?> list, final Collection<?> values) {
		for (final Object value : values) {
			for (final Object object : list) {
				if (isLessToObject(object, value)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean isGreater(final Object object, final Object value) {
		if (object instanceof Collection<?>) {
			return isGreaterToList((Collection<?>) object, value);
		}
		else {
			return isGreaterToObject(object, value);
		}
	}

	private boolean isGreater(final Object object, final Collection<?> values) {
		if (object instanceof Collection<?>) {
			return isGreaterToList((Collection<?>) object, values);
		}
		else {
			return isGreaterToObject(object, values);
		}
	}

	@SuppressWarnings("unchecked")
	private boolean isGreaterToObject(final Object object, final Object value) {
		if (value != null) {
			final Comparable<Object> cvalue = (Comparable<Object>) value;
			return cvalue.compareTo(object) > 0;
		}
		else {
			return false;
		}
	}

	private boolean isGreaterToObject(final Object object, final Collection<?> values) {
		for (final Object value : values) {
			if (isGreaterToObject(object, value)) {
				return true;
			}
		}

		return false;
	}

	private boolean isGreaterToList(final Collection<?> list, final Object value) {
		for (final Object object : list) {
			if (isGreaterToObject(object, value)) {
				return true;
			}
		}
		return false;
	}

	private boolean isGreaterToList(final Collection<?> list, final Collection<?> values) {
		for (final Object value : values) {
			for (final Object object : list) {
				if (isGreaterToObject(object, value)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean containsAny(final Object[] parameters, final Object value) {
		for (final Object object : parameters) {
			if (isEqual(object, value)) {
				return true;
			}
		}
		return false;
	}

	private boolean containsAny(final Object[] parameters, final Collection<?> values) {
		for (final Object value : values) {
			if (containsAny(parameters, value)) {
				return true;
			}
		}
		return false;
	}

	private boolean containsAll(final Object[] parameters, final Collection<?> values) {
		for (final Object parameter : parameters) {
			boolean hit = false;
			for (final Object value : values) {
				if (isEqual(parameter, value)) {
					hit = true;
					break;
				}
			}
			if (!hit) {
				return false;
			}
		}
		return true;
	}

	// This method is probably called too often
	private String getRegex(final String search) {
		final StringBuilder regex = new StringBuilder(search.length());
		for (final char c : search.toLowerCase().toCharArray()) {
			switch (c) {
			// TODO NM improve, escape more
				case '\\':
					regex.append("\\\\");
					break;

				case '[':
				case ']':
				case '(':
				case ')':
				case '.':
				case '+':
				case '^':
				case '$':
					regex.append('\\');
					regex.append(c);
					break;

				// wild cards
				case '%':
				case '*':
					regex.append(".*");
					break;

				case '_':
					regex.append('.');
					break;

				default:
					regex.append(c);
			}
		}
		return regex.toString();
	}

	private Object getParameter(final IArithmeticFilter filter, final int index) {
		return filter.getParameters()[index];
	}

	private Object[] getParameters(final IArithmeticFilter filter) {
		return filter.getParameters();
	}

	private Object getParameter(final IArithmeticPropertyFilter filter, final IBeanDto beanDto, final int index) {
		final String propertyName = filter.getRightHandPropertyNames()[index];
		return beanDto.getValue(propertyName);
	}

	private Object[] getParameters(final IArithmeticPropertyFilter filter, final IBeanDto beanDto) {
		final Object[] result = new Object[filter.getRightHandPropertyNames().length];
		int index = 0;
		for (final String propertyName : filter.getRightHandPropertyNames()) {
			result[index] = beanDto.getValue(propertyName);
			index++;
		}
		return result;
	}

}
