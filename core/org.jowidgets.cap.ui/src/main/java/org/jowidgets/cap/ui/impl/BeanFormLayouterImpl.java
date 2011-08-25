/*
 * Copyright (c) 2011, grossmann, Nikolaus Moll
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

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.IControl;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.ui.api.form.IBeanFormControlFactory;
import org.jowidgets.cap.ui.api.form.IBeanFormGroup;
import org.jowidgets.cap.ui.api.form.IBeanFormLayout;
import org.jowidgets.cap.ui.api.form.IBeanFormLayouter;
import org.jowidgets.cap.ui.api.form.IBeanFormProperty;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.util.Assert;

final class BeanFormLayouterImpl implements IBeanFormLayouter {

	private final IBeanFormLayout layout;

	BeanFormLayouterImpl(final IBeanFormLayout layout) {
		Assert.paramNotNull(layout, "layout");
		this.layout = layout;
	}

	@Override
	public void layout(final IContainer container, final IBeanFormControlFactory controlFactory) {
		final IBluePrintFactory bpf = Toolkit.getBluePrintFactory();
		final int columnCount = layout.getColumnCount();

		container.setLayout(new MigLayoutDescriptor(getColumnConstraints(layout), ""));

		int row = 0;
		//TODO NM this must be done with respect of the defined layout
		for (final IBeanFormGroup group : layout.getGroups()) {
			// reset column index
			int column = 0;

			for (final IBeanFormProperty property : group.getProperties()) {
				final String propertyName = property.getPropertyName();

				final ICustomWidgetCreator<? extends IControl> controlCreator = controlFactory.createControl(propertyName);

				//only add to the layout if there is a control for this property
				if (controlCreator == null) {
					continue;
				}

				final int propertyColumnCount = property.getColumnCount();
				final int propertyColumnSpan = property.getColumnSpan();

				int nextColumn = column + propertyColumnCount;
				if (nextColumn > columnCount) {
					// next row
					row++;
					column = 0;
					nextColumn = propertyColumnCount;
				}

				final String sizeGroupLabel = "sg c" + column + "l";
				final ICustomWidgetCreator<? extends IControl> validationLabelCreator = controlFactory.createValidationLabel(
						propertyName,
						property.getValidationLabel());
				String controlConstraints = "";

				int splitCount = 1;
				if (property.showLabel()) {
					splitCount++;
				}
				if (validationLabelCreator != null) {
					splitCount++;
				}

				if (!property.showLabel()) {
					controlConstraints = "split " + splitCount;
				}

				String cell = "cell " + column + " " + row + " " + propertyColumnSpan;
				//add label
				if (property.showLabel()) {
					container.add(
							bpf.textLabel(controlFactory.getLabel(propertyName)).alignRight(),
							constraints(cell, "split " + splitCount, "alignx r", sizeGroupLabel));
					cell = "";
				}

				//add control
				container.add(controlCreator, constraints(cell, controlConstraints, "growx", "span " + propertyColumnSpan));

				//add validation label
				if (validationLabelCreator != null) {
					container.add(validationLabelCreator, "w 25::");
				}

				column = nextColumn;
			}

		}
	}

	private static String constraints(final String... constraints) {
		final StringBuilder result = new StringBuilder();
		for (final String constraint : constraints) {
			if (result.length() > 0) {
				result.append(", ");
			}
			result.append(constraint);
		}
		return result.toString();
	}

	private static String getColumnConstraints(final IBeanFormLayout layout) {
		final StringBuilder result = new StringBuilder();
		result.append("0");
		for (int column = 0; column < layout.getColumnCount(); column++) {
			if (column > 0) {
				// TODO NM add gap ?
				result.append("0");
			}

			final Integer columnMinSize = layout.getColumnMinSize(column);
			final Integer columnMaxSize = layout.getColumnMaxSize(column);
			result.append('[');
			if (columnMinSize != null) {
				result.append(columnMinSize.toString());
			}
			if (columnMinSize != null && columnMaxSize != null) {
				result.append("::");
			}
			else {
				result.append("grow");
			}
			if (columnMaxSize != null) {
				result.append(columnMaxSize.toString());
			}
			result.append(']');
		}
		result.append("0");

		return result.toString();
	}
}
