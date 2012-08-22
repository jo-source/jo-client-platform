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

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

class BeanRelationGraphImpl<CHILD_BEAN_TYPE> extends ControlWrapper implements IBeanRelationGraph<CHILD_BEAN_TYPE> {

	private static final String NODES = "graph.nodes";
	private static final String EDGES = "graph.edges";

	private final IBeanRelationTreeModel<CHILD_BEAN_TYPE> relationTreeModel;

	private final Map<IBeanProxy<Object>, Node> nodeMap;
	private final HashMap<Integer, Node> nodeMapInt;

	private final Visualization vis;
	private final Graph graph;
	private final Display display;

	BeanRelationGraphImpl(
		final IControl control,
		final Container swingContainer,
		final IBeanRelationGraphSetupBuilder<CHILD_BEAN_TYPE, ?> setup) {
		super(control);

		this.relationTreeModel = setup.getModel();
		this.nodeMap = new HashMap<IBeanProxy<Object>, Node>();
		this.nodeMapInt = new HashMap<Integer, Node>();
		graph = new Graph();
		graph.addColumn("name", String.class);
		vis = new Visualization();
		vis.add("graph", graph);

		final ActionList color = new ActionList();
		color.add(new ColorAction(NODES, VisualItem.FILLCOLOR, ColorLib.rgb(200, 200, 255)));
		color.add(new ColorAction(NODES, VisualItem.STROKECOLOR, 0));
		color.add(new ColorAction(NODES, VisualItem.TEXTCOLOR, ColorLib.rgb(0, 0, 0)));
		color.add(new ColorAction(EDGES, VisualItem.FILLCOLOR, ColorLib.gray(200)));
		color.add(new ColorAction(EDGES, VisualItem.STROKECOLOR, ColorLib.gray(200)));

		final ForceDirectedLayout fdl = new ForceDirectedLayout("graph", true);

		final ActionList layout = new ActionList(Activity.INFINITY);
		layout.add(fdl);
		layout.add(new RepaintAction());
		//		layout.add(new NodeLinkTreeLayout("graph", Constants.ORIENT_LEFT_RIGHT, 50, 0, 8));
		//		layout.add(new RepaintAction(vis));

		vis.putAction("color", color);
		vis.putAction("layout", layout);

		final LabelRenderer r = new LabelRenderer("name");
		//		r.setRoundedCorner(8, 8);

		vis.setRendererFactory(new DefaultRendererFactory(r));

		display = new Display(vis);
		display.setHighQuality(true);

		display.addControlListener(new DragControl());
		display.addControlListener(new FocusControl(1));
		display.addControlListener(new NeighborHighlightControl());
		final JPanel panel = new JPanel(new BorderLayout());
		panel.add(display, BorderLayout.CENTER);

		relationTreeModel.getRoot().addBeanListModelListener(new RootModelListener());
		swingContainer.setLayout(new BorderLayout());
		swingContainer.add(panel, BorderLayout.SOUTH);

	}

	@Override
	public IBeanRelationTreeModel<CHILD_BEAN_TYPE> getModel() {
		return relationTreeModel;
	}

	private void onBeansChanged(final IBeanRelationNodeModel<Object, Object> relationNodeModel) {

		for (int i = 0; i < relationNodeModel.getSize(); i++) {
			final IBeanProxy<Object> bean = relationNodeModel.getBean(i);
			addBeanToGraph(bean, i + nodeMap.size(), relationNodeModel);
		}

	}

	private void addBeanToGraph(
		final IBeanProxy<Object> bean,
		final int index,
		final IBeanRelationNodeModel<Object, Object> beanRelationNodeModel) {

		final IBeanProxyLabelRenderer<Object> renderer = beanRelationNodeModel.getChildRenderer();
		if (nodeMapInt.get(index) != null) {
			graph.removeNode(nodeMap.get(index));
			nodeMapInt.remove(index);
			nodeMap.remove(bean);
		}

		final Node childNode = graph.addNode();
		final Node parentNode = nodeMap.get(beanRelationNodeModel.getParentBean());

		if (parentNode != null && (!bean.isDummy())) {
			graph.addEdge(parentNode, childNode);
		}

		nodeMap.put(bean, childNode);

		renderNode(childNode, bean, renderer);

		if (bean.isDummy()) {
			bean.addMessageStateListener(new DummyBeanMessageStateRenderingListener(childNode, renderer));
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
			graph.clear();
			synchronized (vis) {
				onBeansChanged(root);
			}
		}
	}

	private final class ChildModelListener implements IBeanListModelListener {

		private final IBeanRelationNodeModel<Object, Object> relationNodeModel;

		public ChildModelListener(final IBeanRelationNodeModel<Object, Object> relationNodeModel) {
			this.relationNodeModel = relationNodeModel;
		}

		@Override
		public void beansChanged() {
			synchronized (vis) {
				onBeansChanged(relationNodeModel);
			}
			vis.run("layout");
			vis.run("color");
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
		//		node.set("name", "...");
	}

	private static void renderErrorDummyNode(final Node node, final IBeanMessage message) {
		node.set("name", message.getMessage());
	}

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
