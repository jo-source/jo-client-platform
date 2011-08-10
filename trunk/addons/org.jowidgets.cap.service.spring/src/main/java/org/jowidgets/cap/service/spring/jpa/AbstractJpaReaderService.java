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

package org.jowidgets.cap.service.spring.jpa;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.service.impl.jpa.JpaReaderService;
import org.jowidgets.cap.service.impl.jpa.jpql.CriteriaQueryCreator;
import org.jowidgets.cap.service.impl.jpa.jpql.ICustomFilterPredicateCreator;
import org.jowidgets.cap.service.impl.jpa.jpql.IPredicateCreator;
import org.jowidgets.cap.service.spring.BeanTypeUtil;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractJpaReaderService implements IReaderService<Void>, InitializingBean {

	private final CriteriaQueryCreator queryCreator;

	@PersistenceContext
	private EntityManager entityManager;
	private JpaReaderService<Void> jpaReaderService;

	protected AbstractJpaReaderService(final Class<? extends IBean> beanType) {
		this(beanType, null, null);
	}

	protected AbstractJpaReaderService(final Class<? extends IBean> beanType, final String parentPropertyName) {
		this(beanType, parentPropertyName, null);
	}

	protected AbstractJpaReaderService(final Class<? extends IBean> beanType, final boolean caseInsensitive) {
		this(beanType, null, caseInsensitive);
	}

	protected AbstractJpaReaderService(
		final Class<? extends IBean> beanType,
		final String parentPropertyName,
		final Boolean caseInsensitve) {
		queryCreator = new CriteriaQueryCreator(beanType);
		if (parentPropertyName != null) {
			queryCreator.setParentPropertyName(parentPropertyName);
		}
		if (caseInsensitve != null) {
			queryCreator.setCaseInsensitve(caseInsensitve);
		}
		queryCreator.setPredicateCreator(new IPredicateCreator() {
			@Override
			public Predicate createPredicate(
				final CriteriaBuilder criteriaBuilder,
				final Root<?> bean,
				final CriteriaQuery<?> query) {
				return buildPredicate(criteriaBuilder, bean, query);
			}
		});
	}

	public void setCustomFilterPredicateCreators(
		final Map<String, ? extends ICustomFilterPredicateCreator> customFilterPredicateCreators) {
		queryCreator.setCustomFilterPredicateCreators(customFilterPredicateCreators);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		final BeanTypeUtil beanTypeUtil = new BeanTypeUtil(queryCreator.getPersistenceClass());
		jpaReaderService = new JpaReaderService<Void>(queryCreator, beanTypeUtil.getPropertyNames());
		jpaReaderService.setEntityManager(entityManager);
	}

	protected Predicate buildPredicate(final CriteriaBuilder criteriaBuilder, final Root<?> bean, final CriteriaQuery<?> query) {
		return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
	}

	@Override
	public void read(
		final IResultCallback<List<IBeanDto>> result,
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final List<? extends ISort> sorting,
		final int firstRow,
		final int maxRows,
		final Void parameter,
		final IExecutionCallback executionCallback) {
		jpaReaderService.read(result, parentBeanKeys, filter, sorting, firstRow, maxRows, parameter, executionCallback);
	}

	@Override
	public void count(
		final IResultCallback<Integer> result,
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final Void parameter,
		final IExecutionCallback executionCallback) {
		jpaReaderService.count(result, parentBeanKeys, filter, parameter, executionCallback);
	}

}
