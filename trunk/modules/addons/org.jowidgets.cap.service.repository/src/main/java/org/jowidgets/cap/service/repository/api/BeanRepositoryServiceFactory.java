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

package org.jowidgets.cap.service.repository.api;

import java.util.Collection;

import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.service.api.creator.ICreatorServiceBuilder;
import org.jowidgets.cap.service.api.deleter.IDeleterServiceBuilder;
import org.jowidgets.cap.service.api.refresh.IRefreshServiceBuilder;
import org.jowidgets.cap.service.api.updater.IUpdaterServiceBuilder;

public final class BeanRepositoryServiceFactory {

	private BeanRepositoryServiceFactory() {}

	public static <BEAN_TYPE> IBeanRepositoryServiceFactory<BEAN_TYPE> create(final IBeanRepository<BEAN_TYPE> repository) {
		return BeanRepositoryServiceToolkit.serviceFactory(repository);
	};

	public static <BEAN_TYPE> IBeanRepositoryServiceFactory<BEAN_TYPE> create(
		final IBeanRepository<BEAN_TYPE> repository,
		final Collection<String> properties) {
		return BeanRepositoryServiceToolkit.serviceFactory(repository, properties);
	};

	public static IBeanServicesProvider beanServices(final IBeanRepository<?> repository) {
		return create(repository).beanServices();
	};

	public static IBeanServicesProvider beanServices(final IBeanRepository<?> repository, final Collection<String> properties) {
		return create(repository, properties).beanServices();
	};

	public static <BEAN_TYPE> ICreatorServiceBuilder<BEAN_TYPE> creatorServiceBuilder(final IBeanRepository<BEAN_TYPE> repository) {
		return create(repository).creatorServiceBuilder();
	}

	public static ICreatorService creatorService(final IBeanRepository<?> repository) {
		return create(repository).creatorService();
	}

	public static IReaderService<Void> readerService(final IBeanRepository<?> repository) {
		return create(repository).readerService();
	}

	public static <BEAN_TYPE> IRefreshServiceBuilder<BEAN_TYPE> refreshServiceBuilder(final IBeanRepository<BEAN_TYPE> repository) {
		return create(repository).refreshServiceBuilder();
	}

	public static IRefreshService refreshService(final IBeanRepository<?> repository) {
		return create(repository).refreshService();
	}

	public static <BEAN_TYPE> IDeleterServiceBuilder<BEAN_TYPE> deleterServiceBuilder(final IBeanRepository<BEAN_TYPE> repository) {
		return create(repository).deleterServiceBuilder();
	}

	public static IDeleterService deleterService(final IBeanRepository<?> repository) {
		return create(repository).deleterService();
	}

	public static <BEAN_TYPE> IUpdaterServiceBuilder<BEAN_TYPE> updaterServiceBuilder(final IBeanRepository<BEAN_TYPE> repository) {
		return create(repository).updaterServiceBuilder();
	}

	IUpdaterService updaterService(final IBeanRepository<?> repository) {
		return create(repository).updaterService();
	}

}
