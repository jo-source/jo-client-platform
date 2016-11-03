/*
 * Copyright (c) 2016, grossmann
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

package org.jowidgets.cap.ui.api.command;

import java.util.List;

import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.ordered.IOrderedBean;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.model.IDataModel;
import org.jowidgets.cap.ui.api.sort.ISortModel;

public interface IMoveOrderedBeanActionBuilder<BEAN_TYPE extends IOrderedBean>
		extends ICapActionBuilder<IMoveOrderedBeanActionBuilder<BEAN_TYPE>> {

	enum Direction {
		UP,
		DOWN;
	}

	/**
	 * Sets the sort model provider.
	 * 
	 * If a sort model provider is set, the order can only be changed, of the model is sorted by the order (in any direction)
	 * 
	 * @param sortModel The sort model provider to set
	 * 
	 * @return This builder
	 */
	IMoveOrderedBeanActionBuilder<BEAN_TYPE> setSortModelProvider(ISortModel sortModel);

	/**
	 * Sets the data model.
	 * 
	 * If a data model is set, the order changed will be saved on this model
	 * 
	 * @param dataModel The data model to set
	 * 
	 * @return This builder
	 */
	IMoveOrderedBeanActionBuilder<BEAN_TYPE> setDataModel(IDataModel dataModel);

	/**
	 * Sets the entity labels (singular and plural) with help of the entity id
	 * 
	 * The labels will be determined with the entity service if available
	 * 
	 * @param entityId The entity id to use resolving the entity labels
	 * 
	 * @return This builder
	 */
	IMoveOrderedBeanActionBuilder<BEAN_TYPE> setEntityLabels(Object entityId);

	/**
	 * Sets the entity label (plural).
	 * This will set a proper text with the entity label as a variable.
	 * 
	 * @param label The label to set
	 * 
	 * @return This builder
	 */
	IMoveOrderedBeanActionBuilder<BEAN_TYPE> setEntityLabelPlural(String label);

	/**
	 * Sets the entity label (singular).
	 * This will set a proper text with the entity label as a variable.
	 * 
	 * @param label The label to set
	 * 
	 * @return This builder
	 */
	IMoveOrderedBeanActionBuilder<BEAN_TYPE> setEntityLabelSingular(String label);

	/**
	 * Sets the enabled state message if the move is not possible an only one dataset is selected
	 * 
	 * @param message The message to set
	 * 
	 * @return This builder
	 */
	IMoveOrderedBeanActionBuilder<BEAN_TYPE> setMoveNotPossibleSingleSelectionMessage(String message);

	/**
	 * Sets the enabled state message if the move is not possible an more than one dataset is selected
	 * 
	 * @param message The message to set
	 * 
	 * @return This builder
	 */
	IMoveOrderedBeanActionBuilder<BEAN_TYPE> setMoveNotPossibleMultiSelectionMessage(String message);

	/**
	 * Sets the single selection property. By default, multiselection is used
	 * 
	 * @param singleSelection if true, only one dataset can be moved
	 *
	 * @return This builder
	 */
	IMoveOrderedBeanActionBuilder<BEAN_TYPE> setSingleSelection(boolean singleSelection);

	IMoveOrderedBeanActionBuilder<BEAN_TYPE> setMessageStatePolicy(BeanMessageStatePolicy policy);

	IMoveOrderedBeanActionBuilder<BEAN_TYPE> addEnabledChecker(IEnabledChecker enabledChecker);

	IMoveOrderedBeanActionBuilder<BEAN_TYPE> addExecutableChecker(IExecutableChecker<BEAN_TYPE> executableChecker);

	IMoveOrderedBeanActionBuilder<BEAN_TYPE> addExecutionInterceptor(IExecutionInterceptor<List<IBeanDto>> interceptor);

	IMoveOrderedBeanActionBuilder<BEAN_TYPE> setExceptionConverter(IBeanExceptionConverter exceptionConverter);
}
