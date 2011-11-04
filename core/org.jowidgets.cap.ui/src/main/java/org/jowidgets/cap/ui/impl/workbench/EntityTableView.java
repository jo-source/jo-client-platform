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

package org.jowidgets.cap.ui.impl.workbench;

import java.util.Map;
import java.util.Map.Entry;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.tools.command.ActionBuilder;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.workbench.api.IViewContext;
import org.jowidgets.workbench.tools.AbstractView;

public class EntityTableView extends AbstractView {

	public EntityTableView(
		final IViewContext context,
		final IBeanTableModel<IBean> tableModel,
		final Map<String, IEntityLinkDescriptor> links) {
		final IContainer container = context.getContainer();
		container.setLayout(MigLayoutFactory.growingInnerCellLayout());
		final IBeanTable<IBean> table = container.add(
				CapUiToolkit.bluePrintFactory().beanTable(tableModel),
				MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
		final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService != null) {
			addLinkActions(entityService, table, links);
		}
		tableModel.load();
	}

	private static void addLinkActions(
		final IEntityService entityService,
		final IBeanTable<IBean> table,
		final Map<String, IEntityLinkDescriptor> links) {
		boolean actionCreated = false;
		final IMenuModel popMenu = table.getCellPopMenu();
		for (final Entry<String, IEntityLinkDescriptor> linkEntry : links.entrySet()) {
			final IAction linkAction = createLinkAction(entityService, linkEntry.getKey(), linkEntry.getValue());
			if (linkAction != null) {
				if (!actionCreated) {
					popMenu.addSeparator();
					actionCreated = true;
				}
				popMenu.addAction(linkAction);
			}
		}
		if (!actionCreated) {
			popMenu.addSeparator();
		}
	}

	private static IAction createLinkAction(
		final IEntityService entityService,
		final String entityId,
		final IEntityLinkDescriptor link) {

		if (link.getLinkedTypeId() != null && link.getLinkableTypeId() != null) {

			final IBeanServicesProvider linkTypeServices = entityService.getBeanServices(link.getLinkTypeId());
			final IBeanDtoDescriptor linkableTypeDescriptor = entityService.getDescriptor(link.getLinkableTypeId());
			final IBeanServicesProvider linkableTypeServices = entityService.getBeanServices(link.getLinkableTypeId());

			if (isBeanDescriptorOk(linkableTypeDescriptor)
				&& isLinkServicesOk(linkTypeServices)
				&& isLinkableServicesOk(linkableTypeServices)) {
				return createLinkAction(linkTypeServices, linkableTypeDescriptor, linkableTypeServices);
			}
		}
		return null;
	}

	private static boolean isBeanDescriptorOk(final IBeanDtoDescriptor descriptor) {
		return descriptor != null && !EmptyCheck.isEmpty(descriptor.getLabel());
	}

	private static boolean isLinkServicesOk(final IBeanServicesProvider services) {
		return services != null && services.creatorService() != null;
	}

	private static boolean isLinkableServicesOk(final IBeanServicesProvider services) {
		return services != null && services.readerService() != null;
	}

	private static IAction createLinkAction(
		final IBeanServicesProvider linkTypeServices,
		final IBeanDtoDescriptor linkableTypeDescriptor,
		final IBeanServicesProvider linkableTypeServices) {
		final IActionBuilder builder = new ActionBuilder();
		builder.setText(linkableTypeDescriptor.getLabel() + " verknüpfen ...");
		builder.setCommand(new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				Toolkit.getMessagePane().showInfo(executionContext, "Must be implemented!");
			}
		});
		return builder.build();
	}

}
