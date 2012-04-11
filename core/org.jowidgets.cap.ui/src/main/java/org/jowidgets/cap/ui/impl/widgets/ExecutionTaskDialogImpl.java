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

import org.jowidgets.api.color.Colors;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IButton;
import org.jowidgets.api.widgets.IFrame;
import org.jowidgets.api.widgets.IProgressBar;
import org.jowidgets.api.widgets.ITextLabel;
import org.jowidgets.api.widgets.blueprint.IButtonBluePrint;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.widgets.IExecutionTaskDialog;
import org.jowidgets.cap.ui.api.widgets.IExecutionTaskDialogBluePrint;
import org.jowidgets.cap.ui.tools.execution.ExecutionTaskAdapter;
import org.jowidgets.common.types.Markup;
import org.jowidgets.common.widgets.controller.IActionListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.controller.WindowAdapter;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.tools.widgets.wrapper.WindowWrapper;
import org.jowidgets.util.Assert;

class ExecutionTaskDialogImpl extends WindowWrapper implements IExecutionTaskDialog {

	private final IExecutionTask executionTask;
	private final ITextLabel description;
	private final IProgressBar progressBar;
	private final IButton cancelButton;

	ExecutionTaskDialogImpl(final IFrame frame, final IExecutionTaskDialogBluePrint bluePrint) {
		super(frame);

		Assert.paramNotNull(bluePrint.getExecutionTask(), "bluePrint.getExecutionTask()");
		this.executionTask = bluePrint.getExecutionTask();

		frame.setLayout(new MigLayoutDescriptor("[grow, 0::]", "[][]10[grow, 0::]"));
		this.description = frame.add(BPF.textLabel(" "), "growx, w 0::, wrap");

		this.progressBar = frame.add(BPF.progressBar(), "growx, w 0::, h 22!, wrap");
		progressBar.setIndeterminate(true);

		final IButtonBluePrint cancelButtonBp = BPF.button();
		cancelButtonBp.setSetup(bluePrint.getCancelButton());
		this.cancelButton = frame.add(cancelButtonBp, "alignx r, aligny b, w 60::");

		executionTask.addExecutionTaskListener(new ExecutionTaskListener());
		cancelButton.addActionListener(new CancelButtonListener());

		addWindowListener(new WindowCloseListener());
	}

	@Override
	public void executionFinished(String message) {
		if (message == null) {
			//TODO SP i18n
			message = "Finished";
		}
		description.setText(message);
		description.setMarkup(Markup.STRONG);
		progressBar.setMaximum(1);
		progressBar.setProgress(1);
		setButtonToOk(message);
	}

	@Override
	public void executionError(String message) {
		if (message == null) {
			//TODO SP i18n
			message = "Error";
		}
		description.setText(message);
		description.setMarkup(Markup.STRONG);
		description.setForegroundColor(Colors.ERROR);
		progressBar.setMaximum(100);
		progressBar.setProgress(0);
		setButtonToOk(message);
	}

	private void setButtonToOk(final String message) {
		//TODO SP i18n
		cancelButton.setText("Ok");
		cancelButton.setToolTipText(message);
	}

	@Override
	protected IFrame getWidget() {
		return (IFrame) super.getWidget();
	}

	private final class WindowCloseListener extends WindowAdapter {
		@Override
		public void windowClosed() {
			if (!executionTask.isCanceled()) {
				executionTask.cancel();
			}
		}
	}

	private final class CancelButtonListener implements IActionListener {
		@Override
		public void actionPerformed() {
			executionTask.cancel();
			dispose();
		}
	}

	private final class ExecutionTaskListener extends ExecutionTaskAdapter {

		private final IUiThreadAccess uiThreadAccess = Toolkit.getUiThreadAccess();

		@Override
		public void descriptionChanged(final String decription) {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					description.setText(decription);
				}
			});
		}

		@Override
		public void totalStepCountChanged(final int totalStepCount) {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					progressBar.setMaximum(totalStepCount);
				}
			});
		}

		@Override
		public void worked(final int totalWorked) {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					progressBar.setProgress(totalWorked);
				}
			});
		}
	}

}
