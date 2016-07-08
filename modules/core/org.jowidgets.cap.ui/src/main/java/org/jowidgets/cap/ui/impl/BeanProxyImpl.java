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
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanData;
import org.jowidgets.cap.common.api.bean.IBeanDataBuilder;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanDtoBuilder;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.bean.IBeanKeyBuilder;
import org.jowidgets.cap.common.api.bean.IBeanModification;
import org.jowidgets.cap.common.api.bean.IBeanModificationBuilder;
import org.jowidgets.cap.common.api.validation.BeanValidationResult;
import org.jowidgets.cap.common.api.validation.IBeanValidationResult;
import org.jowidgets.cap.common.api.validation.IBeanValidationResultListBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeSet;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanMessageStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanModificationStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProcessStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyListener;
import org.jowidgets.cap.ui.api.bean.IBeanTransientStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanValidationStateListener;
import org.jowidgets.cap.ui.api.bean.ICustomBeanPropertyListener;
import org.jowidgets.cap.ui.api.bean.IExternalBeanValidator;
import org.jowidgets.cap.ui.api.bean.IExternalBeanValidatorListener;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.execution.IExecutionTaskListener;
import org.jowidgets.cap.ui.api.plugin.IBeanProxyPlugin;
import org.jowidgets.cap.ui.tools.execution.ExecutionTaskAdapter;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.PluginProperties;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.tools.validation.ValidationCache;
import org.jowidgets.tools.validation.ValidationCache.IValidationResultCreator;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.EmptyCompatibleEquivalence;
import org.jowidgets.util.ITypedKey;
import org.jowidgets.util.NullCompatibleEquivalence;
import org.jowidgets.util.ValueHolder;
import org.jowidgets.util.collection.IObserverSet;
import org.jowidgets.util.collection.IObserverSetFactory;
import org.jowidgets.util.collection.IObserverSetFactory.Strategy;
import org.jowidgets.util.collection.ObserverSetFactory;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidationResultBuilder;
import org.jowidgets.validation.ValidationResult;

final class BeanProxyImpl<BEAN_TYPE> implements IBeanProxy<BEAN_TYPE>, IValidationResultCreator, IExternalBeanValidatorListener {

	private final Object beanTypeId;
	private final Class<? extends BEAN_TYPE> beanType;
	private final IAttributeSet attributes;
	private final Map<String, IBeanModification> modifications;
	private final Map<String, IBeanModification> undoneModifications;
	private final Map<ITypedKey<? extends Object>, Object> customProperties;
	private final Map<ITypedKey<? extends Object>, IObserverSet<ICustomBeanPropertyListener<BEAN_TYPE, Object>>> customPropertiesListeners;
	private final PropertyChangeObservable propertyChangeObservable;
	private final BeanModificationStateObservable<BEAN_TYPE> modificationStateObservable;
	private final BeanTransientStateObservable<BEAN_TYPE> transientStateObservable;
	private final BeanMessageStateObservable<BEAN_TYPE> messageStateObservable;
	private final BeanProcessStateObservable<BEAN_TYPE> processStateObservable;
	private final BeanValidationStateObservable<BEAN_TYPE> validationStateObservable;
	private final IObserverSet<IBeanProxyListener<BEAN_TYPE>> beanProxyListeners;
	private final ArrayList<IBeanPropertyValidator<BEAN_TYPE>> beanPropertyValidators;
	private final Map<String, IObserverSet<IExternalBeanValidator>> externalValidators;
	private final IExecutionTaskListener executionTaskListener;
	private final Map<String, IValidationResult> validationResults;
	private final ValueHolder<IValidationResult> independentWorstResult;
	private final IUiThreadAccess uiThreadAccess;
	private final ValidationCache validationCache;
	private final boolean isDummy;
	private boolean validateUnmodified;

	private ArrayList<IBeanMessage> infoMessagesList;
	private ArrayList<IBeanMessage> warningMessagesList;
	private ArrayList<IBeanMessage> errorMessagesList;
	private ArrayList<IBeanMessage> messagesList;

	private ArrayList<String> internalObservedProperties;

	private IExecutionTask executionTask;
	private boolean isTransient;
	private boolean isLastRowDummy;
	private String lastProgress;
	private IBeanDto beanDto;
	private BEAN_TYPE proxy;

	private boolean disposed;

