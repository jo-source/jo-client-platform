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

import java.util.Date;
import java.util.List;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IInputDialog;
import org.jowidgets.api.widgets.blueprint.IInputDialogBluePrint;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.sample1.common.entity.IUser;
import org.jowidgets.cap.sample1.common.service.executor.UserComponentExecutorServices;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.command.IExecutorActionBuilder;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.execution.IParameterProvider;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.examples.common.icons.SilkIcons;
import org.jowidgets.tools.command.ActionWrapper;
import org.jowidgets.tools.content.SingleControlContent;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.maybe.Nothing;
import org.jowidgets.util.maybe.Some;

public class ChangeBirthdayAction extends ActionWrapper {

	private static final String PLEASE_INPUT_THE_NEW_DATE_OF_BIRTH = Messages.getString("ChangeBirthdayAction.please_input_the_new_date_of_birth"); //$NON-NLS-1$
	private static final String CHANGE_DAY_OF_BIRTH = Messages.getString("ChangeBirthdayAction.change_day_of_birth"); //$NON-NLS-1$
	private static final String CHANGE_DAY_OF_BIRTH_TOOLTIP = Messages.getString("ChangeBirthdayAction.change_day_of_birth_tooltip"); //$NON-NLS-1$
	private static final String DATE_OF_BIRTH = Messages.getString("ChangeBirthdayAction.date_of_birth"); //$NON-NLS-1$

	public ChangeBirthdayAction(final IBeanListModel<IUser> model) {
		super(create(model));
	}

	private static IAction create(final IBeanListModel<IUser> model) {
		final IExecutorActionBuilder<IUser, Date> builder = CapUiToolkit.actionFactory().executorActionBuilder(model);
		builder.setText(CHANGE_DAY_OF_BIRTH);
		builder.setToolTipText(CHANGE_DAY_OF_BIRTH_TOOLTIP);
		builder.setIcon(SilkIcons.USER_EDIT);
		builder.setSelectionPolicy(BeanSelectionPolicy.SINGLE_SELECTION);

		builder.addParameterProvider(new IParameterProvider<IUser, Date>() {
			@Override
			public IMaybe<Date> getParameter(
				final IExecutionContext executionContext,
				final List<IBeanProxy<IUser>> beans,
				final Date defaultParameter) throws Exception {
				final IInputDialog<Date> inputDialog = createInputDialog(executionContext);
				inputDialog.setVisible(true);
				if (inputDialog.isOkPressed()) {
					return new Some<Date>(inputDialog.getValue());
				}
				return Nothing.getInstance();
			}
		});

		builder.setExecutor(UserComponentExecutorServices.CHANGE_BIRTHDAY);

		return builder.build();
	}

	private static IInputDialog<Date> createInputDialog(final IExecutionContext executionContext) {
		final IBluePrintFactory bpf = Toolkit.getBluePrintFactory();
		final SingleControlContent<Date> content = new SingleControlContent<Date>(DATE_OF_BIRTH, bpf.inputFieldDate(), 200);
		final IInputDialogBluePrint<Date> inputDialogBp = bpf.inputDialog(content).setExecutionContext(executionContext);
		inputDialogBp.setMissingInputHint(PLEASE_INPUT_THE_NEW_DATE_OF_BIRTH);
		inputDialogBp.setResizable(false);
		return Toolkit.getActiveWindow().createChildWindow(inputDialogBp);
	}
}
