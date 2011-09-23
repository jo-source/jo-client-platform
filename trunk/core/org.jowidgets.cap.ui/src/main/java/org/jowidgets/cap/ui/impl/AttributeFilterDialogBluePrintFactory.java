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

import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.blueprint.IInputDialogBluePrint;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.api.widgets.content.IInputContentContainer;
import org.jowidgets.api.widgets.content.IInputContentCreator;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.cap.ui.api.filter.IUiConfigurableFilter;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IAttributeFilterControl;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;

final class AttributeFilterDialogBluePrintFactory {

	private AttributeFilterDialogBluePrintFactory() {}

	static IInputDialogBluePrint<IUiConfigurableFilter<? extends Object>> createDialogBluePrint(
		final IBeanTableModel<?> model,
		final int columnIndex,
		final IExecutionContext executionContext,
		final IFilterType filterType) {

		final IBluePrintFactory bpf = Toolkit.getBluePrintFactory();
		final ICapApiBluePrintFactory capBpf = CapUiToolkit.bluePrintFactory();

		final IInputContentCreator<IUiConfigurableFilter<? extends Object>> contentCreator = new IInputContentCreator<IUiConfigurableFilter<? extends Object>>() {

			private IAttributeFilterControl filterControl;

			@Override
			public void setValue(final IUiConfigurableFilter<? extends Object> value) {
				filterControl.setValue(value);
			}

			@Override
			public IUiConfigurableFilter<? extends Object> getValue() {
				return filterControl.getValue();
			}

			@Override
			public void createContent(final IInputContentContainer container) {
				container.setLayout(new MigLayoutDescriptor("0[][grow, 0::]0", "0[]0"));

				final IAttribute<?> attribute = model.getAttribute(columnIndex);
				container.add(Toolkit.getBluePrintFactory().textLabel(attribute.getLabel()));

				filterControl = container.add(capBpf.attributeFilterControl(model.getAttributes()), "growx, w 0::");

				if (filterType != null) {
					filterControl.setAttribute(attribute, filterType);
				}
			}
		};

		final IInputDialogBluePrint<IUiConfigurableFilter<? extends Object>> dialogBp = bpf.inputDialog(contentCreator);
		dialogBp.setExecutionContext(executionContext);
		dialogBp.setSize(new Dimension(640, 150));

		return dialogBp;
	}
}
