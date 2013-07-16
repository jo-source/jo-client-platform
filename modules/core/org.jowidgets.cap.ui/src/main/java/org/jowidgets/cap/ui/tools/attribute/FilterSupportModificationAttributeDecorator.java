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

package org.jowidgets.cap.ui.tools.attribute;

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.filter.IOperator;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.filter.IFilterPanelProvider;
import org.jowidgets.cap.ui.api.filter.IFilterSupport;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.cap.ui.api.filter.IIncludingFilterFactory;
import org.jowidgets.cap.ui.api.filter.IOperatorProvider;
import org.jowidgets.cap.ui.api.filter.IUiConfigurableFilter;
import org.jowidgets.cap.ui.tools.filter.FilterPanelProviderWrapper;
import org.jowidgets.cap.ui.tools.filter.FilterSupportWrapper;
import org.jowidgets.cap.ui.tools.filter.IncludingFilterFactoryWrapper;
import org.jowidgets.cap.ui.tools.filter.OperatorProviderWrapper;
import org.jowidgets.util.Assert;

public final class FilterSupportModificationAttributeDecorator {

	private FilterSupportModificationAttributeDecorator() {}

	public interface IFilterSupportModificator {

		boolean acceptFilter(IFilterType filterType);

		boolean acceptOperator(IFilterType filterType, IOperator operator);

		boolean isOperatorInvertible(IFilterType filterType, IOperator operator);
	}

	public static IAttribute<Object> decorateAttribute(final IAttribute<?> attribute, final IFilterSupportModificator modificator) {
		Assert.paramNotNull(attribute, "attribute");
		Assert.paramNotNull(modificator, "modificator");
		return new DecoratedAttribute(attribute, modificator);
	}

	private static final class DecoratedAttribute extends AttributeWrapper<Object> {

		private final IFilterSupportModificator modificator;

		@SuppressWarnings({"rawtypes", "unchecked"})
		private DecoratedAttribute(final IAttribute original, final IFilterSupportModificator modificator) {
			super(original);
			this.modificator = modificator;
		}

		@Override
		public List<IControlPanelProvider<Object>> getControlPanels() {
			final List<IControlPanelProvider<Object>> result = new LinkedList<IControlPanelProvider<Object>>();
			for (final IControlPanelProvider<Object> controlPanelProvider : super.getControlPanels()) {
				result.add(new DecoratedControlPanelProvider(controlPanelProvider, modificator));
			}
			return result;
		}

		@Override
		public IControlPanelProvider<Object> getCurrentFilterControlPanel(final IFilterType filterType) {
			final IControlPanelProvider<Object> original = super.getCurrentFilterControlPanel(filterType);
			if (original != null) {
				return new DecoratedControlPanelProvider(original, modificator);
			}
			else {
				return null;
			}
		}

		@Override
		public IControlPanelProvider<Object> getCurrentControlPanel() {
			final IControlPanelProvider<Object> original = super.getCurrentControlPanel();
			if (original != null) {
				return new DecoratedControlPanelProvider(original, modificator);
			}
			else {
				return null;
			}
		}

		@Override
		public List<IFilterType> getSupportedFilterTypes() {
			final List<IFilterType> result = new LinkedList<IFilterType>();
			for (final IFilterType filterType : super.getSupportedFilterTypes()) {
				if (modificator.acceptFilter(filterType)) {
					result.add(filterType);
				}
			}
			return result;
		}

		@Override
		public IFilterPanelProvider<IOperator> getFilterPanelProvider(final IFilterType filterType) {
			final IFilterPanelProvider<IOperator> original = super.getFilterPanelProvider(filterType);
			if (original != null && modificator.acceptFilter(filterType)) {
				return new DecoratedFilterPanelProvider(original, modificator);
			}
			else {
				return null;
			}
		}

	}

	private static final class DecoratedControlPanelProvider extends ControlPanelProviderWrapper<Object> {

		private final IFilterSupportModificator modificator;

		private DecoratedControlPanelProvider(
			final IControlPanelProvider<Object> original,
			final IFilterSupportModificator modificator) {
			super(original);
			this.modificator = modificator;
		}

