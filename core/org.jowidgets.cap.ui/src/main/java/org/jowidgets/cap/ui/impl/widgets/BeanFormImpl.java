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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.IControl;
import org.jowidgets.api.widgets.IInputComponentValidationLabel;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.api.widgets.IScrollComposite;
import org.jowidgets.api.widgets.IValidationResultLabel;
import org.jowidgets.api.widgets.blueprint.IInputComponentValidationLabelBluePrint;
import org.jowidgets.api.widgets.blueprint.IValidationResultLabelBluePrint;
import org.jowidgets.api.widgets.descriptor.setup.IInputComponentValidationLabelSetup;
import org.jowidgets.api.widgets.descriptor.setup.IValidationLabelSetup;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.bean.IBeanModificationStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProcessStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.form.IBeanFormControlFactory;
import org.jowidgets.cap.ui.api.form.IBeanFormLayouter;
import org.jowidgets.cap.ui.api.widgets.IBeanForm;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.common.color.IColorConstant;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.factory.ICustomWidgetFactory;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.tools.widgets.wrapper.AbstractInputControl;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCompatibleEquivalence;
import org.jowidgets.util.IDecorator;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidationResultBuilder;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.ValidationResult;

final class BeanFormImpl<BEAN_TYPE> extends AbstractInputControl<IBeanProxy<BEAN_TYPE>> implements IBeanForm<BEAN_TYPE> {

	private final IBeanFormLayouter layouter;
	private final IDecorator<String> mandatoryLabelDecorator;
	private final IColorConstant mandatoryBackgroundColor;
	private final IValidator<Object> mandatoryValidator;
	private final String editModeInputHint;
	private final String createModeInputHint;
	private final Map<String, IAttribute<Object>> attributes;

	private final Map<String, IInputControl<Object>> controls;
	private final Map<String, IColorConstant> backgroundColors;
	private final Map<String, IInputListener> bindingListeners;
	private final Map<String, IValidationConditionListener> validationListeners;
	private final Map<String, IValidationResultLabel> validationLabels;
	private final Map<String, IValidationResult> validationResults;
	private final PropertyChangeListener propertyChangeListenerBinding;
	private final PropertyChangeListener propertyChangeListenerValidation;
	private final IBeanModificationStateListener<BEAN_TYPE> modificationStateListener;
	private final IBeanProcessStateListener<BEAN_TYPE> beanProcessStateListener;

	private final IComposite validationLabelContainer;
	private final IInputComponentValidationLabel editModeValidationLabel;
	private final IInputComponentValidationLabel createModeValidationLabel;

	private IBeanProxy<BEAN_TYPE> bean;

