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

package org.jowidgets.cap.sample.app.client.workbench.component.user;

import org.jowidgets.cap.sample.app.client.attribute.UserAttributesFactory;
import org.jowidgets.cap.sample.app.client.workbench.command.WorkbenchActions;
import org.jowidgets.cap.sample.app.client.workbench.component.user.view.UserFormView;
import org.jowidgets.cap.sample.app.client.workbench.component.user.view.UserTableView;
import org.jowidgets.cap.sample.app.common.entity.IUser;
import org.jowidgets.cap.sample.app.common.service.reader.UserReaderServices;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.api.table.IReaderParameterProvider;
import org.jowidgets.common.types.IVetoable;
import org.jowidgets.util.ValueHolder;
import org.jowidgets.workbench.api.IComponent;
import org.jowidgets.workbench.api.IComponentContext;
import org.jowidgets.workbench.api.IView;
import org.jowidgets.workbench.api.IViewContext;
import org.jowidgets.workbench.toolkit.api.IComponentNodeModel;
import org.jowidgets.workbench.tools.AbstractComponent;

public class UserComponent extends AbstractComponent implements IComponent {

	private final ValueHolder<Integer> delayParameter;
	private final IBeanTableModel<IUser> userTableModel;

	public UserComponent(final IComponentNodeModel componentNodeModel, final IComponentContext componentContext) {
		componentContext.setLayout(new UserComponentDefaultLayout().getLayout());
		this.delayParameter = new ValueHolder<Integer>(Integer.valueOf(0));
		this.userTableModel = createUserTableModel();
	}

	@Override
	public IView createView(final String viewId, final IViewContext context) {
		if (UserTableView.ID.equals(viewId)) {
			return new UserTableView(context, userTableModel, delayParameter);
		}
		else if (UserFormView.ID.equals(viewId)) {
			return new UserFormView(context, userTableModel);
		}
		else {
			throw new IllegalArgumentException("View id '" + viewId + "' is not known.");
		}
	}

	@Override
	public void onDispose() {}

	@Override
	public void onActivation() {
		WorkbenchActions.LOAD_ACTION.addDataModel(userTableModel);
		WorkbenchActions.SAVE_ACTION.addDataModel(userTableModel);
		WorkbenchActions.UNDO_ACTION.addDataModel(userTableModel);
		WorkbenchActions.CANCEL_ACTION.addDataModel(userTableModel);
	}

	@Override
	public void onDeactivation(final IVetoable vetoable) {
		WorkbenchActions.LOAD_ACTION.removeDataModel(userTableModel);
		WorkbenchActions.SAVE_ACTION.removeDataModel(userTableModel);
		WorkbenchActions.UNDO_ACTION.removeDataModel(userTableModel);
		WorkbenchActions.CANCEL_ACTION.removeDataModel(userTableModel);
	}

	private IBeanTableModel<IUser> createUserTableModel() {
		final IBeanTableModelBuilder<IUser> builder = CapUiToolkit.createBeanTableModelBuilder(IUser.class);
		builder.setAttributes(new UserAttributesFactory().create());
		builder.setReaderService(UserReaderServices.ALL_USERS, createReaderParameterProvider());
		return builder.build();
	}

	private IReaderParameterProvider<Integer> createReaderParameterProvider() {
		return new IReaderParameterProvider<Integer>() {
			@Override
			public Integer getParameter() {
				return delayParameter.get();
			}
		};
	}

}
