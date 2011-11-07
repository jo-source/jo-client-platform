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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jowidgets.api.color.Colors;
import org.jowidgets.api.controller.IDisposeListener;
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
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
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
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifierBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeConfig;
import org.jowidgets.cap.ui.api.attribute.IAttributeFilter;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanMessageBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeansStateTracker;
import org.jowidgets.cap.ui.api.color.CapColors;
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
import org.jowidgets.common.color.IColorConstant;
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
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.concurrent.DaemonThreadFactory;
import org.jowidgets.util.event.ChangeObservable;
import org.jowidgets.util.event.IChangeListener;

final class BeanTableModelImpl<BEAN_TYPE> implements IBeanTableModel<BEAN_TYPE> {

	private static final int DEFAULT_PAGE_SIZE = 1000;
	private static final int LISTENER_DELAY = 100;
	private static final int INNER_PAGE_LOAD_DELAY = 100;
	private static final Object DUMMY_ID = new Object() {};
	private static final IResultCallback<Void> DUMMY_PAGE_LOAD_RESULT_CALLBACK = new DummyPageLoadResultCallback();

	private final Object entityId;
	private final Class<BEAN_TYPE> beanType;

	private final Map<String, IUiFilter> filters;
	private final ChangeObservable filterChangeObservable;
	private final ISortModel sortModel;

	private final Map<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>> data;
	private final ArrayList<IBeanProxy<BEAN_TYPE>> addedData;
	private final Set<IBeanProxy<BEAN_TYPE>> lastSelectedBeans;

	private final IBeanProxyFactory<BEAN_TYPE> beanProxyFactory;
	private final IBeansStateTracker<BEAN_TYPE> beansStateTracker;
	private final ArrayList<IAttribute<Object>> attributes;
	private final List<String> propertyNames;

	private final ICreatorService creatorService;
	private final IReaderService<Object> readerService;
	private final IReaderParameterProvider<Object> readerParameterProvider;
	private final IRefreshService refreshService;
	private final IUpdaterService updaterService;
	private final IDeleterService deleterService;

	private final IBeanListModel<?> parent;
	private final LinkType linkType;

	private final boolean autoRowCount;

	private final BeanListModelObservable beanListModelObservable;
	private final DisposeObservable disposeObservable;
	private final TableDataModelListener tableDataModelListener;
	private final IChangeListener sortModelChangeListener;
	private final ParentModelListener parentModelListener;
	private final List<AttributeChangeListener> attributeChangeListeners;

	private final IDefaultTableColumnModel columnModel;
	private final DataModel dataModel;
	private final ITableModel tableModel;

	private final Set<AttributeLookUpListener> lookUpListenersStrongRef;
	private final Map<Integer, PageLoader> programmaticPageLoader;

	private final String userCanceledMessage;
	private final String loadErrorMessage;

	private PageLoader evenPageLoader;
	private PageLoader oddPageLoader;
	private CountLoader countLoader;

	private final int pageSize;
	private int rowCount;
	private Integer countedRowCount;
	private int maxPageIndex;
	private boolean dataCleared;
	private boolean autoSelection;
	private boolean onSetConfig;
	private Integer scheduledLoadDelay;
	private boolean disposed;

	@SuppressWarnings("unchecked")
	BeanTableModelImpl(
		final Object entityId,
		final Class<? extends BEAN_TYPE> beanType,
		List<IAttribute<Object>> attributes,
		final ISortModelConfig sortModelConfig,
		final IReaderService<? extends Object> readerService,
		final IReaderParameterProvider<? extends Object> paramProvider,
		final ICreatorService creatorService,
		final IRefreshService refreshService,
		final IUpdaterService updaterService,
		final IDeleterService deleterService,
		final IBeanListModel<?> parent,
		final LinkType linkType,
		final boolean autoRowCount) {

		//arguments checks
		Assert.paramNotNull(entityId, "entityId");
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(attributes, "attributes");
		Assert.paramNotNull(readerService, "readerService");
		Assert.paramNotNull(paramProvider, "paramProvider");

		this.parent = parent;
		this.entityId = entityId;
		this.autoRowCount = autoRowCount;
		this.beanType = (Class<BEAN_TYPE>) beanType;
		this.linkType = linkType;
		if (parent != null) {
			Assert.paramNotNull(linkType, "linkType");
			this.parentModelListener = new ParentModelListener();
			parent.addBeanListModelListener(parentModelListener);
		}
		else {
			this.parentModelListener = null;
		}

		//inject table model plugins
		attributes = createModifiedByPluginsAttributes(entityId, attributes);

		//if no updater service available, set all attributes to editable false
		if (updaterService == null) {
			attributes = createReadonlyAttributes(attributes);
		}

		this.attributes = new ArrayList<IAttribute<Object>>(attributes);
		this.readerService = (IReaderService<Object>) readerService;
		this.readerParameterProvider = (IReaderParameterProvider<Object>) paramProvider;
		this.creatorService = creatorService;
		this.refreshService = refreshService;
		this.updaterService = updaterService;
		this.deleterService = deleterService;
		this.autoSelection = true;
		this.onSetConfig = false;
		this.propertyNames = createPropertyNames(attributes);
		this.filters = new HashMap<String, IUiFilter>();
		this.data = new HashMap<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>>();
		this.addedData = new ArrayList<IBeanProxy<BEAN_TYPE>>();
		this.lastSelectedBeans = new HashSet<IBeanProxy<BEAN_TYPE>>();
		this.sortModel = new SortModelImpl();
		this.sortModelChangeListener = new SortModelChangeListener();
		this.tableDataModelListener = new TableDataModelListener();
		this.attributeChangeListeners = new LinkedList<AttributeChangeListener>();
		this.dataCleared = true;
		this.disposed = false;
		this.pageSize = DEFAULT_PAGE_SIZE;
		this.rowCount = 0;
		this.maxPageIndex = 0;
		this.beansStateTracker = CapUiToolkit.beansStateTracker();
		this.beanProxyFactory = CapUiToolkit.beanProxyFactory(beanType);
		this.beanListModelObservable = new BeanListModelObservable();
		this.disposeObservable = new DisposeObservable();
		this.filterChangeObservable = new ChangeObservable();
		this.programmaticPageLoader = new HashMap<Integer, PageLoader>();
		this.userCanceledMessage = Messages.getString("BeanTableModelImpl.user_canceled");
		this.loadErrorMessage = Messages.getString("BeanTableModelImpl.load_error");

		//configure sort model
		sortModel.setConfig(sortModelConfig);
		sortModel.addChangeListener(sortModelChangeListener);

		//model creation
		this.columnModel = createColumnModel(attributes);
		this.dataModel = new DataModel();
		dataModel.addDataModelListener(tableDataModelListener);
		this.tableModel = new TableModel(columnModel, dataModel);

		//add some listeners
		this.lookUpListenersStrongRef = new HashSet<AttributeLookUpListener>();
		addAttributeListeners(attributes, lookUpListenersStrongRef);

		//update the columns
		updateColumnModel();
	}

