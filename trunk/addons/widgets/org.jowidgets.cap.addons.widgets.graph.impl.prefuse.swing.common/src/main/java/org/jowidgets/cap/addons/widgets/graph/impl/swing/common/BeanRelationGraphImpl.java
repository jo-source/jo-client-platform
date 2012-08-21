/*
 * Copyright (c) 2012, grossmann
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

package org.jowidgets.cap.addons.widgets.graph.impl.swing.common;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.jowidgets.api.widgets.IControl;
import org.jowidgets.cap.ui.api.addons.widgets.IBeanRelationGraph;
import org.jowidgets.cap.ui.api.addons.widgets.IBeanRelationGraphSetupBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanMessageStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.model.ILabelModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModel;
import org.jowidgets.cap.ui.api.types.IEntityTypeId;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;
import org.jowidgets.util.Tuple;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

class BeanRelationGraphImpl<CHILD_BEAN_TYPE> extends ControlWrapper implements IBeanRelationGraph<CHILD_BEAN_TYPE> {

	private final IBeanRelationTreeModel<CHILD_BEAN_TYPE> relationTreeModel;
	private final Map<Node, Tuple<IBeanRelationNodeModel<Object, Object>, IBeanProxy<Object>>> nodesMap;

	private final Map<Integer, Node> nodeMap;

	private final Visualization vis;
	private final Graph graph;
	private final Display d;

	BeanRelationGraphImpl(
		final IControl control,
		final Container swingContainer,
		final IBeanRelationGraphSetupBuilder<CHILD_BEAN_TYPE, ?> setup) {
		super(control);
		this.relationTreeModel = setup.getModel();
		this.nodesMap = new HashMap<Node, Tuple<IBeanRelationNodeModel<Object, Object>, IBeanProxy<Object>>>();
		this.nodeMap = new HashMap<Integer, Node>();
		graph = new Graph();
		graph.addColumn("name", String.class);
		vis = new Visualization();
		vis.add("graph", graph);
		final ColorAction text = new ColorAction("graph.nodes", VisualItem.TEXTCOLOR, ColorLib.gray(0));
		final ColorAction edges = new ColorAction("graph.edges", VisualItem.STROKECOLOR, ColorLib.gray(200));
		final FontAction textFont = new FontAction("graph.nodes", new Font(Font.SANS_SERIF, 0, 15));
		final ActionList color = new ActionList();
		color.add(textFont);
		color.add(text);
		color.add(edges);

		final ActionList layout = new ActionList(Activity.INFINITY);
		layout.add(new ForceDirectedLayout("graph", true));
		layout.add(new RepaintAction(vis));

		vis.putAction("color", color);
		vis.putAction("layout", layout);

		final LabelRenderer r = new LabelRenderer("name");
		r.setRoundedCorner(8, 8);

		vis.setRendererFactory(new DefaultRendererFactory(r));

		d = new Display(vis);
		d.setSize(300, 300);
		d.setHighQuality(true);
		final JPanel panel = new JPanel(new BorderLayout());
		panel.add(d, BorderLayout.CENTER);
		relationTreeModel.getRoot().addBeanListModelListener(new RootModelListener());
		swingContainer.setLayout(new BorderLayout());
		swingContainer.add(panel, BorderLayout.SOUTH);
	}

	@Override
	public IBeanRelationTreeModel<CHILD_BEAN_TYPE> getModel() {
		return relationTreeModel;
	}

	private void onBeansChanged(final IBeanRelationNodeModel<Object, Object> relationNodeModel) {

		//		final int headMatching = getHeadMatchingLength(relationNodeModel);
		//		final int tailMatching = getTailMatchingLength(relationNodeModel);
		//		final int beansToDelete = oldSize - headMatching - tailMatching;
		for (int i = 0; i < relationNodeModel.getSize(); i++) {
			final IBeanProxy<Object> bean = relationNodeModel.getBean(i);
			addBeanToGraph(bean, i + nodeMap.size(), relationNodeModel);
		}

		//		for (int i = 0; i < beansToDelete; i++) {
		//			if (relationNodeModel.getSize() > 0) {
		//
		//				final IBeanProxy<Object> bean = relationNodeModel.getBean(i);
		//				final Node node = nodeMap.get(bean);
		//			}
		//		}
	}

	private void addBeanToGraph(
		final IBeanProxy<Object> bean,
		final int index,
		final IBeanRelationNodeModel<Object, Object> beanRelationNodeModel) {

		final IBeanProxyLabelRenderer<Object> renderer = beanRelationNodeModel.getChildRenderer();
		if (nodeMap.get(index) != null) {
			graph.removeNode(nodeMap.get(index));
			nodeMap.remove(index);

		}

		final Node childNode = graph.addNode();
		//		graph.addEdge(rootNode, childNode);
		Tuple<IBeanRelationNodeModel<Object, Object>, IBeanProxy<Object>> tuple;
		tuple = new Tuple<IBeanRelationNodeModel<Object, Object>, IBeanProxy<Object>>(beanRelationNodeModel, bean);
		nodesMap.put(childNode, tuple);
		nodeMap.put(index, childNode);

		renderNode(childNode, bean, renderer);

		if (bean.isDummy()) {
			bean.addMessageStateListener(new DummyBeanMessageStateRenderingListener(childNode, renderer));
		}
		else if (beanRelationNodeModel.getChildRelations().size() > 0) {
			//add dummy relation node
			final Node childDummyNode = graph.addNode();
			graph.addEdge(childNode, childDummyNode);
			//			childNode.addTreeNodeListener(new TreeNodeExpansionListener(childNode, relationNodeModel, bean));
			//			if (expansionCacheEnabled) {
			//				childNode.addTreeNodeListener(new TreeNodeExpansionTrackingListener(childNode));
			//			}
		}

		for (final IEntityTypeId<Object> childEntityTypeId : beanRelationNodeModel.getChildRelations()) {
			final IBeanRelationNodeModel<Object, Object> childRelationNodeModel = relationTreeModel.getNode(
					beanRelationNodeModel.getChildEntityTypeId(),
					bean,
					childEntityTypeId);

			childRelationNodeModel.addBeanListModelListener(new ChildModelListener(childRelationNodeModel));
		}

	}

	@SuppressWarnings("rawtypes")
	private final class RootModelListener implements IBeanListModelListener {

		private final IBeanRelationNodeModel root;

		private RootModelListener() {
			this.root = relationTreeModel.getRoot();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void beansChanged() {
			onBeansChanged(root);
			vis.run("color");
			vis.run("layout");
		}
	}

	private final class ChildModelListener implements IBeanListModelListener {

		private final IBeanRelationNodeModel<Object, Object> relationNodeModel;

		public ChildModelListener(final IBeanRelationNodeModel<Object, Object> relationNodeModel) {
			this.relationNodeModel = relationNodeModel;
		}

		@Override
		public void beansChanged() {
			onBeansChanged(relationNodeModel);
			vis.run("color");
			vis.run("layout");
		}
	}

	private void renderNode(final Node node, final IBeanProxy<Object> bean, final IBeanProxyLabelRenderer<Object> renderer) {
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

	private static void renderNodeWithLabel(final Node node, final ILabelModel label) {
		node.set("name", label.getText());
	}

	private void renderLoadingDummyNode(final Node node) {
		node.set("name", "...");
	}

	private static void renderErrorDummyNode(final Node node, final IBeanMessage message) {
		node.set("name", message.getMessage());
	}

	//	private int getHeadMatchingLength(final IBeanRelationNodeModel<Object, Object> relationNodeModel) {
	//
	//		final int maxMatching = getMaxMatchingLength(relationNodeModel);
	//		if (maxMatching == 0) {
	//			return 0;
	//		}
	//		final List<Node> children = new ArrayList<Node>();
	//		for (int i = 0; i < graph.getNodeCount(); i++) {
	//
	//			children.add(graph.getNode(i));
	//		}
	//
	//		final ListIterator<Node> treeNodeIterator = children.listIterator(children.size());
	//		for (int index = 0; index < relationNodeModel.getSize(); index++) {
	//			if (treeNodeIterator.hasNext()) {
	//				if (!isNodeAssociatedWithBean(treeNodeIterator.next(), index)) {
	//					return index;
	//				}
	//			}
	//			else {
	//				return index;
	//			}
	//		}
	//		return maxMatching;
	//	}
	//
	//	private int getTailMatchingLength(final IBeanRelationNodeModel<Object, Object> relationNodeModel) {
	//		final int maxMatching = getMaxMatchingLength(relationNodeModel);
	//		if (maxMatching == 0) {
	//			return 0;
	//		}
	//
	//		final List<Node> children = new ArrayList<Node>();
	//		for (int i = 0; i < graph.getNodeCount(); i++) {
	//			children.add(graph.getNode(i));
	//		}
	//
	//		final ListIterator<Node> treeNodeIterator = children.listIterator(children.size());
	//		final int relationNodeModelSize = relationNodeModel.getSize();
	//		for (int index = 0; index < relationNodeModelSize; index++) {
	//			if (treeNodeIterator.hasPrevious()) {
	//				final int beanIndex = relationNodeModelSize - index - 1;
	//				if (!isNodeAssociatedWithBean(treeNodeIterator.previous(), beanIndex)) {
	//					return index;
	//				}
	//			}
	//			else {
	//				return index;
	//			}
	//		}
	//		return maxMatching;
	//	}
	//
	//	private int getMaxMatchingLength(final IBeanRelationNodeModel<Object, Object> relationNodeModel) {
	//		return Math.min(relationNodeModel.getSize(), graph.getNodeCount());
	//	}
	//
	//	private boolean isNodeAssociatedWithBean(final Node node, final int beanIndex) {
	//		final Node tmpNode = nodeMap.get(beanIndex);
	//		if (tmpNode != null) {
	//			return true;
	//		}
	//		return false;
	//	}

	private final class DummyBeanMessageStateRenderingListener implements IBeanMessageStateListener<Object> {

		private final Node node;
		private final IBeanProxyLabelRenderer<Object> renderer;

		private DummyBeanMessageStateRenderingListener(final Node node, final IBeanProxyLabelRenderer<Object> renderer) {
			this.node = node;
			this.renderer = renderer;
		}

		@Override
		public void messageStateChanged(final IBeanProxy<Object> bean) {
			if (node.isValid()) {
				renderNode(node, bean, renderer);
			}
			else {
				bean.removeMessageStateListener(this);
			}
		}
	}

}
