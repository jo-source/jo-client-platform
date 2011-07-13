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

package org.jowidgets.cap.sample1.starter.standalone.common;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jowidgets.api.login.ILoginInterceptor;
import org.jowidgets.api.login.ILoginResultCallback;
import org.jowidgets.security.api.SecurityToolkit;
import org.jowidgets.security.tools.DefaultSecurityContext;
import org.jowidgets.util.Assert;

public class JaasLoginInterceptor implements ILoginInterceptor {

	private String jaasLoginContextName = "default";
	private String defaultErrorMessage = "Login failed (reason unknown)";

	public void setJaasLoginContextName(final String jaasLoginContextName) {
		Assert.paramNotNull(jaasLoginContextName, "jaasLoginContextName");
		this.jaasLoginContextName = jaasLoginContextName;
	}

	public void setDefaultErrorMessage(final String defaultErrorMessage) {
		Assert.paramNotNull(defaultErrorMessage, "defaultErrorMessage");
		this.defaultErrorMessage = defaultErrorMessage;
	}

	@Override
	public void login(final ILoginResultCallback resultCallback, final String username, final String password) {
		final AtomicReference<String> loginErrorMessage = new AtomicReference<String>(defaultErrorMessage);
		try {
			final LoginContext loginContext = new LoginContext(jaasLoginContextName, new CallbackHandler() {
				@Override
				public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
					for (int i = 0; i < callbacks.length; i++) {
						if (callbacks[i] instanceof NameCallback) {
							final NameCallback nc = (NameCallback) callbacks[i];
							nc.setName(username);
						}
						else if (callbacks[i] instanceof PasswordCallback) {
							final PasswordCallback pc = (PasswordCallback) callbacks[i];
							pc.setPassword(password.toCharArray());
						}
						else if (callbacks[i] instanceof TextOutputCallback) {
							final TextOutputCallback tc = (TextOutputCallback) callbacks[i];
							if (tc.getMessageType() == TextOutputCallback.ERROR) {
								loginErrorMessage.set(tc.getMessage());
							}
						}
						else {
							throw new UnsupportedCallbackException(callbacks[i]);
						}
					}
				}
			});
			loginContext.login();
			final Subject subject = loginContext.getSubject();
			if (subject != null) {
				SecurityToolkit.setSecurityContext(new DefaultSecurityContext(username));
				resultCallback.granted();
				return;
			}
		}
		catch (final LoginException e) {
			loginErrorMessage.set(e.getLocalizedMessage());
		}
		catch (final SecurityException e) {
			loginErrorMessage.set(e.getLocalizedMessage());
		}
		resultCallback.denied(loginErrorMessage.get());
	}

}