	private static List<IAttribute<Object>> createModifiedByPluginsAttributes(
		final Object entityId,
		final List<IAttribute<Object>> attributes) {

		List<IAttribute<Object>> result = attributes;

		final IPluginPropertiesBuilder propBuilder = PluginToolkit.pluginPropertiesBuilder();
		propBuilder.add(IBeanTableModelPlugin.ENTITIY_ID_PROPERTY_KEY, entityId);
		final IPluginProperties properties = propBuilder.build();

		for (final IBeanTableModelPlugin plugin : PluginProvider.getPlugins(IBeanTableModelPlugin.ID, properties)) {
			result = plugin.modify(properties, result);
		}
		return result;
	}

	private static List<IAttribute<Object>> createReadonlyAttributes(final List<IAttribute<Object>> attributes) {
		final IAttributeToolkit attributeToolkit = CapUiToolkit.attributeToolkit();
		final IAttributeCollectionModifierBuilder modifierBuilder = attributeToolkit.createAttributeCollectionModifierBuilder();
		modifierBuilder.addDefaultModifier().setEditable(false);
		return CapUiToolkit.attributeToolkit().createAttributesCopy(attributes, modifierBuilder.build());
	}

	private static List<String> createPropertyNames(final List<IAttribute<Object>> attributesList) {
		final List<String> result = new LinkedList<String>();
		for (final IAttribute<Object> attribute : attributesList) {
			result.add(attribute.getPropertyName());
		}
		return result;
	}

	private void addAttributeListeners(
		final List<IAttribute<Object>> attributes,
		final Set<AttributeLookUpListener> listenersStrongRef) {
		for (int index = 0; index < attributes.size(); index++) {
			final IAttribute<Object> attribute = attributes.get(index);

			final AttributeChangeListener attributeChangeListener = new AttributeChangeListener(attribute, index);
			attributeChangeListeners.add(attributeChangeListener);
			attribute.addChangeListener(attributeChangeListener);

			if (attribute.getValueRange() instanceof ILookUpValueRange) {
				addAttributeLookUpListener(attribute, listenersStrongRef);
			}
		}
	}

	private void addAttributeLookUpListener(
		final IAttribute<Object> attribute,
		final Set<AttributeLookUpListener> listenersStrongRef) {
		final ILookUpValueRange lookUpValueRange = (ILookUpValueRange) attribute.getValueRange();
		final ILookUpAccess lookUpAccess = CapUiToolkit.lookUpCache().getAccess(lookUpValueRange.getLookUpId());
		final AttributeLookUpListener lookUpListener = new AttributeLookUpListener(attribute);
		listenersStrongRef.add(lookUpListener);
		lookUpAccess.addLookUpListener(lookUpListener, true);
	}

	private IDefaultTableColumnModel createColumnModel(final List<IAttribute<Object>> attributes) {

		final ITableModelFactory tableModelFactory = Toolkit.getModelFactoryProvider().getTableModelFactory();
		final IDefaultTableColumnModel result = tableModelFactory.columnModel();

		int columnIndex = 0;
		for (final IAttribute<Object> attribute : attributes) {
			final IDefaultTableColumnBuilder columnBuilder = new DefaultTableColumnBuilder();
			columnBuilder.setText(attribute.getCurrentLabel());
			columnBuilder.setToolTipText(attribute.getDescription());
			columnBuilder.setWidth(attribute.getTableWidth());
			columnBuilder.setAlignment(attribute.getTableAlignment());
			columnBuilder.setVisible(attribute.isVisible());
			result.addColumn(columnBuilder.build());
			columnIndex++;
		}

		return result;
	}

	private void updateColumnModel() {
		int index = 0;
		for (final IDefaultTableColumn column : columnModel.getColumns()) {
			final IAttribute<Object> attribute = getAttribute(index);
			if (attribute != null) {
				final IPropertySort propertySort = sortModel.getPropertySort(attribute.getPropertyName());
				column.setIcon(getColumnIcon(attribute, propertySort));
				column.setText(getColumnText(attribute, propertySort));
			}
			index++;
		}
	}

	private IImageConstant getColumnIcon(final IAttribute<Object> attribute, final IPropertySort propertySort) {
		final boolean isFiltered = isAttributeFiltered(attribute);
		if (propertySort.getSortOrder() == SortOrder.ASC && isFiltered) {
			return IconsSmall.TABLE_SORT_FILTER_ASC;
		}
		else if (propertySort.getSortOrder() == SortOrder.ASC) {
			return IconsSmall.TABLE_SORT_ASC;
		}
		else if (propertySort.getSortOrder() == SortOrder.DESC && isFiltered) {
			return IconsSmall.TABLE_SORT_FILTER_DESC;
		}
		else if (propertySort.getSortOrder() == SortOrder.DESC) {
			return IconsSmall.TABLE_SORT_DESC;
		}
		else if (isFiltered) {
			return IconsSmall.TABLE_FILTER;
		}
		else {
			return null;
		}
	}

	private boolean isAttributeFiltered(final IAttribute<Object> attribute) {
		final IUiFilter uiFilter = getFilter(IBeanTableModel.UI_FILTER_ID);
		if (uiFilter != null) {
			final IUiFilterTools filterTools = CapUiToolkit.filterToolkit().filterTools();
			return filterTools.isPropertyFiltered(uiFilter, attribute.getPropertyName());
		}
		else {
			return false;
		}
	}

	private String getColumnText(final IAttribute<Object> attribute, final IPropertySort propertySort) {
		if (propertySort.isSorted() && sortModel.getSorting().size() > 1) {
			return "(" + (propertySort.getSortIndex() + 1) + ") " + attribute.getCurrentLabel();
		}
		else {
			return attribute.getCurrentLabel();
		}
	}

	@Override
	public void addDisposeListener(final IDisposeListener listener) {
		disposeObservable.addDisposeListener(listener);
	}

	@Override
	public void removeDisposeListener(final IDisposeListener listener) {
		disposeObservable.removeDisposeListener(listener);
	}

	@Override
	public void dispose() {
		if (!disposed) {
			disposeObservable.fireOnDispose();
			clear();
			beanListModelObservable.dispose();
			filterChangeObservable.dispose();
			beansStateTracker.dispose();
			for (final AttributeLookUpListener attributeLookUpListener : lookUpListenersStrongRef) {
				attributeLookUpListener.dispose();
			}
			lookUpListenersStrongRef.clear();
			sortModel.removeChangeListener(sortModelChangeListener);
			dataModel.removeDataModelListener(tableDataModelListener);
			if (parentModelListener != null && parent != null) {
				parent.removeBeanListModelListener(parentModelListener);
			}
			for (final AttributeChangeListener listener : attributeChangeListeners) {
				listener.dispose();
			}
			disposeBeans();
			disposed = true;
		}
	}

