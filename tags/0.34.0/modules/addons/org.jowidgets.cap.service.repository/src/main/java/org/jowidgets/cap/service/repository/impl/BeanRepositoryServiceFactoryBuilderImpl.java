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

import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.repository.api.IBeanRepository;
import org.jowidgets.cap.service.repository.api.IBeanRepositoryServiceFactory;
import org.jowidgets.cap.service.repository.api.IBeanRepositoryServiceFactoryBuilder;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.IdentityTransformationDecorator;

final class BeanRepositoryServiceFactoryBuilderImpl<BEAN_TYPE> implements IBeanRepositoryServiceFactoryBuilder<BEAN_TYPE> {

	private static final IServicesDecoratorProvider ASYNC_DECORATOR_PROVIDER = CapServiceToolkit.serviceDecoratorProvider().asyncDecoratorProvider();
	private static final IServicesDecoratorProvider DUMMY_DECORATOR_PROVIDER = new DummyServiceDecoratorProvider();

	private IBeanRepository<BEAN_TYPE> repositiory;
	private Collection<String> properties;
	private IServicesDecoratorProvider serviceDecoratorProvider;

	BeanRepositoryServiceFactoryBuilderImpl() {
		this.serviceDecoratorProvider = DUMMY_DECORATOR_PROVIDER;
	}

	@Override
	public IBeanRepositoryServiceFactoryBuilder<BEAN_TYPE> setRepository(final IBeanRepository<BEAN_TYPE> repositiory) {
		Assert.paramNotNull(repositiory, "repositiory");
		this.repositiory = repositiory;
		return this;
	}

	@Override
	public IBeanRepositoryServiceFactoryBuilder<BEAN_TYPE> setProperties(final Collection<String> properties) {
		Assert.paramNotNull(properties, "properties");
		this.properties = properties;
		return this;
	}

	@Override
	public IBeanRepositoryServiceFactoryBuilder<BEAN_TYPE> setDecorator(final IServicesDecoratorProvider serviceDecoratorProvider) {
		Assert.paramNotNull(serviceDecoratorProvider, "serviceDecoratorProvider");
		this.serviceDecoratorProvider = serviceDecoratorProvider;
		return this;
	}

	@Override
	public IBeanRepositoryServiceFactoryBuilder<BEAN_TYPE> setAsyncDecorator() {
		setDecorator(ASYNC_DECORATOR_PROVIDER);
		return this;
	}

	@Override
	public IBeanRepositoryServiceFactory<BEAN_TYPE> build() {
		if (properties != null) {
			return new BeanRepositoryServiceFactoryImpl<BEAN_TYPE>(repositiory, properties, serviceDecoratorProvider);
		}
		else {
			return new BeanRepositoryServiceFactoryImpl<BEAN_TYPE>(repositiory, serviceDecoratorProvider);
		}
	}

	private static final class DummyServiceDecoratorProvider implements IServicesDecoratorProvider {

		@Override
		public <SERVICE_TYPE> IDecorator<SERVICE_TYPE> getDecorator(final IServiceId<SERVICE_TYPE> id) {
			return new IdentityTransformationDecorator<SERVICE_TYPE>();
		}

		@Override
		public int getOrder() {
			return 0;
		}

	}
}
