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
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.ui.api.command.ICapActionFactory;
import org.jowidgets.cap.ui.api.command.ICreatorActionBuilder;
import org.jowidgets.cap.ui.api.command.IDataModelAction;
import org.jowidgets.cap.ui.api.command.IDataModelActionBuilder;
import org.jowidgets.cap.ui.api.command.IDeleterActionBuilder;
import org.jowidgets.cap.ui.api.command.IExecutorActionBuilder;
import org.jowidgets.cap.ui.api.command.ILinkActionBuilder;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.util.Assert;

final class CapActionFactoryImpl implements ICapActionFactory {

	CapActionFactoryImpl() {}

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
		return builder;
	}

	@Override
	public IDataModelActionBuilder dataModelSaveActionBuilder() {
		final IDataModelActionBuilder builder = new DataModelActionBuilderImpl(new DataModelSaveCommand());
		builder.setText(Messages.getString("CapActionFactoryImpl.save")); //$NON-NLS-1$
		return builder;
	}

	@Override
	public IDataModelActionBuilder dataModelUndoActionBuilder() {
		final IDataModelActionBuilder builder = new DataModelActionBuilderImpl(new DataModelUndoCommand());
		builder.setText(Messages.getString("CapActionFactoryImpl.undo")); //$NON-NLS-1$
		return builder;
	}

	@Override
	public IDataModelActionBuilder dataModelLoadActionBuilder() {
		final IDataModelActionBuilder builder = new DataModelActionBuilderImpl(new DataModelLoadCommand());
		builder.setText(Messages.getString("CapActionFactoryImpl.reload")); //$NON-NLS-1$
		return builder;
	}

	@Override
	public <BEAN_TYPE, PARAM_TYPE> IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> executorActionBuilder(
		final IBeanListModel<BEAN_TYPE> model) {
		return new ExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE>(model);
	}

	@Override
	public <BEAN_TYPE> ICreatorActionBuilder creatorActionBuilder(
		final Class<? extends BEAN_TYPE> beanType,
		final IBeanListModel<BEAN_TYPE> model) {
		return new CreatorActionBuilder<BEAN_TYPE>(beanType, model);
	}

	@Override
	public <BEAN_TYPE> IDeleterActionBuilder<BEAN_TYPE> deleterActionBuilder(final IBeanListModel<BEAN_TYPE> model) {
		return new DeleterActionBuilder<BEAN_TYPE>(model);
	}

	@Override
	public <BEAN_TYPE> ILinkActionBuilder<BEAN_TYPE> linkActionBuilder(final IBeanListModel<BEAN_TYPE> model) {
		Assert.paramNotNull(model, "model");
		return new LinkActionBuilderImpl<BEAN_TYPE>(model);
	}

	@Override
	public <BEAN_TYPE> ILinkActionBuilder<BEAN_TYPE> linkActionBuilder(
		final IBeanListModel<BEAN_TYPE> model,
		final IEntityLinkDescriptor linkDescriptor) {
		final ILinkActionBuilder<BEAN_TYPE> builder = linkActionBuilder(model);
		return LinkActionBuilderFactory.createLinkActionBuilder(linkDescriptor, builder);
	}

	@Override
	public <BEAN_TYPE> IAction linkAction(final IBeanListModel<BEAN_TYPE> model, final IEntityLinkDescriptor linkDescriptor) {
		final ILinkActionBuilder<BEAN_TYPE> builder = linkActionBuilder(model, linkDescriptor);
		if (builder != null) {
			return builder.build();
		}
		else {
			return null;
		}
	}

}
