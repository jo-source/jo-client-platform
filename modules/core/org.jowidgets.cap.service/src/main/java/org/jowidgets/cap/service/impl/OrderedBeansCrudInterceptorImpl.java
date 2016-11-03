/*
 * Copyright (c) 2016, grossmann
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.bean.IBeanModification;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.filter.ArithmeticFilter;
import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.common.api.filter.BooleanFilter;
import org.jowidgets.cap.common.api.filter.IArithmeticFilterBuilder;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.ordered.IOrderedBean;
import org.jowidgets.cap.common.api.sort.SortOrder;
import org.jowidgets.cap.common.tools.sort.Sort;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanIdentityResolver;
import org.jowidgets.cap.service.api.bean.IBeanReader;
import org.jowidgets.cap.service.api.creator.IBeanDataMapper;
import org.jowidgets.cap.service.api.crud.ICrudServiceInterceptor;
import org.jowidgets.cap.service.api.ordered.IOrderedBeanGroupMapper;
import org.jowidgets.cap.service.api.updater.IBeanModificationsMap;
import org.jowidgets.cap.service.tools.crud.CrudServiceInterceptorAdapter;
import org.jowidgets.cap.service.tools.ordered.OrderedBeanComparator;
import org.jowidgets.util.Assert;
import org.jowidgets.util.collection.FilteredIterable;

final class OrderedBeansCrudInterceptorImpl<BEAN_TYPE extends IOrderedBean> extends CrudServiceInterceptorAdapter<BEAN_TYPE>
		implements ICrudServiceInterceptor<BEAN_TYPE> {

	private static final int MAX_BEANS = 20000;

	private static final List<Sort> ORDER_NUMBER_ASC_SORTING = Arrays.asList(
			new Sort(IOrderedBean.ORDER_NUMBER_PROPERTY, SortOrder.ASC));

	private final IBeanIdentityResolver<BEAN_TYPE> identityResolver;
	private final IBeanReader<BEAN_TYPE, Void> beanReader;
	private final IOrderedBeanGroupMapper<BEAN_TYPE, Object> groupMapper;

	@SuppressWarnings("unchecked")
	OrderedBeansCrudInterceptorImpl(final OrderedBeanCrudInterceptorBuilderImpl<BEAN_TYPE, ?> builder) {

		Assert.paramNotNull(builder.getIdentityResolver(), "builder.getIdentityResolver()");
		Assert.paramNotNull(builder.getGroupMapper(), "builder.getGroupMapper()");
		Assert.paramNotNull(builder.getBeanReader(), "builder.getBeanReader()");

		this.identityResolver = builder.getIdentityResolver();
		this.beanReader = builder.getBeanReader();
		this.groupMapper = (IOrderedBeanGroupMapper<BEAN_TYPE, Object>) builder.getGroupMapper();
	}

	@Override
	public void afterInitializeForCreation(
		final List<IBeanKey> parentBeanKeys,
		final Collection<BEAN_TYPE> beans,
		final IBeanDataMapper<BEAN_TYPE> mapper,
		final IExecutionCallback executionCallback) {

		fixOrderNumbersForGroups(new ArrayList<BEAN_TYPE>(beans), new CreatedBeansFilter(mapper), false, executionCallback);
	}

	@Override
	public void afterUpdate(
		final Collection<BEAN_TYPE> beans,
		final IBeanModificationsMap<BEAN_TYPE> modifications,
		final IExecutionCallback executionCallback) {

		final List<BEAN_TYPE> beansWithRelevantModifications = getBeansWithRelevantModifications(
				beans,
				modifications,
				executionCallback);

		fixOrderNumbersForGroups(
				beansWithRelevantModifications,
				new RelevantModifiedBeansFilter(modifications),
				false,
				executionCallback);
	}

	@Override
	public void afterDelete(final Collection<BEAN_TYPE> beans, final IExecutionCallback executionCallback) {
		fixOrderNumbersForGroups(new ArrayList<BEAN_TYPE>(beans), new DeletedBeansFilter(beans), true, executionCallback);
	}

	private void fixOrderNumbersForGroups(
		final List<BEAN_TYPE> beans,
		final org.jowidgets.util.IFilter<BEAN_TYPE> beansFilter,
		final boolean beansDeleted,
		final IExecutionCallback executionCallback) {

		final Set<Entry<Object, List<BEAN_TYPE>>> mappedToGroups = mapBeansToGroups(beans, executionCallback).entrySet();

		for (final Entry<Object, List<BEAN_TYPE>> beansOfGroup : mappedToGroups) {
			CapServiceToolkit.checkCanceled(executionCallback);
			fixOrderNumbersForGroup(beansOfGroup.getKey(), beansOfGroup.getValue(), beansFilter, beansDeleted, executionCallback);
		}
	}

	private List<BEAN_TYPE> getBeansWithRelevantModifications(
		final Collection<BEAN_TYPE> beans,
		final IBeanModificationsMap<BEAN_TYPE> modifications,
		final IExecutionCallback executionCallback) {
		final List<BEAN_TYPE> result = new LinkedList<BEAN_TYPE>();

		for (final BEAN_TYPE bean : beans) {
			CapServiceToolkit.checkCanceled(executionCallback);
			if (hasBeanRelevantModifications(bean, modifications.getModifications(bean))) {
				result.add(bean);
			}
		}
		return result;
	}

	private boolean hasBeanRelevantModifications(final IOrderedBean bean, final List<IBeanModification> modifications) {

		final Set<String> groupChangingAttributes = groupMapper.getGroupChangingAttributes();
		boolean hasGroupChangingModifications = false;
		for (final IBeanModification modification : modifications) {
			if (IOrderedBean.ORDER_NUMBER_PROPERTY.equals(modification.getPropertyName())) {
				final Object lastOrderNumber = modification.getOldValue();
				final Object newOrderNumber = modification.getNewValue();
				if (lastOrderNumber instanceof Long || newOrderNumber instanceof Long) {
					return true;
				}
			}
			else if (groupChangingAttributes.contains(modification.getPropertyName())) {
				hasGroupChangingModifications = true;
			}
		}
		if (hasGroupChangingModifications && bean.getOrderNumber() != null) {
			return true;
		}
		else {
			return false;
		}
	}

	private Map<Object, List<BEAN_TYPE>> mapBeansToGroups(
		final Collection<BEAN_TYPE> beans,
		final IExecutionCallback executionCallback) {
		final Map<Object, List<BEAN_TYPE>> result = new HashMap<Object, List<BEAN_TYPE>>();
		for (final BEAN_TYPE bean : beans) {
			CapServiceToolkit.checkCanceled(executionCallback);
			final Object group = groupMapper.getGroup(bean);
			List<BEAN_TYPE> beansOfGroup = result.get(group);
			if (beansOfGroup == null) {
				beansOfGroup = new ArrayList<BEAN_TYPE>();
				result.put(group, beansOfGroup);
			}
			beansOfGroup.add(bean);
		}
		return result;
	}

	private void fixOrderNumbersForGroup(
		final Object group,
		final List<BEAN_TYPE> relevantGroupMembers,
		final org.jowidgets.util.IFilter<BEAN_TYPE> relevantBeansFilter,
		final boolean beansDeleted,
		final IExecutionCallback executionCallback) {

		final List<BEAN_TYPE> allGroupMembers = readAllMembersOfGroupFromReader(group, executionCallback);

		final Set<Long> newOrderNumbers;
		if (beansDeleted) {
			newOrderNumbers = Collections.emptySet();
		}
		else {
			newOrderNumbers = calculateNewOrderNumbers(relevantGroupMembers);
		}
		final long nextOrderNumber = fixOrderNumbersForGroup(
				new FilteredIterable<BEAN_TYPE>(allGroupMembers, relevantBeansFilter).iterator(),
				newOrderNumbers,
				executionCallback);

		if (!beansDeleted && !relevantGroupMembers.isEmpty()) {
			fixOrderNumbersOfRelevantMembersForGroup(group, relevantGroupMembers, nextOrderNumber, executionCallback);
		}
	}

	private void fixOrderNumbersOfRelevantMembersForGroup(
		final Object group,
		final List<BEAN_TYPE> relevantGroupMembers,
		final long nextOrderNumber,
		final IExecutionCallback executionCallback) {

		Collections.sort(relevantGroupMembers, OrderedBeanComparator.getInstance());
		setOrderOfBeans(relevantGroupMembers, nextOrderNumber, executionCallback);
	}

	private void setOrderOfBeans(final List<BEAN_TYPE> beans, final long startOrder, final IExecutionCallback executionCallback) {

		long order = startOrder;

		final List<IOrderedBean> nullOrderBeans = new LinkedList<IOrderedBean>();
		for (final IOrderedBean bean : beans) {
			final Long orderOfBeanRef = bean.getOrderNumber();
			if (orderOfBeanRef != null) {
				if (orderOfBeanRef.longValue() >= startOrder) {
					bean.setOrderNumber(Long.valueOf(order++));
				}
			}
			else {
				nullOrderBeans.add(bean);
			}
		}

		for (final IOrderedBean beanRef : nullOrderBeans) {
			beanRef.setOrderNumber(Long.valueOf(order++));
		}
	}

	private Set<Long> calculateNewOrderNumbers(final List<BEAN_TYPE> beanRefs) {
		final Set<Long> result = new HashSet<Long>();
		for (final BEAN_TYPE beanRef : beanRefs) {
			final Long orderNumber = beanRef.getOrderNumber();
			if (orderNumber != null) {
				long order = orderNumber.longValue();
				while (result.contains(Long.valueOf(order))) {
					order++;
					beanRef.setOrderNumber(Long.valueOf(order));
				}
				result.add(Long.valueOf(order));
			}
		}
		return result;
	}

	private long fixOrderNumbersForGroup(
		final Iterator<BEAN_TYPE> allIterator,
		final Set<Long> newOrderNumbers,
		final IExecutionCallback executionCallback) {

		long expectedOrderNumber = 0;
		while (allIterator.hasNext()) {
			final IOrderedBean bean = allIterator.next();

			CapServiceToolkit.checkCanceled(executionCallback);

			while (newOrderNumbers.contains(Long.valueOf(expectedOrderNumber))) {
				CapServiceToolkit.checkCanceled(executionCallback);
				expectedOrderNumber++;
			}

			if (bean.getOrderNumber().longValue() != expectedOrderNumber) {
				bean.setOrderNumber(Long.valueOf(expectedOrderNumber));
			}

			expectedOrderNumber++;
		}

		return expectedOrderNumber;
	}

	private List<BEAN_TYPE> readAllMembersOfGroupFromReader(final Object group, final IExecutionCallback executionCallback) {
		final List<IBeanKey> parent = Collections.emptyList();
		return beanReader.read(
				parent,
				createReadFilter(group),
				ORDER_NUMBER_ASC_SORTING,
				0,
				MAX_BEANS + 1,
				null,
				executionCallback);
	}

	private IFilter createReadFilter(final Object group) {
		final IFilter groupFilter = groupMapper.createGroupFilter(group);
		final IFilter orderNumberNotNullFilter = createOrderNumberNotNullFilter();
		if (groupFilter == null) {
			return orderNumberNotNullFilter;
		}
		else {
			return BooleanFilter.create(groupFilter, orderNumberNotNullFilter);
		}
	}

	private IFilter createOrderNumberNotNullFilter() {
		final IArithmeticFilterBuilder builder = ArithmeticFilter.builder();
		builder.setPropertyName(IOrderedBean.ORDER_NUMBER_PROPERTY);
		builder.setOperator(ArithmeticOperator.EMPTY);
		builder.setInverted(true);
		return builder.build();
	}

	/**
	 * Filters all created beans
	 */
	private final class CreatedBeansFilter implements org.jowidgets.util.IFilter<BEAN_TYPE> {

		private final IBeanDataMapper<BEAN_TYPE> beanDataMapper;

		CreatedBeansFilter(final IBeanDataMapper<BEAN_TYPE> beanDataMapper) {
			Assert.paramNotNull(beanDataMapper, "beanDataMapper");
			this.beanDataMapper = beanDataMapper;
		}

		@Override
		public boolean accept(final BEAN_TYPE bean) {
			final boolean isCreatedBean = beanDataMapper.getBeanData(bean) != null;
			//this must be done by database, but if filter does not work correctly, do this to avoid NPE
			final boolean hasOrderNumber = bean.getOrderNumber() != null;
			return !isCreatedBean && hasOrderNumber;
		}

	}

	/**
	 * Filters all updated beans with relevant modifications
	 */
	private final class RelevantModifiedBeansFilter implements org.jowidgets.util.IFilter<BEAN_TYPE> {

		private final IBeanModificationsMap<BEAN_TYPE> modifications;

		RelevantModifiedBeansFilter(final IBeanModificationsMap<BEAN_TYPE> modifications) {
			Assert.paramNotNull(modifications, "modifications");
			this.modifications = modifications;
		}

		@Override
		public boolean accept(final BEAN_TYPE bean) {
			final boolean hasRelevantModification = hasBeanRelevantModifications(bean, modifications.getModifications(bean));
			//this must be done by database, but if filter does not work correctly, do this to avoid NPE
			final boolean hasOrderNumber = bean.getOrderNumber() != null;
			return !hasRelevantModification && hasOrderNumber;
		}

	}

	/**
	 * Filters all created beans
	 */
	private final class DeletedBeansFilter implements org.jowidgets.util.IFilter<BEAN_TYPE> {

		private final Set<Object> beanIds;

		DeletedBeansFilter(final Collection<BEAN_TYPE> deletedBeans) {
			Assert.paramNotNull(deletedBeans, "deletedBeans");
			this.beanIds = new HashSet<Object>();
			for (final BEAN_TYPE bean : deletedBeans) {
				beanIds.add(identityResolver.getId(bean));
			}
		}

		@Override
		public boolean accept(final BEAN_TYPE bean) {
			final boolean isDeletedBean = beanIds.contains(identityResolver.getId(bean));
			//this must be done by database, but if filter does not work correctly, do this to avoid NPE
			final boolean hasOrderNumber = bean.getOrderNumber() != null;
			return !isDeletedBean && hasOrderNumber;
		}

	}

}
