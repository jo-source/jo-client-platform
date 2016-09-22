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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.controller.IDisposeListener;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.filter.BooleanOperator;
import org.jowidgets.cap.common.api.filter.IBooleanFilterBuilder;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.common.tools.bean.BeanKey;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.AttributeSet;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifierBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeSet;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.BeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanMessageBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyContext;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.bean.IBeanSelection;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.bean.IBeansStateTracker;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.filter.IUiBooleanFilter;
import org.jowidgets.cap.ui.api.filter.IUiFilter;
import org.jowidgets.cap.ui.api.filter.IUiFilterFactory;
import org.jowidgets.cap.ui.api.filter.IUiFilterTools;
import org.jowidgets.cap.ui.api.model.DataModelContext;
import org.jowidgets.cap.ui.api.model.IBeanListModelBeansListener;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.model.IDataModelContext;
import org.jowidgets.cap.ui.api.model.IModificationStateListener;
import org.jowidgets.cap.ui.api.model.IProcessStateListener;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.cap.ui.api.plugin.IAttributePlugin;
import org.jowidgets.cap.ui.api.sort.ISortModel;
import org.jowidgets.cap.ui.api.sort.ISortModelConfig;
import org.jowidgets.cap.ui.api.tabfolder.IBeanTabFolderModel;
import org.jowidgets.cap.ui.api.tabfolder.IBeanTabFolderModelInterceptor;
import org.jowidgets.cap.ui.tools.bean.BeanSelectionImpl;
import org.jowidgets.cap.ui.tools.bean.BeanSelectionObservable;
import org.jowidgets.cap.ui.tools.execution.AbstractUiResultCallback;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.plugin.api.PluginToolkit;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.IProvider;
import org.jowidgets.util.event.ChangeObservable;
import org.jowidgets.util.event.IChangeListener;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;

final class BeanTabFolderModelImpl<BEAN_TYPE> implements IBeanTabFolderModel<BEAN_TYPE> {

	private static final IMessage LOAD_ERROR = Messages.getMessage("BeanTableModelImpl.load_error");
	private static final IMessage LOADING_DATA = Messages.getMessage("BeanTableModelImpl.load_data");

	private static final int MAX_TABS = 100;

	private final Object entityId;
	private final Object beanTypeId;
	private final Class<BEAN_TYPE> beanType;
	private final Map<String, Object> defaultValues;
	private final IAttributeSet attributeSet;

	private final Map<String, IUiFilter> filters;
	private final ChangeObservable filterChangeObservable;
	private final ISortModel sortModel;

	private final ArrayList<IBeanProxy<BEAN_TYPE>> data;

	private final IBeanProxyFactory<BEAN_TYPE> beanProxyFactory;
	private final IBeansStateTracker<BEAN_TYPE> beansStateTracker;

	private final List<IBeanPropertyValidator<BEAN_TYPE>> beanPropertyValidators;
	private final List<IBeanPropertyValidator<BEAN_TYPE>> beanPropertyValidatorsView;

	private final List<IBeanTabFolderModelInterceptor<BEAN_TYPE>> interceptors;

	private final ICreatorService creatorService;
	private final IReaderService<Object> readerService;
	private final IProvider<Object> readerParameterProvider;
	private final IDeleterService deleterService;

	private final BeanListSaveDelegate<BEAN_TYPE> saveDelegate;
	private final BeanListRefreshDelegate<BEAN_TYPE> refreshDelegate;

	private final IBeanSelectionProvider<Object> parent;
	private final LinkType linkType;
	private final boolean clearOnEmptyFilter;
	private final boolean clearOnEmptyParentBeans;

	private final BeanListModelObservable<BEAN_TYPE> beanListModelObservable;
	private final BeanSelectionObservable<BEAN_TYPE> beanSelectionObservable;
	private final DisposeObservable disposeObservable;
	private final IChangeListener sortModelChangeListener;
	private final IBeanSelectionListener<Object> parentSelectionListener;
	private final ParentSelectionAddabledChecker parentSelectionAddabledChecker;
	private final IDataModelContext dataModelContext;

