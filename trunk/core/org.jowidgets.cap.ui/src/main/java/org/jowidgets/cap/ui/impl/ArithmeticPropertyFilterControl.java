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

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComboBox;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeFilter;
import org.jowidgets.cap.ui.api.filter.IFilterControl;
import org.jowidgets.cap.ui.api.filter.IOperatorProvider;
import org.jowidgets.cap.ui.api.filter.IUiArithmeticPropertyFilter;
import org.jowidgets.cap.ui.api.filter.IUiArithmeticPropertyFilterBuilder;
import org.jowidgets.cap.ui.api.filter.IUiFilterFactory;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.controller.InputObservable;
import org.jowidgets.tools.validation.MandatoryValidator;
import org.jowidgets.tools.validation.ValidationCache;
import org.jowidgets.tools.validation.ValidationCache.IValidationResultCreator;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidationResultBuilder;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.ValidationResult;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ArithmeticPropertyFilterControl<ELEMENT_VALUE_TYPE> extends ControlWrapper implements
		IFilterControl<ArithmeticOperator, Object, IUiArithmeticPropertyFilter<Object>> {

	//TODO i18n
	private static final String AND = "AND";

	private final String propertyName;

	private final ValidationCache validationCache;
	private final InputObservable inputObservable;
	private final IInputListener inputListener;
	private final List<String> properties;

	private IComboBox<String> combo1;
	private IComboBox<String> combo2;

	private ArithmeticOperator operator;

	ArithmeticPropertyFilterControl(
		final String propertyName,
		final IOperatorProvider<ArithmeticOperator> operatorProvider,
		final List<? extends IAttribute<?>> attributes,
		final IAttributeFilter attributeFilter,
		final IComposite composite) {
		super(composite);

		this.propertyName = propertyName;
		this.properties = getFilteredProperties(propertyName, attributes, attributeFilter);

		this.inputObservable = new InputObservable();

		this.validationCache = new ValidationCache(new IValidationResultCreator() {
			@Override
			public IValidationResult createValidationResult() {
				final IValidationResultBuilder builder = ValidationResult.builder();
				return builder.build();
			}
		});

		this.inputListener = new IInputListener() {
			@Override
			public void inputChanged() {
				inputObservable.fireInputChanged();
			}
		};

		setOperator(operatorProvider.getDefaultOperator());
	}

	private static List<String> getFilteredProperties(
		final String propertyName,
		final List<? extends IAttribute<?>> attributes,
		final IAttributeFilter attributeFilter) {

		final List<String> result = new LinkedList<String>();

		for (final IAttribute<?> attribute : attributes) {
			if (!propertyName.equals(attribute.getPropertyName()) && attributeFilter.accept(attribute)) {
				result.add(attribute.getPropertyName());
			}
		}
		return result;
	}

	@Override
	protected IComposite getWidget() {
		return (IComposite) super.getWidget();
	}

	@Override
	public void addValidator(final IValidator<IUiArithmeticPropertyFilter<Object>> validator) {

	}

	@Override
	public void trySetOperand(final Object value) {
		// TODO implement trySetOperand
	}

	@Override
	public Object getOperand() {
		// TODO implement getOperand
		return null;
	}

	@Override
	public void setOperator(final ArithmeticOperator operator) {
		if (this.operator != operator) {
			this.operator = operator;
			removeInputListener();
			getWidget().removeAll();
			combo1 = null;
			combo2 = null;
			if (operator.getParameterCount() == 1) {
				getWidget().setLayout(new MigLayoutDescriptor("0[grow]0", "0[]0"));
				combo1 = getWidget().add(Toolkit.getBluePrintFactory().comboBoxSelection(properties));
			}
			else if (operator.getParameterCount() == 2) {
				getWidget().setLayout(new MigLayoutDescriptor("0[grow][][grow]0", "0[]0"));
				combo1 = getWidget().add(Toolkit.getBluePrintFactory().comboBoxSelection(properties));
				getWidget().add(Toolkit.getBluePrintFactory().textLabel(AND));
				combo2 = getWidget().add(Toolkit.getBluePrintFactory().comboBoxSelection(properties));
			}
			addInputListener();
			addValidators();
		}
	}

	@Override
	public void setValue(final IUiArithmeticPropertyFilter<Object> filter) {
		Assert.paramNotNull(filter, "filter");
		setOperator(filter.getOperator());
		if (operator.getParameterCount() == 1) {
			combo1.setValue(filter.getRightHandPropertyNames()[0]);
		}
		else if (operator.getParameterCount() == 2) {
			combo1.setValue(filter.getRightHandPropertyNames()[0]);
			combo2.setValue(filter.getRightHandPropertyNames()[1]);
		}
		resetModificationState();
	}

	@Override
	public IUiArithmeticPropertyFilter<Object> getValue() {
		final IUiFilterFactory filterFactory = CapUiToolkit.filterToolkit().filterFactory();
		final IUiArithmeticPropertyFilterBuilder<Object> filterBuilder = filterFactory.arithmeticPropertyFilterBuilder();
		filterBuilder.setOperator(operator);
		filterBuilder.setLeftHandPropertyName(propertyName);
		if (operator.getParameterCount() >= 1) {
			filterBuilder.addRightHandPropertyName(combo1.getValue());
		}
		if (operator.getParameterCount() == 2) {
			filterBuilder.addRightHandPropertyName(combo2.getValue());
		}
		return filterBuilder.build();
	}

	@Override
	public boolean hasModifications() {
		return hasModifications(combo1) || hasModifications(combo2);
	}

	@Override
	public void resetModificationState() {
		resetModificationState(combo1);
		resetModificationState(combo2);
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
	public void setEditable(final boolean editable) {
		setEditable(combo1, editable);
		setEditable(combo2, editable);
	}

	@Override
	public void addInputListener(final IInputListener listener) {
		inputObservable.addInputListener(listener);
	}

	@Override
	public void removeInputListener(final IInputListener listener) {
		inputObservable.removeInputListener(listener);
	}

	private void removeInputListener() {
		removeInputListener(combo1);
		removeInputListener(combo2);
	}

	private void addInputListener() {
		addInputListener(combo1);
		addInputListener(combo2);
	}

	private void addInputListener(final IInputControl<?> control) {
		if (control != null) {
			control.addInputListener(inputListener);
		}
	}

	private void removeInputListener(final IInputControl<?> control) {
		if (control != null) {
			control.removeInputListener(inputListener);
		}
	}

	private void addValidators() {
		addValidator(combo1);
		addValidator(combo2);
	}

	private void addValidator(final IInputControl control) {
		if (control != null) {
			control.addValidator(new MandatoryValidator<Object>());
		}
	}

	private boolean hasModifications(final IInputControl<?> control) {
		return control != null && control.hasModifications();
	}

	private void resetModificationState(final IInputControl<?> control) {
		if (control != null) {
			control.resetModificationState();
		}
	}

	private void setEditable(final IInputControl<?> control, final boolean editable) {
		if (control != null) {
			control.setEditable(editable);
		}
	}

}
