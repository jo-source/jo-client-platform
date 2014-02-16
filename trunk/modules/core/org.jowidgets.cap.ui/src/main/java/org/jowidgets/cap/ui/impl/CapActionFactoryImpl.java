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

package org.jowidgets.cap.ui.impl;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.command.ICapActionFactory;
import org.jowidgets.cap.ui.api.command.ICopyActionBuilder;
import org.jowidgets.cap.ui.api.command.ICreatorActionBuilder;
import org.jowidgets.cap.ui.api.command.IDataModelAction;
import org.jowidgets.cap.ui.api.command.IDataModelActionBuilder;
import org.jowidgets.cap.ui.api.command.IDeleterActionBuilder;
import org.jowidgets.cap.ui.api.command.IExecutorActionBuilder;
import org.jowidgets.cap.ui.api.command.ILinkCreatorActionBuilder;
import org.jowidgets.cap.ui.api.command.ILinkDeleterActionBuilder;
import org.jowidgets.cap.ui.api.command.IPasswordChangeActionBuilder;
import org.jowidgets.cap.ui.api.command.IRefreshLookUpsActionBuilder;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.common.types.Modifier;

final class CapActionFactoryImpl implements ICapActionFactory {

	CapActionFactoryImpl() {}

	@Override
	public IPasswordChangeActionBuilder passwordChangeActionBuilder() {
		return new PasswordChangeActionBuilderImpl();
	}

	@Override
	public IAction passwordChangeAction() {
		return passwordChangeActionBuilder().build();
	}

	@Override
	public IDataModelAction dataModelSaveAction() {
		return dataModelSaveActionBuilder().build();
	}

	@Override
	public IDataModelAction dataModelUndoAction() {
		return dataModelUndoActionBuilder().build();
	}

	@Override
	public IDataModelAction dataModelLoadAction() {
		return dataModelLoadActionBuilder().build();
	}

	@Override
	public IDataModelAction dataModelCancelAction() {
		return dataModelCancelActionBuilder().build();
	}

	@Override
	public IDataModelActionBuilder dataModelCancelActionBuilder() {
		final IDataModelActionBuilder builder = new DataModelActionBuilderImpl(new DataModelCancelCommand());
		builder.setText(Messages.getString("CapActionFactoryImpl.cancel")); //$NON-NLS-1$
		builder.setIcon(IconsSmall.CANCEL);
		return builder;
	}

	@Override
	public IDataModelActionBuilder dataModelSaveActionBuilder() {
		final IDataModelActionBuilder builder = new DataModelActionBuilderImpl(new DataModelSaveCommand());
		builder.setText(Messages.getString("CapActionFactoryImpl.save")); //$NON-NLS-1$
		builder.setIcon(IconsSmall.DISK);
		builder.setAccelerator('S', Modifier.CTRL);
		return builder;
	}

	@Override
	public IDataModelActionBuilder dataModelUndoActionBuilder() {
		final IDataModelActionBuilder builder = new DataModelActionBuilderImpl(new DataModelUndoCommand());
		builder.setText(Messages.getString("CapActionFactoryImpl.undo")); //$NON-NLS-1$
		builder.setAccelerator('Z', Modifier.CTRL);
		builder.setIcon(IconsSmall.UNDO);
		return builder;
	}

	@Override
	public IDataModelActionBuilder dataModelLoadActionBuilder() {
		final IDataModelActionBuilder builder = new DataModelActionBuilderImpl(new DataModelLoadCommand());
		builder.setText(Messages.getString("CapActionFactoryImpl.reload")); //$NON-NLS-1$
		builder.setIcon(IconsSmall.REFRESH);
		return builder;
	}

	@Override
	public IRefreshLookUpsActionBuilder refreshLookUpsActionBuilder() {
		return new RefreshLookUpsActionBuilderImpl();
	}

	@Override
	public IAction refreshLookUpsAction() {
		return refreshLookUpsActionBuilder().build();
	}

	@Override
	public <BEAN_TYPE, PARAM_TYPE> IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> executorActionBuilder(
		final IBeanListModel<BEAN_TYPE> model) {
		return new ExecutorActionBuilderImpl<BEAN_TYPE, PARAM_TYPE>(model);
	}

	@Override
	public <BEAN_TYPE> ICreatorActionBuilder<BEAN_TYPE> creatorActionBuilder(
		final Object entityId,
		final Class<? extends BEAN_TYPE> beanType,
		final IBeanListModel<BEAN_TYPE> model) {
		return new CreatorActionBuilderImpl<BEAN_TYPE>(entityId, beanType, model);
	}

	@Override
	public <BEAN_TYPE> IDeleterActionBuilder<BEAN_TYPE> deleterActionBuilder(final IBeanListModel<BEAN_TYPE> model) {
		return new DeleterActionBuilder<BEAN_TYPE>(model);
	}

	@Override
	public <BEAN_TYPE> ICopyActionBuilder<BEAN_TYPE> copyActionBuilder(final IBeanListModel<BEAN_TYPE> model) {
		return new CopyActionBuilder<BEAN_TYPE>(model);
	}

	@Override
	public <SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> linkCreatorActionBuilder(
		final IBeanSelectionProvider<SOURCE_BEAN_TYPE> source) {
		final ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> result;
		result = new LinkCreatorActionBuilderImpl<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE>();
		result.setSource(source);
		return new LinkCreatorActionBuilderImpl<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE>();
	}

	@Override
	public <SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> ILinkCreatorActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> linkCreatorActionBuilder(
		final IBeanSelectionProvider<SOURCE_BEAN_TYPE> source,
		final IEntityLinkDescriptor linkDescriptor) {

		return new LinkCreatorActionBuilderImpl<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE>(source, linkDescriptor);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public IAction linkCreatorAction(
		final IBeanSelectionProvider<?> source,
		final IBeanListModel<?> linkedModel,
		final IEntityLinkDescriptor linkDescriptor) {
		final ILinkCreatorActionBuilder builder = linkCreatorActionBuilder(source, linkDescriptor);
		builder.setLinkedModel(linkedModel);
		return builder.build();
	}

	@Override
	public <SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> linkDeleterActionBuilder() {
		return new LinkDeleterActionBuilderImpl<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE>();
	}

	@Override
	public <SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> linkDeleterActionBuilder(
		final IBeanSelectionProvider<SOURCE_BEAN_TYPE> source,
		final IBeanListModel<LINKED_BEAN_TYPE> linkedModel,
		final IEntityLinkDescriptor linkDescriptor) {
		return new LinkDeleterActionBuilderImpl<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE>(source, linkedModel, linkDescriptor);
	}

	@Override
	public IAction linkDeleterAction(
		final IBeanSelectionProvider<?> source,
		final IBeanListModel<?> linkedModel,
		final IEntityLinkDescriptor linkDescriptor) {
		return linkDeleterActionBuilder(source, linkedModel, linkDescriptor).build();
	}

}
