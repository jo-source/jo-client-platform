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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jowidgets.api.convert.IObjectStringConverter;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComboBox;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.blueprint.IComboBoxSelectionBluePrint;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.common.api.filter.IOperator;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.filter.IFilterControl;
import org.jowidgets.cap.ui.api.filter.IFilterPanelProvider;
import org.jowidgets.cap.ui.api.filter.IFilterSupport;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.cap.ui.api.filter.IOperatorProvider;
import org.jowidgets.cap.ui.api.filter.IUiConfigurableFilter;
import org.jowidgets.cap.ui.api.widgets.IAttributeFilterControl;
import org.jowidgets.cap.ui.api.widgets.IAttributeFilterControlBluePrint;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.factory.ICustomWidgetFactory;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.widgets.wrapper.AbstractInputControl;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.ValidationResult;

final class AttributeFilterControlImpl extends AbstractInputControl<IUiConfigurableFilter<? extends Object>> implements
		IAttributeFilterControl {

	private static final String NOT = Messages.getString("AttributeFilterControlImpl.not");

	private final List<IAttribute<?>> attributes;
	private final Map<String, IAttribute<?>> attributesMap;

	private final IInputListener inputListener;
	private final IInputListener operatorListener;
	private final IValidationConditionListener validationConditionListener;

	private final IComboBox<Boolean> cmbNot;
	private final IComboBox<IOperator> cmbOperator;

	private IFilterControl<IOperator, Object, IUiConfigurableFilter<Object>> filterControl;
	private boolean editable;

	AttributeFilterControlImpl(final IComposite composite, final IAttributeFilterControlBluePrint bluePrint) {
		super(composite);
		this.attributes = bluePrint.getAttributes();
		this.attributesMap = createAttributesMap(attributes);
		this.editable = true;

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

		this.operatorListener = new IInputListener() {
			@Override
			public void inputChanged() {
				if (filterControl != null) {
					final Object currentOperand = getCurrentOperand();
					filterControl.setOperator(cmbOperator.getValue());
					tryToSetOldOperand(currentOperand);
					getWidget().layout();
				}
			}
		};

		composite.setLayout(new MigLayoutDescriptor("0[][][grow]0", "0[]0"));

		this.cmbNot = composite.add(comboBoxNotBp());
		cmbNot.addInputListener(inputListener);
		cmbNot.addValidationConditionListener(validationConditionListener);

		this.cmbOperator = composite.add(comboBoxOperatorBp());

		cmbOperator.addInputListener(operatorListener);
		cmbOperator.addValidationConditionListener(validationConditionListener);
	}

	private static IComboBoxSelectionBluePrint<IOperator> comboBoxOperatorBp() {
		final IBluePrintFactory bpf = Toolkit.getBluePrintFactory();
		final IObjectStringConverter<IOperator> converter = new IObjectStringConverter<IOperator>() {
			@Override
			public String convertToString(final IOperator value) {
				return value.getLabelLong();
			}

			@Override
			public String getDescription(final IOperator value) {
				return value.getDescription();
			}
		};
		return bpf.comboBoxSelection(converter).autoSelectionOn();
	}

	private static IComboBoxSelectionBluePrint<Boolean> comboBoxNotBp() {
		final IBluePrintFactory bpf = Toolkit.getBluePrintFactory();
		final IObjectStringConverter<Boolean> converter = new IObjectStringConverter<Boolean>() {
			@Override
			public String convertToString(final Boolean bool) {
				if (bool) {
					return NOT;
				}
				else {
					return "";
				}
			}

			@Override
			public String getDescription(final Boolean bool) {
				if (bool) {
					return NOT;
				}
				else {
					return "";
				}
			}
		};

		final IComboBoxSelectionBluePrint<Boolean> result = bpf.comboBoxSelection(converter);
		result.autoCompletionOff().autoSelectionOn().setElements(Boolean.FALSE, Boolean.TRUE);

		return result;
	}

	private static Map<String, IAttribute<?>> createAttributesMap(final List<IAttribute<?>> attributes) {
		final Map<String, IAttribute<?>> result = new HashMap<String, IAttribute<?>>();
		for (final IAttribute<?> attribute : attributes) {
			result.put(attribute.getPropertyName(), attribute);
		}
		return result;
	}

	@Override
	protected IComposite getWidget() {
		return (IComposite) super.getWidget();
	}

	@Override
	protected IValidationResult createValidationResult() {
		if (filterControl != null) {
			return filterControl.validate();
		}
		else {
			return ValidationResult.ok();
		}
	}

	@Override
	public void setAttribute(final IAttribute<?> attribute, final IFilterType filterType) {
		Assert.paramNotNull(attribute, "attribute");
		Assert.paramNotNull(filterType, "filterType");

		final Object currentOperand = getCurrentOperand();

		setAttributeImpl(attribute, filterType, true);

		tryToSetOldOperand(currentOperand);

		resetModificationState();

		getWidget().layout();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setValue(final IUiConfigurableFilter<? extends Object> value) {
		if (value != null) {
			final IAttribute<?> attribute = attributesMap.get(value.getPropertyName());
			if (attribute == null) {
				throw new IllegalArgumentException("No attribute found for the property name '" + value.getPropertyName() + "'.");
			}

			setAttributeImpl(attribute, value.getType(), false);
			cmbOperator.removeInputListener(operatorListener);
			cmbOperator.setValue(value.getOperator());
			cmbOperator.addInputListener(operatorListener);
			filterControl.setValue((IUiConfigurableFilter<Object>) value);

			resetModificationState();
			getWidget().layout();
		}
	}

	private void tryToSetOldOperand(final Object operand) {
		if (filterControl != null) {
			filterControl.trySetOperand(operand);
		}
	}

	private Object getCurrentOperand() {
		if (filterControl != null) {
			return filterControl.getOperand();
		}
		else {
			return null;
		}
	}

	private void setAttributeImpl(final IAttribute<?> attribute, final IFilterType filterType, final boolean setDefaultOperator) {
		Assert.paramNotNull(attribute, "attribute");
		Assert.paramNotNull(filterType, "filterType");

		final IFilterPanelProvider<?> filterPanelProvider = getFilterPanelProvider(attribute, filterType);

		final ICustomWidgetCreator<IFilterControl<IOperator, Object, IUiConfigurableFilter<Object>>> creator;
		creator = createPanelCreator(filterPanelProvider);

		final IComposite composite = getWidget();
		if (filterControl != null) {
			filterControl.removeInputListener(inputListener);
			composite.remove(filterControl);
		}
		filterControl = composite.add(creator, "growx, w 0::");
		filterControl.setEditable(editable);
		filterControl.addInputListener(inputListener);
		filterControl.addValidationConditionListener(validationConditionListener);

		final IOperatorProvider<?> operatorProvider = filterPanelProvider.getOperatorProvider();
		cmbOperator.removeInputListener(operatorListener);
		cmbOperator.setElements(operatorProvider.getOperators());
		if (setDefaultOperator) {
			final IOperator defaultOperator = operatorProvider.getDefaultOperator();
			cmbOperator.setValue(defaultOperator);
			filterControl.setOperator(defaultOperator);
		}
		cmbOperator.addInputListener(operatorListener);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IUiConfigurableFilter<? extends Object> getValue() {
		if (filterControl != null) {
			IUiConfigurableFilter<Object> result = filterControl.getValue();
			if (result != null && cmbNot.getValue().booleanValue()) {
				result = (IUiConfigurableFilter<Object>) CapUiToolkit.filterToolkit().filterTools().invert(result);
			}
			return result;
		}
		else {
			return null;
		}
	}

	@Override
	public boolean hasModifications() {
		return cmbOperator.hasModifications()
			|| cmbNot.hasModifications()
			|| (filterControl != null && filterControl.hasModifications());
	}

	@Override
	public void resetModificationState() {
		cmbOperator.resetModificationState();
		cmbNot.resetModificationState();
		if (filterControl != null) {
			filterControl.resetModificationState();
		}
	}

	@Override
	public void setEditable(final boolean editable) {
		this.editable = editable;
		cmbOperator.setEditable(editable);
		cmbNot.setEditable(editable);
		if (filterControl != null) {
			filterControl.setEditable(editable);
		}
	}

	private ICustomWidgetCreator<IFilterControl<IOperator, Object, IUiConfigurableFilter<Object>>> createPanelCreator(
		final IFilterPanelProvider<?> filterPanelProvider) {

		return new ICustomWidgetCreator<IFilterControl<IOperator, Object, IUiConfigurableFilter<Object>>>() {
			@SuppressWarnings("unchecked")
			@Override
			public IFilterControl<IOperator, Object, IUiConfigurableFilter<Object>> create(
				final ICustomWidgetFactory widgetFactory) {
				return (IFilterControl<IOperator, Object, IUiConfigurableFilter<Object>>) filterPanelProvider.getFilterControlCreator().create(
						widgetFactory,
						attributes);
			}
		};
	}

	private IFilterPanelProvider<?> getFilterPanelProvider(final IAttribute<?> attribute, final IFilterType filterType) {

		final IControlPanelProvider<?> controlPanel = attribute.getCurrentControlPanel();

		if (controlPanel == null) {
			throw new IllegalStateException("No filter control panel provider found for the attribute '"
				+ attribute.getPropertyName()
				+ "'");
		}

		final IFilterSupport<Object> filterSupport = controlPanel.getFilterSupport();

		if (filterSupport == null) {
			throw new IllegalStateException("No filter support found for the attribute '" + attribute.getPropertyName() + "'");
		}

		for (final IFilterPanelProvider<?> filterPanelProvider : filterSupport.getFilterPanels()) {
			if (filterType.getId().equals(filterPanelProvider.getType().getId())) {
				return filterPanelProvider;
			}
		}

		throw new IllegalStateException("No filter panel provider found for the attribute '"
			+ attribute.getPropertyName()
			+ "' and filter type '"
			+ filterType
			+ "'.");

	}
}
