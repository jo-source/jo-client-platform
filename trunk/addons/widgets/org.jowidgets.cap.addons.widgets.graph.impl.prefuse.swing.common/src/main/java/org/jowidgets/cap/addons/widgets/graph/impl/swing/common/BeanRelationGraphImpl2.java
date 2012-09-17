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
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.command.IActionBuilderFactory;
import org.jowidgets.api.command.ICommandAction;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.model.item.ICheckedItemModel;
import org.jowidgets.api.model.item.IToolBarModel;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IFrame;
import org.jowidgets.api.widgets.IToolBar;
import org.jowidgets.api.widgets.blueprint.IComboBoxSelectionBluePrint;
import org.jowidgets.api.widgets.blueprint.IInputFieldBluePrint;
import org.jowidgets.cap.ui.api.addons.widgets.IBeanRelationGraph;
import org.jowidgets.cap.ui.api.addons.widgets.IBeanRelationGraphSetupBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.icons.CapIcons;
import org.jowidgets.cap.ui.api.model.ILabelModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModel;
import org.jowidgets.cap.ui.api.types.IEntityTypeId;
import org.jowidgets.cap.ui.tools.model.BeanListModelListenerAdapter;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.image.IImageHandle;
import org.jowidgets.common.types.Orientation;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.common.widgets.controller.IItemStateListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.spi.impl.swing.common.image.SwingImageRegistry;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.model.item.InputControlItemModel;
import org.jowidgets.tools.model.item.ToolBarModel;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;
import org.jowidgets.util.IConverter;

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
import prefuse.action.filter.VisibilityFilter;
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
import prefuse.data.expression.Predicate;
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
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.sort.TreeDepthItemSorter;

class BeanRelationGraphImpl2<CHILD_BEAN_TYPE> extends ControlWrapper implements IBeanRelationGraph<CHILD_BEAN_TYPE> {

	private static final int MAX_NODE_COUNT_DEFAULT = 100;

	private static final int[] NODE_COLORS = new int[] {
			ColorLib.gray(200), ColorLib.rgba(135, 206, 250, 255), ColorLib.rgba(95, 158, 160, 255),
			ColorLib.rgba(0, 255, 127, 255), ColorLib.rgba(240, 230, 140, 255), ColorLib.rgba(220, 220, 220, 255),
			ColorLib.rgba(222, 127, 0, 255), ColorLib.rgba(222, 127, 227, 255), ColorLib.rgba(218, 191, 113, 255),
			ColorLib.rgba(99, 241, 113, 255), ColorLib.rgba(99, 241, 113, 255), ColorLib.rgba(45, 84, 187, 255)};

	private static final String GRAPH = "graph";
	private static final String NODES = "graph.nodes";
	private static final String EDGES = "graph.edges";
	private static final String EDGE_DECORATORS = "edgeDeco";
	private static final String GRAPH_NODES_GROUP = "graph.nodes.group";

	private static final Schema DECORATOR_SCHEMA = createDecoratorSchema();

	private final IBeanRelationTreeModel<CHILD_BEAN_TYPE> relationTreeModel;

	private final Map<IBeanProxy<Object>, Node> nodeMap;
	private final Map<Class<Object>, String> entityGroupMap;
	private final HashMap<IBeanProxy<Object>, IBeanRelationNodeModel<Object, Object>> beanRelationMap;

	private final Visualization vis;
	private final Graph graph;
	private final Display display;

	private final LabelEdgeLayout labelEdgeLayout;
	private final ImageFactory imageFactory;

	private int autoExpandLevel;

	private ForceSimulator forceSimulator;

	private IFrame dialog;

	private int maxNodeCount = MAX_NODE_COUNT_DEFAULT;
	private int groupCount;

	private final NodeVisibilityFilter visFilter;

	private final EdgeAction edgeFilter;

	private final NodeVisibilityAction nodeFilter;