	@SuppressWarnings("unchecked")
	BeanFormImpl(final IComposite composite, final IBeanFormBluePrint<BEAN_TYPE> bluePrint) {
		super(composite);
		this.layouter = bluePrint.getLayouter();
		this.mandatoryLabelDecorator = bluePrint.getMandatoryLabelDecorator();
		this.mandatoryBackgroundColor = bluePrint.getMandatoryBackgroundColor();
		this.mandatoryValidator = bluePrint.getMandatoryValidator();
		this.editModeInputHint = bluePrint.getEditModeInputHint();
		this.createModeInputHint = bluePrint.getCreateModeInputHint();
		this.attributes = new HashMap<String, IAttribute<Object>>();
		this.controls = new LinkedHashMap<String, IInputControl<Object>>();
		this.backgroundColors = new HashMap<String, IColorConstant>();
		this.bindingListeners = new HashMap<String, IInputListener>();
		this.validationListeners = new HashMap<String, IValidationConditionListener>();
		this.validationLabels = new HashMap<String, IValidationResultLabel>();
		this.validationResults = new LinkedHashMap<String, IValidationResult>();
		this.propertyChangeListenerBinding = new BeanPropertyChangeListenerBinding();
		this.propertyChangeListenerValidation = new BeanPropertyChangeListenerValidation();
		this.modificationStateListener = new BeanModificationStateListener();
		this.beanProcessStateListener = new BeanProcessStateListener();

		for (final IAttribute<?> attribute : bluePrint.getAttributes()) {
			this.attributes.put(attribute.getPropertyName(), (IAttribute<Object>) attribute);
		}

		composite.setLayout(new MigLayoutDescriptor("0[grow, 0::]0", "0[][grow, 0::]0"));
		this.validationLabelContainer = composite.add(BPF.composite(), "growx, growy, w 30::, h 20::, wrap");
		validationLabelContainer.setLayout(new MigLayoutDescriptor("hidemode 3", "0[grow, 0::]0", "0[grow, 0::]0"));
		editModeValidationLabel = addValidationLabel(validationLabelContainer, bluePrint.getEditModeValidationLabel());
		createModeValidationLabel = addValidationLabel(validationLabelContainer, bluePrint.getCreateModeValidationLabel());
		createModeValidationLabel.setVisible(false);

		final IScrollComposite contentPane = composite.add(BPF.scrollComposite(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

		//this must be the last invocation in this constructor
		layouter.layout(contentPane, new BeanFormControlFactory());
	}

	private IInputComponentValidationLabel addValidationLabel(
		final IContainer container,
		final IInputComponentValidationLabelSetup setup) {
		final IInputComponentValidationLabelBluePrint validationLabelBp = Toolkit.getBluePrintFactory().inputComponentValidationLabel();
		validationLabelBp.setSetup(setup);
		validationLabelBp.setInputComponent(this);
		final IInputComponentValidationLabel result = container.add(validationLabelBp, "growx, growy, w 30::, h 20::, wrap");
		result.setMinSize(new Dimension(20, 20));
		return result;
	}

	@Override
	protected IComposite getWidget() {
		return (IComposite) super.getWidget();
	}

	@Override
	public void dispose() {
		if (this.bean != null) {
			this.bean.removePropertyChangeListener(propertyChangeListenerBinding);
			this.bean.removePropertyChangeListener(propertyChangeListenerValidation);
			this.bean.removeModificationStateListener(modificationStateListener);
			this.bean.removeProcessStateListener(beanProcessStateListener);
		}
		super.dispose();
	}

	@Override
	public void setValue(final IBeanProxy<BEAN_TYPE> bean) {
		if (this.bean != null) {
			this.bean.removePropertyChangeListener(propertyChangeListenerBinding);
			this.bean.removePropertyChangeListener(propertyChangeListenerValidation);
			this.bean.removeModificationStateListener(modificationStateListener);
			this.bean.removeProcessStateListener(beanProcessStateListener);
		}

		this.bean = bean;
		if (bean == null || bean.isDummy()) {
			for (final Entry<String, IInputControl<Object>> entry : controls.entrySet()) {
				final IInputControl<Object> control = entry.getValue();
				final IAttribute<?> attribute = attributes.get(entry.getKey());
				control.setValue(null);
				control.setEnabled(false);
				if (mandatoryBackgroundColor != null && attribute.isMandatory()) {
					control.setBackgroundColor(backgroundColors.get(entry.getKey()));
				}
			}
			validationLabelContainer.layoutBegin();
			createModeValidationLabel.setVisible(false);
			editModeValidationLabel.setVisible(true);
			validationLabelContainer.layoutEnd();
		}
		else {
			if (bean.isTransient() != createModeValidationLabel.isVisible()) {
				validationLabelContainer.layoutBegin();
				createModeValidationLabel.setVisible(bean.isTransient());
				editModeValidationLabel.setVisible(!bean.isTransient());
				validationLabelContainer.layoutEnd();
			}
			for (final Entry<String, IInputControl<Object>> entry : controls.entrySet()) {
				final IAttribute<?> attribute = attributes.get(entry.getKey());
				final String propertyName = attribute.getPropertyName();
				final IInputControl<Object> control = entry.getValue();
				control.removeInputListener(bindingListeners.get(propertyName));
				control.removeValidationConditionListener(validationListeners.get(propertyName));
				control.setValue(bean.getValue(entry.getKey()));
				control.setEnabled(!bean.hasExecution() && !bean.hasErrors());
				if (mandatoryBackgroundColor != null && attribute.isMandatory() && attribute.isEditable()) {
					if (bean.hasExecution() || bean.hasErrors()) {
						control.setBackgroundColor(backgroundColors.get(propertyName));
					}
					else {
						control.setBackgroundColor(mandatoryBackgroundColor);
					}
				}
				else if (mandatoryBackgroundColor != null && attribute.isMandatory()) {
					control.setBackgroundColor(backgroundColors.get(entry.getKey()));
				}
				control.setEditable(attribute.isEditable() && !bean.hasExecution() && !bean.hasErrors());
				control.addInputListener(bindingListeners.get(propertyName));
				control.addValidationConditionListener(validationListeners.get(propertyName));
			}
			bean.addPropertyChangeListener(propertyChangeListenerBinding);
			bean.addPropertyChangeListener(propertyChangeListenerValidation);
			bean.addModificationStateListener(modificationStateListener);
			bean.addProcessStateListener(beanProcessStateListener);
		}
		validateAllProperties();
	}

	private void validateAllProperties() {
		resetValidation();
		resetModificationState();
		if (bean != null) {
			boolean validationChanged = false;
			for (final Entry<String, IInputControl<Object>> entry : controls.entrySet()) {
				final boolean propertyValidationChanged = validateProperty(entry.getKey(), entry.getValue());
				if (propertyValidationChanged) {
					validationChanged = true;
				}
			}
			if (validationChanged) {
				setValidationCacheDirty();
			}
		}
	}

	@Override
	public IBeanProxy<BEAN_TYPE> getValue() {
		return bean;
	}

	@Override
	public void setEditable(final boolean editable) {
		for (final IInputControl<Object> control : controls.values()) {
			control.setEditable(editable);
		}
	}

	@Override
	public boolean hasModifications() {
		if (bean != null && bean.hasModifications()) {
			return true;
		}
		for (final IInputControl<Object> control : controls.values()) {
			if (control.hasModifications()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void resetModificationState() {
		for (final IInputControl<Object> control : controls.values()) {
			control.resetModificationState();
		}
	}

	@Override
	public void resetValidation() {
		editModeValidationLabel.resetValidation();
		for (final IValidationResultLabel validationLabel : validationLabels.values()) {
			validationLabel.setEmpty();
		}
		validationResults.clear();
	}

	@Override
	protected IValidationResult createValidationResult() {
		final IValidationResultBuilder builder = ValidationResult.builder();
		for (final IValidationResult validationResult : validationResults.values()) {
			builder.addResult(validationResult);
		}
		return builder.build();
	}

	private boolean validateProperty(final String propertyName, final IInputControl<?> control) {
		boolean validationChanged = false;
		if (control != null && bean != null) {
			//only validate if bean value and control value are in sync, else
			//the validation will be triggered later 
			if (EmptyCompatibleEquivalence.equals(control.getValue(), bean.getValue(propertyName))) {
				IValidationResult validationResult = control.validate();
				//only validate the bean property, if the control is valid
				if (validationResult.isValid()) {
					validationResult = bean.validate(propertyName);
				}

				//change the validation map, if the worst first changed
				final IValidationResult lastResult = validationResults.get(propertyName);
				if (lastResult == null || !validationResult.getWorstFirst().equals(lastResult.getWorstFirst())) {
					final String inputHint = getInputHint();
					if (!validationResult.isValid() && !control.hasModifications() && inputHint != null) {
						validationResults.put(propertyName, ValidationResult.infoError(inputHint));
					}
					else {
						validationResults.put(propertyName, validationResult.withContext(getLabel(propertyName)));
					}
					validationChanged = true;
				}

				//update the validation label
				final IValidationResultLabel validationLabel = validationLabels.get(propertyName);
				if (validationLabel != null) {
					if (control.hasModifications()) {
						validationLabel.setResult(validationResult);
					}
					else {
						validationLabel.setEmpty();
					}
				}
			}
		}
		return validationChanged;
	}

	private String getInputHint() {
		if (bean != null) {
			if (bean.isTransient()) {
				return createModeInputHint;
			}
			else {
				return editModeInputHint;
			}
		}
		return null;
	}

	private String getLabel(final String propertyName) {
		Assert.paramNotNull(propertyName, "propertyName");
		return attributes.get(propertyName).getCurrentLabel();
	}

	private final class BeanFormControlFactory implements IBeanFormControlFactory {

		@Override
		public String getLabel(final String propertyName) {
			final String result = BeanFormImpl.this.getLabel(propertyName);
			if (mandatoryLabelDecorator != null && isMandatory(propertyName)) {
				return mandatoryLabelDecorator.decorate(result);
			}
			else {
				return result;
			}
		}

		@Override
		public ICustomWidgetCreator<? extends IControl> createControl(final String propertyName) {
			Assert.paramNotNull(propertyName, "propertyName");

			final ICustomWidgetCreator<IInputControl<Object>> widgetCreator = getWidgetCreator(attributes.get(propertyName));
			if (widgetCreator != null) {
				return new ICustomWidgetCreator<IInputControl<Object>>() {
					@Override
					public IInputControl<Object> create(final ICustomWidgetFactory widgetFactory) {
						if (controls.containsKey(propertyName)) {
							throw new IllegalStateException("Control must not be created more than once for the property '"
								+ propertyName
								+ "'.");
						}

						final IInputControl<Object> result = widgetCreator.create(widgetFactory);

						if (mandatoryValidator != null && isMandatory(propertyName)) {
							result.addValidator(mandatoryValidator);
						}

						backgroundColors.put(propertyName, result.getBackgroundColor());
						result.setEnabled(false);
						bindingListeners.put(propertyName, new BindingListener(propertyName, result));
						validationListeners.put(propertyName, new ValidationConditionListener(propertyName, result));
						controls.put(propertyName, result);

						return result;
					}
				};
			}
			return null;
		}

		@Override
		public ICustomWidgetCreator<? extends IControl> createValidationLabel(
			final String propertyName,
			final IValidationLabelSetup setup) {

			Assert.paramNotNull(propertyName, "propertyName");
			Assert.paramNotNull(setup, "setup");

			return new ICustomWidgetCreator<IControl>() {
				@Override
				public IControl create(final ICustomWidgetFactory widgetFactory) {
					if (validationLabels.containsKey(propertyName)) {
						throw new IllegalStateException("Validation label must not be created more than onnce for the property '"
							+ propertyName
							+ "'.");
					}
					final IValidationResultLabelBluePrint validationLabelBp = BPF.validationResultLabel();
					validationLabelBp.setSetup(setup);
					final IValidationResultLabel validationLabel = widgetFactory.create(validationLabelBp);
					validationLabels.put(propertyName, validationLabel);
					return validationLabel;
				}
			};

		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		private ICustomWidgetCreator<IInputControl<Object>> getWidgetCreator(final IAttribute<Object> attribute) {
			final IControlPanelProvider<Object> controlPanel = attribute.getCurrentControlPanel();

			final ICustomWidgetCreator controlCreator;
			if (attribute.isCollectionType()) {
				controlCreator = controlPanel.getCollectionControlCreator();
			}
			else {
				controlCreator = controlPanel.getControlCreator();
			}
			//if (controlCreator == null && controlPanel.getObjectLabelConverter() != null) {
			//TODO MG add readonly input field (must be added to jo widgets
			//}

			return controlCreator;
		}

		private boolean isMandatory(final String propertyName) {
			final IAttribute<Object> attribute = attributes.get(propertyName);
			if (attribute != null) {
				return attribute.isMandatory();
			}
			else {
				return false;
			}
		}

	}

	private final class BeanPropertyChangeListenerValidation implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			if (validateProperty(evt.getPropertyName(), controls.get(evt.getPropertyName()))) {
				setValidationCacheDirty();
			}
		}
	}

	private final class ValidationConditionListener implements IValidationConditionListener {

		private final String propertyName;
		private final IInputControl<Object> control;

		private ValidationConditionListener(final String propertyName, final IInputControl<Object> control) {
			super();
			this.propertyName = propertyName;
			this.control = control;
		}

		@Override
		public void validationConditionsChanged() {
			if (validateProperty(propertyName, control)) {
				setValidationCacheDirty();
			}
		}
	}

	private final class BeanPropertyChangeListenerBinding implements PropertyChangeListener {

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			if (bean != null) {
				final String propertyName = evt.getPropertyName();
				final IInputControl<Object> control = controls.get(propertyName);
				if (control != null) {
					control.removeInputListener(bindingListeners.get(propertyName));
					bean.removePropertyChangeListener(propertyChangeListenerBinding);
					control.setValue(evt.getNewValue());
					bean.addPropertyChangeListener(propertyChangeListenerBinding);
					control.addInputListener(bindingListeners.get(propertyName));
				}
			}
		}
	}

	private final class BindingListener implements IInputListener {

		private final String propertyName;
		private final IInputControl<Object> control;

		private BindingListener(final String propertyName, final IInputControl<Object> control) {
			this.propertyName = propertyName;
			this.control = control;
		}

		@Override
		public void inputChanged() {
			if (bean != null) {
				control.removeInputListener(bindingListeners.get(propertyName));
				bean.removeModificationStateListener(modificationStateListener);
				bean.removePropertyChangeListener(propertyChangeListenerBinding);
				bean.removePropertyChangeListener(propertyChangeListenerValidation);
				bean.setValue(propertyName, control.getValue());
				bean.addPropertyChangeListener(propertyChangeListenerValidation);
				bean.addPropertyChangeListener(propertyChangeListenerBinding);
				bean.addModificationStateListener(modificationStateListener);
				control.addInputListener(bindingListeners.get(propertyName));
			}
			fireInputChanged();
		}
	}

	private final class BeanModificationStateListener implements IBeanModificationStateListener<BEAN_TYPE> {
		@Override
		public void modificationStateChanged(final IBeanProxy<BEAN_TYPE> bean) {
			validateAllProperties();
		}
	}

	private final class BeanProcessStateListener implements IBeanProcessStateListener<BEAN_TYPE> {
		@Override
		public void processStateChanged(final IBeanProxy<BEAN_TYPE> bean) {
			if (BeanFormImpl.this.bean != null) {
				for (final Entry<String, IInputControl<Object>> entry : controls.entrySet()) {
					final IAttribute<?> attribute = attributes.get(entry.getKey());
					final IInputControl<Object> control = entry.getValue();
					control.setEnabled(!bean.hasExecution());
					if (mandatoryBackgroundColor != null && attribute.isMandatory()) {
						if (bean.hasExecution()) {
							control.setBackgroundColor(null);
						}
						else {
							control.setBackgroundColor(mandatoryBackgroundColor);
						}
					}
					control.setEditable(attribute.isEditable() && !bean.hasExecution());
				}
			}
		}
	}

}
