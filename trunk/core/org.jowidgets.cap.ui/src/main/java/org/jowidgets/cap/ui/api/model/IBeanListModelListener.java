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

package org.jowidgets.cap.ui.api.model;

import org.jowidgets.cap.ui.api.bean.IBeanProxy;

public interface IBeanListModelListener<BEAN_TYPE> {

	/**
	 * This method will be invoked, if the list of the beans model has been changed.
	 * E.g beans has bean added or removed or both or the complete structure of the list has been changed
	 * (e.g. after an (re)load of the model)
	 */
	void beansChanged();

	/**
	 * This method will me invoked, if beans was added to the model with the addBean method.
	 * 
	 * Remark : This method will not be invoked after a (re)load of the data or a complete structure change of the list
	 * 
	 * @param addedBeans The beans that was added to the list
	 */
	void beansAdded(Iterable<IBeanProxy<BEAN_TYPE>> addedBeans);

	/**
	 * This method will me invoked, if beans was removed from the model with the removeBeans method.
	 * 
	 * Remark : This method will not be invoked after a (re)load of the data or a complete structure change of the list
	 * 
	 * @param addedBeans The beans that was removed from the list
	 */
	void beansRemoved(Iterable<IBeanProxy<BEAN_TYPE>> removeBeans);

}
