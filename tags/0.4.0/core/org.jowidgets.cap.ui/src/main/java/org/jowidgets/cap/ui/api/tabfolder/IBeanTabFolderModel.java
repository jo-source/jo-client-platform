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

package org.jowidgets.cap.ui.api.tabfolder;

import java.util.Collection;
import java.util.List;

import org.jowidgets.api.controller.IDisposeObservable;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.filter.IUiFilter;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IDataModel;
import org.jowidgets.cap.ui.api.sort.ISortModel;
import org.jowidgets.util.event.IChangeListener;

public interface IBeanTabFolderModel<BEAN_TYPE> extends IDataModel, IBeanListModel<BEAN_TYPE>, IDisposeObservable {

	Object getEntityId();

	Class<BEAN_TYPE> getBeanType();

	void refreshBean(IBeanProxy<BEAN_TYPE> bean);

	void refreshBeans(Collection<IBeanProxy<BEAN_TYPE>> beans);

	ICreatorService getCreatorService();

	IDeleterService getDeleterService();

	List<IBeanPropertyValidator<BEAN_TYPE>> getBeanPropertyValidators();

	void addBeanValidator(IBeanValidator<BEAN_TYPE> beanValidator);

	int getBeanIndex(IBeanProxy<BEAN_TYPE> bean);

	IBeanProxy<BEAN_TYPE> getSelectedBean();

	void setSelectedBean(IBeanProxy<BEAN_TYPE> selectedBean);

	void setSelection(int selection);

	Integer getSelectionIndex();

	void addFilterChangeListener(IChangeListener changeListener);

	void removeFilterChangeListener(IChangeListener changeListener);

	void setFilter(String id, IUiFilter filter);

	void addFilter(String id, IUiFilter addedFilter);

	void removeFiltersForProperty(String id, String propertyName);

	void removeFilter(String id);

	IUiFilter getFilter(String id);

	ISortModel getSortModel();

	IBeanProxyLabelRenderer<BEAN_TYPE> getLabelRenderer();

	void addBean(int index, IBeanProxy<BEAN_TYPE> bean);

	void removeBean(IBeanProxy<BEAN_TYPE> bean);

	void exchangeBean(final IBeanProxy<BEAN_TYPE> oldBean, final IBeanProxy<BEAN_TYPE> newBean);

	void dispose();

	boolean isDisposed();

}