	private final IBeanProxyLabelRenderer<BEAN_TYPE> renderer;

	private Integer selectedTab;
	private boolean disposed;

	private DataLoader dataLoader;

	@SuppressWarnings("unchecked")
	BeanTabFolderModelImpl(
		final Object entityId,
		final Object beanTypeId,
		final Class<? extends BEAN_TYPE> beanType,
		final List<IAttribute<Object>> attributes,
		final IBeanProxyLabelRenderer<BEAN_TYPE> renderer,
		final Set<IBeanValidator<BEAN_TYPE>> beanValidators,
		final boolean validateUnmodifiedBeans,
		final List<IBeanTabFolderModelInterceptor<BEAN_TYPE>> interceptors,
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
		final boolean clearOnEmptyFilter,
		final boolean clearOnEmptyParentBeans,
		final IBeanProxyContext beanProxyContext,
		final IDataModelContext dataModelContext) {

		//arguments checks
		Assert.paramNotNull(interceptors, "interceptors");
		Assert.paramNotNull(entityId, "entityId");
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(renderer, "renderer");
		Assert.paramNotNull(readerService, "readerService");
		Assert.paramNotNull(paramProvider, "paramProvider");
		Assert.paramNotNull(attributes, "attributes");
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");

		this.parent = parent;
		this.entityId = entityId;
		this.beanTypeId = beanTypeId;
		this.beanType = (Class<BEAN_TYPE>) beanType;
		this.clearOnEmptyFilter = clearOnEmptyFilter;
		this.clearOnEmptyParentBeans = clearOnEmptyParentBeans;
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

		List<IAttribute<Object>> modfiedAttributes = createModifiedByPluginsAttributes(
				entityId,
				(Class<BEAN_TYPE>) beanType,
				attributes);
		//if no updater service available, set all attributes to editable false
		if (updaterService == null) {
			modfiedAttributes = createReadonlyAttributes(attributes);
		}
		this.attributeSet = AttributeSet.create(modfiedAttributes);

		final LinkedList<String> propertyNames = new LinkedList<String>();
		this.defaultValues = new HashMap<String, Object>();
		for (final IAttribute<?> attribute : this.attributeSet) {
			final String propertyName = attribute.getPropertyName();
			propertyNames.add(propertyName);
			final Object defaultValue = attribute.getDefaultValue();
			if (defaultValue != null) {
				defaultValues.put(propertyName, defaultValue);
			}
		}

		this.interceptors = new LinkedList<IBeanTabFolderModelInterceptor<BEAN_TYPE>>(interceptors);
		this.renderer = renderer;
		this.readerService = (IReaderService<Object>) readerService;
		this.readerParameterProvider = (IProvider<Object>) paramProvider;
		this.creatorService = creatorService;
		this.deleterService = deleterService;
		this.filters = new HashMap<String, IUiFilter>();
		this.data = new ArrayList<IBeanProxy<BEAN_TYPE>>();
		this.sortModel = new SortModelImpl();
		this.sortModelChangeListener = new SortModelChangeListener();
		this.disposed = false;
		this.beansStateTracker = CapUiToolkit.beansStateTracker(beanProxyContext, validateUnmodifiedBeans);
		this.beanListModelObservable = new BeanListModelObservable<BEAN_TYPE>();
		this.beanSelectionObservable = new BeanSelectionObservable<BEAN_TYPE>();
		this.disposeObservable = new DisposeObservable();
		this.filterChangeObservable = new ChangeObservable();
		this.beanPropertyValidators = createBeanPropertyValidators(attributes);
		this.beanPropertyValidatorsView = Collections.unmodifiableList(this.beanPropertyValidators);
		for (final IBeanValidator<BEAN_TYPE> beanValidator : beanValidators) {
			addBeanValidator(beanValidator);
		}

		this.beanProxyFactory = BeanProxyFactory.builder(this.beanType).setBeanTypeId(beanTypeId).setAttributes(
				attributeSet).setValidateUnmodifiedBeans(validateUnmodifiedBeans).setBeanPropertyValidators(
						beanPropertyValidators).build();

		//configure sort model
		sortModel.setConfig(sortModelConfig);
		sortModel.addChangeListener(sortModelChangeListener);

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
			BeanExecutionPolicy.BATCH,
			updaterService,
			creatorService,
			propertyNames,
			parentBeansProvider);

