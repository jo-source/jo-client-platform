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

package org.jowidgets.invocation.server.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jowidgets.invocation.common.api.ICancelService;
import org.jowidgets.invocation.common.api.IMethod;
import org.jowidgets.invocation.common.api.IResponseService;
import org.jowidgets.invocation.common.impl.CancelMessage;
import org.jowidgets.invocation.common.impl.MethodInvocationMessage;
import org.jowidgets.invocation.common.impl.ResponseMessage;
import org.jowidgets.invocation.server.api.IInvocationServerServiceRegistry;
import org.jowidgets.util.Assert;

public class InvocationServerServiceRegistryImpl implements IInvocationServerServiceRegistry {

	private final Map<String, IMethod> methods;

	private ICancelService cancelService;
	private IResponseService responseService;

	InvocationServerServiceRegistryImpl() {
		this.methods = new ConcurrentHashMap<String, IMethod>();
	}

	@Override
	public synchronized void register(final String methodName, final IMethod method) {
		Assert.paramNotNull(methodName, "methodName");
		Assert.paramNotNull(method, "method");
		methods.put(methodName, method);
	}

	@Override
	public synchronized void register(final ICancelService cancelService) {
		Assert.paramNotNull(cancelService, "cancelService");
		this.cancelService = cancelService;
	}

	@Override
	public synchronized void register(final IResponseService responseService) {
		Assert.paramNotNull(responseService, "responseService");
		this.responseService = responseService;
	}

	public void onMethodInvocation(final MethodInvocationMessage message) {
		final IMethod method = methods.get(message.getMethodName());
		method.invoke(message.getInvocationId(), message.getParameter());
	}

	void onCancel(final CancelMessage message) {
		onCancel(message.getInvocationId());
	}

	void onCancel(final Object invocationId) {
		if (cancelService != null) {
			cancelService.canceled(invocationId);
		}
	}

	void onResponse(final ResponseMessage message) {
		if (responseService != null) {
			responseService.response(message.getRequestId(), message.getResponse());
		}
	}

}
