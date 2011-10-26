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
import java.util.Map;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.convert.IStringObjectConverter;
import org.jowidgets.api.model.item.IMenuItemModel;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IPopupMenu;
import org.jowidgets.api.widgets.ITable;
import org.jowidgets.api.widgets.IWidget;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.command.ICapActionFactory;
import org.jowidgets.cap.ui.api.command.ICreatorActionBuilder;
import org.jowidgets.cap.ui.api.command.IDeleterActionBuilder;
import org.jowidgets.cap.ui.api.filter.FilterType;
import org.jowidgets.cap.ui.api.sort.ISortModel;
import org.jowidgets.cap.ui.api.table.IBeanTableConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialog;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.cap.ui.api.widgets.ITableMenuCreationInterceptor;
import org.jowidgets.cap.ui.tools.attribute.AcceptEditableAttributesFilter;
import org.jowidgets.common.types.IVetoable;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.common.types.Position;
import org.jowidgets.common.widgets.controller.ITableCellEditEvent;
import org.jowidgets.common.widgets.controller.ITableCellPopupDetectionListener;
import org.jowidgets.common.widgets.controller.ITableCellPopupEvent;
import org.jowidgets.common.widgets.controller.ITableColumnMouseEvent;
import org.jowidgets.common.widgets.controller.ITableColumnPopupDetectionListener;
import org.jowidgets.common.widgets.controller.ITableColumnPopupEvent;
import org.jowidgets.tools.command.ActionWrapper;
import org.jowidgets.tools.command.ExecutionContextWrapper;
import org.jowidgets.tools.controller.ListModelAdapter;
import org.jowidgets.tools.controller.TableCellEditorAdapter;
import org.jowidgets.tools.controller.TableColumnAdapter;
import org.jowidgets.tools.model.item.MenuModel;
import org.jowidgets.tools.widgets.wrapper.TableWrapper;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.ITypedKey;

final class BeanTableImpl<BEAN_TYPE> extends TableWrapper implements IBeanTable<BEAN_TYPE> {

	private final IBeanTableModel<BEAN_TYPE> model;
	private final IMenuModel headerPopupMenuModel;
	private final IMenuModel cellPopupMenuModel;
	private final IMenuModel tablePopupMenuModel;
	private final Map<Integer, IPopupMenu> headerPopupMenus;
	private final Map<Integer, IPopupMenu> cellPopupMenus;
	private final ITableMenuCreationInterceptor<BEAN_TYPE> headerMenuInterceptor;
	private final ITableMenuCreationInterceptor<BEAN_TYPE> cellMenuInterceptor;
	private final boolean hasDefaultMenus;
	private final boolean hasDefaultCreatorAction;
	private final boolean hasDefaultDeleterAction;

	private IBeanTableSettingsDialog settingsDialog;
	private ITableCellPopupEvent currentCellEvent;
	private ITableColumnPopupEvent currentColumnEvent;