	private void disposeBeans() {
		for (final ArrayList<IBeanProxy<BEAN_TYPE>> page : data.values()) {
			for (final IBeanProxy<BEAN_TYPE> bean : page) {
				if (bean != null) {
					bean.dispose();
				}
			}
		}
		for (final IBeanProxy<BEAN_TYPE> addedBean : addedData) {
			if (addedBean != null) {
				addedBean.dispose();
			}
		}
	}

	@Override
	public boolean isDisposed() {
		return disposed;
	}

	@Override
	public Object getEntityId() {
		return entityId;
	}

	@Override
	public Class<BEAN_TYPE> getBeanType() {
		return beanType;
	}

	@Override
	public ICreatorService getCreatorService() {
		return creatorService;
	}

	@Override
	public IDeleterService getDeleterService() {
		return deleterService;
	}

	@Override
	public void clear() {
		if (!Toolkit.getUiThreadAccess().isUiThread()) {
			throw new IllegalStateException("Clear must be invoked in the ui thread");
		}
		tryToCanceLoader();
		lastSelectedBeans.clear();
		removeSelection();
		rowCount = 0;
		countedRowCount = null;
		maxPageIndex = 0;
		dataCleared = true;
		data.clear();
		addedData.clear();
		beansStateTracker.clearAll();
		dataModel.fireDataChanged();
	}

	@Override
	public void load() {
		if (!Toolkit.getUiThreadAccess().isUiThread()) {
			throw new IllegalStateException("Load must be invoked in the ui thread");
		}
		tryToCanceLoader();
		lastSelectedBeans.clear();
		lastSelectedBeans.addAll(removeSelection());
		beansStateTracker.clearAll();
		if (rowCount == 0) {
			rowCount = 1;
		}
		dataCleared = false;
		maxPageIndex = 0;
		data.clear();
		addedData.clear();
		scheduledLoadDelay = null;
		countedRowCount = null;
		countLoader = new CountLoader();

		dataModel.fireDataChanged();
	}

	@Override
	public void loadScheduled(final int delayMillis) {
		scheduledLoadDelay = Integer.valueOf(delayMillis);
		load();
	}

	@Override
	public int getPageCount() {
		return (getSize() / pageSize) + 1;
	}

	@Override
	public boolean isPageCreated(final int pageIndex) {
		return data.get(Integer.valueOf(pageIndex)) != null;
	}

	@Override
	public boolean isPageLoading(final int pageIndex) {
		final PageLoader loadingPageLoader = getLoadingPageLoader(pageIndex);
		return loadingPageLoader != null && !loadingPageLoader.isDisposed();
	}

	private PageLoader getLoadingPageLoader(final int pageIndex) {
		final boolean even = pageIndex % 2 == 0;
		if (even && evenPageLoader != null && !evenPageLoader.isDisposed()) {
			return evenPageLoader;
		}
		else if (!even && oddPageLoader != null && !oddPageLoader.isDisposed()) {
			return oddPageLoader;
		}
		final PageLoader pageLoader = programmaticPageLoader.get(Integer.valueOf(pageIndex));
		if (pageLoader != null && !pageLoader.isDisposed()) {
			return pageLoader;
		}
		return null;
	}

	@Override
	public void loadPage(final int page) {
		loadPage(DUMMY_PAGE_LOAD_RESULT_CALLBACK, page, CapUiToolkit.executionTaskFactory().create());
	}

	@Override
	public void loadPage(
		final IResultCallback<Void> resultCallback,
		final int pageIndex,
		final IExecutionCallback executionCallback) {
		tryToCancelProgrammaticPageLoader(pageIndex);
		final PageLoader pageLoader = new PageLoader(pageIndex, resultCallback);
		programmaticPageLoader.put(Integer.valueOf(pageIndex), pageLoader);
		pageLoader.loadPage();
	}

	private void loadEvenOddPage(final int pageIndex) {
		final boolean even = pageIndex % 2 == 0;
		tryToCancelProgrammaticPageLoader(pageIndex);
		if (even) {
			tryToCancelPageLoader(evenPageLoader);
			evenPageLoader = new PageLoader(pageIndex, null);
			evenPageLoader.loadPage();
		}
		else {
			tryToCancelPageLoader(oddPageLoader);
			oddPageLoader = new PageLoader(pageIndex, null);
			oddPageLoader.loadPage();
		}
	}

	private void completeEvenOddPage(final int pageIndex) {
		final boolean even = pageIndex % 2 == 0;
		tryToCancelProgrammaticPageLoader(pageIndex);
		if (even) {
			tryToCancelPageLoader(evenPageLoader);
			evenPageLoader = new PageLoader(pageIndex, null);
			evenPageLoader.completePage();
		}
		else {
			tryToCancelPageLoader(oddPageLoader);
			oddPageLoader = new PageLoader(pageIndex, null);
			oddPageLoader.completePage();
		}
	}

	private void tryToCanceLoader() {
		tryToCancelProgrammaticPageLoader();
		tryToCancelCountLoader();
		tryToCancelEvenOddPageLoader();
	}

	private void tryToCancelProgrammaticPageLoader() {
		for (final PageLoader pageLoader : programmaticPageLoader.values()) {
			tryToCancelPageLoader(pageLoader);
		}
		programmaticPageLoader.clear();
	}

	private void tryToCancelProgrammaticPageLoader(final int page) {
		tryToCancelPageLoader(programmaticPageLoader.remove(page));
	}

	private void tryToCancelEvenOddPageLoader() {
		tryToCancelPageLoader(evenPageLoader);
		tryToCancelPageLoader(oddPageLoader);
	}

	private void tryToCancelCountLoader() {
		if (countLoader != null && !countLoader.isDisposed()) {
			countLoader.cancel();
		}
	}

	private void tryToCancelPageLoader(final PageLoader pageLoader) {
		if (pageLoader != null && !pageLoader.isDisposed()) {
			pageLoader.cancel();
		}
	}

	@Override
	public void refreshBean(final IBeanProxy<BEAN_TYPE> bean) {
		refreshBeans(Collections.singletonList(bean));
	}

	@Override
	public void refreshBeans(final Collection<IBeanProxy<BEAN_TYPE>> beans) {
		if (refreshService != null) {
			final BeanListExecutionHelper executionHelper = new BeanListExecutionHelper(
				this,
				beans,
				BeanExecutionPolicy.BATCH,
				new DefaultBeanExceptionConverter());

			for (final List<IBeanProxy<?>> preparedBeans : executionHelper.prepareExecutions()) {
				if (preparedBeans.size() > 0) {
					final IExecutionTask executionTask = preparedBeans.get(0).getExecutionTask();
					if (executionTask != null) {
						final IBeanKeyFactory beanKeyFactory = CapUiToolkit.beanKeyFactory();
						final List<IBeanKey> beanKeys = beanKeyFactory.createKeys(preparedBeans);
						final IResultCallback<List<IBeanDto>> helperCallback = executionHelper.createResultCallback(preparedBeans);
						refreshService.refresh(helperCallback, beanKeys, executionTask);
					}
				}
			}
		}
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
		return dataModel.getBean(rowIndex);
	}

