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
import java.awt.event.MouseEvent;
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
import org.jowidgets.common.types.Dimension;
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
import prefuse.action.animate.LocationAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.animate.VisibilityAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.activity.Activity;
import prefuse.activity.ActivityAdapter;
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
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.sort.TreeDepthItemSorter;

class BeanRelationGraphImpl<CHILD_BEAN_TYPE> extends ControlWrapper implements IBeanRelationGraph<CHILD_BEAN_TYPE> {

	public static final String GRAPH = "graph";
	public static final String NODES = "graph.nodes";
	public static final String EDGES = "graph.edges";
	public static final String GRAPH_NODES_GROUP = "graph.nodes.group";
	public static final String EDGE_DECORATORS = "edgeDeco";

	private static final int MAX_NODE_COUNT_DEFAULT = 100;
	private static final int MAX_LABELTEXT_LENGTH = 40;
	private static final int EXPAND_ICON_SIZE = 18;
	private static final int MAX_EXPANDED_NODES_CACHE = 500;

	private static final int[] NODE_COLORS = new int[] {
			ColorLib.gray(180), ColorLib.rgba(105, 176, 220, 255), ColorLib.rgba(191, 112, 97, 255),
			ColorLib.rgba(7, 162, 28, 255), ColorLib.rgba(192, 210, 0, 255), ColorLib.rgba(147, 129, 186, 255),
			ColorLib.rgba(217, 123, 82, 255), ColorLib.rgba(222, 127, 227, 255), ColorLib.rgba(239, 215, 143, 255),
			ColorLib.rgba(99, 241, 113, 255), ColorLib.rgba(79, 124, 36, 255), ColorLib.rgba(45, 84, 187, 255)};

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

	private final ImageFactory imageFactory;

	private IFrame dialog;

	private int maxNodeCount = MAX_NODE_COUNT_DEFAULT;
	private int groupCount;

	private final EdgeVisibilityAction edgeFilter;
	private final NodeVisibilityAction nodeFilter;
	private final RemoveStandaloneNodesAction removeStandaloneNodesFilter;
	private final IUiThreadAccess uiThreadAccess;
	private InputControlItemModel<Integer> maxNodeTextField;
	private ActionList animation;
	private RootModelListener rootModelListener;
	private ActionList filters;
	private ActionList color;
	private ExpandLevelVisibilityAction expand;
	private NodeMarkedAction marked;
	private LayoutManager layoutManager;
	private GraphLayout activeLayout;
	private InputControlItemModel<Integer> comboBoxExpandLevel;
	private EdgeRenderer edgeRenderer;

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
		rootModelListener = new RootModelListener();

		graph = new Graph();
		graph.addColumn("name", String.class);
		graph.addColumn("tooltip", String.class);
		graph.addColumn("image", Image.class);
		graph.addColumn("type", String.class);
		graph.addColumn("level", Integer.class);
		graph.addColumn("expanded", Expand.class);
		graph.addColumn("visible", Boolean.class);
		graph.addColumn("beanrelation", Object.class);
		graph.addColumn("isParent", Boolean.class);
		graph.addColumn("marked", Boolean.class);
		graph.addColumn("assigned", Integer.class);

		vis = new Visualization();
		vis.addGraph(GRAPH, graph);
		vis.setInteractive(EDGES, null, false);
		vis.setInteractive(NODES, null, true);

		expand = new ExpandLevelVisibilityAction(GRAPH);
		expand.addActivityListener(new ActivityAdapter() {

			@Override
			public void activityFinished(final Activity a) {
				runFilter();
			}

		});
		marked = new NodeMarkedAction();

		filters = new ActionList();
		nodeFilter = new NodeVisibilityAction();
		edgeFilter = new EdgeVisibilityAction(edgeVisibilityMap);
		removeStandaloneNodesFilter = new RemoveStandaloneNodesAction();

		filters.add(edgeFilter);
		filters.add(nodeFilter);
		filters.add(removeStandaloneNodesFilter);

		color = new ActionList();
		color.add(new ColorAction(NODES, VisualItem.TEXTCOLOR, ColorLib.rgb(0, 0, 0)));
		color.add(new FontAction(NODES, new Font("Tahoma", Font.BOLD, 10)));
		color.add(new ColorAction(EDGES, VisualItem.FILLCOLOR, ColorLib.gray(200)));
		color.add(new ColorAction(EDGES, VisualItem.STROKECOLOR, ColorLib.gray(200)));

