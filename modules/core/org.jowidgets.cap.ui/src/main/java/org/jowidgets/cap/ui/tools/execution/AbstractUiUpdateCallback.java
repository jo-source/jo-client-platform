/*
 * Copyright (c) 2012, grossmann
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

package org.jowidgets.cap.ui.tools.execution;

import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.execution.IUpdatableResultCallback;

public abstract class AbstractUiUpdateCallback<UPDATE_TYPE, RESULT_TYPE>
		implements IUpdatableResultCallback<UPDATE_TYPE, RESULT_TYPE> {

	private final IUiThreadAccess uiThreadAccess;

	public AbstractUiUpdateCallback() {
		this.uiThreadAccess = Toolkit.getUiThreadAccess();
	}

	/**
	 * The finished method that will be invoked in the UI thread
	 * 
	 * @param result The result of the service
	 */
	protected abstract void finishedUi(RESULT_TYPE result);

	/**
	 * The update method that will be invoked in the UI thread
	 * 
	 * @param result The result of the service
	 */
	protected abstract void updateUi(UPDATE_TYPE result);

	/**
	 * The exception method that will be invoked in the UI thread
	 * 
	 * @param exception The exception that occurred
	 */
	protected abstract void exceptionUi(Throwable exception);

	@Override
	public final void finished(final RESULT_TYPE result) {
		uiThreadAccess.invokeLater(new Runnable() {
			@Override
			public void run() {
				finishedUi(result);
			}
		});
	}

	@Override
	public void update(final UPDATE_TYPE result) {
		uiThreadAccess.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateUi(result);
			}
		});
	}

	@Override
	public final void exception(final Throwable exception) {
		uiThreadAccess.invokeLater(new Runnable() {
			@Override
			public void run() {
				exceptionUi(exception);
			}
		});
	}

}
