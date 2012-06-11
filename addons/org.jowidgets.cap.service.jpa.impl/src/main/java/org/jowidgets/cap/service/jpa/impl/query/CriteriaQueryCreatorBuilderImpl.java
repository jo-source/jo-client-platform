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

package org.jowidgets.cap.service.jpa.impl.query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Predicate.BooleanOperator;
import javax.persistence.criteria.Root;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.service.jpa.api.query.ICriteriaQueryCreatorBuilder;
import org.jowidgets.cap.service.jpa.api.query.ICustomFilterPredicateCreator;
import org.jowidgets.cap.service.jpa.api.query.IPredicateCreator;
import org.jowidgets.cap.service.jpa.api.query.IPropertyFilterPredicateCreator;
import org.jowidgets.cap.service.jpa.api.query.IQueryCreator;
import org.jowidgets.util.Assert;

final class CriteriaQueryCreatorBuilderImpl<PARAMETER_TYPE> implements ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> {

	private final Class<? extends IBean> beanType;
	private final List<IPredicateCreator<PARAMETER_TYPE>> predicateCreators;
	private final List<IFilter> filters;
	private final Map<String, ICustomFilterPredicateCreator<PARAMETER_TYPE>> customFilterPredicateCreators;
	private final Map<String, IPropertyFilterPredicateCreator<PARAMETER_TYPE>> propertyFilterPredicateCreators;

	private final List<IPredicateCreator<PARAMETER_TYPE>> parentLinkPredicateCreators;
	private final List<IPredicateCreator<PARAMETER_TYPE>> parentUnlinkPredicateCreators;

	private boolean caseSensitive;

	CriteriaQueryCreatorBuilderImpl(final Class<? extends IBean> beanType) {
		Assert.paramNotNull(beanType, "beanType");
		this.beanType = beanType;
		this.caseSensitive = false;
		this.predicateCreators = new LinkedList<IPredicateCreator<PARAMETER_TYPE>>();
		this.parentLinkPredicateCreators = new LinkedList<IPredicateCreator<PARAMETER_TYPE>>();
		this.parentUnlinkPredicateCreators = new LinkedList<IPredicateCreator<PARAMETER_TYPE>>();
		this.filters = new LinkedList<IFilter>();
		this.customFilterPredicateCreators = new HashMap<String, ICustomFilterPredicateCreator<PARAMETER_TYPE>>();
		this.propertyFilterPredicateCreators = new HashMap<String, IPropertyFilterPredicateCreator<PARAMETER_TYPE>>();
	}

	@Override
	public ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> setParentPropertyName(final String parentPropertyName) {
		Assert.paramNotEmpty(parentPropertyName, "parentPropertyName");
		return setParentPropertyPath(parentPropertyName);
	}

	@Override
	public ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> setParentPropertyPath(final String... parentPropertyPath) {
		return setParentPropertyPath(true, parentPropertyPath);
	}

	@Override
	public ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> setParentPropertyName(
		final boolean linked,
		final String parentPropertyName) {
		Assert.paramNotEmpty(parentPropertyName, "parentPropertyName");
		return setParentPropertyPath(linked, parentPropertyName);
	}

	@Override
	public ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> setParentPropertyPath(
		final boolean linked,
		final String... parentPropertyPath) {
		Assert.paramNotNull(parentPropertyPath, "parentPropertyPath");
		parentLinkPredicateCreators.clear();
		parentUnlinkPredicateCreators.clear();
		return addParentPropertyPath(linked, parentPropertyPath);
	}

	@Override
	public ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> addParentPropertyPath(
		final boolean linked,
		final String... parentPropertyPath) {
		Assert.paramNotNull(parentPropertyPath, "parentPropertyPath");
		final ParentLinkPredicateCreator<PARAMETER_TYPE> predicateCreator;
		predicateCreator = new ParentLinkPredicateCreator<PARAMETER_TYPE>(linked, Arrays.asList(parentPropertyPath));
		if (linked) {
			parentLinkPredicateCreators.add(predicateCreator);
		}
		else {
			parentUnlinkPredicateCreators.add(predicateCreator);
		}
		return this;
	}

