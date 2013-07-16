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

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.ui.api.command.IDataModelAction;
import org.jowidgets.cap.ui.api.command.IDataModelActionBuilder;
import org.jowidgets.cap.ui.api.model.IDataModel;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.types.Accelerator;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.common.types.VirtualKey;
import org.jowidgets.tools.command.ActionWrapper;

final class DataModelActionBuilderImpl implements IDataModelActionBuilder {

	private final AbstractDataModelCommand abstractDataModelCommand;
	private final IActionBuilder actionBuilder;
	private final List<IEnabledChecker> enabledCheckers = new LinkedList<IEnabledChecker>();

	DataModelActionBuilderImpl(final AbstractDataModelCommand command) {
		this.abstractDataModelCommand = command;
		this.actionBuilder = Toolkit.getActionBuilderFactory().create();
	}

	@Override
	public IDataModelActionBuilder setText(final String text) {
		actionBuilder.setText(text);
		return this;
	}

	@Override
	public IDataModelActionBuilder setToolTipText(final String toolTipText) {
		actionBuilder.setToolTipText(toolTipText);
		return this;
	}

	@Override
	public IDataModelActionBuilder setIcon(final IImageConstant icon) {
		actionBuilder.setIcon(icon);
		return this;
	}

	@Override
	public IDataModelActionBuilder setMnemonic(final Character mnemonic) {
		actionBuilder.setMnemonic(mnemonic);
		return this;
	}

	@Override
	public IDataModelActionBuilder setMnemonic(final char mnemonic) {
		actionBuilder.setMnemonic(mnemonic);
		return this;
	}

	@Override
	public IDataModelActionBuilder setAccelerator(final Accelerator accelerator) {
		actionBuilder.setAccelerator(accelerator);
		return this;
	}

	@Override
	public IDataModelActionBuilder setAccelerator(final char key, final Modifier... modifier) {
		actionBuilder.setAccelerator(key, modifier);
		return this;
	}

	@Override
	public IDataModelActionBuilder setAccelerator(final VirtualKey virtualKey, final Modifier... modifier) {
		actionBuilder.setAccelerator(virtualKey, modifier);
		return this;
	}

	@Override
	public IDataModelActionBuilder setActionExceptionHandler(final IExceptionHandler exceptionHandler) {
		actionBuilder.setActionExceptionHandler(exceptionHandler);
		return this;
	}

	@Override
	public IDataModelActionBuilder addEnabledChecker(final IEnabledChecker enabledChecker) {
		enabledCheckers.add(enabledChecker);
		return this;
	}

	@Override
	public IDataModelAction build() {
		actionBuilder.setCommand(new CommandWrapper(abstractDataModelCommand, enabledCheckers));
		final IAction action = actionBuilder.build();
		return new DataModelAction(action);
	}

	private class DataModelAction extends ActionWrapper implements IDataModelAction {

		public DataModelAction(final IAction action) {
			super(action);
		}

		@Override
		public void addDataModel(final IDataModel dataModel) {
			abstractDataModelCommand.addDataModel(dataModel);
		}

		@Override
		public void removeDataModel(final IDataModel dataModel) {
			abstractDataModelCommand.removeDataModel(dataModel);
		}

	}
}
