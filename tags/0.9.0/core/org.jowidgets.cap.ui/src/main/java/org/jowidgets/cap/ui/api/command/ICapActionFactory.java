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

package org.jowidgets.cap.ui.api.command;

import org.jowidgets.api.command.IAction;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;

public interface ICapActionFactory {

	IDataModelAction dataModelLoadAction();

	IDataModelActionBuilder dataModelLoadActionBuilder();

	IDataModelAction dataModelSaveAction();

	IDataModelActionBuilder dataModelSaveActionBuilder();

	IDataModelAction dataModelUndoAction();

	IDataModelActionBuilder dataModelUndoActionBuilder();

	IDataModelAction dataModelCancelAction();

	IDataModelActionBuilder dataModelCancelActionBuilder();

	<BEAN_TYPE, PARAM_TYPE> IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> executorActionBuilder(IBeanListModel<BEAN_TYPE> model);

	<BEAN_TYPE> ICreatorActionBuilder<BEAN_TYPE> creatorActionBuilder(
		Object entityId,
		Class<? extends BEAN_TYPE> beanType,
		IBeanListModel<BEAN_TYPE> model);

	<BEAN_TYPE> IDeleterActionBuilder<BEAN_TYPE> deleterActionBuilder(IBeanListModel<BEAN_TYPE> model);

	<BEAN_TYPE> ILinkActionBuilder<BEAN_TYPE> linkActionBuilder(IBeanListModel<BEAN_TYPE> model);

	<BEAN_TYPE> ILinkActionBuilder<BEAN_TYPE> linkActionBuilder(IBeanTableModel<BEAN_TYPE> model);

	<BEAN_TYPE> ILinkActionBuilder<BEAN_TYPE> linkActionBuilder(
		IBeanListModel<BEAN_TYPE> model,
		IEntityLinkDescriptor linkDescriptor);

	<BEAN_TYPE> ILinkActionBuilder<BEAN_TYPE> linkActionBuilder(
		IBeanTableModel<BEAN_TYPE> model,
		IEntityLinkDescriptor linkDescriptor);

	/**
	 * Creates a link action builder with help of the default entity service. If no ILinkDecsripter will be provided
	 * from the IEntityService of the model, null will be returned.
	 * 
	 * @param <BEAN_TYPE>
	 * @param model
	 * @param linkedModel
	 * @return The builder or null
	 */
	<BEAN_TYPE> ILinkActionBuilder<BEAN_TYPE> linkActionBuilder(IBeanTableModel<BEAN_TYPE> model, IBeanTableModel<?> linkedModel);

	<BEAN_TYPE> IAction linkAction(IBeanListModel<BEAN_TYPE> model, IEntityLinkDescriptor linkDescriptor);

	<BEAN_TYPE> IAction linkAction(IBeanTableModel<BEAN_TYPE> model, IEntityLinkDescriptor linkDescriptor);

	/**
	 * Creates a link action with help of the default entity service. If no ILinkDecsripter will be provided
	 * from the iEntityService of the model, null will be returned.
	 * 
	 * @param <BEAN_TYPE>
	 * @param model
	 * @param linkedModel
	 * @return The action or null
	 */
	<BEAN_TYPE> IAction linkAction(IBeanTableModel<BEAN_TYPE> model, IBeanTableModel<?> linkedModel);

	<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> linkCreatorActionBuilder(
		IBeanSelectionProvider<SOURCE_BEAN_TYPE> source);

	<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> linkCreatorActionBuilder(
		IBeanSelectionProvider<SOURCE_BEAN_TYPE> source,
		IEntityLinkDescriptor linkDescriptor);

	IAction linkCreatorAction(
		final IBeanSelectionProvider<?> source,
		final IBeanListModel<?> linkedModel,
		IEntityLinkDescriptor linkDescriptor);

}