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

import java.util.Arrays;
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
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.tools.validation.MandatoryValidator;
import org.jowidgets.tools.widgets.wrapper.AbstractInputControl;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidationResultBuilder;
import org.jowidgets.validation.ValidationResult;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DefaultArithmeticFilterControl<ELEMENT_VALUE_TYPE> extends AbstractInputControl<IUiArithmeticFilter<Object>> implements
		IFilterControl<ArithmeticOperator, Object, IUiArithmeticFilter<Object>> {

	private static final IMessage AND = Messages.getMessage("DefaultArithmeticFilterControl.and"); //$NON-NLS-1$
	private static final IMessage OPERAND_EMPTY = Messages.getMessage("DefaultArithmeticFilterControl.operand_must_not_be_empty"); //$NON-NLS-1$
	private static final IMessage FIRST_OPERAND_EMPTY = Messages.getMessage("DefaultArithmeticFilterControl.first_operand_must_not_be_empty"); //$NON-NLS-1$
	private static final IMessage SECOND_OPERAND_EMPTY = Messages.getMessage("DefaultArithmeticFilterControl.second_operand_must_not_be_empty"); //$NON-NLS-1$

	private final String propertyName;
	private final Class<?> elementValueType;
	private final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator;
	private final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator;

	private final IInputListener inputListener;
	private final IValidationConditionListener validationConditionListener;

	private IInputControl control1;
	private IInputControl control2;
	private IInputControl collectionControl;

	private ArithmeticOperator operator;

	private boolean editable;

	DefaultArithmeticFilterControl(
		final String propertyName,
		final Class<?> elementValueType,
		final IOperatorProvider<ArithmeticOperator> operatorProvider,
		final ICustomWidgetCreator<IInputControl<ELEMENT_VALUE_TYPE>> controlCreator,
		final ICustomWidgetCreator<IInputControl<? extends Collection<ELEMENT_VALUE_TYPE>>> collectionControlCreator,
		final IComposite composite) {
		super(composite);

		this.editable = true;

		this.propertyName = propertyName;
		this.elementValueType = elementValueType;
		this.controlCreator = controlCreator;
		this.collectionControlCreator = collectionControlCreator;

		this.inputListener = new IInputListener() {
			@Override
			public void inputChanged() {
				fireInputChanged();
			}
		};

		this.validationConditionListener = new IValidationConditionListener() {
			@Override
			public void validationConditionsChanged() {
				setValidationCacheDirty();
			}
		};

		setOperator(operatorProvider.getDefaultOperator());
	}

	@Override
	protected IValidationResult createValidationResult() {
		final IValidationResultBuilder builder = ValidationResult.builder();

		if (control1 != null) {
			builder.addResult(control1.validate());
		}
		if (control2 != null) {
			builder.addResult(control2.validate());
		}
		if (collectionControl != null) {
			builder.addResult(collectionControl.validate());
		}

		return builder.build();
	}

	@Override
	protected IComposite getWidget() {
		return (IComposite) super.getWidget();
	}

	@Override
	public void trySetOperand(final Object value) {
		if (value instanceof Object[]) {
			final Object[] parameters = (Object[]) value;
			if (ArithmeticOperator.BETWEEN == operator) {
				if (parameters.length > 0 && isTypeCompatible(parameters[0])) {
					control1.setValue(parameters[0]);
				}
				if (parameters.length > 1 && isTypeCompatible(parameters[1])) {
					control2.setValue(parameters[1]);
				}
			}
			else if (ArithmeticOperator.EMPTY != operator) {
				if (isCollectionOperator(operator)) {
					if (isTypeCompatible(parameters)) {
						collectionControl.setValue(Arrays.asList(parameters));
					}
				}
				else if (parameters.length > 0 && isTypeCompatible(parameters[0])) {
					control1.setValue(parameters[0]);
				}
			}
		}
	}

	private boolean isTypeCompatible(final Object[] array) {
		if (array != null) {
			for (final Object object : array) {
				if (!isTypeCompatible(object)) {
					return false;
				}
			}
			return true;
		}
		else {
			return false;
		}
	}

	private boolean isTypeCompatible(final Object value) {
		if (value != null) {
			return elementValueType.isAssignableFrom(value.getClass());
		}
		else {
			return true;
		}
	}

	@Override
	public Object getOperand() {
		return getParameters();
	}

	@Override
	public void setOperator(final ArithmeticOperator operator) {
		if (this.operator != operator) {
			this.operator = operator;
			removeInputListener();
			getWidget().removeAll();
			control1 = null;
			control2 = null;
			collectionControl = null;

			if (ArithmeticOperator.BETWEEN == operator) {
				getWidget().setLayout(new MigLayoutDescriptor("0[grow][][grow]0", "0[]0")); //$NON-NLS-1$ //$NON-NLS-2$
				control1 = getWidget().add(controlCreator, "growx"); //$NON-NLS-1$
				getWidget().add(Toolkit.getBluePrintFactory().textLabel(AND.get()));
				control2 = getWidget().add(controlCreator, "growx"); //$NON-NLS-1$
			}
			else if (ArithmeticOperator.EMPTY != operator) {
				getWidget().setLayout(new MigLayoutDescriptor("0[grow]0", "0[]0")); //$NON-NLS-1$ //$NON-NLS-2$
				if (isCollectionOperator(operator)) {
					collectionControl = getWidget().add(collectionControlCreator, "growx"); //$NON-NLS-1$
				}
				else {
					control1 = getWidget().add(controlCreator, "growx"); //$NON-NLS-1$
				}
			}
			else {
				getWidget().setLayout(new MigLayoutDescriptor("0[]0", "0[]0")); //$NON-NLS-1$ //$NON-NLS-2$
				getWidget().add(Toolkit.getBluePrintFactory().textLabel());
			}
			addInputListener();
			addValidators();
			setValidationCacheDirty();
		}
	}

	@Override
	public void setValue(final IUiArithmeticFilter<Object> filter) {
		Assert.paramNotNull(filter, "filter"); //$NON-NLS-1$
		setOperator(filter.getOperator());

		if (filter.getParameters() != null) {
			if (ArithmeticOperator.BETWEEN == operator) {
				control1.setValue((filter.getParameters()[0]));
				control2.setValue((filter.getParameters()[1]));
			}
			else if (ArithmeticOperator.EMPTY != operator) {
				if (isCollectionOperator(operator)) {
					collectionControl.setValue(Arrays.asList(filter.getParameters()));
				}
				else {
					control1.setValue((filter.getParameters()[0]));
				}
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
		filterBuilder.setParameters(getParameters());

		return filterBuilder.build();
	}

	@Override
	public boolean hasModifications() {
		return hasModifications(control1) || hasModifications(control2) || hasModifications(collectionControl);
	}

	@Override
	public void resetModificationState() {
		resetModificationState(control1);
		resetModificationState(control2);
		resetModificationState(collectionControl);
	}

	@Override
	public void setEditable(final boolean editable) {
		this.editable = editable;
		setEditable(control1, editable);
		setEditable(control2, editable);
		setEditable(collectionControl, editable);
	}

	@Override
	public boolean isEditable() {
		return editable;
	}

	private Object[] getParameters() {
		if (ArithmeticOperator.BETWEEN == operator) {
			return new Object[] {control1.getValue(), control2.getValue()};
		}
		else if (ArithmeticOperator.EMPTY != operator) {
			if (isCollectionOperator(operator)) {
				final Object value = collectionControl.getValue();
				if (value instanceof Collection<?>) {
					final Collection collection = (Collection) value;
					return collection.toArray();
				}
			}
			else {
				return new Object[] {control1.getValue()};
			}
		}
		return new Object[] {};
	}

	private void removeInputListener() {
		removeListeners(control1);
		removeListeners(control2);
		removeListeners(collectionControl);
	}

	private void addInputListener() {
		addListeners(control1);
		addListeners(control2);
		addListeners(collectionControl);
	}

	private void addListeners(final IInputControl<?> control) {
		if (control != null) {
			control.addInputListener(inputListener);
			control.addValidationConditionListener(validationConditionListener);
		}
	}

	private void removeListeners(final IInputControl<?> control) {
		if (control != null) {
			control.removeInputListener(inputListener);
			control.removeValidationConditionListener(validationConditionListener);
		}
	}

	private void addValidators() {
		if (ArithmeticOperator.BETWEEN == operator) {
			addValidator(control1, FIRST_OPERAND_EMPTY.get());
			addValidator(control2, SECOND_OPERAND_EMPTY.get());
		}
		else if (ArithmeticOperator.EMPTY != operator) {
			addValidator(control1, OPERAND_EMPTY.get());
			addValidator(collectionControl, OPERAND_EMPTY.get());
		}
	}

	private void addValidator(final IInputControl<Object> control, final String message) {
		if (control != null) {
			control.addValidator(new MandatoryValidator<Object>(message));
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

	private boolean isCollectionOperator(final ArithmeticOperator operator) {
		return operator == ArithmeticOperator.CONTAINS_ANY || operator == ArithmeticOperator.CONTAINS_ALL;
	}

}
