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

package org.jowidgets.cap.ui.impl.widgets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.jowidgets.api.color.Colors;
import org.jowidgets.api.controller.IDisposeListener;
import org.jowidgets.api.controller.ITreeSelectionEvent;
import org.jowidgets.api.controller.ITreeSelectionListener;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.api.widgets.ITree;
import org.jowidgets.api.widgets.ITreeContainer;
import org.jowidgets.api.widgets.ITreeNode;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanMessageStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.command.ICapActionFactory;
import org.jowidgets.cap.ui.api.command.IDeleterActionBuilder;
import org.jowidgets.cap.ui.api.command.ILinkDeleterActionBuilder;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.model.ILabelModel;
import org.jowidgets.cap.ui.api.plugin.IBeanRelationTreePlugin;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModel;
import org.jowidgets.cap.ui.api.types.IEntityTypeId;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTree;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeBluePrint;
import org.jowidgets.cap.ui.tools.bean.SingleBeanSelectionProvider;
import org.jowidgets.common.types.Markup;
import org.jowidgets.common.types.SelectionPolicy;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProperties;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.tools.controller.TreeNodeAdapter;
import org.jowidgets.tools.model.item.MenuModel;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.IFilter;
import org.jowidgets.util.Tuple;

final class BeanRelationTreeImpl<CHILD_BEAN_TYPE> extends ControlWrapper implements IBeanRelationTree<CHILD_BEAN_TYPE> {

	private static final int MAX_EXPANDED_NODES_CACHE = 500;

	private final String waitText = Messages.getString("BeanRelationTreeImpl.wait_text");
	private final String waitTooltip = Messages.getString("BeanRelationTreeImpl.wait_tooltip");

	private final IBeanRelationTreeModel<CHILD_BEAN_TYPE> treeModel;
	private final IFilter<IBeanRelationNodeModel<Object, Object>> childRelationFilter;
	private final boolean autoSelection;
	private final boolean treeMultiSelection;
	private final int autoExpandLevel;
	private final Map<ITreeNode, Tuple<IBeanRelationNodeModel<Object, Object>, IBeanProxy<Object>>> nodesMap;
	private final LinkedHashSet<ExpandedNodeKey> expandedNodesCache;
	private final boolean expansionCacheEnabled;