	BeanProxyImpl(
		final IBeanDto beanDto,
		final Object beanTypeId,
		final Class<? extends BEAN_TYPE> beanType,
		final IAttributeSet attributes,
		final boolean isDummy,
		final boolean isTransient,
		final boolean isLastRowDummy,
		final boolean validateUnmodified) {
		Assert.paramNotNull(beanDto, "beanDto");
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(attributes, "attributes");

		this.attributes = attributes;

		this.validateUnmodified = validateUnmodified;
		this.disposed = false;
		this.beanDto = beanDto;
		this.beanTypeId = beanTypeId;
		this.beanType = beanType;
		this.isDummy = isDummy;
		this.isLastRowDummy = isLastRowDummy;
		this.isTransient = isTransient;
		this.modifications = new HashMap<String, IBeanModification>();
		this.customProperties = new HashMap<ITypedKey<? extends Object>, Object>();
		this.customPropertiesListeners = new HashMap<ITypedKey<? extends Object>, IObserverSet<ICustomBeanPropertyListener<BEAN_TYPE, Object>>>();
		this.undoneModifications = new HashMap<String, IBeanModification>();
		this.propertyChangeObservable = new PropertyChangeObservable();
		this.modificationStateObservable = new BeanModificationStateObservable<BEAN_TYPE>();
		this.transientStateObservable = new BeanTransientStateObservable<BEAN_TYPE>();
		this.processStateObservable = new BeanProcessStateObservable<BEAN_TYPE>();
		this.messageStateObservable = new BeanMessageStateObservable<BEAN_TYPE>();
		this.validationStateObservable = new BeanValidationStateObservable<BEAN_TYPE>();
		this.beanProxyListeners = ObserverSetFactory.create(IObserverSetFactory.Strategy.LOW_MEMORY);
		this.beanPropertyValidators = new ArrayList<IBeanPropertyValidator<BEAN_TYPE>>(0);
		this.externalValidators = new LinkedHashMap<String, IObserverSet<IExternalBeanValidator>>();
		this.validationResults = new LinkedHashMap<String, IValidationResult>();
		this.independentWorstResult = new ValueHolder<IValidationResult>();
		this.internalObservedProperties = new ArrayList<String>(attributes.getPropertyNames());
		this.validationCache = new ValidationCache(this);

		this.infoMessagesList = new ArrayList<IBeanMessage>(0);
		this.warningMessagesList = new ArrayList<IBeanMessage>(0);
		this.errorMessagesList = new ArrayList<IBeanMessage>(0);
		this.messagesList = new ArrayList<IBeanMessage>(0);

		this.uiThreadAccess = Toolkit.getUiThreadAccess();

		this.executionTaskListener = new ExecutionTaskAdapter() {

			@Override
			public void worked(final int totalWorked) {
				fireProgressPropertyChangeChanged();
			}

			@Override
			public void totalStepCountChanged(final int totalStepCount) {
				fireProgressPropertyChangeChanged();
			}

			@Override
			public void finished() {
				fireProgressPropertyChangeChanged();
			}

		};

		calculateInternalObservedProperties();
		validateAllInternalProperties();
	}

	@Override
	public IBeanProxy<BEAN_TYPE> createCopy() {
		final BeanProxyImpl<BEAN_TYPE> result = new BeanProxyImpl<BEAN_TYPE>(
			beanDto,
			beanTypeId,
			beanType,
			attributes,
			isDummy,
			isTransient,
			isLastRowDummy,
			validateUnmodified);
		for (final IBeanModification modification : modifications.values()) {
			result.setValue(modification.getPropertyName(), modification.getNewValue());
		}
		setValidators(result);
		return result;
	}

	@Override
	public IBeanProxy<BEAN_TYPE> createUnmodifiedCopy() {

		final IBeanDtoBuilder dtoBuilder = CapCommonToolkit.dtoBuilder(getBeanTypeId());
		dtoBuilder.setId(getId());
		dtoBuilder.setVersion(getVersion());
		for (final String propertyName : getProperties()) {
			if (!ALL_META_ATTRIBUTES.contains(propertyName)
				&& !IBean.ID_PROPERTY.equals(propertyName)
				&& !IBean.VERSION_PROPERTY.equals(propertyName)) {
				dtoBuilder.setValue(propertyName, getValue(propertyName));
			}
		}

		final BeanProxyImpl<BEAN_TYPE> result = new BeanProxyImpl<BEAN_TYPE>(
			dtoBuilder.build(),
			beanTypeId,
			beanType,
			attributes,
			isDummy,
			isTransient,
			isLastRowDummy,
			validateUnmodified);
		setValidators(result);

		return result;
	}

	private void setValidators(final BeanProxyImpl<BEAN_TYPE> bean) {
		if (!EmptyCheck.isEmpty(beanPropertyValidators)) {
			bean.addBeanPropertyValidators(beanPropertyValidators);
		}
		bean.calculateInternalObservedProperties();
	}

	@Override
	public Object getBeanTypeId() {
		return beanTypeId;
	}

	@Override
	public Class<? extends BEAN_TYPE> getBeanType() {
		return beanType;
	}

	@Override
	public Object getId() {
		checkDisposed();
		return beanDto.getId();
	}

	@Override
	public long getVersion() {
		checkDisposed();
		return beanDto.getVersion();
	}

	@Override
	public Object getValue(final String propertyName) {
		checkDisposed();
		Assert.paramNotNull(propertyName, "propertyName");
		if (propertyName == IBeanProxy.META_PROPERTY_PROGRESS) {
			return getProgress();
		}
		else if (propertyName == IBeanProxy.META_PROPERTY_MESSAGES) {
			return getMessages();
		}
		if (modifications.containsKey(propertyName)) {
			return modifications.get(propertyName).getNewValue();
		}
		else {
			return beanDto.getValue(propertyName);
		}
	}

	@Override
	public void setValue(final String propertyName, final Object newValue) {
		checkDisposed();
		Assert.paramNotNull(propertyName, "propertyName");

		undoneModifications.clear();

		final Object originalValue = beanDto.getValue(propertyName);
		final Object currentValue = getValue(propertyName);

		//set to the original value
		if (EmptyCompatibleEquivalence.equals(originalValue, newValue)) {
			final boolean oldModificationState = hasModifications();
			modifications.remove(propertyName);
			final boolean newModificationState = hasModifications();
			propertyChange(this, propertyName, currentValue, newValue);
			if (oldModificationState != newModificationState) {
				modificationStateObservable.fireModificationStateChanged(this);
			}
			validateInternalProperty(propertyName);
		}
		else if (!EmptyCompatibleEquivalence.equals(currentValue, newValue)) {
			final IBeanModificationBuilder modBuilder = CapCommonToolkit.beanModificationBuilder();
			modBuilder.setBeanDto(beanDto).setPropertyName(propertyName).setOldValue(originalValue).setNewValue(newValue);
			final boolean oldModificationState = hasModifications();
			modifications.put(propertyName, modBuilder.build());
			final boolean newModificationState = hasModifications();
			propertyChange(this, propertyName, currentValue, newValue);
			if (oldModificationState != newModificationState) {
				modificationStateObservable.fireModificationStateChanged(this);
			}
			validateInternalProperty(propertyName);
		}

	}

