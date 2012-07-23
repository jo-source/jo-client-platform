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

package org.jowidgets.cap.sample2.plugins.ui.action;

import java.util.Collections;
import java.util.List;

import org.jowidgets.addons.icons.silkicons.SilkIcons;
import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.cap.sample2.app.common.bean.IPerson;
import org.jowidgets.cap.sample2.app.common.security.AuthKeys;
import org.jowidgets.cap.security.common.api.ISecureObject;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.command.IExecutorActionBuilder;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutor;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.tools.command.ActionWrapper;

public class CreateTransientPersonAction extends ActionWrapper implements ISecureObject<String> {

	public CreateTransientPersonAction(final IBeanTable<IPerson> table) {
		super(create(table));
	}

	@Override
	public String getAuthorization() {
		return AuthKeys.CREATE_PERSON;
	}

	private static IAction create(final IBeanTable<IPerson> table) {
		final IExecutorActionBuilder<IPerson, Void> builder = CapUiToolkit.actionFactory().executorActionBuilder(table.getModel());
		builder.setText("Add new user");
		builder.setToolTipText("Adds a new user");
		builder.setIcon(SilkIcons.USER);
		builder.setSelectionPolicy(BeanSelectionPolicy.ANY_SELECTION);
		builder.setExecutor(createExecutor(table));
		return builder.build();
	}

	private static IExecutor<IPerson, Void> createExecutor(final IBeanTable<IPerson> table) {
		return new IExecutor<IPerson, Void>() {
			@Override
			public void execute(
				final IExecutionContext executionContext,
				final List<IBeanProxy<IPerson>> beans,
				final Void defaultParameter) throws Exception {
				final IBeanTableModel<IPerson> model = table.getModel();
				final IBeanProxy<IPerson> person = model.addTransientBean();
				model.setSelectedBeans(Collections.singleton(person));
				table.scrollToSelection();
			}
		};
	}

}
