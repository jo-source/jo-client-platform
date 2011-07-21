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

import org.jowidgets.cap.sample1.ui.workbench.component.dynbeans.DynamicPropertiesBeanComponent;
import org.jowidgets.cap.sample1.ui.workbench.component.user.UserComponent;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.examples.common.icons.SilkIcons;
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
		builder.setLabel("Sample App");
		this.model = builder.build();

		createComponentTree(model);
	}

	public IWorkbenchApplicationModel getModel() {
		return model;
	}

	private void createComponentTree(final IWorkbenchApplicationModel model) {
		final IComponentNodeModel usersFolder = model.addChild("USERS_FOLDER_ID", "Users", SilkIcons.FOLDER);

		final IComponentNodeModelBuilder nodeModelBuilder = new ComponentNodeModelBuilder();
		nodeModelBuilder.setComponentFactory(UserComponent.class);
		for (int i = 0; i < 10; i++) {
			nodeModelBuilder.setId(UserComponent.class.getName() + i);
			nodeModelBuilder.setLabel("User component " + i);
			usersFolder.addChild(nodeModelBuilder.build());
		}

		final IComponentNodeModel entitiesFolder = model.addChild("ENTITIES_FOLDER_ID", "Entities", SilkIcons.DATABASE);
		final List<IComponentNodeModel> entityNodes = CapUiToolkit.workbenchToolkit().entityComponentNodesFactory().createNodes();
		for (final IComponentNodeModel entityNode : entityNodes) {
			entitiesFolder.addChild(entityNode);
		}

		final IComponentNodeModel miscFolder = model.addChild("MISC_FOLDER_ID", "Misc", SilkIcons.FOLDER);
		nodeModelBuilder.setComponentFactory(DynamicPropertiesBeanComponent.class);
		nodeModelBuilder.setId(DynamicPropertiesBeanComponent.class.getName());
		nodeModelBuilder.setLabel("Dynamic properties beans");
		miscFolder.addChild(nodeModelBuilder.build());
	}

}
