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

package org.jowidgets.cap.security.common.tools;

import java.io.Serializable;

import org.jowidgets.cap.security.common.api.CapSecurityCommonToolkit;
import org.jowidgets.cap.security.common.api.ISecureServiceId;
import org.jowidgets.service.api.IServiceId;

public class SecureServiceId<SERVICE_TYPE, AUTHORIZATION_TYPE> implements
		ISecureServiceId<SERVICE_TYPE, AUTHORIZATION_TYPE>,
		Serializable {

	private static final long serialVersionUID = 6331902911986402142L;

	private final ISecureServiceId<SERVICE_TYPE, AUTHORIZATION_TYPE> original;

	public SecureServiceId(final AUTHORIZATION_TYPE authorization, final Class<?> serviceType) {
		this(authorization, serviceType, authorization);
	}

	@SuppressWarnings("unchecked")
	public SecureServiceId(final Object id, final Class<?> serviceType, final AUTHORIZATION_TYPE authorization) {
		this((ISecureServiceId<SERVICE_TYPE, AUTHORIZATION_TYPE>) CapSecurityCommonToolkit.serviceId(
				id,
				serviceType,
				authorization));
	}

	public SecureServiceId(final IServiceId<SERVICE_TYPE> serviceId, final AUTHORIZATION_TYPE authorization) {
		this(CapSecurityCommonToolkit.serviceId(serviceId, authorization));
	}

	private SecureServiceId(final ISecureServiceId<SERVICE_TYPE, AUTHORIZATION_TYPE> original) {
		this.original = original;
	}

	@Override
	public final Class<SERVICE_TYPE> getServiceType() {
		return original.getServiceType();
	}

	@Override
	public final AUTHORIZATION_TYPE getAuthorization() {
		return original.getAuthorization();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((original == null) ? 0 : original.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SecureServiceId)) {
			return false;
		}
		final SecureServiceId<SERVICE_TYPE, AUTHORIZATION_TYPE> other = (SecureServiceId<SERVICE_TYPE, AUTHORIZATION_TYPE>) obj;
		if (original == null) {
			if (other.original != null) {
				return false;
			}
		}
		else if (!original.equals(other.original)) {
			return false;
		}
		return true;
	}

	@Override
	public final String toString() {
		return original.toString();
	}

}
