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

package org.jowidgets.cap.ui.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jowidgets.api.color.Colors;
import org.jowidgets.api.convert.IObjectLabelConverter;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.model.table.IDefaultTableColumn;
import org.jowidgets.api.model.table.IDefaultTableColumnBuilder;
import org.jowidgets.api.model.table.IDefaultTableColumnModel;
import org.jowidgets.api.model.table.ITableCellBuilder;
import org.jowidgets.api.model.table.ITableModel;
import org.jowidgets.api.model.table.ITableModelFactory;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.bean.IBeanModification;
import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.filter.BooleanOperator;
import org.jowidgets.cap.common.api.filter.IBooleanFilterBuilder;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.lookup.ILookUpValueRange;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.common.api.sort.SortOrder;
import org.jowidgets.cap.common.tools.bean.BeanKey;
import org.jowidgets.cap.common.tools.execution.SyncResultCallback;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeBluePrint;
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifierBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeConfig;
import org.jowidgets.cap.ui.api.attribute.IAttributeModifier;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeansStateTracker;
import org.jowidgets.cap.ui.api.control.DisplayFormat;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.filter.IUiFilter;
import org.jowidgets.cap.ui.api.filter.IUiFilterFactory;
import org.jowidgets.cap.ui.api.filter.IUiFilterTools;
import org.jowidgets.cap.ui.api.lookup.ILookUpAccess;
import org.jowidgets.cap.ui.api.lookup.ILookUpListener;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.model.IModificationStateListener;
import org.jowidgets.cap.ui.api.model.IProcessStateListener;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.cap.ui.api.plugin.IBeanTableModelPlugin;
import org.jowidgets.cap.ui.api.sort.IPropertySort;
import org.jowidgets.cap.ui.api.sort.ISortModel;
import org.jowidgets.cap.ui.api.sort.ISortModelConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableConfigBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.IReaderParameterProvider;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.model.ITableCell;
import org.jowidgets.common.types.Markup;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.plugin.api.PluginToolkit;
import org.jowidgets.tools.controller.TableDataModelAdapter;
import org.jowidgets.tools.model.table.AbstractTableDataModel;
import org.jowidgets.tools.model.table.DefaultTableColumnBuilder;
import org.jowidgets.tools.model.table.TableCellBuilder;
import org.jowidgets.tools.model.table.TableModel;
import org.jowidgets.util.Assert;
import org.jowidgets.util.concurrent.DaemonThreadFactory;
import org.jowidgets.util.event.ChangeObservable;
import org.jowidgets.util.event.IChangeListener;

@SuppressWarnings("unused")
class BeanTableModelImpl<BEAN_TYPE> implements IBeanTableModel<BEAN_TYPE> {

	private static final int DEFAULT_PAGE_SIZE = 1000;
	private static final int VIEW_SIZE = 50;
	private static final int INNER_PAGE_DELAY = 100;
	private static final int MAX_PAGE_LOADER_COUNT = 2;
	private static final IDummyValue DUMMY_VALUE = new IDummyValue() {};

	private final Map<String, IUiFilter> filters;
	private final Map<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>> data;
	private final LinkedList<PageLoader> currentPageLoaders;

	private final IBeanProxyFactory<BEAN_TYPE> beanProxyFactory;
	private final IBeansStateTracker<BEAN_TYPE> beansStateTracker;
	private final ArrayList<IAttribute<Object>> attributes;
	private final List<String> propertyNames;
	private final IReaderService<Object> readerService;
	private final IReaderParameterProvider<Object> paramProvider;
	private final ISortModel sortModel;

	private final ICreatorService creatorService;
	private final IRefreshService refreshService;
	private final IUpdaterService updaterService;
	private final IDeleterService deleterService;

	private final IBeanListModel<?> parent;
	private final LinkType linkType;

	private final BeanListModelObservable beanListModelObservable;
	private final ChangeObservable filterChangeObservable;

	private final IDefaultTableColumnModel columnModel;
	private final AbstractTableDataModel dataModel;
	private final ITableModel tableModel;

	private final Set<ILookUpListener> lookUpListenersStrongRef;

	private int pageSize;
	private int rowCount;
	private int maxPageIndex;
	private int lastLoadingPage;
	private int lastRenderedRow;
	private boolean dataCleared;
	private boolean autoSelection;
	private boolean onSetConfig;

