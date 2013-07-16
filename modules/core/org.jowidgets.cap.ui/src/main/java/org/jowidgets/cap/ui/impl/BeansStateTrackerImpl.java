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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.jowidgets.cap.ui.api.bean.IBeanModificationStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProcessStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyContext;
import org.jowidgets.cap.ui.api.bean.IBeanTransientStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanValidationStateListener;
import org.jowidgets.cap.ui.api.bean.IBeansStateTracker;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.model.IModificationStateListener;
import org.jowidgets.cap.ui.api.model.IProcessStateListener;
import org.jowidgets.cap.ui.tools.model.ModificationStateObservable;
import org.jowidgets.cap.ui.tools.model.ProcessStateObservable;
import org.jowidgets.tools.validation.ValidationCache;
import org.jowidgets.tools.validation.ValidationCache.IValidationResultCreator;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidationResultBuilder;
import org.jowidgets.validation.ValidationResult;

final class BeansStateTrackerImpl<BEAN_TYPE> implements IBeansStateTracker<BEAN_TYPE>, IValidationResultCreator {

	private final Set<IBeanProxy<BEAN_TYPE>> registeredBeans;
	private final Set<IBeanProxy<BEAN_TYPE>> modifiedBeans;
	private final Map<Object, IBeanProxy<BEAN_TYPE>> transientBeans;
	private final Set<IBeanProxy<BEAN_TYPE>> processingBeans;
	private final Set<IBeanProxy<BEAN_TYPE>> validationDirtyBeans;

	private final ModificationStateObservable modificationStateObservable;
	private final ProcessStateObservable processStateObservable;

	private final IBeanModificationStateListener<BEAN_TYPE> modificationStateListener;
	private final IBeanTransientStateListener<BEAN_TYPE> transientStateListener;
	private final IBeanProcessStateListener<BEAN_TYPE> processStateListener;
	private final IBeanValidationStateListener<BEAN_TYPE> validationStateListener;
	private final BeanTransientStateListener beanTransientStateListener;

	private final ValidationCache validationCache;
	private final IBeanProxyContext beanProxyContext;

	BeansStateTrackerImpl(final IBeanProxyContext beanProxyContext) {
		Assert.paramNotNull(beanProxyContext, "beanProxyContext");
		this.beanProxyContext = beanProxyContext;
		this.registeredBeans = new HashSet<IBeanProxy<BEAN_TYPE>>();
		this.modifiedBeans = new HashSet<IBeanProxy<BEAN_TYPE>>();
		this.transientBeans = new HashMap<Object, IBeanProxy<BEAN_TYPE>>();
		this.processingBeans = new HashSet<IBeanProxy<BEAN_TYPE>>();
		this.validationDirtyBeans = new LinkedHashSet<IBeanProxy<BEAN_TYPE>>();

		this.modificationStateObservable = new ModificationStateObservable();
		this.processStateObservable = new ProcessStateObservable();

		this.modificationStateListener = new IBeanModificationStateListener<BEAN_TYPE>() {
			@Override
			public void modificationStateChanged(final IBeanProxy<BEAN_TYPE> bean) {
				if (!bean.isTransient()) {
					if (bean.hasModifications() && beanProxyContext.isMaster(bean, BeansStateTrackerImpl.this)) {
						addModifiedBean(bean);
					}
					else {
						removeUnmodifiedBean(bean);
					}
				}
			}
		};

		this.transientStateListener = new IBeanTransientStateListener<BEAN_TYPE>() {
			@Override
			public void transientStateChanged(final Object oldId, final IBeanProxy<BEAN_TYPE> newBean) {
				if (!newBean.isTransient()) {
					removeNotTransientBean(oldId, newBean);
				}

			}
		};

		this.processStateListener = new IBeanProcessStateListener<BEAN_TYPE>() {
			@Override
			public void processStateChanged(final IBeanProxy<BEAN_TYPE> bean) {
				if (bean.hasExecution()) {
					addProcessingBean(bean);
				}
				else {
					removeUnprocessingBean(bean);
				}
			}
		};

		this.validationStateListener = new IBeanValidationStateListener<BEAN_TYPE>() {
			@Override
			public void validationStateChanged(final IBeanProxy<BEAN_TYPE> bean) {
				validationDirtyBeans.add(bean);
				validationCache.setDirty();
			}
		};

		this.beanTransientStateListener = new BeanTransientStateListener();
		this.validationCache = new ValidationCache(this);
	}

