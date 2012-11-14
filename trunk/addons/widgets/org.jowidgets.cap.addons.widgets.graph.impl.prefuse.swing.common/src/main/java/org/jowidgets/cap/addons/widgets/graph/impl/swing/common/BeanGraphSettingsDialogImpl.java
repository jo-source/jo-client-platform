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
import java.util.Map;

import org.jowidgets.api.widgets.IScrollComposite;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.powo.JoFrame;
import org.jowidgets.tools.widgets.blueprint.BPF;

import prefuse.Visualization;

final class BeanGraphSettingsDialog extends JoFrame {

	private final BeanGraphAttributeListImpl beanGraphAttributeListImpl;

	public BeanGraphSettingsDialog(
		final Visualization vis,
		final Map<Class<Object>, Boolean> groupMap,
		final HashMap<String, Boolean> edgeVisibilityMap) {
		super("Group Visibility");

		setLayout(MigLayoutFactory.growingInnerCellLayout());

		final IScrollComposite content = add(BPF.scrollComposite(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
		content.setLayout(MigLayoutFactory.growingInnerCellLayout());
		content.setPreferredSize(new Dimension(300, 300));

		beanGraphAttributeListImpl = new BeanGraphAttributeListImpl(vis, content.add(
				BPF.composite(),
				"grow, wrap, span, w 0::, h 0::"), groupMap, edgeVisibilityMap);

	}

	public Map<Class<Object>, Boolean> updateGroupMap() {
		return beanGraphAttributeListImpl.getGroupMap();
	}

	public HashMap<String, Boolean> updateEdgeMap() {
		return beanGraphAttributeListImpl.getEdgeMap();
	}

}
