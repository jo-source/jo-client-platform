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

import org.jowidgets.cap.addons.widgets.graph.impl.swing.common.BeanRelationGraphImpl.GraphLayout;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.activity.Activity;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;

class LayoutManager {

	private final LabelEdgeLayout labelEdgeLayout;

	private final Visualization vis;
	private ForceSimulator forceSimulator;
	private ActionList layout;

	private NodeLinkTreeLayout nodeLinkTreeLayout;
	private RadialTreeLayout radialTreeLayout;

	private Point anchorPoint;

	LayoutManager(final Visualization vis) {
		this.vis = vis;
		forceSimulator = setForces();
		labelEdgeLayout = new LabelEdgeLayout(vis);
	}

	private ActionList initForceDLayout() {

		final ForceDirectedLayout forceDirectedLayout = new ForceDirectedLayout("graph", true);
		forceDirectedLayout.setForceSimulator(forceSimulator != null ? forceSimulator : setForces());

		layout = new ActionList(Activity.INFINITY);
		layout.add(forceDirectedLayout);
		layout.add(labelEdgeLayout);
		layout.add(new RepaintAction(vis));

		return layout;

	}

	private ActionList initNodeLinkLayout() {
		layout = new ActionList(Activity.DEFAULT_STEP_TIME);
		nodeLinkTreeLayout = new NodeLinkTreeLayout("graph");
		nodeLinkTreeLayout.setRootNodeOffset(120);
		layout.add(nodeLinkTreeLayout);
		layout.add(labelEdgeLayout);
		layout.add(new RepaintAction(vis));
		return layout;
	}

	private ActionList initRadialTreeLayout() {

		layout = new ActionList(Activity.DEFAULT_STEP_TIME);
		radialTreeLayout = new RadialTreeLayout("graph", 100);
		radialTreeLayout.setAutoScale(false);
		radialTreeLayout.setRadiusIncrement(120);
		layout.add(new TreeRootAction("graph", vis));
		layout.add(radialTreeLayout);
		layout.add(labelEdgeLayout);
		layout.add(new RepaintAction(vis));
		return layout;

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

	public LabelEdgeLayout getLabelEdgeLayout() {
		return this.labelEdgeLayout;
	}

	public ForceSimulator getForceSimulator() {
		return this.forceSimulator;
	}
}
