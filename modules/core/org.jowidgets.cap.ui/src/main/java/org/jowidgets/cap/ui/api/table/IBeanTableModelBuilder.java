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

import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.model.IBeanModelBuilder;
import org.jowidgets.cap.ui.api.sort.ISortModelConfig;
import org.jowidgets.service.api.IServiceId;

public interface IBeanTableModelBuilder<BEAN_TYPE> extends IBeanModelBuilder<BEAN_TYPE, IBeanTableModelBuilder<BEAN_TYPE>> {

	IBeanTableModelBuilder<BEAN_TYPE> setEntityLabelSingular(String label);

	IBeanTableModelBuilder<BEAN_TYPE> setEntityLabelPlural(String label);

	@Deprecated
	/**
	 * @deprecated
	 * @param readerService
	 * @param paramProvider
	 * @return
	 */
	<PARAM_TYPE> IBeanTableModelBuilder<BEAN_TYPE> setReaderService(
		IReaderService<PARAM_TYPE> readerService,
		IReaderParameterProvider<PARAM_TYPE> paramProvider);

	@Deprecated
	/**
	 * @deprecated
	 * @param readerService
	 * @param paramProvider
	 * @return
	 */
	<PARAM_TYPE> IBeanTableModelBuilder<BEAN_TYPE> setReaderService(
		IServiceId<IReaderService<PARAM_TYPE>> readerServiceId,
		IReaderParameterProvider<PARAM_TYPE> paramProvider);

	IBeanTableModelBuilder<BEAN_TYPE> setSorting(ISortModelConfig sorting);

	IBeanTableModelBuilder<BEAN_TYPE> setAutoRowCount(boolean autoRowCount);

	IBeanTableModelBuilder<BEAN_TYPE> setAutoSelection(boolean autoSelect);

	IBeanTableModelBuilder<BEAN_TYPE> setAutoRefreshSelection(boolean autoRefresh);

	IBeanTableModelBuilder<BEAN_TYPE> setAutoDisposeInvisibelPages(boolean autoDispose);

	IBeanTableModelBuilder<BEAN_TYPE> setClearOnEmptyFilter(boolean clearOnEmptyFilter);

	/**
	 * If this option is set, data will only be loaded if any parent bean is selected.
	 * Otherwise data will be cleared on load.
	 * The default value is false, if no parent is set and true if a parent is set
	 * 
	 * @param clearOnEmptyFilter
	 * @return This builder
	 */
	IBeanTableModelBuilder<BEAN_TYPE> setClearOnEmptyParentBeans(boolean clearOnEmptyParentBeans);

	IBeanTableModelBuilder<BEAN_TYPE> setPageSize(int pageSize);

	/**
	 * If last bean is enabled, the table has a transient bean at the end of the table that can be used
	 * to create new data. If the last been will be modified, a new last bean will be added to the table, so
	 * data can be created by clicking into the last row or navigation with help of cell editors into the last
	 * row.
	 * 
	 * @param lastBeanEnabled
	 * @return this builder
	 */
	IBeanTableModelBuilder<BEAN_TYPE> setLastBeanEnabled(boolean lastBeanEnabled);

	/**
	 * If sorted updates are used, added beans will be kept sorted.
	 * 
	 * Firstly this means beans inserted by updates from the reader service will
	 * be inserted by the current sort order, otherwise new beans will be added at the end.
	 * 
	 * In addition, updated beans will update their position.
	 * 
	 * Also the order between beans in the same update are expected to already respect the sorting order.
	 * 
	 * Independent of how this flag is set, added Data will always appear after the last page!
	 * 
	 * @param addUpdatesSorted
	 * @return this builder
	 */
	IBeanTableModelBuilder<BEAN_TYPE> setAddUpdatesSorted(boolean addUpdatesSorted);

	/**
	 * If set to true, the default values of the attributes changes each time a property will be modified
	 * 
	 * @param useLastModificationForDefault
	 * 
	 * @return This builder
	 */
	IBeanTableModelBuilder<BEAN_TYPE> setUseLastModificationForDefault(boolean useLastModificationForDefault);

	/**
	 * If true, the table will only load data for the page or pages currently visible.
	 * Otherwise, all beans will all be loaded at once and be treaded like added data.
	 * 
	 * @param pagingEnabled
	 * @return This builder
	 */
	IBeanTableModelBuilder<BEAN_TYPE> setPagingEnabled(boolean pagingEnabled);

	IBeanTableModelBuilder<BEAN_TYPE> addCellRenderer(IBeanTableCellRenderer<BEAN_TYPE> renderer);

	/**
	 * Sets the bean execution policy.
	 * 
	 * The default value is batch
	 * 
	 * @param policy The policy to set, must not be null
	 * 
	 * @return This builder
	 */
	IBeanTableModelBuilder<BEAN_TYPE> setSaveExecutionPolicy(BeanExecutionPolicy policy);

	IBeanTableModel<BEAN_TYPE> build();

}
