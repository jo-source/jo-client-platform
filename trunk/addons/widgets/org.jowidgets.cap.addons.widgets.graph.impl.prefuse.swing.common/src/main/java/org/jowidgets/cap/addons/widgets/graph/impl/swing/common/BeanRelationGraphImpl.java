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

import javax.swing.JPanel;

import org.jowidgets.api.widgets.IControl;
import org.jowidgets.cap.ui.api.addons.widgets.IBeanRelationGraph;
import org.jowidgets.cap.ui.api.addons.widgets.IBeanRelationGraphSetupBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModel;
import org.jowidgets.cap.ui.api.types.IEntityTypeId;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;

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
	private final Visualization vis;
	private final Graph graph;
	private final Display d;

	BeanRelationGraphImpl(
		final IControl control,
		final Container swingContainer,
		final IBeanRelationGraphSetupBuilder<CHILD_BEAN_TYPE, ?> setup) {
		super(control);
		this.relationTreeModel = setup.getModel();
		graph = new Graph();
		graph.addColumn("name", String.class);

		vis = new Visualization();
		vis.add("graph", graph);
		// use black for node text
		final ColorAction text = new ColorAction("graph.nodes", VisualItem.TEXTCOLOR, ColorLib.gray(0));
		// use light grey for edges
		final ColorAction edges = new ColorAction("graph.edges", VisualItem.STROKECOLOR, ColorLib.gray(200));
		final FontAction textFont = new FontAction("graph.nodes", new Font(Font.SANS_SERIF, 0, 15));
		// create an action list containing all color assignments
		final ActionList color = new ActionList();
		color.add(textFont);
		color.add(text);
		color.add(edges);

		// create an action list with an animated layout
		final ActionList layout = new ActionList(Activity.INFINITY);
		layout.add(new ForceDirectedLayout("graph", true, false));
		layout.add(new RepaintAction(vis));

		// add the actions to the visualization
		vis.putAction("color", color);
		vis.putAction("layout", layout);

		final LabelRenderer r = new LabelRenderer("name");
		r.setRoundedCorner(8, 8); // round the corners

		// create a new default renderer factory
		// return our name label renderer as the default for all non-EdgeItems
		// includes straight line edges for EdgeItems by default
		vis.setRendererFactory(new DefaultRendererFactory(r));

		d = new Display(vis);
		d.setSize(300, 300);
		final JPanel panel = new JPanel();
		panel.add(d);
		relationTreeModel.getRoot().addBeanListModelListener(new RootModelListener());
		swingContainer.setLayout(new BorderLayout());
		swingContainer.add(panel, BorderLayout.SOUTH);
		vis.run("color");
		// start up the animated layout
		vis.run("layout");
	}

	@Override
	public IBeanRelationTreeModel<CHILD_BEAN_TYPE> getModel() {
		return relationTreeModel;
	}

	private void onBeansChanged(final IBeanRelationNodeModel<Object, Object> relationNodeModel) {
		for (int i = 0; i < (relationNodeModel.getSize()); i++) {
			final IBeanProxy<Object> bean = relationNodeModel.getBean(i);

			addBeanToGraph(bean, relationNodeModel);
		}

	}

	private void addBeanToGraph(final IBeanProxy<Object> bean, final IBeanRelationNodeModel<Object, Object> beanRelationNodeModel) {

		final IBeanProxyLabelRenderer<Object> renderer = beanRelationNodeModel.getChildRenderer();
		Node childNode = null;
		if (!bean.isDummy()) {
			childNode = graph.addNode();
			childNode.set("name", renderer.getLabel(bean).getText());
		}

		for (final IEntityTypeId<Object> childEntityTypeId : beanRelationNodeModel.getChildRelations()) {
			final IBeanRelationNodeModel<Object, Object> childRelationNodeModel = relationTreeModel.getNode(
					beanRelationNodeModel.getChildEntityTypeId(),
					bean,
					childEntityTypeId);
			for (int j = 0; j < childRelationNodeModel.getSize(); j++) {
				final IBeanProxy<Object> bean2 = childRelationNodeModel.getBean(j);

				childRelationNodeModel.addBeanListModelListener(new ChildModelListener(childRelationNodeModel));
				final String text = childRelationNodeModel.getChildRenderer().getLabel(bean2).getText();
				final Node childChildNode = graph.addNode();

				childChildNode.set("name", text);
				graph.addEdge(childChildNode, childNode);

			}

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
		}
	}

}
