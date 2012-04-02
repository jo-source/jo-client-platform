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
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifierBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeFilter;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanMessageBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.bean.IBeansStateTracker;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.model.IModificationStateListener;
import org.jowidgets.cap.ui.api.model.IProcessStateListener;
import org.jowidgets.cap.ui.api.model.ISingleBeanModel;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.cap.ui.api.plugin.IAttributePlugin;
import org.jowidgets.cap.ui.tools.execution.AbstractUiResultCallback;
import org.jowidgets.cap.ui.tools.model.ProcessStateObservable;
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

final class SingleBeanModelImpl<BEAN_TYPE> implements ISingleBeanModel<BEAN_TYPE> {

	private final String loadErrorMessage;
	private final String loadingDataLabel;

	private final Class<BEAN_TYPE> beanType;
	private final Object entityId;

	private final IReaderService<Object> readerService;
	private final IProvider<Object> readerParameterProvider;

	private final BeanListSaveDelegate<BEAN_TYPE> saveDelegate;
	private final BeanListRefreshDelegate<BEAN_TYPE> refreshDelegate;

	private final IBeanListModel<Object> parent;
	private final LinkType linkType;
	private final List<IAttribute<Object>> attributes;
	private final List<String> propertyNames;
	private final Map<String, Object> defaultValues;

	private final ChangeObservable changeObservable;
	private final BeanListModelObservable beanListModelObservable;
	private final BeanSelectionObservable<BEAN_TYPE> beanSelectionObservable;
	private final ProcessStateObservable processStateObservable;

	private final ParentSelectionListener<Object> parentModelListener;
	private final List<IBeanPropertyValidator<BEAN_TYPE>> beanPropertyValidators;
	private final IBeansStateTracker<BEAN_TYPE> beansStateTracker;
	private final IBeanProxyFactory<BEAN_TYPE> beanProxyFactory;

	private IBeanProxy<BEAN_TYPE> bean;
	private DataLoader dataLoader;

