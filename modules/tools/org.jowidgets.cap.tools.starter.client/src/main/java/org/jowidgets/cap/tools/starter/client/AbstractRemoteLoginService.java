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

package org.jowidgets.cap.tools.starter.client;

import org.jowidgets.api.login.ILoginCancelListener;
import org.jowidgets.api.login.ILoginInterceptor;
import org.jowidgets.api.login.ILoginResultCallback;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.cap.tools.starter.client.login.AbstractBasicAuthenticationRemoteLoginService;
import org.jowidgets.cap.tools.starter.client.remoting.RemotingBasicAuthenticationLoginInterceptor;
import org.jowidgets.common.image.IImageConstant;

public abstract class AbstractRemoteLoginService extends AbstractBasicAuthenticationRemoteLoginService {

	public AbstractRemoteLoginService(final String loginLabel) {
		super(loginLabel);
	}

	public AbstractRemoteLoginService(final IImageConstant logo) {
		super(logo);
	}

	public AbstractRemoteLoginService(final IImageConstant logo, final boolean decoratedLoginDialog) {
		super(logo, decoratedLoginDialog);
	}

	public AbstractRemoteLoginService(final String loginLabel, final boolean decoratedLoginDialog) {
		super(loginLabel, decoratedLoginDialog);
	}

	@Override
	public ILoginInterceptor createLoginInterceptor() {
		final ILoginInterceptor original = new RemotingBasicAuthenticationLoginInterceptor(getAuthorizationProviderServiceId());
		return new ILoginInterceptor() {

			@Override
			public void login(
				final ILoginResultCallback resultCallback,
				final String username,
				final String password,
				final IUiThreadAccess uiThreadAccess) {

				final ILoginResultCallback decoratedCallback = new ILoginResultCallback() {

					@Override
					public void addCancelListener(final ILoginCancelListener cancelListener) {
						resultCallback.addCancelListener(cancelListener);
					}

					@Override
					public void granted() {
						afterLoginSuccess();
						resultCallback.granted();
					}

					@Override
					public void denied(final String reason) {
						afterLoginFailed(reason);
						resultCallback.denied(reason);
					}
				};
				original.login(decoratedCallback, username, password, uiThreadAccess);

			}
		};

	}

	/**
	 * Will be invoked after a successful login
	 * 
	 * Feel free to override this method.
	 */
	public void afterLoginSuccess() {}

	/**
	 * Will be invoked after login failed.
	 * 
	 * Feel free to override this method.
	 */
	public void afterLoginFailed(final String reason) {}

}
