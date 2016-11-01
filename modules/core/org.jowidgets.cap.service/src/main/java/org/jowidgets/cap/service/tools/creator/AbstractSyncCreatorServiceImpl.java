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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanData;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.exception.ExecutableCheckException;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutableState;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.adapter.ISyncCreatorService;
import org.jowidgets.cap.service.api.bean.BeanCreateInterceptor;
import org.jowidgets.cap.service.api.bean.IBeanCreateInterceptor;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanIdentityResolver;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.creator.IBeanDataMapper;
import org.jowidgets.cap.service.api.creator.ICreatorServiceInterceptor;
import org.jowidgets.cap.service.api.plugin.IBeanCreateInterceptorPlugin;
import org.jowidgets.cap.service.tools.bean.DefaultBeanIdentityResolver;
import org.jowidgets.cap.service.tools.validation.ServiceBeanValidationHelper;
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
	private final boolean confirmValidationWarnings;
	private final Collection<ICreatorServiceInterceptor<BEAN_TYPE>> creatorServiceInterceptors;
	private final Collection<IBeanCreateInterceptor<BEAN_TYPE>> beanCreateInterceptors;

	protected AbstractSyncCreatorServiceImpl(
		final Class<? extends IBean> beanType,
		final IBeanDtoFactory<BEAN_TYPE> dtoFactory,
		final IBeanInitializer<BEAN_TYPE> beanInitializer,
		final boolean confirmValidationWarnings) {
		this(beanType, dtoFactory, beanInitializer, null, null, confirmValidationWarnings);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected AbstractSyncCreatorServiceImpl(
		final Class<? extends IBean> beanType,
		final IBeanDtoFactory<BEAN_TYPE> dtoFactory,
		final IBeanInitializer<BEAN_TYPE> beanInitializer,
		final IExecutableChecker<BEAN_TYPE> executableChecker,
		final IBeanValidator<BEAN_TYPE> beanValidator,
		final boolean confirmValidationWarnings) {
		this(
			new DefaultBeanIdentityResolver(beanType),
			dtoFactory,
			beanInitializer,
			executableChecker,
			beanValidator,
			null,
			confirmValidationWarnings);
	}

	protected AbstractSyncCreatorServiceImpl(final AbstractCreatorServiceBuilder<BEAN_TYPE> builder) {
		this(
			builder.getBeanIdentityResolver(),
			builder.getBeanDtoFactory(),
			builder.getBeanInitializer(),
			builder.getExecutableChecker(),
			builder.getBeanValidator(),
			builder.getCreatorServiceInterceptors(),
			builder.isConfirmValidationWarnings());
	}

	protected AbstractSyncCreatorServiceImpl(
		final IBeanIdentityResolver<BEAN_TYPE> beanIdentityResolver,
		final IBeanDtoFactory<BEAN_TYPE> dtoFactory,
		final IBeanInitializer<BEAN_TYPE> beanInitializer,
		final IExecutableChecker<BEAN_TYPE> executableChecker,
		final IBeanValidator<BEAN_TYPE> beanValidator,
		final Collection<ICreatorServiceInterceptor<BEAN_TYPE>> creatorServiceInterceptors,
		final boolean confirmValidationWarnings) {

		Assert.paramNotNull(beanIdentityResolver, "beanIdentityResolver");
		Assert.paramNotNull(dtoFactory, "dtoFactory");
		Assert.paramNotNull(beanInitializer, "beanInitializer");

		this.beanIdentityResolver = beanIdentityResolver;
		this.beanType = beanIdentityResolver.getBeanType();
		this.dtoFactory = dtoFactory;
		this.beanInitializer = beanInitializer;
		this.executableChecker = executableChecker;
		this.beanValidator = beanValidator;
		if (creatorServiceInterceptors != null) {
			this.creatorServiceInterceptors = new ArrayList<ICreatorServiceInterceptor<BEAN_TYPE>>(creatorServiceInterceptors);
		}
		else {
			this.creatorServiceInterceptors = Collections.emptyList();
		}
		this.confirmValidationWarnings = confirmValidationWarnings;
		this.beanCreateInterceptors = createBeanCreateInterceptors(beanType);
	}

	protected abstract BEAN_TYPE createBean(
		final Collection<IBeanKey> parentBeanKeys,
		final IExecutionCallback executionCallback);

	protected abstract void persistBean(
		Collection<IBeanKey> parentBeanKeys,
		BEAN_TYPE bean,
		final IExecutionCallback executionCallback);

	protected Class<? extends BEAN_TYPE> getBeanType() {
		return beanType;
	}

	private List<IBeanCreateInterceptor<BEAN_TYPE>> createBeanCreateInterceptors(final Class<?> beanType) {
		final List<IBeanCreateInterceptor<BEAN_TYPE>> result = new ArrayList<IBeanCreateInterceptor<BEAN_TYPE>>();
		final IBeanCreateInterceptor<BEAN_TYPE> annotatedInterceptor = createAnnotatedInterceptor(beanType);
		if (annotatedInterceptor != null) {
			result.add(annotatedInterceptor);
		}
		result.addAll(createUpdateInterceptorPlugins(beanType));
		return result;
	}

	@SuppressWarnings("unchecked")
	private IBeanCreateInterceptor<BEAN_TYPE> createAnnotatedInterceptor(final Class<?> beanType) {
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

	@SuppressWarnings("unchecked")
	@Override
	public final List<IBeanDto> create(
		final List<? extends IBeanKey> parentBeanKeys,
		final Collection<? extends IBeanData> beansData,
		final IExecutionCallback executionCallback) {

		final List<IBeanDto> result = new LinkedList<IBeanDto>();
		final List<BEAN_TYPE> beans = new ArrayList<BEAN_TYPE>(beansData.size());

		final Map<Object, IBeanData> beanDataMap = new HashMap<Object, IBeanData>();
		for (final IBeanData beanData : beansData) {
			CapServiceToolkit.checkCanceled(executionCallback);
			final BEAN_TYPE bean = createBean((Collection<IBeanKey>) parentBeanKeys, executionCallback);
			beanDataMap.put(beanIdentityResolver.getId(bean), beanData);
			beans.add(bean);
		}
		final IBeanDataMapper<BEAN_TYPE> beanDataMapper = new BeanDataMapper(beanDataMap);

		beforeInitialize(beans, beanDataMapper, executionCallback);
		for (final BEAN_TYPE bean : beans) {
			CapServiceToolkit.checkCanceled(executionCallback);
			beanInitializer.initialize(bean, beanDataMapper.getBeanData(bean));
		}
		afterInitialize(beans, beanDataMapper, executionCallback);

		checkExecutableStates(beans, executionCallback);
		validate(beans, executionCallback);

		for (final BEAN_TYPE bean : beans) {
			CapServiceToolkit.checkCanceled(executionCallback);
			persistBean((Collection<IBeanKey>) parentBeanKeys, bean, executionCallback);
		}
		afterCreate(beans, beanDataMapper, executionCallback);

		for (final BEAN_TYPE bean : beans) {
			CapServiceToolkit.checkCanceled(executionCallback);
			result.add(dtoFactory.createDto(bean));
		}

		CapServiceToolkit.checkCanceled(executionCallback);
		return result;
	}

	private void beforeInitialize(
		final List<BEAN_TYPE> beans,
		final IBeanDataMapper<BEAN_TYPE> beanDataMapper,
		final IExecutionCallback executionCallback) {

		if (!beanCreateInterceptors.isEmpty()) {
			for (final BEAN_TYPE bean : beans) {
				CapServiceToolkit.checkCanceled(executionCallback);
				for (final IBeanCreateInterceptor<BEAN_TYPE> interceptor : beanCreateInterceptors) {
					CapServiceToolkit.checkCanceled(executionCallback);
					interceptor.beforeInitialize(bean);
				}
			}
		}

		for (final ICreatorServiceInterceptor<BEAN_TYPE> serviceInterceptor : creatorServiceInterceptors) {
			CapServiceToolkit.checkCanceled(executionCallback);
			serviceInterceptor.beforeInitializeForCreation(beans, beanDataMapper, executionCallback);
		}
	}

	private void afterInitialize(
		final List<BEAN_TYPE> beans,
		final IBeanDataMapper<BEAN_TYPE> beanDataMapper,
		final IExecutionCallback executionCallback) {

		for (final ICreatorServiceInterceptor<BEAN_TYPE> serviceInterceptor : creatorServiceInterceptors) {
			CapServiceToolkit.checkCanceled(executionCallback);
			serviceInterceptor.afterInitializeForCreation(beans, beanDataMapper, executionCallback);
		}
	}

	private void afterCreate(
		final List<BEAN_TYPE> beans,
		final IBeanDataMapper<BEAN_TYPE> beanDataMapper,
		final IExecutionCallback executionCallback) {
		if (!beanCreateInterceptors.isEmpty()) {
			for (final BEAN_TYPE bean : beans) {
				CapServiceToolkit.checkCanceled(executionCallback);
				for (final IBeanCreateInterceptor<BEAN_TYPE> interceptor : beanCreateInterceptors) {
					CapServiceToolkit.checkCanceled(executionCallback);
					interceptor.afterCreate(bean);
				}
			}
		}

		for (final ICreatorServiceInterceptor<BEAN_TYPE> serviceInterceptor : creatorServiceInterceptors) {
			CapServiceToolkit.checkCanceled(executionCallback);
			serviceInterceptor.afterCreation(beans, beanDataMapper, executionCallback);
		}
	}

	private void checkExecutableStates(final List<BEAN_TYPE> beans, final IExecutionCallback executionCallback) {
		if (executableChecker != null) {
			for (final BEAN_TYPE bean : beans) {
				CapServiceToolkit.checkCanceled(executionCallback);
				checkExecutableState(bean);
			}
		}
	}

	private void checkExecutableState(final BEAN_TYPE bean) {
		if (executableChecker != null) {
			final IExecutableState checkResult = executableChecker.getExecutableState(bean);
			if (checkResult != null && !checkResult.isExecutable()) {
				throw new ExecutableCheckException(beanIdentityResolver.getId(bean), checkResult.getReason());
			}
		}
	}

	private void validate(final List<BEAN_TYPE> beans, final IExecutionCallback executionCallback) {
		if (beanValidator != null) {
			ServiceBeanValidationHelper.validate(
					beanValidator,
					confirmValidationWarnings,
					beans,
					beanIdentityResolver,
					executionCallback);
		}
	}

	private final class BeanDataMapper implements IBeanDataMapper<BEAN_TYPE> {

		private final Map<Object, IBeanData> original;

		BeanDataMapper(final Map<Object, IBeanData> original) {
			Assert.paramNotNull(original, "original");
			this.original = original;
		}

		@Override
		public IBeanData getBeanData(final BEAN_TYPE bean) {
			return original.get(beanIdentityResolver.getId(bean));
		}

	}

}
