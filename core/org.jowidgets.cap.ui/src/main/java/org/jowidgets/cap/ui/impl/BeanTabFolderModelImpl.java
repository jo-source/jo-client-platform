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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanMessageBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.bean.IBeansStateTracker;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.filter.IUiBooleanFilter;
import org.jowidgets.cap.ui.api.filter.IUiFilter;
import org.jowidgets.cap.ui.api.filter.IUiFilterFactory;
import org.jowidgets.cap.ui.api.filter.IUiFilterTools;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.model.IModificationStateListener;
import org.jowidgets.cap.ui.api.model.IProcessStateListener;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.cap.ui.api.sort.ISortModel;
import org.jowidgets.cap.ui.api.sort.ISortModelConfig;
import org.jowidgets.cap.ui.api.tabfolder.IBeanTabFolderModel;
import org.jowidgets.cap.ui.api.tabfolder.IBeanTabFolderModelInterceptor;
import org.jowidgets.cap.ui.tools.execution.AbstractUiResultCallback;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.IProvider;
import org.jowidgets.util.concurrent.DaemonThreadFactory;
import org.jowidgets.util.event.ChangeObservable;
import org.jowidgets.util.event.IChangeListener;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;

final class BeanTabFolderModelImpl<BEAN_TYPE> implements IBeanTabFolderModel<BEAN_TYPE> {

	private static final int MAX_TABS = 100;
	private static final int LISTENER_DELAY = 100;

	private final Object entityId;
	private final Class<BEAN_TYPE> beanType;
	private final List<String> propertyNames;

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

	private final IBeanListModel<?> parent;
	private final LinkType linkType;
	private final boolean clearOnEmptyFilter;
	private final boolean clearOnEmptyParentBeans;

	private final BeanListModelObservable beanListModelObservable;
	private final DisposeObservable disposeObservable;
	private final IChangeListener sortModelChangeListener;
	private final ParentModelListener parentModelListener;

	private final IBeanProxyLabelRenderer<BEAN_TYPE> renderer;

	private final String loadErrorMessage;
	private final String loadingDataLabel;

	private Integer selectedTab;
	private boolean disposed;

	private DataLoader dataLoader;

