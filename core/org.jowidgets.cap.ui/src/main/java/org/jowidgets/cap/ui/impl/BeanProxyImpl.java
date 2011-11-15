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
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanMessageStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanModificationStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProcessStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyListener;
import org.jowidgets.cap.ui.api.bean.IBeanValidationResult;
import org.jowidgets.cap.ui.api.bean.IBeanValidationStateListener;
import org.jowidgets.cap.ui.api.bean.IExternalBeanValidator;
import org.jowidgets.cap.ui.api.bean.IExternalBeanValidatorListener;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.execution.IExecutionTaskListener;
import org.jowidgets.cap.ui.tools.execution.ExecutionTaskAdapter;
import org.jowidgets.tools.validation.ValidationCache;
import org.jowidgets.tools.validation.ValidationCache.IValidationResultCreator;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCompatibleEquivalence;
import org.jowidgets.util.NullCompatibleEquivalence;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidationResultBuilder;
import org.jowidgets.validation.ValidationResult;

final class BeanProxyImpl<BEAN_TYPE> implements IBeanProxy<BEAN_TYPE>, IValidationResultCreator, IExternalBeanValidatorListener {

	private final Class<? extends BEAN_TYPE> beanType;
	private final List<String> properties;
	private final Map<String, IBeanModification> modifications;
	private final Map<String, IBeanModification> undoneModifications;
	private final PropertyChangeObservable propertyChangeObservable;
	private final BeanModificationStateObservable<BEAN_TYPE> modificationStateObservable;
	private final BeanMessageStateObservable<BEAN_TYPE> messageStateObservable;
	private final BeanProcessStateObservable<BEAN_TYPE> processStateObservable;
	private final BeanValidationStateObservable<BEAN_TYPE> validationStateObservable;
	private final Set<IBeanProxyListener<BEAN_TYPE>> beanProxyListeners;
	private final Set<IBeanPropertyValidator<BEAN_TYPE>> beanPropertyValidators;
	private final Set<IExternalBeanValidator> externalValidators;
	private final Set<String> internalObservedProperties;
	private final IExecutionTaskListener executionTaskListener;
	private final Map<String, IValidationResult> validationResults;
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

	BeanProxyImpl(
		final IBeanDto beanDto,
		final Class<? extends BEAN_TYPE> beanType,
		final List<String> properties,
		final boolean isTransient) {
		Assert.paramNotNull(beanDto, "beanDto");
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(properties, "properties");

		this.beanDto = beanDto;
		this.beanType = beanType;
		this.properties = new LinkedList<String>(properties);
		this.isTransient = isTransient;
		this.modifications = new HashMap<String, IBeanModification>();
		this.undoneModifications = new HashMap<String, IBeanModification>();
		this.propertyChangeObservable = new PropertyChangeObservable();
		this.modificationStateObservable = new BeanModificationStateObservable<BEAN_TYPE>();
		this.processStateObservable = new BeanProcessStateObservable<BEAN_TYPE>();
		this.messageStateObservable = new BeanMessageStateObservable<BEAN_TYPE>();
		this.validationStateObservable = new BeanValidationStateObservable<BEAN_TYPE>();
		this.beanProxyListeners = new LinkedHashSet<IBeanProxyListener<BEAN_TYPE>>();
		this.beanPropertyValidators = new LinkedHashSet<IBeanPropertyValidator<BEAN_TYPE>>();
		this.externalValidators = new LinkedHashSet<IExternalBeanValidator>();
		this.validationResults = new HashMap<String, IValidationResult>();
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
		return beanDto.getId();
	}

	@Override
	public long getVersion() {
		return beanDto.getVersion();
	}

	@Override
	public Object getValue(final String propertyName) {
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
		Assert.paramNotNull(beanDto, "beanDto");
		if (!isTransient && !this.beanDto.equals(beanDto)) {
			throw new IllegalArgumentException("The given parameter 'beanDto' must have the same id and type than this proxy");
		}
		fireBeforeBeanUpdate();
		updateImpl(beanDto);
	}

