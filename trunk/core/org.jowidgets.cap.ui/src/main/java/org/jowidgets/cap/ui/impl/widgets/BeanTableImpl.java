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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.convert.IStringObjectConverter;
import org.jowidgets.api.model.item.ICheckedItemModel;
import org.jowidgets.api.model.item.ICheckedItemModelBuilder;
import org.jowidgets.api.model.item.IItemModelFactory;
import org.jowidgets.api.model.item.IMenuItemModel;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.api.model.item.ISeparatorItemModel;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IPopupMenu;
import org.jowidgets.api.widgets.ITable;
import org.jowidgets.api.widgets.IWidget;
import org.jowidgets.api.widgets.blueprint.ITableBluePrint;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.tools.execution.ResultCallbackAdapter;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.filter.FilterType;
import org.jowidgets.cap.ui.api.plugin.IBeanTableMenuContributionPlugin;
import org.jowidgets.cap.ui.api.plugin.IBeanTableMenuInterceptorPlugin;
import org.jowidgets.cap.ui.api.plugin.IBeanTablePlugin;
import org.jowidgets.cap.ui.api.sort.ISortModel;
import org.jowidgets.cap.ui.api.table.BeanTableConfig;
import org.jowidgets.cap.ui.api.table.BeanTableSettings;
import org.jowidgets.cap.ui.api.table.IBeanTableConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableConfigBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuInterceptor;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.IBeanTableSettings;
import org.jowidgets.cap.ui.api.table.IBeanTableSettingsBuilder;
import org.jowidgets.cap.ui.api.types.AutoScrollPolicy;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialog;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.cap.ui.api.widgets.IPopupMenuListener;
import org.jowidgets.cap.ui.api.widgets.ITableMenuCreationInterceptor;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.common.types.IVetoable;
import org.jowidgets.common.types.Interval;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.common.types.Position;
import org.jowidgets.common.types.TablePackPolicy;
import org.jowidgets.common.types.VirtualKey;
import org.jowidgets.common.widgets.controller.IItemStateListener;
import org.jowidgets.common.widgets.controller.IKeyEvent;
import org.jowidgets.common.widgets.controller.IKeyListener;
import org.jowidgets.common.widgets.controller.IPopupDetectionListener;
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
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProperties;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.plugin.api.PluginToolkit;
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
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.ITypedKey;
import org.jowidgets.util.concurrent.DaemonThreadFactory;

final class BeanTableImpl<BEAN_TYPE> extends CompositeWrapper implements IBeanTable<BEAN_TYPE> {

	private static final IMessage NO_SERVICE_AVAILABLE_ENTITY = Messages.getMessage("BeanTableImpl.noServiceAvailableEntityLabel");
	private static final IMessage NO_SERVICE_AVAILABLE = Messages.getMessage("BeanTableImpl.noServiceAvailable");

	private final ITable table;
	private final IBeanTableModel<BEAN_TYPE> model;
	private final BeanTableSearchFilterToolbar<BEAN_TYPE> searchFilterToolbar;
	private final BeanTableFilterToolbar<BEAN_TYPE> filterToolbar;
	private final BeanTableStatusBar<BEAN_TYPE> statusBar;
	private final List<IBeanTableMenuInterceptor<BEAN_TYPE>> menuInterceptors;
	private final IMenuModel headerPopupMenuModel;
	private final IMenuModel pluggedHeaderPopupMenuModel;
	private final IMenuModel cellPopupMenuModel;
	private final IMenuModel pluggedCellPopupMenuModel;
	private final IMenuModel customTablePopupMenuModel;
	private final IMenuModel tablePopupMenuModel;
	private final IMenuModel pluggedTablePopupMenuModell;
	private final Map<Integer, IPopupMenu> headerPopupMenus;
	private final Map<Integer, IPopupMenu> cellPopupMenus;
	private final ITableMenuCreationInterceptor<BEAN_TYPE> headerMenuInterceptor;
	private final ITableMenuCreationInterceptor<BEAN_TYPE> cellMenuInterceptor;
	private final boolean hasDefaultMenus;
	private final boolean hasDefaultCreatorAction;
	private final boolean hasDefaultDeleterAction;
	private final IBeanTableMenuFactory<BEAN_TYPE> menuFactory;
	private final PopupMenuObservable<Position> tableMenuObservable;
	private final PopupMenuObservable<ITableColumnPopupEvent> headerMenuObservable;
	private final PopupMenuObservable<ITableCellPopupEvent> cellMenuObservable;
	private final ScheduledExecutorService autoUpdateExecutorService;
	private final AutoUpdateRunnable autoUpdateRunnable;
	private final boolean isAutoUpdateConfigurable;
	private final ICheckedItemModel autoUpdateItemModel;

