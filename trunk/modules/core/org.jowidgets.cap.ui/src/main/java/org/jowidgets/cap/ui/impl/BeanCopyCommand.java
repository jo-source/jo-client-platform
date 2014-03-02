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

import java.util.List;

import org.jowidgets.api.clipboard.Clipboard;
import org.jowidgets.api.clipboard.ITransferable;
import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.ui.api.bean.IBeanSelection;
import org.jowidgets.cap.ui.api.clipboard.IBeanSelectionTransferableFactory;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.util.Assert;

final class BeanCopyCommand<BEAN_TYPE> implements ICommand, ICommandExecutor {

	private static final IMessage NOTHING_SELECTED = Messages.getMessage("BeanDeleterCommand.nothing_selected");

	private final IBeanListModel<BEAN_TYPE> model;
	private final IBeanSelectionTransferableFactory<BEAN_TYPE> transferableFactory;
	private final BeanSelectionProviderEnabledChecker<BEAN_TYPE> enabledChecker;

	BeanCopyCommand(
		final IBeanListModel<BEAN_TYPE> model,
		final IBeanSelectionTransferableFactory<BEAN_TYPE> transferableFactory,
		final List<IEnabledChecker> enabledCheckers,
		final List<IExecutableChecker<BEAN_TYPE>> executableCheckers,
		final boolean multiSelection,
		final BeanModificationStatePolicy beanModificationStatePolicy,
		final BeanMessageStatePolicy beanMessageStatePolicy) {

		Assert.paramNotNull(model, "model");
		Assert.paramNotNull(transferableFactory, "transferableFactory");
		Assert.paramNotNull(executableCheckers, "executableCheckers");
		Assert.paramNotNull(beanModificationStatePolicy, "beanModificationStatePolicy");
		Assert.paramNotNull(beanMessageStatePolicy, "beanMessageStatePolicy");

		this.model = model;
		this.transferableFactory = transferableFactory;

		this.enabledChecker = new BeanSelectionProviderEnabledChecker<BEAN_TYPE>(
			model,
			multiSelection ? BeanSelectionPolicy.MULTI_SELECTION : BeanSelectionPolicy.SINGLE_SELECTION,
			beanModificationStatePolicy,
			beanMessageStatePolicy,
			enabledCheckers,
			executableCheckers,
			false);
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

		final IBeanSelection<BEAN_TYPE> beanSelection = model.getBeanSelection();

		if (beanSelection == null || beanSelection.getSelection().size() == 0) {
			Toolkit.getMessagePane().showWarning(executionContext, NOTHING_SELECTED.get());
			return;
		}

		final ITransferable transferable = transferableFactory.create(model);
		if (transferable != null) {
			Clipboard.setContents(transferable);
		}

		//		final List<IBeanDto> beanDtos = new LinkedList<IBeanDto>();
		//
		//		for (final IBeanProxy<BEAN_TYPE> bean : beanSelection.getSelection()) {
		//			if (bean != null && !bean.isDummy() && !bean.isTransient()) {
		//				beanDtos.add(bean.getBeanDto());
		//			}
		//		}
		//
		//		final BeanSelectionClipboard clippboard = new BeanSelectionClipboard(entityId, beanType, beanDtos);
		//		final BeanTableTransfer transfer = new BeanTableTransfer(clippboard, beanDtos.toString());
		//		Clipboard.setContents(transfer);

	}

}
