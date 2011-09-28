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
import org.jowidgets.api.command.IEnabledState;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.icons.CapIcons;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.tools.command.ActionBuilder;
import org.jowidgets.tools.command.EnabledChecker;
import org.jowidgets.util.event.IChangeListener;

final class BeanTableShowAllColumnsActionBuilder extends ActionBuilder {

	BeanTableShowAllColumnsActionBuilder(final IBeanTable<?> table) {
		super();

		setText(Messages.getString("BeanTableShowAllColumnsActionBuilder.show_all_columns")); //$NON-NLS-1$
		setToolTipText(Messages.getString("BeanTableShowAllColumnsActionBuilder.show_all_columns_tooltip")); //$NON-NLS-1$
		setIcon(CapIcons.TABLE_UNHIDE_ALL_COLUMNS);

		final IBeanTableModel<?> model = table.getModel();

		final ICommandExecutor commandExecutor = new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				for (int i = 0; i < model.getColumnCount(); i++) {
					model.getAttribute(i).setVisible(true);
				}
			}
		};

		final EnabledChecker enabledChecker = new EnabledChecker();
		enabledChecker.setEnabledState(getUnhideColumnsEnabledState(model));
		final IChangeListener changeListener = new IChangeListener() {
			@Override
			public void changed() {
				enabledChecker.setEnabledState(getUnhideColumnsEnabledState(model));
			}
		};

		for (int i = 0; i < model.getColumnCount(); i++) {
			final IAttribute<?> attribute = model.getAttribute(i);
			attribute.addChangeListener(changeListener);
		}

		setCommand(commandExecutor, enabledChecker);
	}

	private IEnabledState getUnhideColumnsEnabledState(final IBeanTableModel<?> model) {
		if (!hasInvisibleColumns(model)) {
			return EnabledState.disabled(Messages.getString("BeanTableShowAllColumnsActionBuilder.all_columns_are_visible")); //$NON-NLS-1$
		}
		else {
			return EnabledState.ENABLED;
		}
	}

	private boolean hasInvisibleColumns(final IBeanTableModel<?> model) {
		for (int i = 0; i < model.getColumnCount(); i++) {
			final IAttribute<?> attribute = model.getAttribute(i);
			if (!attribute.isVisible()) {
				return true;
			}
		}
		return false;
	}

}
