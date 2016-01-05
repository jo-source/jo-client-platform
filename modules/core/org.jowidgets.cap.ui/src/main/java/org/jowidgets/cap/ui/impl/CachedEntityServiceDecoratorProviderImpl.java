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

package org.jowidgets.cap.ui.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.IEntityInfo;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.tools.service.AbstractEntityService;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;

final class CachedEntityServiceDecoratorProviderImpl implements IServicesDecoratorProvider {

	private final int order;

	CachedEntityServiceDecoratorProviderImpl(final int order) {
		this.order = order;
	}

	@Override
	public <SERVICE_TYPE> IDecorator<SERVICE_TYPE> getDecorator(final IServiceId<SERVICE_TYPE> id) {
		Assert.paramNotNull(id, "id");
		final Class<? extends SERVICE_TYPE> serviceType = id.getServiceType();
		return new IDecorator<SERVICE_TYPE>() {
			@SuppressWarnings("unchecked")
			@Override
			public SERVICE_TYPE decorate(final SERVICE_TYPE original) {
				if (IEntityService.class.equals(serviceType) && original instanceof IEntityService) {
					return (SERVICE_TYPE) new CachedEntityServiceImpl((IEntityService) original);
				}
				else {
					return original;
				}
			}
		};
	}

	@Override
	public int getOrder() {
		return order;
	}

	private final class CachedEntityServiceImpl extends AbstractEntityService implements IEntityService {

		private final IEntityService original;

		private final Map<Object, IEntityInfo> cache;
		private final AtomicBoolean allAccessed;

		CachedEntityServiceImpl(final IEntityService original) {
			Assert.paramNotNull(original, "original");
			this.original = original;
			this.cache = new ConcurrentHashMap<Object, IEntityInfo>();
			this.allAccessed = new AtomicBoolean(false);
		}

		@Override
		public Collection<IEntityInfo> getEntityInfos() {
			if (!allAccessed.get()) {
				final Collection<IEntityInfo> entityInfos = original.getEntityInfos();
				for (final IEntityInfo entityInfo : entityInfos) {
					cache.put(entityInfo.getEntityId(), entityInfo);
				}
				allAccessed.set(true);
				return entityInfos;
			}
			else {
				return cache.values();
			}
		}

		@Override
		public IEntityInfo getEntityInfo(final Object entityId) {
			if (entityId == null) {
				return null;
			}
			IEntityInfo result;
			if (!cache.containsKey(entityId)) {
				result = original.getEntityInfo(entityId);
				if (result == null) {
					result = new NullEntityInfo(entityId);
				}
				cache.put(entityId, result);
			}
			else {
				result = cache.get(entityId);
			}
			return result;
		}

		@Override
		public void clearCache() {
			allAccessed.set(false);
			cache.clear();
		}

	}

	private static final class NullEntityInfo implements IEntityInfo {

		private final Object entityId;

		NullEntityInfo(final Object entityId) {
			Assert.paramNotNull(entityId, "entityId");
			this.entityId = entityId;
		}

		@Override
		public Object getEntityId() {
			return entityId;
		}

		@Override
		public IBeanDtoDescriptor getDescriptor() {
			return null;
		}

		@Override
		public IBeanServicesProvider getBeanServices() {
			return null;
		}

		@Override
		public List<IEntityLinkDescriptor> getEntityLinks() {
			return Collections.emptyList();
		}

	}

}
