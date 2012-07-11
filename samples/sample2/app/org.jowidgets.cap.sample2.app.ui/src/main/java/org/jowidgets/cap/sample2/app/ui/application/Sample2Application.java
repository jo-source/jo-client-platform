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

package org.jowidgets.cap.sample2.app.ui.application;

import org.jowidgets.cap.sample2.app.common.entity.EntityIds;
import org.jowidgets.cap.sample2.app.common.security.AuthKeys;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.workbench.IEntityComponentNodesFactory;
import org.jowidgets.security.tools.SecurityContext;
import org.jowidgets.workbench.toolkit.api.IComponentNodeContainerModel;
import org.jowidgets.workbench.toolkit.api.IWorkbenchApplicationModel;
import org.jowidgets.workbench.toolkit.api.IWorkbenchApplicationModelBuilder;
import org.jowidgets.workbench.tools.WorkbenchApplicationModelBuilder;

public class Sample2Application {

	private final IWorkbenchApplicationModel model;

	public Sample2Application() {
		final IWorkbenchApplicationModelBuilder builder = new WorkbenchApplicationModelBuilder();
		builder.setId(Sample2Application.class.getName());
		builder.setLabel("Administration");
		this.model = builder.build();

		createComponentTree(model);
	}

	public IWorkbenchApplicationModel getModel() {
		return model;
	}

	private void createComponentTree(final IWorkbenchApplicationModel model) {
		addComponent(model, EntityIds.PERSON, AuthKeys.VIEW_PERSON_COMPONENT);
		addComponent(model, EntityIds.ROLE, AuthKeys.VIEW_ROLE_COMPONENT);
		addComponent(model, EntityIds.AUTHORIZATION, AuthKeys.VIEW_AUTHORIZATION_COMPONENT);
		addComponent(model, EntityIds.PERSON_LINK_TYPE, AuthKeys.VIEW_PERSON_LINK_TYPE_COMPONENT);
		addComponent(model, EntityIds.COUNTRY, AuthKeys.VIEW_COUNTRY_COMPONENT);
		addComponent(model, EntityIds.PHONE, AuthKeys.VIEW_PHONE_COMPONENT);
	}

	private void addComponent(final IComponentNodeContainerModel parent, final Object entityId, final String authorization) {
		final IEntityComponentNodesFactory nodesFactory = CapUiToolkit.workbenchToolkit().entityComponentNodesFactory();
		if (SecurityContext.hasAuthorization(authorization)) {
			parent.addChild(nodesFactory.createNode(entityId));
		}
	}

}