	BeanRelationTreeImpl(final ITree tree, IBeanRelationTreeBluePrint<CHILD_BEAN_TYPE> bluePrint) {
		super(tree);

		bluePrint = modififySetupFromPlugins(bluePrint);

		this.treeMultiSelection = bluePrint.getSelectionPolicy() == SelectionPolicy.MULTI_SELECTION;
		this.treeModel = bluePrint.getModel();
		this.autoSelection = bluePrint.getAutoSelection();
		this.autoExpandLevel = bluePrint.getAutoExpandLevel();
		this.childRelationFilter = bluePrint.getChildRelationFilter();
		this.expansionCacheEnabled = bluePrint.getExpansionCacheEnabled();
		this.nodesMap = new HashMap<ITreeNode, Tuple<IBeanRelationNodeModel<Object, Object>, IBeanProxy<Object>>>();
		this.expandedNodesCache = new LinkedHashSet<ExpandedNodeKey>();

		tree.addTreeSelectionListener(new TreeSelectionListener());
		treeModel.getRoot().addBeanListModelListener(new RootModelListener());
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private IBeanRelationTreeBluePrint<CHILD_BEAN_TYPE> modififySetupFromPlugins(
		final IBeanRelationTreeBluePrint<CHILD_BEAN_TYPE> bluePrint) {

		final IBeanRelationTreeBluePrint<CHILD_BEAN_TYPE> result = CapUiToolkit.bluePrintFactory().beanRelationTree();
		result.setSetup(bluePrint);

		final IBeanRelationNodeModel<Void, CHILD_BEAN_TYPE> rootNode = bluePrint.getModel().getRoot();
		final IPluginPropertiesBuilder propertiesBuilder = PluginProperties.builder();
		propertiesBuilder.add(IBeanRelationTreePlugin.ENTITIY_ID_PROPERTY_KEY, rootNode.getChildEntityId());
		propertiesBuilder.add(IBeanRelationTreePlugin.BEAN_TYPE_PROPERTY_KEY, rootNode.getChildBeanType());
		final IPluginProperties pluginProperties = propertiesBuilder.build();
		final List<IBeanRelationTreePlugin<?>> plugins = PluginProvider.getPlugins(IBeanRelationTreePlugin.ID, pluginProperties);
		for (final IBeanRelationTreePlugin plugin : plugins) {
			plugin.modifySetup(pluginProperties, result);
		}

		return result;
	}

	@Override
	protected ITree getWidget() {
		return (ITree) super.getWidget();
	}

	@Override
	public void dispose() {
		nodesMap.clear();
		expandedNodesCache.clear();
		super.dispose();
	}

	@Override
	public IBeanRelationTreeModel<CHILD_BEAN_TYPE> getModel() {
		return treeModel;
	}

	private void onBeansChanged(final ITreeContainer treeContainer, final IBeanRelationNodeModel<Object, Object> relationNodeModel) {
		final int oldSize = treeContainer.getChildren().size();

		final int headMatching = getHeadMatchingLength(treeContainer, relationNodeModel);
		final int tailMatching = getTailMatchingLength(treeContainer, relationNodeModel);
		final int beansToDelete = oldSize - headMatching - tailMatching;

		//add the new beans
		for (int i = headMatching; i < (relationNodeModel.getSize() - tailMatching); i++) {
			//get the bean at index i
			final IBeanProxy<Object> bean = relationNodeModel.getBean(i);

			//add the bean to tree container
			addBeanToTreeContainer(bean, beansToDelete + i, treeContainer, relationNodeModel);
		}

		//remove the old beans
		for (int i = 0; i < beansToDelete; i++) {
			treeContainer.removeNode(headMatching);
		}

		//auto expand the node if necessary
		if (treeContainer.getLevel() < autoExpandLevel
			&& treeContainer instanceof ITreeNode
			&& treeContainer.getChildren().size() > 0) {
			final ITreeNode treeNode = (ITreeNode) treeContainer;
			treeNode.setExpanded(true);
		}
		if (treeContainer instanceof ITreeNode && expandedNodesCache.contains(new ExpandedNodeKey((ITreeNode) treeContainer))) {
			final ITreeNode treeNode = (ITreeNode) treeContainer;
			treeNode.setExpanded(true);
		}
	}

	private int getHeadMatchingLength(
		final ITreeContainer treeContainer,
		final IBeanRelationNodeModel<Object, Object> relationNodeModel) {

		final int maxMatching = getMaxMatchingLength(treeContainer, relationNodeModel);
		if (maxMatching == 0) {
			return 0;
		}
		final Iterator<ITreeNode> treeNodeIterator = treeContainer.getChildren().iterator();
		for (int index = 0; index < relationNodeModel.getSize(); index++) {
			if (treeNodeIterator.hasNext()) {
				if (!isNodeAssociatedWithBean(treeNodeIterator.next(), relationNodeModel.getBean(index))) {
					return index;
				}
			}
			else {
				return index;
			}
		}
		return maxMatching;
	}

	private int getTailMatchingLength(
		final ITreeContainer treeContainer,
		final IBeanRelationNodeModel<Object, Object> relationNodeModel) {
		final int maxMatching = getMaxMatchingLength(treeContainer, relationNodeModel);
		if (maxMatching == 0) {
			return 0;
		}
		final List<ITreeNode> children = treeContainer.getChildren();
		final ListIterator<ITreeNode> treeNodeIterator = children.listIterator(children.size());
		final int relationNodeModelSize = relationNodeModel.getSize();
		for (int index = 0; index < relationNodeModelSize; index++) {
			if (treeNodeIterator.hasPrevious()) {
				final int beanIndex = relationNodeModelSize - index - 1;
				if (!isNodeAssociatedWithBean(treeNodeIterator.previous(), relationNodeModel.getBean(beanIndex))) {
					return index;
				}
			}
			else {
				return index;
			}
		}
		return maxMatching;
	}

	private int getMaxMatchingLength(
		final ITreeContainer treeContainer,
		final IBeanRelationNodeModel<Object, Object> relationNodeModel) {
		return Math.min(relationNodeModel.getSize(), treeContainer.getChildren().size());
	}

	private boolean isNodeAssociatedWithBean(final ITreeNode node, final IBeanProxy<Object> bean) {
		final Tuple<IBeanRelationNodeModel<Object, Object>, IBeanProxy<Object>> tuple = nodesMap.get(node);
		if (tuple != null) {
			//Do not use equals here, it must be the same bean
			if (bean == tuple.getSecond()) {
				return true;
			}
		}
		return false;
	}

	private void addBeanToTreeContainer(
		final IBeanProxy<Object> bean,
		final int index,
		final ITreeContainer treeContainer,
		final IBeanRelationNodeModel<Object, Object> relationNodeModel) {

		//the renderer for the child nodes
		final IBeanProxyLabelRenderer<Object> renderer = relationNodeModel.getChildRenderer();

		//create a child node for the bean
		final ITreeNode childNode = treeContainer.addNode(index);
		renderNode(childNode, bean, renderer);

		//TODO MG remove this later BEGIN
		final IMenuModel menu = createNodeMenus(relationNodeModel);
		if (menu.getChildren().size() > 0) {
			childNode.setPopupMenu(menu);
		}
		//TODO MG remove this later END

		//map the child node to the relation model
		Tuple<IBeanRelationNodeModel<Object, Object>, IBeanProxy<Object>> tuple;
		tuple = new Tuple<IBeanRelationNodeModel<Object, Object>, IBeanProxy<Object>>(relationNodeModel, bean);
		nodesMap.put(childNode, tuple);

		//register listener that re-renders node on property changes
		bean.addPropertyChangeListener(new PropertyChangedRenderingListener(childNode, bean, renderer));

		//registers a listener that re-renders dummy nodes on message state change
		if (bean.isDummy()) {
			bean.addMessageStateListener(new DummyBeanMessageStateRenderingListener(childNode, renderer));
		}
		else if (relationNodeModel.getChildRelations().size() > 0) {
			//add dummy relation node
			childNode.addNode();
			childNode.addTreeNodeListener(new TreeNodeExpansionListener(childNode, relationNodeModel, bean));
			if (expansionCacheEnabled) {
				childNode.addTreeNodeListener(new TreeNodeExpansionTrackingListener(childNode));
			}
		}

		//register listener that removes node from nodes map on dispose
		childNode.addDisposeListener(new TreeNodeDisposeListener(childNode));

		//auto expand the child node if necessary
		if (!bean.isDummy() && childNode.getLevel() < autoExpandLevel && !childNode.isLeaf()) {
			childNode.setExpanded(true);
		}
		if (expandedNodesCache.contains(new ExpandedNodeKey(childNode))) {
			childNode.setExpanded(true);
		}
	}

	/**
	 * Filter a child relation
	 * 
	 * @param childRelationNode The relation to filter
	 * @return True, if the relation should be filter, false if the relation should be accepted
	 */
	private boolean filterChildReleation(final IBeanRelationNodeModel<Object, Object> childRelationNode) {
		if (childRelationNode.getReaderService() == null) {
			return true;
		}
		else if (childRelationFilter != null) {
			return !childRelationFilter.accept(childRelationNode);
		}
		else {
			return false;
		}
	}

	private IMenuModel createRelationNodeMenus(
		final IBeanRelationNodeModel<Object, Object> childRelationNodeModel,
		final IBeanProxy<Object> bean) {
		final IMenuModel result = new MenuModel();

		final ICapActionFactory actionFactory = CapUiToolkit.actionFactory();
		final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService != null) {
			final List<IEntityLinkDescriptor> links = entityService.getEntityLinks(childRelationNodeModel.getParentEntityId());
			for (final IEntityLinkDescriptor link : links) {
				if (childRelationNodeModel.getChildEntityId().equals(link.getLinkedEntityId())) {
					final IBeanSelectionProvider<?> source = new SingleBeanSelectionProvider<Object>(
						bean,
						childRelationNodeModel.getParentEntityId(),
						childRelationNodeModel.getParentBeanType());
					try {
						result.addAction(actionFactory.linkCreatorAction(source, childRelationNodeModel, link));
					}
					catch (final Exception e) {
					}
				}
			}
		}

		return result;
	}

