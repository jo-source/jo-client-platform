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

package org.jowidgets.cap.ui.api.table;

import java.util.Collection;
import java.util.List;

import org.jowidgets.api.controller.IDisposeObservable;
import org.jowidgets.api.model.table.ITableModel;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeFilter;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.filter.IUiFilter;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IDataModel;
import org.jowidgets.cap.ui.api.sort.ISortModel;
import org.jowidgets.util.event.IChangeListener;

public interface IBeanTableModel<BEAN_TYPE> extends IDataModel, IBeanListModel<BEAN_TYPE>, IDisposeObservable {

	String UI_FILTER_ID = IBeanTableModel.class.getName() + ".UI_FILTER_ID";
	String UI_SEARCH_FILTER_ID = IBeanTableModel.class.getName() + ".UI_SEARCH_FILTER_ID";

	Object getEntityId();

	/**
	 * Gets the entity label in the singular form, e.g. Role, Authentication, Member, ...
	 * 
	 * @return The label in the singular form, never null (Default: us=Dataset, de=Datensatz)
	 */
	String getEntityLabelSingular();

	/**
	 * Gets the entity label in the plural form, e.g. Roles, Authentications, Members, ...
	 * 
	 * @return The label in the plural, never null (Default: us=Datasets, de=Datensätze)
	 */
	String getEntityLabelPlural();

	int getPageCount();

	void loadScheduled(int delayMillis);

	boolean isPageCreated(int pageIndex);

	boolean isPageLoading(int pageIndex);

	void loadPage(IResultCallback<Void> resultCallback, int page, IExecutionCallback executionCallback);

	void loadPage(int page);

	void refreshBean(IBeanProxy<BEAN_TYPE> bean);

	void refreshBeans(Collection<IBeanProxy<BEAN_TYPE>> beans);

	Class<BEAN_TYPE> getBeanType();

	ITableModel getTableModel();

	ICreatorService getCreatorService();

	IDeleterService getDeleterService();

	int getColumnCount();

	IAttribute<Object> getAttribute(int columnIndex);

	List<IAttribute<Object>> getAttributes();

	List<IAttribute<Object>> getAttributes(IAttributeFilter filter);

	Object getValue(int rowIndex, int columnIndex);

	IBeanProxy<BEAN_TYPE> getFirstSelectedBean();

	List<IBeanProxy<BEAN_TYPE>> getSelectedBeans();

	void setSelectedBeans(Collection<? extends IBeanProxy<BEAN_TYPE>> selectedBeans);

	void addSelectedBeans(Collection<? extends IBeanProxy<BEAN_TYPE>> selectedBeans);

	void addFilterChangeListener(IChangeListener changeListener);

	void removeFilterChangeListener(IChangeListener changeListener);

	void setFilter(String id, IUiFilter filter);

	void addFilter(String id, IUiFilter addedFilter);

	void removeFiltersForProperty(String id, String propertyName);

	void removeFiltersForProperty(String id, int columnIndex);

	void removeFilter(String id);

	IUiFilter getFilter(String id);

	ISortModel getSortModel();

	void setConfig(IBeanTableConfig config);

	IBeanTableConfig getConfig();

	void dispose();

	boolean isDisposed();

}