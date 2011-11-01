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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.convert.IStringObjectConverter;
import org.jowidgets.api.model.item.ICheckedItemModel;
import org.jowidgets.api.model.item.IMenuItemModel;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IPopupMenu;
import org.jowidgets.api.widgets.ITable;
import org.jowidgets.api.widgets.IWidget;
import org.jowidgets.api.widgets.blueprint.ITableBluePrint;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.command.ICapActionFactory;
import org.jowidgets.cap.ui.api.command.ICreatorActionBuilder;
import org.jowidgets.cap.ui.api.command.IDeleterActionBuilder;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.filter.FilterType;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
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
import org.jowidgets.cap.ui.tools.model.BeanListModelWrapper;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.common.types.IVetoable;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.common.types.Position;
import org.jowidgets.common.types.TablePackPolicy;
import org.jowidgets.common.types.VirtualKey;
import org.jowidgets.common.widgets.controller.IKeyEvent;
import org.jowidgets.common.widgets.controller.IKeyListener;
import org.jowidgets.common.widgets.controller.ITableCellEditEvent;
import org.jowidgets.common.widgets.controller.ITableCellEditorListener;
import org.jowidgets.common.widgets.controller.ITableCellListener;
import org.jowidgets.common.widgets.controller.ITableCellPopupDetectionListener;
import org.jowidgets.common.widgets.controller.ITableCellPopupEvent;
import org.jowidgets.common.widgets.controller.ITableColumnListener;
import org.jowidgets.common.widgets.controller.ITableColumnMouseEvent;
import org.jowidgets.common.widgets.controller.ITableColumnPopupDetectionListener;
import org.jowidgets.common.widgets.controller.ITableColumnPopupEvent;
import org.jowidgets.common.widgets.controller.ITableSelectionListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.command.ActionWrapper;
import org.jowidgets.tools.command.ExecutionContextWrapper;
import org.jowidgets.tools.controller.KeyAdapter;
import org.jowidgets.tools.controller.ListModelAdapter;
import org.jowidgets.tools.controller.TableCellEditorAdapter;
import org.jowidgets.tools.controller.TableColumnAdapter;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.model.item.MenuModel;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.tools.widgets.wrapper.CompositeWrapper;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.ITypedKey;

final class BeanTableImpl<BEAN_TYPE> extends CompositeWrapper implements IBeanTable<BEAN_TYPE> {

	private final ITable table;
	private final IBeanTableModel<BEAN_TYPE> model;
	private final BeanTableSearchFilterToolbar<BEAN_TYPE> searchFilterToolbar;
	private final BeanTableStatusBar<BEAN_TYPE> statusBar;
	private final IMenuModel headerPopupMenuModel;
	private final IMenuModel cellPopupMenuModel;
	private final IMenuModel customTablePopupMenuModel;
	private final IMenuModel tablePopupMenuModel;
	private final Map<Integer, IPopupMenu> headerPopupMenus;
	private final Map<Integer, IPopupMenu> cellPopupMenus;
	private final ITableMenuCreationInterceptor<BEAN_TYPE> headerMenuInterceptor;
	private final ITableMenuCreationInterceptor<BEAN_TYPE> cellMenuInterceptor;
	private final boolean hasDefaultMenus;
	private final boolean hasDefaultCreatorAction;
	private final boolean hasDefaultDeleterAction;

	private IAction creatorAction;
	private IAction deleteAction;
	private IBeanTableSettingsDialog settingsDialog;
	private ITableCellPopupEvent currentCellEvent;
	private ITableColumnPopupEvent currentColumnEvent;

