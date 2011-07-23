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

package org.jowidgets.cap.ui.impl.widgets;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.convert.IStringObjectConverter;
import org.jowidgets.api.model.item.IActionItemModel;
import org.jowidgets.api.model.item.IItemModelFactory;
import org.jowidgets.api.model.item.IMenuItemModel;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IPopupMenu;
import org.jowidgets.api.widgets.ITable;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.sort.ISortModel;
import org.jowidgets.cap.ui.api.table.IBeanTableConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialog;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.common.types.Position;
import org.jowidgets.common.widgets.controler.ITableCellEditEvent;
import org.jowidgets.common.widgets.controler.ITableCellPopupDetectionListener;
import org.jowidgets.common.widgets.controler.ITableCellPopupEvent;
import org.jowidgets.common.widgets.controler.ITableColumnMouseEvent;
import org.jowidgets.common.widgets.controler.ITableColumnPopupDetectionListener;
import org.jowidgets.common.widgets.controler.ITableColumnPopupEvent;
import org.jowidgets.tools.command.ActionWrapper;
import org.jowidgets.tools.command.ExecutionContextWrapper;
import org.jowidgets.tools.controler.TableCellEditorAdapter;
import org.jowidgets.tools.controler.TableColumnAdapter;
import org.jowidgets.tools.model.item.MenuModel;
import org.jowidgets.tools.model.item.SeparatorItemModel;
import org.jowidgets.tools.widgets.wrapper.TableWrapper;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.ITypedKey;

final class BeanTableImpl<BEAN_TYPE> extends TableWrapper implements IBeanTable<BEAN_TYPE> {

	private final IBeanTableModel<BEAN_TYPE> model;
	private final IMenuModel cellPopupMenuModel;
	private final IMenuModel headerPopupMenuModel;
	private final IMenuModel tablePopupMenuModel;
	private final IPopupMenu columnPopupMenu;
	private final boolean hasDefaultMenus;
	private final Map<Integer, List<IMenuItemModel>> headerMenus;
	private final List<IMenuItemModel> tempHeaderMenuItems;

	private IBeanTableSettingsDialog settingsDialog;
	private ITableCellPopupEvent currentCellEvent;
	private ITableColumnPopupEvent currentColumnEvent;

	BeanTableImpl(final ITable table, final IBeanTableBluePrint<BEAN_TYPE> bluePrint) {
		super(table);
		this.model = bluePrint.getModel();
		this.hasDefaultMenus = bluePrint.hasDefaultMenus();
		this.headerMenus = createHeaderMenus(model, hasDefaultMenus);
		this.tempHeaderMenuItems = new LinkedList<IMenuItemModel>();
		this.cellPopupMenuModel = new MenuModel();
		this.headerPopupMenuModel = new MenuModel("Header");
		this.tablePopupMenuModel = new MenuModel();

		table.setPopupMenu(tablePopupMenuModel);

		if (bluePrint.hasDefaultMenus()) {
			final IBeanTableMenuFactory menuFactory = CapUiToolkit.beanTableMenuFactory();

			//cell popup menu
			final IAction settingsDialogAction = menuFactory.settingsAction(this);
			cellPopupMenuModel.addItem(headerPopupMenuModel);
			cellPopupMenuModel.addAction(settingsDialogAction);

			//header popup menu
			final IAction packAllAction = menuFactory.packAllAction(this);
			final IAction packSelectedAction = menuFactory.packSelectedAction(this);
			final IAction hideColumnAction = menuFactory.hideColumnAction(this);
			final IMenuModel columnsVisibilityMenu = menuFactory.columnsVisibilityMenu(model);
			final IAction showAllColumnsAction = menuFactory.showAllColumnsAction(this);
			headerPopupMenuModel.addAction(hideColumnAction);
			headerPopupMenuModel.addItem(columnsVisibilityMenu);
			headerPopupMenuModel.addAction(showAllColumnsAction);
			headerPopupMenuModel.addSeparator();
			headerPopupMenuModel.addAction(packSelectedAction);
			headerPopupMenuModel.addAction(packAllAction);

			//table popup menu
			tablePopupMenuModel.addItem(columnsVisibilityMenu);
			tablePopupMenuModel.addAction(showAllColumnsAction);
			tablePopupMenuModel.addSeparator();
			tablePopupMenuModel.addAction(menuFactory.clearCurrentSortAction(model));
			tablePopupMenuModel.addAction(menuFactory.clearDefaultSortAction(model));
			tablePopupMenuModel.addSeparator();
			tablePopupMenuModel.addAction(packAllAction);
			tablePopupMenuModel.addSeparator();
			tablePopupMenuModel.addAction(settingsDialogAction);
			table.setPopupMenu(tablePopupMenuModel);
		}

		//add cell popup menu
		final IPopupMenu cellPopupMenu = table.createPopupMenu();
		cellPopupMenu.setModel(cellPopupMenuModel);
		cellPopupMenuModel.addDecorator(createDecorator(false));
		table.addTableCellPopupDetectionListener(new ITableCellPopupDetectionListener() {
			@Override
			public void popupDetected(final ITableCellPopupEvent event) {
				if (cellPopupMenu.getChildren().size() > 0) {
					currentCellEvent = event;
					//simulate a column event
					currentColumnEvent = new TableColumnPopupEvent(event);
					cellPopupMenu.show(event.getPosition());
				}
			}
		});

		//add column popup menu
		this.columnPopupMenu = table.createPopupMenu();
		columnPopupMenu.setModel(headerPopupMenuModel);
		headerPopupMenuModel.addDecorator(createDecorator(true));
		table.addTableColumnPopupDetectionListener(new ITableColumnPopupDetectionListener() {
			@Override
			public void popupDetected(final ITableColumnPopupEvent event) {
				if (columnPopupMenu.getChildren().size() > 0) {
					currentColumnEvent = event;
					showHeaderPopupMenu(event.getPosition(), event.getColumnIndex());
				}
			}
		});

		table.addTableCellEditorListener(new TableCellEditorListener());
		table.addTableColumnListener(new TableSortColumnListener());
	}

