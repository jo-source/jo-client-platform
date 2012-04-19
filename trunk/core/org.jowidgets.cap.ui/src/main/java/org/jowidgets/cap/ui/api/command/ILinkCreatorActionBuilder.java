/*
 * Copyright (c) 2011, H.Westphal
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
import org.jowidgets.cap.common.api.entity.IEntityLinkProperties;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.model.IDataModel;

public interface ILinkCreatorActionBuilder<BEAN_TYPE> extends ICapActionBuilder<ILinkCreatorActionBuilder<BEAN_TYPE>> {

	/**
	 * Sets the entity label plural.
	 * This will set a proper text with the entity label as a variable
	 * 
	 * @param label The label to set
	 * 
	 * @return This builder
	 */
	ILinkCreatorActionBuilder<BEAN_TYPE> setDestinationEntityLabelPlural(String label);

	ILinkCreatorActionBuilder<BEAN_TYPE> setLinkCreatorService(ICreatorService creatorService);

	ILinkCreatorActionBuilder<BEAN_TYPE> setLinkAttributes(List<? extends IAttribute<?>> attributes);

	ILinkCreatorActionBuilder<BEAN_TYPE> setLinkableTableAttributes(List<? extends IAttribute<?>> attributes);

	ILinkCreatorActionBuilder<BEAN_TYPE> setLinkableTableEntityId(Object id);

	ILinkCreatorActionBuilder<BEAN_TYPE> setLinkableTableLabel(String label);

	ILinkCreatorActionBuilder<BEAN_TYPE> setLinkableTableReaderService(IReaderService<Void> readerService);

	ILinkCreatorActionBuilder<BEAN_TYPE> setSourceProperties(IEntityLinkProperties properties);

	ILinkCreatorActionBuilder<BEAN_TYPE> setSourceProperties(String keyPropertyName, String foreignKeyPropertyName);

	ILinkCreatorActionBuilder<BEAN_TYPE> setDestinationProperties(IEntityLinkProperties properties);

	ILinkCreatorActionBuilder<BEAN_TYPE> setDestinationProperties(String keyPropertyName, String foreignKeyPropertyName);

	ILinkCreatorActionBuilder<BEAN_TYPE> setMultiSelection(boolean multiSelection);

	ILinkCreatorActionBuilder<BEAN_TYPE> addEnabledChecker(IEnabledChecker enabledChecker);

	ILinkCreatorActionBuilder<BEAN_TYPE> setModificationPolicy(BeanModificationStatePolicy policy);

	ILinkCreatorActionBuilder<BEAN_TYPE> setMessageStatePolicy(BeanMessageStatePolicy policy);

	ILinkCreatorActionBuilder<BEAN_TYPE> addExecutableChecker(IExecutableChecker<BEAN_TYPE> executableChecker);

	ILinkCreatorActionBuilder<BEAN_TYPE> setExceptionConverter(IBeanExceptionConverter exceptionConverter);

	ILinkCreatorActionBuilder<BEAN_TYPE> addExecutionInterceptor(IExecutionInterceptor interceptor);

	/**
	 * If a linked data model is set, it will be reloaded after a link was created
	 * 
	 * @param model The model to set
	 * 
	 * @return This builder
	 */
	ILinkCreatorActionBuilder<BEAN_TYPE> setLinkedDataModel(IDataModel model);

}
