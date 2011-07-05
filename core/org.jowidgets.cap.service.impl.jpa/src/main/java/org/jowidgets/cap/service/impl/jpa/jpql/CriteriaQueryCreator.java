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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.filter.BooleanOperator;
import org.jowidgets.cap.common.api.filter.IArithmeticFilter;
import org.jowidgets.cap.common.api.filter.IBooleanFilter;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.sort.SortOrder;
import org.jowidgets.cap.service.impl.jpa.IQueryCreator;
import org.jowidgets.util.Assert;

public final class CriteriaQueryCreator implements IQueryCreator<Object> {

	private final Class<? extends IBean> persistenceClass;

	private String parentPropertyName = "parent";
	private IPredicateCreator predicateCreator;
	private boolean caseInsensitve;

	public CriteriaQueryCreator(final Class<? extends IBean> persistenceClass) {
		this.persistenceClass = persistenceClass;
	}

	@Override
	public Class<? extends IBean> getPersistenceClass() {
		return persistenceClass;
	}

	public void setParentPropertyName(final String parentPropertyName) {
		Assert.paramNotNull(parentPropertyName, "parentPropertyName");
		this.parentPropertyName = parentPropertyName;
	}

	public void setPredicateCreator(final IPredicateCreator predicateCreator) {
		this.predicateCreator = predicateCreator;
	}

	public void setCaseInsensitve(final boolean caseInsensitve) {
		this.caseInsensitve = caseInsensitve;
	}

	@Override
	public Query createReadQuery(
		final EntityManager entityManager,
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final List<? extends ISort> sorting,
		final Object parameter) {

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		final CriteriaQuery<?> query = criteriaBuilder.createQuery(persistenceClass);

		final Root<?> bean = fillQuery(query, criteriaBuilder, persistenceClass, parentBeanKeys, filter);

		if (sorting != null) {
			final List<Order> order = new LinkedList<Order>();
			for (final ISort sort : sorting) {
				if (sort.getSortOrder() == SortOrder.ASC) {
					order.add(criteriaBuilder.asc(bean.get(sort.getPropertyName())));
				}
				else {
					order.add(criteriaBuilder.desc(bean.get(sort.getPropertyName())));
				}
			}
			query.orderBy(order);
		}

		return entityManager.createQuery(query);
	}

	@Override
	public Query createCountQuery(
		final EntityManager entityManager,
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
		final List<Predicate> predicates = new LinkedList<Predicate>();

		if (predicateCreator != null) {
			predicates.add(predicateCreator.createPredicate(criteriaBuilder, bean, query));
		}

		if (parentBeanKeys != null) {
			final Set<Object> parentIds = new HashSet<Object>();
			for (final IBeanKey parentBeanKey : parentBeanKeys) {
				parentIds.add(parentBeanKey.getId());
			}
			final Path<?> parentPath = bean.get(parentPropertyName);
			predicates.add(parentPath.get("id").in(parentIds));
		}

		if (filter != null) {
			predicates.add(createFilterPredicate(criteriaBuilder, bean, query, filter));
		}

		query.where(predicates.toArray(new Predicate[0]));
		return bean;
	}

	private Predicate createFilterPredicate(
		final CriteriaBuilder criteriaBuilder,
		final Root<?> bean,
		final CriteriaQuery<?> query,
		final IFilter filter) {
		final Predicate predicate;
		if (filter instanceof IArithmeticFilter) {
			predicate = createFilterPredicate(criteriaBuilder, bean, query, (IArithmeticFilter) filter);
		}
		else if (filter instanceof IBooleanFilter) {
			predicate = createFilterPredicate(criteriaBuilder, bean, query, (IBooleanFilter) filter);
		}
		else {
			throw new IllegalArgumentException("unsupported filter type: " + filter.getClass().getName());
		}
		if (filter.isInverted()) {
			return criteriaBuilder.not(predicate);
		}
		return predicate;
	}