	@SuppressWarnings("unchecked")
	SingleBeanModelImpl(
		final Class<? extends BEAN_TYPE> beanType,
		final Object entityId,
		final IReaderService<? extends Object> readerService,
		final IProvider<? extends Object> readerParameterProvider,
		final ICreatorService creatorService,
		final IRefreshService refreshService,
		final IUpdaterService updaterService,
		final IDeleterService deleterService,
		final IBeanExceptionConverter exceptionConverter,
		final Set<IBeanValidator<BEAN_TYPE>> beanValidators,
		final IBeanListModel<Object> parent,
		final LinkType linkType,
		List<IAttribute<Object>> attributes) {

		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(entityId, "entityId");
		Assert.paramNotNull(readerService, "readerService");
		Assert.paramNotNull(readerParameterProvider, "readerParameterProvider");
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");
		Assert.paramNotNull(beanValidators, "beanValidators");
		Assert.paramNotNull(attributes, "attributes");

		this.loadErrorMessage = Messages.getString("BeanTableModelImpl.load_error");
		this.loadingDataLabel = Messages.getString("BeanTableModelImpl.load_data");

		if (parent != null) {
			Assert.paramNotNull(linkType, "linkType");
			final IProvider<Object> parentBeansProvider = new IProvider<Object>() {
				@Override
				public Object get() {
					return getParentBeanKeys();
				}
			};
			this.parentModelListener = new ParentSelectionListener<Object>(this, parentBeansProvider);
			parent.addBeanSelectionListener(parentModelListener);
		}
		else {
			this.parentModelListener = null;
		}

		attributes = createModifiedByPluginsAttributes(entityId, attributes);

		//if no updater service available, set all attributes to editable false
		if (updaterService == null) {
			attributes = createReadonlyAttributes(attributes);
		}

		this.attributes = Collections.unmodifiableList(new LinkedList<IAttribute<Object>>(attributes));
		this.propertyNames = new LinkedList<String>();
		this.defaultValues = new HashMap<String, Object>();
		for (final IAttribute<?> attribute : attributes) {
			final String propertyName = attribute.getPropertyName();
			propertyNames.add(propertyName);
			final Object defaultValue = attribute.getDefaultValue();
			if (defaultValue != null) {
				defaultValues.put(propertyName, defaultValue);
			}
		}

		this.beanPropertyValidators = new LinkedList<IBeanPropertyValidator<BEAN_TYPE>>();
		beanPropertyValidators.add(new BeanPropertyValidatorImpl<BEAN_TYPE>(attributes));
		for (final IBeanValidator<BEAN_TYPE> beanValidator : beanValidators) {
			beanPropertyValidators.add(new BeanPropertyValidatorAdapter<BEAN_TYPE>(beanValidator));
		}

		this.beanType = (Class<BEAN_TYPE>) beanType;
		this.entityId = entityId;

		this.readerService = (IReaderService<Object>) readerService;
		this.readerParameterProvider = (IProvider<Object>) readerParameterProvider;

		this.parent = parent;
		this.linkType = linkType;

		this.beansStateTracker = CapUiToolkit.beansStateTracker();
		this.beanProxyFactory = CapUiToolkit.beanProxyFactory(beanType);

		this.changeObservable = new ChangeObservable();
		this.beanListModelObservable = new BeanListModelObservable();
		this.beanSelectionObservable = new BeanSelectionObservable<BEAN_TYPE>();
		this.processStateObservable = new ProcessStateObservable();

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

	private static List<IAttribute<Object>> createModifiedByPluginsAttributes(
		final Object entityId,
		final List<IAttribute<Object>> attributes) {

		List<IAttribute<Object>> result = attributes;

		final IPluginPropertiesBuilder propBuilder = PluginToolkit.pluginPropertiesBuilder();
		propBuilder.add(IAttributePlugin.ENTITIY_ID_PROPERTY_KEY, entityId);
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
	public Class<BEAN_TYPE> getBeanType() {
		return beanType;
	}

	@Override
	public Object getEntityId() {
		return entityId;
	}

	@Override
	public IAttribute<Object> getAttribute(final int columnIndex) {
		return attributes.get(columnIndex);
	}

	@Override
	public IAttribute<Object> getAttribute(final String propertyName) {
		Assert.paramNotNull(propertyName, "propertyName");
		for (final IAttribute<Object> attribute : attributes) {
			if (propertyName.equals(attribute.getPropertyName())) {
				return attribute;
			}
		}
		return null;
	}

	@Override
	public List<IAttribute<Object>> getAttributes() {
		return attributes;
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
	public void addBeanValidator(final IBeanValidator<BEAN_TYPE> beanValidator) {
		beanPropertyValidators.add(new BeanPropertyValidatorAdapter<BEAN_TYPE>(beanValidator));
	}

	@Override
	public IBeanProxy<BEAN_TYPE> getBean() {
		return bean;
	}

	@Override
	public void setBean(final IBeanProxy<BEAN_TYPE> bean) {
		tryCancelLoader();
		if (this.bean != null) {
			beansStateTracker.unregister(this.bean);
		}
		if (bean != null) {
			beansStateTracker.register(bean);
		}
		this.bean = bean;

		fireSelectionChanged();
		changeObservable.fireChangedEvent();
		beanListModelObservable.fireBeansChanged();
	}

	private void fireSelectionChanged() {
		beanSelectionObservable.fireBeanSelectionEvent(beanType, entityId, getSelectedBeans());
	}

	private List<IBeanProxy<BEAN_TYPE>> getSelectedBeans() {
		if (bean != null) {
			return Collections.singletonList(bean);
		}
		else {
			return Collections.emptyList();
		}
	}

	@Override
	public void clear() {
		tryCancelLoader();
		if (bean != null) {
			bean = null;
			changeObservable.fireChangedEvent();
			beanListModelObservable.fireBeansChanged();
		}
	}

	@Override
	public void load() {
		tryCancelLoader();
		dataLoader = new DataLoader();
		dataLoader.loadData();
	}

	private boolean isDataLoading() {
		return dataLoader != null && !dataLoader.isDisposed();
	}

	@Override
	public void save() {
		saveDelegate.save();
	}

	@Override
	public void refresh() {
		if (bean != null) {
			final List<IBeanProxy<BEAN_TYPE>> beans = Collections.singletonList(bean);
			refreshDelegate.refresh(beans);
		}
	}

	@Override
	public void undo() {
		beansStateTracker.undoModifications();
	}

	private void tryCancelLoader() {
		if (dataLoader != null && !dataLoader.isDisposed()) {
			dataLoader.cancel();
		}
	}

	@Override
	public boolean hasModifications() {
		return beansStateTracker.hasModifications();
	}

	@Override
	public boolean hasExecutions() {
		return beansStateTracker.hasExecutingBeans() || isDataLoading();
	}

	@Override
	public void cancelExecutions() {
		tryCancelLoader();
		beansStateTracker.cancelExecutions();
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
	public void addModificationStateListener(final IModificationStateListener listener) {
		beansStateTracker.addModificationStateListener(listener);
	}

	@Override
	public void removeModificationStateListener(final IModificationStateListener listener) {
		beansStateTracker.removeModificationStateListener(listener);
	}

	@Override
	public void addProcessStateListener(final IProcessStateListener listener) {
		beansStateTracker.addProcessStateListener(listener);
		processStateObservable.addProcessStateListener(listener);
	}

	@Override
	public void removeProcessStateListener(final IProcessStateListener listener) {
		beansStateTracker.removeProcessStateListener(listener);
		processStateObservable.removeProcessStateListener(listener);
	}

	@Override
	public void addChangeListener(final IChangeListener listener) {
		changeObservable.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(final IChangeListener listener) {
		changeObservable.removeChangeListener(listener);
	}

	@Override
	public int getSize() {
		return bean == null ? 0 : 1;
	}

	@Override
	public IBeanProxy<BEAN_TYPE> getBean(final int index) {
		if (bean != null && index == 0) {
			return bean;
		}
		else {
			throw new IndexOutOfBoundsException("Model is empty, no index may match");
		}
	}

	@Override
	public void removeBeans(final Collection<? extends IBeanProxy<BEAN_TYPE>> beans) {
		Assert.paramNotEmpty(beans, "beans");
		if (beans.size() > 1) {
			throw new IllegalArgumentException("Only one bean can be removed from a single bean model");
		}
		if (beans.iterator().next().equals(bean)) {
			setBean(null);
		}
		else {
			throw new IllegalArgumentException("Bean is not set at this model");
		}
	}

	@Override
	public void addBean(final IBeanProxy<BEAN_TYPE> bean) {
		Assert.paramNotNull(bean, "bean");
		if (this.bean == null) {
			setBean(bean);
		}
		else {
			throw new IllegalStateException("No bean could be added to a single bean model that has a bean set");
		}
	}

	@Override
	public IBeanProxy<BEAN_TYPE> addBeanDto(final IBeanDto beanDto) {
		final IBeanProxy<BEAN_TYPE> result = createBeanProxy(beanDto);
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
	public IBeanProxy<BEAN_TYPE> addTransientBean() {
		final IBeanProxy<BEAN_TYPE> result = beanProxyFactory.createTransientProxy(propertyNames, defaultValues);
		for (final IBeanPropertyValidator<BEAN_TYPE> validator : beanPropertyValidators) {
			result.addBeanPropertyValidator(validator);
		}
		addBean(result);
		return result;
	}

	@Override
	public ArrayList<Integer> getSelection() {
		final ArrayList<Integer> result = new ArrayList<Integer>();
		if (bean != null) {
			result.add(0);
			return result;
		}
		return null;
	}

	@Override
	public void setSelection(final Collection<Integer> selection) {
		Assert.paramNotNull(selection, "selection");
		if (selection.size() > 1) {
			throw new IllegalArgumentException("Multi selection is not supported");
		}
		final Integer index = selection.iterator().next();
		Assert.paramNotNull(index, "index");
		if (index.intValue() > 0) {
			throw new IndexOutOfBoundsException("Index must be 0");
		}
	}

	@Override
	public void fireBeansChanged() {
		changeObservable.fireChangedEvent();
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
	public void addBeanSelectionListener(final IBeanSelectionListener<BEAN_TYPE> listener) {
		beanSelectionObservable.addBeanSelectionListener(listener);
	}

	@Override
	public void removeBeanSelectionListener(final IBeanSelectionListener<BEAN_TYPE> listener) {
		beanSelectionObservable.removeBeanSelectionListener(listener);
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

	private class DataLoader {

		private final IUiThreadAccess uiThreadAccess;

		private boolean canceled;
		private boolean finished;

		private IExecutionTask executionTask;

		private IBeanProxy<BEAN_TYPE> oldBean;
		private IBeanProxy<BEAN_TYPE> dummyBean;

		DataLoader() {
			this.uiThreadAccess = Toolkit.getUiThreadAccess();
		}

		void loadData() {
			oldBean = bean;
			if (oldBean != null) {
				beansStateTracker.unregister(oldBean);
			}

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

			final List<ISort> emptySort = Collections.emptyList();
			readerService.read(
					createResultCallback(),
					getParentBeanKeys(),
					null,
					emptySort,
					0,
					1,
					readerParameterProvider.get(),
					executionTask);

			bean = dummyBean;
			changeObservable.fireChangedEvent();
			beanListModelObservable.fireBeansChanged();
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

			bean = null;

			if (beanDtos.size() > 0) {
				final IBeanDto beanDto = beanDtos.iterator().next();
				bean = beanProxyFactory.createProxy(beanDto, propertyNames);
				for (final IBeanPropertyValidator<BEAN_TYPE> validator : beanPropertyValidators) {
					bean.addBeanPropertyValidator(validator);
				}
				beansStateTracker.register(bean);
			}

			finished = true;
			changeObservable.fireChangedEvent();
			beanListModelObservable.fireBeansChanged();
			processStateObservable.fireProcessStateChanged();
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
			changeObservable.fireChangedEvent();
			beanListModelObservable.fireBeansChanged();
			processStateObservable.fireProcessStateChanged();
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
				if (oldBean != null) {
					beansStateTracker.register(oldBean);
				}
				bean = oldBean;

				finished = true;
				canceled = true;

				processStateObservable.fireProcessStateChanged();
				changeObservable.fireChangedEvent();
				beanListModelObservable.fireBeansChanged();
			}
		}

	}

}
