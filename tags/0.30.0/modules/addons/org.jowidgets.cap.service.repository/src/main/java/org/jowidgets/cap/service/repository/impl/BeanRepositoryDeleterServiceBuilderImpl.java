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

import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.adapter.ISyncDeleterService;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.repository.api.IDeleteSupportBeanRepository;
import org.jowidgets.cap.service.tools.deleter.AbstractDeleterServiceBuilder;
import org.jowidgets.util.IAdapterFactory;
import org.jowidgets.util.IDecorator;

final class BeanRepositoryDeleterServiceBuilderImpl<BEAN_TYPE> extends AbstractDeleterServiceBuilder<BEAN_TYPE> {

	private static final IAdapterFactory<IDeleterService, ISyncDeleterService> DELETER_ADAPTER_FACTORY = CapServiceToolkit.adapterFactoryProvider().deleter();

	private final IBeanAccess<BEAN_TYPE> beanAccess;
	private final IDeleteSupportBeanRepository<BEAN_TYPE> repository;
	private final IDecorator<IDeleterService> asyncDecorator;

	BeanRepositoryDeleterServiceBuilderImpl(
		final IBeanAccess<BEAN_TYPE> beanAccess,
		final IDeleteSupportBeanRepository<BEAN_TYPE> repository,
		final IDecorator<IDeleterService> asyncDecorator) {

		this.beanAccess = beanAccess;
		this.repository = repository;
		this.asyncDecorator = asyncDecorator;
	}

	@Override
	public IDeleterService build() {
		return asyncDecorator.decorate(DELETER_ADAPTER_FACTORY.createAdapter(buildSyncService()));
	}

	private ISyncDeleterService buildSyncService() {
		return new SyncBeanRepositoryDeleterServiceImpl<BEAN_TYPE>(
			repository,
			beanAccess,
			getExecutableChecker(),
			getInterceptor(),
			isAllowDeletedBeans(),
			isAllowStaleBeans());
	}
}
