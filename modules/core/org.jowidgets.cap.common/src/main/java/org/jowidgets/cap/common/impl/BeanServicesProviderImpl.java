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

package org.jowidgets.cap.common.impl;

import java.io.Serializable;

import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.ServiceProvider;

final class BeanServicesProviderImpl implements IBeanServicesProvider, Serializable {

	private static final long serialVersionUID = -1736748244538349795L;

	private final IServiceId<IReaderService<Void>> readerServiceId;
	private final IServiceId<ICreatorService> creatorServiceId;
	private final IServiceId<IRefreshService> refreshServiceId;
	private final IServiceId<IUpdaterService> updaterServiceId;
	private final IServiceId<IDeleterService> deleterServiceId;

	BeanServicesProviderImpl(
		final IServiceId<IReaderService<Void>> readerServiceId,
		final IServiceId<ICreatorService> creatorServiceId,
		final IServiceId<IRefreshService> refreshServiceId,
		final IServiceId<IUpdaterService> updaterServiceId,
		final IServiceId<IDeleterService> deleterServiceId) {

		this.readerServiceId = readerServiceId;
		this.creatorServiceId = creatorServiceId;
		this.refreshServiceId = refreshServiceId;
		this.updaterServiceId = updaterServiceId;
		this.deleterServiceId = deleterServiceId;
	}

	@Override
	public IReaderService<Void> readerService() {
		if (readerServiceId != null) {
			return ServiceProvider.getService(readerServiceId);
		}
		return null;
	}

	@Override
	public ICreatorService creatorService() {
		if (creatorServiceId != null) {
			return ServiceProvider.getService(creatorServiceId);
		}
		return null;
	}

	@Override
	public IRefreshService refreshService() {
		if (refreshServiceId != null) {
			return ServiceProvider.getService(refreshServiceId);
		}
		return null;
	}

	@Override
	public IUpdaterService updaterService() {
		if (updaterServiceId != null) {
			return ServiceProvider.getService(updaterServiceId);
		}
		return null;
	}

	@Override
	public IDeleterService deleterService() {
		if (deleterServiceId != null) {
			return ServiceProvider.getService(deleterServiceId);
		}
		return null;
	}

}
