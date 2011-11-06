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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.command.ILinkActionBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.tools.execution.ExecutionInterceptorAdapter;
import org.jowidgets.common.types.IVetoable;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.workbench.api.IViewContext;
import org.jowidgets.workbench.tools.AbstractView;

public class EntityTableView extends AbstractView {

	public EntityTableView(
		final IViewContext context,
		final IBeanTableModel<Object> tableModel,
		final Collection<IEntityLinkDescriptor> links,
		final Map<Object, IBeanTableModel<Object>> linkedModels) {

		final IContainer container = context.getContainer();
		container.setLayout(MigLayoutFactory.growingInnerCellLayout());
		final IBeanTable<Object> table = container.add(
				CapUiToolkit.bluePrintFactory().beanTable(tableModel),
				MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
		final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService != null) {
			addLinkActions(entityService, table, links, linkedModels);
		}
		tableModel.load();
	}

	private static void addLinkActions(
		final IEntityService entityService,
		final IBeanTable<Object> table,
		final Collection<IEntityLinkDescriptor> links,
		final Map<Object, IBeanTableModel<Object>> linkedModels) {
		boolean actionCreated = false;
		final IMenuModel popMenu = table.getCellPopMenu();
		for (final IEntityLinkDescriptor link : links) {
			final ILinkActionBuilder<Object> linkActionBuilder = CapUiToolkit.actionFactory().linkActionBuilder(
					table.getModel(),
					link);
			if (linkActionBuilder != null) {
				linkActionBuilder.addExecutionInterceptor(new LinkActionExecutionInterceptor(table.getModel(), link, linkedModels));
				if (!actionCreated) {
					popMenu.addSeparator();
					actionCreated = true;
				}
				popMenu.addAction(linkActionBuilder.build());
			}
		}
		if (!actionCreated) {
			popMenu.addSeparator();
		}
	}

	private static final class LinkActionExecutionInterceptor extends ExecutionInterceptorAdapter {

		private final IBeanTableModel<Object> tableModel;
		private final IEntityLinkDescriptor link;
		private final Map<Object, IBeanTableModel<Object>> linkedModels;

		private List<IBeanProxy<Object>> lastSelection;

		private LinkActionExecutionInterceptor(
			final IBeanTableModel<Object> tableModel,
			final IEntityLinkDescriptor link,
			final Map<Object, IBeanTableModel<Object>> linkedModels) {
			super();
			this.tableModel = tableModel;
			this.link = link;
			this.linkedModels = linkedModels;
		}

		@Override
		public void beforeExecution(final IExecutionContext executionContext, final IVetoable continueExecution) {
			lastSelection = tableModel.getSelectedBeans();
		}

		@Override
		public void onExecutionVeto(final IExecutionContext executionContext) {
			lastSelection = null;
		}

		@Override
		public void afterExecutionSuccess(final IExecutionContext executionContext) {
			final IBeanTableModel<Object> linkedModel = linkedModels.get(link.getLinkedTypeId());
			if (linkedModel != null) {
				linkedModel.load();
			}
			if (!EmptyCheck.isEmpty(lastSelection)) {
				tableModel.refreshBeans(lastSelection);
			}
		}
	}
}
