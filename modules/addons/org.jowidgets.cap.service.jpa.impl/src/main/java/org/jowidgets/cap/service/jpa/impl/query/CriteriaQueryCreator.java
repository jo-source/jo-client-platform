/*
 * Copyright (c) 2011, H.Westphal, M.Grossmann
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

package org.jowidgets.cap.service.jpa.impl.query;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.bean.IPropertyMap;
import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.common.api.filter.BooleanOperator;
import org.jowidgets.cap.common.api.filter.IArithmeticFilter;
import org.jowidgets.cap.common.api.filter.IArithmeticPropertyFilter;
import org.jowidgets.cap.common.api.filter.IBooleanFilter;
import org.jowidgets.cap.common.api.filter.ICustomFilter;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.filter.IPropertyFilter;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.sort.SortOrder;
import org.jowidgets.cap.service.jpa.api.plugin.ICustomFilterPredicateCreatorPlugin;
import org.jowidgets.cap.service.jpa.api.query.FilterParameterConverter;
import org.jowidgets.cap.service.jpa.api.query.ICustomFilterPredicateCreator;
import org.jowidgets.cap.service.jpa.api.query.IPredicateCreator;
import org.jowidgets.cap.service.jpa.api.query.IPropertyFilterPredicateCreator;
import org.jowidgets.cap.service.jpa.api.query.IQueryCreator;
import org.jowidgets.cap.service.jpa.api.query.PropertyMapQueryPath;
import org.jowidgets.cap.service.jpa.api.query.QueryPath;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProperties;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IConverter;

final class CriteriaQueryCreator<PARAM_TYPE> implements IQueryCreator<PARAM_TYPE> {

	private static final String PROPERTY_MAP_JOIN_ALIAS = "propertyMap";

	private final Class<? extends IBean> beanType;
	private final boolean caseInsensitive;
	private final List<IPredicateCreator<PARAM_TYPE>> predicateCreators;
	private final List<IFilter> filters;
	private final Map<String, ? extends ICustomFilterPredicateCreator<PARAM_TYPE>> customFilterPredicateCreators;
	private final Map<String, ? extends IPropertyFilterPredicateCreator<PARAM_TYPE>> propertyFilterPredicateCreators;

	CriteriaQueryCreator(
		final Class<? extends IBean> beanType,
		final boolean caseSensitive,
		final Collection<? extends IPredicateCreator<PARAM_TYPE>> predicateCreators,
		final Collection<? extends IFilter> filters,
		final Map<String, ? extends ICustomFilterPredicateCreator<PARAM_TYPE>> customFilterPredicateCreators,
		final Map<String, ? extends IPropertyFilterPredicateCreator<PARAM_TYPE>> propertyFilterPredicateCreators) {

		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(predicateCreators, "predicateCreators");
		Assert.paramNotNull(filters, "filters");
		Assert.paramNotNull(customFilterPredicateCreators, "customFilterPredicateCreators");
		Assert.paramNotNull(propertyFilterPredicateCreators, "propertyFilterPredicateCreators");

		this.beanType = beanType;
		this.caseInsensitive = !caseSensitive;
		this.predicateCreators = new LinkedList<IPredicateCreator<PARAM_TYPE>>(predicateCreators);
		this.filters = new LinkedList<IFilter>(filters);
		this.customFilterPredicateCreators = new HashMap<String, ICustomFilterPredicateCreator<PARAM_TYPE>>(
			customFilterPredicateCreators);
		this.propertyFilterPredicateCreators = new HashMap<String, IPropertyFilterPredicateCreator<PARAM_TYPE>>(
			propertyFilterPredicateCreators);
	}

	@Override
	public Query createReadQuery(
		final EntityManager entityManager,
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final List<? extends ISort> sorting,
		final PARAM_TYPE parameter) {

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		final CriteriaQuery<?> query = criteriaBuilder.createQuery(beanType);
		query.distinct(true);
		final Root<?> bean = fillQuery(query, criteriaBuilder, beanType, parentBeanKeys, filter, parameter);

		if (sorting != null) {
			final List<Order> order = new LinkedList<Order>();
			for (final ISort sort : sorting) {
				final Path<?> path = getPath(bean, sort.getPropertyName());
				// TODO MG fix sorting of joined attributes
				if (sort.getSortOrder() == SortOrder.ASC) {
					order.add(criteriaBuilder.asc(path));
				}
				else {
					order.add(criteriaBuilder.desc(path));
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
		final PARAM_TYPE parameter) {

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		final CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
		final Root<?> bean = fillQuery(query, criteriaBuilder, beanType, parentBeanKeys, filter, parameter);

		return entityManager.createQuery(query.select(criteriaBuilder.countDistinct(bean)));
	}

	@SuppressWarnings("unchecked")
	private Root<?> fillQuery(
		final CriteriaQuery<?> query,
		final CriteriaBuilder criteriaBuilder,
		final Class<?> persistenceClass,
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final PARAM_TYPE parameter) {

		final Root<?> bean = query.from(persistenceClass);

		final List<Predicate> predicates = new LinkedList<Predicate>();

		final List<Object> parentIds = new LinkedList<Object>();
		if (parentBeanKeys != null) {
			for (final IBeanKey parentBeanKey : parentBeanKeys) {
				parentIds.add(parentBeanKey.getId());
			}
		}

		for (final IPredicateCreator<PARAM_TYPE> predicateCreator : predicateCreators) {
			final Predicate predicate = predicateCreator.createPredicate(
					criteriaBuilder,
					bean,
					query,
					(List<IBeanKey>) parentBeanKeys,
					parentIds,
					parameter);
			if (predicate != null) {
				predicates.add(predicate);
			}
		}

		for (final IFilter customFilter : filters) {
			predicates.add(createFilterPredicate(criteriaBuilder, bean, query, customFilter, parameter));
		}

		if (filter != null) {
			predicates.add(createFilterPredicate(criteriaBuilder, bean, query, filter, parameter));
		}

		query.where(predicates.toArray(new Predicate[predicates.size()]));
		return bean;
	}

	private Predicate createFilterPredicate(
		final CriteriaBuilder criteriaBuilder,
		final Root<?> bean,
		final CriteriaQuery<?> query,
		final IFilter filter,
		final PARAM_TYPE parameter) {

		if (filter instanceof IPropertyFilter) {
			final IPropertyFilter propertyFilter = (IPropertyFilter) filter;
			final String propertyName = propertyFilter.getPropertyName();
			final IPropertyFilterPredicateCreator<PARAM_TYPE> predicateCreator = propertyFilterPredicateCreators.get(propertyName);
			if (predicateCreator != null) {
				return predicateCreator.createPredicate(criteriaBuilder, bean, query, propertyFilter, parameter);
			}
		}

		if (filter instanceof IArithmeticFilter) {
			return createArithmeticFilterPredicate(criteriaBuilder, bean, query, (IArithmeticFilter) filter);
		}
		else if (filter instanceof IArithmeticPropertyFilter) {
			// TODO MG support IArithmeticPropertyFilter
			throw new IllegalArgumentException("unsupported filter type: " + filter.getClass().getName());
		}
		else if (filter instanceof IBooleanFilter) {
			final Predicate predicate;
			predicate = createBooleanFilterPredicate(criteriaBuilder, bean, query, (IBooleanFilter) filter, parameter);
			return invertPredicateIfNeeded(criteriaBuilder, predicate, filter, true);
		}
		else if (filter instanceof ICustomFilter) {
			final ICustomFilter customFilter = (ICustomFilter) filter;
			final ICustomFilterPredicateCreator<PARAM_TYPE> customFilterPredicateCreator = getCustomFilterPredicateCreator(customFilter);
			if (customFilterPredicateCreator != null) {
				final Predicate predicate;
				predicate = customFilterPredicateCreator.createPredicate(
						criteriaBuilder,
						getPath(bean, customFilter.getPropertyName()),
						query,
						customFilter,
						parameter);
				return invertPredicateIfNeeded(criteriaBuilder, predicate, filter, true);
			}
			else {
				throw new IllegalArgumentException("unsupported custom filter type: " + customFilter.getFilterType());
			}
		}
		else {
			throw new IllegalArgumentException("unsupported filter type: " + filter.getClass().getName());
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private ICustomFilterPredicateCreator<PARAM_TYPE> getCustomFilterPredicateCreator(final ICustomFilter customFilter) {
		ICustomFilterPredicateCreator<PARAM_TYPE> result = customFilterPredicateCreators.get(customFilter.getFilterType());
		final IPluginPropertiesBuilder propertiesBuilder = PluginProperties.builder();
		propertiesBuilder.add(ICustomFilterPredicateCreatorPlugin.FILTER_TYPE_PROPERTY_KEY, customFilter.getFilterType());
		propertiesBuilder.add(ICustomFilterPredicateCreatorPlugin.BEAN_TYPE_PROPERTY_KEY, beanType);
		final List<ICustomFilterPredicateCreatorPlugin<Object>> plugins = PluginProvider.getPlugins(
				ICustomFilterPredicateCreatorPlugin.ID,
				propertiesBuilder.build());
		for (final ICustomFilterPredicateCreatorPlugin plugin : plugins) {
			result = plugin.getPredicateCreator(result);
		}
		return result;
	}

	private Predicate createBooleanFilterPredicate(
		final CriteriaBuilder criteriaBuilder,
		final Root<?> bean,
		final CriteriaQuery<?> query,
		final IBooleanFilter filter,
		final PARAM_TYPE parameter) {
		final List<Predicate> predicates = new LinkedList<Predicate>();
		for (final IFilter subFilter : filter.getFilters()) {
			final Predicate predicate = createFilterPredicate(criteriaBuilder, bean, query, subFilter, parameter);
			predicates.add(predicate);
		}
		if (filter.getOperator() == BooleanOperator.AND) {
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		}
		return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
	}

	private Predicate createArithmeticFilterPredicate(
		final CriteriaBuilder criteriaBuilder,
		final Root<?> bean,
		final CriteriaQuery<?> query,
		final IArithmeticFilter filter) {

		if (isJoinQueryPath(bean, filter.getPropertyName())) {
			return createQueryPathArithmeticFilterPredicate(criteriaBuilder, bean, query, filter);
		}
		else {
			return createArithmeticFilterPredicate(
					criteriaBuilder,
					bean,
					query,
					filter,
					getPath(bean, filter.getPropertyName()),
					true);
		}
	}

	private Predicate createQueryPathArithmeticFilterPredicate(
		final CriteriaBuilder criteriaBuilder,
		final Root<?> bean,
		final CriteriaQuery<?> query,
		final IArithmeticFilter filter) {

		if (ArithmeticOperator.CONTAINS_ALL == filter.getOperator()) {
			return createQueryPathContainsAllFilterPredicate(criteriaBuilder, bean, query, filter);
		}
		else {
			return createQueryPathGenericArithmeticFilterPredicate(criteriaBuilder, bean, query, filter);
		}
	}

	@SuppressWarnings("unchecked")
	private Predicate createQueryPathGenericArithmeticFilterPredicate(
		final CriteriaBuilder criteriaBuilder,
		final Root<?> bean,
		final CriteriaQuery<?> query,
		final IArithmeticFilter filter) {
		final Class<Object> javaType = (Class<Object>) bean.getJavaType();
		final Subquery<Object> subquery = query.subquery(javaType);
		final Root<Object> subqueryRoot = subquery.from(javaType);
		subquery.select(subqueryRoot.get(IBean.ID_PROPERTY));

		final boolean existanceFilter = ArithmeticOperator.EMPTY == filter.getOperator();

		final Path<?> joinQueryPath = getJoinQueryPath(subqueryRoot, filter.getPropertyName());

		final Predicate predicate = createArithmeticFilterPredicate(
				criteriaBuilder,
				subqueryRoot,
				subquery,
				filter,
				joinQueryPath,
				existanceFilter);

		final PropertyMapQueryPath propertyMapQueryPath = bean.getJavaType().getAnnotation(PropertyMapQueryPath.class);
		if (IPropertyMap.class.isAssignableFrom(bean.getJavaType())
			&& joinQueryPath.getParentPath() != null
			&& joinQueryPath.getParentPath().getAlias() != null
			&& propertyMapQueryPath != null
			&& joinQueryPath.getParentPath().getAlias().equals(PROPERTY_MAP_JOIN_ALIAS)) {
			final Predicate propertyMapPredicate = criteriaBuilder.equal(
					joinQueryPath.getParentPath().get(propertyMapQueryPath.propertyNamePath()),
					filter.getPropertyName());
			subquery.where(predicate, propertyMapPredicate);
		}
		else {
			subquery.where(predicate);
		}

		if (filter.isInverted() && !existanceFilter) {
			return criteriaBuilder.not(bean.get(IBean.ID_PROPERTY).in(subquery));
		}
		else {
			return bean.get(IBean.ID_PROPERTY).in(subquery);
		}
	}

	@SuppressWarnings("unchecked")
	private Predicate createQueryPathContainsAllFilterPredicate(
		final CriteriaBuilder criteriaBuilder,
		final Root<?> bean,
		final CriteriaQuery<?> query,
		final IArithmeticFilter filter) {
		final Predicate[] predicates = new Predicate[filter.getParameters().length];
		int index = 0;
		for (final Object parameter : filter.getParameters()) {
			final Class<Object> javaType = (Class<Object>) bean.getJavaType();
			final Subquery<Object> subquery = query.subquery(javaType);
			final Root<Object> subqueryRoot = subquery.from(javaType);
			subquery.select(subqueryRoot.get(IBean.ID_PROPERTY));

			final Predicate predicate = createEqualPredicate(
					criteriaBuilder,
					filter,
					getJoinQueryPath(subqueryRoot, filter.getPropertyName()),
					false,
					parameter);

			subquery.where(predicate);

			if (filter.isInverted()) {
				predicates[index] = criteriaBuilder.not(bean.get(IBean.ID_PROPERTY).in(subquery));
			}
			else {
				predicates[index] = bean.get(IBean.ID_PROPERTY).in(subquery);
			}
			index++;
		}
		return criteriaBuilder.and(predicates);
	}

	private Predicate createArithmeticFilterPredicate(
		final CriteriaBuilder criteriaBuilder,
		final Root<?> root,
		final AbstractQuery<?> query,
		final IArithmeticFilter filter,
		final Path<?> path,
		final boolean doFilterInversion) {

		switch (filter.getOperator()) {
			case BETWEEN:
				return createBetweenPredicate(
						criteriaBuilder,
						getFilterParameterConverter(root, filter.getPropertyName()),
						filter,
						path,
						doFilterInversion);
			case GREATER:
				return createGreaterPredicate(
						criteriaBuilder,
						getFilterParameterConverter(root, filter.getPropertyName()),
						filter,
						path,
						doFilterInversion);
			case GREATER_EQUAL:
				return createGreaterEqualPredicate(
						criteriaBuilder,
						getFilterParameterConverter(root, filter.getPropertyName()),
						filter,
						path,
						doFilterInversion);
			case LESS:
				return createLessPredicate(
						criteriaBuilder,
						getFilterParameterConverter(root, filter.getPropertyName()),
						filter,
						path,
						doFilterInversion);
			case LESS_EQUAL:
				return createLessEqualPredicate(
						criteriaBuilder,
						getFilterParameterConverter(root, filter.getPropertyName()),
						filter,
						path,
						doFilterInversion);
			case EQUAL:
				return createEqualPredicate(
						criteriaBuilder,
						getFilterParameterConverter(root, filter.getPropertyName()),
						filter,
						path,
						doFilterInversion);
			case EMPTY:
				return createEmptyPredicate(criteriaBuilder, filter, path, doFilterInversion);
			case CONTAINS_ANY:
				return createContainsAnyPredicate(
						criteriaBuilder,
						getFilterParameterConverter(root, filter.getPropertyName()),
						filter,
						path,
						doFilterInversion);
			case CONTAINS_ALL:
				return createContainsAllPredicate(
						criteriaBuilder,
						getFilterParameterConverter(root, filter.getPropertyName()),
						query,
						filter,
						path,
						doFilterInversion);
			default:
				throw new IllegalArgumentException("unsupported operator: " + filter.getOperator());
		}
	}

	@SuppressWarnings("unchecked")
	private Predicate createBetweenPredicate(
		final CriteriaBuilder criteriaBuilder,
		final IConverter<Object, Object> converter,
		final IArithmeticFilter filter,
		final Path<?> path,
		final boolean doFilterInversion) {
		final Predicate result = criteriaBuilder.between(
				(Expression<Comparable<Object>>) path,
				criteriaBuilder.literal((Comparable<Object>) getParameter(filter, 0, converter)),
				criteriaBuilder.literal((Comparable<Object>) getParameter(filter, 1, converter)));
		return invertPredicateIfNeeded(criteriaBuilder, result, filter, doFilterInversion);
	}

	@SuppressWarnings("unchecked")
	private Predicate createGreaterPredicate(
		final CriteriaBuilder criteriaBuilder,
		final IConverter<Object, Object> converter,
		final IArithmeticFilter filter,
		final Path<?> path,
		final boolean doFilterInversion) {
		final Predicate result = criteriaBuilder.greaterThan(
				(Expression<Comparable<Object>>) path,
				criteriaBuilder.literal((Comparable<Object>) getParameter(filter, 0, converter)));
		return invertPredicateIfNeeded(criteriaBuilder, result, filter, doFilterInversion);
	}

	@SuppressWarnings("unchecked")
	private Predicate createGreaterEqualPredicate(
		final CriteriaBuilder criteriaBuilder,
		final IConverter<Object, Object> converter,
		final IArithmeticFilter filter,
		final Path<?> path,
		final boolean doFilterInversion) {
		final Predicate result = criteriaBuilder.greaterThanOrEqualTo(
				(Expression<Comparable<Object>>) path,
				criteriaBuilder.literal((Comparable<Object>) getParameter(filter, 0, converter)));
		return invertPredicateIfNeeded(criteriaBuilder, result, filter, doFilterInversion);
	}

	@SuppressWarnings("unchecked")
	private Predicate createLessPredicate(
		final CriteriaBuilder criteriaBuilder,
		final IConverter<Object, Object> converter,
		final IArithmeticFilter filter,
		final Path<?> path,
		final boolean doFilterInversion) {
		final Predicate result = criteriaBuilder.lessThan(
				(Expression<Comparable<Object>>) path,
				criteriaBuilder.literal((Comparable<Object>) getParameter(filter, 0, converter)));
		return invertPredicateIfNeeded(criteriaBuilder, result, filter, doFilterInversion);
	}

	@SuppressWarnings("unchecked")
	private Predicate createLessEqualPredicate(
		final CriteriaBuilder criteriaBuilder,
		final IConverter<Object, Object> converter,
		final IArithmeticFilter filter,
		final Path<?> path,
		final boolean doFilterInversion) {
		final Predicate result = criteriaBuilder.lessThanOrEqualTo(
				(Expression<Comparable<Object>>) path,
				criteriaBuilder.literal((Comparable<Object>) getParameter(filter, 0, converter)));
		return invertPredicateIfNeeded(criteriaBuilder, result, filter, doFilterInversion);
	}

	@SuppressWarnings("unchecked")
	private Predicate createEmptyPredicate(
		final CriteriaBuilder criteriaBuilder,
		final IArithmeticFilter filter,
		final Path<?> path,
		final boolean doFilterInversion) {
		final boolean isCollection = path.getModel() instanceof Attribute && ((Attribute<?, ?>) path.getModel()).isCollection();

		if (isCollection) {
			if (filter.isInverted() && doFilterInversion) {
				return criteriaBuilder.isNotEmpty((Expression<Collection<?>>) path);
			}
			else {
				return criteriaBuilder.isEmpty((Expression<Collection<?>>) path);
			}
		}
		else if (path.getJavaType() == String.class) {
			if (filter.isInverted() && doFilterInversion) {
				return criteriaBuilder.or(path.isNotNull(), criteriaBuilder.notEqual(path, ""));
			}
			else {
				return criteriaBuilder.or(path.isNull(), criteriaBuilder.equal(path, ""));
			}
		}
		else if (filter.isInverted() && doFilterInversion) {
			return path.isNotNull();
		}
		else {
			return path.isNull();
		}
	}

	private Predicate createEqualPredicate(
		final CriteriaBuilder criteriaBuilder,
		final IConverter<Object, Object> converter,
		final IArithmeticFilter filter,
		final Path<?> path,
		final boolean doFilterInversion) {
		return createEqualPredicate(criteriaBuilder, filter, path, doFilterInversion, getParameter(filter, 0, converter));
	}

	@SuppressWarnings("unchecked")
	private Predicate createEqualPredicate(
		final CriteriaBuilder criteriaBuilder,
		final IArithmeticFilter filter,
		final Path<?> path,
		final boolean doFilterInversion,
		final Object parameter) {

		if (parameter instanceof String && path.getJavaType() == String.class) {
			Expression<?> expr = path;
			String paramString = (String) parameter;
			if (caseInsensitive) {
				expr = criteriaBuilder.upper((Expression<String>) expr);
				paramString = paramString.toUpperCase();
			}
			if (paramString.contains("*") || paramString.contains("%")) {
				paramString = paramString.replace('*', '%');
				if (filter.isInverted() && doFilterInversion) {
					return criteriaBuilder.or(
							expr.isNull(),
							criteriaBuilder.equal(path, ""),
							criteriaBuilder.notLike((Expression<String>) expr, paramString));
				}
				else {
					return criteriaBuilder.like((Expression<String>) expr, paramString);
				}
			}
			else {
				if (filter.isInverted() && doFilterInversion) {
					return criteriaBuilder.or(
							expr.isNull(),
							criteriaBuilder.equal(path, ""),
							criteriaBuilder.notEqual(expr, paramString));
				}
				else {
					return criteriaBuilder.equal(expr, paramString);
				}
			}
		}
		else if (filter.isInverted() && doFilterInversion) {
			return criteriaBuilder.or(path.isNull(), criteriaBuilder.notEqual(path, parameter));
		}
		else {
			return criteriaBuilder.equal(path, parameter);
		}
	}

	@SuppressWarnings("unchecked")
	private Predicate createContainsAnyPredicate(
		final CriteriaBuilder criteriaBuilder,
		final IConverter<Object, Object> converter,
		final IArithmeticFilter filter,
		final Path<?> path,
		final boolean doFilterInversion) {

		if (path.getJavaType() == String.class) {
			boolean havePlaceholder = false;
			final Predicate[] predicates = new Predicate[filter.getParameters().length];
			final Collection<String> newParams = new HashSet<String>();
			int index = 0;
			for (Object param : filter.getParameters()) {
				param = getConvertedParameter(param, converter);
				final String paramString = (String) param;
				if (paramString.contains("*") || paramString.contains("%")) {
					havePlaceholder = true;
				}
				if (caseInsensitive) {
					newParams.add(paramString.toUpperCase());
				}
				else {
					newParams.add(paramString);
				}
				predicates[index] = createEqualPredicate(criteriaBuilder, filter, path, false, param);
				index++;
			}
			if (havePlaceholder) {//if the string have placeholders, use disjunction instead
				return invertPredicateIfNeeded(criteriaBuilder, criteriaBuilder.or(predicates), filter, doFilterInversion);
			}
			else if (caseInsensitive) {
				final Predicate predicate = criteriaBuilder.upper((Expression<String>) path).in(newParams);
				return invertPredicateIfNeeded(criteriaBuilder, predicate, filter, doFilterInversion);
			}
			else {
				return invertPredicateIfNeeded(criteriaBuilder, path.in(newParams), filter, doFilterInversion);
			}
		}
		else {
			return invertPredicateIfNeeded(criteriaBuilder, path.in(getParameters(filter, converter)), filter, doFilterInversion);
		}
	}

	@SuppressWarnings("unchecked")
	private Predicate createContainsAllPredicate(
		final CriteriaBuilder criteriaBuilder,
		final IConverter<Object, Object> converter,
		final AbstractQuery<?> query,
		final IArithmeticFilter filter,
		final Path<?> path,
		final boolean doFilterInversion) {

		final Collection<?> params = Arrays.asList(getParameters(filter, converter));
		final Collection<Object> newParams = new HashSet<Object>();
		final boolean toUpper = caseInsensitive && path.getJavaType() == String.class;
		for (final Object p : params) {
			if (p != null) {
				if (toUpper) {
					newParams.add(p.toString().toUpperCase());
				}
				else {
					newParams.add(p);
				}
			}
		}
		final Subquery<Long> subquery = query.subquery(Long.class);
		subquery.select(criteriaBuilder.count(criteriaBuilder.literal(1))).where(
				(toUpper ? criteriaBuilder.upper((Expression<String>) path) : path).in(newParams));

		final Predicate result = criteriaBuilder.ge(subquery, newParams.size());

		return invertPredicateIfNeeded(criteriaBuilder, result, filter, doFilterInversion);
	}

	private Predicate invertPredicateIfNeeded(
		final CriteriaBuilder criteriaBuilder,
		final Predicate predicate,
		final IFilter filter,
		final boolean doFilterInversion) {
		if (filter.isInverted() && doFilterInversion) {
			return criteriaBuilder.not(predicate);
		}
		else {
			return predicate;
		}
	}

	private Object[] getParameters(final IArithmeticFilter filter, final IConverter<Object, Object> converter) {
		final Object[] source = filter.getParameters();
		if (source != null && converter != null) {
			final Object[] result = new Object[source.length];
			for (int i = 0; i < source.length; i++) {
				result[i] = getConvertedParameter(source[i], converter);
			}
			return result;
		}
		else {
			return source;
		}
	}

	private Object getParameter(final IArithmeticFilter filter, final int index, final IConverter<Object, Object> converter) {
		return getConvertedParameter(filter.getParameters()[index], converter);
	}

	private Object getConvertedParameter(final Object parameter, final IConverter<Object, Object> converter) {
		if (converter != null) {
			return converter.convert(parameter);
		}
		else {
			return parameter;
		}
	}

	private Path<?> getPath(final Root<?> bean, final String propertyName) {
		try {
			return bean.get(propertyName);
		}
		catch (final IllegalArgumentException illegalArgumentException) {
			final Path<?> joinQueryPath = getJoinQueryPath(bean, propertyName);
			if (joinQueryPath != null) {
				return joinQueryPath;
			}
			else {
				throw illegalArgumentException;
			}
		}
	}

	private Path<?> getJoinQueryPath(final Root<?> bean, final String propertyName) {
		final QueryPath queryPath = getQueryPathAnno(bean, propertyName);
		if (queryPath != null) {
			Path<?> parentPath = bean;
			Type<?> type = bean.getModel();
			for (final String pathSegment : queryPath.path()) {
				final Attribute<?, ?> attribute;
				if (type != null && type instanceof ManagedType) {
					attribute = ((ManagedType<?>) type).getAttribute(pathSegment);
					type = getType(attribute);
				}
				else {
					attribute = null;
				}
				if (isJoinableType(attribute) && parentPath instanceof From) {
					final From<?, ?> from = (From<?, ?>) parentPath;
					parentPath = from.join(pathSegment, JoinType.LEFT);
				}
				else {
					parentPath = parentPath.get(pathSegment);
				}
			}
			return parentPath;
		}
		else if (IPropertyMap.class.isAssignableFrom(bean.getJavaType())) {
			final PropertyMapQueryPath propertyMapQueryPath = bean.getJavaType().getAnnotation(PropertyMapQueryPath.class);
			if (propertyMapQueryPath != null) {
				Path<?> parentPath = bean;
				Type<?> type = bean.getModel();
				for (final String pathSegment : propertyMapQueryPath.path()) {
					final Attribute<?, ?> attribute;
					if (type != null && type instanceof ManagedType) {
						attribute = ((ManagedType<?>) type).getAttribute(pathSegment);
						type = getType(attribute);
					}
					else {
						attribute = null;
					}
					if (isJoinableType(attribute) && parentPath instanceof From) {
						final From<?, ?> from = (From<?, ?>) parentPath;
						parentPath = from.join(pathSegment, JoinType.LEFT);
					}
					else {
						parentPath = parentPath.get(pathSegment);
					}
				}

				parentPath.alias(PROPERTY_MAP_JOIN_ALIAS);
				return parentPath.get(propertyMapQueryPath.valuePath());
			}
		}
		return null;
	}

	private boolean isJoinQueryPath(final Root<?> bean, final String propertyName) {
		final QueryPath queryPath = getQueryPathAnno(bean, propertyName);
		if (queryPath != null) {
			for (final String pathSegment : queryPath.path()) {
				Path<?> path = bean;
				Type<?> type = bean.getModel();
				final Attribute<?, ?> attribute;
				if (type != null && type instanceof ManagedType) {
					attribute = ((ManagedType<?>) type).getAttribute(pathSegment);
					type = getType(attribute);
				}
				else {
					attribute = null;
				}
				if (isJoinableType(attribute)) {
					return true;
				}
				else {
					path = path.get(pathSegment);
				}
			}
		}
		else {
			try {
				bean.get(propertyName);
				return false;
			}
			catch (final IllegalArgumentException illegalArgumentException) {
				if (IPropertyMap.class.isAssignableFrom(bean.getJavaType())) {
					final PropertyMapQueryPath propertyMapQueryPath = bean.getJavaType().getAnnotation(PropertyMapQueryPath.class);
					if (propertyMapQueryPath != null) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isJoinableType(final Attribute<?, ?> attribute) {
		return attribute != null && attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.BASIC;
	}

	private Type<?> getType(final Attribute<?, ?> attribute) {
		if (attribute != null) {
			if (attribute instanceof PluralAttribute) {
				final PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
				return pluralAttribute.getElementType();
			}
			else if (attribute instanceof SingularAttribute) {
				final SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>) attribute;
				return singularAttribute.getType();
			}
			else {
				return attribute.getDeclaringType();
			}
		}
		return null;
	}

	private QueryPath getQueryPathAnno(final Root<?> root, final String propertyName) {
		return getAnnotation(root, propertyName, QueryPath.class);
	}

	@SuppressWarnings("unchecked")
	private IConverter<Object, Object> getFilterParameterConverter(final Root<?> root, final String propertyName) {
		final FilterParameterConverter converterAnno = getFilterParameterConverterAnno(root, propertyName);
		if (converterAnno != null) {
			try {
				return converterAnno.value().newInstance();
			}
			catch (final Exception e) {
				throw new RuntimeException("Can not create converter defined in FilterParameterConverter annotation", e);
			}
		}
		else {
			return null;
		}
	}

	private FilterParameterConverter getFilterParameterConverterAnno(final Root<?> root, final String propertyName) {
		return getAnnotation(root, propertyName, FilterParameterConverter.class);
	}

	private <ANNOTATION_TYPE extends Annotation> ANNOTATION_TYPE getAnnotation(
		final Root<?> root,
		final String propertyName,
		final Class<ANNOTATION_TYPE> annotation) {
		final Class<?> beanClass = root.getJavaType();
		try {
			final PropertyDescriptor descriptor = new PropertyDescriptor(propertyName, beanClass, "is"
				+ propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH)
				+ propertyName.substring(1), null);
			final Method readMethod = descriptor.getReadMethod();
			return readMethod.getAnnotation(annotation);
		}
		catch (final Exception e) {
			return null;
		}
	}

}
