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

package org.jowidgets.cap.ui.api.lookup;

import java.util.concurrent.TimeUnit;

public interface ILookUpAccess {

	/**
	 * Adds a callback to get the look up asynchronous, and then get informed about look up changes.
	 * This method forces the look up to get initialized, if not already done.
	 * 
	 * @see ILookUpAccess#initialize()
	 * 
	 * @param callback The callback that retrieves the look up changes.
	 * @param weak If weak is true, the callback will be hold in a weak reference, otherwise the callback will
	 *            be hold in a strong reference.
	 *            Remark: If weak is true, anonymous implementations of @link ILookUpCallback will fail.
	 */
	void addCallback(ILookUpCallback callback, boolean weak);

	/**
	 * Removes a calback to get no longer informed about look up changes
	 * 
	 * @param callback The callback to remove
	 */
	void removeCallback(ILookUpCallback callback);

	/**
	 * Forces the look up to get initialized, if not already done.
	 * This method will be invoked automatically, if the method @link {@link ILookUpAccess#addCallback(ILookUpCallback)} will be
	 * invoked.
	 * 
	 * Warning: After invoking this method, the method @link {@link ILookUpAccess#isInitialized()} may return false, because
	 * lookUp's are loaded asynchronous.
	 * Use the method @link {@link ILookUpAccess#addCallback(ILookUpCallback)} to get the lookUp
	 * asynchronous.
	 */
	void initialize();

	/**
	 * @return True if the look up has already been initialized, false otherwise
	 */
	boolean isInitialized();

	/**
	 * Gets the current look up, if the look up is initialized.
	 * 
	 * Warning: The look up may be null,because lookUp's are loaded asynchronous.
	 * Use the method @link {@link ILookUpAccess#addCallback(ILookUpCallback)} to get the lookUp
	 * asynchronous.
	 * 
	 * @return the current look up, or null if the look up is not yet initialized
	 */
	ILookUp getCurrentLookUp();

	/**
	 * Gets the current look up synchronous. If the current look up is not yet initialized, this method
	 * blocks, until the lookup is initialized or the timeout reached.
	 * 
	 * @return The current lookup, or null if the timeout was reached
	 */
	ILookUp getCurrentLookUpSync(long timeout, TimeUnit unit);

	/**
	 * Adds a look up listener
	 * 
	 * @param listener the listener to add
	 * @param weak If weak is true, the listener will be hold in a weak reference, otherwise the listener will
	 *            be hold in a strong reference.
	 *            Remark: If weak is true, anonymous implementations of @link ILookUpListener will fail.
	 */
	void addLookUpListener(ILookUpListener listener, boolean weak);

	void removeLookUpListener(ILookUpListener listener);

}