	@Override
	public void update(final IBeanDto beanDto) {
		checkDisposed();
		Assert.paramNotNull(beanDto, "beanDto");
		if (!isTransient && !this.beanDto.equals(beanDto)) {
			throw new IllegalArgumentException("The given parameter 'beanDto' must have the same id and type than this proxy");
		}
		fireBeforeBeanUpdate();
		if (isTransient) {
			updateTransient(beanDto);
		}
		else {
			updateImpl(beanDto);
		}
	}

	private void updateTransient(final IBeanDto beanDto) {
		final Object oldId = getId();
		this.isTransient = false;
		updateImpl(beanDto);
		transientStateObservable.fireTransientStateChanged(oldId, this);
	}

	private void updateImpl(final IBeanDto beanDto) {
		final boolean oldModificationState = hasModifications();
		final List<PropertyChangeEvent> propertyChangeEvents = getPropertyChangesForUpdate(beanDto);
		this.beanDto = beanDto;
		modifications.clear();
		clearMessages();
		firePropertyChangeEvents(propertyChangeEvents);
		if (oldModificationState) {
			modificationStateObservable.fireModificationStateChanged(this);
		}
		validateAllInternalProperties();
		fireAfterBeanUpdated();
	}

	@Override
	public Collection<IBeanModification> getModifications() {
		checkDisposed();
		return new HashSet<IBeanModification>(modifications.values());
	}

	@Override
	public void setModifications(final Collection<IBeanModification> modifications) {
		checkDisposed();
		Assert.paramNotNull(modifications, "modifications");
		for (final IBeanModification modification : modifications) {
			setValue(modification.getPropertyName(), modification.getNewValue());
		}
	}

	@Override
	public boolean equalsAllProperties(final IBeanDto bean) {
		return equalsAllProperties(bean, false);
	}

