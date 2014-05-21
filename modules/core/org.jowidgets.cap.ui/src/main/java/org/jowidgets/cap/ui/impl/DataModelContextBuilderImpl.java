/*
 * Copyright (c) 2014, grossmann
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
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IWidget;
import org.jowidgets.cap.ui.api.command.IDataModelAction;
import org.jowidgets.cap.ui.api.model.DataModelChangeType;
import org.jowidgets.cap.ui.api.model.IDataModel;
import org.jowidgets.cap.ui.api.model.IDataModelContext;
import org.jowidgets.cap.ui.api.model.IDataModelContextBuilder;
import org.jowidgets.cap.ui.api.model.IDataModelSaveDelegate;
import org.jowidgets.cap.ui.api.workbench.CapWorkbenchActionsProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.ITypedKey;

final class DataModelContextBuilderImpl implements IDataModelContextBuilder {

	private IDataModel rootModel;
	private DataModelChangeType rootModelDepenency;
	private IDataModelSaveDelegate saveDelegate;

	DataModelContextBuilderImpl() {
		rootModelDepenency = DataModelChangeType.DATA_CHANGE;
		this.saveDelegate = new DefaultCapDataModelSaveDelegate();
	}

	@Override
	public IDataModelContextBuilder setRootModel(final IDataModel rootModel) {
		Assert.paramNotNull(rootModel, "rootModel");
		this.rootModel = rootModel;
		return this;
	}

	@Override
	public IDataModelContextBuilder setRootModelDependency(final DataModelChangeType rootModelDepenency) {
		Assert.paramNotNull(rootModelDepenency, "rootModelDepenency");
		this.rootModelDepenency = rootModelDepenency;
		return this;
	}

	@Override
	public IDataModelContextBuilder setSaveDelegate(final IDataModelSaveDelegate saveDelegate) {
		Assert.paramNotNull(saveDelegate, "saveDelegate");
		this.saveDelegate = saveDelegate;
		return this;
	}

	@Override
	public IDataModelContext build() {
		return new DataModelContextImpl(rootModel, rootModelDepenency, saveDelegate);
	}

	private final class DefaultCapDataModelSaveDelegate implements IDataModelSaveDelegate {

		@Override
		public void save() {
			try {
				final IDataModelAction saveAction = CapWorkbenchActionsProvider.saveAction();
				saveAction.execute(new IExecutionContext() {

					@Override
					public <VALUE_TYPE> VALUE_TYPE getValue(final ITypedKey<VALUE_TYPE> key) {
						return null;
					}

					@Override
					public IWidget getSource() {
						return Toolkit.getActiveWindow();
					}

					@Override
					public IAction getAction() {
						return saveAction;
					}
				});
			}
			catch (final Exception e) {
				throw new RuntimeException();
			}
		}

	}
}
