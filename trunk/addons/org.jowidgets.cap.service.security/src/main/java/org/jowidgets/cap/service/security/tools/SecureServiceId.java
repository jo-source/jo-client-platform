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

package org.jowidgets.cap.service.security.tools;

import java.io.Serializable;

import org.jowidgets.cap.service.security.api.ISecureServiceId;
import org.jowidgets.cap.service.security.api.SecureServiceToolkit;
import org.jowidgets.service.api.IServiceId;

public class SecureServiceId<SERVICE_TYPE, AUTHORIZATION_TYPE> implements
		ISecureServiceId<SERVICE_TYPE, AUTHORIZATION_TYPE>,
		Serializable {

	private static final long serialVersionUID = 6331902911986402142L;

	private final ISecureServiceId<SERVICE_TYPE, AUTHORIZATION_TYPE> original;

	@SuppressWarnings("unchecked")
	public SecureServiceId(final Object id, final Class<?> serviceType, final AUTHORIZATION_TYPE authorization) {
		this((ISecureServiceId<SERVICE_TYPE, AUTHORIZATION_TYPE>) SecureServiceToolkit.serviceId(id, serviceType, authorization));
	}

	public SecureServiceId(final IServiceId<SERVICE_TYPE> serviceId, final AUTHORIZATION_TYPE authorization) {
		this(SecureServiceToolkit.serviceId(serviceId, authorization));
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
	public final int hashCode() {
		return original.hashCode();
	}

	@Override
	public final boolean equals(final Object obj) {
		return original.equals(obj);
	}

	@Override
	public final String toString() {
		return original.toString();
	}

}
