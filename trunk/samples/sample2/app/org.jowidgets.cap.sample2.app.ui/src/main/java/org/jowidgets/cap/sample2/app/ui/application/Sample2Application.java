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

import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.sample2.app.common.entity.EntityIds;
import org.jowidgets.cap.security.common.api.ISecureObject;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.workbench.IEntityComponentNodesFactory;
import org.jowidgets.security.tools.SecurityContext;
import org.jowidgets.service.api.ServiceProvider;
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
		addEntityComponent(model, EntityIds.PERSON);
		addEntityComponent(model, EntityIds.ROLE);
		addEntityComponent(model, EntityIds.AUTHORIZATION);
		addEntityComponent(model, EntityIds.PERSON_LINK_TYPE);
		addEntityComponent(model, EntityIds.COUNTRY);
		addEntityComponent(model, EntityIds.PHONE);
	}

	private void addEntityComponent(final IComponentNodeContainerModel parent, final Object entityId) {
		final IEntityComponentNodesFactory nodesFactory = CapUiToolkit.workbenchToolkit().entityComponentNodesFactory();
		if (hasReaderServiceAuthorization(entityId)) {
			parent.addChild(nodesFactory.createNode(entityId));
		}
	}

	private boolean hasReaderServiceAuthorization(final Object entityId) {
		final Object authorization = getReaderServiceAuthorization(entityId);
		if (authorization != null) {
			return SecurityContext.hasAuthorization(authorization);
		}
		else {
			return true;
		}
	}

	private Object getReaderServiceAuthorization(final Object entityId) {
		final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService != null) {
			final IBeanServicesProvider beanServices = entityService.getBeanServices(entityId);
			if (beanServices != null) {
				final IReaderService<Void> readerService = beanServices.readerService();
				if (readerService instanceof ISecureObject<?>) {
					return ((ISecureObject<?>) readerService).getAuthorization();
				}
			}
		}
		return null;
	}

}