	@SuppressWarnings("unchecked")
	BeanTabFolderModelImpl(
		final Object entityId,
		final Class<? extends BEAN_TYPE> beanType,
		final List<String> propertyNames,
		final IBeanProxyLabelRenderer<BEAN_TYPE> renderer,
		final Set<IBeanValidator<BEAN_TYPE>> beanValidators,
		final Set<IBeanPropertyValidator<BEAN_TYPE>> beanPropertyValidators,
		final List<IBeanTabFolderModelInterceptor<BEAN_TYPE>> interceptors,
		final ISortModelConfig sortModelConfig,
		final IReaderService<? extends Object> readerService,
		final IProvider<? extends Object> paramProvider,
		final ICreatorService creatorService,
		final IRefreshService refreshService,
		final IUpdaterService updaterService,
		final IDeleterService deleterService,
		final IBeanExceptionConverter exceptionConverter,
		final IBeanListModel<?> parent,
		final LinkType linkType,
		final boolean clearOnEmptyFilter,
		final boolean clearOnEmptyParentBeans) {

		//arguments checks
		Assert.paramNotNull(interceptors, "interceptors");
		Assert.paramNotNull(entityId, "entityId");
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(renderer, "renderer");
		Assert.paramNotNull(readerService, "readerService");
		Assert.paramNotNull(paramProvider, "paramProvider");
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");

		this.parent = parent;
		this.entityId = entityId;
		this.beanType = (Class<BEAN_TYPE>) beanType;
		this.clearOnEmptyFilter = clearOnEmptyFilter;
		this.clearOnEmptyParentBeans = clearOnEmptyParentBeans;
		this.propertyNames = new LinkedList<String>(propertyNames);
		this.linkType = linkType;
		if (parent != null) {
			Assert.paramNotNull(linkType, "linkType");
			this.parentModelListener = new ParentModelListener();
			parent.addBeanListModelListener(parentModelListener);
		}
		else {
			this.parentModelListener = null;
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
		this.beansStateTracker = CapUiToolkit.beansStateTracker();
		this.beanProxyFactory = CapUiToolkit.beanProxyFactory(beanType);
		this.beanListModelObservable = new BeanListModelObservable();
		this.disposeObservable = new DisposeObservable();
		this.filterChangeObservable = new ChangeObservable();
		this.loadErrorMessage = Messages.getString("BeanTableModelImpl.load_error");
		this.loadingDataLabel = Messages.getString("BeanTableModelImpl.load_data");
		this.beanPropertyValidators = new LinkedList<IBeanPropertyValidator<BEAN_TYPE>>(beanPropertyValidators);
		this.beanPropertyValidatorsView = Collections.unmodifiableList(this.beanPropertyValidators);
		for (final IBeanValidator<BEAN_TYPE> beanValidator : beanValidators) {
			addBeanValidator(beanValidator);
		}

		//configure sort model
		sortModel.setConfig(sortModelConfig);
		sortModel.addChangeListener(sortModelChangeListener);

		this.saveDelegate = new BeanListSaveDelegate<BEAN_TYPE>(
			this,
			beansStateTracker,
			exceptionConverter,
			BeanExecutionPolicy.BATCH,
			updaterService,
			creatorService,
			propertyNames);

		this.refreshDelegate = new BeanListRefreshDelegate<BEAN_TYPE>(
			this,
			exceptionConverter,
			BeanExecutionPolicy.BATCH,
			refreshService);
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
			sortModel.removeChangeListener(sortModelChangeListener);
			if (parentModelListener != null && parent != null) {
				parent.removeBeanListModelListener(parentModelListener);
			}
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
		selectedTab = null;
		tryToCanceLoader();
		for (final IBeanProxy<BEAN_TYPE> bean : data) {
			beansStateTracker.unregister(bean);
		}
		data.clear();
		beansStateTracker.clearAll();
		beanListModelObservable.fireBeansChanged();
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
		//TODO MG this model should get the attributes to have default values
		//then the beanPropertyValidators could be obtained from the attributes as well
		//final IBeanProxy<BEAN_TYPE> result = beanProxyFactory.createTransientProxy(propertyNames, defaultValues);
		final IBeanProxy<BEAN_TYPE> result = beanProxyFactory.createTransientProxy(propertyNames);
		for (final IBeanPropertyValidator<BEAN_TYPE> validator : beanPropertyValidators) {
			result.addBeanPropertyValidator(validator);
		}
		addBean(result);
		return result;
	}

	private IBeanProxy<BEAN_TYPE> createBeanProxy(final IBeanDto beanDto) {
		final IBeanProxy<BEAN_TYPE> beanProxy = beanProxyFactory.createProxy(beanDto, propertyNames);
		for (final IBeanPropertyValidator<BEAN_TYPE> validator : beanPropertyValidators) {
			beanProxy.addBeanPropertyValidator(validator);
		}
		return beanProxy;
	}

	@Override
	public void addBean(final int index, final IBeanProxy<BEAN_TYPE> bean) {
		Assert.paramNotNull(bean, "bean");
		beansStateTracker.register(bean);
		data.add(index, bean);
		fireBeansChanged();
	}

	@Override
	public void removeBean(final IBeanProxy<BEAN_TYPE> bean) {
		Assert.paramNotNull(bean, "bean");
		removeBeans(Collections.singletonList(bean));
	}

	@Override
	public void removeBeans(final Collection<? extends IBeanProxy<BEAN_TYPE>> beans) {
		removeBeansImpl(beans, true);
	}

	private void removeBeansImpl(final Collection<? extends IBeanProxy<BEAN_TYPE>> beans, final boolean fireBeansChanged) {
		Assert.paramNotNull(beans, "beans");
		tryToCanceLoader();

		final IBeanProxy<BEAN_TYPE> selectedBean = getSelectedBean();
		boolean wasSelected = false;

		for (final IBeanProxy<BEAN_TYPE> bean : beans) {
			if (bean.equals(selectedBean)) {
				wasSelected = true;
			}
			beansStateTracker.unregister(bean);
			data.remove(bean);
		}

		if (wasSelected && data.size() > 0) {
			selectedTab = Integer.valueOf(0);
		}
		else {
			selectedTab = null;
		}

		if (fireBeansChanged) {
			fireBeansChanged();
		}
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
	public void addBeanListModelListener(final IBeanListModelListener listener) {
		beanListModelObservable.addBeanListModelListener(listener);
	}

	@Override
	public void removeBeanListModelListener(final IBeanListModelListener listener) {
		beanListModelObservable.removeBeanListModelListener(listener);
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
					throw new IndexOutOfBoundsException("Index must be between '0' and '"
						+ (data.size() - 1)
						+ "' but is: "
						+ newSelection.intValue());
				}
			}
			else {
				throw new IllegalArgumentException("Selection must not conatin null values");
			}
		}
		else {
			throw new IllegalArgumentException("Multiselection is not supported");
		}
		beanListModelObservable.fireSelectionChanged();
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
			return new LinkedList<IBeanKey>(beanKeys.subList(0, 1));
		}
		return beanKeys;
	}

	private class ParentModelListener implements IBeanListModelListener {
		private ScheduledFuture<?> schedule;

		@Override
		public void selectionChanged() {
			loadScheduled();
		}

		@Override
		public void beansChanged() {
			loadScheduled();
		}

		private void loadScheduled() {
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
			load();
		}
	}

	private class DataLoader {

		private final IUiThreadAccess uiThreadAccess;
		private final IFilter filter;

		private boolean canceled;
		private boolean finished;

		private IExecutionTask executionTask;

		private ArrayList<IBeanProxy<BEAN_TYPE>> oldData;
		private IBeanProxy<BEAN_TYPE> dummyBean;

		DataLoader() {
			this.uiThreadAccess = Toolkit.getUiThreadAccess();
			this.filter = getFilter();
		}

		void loadData() {
			oldData = new ArrayList<IBeanProxy<BEAN_TYPE>>();
			for (final IBeanProxy<BEAN_TYPE> bean : data) {
				oldData.add(bean);
				beansStateTracker.unregister(bean);
			}
			data.clear();

			executionTask = CapUiToolkit.executionTaskFactory().create();
			executionTask.setDescription(loadingDataLabel);
			executionTask.addExecutionCallbackListener(new IExecutionCallbackListener() {
				@Override
				public void canceled() {
					if (!canceled) {//if canceled by user
						userCanceledLater();
					}
				}
			});

			dummyBean = beanProxyFactory.createDummyProxy(propertyNames);
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
				final IBeanProxy<BEAN_TYPE> beanProxy = beanProxyFactory.createProxy(beanDto, propertyNames);
				newData.add(beanProxy);
			}

			for (final IBeanTabFolderModelInterceptor<BEAN_TYPE> interceptor : interceptors) {
				newData = interceptor.afterLoad(newData);
			}

			for (final IBeanProxy<BEAN_TYPE> bean : newData) {
				for (final IBeanPropertyValidator<BEAN_TYPE> validator : beanPropertyValidators) {
					bean.addBeanPropertyValidator(validator);
				}
				beansStateTracker.register(bean);
				data.add(bean);
			}

			if (data.size() > 0) {
				selectedTab = Integer.valueOf(0);
			}
			else {
				selectedTab = null;
			}

			finished = true;
			beanListModelObservable.fireBeansChanged();
		}

		private void setException(final Throwable exception) {
			final IBeanMessageBuilder beanMessageBuilder = CapUiToolkit.beanMessageBuilder(BeanMessageType.ERROR);
			beanMessageBuilder.setException(exception);
			beanMessageBuilder.setMessage(loadErrorMessage);
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
				if (oldData != null) {
					data.clear();
					for (final IBeanProxy<BEAN_TYPE> oldBean : oldData) {
						beansStateTracker.register(oldBean);
					}
					data.addAll(oldData);
				}

				finished = true;
				canceled = true;
				beanListModelObservable.fireBeansChanged();
			}
		}

	}

}
