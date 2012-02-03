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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifierBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeFilter;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeansStateTracker;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.model.IModificationStateListener;
import org.jowidgets.cap.ui.api.model.IProcessStateListener;
import org.jowidgets.cap.ui.api.model.ISingleBeanModel;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IProvider;
import org.jowidgets.util.concurrent.DaemonThreadFactory;
import org.jowidgets.util.event.ChangeObservable;
import org.jowidgets.util.event.IChangeListener;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;

final class SingleBeanModelImpl<BEAN_TYPE> implements ISingleBeanModel<BEAN_TYPE> {

	private static final int LISTENER_DELAY = 100;

	private final Class<BEAN_TYPE> beanType;
	private final Object entityId;

	@SuppressWarnings("unused")
	private final IReaderService<? extends Object> readerService;
	@SuppressWarnings("unused")
	private final IProvider<? extends Object> readerParameterProvider;
	@SuppressWarnings("unused")
	private final ICreatorService creatorService;
	@SuppressWarnings("unused")
	private final IRefreshService refreshService;
	@SuppressWarnings("unused")
	private final IUpdaterService updaterService;
	@SuppressWarnings("unused")
	private final IDeleterService deleterService;

	@SuppressWarnings("unused")
	private final Set<IBeanValidator<BEAN_TYPE>> beanValidators;

	@SuppressWarnings("unused")
	private final IBeanListModel<?> parent;
	@SuppressWarnings("unused")
	private final LinkType linkType;
	private final List<IAttribute<Object>> attributes;

	private final ParentModelListener parentModelListener;
	private final List<IBeanPropertyValidator<BEAN_TYPE>> beanPropertyValidators;
	@SuppressWarnings("unused")
	private final List<IBeanPropertyValidator<BEAN_TYPE>> beanPropertyValidatorsView;
	private final IBeansStateTracker<BEAN_TYPE> beansStateTracker;
	private final ChangeObservable changeObservable;
	@SuppressWarnings("unused")
	private final IBeanProxyFactory<BEAN_TYPE> beanProxyFactory;

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
		final Set<IBeanValidator<BEAN_TYPE>> beanValidators,
		final IBeanListModel<?> parent,
		final LinkType linkType,
		List<IAttribute<Object>> attributes) {

		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(entityId, "entityId");
		Assert.paramNotNull(readerService, "readerService");
		Assert.paramNotNull(readerParameterProvider, "readerParameterProvider");
		Assert.paramNotNull(beanValidators, "beanValidators");
		Assert.paramNotNull(attributes, "attributes");

		if (parent != null) {
			Assert.paramNotNull(linkType, "linkType");
			this.parentModelListener = new ParentModelListener();
			parent.addBeanListModelListener(parentModelListener);
		}
		else {
			this.parentModelListener = null;
		}

		//if no updater service available, set all attributes to editable false
		if (updaterService == null) {
			attributes = createReadonlyAttributes(attributes);
		}

		this.attributes = Collections.unmodifiableList(new LinkedList<IAttribute<Object>>(attributes));

		this.beanPropertyValidators = new LinkedList<IBeanPropertyValidator<BEAN_TYPE>>();
		this.beanPropertyValidatorsView = Collections.unmodifiableList(beanPropertyValidators);
		beanPropertyValidators.add(new BeanPropertyValidatorImpl<BEAN_TYPE>(attributes));
		for (final IBeanValidator<BEAN_TYPE> beanValidator : beanValidators) {
			addBeanValidator(beanValidator);
		}

		this.beanType = (Class<BEAN_TYPE>) beanType;
		this.entityId = entityId;

		this.readerService = readerService;
		this.readerParameterProvider = readerParameterProvider;
		this.creatorService = creatorService;
		this.refreshService = refreshService;
		this.updaterService = updaterService;
		this.deleterService = deleterService;

		this.beanValidators = beanValidators;
		this.parent = parent;
		this.linkType = linkType;

		this.beansStateTracker = CapUiToolkit.beansStateTracker();
		this.beanProxyFactory = CapUiToolkit.beanProxyFactory(beanType);
		this.changeObservable = new ChangeObservable();
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
		return null;
	}

	@Override
	public void setBean(final IBeanProxy<BEAN_TYPE> bean) {}

	@Override
	public void clear() {

	}

	@Override
	public void load() {

	}

	@Override
	public void save() {

	}

	@Override
	public void undo() {

	}

	@Override
	public boolean hasModifications() {
		return beansStateTracker.hasModifiedBeans();
	}

	@Override
	public boolean hasExecutions() {
		return beansStateTracker.hasExecutingBeans();
	}

	@Override
	public void cancelExecutions() {
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
	}

	@Override
	public void removeProcessStateListener(final IProcessStateListener listener) {
		beansStateTracker.removeProcessStateListener(listener);
	}

	@Override
	public void addChangeListener(final IChangeListener listener) {
		changeObservable.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(final IChangeListener listener) {
		changeObservable.removeChangeListener(listener);
	}

	private class ParentModelListener implements IBeanListModelListener {
		private ScheduledFuture<?> schedule;

		@Override
		public void selectionChanged() {
			loadBean();
		}

		@Override
		public void beansChanged() {
			loadBean();
		}

		private void loadBean() {
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

}
