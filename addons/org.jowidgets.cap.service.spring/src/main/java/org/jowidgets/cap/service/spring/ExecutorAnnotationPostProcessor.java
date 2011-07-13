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

package org.jowidgets.cap.service.spring;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.service.IExecutorService;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.annotation.DefaultExecutableChecker;
import org.jowidgets.cap.service.api.annotation.Executor;
import org.jowidgets.cap.service.api.annotation.ExecutorBean;
import org.jowidgets.cap.service.api.annotation.Service;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.executor.IBeanExecutor;
import org.jowidgets.cap.service.api.executor.IBeanListExecutor;
import org.jowidgets.cap.service.api.executor.IExecutorServiceBuilder;
import org.jowidgets.util.Assert;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

public final class ExecutorAnnotationPostProcessor implements BeanFactoryPostProcessor {

	private final IBeanAccess<?> beanAccess;
	private final List<String> propertyNames;

	public ExecutorAnnotationPostProcessor(final IBeanAccess<?> beanAccess) {
		Assert.paramNotNull(beanAccess, "beanAccess");
		this.beanAccess = beanAccess;
		propertyNames = getPropertyNames(beanAccess.getBeanType());
	}

	private List<String> getPropertyNames(final Class<? extends IBean> beanType) {
		final Class<? extends IBean> beanInterface = getBeanInterface(beanType);
		final List<String> names = new LinkedList<String>();
		for (final PropertyDescriptor descr : BeanUtils.getPropertyDescriptors(beanInterface)) {
			names.add(descr.getName());
		}
		return names;
	}

	@SuppressWarnings("unchecked")
	private Class<? extends IBean> getBeanInterface(final Class<? extends IBean> beanType) {
		if (beanType.isInterface()) {
			throw new IllegalArgumentException(beanType.getName() + " must not be an interface");
		}
		for (final Class<?> clazz : beanType.getInterfaces()) {
			if (IBean.class.isAssignableFrom(clazz)) {
				return (Class<? extends IBean>) clazz;
			}
		}
		final Class<?> clazz = beanType.getSuperclass();
		if (clazz != null && IBean.class.isAssignableFrom(clazz)) {
			return getBeanInterface((Class<? extends IBean>) clazz);
		}
		return null;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) {
		final Map<String, Object> beans = beanFactory.getBeansWithAnnotation(ExecutorBean.class);
		for (final Entry<String, Object> entry : beans.entrySet()) {
			int i = 0;
			final String beanName = entry.getKey();
			final Object bean = entry.getValue();
			final Set<Method> methods = getExecutorMethods(bean);
			for (final Method method : methods) {
				final Object proxy = createExecutorProxy(bean, method);
				final IExecutorServiceBuilder builder = CapServiceToolkit.executorServiceBuilder(beanAccess);
				if (proxy instanceof IBeanExecutor) {
					builder.setExecutor((IBeanExecutor) proxy);
				}
				else {
					builder.setExecutor((IBeanListExecutor) proxy);
				}
				builder.setPropertyNames(propertyNames);

				final Executor executorAnnotation = method.getAnnotation(Executor.class);
				builder.setAllowDeletedBeans(executorAnnotation.allowDeletedBeans());
				builder.setAllowStaleBeans(executorAnnotation.allowStaleBeans());
				if (executorAnnotation.checker() != DefaultExecutableChecker.class) {
					try {
						builder.setExecutableChecker(executorAnnotation.checker().newInstance());
					}
					catch (final InstantiationException e) {
						throw new RuntimeException(e);
					}
					catch (final IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}

				final IExecutorService executorService = builder.build();

				final Service serviceAnnotation = method.getAnnotation(Service.class);
				final String serviceId;
				if (serviceAnnotation != null) {
					serviceId = serviceAnnotation.value();
				}
				else {
					serviceId = null;
				}

				final BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
				final BeanDefinition beanDefinition = new RootBeanDefinition(TransactionalExecutorService.class);
				beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, executorService);
				if (serviceId != null) {
					beanDefinition.setAttribute("serviceId", serviceId);
				}
				beanDefinition.setDependsOn(new String[] {beanName});
				registry.registerBeanDefinition(beanName + "." + i++, beanDefinition);
			}
		}
	}

	private Object createExecutorProxy(final Object bean, final Method method) {
		final boolean voidMethod = method.getReturnType() == void.class;
		// TODO determine values
		final boolean singleExecutor = true;
		final Integer dataArgPosition = null;
		final Integer callbackArgPosition = null;
		final List<Integer> paramArgPositions = Collections.emptyList();

		final Object[] args = new Object[method.getParameterTypes().length];

		if (singleExecutor) {
			return new IBeanExecutor<IBean, Object>() {
				@Override
				public IBean execute(final IBean data, final Object parameter, final IExecutionCallback executionCallback) {
					if (dataArgPosition != null) {
						args[dataArgPosition] = data;
					}
					if (callbackArgPosition != null) {
						args[callbackArgPosition] = executionCallback;
					}
					if (paramArgPositions.size() == 1) {
						args[paramArgPositions.get(0)] = parameter;
					}
					else if (!paramArgPositions.isEmpty()) {
						final Object[] parameterArray = (Object[]) parameter;
						for (int i = 0; i < paramArgPositions.size(); i++) {
							args[paramArgPositions.get(i)] = parameterArray[i];
						}
					}

					final Object result = ReflectionUtils.invokeMethod(method, bean, args);
					if (voidMethod) {
						return data;
					}
					return (IBean) result;
				}
			};
		}

		else {
			return new IBeanListExecutor<IBean, Object>() {
				@SuppressWarnings("unchecked")
				@Override
				public List<IBean> execute(
					List<? extends IBean> data,
					final Object parameter,
					final IExecutionCallback executionCallback) {

					if (voidMethod) {
						data = new ArrayList<IBean>(data);
					}

					if (dataArgPosition != null) {
						args[dataArgPosition] = data;
					}
					if (callbackArgPosition != null) {
						args[callbackArgPosition] = executionCallback;
					}
					if (paramArgPositions.size() == 1) {
						args[paramArgPositions.get(0)] = parameter;
					}
					else if (!paramArgPositions.isEmpty()) {
						final Object[] parameterArray = (Object[]) parameter;
						for (int i = 0; i < paramArgPositions.size(); i++) {
							args[paramArgPositions.get(i)] = parameterArray[i];
						}
					}

					final Object result = ReflectionUtils.invokeMethod(method, bean, args);
					if (voidMethod) {
						return (List<IBean>) data;
					}
					return (List<IBean>) result;
				}
			};
		}
	}

	private Set<Method> getExecutorMethods(final Object bean) {
		final Set<Method> methods = new HashSet<Method>();
		ReflectionUtils.doWithMethods(bean.getClass(), new MethodCallback() {
			@Override
			public void doWith(final Method method) throws IllegalAccessException {
				if (method.isAnnotationPresent(Executor.class)) {
					methods.add(method);
				}
			}
		});
		return methods;
	}

}
