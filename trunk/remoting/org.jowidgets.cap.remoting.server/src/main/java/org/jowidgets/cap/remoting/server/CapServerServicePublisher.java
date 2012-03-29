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

package org.jowidgets.cap.remoting.server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.jowidgets.cap.remoting.common.CapInvocationMethodNames;
import org.jowidgets.invocation.service.server.api.IInvocationServiceServerRegistry;
import org.jowidgets.invocation.service.server.api.InvocationServiceServerToolkit;
import org.jowidgets.util.concurrent.DaemonThreadFactory;

public class CapServerServicePublisher {

	private static final long DEFAULT_PROGRESS_DELAY = 500;

	public void publishServices() {
		publishServices(Executors.newScheduledThreadPool(50, new DaemonThreadFactory()), DEFAULT_PROGRESS_DELAY);
	}

	public void publishServices(final ScheduledExecutorService progressExecutor, final long progressDelay) {

		final IInvocationServiceServerRegistry registry = InvocationServiceServerToolkit.getRegistry();

		registry.register(CapInvocationMethodNames.SERVICE_LOCATOR_METHOD_NAME, new ServiceLocatorMethod());
		registry.register(CapInvocationMethodNames.GENERIC_REMOTE_METHOD_NAME, new GenericRemoteMethod(
			progressExecutor,
			progressDelay));
	}
}