	private IMenuModel createNodeMenus(final IBeanRelationNodeModel<Object, Object> relationNodeModel) {
		final IMenuModel result = new MenuModel();

		final ICapActionFactory actionFactory = CapUiToolkit.actionFactory();
		final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService != null) {
			final Object childEntityId = relationNodeModel.getChildEntityId();
			if (relationNodeModel.getParentEntityId() != null && relationNodeModel.getParentBean() != null) {
				final List<IEntityLinkDescriptor> links = entityService.getEntityLinks(relationNodeModel.getParentEntityId());
				for (final IEntityLinkDescriptor link : links) {
					if (childEntityId.equals(link.getLinkedEntityId()) && link.getLinkDeleterService() != null) {
						final IBeanSelectionProvider<?> source = new SingleBeanSelectionProvider<Object>(
							relationNodeModel.getParentBean(),
							relationNodeModel.getParentEntityId(),
							relationNodeModel.getParentBeanType());
						try {
							final ILinkDeleterActionBuilder<?, Object> builder = actionFactory.linkDeleterActionBuilder(
									source,
									relationNodeModel,
									link);
							builder.setLinkedMultiSelection(treeMultiSelection);
							result.addAction(builder.build());
						}
						catch (final Exception e) {
						}
					}
				}
			}
			final IBeanServicesProvider beanServices = entityService.getBeanServices(childEntityId);
			if (beanServices != null) {
				final IDeleterService deleterService = beanServices.deleterService();
				if (deleterService != null) {
					final IDeleterActionBuilder<Object> deleterBuilder = actionFactory.deleterActionBuilder(relationNodeModel);
					deleterBuilder.setDeleterService(deleterService);
					deleterBuilder.setMultiSelectionPolicy(false);
					deleterBuilder.setAccelerator(null);
					final IBeanDtoDescriptor descriptor = entityService.getDescriptor(childEntityId);
					if (descriptor != null) {
						deleterBuilder.setEntityLabelPlural(descriptor.getLabelPlural());
						deleterBuilder.setEntityLabelSingular(descriptor.getLabelSingular());
					}
					result.addAction(deleterBuilder.build());
				}
			}
		}

