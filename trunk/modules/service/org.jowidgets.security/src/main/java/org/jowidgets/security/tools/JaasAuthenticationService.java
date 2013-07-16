/*
 * Copyright (c) 2011, H.Westphal
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

package org.jowidgets.security.tools;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jowidgets.security.api.IAuthenticationService;
import org.jowidgets.security.api.ICredentials;
import org.jowidgets.security.api.IPrincipal;
import org.jowidgets.util.Assert;

public final class JaasAuthenticationService implements IAuthenticationService<IPrincipal<String>, ICredentials> {

	private String loginContextName = "default";

	public void setLoginContextName(final String loginContextName) {
		Assert.paramNotNull(loginContextName, "loginContextName");
		this.loginContextName = loginContextName;
	}

	@Override
	public IPrincipal<String> authenticate(final ICredentials credentials) {
		try {
			final LoginContext loginContext = new LoginContext(loginContextName, new CallbackHandler() {
				@Override
				public void handle(final Callback[] callbacks) throws UnsupportedCallbackException {
					for (final Callback callback : callbacks) {
						if (callback instanceof NameCallback) {
							final NameCallback nc = (NameCallback) callback;
							nc.setName(credentials.getUsername());
						}
						else if (callback instanceof PasswordCallback) {
							final PasswordCallback pc = (PasswordCallback) callback;
							pc.setPassword(credentials.getPassword().toCharArray());
						}
						else {
							throw new UnsupportedCallbackException(callback);
						}
					}
				}
			});
			loginContext.login();
			if (loginContext.getSubject() != null) {
				return new DefaultPrincipal(credentials.getUsername());
			}
		}
		catch (final LoginException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

}
