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
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.service.ILinkDeleterService;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.model.IBeanListModel;

public interface ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> extends
		ICapActionBuilder<ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE>> {

	ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setSource(
		IBeanSelectionProvider<SOURCE_BEAN_TYPE> selectionProvider);

	ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setSourceMultiSelection(boolean multiSelection);

	ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setSourceModificationPolicy(BeanModificationStatePolicy policy);

	ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setSourceMessageStatePolicy(BeanMessageStatePolicy policy);

	ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> addSourceExecutableChecker(
		IExecutableChecker<SOURCE_BEAN_TYPE> executableChecker);

	ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setLinkDeleterService(ILinkDeleterService deleterService);

	ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> addEnabledChecker(IEnabledChecker enabledChecker);

	ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setExceptionConverter(IBeanExceptionConverter exceptionConverter);

	ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> addExecutionInterceptor(
		IExecutionInterceptor<List<IBeanDto>> interceptor);

	/**
	 * If auto selection is set, after bean deletion was delegated to the deleter service, the
	 * next bean (the bean after the last selected) will be selected automatically
	 * 
	 * @param autoSelection
	 * 
	 * @return This builder
	 */
	ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setAutoSelection(boolean autoSelection);

	ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setDeletionConfirmDialog(boolean deletionConfirmDialog);

	ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setLinkedModel(IBeanListModel<LINKED_BEAN_TYPE> model);

	/**
	 * Sets the entity label singular.
	 * This will set a proper text with the entity label as a variable for single selection
	 * 
	 * @param label The label to set
	 * 
	 * @return This builder
	 */
	ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setLinkedEntityLabelSingular(final String label);

	/**
	 * Sets the entity label plural.
	 * This will set a proper text with the entity label as a variable for multi selection
	 * 
	 * @param label The label to set
	 * 
	 * @return This builder
	 */
	ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setLinkedEntityLabelPlural(String label);

	ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setLinkedMultiSelection(boolean multiSelection);

	ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setLinkedModificationPolicy(BeanModificationStatePolicy policy);

	ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setLinkedMessageStatePolicy(BeanMessageStatePolicy policy);

	ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> addLinkedExecutableChecker(
		IExecutableChecker<LINKED_BEAN_TYPE> executableChecker);

}
