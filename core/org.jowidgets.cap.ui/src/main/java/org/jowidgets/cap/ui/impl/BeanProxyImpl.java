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
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanModification;
import org.jowidgets.cap.common.api.bean.IBeanModificationBuilder;
import org.jowidgets.cap.common.api.validation.IBeanValidationResult;
import org.jowidgets.cap.common.api.validation.IBeanValidationResultListBuilder;
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
import org.jowidgets.cap.ui.api.bean.IExternalBeanValidator;
import org.jowidgets.cap.ui.api.bean.IExternalBeanValidatorListener;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.execution.IExecutionTaskListener;
import org.jowidgets.cap.ui.tools.execution.ExecutionTaskAdapter;
import org.jowidgets.tools.validation.ValidationCache;
import org.jowidgets.tools.validation.ValidationCache.IValidationResultCreator;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.EmptyCompatibleEquivalence;
import org.jowidgets.util.ITypedKey;
import org.jowidgets.util.NullCompatibleEquivalence;
import org.jowidgets.util.ValueHolder;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidationResultBuilder;
import org.jowidgets.validation.ValidationResult;

final class BeanProxyImpl<BEAN_TYPE> implements IBeanProxy<BEAN_TYPE>, IValidationResultCreator, IExternalBeanValidatorListener {

	private final Class<? extends BEAN_TYPE> beanType;
	private final List<String> properties;
	private final Map<String, IBeanModification> modifications;
	private final Map<String, IBeanModification> undoneModifications;
	private final Map<ITypedKey<? extends Object>, Object> addedProperties;
	private final PropertyChangeObservable propertyChangeObservable;
	private final BeanModificationStateObservable<BEAN_TYPE> modificationStateObservable;
	private final BeanTransientStateObservable<BEAN_TYPE> transientStateObservable;
	private final BeanMessageStateObservable<BEAN_TYPE> messageStateObservable;
	private final BeanProcessStateObservable<BEAN_TYPE> processStateObservable;
	private final BeanValidationStateObservable<BEAN_TYPE> validationStateObservable;
	private final Set<IBeanProxyListener<BEAN_TYPE>> beanProxyListeners;
	private final Map<String, Set<IBeanPropertyValidator<BEAN_TYPE>>> dependendBeanPropertyValidators;
	private final Set<IBeanPropertyValidator<BEAN_TYPE>> independentBeanPropertyValidators;
	private final Map<String, Set<IExternalBeanValidator>> externalValidators;
	private final Set<String> internalObservedProperties;
	private final IExecutionTaskListener executionTaskListener;
	private final Map<String, IValidationResult> validationResults;
	private final ValueHolder<IValidationResult> independentWorstResult;
	private final Map<BeanMessageType, List<IBeanMessage>> messagesMap;
	private final List<IBeanMessage> messagesList;
	private final IUiThreadAccess uiThreadAccess;
	private final ValidationCache validationCache;

	private IExecutionTask executionTask;
	private boolean isTransient;
	private String lastProgress;
	private IBeanDto beanDto;
	private BEAN_TYPE proxy;
	private boolean isDummy;
	private boolean disposed;

	BeanProxyImpl(
		final IBeanDto beanDto,
		final Class<? extends BEAN_TYPE> beanType,
		final List<String> properties,
		final boolean isTransient) {
		Assert.paramNotNull(beanDto, "beanDto");
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(properties, "properties");

		this.disposed = false;
		this.beanDto = beanDto;
		this.beanType = beanType;
		this.properties = new LinkedList<String>(properties);
		this.isTransient = isTransient;
		this.modifications = new HashMap<String, IBeanModification>();
		this.addedProperties = new HashMap<ITypedKey<? extends Object>, Object>();
		this.undoneModifications = new HashMap<String, IBeanModification>();
		this.propertyChangeObservable = new PropertyChangeObservable();
		this.modificationStateObservable = new BeanModificationStateObservable<BEAN_TYPE>();
		this.transientStateObservable = new BeanTransientStateObservable<BEAN_TYPE>();
		this.processStateObservable = new BeanProcessStateObservable<BEAN_TYPE>();
		this.messageStateObservable = new BeanMessageStateObservable<BEAN_TYPE>();
		this.validationStateObservable = new BeanValidationStateObservable<BEAN_TYPE>();
		this.beanProxyListeners = new LinkedHashSet<IBeanProxyListener<BEAN_TYPE>>();
		this.independentBeanPropertyValidators = new HashSet<IBeanPropertyValidator<BEAN_TYPE>>();
		this.dependendBeanPropertyValidators = new LinkedHashMap<String, Set<IBeanPropertyValidator<BEAN_TYPE>>>();
		this.externalValidators = new LinkedHashMap<String, Set<IExternalBeanValidator>>();
		this.validationResults = new HashMap<String, IValidationResult>();
		this.independentWorstResult = new ValueHolder<IValidationResult>();
		this.internalObservedProperties = new HashSet<String>(properties);
		this.validationCache = new ValidationCache(this);

		this.messagesMap = new HashMap<BeanMessageType, List<IBeanMessage>>();
		messagesMap.put(BeanMessageType.INFO, new LinkedList<IBeanMessage>());
		messagesMap.put(BeanMessageType.WARNING, new LinkedList<IBeanMessage>());
		messagesMap.put(BeanMessageType.ERROR, new LinkedList<IBeanMessage>());
		this.messagesList = new LinkedList<IBeanMessage>();

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
		updateImpl(beanDto);
	}