	@SuppressWarnings("unchecked")
	BeanTableModelImpl(
		final Object entityId,
		final Class<? extends BEAN_TYPE> beanType,
		final List<IAttribute<Object>> attributes,
		final ISortModelConfig sortModelConfig,
		final IReaderService<? extends Object> readerService,
		final IReaderParameterProvider<? extends Object> paramProvider,
		final ICreatorService creatorService,
		final IRefreshService refreshService,
		final IUpdaterService updaterService,
		final IDeleterService deleterService,
		final IBeanListModel<?> parent,
		final LinkType linkType) {

		//arguments checks
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(attributes, "attributes");
		Assert.paramNotNull(readerService, "readerService");
		Assert.paramNotNull(paramProvider, "paramProvider");

		this.parent = parent;
		this.linkType = linkType;
		if (parent != null) {
			Assert.paramNotNull(linkType, "linkType");
			parent.addBeanListModelListener(new IBeanListModelListener() {
				private ScheduledFuture<?> schedule;

				@Override
				public void selectionChanged() {
					loadTable();
				}

				@Override
				public void beansChanged() {
					loadTable();
				}

				private void loadTable() {
					clear();
					if (schedule != null) {
						schedule.cancel(false);
					}
					final IUiThreadAccess uiThreadAccess = Toolkit.getUiThreadAccess();
					final Runnable runnable = new Runnable() {
						@Override
						public void run() {
							uiThreadAccess.invokeLater(new Runnable() {
								@Override
								public void run() {
									schedule = null;
									load();
								}
							});
						}
					};
					schedule = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory()).schedule(
							runnable,
							100,
							TimeUnit.MILLISECONDS);
				}
			});
		}

		//inject table model plugins
		List<IAttribute<Object>> modifiedAttributes = attributes;
		final IPluginPropertiesBuilder propBuilder = PluginToolkit.pluginPropertiesBuilder();
		propBuilder.add(IBeanTableModelPlugin.ENTITIY_ID_PROPERTY_KEY, entityId);
		final IPluginProperties properties = propBuilder.build();
		for (final IBeanTableModelPlugin plugin : PluginProvider.getPlugins(IBeanTableModelPlugin.ID, properties)) {
			modifiedAttributes = plugin.modify(properties, modifiedAttributes);
		}

		//if no updater service available, set all attributes to editable false
		if (updaterService == null) {
			final IAttributeToolkit attributeToolkit = CapUiToolkit.attributeToolkit();
			final IAttributeCollectionModifierBuilder modifierBuilder = attributeToolkit.createAttributeCollectionModifierBuilder();
			modifierBuilder.addDefaultModifier(new IAttributeModifier<Object>() {
				@Override
				public void modify(final IProperty sourceProperty, final IAttributeBluePrint<Object> attributeBluePrint) {
					attributeBluePrint.setEditable(false);
				}
			});
			modifiedAttributes = CapUiToolkit.attributeToolkit().createAttributesCopy(modifiedAttributes, modifierBuilder.build());
		}

		this.attributes = new ArrayList<IAttribute<Object>>(modifiedAttributes);
		this.readerService = (IReaderService<Object>) readerService;
		this.paramProvider = (IReaderParameterProvider<Object>) paramProvider;
		this.creatorService = creatorService;
		this.refreshService = refreshService;
		this.updaterService = updaterService;
		this.deleterService = deleterService;

		//fields initialize
		this.onSetConfig = false;
		this.propertyNames = createPropertyNames(modifiedAttributes);
		this.filters = new HashMap<String, IUiFilter>();
		this.data = new HashMap<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>>();
		this.currentPageLoaders = new LinkedList<PageLoader>();
		this.sortModel = new SortModelImpl();
		this.dataCleared = true;
		this.pageSize = DEFAULT_PAGE_SIZE;
		this.rowCount = 0;
		this.maxPageIndex = 0;
		this.beansStateTracker = CapUiToolkit.beansStateTracker();
		this.beanProxyFactory = CapUiToolkit.beanProxyFactory(beanType);
		this.beanListModelObservable = new BeanListModelObservable();
		this.filterChangeObservable = new ChangeObservable();

		//model creation
		this.lookUpListenersStrongRef = new HashSet<ILookUpListener>();
		this.columnModel = createColumnModel(modifiedAttributes, lookUpListenersStrongRef);
		this.dataModel = createDataModel();
		this.tableModel = new TableModel(columnModel, dataModel);

