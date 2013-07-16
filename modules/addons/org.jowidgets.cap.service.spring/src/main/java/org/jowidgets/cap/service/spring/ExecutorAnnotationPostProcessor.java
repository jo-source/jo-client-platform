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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.service.IExecutorService;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.annotation.DefaultExecutableChecker;
import org.jowidgets.cap.service.api.annotation.Executor;
import org.jowidgets.cap.service.api.annotation.ExecutorBean;
import org.jowidgets.cap.service.api.annotation.Parameter;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.executor.IBeanExecutor;
import org.jowidgets.cap.service.api.executor.IBeanListExecutor;
import org.jowidgets.cap.service.api.executor.IExecutorServiceBuilder;
import org.jowidgets.cap.service.impl.DefaultCapServiceToolkit;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.service.tools.ServiceId;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.maybe.Nothing;
import org.jowidgets.util.maybe.Some;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

public final class ExecutorAnnotationPostProcessor implements BeanPostProcessor, ApplicationContextAware {

	private final ExpressionParser expressionParser = new SpelExpressionParser();
	private final ConcurrentMap<Parameter, Expression> expressions = new ConcurrentHashMap<Parameter, Expression>();
	private IBeanAccessProvider beanAccessProvider;
	private PlatformTransactionManager transactionManager;
	private ListableBeanFactory beanFactory;
	private boolean local;

	@Required
	public void setBeanAccessProvider(final IBeanAccessProvider beanAccessProvider) {
		this.beanAccessProvider = beanAccessProvider;
	}

	public void setTransactionManager(final PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName) {
		try {
			final ExecutorBean beanAnnotation = beanFactory.findAnnotationOnBean(beanName, ExecutorBean.class);
			if (beanAnnotation != null) {
				final IBeanAccess<?> beanAccess = beanAccessProvider.getBeanAccess(beanAnnotation.value());
				final List<String> propertyNames = new BeanTypeUtil(beanAccess.getBeanType()).getPropertyNames();

				final Set<Method> methods = getExecutorMethods(bean);
				for (final Method method : methods) {
					final Object proxy = createExecutorProxy(beanFactory, beanName, method);
					final IExecutorServiceBuilder<IBean, Object> builder = CapServiceToolkit.executorServiceBuilder(beanAccess);
					if (proxy instanceof IBeanExecutor) {
						builder.setExecutor((IBeanExecutor<IBean, Object>) proxy);
					}
					else {
						builder.setExecutor((IBeanListExecutor<IBean, Object>) proxy);
					}
					builder.setBeanDtoFactory(propertyNames);

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

					IExecutorService<Object> executorService = builder.build();
					if (transactionManager != null) {
						executorService = new TransactionProxyFactory(transactionManager).createProxy(executorService, "execute");
					}
					final IServiceId<IExecutorService<Object>> serviceId = new ServiceId<IExecutorService<Object>>(
						executorAnnotation.id(),
						IExecutorService.class);

					if (isLocal()) {
						final DefaultCapServiceToolkit defaultCapServiceToolkit = new DefaultCapServiceToolkit();
						final IServicesDecoratorProvider asyncDecoratorProvider = defaultCapServiceToolkit.serviceDecoratorProvider().asyncDecoratorProvider();
						final IDecorator<IExecutorService<Object>> decorator = asyncDecoratorProvider.getDecorator(serviceId);
						executorService = decorator.decorate(executorService);
					}

					SpringServiceProvider.getInstance().addService(serviceId, executorService);
				}
			}
		}
		catch (final NoSuchBeanDefinitionException e) {
		}
		return bean;
	}

