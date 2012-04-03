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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.model.item.IActionItemModel;
import org.jowidgets.api.model.item.IToolBarItemModel;
import org.jowidgets.api.model.item.IToolBarModel;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityClass;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.command.IDataModelAction;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModelBuilder;
import org.jowidgets.common.types.IVetoable;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.workbench.api.IComponent;
import org.jowidgets.workbench.api.IComponentContext;
import org.jowidgets.workbench.api.IView;
import org.jowidgets.workbench.api.IViewContext;
import org.jowidgets.workbench.toolkit.api.IComponentNodeModel;
import org.jowidgets.workbench.tools.AbstractComponent;

public class EntityComponent extends AbstractComponent implements IComponent {

	public static final String ROOT_TABLE_VIEW_ID = EntityComponent.class.getName() + "_ROOT_TABLE_VIEW";
	public static final String LINKED_TABLE_VIEW_ID = EntityComponent.class.getName() + "_LINKED_TABLE_VIEW_";

	private final IEntityService entityService;
	private final IBeanTableModel<?> tableModel;
	private final IBeanRelationTreeModel<?> relationTreeModel;
	private final List<IDataModelAction> dataModelActions;
	private final Set<LinkedEntityTableView> tableViews;
	private final Map<String, IEntityLinkDescriptor> links;
	private final Map<Object, IBeanTableModel<?>> linkedModels;

	private EntityMultiDetailView multiDetailView;

	public EntityComponent(
		final IComponentNodeModel componentNodeModel,
		final IComponentContext componentContext,
		final IEntityClass entityClass) {

		this.entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService == null) {
			throw new IllegalStateException("No entity service found");
		}

		this.tableModel = CapUiToolkit.beanTableModelBuilder(entityClass.getId(), getBeanType(entityClass.getId())).build();
		this.relationTreeModel = createRelationTreeModel(tableModel, entityClass);
		this.dataModelActions = getDataModelActions(componentNodeModel);
		this.tableViews = new LinkedHashSet<LinkedEntityTableView>();
		this.links = new LinkedHashMap<String, IEntityLinkDescriptor>();
		this.linkedModels = new HashMap<Object, IBeanTableModel<?>>();

		final List<IEntityLinkDescriptor> entityLinks = entityService.getEntityLinks(entityClass.getId());
		if (entityLinks != null && !entityLinks.isEmpty()) {
			int i = 0;
			for (final IEntityLinkDescriptor link : entityLinks) {
				final String linkedViewId = LINKED_TABLE_VIEW_ID + i;
				links.put(linkedViewId, link);
				i++;

				final IBeanTableModelBuilder<?> builder = CapUiToolkit.beanTableModelBuilder(
						link.getLinkedTypeId(),
						getBeanType(link.getLinkedTypeId()));
				builder.setParent(tableModel, LinkType.SELECTION_ALL);
				linkedModels.put(link.getLinkedTypeId(), builder.build());
			}
			componentContext.setLayout(new EntityComponentMasterDetailLinksDetailLayout(entityClass, links).getLayout());
		}
		else {
			componentContext.setLayout(new EntityComponentMasterDetailLayout(entityClass).getLayout());
		}

	}

	private IBeanRelationTreeModel<?> createRelationTreeModel(final IBeanTableModel<?> parentModel, final IEntityClass entityClass) {
		final Object id = entityClass.getId();
		final Class<?> beanType = getBeanType(id);
		final IBeanRelationTreeModelBuilder<?> builder = CapUiToolkit.beanRelationTreeModelBuilder(id, beanType);
		builder.setParentSelectionAsReader(parentModel, LinkType.SELECTION_ALL);
		return builder.build();
	}

	@Override
	public IView createView(final String viewId, final IViewContext context) {
		if (ROOT_TABLE_VIEW_ID.equals(viewId)) {
			return new EntityTableView(context, tableModel, links.values(), linkedModels);
		}
		else if (EntityRelationTreeView.ID.equals(viewId)) {
			return new EntityRelationTreeView(context, relationTreeModel);
		}
		else if (EntityDetailView.ID.equals(viewId)) {
			return new EntityDetailView(context, tableModel);
		}
		else if (EntityMultiDetailView.ID.equals(viewId)) {
			if (multiDetailView != null) {
				throw new IllegalStateException("MultiDetailView could only be used once in layout");
			}
			multiDetailView = new EntityMultiDetailView(context);
			multiDetailView.getBeanSelectionForm().registerSelectionObservable(relationTreeModel);
			//TODO MG this will not work, until it is possible to determine the active view
			//			for (final LinkedEntityTableView tableView : tableViews) {
			//				multiDetailView.getBeanSelectionForm().registerSelectionObservable(tableView.getTable().getModel());
			//			}
			return multiDetailView;
		}
		else if (links.containsKey(viewId)) {
			final IEntityLinkDescriptor link = links.get(viewId);
			final LinkedEntityTableView result = new LinkedEntityTableView(
				context,
				linkedModels.get(link.getLinkedTypeId()),
				link);
			registerTableView(result);
			return result;
		}
		else {
			throw new IllegalArgumentException("View id '" + viewId + "' is not known.");
		}
	}

	@Override
	public void onActivation() {
		for (final IDataModelAction dataModelAction : dataModelActions) {
			dataModelAction.addDataModel(tableModel);
			for (final LinkedEntityTableView view : tableViews) {
				dataModelAction.addDataModel(view.getTable().getModel());
			}
		}
	}

	@Override
	public void onDeactivation(final IVetoable vetoable) {
		for (final IDataModelAction dataModelAction : dataModelActions) {
			dataModelAction.removeDataModel(tableModel);
			for (final LinkedEntityTableView view : tableViews) {
				dataModelAction.removeDataModel(view.getTable().getModel());
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

	private void registerTableView(final LinkedEntityTableView tableView) {
		if (multiDetailView != null) {
			multiDetailView.getBeanSelectionForm().registerSelectionObservable(tableView.getTable().getModel());
		}
		tableViews.add(tableView);
	}

	private Class<?> getBeanType(final Object entityId) {
		final IBeanDtoDescriptor beanDtoDescriptor = entityService.getDescriptor(entityId);
		if (beanDtoDescriptor != null) {
			return beanDtoDescriptor.getBeanType();
		}
		else {
			return IBeanDto.class;
		}
	}

}
