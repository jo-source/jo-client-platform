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

import org.jowidgets.api.widgets.IButton;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IFrame;
import org.jowidgets.api.widgets.blueprint.IButtonBluePrint;
import org.jowidgets.api.widgets.descriptor.setup.IButtonSetup;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.widgets.IBeanDialog;
import org.jowidgets.cap.ui.api.widgets.IBeanDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanForm;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.common.widgets.controller.IActionListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.tools.widgets.wrapper.WindowWrapper;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;

class BeanDialogImpl<BEAN_TYPE> extends WindowWrapper implements IBeanDialog<BEAN_TYPE> {

	private static final IMessage NO_MODIFICATIONS = Messages.getMessage("BeanDialogImpl.no_modifications");

	private final IButton okButton;
	private final IBeanForm<BEAN_TYPE> beanForm;
	private final String okButtonTooltip;

	private boolean okPressed;

	public BeanDialogImpl(final IFrame frame, final IBeanDialogBluePrint<BEAN_TYPE> bluePrint) {
		super(frame);

		this.okPressed = false;

		frame.setLayout(new MigLayoutDescriptor("0[grow, 0::]0", "0[grow, 0::][]0"));
		final IBeanFormBluePrint<BEAN_TYPE> beanFormBp = CapUiToolkit.bluePrintFactory().beanForm();
		beanFormBp.setSetup(bluePrint.getBeanForm());
		beanFormBp.setSaveAction(null).setUndoAction(null);
		beanForm = frame.add(beanFormBp, MigLayoutFactory.GROWING_CELL_CONSTRAINTS + ", wrap");

		final IComposite buttonBar = frame.add(BPF.composite(), "alignx right");
		buttonBar.setLayout(new MigLayoutDescriptor("[][]", "[]"));

		final IButtonBluePrint okButtonBp = createButtonBluePrint(bluePrint.getOkButton());
		okButtonTooltip = okButtonBp.getToolTipText();
		final String buttonConstraints = "w 100::, sg bg";
		this.okButton = buttonBar.add(okButtonBp, buttonConstraints);
		final IButton cancelButton = buttonBar.add(createButtonBluePrint(bluePrint.getCancelButton()), buttonConstraints);

		beanForm.addValidationConditionListener(new IValidationConditionListener() {
			@Override
			public void validationConditionsChanged() {
				setOkButtonEnabledState();
			}
		});

		okButton.addActionListener(new IActionListener() {
			@Override
			public void actionPerformed() {
				okPressed = true;
				frame.setVisible(false);
			}
		});

		cancelButton.addActionListener(new IActionListener() {
			@Override
			public void actionPerformed() {
				okPressed = false;
				frame.setVisible(false);
			}
		});

		frame.setDefaultButton(okButton);
	}

	private void setOkButtonEnabledState() {
		final IValidationResult validationResult = beanForm.validate();
		final IBeanProxy<BEAN_TYPE> bean = beanForm.getValue();
		if (validationResult.isValid()) {
			if (!bean.isTransient() && !bean.hasModifications()) {
				okButton.setToolTipText(NO_MODIFICATIONS.get());
				okButton.setEnabled(false);
			}
			else {
				okButton.setToolTipText(okButtonTooltip);
				okButton.setEnabled(true);
			}
		}
		else {
			okButton.setToolTipText(validationResult.getWorstFirst().getText());
			okButton.setEnabled(false);
		}
	}

	private static IButtonBluePrint createButtonBluePrint(final IButtonSetup buttonSetup) {
		return BPF.button().setSetup(buttonSetup);
	}

	@Override
	protected IFrame getWidget() {
		return (IFrame) super.getWidget();
	}

	@Override
	public IBeanProxy<BEAN_TYPE> getBean() {
		return beanForm.getValue();
	}

	@Override
	public void setBean(final IBeanProxy<BEAN_TYPE> bean) {
		beanForm.setValue(bean);
		setOkButtonEnabledState();
	}

	@Override
	public void setMinSize(final int width, final int height) {
		getWidget().setMinSize(width, height);
	}

	@Override
	public boolean isOkPressed() {
		return okPressed;
	}

	@Override
	public void setVisible(final boolean visible) {
		okPressed = false;
		super.setVisible(visible);
	}

}
