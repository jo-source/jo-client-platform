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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.command.EnabledState;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IEnabledState;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutableState;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanMessageStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanModificationStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProcessStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionEvent;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.util.Assert;
import org.jowidgets.util.event.ChangeObservable;
import org.jowidgets.util.event.IChangeListener;

@SuppressWarnings({"rawtypes", "unchecked"})
final class BeanListModelEnabledChecker<BEAN_TYPE> extends ChangeObservable implements IEnabledChecker {

	private final IEnabledState unloadedDataState = EnabledState.disabled(Messages.getString("BeanListModelEnabledChecker.the_selection_contains_unloaded_data")); //$NON-NLS-1$
	private final IEnabledState isInProgressState = EnabledState.disabled(Messages.getString("BeanListModelEnabledChecker.there_is_some_other_execution_in_progress")); //$NON-NLS-1$
	private final IEnabledState singleSelectionState = EnabledState.disabled(Messages.getString("BeanListModelEnabledChecker.there_must_be_selected_exactly_one_record")); //$NON-NLS-1$
	private final IEnabledState multiSelectionState = EnabledState.disabled(Messages.getString("BeanListModelEnabledChecker.there_must_be_selected_at_least_one_record")); //$NON-NLS-1$
	private final IEnabledState noSelectionState = EnabledState.disabled(Messages.getString("BeanListModelEnabledChecker.there_must_not_be_selected_any_record")); //$NON-NLS-1$
	private final IEnabledState unsavedDataState = EnabledState.disabled(Messages.getString("BeanListModelEnabledChecker.the_record_has_unsaved_data")); //$NON-NLS-1$
	private final IEnabledState unhandlesMessageState = EnabledState.disabled(Messages.getString("BeanListModelEnabledChecker.there_are_unhandled_messages")); //$NON-NLS-1$

	private final IBeanListModel<BEAN_TYPE> listModel;
	private final List<IExecutableChecker<BEAN_TYPE>> executableCheckers;
	private final List<IEnabledChecker> enabledCheckers;
	private final BeanSelectionPolicy beanSelectionPolicy;
	private final BeanModificationStatePolicy beanModificationStatePolicy;
	private final BeanMessageStatePolicy beanMessageStatePolicy;
	private final boolean ignoreSelectedBeansState;

	private List<IBeanProxy<BEAN_TYPE>> lastSelection;

