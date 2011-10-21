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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.common.api.filter.BooleanOperator;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.filter.IUiArithmeticFilter;
import org.jowidgets.cap.ui.api.filter.IUiArithmeticFilterBuilder;
import org.jowidgets.cap.ui.api.filter.IUiArithmeticPropertyFilter;
import org.jowidgets.cap.ui.api.filter.IUiArithmeticPropertyFilterBuilder;
import org.jowidgets.cap.ui.api.filter.IUiBooleanFilter;
import org.jowidgets.cap.ui.api.filter.IUiBooleanFilterBuilder;
import org.jowidgets.cap.ui.api.filter.IUiCustomFilter;
import org.jowidgets.cap.ui.api.filter.IUiCustomFilterBuilder;
import org.jowidgets.cap.ui.api.filter.IUiFilter;
import org.jowidgets.cap.ui.api.filter.IUiFilterFactory;
import org.jowidgets.cap.ui.api.filter.IUiFilterTools;
import org.jowidgets.util.Assert;

final class UiFilterToolsImpl implements IUiFilterTools {

	@Override
	public IUiFilter addFilter(final IUiFilter sourceFilter, final IUiFilter addedFilter) {
		Assert.paramNotNull(addedFilter, "addedFilter");

		final IUiFilterFactory filterFactory = CapUiToolkit.filterToolkit().filterFactory();

		if (sourceFilter == null) {
			return addedFilter;
		}
		else if (sourceFilter instanceof IUiBooleanFilter) {
			final IUiBooleanFilter booleanSourceFilter = (IUiBooleanFilter) sourceFilter;
			final IUiBooleanFilterBuilder builder = filterFactory.booleanFilterBuilder();
			builder.setOperator(booleanSourceFilter.getOperator());
			builder.setInverted(booleanSourceFilter.isInverted());
			builder.setFilters(booleanSourceFilter.getFilters());
			builder.addFilter(addedFilter);
			return builder.build();
		}
		else {
			final IUiBooleanFilterBuilder builder = filterFactory.booleanFilterBuilder();
			builder.setOperator(BooleanOperator.AND);
			builder.addFilter(sourceFilter);
			builder.addFilter(addedFilter);
			return builder.build();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public IUiFilter removeProperty(final IUiFilter sourceFilter, final String propertyName) {
		Assert.paramNotNull(sourceFilter, "sourceFilter");
		Assert.paramNotNull(propertyName, "propertyName");

		final IUiFilterFactory filterFactory = CapUiToolkit.filterToolkit().filterFactory();

		if (sourceFilter instanceof IUiBooleanFilter) {
			boolean empty = true;
			final IUiBooleanFilter source = (IUiBooleanFilter) sourceFilter;
			final IUiBooleanFilterBuilder builder = filterFactory.booleanFilterBuilder();
			builder.setOperator(source.getOperator());
			builder.setInverted(source.isInverted());
			for (final IUiFilter childFilter : source.getFilters()) {
				final IUiFilter childResultFilter = removeProperty(childFilter, propertyName);
				if (childResultFilter != null) {
					builder.addFilter(childResultFilter);
					empty = false;
				}
			}
			if (empty) {
				return null;
			}
			else {
				return builder.build();
			}
		}
		else if (sourceFilter instanceof IUiArithmeticFilter<?>) {
			final IUiArithmeticFilter<Object> source = (IUiArithmeticFilter<Object>) sourceFilter;
			if (propertyName.equals(source.getPropertyName())) {
				return null;
			}
			else {
				return source;
			}
		}
		else if (sourceFilter instanceof IUiArithmeticPropertyFilter<?>) {
			final IUiArithmeticPropertyFilter<Object> source = (IUiArithmeticPropertyFilter<Object>) sourceFilter;
			if (propertyName.equals(source.getPropertyName())) {
				return null;
			}
			else {
				return source;
			}
		}
		else if (sourceFilter instanceof IUiCustomFilter<?>) {
			final IUiCustomFilter<Object> source = (IUiCustomFilter<Object>) sourceFilter;
			if (propertyName.equals(source.getPropertyName())) {
				return null;
			}
			else {
				return source;
			}
		}
		else {
			throw new IllegalArgumentException("The source filter type '"
				+ sourceFilter.getClass().getName()
				+ "' is not supported");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public IUiFilter invert(final IUiFilter sourceFilter) {
		Assert.paramNotNull(sourceFilter, "sourceFilter");

		final IUiFilterFactory filterFactory = CapUiToolkit.filterToolkit().filterFactory();

		if (sourceFilter instanceof IUiBooleanFilter) {
			final IUiBooleanFilter source = (IUiBooleanFilter) sourceFilter;
			final IUiBooleanFilterBuilder builder = filterFactory.booleanFilterBuilder();
			builder.setOperator(source.getOperator());
			builder.setInverted(!source.isInverted());
			builder.setFilters(source.getFilters());
			return builder.build();
		}
		else if (sourceFilter instanceof IUiArithmeticFilter<?>) {
			final IUiArithmeticFilter<Object> source = (IUiArithmeticFilter<Object>) sourceFilter;
			final IUiArithmeticFilterBuilder<Object> builder = filterFactory.arithmeticFilterBuilder();
			builder.setOperator(source.getOperator());
			builder.setPropertyName(source.getPropertyName());
			builder.setParameters(source.getParameters());
			builder.setConfig(source.getConfig());
			builder.setInverted(!source.isInverted());
			return builder.build();
		}
		else if (sourceFilter instanceof IUiArithmeticPropertyFilter<?>) {
			final IUiArithmeticPropertyFilter<Object> source = (IUiArithmeticPropertyFilter<Object>) sourceFilter;
			final IUiArithmeticPropertyFilterBuilder<Object> builder = filterFactory.arithmeticPropertyFilterBuilder();
			builder.setOperator(source.getOperator());
			builder.setPropertyName(source.getPropertyName());
			builder.setRightHandPropertyNames(source.getRightHandPropertyNames());
			builder.setConfig(source.getConfig());
			builder.setInverted(!source.isInverted());
			return builder.build();
		}
		else if (sourceFilter instanceof IUiCustomFilter<?>) {
			final IUiCustomFilter<Object> source = (IUiCustomFilter<Object>) sourceFilter;
			final IUiCustomFilterBuilder<Object> builder = filterFactory.customFilterBuilder();
			builder.setOperator(source.getOperator());
			builder.setPropertyName(source.getPropertyName());
			builder.setValue(source.getValue());
			builder.setFilterType(source.getFilterType());
			builder.setConfig(source.getConfig());
			builder.setInverted(!source.isInverted());
			return builder.build();
		}
		else {
			throw new IllegalArgumentException("The source filter type '"
				+ sourceFilter.getClass().getName()
				+ "' is not supported");
		}
	}

	@Override
	public boolean isPropertyFiltered(final IUiFilter sourceFilter, final String propertyName) {
		Assert.paramNotNull(sourceFilter, "sourceFilter");
		Assert.paramNotNull(propertyName, "propertyName");
		if (sourceFilter instanceof IUiBooleanFilter) {
			final IUiBooleanFilter booleanSourceFilter = (IUiBooleanFilter) sourceFilter;
			for (final IUiFilter subSourceFilter : booleanSourceFilter.getFilters()) {
				if (isPropertyFiltered(subSourceFilter, propertyName)) {
					return true;
				}
			}
			return false;
		}
		else if (sourceFilter instanceof IUiArithmeticFilter<?>) {
			return propertyName.equals(((IUiArithmeticFilter<?>) sourceFilter).getPropertyName());
		}
		else if (sourceFilter instanceof IUiArithmeticPropertyFilter<?>) {
			return propertyName.equals(((IUiArithmeticPropertyFilter<?>) sourceFilter).getPropertyName());
		}
		else if (sourceFilter instanceof IUiCustomFilter<?>) {
			return propertyName.equals(((IUiCustomFilter<?>) sourceFilter).getPropertyName());
		}
		else {
			throw new IllegalArgumentException("The source filter type '"
				+ sourceFilter.getClass().getName()
				+ "' is not supported");
		}
	}

	@Override
	public String toHumanReadable(final IUiFilter filter, final List<IAttribute<?>> attributes) {
		Assert.paramNotNull(filter, "filter");
		Assert.paramNotNull(attributes, "attributes");

		final Map<String, String> attributeMap = new HashMap<String, String>();
		for (final IAttribute<?> attribute : attributes) {
			attributeMap.put(attribute.getPropertyName(), attribute.getLabel());
		}

		return toHumanReadable(filter, attributeMap).toString();
	}

	private StringBuilder toHumanReadable(final IUiFilter filter, final Map<String, String> attributeMap) {
		if (filter instanceof IUiBooleanFilter) {
			return toHumanReadable((IUiBooleanFilter) filter, attributeMap);
		}
		else if (filter instanceof IUiArithmeticFilter<?>) {
			return toHumanReadable((IUiArithmeticFilter<?>) filter, attributeMap);
		}
		else if (filter instanceof IUiArithmeticPropertyFilter<?>) {
			return toHumanReadable((IUiArithmeticPropertyFilter<?>) filter, attributeMap);
		}
		else if (filter instanceof IUiCustomFilter<?>) {
			return toHumanReadable((IUiCustomFilter<?>) filter, attributeMap);
		}
		else {
			throw new IllegalArgumentException("Unkown UiFilter type '" + filter.getClass().getName() + "'.");
		}
	}

	private StringBuilder toHumanReadable(final IUiBooleanFilter filter, final Map<String, String> attributeMap) {
		final StringBuilder result = new StringBuilder();
		if (filter.getFilters().size() > 0) {
			int effectiveSize = 0;

			if (filter.isInverted()) {
				result.append("not (");
			}
			for (final IUiFilter subFilter : filter.getFilters()) {
				result.append('(');
				result.append(toHumanReadable(subFilter, attributeMap));
				result.append(')');
				effectiveSize = result.length();
				result.append(' ');
				result.append(filter.getOperator().getLabel());
				result.append(' ');
			}
			result.setLength(effectiveSize);
			if (filter.isInverted()) {
				result.append(')');
			}
		}

		return result;
	}

	private StringBuilder toHumanReadable(final IUiArithmeticFilter<?> filter, final Map<String, String> attributeMap) {
		final StringBuilder result = new StringBuilder();
		if (filter.isInverted()) {
			result.append("not (");
		}
		result.append(attributeMap.get(filter.getPropertyName()));
		result.append(' ');
		result.append(filter.getOperator().getLabel());
		result.append(' ');
		result.append('[');
		int effectiveSize = result.length();
		for (final Object o : filter.getParameters()) {
			if (o == null) {
				result.append("<null>");
			}
			else {
				result.append(o.toString());
			}
			effectiveSize = result.length();
			result.append(", ");
		}
		result.setLength(effectiveSize);
		result.append(']');
		if (filter.isInverted()) {
			result.append(")");
		}
		return result;
	}

	private StringBuilder toHumanReadable(final IUiArithmeticPropertyFilter<?> filter, final Map<String, String> attributeMap) {
		final StringBuilder result = new StringBuilder();
		if (filter.isInverted()) {
			result.append("not (");
		}
		result.append(attributeMap.get(filter.getPropertyName()));
		result.append(' ');
		result.append(filter.getOperator().getLabel());
		result.append(' ');
		result.append('[');
		int effectiveSize = result.length();
		for (final String property : filter.getRightHandPropertyNames()) {
			result.append(attributeMap.get(property));
			effectiveSize = result.length();
			result.append(", ");
		}
		result.setLength(effectiveSize);
		result.append(']');
		if (filter.isInverted()) {
			result.append(")");
		}
		return result;
	}

	private StringBuilder toHumanReadable(final IUiCustomFilter<?> filter, final Map<String, String> attributeMap) {
		final StringBuilder result = new StringBuilder();
		if (filter.isInverted()) {
			result.append("not (");
		}
		result.append("custom filter: ");
		result.append(filter.getFilterType());
		result.append(' ');
		result.append(attributeMap.get(filter.getPropertyName()));
		result.append(' ');
		result.append(filter.getOperator().getLabel());
		result.append(' ');
		result.append(filter.getValue());
		if (filter.isInverted()) {
			result.append(")");
		}
		return result;
	}

}
