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
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeFilter;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyContext;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.filter.IUiFilter;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IDataModel;
import org.jowidgets.cap.ui.api.sort.ISortModel;
import org.jowidgets.common.types.Interval;
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

	/**
	 * Gets the index of the given bean or -1 if the bean is not a element of the table
	 * 
	 * @param bean The bean to get the index for
	 * 
	 * @return The index of the bean or -1 if the bean is not a element of the table
	 */
	int getBeanIndex(IBeanProxy<BEAN_TYPE> bean);

	int getPageCount();

	void loadScheduled(int delayMillis);

	boolean isPageCreated(int pageIndex);

	boolean isPageLoading(int pageIndex);

	void loadPage(IResultCallback<Void> resultCallback, int page, IExecutionCallback executionCallback);

	void loadPage(int page);

	void updateInBackground(IResultCallback<Void> resultCallback, Interval<Integer> visibleRows);

	void updateInBackground(Interval<Integer> visibleRows);

	void refreshBean(IBeanProxy<BEAN_TYPE> bean);

	void refreshBeans(Collection<IBeanProxy<BEAN_TYPE>> beans);

	/**
	 * Could be used to update the table dynamically.
	 * 
	 * @param beansToRemove The beans that should be removed from the table
	 * @param beansToAdd The beans that should be added to the table (the beans will be added to the end, like new beans)
	 */
	void updateModel(Collection<? extends IBeanDto> beansToRemove, Collection<? extends IBeanDto> beansToAdd);

	Object getBeanTypeId();

	Class<BEAN_TYPE> getBeanType();

	ITableModel getTableModel();

	IBeanProxyContext getBeanProxyContext();

	ICreatorService getCreatorService();

	IDeleterService getDeleterService();

	IReaderService<Object> getReaderService();

	/**
	 * Creates a reader that could be used to read the beans from a table external.
	 * The read beans will not be associated with this model.
	 * This could be used e.g. to export beans of this table.
	 * 
	 * REMARK: The filter, sorting and parent beans will be used from this model, when the reader was created.
	 * They will not be updated on the external reader, when they will be changed inside the model.
	 * 
	 * 
	 * @return A new created external reader
	 */
	IExternalReader createExternalReader();

	IBeanSelectionProvider<Object> getParent();

	List<IBeanKey> getParentBeanKeys();

	int getColumnCount();

	IAttribute<Object> getAttribute(int columnIndex);

	IAttribute<Object> getAttribute(String propertyName);

	List<IAttribute<Object>> getAttributes();

	List<IAttribute<Object>> getAttributes(IAttributeFilter filter);

	List<String> getPropertyNames();

	List<IBeanPropertyValidator<BEAN_TYPE>> getBeanPropertyValidators();

	void addBeanValidator(IBeanValidator<BEAN_TYPE> beanValidator);

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

	/**
	 * If last bean is enabled, the table has a transient bean at the end of the table that can be used
	 * to create new data. If the last been will be modified, a new last bean will be added to the table, so
	 * data can be created by clicking into the last row or navigation with help of cell editors into the last
	 * row.
	 * 
	 * @param lastBeanEnabled
	 */
	void setLastBeanEnabled(boolean lastBeanEnabled);

	/**
	 * @return true, if the last bean is enabled, false otherwise
	 */
	boolean isLastBeanEnabled();

	/**
	 * If set to true, the default values of the attributes changes each time a property will be modified
	 * 
	 * @param useLastModificationForDefault
	 */
	void setUseLastModificationForDefault(boolean useLastModificationForDefault);

	void setConfig(IBeanTableModelConfig config);

	IBeanTableModelConfig getConfig();

	void dispose();

	boolean isDisposed();

}
