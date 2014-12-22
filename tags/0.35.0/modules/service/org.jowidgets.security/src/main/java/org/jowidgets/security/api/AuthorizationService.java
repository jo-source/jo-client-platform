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

import org.jowidgets.classloading.api.SharedClassLoader;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class AuthorizationService {

	private static final IAuthorizationService AUTHORIZATION_SERVICE = createAuthorizationService();

	private AuthorizationService() {}

	public static <PRINCIPAL_TYPE> IAuthorizationService<PRINCIPAL_TYPE> getAuthorizationService() {
		return AUTHORIZATION_SERVICE;
	}

	public static <PRINCIPAL_TYPE> PRINCIPAL_TYPE authorize(final PRINCIPAL_TYPE principal) {
		final IAuthorizationService<PRINCIPAL_TYPE> authorizationService = getAuthorizationService();
		return authorizationService.authorize(principal);
	}

	private static IAuthorizationService createAuthorizationService() {
		final ServiceLoader<IAuthorizationService> serviceLoader = ServiceLoader.load(
				IAuthorizationService.class,
				SharedClassLoader.getCompositeClassLoader());
		final Iterator<IAuthorizationService> iterator = serviceLoader.iterator();
		if (iterator.hasNext()) {
			final IAuthorizationService result = iterator.next();
			if (iterator.hasNext()) {
				throw new IllegalStateException("More than one implementation found for '"
					+ IAuthorizationService.class.getName()
					+ "'");
			}
			return result;
		}
		else {
			return new IAuthorizationService() {
				@Override
				public Object authorize(final Object principal) {
					return principal;
				}
			};
		}
	}
}
