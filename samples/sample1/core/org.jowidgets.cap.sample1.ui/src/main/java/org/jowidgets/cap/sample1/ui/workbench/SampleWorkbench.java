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

package org.jowidgets.cap.sample1.ui.workbench;

import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.cap.sample1.ui.workbench.application.SampleApplication;
import org.jowidgets.cap.sample1.ui.workbench.command.WorkbenchActions;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.examples.common.icons.DemoIconsInitializer;
import org.jowidgets.workbench.api.IWorkbench;
import org.jowidgets.workbench.toolkit.api.IWorkbenchModel;
import org.jowidgets.workbench.toolkit.api.IWorkbenchModelBuilder;
import org.jowidgets.workbench.toolkit.api.WorkbenchToolkit;
import org.jowidgets.workbench.tools.WorkbenchModelBuilder;

public class SampleWorkbench {

	private final IWorkbench workbench;
	private final IWorkbenchModel model;

	public SampleWorkbench() {
		DemoIconsInitializer.initialize();

		final IWorkbenchModelBuilder builder = new WorkbenchModelBuilder();
		builder.setInitialDimension(new Dimension(1024, 768));
		builder.setLabel("cap api sample app");

		this.model = builder.build();
		this.model.addApplication(new SampleApplication().getModel());

		this.model.getToolBar().addAction(WorkbenchActions.LOAD_ACTION);
		this.model.getToolBar().addAction(WorkbenchActions.CANCEL_ACTION);
		this.model.getToolBar().addSeparator();
		this.model.getToolBar().addAction(WorkbenchActions.UNDO_ACTION);
		this.model.getToolBar().addAction(WorkbenchActions.SAVE_ACTION);

		final IMenuModel dataMenu = this.model.getMenuBar().addMenu("Data");
		dataMenu.addAction(WorkbenchActions.LOAD_ACTION);
		dataMenu.addAction(WorkbenchActions.CANCEL_ACTION);
		dataMenu.addSeparator();
		dataMenu.addAction(WorkbenchActions.UNDO_ACTION);
		dataMenu.addAction(WorkbenchActions.SAVE_ACTION);

		this.workbench = WorkbenchToolkit.getWorkbenchPartFactory().workbench(model);
	}

	public IWorkbench getWorkbench() {
		return workbench;
	}

}