	@Override
	public void updateTransient(final IBeanDto beanDto) {
		checkDisposed();
		Assert.paramNotNull(beanDto, "beanDto");
		if (!isTransient) {
			throw new IllegalStateException("This bean is not transient");
		}
		final Object oldId = getId();
		fireBeforeBeanUpdate();
		setTransient(false);
		updateImpl(beanDto);
		transientStateObservable.fireTransientStateChanged(oldId, this);
	}

	private void updateImpl(final IBeanDto beanDto) {
		final boolean oldModificationState = hasModifications();
		final List<PropertyChangeEvent> propertyChangeEvents = getPropertyChangesForUpdate(beanDto);
		this.beanDto = beanDto;
		modifications.clear();
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
	public void validationConditionsChanged(final IExternalBeanValidator externalValidator, final Collection<String> properties) {
		checkDisposed();
		final List<IBeanValidationResult> beanValidationResults = new LinkedList<IBeanValidationResult>();
		final ValueHolder<IBeanValidationResult> firstWorstIndependendResultHolder = new ValueHolder<IBeanValidationResult>();
		for (final String propertyName : properties) {
			beanValidationResults.addAll(validateProperty(firstWorstIndependendResultHolder, propertyName));
		}
		if (beanValidationResults.size() > 0) {
			final Collection<IBeanValidationResult> consolidatedResult;
			consolidatedResult = consolidateBeanValidationResult(firstWorstIndependendResultHolder, beanValidationResults).values();
			setValidationResults(firstWorstIndependendResultHolder, externalValidator.validate(consolidatedResult));
		}
	}

	@Override
	public <PROPERTY_TYPE> void putProperty(final ITypedKey<PROPERTY_TYPE> key, final PROPERTY_TYPE value) {
		Assert.paramNotNull(key, "key");
		addedProperties.put(key, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <PROPERTY_TYPE> PROPERTY_TYPE getProperty(final ITypedKey<PROPERTY_TYPE> key) {
		return (PROPERTY_TYPE) addedProperties.get(key);
	}

	private void validateAllInternalProperties() {
		validateInternalProperties(internalObservedProperties);
	}

	private void validateInternalProperty(final String propertyName) {
		validateInternalProperties(Collections.singletonList(propertyName));
	}

	private void validateInternalProperties(final Collection<String> propertyNames) {
		final IBeanValidationResultListBuilder builder = CapCommonToolkit.beanValidationResultListBuilder();
		final ValueHolder<IBeanValidationResult> firstWorstIndependendResultHolder = new ValueHolder<IBeanValidationResult>();
		for (final String propertyName : propertyNames) {
			builder.addResult(validateProperty(firstWorstIndependendResultHolder, propertyName));
		}

		Map<String, IBeanValidationResult> consolidatedResult;
		consolidatedResult = consolidateBeanValidationResult(firstWorstIndependendResultHolder, builder.build());

		//check if the result contains properties that was not validated
		final Set<String> newProperties = new HashSet<String>();
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
				consolidatedResult = consolidateBeanValidationResult(firstWorstIndependendResultHolder, externalResults);
			}
		}

		setValidationResults(firstWorstIndependendResultHolder, consolidatedResult.values());

	}

	private Set<IExternalBeanValidator> getRegisteredExternalValidators(final String propertyName) {
		final Set<IExternalBeanValidator> result = new LinkedHashSet<IExternalBeanValidator>();
		final Set<IExternalBeanValidator> validators = externalValidators.get(propertyName);
		if (validators != null) {
			result.addAll(validators);
		}
		return result;
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
					validationResults.put(propertyName, validationResult);
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

	private List<IBeanValidationResult> validateProperty(
		final ValueHolder<IBeanValidationResult> firstWorstIndependendResultHolder,
		final String propertyName) {
		Assert.paramNotNull(propertyName, "propertyName");
		final IBeanValidationResultListBuilder builder = CapCommonToolkit.beanValidationResultListBuilder();
		builder.addResult(ValidationResult.ok(), propertyName);

		addValidationResults(independentBeanPropertyValidators, builder, propertyName);
		addValidationResults(dependendBeanPropertyValidators.get(propertyName), builder, propertyName);

		return builder.build();
	}

	private Map<String, IBeanValidationResult> consolidateBeanValidationResult(
		final ValueHolder<IBeanValidationResult> firstWorstIndependendResultHolder,
		final Collection<IBeanValidationResult> resultList) {
		final Map<String, IBeanValidationResult> resultMap = new HashMap<String, IBeanValidationResult>();
		for (final IBeanValidationResult result : resultList) {
			final String propertyName = result.getPropertyName();
			if (propertyName != null) {
				final IBeanValidationResult currentResult = resultMap.get(propertyName);
				final IBeanValidationResult worseResult = getWorseResult(currentResult, result);
				if (worseResult == result) {
					resultMap.put(propertyName, result);
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
		return resultMap;
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
		final String propertyName) {
		if (validators != null) {
			for (final IBeanPropertyValidator<BEAN_TYPE> validator : validators) {
				final Collection<IBeanValidationResult> validationResult = validator.validateProperty(this, propertyName);
				if (!EmptyCheck.isEmpty(validationResult)) {
					builder.addResult(validationResult);
				}
				else {
					builder.addResult(ValidationResult.ok(), propertyName);
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
	public void addBeanPropertyValidator(final IBeanPropertyValidator<BEAN_TYPE> validator) {
		checkDisposed();
		Assert.paramNotNull(validator, "validator");
		final Set<String> propertyDependencies = validator.getPropertyDependencies();
		if (EmptyCheck.isEmpty(propertyDependencies)) {
			independentBeanPropertyValidators.add(validator);
		}
		else {
			for (final String propertyName : propertyDependencies) {
				getDependentBeanPropertyValidators(propertyName).add(validator);
			}
		}
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
	}

	@Override
	public void unregisterExternalValidator(final IExternalBeanValidator validator) {
		checkDisposed();
		Assert.paramNotNull(validator, "validator");
		for (final Set<IExternalBeanValidator> validators : externalValidators.values()) {
			validators.remove(validator);
		}
		validator.removeExternalValidatorListener(this);
		calculateInternalObservedProperties();
	}

	private Set<IBeanPropertyValidator<BEAN_TYPE>> getDependentBeanPropertyValidators(final String propertyName) {
		Set<IBeanPropertyValidator<BEAN_TYPE>> result = dependendBeanPropertyValidators.get(propertyName);
		if (result == null) {
			result = new HashSet<IBeanPropertyValidator<BEAN_TYPE>>();
			dependendBeanPropertyValidators.put(propertyName, result);
		}
		return result;
	}

	private Set<IExternalBeanValidator> getExternalValidators(final String propertyName) {
		Set<IExternalBeanValidator> result = externalValidators.get(propertyName);
		if (result == null) {
			result = new LinkedHashSet<IExternalBeanValidator>();
			externalValidators.put(propertyName, result);
		}
		return result;
	}

	private void calculateInternalObservedProperties() {
		final Set<String> result = new HashSet<String>(properties);

		for (final Set<IExternalBeanValidator> externalBeanValidatorsSet : externalValidators.values()) {
			for (final IExternalBeanValidator externalBeanValidator : externalBeanValidatorsSet) {
				result.removeAll(externalBeanValidator.getObservedProperties());
			}
		}
		internalObservedProperties.clear();
		internalObservedProperties.addAll(result);
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
		final List<IBeanMessage> lastMessages = new LinkedList<IBeanMessage>(messagesList);

		Assert.paramNotNull(message, "message");
		Assert.paramNotNull(message.getType(), "message.getType()");
		messagesList.add(0, message);
		messagesMap.get(message.getType()).add(0, message);

		propertyChange(BeanProxyImpl.this, IBeanProxy.META_PROPERTY_MESSAGES, lastMessages, getMessages());

		messageStateObservable.fireMessageStateChanged(this);
	}

	@Override
	public IBeanMessage getFirstWorstMessage() {
		checkDisposed();
		if (messagesMap.get(BeanMessageType.ERROR).size() > 0) {
			return messagesMap.get(BeanMessageType.ERROR).get(0);
		}
		else if (messagesMap.get(BeanMessageType.WARNING).size() > 0) {
			return messagesMap.get(BeanMessageType.WARNING).get(0);
		}
		else if (messagesMap.get(BeanMessageType.INFO).size() > 0) {
			return messagesMap.get(BeanMessageType.INFO).get(0);
		}
		return null;
	}

	@Override
	public IBeanMessage getFirstWorstMandatoryMessage() {
		checkDisposed();
		IBeanMessage result = getFirstMandatoryMessage((messagesMap.get(BeanMessageType.ERROR)));
		if (result == null) {
			result = getFirstMandatoryMessage((messagesMap.get(BeanMessageType.WARNING)));
		}
		if (result == null) {
			result = getFirstMandatoryMessage((messagesMap.get(BeanMessageType.INFO)));
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
		return new LinkedList<IBeanMessage>(messagesList);
	}

	@Override
	public boolean hasErrors() {
		checkDisposed();
		return !messagesMap.get(BeanMessageType.ERROR).isEmpty();
	}

	@Override
	public boolean hasWarnings() {
		checkDisposed();
		return !messagesMap.get(BeanMessageType.WARNING).isEmpty();
	}

	@Override
	public void clearMessages() {
		checkDisposed();
		messagesList.clear();
		messagesMap.get(BeanMessageType.INFO).clear();
		messagesMap.get(BeanMessageType.WARNING).clear();
		messagesMap.get(BeanMessageType.ERROR).clear();
		messageStateObservable.fireMessageStateChanged(this);
	}

	@Override
	public void setTransient(final boolean isTransient) {
		checkDisposed();
		this.isTransient = isTransient;
	}

	@Override
	public boolean isTransient() {
		checkDisposed();
		return isTransient;
	}

	@Override
	public void setDummy(final boolean dummy) {
		checkDisposed();
		this.isDummy = dummy;
	}

	@Override
	public boolean isDummy() {
		checkDisposed();
		return isDummy;
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
		Assert.paramNotNull(listener, "listener");
		beanProxyListeners.add(listener);
	}

	@Override
	public void removeBeanProxyListener(final IBeanProxyListener<BEAN_TYPE> listener) {
		checkDisposed();
		Assert.paramNotNull(listener, "listener");
		beanProxyListeners.remove(listener);
	}

	private void fireBeforeBeanUpdate() {
		for (final IBeanProxyListener<BEAN_TYPE> listener : new LinkedList<IBeanProxyListener<BEAN_TYPE>>(beanProxyListeners)) {
			listener.beforeBeanUpdate(this);
		}
	}

	private void fireAfterBeanUpdated() {
		for (final IBeanProxyListener<BEAN_TYPE> listener : new LinkedList<IBeanProxyListener<BEAN_TYPE>>(beanProxyListeners)) {
			listener.afterBeanUpdated(this);
		}
	}

	private void fireBeforeUndoModifications() {
		for (final IBeanProxyListener<BEAN_TYPE> listener : new LinkedList<IBeanProxyListener<BEAN_TYPE>>(beanProxyListeners)) {
			listener.beforeUndoModifications(this);
		}
	}

	private void fireAfterUndoModifications() {
		for (final IBeanProxyListener<BEAN_TYPE> listener : new LinkedList<IBeanProxyListener<BEAN_TYPE>>(beanProxyListeners)) {
			listener.afterUndoModifications(this);
		}
	}

	private void fireBeforeRedoModifications() {
		for (final IBeanProxyListener<BEAN_TYPE> listener : new LinkedList<IBeanProxyListener<BEAN_TYPE>>(beanProxyListeners)) {
			listener.beforeRedoModifications(this);
		}
	}

	void fireAfterRedoModifications() {
		for (final IBeanProxyListener<BEAN_TYPE> listener : new LinkedList<IBeanProxyListener<BEAN_TYPE>>(beanProxyListeners)) {
			listener.afterRedoModifications(this);
		}
	}

	@Override
	public void dispose() {
		if (!disposed) {
			if (this.executionTask != null) {
				this.executionTask.removeExecutionTaskListener(executionTaskListener);
			}
			addedProperties.clear();
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
		String result;
		try {
			final Integer worked = executionTask.getWorked();
			final Integer total = executionTask.getTotalStepCount();
			if (total != null) {
				if (worked == null) {
					result = "0%";
				}
				else {
					final double percent = (((double) worked) * 100) / total;
					result = "" + Math.round(percent) + "%";
				}
			}
			else {
				result = "?";
			}
		}
		catch (final Exception exception) {
			result = null;
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
		for (final String propertyName : properties) {
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
		else if (obj instanceof IBeanProxy) {
			if (beanDto.getId() != null) {
				return beanDto.getId().equals(((IBeanProxy<?>) obj).getId());
			}
			else {
				return this == obj;
			}
		}
		else {
			return false;
		}
	}

}