	private Object createExecutorProxy(final BeanFactory beanFactory, final String beanName, final Method method) {
		final boolean voidMethod = method.getReturnType() == void.class;

		Boolean singleExecutor = null;
		Integer dataArgPosition = null;
		Integer callbackArgPosition = null;
		final List<Integer> paramArgPositions = new LinkedList<Integer>();
		for (int i = 0; i < method.getParameterTypes().length; i++) {
			final Class<?> parameterType = method.getParameterTypes()[i];

			if (IBean.class.isAssignableFrom(parameterType) && singleExecutor == null) {
				singleExecutor = true;
				dataArgPosition = i;
				continue;
			}

			if (List.class.isAssignableFrom(parameterType) && singleExecutor == null) {
				final Type genericParameterType = method.getGenericParameterTypes()[i];
				if (genericParameterType instanceof ParameterizedType) {
					final ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
					if (IBean.class.isAssignableFrom((Class<?>) parameterizedType.getActualTypeArguments()[0])) {
						singleExecutor = false;
						dataArgPosition = i;
						continue;
					}
				}
			}

			if (parameterType == IExecutionCallback.class) {
				callbackArgPosition = i;
				continue;
			}

			paramArgPositions.add(i);
		}

		final Object[] args = new Object[method.getParameterTypes().length];
		final Integer finalDataArgPosition = dataArgPosition;
		final Integer finalCallbackArgPosition = callbackArgPosition;

		if (singleExecutor != null && singleExecutor) {
			return new IBeanExecutor<IBean, Object>() {
				@Override
				public IBean execute(final IBean data, final Object parameter, final IExecutionCallback executionCallback) {
					if (finalDataArgPosition != null) {
						args[finalDataArgPosition] = data;
					}
					if (finalCallbackArgPosition != null) {
						args[finalCallbackArgPosition] = executionCallback;
					}
					if (paramArgPositions.size() == 1) {
						args[paramArgPositions.get(0)] = getSingleParameterValue(parameter, method, paramArgPositions.get(0));
					}
					else if (!paramArgPositions.isEmpty()) {
						for (int i = 0; i < paramArgPositions.size(); i++) {
							args[paramArgPositions.get(i)] = getMultiParameterValue(
									parameter,
									method,
									paramArgPositions.get(i),
									i);
						}
					}

					final Object result = ReflectionUtils.invokeMethod(method, beanFactory.getBean(beanName), args);
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
				public List<IBean> execute(List<IBean> data, final Object parameter, final IExecutionCallback executionCallback) {

					if (voidMethod) {
						data = new ArrayList<IBean>(data);
					}

					if (finalDataArgPosition != null) {
						args[finalDataArgPosition] = data;
					}
					if (finalCallbackArgPosition != null) {
						args[finalCallbackArgPosition] = executionCallback;
					}
					if (paramArgPositions.size() == 1) {
						args[paramArgPositions.get(0)] = getSingleParameterValue(parameter, method, paramArgPositions.get(0));
					}
					else if (!paramArgPositions.isEmpty()) {
						for (int i = 0; i < paramArgPositions.size(); i++) {
							args[paramArgPositions.get(i)] = getMultiParameterValue(
									parameter,
									method,
									paramArgPositions.get(i),
									i);
						}
					}

					final Object result = ReflectionUtils.invokeMethod(method, beanFactory.getBean(beanName), args);
					if (voidMethod) {
						return data;
					}
					if (result instanceof IBean) {
						return Collections.singletonList((IBean) result);
					}
					return (List<IBean>) result;
				}
			};
		}
	}

	private Object getSingleParameterValue(final Object parameter, final Method method, final int argIndex) {
		final IMaybe<Object> value = evaluateParameterExpression(parameter, method, argIndex);
		if (value.isSomething()) {
			return value.getValue();
		}
		return parameter;
	}

	private Object getMultiParameterValue(final Object parameter, final Method method, final int argIndex, final int paramIndex) {
		final IMaybe<Object> value = evaluateParameterExpression(parameter, method, argIndex);
		if (value.isSomething()) {
			return value.getValue();
		}
		final Object[] parameterArray = (Object[]) parameter;
		return parameterArray[paramIndex];
	}

	IMaybe<Object> evaluateParameterExpression(final Object parameter, final Method method, final int argIndex) {
		final Annotation[] annotations = method.getParameterAnnotations()[argIndex];
		for (final Annotation annotation : annotations) {
			if (annotation instanceof Parameter) {
				final Parameter paramAnno = (Parameter) annotation;
				if (!expressions.containsKey(paramAnno)) {
					expressions.putIfAbsent(paramAnno, expressionParser.parseExpression(paramAnno.value()));
				}
				final Expression expression = expressions.get(paramAnno);
				return new Some<Object>(expression.getValue(parameter, method.getParameterTypes()[argIndex]));
			}
		}
		return Nothing.getInstance();
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

	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName) {
		return bean;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) {
		beanFactory = applicationContext;
	}

	public boolean isLocal() {
		return local;
	}

	public void setLocal(final boolean local) {
		this.local = local;
	}

}
