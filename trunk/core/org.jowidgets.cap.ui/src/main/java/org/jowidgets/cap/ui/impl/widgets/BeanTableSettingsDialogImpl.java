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
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IFrame;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialog;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialogBluePrint;
import org.jowidgets.common.widgets.controler.IActionListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.widgets.wrapper.WindowWrapper;
import org.jowidgets.util.Assert;

@SuppressWarnings("unused")
final class BeanTableSettingsDialogImpl extends WindowWrapper implements IBeanTableSettingsDialog {

	private final IBluePrintFactory bpF;
	private final IBeanTableModel<?> model;
	private final IFrame frame;

	private final BeanTableAttributeListImpl beanTableAttributeListImpl;
	private final IBeanTableConfig currentConfig;

	private boolean okPressed;

	BeanTableSettingsDialogImpl(final IFrame frame, final IBeanTableSettingsDialogBluePrint setup) {
		super(frame);
		Assert.paramNotNull(frame, "frame");
		Assert.paramNotNull(setup, "setup");
		Assert.paramNotNull(setup.getModel(), "setup.getModel()");

		this.bpF = Toolkit.getBluePrintFactory();
		this.frame = frame;
		this.model = setup.getModel();
		this.currentConfig = model.getConfig();

		frame.setLayout(new MigLayoutDescriptor("[grow]", "[grow][top]"));

		beanTableAttributeListImpl = new BeanTableAttributeListImpl(frame.add(
				bpF.compositeWithBorder(),
				"grow, wrap, w 0::, h 0::"), model);
		createButtonBar(frame.add(bpF.composite(), "align right, wrap"));
		frame.pack();
	}

	@Override
	public IBeanTableConfig show() {
		okPressed = false;
		frame.setVisible(true);
		return currentConfig;
	}

	@Override
	public boolean isOkPressed() {
		return okPressed;
	}

	private void createButtonBar(final IComposite buttonBar) {
		buttonBar.setLayout(new MigLayoutDescriptor("[]10[]0", ""));
		final IButton ok = buttonBar.add(bpF.button("Ok"), "sg btn");
		ok.addActionListener(new IActionListener() {
			@Override
			public void actionPerformed() {
				okPressed = true;
				setVisible(false);
			}
		});

		final IButton cancel = buttonBar.add(bpF.button("Cancel", "sg btn, wrap"));
		cancel.addActionListener(new IActionListener() {
			@Override
			public void actionPerformed() {
				setVisible(false);
			}
		});
	}

}
