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

import java.util.List;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.cap.sample.app.common.entity.IUser;
import org.jowidgets.cap.sample.app.common.service.executor.ChangeGenderExecutableChecker;
import org.jowidgets.cap.sample.app.common.service.executor.UserComponentExecutorServices;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.executor.IExecutorActionBuilder;
import org.jowidgets.cap.ui.api.executor.IParameterProvider;
import org.jowidgets.tools.command.ActionWrapper;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.types.Null;


public class ChangeGenderAction extends ActionWrapper {

	public ChangeGenderAction() {
		super(create());
	}

	@SuppressWarnings("null")
	private static IAction create() {
		final IExecutorActionBuilder<IUser, Null> builder = null;
		builder.setText("Change Gender");
		builder.setExecutor(UserComponentExecutorServices.CHANGE_GENDER);
		builder.addParameterProvider(new IParameterProvider<IUser, Null>() {

			@Override
			public IMaybe<Null> getParameter(
				final IExecutionContext executionContext,
				final List<IBeanProxy<IUser>> beans,
				final Null defaultParameter) {
				return Null.SOME_NULL;
			}

		});
		builder.addEnabledChecker(new ChangeGenderExecutableChecker());
		return builder.build();
	}
}
