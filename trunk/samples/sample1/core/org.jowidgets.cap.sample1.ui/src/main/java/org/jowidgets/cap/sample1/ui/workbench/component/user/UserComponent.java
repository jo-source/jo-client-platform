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

package org.jowidgets.cap.sample1.ui.workbench.component.user;

import org.jowidgets.cap.sample1.common.entity.EntityIds;
import org.jowidgets.cap.sample1.common.entity.IUser;
import org.jowidgets.cap.sample1.common.service.reader.ReaderServices;
import org.jowidgets.cap.sample1.ui.attribute.UserAttributesFactory;
import org.jowidgets.cap.sample1.ui.workbench.command.WorkbenchActions;
import org.jowidgets.cap.sample1.ui.workbench.component.user.view.RoleTableView;
import org.jowidgets.cap.sample1.ui.workbench.component.user.view.UserDetailGroupsBorderView;
import org.jowidgets.cap.sample1.ui.workbench.component.user.view.UserDetailGroupsSeparatorsView;
import org.jowidgets.cap.sample1.ui.workbench.component.user.view.UserDetailThreeColumnView;
import org.jowidgets.cap.sample1.ui.workbench.component.user.view.UserDetailView;
import org.jowidgets.cap.sample1.ui.workbench.component.user.view.UserTableView;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.model.LinkType;
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
	private final IBeanTableModel<Object> roleTableModel;

	public UserComponent(final IComponentNodeModel componentNodeModel, final IComponentContext componentContext) {
		componentContext.setLayout(new UserComponentDefaultLayout().getLayout());
		this.delayParameter = new ValueHolder<Integer>(Integer.valueOf(0));
		this.userTableModel = createUserTableModel();
		this.roleTableModel = createRoleTableModel(userTableModel);
	}

	@Override
	public IView createView(final String viewId, final IViewContext context) {
		if (UserTableView.ID.equals(viewId)) {
			return new UserTableView(context, userTableModel, delayParameter);
		}
		else if (RoleTableView.ID.equals(viewId)) {
			return new RoleTableView(context, roleTableModel);
		}
		else if (UserDetailView.ID.equals(viewId)) {
			return new UserDetailView(context, userTableModel);
		}
		else if (UserDetailThreeColumnView.ID.equals(viewId)) {
			return new UserDetailThreeColumnView(context, userTableModel);
		}
		else if (UserDetailGroupsBorderView.ID.equals(viewId)) {
			return new UserDetailGroupsBorderView(context, userTableModel);
		}
		else if (UserDetailGroupsSeparatorsView.ID.equals(viewId)) {
			return new UserDetailGroupsSeparatorsView(context, userTableModel);
		}
		else {
			throw new IllegalArgumentException("View id '" + viewId + "' is not known.");
		}
	}

	@Override
	public void onDispose() {}

	@Override
	public void onActivation() {
		WorkbenchActions.loadAction().addDataModel(userTableModel);
		WorkbenchActions.saveAction().addDataModel(userTableModel);
		WorkbenchActions.undoAction().addDataModel(userTableModel);
		WorkbenchActions.cancelAction().addDataModel(userTableModel);

		WorkbenchActions.saveAction().addDataModel(roleTableModel);
		WorkbenchActions.undoAction().addDataModel(roleTableModel);
		WorkbenchActions.cancelAction().addDataModel(roleTableModel);
	}

	@Override
	public void onDeactivation(final IVetoable vetoable) {
		WorkbenchActions.loadAction().removeDataModel(userTableModel);
		WorkbenchActions.saveAction().removeDataModel(userTableModel);
		WorkbenchActions.undoAction().removeDataModel(userTableModel);
		WorkbenchActions.cancelAction().removeDataModel(userTableModel);

		WorkbenchActions.saveAction().removeDataModel(roleTableModel);
		WorkbenchActions.undoAction().removeDataModel(roleTableModel);
		WorkbenchActions.cancelAction().removeDataModel(roleTableModel);
	}

	private IBeanTableModel<IUser> createUserTableModel() {
		final IBeanTableModelBuilder<IUser> builder = CapUiToolkit.beanTableModelBuilder(IUser.class);
		builder.setAttributes(new UserAttributesFactory().tableAttributes());
		builder.setReaderService(ReaderServices.ALL_USERS, createReaderParameterProvider());
		return builder.build();
	}

	private IBeanTableModel<Object> createRoleTableModel(final IBeanTableModel<IUser> userTableModel) {
		final IBeanTableModelBuilder<Object> builder = CapUiToolkit.beanTableModelBuilder(EntityIds.ROLE);
		builder.setParent(userTableModel, LinkType.SELECTION_ALL);
		builder.setReaderService(ReaderServices.ROLES_OF_USERS);
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
