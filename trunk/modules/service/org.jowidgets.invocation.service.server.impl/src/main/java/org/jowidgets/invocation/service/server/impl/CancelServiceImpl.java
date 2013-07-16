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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jowidgets.invocation.common.api.ICancelService;
import org.jowidgets.invocation.service.common.api.ICancelListener;
import org.jowidgets.util.Assert;

final class CancelServiceImpl implements ICancelService {

	private final Map<Object, Set<ICancelListener>> cancelListeners;

	CancelServiceImpl() {
		cancelListeners = new ConcurrentHashMap<Object, Set<ICancelListener>>();
	}

	@Override
	public void canceled(final Object invocationId) {
		Assert.paramNotNull(invocationId, "invocationId");
		final Set<ICancelListener> cancelListenerSet = cancelListeners.get(invocationId);
		if (cancelListenerSet != null) {
			for (final ICancelListener cancelListener : cancelListenerSet) {
				cancelListener.canceled();
			}
		}
		cancelListeners.remove(invocationId);
	}

	synchronized void registerInvocation(final Object invocationId) {
		Assert.paramNotNull(invocationId, "invocationId");
		cancelListeners.put(invocationId, new HashSet<ICancelListener>());
	}

	synchronized void registerCancelListener(final Object invocationId, final ICancelListener cancelListener) {
		Assert.paramNotNull(invocationId, "invocationId");
		Assert.paramNotNull(cancelListener, "cancelListener");
		final Set<ICancelListener> cancelListenerSet = cancelListeners.get(invocationId);
		if (cancelListenerSet != null) {
			cancelListenerSet.add(cancelListener);
		}
		else {
			cancelListener.canceled();
		}
	}

	synchronized void unregisterInvocation(final Object invocationId) {
		Assert.paramNotNull(invocationId, "invocationId");
		cancelListeners.remove(invocationId);
	}
}
