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

package org.jowidgets.remoting.service.client.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jowidgets.remoting.service.client.api.IRemoteServiceClient;
import org.jowidgets.remoting.service.client.api.IRemoteServiceClientBuilder;
import org.jowidgets.util.Assert;

final class RemoteServiceClientBuilder implements IRemoteServiceClientBuilder {

	private static final long DEFAULT_TIMEOUT = 900000;//15 minutes

	private final InvocationCallbackService invocationCallbackService;
	private final Object clientId;

	private long defaulTimeout;
	private ExecutorService executorService;

	RemoteServiceClientBuilder(final Object clientId, final InvocationCallbackService invocationCallbackService) {
		Assert.paramNotNull(invocationCallbackService, "invocationCallbackService");
		this.defaulTimeout = DEFAULT_TIMEOUT;
		this.invocationCallbackService = invocationCallbackService;
		this.clientId = clientId;
	}

	@Override
	public IRemoteServiceClientBuilder setDefaultTimeout(final long timeout) {
		this.defaulTimeout = timeout;
		return this;
	}

	@Override
	public IRemoteServiceClientBuilder setExecutorService(final ExecutorService executorService) {
		Assert.paramNotNull(executorService, "executorService");
		this.executorService = executorService;
		return this;
	}

	private ExecutorService getExecutorService() {
		if (executorService != null) {
			return executorService;
		}
		else {
			return Executors.newFixedThreadPool(100);
		}
	}

	@Override
	public IRemoteServiceClient build() {
		return new RemoteServiceClient(clientId, invocationCallbackService, getExecutorService(), defaulTimeout);
	}

}
