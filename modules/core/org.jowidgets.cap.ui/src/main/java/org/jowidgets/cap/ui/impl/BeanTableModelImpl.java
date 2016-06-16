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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jowidgets.api.color.Colors;
import org.jowidgets.api.command.IEnabledChecker;
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
import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.bean.IBeanModification;
import org.jowidgets.cap.common.api.exception.ServiceCanceledException;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.common.api.filter.BooleanOperator;
import org.jowidgets.cap.common.api.filter.IArithmeticFilterBuilder;
import org.jowidgets.cap.common.api.filter.IBooleanFilterBuilder;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.lookup.ILookUpValueRange;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.sort.SortOrder;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.common.tools.bean.BeanKey;
import org.jowidgets.cap.common.tools.execution.ResultCallbackAdapter;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.AttributeSet;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifierBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeConfig;
import org.jowidgets.cap.ui.api.attribute.IAttributeFilter;
import org.jowidgets.cap.ui.api.attribute.IAttributeSet;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanMessageBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyContext;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanSelection;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionEvent;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.bean.IBeansStateTracker;
import org.jowidgets.cap.ui.api.color.CapColors;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.filter.IUiBooleanFilter;
import org.jowidgets.cap.ui.api.filter.IUiFilter;
import org.jowidgets.cap.ui.api.filter.IUiFilterFactory;
import org.jowidgets.cap.ui.api.filter.IUiFilterTools;
import org.jowidgets.cap.ui.api.lookup.ILookUpAccess;
import org.jowidgets.cap.ui.api.lookup.ILookUpListener;
import org.jowidgets.cap.ui.api.model.DataModelContext;
import org.jowidgets.cap.ui.api.model.IBeanListModelBeansListener;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.model.IDataModelContext;
import org.jowidgets.cap.ui.api.model.IModificationStateListener;
import org.jowidgets.cap.ui.api.model.IProcessStateListener;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.cap.ui.api.plugin.IAttributePlugin;
import org.jowidgets.cap.ui.api.plugin.IBeanTableModelPlugin;
import org.jowidgets.cap.ui.api.sort.IPropertySort;
import org.jowidgets.cap.ui.api.sort.ISortModel;
import org.jowidgets.cap.ui.api.sort.ISortModelConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableCellRenderer;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModelConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableModelConfigBuilder;
import org.jowidgets.cap.ui.api.table.IExternalReader;
import org.jowidgets.cap.ui.tools.bean.BeanSelectionImpl;
import org.jowidgets.cap.ui.tools.bean.BeanSelectionObservable;
import org.jowidgets.cap.ui.tools.execution.AbstractUiExecutionCallbackListener;
import org.jowidgets.cap.ui.tools.execution.AbstractUiResultCallback;
import org.jowidgets.common.color.IColorConstant;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.model.ITableCell;
import org.jowidgets.common.types.Markup;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.plugin.api.PluginToolkit;
import org.jowidgets.tools.controller.TableDataModelAdapter;
import org.jowidgets.tools.model.table.AbstractTableDataModel;
import org.jowidgets.tools.model.table.DefaultTableColumnBuilder;
import org.jowidgets.tools.model.table.TableCellBluePrint;
import org.jowidgets.tools.model.table.TableCellBuilder;
import org.jowidgets.tools.model.table.TableModel;
import org.jowidgets.util.Assert;
import org.jowidgets.util.CollectionUtils;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.EmptyCompatibleEquivalence;
import org.jowidgets.util.IProvider;
import org.jowidgets.util.Interval;
import org.jowidgets.util.NullCompatibleEquivalence;
import org.jowidgets.util.ValueHolder;
import org.jowidgets.util.concurrent.DaemonThreadFactory;
import org.jowidgets.util.event.ChangeObservable;
import org.jowidgets.util.event.IChangeListener;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;

final class BeanTableModelImpl<BEAN_TYPE> implements IBeanTableModel<BEAN_TYPE> {

	private static final ILogger LOGGER = LoggerProvider.get(BeanTableModelImpl.class);

	private static final IMessage USER_CANCELED = Messages.getMessage("BeanTableModelImpl.user_canceled");
	private static final IMessage LOAD_ERROR = Messages.getMessage("BeanTableModelImpl.load_error");
	private static final IMessage LOADING_DATA = Messages.getMessage("BeanTableModelImpl.load_data");
	private static final IMessage AUTO_REFRESH_HEADER = Messages.getMessage("BeanTableModelImpl.auto_refresh_header");
	private static final IMessage AUTO_REFRESH_TEXT = Messages.getMessage("BeanTableModelImpl.auto_refresh_text");

	private static final int INNER_PAGE_LOAD_DELAY = 100;
	private static final int AUTO_REFRESH_DELAY = 250;
	private static final int PAGE_LOAD_OVERLAP = 25;

	private static final IResultCallback<Void> DUMMY_PAGE_LOAD_RESULT_CALLBACK = new ResultCallbackAdapter<Void>();

	private final Object entityId;
	private final String labelSingular;
	private final String labelPlural;
	private final Object beanTypeId;
	private final Class<BEAN_TYPE> beanType;

	private final Map<String, IUiFilter> filters;
	private final ChangeObservable filterChangeObservable;
	private final ISortModel sortModel;

	private final int pageSize;
	private final Map<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>> data;
	private final ArrayList<IBeanProxy<BEAN_TYPE>> addedData;
	private final Set<IBeanProxy<BEAN_TYPE>> lastSelectedBeans;

	private final IBeanProxyFactory<BEAN_TYPE> beanProxyFactory;
	private final IBeansStateTracker<BEAN_TYPE> beansStateTracker;
	private final IBeanProxyContext beanProxyContext;
	private final IAttributeSet attributeSet;
	private final Map<String, Object> defaultValues;
	private final List<IBeanPropertyValidator<BEAN_TYPE>> beanPropertyValidators;
	private final List<IBeanPropertyValidator<BEAN_TYPE>> beanPropertyValidatorsView;

	private final ICreatorService creatorService;
	private final IUpdaterService updaterService;
	private final IReaderService<Object> readerService;
	private final CachedBeanListReaderService<Object, BEAN_TYPE> cachedReaderService;
	private final IRefreshService refreshService;
	private final IProvider<Object> readerParameterProvider;
	private final IDeleterService deleterService;
	private final BeanListSaveDelegate<BEAN_TYPE> saveDelegate;
	private final BeanListRefreshDelegate<BEAN_TYPE> refreshDelegate;

	private final IBeanSelectionProvider<Object> parent;
	private final LinkType linkType;

	private final boolean validateUnmodifiedBeans;

	private final boolean clearOnEmptyFilter;
	private final boolean clearOnEmptyParentBeans;
	private final boolean autoRefreshSelection;
	private final boolean autoDisposeInvisiblePages;

	private final BeanListModelObservable<BEAN_TYPE> beanListModelObservable;
	private final BeanSelectionObservable<BEAN_TYPE> beanSelectionObservable;
	private final DisposeObservable disposeObservable;
	private final TableDataModelListener tableDataModelListener;
	private final IChangeListener sortModelChangeListener;
	private final ParentSelectionListener<Object> parentSelectionListener;
	private final ParentSelectionAddabledChecker parentSelectionAddabledChecker;
	private final List<AttributeChangeListener> attributeChangeListeners;

	private final IDefaultTableColumnModel columnModel;
	private final DataModel dataModel;
	private final ITableModel tableModel;
	private final IDataModelContext dataModelContext;

	private final Set<AttributeLookUpListener> lookUpListenersStrongRef;
	private final Map<Integer, PageLoader> programmaticPageLoader;

	private final IBeanExceptionConverter exceptionConverter;

	private IBeanProxy<BEAN_TYPE> lastBean;
	private boolean useLastModificationAsDefault;

	private ScheduledExecutorService scheduledExecutorService;
	private PageLoader evenPageLoader;
	private PageLoader oddPageLoader;
	private CountLoader countLoader;
	private BackgroundPageLoader backgroundPageLoader;
	private IExecutionTask autoRefreshExecutionTask;

	private int rowCount;
	private Integer countedRowCount;
	private boolean autoRowCount;
	private int maxPageIndex;
	private boolean dataCleared;
	private boolean autoSelection;
	private boolean onSetConfig;
	private Integer scheduledLoadDelay;
	private boolean disposed;
	private boolean lastBeanEnabled;

