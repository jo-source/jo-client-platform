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

import java.util.Collections;
import java.util.Set;

import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.model.IBeanListModelObservable;
import org.jowidgets.util.Assert;
import org.jowidgets.util.collection.IObserverSet;
import org.jowidgets.util.collection.IObserverSetFactory.Strategy;
import org.jowidgets.util.collection.ObserverSetFactory;

class BeanListModelObservable<BEAN_TYPE> implements IBeanListModelObservable<BEAN_TYPE> {

	private final IObserverSet<IBeanListModelListener<BEAN_TYPE>> listeners;

	BeanListModelObservable() {
		this.listeners = ObserverSetFactory.create(Strategy.HIGH_PERFORMANCE);
	}

	@Override
	public final void addBeanListModelListener(final IBeanListModelListener<BEAN_TYPE> listener) {
		Assert.paramNotNull(listener, "listener");
		listeners.add(listener);
	}

	@Override
	public final void removeBeanListModelListener(final IBeanListModelListener<BEAN_TYPE> listener) {
		Assert.paramNotNull(listener, "listener");
		listeners.remove(listener);
	}

	final void fireBeansChanged() {
		for (final IBeanListModelListener<BEAN_TYPE> listener : listeners) {
			listener.beansChanged();
		}
	}

	final void fireBeansAdded(final IBeanProxy<BEAN_TYPE> addedBean) {
		final Set<IBeanProxy<BEAN_TYPE>> addedBeans = Collections.singleton(addedBean);
		fireBeansAdded(addedBeans);
	}

	@SuppressWarnings("unchecked")
	final void fireBeansAdded(final Iterable<? extends IBeanProxy<BEAN_TYPE>> addedBeans) {
		for (final IBeanListModelListener<BEAN_TYPE> listener : listeners) {
			listener.beansAdded((Iterable<IBeanProxy<BEAN_TYPE>>) addedBeans);
		}
	}

	@SuppressWarnings("unchecked")
	final void fireBeansRemoved(final Iterable<? extends IBeanProxy<BEAN_TYPE>> removeBeans) {
		for (final IBeanListModelListener<BEAN_TYPE> listener : listeners) {
			listener.beansRemoved((Iterable<IBeanProxy<BEAN_TYPE>>) removeBeans);
		}
	}

	void dispose() {
		listeners.clear();
	}

}