	@Override
	public boolean equalsAllProperties(final IBeanDto bean, final boolean ignoreModifiedProperties) {
		if (bean != null) {
			for (final String property : attributes.getPropertyNames()) {
				final boolean isModiedProperty = modifications.containsKey(property);
				if (!isModiedProperty || !ignoreModifiedProperties) {
					final boolean equal = EmptyCompatibleEquivalence.equals(getValue(property), bean.getValue(property));
					if (!equal) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public IBeanData getBeanData() {
		checkDisposed();
		final IBeanDataBuilder builder = CapCommonToolkit.beanDataBuilder();
		for (final String propertyName : attributes.getPropertyNames()) {
			if (!isTransient() || !propertyName.equals(IBean.ID_PROPERTY)) {
				builder.setProperty(propertyName, getValue(propertyName));
			}
		}
		return builder.build();
	}

	@Override
	public IBeanKey getBeanKey() {
		checkDisposed();
		final IBeanKeyBuilder builder = CapCommonToolkit.beanKeyBuilder();
		builder.setId(getId());
		builder.setVersion(getVersion());
		return builder.build();
	}

	@Override
	public IBeanDto getBeanDto() {
		if (beanDto instanceof IBeanProxy<?>) {
			return ((IBeanProxy<?>) beanDto).getBeanDto();
		}
		else {
			return beanDto;
		}
	}

	@Override
	public Collection<String> getProperties() {
		checkDisposed();
		return attributes.getPropertyNames();
	}

	@Override
	public Collection<IAttribute<Object>> getAttributes() {
		checkDisposed();
		return attributes.getAttributes();
	}

	@Override
	public IAttribute<Object> getAttribute(final String propertyName) {
		checkDisposed();
		return attributes.getAttribute(propertyName);
	}

	@Override
	public boolean hasModifications() {
		checkDisposed();
		return modifications.size() > 0;
	}

	@Override
	public boolean isModified(final String propertyName) {
		checkDisposed();
		Assert.paramNotNull(propertyName, "propertyName");
		return modifications.containsKey(propertyName);
	}

	@Override
	public void undoModifications() {
		checkDisposed();
		fireBeforeUndoModifications();
		final boolean oldModificationState = hasModifications();
		final List<PropertyChangeEvent> propertyChangeEvents = getPropertyChangesForClear();
		undoneModifications.clear();
		for (final Entry<String, IBeanModification> entry : modifications.entrySet()) {
			undoneModifications.put(entry.getKey(), entry.getValue());
		}
		modifications.clear();
		firePropertyChangeEvents(propertyChangeEvents);
		if (oldModificationState) {
			modificationStateObservable.fireModificationStateChanged(this);
		}
		validateAllInternalProperties();
		fireAfterUndoModifications();
	}

	@Override
	public void redoModifications() {
		checkDisposed();
		fireBeforeRedoModifications();
		final boolean oldModificationState = hasModifications();
		final List<PropertyChangeEvent> propertyChangeEvents = getPropertyChangesForUndo();
		for (final Entry<String, IBeanModification> entry : undoneModifications.entrySet()) {
			modifications.put(entry.getKey(), entry.getValue());
		}
		undoneModifications.clear();
		final boolean newModificationState = hasModifications();
		firePropertyChangeEvents(propertyChangeEvents);
		if (oldModificationState != newModificationState) {
			modificationStateObservable.fireModificationStateChanged(this);
		}
		validateAllInternalProperties();
		fireAfterRedoModifications();
	}

	@Override
	public void setValidateUnmodified(final boolean validateUnmodified) {
		if (this.validateUnmodified != validateUnmodified) {
			this.validateUnmodified = validateUnmodified;
			validateAllInternalProperties();
		}
	}

	@Override
	public boolean isValidateUnmodified() {
		return validateUnmodified;
	}

	@Override
	public IValidationResult validate() {
		checkDisposed();
		return validationCache.validate();
	}

	@Override
	public IValidationResult createValidationResult() {
		checkDisposed();
		final IValidationResultBuilder builder = ValidationResult.builder();

		final IValidationResult independentResult = independentWorstResult.get();
		if (independentResult != null && !independentResult.isValid()) {
			return independentResult;
		}

		for (final IValidationResult validationResult : validationResults.values()) {
			if (!validationResult.isValid()) {
				return validationResult;
			}
			else {
				builder.addResult(validationResult);
			}
		}
		return builder.build();
	}

	@Override
	public void validationConditionsChanged(final IExternalBeanValidator externalValidator, Collection<String> properties) {
		checkDisposed();
		final List<IBeanValidationResult> beanValidationResults = new LinkedList<IBeanValidationResult>();
		final ValueHolder<IBeanValidationResult> firstWorstIndependendResultHolder = new ValueHolder<IBeanValidationResult>();
		properties = getDependendProperties(properties);
		for (final String propertyName : properties) {
			beanValidationResults.addAll(validateProperty(propertyName));
		}
		if (beanValidationResults.size() > 0) {
			final Collection<IBeanValidationResult> consolidatedResult;
			consolidatedResult = consolidateBeanValidationResult(firstWorstIndependendResultHolder, beanValidationResults).values();
			setValidationResults(firstWorstIndependendResultHolder, externalValidator.validate(consolidatedResult));
		}
	}

	@Override
	public <PROPERTY_TYPE> void setCustomProperty(final ITypedKey<PROPERTY_TYPE> key, final PROPERTY_TYPE value) {
		Assert.paramNotNull(key, "key");
		final Object oldValue = customProperties.get(key);
		customProperties.put(key, value);
		fireCustomPropertyChangedEvent(key, oldValue, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <PROPERTY_TYPE> PROPERTY_TYPE getCustomProperty(final ITypedKey<PROPERTY_TYPE> key) {
		return (PROPERTY_TYPE) customProperties.get(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <PROPERTY_TYPE> void addCustomPropertyListener(
		final ITypedKey<PROPERTY_TYPE> key,
		final ICustomBeanPropertyListener<BEAN_TYPE, PROPERTY_TYPE> listener) {
		Assert.paramNotNull(listener, "listener");
		final IObserverSet<ICustomBeanPropertyListener<BEAN_TYPE, Object>> listeners = getPropertiesListeners(key);
		listeners.add((ICustomBeanPropertyListener<BEAN_TYPE, Object>) listener);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <PROPERTY_TYPE> void removeCustomPropertyListener(
		final ITypedKey<PROPERTY_TYPE> key,
		final ICustomBeanPropertyListener<BEAN_TYPE, PROPERTY_TYPE> listener) {
		Assert.paramNotNull(listener, "listener");
		getPropertiesListeners(key).remove((ICustomBeanPropertyListener<BEAN_TYPE, Object>) listener);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private <PROPERTY_TYPE> IObserverSet<ICustomBeanPropertyListener<BEAN_TYPE, Object>> getPropertiesListeners(
		final ITypedKey<PROPERTY_TYPE> key) {
		Assert.paramNotNull(key, "key");
		IObserverSet result = customPropertiesListeners.get(key);
		if (result == null) {
			result = ObserverSetFactory.create(IObserverSetFactory.Strategy.LOW_MEMORY);
			customPropertiesListeners.put(key, result);
		}
		return result;
	}

	private void fireCustomPropertyChangedEvent(
		final ITypedKey<? extends Object> key,
		final Object oldValue,
		final Object newValue) {
		for (final ICustomBeanPropertyListener<BEAN_TYPE, Object> listener : getPropertiesListeners(key)) {
			listener.propertyChanged(this, oldValue, newValue);
		}
	}

	private void validateAllInternalProperties() {
		validateInternalProperties(internalObservedProperties);
	}

	private void validateInternalProperty(final String propertyName) {
		validateInternalProperties(Collections.singletonList(propertyName));
	}

	private void validateInternalProperties(Collection<String> propertyNames) {
		propertyNames = getDependendProperties(propertyNames);
		final IBeanValidationResultListBuilder builder = CapCommonToolkit.beanValidationResultListBuilder();
		final ValueHolder<IBeanValidationResult> firstWorstIndependendResultHolder = new ValueHolder<IBeanValidationResult>();
		for (final String propertyName : propertyNames) {
			builder.addResult(validateProperty(propertyName));
		}

		Map<String, IBeanValidationResult> consolidatedResult;
		consolidatedResult = consolidateBeanValidationResult(firstWorstIndependendResultHolder, builder.build());

		//check if the result contains properties that was not validated
		final Set<String> newProperties = new LinkedHashSet<String>();
		for (final String potentiallyNewProperty : consolidatedResult.keySet()) {
			if (!propertyNames.contains(potentiallyNewProperty)) {
				newProperties.add(potentiallyNewProperty);
			}
		}
		//if so, validate external if necessary
		if (!newProperties.isEmpty()) {
			final List<IBeanValidationResult> externalResults = new LinkedList<IBeanValidationResult>();
			for (final String newPropertyName : newProperties) {
				for (final IExternalBeanValidator externalValidator : getRegisteredExternalValidators(newPropertyName)) {
					final IBeanValidationResult parentResult = consolidatedResult.get(newPropertyName);
					if (parentResult != null) {
						externalResults.addAll(externalValidator.validate(Collections.singletonList(parentResult)));
					}
				}
			}
			if (!externalResults.isEmpty()) {
				consolidatedResult = consolidateBeanValidationResult(
						consolidatedResult,
						firstWorstIndependendResultHolder,
						externalResults);
			}
		}

		setValidationResults(firstWorstIndependendResultHolder, consolidatedResult.values());

	}

	private Collection<String> getDependendProperties(final Collection<String> propertyNames) {
		if (EmptyCheck.isEmpty(beanPropertyValidators)) {
			return propertyNames;
		}
		for (final IBeanPropertyValidator<BEAN_TYPE> validator : beanPropertyValidators) {
			//if there are validators depending on all properties all attributes must be validated
			if (EmptyCheck.isEmpty(validator.getPropertyDependencies())) {
				return attributes.getPropertyNames();
			}
		}
		final Set<String> result = new LinkedHashSet<String>();
		for (final String propertyName : propertyNames) {
			result.add(propertyName);
			for (final IBeanPropertyValidator<BEAN_TYPE> validator : beanPropertyValidators) {
				if (validator.getPropertyDependencies().contains(propertyName)) {
					result.addAll(validator.getPropertyDependencies());
				}
			}
		}

		return result;
	}

	private IObserverSet<IExternalBeanValidator> getRegisteredExternalValidators(final String propertyName) {
		final IObserverSet<IExternalBeanValidator> validators = externalValidators.get(propertyName);
		if (validators != null) {
			return validators;
		}
		else {
			return ObserverSetFactory.emptySet();
		}
	}

	private void setValidationResults(
		final ValueHolder<IBeanValidationResult> firstWorstIndependendResultHolder,
		final Collection<IBeanValidationResult> results) {
		if (results.size() > 0) {
			boolean validationChanged = false;

			//set the property dependent results
			for (final IBeanValidationResult result : results) {
				final String propertyName = result.getPropertyName();
				final IValidationResult validationResult = result.getValidationResult();
				final IValidationResult lastResult = validationResults.get(propertyName);
				if (lastResult == null || !validationResult.getWorstFirst().equals(lastResult.getWorstFirst())) {
					if (validationResult.isOk()) {
						validationResults.remove(propertyName);
					}
					else {
						validationResults.put(propertyName, validationResult);
					}
					validationChanged = true;
				}
			}

			//set the property independent result
			final IValidationResult lastIndependentResult = independentWorstResult.get();
			final IBeanValidationResult currentIndependentBeanValidationResult = firstWorstIndependendResultHolder.get();
			if (currentIndependentBeanValidationResult != null) {
				final IValidationResult currentIndependentResult = currentIndependentBeanValidationResult.getValidationResult();
				if (lastIndependentResult == null
					|| !currentIndependentResult.getWorstFirst().equals(lastIndependentResult.getWorstFirst())) {
					independentWorstResult.set(currentIndependentResult);
					validationChanged = true;
				}
			}
			else if (lastIndependentResult != null) {
				independentWorstResult.set(null);
				validationChanged = true;
			}

			if (validationChanged) {
				fireValidationConditionsChanged();
			}
		}
	}

	private List<IBeanValidationResult> validateProperty(final String propertyName) {
		Assert.paramNotNull(propertyName, "propertyName");
		final IBeanValidationResultListBuilder builder = CapCommonToolkit.beanValidationResultListBuilder();
		builder.addResult(ValidationResult.ok(), propertyName);

		if (validateUnmodified || hasModifications()) {
			final String currentLabel = getAttribute(propertyName).getCurrentLabel();
			addValidationResults(beanPropertyValidators, builder, propertyName, currentLabel);
		}

		return builder.build();
	}

	private Map<String, IBeanValidationResult> consolidateBeanValidationResult(
		final ValueHolder<IBeanValidationResult> firstWorstIndependendResultHolder,
		final Collection<IBeanValidationResult> resultList) {

		return consolidateBeanValidationResult(
				new LinkedHashMap<String, IBeanValidationResult>(),
				firstWorstIndependendResultHolder,
				resultList);
	}

	private Map<String, IBeanValidationResult> consolidateBeanValidationResult(
		final Map<String, IBeanValidationResult> currentResultMap,
		final ValueHolder<IBeanValidationResult> firstWorstIndependendResultHolder,
		final Collection<IBeanValidationResult> validationResults) {

		for (final IBeanValidationResult result : validationResults) {
			final String propertyName = result.getPropertyName();
			if (propertyName != null) {
				final IBeanValidationResult currentResult = currentResultMap.get(propertyName);
				final IBeanValidationResult worseResult = getWorseResult(currentResult, result);
				if (worseResult == result) {
					currentResultMap.put(propertyName, result);
				}
			}
			else {
				final IBeanValidationResult firstWorstIndependendResult = firstWorstIndependendResultHolder.get();
				final IBeanValidationResult worseResult = getWorseResult(firstWorstIndependendResult, result);
				if (worseResult == result) {
					firstWorstIndependendResultHolder.set(result);
				}
			}
		}
		return currentResultMap;
	}

	private IBeanValidationResult getWorseResult(final IBeanValidationResult first, final IBeanValidationResult second) {
		if (first == null && second == null) {
			return null;
		}
		else if (first == null) {
			return second;
		}
		else if (second == null) {
			return first;
		}
		else {
			if (second.getValidationResult().getWorstFirst().worse(first.getValidationResult().getWorstFirst())) {
				return second;
			}
			else {
				return first;
			}
		}
	}

	private void addValidationResults(
		final Collection<IBeanPropertyValidator<BEAN_TYPE>> validators,
		final IBeanValidationResultListBuilder builder,
		final String propertyName,
		final String context) {
		if (validators != null) {
			for (final IBeanPropertyValidator<BEAN_TYPE> validator : validators) {
				if (EmptyCheck.isEmpty(validator.getPropertyDependencies())
					|| validator.getPropertyDependencies().contains(propertyName)) {
					final Collection<IBeanValidationResult> results = validator.validateProperty(this, propertyName);
					if (!EmptyCheck.isEmpty(results)) {
						builder.addResult(context, results);
					}
				}
			}
		}
	}

	@Override
	public void addValidationStateListener(final IBeanValidationStateListener<BEAN_TYPE> listener) {
		checkDisposed();
		validationStateObservable.addValidationStateListener(listener);
	}

	@Override
	public void removeValidationStateListener(final IBeanValidationStateListener<BEAN_TYPE> listener) {
		checkDisposed();
		validationStateObservable.removeValidationStateListener(listener);
	}

	@Override
	public void addBeanPropertyValidators(final Collection<? extends IBeanPropertyValidator<BEAN_TYPE>> validators) {
		checkDisposed();
		Assert.paramNotNull(validators, "validators");
		beanPropertyValidators.addAll(validators);
		beanPropertyValidators.trimToSize();
		validateAllInternalProperties();
	}

	@Override
	public void addBeanPropertyValidator(final IBeanPropertyValidator<BEAN_TYPE> validator) {
		checkDisposed();
		Assert.paramNotNull(validator, "validator");
		beanPropertyValidators.add(validator);
		beanPropertyValidators.trimToSize();
		validateAllInternalProperties();
	}

	@Override
	public void registerExternalValidator(final IExternalBeanValidator validator) {
		checkDisposed();
		Assert.paramNotNull(validator, "validator");
		for (final String propertyName : validator.getObservedProperties()) {
			getExternalValidators(propertyName).add(validator);
		}
		validator.addExternalValidatorListener(this);
		calculateInternalObservedProperties();

		//check validate the observed properties of the new validator
		final Collection<String> observedProperties = validator.getObservedProperties();
		if (!observedProperties.isEmpty()) {

			validateInternalProperties(observedProperties);

			final List<IBeanValidationResult> parentResults = new LinkedList<IBeanValidationResult>();
			for (final String propertyName : observedProperties) {
				final IValidationResult parentResult = validationResults.get(propertyName);
				if (parentResult != null && !parentResult.isOk()) {
					parentResults.add(BeanValidationResult.create(propertyName, parentResult));
				}
			}

			final Collection<IBeanValidationResult> externalResults = validator.validate(parentResults);
			if (!externalResults.isEmpty()) {
				final ValueHolder<IBeanValidationResult> independentResults = new ValueHolder<IBeanValidationResult>();
				final Map<String, IBeanValidationResult> consolidatedResult = consolidateBeanValidationResult(
						independentResults,
						externalResults);
				setValidationResults(independentResults, consolidatedResult.values());
			}
		}

	}

	@Override
	public void unregisterExternalValidator(final IExternalBeanValidator validator) {
		checkDisposed();
		Assert.paramNotNull(validator, "validator");
		boolean removed = false;
		for (final IObserverSet<IExternalBeanValidator> validators : externalValidators.values()) {
			removed = validators.remove(validator) || removed;
		}
		validator.removeExternalValidatorListener(this);

		if (removed) {
			calculateInternalObservedProperties();

			Collection<String> observedProperties = validator.getObservedProperties();
			if (EmptyCheck.isEmpty(observedProperties)) {
				observedProperties = attributes.getPropertyNames();
			}

			//remove the results for the observed properties
			final List<String> removedProperties = new LinkedList<String>();
			for (final String observedProperty : observedProperties) {
				if (validationResults.remove(observedProperty) != null) {
					removedProperties.add(observedProperty);
				}
			}

			//Validate the properties that was removed from the result again.
			//Thought about whether this can be improved if result knows its validator
			//but on the other hand, maybe a less worse result will be produced by another
			//validator still existing, so this seems not to be trivial.
			//Because of that, validating the whole property again seems to be more robust
			if (!removedProperties.isEmpty()) {
				validateInternalProperties(removedProperties);
			}
		}
	}

	private IObserverSet<IExternalBeanValidator> getExternalValidators(final String propertyName) {
		IObserverSet<IExternalBeanValidator> result = externalValidators.get(propertyName);
		if (result == null) {
			result = ObserverSetFactory.create(Strategy.LOW_MEMORY);
			externalValidators.put(propertyName, result);
		}
		return result;
	}

	private void calculateInternalObservedProperties() {
		final Set<String> result = new LinkedHashSet<String>(attributes.getPropertyNames());
		for (final IObserverSet<IExternalBeanValidator> externalBeanValidatorsSet : externalValidators.values()) {
			for (final IExternalBeanValidator externalBeanValidator : externalBeanValidatorsSet) {
				result.removeAll(externalBeanValidator.getObservedProperties());
			}
		}
		internalObservedProperties = new ArrayList<String>(result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public BEAN_TYPE getBean() {
		checkDisposed();
		if (proxy == null) {
			proxy = (BEAN_TYPE) Proxy.newProxyInstance(
					beanType.getClassLoader(),
					new Class[] {beanType},
					new BeanProxyInvocationHandler(this, beanType));
		}
		return proxy;
	}

	@Override
	public IExecutionTask getExecutionTask() {
		checkDisposed();
		return executionTask;
	}

	@Override
	public boolean hasExecution() {
		checkDisposed();
		return executionTask != null;
	}

	@Override
	public void setExecutionTask(final IExecutionTask executionTask) {
		checkDisposed();
		if (!NullCompatibleEquivalence.equals(this.executionTask, executionTask)) {
			if (this.executionTask != null) {
				this.executionTask.removeExecutionTaskListener(executionTaskListener);
			}
			this.executionTask = executionTask;
			if (this.executionTask != null) {
				this.executionTask.addExecutionTaskListener(executionTaskListener);
			}
			processStateObservable.fireProcessStateChanged(this);
		}
	}

	@Override
	public void addMessage(final IBeanMessage message) {
		checkDisposed();
		Assert.paramNotNull(message, "message");

		final IBeanMessage modifiedMessage = getModifiedByPluginMessage(message);

		if (modifiedMessage != null) {
			final List<IBeanMessage> lastMessages = new LinkedList<IBeanMessage>(messagesList);

			Assert.paramNotNull(message, "message");
			Assert.paramNotNull(message.getType(), "message.getType()");
			messagesList.add(0, message);
			messagesList.trimToSize();

			if (BeanMessageType.ERROR.equals(message.getType())) {
				errorMessagesList.add(message);
				errorMessagesList.trimToSize();
			}
			else if (BeanMessageType.INFO.equals(message.getType())) {
				infoMessagesList.add(message);
				infoMessagesList.trimToSize();
			}
			else if (BeanMessageType.WARNING.equals(message.getType())) {
				warningMessagesList.add(message);
				warningMessagesList.trimToSize();
			}

			propertyChange(BeanProxyImpl.this, IBeanProxy.META_PROPERTY_MESSAGES, lastMessages, getMessages());

			messageStateObservable.fireMessageStateChanged(this);
		}
	}

	private IBeanMessage getModifiedByPluginMessage(final IBeanMessage message) {
		final IPluginProperties pluginProperties = PluginProperties.create(IBeanProxyPlugin.BEAN_TYPE_PROPERTY_KEY, beanType);
		IBeanMessage result = message;
		for (final IBeanProxyPlugin plugin : PluginProvider.getPlugins(IBeanProxyPlugin.ID, pluginProperties)) {
			result = plugin.addMessage(this, result);
			if (result == null) {
				return null;
			}
		}
		return result;
	}

	@Override
	public IBeanMessage getFirstWorstMessage() {
		checkDisposed();
		if (errorMessagesList.size() > 0) {
			return errorMessagesList.get(errorMessagesList.size() - 1);
		}
		else if (warningMessagesList.size() > 0) {
			return warningMessagesList.get(warningMessagesList.size() - 1);
		}
		else if (infoMessagesList.size() > 0) {
			return infoMessagesList.get(infoMessagesList.size() - 1);
		}
		return null;
	}

	@Override
	public IBeanMessage getFirstWorstMandatoryMessage() {
		checkDisposed();
		IBeanMessage result = getFirstMandatoryMessage(errorMessagesList);
		if (result == null) {
			result = getFirstMandatoryMessage(warningMessagesList);
		}
		if (result == null) {
			result = getFirstMandatoryMessage(infoMessagesList);
		}
		return result;
	}

	private IBeanMessage getFirstMandatoryMessage(final List<IBeanMessage> beanMessages) {
		for (final IBeanMessage message : beanMessages) {
			if (message.isFixMandatory()) {
				return message;
			}
		}
		return null;
	}

	@Override
	public List<IBeanMessage> getMessages() {
		checkDisposed();
		return Collections.unmodifiableList(messagesList);
	}

	@Override
	public boolean hasInfos() {
		checkDisposed();
		return !infoMessagesList.isEmpty();
	}

	@Override
	public boolean hasErrors() {
		checkDisposed();
		return !errorMessagesList.isEmpty();
	}

	@Override
	public boolean hasWarnings() {
		checkDisposed();
		return !warningMessagesList.isEmpty();
	}

	@Override
	public boolean hasMessages() {
		return hasInfos() || hasWarnings() || hasErrors();
	}

	@Override
	public void clearMessages() {
		checkDisposed();
		messagesList = new ArrayList<IBeanMessage>(0);
		infoMessagesList = new ArrayList<IBeanMessage>(0);
		warningMessagesList = new ArrayList<IBeanMessage>(0);
		errorMessagesList = new ArrayList<IBeanMessage>(0);
		messageStateObservable.fireMessageStateChanged(this);
	}

	@Override
	public boolean isTransient() {
		checkDisposed();
		return isTransient;
	}

	@Override
	public boolean isDummy() {
		checkDisposed();
		return isDummy;
	}

	@Override
	public boolean isLastRowDummy() {
		checkDisposed();
		return isLastRowDummy;
	}

	@Override
	public void clearLastRowDummyState() {
		checkDisposed();
		isLastRowDummy = false;
		fireValidationConditionsChanged();
	}

	@Override
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		checkDisposed();
		propertyChangeObservable.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(final PropertyChangeListener listener) {
		checkDisposed();
		propertyChangeObservable.removePropertyChangeListener(listener);
	}

	@Override
	public void addModificationStateListener(final IBeanModificationStateListener<BEAN_TYPE> listener) {
		checkDisposed();
		modificationStateObservable.addModificationStateListener(listener);
	}

	@Override
	public void removeModificationStateListener(final IBeanModificationStateListener<BEAN_TYPE> listener) {
		checkDisposed();
		modificationStateObservable.removeModificationStateListener(listener);
	}

	@Override
	public void addTransientStateListener(final IBeanTransientStateListener<BEAN_TYPE> listener) {
		checkDisposed();
		transientStateObservable.addTransientStateListener(listener);
	}

	@Override
	public void removeTransientStateListener(final IBeanTransientStateListener<BEAN_TYPE> listener) {
		checkDisposed();
		transientStateObservable.removeTransientStateListener(listener);
	}

	@Override
	public void addProcessStateListener(final IBeanProcessStateListener<BEAN_TYPE> listener) {
		checkDisposed();
		processStateObservable.addProcessStateListener(listener);
	}

	@Override
	public void removeProcessStateListener(final IBeanProcessStateListener<BEAN_TYPE> listener) {
		checkDisposed();
		processStateObservable.removeProcessStateListener(listener);
	}

	@Override
	public void addMessageStateListener(final IBeanMessageStateListener<BEAN_TYPE> listener) {
		checkDisposed();
		messageStateObservable.addMessageStateListener(listener);
	}

	@Override
	public void removeMessageStateListener(final IBeanMessageStateListener<BEAN_TYPE> listener) {
		checkDisposed();
		messageStateObservable.removeMessageStateListener(listener);
	}

	@Override
	public void addBeanProxyListener(final IBeanProxyListener<BEAN_TYPE> listener) {
		checkDisposed();
		beanProxyListeners.add(listener);
	}

	@Override
	public void removeBeanProxyListener(final IBeanProxyListener<BEAN_TYPE> listener) {
		checkDisposed();
		beanProxyListeners.remove(listener);
	}

	private void fireBeforeBeanUpdate() {
		for (final IBeanProxyListener<BEAN_TYPE> listener : beanProxyListeners) {
			listener.beforeBeanUpdate(this);
		}
	}

	private void fireAfterBeanUpdated() {
		for (final IBeanProxyListener<BEAN_TYPE> listener : beanProxyListeners) {
			listener.afterBeanUpdated(this);
		}
	}

	private void fireBeforeUndoModifications() {
		for (final IBeanProxyListener<BEAN_TYPE> listener : beanProxyListeners) {
			listener.beforeUndoModifications(this);
		}
	}

	private void fireAfterUndoModifications() {
		for (final IBeanProxyListener<BEAN_TYPE> listener : beanProxyListeners) {
			listener.afterUndoModifications(this);
		}
	}

	private void fireBeforeRedoModifications() {
		for (final IBeanProxyListener<BEAN_TYPE> listener : beanProxyListeners) {
			listener.beforeRedoModifications(this);
		}
	}

	void fireAfterRedoModifications() {
		for (final IBeanProxyListener<BEAN_TYPE> listener : beanProxyListeners) {
			listener.afterRedoModifications(this);
		}
	}

	@Override
	public void dispose() {
		if (!disposed) {
			if (this.executionTask != null) {
				this.executionTask.removeExecutionTaskListener(executionTaskListener);
			}
			customProperties.clear();
			customPropertiesListeners.clear();
			modifications.clear();
			modificationStateObservable.dispose();
			processStateObservable.dispose();
			messageStateObservable.dispose();
			propertyChangeObservable.dispose();
			validationStateObservable.dispose();
			executionTask = null;
			beanDto = null;
			proxy = null;
			disposed = true;
		}
	}

	@Override
	public boolean isDisposed() {
		return disposed;
	}

	private void checkDisposed() {
		if (disposed) {
			throw new IllegalStateException("The bean is diposed");
		}
	}

	private void fireValidationConditionsChanged() {
		validationCache.setDirty();
		validationStateObservable.fireValidationStateChanged(this);
	}

	private String getProgress() {
		String result = null;
		final IExecutionTask currentExecutionTask = executionTask;
		if (currentExecutionTask != null) {
			final int worked = currentExecutionTask.getWorked();
			final Integer total = currentExecutionTask.getTotalStepCount();
			if (total != null) {
				final double percent = (((double) worked) * 100) / total;
				result = "" + Math.round(percent) + "%";
			}
			else {
				result = "?";
			}
		}
		lastProgress = result;
		return result;
	}

	private List<PropertyChangeEvent> getPropertyChangesForClear() {
		final List<PropertyChangeEvent> result = new LinkedList<PropertyChangeEvent>();
		for (final Entry<String, IBeanModification> modificationEntry : modifications.entrySet()) {
			final String propertyName = modificationEntry.getKey();
			result.add(new PropertyChangeEvent(
				this,
				propertyName,
				modificationEntry.getValue().getNewValue(),
				beanDto.getValue(propertyName)));
		}
		return result;
	}

	private List<PropertyChangeEvent> getPropertyChangesForUndo() {
		final List<PropertyChangeEvent> result = new LinkedList<PropertyChangeEvent>();
		for (final Entry<String, IBeanModification> modificationEntry : undoneModifications.entrySet()) {
			final String propertyName = modificationEntry.getKey();
			result.add(new PropertyChangeEvent(
				this,
				propertyName,
				modificationEntry.getValue().getNewValue(),
				beanDto.getValue(propertyName)));
		}
		return result;
	}

	private List<PropertyChangeEvent> getPropertyChangesForUpdate(final IBeanDto beanDto) {
		final List<PropertyChangeEvent> result = new LinkedList<PropertyChangeEvent>();
		for (final String propertyName : attributes.getPropertyNames()) {
			final Object oldValue = getValue(propertyName);
			final Object newValue = beanDto.getValue(propertyName);
			if (!NullCompatibleEquivalence.equals(oldValue, newValue)) {
				result.add(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
			}
		}
		return result;
	}

	private void fireProgressPropertyChangeChanged() {
		uiThreadAccess.invokeLater(new Runnable() {
			@Override
			public void run() {
				final String last = lastProgress;
				final String prog = getProgress();
				if (!NullCompatibleEquivalence.equals(last, prog)) {
					propertyChange(BeanProxyImpl.this, IBeanProxy.META_PROPERTY_PROGRESS, last, prog);
				}
			}
		});

	}

	private void firePropertyChangeEvents(final List<PropertyChangeEvent> events) {
		for (final PropertyChangeEvent event : events) {
			propertyChangeObservable.firePropertyChange(event);
		}
	}

	private void propertyChange(final Object source, final String propertyName, final Object oldValue, final Object newValue) {
		final PropertyChangeEvent event = new PropertyChangeEvent(source, propertyName, oldValue, newValue);
		propertyChangeObservable.firePropertyChange(event);
	}

	@Override
	public String toString() {
		checkDisposed();
		return "BeanProxyImpl [beanType="
			+ beanType
			+ ", modifications="
			+ modifications
			+ ", executionTask="
			+ executionTask
			+ ", beanDto="
			+ beanDto
			+ "]";
	}

	@Override
	public int hashCode() {
		checkDisposed();
		return beanDto.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		checkDisposed();
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		else if (obj instanceof IBeanProxy<?>) {
			if (beanDto.getId() != null) {
				return beanDto.equals(((IBeanProxy<?>) obj).getBeanDto());
			}
		}
		else if (obj instanceof IBeanDto) {
			if (beanDto.getId() != null) {
				return beanDto.equals(obj);
			}
		}
		return false;
	}

}