	private IAction creatorAction;
	private IAction deleteAction;
	private IBeanTableSettingsDialog settingsDialog;
	private ITableCellPopupEvent currentCellEvent;
	private ITableColumnPopupEvent currentColumnEvent;
	private int currentAutoUpdateInterval;
	private AutoScrollPolicy autoScrollPolicy;
	private ScheduledFuture<?> autoUpdateFuture;

	BeanTableImpl(final IComposite composite, final IBeanTableBluePrint<BEAN_TYPE> bluePrint) {
		super(composite);

		final Object entityId = bluePrint.getModel().getEntityId();
		if (entityId != null) {
			modifyBeanTableBpByPlugins(entityId, bluePrint);
		}

		this.model = bluePrint.getModel();

		final IComposite mainComposite;
		if (model.getReaderService() == null) {
			composite.setLayout(new MigLayoutDescriptor("hidemode 3", "0[grow, 0::]0", "0[grow, 0::]0"));
			mainComposite = composite.add(BPF.composite(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
			mainComposite.setVisible(false);
			final String entityLabelPlural = model.getEntityLabelPlural();
			final String label;
			if (EmptyCheck.isEmpty(entityLabelPlural)) {
				label = NO_SERVICE_AVAILABLE.get();
			}
			else {
				label = MessageReplacer.replace(NO_SERVICE_AVAILABLE_ENTITY.get(), entityLabelPlural);
			}
			composite.add(BPF.textLabel(label).alignCenter(), "alignx c, aligny c");
		}
		else {
			mainComposite = composite;
		}

		mainComposite.setLayout(new MigLayoutDescriptor("hidemode 2", "0[grow, 0::]0", "0[]0[grow, 0::]0[]0"));

		this.menuInterceptors = getMenuInterceptorsFromPlugins(
				bluePrint.getMenuInterceptor(),
				model.getEntityId(),
				model.getBeanType());
		this.menuFactory = CapUiToolkit.beanTableMenuFactory(menuInterceptors);
		this.autoUpdateItemModel = createAutoUpdateItemModel();

		final ITableBluePrint tableBp = BPF.table(model.getTableModel());
		tableBp.setSetup(bluePrint);

		final IComposite contentComposite = mainComposite.add(BPF.composite(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS
			+ ", wrap");
		contentComposite.setLayout(new MigLayoutDescriptor("hidemode 2", "0[]0[grow, 0::]0", "0[grow, 0::]0"));

		this.table = contentComposite.add(tableBp, MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

		this.tableMenuObservable = new PopupMenuObservable<Position>();
		this.headerMenuObservable = new PopupMenuObservable<ITableColumnPopupEvent>();
		this.cellMenuObservable = new PopupMenuObservable<ITableCellPopupEvent>();

		this.headerPopupMenus = new HashMap<Integer, IPopupMenu>();
		this.cellPopupMenus = new HashMap<Integer, IPopupMenu>();
		this.cellPopupMenuModel = new MenuModel();
		this.pluggedCellPopupMenuModel = new MenuModel();
		this.headerPopupMenuModel = new MenuModel();
		this.pluggedHeaderPopupMenuModel = new MenuModel();
		this.tablePopupMenuModel = new MenuModel();
		this.customTablePopupMenuModel = new MenuModel();
		this.pluggedTablePopupMenuModell = new MenuModel();
		this.hasDefaultMenus = bluePrint.hasDefaultMenus();
		this.hasDefaultCreatorAction = bluePrint.hasDefaultCreatorAction();
		this.hasDefaultDeleterAction = bluePrint.hasDefaultDeleterAction();
		this.headerMenuInterceptor = bluePrint.getHeaderMenuInterceptor();
		this.cellMenuInterceptor = bluePrint.getCellMenuInterceptor();

		addMenusFromPlugins(
				model.getBeanType(),
				model.getEntityId(),
				pluggedTablePopupMenuModell,
				pluggedCellPopupMenuModel,
				pluggedHeaderPopupMenuModel);

		this.searchFilterToolbar = new BeanTableSearchFilterToolbar<BEAN_TYPE>(mainComposite, this);
		this.filterToolbar = new BeanTableFilterToolbar<BEAN_TYPE>(contentComposite, this, menuFactory);
		this.statusBar = new BeanTableStatusBar<BEAN_TYPE>(mainComposite, this);

		headerPopupMenuModel.addListModelListener(new CustomMenuModelListener());
		cellPopupMenuModel.addListModelListener(new CustomMenuModelListener());

		if (bluePrint.hasDefaultMenus()) {

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
			if (bluePrint.getAutoUpdateConfigurable()) {
				tablePopupMenuModel.addItem(getAutoUpdateItemModel());
			}
			tablePopupMenuModel.addItem(getStatusBarItemModel());

			if (hasDefaultCreatorAction && model.getCreatorService() != null) {
				this.creatorAction = menuFactory.creatorAction(this);
				if (creatorAction != null) {
					if (hasDefaultMenus) {
						tablePopupMenuModel.addSeparator();
					}
					tablePopupMenuModel.addAction(creatorAction);
				}
			}
			if (hasDefaultDeleterAction && model.getDeleterService() != null) {
				this.deleteAction = menuFactory.deleterAction(this);
				if (deleteAction != null) {
					if (hasDefaultMenus && !hasDefaultCreatorAction) {
						tablePopupMenuModel.addSeparator();
					}
					tablePopupMenuModel.addAction(deleteAction);
				}
			}

			addMenuModel(tablePopupMenuModel, pluggedTablePopupMenuModell);

			final IPopupMenu tablePopupMenu = table.createPopupMenu();
			tablePopupMenu.setModel(tablePopupMenuModel);
			table.addPopupDetectionListener(new IPopupDetectionListener() {
				@Override
				public void popupDetected(final Position position) {
					tableMenuObservable.fireBeforeMenuShow(position);
					if (tablePopupMenu.getChildren().size() > 0) {
						tablePopupMenu.show(position);
					}
				}
			});
		}

		table.addTableCellPopupDetectionListener(new ITableCellPopupDetectionListener() {
			@Override
			public void popupDetected(final ITableCellPopupEvent event) {
				final IPopupMenu cellPopupMenu = getCellPopupMenu(event.getColumnIndex());
				cellMenuObservable.fireBeforeMenuShow(event);
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
				headerMenuObservable.fireBeforeMenuShow(event);
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
			public void keyPressed(final IKeyEvent event) {
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

		setSearchFilterToolbarVisible(bluePrint.getSearchFilterToolbarVisible());
		setStatusBarVisible(true);

		this.isAutoUpdateConfigurable = bluePrint.getAutoUpdateConfigurable();
		this.currentAutoUpdateInterval = bluePrint.getAutoUpdateInterval();
		this.autoScrollPolicy = bluePrint.getAutoScrollPolicy();
		this.autoUpdateExecutorService = Executors.newScheduledThreadPool(1, new DaemonThreadFactory());
		this.autoUpdateRunnable = new AutoUpdateRunnable();
	}

	private void modifyBeanTableBpByPlugins(final Object entityId, final IBeanTableBluePrint<BEAN_TYPE> beanTableBp) {
		final IPluginProperties properties = PluginProperties.create(IBeanTablePlugin.ENTITIY_ID_PROPERTY_KEY, entityId);
		for (final IBeanTablePlugin plugin : PluginProvider.getPlugins(IBeanTablePlugin.ID, properties)) {
			plugin.modifySetup(properties, beanTableBp);
		}
	}

	private ICheckedItemModel createAutoUpdateItemModel() {
		final IItemModelFactory modelFactory = Toolkit.getModelFactoryProvider().getItemModelFactory();
		final ICheckedItemModelBuilder builder = modelFactory.checkedItemBuilder();
		final String text = Messages.getString("BeanTableImpl.auto_update_text");
		final String tooltip = Messages.getString("BeanTableImpl.auto_update_tooltip");
		builder.setText(text).setToolTipText(tooltip);
		final ICheckedItemModel result = builder.build();
		result.addItemListener(new IItemStateListener() {
			@Override
			public void itemStateChanged() {
				if (result.isSelected()) {
					startAutoUpdateModeImpl();
				}
				else {
					stopAutoUpdateModeImpl();
				}
			}
		});
		return result;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void addMenusFromPlugins(
		final Class<BEAN_TYPE> beanType,
		final Object entityId,
		final IMenuModel menuModel,
		final IMenuModel cellMenuModel,
		final IMenuModel headerMenuModel) {

		final IPluginPropertiesBuilder propBuilder = PluginToolkit.pluginPropertiesBuilder();
		propBuilder.add(IBeanTableMenuContributionPlugin.ENTITIY_ID_PROPERTY_KEY, entityId);
		propBuilder.add(IBeanTableMenuContributionPlugin.BEAN_TYPE_PROPERTY_KEY, beanType);
		final IPluginProperties properties = propBuilder.build();

		final List<IBeanTableMenuContributionPlugin<?>> plugins = PluginProvider.getPlugins(
				IBeanTableMenuContributionPlugin.ID,
				properties);

		if (plugins != null) {
			for (final IBeanTableMenuContributionPlugin plugin : plugins) {
				final IMenuModel menu = plugin.getTableMenu(properties, this);
				if (menu != null) {
					menuModel.addItemsOfModel(menu);
				}
				final IMenuModel cellMenu = plugin.getCellMenu(properties, this);
				if (menu != null) {
					cellMenuModel.addItemsOfModel(cellMenu);
				}
				final IMenuModel headerMenu = plugin.getHeaderMenu(properties, this);
				if (menu != null) {
					headerMenuModel.addItemsOfModel(headerMenu);
				}
			}
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private List<IBeanTableMenuInterceptor<BEAN_TYPE>> getMenuInterceptorsFromPlugins(
		final IBeanTableMenuInterceptor<BEAN_TYPE> initialInterceptor,
		final Object entityId,
		final Class<BEAN_TYPE> beanType) {

		final List<IBeanTableMenuInterceptor<BEAN_TYPE>> result = new LinkedList<IBeanTableMenuInterceptor<BEAN_TYPE>>();

		if (initialInterceptor != null) {
			result.add(initialInterceptor);
		}

		final IPluginPropertiesBuilder propBuilder = PluginToolkit.pluginPropertiesBuilder();
		propBuilder.add(IBeanTableMenuInterceptorPlugin.ENTITIY_ID_PROPERTY_KEY, entityId);
		propBuilder.add(IBeanTableMenuInterceptorPlugin.BEAN_TYPE_PROPERTY_KEY, beanType);
		final IPluginProperties properties = propBuilder.build();

		final List<IBeanTableMenuInterceptorPlugin<?>> plugins = PluginProvider.getPlugins(
				IBeanTableMenuInterceptorPlugin.ID,
				properties);
		for (final IBeanTableMenuInterceptorPlugin plugin : plugins) {
			final IBeanTableMenuInterceptor<?> interceptor = plugin.getMenuInterceptor(properties, this);
			if (interceptor != null) {
				result.add((IBeanTableMenuInterceptor<BEAN_TYPE>) interceptor);
			}
		}

		return result;
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

		IMenuModel menuModel;
		if (hasDefaultMenus) {
			final IMenuModel headerPopupMenu = menuFactory.headerPopupMenu(this, index.intValue());
			if (headerPopupMenu != null) {
				menuModel = headerPopupMenu;
			}
			else {
				menuModel = new MenuModel();
			}
		}
		else {
			menuModel = new MenuModel();
		}

		if (headerMenuInterceptor != null) {
			headerMenuInterceptor.afterMenuCreated(this, menuModel, index.intValue());
		}

		addMenuModel(menuModel, headerPopupMenuModel);
		addMenuModel(menuModel, pluggedHeaderPopupMenuModel);

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

		IMenuModel menuModel;
		if (hasDefaultMenus) {
			final IMenuModel headerPopupMenuModelUndecorated;
			if (!isColumnPopupDetectionSupported()) {
				headerPopupMenuModelUndecorated = createHeaderPopupMenuModelUndecorated(index);
			}
			else {
				headerPopupMenuModelUndecorated = null;
			}
			final IMenuModel cellPopupMenu = menuFactory.cellPopupMenu(this, headerPopupMenuModelUndecorated, index.intValue());
			if (cellPopupMenu != null) {
				menuModel = cellPopupMenu;
			}
			else {
				menuModel = new MenuModel();
			}
		}
		else {
			menuModel = new MenuModel();
		}

		if (creatorAction != null) {
			if (menuModel.getChildren().size() > 0) {
				menuModel.addSeparator();
			}
			menuModel.addAction(creatorAction);
		}
		if (deleteAction != null) {
			if (menuModel.getChildren().size() > 0 && creatorAction == null) {
				menuModel.addSeparator();
			}
			menuModel.addAction(deleteAction);
		}

		if (cellMenuInterceptor != null) {
			cellMenuInterceptor.afterMenuCreated(this, menuModel, index.intValue());
		}

		addMenuModel(menuModel, cellPopupMenuModel);
		addMenuModel(menuModel, pluggedCellPopupMenuModel);

		menuModel.addDecorator(createDecorator(false));
		return menuModel;
	}

	private void addMenuModel(final IMenuModel model, final IMenuModel modelToAdd) {
		if (modelToAdd.getChildren().size() > 0) {
			if (model.getChildren().size() > 0 && !endsWithSeparator(model) && !startsWithSeparator(modelToAdd)) {
				model.addSeparator();
			}
			for (final IMenuItemModel itemModel : modelToAdd.getChildren()) {
				model.addItem(itemModel);
			}
		}
	}

	private boolean startsWithSeparator(final IMenuModel model) {
		final int size = model.getChildren().size();
		if (size > 0) {
			final IMenuItemModel firstItem = model.getChildren().get(0);
			return firstItem instanceof ISeparatorItemModel;
		}
		else {
			return false;
		}
	}

	private boolean endsWithSeparator(final IMenuModel model) {
		final int size = model.getChildren().size();
		if (size > 0) {
			final IMenuItemModel lastItem = model.getChildren().get(size - 1);
			return lastItem instanceof ISeparatorItemModel;
		}
		else {
			return false;
		}
	}

	@Override
	public void setAutoUpdateInterval(final int updateIntervall) {
		if (updateIntervall != currentAutoUpdateInterval) {
			if (autoUpdateFuture != null && !autoUpdateFuture.isCancelled()) {
				startAutoUpdateMode(updateIntervall);
			}
			else {
				currentAutoUpdateInterval = updateIntervall;
			}
		}
	}

	@Override
	public long getAutoUpdateInterval() {
		return currentAutoUpdateInterval;
	}

	@Override
	public boolean isAutoUpdateConfigurable() {
		return isAutoUpdateConfigurable;
	}

	@Override
	public void startAutoUpdateMode() {
		autoUpdateItemModel.setSelected(true);
	}

	@Override
	public void stopAutoUpdateMode() {
		autoUpdateItemModel.setSelected(false);
	}

	private void startAutoUpdateModeImpl() {
		startAutoUpdateMode(currentAutoUpdateInterval);
	}

	private void startAutoUpdateMode(final int updateIntervall) {
		if (currentAutoUpdateInterval != updateIntervall || autoUpdateFuture == null || autoUpdateFuture.isCancelled()) {
			currentAutoUpdateInterval = updateIntervall;
			if (autoUpdateFuture != null && !autoUpdateFuture.isCancelled()) {
				autoUpdateFuture.cancel(false);
			}
			autoUpdateFuture = autoUpdateExecutorService.scheduleAtFixedRate(
					autoUpdateRunnable,
					currentAutoUpdateInterval,
					currentAutoUpdateInterval,
					TimeUnit.SECONDS);
		}
	}

	private void stopAutoUpdateModeImpl() {
		if (autoUpdateFuture != null && !autoUpdateFuture.isCancelled()) {
			autoUpdateFuture.cancel(false);
			autoUpdateFuture = null;
		}
	}

	@Override
	public IBeanTableModel<BEAN_TYPE> getModel() {
		return model;
	}

	@Override
	public IAction getDefaultCreatorAction() {
		return creatorAction;
	}

	@Override
	public IAction getDefaultDeleterAction() {
		return deleteAction;
	}

	@Override
	public void showSettingsDialog() {
		final IBeanTableSettingsDialog dialog = getSettingsDialog();
		final IBeanTableSettings settings = dialog.show();
		if (dialog.isOkPressed()) {
			setSettings(settings);
		}
	}

	@Override
	public void setSearchFilterToolbarVisible(final boolean visible) {
		searchFilterToolbar.setVisible(visible);
	}

	@Override
	public void setSearchFilterToolbarText(final String text) {
		searchFilterToolbar.setSearchFilterToolbarText(text);
	}

	@Override
	public void setFilterToolbarVisible(final boolean visible) {
		filterToolbar.setVisible(visible);
	}

	@Override
	public void setStatusBarVisible(final boolean visible) {
		statusBar.setVisible(visible);
	}

	@Override
	public ICheckedItemModel getSearchFilterToolbarItemModel() {
		return searchFilterToolbar.getItemModel();
	}

	@Override
	public ICheckedItemModel getFilterToolbarItemModel() {
		return filterToolbar.getItemModel();
	}

	@Override
	public ICheckedItemModel getStatusBarItemModel() {
		return statusBar.getStatusBarItemModel();
	}

	@Override
	public ICheckedItemModel getAutoUpdateItemModel() {
		return autoUpdateItemModel;
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

	@Override
	public void addTableMenuListener(final IPopupMenuListener<Position> listener) {
		tableMenuObservable.addPopupMenuListener(listener);
	}

	@Override
	public void removeTableMenuListener(final IPopupMenuListener<Position> listener) {
		tableMenuObservable.removePopupMenuListener(listener);
	}

	@Override
	public void addHeaderMenuListener(final IPopupMenuListener<ITableColumnPopupEvent> listener) {
		headerMenuObservable.addPopupMenuListener(listener);
	}

	@Override
	public void removeHeaderMenuListener(final IPopupMenuListener<ITableColumnPopupEvent> listener) {
		headerMenuObservable.removePopupMenuListener(listener);
	}

	@Override
	public void addCellMenuListener(final IPopupMenuListener<ITableCellPopupEvent> listener) {
		cellMenuObservable.addPopupMenuListener(listener);
	}

	@Override
	public void removeCellMenuListener(final IPopupMenuListener<ITableCellPopupEvent> listener) {
		cellMenuObservable.removePopupMenuListener(listener);
	}

	private IBeanTableSettingsDialog getSettingsDialog() {
		if (settingsDialog == null) {
			final ICapApiBluePrintFactory bpf = CapUiToolkit.bluePrintFactory();
			settingsDialog = Toolkit.getActiveWindow().createChildWindow(bpf.beanTableSettingsDialog(this));
		}
		return settingsDialog;
	}

	@Override
	public IBeanTableSettings getSettings() {
		final IBeanTableSettingsBuilder builder = BeanTableSettings.builder();
		builder.setModelConfig(model.getConfig());
		builder.setAutoUpdate(autoUpdateItemModel.isSelected());
		builder.setAutoUpdateInterval(currentAutoUpdateInterval);
		builder.setAutoScrollPolicy(autoScrollPolicy);
		return builder.build();
	}

	@Override
	public void setSettings(final IBeanTableSettings settings) {
		Assert.paramNotNull(settings, "settings");
		model.setConfig(settings.getModelConfig());
		currentAutoUpdateInterval = settings.getAutoUpdateInterval();
		autoScrollPolicy = settings.getAutoScrollPolicy();
		autoUpdateItemModel.setSelected(settings.isAutoUpdate());
	}

	@Override
	public IBeanTableConfig getConfig() {
		final IBeanTableConfigBuilder builder = BeanTableConfig.builder();
		builder.setColumnPermutation(getColumnPermutation());
		builder.setAutoUpdate(autoUpdateItemModel.isSelected());
		builder.setAutoUpdateInterval(currentAutoUpdateInterval);
		builder.setAutoScrollPolicy(autoScrollPolicy);
		builder.setFilterToolbarVisible(getFilterToolbarItemModel().isSelected());
		builder.setSearchFilterToolbarVisible(getSearchFilterToolbarItemModel().isSelected());
		builder.setStatusBarVisible(getStatusBarItemModel().isSelected());
		return builder.build();
	}

	@Override
	public void setConfig(final IBeanTableConfig config) {
		Assert.paramNotNull(config, "config");
		setColumnPermutation(config.getColumnPermutation());
		setFilterToolbarVisible(config.isFilterToolbarVisible());
		setSearchFilterToolbarVisible(config.isSearchFilterToolbarVisible());
		setStatusBarVisible(config.isStatusBarVisible());
		autoUpdateItemModel.setSelected(config.isAutoUpdate());
		autoScrollPolicy = config.getAutoScrollPolicy();
		setAutoUpdateInterval(config.getAutoUpdateInterval());
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

	private final class AutoUpdateRunnable implements Runnable {

		private final IUiThreadAccess uiThreadAccess;

		private AutoUpdateRunnable() {
			this.uiThreadAccess = Toolkit.getUiThreadAccess();
		}

		@Override
		public void run() {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					final IResultCallback<Void> resultCallback = new ResultCallbackAdapter<Void>() {
						@Override
						public void finished(final Void result) {
							if (AutoScrollPolicy.TO_SELECTION == autoScrollPolicy) {
								table.scrollToSelection();
							}
							else if (AutoScrollPolicy.TO_END == autoScrollPolicy) {
								table.scrollToEnd();
							}
						}
					};
					model.updateInBackground(resultCallback, getVisibleRows());
				}
			});
		}
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
				final IAction filterAction = menuFactory.addFilterAction(model, FilterType.ARITHMETIC_FILTER, modelColumn);
				if (filterAction != null) {
					executeAction(filterAction);
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
	public void setEditable(final boolean editable) {
		table.setEditable(editable);
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
	public void scrollToSelection() {
		table.scrollToSelection();
	}

	@Override
	public void scrollToEnd() {
		table.scrollToEnd();
	}

	@Override
	public void scrollToRow(final int rowIndex) {
		table.scrollToRow(rowIndex);
	}

	@Override
	public boolean isColumnPopupDetectionSupported() {
		return table.isColumnPopupDetectionSupported();
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
	public void pack(final TablePackPolicy policy) {
		table.pack(policy);
	}

	@Override
	public void pack(final int columnIndex, final TablePackPolicy policy) {
		table.pack(columnIndex, policy);
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

	@Override
	public Interval<Integer> getVisibleRows() {
		return table.getVisibleRows();
	}
}
