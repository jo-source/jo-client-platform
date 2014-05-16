/*
 * Copyright (c) 2014, Michael
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

import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.adapter.ISyncRefreshService;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.refresh.IRefreshServiceBuilder;
import org.jowidgets.util.IDecorator;

final class BeanRepositoryRefreshServiceBuilderImpl<BEAN_TYPE> implements IRefreshServiceBuilder<BEAN_TYPE> {

	private final IDecorator<IRefreshService> asyncDecorator;
	private final IRefreshServiceBuilder<BEAN_TYPE> builder;

	BeanRepositoryRefreshServiceBuilderImpl(
		final IBeanAccess<BEAN_TYPE> beanAccess,
		final List<String> allProperties,
		final IDecorator<IRefreshService> asyncDecorator) {

		this.asyncDecorator = asyncDecorator;

		this.builder = CapServiceToolkit.refreshServiceBuilder(beanAccess);
		this.builder.setBeanDtoFactory(allProperties);
	}

	@Override
	public IRefreshServiceBuilder<BEAN_TYPE> setBeanDtoFactory(final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory) {
		builder.setBeanDtoFactory(beanDtoFactory);
		return this;
	}

	@Override
	public IRefreshServiceBuilder<BEAN_TYPE> setBeanDtoFactory(final Collection<String> propertyNames) {
		builder.setBeanDtoFactory(propertyNames);
		return this;
	}

	@Override
	public IRefreshServiceBuilder<BEAN_TYPE> setAllowDeletedBeans(final boolean allowDeletedBeans) {
		builder.setAllowDeletedBeans(allowDeletedBeans);
		return this;
	}

	@Override
	public ISyncRefreshService buildSyncService() {
		return builder.buildSyncService();
	}

	@Override
	public IRefreshService build() {
		return asyncDecorator.decorate(builder.build());
	}

}
