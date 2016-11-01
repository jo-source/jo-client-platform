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

import java.util.List;

import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.adapter.ISyncCreatorService;
import org.jowidgets.cap.service.repository.api.ICreateSupportBeanRepository;
import org.jowidgets.cap.service.tools.creator.AbstractCreatorServiceBuilder;
import org.jowidgets.util.IAdapterFactory;
import org.jowidgets.util.IDecorator;

class BeanRepositoryCreatorServiceBuilderImpl<BEAN_TYPE> extends AbstractCreatorServiceBuilder<BEAN_TYPE> {

	private static final IAdapterFactory<ICreatorService, ISyncCreatorService> ADAPTER_FACTORY = CapServiceToolkit.adapterFactoryProvider().creator();

	private final ICreateSupportBeanRepository<BEAN_TYPE> repository;
	private final IDecorator<ICreatorService> asyncDecorator;

	BeanRepositoryCreatorServiceBuilderImpl(
		final ICreateSupportBeanRepository<BEAN_TYPE> repository,
		final IDecorator<ICreatorService> asyncDecorator,
		final List<String> appProperties) {
		super(repository);
		this.repository = repository;
		this.asyncDecorator = asyncDecorator;
		setBeanDtoFactoryAndBeanInitializer(appProperties);
	}

	protected final ICreateSupportBeanRepository<BEAN_TYPE> getRepository() {
		return repository;
	}

	@Override
	public ICreatorService build() {
		applyPlugins();
		final ISyncCreatorService result = new SyncBeanRepositoryCreatorServiceImpl<BEAN_TYPE>(this);
		return asyncDecorator.decorate(ADAPTER_FACTORY.createAdapter(result));
	}

}
