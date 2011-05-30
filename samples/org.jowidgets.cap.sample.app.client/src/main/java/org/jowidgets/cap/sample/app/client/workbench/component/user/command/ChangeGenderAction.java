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

package org.jowidgets.cap.sample.app.client.workbench.component.user.command;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.image.Icons;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.types.QuestionResult;
import org.jowidgets.cap.sample.app.common.entity.IUser;
import org.jowidgets.cap.sample.app.common.service.executor.ChangeGenderExecutableChecker;
import org.jowidgets.cap.sample.app.common.service.executor.UserComponentExecutorServices;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.command.IExecutorActionBuilder;
import org.jowidgets.cap.ui.api.executor.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.executor.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.executor.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.examples.common.icons.SilkIcons;
import org.jowidgets.tools.command.ActionWrapper;
import org.jowidgets.util.types.Null;

public class ChangeGenderAction extends ActionWrapper {

	public ChangeGenderAction(final IBeanListModel<IUser> model) {
		super(create(model));
	}

	private static IAction create(final IBeanListModel<IUser> model) {
		final IExecutorActionBuilder<IUser, Null> builder = CapUiToolkit.getActionFactory().executorActionBuilder(model);
		builder.setText("Change gender");
		builder.setToolTipText("Changes the gender of the selected person(s)");
		builder.setIcon(SilkIcons.CUT_RED);
		builder.setSelectionPolicy(BeanSelectionPolicy.MULTI_SELECTION);
		builder.setExecutionPolicy(BeanExecutionPolicy.PARALLEL);
		builder.setExecutor(UserComponentExecutorServices.CHANGE_GENDER);
		builder.addExecutableChecker(new ChangeGenderExecutableChecker());
		builder.addExecutionInterceptor(new IExecutionInterceptor() {

			@Override
			public boolean beforeExecution(final IExecutionContext executionContext) {
				final IAction action = executionContext.getAction();
				final int size = model.getSelection().size();
				final String question;
				if (size == 1) {
					final IUser bean = model.getBean(model.getSelection().get(0)).getBean();
					question = "Would you really like to change the gender of '"
						+ bean.getName()
						+ " "
						+ bean.getLastName()
						+ "'?\n This could not be undone!";
				}
				else {
					question = "Would you really like to change the gender of '"
						+ size
						+ "' Persons?\n This could not be undone!";
				}
				final QuestionResult result = Toolkit.getQuestionPane().askYesNoQuestion(
						action.getText(),
						action.getIcon(),
						question,
						QuestionResult.NO,
						Icons.QUESTION);
				return result == QuestionResult.YES;
			}

			@Override
			public void afterExecution(final IExecutionContext executionContext) {}

		});
		return builder.build();
	}
}
