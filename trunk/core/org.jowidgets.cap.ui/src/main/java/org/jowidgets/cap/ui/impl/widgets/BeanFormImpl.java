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
import java.util.Map;
import java.util.Map.Entry;

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IControl;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.api.widgets.IScrollComposite;
import org.jowidgets.api.widgets.IValidationResultLabel;
import org.jowidgets.api.widgets.blueprint.IValidationResultLabelBluePrint;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.api.widgets.descriptor.setup.IValidationLabelSetup;
import org.jowidgets.cap.ui.api.attribute.DisplayFormat;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.form.IBeanFormControlFactory;
import org.jowidgets.cap.ui.api.form.IBeanFormLayouter;
import org.jowidgets.cap.ui.api.widgets.IBeanForm;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.common.widgets.controler.IInputListener;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.factory.ICustomWidgetFactory;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.controler.InputObservable;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidationResultBuilder;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.ValidationResult;

final class BeanFormImpl<BEAN_TYPE> extends ControlWrapper implements IBeanForm<BEAN_TYPE> {

	private final IBeanFormLayouter layouter;
	private final Map<String, IAttribute<Object>> attributes;
	private final Map<String, IInputControl<Object>> controls;
	private final Map<String, IInputListener> inputListeners;
	private final Map<String, IValidationResultLabel> validationLabels;
	private final PropertyChangeListener propertyChangeListener;
	private final IBluePrintFactory bpf;
	private final InputObservable inputObservable;

	private final IValidationResultLabel mainValidationLabel;

	private IBeanProxy<BEAN_TYPE> bean;