	BeanTableImpl(final ITable table, final IBeanTableBluePrint<BEAN_TYPE> bluePrint) {
		super(table);
		this.model = bluePrint.getModel();
		this.headerPopupMenus = new HashMap<Integer, IPopupMenu>();
		this.cellPopupMenus = new HashMap<Integer, IPopupMenu>();
		this.cellPopupMenuModel = new MenuModel();
		this.headerPopupMenuModel = new MenuModel();
		this.tablePopupMenuModel = new MenuModel();
		this.hasDefaultMenus = bluePrint.hasDefaultMenus();
		this.hasDefaultCreatorAction = bluePrint.hasDefaultCreatorAction();
		this.hasDefaultDeleterAction = bluePrint.hasDefaultDeleterAction();
		this.headerMenuInterceptor = bluePrint.getHeaderMenuInterceptor();
		this.cellMenuInterceptor = bluePrint.getCellMenuInterceptor();

		table.setPopupMenu(tablePopupMenuModel);

		headerPopupMenuModel.addListModelListener(new CustomMenuModelListener());
		cellPopupMenuModel.addListModelListener(new CustomMenuModelListener());

		if (bluePrint.hasDefaultMenus()) {
			final IBeanTableMenuFactory menuFactory = CapUiToolkit.beanTableMenuFactory();

			//table popup menu
			tablePopupMenuModel.addItem(menuFactory.columnsVisibilityMenu(model));
			tablePopupMenuModel.addAction(menuFactory.showAllColumnsAction(this));
			tablePopupMenuModel.addSeparator();
			tablePopupMenuModel.addAction(menuFactory.clearCurrentSortAction(model));
			tablePopupMenuModel.addAction(menuFactory.clearDefaultSortAction(model));
			tablePopupMenuModel.addSeparator();
			tablePopupMenuModel.addAction(menuFactory.packAllAction(this));
			tablePopupMenuModel.addSeparator();
			tablePopupMenuModel.addAction(menuFactory.settingsAction(this));
			tablePopupMenuModel.addItem(menuFactory.filterMenu(model));

			final ICapActionFactory actionFactory = CapUiToolkit.actionFactory();
			if (hasDefaultCreatorAction && model.getCreatorService() != null) {
				if (hasDefaultMenus) {
					tablePopupMenuModel.addSeparator();
				}
				final ICreatorActionBuilder creatorActionBuilder = actionFactory.creatorActionBuilder(model.getBeanType(), model);
				creatorActionBuilder.setCreatorService(model.getCreatorService());
				creatorActionBuilder.setBeanForm(model.getAttributes(AcceptEditableAttributesFilter.getInstance()));
				tablePopupMenuModel.addAction(creatorActionBuilder.build());
			}
			if (hasDefaultDeleterAction && model.getDeleterService() != null) {
				if (hasDefaultMenus && !hasDefaultCreatorAction) {
					tablePopupMenuModel.addSeparator();
				}
				final IDeleterActionBuilder<BEAN_TYPE> deleterActionBuilder = actionFactory.deleterActionBuilder(model);
				deleterActionBuilder.setDeleterService(model.getDeleterService());
				tablePopupMenuModel.addAction(deleterActionBuilder.build());
			}
			table.setPopupMenu(tablePopupMenuModel);
		}

		table.addTableCellPopupDetectionListener(new ITableCellPopupDetectionListener() {
			@Override
			public void popupDetected(final ITableCellPopupEvent event) {
				final IPopupMenu cellPopupMenu = getCellPopupMenu(event.getColumnIndex());
				if (cellPopupMenu.getChildren().size() > 0) {
					currentCellEvent = event;
					//simulate a column event
					currentColumnEvent = new TableColumnPopupEvent(event);
					cellPopupMenu.show(event.getPosition());
				}
			}
		});

		table.addTableColumnPopupDetectionListener(new ITableColumnPopupDetectionListener() {
			@Override
			public void popupDetected(final ITableColumnPopupEvent event) {
				final IPopupMenu headerPopupMenu = getHeaderPopupMenu(event.getColumnIndex());
				if (headerPopupMenu.getChildren().size() > 0) {
					currentColumnEvent = event;
					headerPopupMenu.show(event.getPosition());
				}
			}
		});

		table.addTableCellEditorListener(new TableCellEditorListener());
		table.addTableColumnListener(new TableSortColumnListener());
	}

	private IPopupMenu getHeaderPopupMenu(final Integer index) {
		IPopupMenu popupMenu = headerPopupMenus.get(index);
		if (popupMenu == null) {
			popupMenu = createHeaderPopupMenu(index.intValue());
			headerPopupMenus.put(index, popupMenu);
		}
		else if (popupMenu.getChildren().size() == 0) {
			popupMenu.setModel(createHeaderPopupMenuModel(index));
		}
		return popupMenu;
	}

	private IPopupMenu createHeaderPopupMenu(final Integer index) {
		final IPopupMenu popupMenu = getWidget().createPopupMenu();
		popupMenu.setModel(createHeaderPopupMenuModel(index));
		return popupMenu;
	}

	private IMenuModel createHeaderPopupMenuModel(final Integer index) {
		final IMenuModel menuModel = createHeaderPopupMenuModelUndecorated(index);
		menuModel.addDecorator(createDecorator(true));
		return menuModel;
	}

	private IMenuModel createHeaderPopupMenuModelUndecorated(final Integer index) {

		final IMenuModel menuModel;
		if (hasDefaultMenus) {
			menuModel = CapUiToolkit.beanTableMenuFactory().headerPopupMenu(this, index.intValue());
		}
		else {
			menuModel = new MenuModel();
		}

		if (headerMenuInterceptor != null) {
			headerMenuInterceptor.afterMenuCreated(this, menuModel, index.intValue());
		}

		if (headerPopupMenuModel.getChildren().size() > 0) {
			for (final IMenuItemModel itemModel : headerPopupMenuModel.getChildren()) {
				menuModel.addItem(itemModel);
			}
		}

		return menuModel;
	}

