/*
 * Copyright (c) 2012, grossmann
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

package org.jowidgets.cap.service.tools.creator;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanData;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.exception.BeanValidationException;
import org.jowidgets.cap.common.api.exception.ExecutableCheckException;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutableState;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.validation.IBeanValidationResult;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.common.tools.validation.BeanValidationResultUtil;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.adapter.ISyncCreatorService;
import org.jowidgets.cap.service.api.bean.BeanCreateInterceptor;
import org.jowidgets.cap.service.api.bean.IBeanCreateInterceptor;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanIdentityResolver;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.plugin.IBeanCreateInterceptorPlugin;
import org.jowidgets.cap.service.tools.bean.DefaultBeanIdentityResolver;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.plugin.api.PluginToolkit;
import org.jowidgets.util.Assert;
import org.jowidgets.util.reflection.AnnotationCache;

public abstract class AbstractSyncCreatorServiceImpl<BEAN_TYPE> implements ISyncCreatorService {

	private final IBeanIdentityResolver<BEAN_TYPE> beanIdentityResolver;
	private final Class<? extends BEAN_TYPE> beanType;
	private final IBeanDtoFactory<BEAN_TYPE> dtoFactory;
	private final IBeanInitializer<BEAN_TYPE> beanInitializer;
	private final IExecutableChecker<BEAN_TYPE> executableChecker;
	private final IBeanValidator<BEAN_TYPE> beanValidator;

	private final IBeanCreateInterceptor<BEAN_TYPE> interceptor;
	private final Collection<IBeanCreateInterceptorPlugin<BEAN_TYPE>> interceptorPlugins;

	protected AbstractSyncCreatorServiceImpl(
		final Class<? extends IBean> beanType,
		final IBeanDtoFactory<BEAN_TYPE> dtoFactory,
		final IBeanInitializer<BEAN_TYPE> beanInitializer) {
		this(beanType, dtoFactory, beanInitializer, null, null);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected AbstractSyncCreatorServiceImpl(
		final Class<? extends IBean> beanType,
		final IBeanDtoFactory<BEAN_TYPE> dtoFactory,
		final IBeanInitializer<BEAN_TYPE> beanInitializer,
		final IExecutableChecker<BEAN_TYPE> executableChecker,
		final IBeanValidator<BEAN_TYPE> beanValidator) {
		this(new DefaultBeanIdentityResolver(beanType), dtoFactory, beanInitializer, executableChecker, beanValidator);
	}

	protected AbstractSyncCreatorServiceImpl(
		final IBeanIdentityResolver<BEAN_TYPE> beanIdentityResolver,
		final IBeanDtoFactory<BEAN_TYPE> dtoFactory,
		final IBeanInitializer<BEAN_TYPE> beanInitializer,
		final IExecutableChecker<BEAN_TYPE> executableChecker,
		final IBeanValidator<BEAN_TYPE> beanValidator) {

		Assert.paramNotNull(beanIdentityResolver, "beanIdentityResolver");
		Assert.paramNotNull(dtoFactory, "dtoFactory");
		Assert.paramNotNull(beanInitializer, "beanInitializer");

		this.beanIdentityResolver = beanIdentityResolver;
		this.beanType = beanIdentityResolver.getBeanType();
		this.dtoFactory = dtoFactory;
		this.beanInitializer = beanInitializer;
		this.executableChecker = executableChecker;
		this.beanValidator = beanValidator;
		this.interceptor = createInterceptor(beanType);
		this.interceptorPlugins = createUpdateInterceptorPlugins(beanType);
	}

	protected abstract BEAN_TYPE createBean(final IExecutionCallback executionCallback);

	protected abstract void persistBean(BEAN_TYPE bean, final IExecutionCallback executionCallback);

	protected Class<? extends BEAN_TYPE> getBeanType() {
		return beanType;
	}

	@SuppressWarnings("unchecked")
	private IBeanCreateInterceptor<BEAN_TYPE> createInterceptor(final Class<?> beanType) {
		final BeanCreateInterceptor annotation = AnnotationCache.getTypeAnnotationFromHierarchy(
				beanType,
				BeanCreateInterceptor.class);
		if (annotation != null) {
			final Class<? extends IBeanCreateInterceptor<?>> value = annotation.value();
			if (value != null) {
				try {
					return (IBeanCreateInterceptor<BEAN_TYPE>) value.newInstance();
				}
				catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return null;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private Collection<IBeanCreateInterceptorPlugin<BEAN_TYPE>> createUpdateInterceptorPlugins(final Class<?> beanType) {
		final IPluginPropertiesBuilder propBuilder = PluginToolkit.pluginPropertiesBuilder();
		propBuilder.add(IBeanCreateInterceptorPlugin.BEAN_TYPE_PROPERTY_KEY, beanType);
		final List result = PluginProvider.getPlugins(IBeanCreateInterceptorPlugin.ID, propBuilder.build());
		return result;
	}

	@Override
	public final List<IBeanDto> create(final Collection<? extends IBeanData> beansData, final IExecutionCallback executionCallback) {
		final List<IBeanDto> result = new LinkedList<IBeanDto>();
		for (final IBeanData beanData : beansData) {
			final BEAN_TYPE bean = createBean(executionCallback);
			CapServiceToolkit.checkCanceled(executionCallback);

			beforeInitialize(bean);
			CapServiceToolkit.checkCanceled(executionCallback);

			beanInitializer.initialize(bean, beanData);
			CapServiceToolkit.checkCanceled(executionCallback);

			checkExecutableStates(bean);
			CapServiceToolkit.checkCanceled(executionCallback);

			validate(bean);
			CapServiceToolkit.checkCanceled(executionCallback);

			persistBean(bean, executionCallback);
			CapServiceToolkit.checkCanceled(executionCallback);

			afterCreate(bean);
			CapServiceToolkit.checkCanceled(executionCallback);

			result.add(dtoFactory.createDto(bean));
			CapServiceToolkit.checkCanceled(executionCallback);
		}
		CapServiceToolkit.checkCanceled(executionCallback);
		return result;
	}

	private void beforeInitialize(final BEAN_TYPE bean) {
		if (interceptor != null) {
			interceptor.beforeInitialize(bean);
		}
		for (final IBeanCreateInterceptorPlugin<BEAN_TYPE> plugin : interceptorPlugins) {
			plugin.beforeInitialize(bean);
		}
	}

	private void afterCreate(final BEAN_TYPE bean) {
		if (interceptor != null) {
			interceptor.afterCreate(bean);
		}
		for (final IBeanCreateInterceptorPlugin<BEAN_TYPE> plugin : interceptorPlugins) {
			plugin.afterCreate(bean);
		}
	}

	private void checkExecutableStates(final BEAN_TYPE bean) {
		if (executableChecker != null) {
			final IExecutableState checkResult = executableChecker.getExecutableState(bean);
			if (checkResult != null && !checkResult.isExecutable()) {
				throw new ExecutableCheckException(beanIdentityResolver.getId(bean), checkResult.getReason());
			}
		}
	}

	private void validate(final BEAN_TYPE bean) {
		if (beanValidator != null) {
			final Collection<IBeanValidationResult> validationResults = beanValidator.validate(bean);
			final IBeanValidationResult worstFirst = BeanValidationResultUtil.getWorstFirst(validationResults);
			if (worstFirst != null && !worstFirst.getValidationResult().isValid()) {
				throw new BeanValidationException(beanIdentityResolver.getId(bean), worstFirst);
			}
		}
	}

}
