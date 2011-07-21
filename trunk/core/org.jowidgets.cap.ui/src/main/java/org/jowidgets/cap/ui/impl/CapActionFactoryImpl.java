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
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.cap.ui.api.command.ICapActionFactory;
import org.jowidgets.cap.ui.api.command.IDataModelAction;
import org.jowidgets.cap.ui.api.command.IDataModelActionBuilder;
import org.jowidgets.cap.ui.api.command.IExecutorActionBuilder;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
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
		builder.setText("Cancel"); //TODO MG i18n
		return builder;
	}

	@Override
	public IDataModelActionBuilder dataModelSaveActionBuilder() {
		final IDataModelActionBuilder builder = new DataModelActionBuilderImpl(new DataModelSaveCommand());
		builder.setText("Save"); //TODO MG i18n
		return builder;
	}

	@Override
	public IDataModelActionBuilder dataModelUndoActionBuilder() {
		final IDataModelActionBuilder builder = new DataModelActionBuilderImpl(new DataModelUndoCommand());
		builder.setText("Undo"); //TODO MG i18n
		return builder;
	}

	@Override
	public IDataModelActionBuilder dataModelLoadActionBuilder() {
		final IDataModelActionBuilder builder = new DataModelActionBuilderImpl(new DataModelLoadCommand());
		builder.setText("Reload"); //TODO MG i18n
		return builder;
	}

	@Override
	public IActionBuilder beanTableSettingsActionBuilder(final IBeanTable<?> table) {
		Assert.paramNotNull(table, "table");
		return new BeanTableSettingsActionBuilder(table);
	}

	@Override
	public IAction beanTableSettingsAction(final IBeanTable<?> table) {
		return beanTableSettingsActionBuilder(table).build();
	}

	@Override
	public IActionBuilder beanTableHideColumnActionBuilder(final IBeanTable<?> table) {
		Assert.paramNotNull(table, "table");
		return new BeanTableHideColumnActionBuilder(table);
	}

	@Override
	public IAction beanTableHideColumnAction(final IBeanTable<?> table) {
		return beanTableHideColumnActionBuilder(table).build();
	}

	@Override
	public IActionBuilder beanTableUnhideColumnsActionBuilder(final IBeanTable<?> table) {
		Assert.paramNotNull(table, "table");
		return new BeanTableUnhideColumnsActionBuilder(table);
	}

	@Override
	public IAction beanTableUnhideColumnsAction(final IBeanTable<?> table) {
		return beanTableUnhideColumnsActionBuilder(table).build();
	}

	@Override
	public IActionBuilder beanTablePackAllActionBuilder(final IBeanTable<?> table) {
		Assert.paramNotNull(table, "table");
		return new BeanTablePackAllActionBuilder(table);
	}

	@Override
	public IAction beanTablePackAllAction(final IBeanTable<?> table) {
		return beanTablePackAllActionBuilder(table).build();
	}

	@Override
	public IActionBuilder beanTablePackSelectedActionBuilder(final IBeanTable<?> table) {
		Assert.paramNotNull(table, "table");
		return new BeanTablePackSelectedActionBuilder(table);
	}

	@Override
	public IAction beanTablePackSelectedAction(final IBeanTable<?> table) {
		return beanTablePackSelectedActionBuilder(table).build();
	}

	@Override
	public <BEAN_TYPE, PARAM_TYPE> IExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE> executorActionBuilder(
		final IBeanListModel<BEAN_TYPE> model) {
		return new ExecutorActionBuilder<BEAN_TYPE, PARAM_TYPE>(model);
	}

}