	@Override
	public void dispose() {
		clearAll();
		modificationStateObservable.dispose();
		processStateObservable.dispose();
		validationCache.dispose();
		processingBeans.clear();
		validationDirtyBeans.clear();
		modifiedBeans.clear();
		transientBeans.clear();
	}

	@Override
	public void register(final IBeanProxy<BEAN_TYPE> bean) {
		Assert.paramNotNull(bean, "bean");

		if (bean.isTransient()) {
			bean.addTransientStateListener(beanTransientStateListener);
		}
		else {
			beanProxyContext.registerBean(bean, this);
		}

		registeredBeans.add(bean);

		bean.addModificationStateListener(modificationStateListener);

		if (bean.hasModifications() && !bean.isTransient()) {
			addModifiedBean(bean);
		}
		if (bean.isTransient()) {
			addTransientBean(bean);
		}

		validationDirtyBeans.add(bean);
		validationCache.setDirty();

		bean.addProcessStateListener(processStateListener);
		if (bean.hasExecution()) {
			addProcessingBean(bean);
		}

		bean.addValidationStateListener(validationStateListener);
	}

	@Override
	public void unregister(final IBeanProxy<BEAN_TYPE> bean) {
		unregisterImpl(bean, true);
	}

	private void unregisterImpl(final IBeanProxy<BEAN_TYPE> bean, final boolean setValidationCacheDirty) {
		Assert.paramNotNull(bean, "bean");

		if (bean.isTransient()) {
			bean.removeTransientStateListener(beanTransientStateListener);
		}
		else {
			beanProxyContext.unregisterBean(bean, this);
		}

		registeredBeans.remove(bean);

		bean.removeModificationStateListener(modificationStateListener);
		removeUnmodifiedBean(bean);
		removeNotTransientBean(bean.getId(), bean);
		bean.removeProcessStateListener(processStateListener);
		removeUnprocessingBean(bean);
		validationDirtyBeans.remove(bean);
		if (setValidationCacheDirty) {
			validationCache.setDirty();
		}
	}

	@Override
	public IValidationResult validate() {
		return validationCache.validate();
	}

	@Override
	public void addValidationConditionListener(final IValidationConditionListener listener) {
		validationCache.addValidationConditionListener(listener);
	}

	@Override
	public void removeValidationConditionListener(final IValidationConditionListener listener) {
		validationCache.removeValidationConditionListener(listener);
	}

	@Override
	public IValidationResult createValidationResult() {
		final IValidationResultBuilder builder = ValidationResult.builder();
		for (final IBeanProxy<BEAN_TYPE> bean : new LinkedList<IBeanProxy<BEAN_TYPE>>(validationDirtyBeans)) {
			final IValidationResult validationResult = bean.validate();
			builder.addResult(validationResult);
			if (validationResult.isValid()) {
				validationDirtyBeans.remove(bean);
			}
			else {
				break;
			}
		}
		return builder.build();
	}

	@Override
	public void undoModifications() {
		for (final IBeanProxy<BEAN_TYPE> bean : modifiedBeans) {
			bean.undoModifications();
		}
	}

	@Override
	public Set<IBeanProxy<BEAN_TYPE>> getBeansToUpdate() {
		return Collections.unmodifiableSet(modifiedBeans);
	}

	@Override
	public Set<IBeanProxy<BEAN_TYPE>> getMasterBeansToUpdate() {
		final Set<IBeanProxy<BEAN_TYPE>> result = new HashSet<IBeanProxy<BEAN_TYPE>>();
		for (final IBeanProxy<BEAN_TYPE> bean : modifiedBeans) {
			if (beanProxyContext.isMaster(bean, this)) {
				result.add(bean);
			}
		}
		return Collections.unmodifiableSet(result);
	}

	@Override
	public Set<IBeanProxy<BEAN_TYPE>> getBeansToCreate() {
		return Collections.unmodifiableSet(new HashSet<IBeanProxy<BEAN_TYPE>>(transientBeans.values()));
	}

	@Override
	public boolean hasBeansToUpdate() {
		return !modifiedBeans.isEmpty();
	}

	@Override
	public boolean hasBeansToSave() {
		return !transientBeans.isEmpty();
	}

	@Override
	public boolean hasModifications() {
		return hasBeansToUpdate() || hasBeansToSave();
	}

