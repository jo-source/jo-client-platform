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

import org.jowidgets.remoting.service.common.api.IInvocationCallback;
import org.jowidgets.remoting.service.common.api.IInterimResponseCallback;
import org.jowidgets.remoting.service.common.api.IInterimRequestCallback;

@SuppressWarnings({"rawtypes", "unchecked"})
final class MethodInvocationContext {

	private final Object serverId;
	private final IInvocationCallback invocationCallback;
	private final IInterimResponseCallback interimResponseCallback;
	private final IInterimRequestCallback interimRequestCallback;
	private final long timeout;
	private final long timestamp;

	MethodInvocationContext(
		final Object serverId,
		final IInvocationCallback<?> invocationCallback,
		final IInterimResponseCallback<?> interimResponseCallback,
		final IInterimRequestCallback<?, ?> interimRequestCalbback,
		final long timeout,
		final long timestamp) {

		this.serverId = serverId;
		this.invocationCallback = invocationCallback;
		this.interimResponseCallback = interimResponseCallback;
		this.interimRequestCallback = interimRequestCalbback;
		this.timeout = timeout;
		this.timestamp = timestamp;
	}

	IInvocationCallback<Object> getResultCallback() {
		return invocationCallback;
	}

	IInterimResponseCallback<Object> getInterimResponseCallback() {
		return interimResponseCallback;
	}

	IInterimRequestCallback<Object, Object> getInterimRequestCallback() {
		return interimRequestCallback;
	}

	long getTimeout() {
		return timeout;
	}

	long getTimestamp() {
		return timestamp;
	}

	Object getServerId() {
		return serverId;
	}

}
