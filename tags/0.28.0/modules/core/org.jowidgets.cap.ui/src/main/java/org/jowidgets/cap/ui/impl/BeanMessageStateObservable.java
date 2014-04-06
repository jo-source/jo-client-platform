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

package org.jowidgets.cap.ui.impl;

import java.util.HashSet;
import java.util.Set;

import org.jowidgets.cap.ui.api.bean.IBeanMessageStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanMessageStateObservable;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;

class BeanMessageStateObservable<BEAN_TYPE> implements IBeanMessageStateObservable<BEAN_TYPE> {

	private final Set<IBeanMessageStateListener<BEAN_TYPE>> listeners;

	BeanMessageStateObservable() {
		this.listeners = new HashSet<IBeanMessageStateListener<BEAN_TYPE>>();
	}

	@Override
	public final void addMessageStateListener(final IBeanMessageStateListener<BEAN_TYPE> listener) {
		listeners.add(listener);
	}

	@Override
	public final void removeMessageStateListener(final IBeanMessageStateListener<BEAN_TYPE> listener) {
		listeners.remove(listener);
	}

	public final void fireMessageStateChanged(final IBeanProxy<BEAN_TYPE> bean) {
		for (final IBeanMessageStateListener<BEAN_TYPE> listener : listeners) {
			listener.messageStateChanged(bean);
		}
	}

	public final void dispose() {
		listeners.clear();
	}
}
