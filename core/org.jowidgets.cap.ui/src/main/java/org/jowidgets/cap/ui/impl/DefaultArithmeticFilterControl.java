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

import java.util.Collection;

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.filter.IFilterControl;
import org.jowidgets.cap.ui.api.filter.IOperatorProvider;
import org.jowidgets.cap.ui.api.filter.IUiArithmeticFilter;
import org.jowidgets.cap.ui.api.filter.IUiArithmeticFilterBuilder;
import org.jowidgets.cap.ui.api.filter.IUiFilterFactory;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.controller.InputObservable;
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
public class DefaultArithmeticFilterControl<ELEMENT_VALUE_TYPE> extends ControlWrapper implements
		IFilterControl<ArithmeticOperator, Object, IUiArithmeticFilter<Object>> {

	//TODO i18n
	private static final String AND = "AND";

	private final String propertyName;

	private final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator;
	private final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator;

	private final ValidationCache validationCache;
	private final InputObservable inputObservable;
	private final IInputListener inputListener;

	private IInputControl control1;
	private IInputControl control2;
	private IInputControl collectionControl1;
	private IInputControl collectionControl2;

	private ArithmeticOperator operator;

	DefaultArithmeticFilterControl(
		final String propertyName,
		final IOperatorProvider<ArithmeticOperator> operatorProvider,
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator,
		final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator,
		final IComposite composite) {
		super(composite);

		this.propertyName = propertyName;
		this.controlCreator = controlCreator;
		this.collectionControlCreator = collectionControlCreator;

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

	@Override
	protected IComposite getWidget() {
		return (IComposite) super.getWidget();
	}

	@Override
	public void addValidator(final IValidator<IUiArithmeticFilter<Object>> validator) {

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
			control1 = null;
			control2 = null;
			collectionControl1 = null;
			collectionControl2 = null;
			if (operator.getParameterCount() == 1) {
				getWidget().setLayout(new MigLayoutDescriptor("0[grow]0", "0[]0"));
				if (operator.isCollectionOperator()) {
					collectionControl1 = getWidget().add(collectionControlCreator, "growx");
				}
				else {
					control1 = getWidget().add(controlCreator, "growx");
				}
			}
			else if (operator.getParameterCount() == 2) {
				getWidget().setLayout(new MigLayoutDescriptor("0[grow][][grow]0", "0[]0"));
				if (operator.isCollectionOperator()) {
					collectionControl1 = getWidget().add(collectionControlCreator, "growx");
					getWidget().add(Toolkit.getBluePrintFactory().textLabel(AND));
					collectionControl2 = getWidget().add(collectionControlCreator, "growx");
				}
				else {
					control1 = getWidget().add(controlCreator, "growx");
					getWidget().add(Toolkit.getBluePrintFactory().textLabel(AND));
					control2 = getWidget().add(controlCreator, "growx");
				}
			}
			addInputListener();
		}
	}

	@Override
	public void setValue(final IUiArithmeticFilter<Object> filter) {
		Assert.paramNotNull(filter, "filter");
		setOperator(filter.getOperator());
		if (operator.getParameterCount() == 1) {
			if (operator.isCollectionOperator()) {
				collectionControl1.setValue((filter.getParameters()[0]));
			}
			else {
				control1.setValue((filter.getParameters()[0]));
			}
		}
		else if (operator.getParameterCount() == 2) {
			if (operator.isCollectionOperator()) {
				collectionControl1.setValue((filter.getParameters()[0]));
				collectionControl2.setValue((filter.getParameters()[1]));
			}
			else {
				control1.setValue((filter.getParameters()[0]));
				control2.setValue((filter.getParameters()[1]));
			}
		}
		resetModificationState();
	}

	@Override
	public IUiArithmeticFilter<Object> getValue() {
		final IUiFilterFactory filterFactory = CapUiToolkit.filterToolkit().filterFactory();
		final IUiArithmeticFilterBuilder<Object> filterBuilder = filterFactory.arithmeticFilterBuilder();
		filterBuilder.setOperator(operator);
		filterBuilder.setPropertyName(propertyName);
		if (operator.getParameterCount() >= 1) {
			if (operator.isCollectionOperator()) {
				filterBuilder.addParameter(collectionControl1.getValue());
			}
			else {
				filterBuilder.addParameter(control1.getValue());
			}
		}
		if (operator.getParameterCount() == 2) {
			if (operator.isCollectionOperator()) {
				filterBuilder.addParameter(collectionControl2.getValue());
			}
			else {
				filterBuilder.addParameter(control2.getValue());
			}
		}
		return filterBuilder.build();
	}

	@Override
	public boolean hasModifications() {
		return hasModifications(control1)
			|| hasModifications(control2)
			|| hasModifications(collectionControl1)
			|| hasModifications(collectionControl2);
	}

	@Override
	public void resetModificationState() {
		resetModificationState(control1);
		resetModificationState(control2);
		resetModificationState(collectionControl1);
		resetModificationState(collectionControl2);
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
		setEditable(control1, editable);
		setEditable(control2, editable);
		setEditable(collectionControl1, editable);
		setEditable(collectionControl2, editable);
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
		removeInputListener(control1);
		removeInputListener(control2);
		removeInputListener(collectionControl1);
		removeInputListener(collectionControl2);
	}

	private void addInputListener() {
		addInputListener(control1);
		addInputListener(control2);
		addInputListener(collectionControl1);
		addInputListener(collectionControl2);
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
