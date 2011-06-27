/*
 * Copyright (c) 2011, H.Westphal
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

package org.jowidgets.cap.service.impl.jpa.jpql;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.swing.SortOrder;

import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.filter.BooleanOperator;
import org.jowidgets.cap.common.api.filter.IArithmeticFilter;
import org.jowidgets.cap.common.api.filter.IBooleanFilter;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.service.impl.jpa.IQueryCreator;
import org.jowidgets.util.Assert;

public class CriteriaQueryCreator implements IQueryCreator<Object> {

	private String parentPropertyName = "parent";

	public void setParentPropertyName(final String parentPropertyName) {
		Assert.paramNotNull(parentPropertyName, "parentPropertyName");
		this.parentPropertyName = parentPropertyName;
	}

	@Override
	public Query createReadQuery(
		final EntityManager entityManager,
		final Class<?> persistenceClass,
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final List<? extends ISort> sorting,
		final Object parameter) {

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		final CriteriaQuery<?> query = criteriaBuilder.createQuery(persistenceClass);

		final Root<?> bean = fillQuery(query, criteriaBuilder, persistenceClass, parentBeanKeys, filter);

		if (sorting != null) {
			for (final ISort sort : sorting) {
				if (sort.getSortOrder() == SortOrder.ASCENDING) {
					query.orderBy(criteriaBuilder.asc(bean.get(sort.getPropertyName())));
				}
				else {
					query.orderBy(criteriaBuilder.desc(bean.get(sort.getPropertyName())));
				}
			}
		}

		return entityManager.createQuery(query);
	}

	@Override
	public Query createCountQuery(
		final EntityManager entityManager,
		final Class<?> persistenceClass,
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final Object parameter) {

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		final CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);

		final Root<?> bean = fillQuery(query, criteriaBuilder, persistenceClass, parentBeanKeys, filter);

		return entityManager.createQuery(query.select(criteriaBuilder.count(bean)));
	}

	private Root<?> fillQuery(
		final CriteriaQuery<?> query,
		final CriteriaBuilder criteriaBuilder,
		final Class<?> persistenceClass,
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter) {

		final Root<?> bean = query.from(persistenceClass);

		if (parentBeanKeys != null) {
			final Set<Object> parentIds = new HashSet<Object>();
			for (final IBeanKey parentBeanKey : parentBeanKeys) {
				parentIds.add(parentBeanKey.getId());
			}
			query.where(bean.get(parentPropertyName + ".id").in(parentIds));
		}

		if (filter != null) {
			query.where(createFilterExpression(criteriaBuilder, bean, filter));
		}

		return bean;
	}

	private Expression<Boolean> createFilterExpression(
		final CriteriaBuilder criteriaBuilder,
		final Root<?> bean,
		final IFilter filter) {
		final Expression<Boolean> expr;
		if (filter instanceof IArithmeticFilter) {
			expr = createFilterExpression(criteriaBuilder, bean, (IArithmeticFilter) filter);
		}
		else if (filter instanceof IBooleanFilter) {
			expr = createFilterExpression(criteriaBuilder, bean, (IBooleanFilter) filter);
		}
		else {
			throw new IllegalArgumentException("unsupported filter type: " + filter.getClass().getName());
		}
		if (filter.isInverted()) {
			return criteriaBuilder.not(expr);
		}
		return expr;
	}

	@SuppressWarnings("unchecked")
	private Expression<Boolean> createFilterExpression(
		final CriteriaBuilder criteriaBuilder,
		final Root<?> bean,
		final IArithmeticFilter filter) {
		final Path<?> path = bean.get(filter.getPropertyName());
		switch (filter.getOperator()) {
			case BETWEEN:
				return criteriaBuilder.between(
						(Path<Comparable<Object>>) path,
						criteriaBuilder.literal((Comparable<Object>) filter.getParameters()[0]),
						criteriaBuilder.literal((Comparable<Object>) filter.getParameters()[1]));
			case GREATER:
				return criteriaBuilder.gt((Path<Number>) path, criteriaBuilder.literal((Number) filter.getParameters()[0]));
			case GREATER_EQUAL:
				return criteriaBuilder.ge((Path<Number>) path, criteriaBuilder.literal((Number) filter.getParameters()[0]));
			case LESS:
				return criteriaBuilder.lt((Path<Number>) path, criteriaBuilder.literal((Number) filter.getParameters()[0]));
			case LESS_EQUAL:
				return criteriaBuilder.le((Path<Number>) path, criteriaBuilder.literal((Number) filter.getParameters()[0]));
			case EQUAL:
				// TODO HW handle null argument?
				// TODO HW handle collection property
				return criteriaBuilder.equal(path, filter.getParameters()[0]);
			case EMPTY:
				// TODO HW handle collection property
				return criteriaBuilder.isNull(path);
			case CONTAINS_ALL:
			case CONTAINS_ANY:
				// TODO HW implement collection operations
			default:
				throw new IllegalArgumentException("unsupported operator: " + filter.getOperator());
		}
	}

	private Expression<Boolean> createFilterExpression(
		final CriteriaBuilder criteriaBuilder,
		final Root<?> bean,
		final IBooleanFilter filter) {
		if (filter.getFilters().size() == 0) {
			return criteriaBuilder.literal(filter.getOperator() == BooleanOperator.AND);
		}
		final IBooleanFilter next = new IBooleanFilter() {
			@Override
			public boolean isInverted() {
				return false;
			}

			@Override
			public BooleanOperator getOperator() {
				return filter.getOperator();
			}

			@Override
			public List<IFilter> getFilters() {
				return filter.getFilters().subList(1, filter.getFilters().size());
			}
		};
		final Expression<Boolean> expr1 = createFilterExpression(criteriaBuilder, bean, filter.getFilters().get(0));
		final Expression<Boolean> expr2 = createFilterExpression(criteriaBuilder, bean, next);
		if (filter.getOperator() == BooleanOperator.AND) {
			return criteriaBuilder.and(expr1, expr2);
		}
		return criteriaBuilder.or(expr1, expr2);
	}

}
