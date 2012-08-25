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

package org.jowidgets.cap.security.common.impl;

import org.jowidgets.cap.security.common.api.IAuthorizationChecker;
import org.jowidgets.cap.security.common.api.ICapSecurityCommonToolkit;
import org.jowidgets.cap.security.common.api.ICrudAuthorizationMapperFactory;
import org.jowidgets.cap.security.common.api.ISecureEntityId;
import org.jowidgets.cap.security.common.api.ISecureServiceId;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.tools.ServiceId;

public final class CapSecurityCommonToolkitImpl implements ICapSecurityCommonToolkit {

	private final IAuthorizationChecker<Object> defaultAuthorizationChecker;
	private final IAuthorizationChecker<Object> authorizationChecker;

	private ICrudAuthorizationMapperFactory crudAuthorizationMapperProvider;

	public CapSecurityCommonToolkitImpl() {
		this.defaultAuthorizationChecker = new DefaultAuthorizationChecker();
		this.authorizationChecker = new LazyAuthorizationChecker(defaultAuthorizationChecker);
	}

	@Override
	public <SERVICE_TYPE, AUTHORIZATION_TYPE> ISecureServiceId<SERVICE_TYPE, AUTHORIZATION_TYPE> serviceId(
		final Object id,
		final Class<?> serviceType,
		final AUTHORIZATION_TYPE authorization) {
		return serviceId(new ServiceId<SERVICE_TYPE>(id, serviceType), authorization);
	}

	@Override
	public <SERVICE_TYPE, AUTHORIZATION_TYPE> ISecureServiceId<SERVICE_TYPE, AUTHORIZATION_TYPE> serviceId(
		final IServiceId<SERVICE_TYPE> serviceId,
		final AUTHORIZATION_TYPE authorization) {
		return new SecureServiceIdImpl<SERVICE_TYPE, AUTHORIZATION_TYPE>(serviceId, authorization);
	}

	@Override
	public <AUTHORIZATION_TYPE> ISecureEntityId<AUTHORIZATION_TYPE> entityId(
		final Object id,
		final AUTHORIZATION_TYPE create,
		final AUTHORIZATION_TYPE read,
		final AUTHORIZATION_TYPE update,
		final AUTHORIZATION_TYPE delete) {
		return new SecureEntityIdImpl<AUTHORIZATION_TYPE>(id, create, read, update, delete);
	}

	@Override
	public ICrudAuthorizationMapperFactory crudAuthorizationMapperFactory() {
		if (crudAuthorizationMapperProvider == null) {
			crudAuthorizationMapperProvider = new CrudAuthorizationMapperFactoryImpl();
		}
		return crudAuthorizationMapperProvider;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <AUTHORIZATION_TYPE> IAuthorizationChecker<AUTHORIZATION_TYPE> defaultAuthorizationChecker() {
		return (IAuthorizationChecker<AUTHORIZATION_TYPE>) defaultAuthorizationChecker;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <AUTHORIZATION_TYPE> IAuthorizationChecker<AUTHORIZATION_TYPE> authorizationChecker() {
		return (IAuthorizationChecker<AUTHORIZATION_TYPE>) authorizationChecker;
	}

}
