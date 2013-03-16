/*
 * Copyright (c) 2012, grossmann
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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.common.tools.bean.BeanKey;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.BeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanMessageBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.bean.IBeanSelection;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.bean.IBeansStateTracker;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.model.ILabelModel;
import org.jowidgets.cap.ui.api.model.IModificationStateListener;
import org.jowidgets.cap.ui.api.model.IProcessStateListener;
import org.jowidgets.cap.ui.api.plugin.IBeanProxyLabelRendererPlugin;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModel;
import org.jowidgets.cap.ui.api.types.IEntityTypeId;
import org.jowidgets.cap.ui.tools.execution.AbstractUiResultCallback;
import org.jowidgets.common.color.IColorConstant;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.types.Markup;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProperties;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.IProvider;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;

public class BeanRelationNodeModelImpl<PARENT_BEAN_TYPE, CHILD_BEAN_TYPE> implements
		IBeanRelationNodeModel<PARENT_BEAN_TYPE, CHILD_BEAN_TYPE> {

	private static final int MAX_CHILDREN = 200;

	private final ILabelModel label;

	private final IEntityTypeId<PARENT_BEAN_TYPE> parentEntityTypeId;
	private final IBeanProxy<PARENT_BEAN_TYPE> parentBean;
	private final Object parentEntityId;
	private final Class<PARENT_BEAN_TYPE> parentBeanType;
	private final IEntityTypeId<CHILD_BEAN_TYPE> childEntityTypeId;
	private final Object childEntityId;
	private final Class<CHILD_BEAN_TYPE> childBeanType;
	private final IBeanProxyLabelRenderer<CHILD_BEAN_TYPE> childRenderer;
	private final List<IEntityTypeId<Object>> childRelations;
	private final IReaderService<Object> readerService;
	private final IProvider<Object> readerParameterProvider;
	private final ICreatorService creatorService;
	@SuppressWarnings("unused")
	private final IRefreshService refreshService;
	@SuppressWarnings("unused")
	private final IUpdaterService updaterService;
	@SuppressWarnings("unused")
	private final IDeleterService deleterService;
	private final List<ISort> defaultSort;
	@SuppressWarnings("unused")
	private final Set<IBeanValidator<CHILD_BEAN_TYPE>> beanValidators;
	private final Set<IBeanPropertyValidator<CHILD_BEAN_TYPE>> beanPropertyValidators;
	private final List<IAttribute<Object>> childBeanAttributes;
	private final List<String> propertyNames;
	private final Map<String, Object> defaultValues;

	private final String loadErrorMessage;
	private final String loadingDataLabel;

	private final BeanListModelObservable beanListModelObservable;
	private final BeanSelectionObservable<CHILD_BEAN_TYPE> beanSelectionObservable;
	private final IBeansStateTracker<CHILD_BEAN_TYPE> beanStateTracker;
	private final IBeanProxyFactory<CHILD_BEAN_TYPE> beanProxyFactory;

	private final BeanListSaveDelegate<CHILD_BEAN_TYPE> saveDelegate;

	private final ArrayList<IBeanProxy<CHILD_BEAN_TYPE>> data;

	private ArrayList<Integer> selection;
	private DataLoader dataLoader;
	private boolean hasInitialLoad;

	@SuppressWarnings("unchecked")
	BeanRelationNodeModelImpl(
		final ILabelModel label,
		final IBeanProxy<PARENT_BEAN_TYPE> parentBean,
		final IEntityTypeId<PARENT_BEAN_TYPE> parentEntityTypeId,
		final IEntityTypeId<CHILD_BEAN_TYPE> childEntityTypeId,
		final IBeanProxyLabelRenderer<CHILD_BEAN_TYPE> childRenderer,
		final List<IEntityTypeId<Object>> childRelations,
		final IReaderService<? extends Object> readerService,
		final IProvider<? extends Object> readerParameterProvider,
		final ICreatorService creatorService,
		final IRefreshService refreshService,
		final IUpdaterService updaterService,
		final IDeleterService deleterService,
		final Collection<? extends ISort> defaultSort,
		final Set<IBeanValidator<CHILD_BEAN_TYPE>> beanValidators,
		final List<IAttribute<Object>> childBeanAttributes) {

		Assert.paramNotNull(label, "label");
		Assert.paramNotNull(childEntityTypeId, "childEntityTypeId");
		Assert.paramNotNull(childRenderer, "childRenderer");
		Assert.paramNotNull(childRelations, "childRelations");
		Assert.paramNotNull(readerService, "readerService");
		Assert.paramNotNull(readerParameterProvider, "readerParameterProvider");
		Assert.paramNotNull(defaultSort, "defaultSort");
		Assert.paramNotNull(beanValidators, "beanValidators");
		Assert.paramNotNull(childBeanAttributes, "childBeanAttributes");

		this.label = label;
		this.parentBean = parentBean;
		this.parentEntityTypeId = parentEntityTypeId;
		this.parentBeanType = parentEntityTypeId != null ? parentEntityTypeId.getBeanType() : null;
		this.parentEntityId = parentEntityTypeId != null ? parentEntityTypeId.getEntityId() : null;
		this.childEntityTypeId = childEntityTypeId;
		this.childEntityId = childEntityTypeId.getEntityId();
		this.childBeanType = childEntityTypeId.getBeanType();
		this.childRenderer = getPluginDecoratedRenderer(childEntityId, childBeanType, childRenderer);
		this.childRelations = new LinkedList<IEntityTypeId<Object>>(childRelations);
		this.readerService = (IReaderService<Object>) readerService;
		this.readerParameterProvider = (IProvider<Object>) readerParameterProvider;
		this.creatorService = creatorService;
		this.refreshService = refreshService;
		this.updaterService = updaterService;
		this.deleterService = deleterService;
		this.defaultSort = new LinkedList<ISort>(defaultSort);
		this.beanValidators = new LinkedHashSet<IBeanValidator<CHILD_BEAN_TYPE>>(beanValidators);
		this.childBeanAttributes = Collections.unmodifiableList(new LinkedList<IAttribute<Object>>(childBeanAttributes));

		//TODO MG get the validators
		this.beanPropertyValidators = new LinkedHashSet<IBeanPropertyValidator<CHILD_BEAN_TYPE>>();
		this.propertyNames = createPropertyNames(childBeanAttributes);
		this.defaultValues = createDefaultValues(childBeanAttributes);

		this.loadErrorMessage = Messages.getString("BeanTableModelImpl.load_error");
		this.loadingDataLabel = Messages.getString("BeanTableModelImpl.load_data");

		this.beanListModelObservable = new BeanListModelObservable();
		this.beanSelectionObservable = new BeanSelectionObservable<CHILD_BEAN_TYPE>();
		this.beanStateTracker = CapUiToolkit.beansStateTracker();
		this.beanProxyFactory = CapUiToolkit.beanProxyFactory(childBeanType);

		this.data = new ArrayList<IBeanProxy<CHILD_BEAN_TYPE>>();

		this.saveDelegate = new BeanListSaveDelegate<CHILD_BEAN_TYPE>(
			this,
			beanStateTracker,
			BeanExceptionConverter.get(),
			BeanExecutionPolicy.BATCH,
			updaterService,
			creatorService,
			propertyNames,
			false);

		this.hasInitialLoad = false;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static IBeanProxyLabelRenderer getPluginDecoratedRenderer(
		final Object entityId,
		final Class<?> entityType,
		final IBeanProxyLabelRenderer renderer) {
		final IPluginPropertiesBuilder propertiesBuilder = PluginProperties.builder();
		propertiesBuilder.add(IBeanProxyLabelRendererPlugin.ENTITIY_ID_PROPERTY_KEY, entityId);
		propertiesBuilder.add(IBeanProxyLabelRendererPlugin.BEAN_TYPE_PROPERTY_KEY, entityType);
		final IPluginProperties properties = propertiesBuilder.build();
		IBeanProxyLabelRenderer result = renderer;
		for (final IBeanProxyLabelRendererPlugin plugin : PluginProvider.getPlugins(IBeanProxyLabelRendererPlugin.ID, properties)) {
			final IDecorator<IBeanProxyLabelRenderer<?>> decorator = plugin.getRendererDecorator(properties);
			if (decorator != null) {
				result = decorator.decorate(result);
			}
		}
		return result;
	}

	private Map<String, Object> createDefaultValues(final List<IAttribute<Object>> attributes) {
		final Map<String, Object> result = new HashMap<String, Object>();
		for (final IAttribute<Object> attribute : attributes) {
			final Object defaultValue = attribute.getDefaultValue();
			if (defaultValue != null) {
				result.put(attribute.getPropertyName(), defaultValue);
			}
		}
		return result;
	}

	private List<String> createPropertyNames(final List<IAttribute<Object>> attributes) {
		final List<String> result = new LinkedList<String>();
		for (final IAttribute<Object> attribute : attributes) {
			result.add(attribute.getPropertyName());
		}
		return result;
	}

	@Override
	public void dispose() {
		beanStateTracker.dispose();
		beanListModelObservable.dispose();
		beanSelectionObservable.dispose();
		tryToCanceLoader();
	}

	@Override
	public ICreatorService getCreatorService() {
		return creatorService;
	}

	@Override
	public String getText() {
		return label.getText();
	}

	@Override
	public String getDescription() {
		return label.getDescription();
	}

	@Override
	public IImageConstant getIcon() {
		return label.getIcon();
	}

	@Override
	public Integer getFontSize() {
		return label.getFontSize();
	}

	@Override
	public String getFontName() {
		return label.getFontName();
	}

	@Override
	public Markup getMarkup() {
		return label.getMarkup();
	}

	@Override
	public IColorConstant getForegroundColor() {
		return label.getForegroundColor();
	}

	@Override
	public IBeanProxy<PARENT_BEAN_TYPE> getParentBean() {
		return parentBean;
	}

	@Override
	public IEntityTypeId<PARENT_BEAN_TYPE> getParentEntityTypeId() {
		return parentEntityTypeId;
	}

	@Override
	public Object getParentEntityId() {
		return parentEntityId;
	}

	@Override
	public Class<PARENT_BEAN_TYPE> getParentBeanType() {
		return parentBeanType;
	}

	@Override
	public IEntityTypeId<CHILD_BEAN_TYPE> getChildEntityTypeId() {
		return childEntityTypeId;
	}

	@Override
	public Object getChildEntityId() {
		return childEntityId;
	}

	@Override
	public Class<CHILD_BEAN_TYPE> getChildBeanType() {
		return childBeanType;
	}

	@Override
	public IBeanProxyLabelRenderer<CHILD_BEAN_TYPE> getChildRenderer() {
		return childRenderer;
	}

	@Override
	public List<IAttribute<Object>> getChildBeanAttributes() {
		return childBeanAttributes;
	}

	@Override
	public List<IEntityTypeId<Object>> getChildRelations() {
		return childRelations;
	}

	@Override
	public void clear() {
		if (!Toolkit.getUiThreadAccess().isUiThread()) {
			throw new IllegalStateException("Clear must be invoked in the ui thread");
		}
		tryToCanceLoader();
		for (final IBeanProxy<CHILD_BEAN_TYPE> bean : data) {
			beanStateTracker.unregister(bean);
		}
		data.clear();
		beanStateTracker.clearAll();
		beanListModelObservable.fireBeansChanged();
	}

	@Override
	public boolean loadIfNotYetDone() {
		if (!hasInitialLoad) {
			load();
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public void load() {
		if (!Toolkit.getUiThreadAccess().isUiThread()) {
			throw new IllegalStateException("Load must be invoked in the ui thread");
		}
		this.hasInitialLoad = true;
		tryToCanceLoader();
		dataLoader = new DataLoader();
		dataLoader.loadData();
	}

	@Override
	public void save() {
		saveDelegate.save();
	}

	@Override
	public void undo() {
		for (final IBeanProxy<CHILD_BEAN_TYPE> bean : new HashSet<IBeanProxy<CHILD_BEAN_TYPE>>(
			beanStateTracker.getBeansToUpdate())) {
			bean.undoModifications();
		}

		final Set<IBeanProxy<CHILD_BEAN_TYPE>> beansToCreate = beanStateTracker.getBeansToCreate();
		final boolean beansChanged;
		if (!beansToCreate.isEmpty()) {
			removeBeansImpl(beansToCreate, false);
			beansChanged = true;
		}
		else {
			beansChanged = false;
		}
		beanStateTracker.clearModifications();
		if (beansChanged) {
			fireBeansChanged();
		}
	}

	private void tryToCanceLoader() {
		if (dataLoader != null && !dataLoader.isDisposed()) {
			dataLoader.cancel();
		}
	}

	@Override
	public boolean hasModifications() {
		return beanStateTracker.hasModifications();
	}

	@Override
	public boolean hasExecutions() {
		return beanStateTracker.hasExecutingBeans();
	}

	@Override
	public void cancelExecutions() {
		beanStateTracker.cancelExecutions();
	}

	@Override
	public IValidationResult validate() {
		return beanStateTracker.validate();
	}

	@Override
	public void addValidationConditionListener(final IValidationConditionListener listener) {
		beanStateTracker.addValidationConditionListener(listener);
	}

	@Override
	public void removeValidationConditionListener(final IValidationConditionListener listener) {
		beanStateTracker.removeValidationConditionListener(listener);
	}

	@Override
	public void addModificationStateListener(final IModificationStateListener listener) {
		beanStateTracker.addModificationStateListener(listener);
	}

	@Override
	public void removeModificationStateListener(final IModificationStateListener listener) {
		beanStateTracker.removeModificationStateListener(listener);
	}

	@Override
	public void addProcessStateListener(final IProcessStateListener listener) {
		beanStateTracker.addProcessStateListener(listener);
	}

	@Override
	public void removeProcessStateListener(final IProcessStateListener listener) {
		beanStateTracker.removeProcessStateListener(listener);
	}

	@Override
	public int getSize() {
		return data.size();
	}

	@Override
	public IBeanProxy<CHILD_BEAN_TYPE> getBean(final int index) {
		return data.get(index);
	}

	@Override
	public void removeBeans(final Collection<? extends IBeanProxy<CHILD_BEAN_TYPE>> beans) {
		removeBeansImpl(beans, true);
	}

	private void removeBeansImpl(final Collection<? extends IBeanProxy<CHILD_BEAN_TYPE>> beans, final boolean fireBeansChanged) {
		Assert.paramNotNull(beans, "beans");
		tryToCanceLoader();

		for (final IBeanProxy<CHILD_BEAN_TYPE> bean : beans) {
			beanStateTracker.unregister(bean);
			data.remove(bean);
		}

		if (fireBeansChanged) {
			fireBeansChanged();
		}
	}

	@Override
	public void addBean(final IBeanProxy<CHILD_BEAN_TYPE> bean) {
		addBean(data.size(), bean);
	}

	@Override
	public IBeanProxy<CHILD_BEAN_TYPE> addBeanDto(final IBeanDto beanDto) {
		final IBeanProxy<CHILD_BEAN_TYPE> result = createBeanProxy(beanDto);
		addBean(result);
		return result;
	}

	@Override
	public IBeanProxy<CHILD_BEAN_TYPE> addTransientBean() {
		final IBeanProxy<CHILD_BEAN_TYPE> result = beanProxyFactory.createTransientProxy(childBeanAttributes, defaultValues);
		for (final IBeanPropertyValidator<CHILD_BEAN_TYPE> validator : beanPropertyValidators) {
			result.addBeanPropertyValidator(validator);
		}
		addBean(result);
		return result;
	}

	private IBeanProxy<CHILD_BEAN_TYPE> createBeanProxy(final IBeanDto beanDto) {
		final IBeanProxy<CHILD_BEAN_TYPE> beanProxy = beanProxyFactory.createProxy(beanDto, childBeanAttributes);
		for (final IBeanPropertyValidator<CHILD_BEAN_TYPE> validator : beanPropertyValidators) {
			beanProxy.addBeanPropertyValidator(validator);
		}
		return beanProxy;
	}

	public void addBean(final int index, final IBeanProxy<CHILD_BEAN_TYPE> bean) {
		Assert.paramNotNull(bean, "bean");
		beanStateTracker.register(bean);
		data.add(index, bean);
		fireBeansChanged();
	}

	@Override
	public ArrayList<Integer> getSelection() {
		if (selection == null) {
			return new ArrayList<Integer>();
		}
		else {
			return new ArrayList<Integer>(selection);
		}
	}

	@Override
	public List<IBeanProxy<CHILD_BEAN_TYPE>> getSelectedBeans() {
		final List<IBeanProxy<CHILD_BEAN_TYPE>> result = new LinkedList<IBeanProxy<CHILD_BEAN_TYPE>>();
		for (final Integer selectionIndex : getSelection()) {
			result.add(getBean(selectionIndex.intValue()));
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	public IBeanSelection<CHILD_BEAN_TYPE> getBeanSelection() {
		return new BeanSelectionImpl<CHILD_BEAN_TYPE>(childBeanType, childEntityId, getSelectedBeans());
	}

	@Override
	public void setSelection(final Collection<Integer> selection) {
		this.selection = new ArrayList<Integer>(selection);
		beanSelectionObservable.fireBeanSelectionEvent(this, childBeanType, childEntityId, getSelectedBeans());
	}

	@Override
	public void setSelectedBeans(final Collection<? extends IBeanProxy<CHILD_BEAN_TYPE>> selectedBeans) {
		Assert.paramNotNull(selectedBeans, "selectedBeans");

		final Set<Object> selectedMap = new HashSet<Object>(selectedBeans);
		final Set<Integer> newSelection = new LinkedHashSet<Integer>();

		for (int i = 0; i < data.size(); i++) {
			if (selectedMap.contains(data.get(i))) {
				newSelection.add(Integer.valueOf(i));
			}
		}
		setSelection(newSelection);
	}

	@Override
	public void fireBeansChanged() {
		beanListModelObservable.fireBeansChanged();
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
	public void addBeanSelectionListener(final IBeanSelectionListener<CHILD_BEAN_TYPE> listener) {
		beanSelectionObservable.addBeanSelectionListener(listener);
	}

	@Override
	public void removeBeanSelectionListener(final IBeanSelectionListener<CHILD_BEAN_TYPE> listener) {
		beanSelectionObservable.removeBeanSelectionListener(listener);
	}

	private class DataLoader {

		private final IUiThreadAccess uiThreadAccess;
		private final IFilter filter;

		private boolean canceled;
		private boolean finished;

		private IExecutionTask executionTask;

		private IBeanProxy<CHILD_BEAN_TYPE> dummyBean;

		DataLoader() {
			this.uiThreadAccess = Toolkit.getUiThreadAccess();
			this.filter = null;
		}

		void loadData() {
			for (final IBeanProxy<CHILD_BEAN_TYPE> bean : data) {
				beanStateTracker.unregister(bean);
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

			dummyBean = beanProxyFactory.createDummyProxy(childBeanAttributes);
			beanStateTracker.register(dummyBean);
			dummyBean.setExecutionTask(executionTask);
			data.add(dummyBean);

			final List<ISort> sorting = new LinkedList<ISort>(defaultSort);

			final List<? extends IBeanKey> parentBeanKeys;
			if (parentBean != null && !parentBean.isTransient() && !parentBean.isDummy()) {
				parentBeanKeys = Collections.singletonList(new BeanKey(parentBean.getId(), parentBean.getVersion()));
			}
			else {
				parentBeanKeys = Collections.emptyList();
			}

			readerService.read(
					createResultCallback(),
					parentBeanKeys,
					filter,
					sorting,
					0,
					MAX_CHILDREN,
					readerParameterProvider.get(),
					executionTask);
			fireBeansChanged();

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

		@SuppressWarnings("unchecked")
		private void setResult(final List<IBeanDto> beanDtos) {
			if (dummyBean != null) {
				dummyBean.setExecutionTask(null);
				beanStateTracker.unregister(dummyBean);
			}

			data.clear();

			final List<IBeanProxy<CHILD_BEAN_TYPE>> newData = new LinkedList<IBeanProxy<CHILD_BEAN_TYPE>>();
			for (final IBeanDto beanDto : beanDtos) {
				final IBeanProxy<CHILD_BEAN_TYPE> beanProxy;
				if (beanDto instanceof IBeanProxy) {
					beanProxy = (IBeanProxy<CHILD_BEAN_TYPE>) beanDto;
				}
				else {
					beanProxy = beanProxyFactory.createProxy(beanDto, childBeanAttributes);
					beanStateTracker.register(beanProxy);
				}
				newData.add(beanProxy);
			}

			for (final IBeanProxy<CHILD_BEAN_TYPE> bean : newData) {
				for (final IBeanPropertyValidator<CHILD_BEAN_TYPE> validator : beanPropertyValidators) {
					bean.addBeanPropertyValidator(validator);
				}

				data.add(bean);
			}

			finished = true;
			beanListModelObservable.fireBeansChanged();
		}

		private void setException(final Throwable exception) {
			final IBeanMessageBuilder beanMessageBuilder = CapUiToolkit.beanMessageBuilder(BeanMessageType.ERROR);
			beanMessageBuilder.setException(exception);
			beanMessageBuilder.setShortMessage(loadingDataLabel);
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
					beanStateTracker.unregister(dummyBean);
				}
				data.clear();

				finished = true;
				canceled = true;
				beanListModelObservable.fireBeansChanged();
			}
		}

	}

}