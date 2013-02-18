/*
 * Copyright (c) 2013, sapalm
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

import java.awt.Font;
import java.util.HashMap;

import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.animate.PolarLocationAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.activity.Activity;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

public class ActionManager {

	private final ActionList filterActionList;
	private final ActionList neighborHighlightActionList;
	private final ActionList colorActionList;
	private final ActionList animationActionList;

	private final NodeMarkedAction nodeMarkedAction;

	private ColorAction nodeColor;

	public ActionManager(
		final Visualization vis,
		final HashMap<String, Boolean> groupVisibilityMap,
		final HashMap<String, Boolean> edgeVisibilityMap) {

		filterActionList = new ActionList();
		initializeFilterActionList(groupVisibilityMap, edgeVisibilityMap);

		neighborHighlightActionList = new ActionList(Activity.INFINITY);
		initializeHighlightActionList();

		colorActionList = new ActionList();
		initializeColorActionList();

		animationActionList = new ActionList(1250);
		initializeAnimationActionList();

		nodeMarkedAction = new NodeMarkedAction();

		vis.putAction("filter", filterActionList);
		vis.putAction("color", colorActionList);
		vis.putAction("neighbor", neighborHighlightActionList);
		vis.putAction("animation", animationActionList);
		vis.putAction("marked", nodeMarkedAction);
	}

	public void handleNeighborHighlighingAtFixedNodes(final boolean fix) {
		if (fix) {
			neighborHighlightActionList.remove(nodeColor);
		}
		else {
			neighborHighlightActionList.add(nodeColor);
		}
	}

	public boolean neighborActionIsRunning() {
		return this.neighborHighlightActionList.isRunning();
	}

	public ActionList getAnimationActionList() {
		return this.animationActionList;
	}

	public ActionList getNeighborHighlightActionList() {
		return this.neighborHighlightActionList;
	}

	public ActionList getFilterActionList() {
		return this.filterActionList;
	}

	private void initializeAnimationActionList() {
		animationActionList.setPacingFunction(new SlowInSlowOutPacer());
		animationActionList.add(new QualityControlAnimator());
		animationActionList.add(new PolarLocationAnimator(BeanRelationGraphImpl.NODES, "linear"));
		animationActionList.add(new RepaintAction());
	}

	private void initializeColorActionList() {
		colorActionList.add(new ColorAction(BeanRelationGraphImpl.NODES, VisualItem.TEXTCOLOR, ColorLib.rgb(0, 0, 0)));
		colorActionList.add(new FontAction(BeanRelationGraphImpl.NODES, new Font("Tahoma", Font.BOLD, 11)));
		colorActionList.add(new ColorAction(BeanRelationGraphImpl.EDGES, VisualItem.STROKECOLOR, ColorLib.gray(200)));

		final ColorAction fill = new ColorAction("graph.nodes", VisualItem.FILLCOLOR, ColorLib.rgb(200, 200, 255));
		for (int index = 0; index < BeanRelationGraphImpl.NODE_COLORS.length; index++) {
			fill.add("type == '" + BeanRelationGraphImpl.GRAPH_NODES_GROUP + index + "'", ColorLib.rgba(
					BeanRelationGraphImpl.NODE_COLORS[index][0],
					BeanRelationGraphImpl.NODE_COLORS[index][1],
					BeanRelationGraphImpl.NODE_COLORS[index][2],
					BeanRelationGraphImpl.NODE_COLORS[index][3]));
		}
		colorActionList.add(fill);
	}

	private void initializeFilterActionList(
		final HashMap<String, Boolean> groupVisibilityMap,
		final HashMap<String, Boolean> edgeVisibilityMap) {
		filterActionList.add(new NodeVisibilityAction(groupVisibilityMap));
		filterActionList.add(new EdgeVisibilityAction(edgeVisibilityMap));
		filterActionList.add(new RemoveStandaloneNodesAction(groupVisibilityMap));
	}

	private void initializeHighlightActionList() {
		nodeColor = new ColorAction(BeanRelationGraphImpl.NODES, VisualItem.TEXTCOLOR, ColorLib.rgb(0, 0, 0));
		nodeColor.add(VisualItem.HIGHLIGHT, ColorLib.rgb(16, 112, 41));
		nodeColor.add(VisualItem.FIXED, ColorLib.rgb(0, 11, 152));
		final ColorAction edgeColor = new ColorAction("graph.edges", VisualItem.STROKECOLOR, ColorLib.gray(200));
		edgeColor.add(VisualItem.HIGHLIGHT, ColorLib.rgb(16, 112, 41));
		neighborHighlightActionList.add(nodeColor);
		neighborHighlightActionList.add(edgeColor);
	}
}
