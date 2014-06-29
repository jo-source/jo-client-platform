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

package org.jowidgets.cap.service.tools.entity;

import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.service.api.adapter.ISyncCreatorService;
import org.jowidgets.cap.service.api.adapter.ISyncDeleterService;
import org.jowidgets.cap.service.api.adapter.ISyncReaderService;
import org.jowidgets.cap.service.api.adapter.ISyncRefreshService;
import org.jowidgets.cap.service.api.adapter.ISyncUpdaterService;
import org.jowidgets.cap.service.api.entity.IBeanServicesProviderBuilder;
import org.jowidgets.util.Assert;

public class BeanServicesProviderBuilder implements IBeanServicesProviderBuilder {

	private final IBeanServicesProviderBuilder builder;

	BeanServicesProviderBuilder(final IBeanServicesProviderBuilder builder) {
		Assert.paramNotNull(builder, "builder");
		this.builder = builder;
	}

	@Override
	public final IBeanServicesProviderBuilder setReaderService(final IReaderService<Void> readerService) {
		builder.setReaderService(readerService);
		return this;
	}

	@Override
	public final IBeanServicesProviderBuilder setReaderService(final ISyncReaderService<Void> readerService) {
		builder.setReaderService(readerService);
		return this;
	}

	@Override
	public final IBeanServicesProviderBuilder setCreatorService(final ICreatorService creatorService) {
		builder.setCreatorService(creatorService);
		return this;
	}

	@Override
	public final IBeanServicesProviderBuilder setCreatorService(final ISyncCreatorService creatorService) {
		builder.setCreatorService(creatorService);
		return this;
	}

	@Override
	public final IBeanServicesProviderBuilder setRefreshService(final IRefreshService refreshService) {
		builder.setRefreshService(refreshService);
		return this;
	}

	@Override
	public final IBeanServicesProviderBuilder setRefreshService(final ISyncRefreshService refreshService) {
		builder.setRefreshService(refreshService);
		return this;
	}

	@Override
	public final IBeanServicesProviderBuilder setUpdaterService(final IUpdaterService updaterService) {
		builder.setUpdaterService(updaterService);
		return this;
	}

	@Override
	public final IBeanServicesProviderBuilder setUpdaterService(final ISyncUpdaterService updaterService) {
		builder.setUpdaterService(updaterService);
		return this;
	}

	@Override
	public final IBeanServicesProviderBuilder setDeleterService(final IDeleterService deleterService) {
		builder.setDeleterService(deleterService);
		return this;
	}

	@Override
	public final IBeanServicesProviderBuilder setDeleterService(final ISyncDeleterService deleterService) {
		builder.setDeleterService(deleterService);
		return this;
	}

	@Override
	public IBeanServicesProvider build() {
		return builder.build();
	}

}
