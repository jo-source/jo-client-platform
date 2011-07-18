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

package org.jowidgets.cap.sample1.ui.workbench.component.dynbeans.view;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.model.item.IToolBarModel;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.IPopupMenu;
import org.jowidgets.cap.sample1.common.entity.IDynamicPropertiesBean;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.common.widgets.controler.ITableCellPopupDetectionListener;
import org.jowidgets.common.widgets.controler.ITableCellPopupEvent;
import org.jowidgets.common.widgets.controler.ITableColumnPopupDetectionListener;
import org.jowidgets.common.widgets.controler.ITableColumnPopupEvent;
import org.jowidgets.examples.common.icons.SilkIcons;
import org.jowidgets.tools.command.ExecutionContextValues;
import org.jowidgets.workbench.api.IViewContext;
import org.jowidgets.workbench.tools.AbstractView;

public class DynamicPropertiesBeanTableView extends AbstractView {

	public static final String ID = DynamicPropertiesBeanTableView.class.getName();
	public static final String DEFAULT_LABEL = "Dynamic properties";
	public static final String DEFAULT_TOOLTIP = "Table with dynamic properties";

	private final IBeanTableModel<IDynamicPropertiesBean> beanTableModel;
	private final IBeanTable<IDynamicPropertiesBean> table;

	private int popupColumn;

	public DynamicPropertiesBeanTableView(final IViewContext context, final IBeanTableModel<IDynamicPropertiesBean> tableModel) {

		this.beanTableModel = tableModel;
		this.popupColumn = -1;

		final IContainer container = context.getContainer();

		container.setLayout(Toolkit.getLayoutFactoryProvider().fillLayout());

		this.table = container.add(CapUiToolkit.getBluePrintFactory().beanTable(beanTableModel));

		final IToolBarModel toolBar = context.getToolBar();
		toolBar.addAction(createClearAction());
		toolBar.addAction(createPackAction());

		final IPopupMenu popupMenu = table.createPopupMenu();

		table.addTableCellPopupDetectionListener(new ITableCellPopupDetectionListener() {
			@Override
			public void popupDetected(final ITableCellPopupEvent event) {
				popupMenu.show(event.getPosition());
			}
		});

		final IPopupMenu columnPopupMenu = table.createPopupMenu();
		columnPopupMenu.addAction(createHideColumnAction(beanTableModel));
		columnPopupMenu.addAction(createUnhideAllColumnAction(beanTableModel));

		table.addTableColumnPopupDetectionListener(new ITableColumnPopupDetectionListener() {
			@Override
			public void popupDetected(final ITableColumnPopupEvent event) {
				popupColumn = event.getColumnIndex();
				final ExecutionContextValues executionContextValues = new ExecutionContextValues();
				columnPopupMenu.show(event.getPosition(), executionContextValues);
			}
		});

		beanTableModel.load();
	}

	private IAction createHideColumnAction(final IBeanTableModel<?> beanTableModel) {
		final IActionBuilder builder = Toolkit.getActionBuilderFactory().create();
		builder.setText("Hide column");
		builder.setCommand(new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				beanTableModel.getAttribute(popupColumn).setVisible(false);
			}
		});
		return builder.build();
	}

	private IAction createUnhideAllColumnAction(final IBeanTableModel<?> beanTableModel) {
		final IActionBuilder builder = Toolkit.getActionBuilderFactory().create();
		builder.setText("Unhide all columns");
		builder.setCommand(new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				for (int i = 0; i < beanTableModel.getColumnCount(); i++) {
					beanTableModel.getAttribute(i).setVisible(true);
				}
			}
		});
		return builder.build();
	}

	private IAction createClearAction() {
		final IActionBuilder builder = Toolkit.getActionBuilderFactory().create();
		builder.setText("Clear").setToolTipText("Clear the data");
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
		builder.setText("Pack");
		builder.setIcon(SilkIcons.ARROW_INOUT);
		builder.setCommand(new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				table.pack();
			}
		});
		return builder.build();
	}

}
