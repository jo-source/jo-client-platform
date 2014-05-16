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

package org.jowidgets.cap.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.exception.BeanValidationException;
import org.jowidgets.cap.common.api.exception.DeletedBeanException;
import org.jowidgets.cap.common.api.exception.ExecutableCheckException;
import org.jowidgets.cap.common.api.exception.StaleBeanException;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutableState;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.validation.IBeanValidationResult;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.common.tools.validation.BeanValidationResultUtil;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.adapter.ISyncExecutorService;
import org.jowidgets.cap.service.api.bean.BeanUpdateInterceptor;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanUpdateInterceptor;
import org.jowidgets.cap.service.api.executor.IBeanExecutor;
import org.jowidgets.cap.service.api.executor.IBeanListExecutor;
import org.jowidgets.cap.service.api.plugin.IBeanUpdateInterceptorPlugin;
import org.jowidgets.cap.service.tools.bean.BeanDtoFactoryHelper;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.plugin.api.PluginToolkit;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.reflection.AnnotationCache;

public final class SyncExecutorServiceImpl<BEAN_TYPE, PARAM_TYPE> implements ISyncExecutorService<PARAM_TYPE> {

	private final IBeanAccess<BEAN_TYPE> beanAccess;
	private final Object executor;
	private final IExecutableChecker<BEAN_TYPE> executableChecker;
	private final IBeanValidator<BEAN_TYPE> beanValidator;
	private final boolean allowDeletedBeans;
	private final boolean allowStaleBeans;

	private final IBeanDtoFactory<BEAN_TYPE> dtoFactory;
	private final IBeanUpdateInterceptor<BEAN_TYPE> updateInterceptor;
	private final Collection<IBeanUpdateInterceptorPlugin<BEAN_TYPE>> updateInterceptorPlugins;

	@SuppressWarnings("unchecked")
	SyncExecutorServiceImpl(
		final IBeanAccess<? extends BEAN_TYPE> beanAccess,
		final Object executor,
		final IExecutableChecker<? extends BEAN_TYPE> executableChecker,
		final IBeanValidator<? extends BEAN_TYPE> beanValidator,
		final IBeanDtoFactory<BEAN_TYPE> dtoFactory,
		final boolean allowDeletedBeans,
		final boolean allowStaleBeans) {

		Assert.paramNotNull(beanAccess, "beanAccess");
		Assert.paramNotNull(beanAccess.getBeanType(), "beanAccess.getBeanType()");
		Assert.paramNotNull(executor, "executor");
		Assert.paramNotNull(dtoFactory, "dtoFactory");

		this.beanAccess = (IBeanAccess<BEAN_TYPE>) beanAccess;
		this.executor = executor;
		this.executableChecker = (IExecutableChecker<BEAN_TYPE>) executableChecker;
		this.beanValidator = (IBeanValidator<BEAN_TYPE>) beanValidator;
		this.allowDeletedBeans = allowDeletedBeans;
		this.allowStaleBeans = allowStaleBeans;

		this.dtoFactory = dtoFactory;
		this.updateInterceptor = createInterceptor(beanAccess.getBeanType());
		this.updateInterceptorPlugins = createUpdateInterceptorPlugins(beanAccess.getBeanType());
	}