	BeanRelationGraphImpl2(
		final IComposite composite,
		final IConverter<IComposite, Container> awtConverter,
		final IBeanRelationGraphSetupBuilder<CHILD_BEAN_TYPE, ?> setup) {
		super(composite);

		relationTreeModel = setup.getModel();
		nodeMap = new HashMap<IBeanProxy<Object>, Node>();
		entityGroupMap = new HashMap<Class<Object>, String>();
		beanRelationMap = new HashMap<IBeanProxy<Object>, IBeanRelationNodeModel<Object, Object>>();

		autoExpandLevel = setup.getAutoExpandLevel();

		graph = new Graph();
		graph.addColumn("name", String.class);
		graph.addColumn("image", Image.class);
		graph.addColumn("type", String.class);
		graph.addColumn("level", Integer.class);
		graph.addColumn("expanded", Boolean.class);
		graph.addColumn("position", Point.class);

		vis = new Visualization();
		vis.addGraph(GRAPH, graph);
		vis.setInteractive(EDGES, null, false);
		vis.setInteractive(NODES, null, true);

		//		final SearchQueryBinding searchQ = new SearchQueryBinding(graph, "type");
		//		final AndPredicate filter = new AndPredicate(searchQ.getPredicate());

		final Predicate p = (Predicate) ExpressionParser.parse("level <= " + autoExpandLevel);

		final ActionList filter = new ActionList();
		visFilter = new NodeVisibilityFilter(GRAPH, p);
		edgeFilter = new EdgeAction();
		nodeFilter = new NodeVisibilityAction();
		filter.add(visFilter);
		filter.add(edgeFilter);
		filter.add(nodeFilter);

		final ActionList color = new ActionList();
		color.add(new ColorAction(NODES, VisualItem.STROKECOLOR, 0));
		color.add(new ColorAction(NODES, VisualItem.TEXTCOLOR, ColorLib.rgb(0, 0, 0)));
		color.add(new FontAction(NODES, new Font("Tahoma", Font.BOLD, 10)));
		color.add(new ColorAction(EDGES, VisualItem.FILLCOLOR, ColorLib.gray(200)));
		color.add(new ColorAction(EDGES, VisualItem.STROKECOLOR, ColorLib.gray(200)));

		final ColorAction fill = new ColorAction(NODES, VisualItem.FILLCOLOR);
		for (int index = 0; index < NODE_COLORS.length; index++) {
			fill.add("type == '" + GRAPH_NODES_GROUP + index + "'", NODE_COLORS[index]);
		}
		color.add(fill);

		vis.putAction("color", color);
		vis.putAction("filter", filter);

		final LabelRenderer renderer = new LabelRenderer("name", "image");
		renderer.setRoundedCorner(8, 8);
		renderer.setHorizontalAlignment(Constants.CENTER);
		renderer.setVerticalAlignment(Constants.CENTER);

		imageFactory = new ImageFactory();
		renderer.setImageFactory(imageFactory);
		final DefaultRendererFactory rendererFactory = new DefaultRendererFactory(renderer);
		vis.setRendererFactory(rendererFactory);
		rendererFactory.add(new InGroupPredicate(EDGE_DECORATORS), new LabelRenderer("name"));

		vis.addDecorators(EDGE_DECORATORS, EDGES, DECORATOR_SCHEMA);

		labelEdgeLayout = new LabelEdgeLayout();
		labelEdgeLayout.setEdgesVisible(false);
		initForceDLayout();

		display = new Display(vis);
		display.setHighQuality(true);
		//		display.addControlListener(new FocusControl(1));
		display.addControlListener(new DragControl());
		display.addControlListener(new PanControl());
		display.addControlListener(new ZoomControl());
		display.addControlListener(new WheelZoomControl());
		display.addControlListener(new ZoomToFitControl());
		display.addControlListener(new NeighborHighlightControl());
		display.addControlListener(new FocusControl(NODES, 2) {
			@Override
			public void itemClicked(final VisualItem item, final MouseEvent e) {
				if (e.getClickCount() == 2) {
					final int row = item.getRow();
					final Node node = graph.getNode(row);
					for (final Entry<IBeanProxy<Object>, Node> entry : nodeMap.entrySet()) {
						if (entry.getValue() == node) {
							final IBeanRelationNodeModel<Object, Object> beanRelationNodeModel = beanRelationMap.get(entry.getKey());
							for (final IEntityTypeId<Object> entityType : beanRelationNodeModel.getChildRelations()) {
								final IBeanRelationNodeModel<Object, Object> childRelationModel = relationTreeModel.getNode(
										beanRelationNodeModel.getChildEntityTypeId(),
										entry.getKey(),
										entityType);
								childRelationModel.loadIfNotYetDone();

							}

							beanRelationNodeModel.loadIfNotYetDone();
							break;
						}
					}

				}
			}
		});

		final ToolTipControl ttc = new ToolTipControl("name");
		display.addControlListener(ttc);

		composite.setLayout(new MigLayoutDescriptor("0[grow, 0::]0", "0[][]0[grow, 0::]0"));
		final IToolBar toolbar = composite.add(BPF.toolBar(), "wrap");
		composite.add(BPF.separator().setOrientation(Orientation.HORIZONTAL), "growx, w 0::, wrap");
		final IComposite content = composite.add(BPF.composite(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

		final Container swingContainer = awtConverter.convert(content);

		swingContainer.setLayout(new BorderLayout());
		swingContainer.add(display, BorderLayout.CENTER);

		swingContainer.addHierarchyListener(new HierarchyListener() {

			@Override
			public void hierarchyChanged(final HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					if (!swingContainer.isShowing()) {
						if (dialog != null) {
							dialog.setVisible(false);
							dialog.dispose();
						}
					}
				}
			}
		});

		toolbar.setModel(initToolBar());

		relationTreeModel.getRoot().addBeanListModelListener(new RootModelListener());
	}

	private static Schema createDecoratorSchema() {
		final Schema result = PrefuseLib.getVisualItemSchema();
		result.setDefault(VisualItem.INTERACTIVE, false);
		result.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(128));
		result.setDefault(VisualItem.FONT, FontLib.getFont("Tahoma", 9));
		result.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(0));
		return result;
	}

	@Override
	public IBeanRelationTreeModel<CHILD_BEAN_TYPE> getModel() {
		return relationTreeModel;
	}

	private void onBeansChanged(final IBeanRelationNodeModel<Object, Object> relationNodeModel) {

		for (int i = 0; i < relationNodeModel.getSize(); i++) {
			final IBeanProxy<Object> bean = relationNodeModel.getBean(i);
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
			childNode.set("expanded", false);
			beanRelationMap.put(bean, beanRelationNodeModel);
		}

		if (entityGroupMap.get(beanRelationNodeModel.getChildBeanType()) == null) {
			entityGroupMap.put(beanRelationNodeModel.getChildBeanType(), GRAPH_NODES_GROUP + groupCount);
			groupCount = (groupCount < NODE_COLORS.length) ? ++groupCount : 0;
		}

		final String nodeGroup = entityGroupMap.get(beanRelationNodeModel.getChildBeanType());

		final Node parentNode = nodeMap.get(beanRelationNodeModel.getParentBean());

		if (parentNode != null && (!bean.isDummy())) {
			parentNode.set("expanded", true);
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
			synchronized (vis) {
				graph.clear();
				onBeansChanged(root);
				vis.run("color");
				vis.run("filter");
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
				vis.run("filter");
			}
		}
	}

	private void renderNode(final Node node, final IBeanProxy<Object> bean, final IBeanProxyLabelRenderer<Object> renderer) {
		if (!bean.isDummy()) {
			renderIcon(node, bean, renderer);
			renderNodeWithLabel(node, renderer.getLabel(bean));
		}

	}

	private void renderIcon(final Node node, final IBeanProxy<Object> bean, final IBeanProxyLabelRenderer<Object> renderer) {
		final IImageConstant icon = renderer.getLabel(bean).getIcon();
		if (icon != null) {
			final IImageHandle imageHandle = Toolkit.getImageRegistry().getImageHandle(icon);
			if (imageHandle != null) {
				final Object image = imageHandle.getImage();
				final URL imageUrl = imageHandle.getImageUrl();
				if (image instanceof Image) {
					renderIcon(node, (Image) image);
				}
				else if (imageUrl != null) {
					IImageHandle awtImageHandle = SwingImageRegistry.getInstance().getImageHandle(icon);
					if (awtImageHandle == null) {
						SwingImageRegistry.getInstance().registerImageConstant(icon, imageUrl);
						awtImageHandle = SwingImageRegistry.getInstance().getImageHandle(icon);
					}
					renderIcon(node, (Image) awtImageHandle.getImage());
				}
			}
		}
	}

	private void renderIcon(final Node node, final Image awtImage) {
		imageFactory.addImage(awtImage.toString(), awtImage);
		node.set("image", awtImage);
	}

	private ForceSimulator setForces() {
		final SpringForce springForce = new SpringForce(1E-4f, 120);
		final NBodyForce nBodyForce = new NBodyForce(-10, 320, 0);
		final DragForce dragForce = new DragForce(0.1f);

		forceSimulator = new ForceSimulator();
		forceSimulator.addForce(dragForce);
		forceSimulator.addForce(nBodyForce);
		forceSimulator.addForce(springForce);

		return forceSimulator;
	}

	private IToolBarModel initToolBar() {
		final IActionBuilderFactory actionBF = Toolkit.getActionBuilderFactory();
		final ToolBarModel model = new ToolBarModel();

		model.addTextLabel(Messages.getString("BeanRelationGraphImpl.max.nodecount") + " ");

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

		model.addTextLabel("ExpandLevel");
		final IComboBoxSelectionBluePrint<Integer> comboBoxExpandLevelBp = BPF.comboBoxSelectionIntegerNumber().setElements(
				0,
				1,
				2,
				3,
				4,
				5);
		final InputControlItemModel<Integer> comboBoxExpandLevel = new InputControlItemModel<Integer>(comboBoxExpandLevelBp, 30);
		comboBoxExpandLevel.addInputListener(new IInputListener() {

			@Override
			public void inputChanged() {
				if (comboBoxExpandLevel.getValue() != null) {
					synchronized (vis) {
						autoExpandLevel = comboBoxExpandLevel.getValue();
						vis.run("filter");
						display.repaint();
					}
				}
			}
		});
		comboBoxExpandLevel.setValue(autoExpandLevel);
		model.addItem(comboBoxExpandLevel);

		model.addSeparator();

		final IActionBuilder settingsDialogActionBuilder = actionBF.create();
		settingsDialogActionBuilder.setIcon(CapIcons.GRAPH_SETTINGS);
		settingsDialogActionBuilder.setCommand(new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				if (dialog == null) {
					dialog = new GraphSettingsDialog(forceSimulator);
				}
				dialog.setVisible(true);
			}
		});

		final ICommandAction settingsDialogAction = settingsDialogActionBuilder.build();

		final IComboBoxSelectionBluePrint<GraphLayout> comboBoxBp = BPF.comboBoxSelection(GraphLayout.values());

		comboBoxBp.setAutoCompletion(false);
		final InputControlItemModel<GraphLayout> comboBox = new InputControlItemModel<GraphLayout>(comboBoxBp, 150);
		comboBox.addInputListener(new IInputListener() {

			@Override
			public void inputChanged() {
				final GraphLayout value = comboBox.getValue();
				if (GraphLayout.FORCE_DIRECTED_LAYOUT == value) {
					settingsDialogAction.setEnabled(true);
					initForceDLayout();
				}
				else if (GraphLayout.NODE_TREE_LINK_LAYOUT == value) {
					settingsDialogAction.setEnabled(false);
					initNodeLinkLayout();
				}
				else if (GraphLayout.RADIAL_TREE_LAYOUT == value) {
					settingsDialogAction.setEnabled(false);
					initRadialTreeLayout();
				}

			}
		});
		comboBox.setValue(GraphLayout.FORCE_DIRECTED_LAYOUT);
		model.addItem(comboBox);

		final ICheckedItemModel checkItemModel = model.addCheckedItem(
				CapIcons.GRAPH_ANIMATION,
				Messages.getMessage("BeanRelationGraphImpl.animation.on").get());
		checkItemModel.setSelected(true);
		checkItemModel.addItemListener(new IItemStateListener() {
			private boolean on = true;

			@Override
			public void itemStateChanged() {
				synchronized (vis) {
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
			}
		});

		model.addSeparator();

		model.addAction(settingsDialogAction);
		final ICheckedItemModel edgeCheckedItem = model.addCheckedItem("Edge Label");
		edgeCheckedItem.setIcon(CapIcons.GRAPH_LETTERING);
		edgeCheckedItem.setSelected(false);
		edgeCheckedItem.addItemListener(new IItemStateListener() {

			@Override
			public void itemStateChanged() {
				labelEdgeLayout.setEdgesVisible(edgeCheckedItem.isSelected());
			}
		});

		final IActionBuilder screenShotActionBuilder = actionBF.create();
		screenShotActionBuilder.setText(Messages.getMessage("BeanRelationGraphImpl.screenshot").get());
		screenShotActionBuilder.setIcon(CapIcons.GRAPH_SNAPSHOT);
		screenShotActionBuilder.setToolTipText(Messages.getMessage("BeanRelationGraphImpl.copy.in.clipboard").get());
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
			fdl.setForceSimulator(forceSimulator != null ? forceSimulator : setForces());

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
		node.set("level", node.getDepth());
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
			final Graph g = (Graph) vis.getGroup(m_group);
			final Node node = getFirstContainingNode(g, focus);
			if (node != null) {
				g.getSpanningTree(node);
			}
		}

		private Node getFirstContainingNode(final Graph graph, final TupleSet focus) {
			final Iterator<?> iterator = focus.tuples();
			while (iterator.hasNext()) {
				final Node node = (Node) iterator.next();
				if (graph.containsTuple(node)) {
					return node;
				}
			}
			return null;
		}
	}

	private final class LabelEdgeLayout extends Layout {
		private boolean edgesVisible;

		private LabelEdgeLayout() {
			super(EDGE_DECORATORS);
			edgesVisible = true;
		}

		@Override
		public void run(final double frac) {

			final Iterator<?> iter = m_vis.items(m_group);
			while (iter.hasNext()) {
				final DecoratorItem decorator = (DecoratorItem) iter.next();
				if (edgesVisible) {
					decorator.setVisible(true);
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

		private void setEdgesVisible(final boolean visible) {
			edgesVisible = visible;
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

	private class EdgeAction extends GroupAction {

		public EdgeAction() {
			super();
		}

		@Override
		public void run(final double frac) {
			final TupleSet edges = vis.getGroup(EDGES);
			final Iterator<?> edge = edges.tuples();
			while (edge.hasNext()) {
				final Edge test = (Edge) edge.next();

				final VisualItem childNode = (VisualItem) test.getTargetNode();
				final VisualItem parentNode = (VisualItem) test.getSourceNode();
				if (!childNode.isVisible() || !parentNode.isVisible()) {
					final VisualItem result = (VisualItem) test;
					result.setVisible(false);
				}
				else if (childNode.isVisible()) {
					final VisualItem result = (VisualItem) test;
					result.setVisible(true);
				}
			}
		}
	}

	private class NodeVisibilityAction extends GroupAction {

		public NodeVisibilityAction() {
			super();
		}

		@Override
		public void run(final double frac) {
			final TupleSet nodes = vis.getGroup(NODES);
			final Iterator<?> node = nodes.tuples();
			while (node.hasNext()) {
				final VisualItem test = (VisualItem) node.next();
				//CHECKSTYLE:OFF
				System.out.println(test.get("name") + " | " + test.get("level") + " | " + test.get("expanded"));
				//CHECKSTYLE:ON
			}
		}
	}

	private class NodeVisibilityFilter extends VisibilityFilter {

		public NodeVisibilityFilter(final String group, final Predicate p) {
			super(group, p);
		}

		@Override
		public void run(final double frac) {
			final Predicate p = (Predicate) ExpressionParser.parse("level <= " + autoExpandLevel);
			this.setPredicate(p);
			super.run(frac);
		}

	}

	private static enum GraphLayout {

		//TODO i18n
		FORCE_DIRECTED_LAYOUT("Force directed layout"),
		NODE_TREE_LINK_LAYOUT("Node tree layout"),
		RADIAL_TREE_LAYOUT("Radial tree layout");

		private final String label;

		private GraphLayout(final String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}

	}
}