	@SuppressWarnings("unchecked")
	BeanTableModelImpl(
		final Object entityId,
		final Object beanTypeId,
		final Class<? extends BEAN_TYPE> beanType,
		final Set<IBeanValidator<BEAN_TYPE>> beanValidators,
		final boolean validateUnmodifiedBeans,
		final String labelSingular,
		final String labelPlural,
		final List<IAttribute<Object>> attributes,
		final ISortModelConfig sortModelConfig,
		final IReaderService<? extends Object> readerService,
		final IProvider<? extends Object> paramProvider,
		final ICreatorService creatorService,
		final IRefreshService refreshService,
		final IUpdaterService updaterService,
		final IDeleterService deleterService,
		final IBeanExceptionConverter exceptionConverter,
		final IBeanSelectionProvider<Object> parent,
		final LinkType linkType,
		final Long listenerDelay,
		final boolean autoRowCount,
		final boolean autoSelect,
		final boolean autoRefreshSelection,
		final boolean autoDisposeInvisiblePages,
		final boolean clearOnEmptyFilter,
		final boolean clearOnEmptyParentBeans,
		final boolean lastBeanEnabled,
		final boolean useLastModificationAsDefault,
		final BeanExecutionPolicy saveExecutionPolicy,
		final int pageSize,
		final IBeanProxyContext beanProxyContext,
		final IDataModelContext dataModelContext,
		final List<IBeanTableCellRenderer<BEAN_TYPE>> cellRenderers) {

		//arguments checks
		Assert.paramNotNull(entityId, "entityId");
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotEmpty(labelSingular, "labelSingular");
		Assert.paramNotEmpty(labelPlural, "labelPlural");
		Assert.paramNotNull(attributes, "attributes");
		Assert.paramNotNull(readerService, "readerService");
		Assert.paramNotNull(paramProvider, "paramProvider");
		Assert.paramNotNull(saveExecutionPolicy, "saveExecutionPolicy");
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");
		Assert.paramNotNull(beanProxyContext, "beanProxyContext");

		this.parent = parent;
		this.entityId = entityId;
		this.validateUnmodifiedBeans = validateUnmodifiedBeans;
		this.autoRowCount = autoRowCount;
		this.clearOnEmptyFilter = clearOnEmptyFilter;
		this.clearOnEmptyParentBeans = clearOnEmptyParentBeans;
		this.autoDisposeInvisiblePages = autoDisposeInvisiblePages;
		this.beanType = (Class<BEAN_TYPE>) beanType;
		this.beanTypeId = beanTypeId;
		this.labelSingular = labelSingular;
		this.labelPlural = labelPlural;
		this.linkType = linkType;
		if (parent != null) {
			Assert.paramNotNull(linkType, "linkType");
			this.parentSelectionListener = new ParentSelectionListener<Object>(parent, this, listenerDelay);
			parent.addBeanSelectionListener(parentSelectionListener);
		}
		else {
			this.parentSelectionListener = null;
		}
		this.parentSelectionAddabledChecker = new ParentSelectionAddabledChecker(parent, linkType);
		parentSelectionAddabledChecker.addChangeListener(new IChangeListener() {
			@Override
			public void changed() {
				setLastBeanByParentState();
			}
		});

		//inject table model plugins
		List<IAttribute<Object>> modifiedAttributes = createModifiedByPluginsAttributes(
				entityId,
				(Class<BEAN_TYPE>) beanType,
				attributes);

		//if no updater service available, set all attributes to editable false
		if (updaterService == null && creatorService == null) {
			modifiedAttributes = createReadonlyAttributes(modifiedAttributes);
		}

		this.attributeSet = AttributeSet.create(modifiedAttributes);

		final List<String> mutablePropertyNames = new LinkedList<String>();
		this.defaultValues = new HashMap<String, Object>();
		for (final IAttribute<?> attribute : modifiedAttributes) {
			final String propertyName = attribute.getPropertyName();
			mutablePropertyNames.add(propertyName);
			final Object defaultValue = attribute.getDefaultValue();
			if (defaultValue != null) {
				defaultValues.put(propertyName, defaultValue);
			}
		}

		this.readerService = (IReaderService<Object>) readerService;
		this.cachedReaderService = new CachedBeanListReaderService<Object, BEAN_TYPE>(this.readerService, pageSize);
		this.refreshService = refreshService;
		this.readerParameterProvider = (IProvider<Object>) paramProvider;
		this.creatorService = creatorService;
		this.deleterService = deleterService;
		this.updaterService = updaterService;
		this.autoSelection = autoSelect;
		this.autoRefreshSelection = autoRefreshSelection;
		this.onSetConfig = false;
		this.exceptionConverter = exceptionConverter;
		this.useLastModificationAsDefault = useLastModificationAsDefault;

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
		this.pageSize = pageSize;
		this.rowCount = 0;
		this.maxPageIndex = 0;
		this.beanProxyContext = beanProxyContext;
		this.beansStateTracker = CapUiToolkit.beansStateTracker(beanProxyContext, validateUnmodifiedBeans);
		this.beanProxyFactory = CapUiToolkit.beanProxyFactory(beanTypeId, beanType, attributeSet);
		this.beanListModelObservable = new BeanListModelObservable<BEAN_TYPE>();
		this.beanSelectionObservable = new BeanSelectionObservable<BEAN_TYPE>();
		this.disposeObservable = new DisposeObservable();
		this.filterChangeObservable = new ChangeObservable();
		this.programmaticPageLoader = new HashMap<Integer, PageLoader>();

		this.beanPropertyValidators = new LinkedList<IBeanPropertyValidator<BEAN_TYPE>>();
		this.beanPropertyValidatorsView = Collections.unmodifiableList(beanPropertyValidators);
		beanPropertyValidators.add(new AttributesBeanPropertyValidator<BEAN_TYPE>(modifiedAttributes));
		for (final IBeanValidator<BEAN_TYPE> beanValidator : beanValidators) {
			addBeanValidator(beanValidator);
		}

		//configure sort model
		sortModel.setConfig(sortModelConfig);
		sortModel.addChangeListener(sortModelChangeListener);

		//model creation
		this.columnModel = createColumnModel(modifiedAttributes);
		this.dataModel = new DataModel(cellRenderers);
		dataModel.addDataModelListener(tableDataModelListener);
		this.tableModel = new TableModel(columnModel, dataModel);

		//add some listeners
		this.lookUpListenersStrongRef = new HashSet<AttributeLookUpListener>();
		addAttributeListeners(modifiedAttributes, lookUpListenersStrongRef);

		//update the columns
		updateColumnModel();

		final IProvider<List<IBeanKey>> parentBeansProvider = new IProvider<List<IBeanKey>>() {
			@Override
			public List<IBeanKey> get() {
				return getParentBeanKeys();
			}
		};

		this.saveDelegate = new BeanListSaveDelegate<BEAN_TYPE>(
			this,
			beansStateTracker,
			exceptionConverter,
			saveExecutionPolicy,
			updaterService,
			creatorService,
			this.attributeSet.getPropertyNames(),
			parentBeansProvider);

		this.refreshDelegate = new BeanListRefreshDelegate<BEAN_TYPE>(
			this,
			exceptionConverter,
			BeanExecutionPolicy.BATCH,
			refreshService);

		if (refreshService != null) {
			addBeanSelectionListener(new AutoRefreshListener());
		}

		beansStateTracker.addProcessStateListener(new IProcessStateListener() {
			@Override
			public void processStateChanged() {
				fireBeansChanged();
			}
		});

		setLastBeanEnabled(lastBeanEnabled);

		this.dataModelContext = dataModelContext != null ? dataModelContext : DataModelContext.create(this);
	}

	@Override
	public IDataModelContext getDataModelContext() {
		return dataModelContext;
	}

	@Override
	public void setUseLastModificationForDefault(final boolean useLastModificationForDefault) {
		this.useLastModificationAsDefault = useLastModificationForDefault;
	}

	@Override
	public boolean hasLastBean() {
		return lastBean != null;
	}

	@Override
	public boolean isLastBeanEnabled() {
		return lastBeanEnabled;
	}

	@Override
	public void setLastBeanEnabled(final boolean lastBeanEnabled) {
		this.lastBeanEnabled = lastBeanEnabled;
		setLastBeanByParentState();
	}

	@Override
	public IEnabledChecker getDataAddableChecker() {
		return parentSelectionAddabledChecker;
	}

	void setScheduledExecutorService(final ScheduledExecutorService scheduledExecutorService) {
		this.scheduledExecutorService = scheduledExecutorService;
	}

	private void setLastBeanByParentState() {
		if (lastBeanEnabled && parentSelectionAddabledChecker.getEnabledState().isEnabled()) {
			addLastBean();
		}
		else {
			removeLastBean();
		}
	}

	private void addLastBean() {
		if (lastBean == null) {
			lastBean = beanProxyFactory.createLastRowDummyProxy();
			lastBean.addBeanPropertyValidators(beanPropertyValidators);

			final ValueHolder<PropertyChangeListener> listenerHolder = new ValueHolder<PropertyChangeListener>();
			final PropertyChangeListener listener = new PropertyChangeListener() {
				@Override
				public void propertyChange(final PropertyChangeEvent evt) {
					if (lastBean.hasModifications()) {
						lastBean.removePropertyChangeListener(listenerHolder.get());
						for (final Entry<String, Object> entry : defaultValues.entrySet()) {
							final String propertyName = entry.getKey();
							if (!NullCompatibleEquivalence.equals(evt.getPropertyName(), propertyName)) {
								lastBean.setValue(propertyName, entry.getValue());
							}
						}
						final IBeanProxy<BEAN_TYPE> beanToAdd = lastBean;
						beanToAdd.clearLastRowDummyState();
						addBeanImpl(beanToAdd, false);
						lastBean = null;
						addLastBean();
					}
				}
			};
			listenerHolder.set(listener);
			lastBean.addPropertyChangeListener(listener);
			fireBeansChanged();
			fireSelectionChanged();
		}
	}

	private void removeLastBean() {
		if (lastBean != null) {
			lastBean = null;
			fireBeansChanged();
		}
	}

	private List<IAttribute<Object>> createModifiedByPluginsAttributes(
		final Object entityId,
		final Class<BEAN_TYPE> beanType,
		final List<IAttribute<Object>> attributes) {

		List<IAttribute<Object>> result = attributes;

		IPluginPropertiesBuilder propBuilder = PluginToolkit.pluginPropertiesBuilder();
		propBuilder.add(IAttributePlugin.ENTITIY_ID_PROPERTY_KEY, entityId);
		propBuilder.add(IAttributePlugin.BEAN_TYPE_PROPERTY_KEY, beanType);
		IPluginProperties properties = propBuilder.build();
		for (final IAttributePlugin plugin : PluginProvider.getPlugins(IAttributePlugin.ID, properties)) {
			result = plugin.modifyAttributes(properties, result);
		}

		propBuilder = PluginToolkit.pluginPropertiesBuilder();
		propBuilder.add(IBeanTableModelPlugin.ENTITIY_ID_PROPERTY_KEY, entityId);
		propBuilder.add(IBeanTableModelPlugin.BEAN_TYPE_PROPERTY_KEY, beanType);
		properties = propBuilder.build();
		for (final IBeanTableModelPlugin plugin : PluginProvider.getPlugins(IBeanTableModelPlugin.ID, properties)) {
			result = plugin.modifyTableAttributes(properties, result);
		}

		return result;
	}

