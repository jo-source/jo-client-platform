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

import java.util.Iterator;

import prefuse.action.GroupAction;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualItem;

class NodeVisibilityAction extends GroupAction {

	NodeVisibilityAction() {
		super();
	}

	@Override
	public void run(final double frac) {
		synchronized (m_vis) {

			final TupleSet nodes = m_vis.getGroup(BeanRelationGraphImpl.NODES);
			final Iterator<?> node = nodes.tuples();
			while (node.hasNext()) {
				final Node result = (Node) node.next();
				final VisualItem visualItem = (VisualItem) result;
				if (result != null) {

					if (result.getParent() != null) {
						@SuppressWarnings("unchecked")
						final Iterator<Edge> inEdges = result.inEdges();
						boolean visible = false;
						while (inEdges.hasNext()) {
							final Edge parent = inEdges.next();
							visible = (Boolean) parent.get("visible") ? true : visible;
						}
						result.set("visible", visible);
					}

					if ((Boolean) result.get("visible") != null) {
						visualItem.setVisible((Boolean) result.get("visible"));
					}
				}
				if (result.getOutDegree() == 0) {
					result.set("isParent", false);
				}
			}
		}
	}
}
