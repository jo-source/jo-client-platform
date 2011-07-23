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

import org.jowidgets.api.command.EnabledState;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.types.QuestionResult;
import org.jowidgets.cap.ui.api.sort.ISortModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.tools.command.ActionBuilder;
import org.jowidgets.tools.command.EnabledChecker;
import org.jowidgets.util.event.IChangeListener;

final class BeanTableClearDefaultSortActionBuilder extends ActionBuilder {

	private final EnabledChecker enabledChecker;
	private final ISortModel sortModel;

	BeanTableClearDefaultSortActionBuilder(final IBeanTableModel<?> model) {
		super();
		//TODO i18n
		setText("Clear all default sorting");
		setToolTipText("Clears the default sorting of all columns");

		this.sortModel = model.getSortModel();
		this.enabledChecker = new EnabledChecker();

		model.getSortModel().addChangeListener(new IChangeListener() {
			@Override
			public void changed() {
				sortModelStateChanged();
			}
		});

		final ICommandExecutor executor = new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				final QuestionResult questionResult = Toolkit.getQuestionPane().askYesNoQuestion(
						"Clear default sorting",
						"Do you really want to clear the default sorting?\nThis can not be undone!");
				if (questionResult == QuestionResult.YES) {
					model.getSortModel().clearDefaultSorting();
				}
			}
		};

		setCommand(executor, enabledChecker);

		sortModelStateChanged();
	}

	private void sortModelStateChanged() {
		if (sortModel.getDefaultSorting().isEmpty()) {
			enabledChecker.setEnabledState(EnabledState.disabled("There is no default sorting"));
		}
		else {
			enabledChecker.setEnabledState(EnabledState.ENABLED);
		}
	}
}
