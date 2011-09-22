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
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.filter.IFilterControl;
import org.jowidgets.cap.ui.api.filter.IFilterPanelProvider;
import org.jowidgets.cap.ui.api.filter.IFilterSupport;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.cap.ui.api.filter.IUiConfigurableFilter;
import org.jowidgets.cap.ui.api.widgets.IAttributeFilterControl;
import org.jowidgets.cap.ui.api.widgets.IAttributeFilterControlBluePrint;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.factory.ICustomWidgetFactory;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.ValidationResult;

//TODO MG implement AttributeFilterControlImpl
final class AttributeFilterControlImpl extends ControlWrapper implements IAttributeFilterControl {

	private static final String NOT = "NOT";

	private final List<IAttribute<?>> attributes;
	private final Map<String, IAttribute<?>> attributesMap;

	@SuppressWarnings("unused")
	private final IComboBox<String> cmbNot;
	private final IComboBox<IOperator> cmbOperator;

	private IFilterControl<IOperator, Object, IUiConfigurableFilter<Object>> filterControl;

	AttributeFilterControlImpl(final IComposite composite, final IAttributeFilterControlBluePrint bluePrint) {
		super(composite);
		this.attributes = bluePrint.getAttributes();
		this.attributesMap = createAttributesMap(attributes);

		composite.setLayout(new MigLayoutDescriptor("0[][][grow]0", "0[]0"));

		final IBluePrintFactory bpf = Toolkit.getBluePrintFactory();

		this.cmbNot = composite.add(bpf.comboBoxSelection().setAutoCompletion(false).setElements("", NOT).autoSelectionOn());
		this.cmbOperator = composite.add(comboBoxOperatorBp());
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
	public void setAttribute(final IAttribute<?> attribute, final IFilterType filterType) {
		Assert.paramNotNull(attribute, "attribute");
		Assert.paramNotNull(filterType, "filterType");

		final IFilterSupport<Object> filterSupport = attribute.getCurrentControlPanel().getFilterSupport();
		final IFilterPanelProvider<?> filterPanelProvider = getFilterPanelProvider(filterSupport, filterType);

		final ICustomWidgetCreator<IFilterControl<IOperator, Object, IUiConfigurableFilter<Object>>> creator;
		creator = createPanelCreator(filterPanelProvider);

		final IComposite composite = getWidget();
		if (filterControl != null) {
			composite.remove(filterControl);
		}
		filterControl = composite.add(creator, "growx, w 0::");
		cmbOperator.setElements(filterPanelProvider.getOperatorProvider().getOperators());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setValue(final IUiConfigurableFilter<? extends Object> value) {
		if (value != null) {
			final IAttribute<?> attribute = attributesMap.get(value.getPropertyName());
			if (attribute == null) {
				throw new IllegalArgumentException("No attribute found for the property name '" + value.getPropertyName() + "'.");
			}
			setAttribute(attribute, value.getType());
			cmbOperator.setValue(value.getOperator());
			filterControl.setValue((IUiConfigurableFilter<Object>) value);
		}

	}

	@Override
	public IUiConfigurableFilter<? extends Object> getValue() {
		return null;
	}

	@Override
	public boolean hasModifications() {
		return false;
	}

	@Override
	public void resetModificationState() {

	}

	@Override
	public IValidationResult validate() {
		return ValidationResult.ok();
	}

	@Override
	public void addValidator(final IValidator<IUiConfigurableFilter<? extends Object>> validator) {

	}

	@Override
	public void addValidationConditionListener(final IValidationConditionListener listener) {

	}

	@Override
	public void removeValidationConditionListener(final IValidationConditionListener listener) {

	}

	@Override
	public void setEditable(final boolean editable) {

	}

	@Override
	public void addInputListener(final IInputListener listener) {

	}

	@Override
	public void removeInputListener(final IInputListener listener) {

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

	private IFilterPanelProvider<?> getFilterPanelProvider(
		final IFilterSupport<Object> filterSupport,
		final IFilterType filterType) {

		for (final IFilterPanelProvider<?> filterPanelProvider : filterSupport.getFilterPanels()) {
			if (filterType.getId().equals(filterPanelProvider.getType().getId())) {
				return filterPanelProvider;
			}
		}

		return null;
	}
}
