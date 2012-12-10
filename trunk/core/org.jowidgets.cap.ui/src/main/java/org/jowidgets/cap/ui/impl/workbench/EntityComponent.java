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

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.model.item.IActionItemModel;
import org.jowidgets.api.model.item.IToolBarItemModel;
import org.jowidgets.api.model.item.IToolBarModel;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityApplicationNode;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.command.IDataModelAction;
import org.jowidgets.cap.ui.api.command.IDeleterActionBuilder;
import org.jowidgets.cap.ui.api.command.ILinkCreatorActionBuilder;
import org.jowidgets.cap.ui.api.command.ILinkDeleterActionBuilder;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeMenuInterceptor;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModelBuilder;
import org.jowidgets.cap.ui.api.types.EntityTypeId;
import org.jowidgets.cap.ui.api.types.IEntityTypeId;
import org.jowidgets.cap.ui.api.workbench.IEntityComponent;
import org.jowidgets.cap.ui.tools.execution.BeanTableRefreshInterceptor;
import org.jowidgets.cap.ui.tools.execution.ExecutionInterceptorAdapter;
import org.jowidgets.cap.ui.tools.tree.BeanRelationTreeMenuInterceptorAdapter;
import org.jowidgets.common.types.IVetoable;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.workbench.api.IComponent;
import org.jowidgets.workbench.api.IComponentContext;
import org.jowidgets.workbench.api.IView;
import org.jowidgets.workbench.api.IViewContext;
import org.jowidgets.workbench.toolkit.api.IComponentNodeModel;
import org.jowidgets.workbench.tools.AbstractComponent;

class EntityComponent extends AbstractComponent implements IComponent, IEntityComponent {

	static final String ROOT_TABLE_VIEW_ID = EntityComponent.class.getName() + "_ROOT_TABLE_VIEW";

	private final IEntityService entityService;
	private final IBeanTableModel<Object> tableModel;
	private final IBeanRelationTreeModel<?> relationTreeModel;
	private final List<IDataModelAction> dataModelActions;
	private final List<IAction> linkCreatorActions;
	private final IEntityTypeId<Object> entityTypeId;
	private final IBeanRelationTreeMenuInterceptor treeMenuInterceptor;

	private EntityTableView entityTableView;
	private EntityRelationTreeDetailView relationTreeDetailView;
	private EntityRelationTreeView relationTreeView;

	EntityComponent(
		final IComponentNodeModel componentNodeModel,
		final IComponentContext componentContext,
		final IEntityApplicationNode applicationNode) {

		this.entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService == null) {
			throw new IllegalStateException("No entity service found");
		}

		final Class<Object> beanType = getBeanType(applicationNode.getEntityId());
		this.entityTypeId = EntityTypeId.create(applicationNode.getEntityId(), beanType);
		this.tableModel = CapUiToolkit.beanTableModelBuilder(applicationNode.getEntityId(), beanType).build();
		this.dataModelActions = getDataModelActions(componentNodeModel);
		this.linkCreatorActions = new LinkedList<IAction>();
		this.treeMenuInterceptor = new TreeMenuInterceptor();