	private static List<IAttribute<Object>> createReadonlyAttributes(final List<IAttribute<Object>> attributes) {
		final IAttributeToolkit attributeToolkit = CapUiToolkit.attributeToolkit();
		final IAttributeCollectionModifierBuilder modifierBuilder = attributeToolkit.createAttributeCollectionModifierBuilder();
		modifierBuilder.addDefaultModifier().setEditable(false);
		return CapUiToolkit.attributeToolkit().createAttributesCopy(attributes, modifierBuilder.build());
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

		for (final IAttribute<Object> attribute : attributes) {
			final IDefaultTableColumnBuilder columnBuilder = new DefaultTableColumnBuilder();
			columnBuilder.setText(attribute.getCurrentLabel());
			columnBuilder.setToolTipText(attribute.getDescription().get());
			columnBuilder.setWidth(attribute.getTableWidth());
			columnBuilder.setAlignment(attribute.getTableAlignment());
			columnBuilder.setVisible(attribute.isVisible());
			result.addColumn(columnBuilder.build());
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
			tryCancelAutoRefreshExecutionTask();
			tryToCancelLoader();
			disposeObservable.fireOnDispose();
			lastSelectedBeans.clear();
			data.clear();
			addedData.clear();
			beanListModelObservable.dispose();
			beanSelectionObservable.dispose();
			filterChangeObservable.dispose();
			beansStateTracker.dispose();
			for (final AttributeLookUpListener attributeLookUpListener : lookUpListenersStrongRef) {
				attributeLookUpListener.dispose();
			}
			lookUpListenersStrongRef.clear();
			sortModel.removeChangeListener(sortModelChangeListener);
			dataModel.removeDataModelListener(tableDataModelListener);
			if (parentSelectionListener != null && parent != null) {
				parent.removeBeanSelectionListener(parentSelectionListener);
			}
			parentSelectionAddabledChecker.dispose();
			for (final AttributeChangeListener listener : attributeChangeListeners) {
				listener.dispose();
			}
			disposed = true;
		}
	}

	private void tryCancelAutoRefreshExecutionTask() {
		if (autoRefreshExecutionTask != null && !autoRefreshExecutionTask.isCanceled()) {
			autoRefreshExecutionTask.cancel();
			autoRefreshExecutionTask = null;
		}
	}

