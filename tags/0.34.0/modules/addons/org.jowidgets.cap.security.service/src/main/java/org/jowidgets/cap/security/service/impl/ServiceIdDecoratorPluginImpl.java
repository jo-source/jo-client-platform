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

package org.jowidgets.cap.security.service.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.ILinkCreatorService;
import org.jowidgets.cap.common.api.service.ILinkDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.security.common.api.CrudServiceType;
import org.jowidgets.cap.security.common.api.ICrudAuthorizationMapper;
import org.jowidgets.cap.security.common.api.SecureServiceId;
import org.jowidgets.cap.service.api.plugin.IServiceIdDecoratorPlugin;
import org.jowidgets.service.api.IServiceId;

final class ServiceIdDecoratorPluginImpl<AUTHORIZATION_TYPE> implements IServiceIdDecoratorPlugin {

	private static final Map<Class<?>, CrudServiceType> SERVICE_TYPES = createServiceTypes();

	private final List<ICrudAuthorizationMapper<AUTHORIZATION_TYPE>> mappers;

	ServiceIdDecoratorPluginImpl(final LinkedList<ICrudAuthorizationMapper<AUTHORIZATION_TYPE>> mappers) {
		this.mappers = new LinkedList<ICrudAuthorizationMapper<AUTHORIZATION_TYPE>>(mappers);
	}

	private static Map<Class<?>, CrudServiceType> createServiceTypes() {
		final Map<Class<?>, CrudServiceType> result = new HashMap<Class<?>, CrudServiceType>();
		result.put(ICreatorService.class, CrudServiceType.CREATE);
		result.put(ILinkCreatorService.class, CrudServiceType.CREATE);
		result.put(IReaderService.class, CrudServiceType.READ);
		result.put(IRefreshService.class, CrudServiceType.READ);
		result.put(IUpdaterService.class, CrudServiceType.UPDATE);
		result.put(IDeleterService.class, CrudServiceType.DELETE);
		result.put(ILinkDeleterService.class, CrudServiceType.DELETE);
		return result;
	}

	@Override
	public <SERVICE_TYPE> IServiceId<SERVICE_TYPE> decorateServiceId(
		final IServiceId<SERVICE_TYPE> defaultId,
		final IServiceId<IEntityService> entityServiceId,
		final Class<? extends IBean> beanType,
		final Object entityId,
		final Class<SERVICE_TYPE> serviceType) {

		final CrudServiceType crudServiceType = SERVICE_TYPES.get(serviceType);

		if (crudServiceType != null) {
			for (final ICrudAuthorizationMapper<AUTHORIZATION_TYPE> mapper : mappers) {
				final AUTHORIZATION_TYPE authorization = mapper.getAuthorization(beanType, entityId, crudServiceType);
				if (authorization != null) {
					return SecureServiceId.create(defaultId, authorization);
				}
			}
		}

		return defaultId;
	}
}
