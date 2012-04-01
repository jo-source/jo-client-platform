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

package org.jowidgets.security.api;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.jowidgets.security.tools.DefaultCredentials;
import org.jowidgets.security.tools.DefaultPrincipal;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class AuthenticationService {

	private static final IAuthenticationService AUTHENTICATION_SERVICE = createAuthenticationService();

	private AuthenticationService() {}

	public static <PRINCIPAL_TYPE, CREDENTIAL_TYPE> IAuthenticationService<PRINCIPAL_TYPE, CREDENTIAL_TYPE> getAuthenticationService() {
		return AUTHENTICATION_SERVICE;
	}

	public static <PRINCIPAL_TYPE, CREDENTIAL_TYPE> PRINCIPAL_TYPE authenticate(final CREDENTIAL_TYPE credentials) {
		final IAuthenticationService<PRINCIPAL_TYPE, CREDENTIAL_TYPE> authenticationService = getAuthenticationService();
		return authenticationService.authenticate(credentials);
	}

	private static IAuthenticationService createAuthenticationService() {
		final ServiceLoader<IAuthenticationService> serviceLoader = ServiceLoader.load(IAuthenticationService.class);
		final Iterator<IAuthenticationService> iterator = serviceLoader.iterator();
		if (iterator.hasNext()) {
			final IAuthenticationService result = iterator.next();
			if (iterator.hasNext()) {
				throw new IllegalStateException("More than one implementation found for '"
					+ IAuthenticationService.class.getName()
					+ "'");
			}
			return result;
		}
		else {
			return new IAuthenticationService() {
				@Override
				public Object authenticate(final Object credentials) {
					if (credentials instanceof DefaultCredentials) {
						return new DefaultPrincipal(((DefaultCredentials) credentials).getUsername());
					}
					return null;
				}
			};
		}
	}
}
