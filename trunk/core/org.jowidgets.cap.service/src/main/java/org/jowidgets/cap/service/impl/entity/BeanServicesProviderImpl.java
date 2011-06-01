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

import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServiceRegistry;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.service.tools.ServiceId;
import org.jowidgets.util.Assert;

final class BeanServicesProviderImpl<BEAN_TYPE> implements IBeanServicesProvider<BEAN_TYPE>, Serializable {

	private static final long serialVersionUID = -8588074689307098706L;

	private final IServiceId<ICreatorService> creatorServiceId;
	private final IServiceId<IRefreshService> refreshServiceId;
	private final IServiceId<IUpdaterService> updaterServiceId;
	private final IServiceId<IDeleterService> deleterServiceId;

	BeanServicesProviderImpl(
		final IServiceRegistry serviceRegistry,
		final IServiceId<IEntityService> entityServiceId,
		final Class<? extends BEAN_TYPE> beanType,
		final ICreatorService creatorService,
		final IRefreshService refreshService,
		final IUpdaterService updaterService,
		final IDeleterService deleterService) {

		Assert.paramNotNull(serviceRegistry, "serviceRegistry");
		Assert.paramNotNull(entityServiceId, "entityServiceId");
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(creatorService, "creatorService");
		Assert.paramNotNull(refreshService, "refreshService");
		Assert.paramNotNull(updaterService, "updaterService");
		Assert.paramNotNull(deleterService, "deleterService");

		this.creatorServiceId = new ServiceId<ICreatorService>(
			new Id(entityServiceId, beanType, ICreatorService.class),
			ICreatorService.class);

		this.refreshServiceId = new ServiceId<IRefreshService>(
			new Id(entityServiceId, beanType, IRefreshService.class),
			IRefreshService.class);

		this.updaterServiceId = new ServiceId<IUpdaterService>(
			new Id(entityServiceId, beanType, IUpdaterService.class),
			IUpdaterService.class);

		this.deleterServiceId = new ServiceId<IDeleterService>(
			new Id(entityServiceId, beanType, IDeleterService.class),
			IDeleterService.class);

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

	private final class Id {

		private final IServiceId<IEntityService> entityServiceId;
		private final Class<? extends BEAN_TYPE> beanType;
		private final Object service;

		private Id(
			final IServiceId<IEntityService> entityServiceId,
			final Class<? extends BEAN_TYPE> beanType,
			final Object service) {
			super();
			this.entityServiceId = entityServiceId;
			this.beanType = beanType;
			this.service = service;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((beanType == null) ? 0 : beanType.hashCode());
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
			@SuppressWarnings("unchecked")
			final Id other = (Id) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (beanType == null) {
				if (other.beanType != null) {
					return false;
				}
			}
			else if (!beanType.equals(other.beanType)) {
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

		private BeanServicesProviderImpl<?> getOuterType() {
			return BeanServicesProviderImpl.this;
		}

	}
}
