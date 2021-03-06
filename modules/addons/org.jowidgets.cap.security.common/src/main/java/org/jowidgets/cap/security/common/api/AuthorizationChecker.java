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

package org.jowidgets.cap.security.common.api;

public final class AuthorizationChecker {

	private AuthorizationChecker() {}

	/**
	 * Gets the default authorization checker. The default authorization checker uses the default security
	 * context and assumes that the context holder hold an IDefaultPrincipal. In other cases, class cast exceptions
	 * will occur on authorization checking
	 * 
	 * @return The default authorization checker, never null
	 */
	public static <AUTHORIZATION_TYPE> IAuthorizationChecker<AUTHORIZATION_TYPE> getDefault() {
		return CapSecurityCommonToolkit.defaultAuthorizationChecker();
	}

	/**
	 * Gets the authorization checker. This is the default authorization checker decorated with the plugged
	 * in authorization checkers (@see IAuthorizationCheckerDecoratorPlugin).
	 * 
	 * @return The authorization checker, never null
	 */
	public static <AUTHORIZATION_TYPE> IAuthorizationChecker<AUTHORIZATION_TYPE> get() {
		return CapSecurityCommonToolkit.authorizationChecker();
	}

	public static boolean hasAuthorization(final Object authorization) {
		return get().hasAuthorization(authorization);
	}
}