		@Override
		public IFilterSupport<Object> getFilterSupport() {
			final IFilterSupport<Object> original = super.getFilterSupport();
			if (original != null) {
				return new DecoratedFilterSupport(original, modificator);
			}
			else {
				return null;
			}
		}

	}

	private static final class DecoratedFilterSupport extends FilterSupportWrapper<Object> {

		private final IFilterSupportModificator modificator;

		private DecoratedFilterSupport(final IFilterSupport<Object> original, final IFilterSupportModificator modificator) {
			super(original);
			this.modificator = modificator;
		}

		@Override
		public List<IFilterPanelProvider<IOperator>> getFilterPanels() {
			final List<IFilterPanelProvider<IOperator>> result = new LinkedList<IFilterPanelProvider<IOperator>>();
			for (final IFilterPanelProvider<IOperator> original : super.getFilterPanels()) {
				if (modificator.acceptFilter(original.getType())) {
					result.add(new DecoratedFilterPanelProvider(original, modificator));
				}
			}
			return result;
		}

		@Override
		public IIncludingFilterFactory<Object> getIncludingFilterFactory() {
			final IIncludingFilterFactory<Object> original = super.getIncludingFilterFactory();
			if (original != null && modificator.acceptFilter(original.getFilterType())) {
				return new DecoratedIncludingFilterFactory(original, modificator);
			}
			else {
				return null;
			}
		}

	}

	private static final class DecoratedFilterPanelProvider extends FilterPanelProviderWrapper<IOperator> {

		private final IFilterSupportModificator modificator;

		private DecoratedFilterPanelProvider(
			final IFilterPanelProvider<IOperator> original,
			final IFilterSupportModificator modificator) {
			super(original);
			this.modificator = modificator;
		}

		@Override
		public IOperatorProvider<IOperator> getOperatorProvider() {
			return new DecoratedOperatorProvider(super.getOperatorProvider(), getType(), modificator);
		}

	}

	private static final class DecoratedOperatorProvider extends OperatorProviderWrapper<IOperator> {

		private final IFilterSupportModificator modificator;
		private final IFilterType filterType;

		private DecoratedOperatorProvider(
			final IOperatorProvider<IOperator> original,
			final IFilterType filterType,
			final IFilterSupportModificator modificator) {
			super(original);
			this.modificator = modificator;
			this.filterType = filterType;
		}

		@Override
		public List<IOperator> getOperators() {
			return filterOperators(super.getOperators());
		}

		@Override
		public IOperator getDefaultOperator() {
			final IOperator originalDefaultOperator = super.getDefaultOperator();
			if (!modificator.acceptOperator(filterType, originalDefaultOperator)) {
				final List<IOperator> operators = getOperators();
				if (operators.size() > 0) {
					return operators.iterator().next();
				}
				else {
					return null;
				}
			}
			else {
				return originalDefaultOperator;
			}
		}

		private List<IOperator> filterOperators(final List<IOperator> originalOperators) {
			final List<IOperator> result = new LinkedList<IOperator>();
			for (final IOperator operator : originalOperators) {
				if (modificator.acceptOperator(filterType, operator)) {
					result.add(operator);
				}
			}
			return result;
		}

		@Override
		public boolean isInvertible(final IOperator operator) {
			return modificator.isOperatorInvertible(filterType, operator);
		}

	}

	private static final class DecoratedIncludingFilterFactory extends IncludingFilterFactoryWrapper<Object> {

		private final IFilterSupportModificator modificator;

		private DecoratedIncludingFilterFactory(
			final IIncludingFilterFactory<Object> original,
			final IFilterSupportModificator modificator) {
			super(original);
			this.modificator = modificator;
		}

		@Override
		public IUiConfigurableFilter<?> getIncludingFilter(final Object attributeValue) {
			final IUiConfigurableFilter<?> originalFilter = super.getIncludingFilter(attributeValue);
			if (originalFilter != null && !modificator.acceptOperator(getFilterType(), originalFilter.getOperator())) {
				return null;
			}
			else {
				return originalFilter;
			}
		}

	}
}
