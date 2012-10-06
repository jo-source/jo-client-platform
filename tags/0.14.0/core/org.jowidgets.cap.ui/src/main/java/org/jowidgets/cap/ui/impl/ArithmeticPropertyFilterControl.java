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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.api.convert.IObjectStringConverter;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComboBox;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.api.widgets.blueprint.IComboBoxSelectionBluePrint;
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
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.tools.message.MessageReplacer;
import org.jowidgets.tools.validation.MandatoryValidator;
import org.jowidgets.tools.widgets.wrapper.AbstractInputControl;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.ValidationResult;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ArithmeticPropertyFilterControl<ELEMENT_VALUE_TYPE> extends
		AbstractInputControl<IUiArithmeticPropertyFilter<Object>> implements
		IFilterControl<ArithmeticOperator, Object, IUiArithmeticPropertyFilter<Object>> {

	private static final IMessage AND = Messages.getMessage("ArithmeticPropertyFilterControl.and");
	private static final IMessage NO_ATTRIBUTE_COULD_BE_COMPARED_WITH = Messages.getMessage("ArithmeticPropertyFilterControl.no_attribute_could_be_compared_with");

	private static final IObjectStringConverter<IAttribute<?>> ATTRIBUTE_CONVERTER = attributeConverter();

	private final String propertyName;

	private final IInputListener inputListener;
	private final IValidationConditionListener validationConditionListener;
	private final Map<String, IAttribute<?>> attributesMap;
	private final List<IAttribute<?>> attributes;
	private final List<IAttribute<?>> collectionTypeAttributes;
	private final IAttribute<?> attribute;

	private IComboBox<IAttribute<?>> combo1;
	private IComboBox<IAttribute<?>> combo2;

	private ArithmeticOperator operator;

	ArithmeticPropertyFilterControl(
		final String propertyName,
		final IOperatorProvider<ArithmeticOperator> operatorProvider,
		final List<? extends IAttribute<?>> attributes,
		final IAttributeFilter attributeFilter,
		final IComposite composite) {
		super(composite);

		this.propertyName = propertyName;
		this.attributesMap = getAttributesMap((List<IAttribute<?>>) attributes);
		this.attributes = getFilteredAttributes(propertyName, attributes, attributeFilter);
		this.attribute = attributesMap.get(propertyName);

		this.collectionTypeAttributes = getCollectionTypeAttributes(this.attributes);

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

	private static List<IAttribute<?>> getFilteredAttributes(
		final String propertyName,
		final List<? extends IAttribute<?>> attributes,
		final IAttributeFilter attributeFilter) {

		final List<IAttribute<?>> result = new LinkedList<IAttribute<?>>();

		for (final IAttribute<?> attribute : attributes) {
			if (!propertyName.equals(attribute.getPropertyName())
				&& attributeFilter.accept(attribute)
				&& attribute.isFilterable()) {
				result.add(attribute);
			}
		}
		return result;
	}

	private static Map<String, IAttribute<?>> getAttributesMap(final List<IAttribute<?>> attributes) {

		final Map<String, IAttribute<?>> result = new HashMap<String, IAttribute<?>>();

		for (final IAttribute<?> attribute : attributes) {
			result.put(attribute.getPropertyName(), attribute);
		}
		return result;
	}

	private static List<IAttribute<?>> getCollectionTypeAttributes(final List<IAttribute<?>> attributes) {

		final List<IAttribute<?>> result = new LinkedList<IAttribute<?>>();

		for (final IAttribute<?> attribute : attributes) {
			if (attribute.isCollectionType()) {
				result.add(attribute);
			}
		}
		return result;
	}

	private static IObjectStringConverter<IAttribute<?>> attributeConverter() {
		return new IObjectStringConverter<IAttribute<?>>() {

			@Override
			public String convertToString(final IAttribute<?> value) {
				return value.getCurrentLabel();
			}

			@Override
			public String getDescription(final IAttribute<?> value) {
				return value.getDescription().get();
			}
		};
	}

	@Override
	protected IComposite getWidget() {
		return (IComposite) super.getWidget();
	}

	@Override
	protected IValidationResult createValidationResult() {
		if (combo1 != null && combo1.getElements().isEmpty() || combo2 != null && combo2.getElements().isEmpty()) {
			final String message = MessageReplacer.replace(NO_ATTRIBUTE_COULD_BE_COMPARED_WITH.get(), attribute.getCurrentLabel());
			return ValidationResult.infoError(message); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return ValidationResult.ok();

	}

	@Override
	public void trySetOperand(final Object value) {
		// TODO MG implement trySetOperand
	}

	@Override
	public Object getOperand() {
		// TODO MG implement getOperand
		return null;
	}

	@Override
	public void setOperator(final ArithmeticOperator operator) {
		Assert.paramNotNull(operator, "operator"); //$NON-NLS-1$
		if (this.operator != operator) {
			this.operator = operator;
			removeInputListener();
			getWidget().removeAll();
			combo1 = null;
			combo2 = null;
			if (ArithmeticOperator.BETWEEN == operator) {
				getWidget().setLayout(new MigLayoutDescriptor("0[grow][][grow]0", "0[]0")); //$NON-NLS-1$ //$NON-NLS-2$
				combo1 = getWidget().add(comboBoxBluePrint(attributes), "grow, w 0::"); //$NON-NLS-1$
				getWidget().add(Toolkit.getBluePrintFactory().textLabel(AND.get()));
				combo2 = getWidget().add(comboBoxBluePrint(attributes), "grow, w 0::"); //$NON-NLS-1$
			}
			else if (ArithmeticOperator.EMPTY != operator) {
				getWidget().setLayout(new MigLayoutDescriptor("0[grow]0", "0[]0")); //$NON-NLS-1$ //$NON-NLS-2$
				final List<IAttribute<?>> currentAttributes;
				if (isCollectionOperator(operator)) {
					currentAttributes = collectionTypeAttributes;
				}
				else {
					currentAttributes = attributes;
				}
				combo1 = getWidget().add(comboBoxBluePrint(currentAttributes), "grow, w 0::"); //$NON-NLS-1$
			}

			addInputListener();
			addValidators();
		}
	}

	@Override
	public void setValue(final IUiArithmeticPropertyFilter<Object> filter) {
		Assert.paramNotNull(filter, "filter"); //$NON-NLS-1$
		setOperator(filter.getOperator());
		if (filter.getRightHandPropertyNames() != null) {
			if (ArithmeticOperator.BETWEEN == operator) {
				combo1.setValue(attributesMap.get(filter.getRightHandPropertyNames()[0]));
				combo2.setValue(attributesMap.get(filter.getRightHandPropertyNames()[1]));
			}
			else if (ArithmeticOperator.EMPTY != operator) {
				combo1.setValue(attributesMap.get(filter.getRightHandPropertyNames()[0]));
			}
		}
		resetModificationState();
	}

	@Override
	public IUiArithmeticPropertyFilter<Object> getValue() {
		final IUiFilterFactory filterFactory = CapUiToolkit.filterToolkit().filterFactory();
		final IUiArithmeticPropertyFilterBuilder<Object> filterBuilder = filterFactory.arithmeticPropertyFilterBuilder();
		filterBuilder.setOperator(operator);
		filterBuilder.setPropertyName(propertyName);
		if (ArithmeticOperator.BETWEEN == operator) {
			if (combo1.getValue() == null || combo2.getValue() == null) {
				return null;
			}
			filterBuilder.addRightHandPropertyName(combo1.getValue().getPropertyName());
			filterBuilder.addRightHandPropertyName(combo2.getValue().getPropertyName());
		}
		else if (ArithmeticOperator.EMPTY != operator) {
			if (combo1.getValue() == null) {
				return null;
			}
			filterBuilder.addRightHandPropertyName(combo1.getValue().getPropertyName());
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
	public void setEditable(final boolean editable) {
		setEditable(combo1, editable);
		setEditable(combo2, editable);
	}

	private IComboBoxSelectionBluePrint<IAttribute<?>> comboBoxBluePrint(final List<IAttribute<?>> attributes) {
		final IComboBoxSelectionBluePrint<IAttribute<?>> result;
		result = Toolkit.getBluePrintFactory().comboBoxSelection(ATTRIBUTE_CONVERTER);
		result.setElements(attributes).autoSelectionOn();
		return result;
	}

	private void removeInputListener() {
		removeListeners(combo1);
		removeListeners(combo2);
	}

	private void addInputListener() {
		addListeners(combo1);
		addListeners(combo2);
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

	private boolean isCollectionOperator(final ArithmeticOperator operator) {
		return operator == ArithmeticOperator.CONTAINS_ALL || operator == ArithmeticOperator.CONTAINS_ALL;
	}

}
