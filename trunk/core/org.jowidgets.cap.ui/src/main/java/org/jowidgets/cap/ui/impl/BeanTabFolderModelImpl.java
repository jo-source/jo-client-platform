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
import java.util.concurrent.TimeoutException;

import org.jowidgets.api.controller.IDisposeListener;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.bean.IBeanModification;
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
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanMessageBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.bean.IBeansStateTracker;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
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
	private final IRefreshService refreshService;
	private final IUpdaterService updaterService;
	private final IDeleterService deleterService;

	private final IBeanListModel<?> parent;
	private final LinkType linkType;

	private final BeanListModelObservable beanListModelObservable;
	private final DisposeObservable disposeObservable;
	private final IChangeListener sortModelChangeListener;
	private final ParentModelListener parentModelListener;

	private final IBeanProxyLabelRenderer<BEAN_TYPE> renderer;

	private final String loadErrorMessage;

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
		final IBeanListModel<?> parent,
		final LinkType linkType) {

		//arguments checks
		Assert.paramNotNull(interceptors, "interceptors");
		Assert.paramNotNull(entityId, "entityId");
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(renderer, "renderer");
		Assert.paramNotNull(readerService, "readerService");
		Assert.paramNotNull(paramProvider, "paramProvider");

		this.parent = parent;
		this.entityId = entityId;
		this.beanType = (Class<BEAN_TYPE>) beanType;
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
		this.refreshService = refreshService;
		this.updaterService = updaterService;
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
		this.beanPropertyValidators = new LinkedList<IBeanPropertyValidator<BEAN_TYPE>>(beanPropertyValidators);
		this.beanPropertyValidatorsView = Collections.unmodifiableList(this.beanPropertyValidators);
		for (final IBeanValidator<BEAN_TYPE> beanValidator : beanValidators) {
			addBeanValidator(beanValidator);
		}

		//configure sort model
		sortModel.setConfig(sortModelConfig);
		sortModel.addChangeListener(sortModelChangeListener);
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
		data.clear();
		for (final IBeanProxy<BEAN_TYPE> bean : data) {
			beansStateTracker.unregister(bean);
		}
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

		fireBeansChanged();
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
				if (newSelection.intValue() < data.size()) {
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

		DataLoader() {
			this.uiThreadAccess = Toolkit.getUiThreadAccess();
			this.filter = getFilter();
		}

		void loadData() {
			executionTask = CapUiToolkit.executionTaskFactory().create();
			executionTask.addExecutionCallbackListener(new IExecutionCallbackListener() {
				@Override
				public void canceled() {
					if (!canceled) {//if canceled by user
						userCanceledLater();
					}
				}
			});
			for (final IBeanProxy<BEAN_TYPE> bean : data) {
				bean.setExecutionTask(executionTask);
			}

			readerService.read(
					createResultCallback(),
					getParentBeanKeys(),
					filter,
					sortModel.getSorting(),
					0,
					MAX_TABS,
					readerParameterProvider.get(),
					executionTask);
		}

		boolean isDisposed() {
			return canceled || finished;
		}

		void cancel() {
			if (!canceled) {
				this.canceled = true;
				for (final IBeanProxy<BEAN_TYPE> bean : data) {
					bean.setExecutionTask(null);
				}
				if (executionTask != null) {
					executionTask.cancel();
				}
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
			final List<IBeanProxy<BEAN_TYPE>> oldBeans = new LinkedList<IBeanProxy<BEAN_TYPE>>(data);
			final IBeanProxy<BEAN_TYPE> lastSelectedBean = getSelectedBean();

			for (final IBeanProxy<BEAN_TYPE> oldBean : oldBeans) {
				oldBean.setExecutionTask(null);
				beansStateTracker.unregister(oldBean);
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

			int index = 0;
			for (final IBeanProxy<BEAN_TYPE> bean : newData) {
				for (final IBeanPropertyValidator<BEAN_TYPE> validator : beanPropertyValidators) {
					bean.addBeanPropertyValidator(validator);
				}
				beansStateTracker.register(bean);
				data.add(bean);
				if (lastSelectedBean != null && lastSelectedBean.equals(bean)) {
					selectedTab = Integer.valueOf(index);
				}
				index++;
			}

			finished = true;

			if (selectedTab != null && selectedTab.intValue() >= data.size()) {
				selectedTab = null;
			}

			if (selectedTab == null && data.size() > 0) {
				selectedTab = Integer.valueOf(0);
			}

			beanListModelObservable.fireBeansChanged();
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
			final IBeanMessageBuilder beanMessageBuilder = CapUiToolkit.beanMessageBuilder(BeanMessageType.ERROR);
			beanMessageBuilder.setException(exception);
			beanMessageBuilder.setMessage(loadErrorMessage);
			final IBeanMessage message = beanMessageBuilder.build();
			for (final IBeanProxy<BEAN_TYPE> bean : data) {
				bean.setExecutionTask(null);
				bean.addMessage(message);
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
				for (final IBeanProxy<BEAN_TYPE> bean : data) {
					bean.setExecutionTask(null);
				}
				finished = true;
				canceled = true;
				beanListModelObservable.fireBeansChanged();
			}
		}

	}

}
