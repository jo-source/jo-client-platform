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

package org.jowidgets.cap.sample.app.client.workbench.component.user.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.IInputField;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.sample.app.common.entity.IUser;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.tools.model.BeanListModelAdapter;
import org.jowidgets.common.widgets.controler.IInputListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.workbench.api.IViewContext;
import org.jowidgets.workbench.tools.AbstractView;

public class UserDetailView extends AbstractView {

	public static final String ID = UserDetailView.class.getName();
	public static final String DEFAULT_LABEL = "User details";
	public static final String DEFAULT_TOOLTIP = "Formular with user details";

	private IBeanProxy<IUser> currentBean;
	private final IInputField<String> nameField;
	private final IInputField<String> lastNameField;
	private final IInputListener nameListener;
	private final IInputListener lastNameListener;

	public UserDetailView(final IViewContext context, final IBeanTableModel<IUser> tableModel) {
		final IBluePrintFactory bpf = Toolkit.getBluePrintFactory();
		final IContainer container = context.getContainer();
		container.setLayout(new MigLayoutDescriptor("[][grow]", "[][]"));

		container.add(bpf.textLabel("Name"));
		nameField = container.add(bpf.inputFieldString(), "growx, wrap");

		container.add(bpf.textLabel("Lastname"));
		lastNameField = container.add(bpf.inputFieldString(), "growx");

		currentBean = tableModel.getFirstSelectedBean();

		final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				setBeanOnFields();
			}
		};

		nameListener = new IInputListener() {
			@Override
			public void inputChanged() {
				if (currentBean != null) {
					currentBean.removePropertyChangeListener(propertyChangeListener);
					currentBean.getBean().setName(nameField.getValue());
					currentBean.addPropertyChangeListener(propertyChangeListener);
				}
			}
		};
		nameField.addInputListener(nameListener);

		lastNameListener = new IInputListener() {
			@Override
			public void inputChanged() {
				if (currentBean != null) {
					currentBean.removePropertyChangeListener(propertyChangeListener);
					currentBean.getBean().setLastName(lastNameField.getValue());
					currentBean.addPropertyChangeListener(propertyChangeListener);
				}
			}
		};
		lastNameField.addInputListener(lastNameListener);

		tableModel.addBeanListModelListener(new BeanListModelAdapter() {
			@Override
			public void selectionChanged() {
				if (currentBean != null) {
					currentBean.removePropertyChangeListener(propertyChangeListener);
				}
				currentBean = tableModel.getFirstSelectedBean();
				currentBean.addPropertyChangeListener(propertyChangeListener);
				setBeanOnFields();
			}
		});

		setBeanOnFields();

	}

	private void setBeanOnFields() {
		lastNameField.removeInputListener(lastNameListener);
		nameField.removeInputListener(nameListener);
		if (currentBean != null) {
			nameField.setEditable(true);
			lastNameField.setEditable(true);
			nameField.setValue(currentBean.getBean().getName());
			lastNameField.setValue(currentBean.getBean().getLastName());
		}
		else {
			nameField.setValue(null);
			lastNameField.setValue(null);
			nameField.setEditable(false);
			lastNameField.setEditable(false);
		}
		lastNameField.addInputListener(lastNameListener);
		nameField.addInputListener(nameListener);
	}
}
