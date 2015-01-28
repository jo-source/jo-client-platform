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

package org.jowidgets.cap.remoting.client;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jowidgets.cap.common.api.exception.ServiceCanceledException;
import org.jowidgets.cap.remoting.common.RemotingBrokerId;
import org.jowidgets.service.api.IServiceProvider;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.service.tools.DefaultServiceProviderHolder;
import org.jowidgets.util.Assert;
import org.jowidgets.util.ICancelCallback;

public final class RemotingServiceInitializer {

	private final Object brokerId;

	private final AtomicBoolean initialized;

	public RemotingServiceInitializer() {
		this(RemotingBrokerId.DEFAULT_BROKER_ID);
	}

	public RemotingServiceInitializer(final Object brokerId) {
		Assert.paramNotNull(brokerId, "brokerId");
		this.brokerId = brokerId;
		this.initialized = new AtomicBoolean(false);
	}

	/**
	 * Initializes the remoting services
	 * 
	 * This method blocks until the remoting services are initialized or the timeout elapsed
	 * 
	 * @param timeout The timeout
	 * 
	 * @return True if the remoting services was initialized, false otherwise (e.g. timeout occured)
	 */
	public synchronized boolean initialize(final long timeout) {
		return initialize(timeout, null);
	}

	/**
	 * Initializes the remoting services.
	 * This method blocks until the remoting services are initialized, the invocation was canceled or the timeout elapsed
	 * 
	 * @param timeout The timeout
	 * @param cancelCallback Can be used to cancel the initialization
	 * 
	 * @return True if the remoting services was initialized, false otherwise (e.g. timeout occured)
	 */
	public synchronized boolean initialize(final long timeout, final ICancelCallback cancelCallback) {
		if (!initialized.get()) {
			final IServiceProvider serviceProvider;
			try {
				serviceProvider = RemotingServiceProviderFactory.create(brokerId, timeout, cancelCallback);
				ServiceProvider.registerServiceProviderHolder(new DefaultServiceProviderHolder(serviceProvider));
				initialized.set(true);
			}
			catch (final RemotingTimeoutException e) {
				//do nothing, because this method returns false then
			}
			catch (final ServiceCanceledException e) {
				//do nothing, because this method returns false then
			}
		}
		return initialized.get();
	}
}
