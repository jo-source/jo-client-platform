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

import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.common.widgets.controller.ITableColumnPopupEvent;
import org.jowidgets.tools.command.ActionBuilder;

final class BeanTablePackSelectedActionBuilder extends ActionBuilder {

	BeanTablePackSelectedActionBuilder(final IBeanTable<?> table) {
		super();
		setText(Messages.getString("BeanTablePackSelectedActionBuilder.fit_column")); //$NON-NLS-1$
		setToolTipText(Messages.getString("BeanTablePackSelectedActionBuilder.fit_column_tooltip")); //$NON-NLS-1$
		setCommand(new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				final ITableColumnPopupEvent columnPopupEvent = executionContext.getValue(IBeanTable.COLUMN_POPUP_EVENT_CONTEXT_KEY);
				table.pack(columnPopupEvent.getColumnIndex());
			}
		});
	}

}
