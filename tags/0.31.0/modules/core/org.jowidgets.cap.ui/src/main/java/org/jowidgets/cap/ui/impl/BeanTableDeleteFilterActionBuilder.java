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
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.tools.model.DataModelContextExecutor;
import org.jowidgets.tools.command.ActionBuilder;
import org.jowidgets.tools.command.EnabledChecker;
import org.jowidgets.util.event.IChangeListener;

final class BeanTableDeleteFilterActionBuilder extends ActionBuilder {

	BeanTableDeleteFilterActionBuilder(final IBeanTableModel<?> model) {
		super();
		setText(Messages.getString("BeanTableDeleteFilterActionBuilder.delete_table_filter")); //$NON-NLS-1$
		setToolTipText(Messages.getString("BeanTableDeleteFilterActionBuilder.deletes_removes_the_table_filters")); //$NON-NLS-1$
		setIcon(IconsSmall.FILTER_DELETE);

		final EnabledChecker enabledChecker = new EnabledChecker();
		enabledChecker.setEnabledState(getEnabledState(model));
		model.addFilterChangeListener(new IChangeListener() {
			@Override
			public void changed() {
				enabledChecker.setEnabledState(getEnabledState(model));
			}
		});

		final ICommandExecutor executor = new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				DataModelContextExecutor.executeDataChange(model, new Runnable() {
					@Override
					public void run() {
						model.removeFilter(IBeanTableModel.UI_FILTER_ID);
						model.load();
					}
				});
			}
		};

		setCommand(executor, enabledChecker);
	}

	private IEnabledState getEnabledState(final IBeanTableModel<?> model) {
		if (model.getFilter(IBeanTableModel.UI_FILTER_ID) != null) {
			return EnabledState.ENABLED;
		}
		else {
			return EnabledState.disabled(Messages.getString("BeanTableDeleteFilterActionBuilder.there_is_no_filter_defined_on_this_table")); //$NON-NLS-1$
		}
	}

}
