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

import java.util.Collection;
import java.util.List;

import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.bean.IBeanModification;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IDataModel;
import org.jowidgets.cap.ui.api.widgets.IBeanDialog;
import org.jowidgets.cap.ui.api.widgets.IBeanDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.common.types.Rectangle;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.Assert;

final class BeanEditCommand<BEAN_TYPE> implements ICommand, ICommandExecutor {

	private static final int INITIAL_MIN_WIDTH = 450;

	private final IBeanListModel<BEAN_TYPE> model;
	private final IDataModel dataModel;
	private final IBeanFormBluePrint<BEAN_TYPE> beanFormBp;
	private final BeanSelectionProviderEnabledChecker<BEAN_TYPE> enabledChecker;

	private Rectangle dialogBounds;

	BeanEditCommand(
		final IBeanListModel<BEAN_TYPE> model,
		final IDataModel dataModel,
		final IBeanFormBluePrint<BEAN_TYPE> beanFormBp,
		final List<IEnabledChecker> enabledCheckers) {

		Assert.paramNotNull(model, "model");
		Assert.paramNotNull(beanFormBp, "beanFormBp");
		Assert.paramNotNull(enabledCheckers, "enabledCheckers");

		this.enabledChecker = new BeanSelectionProviderEnabledChecker<BEAN_TYPE>(
			model,
			BeanSelectionPolicy.SINGLE_SELECTION,
			BeanModificationStatePolicy.ANY_MODIFICATION,
			null,
			enabledCheckers,
			null,
			true);

		this.model = model;
		this.dataModel = dataModel;
		this.beanFormBp = beanFormBp;
	}

	@Override
	public ICommandExecutor getCommandExecutor() {
		return this;
	}

	@Override
	public IEnabledChecker getEnabledChecker() {
		return enabledChecker;
	}

	@Override
	public IExceptionHandler getExceptionHandler() {
		return null;
	}

	@Override
	public void execute(final IExecutionContext executionContext) throws Exception {

		final IBeanProxy<BEAN_TYPE> original = model.getBeanSelection().getFirstSelected();
		if (original == null) {
			return;
		}

		final IBeanProxy<BEAN_TYPE> copy = original.createCopy();

		final IBeanDialogBluePrint<BEAN_TYPE> beanDialogBp = CapUiToolkit.bluePrintFactory().beanDialog(beanFormBp);
		beanDialogBp.autoPackOff();
		if (dialogBounds != null) {
			beanDialogBp.setPosition(dialogBounds.getPosition()).setSize(dialogBounds.getSize());
			beanDialogBp.autoPackOff().autoCenterOff();
		}
		beanDialogBp.setExecutionContext(executionContext);
		beanDialogBp.setOkButton(BPF.buttonSave());

		final IBeanDialog<BEAN_TYPE> dialog = Toolkit.getActiveWindow().createChildWindow(beanDialogBp);
		dialog.setBean(copy);
		dialog.pack();

		dialog.setSize(Math.max(dialog.getSize().getWidth(), INITIAL_MIN_WIDTH), dialog.getSize().getHeight());

		dialog.setVisible(true);
		if (dialog.isOkPressed()) {
			final Collection<IBeanModification> modifications = copy.getModifications();
			original.setModifications(modifications);
			dataModel.save();
		}
		dialogBounds = dialog.getBounds();
		dialog.dispose();
	}

}