		final ColorAction fill = new ColorAction(NODES, VisualItem.FILLCOLOR);
		for (int index = 0; index < NODE_COLORS.length; index++) {
			fill.add("type == '" + GRAPH_NODES_GROUP + index + "'", NODE_COLORS[index]);
		}
		color.add(fill);

		vis.putAction("filter", filters);
		vis.putAction("color", color);
		vis.putAction("expand", expand);
		vis.putAction("marked", marked);

		final NodeRenderer renderer = new NodeRenderer("name", "image");
		renderer.setHorizontalAlignment(Constants.CENTER);
		renderer.setVerticalAlignment(Constants.CENTER);

		imageFactory = new ImageFactory();
		renderer.setImageFactory(imageFactory);
		edgeRenderer = new EdgeRenderer("name");
		final DefaultRendererFactory rendererFactory = new DefaultRendererFactory(renderer);
		rendererFactory.add(new InGroupPredicate(EDGE_DECORATORS), edgeRenderer);
		vis.setRendererFactory(rendererFactory);

		vis.addDecorators(EDGE_DECORATORS, EDGES, DECORATOR_SCHEMA);

		layoutManager = new LayoutManager(vis);
		layoutManager.getLabelEdgeLayout().setEdgesVisible(false);
		activeLayout = layoutManager.getLayout(GraphLayout.FORCE_DIRECTED_LAYOUT);
		final Action anim = vis.putAction("animate", initializeAnimationList());
		anim.addActivityListener(new ActivityAdapter() {

			@Override
			public void activityFinished(final Activity a) {
				if (layoutManager != null) {
					if (activeLayout != GraphLayout.FORCE_DIRECTED_LAYOUT) {
						layoutManager.assignNodes(true);
					}
				}
			}
		});

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

				if (item.isInGroup(EDGES)) {
					return;
				}

				final double scale = display.getScale();

				if ((Boolean) item.get("isParent")) {

					if (item.get("expanded") == Expand.PARTIALLY) {
						if (checkExpandIconHit(false, item, e, scale)) {
							item.set("expanded", Expand.FULL);
							loadModel(node);
							runFilter();
							return;

						}
						else if (checkExpandIconHit(true, item, e, scale)) {
							item.set("expanded", Expand.NOT);
							loadModel(node);
							runFilter();
							return;
						}
					}
					else if (item.get("expanded") == Expand.FULL || item.get("expanded") == Expand.NOT) {
						if (checkExpandIconHit(false, item, e, scale)) {
							loadModel(node);
							runFilter();
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

				vis.run("marked");
				runFilter();
				runLayout(true);
			}

		});

