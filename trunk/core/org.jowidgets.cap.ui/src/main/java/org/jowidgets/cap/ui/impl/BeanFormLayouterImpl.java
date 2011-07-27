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
		//TODO NM this must be done with respect of the defined layout
		final IBluePrintFactory bpf = Toolkit.getBluePrintFactory();
		container.setLayout(new MigLayoutDescriptor("0[]8[grow][]0", ""));
		for (final IBeanFormGroup group : layout.getGroups()) {
			for (final IBeanFormProperty property : group.getProperties()) {

				final String propertyName = property.getPropertyName();

				final ICustomWidgetCreator<? extends IControl> controlCreator = controlFactory.createControl(propertyName);

				//only add to the layout if there is a control for this property
				if (controlCreator != null) {
					//add label
					container.add(bpf.textLabel(controlFactory.getLabel(propertyName)).alignRight(), "alignx r, sg lg");

					//add control
					container.add(controlCreator, "growx");

					//add validation label
					final ICustomWidgetCreator<? extends IControl> validationLabelCreator = controlFactory.createValidationLabel(
							propertyName,
							property.getValidationLabel());

					container.add(validationLabelCreator, "w 25::, wrap");
				}
			}
		}

	}
}