		this.refreshDelegate = new BeanListRefreshDelegate<BEAN_TYPE>(
			this,
			exceptionConverter,
			BeanExecutionPolicy.BATCH,
			refreshService);

		this.dataModelContext = dataModelContext != null ? dataModelContext : DataModelContext.create(this);
	}

	private List<IBeanPropertyValidator<BEAN_TYPE>> createBeanPropertyValidators(
		final Collection<? extends IAttribute<?>> attributes) {
		final List<IBeanPropertyValidator<BEAN_TYPE>> result = new LinkedList<IBeanPropertyValidator<BEAN_TYPE>>();
		final AttributesBeanPropertyValidator<BEAN_TYPE> validator = new AttributesBeanPropertyValidator<BEAN_TYPE>(attributes);
		if (validator.hasValidators()) {
			result.add(validator);
		}
		return result;
	}

	@Override
	public IDataModelContext getDataModelContext() {
		return dataModelContext;
	}

	@Override
	public IEnabledChecker getDataAddableChecker() {
		return parentSelectionAddabledChecker;
	}

	private List<IAttribute<Object>> createModifiedByPluginsAttributes(
		final Object entityId,
		final Class<BEAN_TYPE> beanType,
		final List<IAttribute<Object>> attributes) {

		List<IAttribute<Object>> result = attributes;

		final IPluginPropertiesBuilder propBuilder = PluginToolkit.pluginPropertiesBuilder();
		propBuilder.add(IAttributePlugin.ENTITIY_ID_PROPERTY_KEY, entityId);
		propBuilder.add(IAttributePlugin.BEAN_TYPE_PROPERTY_KEY, beanType);
		final IPluginProperties properties = propBuilder.build();
		for (final IAttributePlugin plugin : PluginProvider.getPlugins(IAttributePlugin.ID, properties)) {
			result = plugin.modifyAttributes(properties, result);
		}

		return result;
	}

	private static List<IAttribute<Object>> createReadonlyAttributes(final List<IAttribute<Object>> attributes) {
		final IAttributeToolkit attributeToolkit = CapUiToolkit.attributeToolkit();
		final IAttributeCollectionModifierBuilder modifierBuilder = attributeToolkit.createAttributeCollectionModifierBuilder();
		modifierBuilder.addDefaultModifier().setEditable(false);
		return CapUiToolkit.attributeToolkit().createAttributesCopy(attributes, modifierBuilder.build());
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
			beanSelectionObservable.dispose();
			filterChangeObservable.dispose();
			beansStateTracker.dispose();
			sortModel.removeChangeListener(sortModelChangeListener);
			if (parentSelectionListener != null && parent != null) {
				parent.removeBeanSelectionListener(parentSelectionListener);
			}
			parentSelectionAddabledChecker.dispose();
			disposeBeans();
			disposed = true;
		}
	}

	private void disposeBeans() {
		for (final IBeanProxy<BEAN_TYPE> bean : data) {
			if (bean != null) {
				bean.dispose();
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
	public Object getBeanTypeId() {
		return beanTypeId;
	}

	@Override
	public Class<BEAN_TYPE> getBeanType() {
		return beanType;
	}

	@Override
	public Collection<IAttribute<Object>> getAttributes() {
		return attributeSet.getAttributes();
	}

	@Override
	public IAttributeSet getAttributeSet() {
		return attributeSet;
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
	public IReaderService<Object> getReaderService() {
		return readerService;
	}

	@Override
	public void clear() {
		if (!Toolkit.getUiThreadAccess().isUiThread()) {
			throw new IllegalStateException("Clear must be invoked in the ui thread");
		}
		selectedTab = null;
		tryToCanceLoader();
		beansStateTracker.unregister(data);
		data.clear();
		beansStateTracker.clearAll();
		beanListModelObservable.fireBeansChanged();
	}

	@Override
	public void clearCache() {}

	@Override
	public boolean hasModificationsCached() {
		return false;
	}

	@Override
	public void load() {
		if (!Toolkit.getUiThreadAccess().isUiThread()) {
			throw new IllegalStateException("Load must be invoked in the ui thread");
		}
		tryToCanceLoader();
		dataLoader = new DataLoader();
		dataLoader.loadData();
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

	private void tryToCanceLoader() {
		if (dataLoader != null && !dataLoader.isDisposed()) {
			dataLoader.cancel();
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
		final Set<IBeanProxy<BEAN_TYPE>> beansToCreate = beansStateTracker.getBeansToCreate();
		if (!beansToCreate.isEmpty()) {
			removeBeansImpl(beansToCreate, false);
		}
		beansStateTracker.clearModifications();
		fireBeansChanged();
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
		beanListModelObservable.fireBeansChanged();
	}

	@Override
	public IBeanProxy<BEAN_TYPE> getBean(final int index) {
		return data.get(index);
	}

	@Override
	public int getBeanIndex(final IBeanProxy<BEAN_TYPE> sourceBean) {
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).equals(sourceBean)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void exchangeBean(final IBeanProxy<BEAN_TYPE> oldBean, final IBeanProxy<BEAN_TYPE> newBean) {
		Assert.paramNotNull(oldBean, "oldBean");
		Assert.paramNotNull(newBean, "newBean");
		final int beanIndex = getBeanIndex(oldBean);
		if (beanIndex == -1) {
			throw new IllegalArgumentException("Parameter 'oldBean' " + oldBean + " is not part of the model");
		}
		beansStateTracker.unregister(oldBean);
		beansStateTracker.register(newBean);
		data.set(beanIndex, newBean);
		fireBeansChanged();
	}

	@Override
	public void addBean(final IBeanProxy<BEAN_TYPE> bean) {
		addBean(data.size(), bean);
	}

	@Override
	public IBeanProxy<BEAN_TYPE> addBeanDto(final IBeanDto beanDto) {
		final IBeanProxy<BEAN_TYPE> result = createBeanProxy(beanDto);
		addBean(result);
		return result;
	}

	@Override
	public IBeanProxy<BEAN_TYPE> addTransientBean() {
		final IBeanProxy<BEAN_TYPE> result = beanProxyFactory.createTransientProxy(defaultValues);
		addBean(result);
		return result;
	}

	private IBeanProxy<BEAN_TYPE> createBeanProxy(final IBeanDto beanDto) {
		return beanProxyFactory.createProxy(beanDto);
	}

	@Override
	public void addBean(final int index, final IBeanProxy<BEAN_TYPE> bean) {
		Assert.paramNotNull(bean, "bean");
		beansStateTracker.register(bean);
		data.add(index, bean);
		beanListModelObservable.fireBeansAdded(bean);
		fireBeansChanged();
	}

	@Override
	public void removeBean(final IBeanProxy<BEAN_TYPE> bean) {
		Assert.paramNotNull(bean, "bean");
		removeBeans(Collections.singletonList(bean));
	}

	@Override
	public void removeBeans(final Iterable<? extends IBeanProxy<BEAN_TYPE>> beans) {
		removeBeansImpl(beans, true);
	}

	@Override
	public void removeAllBeans() {
		clear();
	}

	private void removeBeansImpl(final Iterable<? extends IBeanProxy<BEAN_TYPE>> beans, final boolean fireBeansChanged) {
		Assert.paramNotNull(beans, "beans");
		tryToCanceLoader();

		final IBeanProxy<BEAN_TYPE> selectedBean = getSelectedBean();
		boolean wasSelected = false;

		for (final IBeanProxy<BEAN_TYPE> bean : beans) {
			if (bean.equals(selectedBean)) {
				wasSelected = true;
			}
			data.remove(bean);
		}
		beansStateTracker.unregister(beans);

		if (wasSelected && data.size() > 0) {
			selectedTab = Integer.valueOf(0);
		}
		else {
			selectedTab = null;
		}

		if (fireBeansChanged) {
			fireBeansChanged();
		}

		beanListModelObservable.fireBeansRemoved(beans);
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
	public ArrayList<Integer> getSelection() {
		final ArrayList<Integer> result = new ArrayList<Integer>();
		if (selectedTab != null && selectedTab.intValue() < data.size()) {
			result.add(selectedTab);
		}
		return result;
	}

	@Override
	public Integer getSelectionIndex() {
		if (selectedTab != null && selectedTab.intValue() < data.size()) {
			return selectedTab;
		}
		else {
			return null;
		}
	}

	@Override
	public void setSelection(final Collection<Integer> selection) {
		Assert.paramNotNull(selection, "selection");
		if (selection.isEmpty()) {
			selectedTab = null;
		}
		else if (selection.size() == 1) {
			final Integer newSelection = selection.iterator().next();

			if (newSelection != null) {
				if (data.size() == 0 && newSelection.intValue() == 0) {
					selectedTab = null;
				}
				else if (newSelection.intValue() < data.size()) {
					selectedTab = newSelection;
				}
				else {
					throw new IndexOutOfBoundsException(
						"Index must be between '0' and '" + (data.size() - 1) + "' but is: " + newSelection.intValue());
				}
			}
			else {
				throw new IllegalArgumentException("Selection must not conatin null values");
			}
		}
		else {
			throw new IllegalArgumentException("Multiselection is not supported");
		}
		fireSelectionChanged();
	}

	private void fireSelectionChanged() {
		beanSelectionObservable.fireBeanSelectionEvent(this, beanTypeId, beanType, entityId, getSelectedBeans());
	}

	private List<IBeanProxy<BEAN_TYPE>> getSelectedBeans() {
		final IBeanProxy<BEAN_TYPE> selectedBean = getSelectedBean();
		if (selectedBean != null) {
			return Collections.singletonList(selectedBean);
		}
		else {
			return Collections.emptyList();
		}
	}

	@Override
	public IBeanSelection<BEAN_TYPE> getBeanSelection() {
		return new BeanSelectionImpl<BEAN_TYPE>(beanTypeId, beanType, entityId, getSelectedBeans());
	}

	@Override
	public void setSelection(final int selection) {
		setSelection(Collections.singletonList(Integer.valueOf(selection)));
	}

	@Override
	public void setSelectedBean(final IBeanProxy<BEAN_TYPE> selectedBean) {
		Assert.paramNotNull(selectedBean, "selectedBean");

		final int selectedIndex = data.indexOf(selectedBean);
		if (selectedIndex != -1) {
			setSelection(selectedIndex);
		}
		else {
			final Collection<Integer> emptyCollection = Collections.emptyList();
			setSelection(emptyCollection);
		}
	}

	@Override
	public IBeanProxy<BEAN_TYPE> getSelectedBean() {
		if (selectedTab != null && selectedTab.intValue() < data.size()) {
			return data.get(selectedTab.intValue());
		}
		else {
			return null;
		}
	}

	@Override
	public int getSize() {
		return data.size();
	}

	@Override
	public IBeanProxyLabelRenderer<BEAN_TYPE> getLabelRenderer() {
		return renderer;
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
	public IUiFilter getFilter(final String id) {
		Assert.paramNotNull(id, "id");
		return filters.get(id);
	}

	private void afterFilterChanged(final String id) {
		filterChangeObservable.fireChangedEvent();
	}

	@Override
	public ISortModel getSortModel() {
		return sortModel;
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

	private List<IBeanKey> getParentBeanKeys() {
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
			if (proxy != null && !proxy.isDummy() && !proxy.isTransient()) {
				beanKeys.add(new BeanKey(proxy.getId(), proxy.getVersion()));
			}
		}

		return beanKeys;
	}

	private class SortModelChangeListener implements IChangeListener {
		@Override
		public void changed() {
			load();
		}
	}

	private class DataLoader {

		private final IUiThreadAccess uiThreadAccess;
		private final IFilter filter;

		private boolean canceled;
		private boolean finished;

		private IExecutionTask executionTask;

		private IBeanProxy<BEAN_TYPE> dummyBean;

		DataLoader() {
			this.uiThreadAccess = Toolkit.getUiThreadAccess();
			this.filter = getFilter();
		}

		void loadData() {
			beansStateTracker.unregister(data);
			data.clear();

			executionTask = CapUiToolkit.executionTaskFactory().create();
			executionTask.setDescription(LOADING_DATA.get());
			executionTask.addExecutionCallbackListener(new IExecutionCallbackListener() {
				@Override
				public void canceled() {
					if (!canceled) {//if canceled by user
						userCanceledLater();
					}
				}
			});

			dummyBean = beanProxyFactory.createDummyProxy();
			beansStateTracker.register(dummyBean);
			dummyBean.setExecutionTask(executionTask);
			data.add(dummyBean);
			selectedTab = Integer.valueOf(0);

			if (isLoadNeeded()) {
				readerService.read(
						createResultCallback(),
						getParentBeanKeys(),
						filter,
						sortModel.getSorting(),
						0,
						MAX_TABS,
						readerParameterProvider.get(),
						executionTask);
				fireBeansChanged();
				fireSelectionChanged();
			}
			else {
				setResult(new LinkedList<IBeanDto>());
			}
		}

		boolean isLoadNeeded() {
			if (clearOnEmptyFilter && isFilterEmpty() || clearOnEmptyParentBeans && EmptyCheck.isEmpty(getParentBeanKeys())) {
				return false;
			}
			else {
				return true;
			}
		}

		boolean isDisposed() {
			return canceled || finished;
		}

		void cancel() {
			if (!canceled) {
				if (executionTask != null) {
					executionTask.cancel();
				}
				userCanceled();
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

		private void setResult(final List<IBeanDto> beanDtos) {
			if (dummyBean != null) {
				dummyBean.setExecutionTask(null);
				beansStateTracker.unregister(dummyBean);
			}

			data.clear();

			List<IBeanProxy<BEAN_TYPE>> newData = new LinkedList<IBeanProxy<BEAN_TYPE>>();
			for (final IBeanDto beanDto : beanDtos) {
				final IBeanProxy<BEAN_TYPE> beanProxy = beanProxyFactory.createProxy(beanDto);
				newData.add(beanProxy);
			}

			for (final IBeanTabFolderModelInterceptor<BEAN_TYPE> interceptor : interceptors) {
				newData = interceptor.afterLoad(newData);
			}

			for (final IBeanProxy<BEAN_TYPE> bean : newData) {
				data.add(bean);
			}
			beansStateTracker.register(newData);

			if (data.size() > 0) {
				selectedTab = Integer.valueOf(0);
			}
			else {
				selectedTab = null;
			}

			finished = true;
			beanListModelObservable.fireBeansChanged();
			fireSelectionChanged();
		}

		private void setException(final Throwable exception) {
			final IBeanMessageBuilder beanMessageBuilder = CapUiToolkit.beanMessageBuilder(BeanMessageType.ERROR);
			beanMessageBuilder.setException(exception);
			beanMessageBuilder.setShortMessage(LOADING_DATA.get());
			beanMessageBuilder.setMessage(LOAD_ERROR.get());
			final IBeanMessage message = beanMessageBuilder.build();

			if (dummyBean != null) {
				dummyBean.setExecutionTask(null);
				dummyBean.addMessage(message);
			}

			finished = true;
			beanListModelObservable.fireBeansChanged();
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
			if (!canceled) {
				if (dummyBean != null) {
					dummyBean.setExecutionTask(null);
					beansStateTracker.unregister(dummyBean);
				}
				data.clear();

				finished = true;
				canceled = true;
				beanListModelObservable.fireBeansChanged();
			}
		}

	}

	@Override
	public void clearAddedData() {
		// TODO implement
		throw new UnsupportedOperationException();
	}

	@Override
	public void addBeans(final Collection<IBeanProxy<BEAN_TYPE>> beansToAdd) {
		// TODO implement
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IBeanProxy<BEAN_TYPE>> addBeanDtos(final Collection<? extends IBeanDto> beanDtos) {
		// TODO implement
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IBeanProxy<BEAN_TYPE>> updateBeans(final Collection<IBeanDto> beansToAdd) {
		// TODO implement
		throw new UnsupportedOperationException();
	}
}
