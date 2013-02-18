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
	private ForceSimulator forceSimulator;
	private ActionList layout;

	private NodeLinkTreeLayout nodeLinkTreeLayout;
	private RadialTreeLayout radialTreeLayout;
	private ForceDirectedLayout forceDirectedLayout;

	private Point anchorPoint;

	LayoutManager(final Visualization vis) {
		this.vis = vis;
		forceSimulator = setForces();
		labelEdgeLayout = new LabelEdgeLayout(vis);
	}

	private ActionList initForceDLayout() {

		forceDirectedLayout = new ForceDirectedLayout("graph", true);
		forceDirectedLayout.setForceSimulator(forceSimulator != null ? forceSimulator : setForces());
		layout = new ActionList(Activity.INFINITY);
		layout.add(forceDirectedLayout);
		layout.add(labelEdgeLayout);
		layout.add(new RepaintAction(vis));

		return layout;

	}

	private ActionList initNodeLinkLayout() {
		layout = new ActionList(Activity.DEFAULT_STEP_TIME);
		nodeLinkTreeLayout = new NodeLinkTreeLayout("graph", Constants.ORIENT_LEFT_RIGHT, nodeLinkTreeLayout != null
				? nodeLinkTreeLayout.getDepthSpacing() : DEFAULT_NODELINKLAYOUT_DEPTH, nodeLinkTreeLayout != null
				? nodeLinkTreeLayout.getBreadthSpacing() : DEFAULT_NODELINKLAYOUT_BREADTH, nodeLinkTreeLayout != null
				? nodeLinkTreeLayout.getSubtreeSpacing() : DEFAULT_NODELINKLAYOUT_SUBTREE);
		nodeLinkTreeLayout.setRootNodeOffset(DEFAULT_NODELINKLAYOUT_ROOTNODEOFFSET);
		layout.add(nodeLinkTreeLayout);
		layout.add(labelEdgeLayout);
		layout.add(new RepaintAction(vis));
		return layout;
	}

	private ActionList initRadialTreeLayout() {

		layout = new ActionList(Activity.DEFAULT_STEP_TIME);
		radialTreeLayout = new RadialTreeLayout("graph", radialTreeLayout != null
				? (int) radialTreeLayout.getRadiusIncrement() : DEFAULT_RADIALLAYOUT_RADIUS);
		radialTreeLayout.setAutoScale(false);

		layout.add(new TreeRootAction("graph", vis));
		layout.add(radialTreeLayout);
		layout.add(labelEdgeLayout);
		layout.add(new RepaintAction(vis));
		return layout;

	}

	public void assignNodes(final boolean first) {

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

	public void updateAnchorPoint(final GraphLayout type, final Display display) {
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

	public void assignAnchorPoint(final Display display, final GraphLayout layout) {
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

	public GraphLayout getLayout(final GraphLayout type) {
		synchronized (vis) {
			vis.removeAction("layout");
			switch (type) {
				case FORCE_DIRECTED_LAYOUT:
					layout = initForceDLayout();
					break;
				case NODE_TREE_LINK_LAYOUT:
					layout = initNodeLinkLayout();
					break;
				case RADIAL_TREE_LAYOUT:
					layout = initRadialTreeLayout();
					break;
				default:
					layout = null;
					break;
			}
			vis.putAction("layout", layout);
		}
		return type;
	}

	public void resetLayout() {
		this.layout = null;
	}

	public void resetNodePositions() {
		final Iterator<?> iterator = vis.items(BeanRelationGraphImpl.NODES);
		while (iterator.hasNext()) {
			final Node node = (Node) iterator.next();
			node.set("position", null);
		}
	}

	public LabelEdgeLayout getLabelEdgeLayout() {
		return this.labelEdgeLayout;
	}

	public ForceSimulator getForceSimulator() {
		return this.forceSimulator;
	}

	public void setRadialRadius(final double radius) {
		this.radialTreeLayout.setRadiusIncrement(radius);
	}

	public double getRadialRadius() {
		return this.radialTreeLayout.getRadiusIncrement();
	}

	public void setNodeLinkLayoutRoot(final Node node) {
		this.nodeLinkTreeLayout.setLayoutRoot((NodeItem) node);
	}

	public void setRadialTreeLayoutRoot(final Node node) {
		this.radialTreeLayout.setLayoutRoot((NodeItem) node);
	}

	public void setNodeLinkedDistance(final double distance) {
		this.nodeLinkTreeLayout.setDepthSpacing(distance);
	}

	public void setNodeLinkedNeighborDistance(final double distance) {
		this.nodeLinkTreeLayout.setBreadthSpacing(distance);
	}

	public void setNodeLinkedSubtreeDistance(final double distance) {
		this.nodeLinkTreeLayout.setSubtreeSpacing(distance);
	}

	public double[] getNodeLinkedDistances() {
		return new double[] {
				this.nodeLinkTreeLayout.getDepthSpacing(), this.nodeLinkTreeLayout.getBreadthSpacing(),
				this.nodeLinkTreeLayout.getSubtreeSpacing()};
	}

	public String[] getNodeLinkedForces() {
		return new String[] {"Level", "Neighbor", "Subtree"};
	}

	public void runActiveLayout() {
		this.vis.run("layout");
	}
}
