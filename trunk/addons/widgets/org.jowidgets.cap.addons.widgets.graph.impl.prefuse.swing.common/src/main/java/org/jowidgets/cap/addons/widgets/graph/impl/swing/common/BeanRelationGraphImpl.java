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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.command.IActionBuilderFactory;
import org.jowidgets.api.command.ICommandAction;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.model.item.ICheckedItemModel;
import org.jowidgets.api.model.item.IToolBarModel;
import org.jowidgets.api.threads.IUiThreadAccess;
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
import org.jowidgets.common.image.IImageDescriptor;
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
import prefuse.action.Action;
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
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.ImageFactory;
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

class BeanRelationGraphImpl<CHILD_BEAN_TYPE> extends ControlWrapper implements IBeanRelationGraph<CHILD_BEAN_TYPE> {

	public static final String GRAPH = "graph";
	public static final String NODES = "graph.nodes";
	public static final String EDGES = "graph.edges";
	public static final String GRAPH_NODES_GROUP = "graph.nodes.group";

	private static final long ACTIVITY_DEFAULT_TIME = 15L;
	private static final int MAX_NODE_COUNT_DEFAULT = 100;
	private static final int MAX_LABELTEXT_LENGTH = 40;
	private static final int EXPAND_ICON_SIZE = 18;
	private static final int MAX_EXPANDED_NODES_CACHE = 500;

	private static final int[] NODE_COLORS = new int[] {
			ColorLib.gray(180), ColorLib.rgba(105, 176, 220, 255), ColorLib.rgba(191, 112, 97, 255),
			ColorLib.rgba(7, 162, 28, 255), ColorLib.rgba(192, 210, 0, 255), ColorLib.rgba(147, 129, 186, 255),
			ColorLib.rgba(217, 123, 82, 255), ColorLib.rgba(222, 127, 227, 255), ColorLib.rgba(239, 215, 143, 255),
			ColorLib.rgba(99, 241, 113, 255), ColorLib.rgba(79, 124, 36, 255), ColorLib.rgba(45, 84, 187, 255)};

	private static final String EDGE_DECORATORS = "edgeDeco";
	private static final Schema DECORATOR_SCHEMA = createDecoratorSchema();

	private static int autoExpandLevel;
	private static Node markedNode;
	private static BeanGraphSettingsDialog settingsDialog;

	private final IBeanRelationTreeModel<CHILD_BEAN_TYPE> relationTreeModel;
	private final Map<IBeanProxy<Object>, Node> nodeMap;
	private final Map<Class<Object>, String> entityGroupMap;
	private Map<Class<Object>, Boolean> groupVisibilityMap;
	private HashMap<String, Boolean> edgeVisibilityMap;
	private final HashMap<IBeanProxy<Object>, IBeanRelationNodeModel<Object, Object>> beanRelationMap;
	private final HashMap<String, String> groupNames;
	private final List<Node> expandedNodesCache;
	private Set<IBeanRelationNodeModel<Object, Object>> expandMapResult;

	private final Visualization vis;
	private final Graph graph;
	private final Display display;

	private final LabelEdgeLayout labelEdgeLayout;
	private final ImageFactory imageFactory;

	private ForceSimulator forceSimulator;

	private IFrame dialog;

	private int maxNodeCount = MAX_NODE_COUNT_DEFAULT;
	private int groupCount;

	private final EdgeVisibilityAction edgeFilter;
	private final NodeVisibilityAction nodeFilter;
	private final RemoveStandaloneNodesAction removeStandaloneNodesFilter;
	private final IUiThreadAccess uiThreadAccess;
	private InputControlItemModel<Integer> maxNodeTextField;
	private RadialTreeLayout radialTreeLayout;

