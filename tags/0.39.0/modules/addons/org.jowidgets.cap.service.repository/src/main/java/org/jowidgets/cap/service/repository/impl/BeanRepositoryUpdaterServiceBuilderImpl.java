/*
 * Copyright (c) 2014, grossmann
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

package org.jowidgets.cap.service.repository.impl;

import java.util.Collection;
import java.util.List;

import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.adapter.ISyncUpdaterService;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanModifier;
import org.jowidgets.cap.service.api.bean.IBeanUpdateInterceptor;
import org.jowidgets.cap.service.api.updater.IUpdaterServiceBuilder;
import org.jowidgets.cap.service.repository.api.IUpdateSupportBeanRepository;
import org.jowidgets.util.IDecorator;
import org.jowidgets.validation.IValidator;

final class BeanRepositoryUpdaterServiceBuilderImpl<BEAN_TYPE> implements IUpdaterServiceBuilder<BEAN_TYPE> {

	private final IUpdaterServiceBuilder<BEAN_TYPE> builder;
	private final IDecorator<IUpdaterService> asyncDecorator;

	BeanRepositoryUpdaterServiceBuilderImpl(
		final IUpdateSupportBeanRepository<BEAN_TYPE> repository,
		final IBeanAccess<BEAN_TYPE> beanAccess,
		final List<String> allProperties,
		final IDecorator<IUpdaterService> asyncDecorator) {

		this.asyncDecorator = asyncDecorator;

		this.builder = CapServiceToolkit.updaterServiceBuilder(beanAccess);
		this.builder.setBeanDtoFactoryAndBeanModifier(allProperties);
		this.builder.addUpdaterInterceptor(new IBeanUpdateInterceptor<BEAN_TYPE>() {
			@Override
			public void beforeUpdate(final BEAN_TYPE bean) {
				repository.preUpdate(bean);
			}

			@Override
			public void afterUpdate(final BEAN_TYPE bean) {
				repository.postUpdate(bean);
			}
		});
	}

	@Override
	public IUpdaterServiceBuilder<BEAN_TYPE> addUpdaterInterceptor(final IBeanUpdateInterceptor<BEAN_TYPE> interceptor) {
		builder.addUpdaterInterceptor(interceptor);
		return this;
	}

	@Override
	public IUpdaterServiceBuilder<BEAN_TYPE> addBeanValidator(final IBeanValidator<? extends BEAN_TYPE> validator) {
		builder.addBeanValidator(validator);
		return this;
	}

	@Override
	public IUpdaterServiceBuilder<BEAN_TYPE> addPropertyValidator(
		final String propertyName,
		final IValidator<? extends Object> validator) {
		builder.addPropertyValidator(propertyName, validator);
		return this;
	}

	@Override
	public IUpdaterServiceBuilder<BEAN_TYPE> addExecutableChecker(final IExecutableChecker<? extends BEAN_TYPE> executableChecker) {
		builder.addExecutableChecker(executableChecker);
		return this;
	}

	@Override
	public IUpdaterServiceBuilder<BEAN_TYPE> setExecutableChecker(final IExecutableChecker<? extends BEAN_TYPE> executableChecker) {
		builder.setExecutableChecker(executableChecker);
		return this;
	}

	@Override
	public IUpdaterServiceBuilder<BEAN_TYPE> setBeanDtoFactory(final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory) {
		builder.setBeanDtoFactory(beanDtoFactory);
		return this;
	}

	@Override
	public IUpdaterServiceBuilder<BEAN_TYPE> setBeanModifier(final IBeanModifier<BEAN_TYPE> beanModifier) {
		builder.setBeanModifier(beanModifier);
		return this;
	}

	@Override
	public IUpdaterServiceBuilder<BEAN_TYPE> setBeanDtoFactoryAndBeanModifier(final Collection<String> propertyNames) {
		builder.setBeanDtoFactoryAndBeanModifier(propertyNames);
		return this;
	}

	@Override
	public IUpdaterServiceBuilder<BEAN_TYPE> setAllowDeletedBeans(final boolean allowDeletedBeans) {
		builder.setAllowDeletedBeans(allowDeletedBeans);
		return this;
	}

	@Override
	public IUpdaterServiceBuilder<BEAN_TYPE> setAllowStaleBeans(final boolean allowStaleBeans) {
		builder.setAllowStaleBeans(allowStaleBeans);
		return this;
	}

	@Override
	public ISyncUpdaterService buildSyncService() {
		return builder.buildSyncService();
	}

	@Override
	public IUpdaterService build() {
		return asyncDecorator.decorate(builder.build());
	}

}