	BeanListModelEnabledChecker(
		final IBeanListModel<BEAN_TYPE> listModel,
		final BeanSelectionPolicy beanSelectionPolicy,
		final BeanModificationStatePolicy beanModificationStatePolicy,
		final BeanMessageStatePolicy beanMessageStatePolicy,
		final List<IEnabledChecker> enabledCheckers,
		final List<IExecutableChecker<BEAN_TYPE>> executableCheckers,
		final boolean ignoreSelectedBeansState) {
		super();
		Assert.paramNotNull(listModel, "listModel");
		Assert.paramNotNull(beanSelectionPolicy, "beanSelectionPolicy");
		Assert.paramNotNull(beanModificationStatePolicy, "beanModificationStatePolicy");
		Assert.paramNotNull(enabledCheckers, "enabledCheckers");

		this.listModel = listModel;
		this.beanSelectionPolicy = beanSelectionPolicy;
		this.beanModificationStatePolicy = beanModificationStatePolicy;
		this.beanMessageStatePolicy = beanMessageStatePolicy;
		this.enabledCheckers = enabledCheckers;
		if (executableCheckers != null) {
			this.executableCheckers = executableCheckers;
		}
		else {
			this.executableCheckers = Collections.emptyList();
		}
		this.ignoreSelectedBeansState = ignoreSelectedBeansState;

		this.lastSelection = getSelectedBeans();

		final IChangeListener changeListener = new IChangeListener() {
			@Override
			public void changed() {
				fireChangedEvent();
			}
		};

		for (final IEnabledChecker enabledChecker : enabledCheckers) {
			enabledChecker.addChangeListener(changeListener);
		}

		//TODO MG enabled checks must be done better performance
		final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				fireChangedEvent();
			}
		};

		final IBeanModificationStateListener modificationStateListener = new IBeanModificationStateListener<Object>() {
			@Override
			public void modificationStateChanged(final IBeanProxy<Object> bean) {
				fireChangedEvent();
			}
		};

		final IBeanProcessStateListener processStateListener = new IBeanProcessStateListener<Object>() {
			@Override
			public void processStateChanged(final IBeanProxy<Object> bean) {
				fireChangedEvent();
			}
		};

		final IBeanMessageStateListener messageStateListener = new IBeanMessageStateListener<Object>() {
			@Override
			public void messageStateChanged(final IBeanProxy<Object> bean) {
				fireChangedEvent();
			}
		};

		listModel.addBeanSelectionListener(new IBeanSelectionListener<BEAN_TYPE>() {

			@Override
			public void selectionChanged(final IBeanSelectionEvent<BEAN_TYPE> selectionEvent) {
				final List<IBeanProxy<BEAN_TYPE>> selectedBeans = selectionEvent.getSelection();

				if (!ignoreSelectedBeansState) {
					for (final IBeanProxy bean : lastSelection) {
						if (bean != null) {
							bean.removeProcessStateListener(processStateListener);
							bean.removePropertyChangeListener(propertyChangeListener);
							bean.removeModificationStateListener(modificationStateListener);
							bean.removeMessageStateListener(messageStateListener);
						}
					}

					for (final IBeanProxy bean : selectedBeans) {
						if (bean != null) {
							bean.addProcessStateListener(processStateListener);
							bean.addPropertyChangeListener(propertyChangeListener);
							bean.addModificationStateListener(modificationStateListener);
							bean.addMessageStateListener(messageStateListener);
						}
					}
				}

				lastSelection = selectedBeans;

				fireChangedEvent();
			}
		});
	}

	@Override
	public IEnabledState getEnabledState() {
		//TODO MG enabled checks must be done better performance
		if (BeanSelectionPolicy.SINGLE_SELECTION == beanSelectionPolicy && lastSelection.size() != 1) {
			return singleSelectionState;
		}
		else if (BeanSelectionPolicy.MULTI_SELECTION == beanSelectionPolicy && lastSelection.size() < 1) {
			return multiSelectionState;
		}
		else if (BeanSelectionPolicy.NO_SELECTION == beanSelectionPolicy && lastSelection.size() > 0) {
			return noSelectionState;
		}
		for (final IEnabledChecker enabledChecker : enabledCheckers) {
			final IEnabledState result = enabledChecker.getEnabledState();
			if (!result.isEnabled()) {
				return result;
			}
		}
		if (!ignoreSelectedBeansState) {
			for (final IBeanProxy bean : lastSelection) {
				if (bean == null) {
					return unloadedDataState;
				}
				final IBeanMessage worstMessage = bean.getFirstWorstMessage();
				final IBeanMessage worstMandatoryMessage = bean.getFirstWorstMandatoryMessage();
				if (bean.getExecutionTask() != null) {
					return isInProgressState;
				}
				else if (BeanMessageStatePolicy.NO_MESSAGE == beanMessageStatePolicy && worstMessage != null) {
					return unhandlesMessageState;
				}
				else if (BeanMessageStatePolicy.NO_MESSAGE_MANDATORY == beanMessageStatePolicy && worstMandatoryMessage != null) {
					return unhandlesMessageState;
				}
				else if (BeanModificationStatePolicy.NO_MODIFICATION == beanModificationStatePolicy && bean.hasModifications()) {
					return unsavedDataState;
				}
				else if (BeanMessageStatePolicy.NO_WARNING_OR_ERROR == beanMessageStatePolicy
					&& worstMessage != null
					&& worstMessage.getType().equalOrWorse(BeanMessageType.WARNING)) {
					return unhandlesMessageState;
				}
				else if (BeanMessageStatePolicy.NO_WARNING_OR_ERROR_MANDATORY == beanMessageStatePolicy
					&& worstMandatoryMessage != null
					&& worstMandatoryMessage.getType().equalOrWorse(BeanMessageType.WARNING)) {
					return unhandlesMessageState;
				}
				else if (BeanMessageStatePolicy.NO_ERROR == beanMessageStatePolicy
					&& worstMessage != null
					&& worstMessage.getType() == BeanMessageType.ERROR) {
					return unhandlesMessageState;
				}
				else if (BeanMessageStatePolicy.NO_ERROR_MANDATORY == beanMessageStatePolicy
					&& worstMandatoryMessage != null
					&& worstMandatoryMessage.getType() == BeanMessageType.ERROR) {
					return unhandlesMessageState;
				}
				for (final IExecutableChecker executableChecker : executableCheckers) {
					final IExecutableState result = executableChecker.getExecutableState(bean.getBean());
					if (!result.isExecutable()) {
						return EnabledState.disabled(result.getReason());
					}
				}
			}
		}

		return EnabledState.ENABLED;
	}

	private List<IBeanProxy<BEAN_TYPE>> getSelectedBeans() {
		final List<IBeanProxy<BEAN_TYPE>> result = new LinkedList<IBeanProxy<BEAN_TYPE>>();
		for (final Integer index : listModel.getSelection()) {
			result.add(listModel.getBean(index.intValue()));
		}
		return result;
	}

}