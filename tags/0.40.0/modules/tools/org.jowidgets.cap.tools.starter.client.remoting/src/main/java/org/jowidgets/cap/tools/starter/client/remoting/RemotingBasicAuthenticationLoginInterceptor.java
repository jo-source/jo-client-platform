/*
 * Copyright (c) 2015, MGrossmann
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

package org.jowidgets.cap.tools.starter.client.remoting;

import org.jowidgets.api.login.ILoginCancelListener;
import org.jowidgets.api.login.ILoginInterceptor;
import org.jowidgets.api.login.ILoginResultCallback;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.cap.common.api.service.IAuthorizationProviderService;
import org.jowidgets.cap.remoting.client.RemotingServiceInitializer;
import org.jowidgets.cap.remoting.common.RemotingBrokerId;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.security.impl.http.client.BasicAuthenticationLoginInterceptor;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.util.Assert;
import org.jowidgets.util.CancelCallback;

public final class RemotingBasicAuthenticationLoginInterceptor implements ILoginInterceptor {

	private static final long REMOTING_INITIALIZATION_DEFAULT_TIMEOUT = 600;
	private static final IMessage SERVER_NOT_AVAILABLE = Messages.getMessage("RemotingBasicAuthenticationLoginInterceptor.ServerNotAvailable");

	private final RemotingServiceInitializer remotingServiceInitializer;
	private final BasicAuthenticationLoginInterceptor basicAuthenticationLoginInterceptor;
	private final long timeout;

	public RemotingBasicAuthenticationLoginInterceptor(
		final IServiceId<? extends IAuthorizationProviderService<?>> authorizationProviderServiceId) {
		this(RemotingBrokerId.DEFAULT_BROKER_ID, authorizationProviderServiceId, null);
	}

	public RemotingBasicAuthenticationLoginInterceptor(
		final Object brokerId,
		final IServiceId<? extends IAuthorizationProviderService<?>> authorizationProviderServiceId) {
		this(brokerId, authorizationProviderServiceId, null);
	}

	public RemotingBasicAuthenticationLoginInterceptor(
		final Object brokerId,
		final IServiceId<? extends IAuthorizationProviderService<?>> authorizationProviderServiceId,
		final Long timeout) {
		Assert.paramNotNull(brokerId, "brokerId");
		Assert.paramNotNull(authorizationProviderServiceId, "authorizationProviderServiceId");

		this.remotingServiceInitializer = new RemotingServiceInitializer(brokerId);
		this.basicAuthenticationLoginInterceptor = new BasicAuthenticationLoginInterceptor(authorizationProviderServiceId);
		this.timeout = timeout != null ? timeout.longValue() : REMOTING_INITIALIZATION_DEFAULT_TIMEOUT;
	}

	@Override
	public void login(
		final ILoginResultCallback resultCallback,
		final String username,
		final String password,
		final IUiThreadAccess uiThreadAccess) {

		final CancelCallback cancelCallback = new CancelCallback();
		resultCallback.addCancelListener(new ILoginCancelListener() {
			@Override
			public void canceled() {
				cancelCallback.cancel();
			}
		});

		boolean servicesInitialized = false;
		try {
			servicesInitialized = remotingServiceInitializer.initialize(timeout, cancelCallback);
		}
		catch (final Exception e) {
			resultCallback.denied(e.getLocalizedMessage());
		}

		if (servicesInitialized) {
			basicAuthenticationLoginInterceptor.login(resultCallback, username, password, uiThreadAccess);
		}
		else {
			resultCallback.denied(SERVER_NOT_AVAILABLE.get());
		}

	}

}