	@Override
	public void addBean(final IBeanProxy<BEAN_TYPE> bean) {
		Assert.paramNotNull(bean, "bean");
		beansStateTracker.register(bean);
		addedData.add(bean);
		fireBeansChanged();
	}

	@Override
	public void removeBeans(final Collection<? extends IBeanProxy<BEAN_TYPE>> beans) {
		Assert.paramNotNull(beans, "beans");
		//data structure must rebuild, so do not load until this happens
		tryToCanceLoader();

		final List<IBeanProxy<BEAN_TYPE>> selectedBeans = getSelectedBeans();
		final List<Integer> newSelection = new LinkedList<Integer>();

		removeBeansFromData(new HashSet<IBeanProxy<BEAN_TYPE>>(selectedBeans), newSelection, beans);
		removeBeansFromAddedData(new HashSet<IBeanProxy<BEAN_TYPE>>(selectedBeans), newSelection, beans);

		setSelection(newSelection);

		fireBeansChanged();
	}

	private void removeBeansFromData(
		final Set<IBeanProxy<BEAN_TYPE>> oldSelection,
		final List<Integer> newSelection,
		final Collection<? extends IBeanProxy<BEAN_TYPE>> beans) {
		final Map<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>> newData = getNewData(oldSelection, newSelection, beans);
		data.clear();
		for (final Entry<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>> entry : newData.entrySet()) {
			data.put(entry.getKey(), entry.getValue());
		}
	}

