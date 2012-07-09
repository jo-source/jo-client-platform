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

package org.jowidgets.cap.service.security.impl;

import java.io.Serializable;

import org.jowidgets.cap.service.security.api.ISecureServiceId;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.util.Assert;

final class SecureServiceIdImpl<SERVICE_TYPE, AUTHORIZATION_TYPE> implements
		ISecureServiceId<SERVICE_TYPE, AUTHORIZATION_TYPE>,
		Serializable {

	private static final long serialVersionUID = -5942074317483952205L;

	private final IServiceId<SERVICE_TYPE> serviceId;
	private final AUTHORIZATION_TYPE authorization;

	SecureServiceIdImpl(final IServiceId<SERVICE_TYPE> serviceId, final AUTHORIZATION_TYPE authorization) {
		Assert.paramNotNull(serviceId, "serviceId");
		Assert.paramNotNull(authorization, "authorization");
		this.serviceId = serviceId;
		this.authorization = authorization;
	}

	@Override
	public Class<SERVICE_TYPE> getServiceType() {
		return serviceId.getServiceType();
	}

	@Override
	public AUTHORIZATION_TYPE getAuthorization() {
		return authorization;
	}

	@Override
	public int hashCode() {
		return serviceId.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return serviceId.equals(obj);
	}

	@Override
	public String toString() {
		return "SecureServiceIdImpl [serviceId=" + serviceId + ", authorization=" + authorization + "]";
	}

}
