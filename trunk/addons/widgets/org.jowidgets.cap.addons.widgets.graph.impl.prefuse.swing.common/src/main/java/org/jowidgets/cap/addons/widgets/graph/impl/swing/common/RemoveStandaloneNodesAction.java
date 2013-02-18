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

import prefuse.action.GroupAction;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.visual.VisualItem;

class RemoveStandaloneNodesAction extends GroupAction {

	RemoveStandaloneNodesAction(final HashMap<String, Boolean> groupMap) {
		super();
	}

	@Override
	public void run(final double frac) {
		synchronized (m_vis) {
			if (m_vis != null) {

				final Iterator<?> iterEdges = m_vis.visibleItems(BeanRelationGraphImpl.EDGES);
				while (iterEdges.hasNext()) {
					final Edge edge = (Edge) iterEdges.next();
					if (!(Boolean) edge.getSourceNode().get("visible")) {
						edge.set("visible", false);
						edge.getTargetNode().set("visible", false);
					}

					final VisualItem visualItem = (VisualItem) edge;
					visualItem.setVisible((Boolean) edge.get("visible"));
				}

				final Iterator<?> iterNodes = m_vis.visibleItems(BeanRelationGraphImpl.NODES);
				while (iterNodes.hasNext()) {
					final Node node = (Node) iterNodes.next();
					if (node.getParent() == null) {
						continue;
					}
					final Iterator<?> itNodes = node.inEdges();
					boolean visible = false;
					while (itNodes.hasNext()) {
						final Edge parent = (Edge) itNodes.next();
						visible = (Boolean) parent.get("visible") ? true : visible;
					}
					node.set("visible", visible);
					final VisualItem visualItem = (VisualItem) node;
					visualItem.setVisible((Boolean) node.get("visible"));
				}

				final Iterator<?> itEdges = m_vis.visibleItems(BeanRelationGraphImpl.EDGES);
				while (itEdges.hasNext()) {
					final Edge edge = (Edge) itEdges.next();
					if (!(Boolean) edge.getSourceNode().get("visible") || !(Boolean) edge.getTargetNode().get("visible")) {
						edge.set("visible", false);
						final VisualItem visualItem = (VisualItem) edge;
						visualItem.setVisible((Boolean) edge.get("visible"));
					}
				}
			}
		}
	}
}