	private Map<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>> getNewData(
		final Set<IBeanProxy<BEAN_TYPE>> oldSelection,
		final List<Integer> newSelection,
		final Collection<? extends IBeanProxy<BEAN_TYPE>> beans) {

		//hold the beans that should be deleted but that are currently not deleted
		final Set<IBeanProxy<BEAN_TYPE>> beansToDelete = new HashSet<IBeanProxy<BEAN_TYPE>>(beans);

		final Map<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>> newData = new HashMap<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>>();
		int newPageIndex = 0;
		ArrayList<IBeanProxy<BEAN_TYPE>> newPage = new ArrayList<IBeanProxy<BEAN_TYPE>>(pageSize);

		final int pageCount = getPage(dataModel.getDataRowCount()) + 1;
		int addedNullCount = 0;
		for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
			final ArrayList<IBeanProxy<BEAN_TYPE>> page = data.get(pageIndex);
			//add a regular page
			if (page != null) {
				for (final IBeanProxy<BEAN_TYPE> bean : page) {
					final boolean deletedBeanRemoved = beansToDelete.remove(bean);
					if (deletedBeanRemoved) {
						beansStateTracker.unregister(bean);
						rowCount--;
						if (countedRowCount != null) {
							countedRowCount = Integer.valueOf(countedRowCount.intValue() - 1);
						}
					}
					else if (newPage.size() < pageSize && addedNullCount < pageSize) {
						newPage.add(bean);
					}
					else {
						if (addedNullCount < pageSize) {
							newData.put(newPageIndex, newPage);
						}
						newPageIndex++;
						newPage = new ArrayList<IBeanProxy<BEAN_TYPE>>(pageSize);
						newPage.add(bean);
					}
					if (oldSelection.remove(bean)) {
						if (!deletedBeanRemoved) {
							newSelection.add(newPageIndex * pageSize + newPage.size() - 1);
						}
					}
					if (bean == null) {
						addedNullCount++;
					}
					else {
						addedNullCount = 0;
					}
				}
			}
			//add a null page (add 'pageSize' times a 'null')
			else if (addedNullCount < pageSize) {
				for (int i = 0; i < pageSize; i++) {
					if (newPage.size() < pageSize) {
						newPage.add(null);
						addedNullCount++;
					}
					else {
						if (addedNullCount < pageSize) {
							newData.put(newPageIndex, newPage);
						}
						newPageIndex++;
						newPage = new ArrayList<IBeanProxy<BEAN_TYPE>>(pageSize);
						newPage.add(null);
						addedNullCount++;
					}
				}
			}
			//skip the page 
			//(there a more than one page of null's added, so the page can be skipped) 
			else {
				newPage = new ArrayList<IBeanProxy<BEAN_TYPE>>(pageSize);
				newPageIndex++;
				addedNullCount = addedNullCount + pageSize;
			}

		}
		//add the last new page if it has data
		if (newPage.size() > 0) {
			newData.put(newPageIndex, newPage);
		}
		return newData;
	}

	private void removeBeansFromAddedData(
		final Set<IBeanProxy<BEAN_TYPE>> oldSelection,
		final List<Integer> newSelection,
		final Collection<? extends IBeanProxy<BEAN_TYPE>> beans) {
		//hold the beans that should be deleted but that are currently not deleted
		final Set<IBeanProxy<BEAN_TYPE>> beansToDelete = new HashSet<IBeanProxy<BEAN_TYPE>>(beans);
		final LinkedList<IBeanProxy<BEAN_TYPE>> newAddedData = new LinkedList<IBeanProxy<BEAN_TYPE>>();
		for (final IBeanProxy<BEAN_TYPE> addedBean : addedData) {
			final boolean removed = beansToDelete.remove(addedBean);
			if (removed) {
				beansStateTracker.unregister(addedBean);
			}
			else {
				newAddedData.add(addedBean);
				if (oldSelection.remove(addedBean)) {
					newSelection.add(dataModel.getDataRowCount() + newAddedData.size() - 1);
				}
			}
		}
		addedData.clear();
		addedData.addAll(newAddedData);
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
	public List<IAttribute<Object>> getAttributes(final IAttributeFilter filter) {
		Assert.paramNotNull(filter, "filter");
		final List<IAttribute<Object>> result = new LinkedList<IAttribute<Object>>();
		for (final IAttribute<Object> attribute : attributes) {
			if (filter.accept(attribute)) {
				result.add(attribute);
			}
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	public Object getValue(final int rowIndex, final int columnIndex) {
		final IAttribute<Object> attribute = getAttribute(columnIndex);
		final IBeanProxy<?> bean = getBean(rowIndex);
		if (bean != null && attribute != null) {
			return bean.getValue(attribute.getPropertyName());
		}
		else {
			return null;
		}
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
		return dataModel.getRowCount();
	}

	@Override
	public ArrayList<Integer> getSelection() {
		return tableModel.getSelection();
	}

	@Override
	public void setSelection(final Collection<Integer> selection) {
		tableModel.setSelection(selection);
	}

	@Override
	public void setSelectedBeans(final Collection<? extends IBeanProxy<BEAN_TYPE>> selectedBeans) {
		Assert.paramNotNull(selectedBeans, "selectedBeans");
		setSelectedBeans(new LinkedList<Integer>(), selectedBeans);
	}

	@Override
	public void addSelectedBeans(final Collection<? extends IBeanProxy<BEAN_TYPE>> selectedBeans) {
		addSelectedBeansImpl(selectedBeans);
	}

	private List<IBeanProxy<BEAN_TYPE>> addSelectedBeansImpl(final Collection<? extends IBeanProxy<BEAN_TYPE>> selectedBeans) {
		return setSelectedBeans(getSelection(), selectedBeans);
	}

	@SuppressWarnings("unchecked")
	private List<IBeanProxy<BEAN_TYPE>> setSelectedBeans(
		final List<Integer> currentSelection,
		final Collection<? extends IBeanProxy<BEAN_TYPE>> selectedBeans) {

		final Set<Integer> newSelection = new LinkedHashSet<Integer>(currentSelection);
		final List<IBeanProxy<BEAN_TYPE>> result = new LinkedList<IBeanProxy<BEAN_TYPE>>();

		boolean added = false;

		if (!EmptyCheck.isEmpty(selectedBeans)) {
			//use hash set instead of collection for faster access
			final Set<IBeanProxy<BEAN_TYPE>> selectedBeansSet;
			if (selectedBeans instanceof HashSet) {
				selectedBeansSet = (Set<IBeanProxy<BEAN_TYPE>>) selectedBeans;
			}
			else {
				selectedBeansSet = new HashSet<IBeanProxy<BEAN_TYPE>>(selectedBeans);
			}
			for (final Entry<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>> pageEntry : data.entrySet()) {
				final int pageIndex = pageEntry.getKey().intValue();
				final int pageStartIndex = pageIndex * pageSize;
				int relativeIndex = 0;
				for (final IBeanProxy<BEAN_TYPE> bean : pageEntry.getValue()) {
					if (bean != null && selectedBeansSet.contains(bean)) {
						added = newSelection.add(Integer.valueOf(pageStartIndex + relativeIndex)) || added;
						result.add(bean);
					}
					relativeIndex++;
				}
			}
			final int dataRowCount = dataModel.getDataRowCount();
			for (final IBeanProxy<BEAN_TYPE> bean : addedData) {
				int relativeIndex = 0;
				if (bean != null && selectedBeansSet.contains(bean)) {
					added = newSelection.add(Integer.valueOf(dataRowCount + relativeIndex)) || added;
					result.add(bean);
				}
				relativeIndex++;
			}
		}
		if (added) {
			setSelection(newSelection);
		}

		return result;
	}

	@Override
	public List<IBeanProxy<BEAN_TYPE>> getSelectedBeans() {
		final List<IBeanProxy<BEAN_TYPE>> result = new LinkedList<IBeanProxy<BEAN_TYPE>>();
		for (final Integer selectionIndex : getSelection()) {
			result.add(getBean(selectionIndex.intValue()));
		}
		return Collections.unmodifiableList(result);
	}

	private List<IBeanProxy<BEAN_TYPE>> removeSelection() {
		final List<IBeanProxy<BEAN_TYPE>> result = new LinkedList<IBeanProxy<BEAN_TYPE>>();

		final List<Integer> currentSelection = getSelection();

		if (!currentSelection.isEmpty()) {
			for (final Integer index : currentSelection) {
				final int selectedIndex = index.intValue();
				final IBeanProxy<BEAN_TYPE> selectedBean = getBean(selectedIndex);
				if (selectedBean != null && !selectedBean.isDummy()) {
					result.add(selectedBean);
				}
			}
			dataModel.removeDataModelListener(tableDataModelListener);
			final List<Integer> emptySelection = Collections.emptyList();
			setSelection(emptySelection);
			dataModel.addDataModelListener(tableDataModelListener);
			beanListModelObservable.fireSelectionChanged();
		}

		return result;
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

	private int getPage(final int rowIndex) {
		return rowIndex / pageSize;
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

	private List<? extends IBeanKey> getParentBeanKeys() {
		if (parent == null || EmptyCheck.isEmpty(parent.getSelection())) {
			return null;
		}
		final List<IBeanKey> beanKeys = new LinkedList<IBeanKey>();
		for (final int i : parent.getSelection()) {
			final IBeanProxy<?> proxy = parent.getBean(i);
			if (proxy != null && !proxy.isDummy() && !proxy.isTransient()) {
				beanKeys.add(new BeanKey(proxy.getId(), proxy.getVersion()));
			}
		}
		if (!beanKeys.isEmpty() && linkType == LinkType.SELECTION_FIRST) {
			return beanKeys.subList(0, 1);
		}
		return beanKeys;
	}

	private class DataModel extends AbstractTableDataModel {

		@Override
		public int getRowCount() {
			return getDataRowCount() + addedData.size();
		}

		private int getDataRowCount() {
			if (countedRowCount != null) {
				return Math.max(countedRowCount.intValue(), rowCount);
			}
			else {
				return rowCount;
			}
		}

		@Override
		public ITableCell getCell(final int rowIndex, final int columnIndex) {
			if (dataCleared) {
				return new TableCellBuilder().build();
			}

			if (countLoader != null && !countLoader.isStarted() && !countLoader.isDisposed()) {
				countLoader.loadCount();
			}

			final IAttribute<Object> attribute = attributes.get(columnIndex);
			final int pageIndex = getPage(rowIndex);
			final ArrayList<IBeanProxy<BEAN_TYPE>> page = data.get(Integer.valueOf(pageIndex));

			if ((countedRowCount != null && rowIndex >= countedRowCount) && rowIndex >= rowCount) {
				final IBeanProxy<BEAN_TYPE> bean = getBean(rowIndex);
				if (bean == null) {
					//TODO MG this should not happen, if happens the the addedData array might be fixed
					//CHECKSTYLE:OFF
					System.out.println("Uup's, added data might be inconsistent");
					//CHECKSTYLE:ON
					return new TableCellBuilder().build();
				}
				else {
					return createAddedBeanCell(rowIndex, columnIndex, bean);
				}
			}
			else if (page == null) {
				loadEvenOddPage(pageIndex);
				return createDummyCell(rowIndex, columnIndex, null, attribute);
			}
			else {
				final IBeanProxy<BEAN_TYPE> bean = getBean(rowIndex);
				if (bean == null) {
					final PageLoader loadingPageLoader = getLoadingPageLoader(pageIndex);
					if (loadingPageLoader != null && !loadingPageLoader.isDisposed()) {
						loadingPageLoader.fixPageSize();
					}
					else {
						completeEvenOddPage(pageIndex);
					}
					return createDummyCell(rowIndex, columnIndex, null, attribute);
				}
				else if (!bean.isDummy()) {
					return createCell(rowIndex, columnIndex, bean);
				}
				else {
					return createDummyCell(rowIndex, columnIndex, bean, attribute);
				}
			}
		}

		private IBeanProxy<BEAN_TYPE> getBean(final int rowIndex) {
			if ((countedRowCount != null && rowIndex >= countedRowCount) && rowIndex >= rowCount) {
				return getBeanFromAddedData(rowIndex);
			}
			else {
				return getBeanFromPages(rowIndex);
			}
		}

		private IBeanProxy<BEAN_TYPE> getBeanFromAddedData(final int rowIndex) {
			final int dataRowCount = getDataRowCount();
			final int createdIndex = rowIndex - dataRowCount;
			if (createdIndex < addedData.size()) {
				return addedData.get(createdIndex);
			}
			else {
				return null;
			}
		}

		private IBeanProxy<BEAN_TYPE> getBeanFromPages(final int rowIndex) {
			final int pageIndex = getPage(rowIndex);
			final ArrayList<IBeanProxy<BEAN_TYPE>> page = data.get(Integer.valueOf(pageIndex));
			final int startIndex = pageIndex * pageSize;
			final int index = rowIndex - startIndex;
			if (page != null && index >= 0 && index < page.size()) {
				return page.get(index);
			}
			else {
				return null;
			}
		}

		private ITableCell createCell(final int rowIndex, final int columnIndex, final IBeanProxy<BEAN_TYPE> bean) {
			return createCellBuilder(rowIndex, columnIndex, bean, false).build();
		}

		private ITableCell createAddedBeanCell(final int rowIndex, final int columnIndex, final IBeanProxy<BEAN_TYPE> bean) {
			return createCellBuilder(rowIndex, columnIndex, bean, true).build();
		}

		private ITableCellBuilder createCellBuilder(
			final int rowIndex,
			final int columnIndex,
			final IBeanProxy<BEAN_TYPE> bean,
			final boolean createdBean) {

			final IAttribute<Object> attribute = attributes.get(columnIndex);
			final IObjectLabelConverter<Object> converter = attribute.getCurrentControlPanel().getObjectLabelConverter();
			final Object value = bean.getValue(attribute.getPropertyName());

			final ITableCellBuilder cellBuilder = createDefaultCellBuilder(rowIndex, columnIndex);

			//set label
			cellBuilder.setText(getCellText(converter, value));
			cellBuilder.setToolTipText(getCellDecription(converter, value));
			cellBuilder.setIcon(getCellIcon(converter, value));

			//set style
			cellBuilder.setForegroundColor(getCellForegroundColor(bean, createdBean));
			cellBuilder.setMarkup(getCellMarkup(attribute, bean, createdBean));

			//set editable
			cellBuilder.setEditable(isCellEditable(bean, attribute));

			return cellBuilder;
		}

		private String getCellText(final IObjectLabelConverter<Object> converter, final Object value) {
			if (value instanceof Collection<?>) {
				final Collection<?> collection = (Collection<?>) value;
				final int collectionSize = collection.size();
				if (collectionSize > 0) {
					final Object firstElement = collection.iterator().next();
					if (collectionSize > 1) {
						return getCellText(converter, firstElement) + " [" + collectionSize + "]";
					}
					else {
						return getCellText(converter, firstElement);
					}
				}
				else {
					return null;
				}
			}
			else {
				return converter.convertToString(value);
			}
		}

		private String getCellDecription(final IObjectLabelConverter<Object> converter, final Object value) {
			if (value instanceof Collection<?>) {
				final Collection<?> collection = (Collection<?>) value;
				if (!EmptyCheck.isEmpty(collection)) {
					return getCellDecription(converter, collection.iterator().next());
				}
				else {
					return null;
				}
			}
			else {
				return converter.getDescription(value);
			}
		}

		private IImageConstant getCellIcon(final IObjectLabelConverter<Object> converter, final Object value) {
			if (value instanceof Collection<?>) {
				final Collection<?> collection = (Collection<?>) value;
				if (!EmptyCheck.isEmpty(collection)) {
					return getCellIcon(converter, collection.iterator().next());
				}
				else {
					return null;
				}
			}
			else {
				return converter.getIcon(value);
			}
		}

		private IColorConstant getCellForegroundColor(final IBeanProxy<BEAN_TYPE> bean, final boolean createdBean) {
			final IBeanMessage message = bean.getFirstWorstMessage();
			if (bean.hasExecution()) {
				return Colors.DISABLED;
			}
			else if (message != null && (message.getType() == BeanMessageType.ERROR)) {
				return Colors.ERROR;
			}
			else if (message != null && (message.getType() == BeanMessageType.WARNING)) {
				return Colors.WARNING;
			}
			else if (bean.isTransient() || createdBean) {
				return CapColors.TRANSIENT_BEAN;
			}
			else if (bean.hasModifications()) {
				return Colors.STRONG;
			}
			else {
				return null;
			}
		}

		private Markup getCellMarkup(
			final IAttribute<Object> attribute,
			final IBeanProxy<BEAN_TYPE> bean,
			final boolean createdBean) {
			if (bean.hasModifications()) {
				if (bean.isModified(attribute.getPropertyName())) {
					return Markup.STRONG;
				}
				else {
					return Markup.EMPHASIZED;
				}
			}
			return null;
		}

		private boolean isCellEditable(final IBeanProxy<BEAN_TYPE> bean, final IAttribute<Object> attribute) {
			boolean result = attribute.isEditable();
			result = result && !attribute.isCollectionType();
			result = result && !bean.hasExecution();
			result = result && attribute.getCurrentControlPanel().getStringObjectConverter() != null;
			return result;
		}

		private ITableCell createDummyCell(
			final int rowIndex,
			final int columnIndex,
			final IBeanProxy<BEAN_TYPE> bean,
			final IAttribute<Object> attribute) {

			final ITableCellBuilder cellBuilder;
			final boolean hasMessages = bean != null && !EmptyCheck.isEmpty(bean.getMessages());

			if (bean != null && hasMessages && IBeanProxy.META_PROPERTY_MESSAGES.equals(attribute.getPropertyName())) {
				cellBuilder = createCellBuilder(rowIndex, columnIndex, bean, false);
			}
			else {
				cellBuilder = createDefaultCellBuilder(rowIndex, columnIndex);
				if (hasMessages) {
					final String message = bean.getFirstWorstMessage().getMessage();
					cellBuilder.setText("---").setToolTipText(message);
					cellBuilder.setForegroundColor(getCellForegroundColor(bean, false));
				}
				else {
					cellBuilder.setText("...").setToolTipText("Data will be loaded in background");
					cellBuilder.setForegroundColor(Colors.DISABLED);
				}
			}
			cellBuilder.setEditable(false);
			return cellBuilder.build();
		}

		private ITableCellBuilder createDefaultCellBuilder(final int rowIndex, final int columnIndex) {
			final ITableCellBuilder cellBuilder = new TableCellBuilder();
			if (rowIndex % 2 == 0) {
				cellBuilder.setBackgroundColor(Colors.DEFAULT_TABLE_EVEN_BACKGROUND_COLOR);
			}
			return cellBuilder;
		}
	}

	private class TableDataModelListener extends TableDataModelAdapter {
		@Override
		public void selectionChanged() {
			//clear the last selected beans if user changes selection itself
			//or on programmatic selection change (except changes from PageLoader)
			lastSelectedBeans.clear();
			beanListModelObservable.fireSelectionChanged();
		}
	}

	private class ParentModelListener implements IBeanListModelListener {
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
					LISTENER_DELAY,
					TimeUnit.MILLISECONDS);
		}
	}

	private class SortModelChangeListener implements IChangeListener {
		@Override
		public void changed() {
			updateColumnModel();
			load();
		}
	}

	private final class AttributeChangeListener implements IChangeListener {

		private final IAttribute<Object> attribute;
		private final IDefaultTableColumn column;
		private final IChangeListener columnChangeListener;

		private AttributeChangeListener(final IAttribute<Object> attribute, final int columnIndex) {
			this.attribute = attribute;
			this.column = columnModel.getColumn(columnIndex);
			this.columnChangeListener = new IChangeListener() {
				@Override
				public void changed() {
					if (column.getWidth() != attribute.getTableWidth()) {
						attribute.removeChangeListener(this);
						attribute.setTableWidth(column.getWidth());
						attribute.addChangeListener(this);
					}
				}
			};
			column.addChangeListener(columnChangeListener);
		}

		@Override
		public void changed() {
			column.removeChangeListener(columnChangeListener);
			column.setVisible(attribute.isVisible());
			column.setAlignment(attribute.getTableAlignment());
			if (!onSetConfig) {
				dataModel.fireDataChanged();
			}
			updateColumnModel();
			column.addChangeListener(columnChangeListener);
		}

		void dispose() {
			attribute.removeChangeListener(this);
			column.removeChangeListener(columnChangeListener);
		}
	}

	private final class AttributeLookUpListener implements ILookUpListener {

		private final IAttribute<Object> attribute;

		private boolean disposed;

		private AttributeLookUpListener(final IAttribute<Object> attribute) {
			this.attribute = attribute;
			this.disposed = false;
		}

		@Override
		public void taskCreated(final IExecutionTask task) {}

		@Override
		public void afterLookUpChanged() {
			if (!disposed && attribute.isVisible()) {
				dataModel.fireDataChanged();
			}
		}

		private void dispose() {
			this.disposed = true;
		}
	}

	private class CountLoader {
		private final IUiThreadAccess uiThreadAccess;
		private final IFilter filter;

		private Object parameter;
		private boolean canceled;
		private boolean finished;
		private boolean started;
		private IExecutionTask executionTask;

		CountLoader() {
			this.uiThreadAccess = Toolkit.getUiThreadAccess();
			this.filter = getFilter();
			this.canceled = false;
			this.finished = false;
			this.started = false;
		}

		void loadCount() {
			started = true;
			if (autoRowCount) {
				this.parameter = readerParameterProvider.getParameter();
				executionTask = CapUiToolkit.executionTaskFactory().create();
				readerService.count(createResultCallback(), getParentBeanKeys(), filter, parameter, executionTask);
			}
		}

		private boolean isStarted() {
			return started;
		}

		private IResultCallback<Integer> createResultCallback() {
			return new IResultCallback<Integer>() {

				@Override
				public void finished(final Integer result) {
					setResultLater(result);
				}

				@Override
				public void exception(final Throwable exception) {
					setExceptionLater(exception);
				}

				@Override
				public void timeout() {
					exception(new TimeoutException("Timeout while reading table count"));
				}
			};
		}

		private void setResultLater(final Integer result) {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					setResult(result);
				}
			});
		}

		private void setResult(final Integer result) {
			executionTask = null;
			finished = true;
			countedRowCount = result;
			fixLoadingPages();
			fireBeansChanged();
		}

		private void fixLoadingPages() {
			fixLoadingPage(evenPageLoader);
			fixLoadingPage(oddPageLoader);
			for (final PageLoader pageLoader : programmaticPageLoader.values()) {
				fixLoadingPage(pageLoader);
			}

		}

		private void fixLoadingPage(final PageLoader pageLoader) {
			if (pageLoader != null && !pageLoader.isDisposed()) {
				pageLoader.fixPageSize();
			}
		}

		private void setExceptionLater(final Throwable exception) {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					setException(exception);
				}
			});
		}

		private void setException(final Throwable exception) {
			executionTask = null;
			finished = true;
			countedRowCount = null;

			//TODO MG exception handling
			//CHECKSTYLE:OFF
			exception.printStackTrace();
			//CHECKSTYLE:ON

			fireBeansChanged();
		}

		boolean isDisposed() {
			return canceled || finished;
		}

		void cancel() {
			if (!canceled) {
				canceled = true;
				if (executionTask != null) {
					executionTask.cancel();
				}
			}
		}
	}

	private class PageLoader {

		private final int pageIndex;
		private final IUiThreadAccess uiThreadAccess;
		private final IFilter filter;
		private final IResultCallback<Void> resultCallback;

		private Object parameter;
		private boolean canceled;
		private boolean finished;
		private boolean pageRemoved;
		private ArrayList<IBeanProxy<BEAN_TYPE>> page;
		private int offset;
		private IExecutionTask executionTask;
		private IBeanProxy<BEAN_TYPE> dummyBeanProxy;
		private ScheduledFuture<?> schedule;

		PageLoader(final int pageIndex, final IResultCallback<Void> resultCallback) {
			this.pageIndex = pageIndex;
			this.uiThreadAccess = Toolkit.getUiThreadAccess();
			this.filter = getFilter();
			this.resultCallback = resultCallback;
		}

		public void fixPageSize() {
			final int dataModelRowCount = dataModel.getDataRowCount();
			while (page.size() < pageSize && dataModelRowCount > pageIndex * pageSize + page.size()) {
				page.add(dummyBeanProxy);
			}
		}

		void completePage() {
			page = data.get(Integer.valueOf(pageIndex));
			offset = getPageOffset();
			startPageLoading();
		}

		private int getPageOffset() {
			if (page != null) {
				int result = 0;
				for (final IBeanProxy<BEAN_TYPE> bean : page) {
					if (bean == null) {
						return result;
					}
					result++;
				}
			}
			return 0;
		}

		void loadPage() {
			page = new ArrayList<IBeanProxy<BEAN_TYPE>>();
			data.put(Integer.valueOf(pageIndex), page);
			startPageLoading();
		}

		void startPageLoading() {

			dummyBeanProxy = beanProxyFactory.createProxy(new DummyBeanDto(), propertyNames);
			dummyBeanProxy.setDummy(true);
			executionTask = CapUiToolkit.executionTaskFactory().create();
			executionTask.addExecutionCallbackListener(new IExecutionCallbackListener() {
				@Override
				public void canceled() {
					if (!canceled) {//if canceled by user
						userCanceledLater();
					}
				}
			});

			final int dataModelRowCount = dataModel.getDataRowCount();
			int count = offset;
			if (pageIndex > 0 || pageIndex < getPage(dataModelRowCount)) {
				for (int i = offset; i < pageSize; i++) {
					page.add(dummyBeanProxy);
				}
				count++;
			}
			else {
				page.add(dummyBeanProxy);
				count++;
				while (page.size() < pageSize && dataModelRowCount > pageIndex * pageSize + page.size()) {
					page.add(dummyBeanProxy);
					count++;
				}
			}

			rowCount = Math.max(rowCount, count);

			this.parameter = readerParameterProvider.getParameter();

			dummyBeanProxy.setExecutionTask(executionTask);
			beansStateTracker.register(dummyBeanProxy);

			dataModel.fireDataChanged();
			beanListModelObservable.fireBeansChanged();

			if (scheduledLoadDelay != null) {
				readDataFromServiceScheduled(scheduledLoadDelay.intValue());
				scheduledLoadDelay = null;
			}
			else if (pageIndex == 0 || pageIndex == getPage(rowCount)) {
				readDataFromService();
			}
			else {
				readDataFromServiceScheduled(INNER_PAGE_LOAD_DELAY);
			}
		}

		void readDataFromServiceScheduled(final int dealy) {
			final Runnable runnable = new Runnable() {
				@Override
				public void run() {
					uiThreadAccess.invokeLater(new Runnable() {
						@Override
						public void run() {
							schedule = null;
							readDataFromService();
						}
					});
				}
			};
			schedule = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory()).schedule(
					runnable,
					dealy,
					TimeUnit.MILLISECONDS);
		}

		void readDataFromService() {
			readerService.read(
					createResultCallback(),
					getParentBeanKeys(),
					filter,
					sortModel.getSorting(),
					(pageIndex * pageSize) + offset,
					pageSize - offset + 1,
					parameter,
					executionTask);
		}

		boolean isDisposed() {
			return canceled || finished;
		}

		void cancel() {
			if (!canceled) {
				this.canceled = true;
				if (schedule != null) {
					schedule.cancel(false);
				}
				if (executionTask != null) {
					executionTask.cancel();
				}
				removePage();
			}
		}

		private IResultCallback<List<IBeanDto>> createResultCallback() {
			return new IResultCallback<List<IBeanDto>>() {

				@Override
				public void finished(final List<IBeanDto> beanDtos) {
					if (!canceled && !executionTask.isCanceled()) {
						setResultLater(beanDtos);
					}
				}

				@Override
				public void timeout() {
					exception(new TimeoutException("Timeout while loading data"));
				}

				@Override
				public void exception(final Throwable exception) {
					setExceptionLater(exception);
				}

			};
		}

		private void setResultLater(final List<IBeanDto> beanDtos) {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					setResult(beanDtos);
				}
			});
		}

		private void setResult(final List<IBeanDto> beanDtos) {
			if (beanDtos.size() == 0 && pageIndex > 0) {
				rowCount = 0;
				countedRowCount = null;
				load();
				return;
			}
			else if (beanDtos.size() > pageSize && pageIndex >= maxPageIndex) {
				rowCount = Math.max(rowCount, ((pageIndex + 1) * pageSize + pageSize - 1));
				maxPageIndex = pageIndex;
			}
			else if (pageIndex >= maxPageIndex) {
				rowCount = (pageIndex * pageSize + beanDtos.size());
				maxPageIndex = pageIndex;
				if (countedRowCount != null && beanDtos.size() < pageSize) {
					countedRowCount = Integer.valueOf(rowCount);
				}
			}

			if (countedRowCount != null && rowCount > countedRowCount.intValue()) {
				tryToCancelCountLoader();
				countLoader = new CountLoader();
				countLoader.loadCount();
			}

			if (offset > 0) {
				while (page.size() > offset) {
					page.remove(page.size() - 1);
				}
			}
			else {
				page.clear();
			}
			int index = offset;
			final int pageOffset = pageSize * pageIndex;
			for (final IBeanDto beanDto : beanDtos) {
				if (index < pageSize) {
					final IBeanProxy<BEAN_TYPE> beanProxy = beanProxyFactory.createProxy(beanDto, propertyNames);
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
			beanListModelObservable.fireBeansChanged();

			programmaticPageLoader.remove(pageIndex);

			finished = true;

			if (!lastSelectedBeans.isEmpty()) {
				dataModel.removeDataModelListener(tableDataModelListener);
				final List<IBeanProxy<BEAN_TYPE>> addedBeans = addSelectedBeansImpl(lastSelectedBeans);
				dataModel.addDataModelListener(tableDataModelListener);
				if (!addedBeans.isEmpty()) {
					lastSelectedBeans.removeAll(addedBeans);
					beanListModelObservable.fireSelectionChanged();
				}
				else {
					tryAutoSelectFirst();
				}
			}
			else {
				tryAutoSelectFirst();
			}

			doCallbackSuccess();
		}

		private void tryAutoSelectFirst() {
			if (autoSelection && pageIndex == 0 && rowCount > 0 && getSelection().isEmpty()) {
				setSelection(Collections.singletonList(Integer.valueOf(0)));
			}
		}

		private void setExceptionLater(final Throwable exception) {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					setException(exception);
				}
			});
		}

		private void setException(final Throwable exception) {
			dummyBeanProxy.setExecutionTask(null);
			final IBeanMessageBuilder beanMessageBuilder = CapUiToolkit.beanMessageBuilder(BeanMessageType.ERROR);
			beanMessageBuilder.setException(exception);
			beanMessageBuilder.setMessage(loadErrorMessage);
			dummyBeanProxy.addMessage(beanMessageBuilder.build());
			programmaticPageLoader.remove(pageIndex);
			finished = true;
			dataModel.fireDataChanged();
			doCallbackError(exception);
		}

		private void removePage() {
			if (!pageRemoved) {
				dummyBeanProxy.setExecutionTask(null);
				beansStateTracker.unregister(dummyBeanProxy);
				data.remove(Integer.valueOf(pageIndex));
				programmaticPageLoader.remove(pageIndex);
				pageRemoved = true;
				finished = true;
				dataModel.fireDataChanged();
			}
		}

		private void userCanceledLater() {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					userCanceled();
				}
			});
		}

		private void userCanceled() {
			dummyBeanProxy.setExecutionTask(null);
			final IBeanMessageBuilder beanMessageBuilder = CapUiToolkit.beanMessageBuilder(BeanMessageType.WARNING);
			beanMessageBuilder.setMessage(userCanceledMessage);
			dummyBeanProxy.addMessage(beanMessageBuilder.build());
			programmaticPageLoader.remove(pageIndex);
			finished = true;
			dataModel.fireDataChanged();
		}

		void doCallbackSuccess() {
			if (resultCallback != null) {
				resultCallback.finished(null);
			}
		}

		void doCallbackError(final Throwable exception) {
			if (resultCallback != null) {
				resultCallback.exception(exception);
			}
		}
	}

	private static class DummyBeanDto implements IBeanDto {

		@Override
		public Object getValue(final String propertyName) {
			return null;
		}

		@Override
		public Object getId() {
			return DUMMY_ID;
		}

		@Override
		public long getVersion() {
			return 0;
		}

	}

	private static class DummyPageLoadResultCallback implements IResultCallback<Void> {

		@Override
		public void finished(final Void result) {}

		@Override
		public void exception(final Throwable exception) {}

		@Override
		public void timeout() {}

	}

}