	private IPopupMenu getCellPopupMenu(final Integer index) {
		IPopupMenu popupMenu = cellPopupMenus.get(index);
		if (popupMenu == null) {
			popupMenu = createCellPopupMenu(index.intValue());
			cellPopupMenus.put(index, popupMenu);
		}
		else if (popupMenu.getChildren().size() == 0) {
			popupMenu.setModel(createCellPopupMenuModel(index));
		}
		return popupMenu;
	}

	private IPopupMenu createCellPopupMenu(final Integer index) {
		final IPopupMenu popupMenu = getWidget().createPopupMenu();
		popupMenu.setModel(createCellPopupMenuModel(index));
		return popupMenu;
	}

	private IMenuModel createCellPopupMenuModel(final Integer index) {

		final IMenuModel menuModel;
		if (hasDefaultMenus) {
			menuModel = CapUiToolkit.beanTableMenuFactory().cellPopupMenu(
					this,
					createHeaderPopupMenuModelUndecorated(index),
					index.intValue());
		}
		else {
			menuModel = new MenuModel();
		}

		final ICapActionFactory actionFactory = CapUiToolkit.actionFactory();
		if (hasDefaultCreatorAction && model.getCreatorService() != null) {
			if (hasDefaultMenus) {
				menuModel.addSeparator();
			}
			final ICreatorActionBuilder creatorActionBuilder = actionFactory.creatorActionBuilder(model.getBeanType(), model);
			creatorActionBuilder.setCreatorService(model.getCreatorService());
			creatorActionBuilder.setBeanForm(model.getAttributes(AcceptEditableAttributesFilter.getInstance()));
			menuModel.addAction(creatorActionBuilder.build());
		}
		if (hasDefaultDeleterAction && model.getDeleterService() != null) {
			if (hasDefaultMenus && !hasDefaultCreatorAction) {
				menuModel.addSeparator();
			}
			final IDeleterActionBuilder<BEAN_TYPE> deleterActionBuilder = actionFactory.deleterActionBuilder(model);
			deleterActionBuilder.setDeleterService(model.getDeleterService());
			menuModel.addAction(deleterActionBuilder.build());
		}

		if (cellMenuInterceptor != null) {
			cellMenuInterceptor.afterMenuCreated(this, menuModel, index.intValue());
		}

		if (cellPopupMenuModel.getChildren().size() > 0) {
			for (final IMenuItemModel itemModel : cellPopupMenuModel.getChildren()) {
				menuModel.addItem(itemModel);
			}
		}

		menuModel.addDecorator(createDecorator(false));

		return menuModel;
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

		@Override
		public void onEdit(final IVetoable veto, final ITableCellEditEvent event) {
			final IBeanProxy<BEAN_TYPE> bean = model.getBean(event.getRowIndex());
			final IAttribute<Object> attribute = model.getAttribute(event.getColumnIndex());
			if (bean != null && attribute != null && !attribute.isCollectionType()) {
				if (bean.hasExecution()) {
					veto.veto();
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
			else if (hasDefaultMenus && event.getModifiers().contains(Modifier.ALT)) {
				final IAction filterAction = CapUiToolkit.beanTableMenuFactory().addFilterAction(
						model,
						FilterType.ARITHMETIC_FILTER,
						modelColumn);
				try {
					filterAction.execute(new IExecutionContext() {
						@Override
						public <VALUE_TYPE> VALUE_TYPE getValue(final ITypedKey<VALUE_TYPE> key) {
							return null;
						}

						@Override
						public IWidget getSource() {
							return BeanTableImpl.this;
						}

						@Override
						public IAction getAction() {
							return filterAction;
						}
					});
				}
				catch (final Exception e) {
					//TODO MG handle exception
				}
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

	private class CustomMenuModelListener extends ListModelAdapter {

		@Override
		public void afterChildRemoved(final int index) {
			clearMenus();
		}

		@Override
		public void afterChildAdded(final int index) {
			clearMenus();
		}

		private void clearMenus() {
			for (final IPopupMenu popupMenu : cellPopupMenus.values()) {
				popupMenu.setModel(new MenuModel());
			}
			for (final IPopupMenu popupMenu : headerPopupMenus.values()) {
				popupMenu.setModel(new MenuModel());
			}
		}

	}

}
