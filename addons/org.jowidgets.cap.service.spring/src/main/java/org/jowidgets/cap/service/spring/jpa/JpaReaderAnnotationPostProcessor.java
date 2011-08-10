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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.service.api.annotation.ExecutorBean;
import org.jowidgets.cap.service.api.annotation.Reader;
import org.jowidgets.cap.service.api.annotation.ReaderBean;
import org.jowidgets.cap.service.impl.jpa.JpaReaderService;
import org.jowidgets.cap.service.impl.jpa.jpql.CriteriaQueryCreator;
import org.jowidgets.cap.service.impl.jpa.jpql.ICustomFilterPredicateCreator;
import org.jowidgets.cap.service.impl.jpa.jpql.IPredicateCreator;
import org.jowidgets.cap.service.spring.BeanTypeUtil;
import org.jowidgets.cap.service.spring.SpringServiceProvider;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.tools.ServiceId;
import org.jowidgets.util.Assert;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

public final class JpaReaderAnnotationPostProcessor implements BeanFactoryPostProcessor {

	@PersistenceContext
	private EntityManager entityManager;
	private Map<String, ? extends ICustomFilterPredicateCreator> customFilterPredicateCreators = Collections.emptyMap();

	public void setCustomFilterPredicateCreators(
		final Map<String, ? extends ICustomFilterPredicateCreator> customFilterPredicateCreators) {
		Assert.paramNotNull(customFilterPredicateCreators, "customFilterPredicateCreators");
		this.customFilterPredicateCreators = customFilterPredicateCreators;
	}

	@Override
	public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) {
		final Map<String, Object> beans = beanFactory.getBeansWithAnnotation(ExecutorBean.class);
		for (final Entry<String, Object> entry : beans.entrySet()) {
			final String beanName = entry.getKey();
			final Object bean = entry.getValue();
			final ReaderBean beanAnnotation = beanFactory.findAnnotationOnBean(beanName, ReaderBean.class);
			final Class<? extends IBean> beanType = beanAnnotation.type();
			final List<String> propertyNames = new BeanTypeUtil(beanType).getPropertyNames();

			final Set<Method> methods = getReaderMethods(bean);
			for (final Method method : methods) {
				final IPredicateCreator predicateCreator = createPredicateCreator(beanFactory, beanName, method);
				final CriteriaQueryCreator queryCreator = new CriteriaQueryCreator(beanType);
				queryCreator.setPredicateCreator(predicateCreator);
				queryCreator.setCustomFilterPredicateCreators(customFilterPredicateCreators);
				queryCreator.setCaseInsensitve(beanAnnotation.caseInsensitive());
				queryCreator.setParentPropertyName(beanAnnotation.parentPropertyName());

				final JpaReaderService<Void> readerService = new JpaReaderService<Void>(queryCreator, propertyNames);
				readerService.setEntityManager(entityManager);

				final Reader executorAnnotation = method.getAnnotation(Reader.class);
				final IServiceId<IReaderService<Void>> serviceId = new ServiceId<IReaderService<Void>>(
					executorAnnotation.value(),
					IReaderService.class);
				SpringServiceProvider.getInstance().addService(serviceId, readerService);
			}
		}
	}

	private IPredicateCreator createPredicateCreator(
		final ConfigurableListableBeanFactory beanFactory,
		final String beanName,
		final Method method) {

		if (method.getReturnType() != Predicate.class) {
			return null;
		}

		Integer criteriaBuilderArgPos = null;
		Integer beanArgPos = null;
		Integer queryArgPos = null;

		for (int i = 0; i < method.getParameterTypes().length; i++) {
			final Class<?> parameterType = method.getParameterTypes()[i];
			if (parameterType == CriteriaBuilder.class) {
				criteriaBuilderArgPos = i;
				continue;
			}
			if (parameterType == Root.class) {
				beanArgPos = i;
				continue;
			}
			if (parameterType == CriteriaQuery.class) {
				queryArgPos = i;
			}
		}

		final Object[] args = new Object[method.getParameterTypes().length];
		final Integer finalCriteriaBuilderArgPos = criteriaBuilderArgPos;
		final Integer finalBeanArgPos = beanArgPos;
		final Integer finalQueryArgPos = queryArgPos;
		return new IPredicateCreator() {
			@Override
			public Predicate createPredicate(
				final CriteriaBuilder criteriaBuilder,
				final Root<?> bean,
				final CriteriaQuery<?> query) {

				if (finalCriteriaBuilderArgPos != null) {
					args[finalCriteriaBuilderArgPos] = criteriaBuilder;
				}
				if (finalBeanArgPos != null) {
					args[finalBeanArgPos] = bean;
				}
				if (finalQueryArgPos != null) {
					args[finalQueryArgPos] = query;
				}

				return (Predicate) ReflectionUtils.invokeMethod(method, beanFactory.getBean(beanName), args);
			}
		};
	}

	private Set<Method> getReaderMethods(final Object bean) {
		final Set<Method> methods = new HashSet<Method>();
		ReflectionUtils.doWithMethods(bean.getClass(), new MethodCallback() {
			@Override
			public void doWith(final Method method) throws IllegalAccessException {
				if (method.isAnnotationPresent(Reader.class)) {
					methods.add(method);
				}
			}
		});
		return methods;
	}

}
