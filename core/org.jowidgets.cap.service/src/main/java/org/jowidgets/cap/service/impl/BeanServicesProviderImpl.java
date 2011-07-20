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

package org.jowidgets.cap.service.impl;

import java.io.Serializable;

import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServiceRegistry;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.service.tools.ServiceId;
import org.jowidgets.util.Assert;

final class BeanServicesProviderImpl implements IBeanServicesProvider, Serializable {

	private static final long serialVersionUID = -8588074689307098706L;

	private final IServiceId<IReaderService<Void>> readerServiceId;
	private final IServiceId<ICreatorService> creatorServiceId;
	private final IServiceId<IRefreshService> refreshServiceId;
	private final IServiceId<IUpdaterService> updaterServiceId;
	private final IServiceId<IDeleterService> deleterServiceId;

	BeanServicesProviderImpl(
		final IServiceRegistry serviceRegistry,
		final IServiceId<IEntityService> entityServiceId,
		final Object entityId,
		final IReaderService<Void> readerService,
		final ICreatorService creatorService,
		final IRefreshService refreshService,
		final IUpdaterService updaterService,
		final IDeleterService deleterService) {

		Assert.paramNotNull(serviceRegistry, "serviceRegistry");
		Assert.paramNotNull(entityServiceId, "entityServiceId");
		Assert.paramNotNull(entityId, "entityId");

		if (readerService != null) {
			this.readerServiceId = new ServiceId<IReaderService<Void>>(new Id(
				entityServiceId,
				entityId.getClass().getName(),
				IReaderService.class.getName()), IReaderService.class);
			serviceRegistry.addService(readerServiceId, readerService);
		}
		else {
			this.readerServiceId = null;
		}

		if (creatorService != null) {
			this.creatorServiceId = new ServiceId<ICreatorService>(new Id(
				entityServiceId,
				entityId.getClass().getName(),
				ICreatorService.class.getName()), ICreatorService.class);
			serviceRegistry.addService(creatorServiceId, creatorService);
		}
		else {
			this.creatorServiceId = null;
		}

		if (refreshService != null) {
			this.refreshServiceId = new ServiceId<IRefreshService>(new Id(
				entityServiceId,
				entityId.getClass().getName(),
				IRefreshService.class.getName()), IRefreshService.class);
			serviceRegistry.addService(refreshServiceId, refreshService);
		}
		else {
			this.refreshServiceId = null;
		}

		if (updaterService != null) {
			this.updaterServiceId = new ServiceId<IUpdaterService>(new Id(
				entityServiceId,
				entityId.getClass().getName(),
				IUpdaterService.class.getName()), IUpdaterService.class);
			serviceRegistry.addService(updaterServiceId, updaterService);
		}
		else {
			updaterServiceId = null;
		}

		if (deleterService != null) {
			this.deleterServiceId = new ServiceId<IDeleterService>(new Id(
				entityServiceId,
				entityId.getClass().getName(),
				IDeleterService.class.getName()), IDeleterService.class);
			serviceRegistry.addService(deleterServiceId, deleterService);
		}
		else {
			this.deleterServiceId = null;
		}

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
		return ServiceProvider.getService(deleterServiceId);
	}

	private final class Id implements Serializable {

		private static final long serialVersionUID = -2049008694890176142L;

		private final IServiceId<IEntityService> entityServiceId;
		private final String entityId;
		private final String service;

		private Id(final IServiceId<IEntityService> entityServiceId, final String entityId, final String service) {
			super();
			this.entityServiceId = entityServiceId;
			this.entityId = entityId;
			this.service = service;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
			result = prime * result + ((entityServiceId == null) ? 0 : entityServiceId.hashCode());
			result = prime * result + ((service == null) ? 0 : service.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Id other = (Id) obj;
			if (entityId == null) {
				if (other.entityId != null) {
					return false;
				}
			}
			else if (!entityId.equals(other.entityId)) {
				return false;
			}
			if (entityServiceId == null) {
				if (other.entityServiceId != null) {
					return false;
				}
			}
			else if (!entityServiceId.equals(other.entityServiceId)) {
				return false;
			}
			if (service == null) {
				if (other.service != null) {
					return false;
				}
			}
			else if (!service.equals(other.service)) {
				return false;
			}
			return true;
		}

	}
}
