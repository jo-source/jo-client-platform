/*
 * Copyright (c) 2012, David Bauknecht
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
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.command.IActionBuilderFactory;
import org.jowidgets.api.command.ICommandAction;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.model.item.ICheckedItemModel;
import org.jowidgets.api.model.item.IToolBarModel;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IControl;
import org.jowidgets.api.widgets.IFrame;
import org.jowidgets.api.widgets.IToolBar;
import org.jowidgets.api.widgets.blueprint.IComboBoxBluePrint;
import org.jowidgets.api.widgets.blueprint.IDialogBluePrint;
import org.jowidgets.api.widgets.blueprint.IInputFieldBluePrint;
import org.jowidgets.cap.ui.api.addons.widgets.IBeanRelationGraph;
import org.jowidgets.cap.ui.api.addons.widgets.IBeanRelationGraphSetupBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.model.ILabelModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModel;
import org.jowidgets.cap.ui.api.types.IEntityTypeId;
import org.jowidgets.cap.ui.tools.model.BeanListModelListenerAdapter;
import org.jowidgets.common.types.Position;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.common.widgets.controller.IItemStateListener;
import org.jowidgets.spi.impl.swing.addons.JoToSwing;
import org.jowidgets.spi.impl.swing.addons.SwingToJo;
import org.jowidgets.spi.impl.swing.common.image.SwingImageRegistry;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.model.item.InputControlItemModel;
import org.jowidgets.tools.model.item.ToolBarModel;
import org.jowidgets.tools.powo.JoComposite;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.GroupAction;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.animate.PolarLocationAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.animate.VisibilityAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.layout.CollapsedSubtreeLayout;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.activity.Activity;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.ToolTipControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.ImageFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;
import prefuse.util.ui.JForcePanel;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.sort.TreeDepthItemSorter;

class BeanRelationGraphImpl<CHILD_BEAN_TYPE> extends ControlWrapper implements IBeanRelationGraph<CHILD_BEAN_TYPE> {

	private static final String NODES = "graph.nodes";
	private static final String EDGES = "graph.edges";
	private static final String EDGE_DECORATORS = "edgeDeco";

	private static final Schema DECORATOR_SCHEMA = PrefuseLib.getVisualItemSchema();
	static {
		DECORATOR_SCHEMA.setDefault(VisualItem.INTERACTIVE, false);
		DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(128));
		DECORATOR_SCHEMA.setDefault(VisualItem.FONT, FontLib.getFont("Tahoma", 9));
	}

	private final IBeanRelationTreeModel<CHILD_BEAN_TYPE> relationTreeModel;

	private final Map<IBeanProxy<Object>, Node> nodeMap;
	private final Map<Integer, Node> nodeMapInt;
	private final Map<Class<Object>, String> entityGroupMap;

	private int maxNodeCount = 500;

	private final Visualization vis;
	private final Graph graph;
	private final Display display;
	private ForceSimulator forceSimulator;
	private final JoComposite jotoolBarPanel;

	private IFrame dialog;
	private Position dialogPosition;

	private final ImageFactory imageFactory;
	private final LabelEdgeLayout labelEdgeLayout;
	private int groupCount;
	private ICheckedItemModel nodeCountItem;

	BeanRelationGraphImpl(
		final IControl control,
		final Container swingContainer,
		final IBeanRelationGraphSetupBuilder<CHILD_BEAN_TYPE, ?> setup) {
		super(control);

		this.relationTreeModel = setup.getModel();
		this.nodeMap = new HashMap<IBeanProxy<Object>, Node>();
		this.nodeMapInt = new HashMap<Integer, Node>();
		this.entityGroupMap = new HashMap<Class<Object>, String>();

		graph = new Graph();
		graph.addColumn("name", String.class);
		graph.addColumn("image", Image.class);
		graph.addColumn("type", String.class);

		vis = new Visualization();
		vis.addGraph("graph", graph);
		vis.setInteractive("graph.edges", null, false);
		vis.setInteractive("graph.nodes", null, true);

		final TupleSet focusGroup = vis.getGroup(Visualization.FOCUS_ITEMS);
		focusGroup.addTupleSetListener(new TupleSetListener() {
			@Override
			public void tupleSetChanged(final TupleSet ts, final Tuple[] add, final Tuple[] rem) {
				for (int i = 0; i < rem.length; ++i) {
					((VisualItem) rem[i]).setFixed(false);
				}
				for (int i = 0; i < add.length; ++i) {
					((VisualItem) add[i]).setFixed(false);
					((VisualItem) add[i]).setFixed(true);
				}
				vis.run("color");
			}
		});

		final ActionList color = new ActionList();
		color.add(new ColorAction(NODES, VisualItem.FILLCOLOR, ColorLib.rgb(255, 255, 255)));
		color.add(new ColorAction(NODES, VisualItem.STROKECOLOR, 0));
		color.add(new ColorAction(NODES, VisualItem.TEXTCOLOR, ColorLib.rgb(0, 0, 0)));
		color.add(new ColorAction(EDGES, VisualItem.FILLCOLOR, ColorLib.gray(200)));
		color.add(new ColorAction(EDGES, VisualItem.STROKECOLOR, ColorLib.gray(200)));
		color.add(new FontAction(NODES, new Font("Tahoma", Font.BOLD, 10)));

		final ColorAction fill2 = new ColorAction(NODES, VisualItem.FILLCOLOR, ColorLib.gray(200));
		fill2.add(ExpressionParser.predicate("type == 'graph.nodes.group1'"), ColorLib.rgba(135, 206, 250, 255));
		fill2.add(ExpressionParser.predicate("type == 'graph.nodes.group2'"), ColorLib.rgba(95, 158, 160, 255));
		fill2.add(ExpressionParser.predicate("type == 'graph.nodes.group3'"), ColorLib.rgba(0, 255, 127, 255));
		fill2.add(ExpressionParser.predicate("type == 'graph.nodes.group4'"), ColorLib.rgba(240, 230, 140, 255));
		fill2.add(ExpressionParser.predicate("type == 'graph.nodes.group5'"), ColorLib.rgba(220, 220, 220, 255));
		fill2.add(ExpressionParser.predicate("type == 'graph.nodes.group6'"), ColorLib.rgba(222, 127, 0, 255));
		fill2.add(ExpressionParser.predicate("type == 'graph.nodes.group7'"), ColorLib.rgba(222, 127, 227, 255));
		fill2.add(ExpressionParser.predicate("type == 'graph.nodes.group8'"), ColorLib.rgba(218, 191, 113, 255));
		fill2.add(ExpressionParser.predicate("type == 'graph.nodes.group9'"), ColorLib.rgba(99, 241, 113, 255));
		fill2.add(ExpressionParser.predicate("type == 'graph.nodes.group10'"), ColorLib.rgba(45, 84, 187, 255));
		fill2.add("_highlight", ColorLib.rgb(255, 200, 125));

		color.add(fill2);

		vis.putAction("color", color);

		final LabelRenderer r = new LabelRenderer("name", "image");
		r.setRoundedCorner(8, 8);
		r.setHorizontalAlignment(Constants.CENTER);
		r.setVerticalAlignment(Constants.CENTER);

		imageFactory = new ImageFactory();
		r.setImageFactory(imageFactory);
		final DefaultRendererFactory drf = new DefaultRendererFactory(r);
		drf.add(new InGroupPredicate(EDGE_DECORATORS), new LabelRenderer("name"));
		vis.setRendererFactory(drf);
		drf.add(new InGroupPredicate("graph.nodes.group1"), new LabelRenderer("type"));

		DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(0));
		vis.addDecorators(EDGE_DECORATORS, EDGES, DECORATOR_SCHEMA);

		labelEdgeLayout = new LabelEdgeLayout(EDGE_DECORATORS);
		initForceDLayout();

		display = new Display(vis);
		display.setHighQuality(true);
		display.addControlListener(new FocusControl(1));
		display.addControlListener(new DragControl());
		display.addControlListener(new PanControl());
		display.addControlListener(new ZoomControl());
		display.addControlListener(new WheelZoomControl());
		display.addControlListener(new ZoomToFitControl());
		display.addControlListener(new NeighborHighlightControl());

		final ToolTipControl ttc = new ToolTipControl("name");
		display.addControlListener(ttc);

		final JPanel toolBarPanel = new JPanel();
		jotoolBarPanel = SwingToJo.create(toolBarPanel);

		final IToolBar toolbar = jotoolBarPanel.add(BPF.toolBar());
		toolbar.setModel(initToolBar());
		swingContainer.setLayout(new BorderLayout());
		swingContainer.add(display, BorderLayout.CENTER);
		swingContainer.add(toolBarPanel, BorderLayout.NORTH);

		swingContainer.addHierarchyListener(new HierarchyListener() {

			@Override
			public void hierarchyChanged(final HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					if (!swingContainer.isShowing()) {
						if (dialog != null) {
							dialogPosition = dialog.getPosition();
							dialog.setVisible(false);
							dialog.dispose();
						}
					}
				}
			}
		});

		relationTreeModel.getRoot().addBeanListModelListener(new RootModelListener());
	}

	@Override
	public IBeanRelationTreeModel<CHILD_BEAN_TYPE> getModel() {
		return relationTreeModel;
	}

	private void onBeansChanged(final IBeanRelationNodeModel<Object, Object> relationNodeModel) {

		for (int i = 0; i < relationNodeModel.getSize(); i++) {
			final IBeanProxy<Object> bean = relationNodeModel.getBean(i);
			if (nodeMapInt.get(i + nodeMap.size()) != null) {
				graph.removeNode(nodeMap.get(i + nodeMap.size()));
				nodeMapInt.remove(i + nodeMap.size());
				nodeMap.remove(bean);
			}
			if (graph.getNodeCount() < maxNodeCount && !(bean.isDummy())) {
				addBeanToGraph(bean, i + nodeMap.size(), relationNodeModel);
			}
		}
	}

	private void addBeanToGraph(
		final IBeanProxy<Object> bean,
		final int index,
		final IBeanRelationNodeModel<Object, Object> beanRelationNodeModel) {

		final IBeanProxyLabelRenderer<Object> renderer = beanRelationNodeModel.getChildRenderer();

		Node childNode = nodeMap.get(bean);

		if (nodeMap.get(bean) == null) {
			childNode = graph.addNode();
			nodeMap.put(bean, childNode);
		}

		if (entityGroupMap.get(beanRelationNodeModel.getChildBeanType()) == null) {
			entityGroupMap.put(beanRelationNodeModel.getChildBeanType(), "graph.nodes.group" + groupCount);
			if (groupCount <= 10) {
				groupCount++;
			}
		}

		final String nodeGroup = entityGroupMap.get(beanRelationNodeModel.getChildBeanType());

		final Node parentNode = nodeMap.get(beanRelationNodeModel.getParentBean());
		if (parentNode != null && (!bean.isDummy())) {
			if (graph.getEdge(parentNode, childNode) == null && graph.getEdge(childNode, parentNode) == null) {

				final Edge edge = graph.addEdge(parentNode, childNode);
				edge.set("name", beanRelationNodeModel.getText());
			}
			else {
				if (graph.getEdge(childNode, parentNode) != null) {
					final String previousString = (String) graph.getEdge(childNode, parentNode).get("name");
					graph.getEdge(childNode, parentNode).set("name", previousString + " / " + beanRelationNodeModel.getText());
				}
				else if (graph.getEdge(parentNode, childNode) != null) {
					final String previousString = (String) graph.getEdge(parentNode, childNode).get("name");
					graph.getEdge(parentNode, childNode).set("name", previousString + " / " + beanRelationNodeModel.getText());
				}

			}
		}

		renderNodeShape(nodeGroup, childNode);
		renderNode(childNode, bean, renderer);

		for (final IEntityTypeId<Object> childEntityTypeId : beanRelationNodeModel.getChildRelations()) {
			final IBeanRelationNodeModel<Object, Object> childRelationNodeModel = relationTreeModel.getNode(
					beanRelationNodeModel.getChildEntityTypeId(),
					bean,
					childEntityTypeId);
			final ChildModelListener childModelListener = new ChildModelListener(childRelationNodeModel);
			childRelationNodeModel.addBeanListModelListener(childModelListener);
		}
	}

	private void renderNodeShape(final String nodeGroup, final Node childNode) {
		childNode.set("type", nodeGroup);

	}

	@SuppressWarnings("rawtypes")
	private final class RootModelListener extends BeanListModelListenerAdapter<CHILD_BEAN_TYPE> {

		private final IBeanRelationNodeModel root;

		private RootModelListener() {
			this.root = relationTreeModel.getRoot();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void beansChanged() {
			nodeMap.clear();
			nodeMapInt.clear();
			synchronized (vis) {
				graph.clear();
				onBeansChanged(root);
			}

		}
	}

	private final class ChildModelListener extends BeanListModelListenerAdapter<Object> {

		private final IBeanRelationNodeModel<Object, Object> relationNodeModel;

		public ChildModelListener(final IBeanRelationNodeModel<Object, Object> relationNodeModel) {
			this.relationNodeModel = relationNodeModel;
		}

		@Override
		public void beansChanged() {
			synchronized (vis) {
				onBeansChanged(relationNodeModel);
				vis.run("layout");
				vis.run("color");
			}
		}
	}

	private void renderNode(final Node node, final IBeanProxy<Object> bean, final IBeanProxyLabelRenderer<Object> renderer) {
		if (!bean.isDummy()) {
			loadIcon(node, bean, renderer);
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

	private void loadIcon(final Node node, final IBeanProxy<Object> bean, final IBeanProxyLabelRenderer<Object> renderer) {

		final ImageIcon imageIcon = SwingImageRegistry.getInstance().getImageIcon(renderer.getLabel(bean).getIcon());
		if (imageIcon != null) {
			imageFactory.addImage(imageIcon.getImage().toString(), imageIcon.getImage());
			node.set("image", imageIcon.getImage());
		}
	}

	private ForceSimulator setForces() {
		final SpringForce springForce = new SpringForce(5.95E-5F, 120);
		final NBodyForce nBodyForce = new NBodyForce(-10, 320, 0);
		final DragForce dragForce = new DragForce(0.100f);

		forceSimulator = new ForceSimulator();
		forceSimulator.addForce(dragForce);
		forceSimulator.addForce(nBodyForce);
		forceSimulator.addForce(springForce);

		return forceSimulator;
	}

	private IToolBarModel initToolBar() {
		final IActionBuilderFactory actionBF = Toolkit.getActionBuilderFactory();
		final ToolBarModel model = new ToolBarModel();
		nodeCountItem = model.addCheckedItem(Messages.getMessage("BeanRelationGraphImpl.max.nodecount").get());
		nodeCountItem.setEnabled(false);

		final IInputFieldBluePrint<Integer> textFieldBP = BPF.inputFieldIntegerNumber().setMaxLength(3).setValue(maxNodeCount);

		final InputControlItemModel<Integer> textField = new InputControlItemModel<Integer>(textFieldBP, 30);
		textField.addInputListener(new IInputListener() {

			@Override
			public void inputChanged() {
				if (textField.getValue() != null) {
					maxNodeCount = textField.getValue();
					relationTreeModel.load();
				}
			}
		});
		model.addItem(textField);
		model.addSeparator();

		final ICheckedItemModel checkItemModel = model.addCheckedItem(
				IconsSmall.NAVIGATION_NEXT_TINY,
				Messages.getMessage("BeanRelationGraphImpl.animation.on").get());
		checkItemModel.setSelected(true);
		checkItemModel.addItemListener(new IItemStateListener() {
			private boolean on = true;

			@Override
			public void itemStateChanged() {
				if (on) {
					checkItemModel.setText(Messages.getMessage("BeanRelationGraphImpl.animation.on").get());
					vis.setValue(NODES, null, VisualItem.FIXED, true);
					vis.setValue(EDGES, null, VisualItem.FIXED, true);
				}
				else {
					checkItemModel.setText(Messages.getMessage("BeanRelationGraphImpl.animation.off").get());
					vis.setValue(NODES, null, VisualItem.FIXED, false);
					vis.setValue(EDGES, null, VisualItem.FIXED, false);
				}
				on = !on;
			}
		});

		final IActionBuilder settingsDialogActionBuilder = actionBF.create();
		settingsDialogActionBuilder.setIcon(IconsSmall.SETTINGS);
		settingsDialogActionBuilder.setCommand(new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				if (dialog == null) {
					dialog = getControlDialog();
				}
				else {
					dialog.setVisible(true);
				}
			}
		});
		final ICommandAction settingsDialogAction = settingsDialogActionBuilder.build();

		final IComboBoxBluePrint<String> comboBoxBp = BPF.comboBox(
				"ForceDirectedLayout",
				"NodeLinkTreeLayout",
				"RadialTreeLayout");
		final InputControlItemModel<String> comboBox = new InputControlItemModel<String>(comboBoxBp, 150);
		comboBox.addInputListener(new IInputListener() {

			@Override
			public void inputChanged() {

				if (comboBox.getValue().equals("ForceDirectedLayout")) {
					settingsDialogAction.setEnabled(true);
					initForceDLayout();
				}
				else if (comboBox.getValue().equals("NodeLinkTreeLayout")) {
					settingsDialogAction.setEnabled(false);
					initNodeLinkLayout();
				}
				else if (comboBox.getValue().equals("RadialTreeLayout")) {
					settingsDialogAction.setEnabled(false);
					initRadialTreeLayout();
				}

			}
		});

		comboBox.setValue("ForceDirectedLayout");

		model.addItem(comboBox);
		model.addSeparator();

		model.addAction(settingsDialogAction);
		final ICheckedItemModel edgeCheckedItem = model.addCheckedItem("Edge Label");
		edgeCheckedItem.setSelected(true);
		edgeCheckedItem.addItemListener(new IItemStateListener() {

			@Override
			public void itemStateChanged() {
				labelEdgeLayout.setTrigger(edgeCheckedItem.isSelected());
			}
		});

		final IActionBuilder screenShotActionBuilder = actionBF.create();
		screenShotActionBuilder.setText(Messages.getMessage("BeanRelationGraphImpl.screenshot").get()).setToolTipText(
				Messages.getMessage("BeanRelationGraphImpl.copy.in.clipboard").get());
		screenShotActionBuilder.setCommand(new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				final BufferedImage bufImage = new BufferedImage(
					display.getSize().width,
					display.getSize().height,
					BufferedImage.TYPE_INT_RGB);
				display.paint(bufImage.createGraphics());

				java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageSelection(bufImage), null);

			}
		});
		final ICommandAction screenShotAction = screenShotActionBuilder.build();
		model.addAction(screenShotAction);

		return model;
	}

	private void initForceDLayout() {
		synchronized (vis) {
			vis.removeAction("layout");

			final ForceDirectedLayout fdl = new ForceDirectedLayout("graph", true);
			fdl.setForceSimulator(setForces());

			final ActionList layout = new ActionList(Activity.INFINITY);
			layout.add(fdl);
			layout.add(labelEdgeLayout);
			layout.add(new RepaintAction());

			vis.putAction("layout", layout);
			vis.run("layout");
			vis.run("color");
		}
	}

	private void initNodeLinkLayout() {
		vis.removeAction("layout");
		synchronized (vis) {
			final ActionList layout = new ActionList(Activity.INFINITY);
			final NodeLinkTreeLayout nodeLinkTreeLayout = new NodeLinkTreeLayout("graph", Constants.ORIENT_LEFT_RIGHT, 50, 5, 5);
			nodeLinkTreeLayout.setRootNodeOffset(10);
			layout.add(nodeLinkTreeLayout);
			layout.add(labelEdgeLayout);

			layout.add(new RepaintAction(vis));

			vis.putAction("layout", layout);
		}
		vis.run("layout");
		vis.run("color");
	}

	private void initRadialTreeLayout() {
		synchronized (vis) {
			vis.removeAction("layout");

			final ActionList layout = new ActionList(Activity.INFINITY);
			final RadialTreeLayout treeLayout = new RadialTreeLayout("graph");
			layout.add(treeLayout);
			layout.add(new RepaintAction(vis));
			layout.add(new TreeRootAction("graph"));
			final CollapsedSubtreeLayout subLayout = new CollapsedSubtreeLayout("graph");
			layout.add(subLayout);

			layout.add(labelEdgeLayout);

			final ActionList animate = new ActionList(1250);
			animate.setPacingFunction(new SlowInSlowOutPacer());
			animate.add(new QualityControlAnimator());
			animate.add(new VisibilityAnimator("graph"));
			animate.add(new PolarLocationAnimator(NODES, "linear"));
			animate.add(new ColorAnimator(NODES));
			animate.add(new RepaintAction());

			display.setItemSorter(new TreeDepthItemSorter());

			vis.putAction("animate", animate);
			vis.putAction("layout", layout);
			vis.alwaysRunAfter("layout", "animate");
			vis.run("layout");
			vis.run("color");
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

	private IFrame getControlDialog() {
		final IDialogBluePrint dialogBP = BPF.dialog().setResizable(false).setModal(false);

		final IFrame childWindow = Toolkit.getActiveWindow().createChildWindow(dialogBP);
		childWindow.setLayout(MigLayoutFactory.growingInnerCellLayout());

		final IComposite composite = childWindow.add(BPF.composite(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

		final JPanel panel = JoToSwing.convert(composite);
		panel.setLayout(new BorderLayout());
		panel.add(new JForcePanel(forceSimulator), BorderLayout.CENTER);

		if (dialogPosition != null) {
			childWindow.setPosition(dialogPosition);
		}
		childWindow.setVisible(true);

		return childWindow;
	}

	private class TreeRootAction extends GroupAction {
		public TreeRootAction(final String graphGroup) {
			super(graphGroup);
		}

		@Override
		public void run(final double frac) {
			final TupleSet focus = vis.getGroup(Visualization.FOCUS_ITEMS);
			if (focus == null || focus.getTupleCount() == 0) {
				return;
			}
			//CHECKSTYLE:OFF
			final Graph g = (Graph) vis.getGroup(m_group);
			Node f = null;
			while (!g.containsTuple(f = (Node) focus.tuples().next())) {
				f = null;
			}
			if (f == null) {
				return;
			}
			//CHECKSTYLE:ON
			g.getSpanningTree(f);
		}
	}

	private class LabelEdgeLayout extends Layout {
		private boolean trigger;

		public LabelEdgeLayout(final String group) {
			super(group);
			trigger = true;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void run(final double frac) {

			final Iterator iter = m_vis.items(m_group);
			while (iter.hasNext()) {
				final DecoratorItem decorator = (DecoratorItem) iter.next();
				decorator.setVisible(true);
				if (trigger) {
					final VisualItem decoratedItem = decorator.getDecoratedItem();
					final Rectangle2D bounds = decoratedItem.getBounds();
					final double x = bounds.getCenterX();
					final double y = bounds.getCenterY();
					setX(decorator, null, x);
					setY(decorator, null, y);
				}
				else {
					decorator.setVisible(false);
				}
			}

		}

		public void setTrigger(final boolean onOff) {
			trigger = onOff;
		}
	}

	private static class ImageSelection implements Transferable {
		private final Image image;

		public ImageSelection(final Image image) {
			this.image = image;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] {DataFlavor.imageFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(final DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}

		@Override
		public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (!DataFlavor.imageFlavor.equals(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			return image;
		}
	}
}
