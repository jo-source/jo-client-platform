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

package org.jowidgets.cap.sample1.ui.workbench.component.user.view;

import java.util.Date;
import java.util.Random;

import org.jowidgets.addons.icons.silkicons.SilkIcons;
import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.model.item.IContainerContentCreator;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.api.model.item.IToolBarModel;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.IInputField;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.sample1.common.entity.IUser;
import org.jowidgets.cap.sample1.ui.workbench.component.user.command.ChangeBirthdayAction;
import org.jowidgets.cap.sample1.ui.workbench.component.user.command.ChangeGenderAction;
import org.jowidgets.cap.sample1.ui.workbench.component.user.command.LongLastingAction;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IPopupMenuListener;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.model.item.MenuModel;
import org.jowidgets.util.ValueHolder;
import org.jowidgets.workbench.api.IViewContext;
import org.jowidgets.workbench.tools.AbstractView;

public class UserTableView extends AbstractView {

	public static final String ID = UserTableView.class.getName();
	public static final String DEFAULT_LABEL = Messages.getString("UserTableView.users"); //$NON-NLS-1$
	public static final String DEFAULT_TOOLTIP = Messages.getString("UserTableView.users_tooltip"); //$NON-NLS-1$

	private final IBeanTableModel<IUser> beanTableModel;
	private final IBeanTable<IUser> table;
	private final ValueHolder<Integer> parameter;

	private IInputField<Integer> delayField;

	public UserTableView(
		final IViewContext context,
		final IBeanTableModel<IUser> tableModel,
		final ValueHolder<Integer> parameter,
		final IAction userRoleLinkAction) {

		this.beanTableModel = tableModel;
		this.parameter = parameter;

		final IContainer container = context.getContainer();

		container.setLayout(MigLayoutFactory.growingInnerCellLayout());

		this.table = container.add(
				CapUiToolkit.bluePrintFactory().beanTable(beanTableModel),
				MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

		final IToolBarModel toolBar = context.getToolBar();
		toolBar.addContainer(createDelayFieldCreator());
		toolBar.addAction(createClearAction());
		toolBar.addAction(createPackAction());

		if (userRoleLinkAction != null) {
			table.getCellPopMenu().addAction(userRoleLinkAction);
			table.getCellPopMenu().addSeparator();
		}

		table.getCellPopMenu().addAction(new ChangeGenderAction(tableModel));
		table.getCellPopMenu().addAction(new ChangeBirthdayAction(tableModel));
		table.getCellPopMenu().addAction(new LongLastingAction(tableModel));

		table.getCellPopMenu().addSeparator();

		//example for dynamic menu
		final Random random = new Random();
		final IMenuModel dynamicMenuModel = createDynamicMenuModelStub();
		table.getCellPopMenu().addItem(dynamicMenuModel);
		table.addCellMenuListener(new IPopupMenuListener() {
			@Override
			public void beforeMenuShow() {
				fillDynamicMenuModel(dynamicMenuModel, random);
			}
		});

		beanTableModel.load();
	}

	private void fillDynamicMenuModel(final IMenuModel menuModel, final Random random) {
		menuModel.removeAllItems();
		menuModel.addActionItem("Its " + (new Date()));
		final int count = random.nextInt(5) + 1;
		for (int i = 0; i < count; i++) {
			menuModel.addActionItem("Random item " + i);
		}
	}

	private IMenuModel createDynamicMenuModelStub() {
		final IMenuModel result = new MenuModel();
		result.setText("Dynamic menu example");
		return result;
	}

	private IAction createClearAction() {
		final IActionBuilder builder = Toolkit.getActionBuilderFactory().create();
		builder.setText(Messages.getString("UserTableView.clear")).setToolTipText(Messages.getString("UserTableView.clear_tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
		builder.setCommand(new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				beanTableModel.clear();
			}
		});
		return builder.build();
	}

	private IAction createPackAction() {
		final IActionBuilder builder = Toolkit.getActionBuilderFactory().create();
		builder.setText(Messages.getString("UserTableView.pack")); //$NON-NLS-1$
		builder.setIcon(SilkIcons.ARROW_INOUT);
		builder.setCommand(new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				table.pack();
			}
		});
		return builder.build();
	}

	private IContainerContentCreator createDelayFieldCreator() {
		final IBluePrintFactory bpf = Toolkit.getBluePrintFactory();
		return new IContainerContentCreator() {

			@Override
			public void createContent(final IContainer container) {
				container.setLayout(new MigLayoutDescriptor("[][100!]", "0[]0")); //$NON-NLS-1$ //$NON-NLS-2$
				container.add(bpf.textLabel(Messages.getString("UserTableView.delay")), ""); //$NON-NLS-1$ //$NON-NLS-2$
				delayField = container.add(bpf.inputFieldIntegerNumber(), "w 100!"); //$NON-NLS-1$
				delayField.setValue(parameter.get());
				delayField.addInputListener(new IInputListener() {
					@Override
					public void inputChanged() {
						parameter.set(delayField.getValue());
					}
				});
			}

			@Override
			public void containerDisposed(final IContainer container) {}
		};
	}

}