	BeanRelationGraphImpl(
		final IComposite composite,
		final IConverter<IComposite, Container> awtConverter,
		final IBeanRelationGraphSetupBuilder<CHILD_BEAN_TYPE, ?> setup) {
		super(composite);

		uiThreadAccess = Toolkit.getUiThreadAccess();

		relationTreeModel = setup.getModel();
		nodeMap = new HashMap<IBeanProxy<Object>, Node>();
		entityGroupMap = new HashMap<Class<Object>, String>();
		groupVisibilityMap = new HashMap<Class<Object>, Boolean>();
		edgeVisibilityMap = new HashMap<String, Boolean>();
		beanRelationMap = new HashMap<IBeanProxy<Object>, IBeanRelationNodeModel<Object, Object>>();
		groupNames = new HashMap<String, String>();
		expandedNodesCache = new LinkedList<Node>();
		expandMapResult = new HashSet<IBeanRelationNodeModel<Object, Object>>();

		autoExpandLevel = setup.getAutoExpandLevel();
		settingsDialog = null;

		graph = new Graph();
		graph.addColumn("name", String.class);
		graph.addColumn("tooltip", String.class);
		graph.addColumn("image", Image.class);
		graph.addColumn("type", String.class);
		graph.addColumn("level", Integer.class);
		graph.addColumn("expanded", Expand.class);
		graph.addColumn("position", Point.class);
		graph.addColumn("visible", Boolean.class);
		graph.addColumn("beanrelation", Object.class);
		graph.addColumn("isParent", Boolean.class);
		graph.addColumn("marked", Boolean.class);

		vis = new Visualization();
		vis.addGraph(GRAPH, graph);
		vis.setInteractive(EDGES, null, false);
		vis.setInteractive(NODES, null, true);

		final Action expand = new ExpandLevelVisibilityAction(GRAPH);
		final Action marked = new NodeMarkedAction(vis);

		final ActionList filter = new ActionList();
		nodeFilter = new NodeVisibilityAction(vis);
		edgeFilter = new EdgeVisibilityAction(edgeVisibilityMap, vis);
		removeStandaloneNodesFilter = new RemoveStandaloneNodesAction(vis);

		filter.add(nodeFilter);
		filter.add(edgeFilter);
		filter.add(removeStandaloneNodesFilter);

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
		vis.putAction("expand", expand);
		vis.putAction("marked", marked);

		final NodeRenderer renderer = new NodeRenderer("name", "image");
		renderer.setHorizontalAlignment(Constants.CENTER);
		renderer.setVerticalAlignment(Constants.CENTER);

		imageFactory = new ImageFactory();
		renderer.setImageFactory(imageFactory);
		final DefaultRendererFactory rendererFactory = new DefaultRendererFactory(renderer);
		vis.setRendererFactory(rendererFactory);
		rendererFactory.add(new InGroupPredicate(EDGE_DECORATORS), new EdgeRenderer("name"));

		vis.addDecorators(EDGE_DECORATORS, EDGES, DECORATOR_SCHEMA);

		labelEdgeLayout = new LabelEdgeLayout();
		labelEdgeLayout.setEdgesVisible(false);
		initForceDLayout();

		display = new Display(vis);
		display.setHighQuality(true);
		display.addControlListener(new DragControl());
		display.addControlListener(new PanControl());
		display.addControlListener(new ZoomControl());
		display.addControlListener(new WheelZoomControl());
		display.addControlListener(new ZoomToFitControl());
		display.addControlListener(new NeighborHighlightControl());
		display.addControlListener(new FocusControl(NODES, 1) {
			@Override
			public void itemClicked(final VisualItem item, final MouseEvent e) {

				final int row = item.getRow();
				final Node node = graph.getNode(row);

				final double scale = display.getScale();

				if ((Boolean) item.get("isParent")) {

					if (item.get("expanded") == Expand.PARTIALLY) {
						if (checkExpandIconHit(false, item, e, scale)) {
							item.set("expanded", Expand.FULL);
							loadModel(node);
							return;

						}
						else if (checkExpandIconHit(true, item, e, scale)) {
							item.set("expanded", Expand.NOT);
							loadModel(node);
							return;
						}
					}
					else if (item.get("expanded") == Expand.FULL || item.get("expanded") == Expand.NOT) {
						if (checkExpandIconHit(false, item, e, scale)) {
							loadModel(node);
							return;
						}
					}
				}

				if ((Boolean) node.get("marked")) {
					node.set("marked", false);
					markedNode = null;
				}
				else {
					node.set("marked", true);
					markedNode = node;
				}
				synchronized (vis) {
					vis.run("marked");
					vis.run("filter");
				}
			}
		});

		final ToolTipControl ttc = new ToolTipControl("tooltip");
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

	private Set<Node> getOutlinedNodes(final Node node) {

		final Iterator<?> outNode = node.outNeighbors();
		final Iterator<?> children = node.children();
		final Set<Node> outs = new LinkedHashSet<Node>();
		Set<Node> kids = new LinkedHashSet<Node>();

		while (outNode.hasNext()) {
			final Node out = (Node) outNode.next();
			outs.add(out);
		}
		while (children.hasNext()) {
			final Node child = (Node) children.next();
			kids.add(child);
		}
		outs.addAll(kids);
		kids.clear();
		kids = null;

		return outs;
	}

	private boolean checkExpandIconHit(
		final boolean partiallyExpanded,
		final VisualItem item,
		final MouseEvent e,
		final double scale) {
		final double addition = (partiallyExpanded) ? EXPAND_ICON_SIZE * scale : 0;
		final boolean result = ((e.getX() > (item.getBounds().getX() * scale + ((1 * scale) + addition) - display.getDisplayX()))
			&& (e.getX()) < (item.getBounds().getX() * scale + ((EXPAND_ICON_SIZE * scale) + addition) - display.getDisplayX())
			&& (e.getY()) > item.getBounds().getY() * scale - display.getDisplayY() && (e.getY()) < item.getBounds().getY()
			* scale
			+ (EXPAND_ICON_SIZE * scale)
			- display.getDisplayY());
		return result;
	}

	private void loadModel(final Node node) {
		for (final Entry<IBeanProxy<Object>, Node> entry : nodeMap.entrySet()) {
			if (entry.getValue() == node) {
				final IBeanRelationNodeModel<Object, Object> beanRelationNodeModel = beanRelationMap.get(entry.getKey());
				for (final IEntityTypeId<Object> entityType : beanRelationNodeModel.getChildRelations()) {
					final IBeanRelationNodeModel<Object, Object> childRelationModel = relationTreeModel.getNode(
							beanRelationNodeModel.getChildEntityTypeId(),
							entry.getKey(),
							entityType);
					childRelationModel.loadIfNotYetDone();
					if (graph.getNodeCount() <= maxNodeCount) {
						loadChildren(childRelationModel);
					}
				}
				beanRelationNodeModel.loadIfNotYetDone();
				break;
			}
		}
		contractExpandNode(node);
		synchronized (vis) {
			vis.run("filter");
		}
	}

	private List<IBeanRelationNodeModel<Object, Object>> loadChildren(
		final IBeanRelationNodeModel<Object, Object> beanRelationNodeModel) {

		final List<IBeanRelationNodeModel<Object, Object>> childList = new LinkedList<IBeanRelationNodeModel<Object, Object>>();

		for (final IEntityTypeId<Object> entityType : beanRelationNodeModel.getChildRelations()) {
			final int childRelations = beanRelationNodeModel.getSize();
			for (int i = 0; i < childRelations; i++) {
				final IBeanRelationNodeModel<Object, Object> childRelationModel = relationTreeModel.getNode(
						beanRelationNodeModel.getChildEntityTypeId(),
						beanRelationNodeModel.getBean(i),
						entityType);
				if (!childRelationModel.loadIfNotYetDone()) {
					onBeansChanged(childRelationModel);
				}
				childList.add(childRelationModel);
			}
		}
		return childList;
	}

	private void contractExpandNode(final Node node) {

		Set<Node> nodes = getOutlinedNodes(node);
		addNodeToCache(node);

		if (node.get("expanded") == Expand.FULL) {

			final Iterator<?> result = nodes.iterator();
			while (result.hasNext()) {
				final Node out = (Node) result.next();

				if (out != node.getParent()) {
					if (out.get("expanded") == Expand.FULL) {
						out.set("expanded", out.get("expanded"));
						contractExpandNode(out);
					}
					else if (out.get("expanded") == Expand.PARTIALLY) {
						out.set("expanded", out.get("expanded"));
					}

					out.set("visible", false);
					out.set("expanded", Expand.NOT);
					contractParentNodes(out);
				}
			}
			node.set("expanded", Expand.NOT);
		}

		else if (node.get("expanded") == Expand.NOT) {
			final Iterator<?> result = nodes.iterator();
			while (result.hasNext()) {
				final Node out = (Node) result.next();
				if (out != node.getParent()) {
					out.set("visible", true);
					out.set("expanded", out.get("expanded") == Expand.PARTIALLY ? Expand.PARTIALLY : Expand.NOT);
				}
				if (expandedNodesCache.contains(out)) {
					contractExpandNode(out);
				}
				expandParents(out);

			}
			node.set("expanded", Expand.FULL);
		}

		nodes.clear();
		nodes = null;
		synchronized (vis) {
			runLayout();
			vis.run("filter");
		}
	}

	private void contractParentNodes(final Node node) {
		final Iterator<?> parent = node.inNeighbors();
		while (parent.hasNext()) {
			final Node result = (Node) parent.next();
			final Iterator<?> children = result.outNeighbors();
			boolean full = false;
			while (children.hasNext()) {
				final Node child = (Node) children.next();
				if (node != child) {
					full = !(Boolean) child.get("visible") ? full : true;
				}
			}
			if (full) {
				result.set("expanded", Expand.PARTIALLY);
			}
			else {
				result.set("expanded", Expand.NOT);
			}
		}
	}

	private void expandParents(final Node node) {
		final Iterator<?> parent = node.inNeighbors();
		while (parent.hasNext()) {
			final Node result = (Node) parent.next();
			final Iterator<?> children = result.outNeighbors();
			boolean full = true;
			while (children.hasNext()) {
				final Node child = (Node) children.next();
				if (node != child) {
					full = !(Boolean) child.get("visible") ? false : full;
				}
			}
			if (full) {
				result.set("expanded", Expand.FULL);
			}
			else {
				result.set("expanded", Expand.PARTIALLY);
			}
		}
	}

	private void addNodeToCache(final Node node) {

		if (expandedNodesCache.contains(node)) {
			return;
		}

		if (expandedNodesCache.size() > MAX_EXPANDED_NODES_CACHE) {
			final Node keyToRemove = expandedNodesCache.iterator().next();
			expandedNodesCache.remove(keyToRemove);
		}
		expandedNodesCache.remove(node);
		expandedNodesCache.add(node);

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
		runLayout();
	}

	private void addBeanToGraph(
		final IBeanProxy<Object> bean,
		final int index,
		final IBeanRelationNodeModel<Object, Object> beanRelationNodeModel) {

		final IBeanProxyLabelRenderer<Object> renderer = beanRelationNodeModel.getChildRenderer();
		final Node childNode;

		if (nodeMap.get(bean) == null) {
			childNode = graph.addNode();
			nodeMap.put(bean, childNode);
			childNode.set("visible", true);
			childNode.set("expanded", Expand.NOT);
			childNode.set("isParent", false);
			childNode.set("marked", false);
			beanRelationMap.put(bean, beanRelationNodeModel);
		}
		else {
			childNode = nodeMap.get(bean);
		}

		if (entityGroupMap.get(beanRelationNodeModel.getChildBeanType()) == null) {
			entityGroupMap.put(beanRelationNodeModel.getChildBeanType(), GRAPH_NODES_GROUP + groupCount);
			groupVisibilityMap.put(beanRelationNodeModel.getChildBeanType(), true);
			groupNames.put(GRAPH_NODES_GROUP + groupCount, beanRelationNodeModel.getText());
			groupCount = (groupCount < NODE_COLORS.length - 1) ? ++groupCount : 0;
		}
		childNode.set("beanrelation", beanRelationNodeModel.getChildBeanType());
		final String nodeGroup = entityGroupMap.get(beanRelationNodeModel.getChildBeanType());

		final Node parentNode = nodeMap.get(beanRelationNodeModel.getParentBean());

		if (parentNode != null && (!bean.isDummy())) {

			parentNode.set("visible", true);

			if (graph.getEdge(parentNode, childNode) == null && graph.getEdge(childNode, parentNode) == null) {

				final Edge edge = graph.addEdge(parentNode, childNode);
				edge.set("visible", false);
				edge.set("name", beanRelationNodeModel.getText());
				edgeVisibilityMap.put((String) edge.get("name"), true);
			}
			else {
				if (graph.getEdge(childNode, parentNode) != null) {
					final String previousString = (String) graph.getEdge(childNode, parentNode).get("name");
					if (!previousString.contains("/") && !previousString.equals(beanRelationNodeModel.getText())) {
						graph.getEdge(childNode, parentNode).set("name", previousString + " / " + beanRelationNodeModel.getText());
					}
					if (edgeVisibilityMap.containsKey(previousString)) {
						final boolean result = edgeVisibilityMap.get(previousString);
						edgeVisibilityMap.remove(previousString);
						edgeVisibilityMap.put((String) graph.getEdge(childNode, parentNode).get("name"), result);
					}
					else {
						edgeVisibilityMap.put(previousString, true);
					}
				}
				else if (graph.getEdge(parentNode, childNode) != null) {
					final String previousString = (String) graph.getEdge(parentNode, childNode).get("name");
					if (!previousString.contains("/") && !previousString.equals(beanRelationNodeModel.getText())) {
						graph.getEdge(parentNode, childNode).set("name", previousString + " / " + beanRelationNodeModel.getText());
					}
					if (edgeVisibilityMap.containsKey(previousString)) {
						final boolean result = edgeVisibilityMap.get(previousString);
						edgeVisibilityMap.remove(previousString);
						edgeVisibilityMap.put((String) graph.getEdge(parentNode, childNode).get("name"), result);
					}
					else {
						edgeVisibilityMap.put(previousString, true);
					}
				}
			}
		}

		if ((Boolean) childNode.get("visible")) {
			renderNodeShape(nodeGroup, childNode);
			renderNode(childNode, bean, renderer);
		}

		if (parentNode != null) {
			if (getOutlinedNodes(parentNode).size() >= 1) {
				parentNode.set("isParent", true);
			}
			else {
				parentNode.set("isParent", false);
			}
		}

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
				vis.runAfter("filter", "expand");
				vis.runAfter("filter", "expand");
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
				runLayout();
				vis.run("filter");
			}
		}
	}

	private class ExpandLevelNodeListener extends BeanListModelListenerAdapter<Object> {
		private final IBeanRelationNodeModel<Object, Object> relationNodeModel;
		private final int level;

		public ExpandLevelNodeListener(final IBeanRelationNodeModel<Object, Object> relationNodeModel, final int level) {
			this.relationNodeModel = relationNodeModel;
			this.level = level;
		}

		@Override
		public void beansChanged() {

			synchronized (vis) {
				if (checkFullyLoaded()) {
					if (level >= 0) {
						if (graph.getNodeCount() < maxNodeCount) {
							expandMapResult.clear();
							vis.run("expand");
						}
					}
				}
			}

			for (final Entry<IBeanProxy<Object>, IBeanRelationNodeModel<Object, Object>> entry : beanRelationMap.entrySet()) {
				if (entry.getValue() == relationNodeModel) {
					final Node result = nodeMap.get(entry.getKey());
					if (result != null
						&& (Integer) result.get("level") != null
						&& (Integer) result.get("level") < autoExpandLevel) {
						result.set("expanded", Expand.FULL);
					}
					else {
						result.set("expanded", Expand.NOT);
					}
				}
			}
		}

		private boolean checkFullyLoaded() {
			synchronized (expandMapResult) {
				if (expandMapResult.size() != 0) {
					final Iterator<IBeanRelationNodeModel<Object, Object>> it = expandMapResult.iterator();
					while (it.hasNext()) {
						final IBeanRelationNodeModel<Object, Object> elem = it.next();
						if (elem.hasExecutions()) {
							return false;
						}
					}
					return true;
				}
				return false;
			}
		}

	}

	private void addExpandLevelNodeListenerToChildren(
		final IBeanRelationNodeModel<Object, Object> beanRelationNodeModel,
		final int parentValue) {

		int decreaseLevel = parentValue;
		if (decreaseLevel >= 0) {
			decreaseLevel = decreaseLevel - 1;
			for (final IEntityTypeId<Object> entityType : beanRelationNodeModel.getChildRelations()) {
				final int childRelations = beanRelationNodeModel.getSize();
				for (int i = 0; i < childRelations; i++) {
					final IBeanRelationNodeModel<Object, Object> childRelationModel = relationTreeModel.getNode(
							beanRelationNodeModel.getChildEntityTypeId(),
							beanRelationNodeModel.getBean(i),
							entityType);

					if (!beanRelationMap.containsValue(childRelationModel)
						&& !childRelationModel.equals(relationTreeModel.getRoot())
						&& !childRelationModel.hasExecutions()) {
						final ExpandLevelNodeListener expandListener = new ExpandLevelNodeListener(
							childRelationModel,
							decreaseLevel);
						childRelationModel.addBeanListModelListener(expandListener);
						expandMapResult.add(childRelationModel);
						if (!childRelationModel.loadIfNotYetDone()) {
							childRelationModel.fireBeansChanged();
						}
					}
				}
			}
		}
	}

	private void renderNode(final Node node, final IBeanProxy<Object> bean, final IBeanProxyLabelRenderer<Object> renderer) {
		if (!bean.isDummy() && (Boolean) node.get("visible")) {
			renderNodeWithLabel(node, renderer.getLabel(bean));
			renderIcon(node, bean, renderer);
		}
	}

	private void switchNodeAnimation(final boolean animate) {
		vis.setValue(NODES, null, VisualItem.FIXED, animate);
		vis.setValue(EDGES, null, VisualItem.FIXED, animate);
	}

	private void renderIcon(final Node node, final IBeanProxy<Object> bean, final IBeanProxyLabelRenderer<Object> renderer) {
		final IImageConstant icon = renderer.getLabel(bean).getIcon();
		if (icon != null) {
			final IImageHandle imageHandle = Toolkit.getImageRegistry().getImageHandle(icon);
			if (imageHandle != null) {
				final Object image = imageHandle.getImage();
				final IImageDescriptor imageDescriptor = imageHandle.getImageDescriptor();
				if (image instanceof Image) {
					renderIcon(node, (Image) image);
				}
				else if (imageDescriptor != null) {
					IImageHandle awtImageHandle = SwingImageRegistry.getInstance().getImageHandle(icon);
					if (awtImageHandle == null) {
						SwingImageRegistry.getInstance().registerImageConstant(icon, imageDescriptor);
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

	public int getGroupCount() {
		return this.groupCount;
	}

	public static Node getMarkedNode() {
		return markedNode;
	}

	private ForceSimulator setForces() {
		final SpringForce springForce = new SpringForce(1E-4f, 250);
		final NBodyForce nBodyForce = new NBodyForce(-10, 320, 0);
		final DragForce dragForce = new DragForce(0.03f);

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
		maxNodeTextField = new InputControlItemModel<Integer>(textFieldBP, 30);
		maxNodeTextField.addInputListener(new IInputListener() {

			@Override
			public void inputChanged() {
				if (maxNodeTextField.getValue() != null) {
					maxNodeCount = maxNodeTextField.getValue();
					relationTreeModel.load();

				}
			}
		});
		model.addItem(maxNodeTextField);
		model.addSeparator();

		model.addTextLabel("Expand Level" + " ");

		final IComboBoxSelectionBluePrint<Integer> comboBoxExpandLevelBp = BPF.comboBoxSelectionIntegerNumber().setElements(
				0,
				1,
				2,
				3,
				4,
				5).autoCompletionOff();
		final InputControlItemModel<Integer> comboBoxExpandLevel = new InputControlItemModel<Integer>(comboBoxExpandLevelBp, 35);
		comboBoxExpandLevel.setValue(autoExpandLevel);
		comboBoxExpandLevel.addInputListener(new IInputListener() {

			@Override
			public void inputChanged() {
				synchronized (vis) {
					if (comboBoxExpandLevel.getValue() != null) {
						autoExpandLevel = comboBoxExpandLevel.getValue();
						vis.run("expand");
						vis.run("filter");
						runLayout();
					}
				}
			}
		});
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

		//SettingsDialog
		final ICommandAction settingsDialogAction = settingsDialogActionBuilder.build();

		final IComboBoxSelectionBluePrint<GraphLayout> comboBoxBp = BPF.comboBoxSelection(GraphLayout.values());

		comboBoxBp.setAutoCompletion(false);
		final InputControlItemModel<GraphLayout> comboBox = new InputControlItemModel<GraphLayout>(comboBoxBp, 130);
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

		//AnimationCheckedItem
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
						switchNodeAnimation(true);
					}
					else {
						checkItemModel.setText(Messages.getMessage("BeanRelationGraphImpl.animation.off").get());
						switchNodeAnimation(false);
					}
					on = !on;
				}
			}
		});

		final IActionBuilder groupFilterActionBuilder = actionBF.create();
		groupFilterActionBuilder.setIcon(IconsSmall.SETTINGS);
		groupFilterActionBuilder.setCommand(new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				if (settingsDialog != null) {
					settingsDialog.setVisible(false);
					settingsDialog.removeAll();
					settingsDialog.dispose();
					settingsDialog = null;
				}
				settingsDialog = new BeanGraphSettingsDialog(vis, groupVisibilityMap, edgeVisibilityMap);

				settingsDialog.setVisible(true);
				groupVisibilityMap = settingsDialog.updateGroupMap();
				edgeVisibilityMap = settingsDialog.updateEdgeMap();
			}
		});
		final ICommandAction groupFilterAction = groupFilterActionBuilder.build();

		model.addSeparator();

		model.addAction(settingsDialogAction);
		model.addAction(groupFilterAction);

		model.addSeparator();
		final ICheckedItemModel edgeCheckedItem = model.addCheckedItem("Edge Label");
		edgeCheckedItem.setIcon(CapIcons.GRAPH_LETTERING);
		edgeCheckedItem.setSelected(false);
		edgeCheckedItem.addItemListener(new IItemStateListener() {

			@Override
			public void itemStateChanged() {
				labelEdgeLayout.setEdgesVisible(edgeCheckedItem.isSelected());
				synchronized (vis) {
					vis.repaint();
				}
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

			final ForceDirectedLayout forceDirectedLayout = new ForceDirectedLayout(GRAPH, true);
			forceDirectedLayout.setForceSimulator(forceSimulator != null ? forceSimulator : setForces());

			final ActionList layout = new ActionList(Activity.INFINITY);
			layout.add(forceDirectedLayout);
			layout.add(labelEdgeLayout);
			layout.add(new RepaintAction(vis));

			vis.putAction("layout", layout);

			runLayout();
		}
	}

	private void initNodeLinkLayout() {
		synchronized (vis) {
			vis.removeAction("layout");
		}
		final ActionList layout = new ActionList(ACTIVITY_DEFAULT_TIME);
		final NodeLinkTreeLayout nodeLinkTreeLayout = new NodeLinkTreeLayout(GRAPH);
		nodeLinkTreeLayout.setRootNodeOffset(10);
		layout.add(nodeLinkTreeLayout);
		layout.add(labelEdgeLayout);
		layout.add(new RepaintAction(vis));
		vis.putAction("layout", layout);

		runLayout();
	}

	private void initRadialTreeLayout() {
		synchronized (vis) {
			vis.removeAction("layout");

			final ActionList layout = new ActionList(ACTIVITY_DEFAULT_TIME);
			radialTreeLayout = new RadialTreeLayout(GRAPH);
			radialTreeLayout.setAutoScale(false);
			radialTreeLayout.setRadiusIncrement(radialTreeLayout.getRadiusIncrement() * 2);
			layout.add(radialTreeLayout);
			//						layout.add(new RepaintAction(vis));
			layout.add(new TreeRootAction(GRAPH, vis));
			final CollapsedSubtreeLayout subLayout = new CollapsedSubtreeLayout(GRAPH);
			layout.add(subLayout);

			layout.add(labelEdgeLayout);

			final ActionList animate = new ActionList(1250);
			animate.setPacingFunction(new SlowInSlowOutPacer());
			animate.add(new QualityControlAnimator());
			animate.add(new VisibilityAnimator(GRAPH));
			animate.add(new PolarLocationAnimator(NODES, "linear"));
			animate.add(new ColorAnimator(NODES));
			animate.add(new RepaintAction(vis));

			display.setItemSorter(new TreeDepthItemSorter(false));

			vis.putAction("animate", animate);
			vis.putAction("layout", layout);

			runLayout();
		}

	}

	private void runLayout() {
		vis.run("layout");
		if (radialTreeLayout != null) {
			vis.run("animate");
		}
		vis.run("color");
	}

	private static void renderNodeWithLabel(final Node node, final ILabelModel label) {

		node.set("tooltip", label.getText());
		node.set(
				"name",
				(label.getText().length() > MAX_LABELTEXT_LENGTH) ? label.getText().substring(0, MAX_LABELTEXT_LENGTH - 1)
					+ "..." : label.getText());
		node.set("level", node.getDepth());
	}

	public static int getAutoExpandLevel() {
		return autoExpandLevel;
	}

	public IUiThreadAccess getUiThreadAccess() {
		return uiThreadAccess;
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

	private class ExpandLevelVisibilityAction extends GroupAction {

		public ExpandLevelVisibilityAction(final String group) {
			super(group);
		}

		@Override
		public void run(final double frac) {

			final TupleSet nodes = vis.getGroup(NODES);
			final Iterator<?> node = nodes.tuples();
			final List<Node> lastNodes = new LinkedList<Node>();
			int highestLevel = 0;
			while (node.hasNext()) {

				final VisualItem result = (VisualItem) node.next();
				if (getOutlinedNodes((Node) result.getSourceTuple()).size() == 0) {
					final int nodeLevel = result.get("level") != null ? (Integer) result.get("level") : autoExpandLevel;
					highestLevel = (highestLevel <= nodeLevel ? nodeLevel : highestLevel);
					lastNodes.add((Node) result);
				}

				if ((Integer) result.get("level") < autoExpandLevel) {
					result.set("expanded", Expand.FULL);
				}
				else {
					result.set("expanded", Expand.NOT);
				}

				if ((Integer) result.get("level") > autoExpandLevel) {
					result.set("visible", false);
				}
				else {
					result.set("visible", true);
				}
			}
			final Iterator<Node> itLastNodes = lastNodes.iterator();
			while (itLastNodes.hasNext()) {
				final Node end = itLastNodes.next();
				if ((Integer) (end.get("level")) < highestLevel) {
					itLastNodes.remove();
				}
			}

			if (highestLevel <= autoExpandLevel && graph.getNodeCount() < maxNodeCount) {
				final Iterator<?> itSecond = lastNodes.iterator();
				while (itSecond.hasNext()) {
					final VisualItem second = (VisualItem) itSecond.next();
					final IBeanRelationNodeModel<Object, Object> beanRelationNodeModel = getBeanRelationNodeModelFromNode((Node) second.getSourceTuple());
					if (beanRelationNodeModel != null) {
						final int level = (Integer) second.get("level");
						if (!getUiThreadAccess().isUiThread()) {
							getUiThreadAccess().invokeLater(new Runnable() {
								@Override
								public void run() {
									addExpandLevelNodeListenerToChildren(beanRelationNodeModel, autoExpandLevel - level);
								}
							});
						}
						else {
							addExpandLevelNodeListenerToChildren(beanRelationNodeModel, autoExpandLevel - level);
						}
					}
				}
			}
			runLayout();
			expandMapResult.clear();
		}

		private IBeanRelationNodeModel<Object, Object> getBeanRelationNodeModelFromNode(final Node result) {
			for (final Entry<IBeanProxy<Object>, Node> entry : nodeMap.entrySet()) {
				if (entry.getValue() == result) {
					final IBeanRelationNodeModel<Object, Object> beanRelationNodeModel = beanRelationMap.get(entry.getKey());
					return beanRelationNodeModel;
				}
			}
			return null;
		}
	}

	static enum Expand {

		FULL,
		PARTIALLY,
		NOT;
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
