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
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.IAuthorizationProviderService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.security.api.SecurityContextHolder;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;

public final class BasicAuthenticationLoginInterceptor implements ILoginInterceptor {

	private static final String AUTHORIZATION_SERVICE_NOT_AVAILABLE = Messages.getString("BasicAuthenticationLoginInterceptor.authorization_service_not_available"); //$NON-NLS-1$
	private static final String LOGIN_FAILED = Messages.getString("BasicAuthenticationLoginInterceptor.login_failed"); //$NON-NLS-1$

	private final IServiceId<? extends IAuthorizationProviderService<?>> authorizationProviderServiceId;
	private final IUiThreadAccess uiThreadAccess;

	public BasicAuthenticationLoginInterceptor(
		final IServiceId<? extends IAuthorizationProviderService<?>> authorizationProviderServiceId) {
		Assert.paramNotNull(authorizationProviderServiceId, "authorizationProviderServiceId"); //$NON-NLS-1$
		this.authorizationProviderServiceId = authorizationProviderServiceId;
		this.uiThreadAccess = Toolkit.getUiThreadAccess();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void login(final ILoginResultCallback resultCallback, final String username, final String password) {
		final IAuthorizationProviderService<?> authorizationService = ServiceProvider.getService(authorizationProviderServiceId);
		if (authorizationService == null) {
			resultCallback.denied(AUTHORIZATION_SERVICE_NOT_AVAILABLE);
			return;
		}

		try {
			uiThreadAccess.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					final IExecutionTask executionTask = CapUiToolkit.executionTaskFactory().create();
					resultCallback.addCancelListener(new ILoginCancelListener() {
						@Override
						public void canceled() {
							executionTask.cancel();
						}
					});

					BasicAuthenticationInitializer.getInstance().setCredentials(username, password);
					authorizationService.getPrincipal(new IResultCallback() {
						@Override
						public void finished(final Object principal) {
							if (principal == null) {
								resultCallback.denied(LOGIN_FAILED);
								BasicAuthenticationInitializer.getInstance().clearCredentials();
							}
							else {
								SecurityContextHolder.setSecurityContext(principal);
								resultCallback.granted();
							}
						}

						@Override
						public void exception(final Throwable exception) {
							resultCallback.denied(exception.getLocalizedMessage());
							BasicAuthenticationInitializer.getInstance().clearCredentials();
						}
					}, executionTask);

				}
			});
		}
		catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}

	}

}