	BeanTableImpl(final IComposite composite, final IBeanTableBluePrint<BEAN_TYPE> bluePrint) {
		super(composite);
		composite.setLayout(new MigLayoutDescriptor("hidemode 2", "0[grow, 0::]0", "0[]0[grow, 0::]0[]0"));

		this.model = bluePrint.getModel();
		final ITableBluePrint tableBp = BPF.table(model.getTableModel());
		tableBp.setSetup(bluePrint);
		this.table = composite.add(tableBp, MigLayoutFactory.GROWING_CELL_CONSTRAINTS + ", wrap");

		this.headerPopupMenus = new HashMap<Integer, IPopupMenu>();
		this.cellPopupMenus = new HashMap<Integer, IPopupMenu>();
		this.cellPopupMenuModel = new MenuModel();
		this.headerPopupMenuModel = new MenuModel();
		this.tablePopupMenuModel = new MenuModel();
		this.customTablePopupMenuModel = new MenuModel();
		this.hasDefaultMenus = bluePrint.hasDefaultMenus();
		this.hasDefaultCreatorAction = bluePrint.hasDefaultCreatorAction();
		this.hasDefaultDeleterAction = bluePrint.hasDefaultDeleterAction();
		this.headerMenuInterceptor = bluePrint.getHeaderMenuInterceptor();
		this.cellMenuInterceptor = bluePrint.getCellMenuInterceptor();

		table.setPopupMenu(tablePopupMenuModel);

		this.searchFilterToolbar = new BeanTableSearchFilterToolbar<BEAN_TYPE>(composite, this);
		this.statusBar = new BeanTableStatusBar<BEAN_TYPE>(composite, this);

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
			tablePopupMenuModel.addItem(menuFactory.filterMenu(this));
			tablePopupMenuModel.addAction(menuFactory.settingsAction(this));
			tablePopupMenuModel.addItem(getStatusBarItemModel());

			final ICapActionFactory actionFactory = CapUiToolkit.actionFactory();
			if (hasDefaultCreatorAction && model.getCreatorService() != null) {
				this.creatorAction = createCreatorAction(this);
				if (hasDefaultMenus) {
					tablePopupMenuModel.addSeparator();
				}
				tablePopupMenuModel.addAction(creatorAction);
			}
			if (hasDefaultDeleterAction && model.getDeleterService() != null) {
				this.deleteAction = createDeleteAction(this);
				if (hasDefaultMenus && !hasDefaultCreatorAction) {
					tablePopupMenuModel.addSeparator();
				}
				final IDeleterActionBuilder<BEAN_TYPE> deleterActionBuilder = actionFactory.deleterActionBuilder(model);
				deleterActionBuilder.setDeleterService(model.getDeleterService());
				tablePopupMenuModel.addAction(deleteAction);
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

		final IKeyListener keyListener = new KeyAdapter() {
			@Override
			public void keyReleased(final IKeyEvent event) {
				if (event.getModifier().contains(Modifier.CTRL) && event.getVirtualKey() == VirtualKey.F) {
					setSearchFilterToolbarVisible(true);
					searchFilterToolbar.requestSearchFocus();
				}
				else if (creatorAction != null
					&& event.getModifier().contains(Modifier.CTRL)
					&& event.getVirtualKey() == VirtualKey.N) {
					executeAction(creatorAction);
				}
				else if (deleteAction != null && event.getVirtualKey() == VirtualKey.DELETE) {
					executeAction(deleteAction);
				}
			}
		};

		getWidget().addKeyListener(keyListener);
		table.addKeyListener(keyListener);

		customTablePopupMenuModel.addListModelListener(new ListModelAdapter() {

			@Override
			public void beforeChildRemove(final int index) {
				final IMenuItemModel item = customTablePopupMenuModel.getChildren().get(index);
				tablePopupMenuModel.removeItem(item);
				if (customTablePopupMenuModel.getChildren().size() == 1 && tablePopupMenuModel.getChildren().size() > 0) {
					tablePopupMenuModel.removeItem(tablePopupMenuModel.getChildren().size() - 1);
				}
			}

			@Override
			public void afterChildAdded(final int index) {
				if (customTablePopupMenuModel.getChildren().size() == 1 && tablePopupMenuModel.getChildren().size() > 0) {
					tablePopupMenuModel.addSeparator();
				}
				tablePopupMenuModel.addItem(customTablePopupMenuModel.getChildren().get(index));
			}
		});

		setSearchFilterToolbarVisible(bluePrint.isSearchFilterToolbarVisible());
		setStatusBarVisible(true);
	}

	private static <BEAN_TYPE> IAction createCreatorAction(final IBeanTable<BEAN_TYPE> table) {
		final IBeanTableModel<BEAN_TYPE> model = table.getModel();
		final IBeanListModel<BEAN_TYPE> wrappedModel = new BeanListModelWrapper<BEAN_TYPE>(model) {
			@Override
			public void addBean(final IBeanProxy<BEAN_TYPE> bean) {
				super.addBean(bean);
				model.setSelection(Collections.singletonList(Integer.valueOf(model.getSize() - 1)));
				table.showSelection();
			}
		};
		final ICapActionFactory actionFactory = CapUiToolkit.actionFactory();
		final ICreatorActionBuilder builder = actionFactory.creatorActionBuilder(model.getBeanType(), wrappedModel);
		builder.setCreatorService(model.getCreatorService());
		builder.setBeanForm(model.getAttributes(AcceptEditableAttributesFilter.getInstance()));
		builder.addExecutionInterceptor(new IExecutionInterceptor() {
			@Override
			public boolean beforeExecution(final IExecutionContext executionContext) {
				final int pageCount = model.getPageCount();
				if (pageCount > 0 && !model.isPageLoaded(pageCount - 1)) {
					model.loadPage(pageCount - 1);
				}
				return true;
			}

			@Override
			public void afterExecution(final IExecutionContext executionContext) {}
		});
		return builder.build();
	}

	private static <BEAN_TYPE> IAction createDeleteAction(final IBeanTable<BEAN_TYPE> table) {
		final IBeanTableModel<BEAN_TYPE> model = table.getModel();
		final IBeanListModel<BEAN_TYPE> wrappedModel = new BeanListModelWrapper<BEAN_TYPE>(model) {

		};
		final ICapActionFactory actionFactory = CapUiToolkit.actionFactory();
		final IDeleterActionBuilder<BEAN_TYPE> builder = actionFactory.deleterActionBuilder(wrappedModel);
		builder.setDeleterService(model.getDeleterService());
		return builder.build();
	}

	private void executeAction(final IAction action) {
		final IExecutionContext executionContext = getExecutionContext(action);
		try {
			if (action.isEnabled()) {
				action.execute(executionContext);
			}
		}
		catch (final Exception e) {
			try {
				action.getExceptionHandler().handleException(executionContext, e);
			}
			catch (final Exception e1) {
				final UncaughtExceptionHandler uncaughtExceptionHandler = Thread.currentThread().getUncaughtExceptionHandler();
				if (uncaughtExceptionHandler != null) {
					uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), e1);
				}
			}
		}
	}

	private IExecutionContext getExecutionContext(final IAction action) {
		return new IExecutionContext() {
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
				return action;
			}
		};
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
		final IPopupMenu popupMenu = table.createPopupMenu();
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
		final IPopupMenu popupMenu = table.createPopupMenu();
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

		if (hasDefaultCreatorAction && model.getCreatorService() != null) {
			if (hasDefaultMenus) {
				menuModel.addSeparator();
			}
			menuModel.addAction(creatorAction);
		}
		if (hasDefaultDeleterAction && model.getDeleterService() != null) {
			if (hasDefaultMenus && !hasDefaultCreatorAction) {
				menuModel.addSeparator();
			}
			menuModel.addAction(deleteAction);
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
	public void setSearchFilterToolbarVisible(final boolean visible) {
		searchFilterToolbar.setVisible(visible);
	}

	@Override
	public void setStatusBarVisible(final boolean visible) {
		statusBar.setVisible(visible);
	}

	@Override
	public ICheckedItemModel getSearchFilterItemModel() {
		return searchFilterToolbar.getSearchFilterItemModel();
	}

	@Override
	public ICheckedItemModel getStatusBarItemModel() {
		return statusBar.getStatusBarItemModel();
	}

	@Override
	public IMenuModel getTablePopupMenu() {
		return customTablePopupMenuModel;
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

	@Override
	public void resetFromModel() {
		table.resetFromModel();
	}

	@Override
	public Position getCellPosition(final int rowIndex, final int columnIndex) {
		return table.getCellPosition(rowIndex, columnIndex);
	}

	@Override
	public Dimension getCellSize(final int rowIndex, final int columnIndex) {
		return table.getCellSize(rowIndex, columnIndex);
	}

	@Override
	public ArrayList<Integer> getColumnPermutation() {
		return table.getColumnPermutation();
	}

	@Override
	public void setColumnPermutation(final List<Integer> permutation) {
		table.setColumnPermutation(permutation);
	}

	@Override
	public ArrayList<Integer> getSelection() {
		return table.getSelection();
	}

	@Override
	public void setSelection(final List<Integer> selection) {
		table.setSelection(selection);
	}

	@Override
	public void showSelection() {
		table.showSelection();
	}

	@Override
	public void pack(final TablePackPolicy policy) {
		table.pack(policy);
	}

	@Override
	public void pack(final int columnIndex, final TablePackPolicy policy) {
		table.pack(columnIndex, policy);
	}

	@Override
	public void addTableCellPopupDetectionListener(final ITableCellPopupDetectionListener listener) {
		table.addTableCellPopupDetectionListener(listener);
	}

	@Override
	public void removeTableCellPopupDetectionListener(final ITableCellPopupDetectionListener listener) {
		table.removeTableCellPopupDetectionListener(listener);
	}

	@Override
	public void addTableColumnPopupDetectionListener(final ITableColumnPopupDetectionListener listener) {
		table.addTableColumnPopupDetectionListener(listener);
	}

	@Override
	public void removeTableColumnPopupDetectionListener(final ITableColumnPopupDetectionListener listener) {
		table.removeTableColumnPopupDetectionListener(listener);
	}

	@Override
	public void addTableCellEditorListener(final ITableCellEditorListener listener) {
		table.addTableCellEditorListener(listener);
	}

	@Override
	public void removeTableCellEditorListener(final ITableCellEditorListener listener) {
		table.removeTableCellEditorListener(listener);
	}

	@Override
	public void addTableSelectionListener(final ITableSelectionListener listener) {
		table.addTableSelectionListener(listener);
	}

	@Override
	public void removeTableSelectionListener(final ITableSelectionListener listener) {
		table.removeTableSelectionListener(listener);
	}

	@Override
	public void addTableCellListener(final ITableCellListener listener) {
		table.addTableCellListener(listener);
	}

	@Override
	public void removeTableCellListener(final ITableCellListener listener) {
		table.removeTableCellListener(listener);
	}

	@Override
	public void pack() {
		table.pack();
	}

	@Override
	public void pack(final int columnIndex) {
		table.pack(columnIndex);
	}

	@Override
	public int getRowCount() {
		return table.getRowCount();
	}

	@Override
	public int getColumnCount() {
		return table.getColumnCount();
	}

	@Override
	public int convertColumnIndexToView(final int modelIndex) {
		return table.convertColumnIndexToView(modelIndex);
	}

	@Override
	public int convertColumnIndexToModel(final int viewIndex) {
		return table.convertColumnIndexToModel(viewIndex);
	}

	@Override
	public void moveColumn(final int oldViewIndex, final int newViewIndex) {
		table.moveColumn(oldViewIndex, newViewIndex);
	}

	@Override
	public void resetColumnPermutation() {
		table.resetColumnPermutation();
	}

	@Override
	public void addTableColumnListener(final ITableColumnListener listener) {
		table.addTableColumnListener(listener);
	}

	@Override
	public void removeTableColumnListener(final ITableColumnListener listener) {
		table.removeTableColumnListener(listener);
	}

}
