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

import java.util.List;

import org.jowidgets.api.model.item.IActionItemModel;
import org.jowidgets.api.model.item.IContainerContentCreator;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.IInputField;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.sample.app.common.entity.IUser;
import org.jowidgets.cap.sample.app.common.service.reader.UserReaderServices;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifierBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeModifier;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.api.table.IReaderParameterProvider;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IDataApiBluePrintFactory;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.common.widgets.controler.IActionListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.workbench.api.IViewContext;
import org.jowidgets.workbench.tools.AbstractView;


public class UserTableView extends AbstractView {

	public static final String ID = UserTableView.class.getName();
	public static final String DEFAULT_LABEL = "Users";
	public static final String DEFAULT_TOOLTIP = "Table of all users";

	private IInputField<Integer> delayField;

	public UserTableView(final IViewContext context) {

		final IBluePrintFactory bpf = Toolkit.getBluePrintFactory();

		final IBeanTableModelBuilder<IUser> builder = CapUiToolkit.createBeanTableModelBuilder(IUser.class);

		final List<IProperty> properties = ServiceProvider.getService(IEntityService.ID).getDescriptor(IUser.class).getProperties();

		final IAttributeCollectionModifierBuilder modifierBuilder = CapUiToolkit.getAttributeToolkit().createAttributeCollectionModifierBuilder();
		modifierBuilder.addModifier(IUser.GENDER_PROPERTY, new IAttributeModifier() {
			@Override
			public void modify(final IProperty sourceProperty, final IAttributeBuilder<?> attributeBuilder) {
				attributeBuilder.setTableAlignment(AlignmentHorizontal.CENTER);
			}
		});
		modifierBuilder.addDefaultEditableModifier(true);

		final List<IAttribute<Object>> attributes = CapUiToolkit.getAttributeToolkit().createAttributes(
				properties,
				modifierBuilder.build());

		builder.setAttributes(attributes);
		builder.setReaderService(UserReaderServices.ALL_USERS, new IReaderParameterProvider<Integer>() {

			@Override
			public Integer getParameter() {
				if (delayField != null) {
					return delayField.getValue();
				}
				return null;
			}
		});

		final IBeanTableModel<IUser> beanTableModel = builder.build();

		context.getContainer().setLayout(MigLayoutFactory.growingInnerCellLayout());

		final IDataApiBluePrintFactory dbpf = CapUiToolkit.getBluePrintFactory();

		final IBeanTable<IUser> table = context.getContainer().add(
				dbpf.beanTable(beanTableModel),
				MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

		final IActionItemModel reloadAction = context.getToolBar().addActionItem("Reload", "Reload the data");
		reloadAction.addActionListener(new IActionListener() {
			@Override
			public void actionPerformed() {
				beanTableModel.loadData();
			}
		});

		context.getToolBar().addContainer(new IContainerContentCreator() {
			@Override
			public void createContent(final IContainer container) {
				container.setLayout(new MigLayoutDescriptor("[][100!]", "0[]0"));
				container.add(bpf.textLabel("Delay (ms)"), "");
				delayField = container.add(bpf.inputFieldIntegerNumber(), "w 100!");
				delayField.setValue(1000);
			}

			@Override
			public void containerDisposed(final IContainer container) {}
		});

		final IActionItemModel clearAction = context.getToolBar().addActionItem("Clear", "Clear the data");
		clearAction.addActionListener(new IActionListener() {
			@Override
			public void actionPerformed() {
				beanTableModel.clearData();
			}
		});

		final IActionItemModel packAction = context.getToolBar().addActionItem("Pack");
		packAction.addActionListener(new IActionListener() {
			@Override
			public void actionPerformed() {
				table.pack();
			}
		});

		beanTableModel.loadData();

	}
}