	@Override
	public IBeanSelectionProvider<Object> getParent() {
		return parent;
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
	public Object getBeanTypeId() {
		return beanTypeId;
	}

	@Override
	public Class<BEAN_TYPE> getBeanType() {
		return beanType;
	}

	@Override
	public IBeanProxyContext getBeanProxyContext() {
		return beanProxyContext;
	}

	@Override
	public String getEntityLabelSingular() {
		return labelSingular;
	}

	@Override
	public String getEntityLabelPlural() {
		return labelPlural;
	}

	@Override
	public ICreatorService getCreatorService() {
		return creatorService;
	}

	@Override
	public IUpdaterService getUpdaterService() {
		return updaterService;
	}

	@Override
	public IDeleterService getDeleterService() {
		return deleterService;
	}

	@Override
	public IReaderService<Object> getReaderService() {
		//Do not return the null reader service. It is only used to make this
		//implementation more robust. The reader service set on this model is null
		if (readerService instanceof NullReaderService<?>) {
			return null;
		}
		else {
			return readerService;
		}
	}

	@Override
	public IExternalReader createExternalReader() {
		return new ExternalReader();
	}

	@Override
	public void clear() {
		if (!Toolkit.getUiThreadAccess().isUiThread()) {
			throw new IllegalStateException("Clear must be invoked in the ui thread");
		}

		tryToCancelLoader();
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
	public void clearCache() {
		cachedReaderService.clearCache();
	}

	@Override
	public boolean hasModificationsCached() {
		return cachedReaderService.isReadFromCachePossible(getFilter(), readerParameterProvider.get());
	}

	@Override
	public void load() {
		load(null);
	}

	@Override
	public void loadScheduled(final int delayMillis) {
		load(Integer.valueOf(delayMillis));
	}

	private void load(final Integer scheduledLoadDelay) {
		if (!Toolkit.getUiThreadAccess().isUiThread()) {
			throw new IllegalStateException("Load must be invoked in the ui thread");
		}
		if (clearOnEmptyFilter && isFilterEmpty() || clearOnEmptyParentBeans && EmptyCheck.isEmpty(getParentBeanKeys())) {
			clear();
		}
		else {
			tryToCancelLoader();
			lastSelectedBeans.addAll(removeSelection());
			beansStateTracker.clearAll();
			if (rowCount == 0) {
				rowCount = 1;
			}
			dataCleared = false;
			maxPageIndex = 0;
			data.clear();
			addedData.clear();
			this.scheduledLoadDelay = scheduledLoadDelay;
			countedRowCount = null;
			countLoader = new CountLoader();

			dataModel.fireDataChanged();
		}
	}

	private boolean isFilterEmpty() {
		for (final IUiFilter filter : filters.values()) {
			if (!isFilterEmpty(filter)) {
				return false;
			}
		}
		return true;
	}

	private boolean isFilterEmpty(final IUiFilter filter) {
		if (filter instanceof IUiBooleanFilter) {
			final IUiBooleanFilter booleanFilter = (IUiBooleanFilter) filter;
			for (final IUiFilter childFilter : booleanFilter.getFilters()) {
				if (!isFilterEmpty(childFilter)) {
					return false;
				}
			}
			return true;
		}
		else {
			return false;
		}
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
		if (backgroundPageLoader != null && backgroundPageLoader.isPageLoading(pageIndex)) {
			return true;
		}
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

	@Override
	public void updateInBackground(final Interval<Integer> visibleRows) {
		updateInBackground(new ResultCallbackAdapter<Void>(), visibleRows);

	}

	@Override
	public void updateInBackground(final IResultCallback<Void> resultCallback, final Interval<Integer> visibleRows) {
		final Integer left = visibleRows.getLeftBoundary();
		final Integer right = visibleRows.getRightBoundary();
		if (left != null && right != null) {
			final int startPageIndex = getPage(left.intValue());
			final int endPageIndex = getPage(right.intValue());
			for (int pageIndex = startPageIndex; pageIndex <= endPageIndex; pageIndex++) {
				if (isPageLoading(pageIndex)) {
					//there is some loading for the viewport, so do not update unloaded pages
					return;
				}
			}
			//if the background pageLoader loads something outside the viewport, cancel this
			tryToCancelBackgroundPageLoader();
			backgroundPageLoader = new BackgroundPageLoader(startPageIndex, endPageIndex, resultCallback);
			backgroundPageLoader.loadInBackground();
			startCountLoaderIfNotRunning();
		}
		else {
			if (rowCount == 0) {
				startCountLoaderIfNotRunning();
			}
		}
	}

	private void startCountLoaderIfNotRunning() {
		if (countLoader == null || !countLoader.started || countLoader.isDisposed()) {
			countLoader = new CountLoader();
			countLoader.loadCount();
		}
	}

	private void loadEvenOddPage(final int pageIndex) {
		final boolean even = pageIndex % 2 == 0;
		tryToCancelProgrammaticPageLoader(pageIndex);
		tryToCancelBackgroundPageLoader(pageIndex);
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
		autoDisposeInvisiblePages(pageIndex);
	}

	private void autoDisposeInvisiblePages(final int visiblePageIndex) {
		if (autoDisposeInvisiblePages) {
			int newMaxPage = 0;
			final Set<Integer> selectedPages = getSelectedPages();

			for (final Integer pageIndexW : new ArrayList<Integer>(data.keySet())) {
				final int pageIndex = pageIndexW.intValue();

				if (!selectedPages.contains(pageIndexW) && (pageIndex < visiblePageIndex - 1 || pageIndex > visiblePageIndex + 1)) {

					removePage(pageIndex);
				}

				else {
					newMaxPage = Math.max(newMaxPage, pageIndex);
				}
			}

			maxPageIndex = newMaxPage;
		}
	}

	private Set<Integer> getSelectedPages() {
		final Set<Integer> result = new HashSet<Integer>();
		for (final Integer selectedIndex : getSelection()) {
			result.add(getPage(selectedIndex));
		}
		return result;
	}

	private void completeEvenOddPage(final int pageIndex) {
		final boolean even = pageIndex % 2 == 0;
		tryToCancelProgrammaticPageLoader(pageIndex);
		tryToCancelBackgroundPageLoader(pageIndex);
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

	private void removePage(final int pageIndex) {
		tryToCancelPageLoader(pageIndex);
		final ArrayList<IBeanProxy<BEAN_TYPE>> pageToDelete = data.get(pageIndex);
		if (pageToDelete != null) {
			beansStateTracker.unregister(pageToDelete);
			pageToDelete.clear();
			data.remove(pageIndex);
		}
	}

	private void tryToCancelLoader() {
		tryToCancelBackgroundPageLoader();
		tryToCancelProgrammaticPageLoader();
		tryToCancelCountLoader();
		tryToCancelEvenOddPageLoader();
	}

	private void tryToCancelBackgroundPageLoader() {
		if (backgroundPageLoader != null && !backgroundPageLoader.isDisposed()) {
			backgroundPageLoader.cancel();
		}
	}

	private void tryToCancelBackgroundPageLoader(final int index) {
		if (backgroundPageLoader != null && backgroundPageLoader.isPageLoading(index)) {
			backgroundPageLoader.cancel();
		}
	}

	private void tryToCancelProgrammaticPageLoader() {
		for (final PageLoader pageLoader : programmaticPageLoader.values()) {
			tryToCancelPageLoader(pageLoader);
		}
		programmaticPageLoader.clear();
	}

	private void tryToCancelPageLoader(final int pageIndex) {
		tryToCancelBackgroundPageLoader(pageIndex);
		tryToCancelProgrammaticPageLoader(pageIndex);
		final boolean even = pageIndex % 2 == 0;
		if (even) {
			if (evenPageLoader != null && evenPageLoader.pageIndex == pageIndex) {
				tryToCancelPageLoader(evenPageLoader);
			}
		}
		else {
			if (oddPageLoader != null && oddPageLoader.pageIndex == pageIndex) {
				tryToCancelPageLoader(oddPageLoader);
			}
		}
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
		refreshDelegate.refresh(beans);
	}

	@Override
	public void save() {
		saveDelegate.save();
	}

	@Override
	public void undo() {
		for (final IBeanProxy<BEAN_TYPE> bean : new HashSet<IBeanProxy<BEAN_TYPE>>(beansStateTracker.getBeansToUpdate())) {
			bean.undoModifications();
		}
		if (lastBean != null) {
			lastBean.undoModifications();
		}
		final Set<IBeanProxy<BEAN_TYPE>> beansToCreate = beansStateTracker.getBeansToCreate();
		if (!beansToCreate.isEmpty()) {
			final List<IBeanProxy<BEAN_TYPE>> removedBeans = removeBeansImpl(beansToCreate, false);
			if (removedBeans.size() > 0) {
				beanListModelObservable.fireBeansRemoved(removedBeans);
				beanListModelObservable.fireBeansChanged();
			}
		}
		cachedReaderService.clearCache();
		beansStateTracker.clearModifications();
		dataModel.fireDataChanged();
	}

	@Override
	public IValidationResult validate() {
		return beansStateTracker.validate();
	}

	@Override
	public void addValidationConditionListener(final IValidationConditionListener listener) {
		beansStateTracker.addValidationConditionListener(listener);
	}

	@Override
	public void removeValidationConditionListener(final IValidationConditionListener listener) {
		beansStateTracker.removeValidationConditionListener(listener);
	}

	@Override
	public boolean hasModifications() {
		return beansStateTracker.hasModifications();
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
		tryToCancelCountLoader();
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
	public int getBeanIndex(final IBeanProxy<BEAN_TYPE> beanToFind) {
		Assert.paramNotNull(beanToFind, "beanToFind");
		for (final Entry<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>> entry : data.entrySet()) {
			final int page = entry.getKey().intValue();
			int elementIndex = 0;
			for (final IBeanProxy<BEAN_TYPE> bean : entry.getValue()) {
				if (beanToFind.equals(bean)) {
					return page * pageSize + elementIndex;
				}
				elementIndex++;
			}
		}
		for (final IBeanProxy<BEAN_TYPE> bean : addedData) {
			int elementIndex = 0;
			if (beanToFind.equals(bean)) {
				final int dataRowCount = dataModel.getDataRowCount();
				return dataRowCount + elementIndex;
			}
			elementIndex++;
		}
		return -1;
	}

	@Override
	public void addBean(final IBeanProxy<BEAN_TYPE> bean) {
		Assert.paramNotNull(bean, "bean");
		addBeanImpl(bean, true);
		beanListModelObservable.fireBeansAdded(bean);
		beanListModelObservable.fireBeansChanged();
	}

	private void addBeanImpl(final IBeanProxy<BEAN_TYPE> bean, final boolean fireBeansChanged) {
		addedData.add(bean);
		final int index = dataModel.getRowCount() - dataModel.getLastBeanCount() - 1;
		bean.addPropertyChangeListener(new BeanPropertyChangeListener(index));
		beansStateTracker.register(bean);
		cachedReaderService.addBean(getParentBeanKeys(), bean);
		if (fireBeansChanged) {
			fireBeansChanged();
		}
	}

	@Override
	public IBeanProxy<BEAN_TYPE> addBeanDto(final IBeanDto beanDto) {
		Assert.paramNotNull(beanDto, "beanDto");
		final IBeanProxy<BEAN_TYPE> addedBeanProxy = addBeanDtoImpl(beanDto, true);
		beanListModelObservable.fireBeansAdded(addedBeanProxy);
		beanListModelObservable.fireBeansChanged();
		return addedBeanProxy;
	}

	private IBeanProxy<BEAN_TYPE> addBeanDtoImpl(final IBeanDto beanDto, final boolean fireBeansChanged) {
		final IBeanProxy<BEAN_TYPE> result = createBeanProxy(beanDto);
		addBeanImpl(result, fireBeansChanged);
		return result;
	}

	@Override
	public void updateModel(final Collection<? extends IBeanDto> beansToRemove, final Collection<? extends IBeanDto> beansToAdd) {

		Assert.paramNotNull(beansToRemove, "beansToRemove");
		Assert.paramNotNull(beansToAdd, "beansToAdd");

		final List<IBeanProxy<BEAN_TYPE>> removedBeans = removeBeansImpl(beansToRemove, false);

		final List<IBeanProxy<BEAN_TYPE>> addedBeans = new LinkedList<IBeanProxy<BEAN_TYPE>>();
		for (final IBeanDto bean : beansToAdd) {
			addedBeans.add(addBeanDtoImpl(bean, false));
		}

		boolean changed = false;
		if (removedBeans.size() > 0) {
			changed = true;
			beanListModelObservable.fireBeansRemoved(removedBeans);
		}
		if (addedBeans.size() > 0) {
			changed = true;
			beanListModelObservable.fireBeansAdded(addedBeans);
		}
		if (changed) {
			beanListModelObservable.fireBeansChanged();
		}

		fireBeansChanged();
	}

	@Override
	public IBeanProxy<BEAN_TYPE> addTransientBean() {
		final IBeanProxy<BEAN_TYPE> result = beanProxyFactory.createTransientProxy(defaultValues);
		if (!EmptyCheck.isEmpty(beanPropertyValidators)) {
			result.addBeanPropertyValidators(beanPropertyValidators);
		}
		addBean(result);
		return result;
	}

	private IBeanProxy<BEAN_TYPE> createBeanProxy(final IBeanDto beanDto) {
		final IBeanProxy<BEAN_TYPE> beanProxy = beanProxyFactory.createProxy(beanDto);
		beanProxy.setValidateUnmodified(validateUnmodifiedBeans);
		if (!EmptyCheck.isEmpty(beanPropertyValidators)) {
			beanProxy.addBeanPropertyValidators(beanPropertyValidators);
		}
		return beanProxy;
	}

	@Override
	public void removeBeans(final Iterable<? extends IBeanProxy<BEAN_TYPE>> beans) {
		cachedReaderService.removeBeans(getParentBeanKeys(), beans);
		final List<IBeanProxy<BEAN_TYPE>> removedBeans = removeBeansImpl(beans, true);
		if (removedBeans.size() > 0) {
			beanListModelObservable.fireBeansRemoved(removedBeans);
			beanListModelObservable.fireBeansChanged();
		}
	}

	@Override
	public void removeAllBeans() {
		final List<IBeanProxy<BEAN_TYPE>> beansToRemove = new LinkedList<IBeanProxy<BEAN_TYPE>>();
		for (final List<IBeanProxy<BEAN_TYPE>> page : data.values()) {
			beansToRemove.addAll(page);
		}
		beansToRemove.addAll(addedData);
		removeBeans(beansToRemove);
	}

	private List<IBeanProxy<BEAN_TYPE>> removeBeansImpl(final Iterable<? extends IBeanDto> beans, final boolean fireEvents) {
		Assert.paramNotNull(beans, "beans");
		//data structure must rebuild, so do not load until this happens
		tryToCancelLoader();

		final List<IBeanProxy<BEAN_TYPE>> selectedBeans = getSelectedBeans();
		final List<Integer> newSelection = new LinkedList<Integer>();

		final List<IBeanProxy<BEAN_TYPE>> result = new LinkedList<IBeanProxy<BEAN_TYPE>>();

		result.addAll(removeBeansFromData(new HashSet<IBeanProxy<BEAN_TYPE>>(selectedBeans), newSelection, beans));
		result.addAll(removeBeansFromAddedData(new HashSet<IBeanProxy<BEAN_TYPE>>(selectedBeans), newSelection, beans));

		setSelection(newSelection);

		if (fireEvents) {
			fireBeansChanged();
		}

		return result;
	}

	private List<IBeanProxy<BEAN_TYPE>> removeBeansFromData(
		final Set<IBeanProxy<BEAN_TYPE>> oldSelection,
		final List<Integer> newSelection,
		final Iterable<? extends IBeanDto> beans) {
		final List<IBeanProxy<BEAN_TYPE>> removedBeanProxies = new LinkedList<IBeanProxy<BEAN_TYPE>>();
		final Map<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>> newData = getNewData(
				oldSelection,
				newSelection,
				beans,
				removedBeanProxies);
		data.clear();
		for (final Entry<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>> entry : newData.entrySet()) {
			data.put(entry.getKey(), entry.getValue());
		}
		return removedBeanProxies;
	}

	private Map<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>> getNewData(
		final Set<IBeanProxy<BEAN_TYPE>> oldSelection,
		final List<Integer> newSelection,
		final Iterable<? extends IBeanDto> beans,
		final List<IBeanProxy<BEAN_TYPE>> removedBeanProxies) {

		//hold the beans that should be deleted but that are currently not deleted
		final Set<IBeanDto> beansToDelete = CollectionUtils.createHashSet(beans);

		final Map<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>> newData = new HashMap<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>>();
		int newPageIndex = 0;
		ArrayList<IBeanProxy<BEAN_TYPE>> newPage = new ArrayList<IBeanProxy<BEAN_TYPE>>(pageSize);

		final int pageCount = getPage(dataModel.getDataRowCount()) + 1;
		int addedNullCount = 0;
		final List<IBeanProxy<BEAN_TYPE>> beansToUnregister = new LinkedList<IBeanProxy<BEAN_TYPE>>();
		for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
			final ArrayList<IBeanProxy<BEAN_TYPE>> page = data.get(pageIndex);
			//add a regular page
			if (page != null) {
				for (final IBeanProxy<BEAN_TYPE> bean : page) {
					final boolean deletedBeanRemoved = beansToDelete.remove(bean);
					if (deletedBeanRemoved) {
						beansToUnregister.add(bean);
						removedBeanProxies.add(bean);
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
		beansStateTracker.unregister(beansToUnregister);

		//add the last new page if it has data
		if (newPage.size() > 0) {
			newData.put(newPageIndex, newPage);
		}
		return newData;
	}

	private List<IBeanProxy<BEAN_TYPE>> removeBeansFromAddedData(
		final Set<IBeanProxy<BEAN_TYPE>> oldSelection,
		final List<Integer> newSelection,
		final Iterable<? extends IBeanDto> beans) {
		final List<IBeanProxy<BEAN_TYPE>> result = new LinkedList<IBeanProxy<BEAN_TYPE>>();
		//hold the beans that should be deleted but that are currently not deleted
		final Set<IBeanDto> beansToDelete = CollectionUtils.createHashSet(beans);
		final LinkedList<IBeanProxy<BEAN_TYPE>> newAddedData = new LinkedList<IBeanProxy<BEAN_TYPE>>();
		for (final IBeanProxy<BEAN_TYPE> addedBean : addedData) {
			final boolean removed = beansToDelete.remove(addedBean);
			if (removed) {
				result.add(addedBean);
			}
			else {
				newAddedData.add(addedBean);
				if (oldSelection.remove(addedBean)) {
					newSelection.add(dataModel.getDataRowCount() + newAddedData.size() - 1);
				}
			}
		}
		beansStateTracker.unregister(result);
		addedData.clear();
		addedData.addAll(newAddedData);
		return result;
	}

	@Override
	public IAttribute<Object> getAttribute(final int columnIndex) {
		return attributeSet.getAttribute(columnIndex);
	}

	@Override
	public IAttribute<Object> getAttribute(final String propertyName) {
		Assert.paramNotNull(propertyName, "propertyName");
		for (final IAttribute<Object> attribute : attributeSet) {
			if (propertyName.equals(attribute.getPropertyName())) {
				return attribute;
			}
		}
		return null;
	}

	@Override
	public Collection<IAttribute<Object>> getAttributes() {
		return attributeSet.getAttributes();
	}

	@Override
	public Collection<String> getPropertyNames() {
		return attributeSet.getPropertyNames();
	}

	@Override
	public Collection<IAttribute<Object>> getAttributes(final IAttributeFilter filter) {
		Assert.paramNotNull(filter, "filter");
		return attributeSet.getAttributes(filter);
	}

	@Override
	public IAttributeSet getAttributeSet() {
		return attributeSet;
	}

	@Override
	public List<IBeanPropertyValidator<BEAN_TYPE>> getBeanPropertyValidators() {
		return beanPropertyValidatorsView;
	}

	@Override
	public void addBeanValidator(final IBeanValidator<BEAN_TYPE> beanValidator) {
		beanPropertyValidators.add(new BeanPropertyValidatorAdapter<BEAN_TYPE>(beanValidator));
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
		return attributeSet.size();
	}

	@Override
	public void addBeanListModelListener(final IBeanListModelListener<BEAN_TYPE> listener) {
		beanListModelObservable.addBeanListModelListener(listener);
	}

	@Override
	public void removeBeanListModelListener(final IBeanListModelListener<BEAN_TYPE> listener) {
		beanListModelObservable.removeBeanListModelListener(listener);
	}

	@Override
	public void addBeanListModelBeansListener(final IBeanListModelBeansListener<BEAN_TYPE> listener) {
		beansStateTracker.addBeanListModelBeansListener(listener);
	}

	@Override
	public void removeBeanListModelBeansListener(final IBeanListModelBeansListener<BEAN_TYPE> listener) {
		beansStateTracker.removeBeanListModelBeansListener(listener);
	}

	@Override
	public void addBeanSelectionListener(final IBeanSelectionListener<BEAN_TYPE> listener) {
		beanSelectionObservable.addBeanSelectionListener(listener);
	}

	@Override
	public void removeBeanSelectionListener(final IBeanSelectionListener<BEAN_TYPE> listener) {
		beanSelectionObservable.removeBeanSelectionListener(listener);
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
		addSelectedBeansImpl(new LinkedList<Integer>(), selectedBeans);
	}

	@Override
	public void addSelectedBeans(final Collection<? extends IBeanProxy<BEAN_TYPE>> selectedBeans) {
		addSelectedBeansImpl(selectedBeans);
	}

	private List<IBeanProxy<BEAN_TYPE>> addSelectedBeansImpl(final Collection<? extends IBeanProxy<BEAN_TYPE>> selectedBeans) {
		return addSelectedBeansImpl(getSelection(), selectedBeans);
	}

	@SuppressWarnings("unchecked")
	private List<IBeanProxy<BEAN_TYPE>> addSelectedBeansImpl(
		final List<Integer> currentSelection,
		final Collection<? extends IBeanProxy<BEAN_TYPE>> selectedBeans) {

		final Set<Integer> newSelection = new LinkedHashSet<Integer>(currentSelection);
		final List<IBeanProxy<BEAN_TYPE>> result = new LinkedList<IBeanProxy<BEAN_TYPE>>();

		boolean setSelection = EmptyCheck.isEmpty(currentSelection);

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
						setSelection = newSelection.add(Integer.valueOf(pageStartIndex + relativeIndex)) || setSelection;
						result.add(bean);
					}
					relativeIndex++;
				}
			}
			final int dataRowCount = dataModel.getDataRowCount();
			int relativeIndex = 0;
			for (final IBeanProxy<BEAN_TYPE> bean : addedData) {
				if (bean != null && selectedBeansSet.contains(bean)) {
					setSelection = newSelection.add(Integer.valueOf(dataRowCount + relativeIndex)) || setSelection;
					result.add(bean);
				}
				relativeIndex++;
			}
		}
		if (setSelection) {
			setSelection(newSelection);
		}

		return result;
	}

	@Override
	public List<IBeanProxy<BEAN_TYPE>> getSelectedBeans() {
		final List<IBeanProxy<BEAN_TYPE>> result = new LinkedList<IBeanProxy<BEAN_TYPE>>();
		for (final Integer selectionIndex : getSelection()) {
			final IBeanProxy<BEAN_TYPE> selectedBean = getBean(selectionIndex.intValue());
			if (selectedBean != null) {
				result.add(selectedBean);
			}
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	public IBeanSelection<BEAN_TYPE> getBeanSelection() {
		return new BeanSelectionImpl<BEAN_TYPE>(beanTypeId, beanType, entityId, getSelectedBeans());
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
			fireSelectionChanged();
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
	public void setConfig(final IBeanTableModelConfig config) {
		Assert.paramNotNull(config, "config");
		onSetConfig = true;
		final Map<String, IAttributeConfig> attributeConfigs = config.getAttributeConfigs();
		if (attributeConfigs != null) {
			for (final IAttribute<Object> attribute : attributeSet) {
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
	public IBeanTableModelConfig getConfig() {
		final IBeanTableModelConfigBuilder builder = CapUiToolkit.beanTableModelConfigBuilder();
		for (final IAttribute<Object> attribute : attributeSet) {
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
		final IFilter addedDataFilter = getAddedDataFilter();

		if (filters.size() > 0 || addedDataFilter != null) {
			final IBooleanFilterBuilder builder = CapCommonToolkit.filterFactory().booleanFilterBuilder();
			builder.setOperator(BooleanOperator.AND);

			final IUiFilterFactory filterFactory = CapUiToolkit.filterToolkit().filterFactory();
			for (final IUiFilter uiFilter : filters.values()) {
				builder.addFilter(filterFactory.convert(uiFilter));
			}

			if (addedDataFilter != null) {
				builder.addFilter(addedDataFilter);
			}

			return builder.build();
		}
		else {
			return addedDataFilter;
		}
	}

	private IFilter getAddedDataFilter() {
		if (!EmptyCheck.isEmpty(addedData)) {
			final IArithmeticFilterBuilder builder = CapCommonToolkit.filterFactory().arithmeticFilterBuilder();
			builder.setInverted(true);
			builder.setPropertyName(IBean.ID_PROPERTY);
			builder.setOperator(ArithmeticOperator.CONTAINS_ANY);
			boolean added = false;
			for (final IBeanProxy<BEAN_TYPE> addedBean : addedData) {
				if (!addedBean.isDummy() && !addedBean.isTransient()) {
					builder.addParameter(addedBean.getId());
					added = true;
				}
			}
			if (added) {
				return builder.build();
			}
		}
		return null;
	}

	@Override
	public List<IBeanKey> getParentBeanKeys() {
		if (parent == null) {
			return null;
		}

		final IBeanSelection<Object> beanSelection = parent.getBeanSelection();
		List<IBeanProxy<Object>> selection = beanSelection.getSelection();
		if (EmptyCheck.isEmpty(selection)) {
			return null;
		}
		else if (linkType == LinkType.SELECTION_FIRST) {
			selection = selection.subList(0, 1);
		}

		final List<IBeanKey> beanKeys = new LinkedList<IBeanKey>();
		for (final IBeanProxy<Object> proxy : selection) {
			if (proxy != null && !proxy.isDummy() && !proxy.isTransient() && !proxy.isLastRowDummy()) {
				beanKeys.add(new BeanKey(proxy.getId(), proxy.getVersion()));
			}
		}

		return beanKeys;
	}

	private ScheduledExecutorService getScheduledExecutorService() {
		if (scheduledExecutorService == null) {
			scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
		}
		return scheduledExecutorService;
	}

	private void fireSelectionChanged() {
		beanSelectionObservable.fireBeanSelectionEvent(this, beanTypeId, beanType, entityId, getSelectedBeans());
	}

	@Override
	public String toString() {
		return "BeanTableModelImpl [entityId="
			+ entityId
			+ ", labelPlural="
			+ labelPlural
			+ ", beanTypeId="
			+ beanTypeId
			+ ", beanType="
			+ beanType
			+ ", hasModifications()="
			+ hasModifications()
			+ ", hasExecutions()="
			+ hasExecutions()
			+ ", getSize()="
			+ getSize()
			+ "]";
	}

	private final class DataModel extends AbstractTableDataModel {

		private final List<IBeanTableCellRenderer<BEAN_TYPE>> cellRenderers;

		private DataModel(final List<IBeanTableCellRenderer<BEAN_TYPE>> cellRenderers) {
			this.cellRenderers = new LinkedList<IBeanTableCellRenderer<BEAN_TYPE>>(cellRenderers);
		}

		@Override
		public int getRowCount() {
			return getDataRowCount() + addedData.size() + getLastBeanCount();
		}

		private int getDataRowCount() {
			if (countedRowCount != null) {
				return Math.max(countedRowCount.intValue(), rowCount);
			}
			else {
				return rowCount;
			}
		}

		private int getLastBeanCount() {
			if (lastBean != null) {
				return 1;
			}
			else {
				return 0;
			}
		}

		@Override
		public ITableCell getCell(final int rowIndex, final int columnIndex) {
			if (dataCleared) {
				return new TableCellBuilder().build();
			}

			if (autoRowCount && countLoader != null && !countLoader.isStarted() && !countLoader.isDisposed()) {
				countLoader.loadCount();
			}

			final IAttribute<Object> attribute = attributeSet.getAttribute(columnIndex);
			final int pageIndex = getPage(rowIndex);
			final ArrayList<IBeanProxy<BEAN_TYPE>> page = data.get(Integer.valueOf(pageIndex));

			if (rowIndex >= getDataRowCount()) {
				final IBeanProxy<BEAN_TYPE> bean = getBean(rowIndex);
				if (bean == null) {
					//TODO MG this should not happen, if happens the the addedData array might be fixed
					LOGGER.warn("Added data might be inconsistent" + rowIndex);
					return new TableCellBuilder().build();
				}
				else {
					final ITableCellBuilder cellBuilder = createAddedBeanCellBuilder(rowIndex, columnIndex, bean);
					return applyRenderers(cellBuilder, bean, attribute, rowIndex, columnIndex, true);
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
					else if (backgroundPageLoader == null || !backgroundPageLoader.isPageLoading(pageIndex)) {
						completeEvenOddPage(pageIndex);
					}
					return createDummyCell(rowIndex, columnIndex, null, attribute);
				}
				else if (!bean.isDummy()) {
					final ITableCellBuilder cellBuilder = createCellBuilder(rowIndex, columnIndex, bean);
					return applyRenderers(cellBuilder, bean, attribute, rowIndex, columnIndex, false);
				}
				else {
					return createDummyCell(rowIndex, columnIndex, bean, attribute);
				}
			}
		}

		private IBeanProxy<BEAN_TYPE> getBean(final int rowIndex) {
			if (rowIndex >= getDataRowCount()) {
				if (rowIndex == (getRowCount() - 1) && lastBean != null) {
					return lastBean;
				}
				else {
					return getBeanFromAddedData(rowIndex);
				}
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

		private ITableCellBuilder createCellBuilder(final int rowIndex, final int columnIndex, final IBeanProxy<BEAN_TYPE> bean) {
			return createCellBuilder(rowIndex, columnIndex, bean, false);
		}

		private ITableCellBuilder createAddedBeanCellBuilder(
			final int rowIndex,
			final int columnIndex,
			final IBeanProxy<BEAN_TYPE> bean) {
			return createCellBuilder(rowIndex, columnIndex, bean, true);
		}

		private ITableCellBuilder createCellBuilder(
			final int rowIndex,
			final int columnIndex,
			final IBeanProxy<BEAN_TYPE> bean,
			final boolean createdBean) {

			final IAttribute<Object> attribute = attributeSet.getAttribute(columnIndex);
			final IObjectLabelConverter<Object> converter = attribute.getCurrentControlPanel().getObjectLabelConverter();
			final Object value = bean.getValue(attribute.getPropertyName());

			final ITableCellBuilder cellBuilder = createDefaultCellBuilder(bean, rowIndex, columnIndex);

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
			result = result && !bean.hasExecution();
			if (result) {
				result = result && hasEditor(attribute);
			}
			return result;
		}

		private boolean hasEditor(final IAttribute<Object> attribute) {
			final IControlPanelProvider<Object> controlPanel = attribute.getCurrentControlPanel();
			if (controlPanel != null) {
				if (attribute.isCollectionType()) {
					return controlPanel.getCollectionControlCreator() != null;
				}
				else {
					return controlPanel.getControlCreator() != null;
				}
			}
			return false;
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
				cellBuilder = createDefaultCellBuilder(bean, rowIndex, columnIndex);
				if (hasMessages) {
					final IBeanMessage worstMessage = bean.getFirstWorstMessage();
					cellBuilder.setText("---").setToolTipText(worstMessage.getLabel());
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

		private ITableCellBuilder createDefaultCellBuilder(
			final IBeanProxy<BEAN_TYPE> bean,
			final int rowIndex,
			final int columnIndex) {
			final ITableCellBuilder cellBuilder = new TableCellBuilder();
			if (bean != lastBean && rowIndex % 2 == 0) {
				cellBuilder.setBackgroundColor(Colors.DEFAULT_TABLE_EVEN_BACKGROUND_COLOR);
			}
			return cellBuilder;
		}

		private ITableCell applyRenderers(
			final ITableCellBuilder builder,
			final IBeanProxy<BEAN_TYPE> bean,
			final IAttribute<Object> attribute,
			final int rowIndex,
			final int columnIndex,
			final boolean addedBean) {
			for (final IBeanTableCellRenderer<BEAN_TYPE> renderer : cellRenderers) {
				renderer.render(
						new TableCellBluePrint(builder),
						builder.build(),
						bean,
						attribute,
						rowIndex,
						columnIndex,
						addedBean);
			}
			return builder.build();
		}
	}

	private class TableDataModelListener extends TableDataModelAdapter {
		@Override
		public void selectionChanged() {
			//clear the last selected beans if user changes selection itself
			//or on programmatic selection change (except changes from PageLoader)
			lastSelectedBeans.clear();
			fireSelectionChanged();
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
			column.setWidth(attribute.getTableWidth());
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

		private final IFilter filter;

		private boolean canceled;
		private boolean finished;
		private boolean started;
		private IExecutionTask executionTask;

		CountLoader() {
			this.filter = getFilter();
			this.canceled = false;
			this.finished = false;
			this.started = false;
		}

		void loadCount() {
			started = true;
			if (autoRowCount) {
				executionTask = CapUiToolkit.executionTaskFactory().create();
				executionTask.addExecutionCallbackListener(new AbstractUiExecutionCallbackListener() {
					@Override
					public void canceledUi() {
						canceled = true;
					}
				});

				readerService.count(
						createResultCallback(),
						getParentBeanKeys(),
						filter,
						readerParameterProvider.get(),
						executionTask);
			}
			else {
				finished = true;
			}
		}

		private boolean isStarted() {
			return started;
		}

		private IResultCallback<Integer> createResultCallback() {
			return new AbstractUiResultCallback<Integer>() {

				@Override
				public void finishedUi(final Integer result) {
					setResult(result);
				}

				@Override
				public void exceptionUi(final Throwable exception) {
					setException(exception);
				}
			};
		}

		private void setResult(final Integer result) {
			executionTask = null;
			finished = true;
			if (result != null) {
				final List<IBeanProxy<BEAN_TYPE>> selectedBeans = getSelectedBeans();

				countedRowCount = result;

				//data may be deleted
				if (countedRowCount < rowCount) {
					//change the row count to the new row count
					rowCount = countedRowCount.intValue();

					//remove the pages after the new row count
					final int newMaxPageIndex = getPage(rowCount);
					for (int pageIndex = newMaxPageIndex + 1; pageIndex <= maxPageIndex; pageIndex++) {
						removePage(pageIndex);
					}
					maxPageIndex = newMaxPageIndex;
				}
				else {
					fixLoadingPages();
				}

				fireBeansChanged();

				dataModel.removeDataModelListener(tableDataModelListener);
				setSelectedBeans(selectedBeans);
				fireSelectionChanged();
				dataModel.addDataModelListener(tableDataModelListener);
			}
			else {
				autoRowCount = false;
			}
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

		private void setException(final Throwable exception) {
			executionTask = null;
			finished = true;
			countedRowCount = null;

			LOGGER.error(exception);

			fireBeansChanged();
		}

		boolean isDisposed() {
			return canceled || finished;
		}

		void cancel() {
			if (!canceled) {
				canceled = true;
				if (executionTask != null && !executionTask.isCanceled()) {
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

		void fixPageSize() {
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

			dummyBeanProxy = beanProxyFactory.createDummyProxy();
			executionTask = CapUiToolkit.executionTaskFactory().create();
			executionTask.setDescription(LOADING_DATA.get());
			executionTask.addExecutionCallbackListener(new AbstractUiExecutionCallbackListener() {
				@Override
				public void canceledUi() {
					if (!canceled) {//if canceled by user
						userCanceled();
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

			this.parameter = readerParameterProvider.get();

			dummyBeanProxy.setExecutionTask(executionTask);
			beansStateTracker.register(dummyBeanProxy);

			dataModel.fireDataChanged();
			fireSelectionChanged();

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
					if (!canceled) {
						uiThreadAccess.invokeLater(new Runnable() {
							@Override
							public void run() {
								schedule = null;
								if (!canceled) {
									readDataFromService();
								}
							}
						});
					}
				}
			};
			schedule = getScheduledExecutorService().schedule(runnable, dealy, TimeUnit.MILLISECONDS);
		}

		void readDataFromService() {
			cachedReaderService.read(
					createResultCallback(),
					getParentBeanKeys(),
					filter,
					sortModel.getSorting(),
					(pageIndex * pageSize) + offset,
					pageSize - offset + PAGE_LOAD_OVERLAP,
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
					schedule = null;
				}
				if (executionTask != null) {
					executionTask.cancel();
				}
				removePage();
			}
		}

		private IResultCallback<List<IBeanDto>> createResultCallback() {
			return new AbstractUiResultCallback<List<IBeanDto>>() {

				@Override
				public void finishedUi(final List<IBeanDto> beanDtos) {
					if (!canceled && !executionTask.isCanceled()) {
						setResult(beanDtos);
					}
				}

				@Override
				public void exceptionUi(final Throwable exception) {
					setException(exception);
				}

			};
		}

		@SuppressWarnings("unchecked")
		private void setResult(final List<IBeanDto> loadedBeans) {
			//if the result is empty and the page is not the first page, reload the whole table
			if (loadedBeans.size() == 0 && pageIndex > 0) {
				rowCount = 0;
				countedRowCount = null;
				load();
				return;
			}
			//if the page is the last page, adapt the row count and maxPage index
			else if (pageIndex >= maxPageIndex) {
				rowCount = pageIndex * pageSize + loadedBeans.size();
				maxPageIndex = pageIndex;
				//if the counted row count is potentially wrong, reset it (and recount on autoRowcount)
				if (countedRowCount != null && countedRowCount.intValue() > rowCount && loadedBeans.size() <= pageSize) {
					countedRowCount = null;
					if (autoRowCount) {
						tryToCancelCountLoader();
						countLoader = new CountLoader();
						countLoader.loadCount();
					}
				}
			}

			//remove the dummy beans from the page
			if (offset > 0) {
				while (page.size() > offset) {
					page.remove(page.size() - 1);
				}
			}
			else {
				page.clear();
			}

			//add the loaded beans to the page
			int index = offset;
			final int pageOffset = pageSize * pageIndex;

			final List<IBeanProxy<BEAN_TYPE>> beansToRegister = new LinkedList<IBeanProxy<BEAN_TYPE>>();
			for (final IBeanDto beanDto : loadedBeans) {
				if (index < pageSize) {
					final IBeanProxy<BEAN_TYPE> beanProxy;
					if (beanDto instanceof IBeanProxy<?>) {
						beanProxy = (IBeanProxy<BEAN_TYPE>) beanDto;
					}
					else {
						beanProxy = createBeanProxy(beanDto);
					}
					page.add(beanProxy);
					beansToRegister.add(beanProxy);
					final int rowNr = pageOffset + index;
					beanProxy.addPropertyChangeListener(new BeanPropertyChangeListener(rowNr));
					index++;

				}
				else {
					break;
				}
			}
			beansStateTracker.register(beansToRegister);

			//unregister the dummy bean
			dummyBeanProxy.setExecutionTask(null);
			beansStateTracker.unregister(dummyBeanProxy);

			//fire model changes
			dataModel.fireDataChanged();
			beanListModelObservable.fireBeansChanged();

			//remove from programmatic loaders map, if one
			programmaticPageLoader.remove(pageIndex);

			//set to finished
			finished = true;

			//reselect the beans
			if (!lastSelectedBeans.isEmpty()) {
				dataModel.removeDataModelListener(tableDataModelListener);
				final List<IBeanProxy<BEAN_TYPE>> addedBeans = addSelectedBeansImpl(lastSelectedBeans);
				dataModel.addDataModelListener(tableDataModelListener);
				if (!addedBeans.isEmpty()) {
					lastSelectedBeans.removeAll(addedBeans);
					fireSelectionChanged();
				}
				else {
					tryAutoSelectFirst();
				}
			}
			else {
				tryAutoSelectFirst();
			}

			page = null;

			//inform callback of success
			doCallbackSuccess();
		}

		private void tryAutoSelectFirst() {
			if (autoSelection && pageIndex == 0 && rowCount > 0 && getSelection().isEmpty()) {
				setSelection(Collections.singletonList(Integer.valueOf(0)));
			}
		}

		private void setException(final Throwable exception) {
			LOGGER.error(exception);
			dummyBeanProxy.setExecutionTask(null);
			final List<IBeanProxy<BEAN_TYPE>> dummyBeanList = Collections.singletonList(dummyBeanProxy);
			if (!(exception instanceof ServiceCanceledException)) {
				dummyBeanProxy.addMessage(exceptionConverter.convert(LOAD_ERROR.get(), dummyBeanList, dummyBeanProxy, exception));
			}
			programmaticPageLoader.remove(pageIndex);
			finished = true;
			dataModel.fireDataChanged();
			page = null;
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
				page = null;
				dataModel.fireDataChanged();
			}
		}

		private void userCanceled() {
			if (schedule != null) {
				schedule.cancel(false);
				schedule = null;
			}
			dummyBeanProxy.setExecutionTask(null);
			final IBeanMessageBuilder beanMessageBuilder = CapUiToolkit.beanMessageBuilder(BeanMessageType.WARNING);
			beanMessageBuilder.setShortMessage(LOADING_DATA.get());
			beanMessageBuilder.setMessage(USER_CANCELED.get());
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

	private class BeanPropertyChangeListener implements PropertyChangeListener {

		private int rowIndex;

		BeanPropertyChangeListener(final int rowIndex) {
			this.rowIndex = rowIndex;
		}

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			@SuppressWarnings("unchecked")
			final IBeanProxy<BEAN_TYPE> source = (IBeanProxy<BEAN_TYPE>) evt.getSource();
			final IBeanProxy<BEAN_TYPE> beanAtIndex = getBean(rowIndex);
			if (beanAtIndex == null || !NullCompatibleEquivalence.equals(source, beanAtIndex)) {
				rowIndex = getBeanIndex(source);
			}
			if (rowIndex != -1) {
				try {
					//sometimes this leads to an index out of bound exception
					//but because everything runs in the ui thread i do not
					//understand why
					dataModel.fireRowsChanged(new int[] {rowIndex});
				}
				catch (final Exception e) {
					dataModel.fireDataChanged();
				}
				final String propertyName = evt.getPropertyName();
				if (useLastModificationAsDefault && defaultValues.containsKey(propertyName)) {
					defaultValues.put(propertyName, evt.getNewValue());
				}
			}
			else {
				if (!source.isDisposed()) {
					source.removePropertyChangeListener(this);
				}
			}
		}

	}

	private class BackgroundPageLoader {

		private final int startPageIndex;
		private final int endPageIndex;
		private final IResultCallback<Void> resultCallback;
		private final int pageCount;
		private final IFilter filter;

		private boolean started;
		private boolean canceled;
		private boolean finished;

		private IExecutionTask executionTask;

		BackgroundPageLoader(final int startPageIndex, final int endPageIndex, final IResultCallback<Void> resultCallback) {
			this.startPageIndex = startPageIndex;
			this.endPageIndex = endPageIndex;
			this.resultCallback = resultCallback;
			this.pageCount = 1 + (endPageIndex - startPageIndex);
			this.filter = getFilter();

			this.started = false;
			this.canceled = false;
			this.finished = false;
		}

		boolean isPageLoading(final int page) {
			return started && !isDisposed() && page >= startPageIndex && page <= endPageIndex;
		}

		void loadInBackground() {
			started = true;

			executionTask = CapUiToolkit.executionTaskFactory().create();
			executionTask.setDescription(LOADING_DATA.get());
			executionTask.addExecutionCallbackListener(new AbstractUiExecutionCallbackListener() {
				@Override
				public void canceledUi() {
					if (!canceled) {//if canceled by user
						userCanceled();
					}
				}
			});

			readerService.read(
					createResultCallback(),
					getParentBeanKeys(),
					filter,
					sortModel.getSorting(),
					(startPageIndex * pageSize),
					(pageCount * pageSize) + PAGE_LOAD_OVERLAP,
					readerParameterProvider.get(),
					executionTask);

		}

		boolean isDisposed() {
			return canceled || finished;
		}

		void cancel() {
			if (!canceled) {
				this.canceled = true;
				if (executionTask != null) {
					executionTask.cancel();
				}
			}
		}

		private IResultCallback<List<IBeanDto>> createResultCallback() {
			return new AbstractUiResultCallback<List<IBeanDto>>() {

				@Override
				public void finishedUi(final List<IBeanDto> beanDtos) {
					if (!canceled && !executionTask.isCanceled()) {
						setResult(beanDtos);
					}
				}

				@Override
				public void exceptionUi(final Throwable exception) {
					setException(exception);
				}

			};
		}

		private void setResult(final List<IBeanDto> resultBeanDtos) {
			final List<IBeanProxy<BEAN_TYPE>> selectedBeans = getSelectedBeans();

			final List<IBeanProxy<BEAN_TYPE>> createdBeans = new LinkedList<IBeanProxy<BEAN_TYPE>>();

			boolean dataChanged = false;
			final Iterator<IBeanDto> newDataIterator = resultBeanDtos.iterator();
			final LinkedList<IBeanProxy<BEAN_TYPE>> beansToUnregister = new LinkedList<IBeanProxy<BEAN_TYPE>>();
			for (int pageIndex = startPageIndex; pageIndex <= endPageIndex; pageIndex++) {
				final int pageOffset = pageSize * pageIndex;

				ArrayList<IBeanProxy<BEAN_TYPE>> oldPage = data.get(pageIndex);
				if (oldPage == null) {
					oldPage = new ArrayList<IBeanProxy<BEAN_TYPE>>();
				}
				final ArrayList<IBeanProxy<BEAN_TYPE>> newPage = new ArrayList<IBeanProxy<BEAN_TYPE>>();
				boolean pageChanged = false;

				for (int i = 0; i < pageSize; i++) {
					final IBeanProxy<BEAN_TYPE> oldBeanProxy = i < oldPage.size() ? oldPage.get(i) : null;
					if (newDataIterator.hasNext()) {
						final IBeanDto newBean = newDataIterator.next();
						if (oldBeanProxy == null || hasBeanChanged(oldBeanProxy, newBean)) {
							final int rowNr = pageOffset + i;
							final IBeanProxy<BEAN_TYPE> newBeanProxy = createBeanProxy(newBean);
							createdBeans.add(newBeanProxy);
							newBeanProxy.addPropertyChangeListener(new BeanPropertyChangeListener(rowNr));
							if (oldBeanProxy != null) {
								beansToUnregister.add(oldBeanProxy);
							}
							newPage.add(newBeanProxy);
							dataChanged = true;
							pageChanged = true;
						}
						else {
							newPage.add(oldBeanProxy);
						}
					}
					else {
						final boolean changed = oldBeanProxy != null;
						dataChanged = dataChanged || changed;
						pageChanged = pageChanged || changed;
						if (oldBeanProxy != null) {
							beansToUnregister.add(oldBeanProxy);
						}
						else {
							break;
						}
					}
				}

				if (pageChanged && newPage.size() > 0) {
					data.put(Integer.valueOf(pageIndex), newPage);
				}
			}

			beansStateTracker.unregister(beansToUnregister);
			if (dataChanged) {

				//change the row count to the new row count
				rowCount = startPageIndex * pageSize + resultBeanDtos.size();

				//if the result is empty and the page is not the first page, reload the whole table
				if (resultBeanDtos.size() == 0 && startPageIndex > 0) {
					rowCount = 0;
					countedRowCount = null;
					load();
					return;
				}
				//if the page is the last page, adapt the row count and maxPage index
				else if (endPageIndex >= maxPageIndex) {
					maxPageIndex = endPageIndex;
					//if the counted row count is potentially wrong, reset it (and recount on autoRowcount)
					if (countedRowCount != null
						&& countedRowCount.intValue() > rowCount
						&& resultBeanDtos.size() <= (pageSize * pageCount)) {
						countedRowCount = null;
						if (autoRowCount) {
							tryToCancelCountLoader();
							countLoader = new CountLoader();
							countLoader.loadCount();
						}
					}
				}

				//if data is not consistent, delete pages before and after viewport
				for (int pageIndex = 0; pageIndex < startPageIndex; pageIndex++) {
					removePage(pageIndex);
				}
				for (int pageIndex = endPageIndex + 1; pageIndex <= maxPageIndex; pageIndex++) {
					removePage(pageIndex);
				}
				maxPageIndex = endPageIndex;

				beansStateTracker.register(createdBeans);

				dataModel.fireDataChanged();
				beanListModelObservable.fireBeansChanged();

				dataModel.removeDataModelListener(tableDataModelListener);
				setSelectedBeans(selectedBeans);
				fireSelectionChanged();
				dataModel.addDataModelListener(tableDataModelListener);

				resultCallback.finished(null);
			}

			backgroundPageLoader = null;
			finished = true;

		}

		private boolean hasBeanChanged(final IBeanProxy<BEAN_TYPE> oldBean, final IBeanDto newBean) {
			if (!(oldBean.getId().equals(newBean.getId()))) {
				return true;
			}
			if (oldBean.getVersion() != newBean.getVersion()) {
				return true;
			}
			for (final String propertyName : oldBean.getProperties()) {
				final Object oldValue = oldBean.getValue(propertyName);
				final Object newValue = newBean.getValue(propertyName);
				if (!IBeanProxy.ALL_META_ATTRIBUTES.contains(propertyName)
					&& !EmptyCompatibleEquivalence.equals(oldValue, newValue)) {
					return true;
				}
			}
			return false;
		}

		private void setException(final Throwable exception) {
			finished = true;
			resultCallback.exception(exception);
		}

		private void userCanceled() {
			finished = true;
		}

	}

	private final class AutoRefreshListener implements IBeanSelectionListener<BEAN_TYPE> {

		private ScheduledFuture<?> schedule;
		private IBeanProxy<BEAN_TYPE> lastSelected;

		@Override
		public void selectionChanged(final IBeanSelectionEvent<BEAN_TYPE> selectionEvent) {
			final IBeanProxy<BEAN_TYPE> selected = selectionEvent.getFirstSelected();
			if (autoRefreshSelection
				&& selected != null
				&& !NullCompatibleEquivalence.equals(lastSelected, selected)
				&& !selected.hasExecution()
				&& !selected.isDummy()
				&& !selected.isTransient()
				&& !selected.hasMessages()) {

				tryCancelAutoRefreshExecutionTask();
				if (schedule != null) {
					schedule.cancel(false);
					schedule = null;
				}

				lastSelected = selected;

				final Runnable uiRunnable = new Runnable() {

					@Override
					public void run() {
						final IExecutionTask executionTask = CapUiToolkit.executionTaskFactory().create();
						autoRefreshExecutionTask = executionTask;
						final IBeanKeyFactory beanKeyFactory = CapUiToolkit.beanKeyFactory();
						final List<IBeanKey> beanKeys = beanKeyFactory.createKeys(Collections.singleton(selected));
						final IResultCallback<List<IBeanDto>> result = new AbstractUiResultCallback<List<IBeanDto>>() {

							@Override
							protected void finishedUi(final List<IBeanDto> result) {
								if (result.size() == 1) {
									setResult(result.iterator().next());
								}
							}

							@Override
							protected void exceptionUi(final Throwable exception) {
								//TODO MG handle exception
							}

							private void setResult(final IBeanDto refreshedBean) {
								if (!executionTask.isCanceled()
									&& selected == lastSelected
									&& !selected.hasExecution()
									&& !selected.equalsAllProperties(refreshedBean, true)) {
									final Collection<IBeanModification> modifications = selected.getModifications();
									selected.update(refreshedBean);
									if (!EmptyCheck.isEmpty(modifications)) {
										selected.setModifications(modifications);
									}
									Toolkit.getMessagePane().showInfo(AUTO_REFRESH_HEADER.get(), AUTO_REFRESH_TEXT.get());
								}
							}
						};

						refreshService.refresh(result, beanKeys, executionTask);
					}
				};
				final IUiThreadAccess uiThreadAccess = Toolkit.getUiThreadAccess();
				final Runnable scheduleRunnable = new Runnable() {
					@Override
					public void run() {
						uiThreadAccess.invokeLater(uiRunnable);
					}
				};

				schedule = getScheduledExecutorService().schedule(scheduleRunnable, AUTO_REFRESH_DELAY, TimeUnit.MILLISECONDS);
			}

		}
	}

	private final class ExternalReader implements IExternalReader {

		private final List<? extends IBeanKey> parentBeanKeys;
		private final IFilter filter;
		private final List<ISort> sorting;
		private final Object parameter;

		private ExternalReader() {
			this.parentBeanKeys = getParentBeanKeys();
			this.filter = getFilter();
			this.sorting = new LinkedList<ISort>(sortModel.getSorting());
			this.parameter = readerParameterProvider.get();
		}

		@Override
		public void read(
			final IResultCallback<List<IBeanDto>> result,
			final int firstRow,
			final int maxRows,
			final IExecutionCallback executionCallback) {
			readerService.read(result, parentBeanKeys, filter, sorting, firstRow, maxRows, parameter, executionCallback);

		}

		@Override
		public void count(final IResultCallback<Integer> result, final IExecutionCallback executionCallback) {
			readerService.count(result, getParentBeanKeys(), getFilter(), readerParameterProvider.get(), executionCallback);
		}

	}

}