	private static Map<Integer, List<IMenuItemModel>> createHeaderMenus(
		final IBeanTableModel<?> model,
		final boolean hasDefaultMenus) {

		final Map<Integer, List<IMenuItemModel>> result = new HashMap<Integer, List<IMenuItemModel>>();
		if (hasDefaultMenus) {
			final IBeanTableMenuFactory menuFactory = CapUiToolkit.beanTableMenuFactory();
			final IAction clearCurrentSortAction = menuFactory.clearCurrentSortAction(model);
			final IAction clearDefaultSortAction = menuFactory.clearDefaultSortAction(model);
			for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
				//create items list for this column
				final List<IMenuItemModel> menuItems = new LinkedList<IMenuItemModel>();
				result.put(Integer.valueOf(columnIndex), menuItems);

				//add separator
				menuItems.add(new SeparatorItemModel());

				//add display format menus
				final IMenuModel headerFormatMenu = menuFactory.headerFormatMenu(model, columnIndex);
				final IMenuModel contentFormatMenu = menuFactory.contentFormatMenu(model, columnIndex);

				if (contentFormatMenu != null) {
					menuItems.add(contentFormatMenu);
				}
				if (headerFormatMenu != null) {
					menuItems.add(headerFormatMenu);
				}

				//add alignment menu
				final IMenuModel alignmentMenu = menuFactory.alignmentMenu(model, columnIndex);
				menuItems.add(alignmentMenu);

				//add the sort menus
				menuItems.add(new SeparatorItemModel());
				final IItemModelFactory itemModelFactory = Toolkit.getModelFactoryProvider().getItemModelFactory();

				final IActionItemModel clearDefaultSortingModel = itemModelFactory.actionItem();
				clearDefaultSortingModel.setAction(clearDefaultSortAction);
				menuItems.add(clearDefaultSortingModel);

				final IMenuModel defaultSortMenu = menuFactory.defaultSortMenu(model, columnIndex);
				if (defaultSortMenu != null) {
					menuItems.add(defaultSortMenu);
				}

				menuItems.add(new SeparatorItemModel());

				final IActionItemModel clearCurrentSortingModel = itemModelFactory.actionItem();
				clearCurrentSortingModel.setAction(clearCurrentSortAction);
				menuItems.add(clearCurrentSortingModel);

				final IMenuModel currentSortMenu = menuFactory.currentSortMenu(model, columnIndex);
				if (currentSortMenu != null) {
					menuItems.add(currentSortMenu);
				}

			}
		}
		return result;
	}

	private void showHeaderPopupMenu(final Position position, final int columnIndex) {
		if (hasDefaultMenus) {
			for (final IMenuItemModel tempMenuItem : new LinkedList<IMenuItemModel>(tempHeaderMenuItems)) {
				tempHeaderMenuItems.remove(tempMenuItem);
				headerPopupMenuModel.removeItem(tempMenuItem);
			}
			final int headerFormatIndex = 4;
			for (final IMenuItemModel menuItem : headerMenus.get(Integer.valueOf(columnIndex))) {
				headerPopupMenuModel.addItem(headerFormatIndex, menuItem);
				tempHeaderMenuItems.add(menuItem);
			}
		}
		columnPopupMenu.show(position);
	}

	@Override
	protected ITable getWidget() {
		return super.getWidget();
	}

	@Override
	public IBeanTableModel<BEAN_TYPE> getModel() {
		return model;
	}

	@Override
	public void showSettingsDialog() {
		final IBeanTableSettingsDialog dialog = getSettingsDialog();
		final IBeanTableConfig tableConfig = dialog.show();
		if (dialog.isOkPressed()) {
			model.setConfig(tableConfig);
		}
	}

	@Override
	public IMenuModel getCellPopMenu() {
		return cellPopupMenuModel;
	}

	@Override
	public IMenuModel getHeaderPopMenu() {
		return headerPopupMenuModel;
	}

	private IBeanTableSettingsDialog getSettingsDialog() {
		if (settingsDialog == null) {
			final ICapApiBluePrintFactory bpf = CapUiToolkit.bluePrintFactory();
			settingsDialog = Toolkit.getActiveWindow().createChildWindow(bpf.beanTableSettingsDialog(model));
		}
		return settingsDialog;
	}

	private IDecorator<IAction> createDecorator(final boolean header) {
		return new IDecorator<IAction>() {
			@Override
			public IAction decorate(final IAction original) {
				return new ActionWrapper(original) {
					@Override
					public void execute(final IExecutionContext executionContext) throws Exception {
						super.execute(getDecoratedExecutionContext(executionContext, header));
					}

					@Override
					public String toString() {
						return original.toString();
					}

				};
			}
		};
	}

	private IExecutionContext getDecoratedExecutionContext(final IExecutionContext executionContext, final boolean header) {
		return new ExecutionContextWrapper(executionContext) {
			@SuppressWarnings("unchecked")
			@Override
			public <VALUE_TYPE> VALUE_TYPE getValue(final ITypedKey<VALUE_TYPE> key) {
				if (header && IBeanTable.COLUMN_POPUP_EVENT_CONTEXT_KEY == key) {
					return (VALUE_TYPE) currentColumnEvent;
				}
				else if (IBeanTable.CELL_POPUP_EVENT_CONTEXT_KEY == key) {
					return (VALUE_TYPE) currentCellEvent;
				}
				else {
					return executionContext.getValue(key);
				}
			}
		};
	}

	private class TableCellEditorListener extends TableCellEditorAdapter {
		@Override
		public void editFinished(final ITableCellEditEvent event) {
			final IBeanProxy<BEAN_TYPE> bean = model.getBean(event.getRowIndex());
			final IAttribute<Object> attribute = model.getAttribute(event.getColumnIndex());
			if (bean != null && attribute != null && !attribute.isCollectionType()) {
				final IStringObjectConverter<Object> converter = attribute.getCurrentControlPanel().getStringObjectConverter();
				if (converter != null) {
					if (converter.getStringValidator() == null
						|| converter.getStringValidator().validate(event.getCurrentText()).isValid()) {
						final Object value = converter.convertToObject(event.getCurrentText());
						bean.setValue(attribute.getPropertyName(), value);
					}
				}
			}
		}
	}

	private class TableSortColumnListener extends TableColumnAdapter {
		@Override
		public void mouseClicked(final ITableColumnMouseEvent event) {
			final int modelColumn = event.getColumnIndex();
			if (modelColumn < 0) {
				return;
			}
			if (event.getModifiers().contains(Modifier.SHIFT)) {
				pack();
				return;
			}
			final IAttribute<?> attribute = model.getAttribute(modelColumn);
			if (attribute != null && attribute.isSortable()) {
				final ISortModel sortModel = model.getSortModel();
				final String propertyName = attribute.getPropertyName();
				if (event.getModifiers().contains(Modifier.CTRL)) {
					sortModel.addOrToggleCurrentProperty(propertyName);
				}
				else {
					sortModel.setOrToggleCurrentProperty(propertyName);
				}
			}
		}
	}

	private final class TableColumnPopupEvent implements ITableColumnPopupEvent {

		private final ITableCellPopupEvent cellPopupEvent;

		private TableColumnPopupEvent(final ITableCellPopupEvent cellPopupEvent) {
			this.cellPopupEvent = cellPopupEvent;
		}

		@Override
		public int getColumnIndex() {
			return cellPopupEvent.getColumnIndex();
		}

		@Override
		public Position getPosition() {
			return cellPopupEvent.getPosition();
		}

	}

}