	@SuppressWarnings("unchecked")
	BeanFormImpl(final IComposite composite, final IBeanFormBluePrint<BEAN_TYPE> bluePrint) {
		super(composite);
		this.layouter = bluePrint.getLayouter();
		this.attributes = new HashMap<String, IAttribute<Object>>();
		this.controls = new HashMap<String, IInputControl<Object>>();
		this.inputListeners = new HashMap<String, IInputListener>();
		this.validationLabels = new HashMap<String, IValidationResultLabel>();
		this.propertyChangeListener = new BeanPropertyChangeListener();
		this.inputObservable = new InputObservable();
		this.bpf = Toolkit.getBluePrintFactory();

		for (final IAttribute<?> attribute : bluePrint.getAttributes()) {
			this.attributes.put(attribute.getPropertyName(), (IAttribute<Object>) attribute);
		}

		composite.setLayout(new MigLayoutDescriptor("0[grow]0", "0[][grow]0"));
		final String contentConstraints = "growx, growy, w 30::, h 20::, wrap";
		this.mainValidationLabel = composite.add(bluePrint.getValidationLabel(), contentConstraints);
		mainValidationLabel.setMinSize(new Dimension(20, 20));

		final IScrollComposite contentPane = composite.add(bpf.scrollComposite(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

		//this must be the last invocation in this constructor
		layouter.layout(contentPane, new BeanFormControlFactory());
	}

	@Override
	protected IComposite getWidget() {
		return (IComposite) super.getWidget();
	}

	@Override
	public void setValue(final IBeanProxy<BEAN_TYPE> bean) {
		if (this.bean != null) {
			this.bean.removePropertyChangeListener(propertyChangeListener);
		}

		this.bean = bean;
		if (bean == null) {
			for (final IInputControl<Object> control : controls.values()) {
				control.setValue(null);
				control.setEnabled(false);
			}
		}
		else {
			for (final Entry<String, IInputControl<Object>> entry : controls.entrySet()) {
				final IAttribute<?> attribute = attributes.get(entry.getKey());
				final String propertyName = attribute.getPropertyName();
				final IInputControl<Object> control = entry.getValue();
				control.removeInputListener(inputListeners.get(propertyName));
				control.setValue(bean.getValue(entry.getKey()));
				control.setEnabled(true);
				control.setEditable(attribute.isEditable());
				control.addInputListener(inputListeners.get(propertyName));
			}
			bean.addPropertyChangeListener(propertyChangeListener);
		}
		resetValidation();
	}

	@Override
	public IBeanProxy<BEAN_TYPE> getValue() {
		return bean;
	}

	@Override
	public void addValidator(final IValidator<IBeanProxy<BEAN_TYPE>> validator) {

	}

	@Override
	public IValidationResult validate() {
		final IValidationResultBuilder builder = ValidationResult.builder();
		for (final Entry<String, IInputControl<Object>> controlEntry : controls.entrySet()) {
			final String propertyName = controlEntry.getKey();
			final IInputControl<Object> control = controlEntry.getValue();
			final IAttribute<?> attribute = attributes.get(propertyName);
			final String label = attribute.getLabel();
			final IValidationResult validationResult = control.validate();
			builder.addResult(validationResult.withContext(label));
		}

		return builder.build();
	}

	@Override
	public void setEditable(final boolean editable) {

	}

	@Override
	public boolean hasModifications() {
		// TODO MG implement hasModifications
		return false;
	}

	@Override
	public void resetModificationState() {
		// TODO MG implement resetModificationState
	}

	@Override
	public void addValidationConditionListener(final IValidationConditionListener listener) {
		// TODO MG implement addValidationStateListener
	}

	@Override
	public void removeValidationConditionListener(final IValidationConditionListener listener) {
		// TODO MG implement removeValidationStateListener
	}

	@Override
	public void addInputListener(final IInputListener listener) {
		inputObservable.addInputListener(listener);
	}

	@Override
	public void removeInputListener(final IInputListener listener) {
		inputObservable.removeInputListener(listener);
	}

	@Override
	public void resetValidation() {
		mainValidationLabel.setEmpty();
		for (final IValidationResultLabel validationLabel : validationLabels.values()) {
			validationLabel.setEmpty();
		}
	}

	private final class BeanFormControlFactory implements IBeanFormControlFactory {

		@Override
		public String getLabel(final String propertyName) {
			Assert.paramNotNull(propertyName, "propertyName");

			final IAttribute<Object> attribute = attributes.get(propertyName);
			if (DisplayFormat.LONG == attribute.getLabelDisplayFormat()) {
				return attribute.getLabelLong();
			}
			else {
				return attribute.getLabel();
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

						result.addValidator(new IValidator<Object>() {
							@Override
							public IValidationResult validate(final Object value) {
								if (bean != null) {
									return bean.validate(propertyName, value);
								}
								return ValidationResult.ok();
							}
						});
						result.setEnabled(false);
						inputListeners.put(propertyName, new InputListener(propertyName, result));
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
					final IValidationResultLabelBluePrint validationLabelBp = bpf.validationResultLabel();
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
			return controlCreator;
		}

	}

	private final class BeanPropertyChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			if (bean != null) {
				final IInputControl<Object> control = controls.get(evt.getPropertyName());
				if (control != null) {
					control.setValue(evt.getNewValue());
					if (!bean.isModified(evt.getPropertyName())) {
						validationLabels.get(evt.getPropertyName()).setEmpty();
					}
				}
			}
		}

	}

	private final class InputListener implements IInputListener {

		private final String propertyName;
		private final IInputControl<Object> control;

		private InputListener(final String propertyName, final IInputControl<Object> control) {
			this.propertyName = propertyName;
			this.control = control;
		}

		@Override
		public void inputChanged() {
			if (bean != null) {
				bean.removePropertyChangeListener(propertyChangeListener);
				bean.setValue(propertyName, control.getValue());
				//getWidget().layoutBegin();
				inputObservable.fireInputChanged();
				//mainValidationLabel.redraw();
				//getWidget().layoutEnd();
				bean.addPropertyChangeListener(propertyChangeListener);
			}
		}
	}
}