	@Override
	public void updateTransient(final IBeanDto beanDto) {
		Assert.paramNotNull(beanDto, "beanDto");
		if (!isTransient) {
			throw new IllegalStateException("This bean is not transient");
		}
		fireBeforeBeanUpdate();
		setTransient(false);
		updateImpl(beanDto);
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
		return modifications.values();
	}

	@Override
	public boolean hasModifications() {
		return modifications.size() > 0;
	}

	@Override
	public boolean isModified(final String propertyName) {
		Assert.paramNotNull(propertyName, "propertyName");
		return modifications.containsKey(propertyName);
	}

	@Override
	public void undoModifications() {
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
		return validationCache.validate();
	}

	@Override
	public IValidationResult createValidationResult() {
		final IValidationResultBuilder builder = ValidationResult.builder();
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
		final List<IBeanValidationResult> beanValidationResults = new LinkedList<IBeanValidationResult>();
		for (final String propertyName : properties) {
			final IValidationResult validationResult = validateProperty(propertyName);
			beanValidationResults.add(new IBeanValidationResult() {
				@Override
				public IValidationResult getValidationResult() {
					return validationResult;
				}

				@Override
				public String getPropertyName() {
					return propertyName;
				}
			});
		}
		if (beanValidationResults.size() > 0) {
			boolean validationChanged = false;
			for (final IBeanValidationResult externalResult : externalValidator.validate(beanValidationResults)) {
				final String propertyName = externalResult.getPropertyName();
				final IValidationResult validationResult = externalResult.getValidationResult();
				final IValidationResult lastResult = validationResults.get(propertyName);
				if (lastResult == null || !validationResult.getWorstFirst().equals(lastResult.getWorstFirst())) {
					validationResults.put(propertyName, validationResult);
					validationChanged = true;
				}
			}
			if (validationChanged) {
				fireValidationConditionsChanged();
			}
		}
	}

	private void validateAllInternalProperties() {
		validateInternalProperties(internalObservedProperties);
	}

	private void validateInternalProperty(final String propertyName) {
		validateInternalProperties(Collections.singletonList(propertyName));
	}

	private void validateInternalProperties(final Collection<String> propertyNames) {
		boolean validationChanged = false;
		for (final String propertyName : propertyNames) {
			final IValidationResult validationResult = validateProperty(propertyName);
			final IValidationResult lastResult = validationResults.get(propertyName);
			if (lastResult == null || !validationResult.getWorstFirst().equals(lastResult.getWorstFirst())) {
				validationResults.put(propertyName, validationResult);
				validationChanged = true;
			}
		}
		if (validationChanged) {
			fireValidationConditionsChanged();
		}
	}

	private IValidationResult validateProperty(final String propertyName) {
		Assert.paramNotNull(propertyName, "propertyName");
		final IValidationResultBuilder builder = ValidationResult.builder();
		//validate with the added bean validators
		for (final IBeanPropertyValidator<BEAN_TYPE> validator : beanPropertyValidators) {
			final IValidationResult validationResult = validator.validateProperty(this, propertyName);
			if (!validationResult.isValid()) {
				//return for the first error for performance issue
				return validationResult;
			}
			else {
				builder.addResult(validationResult);
			}
		}
		return builder.build();
	}

	@Override
	public void addValidationStateListener(final IBeanValidationStateListener<BEAN_TYPE> listener) {
		validationStateObservable.addValidationStateListener(listener);
	}

	@Override
	public void removeValidationStateListener(final IBeanValidationStateListener<BEAN_TYPE> listener) {
		validationStateObservable.removeValidationStateListener(listener);
	}

	@Override
	public void addBeanPropertyValidator(final IBeanPropertyValidator<BEAN_TYPE> validator) {
		Assert.paramNotNull(validator, "validator");
		this.beanPropertyValidators.add(validator);
	}

	@Override
	public void registerExternalValidator(final IExternalBeanValidator validator) {
		Assert.paramNotNull(validator, "validator");
		externalValidators.add(validator);
		validator.addExternalValidatorListener(this);
		calculateInternalObservedProperties();
	}

