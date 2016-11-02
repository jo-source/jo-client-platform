/*
 * Copyright (c) 2016, grossmann
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

package org.jowidgets.cap.ui.tools.model;

import org.jowidgets.cap.ui.api.model.IDataSaveListener;
import org.jowidgets.cap.ui.api.model.IDataSaveObservable;
import org.jowidgets.util.collection.IObserverSet;
import org.jowidgets.util.collection.IObserverSetFactory.Strategy;
import org.jowidgets.util.collection.ObserverSetFactory;

public class DataSaveObservable implements IDataSaveObservable {

	private final IObserverSet<IDataSaveListener> listeners;

	public DataSaveObservable() {
		this.listeners = ObserverSetFactory.create(Strategy.HIGH_PERFORMANCE);
	}

	@Override
	public final void addDataSaveListener(final IDataSaveListener dataSaveListener) {
		listeners.add(dataSaveListener);
	}

	@Override
	public final void removeDataSaveListener(final IDataSaveListener dataSaveListener) {
		listeners.remove(dataSaveListener);
	}

	public final void fireBeforeDataSave() {
		for (final IDataSaveListener listener : listeners) {
			listener.beforeDataSave();
		}
	}

	public final void fireAfterDataSaved() {
		for (final IDataSaveListener listener : listeners) {
			listener.afterDataSaved();
		}
	}

}
