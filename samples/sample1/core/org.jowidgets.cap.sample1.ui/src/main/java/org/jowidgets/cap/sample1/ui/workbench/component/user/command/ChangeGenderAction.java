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

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.image.Icons;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.types.QuestionResult;
import org.jowidgets.cap.sample1.common.entity.IUser;
import org.jowidgets.cap.sample1.common.service.executor.ChangeGenderExecutableChecker;
import org.jowidgets.cap.sample1.common.service.executor.UserComponentExecutorServices;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.command.IExecutorActionBuilder;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.tools.execution.ExecutionInterceptorAdapter;
import org.jowidgets.common.types.IVetoable;
import org.jowidgets.examples.common.icons.SilkIcons;
import org.jowidgets.tools.command.ActionWrapper;

public class ChangeGenderAction extends ActionWrapper {

	private static final String CHANGE_GENDER = Messages.getString("ChangeGenderAction.change_gender"); //$NON-NLS-1$
	private static final String CHANGES_GENDER_TOOLTIP = Messages.getString("ChangeGenderAction.change_gende_tooltip"); //$NON-NLS-1$
	private static final String WOULD_YOU_REALLY_LIKE_TO_CHANGE_THE_GENDER_OF_N_M_THIS_CAN_T_BE_UNDONE = Messages.getString("ChangeGenderAction.would_you_really_like_to_change_the_gender_of_n_m_this_cant_be_undonw"); //$NON-NLS-1$
	private static final String WOULD_YOU_REALLY_LIKE_TO_CHANGE_THE_GENDER_OF_N_PERSONS_THIS_CAN_T_BE_UNDONE = Messages.getString("ChangeGenderAction.would_you_really_like_to_change_the_gender_of_n_persons_this_cant_be_undone"); //$NON-NLS-1$

	public ChangeGenderAction(final IBeanListModel<IUser> model) {
		super(create(model));
	}

	private static IAction create(final IBeanListModel<IUser> model) {
		final IExecutorActionBuilder<IUser, Void> builder = CapUiToolkit.actionFactory().executorActionBuilder(model);
		builder.setText(CHANGE_GENDER);
		builder.setToolTipText(CHANGES_GENDER_TOOLTIP);
		builder.setIcon(SilkIcons.CUT_RED);
		builder.setSelectionPolicy(BeanSelectionPolicy.MULTI_SELECTION);
		builder.setExecutionPolicy(BeanExecutionPolicy.PARALLEL);
		builder.setExecutor(UserComponentExecutorServices.CHANGE_GENDER);
		//TODO MG get checker from service (id oder so)
		builder.addExecutableChecker(new ChangeGenderExecutableChecker());
		builder.addExecutionInterceptor(new ExecutionInterceptorAdapter() {

			@Override
			public void beforeExecution(final IExecutionContext executionContext, final IVetoable continueExecution) {
				final IAction action = executionContext.getAction();
				final int size = model.getSelection().size();
				final String question;
				if (size == 1) {
					final IUser bean = model.getBean(model.getSelection().get(0)).getBean();
					question = Toolkit.getMessageReplacer().replace(
							WOULD_YOU_REALLY_LIKE_TO_CHANGE_THE_GENDER_OF_N_M_THIS_CAN_T_BE_UNDONE,
							bean.getName(),
							bean.getLastName());
				}
				else {
					question = Toolkit.getMessageReplacer().replace(
							WOULD_YOU_REALLY_LIKE_TO_CHANGE_THE_GENDER_OF_N_PERSONS_THIS_CAN_T_BE_UNDONE,
							String.valueOf(size));
				}
				final QuestionResult result = Toolkit.getQuestionPane().askYesNoQuestion(
						action.getText(),
						action.getIcon(),
						question,
						QuestionResult.NO,
						Icons.QUESTION);

				if (result != QuestionResult.YES) {
					continueExecution.veto();
				}
			}

		});
		return builder.build();
	}
}
