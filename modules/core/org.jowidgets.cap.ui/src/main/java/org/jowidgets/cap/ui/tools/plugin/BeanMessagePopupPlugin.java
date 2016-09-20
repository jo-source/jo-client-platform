/*
 * Copyright (c) 2014, grossmann
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

package org.jowidgets.cap.ui.tools.plugin;

import org.jowidgets.api.image.Icons;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IButton;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IFrame;
import org.jowidgets.api.widgets.IIcon;
import org.jowidgets.api.widgets.ITextLabel;
import org.jowidgets.api.widgets.IWindow;
import org.jowidgets.api.widgets.blueprint.IDialogBluePrint;
import org.jowidgets.api.widgets.blueprint.IFrameBluePrint;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.plugin.IBeanProxyPlugin;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.common.widgets.controller.IActionListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.widgets.blueprint.BPF;

public final class BeanMessagePopupPlugin implements IBeanProxyPlugin {

	private BeanMessageDialog messageDialog;

	@Override
	public IBeanMessage addMessage(final IBeanProxy<?> bean, final IBeanMessage message) {
		if (message != null
			&& (BeanMessageType.ERROR.equals(message.getType()) || BeanMessageType.WARNING.equals(message.getType()))) {
			if (messageDialog != null && !messageDialog.isDisposed()) {
				messageDialog.setMessage(message);
			}
			else {
				messageDialog = new BeanMessageDialog(message);
			}
		}
		return message;
	}

	private final class BeanMessageDialog {

		private final IFrame dialog;
		private final ITextLabel label;
		private final IIcon icon;

		private BeanMessageDialog(final IBeanMessage message) {
			super();

			final IWindow activeWindow = Toolkit.getActiveWindow();
			if (activeWindow != null) {
				final IDialogBluePrint dialogBp = BPF.dialog().setAutoDispose(true).setModal(false);
				dialogBp.setTitle(message.getShortMessage());
				this.dialog = activeWindow.createChildWindow(dialogBp);
			}
			else {
				final IFrameBluePrint dialogBp = BPF.frame().setAutoDispose(true);
				dialogBp.setTitle(message.getShortMessage());
				this.dialog = Toolkit.createRootFrame(dialogBp);
			}

			dialog.setLayout(new MigLayoutDescriptor("[]20[grow]", "15[][]"));
			this.icon = dialog.add(BPF.icon(getIcon(message)));

			this.label = dialog.add(BPF.textLabel(message.getMessage()), "wrap");

			final IComposite buttonBar = dialog.add(BPF.composite(), "span, align center");
			buttonBar.setLayout(new MigLayoutDescriptor("[]", "[]"));

			final IButton okButton = buttonBar.add(BPF.buttonOk(), "w 80::, sg bg");

			okButton.addActionListener(new IActionListener() {
				@Override
				public void actionPerformed() {
					dialog.setVisible(false);
					messageDialog = null;
				}
			});

			dialog.setDefaultButton(okButton);
			dialog.setVisible(true);
		}

		private void setMessage(final IBeanMessage message) {
			dialog.pack();
			dialog.setTitle(message.getShortMessage());
			icon.setIcon(getIcon(message));
			final Dimension lastLabelSize = label.getPreferredSize();
			label.setText(message.getMessage());
			final Dimension newLabelSize = label.getPreferredSize();
			if (lastLabelSize.getHeight() < newLabelSize.getHeight() || lastLabelSize.getWidth() < newLabelSize.getWidth()) {
				dialog.pack();
			}
			dialog.layout();
			if (!dialog.isVisible()) {
				dialog.setVisible(true);
			}
		}

		private IImageConstant getIcon(final IBeanMessage message) {
			if (BeanMessageType.ERROR.equals(message.getType())) {
				return Icons.ERROR;
			}
			else if (BeanMessageType.WARNING.equals(message.getType())) {
				return Icons.WARNING;
			}
			else {
				throw new IllegalArgumentException("Only error and warning is suppported");
			}
		}

		private boolean isDisposed() {
			return dialog.isDisposed();
		}
	}
}
