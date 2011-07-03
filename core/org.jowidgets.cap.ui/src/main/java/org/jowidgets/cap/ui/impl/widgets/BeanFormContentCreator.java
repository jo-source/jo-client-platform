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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.validation.ValidationResult;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.api.widgets.IValidationLabel;
import org.jowidgets.api.widgets.blueprint.IValidationLabelBluePrint;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.api.widgets.content.IInputContentContainer;
import org.jowidgets.api.widgets.content.IInputContentCreator;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.form.IBeanFormGroup;
import org.jowidgets.cap.ui.api.form.IBeanFormLayout;
import org.jowidgets.cap.ui.api.form.IBeanFormProperty;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;

class BeanFormContentCreator<BEAN_TYPE> implements IInputContentCreator<IBeanProxy<BEAN_TYPE>> {

	private final IBeanFormLayout layout;
	private final Map<String, IAttribute<Object>> attributes;
	private final Map<String, IInputControl<Object>> controls;

	private IBeanProxy<BEAN_TYPE> bean;

	@SuppressWarnings("unchecked")
	BeanFormContentCreator(final IBeanFormLayout layout, final Collection<? extends IAttribute<?>> attributes) {
		this.layout = layout;
		this.attributes = new HashMap<String, IAttribute<Object>>();
		this.controls = new HashMap<String, IInputControl<Object>>();

		for (final IAttribute<?> attribute : attributes) {
			this.attributes.put(attribute.getPropertyName(), (IAttribute<Object>) attribute);
		}
	}

	@Override
	public void setValue(final IBeanProxy<BEAN_TYPE> bean) {
		this.bean = bean;
		if (bean == null) {
			for (final IInputControl<Object> control : controls.values()) {
				control.setValue(null);
			}
		}
		else {
			for (final Entry<String, IInputControl<Object>> entry : controls.entrySet()) {
				entry.getValue().setValue(bean.getValue(entry.getKey()));
			}
		}
	}

	@Override
	public IBeanProxy<BEAN_TYPE> getValue() {
		return bean;
	}

	@Override
	public ValidationResult validate() {
		return new ValidationResult();
	}

	@Override
	public void createContent(final IInputContentContainer container) {
		final IBluePrintFactory bpf = Toolkit.getBluePrintFactory();

		//TODO MG this must be done with respect of the defined layout
		container.setLayout(new MigLayoutDescriptor("[]8[grow][]", ""));
		for (final IBeanFormGroup group : layout.getGroups()) {
			for (final IBeanFormProperty property : group.getProperties()) {

				final String propertyName = property.getPropertyName();
				final IAttribute<Object> attribute = attributes.get(propertyName);

				final ICustomWidgetCreator<IInputControl<Object>> widgetCreator = getWidgetCreator(attribute);
				if (widgetCreator != null) {
					//add label
					container.add(bpf.textLabel(attribute.getLabel()).alignRight(), "alignx r, sg lg");

					//add control
					final IInputControl<Object> control = container.add(widgetCreator, "growx");
					container.registerInputWidget(attribute.getLabel(), control);
					controls.put(propertyName, control);

					//add validation label
					final IValidationLabelBluePrint validationLabelBp = bpf.validationLabel();
					validationLabelBp.setSetup(property.getValidationLabel());
					final IValidationLabel validationLabel = container.add(validationLabelBp, "w 20::, wrap");
					validationLabel.registerInputWidget(control);
				}
			}
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private ICustomWidgetCreator<IInputControl<Object>> getWidgetCreator(final IAttribute<Object> attribute) {
		final IControlPanelProvider<Object> controlPanel = attribute.getDefaultControlPanel();

		final ICustomWidgetCreator controlCreator;
		if (attribute.isCollectionType()) {
			controlCreator = controlPanel.getCollectionControlCreator();
		}
		else {
			controlCreator = controlPanel.getControlCreator();
		}
		return controlCreator;
	}

	@Override
	public boolean isMandatory() {
		return false;
	}

}
