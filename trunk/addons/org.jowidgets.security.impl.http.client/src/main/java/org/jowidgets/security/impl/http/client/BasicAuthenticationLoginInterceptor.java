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

package org.jowidgets.security.impl.http.client;

import org.jowidgets.api.login.ILoginCancelListener;
import org.jowidgets.api.login.ILoginInterceptor;
import org.jowidgets.api.login.ILoginResultCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.IAuthorizationProviderService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.security.api.SecurityContextHolder;
import org.jowidgets.security.tools.DefaultPrincipal;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;

public final class BasicAuthenticationLoginInterceptor implements ILoginInterceptor {

	private final IServiceId<IAuthorizationProviderService<DefaultPrincipal>> authorizationProviderServiceId;

	public BasicAuthenticationLoginInterceptor(
		final IServiceId<IAuthorizationProviderService<DefaultPrincipal>> authorizationProviderServiceId) {
		Assert.paramNotNull(authorizationProviderServiceId, "authorizationProviderServiceId");
		this.authorizationProviderServiceId = authorizationProviderServiceId;
	}

	@Override
	public void login(final ILoginResultCallback resultCallback, final String username, final String password) {
		final IAuthorizationProviderService<DefaultPrincipal> authorizationService = ServiceProvider.getService(authorizationProviderServiceId);
		if (authorizationService == null) {
			// TODO i18n
			resultCallback.denied("Authorization service not available");
			return;
		}

		final IExecutionTask executionTask = CapUiToolkit.executionTaskFactory().create();
		resultCallback.addCancelListener(new ILoginCancelListener() {
			@Override
			public void canceled() {
				executionTask.cancel();
			}
		});

		BasicAuthenticationInitializer.getInstance().setCredentials(username, password);
		authorizationService.getPrincipal(new IResultCallback<DefaultPrincipal>() {
			@Override
			public void finished(final DefaultPrincipal principal) {
				if (principal == null) {
					// TODO i18n
					resultCallback.denied("Login failed");
					BasicAuthenticationInitializer.getInstance().clearCredentials();
				}
				else {
					SecurityContextHolder.setSecurityContext(principal);
					resultCallback.granted();
				}
			}

			@Override
			public void timeout() {
				// TODO i18n
				resultCallback.denied("Timeout");
				BasicAuthenticationInitializer.getInstance().clearCredentials();
			}

			@Override
			public void exception(final Throwable exception) {
				resultCallback.denied(exception.getLocalizedMessage());
				BasicAuthenticationInitializer.getInstance().clearCredentials();
			}
		}, executionTask);
	}

}
