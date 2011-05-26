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

package org.jowidgets.cap.service.impl.entity;

import java.io.Serializable;
import java.util.UUID;

import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServiceRegistry;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.service.tools.ServiceId;

final class BeanServicesProviderImpl<BEAN_TYPE> implements IBeanServicesProvider<BEAN_TYPE>, Serializable {

	private static final long serialVersionUID = -8588074689307098706L;

	private final IServiceId<ICreatorService> creatorServiceId;
	private final IServiceId<IRefreshService> refreshServiceId;
	private final IServiceId<IUpdaterService> updaterServiceId;
	private final IServiceId<IDeleterService> deleterServiceId;

	BeanServicesProviderImpl(
		final IServiceRegistry serviceRegistry,
		final ICreatorService creatorService,
		final IRefreshService refreshService,
		final IUpdaterService updaterService,
		final IDeleterService deleterService) {

		this.creatorServiceId = new ServiceId<ICreatorService>(UUID.randomUUID(), ICreatorService.class);
		this.refreshServiceId = new ServiceId<IRefreshService>(UUID.randomUUID(), IRefreshService.class);
		this.updaterServiceId = new ServiceId<IUpdaterService>(UUID.randomUUID(), IUpdaterService.class);
		this.deleterServiceId = new ServiceId<IDeleterService>(UUID.randomUUID(), IDeleterService.class);

		serviceRegistry.register(creatorServiceId, creatorService);
		serviceRegistry.register(refreshServiceId, refreshService);
		serviceRegistry.register(updaterServiceId, updaterService);
		serviceRegistry.register(deleterServiceId, deleterService);
	}

	@Override
	public ICreatorService creatorService() {
		return ServiceProvider.getService(creatorServiceId);
	}

	@Override
	public IRefreshService refreshService() {
		return ServiceProvider.getService(refreshServiceId);
	}

	@Override
	public IUpdaterService updaterService() {
		return ServiceProvider.getService(updaterServiceId);
	}

	@Override
	public IDeleterService deleterService() {
		return ServiceProvider.getService(deleterServiceId);
	}

}