		final ToolTipControl ttc = new ToolTipControl("tooltip");
		display.addControlListener(ttc);
		display.setItemSorter(new TreeDepthItemSorter(false));

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
					if (swingContainer.isShowing()) {
						relationTreeModel.getRoot().addBeanListModelListener(rootModelListener);
						relationTreeModel.getRoot().fireBeansChanged();
					}
					else if (!swingContainer.isShowing()) {
						relationTreeModel.getRoot().removeBeanListModelListener(rootModelListener);
						relationTreeModel.getRoot().fireBeansChanged();
						if (dialog != null) {
							dialog.setVisible(false);
							dialog.dispose();
						}
					}
				}
			}
		});

		toolbar.setModel(initToolBar());

	}

	private ActionList initializeAnimationList() {
		if (animation == null) {
			animation = new ActionList(1250);
			//			final CollapsedSubtreeLayout subLayout = new CollapsedSubtreeLayout(GRAPH);
			animation.setPacingFunction(new SlowInSlowOutPacer());
			animation.add(new VisibilityAnimator(GRAPH));
			animation.add(new QualityControlAnimator());
			//			animation.add(subLayout);
			animation.add(new LocationAnimator());
			animation.add(new RepaintAction());
		}
		return animation;
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
		contractExpandNodes(node);
	}

	private List<IBeanRelationNodeModel<Object, Object>> loadChildren(
		final IBeanRelationNodeModel<?, Object> beanRelationNodeModel) {

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

	//TODO CONTRACT

	private void contractExpandNodes(final Node node) {
		if (node.get("expanded") == Expand.FULL) {

			final Set<Node> outNodes = setEdges(node, false);
			node.set("expanded", Expand.NOT);

			final Iterator<Node> iteratorOutNodes = outNodes.iterator();
			while (iteratorOutNodes.hasNext()) {
				final Node elem = iteratorOutNodes.next();
				contractExpandNodes(elem, false);
				setNeighborEdges(elem, false);
				checkExpandLevel(elem);
			}
		}

		else if (node.get("expanded") == Expand.NOT) {
			final Set<Node> outNodes = setEdges(node, true);
			node.set("expanded", Expand.FULL);

			final Iterator<Node> iteratorNodes = outNodes.iterator();
			while (iteratorNodes.hasNext()) {
				final Node elem = iteratorNodes.next();
				setNeighborEdges(elem, true);
				checkExpandLevel(elem);
			}
		}
	}

	private void contractExpandNodes(final Node node, final boolean contract) {

		final Set<Node> outNodes = setEdges(node, contract);
		node.set("expanded", Expand.NOT);

		final Iterator<Node> iteratorOutNodes = outNodes.iterator();
		while (iteratorOutNodes.hasNext()) {
			contractExpandNodes(iteratorOutNodes.next(), contract);
		}
	}

	private Set<Node> setEdges(final Node node, final boolean visible) {
		final Set<Node> outNodes = new HashSet<Node>();

		final Iterator<?> outEdges = node.outEdges();
		while (outEdges.hasNext()) {
			final Edge out = (Edge) outEdges.next();
			outNodes.add(out.getTargetNode());
			out.set("visible", visible);
		}
		return outNodes;
	}

	private void setNeighborEdges(final Node node, final boolean visible) {
		final Iterator<?> iteratorAllEdges = node.inEdges();
		while (iteratorAllEdges.hasNext()) {
			final Edge elem = (Edge) iteratorAllEdges.next();
			if ((Boolean) elem.getSourceNode().get("visible")) {
				elem.set("visible", visible);
			}
		}
	}

	private void checkExpandLevel(final Node node) {
		final Iterator<?> parents = node.inNeighbors();
		while (parents.hasNext()) {
			final Node parent = (Node) parents.next();
			final int outEdges = parent.getOutDegree();
			int count = 0;
			final Iterator<?> itEdges = parent.outEdges();
			while (itEdges.hasNext()) {
				if ((Boolean) ((Edge) itEdges.next()).get("visible")) {
					count++;
				}
			}
			if (count == 0) {
				parent.set("expanded", Expand.NOT);
			}
			else if (count > 0 && count < outEdges) {
				parent.set("expanded", Expand.PARTIALLY);
			}
			else if (count == outEdges) {
				parent.set("expanded", Expand.FULL);
			}
		}
	}

	@SuppressWarnings("unused")
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

	private boolean onBeansChanged(final IBeanRelationNodeModel<Object, Object> relationNodeModel) {

		boolean runFilter = false;
		for (int i = 0; i < relationNodeModel.getSize(); i++) {
			final IBeanProxy<Object> bean = relationNodeModel.getBean(i);
			synchronized (graph) {
				if (graph.getNodeCount() < maxNodeCount && !(bean.isDummy())) {
					addBeanToGraph(bean, i + nodeMap.size(), relationNodeModel);
					runFilter = true;
				}
			}
		}
		runLayout(true);
		return runFilter;
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
			childNode.set("assigned", 0);
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

			synchronized (vis) {
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
							graph.getEdge(childNode, parentNode).set(
									"name",
									previousString + " / " + beanRelationNodeModel.getText());
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
							graph.getEdge(parentNode, childNode).set(
									"name",
									previousString + " / " + beanRelationNodeModel.getText());
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
		}

		if ((Boolean) childNode.get("visible")) {
			synchronized (vis) {
				renderNodeShape(nodeGroup, childNode);
				renderNode(childNode, bean, renderer);

			}
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
			if (nodeMap.size() != 0) {
				nodeMap.clear();
				synchronized (vis) {
					graph.clear();
					layoutManager.resetLayout();
					return;
				}
			}
			onBeansChanged(root);
			vis.run("expand");
		}
	}

	private final class ChildModelListener extends BeanListModelListenerAdapter<Object> {

		private final IBeanRelationNodeModel<Object, Object> relationNodeModel;

		public ChildModelListener(final IBeanRelationNodeModel<Object, Object> relationNodeModel) {
			this.relationNodeModel = relationNodeModel;
		}

		@Override
		public void beansChanged() {
			if (onBeansChanged(relationNodeModel)) {
				synchronized (vis) {
					runFilter();
				}
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
			if (level >= 0) {
				if (checkFullyLoaded()) {
					synchronized (vis) {
						expandMapResult.clear();
						vis.run("expand");
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
						if (!expandMapResult.contains(childRelationModel)) {
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

	//TODO SP INITTOOLBAR
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
		comboBoxExpandLevel = new InputControlItemModel<Integer>(comboBoxExpandLevelBp, 35);
		comboBoxExpandLevel.setValue(autoExpandLevel);
		comboBoxExpandLevel.addInputListener(new IInputListener() {

			@Override
			public void inputChanged() {
				if (comboBoxExpandLevel.getValue() != null) {
					autoExpandLevel = comboBoxExpandLevel.getValue();
					runLayout(true);
					synchronized (vis) {
						vis.run("expand");
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
					dialog = new GraphSettingsDialog(activeLayout, layoutManager);
				}
				dialog.setVisible(true);
				dialog = null;
			}
		});

		final ICommandAction settingsDialogAction = settingsDialogActionBuilder.build();

		final IComboBoxSelectionBluePrint<GraphLayout> comboBoxBp = BPF.comboBoxSelection(GraphLayout.values());

		comboBoxBp.setAutoCompletion(false);
		final InputControlItemModel<GraphLayout> comboBox = new InputControlItemModel<GraphLayout>(comboBoxBp, 130);
		comboBox.addInputListener(new IInputListener() {

			@Override
			public void inputChanged() {
				final GraphLayout value = comboBox.getValue();
				for (final GraphLayout elem : GraphLayout.values()) {
					if (elem == value) {
						settingsDialogAction.setEnabled(true);
						activeLayout = layoutManager.getLayout(elem);
					}
				}
				if (layoutManager.getLabelEdgeLayout() != null) {
					layoutManager.getLabelEdgeLayout().run();
				}
				layoutManager.assignAnchorPoint(display, activeLayout);
				runLayout(true);
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

				settingsDialog.setMinPackSize(new Dimension(400, 300));
				settingsDialog.setMaxPackSize(new Dimension(400, 300));
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
				synchronized (vis) {
					layoutManager.getLabelEdgeLayout().setEdgesVisible(edgeCheckedItem.isSelected());
					runLayout(true);
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

	public void runLayout(final boolean updateAnchor) {
		synchronized (vis) {
			if (graph.getTupleCount() > 0) {
				if (updateAnchor) {
					layoutManager.updateAnchorPoint(activeLayout, display);
				}
				vis.run("layout");

				if (animation != null) {
					vis.run("animate");
				}
				color.run();
			}
		}
	}

	private void runFilter() {
		if (graph.getTupleCount() > 0) {
			vis.run("filter");
		}
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

	@SuppressWarnings("unused")
	private int getVisibleNodeCount() {
		synchronized (vis) {
			int count = 0;
			final Iterator<?> it = vis.visibleItems(NODES);
			while (it.hasNext()) {
				count++;
				it.next();
			}
			return count;
		}
	}

	public IUiThreadAccess getUiThreadAccess() {
		return uiThreadAccess;
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
			final Iterator<?> itNodes = nodes.tuples();
			final List<Node> lastNodes = new LinkedList<Node>();
			int highestLevel = 0;
			while (itNodes.hasNext()) {

				final Node node = (Node) itNodes.next();
				final VisualItem result = (VisualItem) node;
				if (getOutlinedNodes(node).size() == 0) {
					final int nodeLevel = result.get("level") != null ? (Integer) result.get("level") : autoExpandLevel;
					highestLevel = (highestLevel <= nodeLevel ? nodeLevel : highestLevel);
					lastNodes.add(node);
				}

				if ((Integer) node.get("level") != null) {
					if ((Integer) node.get("level") < autoExpandLevel) {
						result.set("expanded", Expand.FULL);
						setEdges(node, true);
						result.set("visible", true);
					}
					else {
						result.set("expanded", Expand.NOT);
						setEdges(node, false);
						result.set("visible", false);
					}
				}
			}
			final Iterator<Node> itLastNodes = lastNodes.iterator();
			while (itLastNodes.hasNext()) {
				final Node end = itLastNodes.next();
				if ((Integer) (end.get("level")) != null) {
					if ((Integer) end.get("level") < highestLevel) {
						itLastNodes.remove();
					}
				}
			}

			if ((highestLevel <= autoExpandLevel) && graph.getNodeCount() < maxNodeCount) {
				final Iterator<?> itSecond = lastNodes.iterator();
				while (itSecond.hasNext()) {
					final VisualItem second = (VisualItem) itSecond.next();
					final IBeanRelationNodeModel<Object, Object> beanRelationNodeModel = getBeanRelationNodeModelFromNode((Node) second.getSourceTuple());
					if (beanRelationNodeModel != null) {
						if (second.get("level") != null) {
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
			}
			runLayout(true);
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

	static enum GraphLayout {

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