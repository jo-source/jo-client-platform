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

package org.jowidgets.invocation.service.server.impl;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jowidgets.invocation.common.api.IResponseService;
import org.jowidgets.invocation.service.common.api.IInterimResponseCallback;
import org.jowidgets.util.Assert;

final class ResponseServiceImpl implements IResponseService {

	private final Map<Object, IInterimResponseCallback<Object>> interimResponseCallback;

	ResponseServiceImpl() {
		this.interimResponseCallback = new ConcurrentHashMap<Object, IInterimResponseCallback<Object>>();
	}

	@Override
	public void response(final Object invocationId, final Object requestId, final Object response) {
		Assert.paramNotNull(requestId, "requestId");
		final IInterimResponseCallback<Object> responseCallback = interimResponseCallback.get(requestId);
		if (responseCallback != null) {
			responseCallback.response(response);
		}
	}

	Object register(final IInterimResponseCallback<Object> callback) {
		final Object requestId = UUID.randomUUID();
		interimResponseCallback.put(requestId, callback);
		return requestId;
	}

}
