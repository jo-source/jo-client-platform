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

import org.jowidgets.cap.service.repository.api.IBeanRepository;
import org.jowidgets.cap.service.repository.api.IBeanRepositoryServiceFactory;
import org.jowidgets.cap.service.repository.api.IBeanRepositoryServiceFactoryBuilder;
import org.jowidgets.cap.service.repository.api.IBeanRepositoryServiceToolkit;

public final class BeanRepositoryServiceToolkitImpl implements IBeanRepositoryServiceToolkit {

	@Override
	public <BEAN_TYPE> IBeanRepositoryServiceFactory<BEAN_TYPE> serviceFactory(final IBeanRepository<BEAN_TYPE> repositiory) {
		final IBeanRepositoryServiceFactoryBuilder<BEAN_TYPE> builder = serviceFactoryBuilder();
		return builder.setRepository(repositiory).build();
	}

	@Override
	public <BEAN_TYPE> IBeanRepositoryServiceFactory<BEAN_TYPE> serviceFactory(
		final IBeanRepository<BEAN_TYPE> repositiory,
		final Collection<String> properties) {
		final IBeanRepositoryServiceFactoryBuilder<BEAN_TYPE> builder = serviceFactoryBuilder();
		return builder.setRepository(repositiory).setProperties(properties).build();
	}

	@Override
	public <BEAN_TYPE> IBeanRepositoryServiceFactory<BEAN_TYPE> serviceFactory(
		final IBeanRepository<BEAN_TYPE> repositiory,
		final boolean asyncDecorator) {
		final IBeanRepositoryServiceFactoryBuilder<BEAN_TYPE> builder = serviceFactoryBuilder();
		if (asyncDecorator) {
			builder.setAsyncDecorator();
		}
		return builder.setRepository(repositiory).build();
	}

	@Override
	public <BEAN_TYPE> IBeanRepositoryServiceFactory<BEAN_TYPE> serviceFactory(
		final IBeanRepository<BEAN_TYPE> repositiory,
		final Collection<String> properties,
		final boolean asyncDecorator) {
		final IBeanRepositoryServiceFactoryBuilder<BEAN_TYPE> builder = serviceFactoryBuilder();
		if (asyncDecorator) {
			builder.setAsyncDecorator();
		}
		return builder.setRepository(repositiory).setProperties(properties).build();
	}

	@Override
	public <BEAN_TYPE> IBeanRepositoryServiceFactoryBuilder<BEAN_TYPE> serviceFactoryBuilder() {
		return new BeanRepositoryServiceFactoryBuilderImpl<BEAN_TYPE>();
	}

}
