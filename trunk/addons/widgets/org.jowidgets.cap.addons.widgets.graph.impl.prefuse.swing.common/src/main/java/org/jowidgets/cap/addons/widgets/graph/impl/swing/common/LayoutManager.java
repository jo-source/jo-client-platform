/*
 * Copyright (c) 2012, sapalm
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

import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;

import org.jowidgets.cap.addons.widgets.graph.impl.swing.common.BeanRelationGraphImpl.GraphLayout;
import org.jowidgets.util.Assert;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.activity.Activity;
import prefuse.data.Node;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

class LayoutManager {

	private static final int DEFAULT_RADIALLAYOUT_RADIUS = 200;
	private static final int DEFAULT_NODELINKLAYOUT_DEPTH = 50;
	private static final int DEFAULT_NODELINKLAYOUT_BREADTH = 5;
	private static final int DEFAULT_NODELINKLAYOUT_SUBTREE = 25;
	private static final int DEFAULT_NODELINKLAYOUT_ROOTNODEOFFSET = 120;

	private final LabelEdgeLayout labelEdgeLayout;
	private final Visualization vis;
	private final NodeLinkTreeLayout nodeLinkTreeLayout;

	private final RadialTreeLayout radialTreeLayout;
	private ForceDirectedLayout forceDirectedLayout;

	private Point anchorPoint;
	private final ForceSimulator forceSimulator;

	LayoutManager(final Visualization vis) {
		Assert.paramNotNull(vis, "vis");
		this.vis = vis;
		labelEdgeLayout = new LabelEdgeLayout(vis);
		forceSimulator = createForceSimulator();
		nodeLinkTreeLayout = createNodeLinkTreeLayout();
		radialTreeLayout = createRadialTreeLayout();
		forceDirectedLayout = createForceDirectedLayout(forceSimulator);
	}

	private static NodeLinkTreeLayout createNodeLinkTreeLayout() {
		final NodeLinkTreeLayout result = new NodeLinkTreeLayout(
			"graph",
			Constants.ORIENT_LEFT_RIGHT,
			DEFAULT_NODELINKLAYOUT_DEPTH,
			DEFAULT_NODELINKLAYOUT_BREADTH,
			DEFAULT_NODELINKLAYOUT_SUBTREE);
		result.setRootNodeOffset(DEFAULT_NODELINKLAYOUT_ROOTNODEOFFSET);
		return result;
	}

	private static RadialTreeLayout createRadialTreeLayout() {
		final RadialTreeLayout result = new RadialTreeLayout("graph", DEFAULT_RADIALLAYOUT_RADIUS);
		result.setAutoScale(false);
		return result;
	}

	private static ForceDirectedLayout createForceDirectedLayout(final ForceSimulator forceSimulator) {
		final ForceDirectedLayout result = new ForceDirectedLayout("graph", true);
		result.setForceSimulator(forceSimulator);
		return result;
	}

	private static ForceSimulator createForceSimulator() {
		final ForceSimulator result = new ForceSimulator();
		result.addForce(new DragForce(0.03f));
		result.addForce(new NBodyForce(-10, 320, 0));
		result.addForce(new SpringForce(1E-4f, 250));
		return result;
	}

	private ActionList createForceDLayoutActionList() {
		forceDirectedLayout = new ForceDirectedLayout("graph", true);
		forceDirectedLayout.setForceSimulator(forceSimulator);
		final ActionList result = new ActionList(Activity.INFINITY);
		result.add(forceDirectedLayout);
		result.add(labelEdgeLayout);
		result.add(new RepaintAction(vis));
		return result;
	}

	private ActionList createNodeLinkLayoutActionList() {
		final ActionList result = new ActionList(Activity.DEFAULT_STEP_TIME);
		result.add(nodeLinkTreeLayout);
		result.add(labelEdgeLayout);
		result.add(new RepaintAction(vis));
		return result;
	}

	private ActionList createRadialTreeLayoutActionList() {
		final ActionList result = new ActionList(Activity.DEFAULT_STEP_TIME);
		result.add(new TreeRootAction("graph", vis));
		result.add(radialTreeLayout);
		result.add(labelEdgeLayout);
		result.add(new RepaintAction(vis));
		return result;
	}

	void assignNodes(final boolean first) {
		final Iterator<?> iterator = vis.visibleItems("graph.nodes");
		final LinkedList<VisualItem> boundaries = new LinkedList<VisualItem>();
		while (iterator.hasNext()) {
			final VisualItem item = (VisualItem) iterator.next();
			final Iterator<VisualItem> it = boundaries.iterator();
			while (it.hasNext()) {
				final VisualItem elem = it.next();
				if (item.getBounds().intersects(elem.getBounds())) {
					double diffX = 0;
					double diffY = 0;
					if (item.getX() > elem.getX()) {
						diffX = (elem.getX() + (elem.getBounds().getWidth() / 2))
							- (item.getX() - (item.getBounds().getWidth() / 2));
					}
					else if (item.getX() < elem.getX()) {
						diffX = (item.getX() + (item.getBounds().getWidth() / 2))
							- (elem.getX() - (elem.getBounds().getWidth() / 2));
					}

					if (item.getY() > elem.getY()) {
						diffY = (elem.getY() + (elem.getBounds().getHeight() / 2))
							- (item.getY() - (item.getBounds().getHeight() / 2));

					}
					else if (item.getY() < elem.getY()) {
						diffY = (item.getY() + (item.getBounds().getHeight() / 2))
							- (elem.getY() - (elem.getBounds().getHeight() / 2));
					}

					if (diffX < diffY || diffX == 0.0) {
						if (item.getX() >= elem.getX()) {
							item.setX(item.getX() + diffX);
						}
						else {
							item.setX(item.getX() - diffX);
						}
					}
					else if (diffX > diffY || diffY == 0.0) {
						if (item.getY() > elem.getY()) {
							item.setY(item.getY() + diffY);
						}
						else {
							item.setY(item.getY() - diffY);
						}
					}
				}
			}
			boundaries.add(item);
		}
		boundaries.clear();
		vis.repaint();
		if (first) {
			assignNodes(false);
		}
	}

	@SuppressWarnings("unused")
	private void setFixedPosition() {
		final Iterator<?> iteratorVisNodes = vis.visibleItems(BeanRelationGraphImpl.NODES);
		while (iteratorVisNodes.hasNext()) {
			final Node node = (Node) iteratorVisNodes.next();
			if (node.get("position") != null) {
				final VisualItem item = (VisualItem) node;
				item.setX(((Point) node.get("position")).x);
				item.setY(((Point) node.get("position")).y);
			}
		}
		vis.repaint();
	}

	void updateAnchorPoint(final GraphLayout type, final Display display) {
		switch (type) {
			case NODE_TREE_LINK_LAYOUT:
				nodeLinkTreeLayout.setLayoutAnchor(anchorPoint);
				break;
			case RADIAL_TREE_LAYOUT:
				radialTreeLayout.setLayoutAnchor(anchorPoint);
				break;
			default:
				forceDirectedLayout.setLayoutAnchor(anchorPoint);
				break;
		}
	}

	void assignAnchorPoint(final Display display, final GraphLayout layout) {
		switch (layout) {
			case RADIAL_TREE_LAYOUT:
				anchorPoint = new Point((int) display.getDisplayX() + (display.getWidth() / 2), (int) display.getDisplayY()
					+ (display.getHeight() / 2));
				break;
			case NODE_TREE_LINK_LAYOUT:
				anchorPoint = new Point((int) display.getDisplayX() + (display.getWidth() / 8), (int) display.getDisplayY()
					+ (display.getHeight() / 2));
				break;
			default:
				anchorPoint = new Point((int) display.getDisplayX() + (display.getWidth() / 2), (int) display.getDisplayY()
					+ (display.getHeight() / 2));
				break;
		}
	}

	void setLayout(final GraphLayout type) {
		synchronized (vis) {
			vis.removeAction("layout");
			final ActionList layout;
			switch (type) {
				case FORCE_DIRECTED_LAYOUT:
					layout = createForceDLayoutActionList();
					break;
				case NODE_TREE_LINK_LAYOUT:
					layout = createNodeLinkLayoutActionList();
					break;
				case RADIAL_TREE_LAYOUT:
					layout = createRadialTreeLayoutActionList();
					break;
				default:
					layout = null;
					break;
			}
			vis.putAction("layout", layout);
		}
	}

	void resetNodePositions() {
		final Iterator<?> iterator = vis.items(BeanRelationGraphImpl.NODES);
		while (iterator.hasNext()) {
			final Node node = (Node) iterator.next();
			node.set("position", null);
		}
	}

	LabelEdgeLayout getLabelEdgeLayout() {
		return this.labelEdgeLayout;
	}

	ForceSimulator getForceSimulator() {
		return this.forceSimulator;
	}

	void setRadialRadius(final double radius) {
		this.radialTreeLayout.setRadiusIncrement(radius);
	}

	double getRadialRadius() {
		return this.radialTreeLayout.getRadiusIncrement();
	}

	void setNodeLinkLayoutRoot(final Node node) {
		this.nodeLinkTreeLayout.setLayoutRoot((NodeItem) node);
	}

	void setRadialTreeLayoutRoot(final Node node) {
		this.radialTreeLayout.setLayoutRoot((NodeItem) node);
	}

	void setNodeLinkedDistance(final double distance) {
		this.nodeLinkTreeLayout.setDepthSpacing(distance);
	}

	void setNodeLinkedNeighborDistance(final double distance) {
		this.nodeLinkTreeLayout.setBreadthSpacing(distance);
	}

	void setNodeLinkedSubtreeDistance(final double distance) {
		this.nodeLinkTreeLayout.setSubtreeSpacing(distance);
	}

	double[] getNodeLinkedDistances() {
		return new double[] {
				this.nodeLinkTreeLayout.getDepthSpacing(), this.nodeLinkTreeLayout.getBreadthSpacing(),
				this.nodeLinkTreeLayout.getSubtreeSpacing()};
	}

	String[] getNodeLinkedForces() {
		return new String[] {"Level", "Neighbor", "Subtree"};
	}

	void runActiveLayout() {
		this.vis.run("layout");
	}
}
