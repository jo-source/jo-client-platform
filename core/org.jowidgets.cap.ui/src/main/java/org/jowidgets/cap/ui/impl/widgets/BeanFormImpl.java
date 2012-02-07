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

package org.jowidgets.cap.ui.impl.widgets;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.cap.common.api.validation.IBeanValidationResult;
import org.jowidgets.cap.ui.api.bean.IBeanProcessStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyListener;
import org.jowidgets.cap.ui.api.bean.IExternalBeanValidator;
import org.jowidgets.cap.ui.api.bean.IExternalBeanValidatorListener;
import org.jowidgets.cap.ui.api.plugin.IBeanFormPlugin;
import org.jowidgets.cap.ui.api.widgets.IBeanForm;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.tools.bean.BeanProxyListenerAdapter;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.plugin.api.PluginToolkit;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidator;

final class BeanFormImpl<BEAN_TYPE> extends ControlWrapper implements IBeanForm<BEAN_TYPE>, IExternalBeanValidator {

	private final IComposite editFormComposite;
	private final BeanFormControl<BEAN_TYPE> editForm;

	private final IComposite createFormComposite;
	private final BeanFormControl<BEAN_TYPE> createForm;

	private final IBeanProcessStateListener<BEAN_TYPE> beanProcessStateListener;
	private final IBeanProxyListener<BEAN_TYPE> beanProxyListener;

	private IBeanProxy<BEAN_TYPE> bean;