		sortModel.setConfig(sortModelConfig);
		sortModel.addChangeListener(new IChangeListener() {
			@Override
			public void changed() {
				updateColumnModel();
				load();
			}
		});
		updateColumnModel();
	}

	private static List<String> createPropertyNames(final List<IAttribute<Object>> attributesList) {
		final List<String> result = new LinkedList<String>();
		for (final IAttribute<Object> attribute : attributesList) {
			result.add(attribute.getPropertyName());
		}
		return result;
	}

	@Override
	public void clear() {
		rowCount = 0;
		maxPageIndex = 0;
		dataCleared = true;
		data.clear();
		beansStateTracker.clearAll();
		dataModel.fireDataChanged();
	}

	@Override
	public void load() {
		//cancel all page loader
		for (final PageLoader pageLoader : currentPageLoaders) {
			pageLoader.cancel();
		}
		beansStateTracker.clearAll();
		//TODO MG make async call
		//		final SyncResultCallback<Integer> resultCallback = new SyncResultCallback<Integer>();
		//		readerService.count(resultCallback, getParentBeanKeys(), null, null, null);
		//		final Integer rowCountResult = resultCallback.getResultSynchronious();
		//		if (rowCountResult != null) {
		//			rowCount = rowCountResult.intValue();
		//		}
		rowCount = 0;
		dataCleared = false;
		maxPageIndex = 0;
		data.clear();
		loadPage(0);
		dataModel.fireDataChanged();
	}

	private List<? extends IBeanKey> getParentBeanKeys() {
		if (parent == null) {
			return null;
		}
		final List<IBeanKey> beanKeys = new LinkedList<IBeanKey>();
		for (final int i : parent.getSelection()) {
			final IBeanProxy<?> proxy = parent.getBean(i);
			if (proxy != null && proxy.getId() != DUMMY_VALUE) {
				beanKeys.add(new BeanKey(proxy.getId(), proxy.getVersion()));
			}
		}
		if (!beanKeys.isEmpty() && linkType == LinkType.SELECTION_FIRST) {
			return beanKeys.subList(0, 1);
		}
		return beanKeys;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void save() {
		final Set<IBeanProxy<BEAN_TYPE>> modifiedBeans = beansStateTracker.getModifiedBeans();

		final BeanListExecutionHelper executionHelper = new BeanListExecutionHelper(
			this,
			modifiedBeans,
			BeanExecutionPolicy.BATCH,
			new DefaultBeanExceptionConverter());

		final IUiThreadAccess uiThreadAccess = Toolkit.getUiThreadAccess();

		for (final List<IBeanProxy<?>> preparedBeans : executionHelper.prepareExecutions()) {
			if (preparedBeans.size() > 0) {
				final IExecutionTask executionTask = preparedBeans.get(0).getExecutionTask();
				if (executionTask != null) {
					final List<IBeanModification> modifications = new LinkedList<IBeanModification>();
					for (final IBeanProxy<?> bean : preparedBeans) {
						modifications.addAll(bean.getModifications());
						beansStateTracker.unregister((IBeanProxy<BEAN_TYPE>) bean);
					}
					final IResultCallback<List<IBeanDto>> helperCallback = executionHelper.createResultCallback(preparedBeans);
					final IResultCallback<List<IBeanDto>> resultCallback = new IResultCallback<List<IBeanDto>>() {
						@Override
						public void finished(final List<IBeanDto> result) {
							helperCallback.finished(result);
							registerBeans();
						}

						@Override
						public void exception(final Throwable exception) {
							helperCallback.exception(exception);
							registerBeans();
						}

						@Override
						public void timeout() {
							helperCallback.timeout();
							registerBeans();
						}

						private void registerBeans() {
							uiThreadAccess.invokeLater(new Runnable() {
								@Override
								public void run() {
									for (final IBeanProxy<?> bean : preparedBeans) {
										beansStateTracker.register((IBeanProxy<BEAN_TYPE>) bean);
									}
								}
							});
						}
					};
					updaterService.update(resultCallback, modifications, executionTask);
				}
			}
		}
	}

	@Override
	public void undo() {
		for (final IBeanProxy<BEAN_TYPE> bean : new HashSet<IBeanProxy<BEAN_TYPE>>(beansStateTracker.getModifiedBeans())) {
			bean.undoModifications();
		}
		beansStateTracker.clearModifications();
		dataModel.fireDataChanged();
	}

	@Override
	public boolean isDirty() {
		return beansStateTracker.hasModifiedBeans();
	}

	@Override
	public void addModificationStateListener(final IModificationStateListener listener) {
		beansStateTracker.addModificationStateListener(listener);
	}

	@Override
	public void removeModificationStateListener(final IModificationStateListener listener) {
		beansStateTracker.removeModificationStateListener(listener);
	}

	@Override
	public boolean hasExecutions() {
		return beansStateTracker.hasExecutingBeans();
	}

	@Override
	public void cancelExecutions() {
		beansStateTracker.cancelExecutions();
		fireBeansChanged();
	}

	@Override
	public void addProcessStateListener(final IProcessStateListener listener) {
		beansStateTracker.addProcessStateListener(listener);
	}

	@Override
	public void removeProcessStateListener(final IProcessStateListener listener) {
		beansStateTracker.removeProcessStateListener(listener);
	}

	@Override
	public void fireBeansChanged() {
		dataModel.fireDataChanged();
	}

	@Override
	public IBeanProxy<BEAN_TYPE> getBean(final int rowIndex) {
		final int pageIndex = getPage(rowIndex);
		final ArrayList<IBeanProxy<BEAN_TYPE>> page = data.get(Integer.valueOf(pageIndex));
		if (page != null && page.size() > 0) {
			final int startIndex = pageIndex * pageSize;
			final int index = rowIndex - startIndex;
			if (index >= 0 && index < page.size()) {
				final IBeanProxy<BEAN_TYPE> result = page.get(index);
				return result;
			}
		}
		return null;
	}

	@Override
	public void addBean(final int index, final IBeanProxy<BEAN_TYPE> bean) {
		// TODO MG implement addBean
	}

	@Override
	public void removeBean(final int index) {
		// TODO MG implement removeBean
	}

	@Override
	public IAttribute<Object> getAttribute(final int columnIndex) {
		return attributes.get(columnIndex);
	}

	@Override
	public List<IAttribute<Object>> getAttributes() {
		return Collections.unmodifiableList(attributes);
	}

	@Override
	public Object getValue(final int rowIndex, final int columnIndex) {
		final IAttribute<Object> attribute = getAttribute(columnIndex);
		final IBeanProxy<?> bean = getBean(rowIndex);
		return bean.getValue(attribute.getPropertyName());
	}

	@Override
	public int getColumnCount() {
		return attributes.size();
	}

	@Override
	public void addBeanListModelListener(final IBeanListModelListener listener) {
		beanListModelObservable.addBeanListModelListener(listener);
	}

	@Override
	public void removeBeanListModelListener(final IBeanListModelListener listener) {
		beanListModelObservable.removeBeanListModelListener(listener);
	}

	@Override
	public ITableModel getTableModel() {
		return tableModel;
	}

	@Override
	public int getSize() {
		return rowCount;
	}

	@Override
	public ArrayList<Integer> getSelection() {
		return tableModel.getSelection();
	}

	@Override
	public void setSelection(final List<Integer> selection) {
		tableModel.setSelection(selection);
	}

	@Override
	public IBeanProxy<BEAN_TYPE> getFirstSelectedBean() {
		final ArrayList<Integer> selection = getSelection();
		if (selection != null && selection.size() > 0) {
			return getBean(selection.get(0).intValue());
		}
		return null;
	}

	@Override
	public void addFilterChangeListener(final IChangeListener changeListener) {
		filterChangeObservable.addChangeListener(changeListener);
	}

	@Override
	public void removeFilterChangeListener(final IChangeListener changeListener) {
		filterChangeObservable.removeChangeListener(changeListener);
	}

	@Override
	public void setFilter(final String id, final IUiFilter filter) {
		Assert.paramNotNull(id, "id");
		if (filter != null) {
			filters.put(id, filter);
		}
		else {
			filters.remove(id);
		}
		afterFilterChanged(id);
	}

	@Override
	public void addFilter(final String id, final IUiFilter addedFilter) {
		Assert.paramNotNull(id, "id");
		Assert.paramNotNull(addedFilter, "addedFilter");

		final IUiFilterTools filterTools = CapUiToolkit.filterToolkit().filterTools();
		final IUiFilter currentFilter = getFilter(id);
		setFilter(id, filterTools.addFilter(currentFilter, addedFilter));
	}

	@Override
	public void removeFilter(final String id) {
		Assert.paramNotNull(id, "id");
		filters.remove(id);
		afterFilterChanged(id);
	}

	@Override
	public void removeFiltersForProperty(final String id, final String propertyName) {
		final IUiFilterTools filterTools = CapUiToolkit.filterToolkit().filterTools();
		final IUiFilter currentFilter = getFilter(id);
		if (currentFilter != null) {
			setFilter(id, filterTools.removeProperty(currentFilter, propertyName));
		}
	}

	@Override
	public void removeFiltersForProperty(final String id, final int columnIndex) {
		final IAttribute<Object> attribute = getAttribute(columnIndex);
		removeFiltersForProperty(id, attribute.getPropertyName());
	}

	@Override
	public IUiFilter getFilter(final String id) {
		Assert.paramNotNull(id, "id");
		return filters.get(id);
	}

	private void afterFilterChanged(final String id) {
		if (IBeanTableModel.UI_FILTER_ID.equals(id)) {
			updateColumnModel();
		}
		filterChangeObservable.fireChangedEvent();
	}

	@Override
	public ISortModel getSortModel() {
		return sortModel;
	}

	@Override
	public void setPageSize(final int pageSize) {
		this.pageSize = pageSize;
	}

	@Override
	public void setConfig(final IBeanTableConfig config) {
		Assert.paramNotNull(config, "config");
		onSetConfig = true;
		final Map<String, IAttributeConfig> attributeConfigs = config.getAttributeConfigs();
		if (attributeConfigs != null) {
			for (final IAttribute<Object> attribute : attributes) {
				final IAttributeConfig attributeConfig = attributeConfigs.get(attribute.getPropertyName());
				if (attributeConfig != null) {
					attribute.setConfig(attributeConfig);
				}
			}
		}
		final Map<String, IUiFilter> filtersConfig = config.getFilters();
		if (filtersConfig != null) {
			for (final Entry<String, IUiFilter> entry : filtersConfig.entrySet()) {
				setFilter(entry.getKey(), entry.getValue());
			}
		}
		if (config.getSortModelConfig() != null) {
			this.sortModel.setConfig(config.getSortModelConfig());
		}
		if (config.isAutoSelection() != null) {
			this.autoSelection = config.isAutoSelection();
		}
		onSetConfig = false;
		dataModel.fireDataChanged();
	}

	@Override
	public IBeanTableConfig getConfig() {
		final IBeanTableConfigBuilder builder = CapUiToolkit.beanTableConfigBuilder();
		for (final IAttribute<Object> attribute : attributes) {
			builder.addAttributeConfig(attribute);
		}
		builder.setAutoSelection(autoSelection);
		builder.setSortModelConfig(sortModel.getConfig());

		builder.setFilters(filters);
		return builder.build();
	}

	@Override
	public void setActive(final boolean active) {}

	private int getPage(final int rowIndex) {
		return rowIndex / pageSize;
	}

	private void loadPage(final int pageIndex) {
		lastLoadingPage = pageIndex;
		final PageLoader pageLoader = new PageLoader(pageIndex);
		currentPageLoaders.add(pageLoader);
		pageLoader.loadPage();

		while (currentPageLoaders.size() > MAX_PAGE_LOADER_COUNT) {
			final PageLoader pageLoaderToRemove = currentPageLoaders.removeFirst();
			pageLoaderToRemove.cancel();
		}
	}

	private IDefaultTableColumnModel createColumnModel(
		final List<IAttribute<Object>> attributes,
		final Set<ILookUpListener> listenersStrongRef) {
		final ITableModelFactory tableModelFactory = Toolkit.getModelFactoryProvider().getTableModelFactory();

		final IDefaultTableColumnModel result = tableModelFactory.columnModel();
		int columnIndex = 0;
		for (final IAttribute<Object> attribute : attributes) {
			final IDefaultTableColumnBuilder columnBuilder = new DefaultTableColumnBuilder();
			columnBuilder.setText(attribute.getLabel());
			columnBuilder.setToolTipText(attribute.getDescription());
			columnBuilder.setWidth(attribute.getTableWidth());
			columnBuilder.setAlignment(attribute.getTableAlignment());
			columnBuilder.setVisible(attribute.isVisible());
			result.addColumn(columnBuilder);

			final int currentColumnIndex = columnIndex;
			attribute.addChangeListener(new IChangeListener() {
				@Override
				public void changed() {
					result.getColumn(currentColumnIndex).setVisible(attribute.isVisible());
					result.getColumn(currentColumnIndex).setAlignment(attribute.getTableAlignment());
					if (!onSetConfig) {
						dataModel.fireDataChanged();
					}
					updateColumnModel();
				}
			});

			final IValueRange valueRange = attribute.getValueRange();
			if (valueRange instanceof ILookUpValueRange) {
				final ILookUpValueRange lookUpValueRange = (ILookUpValueRange) valueRange;
				final ILookUpAccess lookUpAccess = CapUiToolkit.lookUpCache().getAccess(lookUpValueRange.getLookUpId());
				final ILookUpListener lookUpListener = new ILookUpListener() {

					@Override
					public void taskCreated(final IExecutionTask task) {}

					@Override
					public void afterLookUpChanged() {
						if (attribute.isVisible()) {
							dataModel.fireDataChanged();
						}
					}
				};
				listenersStrongRef.add(lookUpListener);
				lookUpAccess.addLookUpListener(lookUpListener, true);
			}

			columnIndex++;
		}

		return result;
	}

	private void updateColumnModel() {
		int index = 0;
		final boolean cascaded = sortModel.getSorting().size() > 1;
		for (final IDefaultTableColumn column : columnModel.getColumns()) {
			final IAttribute<?> attribute = getAttribute(index);
			if (attribute != null) {
				final IUiFilterTools filterTools = CapUiToolkit.filterToolkit().filterTools();
				final IUiFilter uiFilter = getFilter(IBeanTableModel.UI_FILTER_ID);
				final boolean isFiltered;
				if (uiFilter != null) {
					isFiltered = filterTools.isPropertyFiltered(uiFilter, attribute.getPropertyName());
				}
				else {
					isFiltered = false;
				}

				final IPropertySort propertySort = sortModel.getPropertySort(attribute.getPropertyName());
				if (propertySort.isSorted()) {
					if (propertySort.getSortOrder() == SortOrder.ASC && isFiltered) {
						column.setIcon(IconsSmall.TABLE_SORT_FILTER_ASC);
					}
					else if (propertySort.getSortOrder() == SortOrder.ASC) {
						column.setIcon(IconsSmall.TABLE_SORT_ASC);
					}
					else if (propertySort.getSortOrder() == SortOrder.DESC && isFiltered) {
						column.setIcon(IconsSmall.TABLE_SORT_FILTER_DESC);
					}
					else if (propertySort.getSortOrder() == SortOrder.DESC) {
						column.setIcon(IconsSmall.TABLE_SORT_DESC);
					}
					else if (isFiltered) {
						column.setIcon(IconsSmall.TABLE_FILTER);
					}
					if (cascaded) {
						column.setText("(" + (propertySort.getSortIndex() + 1) + ") " + getLabel(attribute));
					}
					else {
						column.setText(getLabel(attribute));
					}
				}
				else {
					//TODO MG use icon and also do this if sorted
					if (isFiltered) {
						column.setIcon(IconsSmall.TABLE_FILTER);
					}
					else {
						column.setIcon(null);
					}
					column.setText(getLabel(attribute));
				}
			}
			index++;
		}
	}

	private String getLabel(final IAttribute<?> attribute) {
		if (DisplayFormat.LONG == attribute.getLabelDisplayFormat()) {
			return attribute.getLabelLong();
		}
		else {
			return attribute.getLabel();
		}
	}

	private AbstractTableDataModel createDataModel() {
		final AbstractTableDataModel result = new AbstractTableDataModel() {

			@Override
			public int getRowCount() {
				return rowCount;
			}

			@Override
			public ITableCell getCell(final int rowIndex, final int columnIndex) {
				if (dataCleared) {
					return new TableCellBuilder().build();
				}

				lastRenderedRow = rowIndex;

				final int pageIndex = getPage(rowIndex);
				final ArrayList<IBeanProxy<BEAN_TYPE>> page = data.get(Integer.valueOf(pageIndex));

				if (page == null) {
					loadPage(pageIndex);
					return createDummyCellBuilder(rowIndex, columnIndex).build();
				}
				else {
					final IAttribute<Object> attribute = attributes.get(columnIndex);
					final int startIndex = pageIndex * pageSize;
					final IBeanProxy<BEAN_TYPE> bean = page.get(rowIndex - startIndex);
					final Object value = bean.getValue(attribute.getPropertyName());

					final ITableCellBuilder cellBuilder;
					if (bean.getId() instanceof IDummyValue) {
						cellBuilder = createDummyCellBuilder(rowIndex, columnIndex);
					}
					else {
						cellBuilder = createCellBuilder(rowIndex, columnIndex, attribute, value);
					}

					final IBeanMessage message = bean.getFirstWorstMessage();
					if (bean.getExecutionTask() != null) {
						cellBuilder.setForegroundColor(Colors.DISABLED);
					}
					else if (message != null && (message.getType() == BeanMessageType.ERROR)) {
						cellBuilder.setForegroundColor(Colors.ERROR);
					}
					else if (bean.hasModifications()) {
						cellBuilder.setForegroundColor(Colors.STRONG);
						if (bean.isModified(attribute.getPropertyName())) {
							cellBuilder.setMarkup(Markup.STRONG);
						}
						else {
							cellBuilder.setMarkup(Markup.EMPHASIZED);
						}
					}

					if (bean.hasExecution()) {
						cellBuilder.setEditable(false);
					}

					return cellBuilder.build();
				}
			}

			private ITableCellBuilder createCellBuilder(
				final int rowIndex,
				final int columnIndex,
				final IAttribute<Object> attribute,
				final Object value) {

				final ITableCellBuilder cellBuilder = createDefaultCellBuilder(rowIndex, columnIndex);
				final IObjectLabelConverter<Object> converter = attribute.getCurrentControlPanel().getObjectLabelConverter();

				String text;
				String toolTipText;
				IImageConstant icon;

				if (value instanceof Collection<?>) {
					final Collection<?> collection = (Collection<?>) value;
					final int collectionSize = collection.size();
					if (collectionSize > 0) {
						final Object firstElement = collection.iterator().next();
						if (collectionSize > 1) {
							text = converter.convertToString(firstElement) + " [" + collectionSize + "]";
						}
						else {
							text = converter.convertToString(firstElement);
						}
						toolTipText = converter.getDescription(firstElement);
						icon = null;
					}
					else {
						text = null;
						toolTipText = null;
						icon = null;
					}
				}
				else {
					text = converter.convertToString(value);
					toolTipText = converter.getDescription(value);
					icon = converter.getIcon(value);
				}

				cellBuilder.setText(text).setToolTipText(toolTipText).setIcon(icon);
				return cellBuilder;
			}

			private ITableCellBuilder createDummyCellBuilder(final int rowIndex, final int columnIndex) {
				final ITableCellBuilder cellBuilder = createDefaultCellBuilder(rowIndex, columnIndex);
				cellBuilder.setText("...").setToolTipText("Data will be loaded in background");
				return cellBuilder;
			}

			private ITableCellBuilder createDefaultCellBuilder(final int rowIndex, final int columnIndex) {
				final ITableCellBuilder cellBuilder = new TableCellBuilder();
				if (rowIndex % 2 == 0) {
					cellBuilder.setBackgroundColor(Colors.DEFAULT_TABLE_EVEN_BACKGROUND_COLOR);
				}

				final IAttribute<Object> attribute = attributes.get(columnIndex);
				boolean editable = attribute.isEditable();
				editable = editable && !attribute.isCollectionType();
				editable = editable && attribute.getCurrentControlPanel().getStringObjectConverter() != null;
				cellBuilder.setEditable(editable);

				return cellBuilder;
			}
		};

		result.addDataModelListener(new TableDataModelAdapter() {

			@Override
			public void selectionChanged() {
				beanListModelObservable.fireSelectionChanged();
			}

		});

		return result;
	}

	private class PageLoader {

		private final int pageIndex;
		private final IUiThreadAccess uiThreadAccess;

		private Object parameter;
		private boolean innerPage;
		private boolean canceled;
		private ArrayList<IBeanProxy<BEAN_TYPE>> page;

		private IExecutionTask executionTask;
		private IBeanProxy<BEAN_TYPE> dummyBeanProxy;

		private final IFilter filter = getFilter();

		PageLoader(final int pageIndex) {
			this.pageIndex = pageIndex;
			this.uiThreadAccess = Toolkit.getUiThreadAccess();
		}

		void loadPage() {
			page = new ArrayList<IBeanProxy<BEAN_TYPE>>();
			data.put(Integer.valueOf(pageIndex), page);

			dummyBeanProxy = beanProxyFactory.createProxy(new DummyBeanDto(), propertyNames);
			executionTask = CapUiToolkit.executionTaskFactory().create();
			dummyBeanProxy.setExecutionTask(executionTask);
			beansStateTracker.register(dummyBeanProxy);

			for (int i = 0; i < pageSize; i++) {
				page.add(dummyBeanProxy);
			}

			final int count = ((pageIndex) * pageSize) + pageSize;
			innerPage = rowCount > count;
			rowCount = Math.max(rowCount, count);

			dataModel.fireDataChanged();

			beanListModelObservable.fireBeansChanged();

			this.parameter = paramProvider.getParameter();

			final Thread thread = new Thread(createRunnable());
			thread.setDaemon(true);
			thread.start();
		}

		void cancel() {
			this.canceled = true;
			if (executionTask != null) {
				executionTask.cancel();
			}
		}

		private void removePageLater() {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					data.remove(Integer.valueOf(pageIndex));
					dataModel.fireDataChanged();

					currentPageLoaders.remove(this);
					dummyBeanProxy.setExecutionTask(null);
					beansStateTracker.unregister(dummyBeanProxy);
				}
			});
		}

		private void clearLater() {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					data.remove(Integer.valueOf(pageIndex));
					currentPageLoaders.remove(this);
					dummyBeanProxy.setExecutionTask(null);
					beansStateTracker.unregister(dummyBeanProxy);
					clear();
				}
			});
		}

		private Runnable createRunnable() {
			return new Runnable() {

				@Override
				public void run() {
					if (innerPage) {
						try {
							Thread.sleep(INNER_PAGE_DELAY);
						}
						catch (final InterruptedException e) {
							return;
						}
					}

					if (canceled) {
						removePageLater();
						return;
					}

					executionTask.addExecutionCallbackListener(new IExecutionCallbackListener() {
						@Override
						public void canceled() {
							if (!canceled) {//if canceled by user
								clearLater();
							}
						}
					});

					if (lastLoadingPage != pageIndex) {
						final int pageStart = (pageIndex) * pageSize;
						final int pageEnd = pageStart + pageSize;
						if (pageStart - VIEW_SIZE > lastRenderedRow || lastRenderedRow > pageEnd + VIEW_SIZE) {
							removePageLater();
							return;
						}
					}

					//CHECKSTYLE:OFF
					System.out.println("Load page start: " + pageIndex);
					//CHECKSTYLE:ON

					final List<IBeanDto> beanDtos;
					try {
						//TODO MG make async call
						final SyncResultCallback<List<IBeanDto>> resultCallback = new SyncResultCallback<List<IBeanDto>>();
						BeanTableModelImpl.this.readerService.read(
								resultCallback,
								getParentBeanKeys(),
								filter,
								sortModel.getSorting(),
								pageIndex * pageSize,
								pageSize + 1,
								parameter,
								executionTask);
						beanDtos = resultCallback.getResultSynchronious();
					}
					catch (final Exception e) {
						removePageLater();
						//TODO to proper error handling here
						throw (new RuntimeException(e));
					}

					if (canceled || executionTask.isCanceled()) {
						removePageLater();
					}
					else {
						uiThreadAccess.invokeLater(new Runnable() {
							@Override
							public void run() {

								currentPageLoaders.remove(this);

								if (beanDtos.size() > pageSize && pageIndex >= maxPageIndex) {
									rowCount = Math.max(rowCount, ((pageIndex + 1) * pageSize + pageSize - 1));
									maxPageIndex = pageIndex;
								}
								else if (pageIndex >= maxPageIndex) {
									rowCount = (pageIndex * pageSize + beanDtos.size());
									maxPageIndex = pageIndex;
								}

								page.clear();
								int index = 0;
								final int pageOffset = pageSize * pageIndex;
								for (final IBeanDto beanDto : beanDtos) {
									if (index < pageSize) {
										final IBeanProxy<BEAN_TYPE> beanProxy = beanProxyFactory.createProxy(
												beanDto,
												propertyNames);
										page.add(beanProxy);
										beansStateTracker.register(beanProxy);
										final int rowNr = pageOffset + index;
										beanProxy.addPropertyChangeListener(new PropertyChangeListener() {
											@Override
											public void propertyChange(final PropertyChangeEvent evt) {
												dataModel.fireRowsChanged(new int[] {rowNr});
											}
										});
										index++;
									}
								}

								dummyBeanProxy.setExecutionTask(null);
								beansStateTracker.unregister(dummyBeanProxy);

								dataModel.fireDataChanged();
								// TODO MG,HRW review, will be fixed after refactoring
								beanListModelObservable.fireBeansChanged();

								//CHECKSTYLE:OFF
								System.out.println("Load page finished: " + pageIndex);
								//CHECKSTYLE:ON
							}
						});
					}

				}
			};
		}
	}

	private IFilter getFilter() {
		if (filters.size() > 0) {
			final IBooleanFilterBuilder builder = CapCommonToolkit.filterFactory().booleanFilterBuilder();
			builder.setOperator(BooleanOperator.AND);
			final IUiFilterFactory filterFactory = CapUiToolkit.filterToolkit().filterFactory();
			for (final IUiFilter uiFilter : filters.values()) {
				builder.addFilter(filterFactory.convert(uiFilter));
			}
			return builder.build();
		}
		else {
			return null;
		}
	}

	private interface IDummyValue {}

	private static IBeanDto createCopyBeanDto(final IBeanProxy<?> proxy) {
		return new IBeanDto() {
			@Override
			public Object getValue(final String propertyName) {
				return proxy.getValue(propertyName);
			}

			@Override
			public Object getId() {
				return DUMMY_VALUE;
			}

			@Override
			public long getVersion() {
				return 0;
			}
		};
	}

	private static class DummyBeanDto implements IBeanDto {

		@Override
		public Object getValue(final String propertyName) {
			return null;
		}

		@Override
		public Object getId() {
			return DUMMY_VALUE;
		}

		@Override
		public long getVersion() {
			return 0;
		}

	}

}
