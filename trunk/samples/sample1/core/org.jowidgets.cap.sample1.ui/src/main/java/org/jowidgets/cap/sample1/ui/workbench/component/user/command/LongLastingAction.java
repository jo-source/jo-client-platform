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

package org.jowidgets.cap.sample1.ui.workbench.component.user.command;

import org.jowidgets.addons.icons.silkicons.SilkIcons;
import org.jowidgets.api.command.IAction;
import org.jowidgets.cap.sample1.common.entity.IUser;
import org.jowidgets.cap.sample1.common.service.executor.UserComponentExecutorServices;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.command.IExecutorActionBuilder;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.tools.command.ActionWrapper;

public class LongLastingAction extends ActionWrapper {

	public LongLastingAction(final IBeanListModel<IUser> model) {
		super(create(model));
	}

	private static IAction create(final IBeanListModel<IUser> model) {
		final IExecutorActionBuilder<IUser, Void> builder = CapUiToolkit.actionFactory().executorActionBuilder(model);
		builder.setText(Messages.getString("LongLastingAction.long_lasting_test")); //$NON-NLS-1$
		builder.setToolTipText(Messages.getString("LongLastingAction.long_lasting_test_tooltip")); //$NON-NLS-1$
		builder.setIcon(SilkIcons.TIME);
		builder.setSelectionPolicy(BeanSelectionPolicy.MULTI_SELECTION);
		builder.setExecutionPolicy(BeanExecutionPolicy.PARALLEL);
		builder.setExecutor(UserComponentExecutorServices.LONG_LASTING);
		return builder.build();
	}
}
