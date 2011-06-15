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
import java.util.HashMap;
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
import org.jowidgets.cap.ui.api.bean.BeanStateType;
import org.jowidgets.cap.ui.api.bean.IBeanModificationStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProcessStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanState;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.execution.IExecutionTaskListener;
import org.jowidgets.cap.ui.tools.execution.ExecutionTaskAdapter;
import org.jowidgets.util.Assert;
import org.jowidgets.util.NullCompatibleEquivalence;

final class BeanProxyImpl<BEAN_TYPE> implements IBeanProxy<BEAN_TYPE> {

	private static final IBeanState OK_STATE = createOkState();

	private final Class<? extends BEAN_TYPE> beanType;
	private final Map<String, IBeanModification> modifications;
	private final Map<String, IBeanModification> undoneModifications;
	private final PropertyChangeObservable propertyChangeObservable;
	private final BeanModificationStateObservable<BEAN_TYPE> modificationStateObservable;
	private final BeanProcessStateObservable<BEAN_TYPE> processStateObservable;
	private final IExecutionTaskListener executionTaskListener;
	private final IUiThreadAccess uiThreadAccess;

	private IExecutionTask executionTask;
	private IBeanState state;
	private String lastProgress;
	private IBeanDto beanDto;
	private BEAN_TYPE proxy;

	BeanProxyImpl(final IBeanDto beanDto, final Class<? extends BEAN_TYPE> beanType) {
		Assert.paramNotNull(beanDto, "beanDto");
		Assert.paramNotNull(beanType, "beanType");

		this.beanDto = beanDto;
		this.beanType = beanType;
		this.modifications = new HashMap<String, IBeanModification>();
		this.undoneModifications = new HashMap<String, IBeanModification>();
		this.propertyChangeObservable = new PropertyChangeObservable();
		this.modificationStateObservable = new BeanModificationStateObservable<BEAN_TYPE>();
		this.processStateObservable = new BeanProcessStateObservable<BEAN_TYPE>();
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
	public Set<String> getPropertyNames() {
		return beanDto.getPropertyNames();
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
		if (NullCompatibleEquivalence.equals(originalValue, newValue)) {
			final boolean oldModificationState = hasModifications();
			modifications.remove(propertyName);
			final boolean newModificationState = hasModifications();
			propertyChange(this, propertyName, currentValue, newValue);
			if (oldModificationState != newModificationState) {
				modificationStateObservable.fireModificationStateChanged(this);
			}
		}
		else if (!NullCompatibleEquivalence.equals(currentValue, newValue)) {
			final IBeanModificationBuilder modBuilder = CapCommonToolkit.beanModificationBuilder();
			modBuilder.setBeanDto(beanDto).setPropertyName(propertyName).setNewValue(newValue);
			final boolean oldModificationState = hasModifications();
			modifications.put(propertyName, modBuilder.build());
			final boolean newModificationState = hasModifications();
			propertyChange(this, propertyName, currentValue, newValue);
			if (oldModificationState != newModificationState) {
				modificationStateObservable.fireModificationStateChanged(this);
			}
		}
	}

	@Override
	public void update(final IBeanDto beanDto) {
		Assert.paramNotNull(beanDto, "beanDto");
		if (!this.beanDto.equals(beanDto)) {
			throw new IllegalArgumentException("The given parameter 'beanDto' must have the same id and type than this proxy");
		}

		final boolean oldModificationState = hasModifications();
		final List<PropertyChangeEvent> propertyChangeEvents = getPropertyChangesForUpdate(beanDto);
		this.beanDto = beanDto;
		modifications.clear();
		firePropertyChangeEvents(propertyChangeEvents);
		if (oldModificationState) {
			modificationStateObservable.fireModificationStateChanged(this);
		}
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
	public void undoModifications() {
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
	}

	@Override
	public void redoModifications() {
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
	public void setState(final IBeanState state) {
		this.state = state;
	}

	@Override
	public IBeanState getState() {
		if (state != null) {
			return state;
		}
		return OK_STATE;
	}

	@Override
	public void clearState() {
		state = null;
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
	public void dispose() {
		modifications.clear();
		modificationStateObservable.dispose();
		processStateObservable.dispose();
		executionTask = null;
		beanDto = null;
		proxy = null;
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
		for (final String propertyName : this.beanDto.getPropertyNames()) {
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
				propertyChange(BeanProxyImpl.this, IBeanProxy.META_PROPERTY_PROGRESS, lastProgress, getProgress());
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
			+ ", proxy="
			+ proxy
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
		else {
			return beanDto.equals(((BeanProxyImpl<?>) obj).beanDto);
		}
	}

	private static IBeanState createOkState() {
		return new IBeanState() {

			@Override
			public String getUserMessage() {
				return "";
			}

			@Override
			public BeanStateType getType() {
				return BeanStateType.OK;
			}

			@Override
			public Throwable getException() {
				return null;
			}
		};
	}

}
