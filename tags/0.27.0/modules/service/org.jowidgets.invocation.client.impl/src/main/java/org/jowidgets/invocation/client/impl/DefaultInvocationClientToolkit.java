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

package org.jowidgets.invocation.client.impl;

import java.util.HashMap;
import java.util.Map;

import org.jowidgets.invocation.client.api.IInvocationClient;
import org.jowidgets.invocation.client.api.IInvocationClientServiceRegistry;
import org.jowidgets.invocation.client.api.IInvocationClientToolkit;
import org.jowidgets.message.api.MessageToolkit;
import org.jowidgets.util.Assert;

public final class DefaultInvocationClientToolkit implements IInvocationClientToolkit {

	private final Map<Object, InvocationCallbackMessageReceiver> receivers;

	public DefaultInvocationClientToolkit() {
		this.receivers = new HashMap<Object, InvocationCallbackMessageReceiver>();
	}

	@Override
	public IInvocationClient getClient(final Object brokerId) {
		Assert.paramNotNull(brokerId, "brokerId");
		return getMessageReceiver(brokerId).getInvocationClient();
	}

	@Override
	public IInvocationClientServiceRegistry getClientRegistry(final Object brokerId) {
		Assert.paramNotNull(brokerId, "brokerId");
		return getMessageReceiver(brokerId).getInvocationClientServiceRegistry();
	}

	private InvocationCallbackMessageReceiver getMessageReceiver(final Object brokerId) {
		Assert.paramNotNull(brokerId, "brokerId");
		InvocationCallbackMessageReceiver result = receivers.get(brokerId);
		if (result == null) {
			result = createMessageReceiver(brokerId);
		}
		return result;
	}

	private synchronized InvocationCallbackMessageReceiver createMessageReceiver(final Object brokerId) {
		InvocationCallbackMessageReceiver result = receivers.get(brokerId);
		if (result == null) {
			final InvocationClientServiceRegistryImpl invocationClientServiceRegistry = new InvocationClientServiceRegistryImpl();
			final InvocationClientImpl invocationClient = new InvocationClientImpl(brokerId, invocationClientServiceRegistry);
			result = new InvocationCallbackMessageReceiver(invocationClient, invocationClientServiceRegistry);
			receivers.put(brokerId, result);
			MessageToolkit.setReceiver(brokerId, result);
		}
		return result;
	}
}
