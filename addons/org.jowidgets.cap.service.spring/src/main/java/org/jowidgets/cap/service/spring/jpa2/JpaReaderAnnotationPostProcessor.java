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

package org.jowidgets.cap.service.spring.jpa2;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.service.api.annotation.Reader;
import org.jowidgets.cap.service.api.annotation.ReaderBean;
import org.jowidgets.cap.service.jpa.api.JpaServiceToolkit;
import org.jowidgets.cap.service.jpa.api.query.ICriteriaQueryCreatorBuilder;
import org.jowidgets.cap.service.jpa.api.query.ICustomFilterPredicateCreator;
import org.jowidgets.cap.service.jpa.api.query.IPredicateCreator;
import org.jowidgets.cap.service.jpa.api.query.IQueryCreator;
import org.jowidgets.cap.service.jpa.api.query.JpaQueryToolkit;
import org.jowidgets.cap.service.spring.BeanTypeUtil;
import org.jowidgets.cap.service.spring.SpringServiceProvider;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.tools.ServiceId;
import org.jowidgets.util.Assert;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

public final class JpaReaderAnnotationPostProcessor implements BeanPostProcessor, ApplicationContextAware {

	private ListableBeanFactory beanFactory;
	private Map<String, ? extends ICustomFilterPredicateCreator<?>> customFilterPredicateCreators = Collections.emptyMap();

	public void setCustomFilterPredicateCreators(
		final Map<String, ? extends ICustomFilterPredicateCreator<?>> customFilterPredicateCreators) {
		Assert.paramNotNull(customFilterPredicateCreators, "customFilterPredicateCreators");
		this.customFilterPredicateCreators = customFilterPredicateCreators;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName) {
		try {
			final ReaderBean beanAnnotation = beanFactory.findAnnotationOnBean(beanName, ReaderBean.class);
			if (beanAnnotation != null) {
				final Class<? extends IBean> beanType = beanAnnotation.type();
				final List<String> propertyNames = new BeanTypeUtil(beanType).getPropertyNames();

				final Set<Method> methods = getReaderMethods(bean);
				for (final Method method : methods) {
					final IPredicateCreator<Object> predicateCreator = createPredicateCreator(beanFactory, beanName, method);

					final ICriteriaQueryCreatorBuilder<Object> queryCreatorBuilder = JpaQueryToolkit.criteriaQueryCreatorBuilder(
							beanType).setCaseSensitve(!beanAnnotation.caseInsensitive()).setParentPropertyName(
							beanAnnotation.parentPropertyName());
					if (predicateCreator != null) {
						queryCreatorBuilder.addPredicateCreator(predicateCreator);
					}
					for (final Map.Entry<String, ? extends ICustomFilterPredicateCreator<?>> entry : customFilterPredicateCreators.entrySet()) {
						final String filterName = entry.getKey();
						@SuppressWarnings("unchecked")
						final ICustomFilterPredicateCreator<Object> customFilterPredicateCreator = (ICustomFilterPredicateCreator<Object>) entry.getValue();
						queryCreatorBuilder.addCustomFilterPredicateCreator(filterName, customFilterPredicateCreator);
					}
					final IQueryCreator<Object> queryCreator = queryCreatorBuilder.build();

					final IReaderService<Object> readerService = JpaServiceToolkit.serviceFactory().readerService(
							beanType,
							queryCreator,
							propertyNames);

					final Reader executorAnnotation = method.getAnnotation(Reader.class);
					final IServiceId<IReaderService<Void>> serviceId = new ServiceId<IReaderService<Void>>(
						executorAnnotation.value(),
						IReaderService.class);
					SpringServiceProvider.getInstance().addService(serviceId, readerService);
				}
			}
		}
		catch (final NoSuchBeanDefinitionException e) {
		}
		return bean;
	}

	// TODO HRW support parentBeanKeys, parentBeanIds and parameter in method argument list
	private IPredicateCreator<Object> createPredicateCreator(
		final BeanFactory beanFactory,
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
		return new IPredicateCreator<Object>() {
			@Override
			public Predicate createPredicate(
				final CriteriaBuilder criteriaBuilder,
				final Root<?> bean,
				final CriteriaQuery<?> query,
				final List<IBeanKey> parentBeanKeys,
				final List<Object> parentBeanIds,
				final Object parameter) {

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

	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName) {
		return bean;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) {
		beanFactory = applicationContext;
	}

}