		final List<IEntityLinkDescriptor> entityLinks = entityService.getEntityLinks(applicationNode.getEntityId());
		if (entityLinks != null && !entityLinks.isEmpty()) {
			this.relationTreeModel = createRelationTreeModel(tableModel, applicationNode);
			for (final IEntityLinkDescriptor link : entityLinks) {
				if (link.getLinkCreatorService() != null) {
					final IAction linkCreatorAction = createLinkCreatorAction(link);
					if (linkCreatorAction != null) {
						linkCreatorActions.add(linkCreatorAction);
					}
				}
			}
			componentContext.setLayout(new EntityComponentMasterRelationTreeDetailLayout(applicationNode).getLayout());
		}
		else {
			this.relationTreeModel = null;
			componentContext.setLayout(new EntityComponentMasterDetailLayout(applicationNode).getLayout());
		}
	}

	private IAction createLinkCreatorAction(final IEntityLinkDescriptor link) {
		final Object linkedEntityId = link.getLinkedEntityId();
		final Class<Object> linkedBeanType = getBeanType(linkedEntityId);
		final IEntityTypeId<Object> linkedEntityTypeId = EntityTypeId.create(linkedEntityId, linkedBeanType);
		if (link.getLinkCreatorService() != null) {
			final ILinkCreatorActionBuilder<Object, Object, Object> builder;
			builder = CapUiToolkit.actionFactory().linkCreatorActionBuilder(tableModel, link);
			builder.addExecutionInterceptor(new BeanTableRefreshInterceptor<Object, List<IBeanDto>>(tableModel));
			builder.addExecutionInterceptor(new BeanAddToTreeInterceptor(entityTypeId, linkedEntityTypeId));
			return builder.build();
		}
		return null;
	}

	private IBeanRelationTreeModel<?> createRelationTreeModel(
		final IBeanTableModel<?> parentModel,
		final IEntityApplicationNode entityClass) {
		final Object id = entityClass.getEntityId();
		final Class<?> beanType = getBeanType(id);
		final IBeanRelationTreeModelBuilder<?> builder = CapUiToolkit.beanRelationTreeModelBuilder(id, beanType);
		builder.setParentSelectionAsReader(parentModel, LinkType.SELECTION_ALL);
		builder.setBeanProxyContext(parentModel.getBeanProxyContext());
		return builder.build();
	}

	@Override
	public IView createView(final String viewId, final IViewContext context) {
		if (ROOT_TABLE_VIEW_ID.equals(viewId)) {
			if (entityTableView != null) {
				throw new IllegalStateException(ROOT_TABLE_VIEW_ID + " can only be used once in layout");
			}
			entityTableView = new EntityTableView(context, tableModel, linkCreatorActions);
			doPreInitialize();
			return entityTableView;
		}
		else if (EntityRelationTreeView.ID.equals(viewId)) {
			if (relationTreeView != null) {
				throw new IllegalStateException(EntityRelationTreeView.ID + " can only be used once in layout");
			}
			relationTreeView = new EntityRelationTreeView(context);
			doPreInitialize();
			return relationTreeView;
		}
		else if (EntityRelationGraphView.ID.equals(viewId)) {
			return new EntityRelationGraphView(context, relationTreeModel);
		}
		else if (EntityDetailView.ID.equals(viewId)) {
			return new EntityDetailView(context, tableModel);
		}
		else if (EntityRelationTreeDetailView.ID.equals(viewId)) {
			if (relationTreeDetailView != null) {
				throw new IllegalStateException(EntityRelationTreeDetailView.ID + " can only be used once in layout");
			}
			relationTreeDetailView = new EntityRelationTreeDetailView(context);
			doPreInitialize();
			return relationTreeDetailView;
		}
		else {
			throw new IllegalArgumentException("View id '" + viewId + "' is not known.");
		}
	}

	@Override
	public IBeanTableModel<Object> getRootModel() {
		return tableModel;
	}

	private void doPreInitialize() {
		if (relationTreeDetailView != null && relationTreeView != null && entityTableView != null) {
			relationTreeDetailView.initialize(entityTableView.getTable(), relationTreeView.getTree(), linkCreatorActions);
		}
		if (relationTreeView != null && entityTableView != null) {
			relationTreeView.initialize(entityTableView.getTable(), relationTreeModel, treeMenuInterceptor, linkCreatorActions);
		}
	}

	@Override
	public void onActivation() {
		for (final IDataModelAction dataModelAction : dataModelActions) {
			dataModelAction.addDataModel(tableModel);
			if (relationTreeModel != null) {
				dataModelAction.addDataModel(relationTreeModel);
			}
		}
	}

	@Override
	public void onDeactivation(final IVetoable vetoable) {
		for (final IDataModelAction dataModelAction : dataModelActions) {
			dataModelAction.removeDataModel(tableModel);
			if (relationTreeModel != null) {
				dataModelAction.removeDataModel(relationTreeModel);
			}
		}
	}

	private List<IDataModelAction> getDataModelActions(final IComponentNodeModel componentNodeModel) {
		final IToolBarModel toolBar = componentNodeModel.getApplication().getWorkbench().getToolBar();

		final List<IDataModelAction> result = new LinkedList<IDataModelAction>();
		for (final IToolBarItemModel item : toolBar.getItems()) {
			if (item instanceof IActionItemModel) {
				final IAction action = ((IActionItemModel) item).getAction();
				if (action instanceof IDataModelAction) {
					result.add((IDataModelAction) action);
				}
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private <BEAN_TYPE> Class<BEAN_TYPE> getBeanType(final Object entityId) {
		final IBeanDtoDescriptor beanDtoDescriptor = entityService.getDescriptor(entityId);
		if (beanDtoDescriptor != null) {
			return (Class<BEAN_TYPE>) beanDtoDescriptor.getBeanType();
		}
		else {
			return (Class<BEAN_TYPE>) IBeanDto.class;
		}
	}

	private final class BeanAddToTreeInterceptor extends ExecutionInterceptorAdapter<List<IBeanDto>> {

		private final IEntityTypeId<Object> entityTypeId;
		private final IEntityTypeId<Object> linkedEntityTypeId;

		private List<IBeanProxy<Object>> selection;

		private BeanAddToTreeInterceptor(final IEntityTypeId<Object> entityTypeId, final IEntityTypeId<Object> linkedEntityTypeId) {
			this.entityTypeId = entityTypeId;
			this.linkedEntityTypeId = linkedEntityTypeId;
		}

		@Override
		public void beforeExecution(final IExecutionContext executionContext, final IVetoable continueExecution) {
			this.selection = tableModel.getSelectedBeans();
		}

		@Override
		public void afterExecutionSuccess(final IExecutionContext executionContext, final List<IBeanDto> result) {
			for (final IBeanProxy<Object> bean : selection) {
				if (relationTreeModel.hasNode(bean, linkedEntityTypeId)) {
					final IBeanRelationNodeModel<Object, Object> relationNodeModel;
					relationNodeModel = relationTreeModel.getNode(entityTypeId, bean, linkedEntityTypeId);
					for (final IBeanDto beanDto : result) {
						relationNodeModel.addBeanDto(beanDto);
					}
				}
			}
		}
	}

	private final class TreeMenuInterceptor extends BeanRelationTreeMenuInterceptorAdapter {

		@Override
		public ILinkCreatorActionBuilder<Object, Object, Object> linkCreatorActionBuilder(
			final IBeanRelationNodeModel<Object, Object> relationNode,
			final ILinkCreatorActionBuilder<Object, Object, Object> builder) {
			builder.addExecutionInterceptor(new BeanTableRefreshInterceptor<Object, List<IBeanDto>>(tableModel));
			return builder;
		}

		@Override
		public ILinkDeleterActionBuilder<Object, Object> linkDeleterActionBuilder(
			final IBeanRelationNodeModel<Object, Object> relationNode,
			final ILinkDeleterActionBuilder<Object, Object> builder) {
			builder.addExecutionInterceptor(new BeanTableRefreshInterceptor<Object, Void>(tableModel));
			return builder;
		}

		@Override
		public IDeleterActionBuilder<Object> deleterActionBuilder(
			final IBeanRelationNodeModel<Object, Object> relationNode,
			final IDeleterActionBuilder<Object> builder) {
			builder.addExecutionInterceptor(new BeanTableRefreshInterceptor<Object, Void>(tableModel));
			return builder;
		}

	}

}
