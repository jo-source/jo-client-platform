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

package org.jowidgets.cap.ui.api.tree;

import java.util.Collection;
import java.util.List;

import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyContext;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.filter.IUiFilter;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IDataModel;
import org.jowidgets.cap.ui.api.model.ILabelModel;
import org.jowidgets.cap.ui.api.sort.ISortModel;
import org.jowidgets.cap.ui.api.types.IEntityTypeId;
import org.jowidgets.util.IProvider;
import org.jowidgets.util.ITypedKey;

public interface IBeanRelationNodeModel<PARENT_BEAN_TYPE, CHILD_BEAN_TYPE> extends
		ILabelModel,
		IDataModel,
		IBeanListModel<CHILD_BEAN_TYPE> {

	ITypedKey<Boolean> IS_PAGE_END_DUMMY = new ITypedKey<Boolean>() {};

	/**
	 * Loads the model if not yet done.
	 * 
	 * @return true, if load will be done, false if load will not be done because it was invoked before
	 */
	boolean loadIfNotYetDone();

	boolean hasInitialLoad();

	void loadNextPage();

	void load(IResultCallback<Void> resultCallback);

	ICreatorService getCreatorService();

	IReaderService<Object> getReaderService();

	IProvider<Object> getReaderParameterProvider();

	IBeanProxy<PARENT_BEAN_TYPE> getParentBean();

	IEntityTypeId<PARENT_BEAN_TYPE> getParentEntityTypeId();

	Object getParentEntityId();

	Object getParentBeanTypeId();

	Class<PARENT_BEAN_TYPE> getParentBeanType();

	IEntityTypeId<CHILD_BEAN_TYPE> getChildEntityTypeId();

	Object getChildEntityId();

	Object getChildBeanTypeId();

	Class<CHILD_BEAN_TYPE> getChildBeanType();

	IBeanProxyContext getBeanProxyContext();

	IBeanProxyLabelRenderer<CHILD_BEAN_TYPE> getChildRenderer();

	List<IAttribute<Object>> getChildBeanAttributes();

	List<IBeanPropertyValidator<CHILD_BEAN_TYPE>> getChildBeanPropertyValidators();

	List<IEntityTypeId<Object>> getChildRelations();

	List<IBeanProxy<CHILD_BEAN_TYPE>> getSelectedBeans();

	void setSelectedBeans(Collection<? extends IBeanProxy<CHILD_BEAN_TYPE>> selectedBeans);

	void refreshBean(final IBeanProxy<CHILD_BEAN_TYPE> bean);

	void refreshBeans(final Collection<IBeanProxy<CHILD_BEAN_TYPE>> beans);

	void setFilter(String id, IUiFilter filter);

	void addFilter(String id, IUiFilter addedFilter);

	void removeFiltersForProperty(String id, String propertyName);

	void removeFilter(String id);

	ISortModel getSortModel();

	IUiFilter getFilter(String id);

	void dispose();

}