	@Override
	public void unregisterExternalValidator(final IExternalBeanValidator validator) {
		Assert.paramNotNull(validator, "validator");
		externalValidators.remove(validator);
		validator.removeExternalValidatorListener(this);
		calculateInternalObservedProperties();
	}

	private void calculateInternalObservedProperties() {
		final Set<String> result = new HashSet<String>(properties);
		for (final IExternalBeanValidator externalBeanValidator : externalValidators) {
			result.removeAll(externalBeanValidator.getObservedProperties());
		}
		internalObservedProperties.clear();
		internalObservedProperties.addAll(result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public BEAN_TYPE getBean() {
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
		return executionTask;
	}

	@Override
	public boolean hasExecution() {
		return executionTask != null;
	}

	@Override
	public void setExecutionTask(final IExecutionTask executionTask) {
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
		return new LinkedList<IBeanMessage>(messagesList);
	}

	@Override
	public boolean hasErrors() {
		return !messagesMap.get(BeanMessageType.ERROR).isEmpty();
	}

	@Override
	public void clearMessages() {
		messagesList.clear();
		messagesMap.get(BeanMessageType.INFO).clear();
		messagesMap.get(BeanMessageType.WARNING).clear();
		messagesMap.get(BeanMessageType.ERROR).clear();
		messageStateObservable.fireMessageStateChanged(this);
	}

	@Override
	public void setTransient(final boolean isTransient) {
		this.isTransient = isTransient;
	}

	@Override
	public boolean isTransient() {
		return isTransient;
	}

	@Override
	public void setDummy(final boolean dummy) {
		this.isDummy = dummy;
	}

	@Override
	public boolean isDummy() {
		return isDummy;
	}

	@Override
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		propertyChangeObservable.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(final PropertyChangeListener listener) {
		propertyChangeObservable.removePropertyChangeListener(listener);
	}

	@Override
	public void addModificationStateListener(final IBeanModificationStateListener<BEAN_TYPE> listener) {
		modificationStateObservable.addModificationStateListener(listener);
	}

	@Override
	public void removeModificationStateListener(final IBeanModificationStateListener<BEAN_TYPE> listener) {
		modificationStateObservable.removeModificationStateListener(listener);
	}

	@Override
	public void addProcessStateListener(final IBeanProcessStateListener<BEAN_TYPE> listener) {
		processStateObservable.addProcessStateListener(listener);
	}

	@Override
	public void removeProcessStateListener(final IBeanProcessStateListener<BEAN_TYPE> listener) {
		processStateObservable.removeProcessStateListener(listener);
	}

	@Override
	public void addMessageStateListener(final IBeanMessageStateListener<BEAN_TYPE> listener) {
		messageStateObservable.addMessageStateListener(listener);
	}

	@Override
	public void removeMessageStateListener(final IBeanMessageStateListener<BEAN_TYPE> listener) {
		messageStateObservable.removeMessageStateListener(listener);
	}

	@Override
	public void addBeanProxyListener(final IBeanProxyListener<BEAN_TYPE> listener) {
		Assert.paramNotNull(listener, "listener");
		beanProxyListeners.add(listener);
	}

	@Override
	public void removeBeanProxyListener(final IBeanProxyListener<BEAN_TYPE> listener) {
		Assert.paramNotNull(listener, "listener");
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
		if (this.executionTask != null) {
			this.executionTask.removeExecutionTaskListener(executionTaskListener);
		}
		modifications.clear();
		modificationStateObservable.dispose();
		processStateObservable.dispose();
		messageStateObservable.dispose();
		propertyChangeObservable.dispose();
		validationStateObservable.dispose();
		executionTask = null;
		beanDto = null;
		proxy = null;
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
		return beanDto.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		else if (obj instanceof IBeanProxy) {
			return beanDto.getId().equals(((IBeanProxy<?>) obj).getId());
		}
		else {
			return false;
		}
	}

}
