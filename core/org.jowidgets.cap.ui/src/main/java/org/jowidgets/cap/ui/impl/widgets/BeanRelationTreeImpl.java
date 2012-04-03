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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.api.controller.IDisposeListener;
import org.jowidgets.api.controller.ITreeSelectionEvent;
import org.jowidgets.api.controller.ITreeSelectionListener;
import org.jowidgets.api.widgets.ITree;
import org.jowidgets.api.widgets.ITreeContainer;
import org.jowidgets.api.widgets.ITreeNode;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.model.ILabelModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModel;
import org.jowidgets.cap.ui.api.tree.IEntityTypeId;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTree;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeBluePrint;
import org.jowidgets.common.types.Markup;
import org.jowidgets.tools.controller.TreeNodeAdapter;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;
import org.jowidgets.util.Tuple;

final class BeanRelationTreeImpl<CHILD_BEAN_TYPE> extends ControlWrapper implements IBeanRelationTree<CHILD_BEAN_TYPE> {

	private final IBeanRelationTreeModel<CHILD_BEAN_TYPE> treeModel;
	private final boolean autoSelection;
	private final int autoExpandLevel;
	private final Map<ITreeNode, Tuple<IBeanRelationNodeModel<Object, Object>, Integer>> nodesMap;

	@SuppressWarnings("unchecked")
	BeanRelationTreeImpl(final ITree tree, final IBeanRelationTreeBluePrint<CHILD_BEAN_TYPE> bluePrint) {
		super(tree);
		this.treeModel = bluePrint.getModel();
		this.autoSelection = bluePrint.getAutoSelection();
		this.autoExpandLevel = bluePrint.getAutoExpandLevel();
		this.nodesMap = new HashMap<ITreeNode, Tuple<IBeanRelationNodeModel<Object, Object>, Integer>>();

		tree.addTreeSelectionListener(new ITreeSelectionListener() {
			@Override
			public void selectionChanged(final ITreeSelectionEvent event) {
				final List<Integer> newSelection = new LinkedList<Integer>();
				IBeanRelationNodeModel<Object, Object> relationNodeModel = null;
				for (final ITreeNode selected : event.getSelected()) {
					final Tuple<IBeanRelationNodeModel<Object, Object>, Integer> tuple = nodesMap.get(selected);
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
					relationNodeModel.setSelection(newSelection);
				}
				else {
					final List<IBeanProxy<?>> emptyList = Collections.emptyList();
					treeModel.setSelection(emptyList);
				}

			}
		});

		@SuppressWarnings("rawtypes")
		final IBeanRelationNodeModel root = treeModel.getRoot();
		registerRelationModel(tree, root);
	}

	private void registerRelationModel(
		final ITreeContainer parentContainer,
		final IBeanRelationNodeModel<Object, Object> relationNodeModel) {
		relationNodeModel.addBeanListModelListener(new IBeanListModelListener() {
			@Override
			public void beansChanged() {
				onBeansChanged(parentContainer, relationNodeModel);
				final ITree tree = getWidget();
				if (autoSelection && parentContainer == tree && tree.getChildren().size() > 0) {
					tree.getChildren().iterator().next().setSelected(true);
				}
			}
		});
	}

	private void onBeansChanged(
		final ITreeContainer parentContainer,
		final IBeanRelationNodeModel<Object, Object> relationNodeModel) {
		parentContainer.removeAllNodes();
		for (int i = 0; i < relationNodeModel.getSize(); i++) {
			final IBeanProxy<Object> bean = relationNodeModel.getBean(i);
			final IBeanProxyLabelRenderer<Object> childRenderer = relationNodeModel.getChildRenderer();

			final ITreeNode childNode = parentContainer.addNode();
			renderNode(childNode, childRenderer.getLabel(bean));

			Tuple<IBeanRelationNodeModel<Object, Object>, Integer> tuple;
			tuple = new Tuple<IBeanRelationNodeModel<Object, Object>, Integer>(relationNodeModel, Integer.valueOf(i));
			nodesMap.put(childNode, tuple);
			childNode.addDisposeListener(new IDisposeListener() {
				@Override
				public void onDispose() {
					nodesMap.remove(childNode);
				}
			});

			final List<Tuple<IBeanRelationNodeModel<Object, Object>, ITreeNode>> unregisteredChildren;
			unregisteredChildren = new LinkedList<Tuple<IBeanRelationNodeModel<Object, Object>, ITreeNode>>();
			for (final IEntityTypeId<Object> childEntityTypeId : relationNodeModel.getChildRelations()) {

				final IBeanRelationNodeModel<Object, Object> childRelationNodeModel = treeModel.getNode(
						relationNodeModel.getChildEntityTypeId(),
						bean,
						childEntityTypeId);

				final ITreeNode childRelationNode = childNode.addNode();
				renderRelationNode(childRelationNode, childRelationNodeModel);

				unregisteredChildren.add(new Tuple<IBeanRelationNodeModel<Object, Object>, ITreeNode>(
					childRelationNodeModel,
					childRelationNode));
			}

			childNode.addTreeNodeListener(new TreeNodeAdapter() {
				@Override
				public void expandedChanged(final boolean expanded) {
					if (expanded) {
						if (unregisteredChildren.size() > 0) {
							for (final Tuple<IBeanRelationNodeModel<Object, Object>, ITreeNode> tuple : unregisteredChildren) {
								final IBeanRelationNodeModel<Object, Object> childRelationNodeModel = tuple.getFirst();
								final ITreeNode childRelationNode = tuple.getSecond();
								registerRelationModel(childRelationNode, childRelationNodeModel);
								final boolean loadOccured = childRelationNodeModel.loadIfNotYetDone();
								if (!loadOccured) {
									onBeansChanged(childRelationNode, childRelationNodeModel);
								}
							}
							unregisteredChildren.clear();
						}
					}
				}

			});

			if (!bean.isDummy() && parentContainer.getLevel() < autoExpandLevel && parentContainer instanceof ITreeNode) {
				final ITreeNode treeNode = (ITreeNode) parentContainer;
				treeNode.setExpanded(true);
			}

			if (!bean.isDummy() && childNode.getLevel() < autoExpandLevel && !childNode.isLeaf()) {
				childNode.setExpanded(true);
			}

		}

	}

	private void renderNode(final ITreeNode node, final ILabelModel label) {
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

	private void renderRelationNode(final ITreeNode node, final IBeanRelationNodeModel<Object, Object> model) {
		node.setText(model.getText());
		node.setToolTipText(model.getDescription());
		node.setIcon(model.getIcon());
		node.setMarkup(Markup.EMPHASIZED);

	}

	@Override
	protected ITree getWidget() {
		return (ITree) super.getWidget();
	}

	@Override
	public IBeanRelationTreeModel<CHILD_BEAN_TYPE> getModel() {
		return treeModel;
	}

}
