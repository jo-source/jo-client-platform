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

package org.jowidgets.cap.sample2.app.ui.workbench;

import java.util.Locale;

import org.jowidgets.addons.icons.silkicons.SilkIconsInitializer;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.content.IContentCreator;
import org.jowidgets.cap.sample2.app.ui.application.Sample2Application;
import org.jowidgets.cap.sample2.app.ui.command.WorkbenchActions;
import org.jowidgets.cap.sample2.app.ui.lookup.LookupInitializer;
import org.jowidgets.cap.ui.api.login.LoginService;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.common.types.IVetoable;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.security.tools.SecurityContext;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.workbench.api.ILoginCallback;
import org.jowidgets.workbench.api.IWorkbench;
import org.jowidgets.workbench.api.IWorkbenchContext;
import org.jowidgets.workbench.api.IWorkbenchFactory;
import org.jowidgets.workbench.toolkit.api.IWorkbenchInitializeCallback;
import org.jowidgets.workbench.toolkit.api.IWorkbenchModel;
import org.jowidgets.workbench.toolkit.api.IWorkbenchModelBuilder;
import org.jowidgets.workbench.toolkit.api.WorkbenchToolkit;
import org.jowidgets.workbench.tools.WorkbenchModelBuilder;

public class Sample2Workbench implements IWorkbenchFactory {

	private IWorkbenchModel model;

	@Override
	public IWorkbench create() {
		Locale.setDefault(Locale.US);

		SilkIconsInitializer.initializeFull();

		final IWorkbenchModelBuilder builder = new WorkbenchModelBuilder();
		builder.setInitialDimension(new Dimension(1024, 768));
		builder.setInitialSplitWeight(0.2);
		builder.setLabel("Sample2");
		builder.setLoginCallback(new ILoginCallback() {
			@Override
			public void onLogin(final IVetoable vetoable) {
				final boolean doLogin = LoginService.doLogin();
				if (!doLogin) {
					vetoable.veto();
				}
			}
		});
		builder.setInitializeCallback(new IWorkbenchInitializeCallback() {
			@Override
			public void onContextInitialize(final IWorkbenchContext context) {
				LookupInitializer.initializeLookupsAsync();
				model.addApplication(new Sample2Application().getModel());

				model.setStatusBarCreator(new IContentCreator() {
					@Override
					public void createContent(final IContainer container) {
						container.setLayout(new MigLayoutDescriptor("[grow, right]", "2[]2"));
						container.add(BPF.textLabel(SecurityContext.getUsername()).alignRight(), "alignx r");
					}
				});
			}
		});

		this.model = builder.build();

		model.getToolBar().addAction(WorkbenchActions.loadAction());
		model.getToolBar().addAction(WorkbenchActions.cancelAction());
		model.getToolBar().addSeparator();
		model.getToolBar().addAction(WorkbenchActions.undoAction());
		model.getToolBar().addAction(WorkbenchActions.saveAction());

		final IMenuModel dataMenu = model.getMenuBar().addMenu("Data");
		dataMenu.addAction(WorkbenchActions.loadAction());
		dataMenu.addAction(WorkbenchActions.cancelAction());
		dataMenu.addSeparator();
		dataMenu.addAction(WorkbenchActions.undoAction());
		dataMenu.addAction(WorkbenchActions.saveAction());

		return WorkbenchToolkit.getWorkbenchPartFactory().workbench(model);
	}

}
