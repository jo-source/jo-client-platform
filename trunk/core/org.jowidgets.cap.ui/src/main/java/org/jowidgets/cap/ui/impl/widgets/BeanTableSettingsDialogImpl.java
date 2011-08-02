/*
 * Copyright (c) 2011, nimoll
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

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IButton;
import org.jowidgets.api.widgets.ICheckBox;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IFrame;
import org.jowidgets.api.widgets.IInputField;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialog;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialogBluePrint;
import org.jowidgets.common.widgets.controler.IActionListener;
import org.jowidgets.common.widgets.controler.IInputListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.widgets.wrapper.WindowWrapper;
import org.jowidgets.util.Assert;

final class BeanTableSettingsDialogImpl extends WindowWrapper implements IBeanTableSettingsDialog {

	private final IBluePrintFactory bpF;
	private final IBeanTableModel<?> model;
	private final IFrame frame;

	private final BeanTableAttributeListImpl beanTableAttributeListImpl;
	private final IBeanTableConfig currentConfig;

	@SuppressWarnings("unused")
	private boolean okPressed;

	@SuppressWarnings("unused")
	private final ICheckBox autoSelection;

	BeanTableSettingsDialogImpl(final IFrame frame, final IBeanTableSettingsDialogBluePrint setup) {
		super(frame);
		Assert.paramNotNull(frame, "frame");
		Assert.paramNotNull(setup, "setup");
		Assert.paramNotNull(setup.getModel(), "setup.getModel()");

		this.bpF = Toolkit.getBluePrintFactory();
		this.frame = frame;
		this.model = setup.getModel();
		this.currentConfig = model.getConfig();

		frame.setLayout(new MigLayoutDescriptor("[][grow]", "[][][][][grow][pref!]"));

		// common settings
		// TODO i18n
		frame.add(bpF.textSeparator("Common settings"), "grow, span, wrap");
		autoSelection = frame.add(bpF.checkBox().setText("Auto selection"), "grow, span, wrap");

		// TODO i18n 
		frame.add(bpF.textSeparator("Columns"), "grow, span, wrap");
		frame.add(bpF.textLabel("Search:"), "");
		final IInputField<String> filter = frame.add(bpF.inputFieldString(), "wrap, grow");
		filter.addInputListener(new IInputListener() {

			@Override
			public void inputChanged() {
				beanTableAttributeListImpl.setFilter(filter.getText());
			}
		});

		beanTableAttributeListImpl = new BeanTableAttributeListImpl(frame.add(
				bpF.compositeWithBorder(),
				"grow, wrap, span, w 0::, h 0::"), model);

		createButtonBar(frame.add(bpF.composite(), "alignx right, span, wrap"));
		frame.pack();
	}

	@Override
	public IBeanTableConfig show() {
		okPressed = false;
		beanTableAttributeListImpl.updateValues(model.getConfig());
		frame.setVisible(true);

		// TODO NM build current config
		return currentConfig;
	}

	@Override
	public boolean isOkPressed() {
		return false;
		// TODO NM return okPressed;
	}

	private void createButtonBar(final IComposite buttonBar) {

		buttonBar.setLayout(new MigLayoutDescriptor("0[][]0", "0[]0"));
		final IButton ok = buttonBar.add(bpF.button("Ok"), "w 80::, aligny b, sg bg");
		ok.addActionListener(new IActionListener() {
			@Override
			public void actionPerformed() {
				okPressed = true;
				setVisible(false);
			}
		});
		frame.setDefaultButton(ok);

		final IButton cancel = buttonBar.add(bpF.button("Cancel", "w 80::, aligny b, sg bg"));
		cancel.addActionListener(new IActionListener() {
			@Override
			public void actionPerformed() {
				setVisible(false);
			}
		});
	}
}
