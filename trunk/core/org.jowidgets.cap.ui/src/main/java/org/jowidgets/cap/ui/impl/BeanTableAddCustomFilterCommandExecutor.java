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

import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IInputDialog;
import org.jowidgets.api.widgets.blueprint.IInputDialogBluePrint;
import org.jowidgets.cap.ui.api.filter.IUiConfigurableFilter;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.tools.message.MessageReplacer;

final class BeanTableAddCustomFilterCommandExecutor extends AbstractBeanTableAddFilterCommandExecutor {

	private static final IMessage NO_USER_DEFINED_FILTER = Messages.getMessage("BeanTableAddCustomFilterCommandExecutor.noUserDefinedFilter");

	private final IBeanTableModel<?> model;
	private final int columnIndex;

	BeanTableAddCustomFilterCommandExecutor(final IBeanTable<?> table, final int columnIndex) {
		super(table, columnIndex, false);
		this.model = table.getModel();
		this.columnIndex = columnIndex;
	}

	@Override
	public void execute(final IExecutionContext executionContext) throws Exception {

		final IUiConfigurableFilter<?> includingFilter = getIncludingFilter(executionContext);

		final IInputDialogBluePrint<IUiConfigurableFilter<? extends Object>> dialogBp;
		dialogBp = AttributeFilterDialogBluePrintFactory.createDialogBluePrint(model, columnIndex, executionContext, null);

		final IInputDialog<IUiConfigurableFilter<?>> dialog = Toolkit.getActiveWindow().createChildWindow(dialogBp);
		dialog.setValue(includingFilter);
		dialog.setVisible(true);

		if (dialog.isOkPressed()) {
			model.addFilter(IBeanTableModel.UI_FILTER_ID, dialog.getValue());
			model.load();
		}

		dialog.dispose();
	}

	@Override
	String getDisabledMessageOnNoIncludingFilter(final String value) {
		return MessageReplacer.replace(NO_USER_DEFINED_FILTER.get(), value);
	}

}
