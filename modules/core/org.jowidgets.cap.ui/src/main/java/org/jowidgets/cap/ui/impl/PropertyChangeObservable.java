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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jowidgets.cap.ui.api.bean.IPropertyChangeObservable;
import org.jowidgets.util.Assert;
import org.jowidgets.util.collection.IObserverSet;
import org.jowidgets.util.collection.IObserverSetFactory.Strategy;
import org.jowidgets.util.collection.ObserverSetFactory;

class PropertyChangeObservable implements IPropertyChangeObservable {

	private final IObserverSet<PropertyChangeListener> listeners;

	PropertyChangeObservable() {
		this.listeners = ObserverSetFactory.create(Strategy.LOW_MEMORY);
	}

	@Override
	public final void addPropertyChangeListener(final PropertyChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public final void removePropertyChangeListener(final PropertyChangeListener listener) {
		listeners.remove(listener);
	}

	final void firePropertyChange(final PropertyChangeEvent event) {
		Assert.paramNotNull(event, "event");
		for (final PropertyChangeListener listener : listeners) {
			listener.propertyChange(event);
		}
	}

	final void firePropertyChange(final Object source, final String propertyName, final Object oldValue, final Object newValue) {
		firePropertyChange(new PropertyChangeEvent(source, propertyName, oldValue, newValue));
	}

	public final void dispose() {
		listeners.clear();
	}

}
