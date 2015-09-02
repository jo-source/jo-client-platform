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

package org.jowidgets.cap.ui.api.bean;

import java.util.Set;

import org.jowidgets.cap.ui.api.model.IBeanListModelBeansObservable;
import org.jowidgets.cap.ui.api.model.IModificationStateObservable;
import org.jowidgets.cap.ui.api.model.IProcessStateObservable;
import org.jowidgets.validation.IValidateable;

public interface IBeansStateTracker<BEAN_TYPE> extends
		IValidateable,
		IModificationStateObservable,
		IProcessStateObservable,
		IBeanListModelBeansObservable<BEAN_TYPE> {

	void register(IBeanProxy<BEAN_TYPE> bean);

	void unregister(IBeanProxy<BEAN_TYPE> bean);

	Set<IBeanProxy<BEAN_TYPE>> getBeansToUpdate();

	Set<IBeanProxy<BEAN_TYPE>> getMasterBeansToUpdate();

	Set<IBeanProxy<BEAN_TYPE>> getBeansToCreate();

	boolean hasBeansToUpdate();

	boolean hasBeansToSave();

	/**
	 * @return True if there are beans to update or beans to create
	 */
	boolean hasModifications();

	void undoModifications();

	Set<IBeanProxy<BEAN_TYPE>> getExecutingBeans();

	boolean hasExecutingBeans();

	void cancelExecutions();

	void clearModifications();

	void clearAll();

	void dispose();

}