	@Override
	public Set<IBeanProxy<BEAN_TYPE>> getExecutingBeans() {
		return Collections.unmodifiableSet(processingBeans);
	}

	@Override
	public boolean hasExecutingBeans() {
		return !processingBeans.isEmpty();
	}

	@Override
	public void cancelExecutions() {
		final Set<IExecutionTask> tasks = new HashSet<IExecutionTask>();
		for (final IBeanProxy<BEAN_TYPE> bean : new HashSet<IBeanProxy<BEAN_TYPE>>(processingBeans)) {
			tasks.add(bean.getExecutionTask());
			bean.setExecutionTask(null);
		}
		processingBeans.clear();

		for (final IExecutionTask task : tasks) {
			if (task != null && !task.isCanceled() && !task.isFinshed()) {
				final Runnable runnable = new Runnable() {
					@Override
					public void run() {
						task.cancel();
					}
				};
				final Thread thread = new Thread(runnable);
				thread.setDaemon(true);
				thread.start();
			}
		}

	}

	@Override
	public void clearModifications() {
		modifiedBeans.clear();
		transientBeans.clear();
	}

	@Override
	public void clearAll() {
		for (final IBeanProxy<BEAN_TYPE> bean : new LinkedList<IBeanProxy<BEAN_TYPE>>(registeredBeans)) {
			unregisterImpl(bean, false);
		}
		validationCache.setDirty();
	}

	@Override
	public void addModificationStateListener(final IModificationStateListener listener) {
		modificationStateObservable.addModificationStateListener(listener);
	}

	@Override
	public void removeModificationStateListener(final IModificationStateListener listener) {
		modificationStateObservable.removeModificationStateListener(listener);
	}

	@Override
	public void addProcessStateListener(final IProcessStateListener listener) {
		processStateObservable.addProcessStateListener(listener);
	}

	@Override
	public void removeProcessStateListener(final IProcessStateListener listener) {
		processStateObservable.removeProcessStateListener(listener);
	}

	private void addModifiedBean(final IBeanProxy<BEAN_TYPE> bean) {
		final boolean lastDirty = hasModifications();
		modifiedBeans.add(bean);
		if (lastDirty != hasModifications()) {
			modificationStateObservable.fireModificationStateChanged();
		}
	}

	private void addTransientBean(final IBeanProxy<BEAN_TYPE> bean) {
		final boolean lastDirty = hasModifications();
		transientBeans.put(bean.getId(), bean);
		bean.addTransientStateListener(transientStateListener);
		if (lastDirty != hasModifications()) {
			modificationStateObservable.fireModificationStateChanged();
		}
	}

	private void removeNotTransientBean(final Object oldId, final IBeanProxy<BEAN_TYPE> bean) {
		final boolean lastDirty = hasModifications();
		transientBeans.remove(oldId);
		bean.removeTransientStateListener(transientStateListener);
		if (lastDirty != hasModifications()) {
			modificationStateObservable.fireModificationStateChanged();
		}
	}

	private void removeUnmodifiedBean(final IBeanProxy<BEAN_TYPE> bean) {
		final boolean lastDirty = hasModifications();
		modifiedBeans.remove(bean);
		if (lastDirty != hasModifications()) {
			modificationStateObservable.fireModificationStateChanged();
		}
	}

	private void addProcessingBean(final IBeanProxy<BEAN_TYPE> bean) {
		final boolean lastHasProccesingBeans = hasExecutingBeans();
		processingBeans.add(bean);
		if (lastHasProccesingBeans != hasExecutingBeans()) {
			processStateObservable.fireProcessStateChanged();
		}
	}

	private void removeUnprocessingBean(final IBeanProxy<BEAN_TYPE> bean) {
		final boolean lastHasProccesingBeans = hasExecutingBeans();
		processingBeans.remove(bean);
		if (lastHasProccesingBeans != hasExecutingBeans()) {
			processStateObservable.fireProcessStateChanged();
		}
	}

	private final class BeanTransientStateListener implements IBeanTransientStateListener<BEAN_TYPE> {
		@Override
		public void transientStateChanged(final Object oldId, final IBeanProxy<BEAN_TYPE> newBean) {
			beanProxyContext.registerBean(newBean, BeansStateTrackerImpl.this);
			newBean.removeTransientStateListener(beanTransientStateListener);
		}
	}

}
