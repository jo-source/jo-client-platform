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

import java.util.ArrayList;
import java.util.Collection;

import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;

public interface IBeanListModel<BEAN_TYPE>
		extends IBeanListModelObservable<BEAN_TYPE>, IBeanListModelBeansObservable<BEAN_TYPE>, IBeanSelectionProvider<BEAN_TYPE> {

	int getSize();

	IBeanProxy<BEAN_TYPE> getBean(int index);

	void removeBeans(Iterable<? extends IBeanProxy<BEAN_TYPE>> beans);

	void removeAllBeans();

	void addBean(IBeanProxy<BEAN_TYPE> bean);

	IBeanProxy<BEAN_TYPE> addBeanDto(IBeanDto beanDto);

	IBeanProxy<BEAN_TYPE> addTransientBean();

	ArrayList<Integer> getSelection();

	void setSelection(Collection<Integer> selection);

	void fireBeansChanged();

	/**
	 * Provides a checker that checks if data can be added to the model. E.g. if the model is a child and
	 * the parent selection is empty. so data should be added.
	 * 
	 * Implementors may return null if this feature should not be supported
	 * 
	 * @return The enabled checker or null
	 */
	IEnabledChecker getDataAddableChecker();

}
