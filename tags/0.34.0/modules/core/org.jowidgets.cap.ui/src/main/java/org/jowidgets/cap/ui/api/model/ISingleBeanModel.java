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

package org.jowidgets.cap.ui.api.model;

import java.util.Collection;

import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeFilter;
import org.jowidgets.cap.ui.api.attribute.IAttributeSet;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.util.event.IChangeObservable;

public interface ISingleBeanModel<BEAN_TYPE> extends
		IDataModel,
		IDataModelContextProvider,
		IBeanListModel<BEAN_TYPE>,
		IChangeObservable {

	Object getBeanTypeId();

	Class<BEAN_TYPE> getBeanType();

	Object getEntityId();

	void refresh();

	IBeanProxy<BEAN_TYPE> getBean();

	void setBean(IBeanProxy<BEAN_TYPE> bean);

	IAttribute<Object> getAttribute(int columnIndex);

	IAttribute<Object> getAttribute(String propertyName);

	Collection<IAttribute<Object>> getAttributes();

	Collection<IAttribute<Object>> getAttributes(IAttributeFilter filter);

	IAttributeSet getAttributeSet();

	void addBeanValidator(IBeanValidator<BEAN_TYPE> beanValidator);

	IReaderService<Object> getReaderService();
}
