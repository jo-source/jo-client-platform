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

import java.util.List;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.command.ILinkCreatorActionBuilder;
import org.jowidgets.cap.ui.api.command.ILinkDeleterActionBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModel;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeDetail;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeDetailBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IBeanTableLifecycleInterceptor;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSetupBuilder;
import org.jowidgets.cap.ui.tools.bean.SingleBeanSelectionProvider;
import org.jowidgets.cap.ui.tools.execution.BeanRefreshInterceptor;
import org.jowidgets.cap.ui.tools.execution.ExecutionInterceptorAdapter;
import org.jowidgets.cap.ui.tools.widgets.BeanTableLifecycleInterceptorAdapter;
import org.jowidgets.common.types.IVetoable;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.workbench.api.IViewContext;
import org.jowidgets.workbench.tools.AbstractView;

final class EntityRelationTreeDetailView extends AbstractView {

	public static final String ID = EntityRelationTreeDetailView.class.getName();
	public static final IMessage DEFAULT_LABEL = Messages.getMessage("EntityRelationTreeDetailView.details");

	private final IBeanTableModel<Object> rootTableModel;
	private final IEntityService entityService;

	EntityRelationTreeDetailView(
		final IViewContext context,
		final IBeanTableModel<Object> rootTableModel,
		final IBeanRelationTreeModel<?> treeModel) {

		this.rootTableModel = rootTableModel;

		this.entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService == null) {
			throw new IllegalStateException("No entity service found");
		}

		final IContainer container = context.getContainer();
		container.setLayout(MigLayoutFactory.growingInnerCellLayout());
		final IBeanRelationTreeDetailBluePrint<?> treeDetailBp = CapUiToolkit.bluePrintFactory().beanRelationTreeDetail(treeModel);

		final IBeanRelationTreeDetail<?> treeDetail = container.add(treeDetailBp, MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

		final IBeanTableLifecycleInterceptor<Object> interceptor = new BeanTableLifecycleInterceptorAdapterImpl();

		treeDetail.addBeanTableLifecycleInterceptor(interceptor);
	}

	private final class BeanTableLifecycleInterceptorAdapterImpl extends BeanTableLifecycleInterceptorAdapter<Object> {

		@Override
		public void onTableCreate(
			final IBeanRelationNodeModel<Object, Object> relationNode,
			final IBeanTableSetupBuilder<Object> builder) {
			builder.setDefaultCreatorAction(false);
		}

		@Override
		public void afterTableCreated(final IBeanRelationNodeModel<Object, Object> relationNode, final IBeanTable<Object> table) {
			final IEntityLinkDescriptor link = getLinkDescriptor(relationNode);
			if (link != null && link.getLinkCreatorService() != null) {
				final IAction linkCreatorAction = createLinkCreatorAction(relationNode, table, link);
				table.getCellPopMenu().addAction(linkCreatorAction);
				table.getTablePopupMenu().addAction(linkCreatorAction);
			}
			if (link != null && link.getLinkDeleterService() != null) {
				table.getCellPopMenu().addAction(createLinkDeleterAction(relationNode, table, link));
			}
		}

		private IAction createLinkCreatorAction(
			final IBeanRelationNodeModel<Object, Object> relationNode,
			final IBeanTable<Object> table,
			final IEntityLinkDescriptor link) {

			final SingleBeanSelectionProvider<Object> linkSource = new SingleBeanSelectionProvider<Object>(
				relationNode.getParentBean(),
				relationNode.getChildEntityId(),
				relationNode.getChildBeanType());

			final ILinkCreatorActionBuilder<Object, Object, Object> builder;
			builder = CapUiToolkit.actionFactory().linkCreatorActionBuilder(linkSource, link);
			builder.setLinkedModel(table.getModel());
			builder.addExecutionInterceptor(new AddBeanInterceptor(relationNode));
			builder.addExecutionInterceptor(new BeanRefreshInterceptor<Object, List<IBeanDto>>(rootTableModel));

			return builder.build();
		}

		private IAction createLinkDeleterAction(
			final IBeanRelationNodeModel<Object, Object> relationNode,
			final IBeanTable<Object> table,
			final IEntityLinkDescriptor link) {

			final ILinkDeleterActionBuilder<Object, Object> builder;
			final SingleBeanSelectionProvider<Object> linkSource = new SingleBeanSelectionProvider<Object>(
				relationNode.getParentBean(),
				relationNode.getChildEntityId(),
				relationNode.getChildBeanType());
			builder = CapUiToolkit.actionFactory().linkDeleterActionBuilder(linkSource, table.getModel(), link);
			builder.addExecutionInterceptor(new RemoveBeanInterceptor(relationNode, table.getModel()));
			builder.addExecutionInterceptor(new BeanRefreshInterceptor<Object, List<IBeanDto>>(rootTableModel));
			return builder.build();
		}

		private IEntityLinkDescriptor getLinkDescriptor(final IBeanRelationNodeModel<Object, Object> relationNode) {
			final List<IEntityLinkDescriptor> links = entityService.getEntityLinks(relationNode.getParentEntityId());
			if (links != null) {
				for (final IEntityLinkDescriptor link : links) {
					if (link.getLinkedEntityId().equals(relationNode.getChildEntityId())) {
						return link;
					}
				}
			}
			return null;
		}
	}

	private final class AddBeanInterceptor extends ExecutionInterceptorAdapter<List<IBeanDto>> {

		private final IBeanRelationNodeModel<Object, Object> relationNode;

		private AddBeanInterceptor(final IBeanRelationNodeModel<Object, Object> relationNode) {
			this.relationNode = relationNode;
		}

		@Override
		public void afterExecutionSuccess(final IExecutionContext executionContext, final List<IBeanDto> result) {
			for (final IBeanDto beanDto : result) {
				relationNode.addBeanDto(beanDto);
			}
		}
	}

	private final class RemoveBeanInterceptor extends ExecutionInterceptorAdapter<List<IBeanDto>> {

		private final IBeanRelationNodeModel<Object, Object> relationNode;
		private final IBeanTableModel<Object> linkedModel;

		private List<IBeanProxy<Object>> linkedSelection;

		private RemoveBeanInterceptor(
			final IBeanRelationNodeModel<Object, Object> relationNode,
			final IBeanTableModel<Object> linkedModel) {
			this.relationNode = relationNode;
			this.linkedModel = linkedModel;
		}

		@Override
		public void beforeExecution(final IExecutionContext executionContext, final IVetoable continueExecution) {
			this.linkedSelection = linkedModel.getSelectedBeans();
		}

		@Override
		public void afterExecutionSuccess(final IExecutionContext executionContext, final List<IBeanDto> result) {
			relationNode.removeBeans(linkedSelection);
		}
	}

}