	@SuppressWarnings("unchecked")
	private Predicate createFilterPredicate(
		final CriteriaBuilder criteriaBuilder,
		final Root<?> bean,
		final CriteriaQuery<?> query,
		final IArithmeticFilter filter) {
		final Path<?> path = bean.get(filter.getPropertyName());
		boolean isCollection;
		try {
			isCollection = bean.fetch(filter.getPropertyName()).getAttribute().isCollection();
		}
		catch (final Exception e) {
			isCollection = false;
		}
		switch (filter.getOperator()) {
			case BETWEEN:
				return criteriaBuilder.between(
						(Path<Comparable<Object>>) path,
						criteriaBuilder.literal((Comparable<Object>) filter.getParameters()[0]),
						criteriaBuilder.literal((Comparable<Object>) filter.getParameters()[1]));
			case GREATER:
				return criteriaBuilder.greaterThan(
						(Path<Comparable<Object>>) path,
						criteriaBuilder.literal((Comparable<Object>) filter.getParameters()[0]));
			case GREATER_EQUAL:
				return criteriaBuilder.greaterThanOrEqualTo(
						(Path<Comparable<Object>>) path,
						criteriaBuilder.literal((Comparable<Object>) filter.getParameters()[0]));
			case LESS:
				return criteriaBuilder.lessThan(
						(Path<Comparable<Object>>) path,
						criteriaBuilder.literal((Comparable<Object>) filter.getParameters()[0]));
			case LESS_EQUAL:
				return criteriaBuilder.lessThanOrEqualTo(
						(Path<Comparable<Object>>) path,
						criteriaBuilder.literal((Comparable<Object>) filter.getParameters()[0]));
			case EQUAL:
				query.distinct(isCollection);
				Expression<?> expr = path;
				Object arg = filter.getParameters()[0];
				boolean useLike = false;
				if (arg instanceof String && path.getJavaType() == String.class) {
					final String s = (String) arg;
					if (s.contains("*") || s.contains("%")) {
						useLike = true;
						arg = s.replace("*", "%");
					}
					if (caseInsensitve) {
						expr = criteriaBuilder.upper((Expression<String>) expr);
						arg = ((String) arg).toUpperCase();
					}
				}
				if (useLike) {
					return criteriaBuilder.like((Expression<String>) expr, (String) arg);
				}
				return criteriaBuilder.equal(expr, arg);
			case EMPTY:
				if (isCollection) {
					query.distinct(true);
					// TODO HRW fix query for empty collections
					return criteriaBuilder.isEmpty((Expression<Collection<?>>) path);
					//					final Join<?, ?> join = bean.join(filter.getPropertyName(), JoinType.LEFT);
					//					final Subquery<Long> subQuery = query.subquery(Long.class);
					//					final Root<?> thingy = subQuery.from(bean.getJavaType());
					//					subQuery.select(criteriaBuilder.count(thingy)).where(criteriaBuilder.equal(thingy, path));
					//					return criteriaBuilder.equal(subQuery, 0);
				}
				if (path.getJavaType() == String.class) {
					return criteriaBuilder.or(path.isNull(), criteriaBuilder.equal(path, ""));
				}
				return path.isNull();
			case CONTAINS_ANY:
				// TODO HRW evaluate case insensitive flag	
				query.distinct(true);
				return path.in((Collection<?>) filter.getParameters()[0]);
			case CONTAINS_ALL:
				// TODO HRW add support for CONTAINS_ALL
			default:
				throw new IllegalArgumentException("unsupported operator: " + filter.getOperator());
		}
	}

	private Predicate createFilterPredicate(
		final CriteriaBuilder criteriaBuilder,
		final Root<?> bean,
		final CriteriaQuery<?> query,
		final IBooleanFilter filter) {
		final List<Predicate> predicates = new LinkedList<Predicate>();
		for (final IFilter subFilter : filter.getFilters()) {
			final Predicate predicate = createFilterPredicate(criteriaBuilder, bean, query, subFilter);
			predicates.add(predicate);
		}
		if (filter.getOperator() == BooleanOperator.AND) {
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		}
		return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
	}

}
