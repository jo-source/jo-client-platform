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

package org.jowidgets.cap.sample1.ui.workbench.application;

import java.util.List;

import org.jowidgets.addons.icons.silkicons.SilkIcons;
import org.jowidgets.cap.sample1.ui.workbench.component.generic.GenericBeanComponent;
import org.jowidgets.cap.sample1.ui.workbench.component.roles.RolesTabFolderComponent;
import org.jowidgets.cap.sample1.ui.workbench.component.user.UserComponent;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.workbench.toolkit.api.IComponentNodeModel;
import org.jowidgets.workbench.toolkit.api.IComponentNodeModelBuilder;
import org.jowidgets.workbench.toolkit.api.IWorkbenchApplicationModel;
import org.jowidgets.workbench.toolkit.api.IWorkbenchApplicationModelBuilder;
import org.jowidgets.workbench.tools.ComponentNodeModelBuilder;
import org.jowidgets.workbench.tools.WorkbenchApplicationModelBuilder;

public class SampleApplication {

	private final IWorkbenchApplicationModel model;

	public SampleApplication() {
		final IWorkbenchApplicationModelBuilder builder = new WorkbenchApplicationModelBuilder();
		builder.setId(SampleApplication.class.getName());
		builder.setLabel(Messages.getString("SampleApplication.sample_app")); //$NON-NLS-1$
		this.model = builder.build();

		createComponentTree(model);
	}

	public IWorkbenchApplicationModel getModel() {
		return model;
	}

	private void createComponentTree(final IWorkbenchApplicationModel model) {
		final IComponentNodeModel usersFolder = model.addChild(
				"USERS_FOLDER_ID", Messages.getString("SampleApplication.users"), SilkIcons.FOLDER); //$NON-NLS-1$ //$NON-NLS-2$

		final IComponentNodeModelBuilder nodeModelBuilder = new ComponentNodeModelBuilder();
		nodeModelBuilder.setComponentFactory(UserComponent.class);
		for (int i = 0; i < 4; i++) {
			nodeModelBuilder.setId(UserComponent.class.getName() + i);
			nodeModelBuilder.setLabel(Messages.getString("SampleApplication.user_component") + i); //$NON-NLS-1$
			usersFolder.addChild(nodeModelBuilder.build());
		}

		final IComponentNodeModel miscFolder = model.addChild(
				"MISC_FOLDER_ID", Messages.getString("SampleApplication.misc"), SilkIcons.FOLDER); //$NON-NLS-1$ //$NON-NLS-2$
		nodeModelBuilder.setComponentFactory(GenericBeanComponent.class);
		nodeModelBuilder.setId(GenericBeanComponent.class.getName());
		nodeModelBuilder.setLabel(Messages.getString("SampleApplication.generic_bean")); //$NON-NLS-1$
		miscFolder.addChild(nodeModelBuilder.build());

		nodeModelBuilder.setComponentFactory(RolesTabFolderComponent.class);
		nodeModelBuilder.setId(RolesTabFolderComponent.class.getName());
		nodeModelBuilder.setLabel("Roles Folder"); //$NON-NLS-1$
		miscFolder.addChild(nodeModelBuilder.build());

		final IComponentNodeModel entitiesFolder = model.addChild("GENERIC_COMPONENTS_FOLDER_ID", //$NON-NLS-1$
				Messages.getString("SampleApplication.generic_components"), //$NON-NLS-1$
				SilkIcons.FOLDER);
		final List<IComponentNodeModel> entityNodes = CapUiToolkit.workbenchToolkit().entityComponentNodesFactory().createNodes();
		for (final IComponentNodeModel entityNode : entityNodes) {
			entitiesFolder.addChild(entityNode);
		}
	}

}
