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

import java.util.HashMap;
import java.util.Iterator;

import org.jowidgets.cap.addons.widgets.graph.impl.swing.common.BeanRelationGraphImpl.Expand;

import prefuse.action.GroupAction;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualItem;

class EdgeVisibilityAction extends GroupAction {

	private HashMap<String, Boolean> edgeMap;

	EdgeVisibilityAction(final HashMap<String, Boolean> edgeVisibilityGroup) {
		super();
		this.edgeMap = edgeVisibilityGroup;
	}

	@Override
	public void run(final double frac) {

		final TupleSet edges = m_vis.getGroup(BeanRelationGraphImpl.EDGES);
		final Iterator<?> edge = edges.tuples();
		while (edge.hasNext()) {
			final Edge test = (Edge) edge.next();
			if (test != null) {
				if (test.getTargetNode() != null) {
					final Node childNode = test.getTargetNode();
					final Node parentNode = test.getSourceNode();

					final VisualItem result = (VisualItem) test;

					if ((Boolean) parentNode.get("visible")
						&& parentNode.get("expanded") == Expand.FULL
						&& edgeMap != null
						&& edgeMap.containsKey(test.get("name"))) {
						test.set("visible", edgeMap.get(test.get("name")));
					}

					else if (!(Boolean) childNode.get("visible") || !(Boolean) parentNode.get("visible")) {
						test.set("visible", false);
					}
					else if (edgeMap != null) {
						if (edgeMap.containsKey(test.get("name"))) {
							test.set("visible", edgeMap.get(test.get("name")));
						}
						else {
							test.set("visible", true);
						}
					}
					result.setVisible((Boolean) test.get("visible"));
				}
			}
		}
	}

	public void updateEdgeMap(final HashMap<String, Boolean> edgeMap) {
		this.edgeMap = edgeMap;
	}
}