	BeanFormImpl(final IComposite composite, final IBeanFormBluePrint<BEAN_TYPE> bluePrint) {
		super(composite);

		final Object entityId = bluePrint.getEntityId();
		if (entityId != null) {
			modifyBeanFormBpByPlugins(entityId, bluePrint);
		}

		composite.setLayout(new MigLayoutDescriptor("hidemode 3", "0[grow, 0::]0", "0[grow, 0::]0"));
		this.editFormComposite = composite.add(BPF.composite(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
		this.editForm = new BeanFormControl<BEAN_TYPE>(
			editFormComposite,
			bluePrint.getEntityId(),
			bluePrint.getEditModeAttributes(),
			bluePrint.getEditModeLayouter(),
			bluePrint.getMandatoryLabelDecorator(),
			bluePrint.getMandatoryBackgroundColor(),
			null,
			bluePrint.getMandatoryValidator(),
			bluePrint.getEditModeInputHint(),
			bluePrint.getEditModeValidationLabel(),
			bluePrint.getUndoAction(),
			bluePrint.getSaveAction());

		this.createFormComposite = composite.add(BPF.composite(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
		this.createForm = new BeanFormControl<BEAN_TYPE>(
			createFormComposite,
			bluePrint.getEntityId(),
			bluePrint.getCreateModeAttributes(),
			bluePrint.getCreateModeLayouter(),
			bluePrint.getMandatoryLabelDecorator(),
			bluePrint.getMandatoryBackgroundColor(),
			bluePrint.getCreateModeForegroundColor(),
			bluePrint.getMandatoryValidator(),
			bluePrint.getCreateModeInputHint(),
			bluePrint.getCreateModeValidationLabel(),
			bluePrint.getUndoAction(),
			bluePrint.getSaveAction());
		createFormComposite.setVisible(false);

		this.beanProcessStateListener = new BeanProcessStateListener();
		this.beanProxyListener = new BeanProxyListener();
	}

	private void modifyBeanFormBpByPlugins(final Object entityId, final IBeanFormBluePrint<BEAN_TYPE> beanFormBp) {
		final IPluginPropertiesBuilder propBuilder = PluginToolkit.pluginPropertiesBuilder();
		propBuilder.add(IBeanFormPlugin.ENTITIY_ID_PROPERTY_KEY, entityId);
		final IPluginProperties properties = propBuilder.build();
		for (final IBeanFormPlugin plugin : PluginProvider.getPlugins(IBeanFormPlugin.ID, properties)) {
			plugin.modifySetup(properties, beanFormBp);
		}
	}

	@Override
	protected IComposite getWidget() {
		return (IComposite) super.getWidget();
	}

	@Override
	public void dispose() {
		editForm.dispose();
		createForm.dispose();
		super.dispose();
	}

	@Override
	public void setValue(final IBeanProxy<BEAN_TYPE> bean) {
		if (this.bean != null) {
			this.bean.removeProcessStateListener(beanProcessStateListener);
			this.bean.removeBeanProxyListener(beanProxyListener);
		}
		this.bean = bean;
		if (isEditForm(bean)) {
			createForm.setValue(null);
			editForm.setValue(bean);
		}
		else {
			editForm.setValue(null);
			createForm.setValue(bean);
		}
		updateFormVisibility(bean);
		if (bean != null) {
			bean.addProcessStateListener(beanProcessStateListener);
			bean.addBeanProxyListener(beanProxyListener);
		}
	}

	@Override
	public void addInputListener(final IInputListener listener) {
		editForm.addInputListener(listener);
		createForm.addInputListener(listener);
	}

	@Override
	public void removeInputListener(final IInputListener listener) {
		editForm.removeInputListener(listener);
		createForm.removeInputListener(listener);
	}

	@Override
	public IValidationResult validate() {
		return getCurrentBeanForm().validate();
	}

	@Override
	public void addValidationConditionListener(final IValidationConditionListener listener) {
		editForm.addValidationConditionListener(listener);
		createForm.addValidationConditionListener(listener);
	}

	@Override
	public void removeValidationConditionListener(final IValidationConditionListener listener) {
		editForm.removeValidationConditionListener(listener);
		createForm.removeValidationConditionListener(listener);
	}

	@Override
	public void addValidator(final IValidator<IBeanProxy<BEAN_TYPE>> validator) {
		editForm.addValidator(validator);
		createForm.addValidator(validator);
	}

	private void updateFormVisibility(final IBeanProxy<BEAN_TYPE> bean) {
		if (isEditForm(bean)) {
			if (!editForm.isVisible()) {
				getWidget().layoutBegin();
				createForm.setVisible(false);
				createForm.setValue(null);
				editForm.setVisible(true);
				editForm.setValue(bean);
				getWidget().layoutEnd();
			}
		}
		else {
			if (!createForm.isVisible()) {
				getWidget().layoutBegin();
				editForm.setVisible(false);
				editForm.setValue(null);
				createForm.setVisible(true);
				createForm.setValue(bean);
				getWidget().layoutEnd();
			}
		}
	}

	private BeanFormControl<BEAN_TYPE> getCurrentBeanForm() {
		if (isEditForm(bean)) {
			return editForm;
		}
		else {
			return createForm;
		}
	}

	private boolean isEditForm(final IBeanProxy<BEAN_TYPE> bean) {
		return bean == null || bean.isDummy() || !bean.isTransient();
	}

	@Override
	public IBeanProxy<BEAN_TYPE> getValue() {
		return bean;
	}

	@Override
	public void setEditable(final boolean editable) {
		editForm.setEditable(editable);
		createForm.setEditable(editable);
	}

	@Override
	public boolean hasModifications() {
		return getCurrentBeanForm().hasModifications();
	}

	@Override
	public void resetModificationState() {
		editForm.resetModificationState();
		createForm.resetModificationState();
	}

	@Override
	public void resetValidation() {
		editForm.resetValidation();
		createForm.resetValidation();
	}

	@Override
	public List<IBeanValidationResult> validate(final Collection<IBeanValidationResult> parentResults) {
		return getCurrentBeanForm().validate(parentResults);
	}

	@Override
	public Collection<String> getObservedProperties() {
		final HashSet<String> result = new HashSet<String>();
		result.addAll(editForm.getObservedProperties());
		result.addAll(createForm.getObservedProperties());
		return Collections.unmodifiableSet(result);
	}

	@Override
	public void addExternalValidatorListener(final IExternalBeanValidatorListener listener) {
		Assert.paramNotNull(listener, "listener");
		editForm.addExternalValidatorListener(listener);
		createForm.addExternalValidatorListener(listener);
	}

	@Override
	public void removeExternalValidatorListener(final IExternalBeanValidatorListener listener) {
		Assert.paramNotNull(listener, "listener");
		editForm.removeExternalValidatorListener(listener);
		createForm.removeExternalValidatorListener(listener);
	}

	private final class BeanProcessStateListener implements IBeanProcessStateListener<BEAN_TYPE> {
		@Override
		public void processStateChanged(final IBeanProxy<BEAN_TYPE> bean) {
			if (BeanFormImpl.this.bean != null && BeanFormImpl.this.bean == bean) {
				updateFormVisibility(BeanFormImpl.this.bean);
			}
		}
	}

	private final class BeanProxyListener extends BeanProxyListenerAdapter<BEAN_TYPE> {
		@Override
		public void afterBeanUpdated(final IBeanProxy<BEAN_TYPE> bean) {
			if (BeanFormImpl.this.bean != null && BeanFormImpl.this.bean == bean) {
				updateFormVisibility(BeanFormImpl.this.bean);
			}
		}
	}

}