	@SuppressWarnings("unchecked")
	private IBeanUpdateInterceptor<BEAN_TYPE> createInterceptor(final Class<?> beanType) {
		final BeanUpdateInterceptor annotation = AnnotationCache.getTypeAnnotationFromHierarchy(
				beanType,
				BeanUpdateInterceptor.class);
		if (annotation != null) {
			final Class<? extends IBeanUpdateInterceptor<?>> value = annotation.value();
			if (value != null) {
				try {
					return (IBeanUpdateInterceptor<BEAN_TYPE>) value.newInstance();
				}
				catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return null;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private Collection<IBeanUpdateInterceptorPlugin<BEAN_TYPE>> createUpdateInterceptorPlugins(final Class<?> beanType) {
		final IPluginPropertiesBuilder propBuilder = PluginToolkit.pluginPropertiesBuilder();
		propBuilder.add(IBeanUpdateInterceptorPlugin.BEAN_TYPE_PROPERTY_KEY, beanType);
		final List result = PluginProvider.getPlugins(IBeanUpdateInterceptorPlugin.ID, propBuilder.build());
		return result;
	}

	@Override
	public List<IBeanDto> execute(
		final Collection<? extends IBeanKey> keys,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {

		Assert.paramNotNull(keys, "beanInfos");

		CapServiceToolkit.checkCanceled(executionCallback);

		final List<BEAN_TYPE> beans;
		if (EmptyCheck.isEmpty(keys)) {
			beans = Collections.emptyList();
		}
		else {
			beans = beanAccess.getBeans(keys, executionCallback);
		}

		CapServiceToolkit.checkCanceled(executionCallback);

		if (!allowStaleBeans) {
			checkBeans(keys, beans, executionCallback);
		}
		else if (!allowDeletedBeans && beans.size() != keys.size()) {
			checkBeans(keys, beans, executionCallback);
		}

		if (executor instanceof IBeanListExecutor) {
			final List<IBeanDto> result = executeBeanList(beans, parameter, executionCallback);
			CapServiceToolkit.checkCanceled(executionCallback);
			return result;
		}
		else if (executor instanceof IBeanExecutor) {
			final List<IBeanDto> result = execute(beans, parameter, executionCallback);
			CapServiceToolkit.checkCanceled(executionCallback);
			return result;
		}
		else {
			throw new IllegalStateException("Data executor type '" + executor + "' is not supported");
		}
	}

	private void checkBeans(
		final Collection<? extends IBeanKey> keys,
		final List<BEAN_TYPE> beans,
		final IExecutionCallback executionCallback) {
		//put beans into map to access them faster at the next step
		final Map<Object, BEAN_TYPE> beanMap = new HashMap<Object, BEAN_TYPE>();
		for (final BEAN_TYPE bean : beans) {
			beanMap.put(getId(bean), bean);
			CapServiceToolkit.checkCanceled(executionCallback);
		}

		//check if beans are deleted or stale
		for (final IBeanKey key : keys) {
			final BEAN_TYPE bean = beanMap.get(key.getId());
			if (!allowDeletedBeans && bean == null) {
				throw new DeletedBeanException(key.getId());
			}
			else {
				if (!allowStaleBeans && key.getVersion() != getVersion(bean)) {
					throw new StaleBeanException(key.getId());
				}
			}
			CapServiceToolkit.checkCanceled(executionCallback);
		}
	}

	@SuppressWarnings("unchecked")
	private List<IBeanDto> executeBeanList(
		final List<BEAN_TYPE> beans,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {

		beforeUpdate(beans);
		if (executableChecker != null) {
			checkExecutableStates(beans, executionCallback);
		}
		final IBeanListExecutor<BEAN_TYPE, PARAM_TYPE> listExecutor = (IBeanListExecutor<BEAN_TYPE, PARAM_TYPE>) executor;
		final List<? extends BEAN_TYPE> executionResult = listExecutor.execute(beans, parameter, executionCallback);
		CapServiceToolkit.checkCanceled(executionCallback);
		validate(beans);
		beanAccess.flush();
		afterUpdate(executionResult);
		return BeanDtoFactoryHelper.createDtos(dtoFactory, executionResult);
	}

	@SuppressWarnings("unchecked")
	private List<IBeanDto> execute(
		final List<BEAN_TYPE> beans,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {

		beforeUpdate(beans);
		final IBeanExecutor<BEAN_TYPE, PARAM_TYPE> beanExecutor = (IBeanExecutor<BEAN_TYPE, PARAM_TYPE>) executor;

		final List<BEAN_TYPE> executionResultList = new LinkedList<BEAN_TYPE>();
		if (EmptyCheck.isEmpty(beans)) {
			if (executableChecker != null) {
				checkExecutableState(null, executionCallback);
			}
			final BEAN_TYPE executionResult = beanExecutor.execute(null, parameter, executionCallback);
			CapServiceToolkit.checkCanceled(executionCallback);

			if (executionResult != null) {
				executionResultList.add(executionResult);
			}
			CapServiceToolkit.checkCanceled(executionCallback);
		}
		else {
			for (final BEAN_TYPE bean : beans) {
				if (executableChecker != null) {
					checkExecutableState(bean, executionCallback);
				}
				final BEAN_TYPE executionResult = beanExecutor.execute(bean, parameter, executionCallback);
				CapServiceToolkit.checkCanceled(executionCallback);

				if (executionResult != null) {
					executionResultList.add(executionResult);
				}
				CapServiceToolkit.checkCanceled(executionCallback);
			}
		}

		validate(beans);
		beanAccess.flush();
		afterUpdate(executionResultList);
		return BeanDtoFactoryHelper.createDtos(dtoFactory, executionResultList);
	}

	private void beforeUpdate(final Collection<? extends BEAN_TYPE> beans) {
		if (updateInterceptor != null) {
			for (final BEAN_TYPE bean : beans) {
				updateInterceptor.beforeUpdate(bean);
			}
		}
		for (final IBeanUpdateInterceptorPlugin<BEAN_TYPE> plugin : updateInterceptorPlugins) {
			for (final BEAN_TYPE bean : beans) {
				plugin.beforeUpdate(bean);
			}
		}
	}

	private void afterUpdate(final Collection<? extends BEAN_TYPE> beans) {
		if (updateInterceptor != null) {
			for (final BEAN_TYPE bean : beans) {
				updateInterceptor.afterUpdate(bean);
			}
		}
		for (final IBeanUpdateInterceptorPlugin<BEAN_TYPE> plugin : updateInterceptorPlugins) {
			for (final BEAN_TYPE bean : beans) {
				plugin.afterUpdate(bean);
			}
		}
	}

	private void validate(final Collection<BEAN_TYPE> beans) {
		if (beanValidator != null) {
			for (final BEAN_TYPE bean : beans) {
				final Collection<IBeanValidationResult> validationResults = beanValidator.validate(bean);
				final IBeanValidationResult worstFirst = BeanValidationResultUtil.getWorstFirst(validationResults);
				if (worstFirst != null && !worstFirst.getValidationResult().isValid()) {
					throw new BeanValidationException(getId(bean), worstFirst);
				}
			}
		}
	}

	private void checkExecutableStates(final List<BEAN_TYPE> beans, final IExecutionCallback executionCallback) {
		for (final BEAN_TYPE bean : beans) {
			final IExecutableState executableState = executableChecker.getExecutableState(bean);
			if (!executableState.isExecutable()) {
				throw new ExecutableCheckException(getId(bean), "Executable check could not pass on service execution for bean '"
					+ bean
					+ "'", executableState.getReason());
			}
			CapServiceToolkit.checkCanceled(executionCallback);
		}
	}

	private void checkExecutableState(final BEAN_TYPE bean, final IExecutionCallback executionCallback) {
		final IExecutableState executableState = executableChecker.getExecutableState(bean);
		if (!executableState.isExecutable()) {
			throw new ExecutableCheckException(getId(bean), "Executable check could not pass on service execution for bean '"
				+ bean
				+ "'", executableState.getReason());
		}
	}

	private Object getId(final BEAN_TYPE bean) {
		return beanAccess.getId(bean);
	}

	private long getVersion(final BEAN_TYPE bean) {
		return beanAccess.getVersion(bean);
	}

}