	@Override
	public ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> setCaseSensitve(final boolean caseSensitve) {
		this.caseSensitive = caseSensitve;
		return this;
	}

	@Override
	public ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> addPredicateCreator(
		final IPredicateCreator<PARAMETER_TYPE> predicateCreator) {
		Assert.paramNotNull(predicateCreator, "predicateCreator");
		predicateCreators.add(predicateCreator);
		return this;
	}

	@Override
	public ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> addFilter(final IFilter filter) {
		Assert.paramNotNull(filter, "filter");
		filters.add(filter);
		return this;
	}

	@Override
	public ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> addPropertyFilterPredicateCreator(
		final String propertyName,
		final IPropertyFilterPredicateCreator<PARAMETER_TYPE> predicateCreator) {
		Assert.paramNotEmpty(propertyName, "propertyName");
		Assert.paramNotNull(predicateCreator, "predicateCreator");
		propertyFilterPredicateCreators.put(propertyName, predicateCreator);
		return this;
	}

	@Override
	public ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> addCustomFilterPredicateCreator(
		final String filterType,
		final ICustomFilterPredicateCreator<PARAMETER_TYPE> customFilterPredicateCreator) {
		Assert.paramNotEmpty(filterType, "filterType");
		Assert.paramNotNull(customFilterPredicateCreators, "customFilterPredicateCreators");
		customFilterPredicateCreators.put(filterType, customFilterPredicateCreator);
		return this;
	}

	@Override
	public IQueryCreator<PARAMETER_TYPE> build() {

		final List<IPredicateCreator<PARAMETER_TYPE>> predicateCreatorsComposite;
		predicateCreatorsComposite = new LinkedList<IPredicateCreator<PARAMETER_TYPE>>(predicateCreators);

		predicateCreatorsComposite.addAll(getParentPathPredicateCreators(parentLinkPredicateCreators, BooleanOperator.OR));
		predicateCreatorsComposite.addAll(getParentPathPredicateCreators(parentUnlinkPredicateCreators, BooleanOperator.AND));

		return new CriteriaQueryCreator<PARAMETER_TYPE>(
			beanType,
			caseSensitive,
			predicateCreatorsComposite,
			filters,
			customFilterPredicateCreators,
			propertyFilterPredicateCreators);
	}

	private List<IPredicateCreator<PARAMETER_TYPE>> getParentPathPredicateCreators(
		final List<IPredicateCreator<PARAMETER_TYPE>> predicateCreators,
		final BooleanOperator operator) {

		final List<IPredicateCreator<PARAMETER_TYPE>> result = new LinkedList<IPredicateCreator<PARAMETER_TYPE>>();
		if (predicateCreators.size() > 0) {
			result.add(new IPredicateCreator<PARAMETER_TYPE>() {
				@Override
				public Predicate createPredicate(
					final CriteriaBuilder criteriaBuilder,
					final Root<?> bean,
					final CriteriaQuery<?> query,
					final List<IBeanKey> parentBeanKeys,
					final List<Object> parentBeanIds,
					final PARAMETER_TYPE parameter) {

					final Predicate[] predicates = new Predicate[predicateCreators.size()];
					int i = 0;
					for (final IPredicateCreator<PARAMETER_TYPE> parentLinkPredicateCreator : parentLinkPredicateCreators) {
						predicates[i] = parentLinkPredicateCreator.createPredicate(
								criteriaBuilder,
								bean,
								query,
								parentBeanKeys,
								parentBeanIds,
								parameter);
						i++;
					}
					if (predicates.length == 1) {
						return predicates[0];
					}
					else if (operator == BooleanOperator.AND) {
						return criteriaBuilder.and(predicates);
					}
					else {
						return criteriaBuilder.or(predicates);
					}
				}
			});

		}
		return result;
	}
}