		return result;
	}

	private void renderNode(final ITreeNode node, final IBeanProxy<Object> bean, final IBeanProxyLabelRenderer<Object> renderer) {
		if (!bean.isDummy()) {
			renderNodeWithLabel(node, renderer.getLabel(bean));
		}
		else {
			if (bean.hasExecution()) {
				renderLoadingDummyNode(node);
			}
			else if (bean.hasMessages()) {
				renderErrorDummyNode(node, bean.getFirstWorstMessage());
			}
		}

	}

	private static void renderNodeWithLabel(final ITreeNode node, final ILabelModel label) {
		node.setText(label.getText());
		node.setToolTipText(label.getDescription());
		node.setIcon(label.getIcon());
		if (label.getMarkup() != null) {
			node.setMarkup(label.getMarkup());
		}
		if (label.getForegroundColor() != null) {
			node.setForegroundColor(label.getForegroundColor());
		}
	}

	private void renderLoadingDummyNode(final ITreeNode node) {
		node.setText(waitText);
		node.setToolTipText(waitTooltip);
	}

	private static void renderErrorDummyNode(final ITreeNode node, final IBeanMessage message) {
		if (BeanMessageType.ERROR == message.getType()) {
			node.setForegroundColor(Colors.ERROR);
		}
		else if (BeanMessageType.WARNING == message.getType()) {
			node.setForegroundColor(Colors.WARNING);
		}
		node.setText(message.getLabel());
		node.setToolTipText(null);
	}

	private static void renderRelationNode(final ITreeNode node, final IBeanRelationNodeModel<Object, Object> model) {
		node.setText(model.getText());
		node.setToolTipText(model.getDescription());
		node.setIcon(model.getIcon());
		node.setMarkup(Markup.EMPHASIZED);
	}

	private class TreeSelectionListener implements ITreeSelectionListener {

		@Override
		public void selectionChanged(final ITreeSelectionEvent event) {
			final List<IBeanProxy<Object>> newSelection = new LinkedList<IBeanProxy<Object>>();
			IBeanRelationNodeModel<Object, Object> relationNodeModel = null;
			for (final ITreeNode selected : event.getSelected()) {
				final Tuple<IBeanRelationNodeModel<Object, Object>, IBeanProxy<Object>> tuple = nodesMap.get(selected);
				if (tuple != null) {
					if (relationNodeModel == null) {
						relationNodeModel = tuple.getFirst();
						newSelection.add(tuple.getSecond());
					}
					else if (relationNodeModel == tuple.getFirst()) {
						newSelection.add(tuple.getSecond());
					}
					//else {
					//TODO MG unsupported selection, all elements must have the same childEntityTypeId
					//}
				}
			}
			if (relationNodeModel != null) {
				relationNodeModel.setSelectedBeans(newSelection);
			}
			else {
				final List<IBeanProxy<?>> emptyList = Collections.emptyList();
				treeModel.setSelection(emptyList);
			}
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private final class RootModelListener implements IBeanListModelListener {

		private final IBeanRelationNodeModel root;
		private final ITree tree;

		private RootModelListener() {
			this.root = treeModel.getRoot();
			this.tree = getWidget();
		}

		@Override
		public void beansChanged() {
			onBeansChanged(tree, root);
			if (autoSelection && tree.getChildren().size() > 0 && root.getSize() > 0) {
				final ITreeNode node = tree.getChildren().iterator().next();
				if (!root.getBean(0).isDummy()) {
					node.setSelected(true);
				}
			}
		}
	}

	private final class ChildModelListener implements IBeanListModelListener {

		private final ITreeContainer parentNode;
		private final IBeanRelationNodeModel<Object, Object> relationNodeModel;

		public ChildModelListener(final ITreeNode parentNode, final IBeanRelationNodeModel<Object, Object> relationNodeModel) {
			this.parentNode = parentNode;
			this.relationNodeModel = relationNodeModel;
		}

		@Override
		public void beansChanged() {
			onBeansChanged(parentNode, relationNodeModel);
		}
	}

	private final class PropertyChangedRenderingListener implements PropertyChangeListener {

		private final ITreeNode node;
		private final IBeanProxy<Object> bean;
		private final IBeanProxyLabelRenderer<Object> renderer;

		private PropertyChangedRenderingListener(
			final ITreeNode node,
			final IBeanProxy<Object> bean,
			final IBeanProxyLabelRenderer<Object> renderer) {

			this.node = node;
			this.bean = bean;
			this.renderer = renderer;
		}

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			if (!node.isDisposed()) {
				final Set<String> propertyDependencies = renderer.getPropertyDependencies();
				if (EmptyCheck.isEmpty(propertyDependencies) || propertyDependencies.contains(evt.getPropertyName())) {
					renderNode(node, bean, renderer);
				}
			}
			else {
				bean.removePropertyChangeListener(this);
			}
		}
	}

	private final class DummyBeanMessageStateRenderingListener implements IBeanMessageStateListener<Object> {

		private final ITreeNode node;
		private final IBeanProxyLabelRenderer<Object> renderer;

		private DummyBeanMessageStateRenderingListener(final ITreeNode node, final IBeanProxyLabelRenderer<Object> renderer) {
			this.node = node;
			this.renderer = renderer;
		}

		@Override
		public void messageStateChanged(final IBeanProxy<Object> bean) {
			if (!node.isDisposed()) {
				renderNode(node, bean, renderer);
			}
			else {
				bean.removeMessageStateListener(this);
			}
		}
	}

	private final class TreeNodeDisposeListener implements IDisposeListener {

		private final ITreeNode node;

		private TreeNodeDisposeListener(final ITreeNode node) {
			this.node = node;
		}

		@Override
		public void onDispose() {
			node.setPopupMenu(null);
		}
	}

	private final class TreeNodeExpansionListener extends TreeNodeAdapter {

		private final ITreeNode node;
		private final IBeanRelationNodeModel<Object, Object> relationNodeModel;
		private final IBeanProxy<Object> bean;

		private TreeNodeExpansionListener(
			final ITreeNode node,
			final IBeanRelationNodeModel<Object, Object> relationNodeModel,
			final IBeanProxy<Object> bean) {
			this.node = node;
			this.relationNodeModel = relationNodeModel;
			this.bean = bean;
		}

		@Override
		public void expandedChanged(final boolean expanded) {
			if (expanded) {

				for (final IEntityTypeId<Object> childEntityTypeId : relationNodeModel.getChildRelations()) {

					final IBeanRelationNodeModel<Object, Object> childRelationNodeModel = treeModel.getNode(
							relationNodeModel.getChildEntityTypeId(),
							bean,
							childEntityTypeId);

					if (!filterChildReleation(childRelationNodeModel)) {
						final ITreeNode childRelationNode = node.addNode();
						renderRelationNode(childRelationNode, childRelationNodeModel);

						//TODO MG remove this later BEGIN
						final IMenuModel relationMenu = createRelationNodeMenus(
								childRelationNodeModel,
								childRelationNodeModel.getParentBean());

						if (relationMenu.getChildren().size() > 0) {
							childRelationNode.setPopupMenu(relationMenu);
						}
						//TODO MG remove this later END

						if (expansionCacheEnabled) {
							childRelationNode.addTreeNodeListener(new TreeNodeExpansionTrackingListener(childRelationNode));
						}

						childRelationNodeModel.addBeanListModelListener(new ChildModelListener(
							childRelationNode,
							childRelationNodeModel));

						final boolean loadOccured = childRelationNodeModel.loadIfNotYetDone();
						if (!loadOccured) {
							onBeansChanged(childRelationNode, childRelationNodeModel);
						}
					}
				}

				node.removeTreeNodeListener(this);
				//remove the dummy node
				node.removeNode(0);
			}
		}
	}

	private final class TreeNodeExpansionTrackingListener extends TreeNodeAdapter {

		private final ExpandedNodeKey key;

		private TreeNodeExpansionTrackingListener(final ITreeNode node) {
			this.key = new ExpandedNodeKey(node);
		}

		@Override
		public void expandedChanged(final boolean expanded) {
			if (expanded) {
				//clear the first entry in the cache if the cache size exceeds
				if (expandedNodesCache.size() > MAX_EXPANDED_NODES_CACHE) {
					final ExpandedNodeKey keyToRemove = expandedNodesCache.iterator().next();
					expandedNodesCache.remove(keyToRemove);
				}
				//remove the key before adding it to ensure that the key gets to the end of the linked hash set
				//and so it will not erased from cache so early
				expandedNodesCache.remove(key);
				expandedNodesCache.add(key);
			}
			else {
				expandedNodesCache.remove(key);
			}
		}
	}

	private final class ExpandedNodeKey {

		private final List<Object> path;

		private ExpandedNodeKey(final ITreeNode node) {
			this.path = new LinkedList<Object>();
			addPathObjects(path, node);
		}

		private void addPathObjects(final List<Object> result, final ITreeNode node) {
			if (node != null) {
				final Tuple<IBeanRelationNodeModel<Object, Object>, IBeanProxy<Object>> tuple = nodesMap.get(node);
				if (tuple != null) {
					path.add(IBeanProxy.class.getName() + "ID:" + tuple.getSecond().getId());
				}
				else {
					path.add(node.getText());
				}
				addPathObjects(result, node.getParent());
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			@SuppressWarnings("unchecked")
			final ExpandedNodeKey other = (ExpandedNodeKey) obj;

			if (path == null) {
				if (other.path != null) {
					return false;
				}
			}
			else if (!path.equals(other.path)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "ExpandedNodeKey [path=" + path + "]";
		}

	}
}
