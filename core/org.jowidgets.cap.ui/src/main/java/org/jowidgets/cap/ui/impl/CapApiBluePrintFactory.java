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

import java.util.List;

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.form.IBeanFormLayout;
import org.jowidgets.cap.ui.api.form.IBeanFormToolkit;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IAttributeFilterControlBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.util.Assert;

final class CapApiBluePrintFactory implements ICapApiBluePrintFactory {

	private final IBluePrintFactory bluePrintFactory;

	CapApiBluePrintFactory() {
		this.bluePrintFactory = Toolkit.getBluePrintFactory();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <BEAN_TYPE> IBeanTableBluePrint<BEAN_TYPE> beanTable(final IBeanTableModel<BEAN_TYPE> model) {
		Assert.paramNotNull(model, "model");
		final IBeanTableBluePrint<BEAN_TYPE> result = bluePrintFactory.bluePrint(IBeanTableBluePrint.class);
		result.setModel(model);
		return result;
	}

	@Override
	public IBeanTableSettingsDialogBluePrint beanTableSettingsDialog(final IBeanTableModel<?> model) {
		Assert.paramNotNull(model, "model");
		final IBeanTableSettingsDialogBluePrint result = bluePrintFactory.bluePrint(IBeanTableSettingsDialogBluePrint.class);
		result.setModel(model);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <BEAN_TYPE> IBeanFormBluePrint<BEAN_TYPE> beanForm(final List<? extends IAttribute<?>> attributes) {
		Assert.paramNotNull(attributes, "attributes");
		final IBeanFormBluePrint<BEAN_TYPE> result = bluePrintFactory.bluePrint(IBeanFormBluePrint.class);
		result.setAttributes(attributes);
		final IBeanFormToolkit beanFormToolkit = CapUiToolkit.beanFormToolkit();
		final IBeanFormLayout layout = CapUiToolkit.beanFormToolkit().layoutBuilder().addGroups(attributes).build();
		result.setLayouter(beanFormToolkit.layouter(layout));
		return result;
	}

	@Override
	public IAttributeFilterControlBluePrint attributeFilterControl(final List<? extends IAttribute<?>> attributes) {
		Assert.paramNotNull(attributes, "attributes");
		final IAttributeFilterControlBluePrint result = bluePrintFactory.bluePrint(IAttributeFilterControlBluePrint.class);
		result.setAttributes(attributes);
		return result;
	}

}
