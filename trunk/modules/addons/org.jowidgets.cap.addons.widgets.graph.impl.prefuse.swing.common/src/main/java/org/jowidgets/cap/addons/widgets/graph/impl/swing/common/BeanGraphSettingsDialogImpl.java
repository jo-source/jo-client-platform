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

import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IScrollComposite;
import org.jowidgets.api.widgets.ITabFolder;
import org.jowidgets.api.widgets.ITabItem;
import org.jowidgets.cap.addons.widgets.graph.impl.swing.common.BeanGraphAttributeListImpl.FilterType;
import org.jowidgets.common.types.Position;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.powo.JoFrame;
import org.jowidgets.tools.widgets.blueprint.BPF;

import prefuse.Visualization;

final class BeanGraphSettingsDialog extends JoFrame {

	//	private final BeanGraphAttributeListImpl beanGraphAttributeListImplRelations;
	private final BeanGraphAttributeListImpl beanGraphAttributeListImplGroups;
	private final HashMap<String, Boolean> groupVisibilityMap;
	private final Map<String, int[]> groupColorMap;
	private final Visualization vis;

	private ITabFolder tabFolder;

	BeanGraphSettingsDialog(
		final Visualization vis,
		final HashMap<String, Boolean> groupMap,
		final HashMap<String, Boolean> edgeVisibilityMap,
		final Position position,
		final int filterTabIndex,
		final Map<String, int[]> groupColorMap) {
		super("Filter");
		this.groupVisibilityMap = groupMap;
		this.vis = vis;
		this.groupColorMap = groupColorMap;
		setLayout(MigLayoutFactory.growingCellLayout());
		if (position != null) {
			setPosition(position);
		}

		beanGraphAttributeListImplGroups = initializeTabFolder(filterTabIndex);
	}

	private BeanGraphAttributeListImpl initializeTabFolder(final int filterTabIndex) {
		tabFolder = add(BPF.tabFolder().setTabsCloseable(false), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

		final ITabItem itemGroupFilter = tabFolder.addItem(BPF.tabItem());
		itemGroupFilter.setText("GroupFilter");
		itemGroupFilter.setLayout(MigLayoutFactory.growingInnerCellLayout());
		final IScrollComposite contentGroup = itemGroupFilter.add(
				BPF.scrollComposite(),
				MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
		contentGroup.setLayout(MigLayoutFactory.growingInnerCellLayout());

		//		final ITabItem itemRelationFilter = tabFolder.addItem(BPF.tabItem());
		//		itemRelationFilter.setText("RelationFilter");
		//		itemRelationFilter.setLayout(MigLayoutFactory.growingInnerCellLayout());
		//		final IScrollComposite contentRelations = itemRelationFilter.add(
		//				BPF.scrollComposite(),
		//				MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
		//		contentRelations.setLayout(MigLayoutFactory.growingInnerCellLayout());

		//		beanGraphAttributeListImplRelations = new BeanGraphAttributeListImpl(vis, contentRelations.add(
		//				BPF.composite(),
		//				"aligny top, growx,  w 0::, h 0::"), edgeVisibilityMap, FilterType.RELATIONS);

		final IComposite content = contentGroup.add(BPF.composite(), "aligny top, growx,  w 0::, h 0::");
		final BeanGraphAttributeListImpl beanGraphAttributeListImplGroup = new BeanGraphAttributeListImpl(
			vis,
			content,
			groupVisibilityMap,
			FilterType.GROUPS,
			groupColorMap);

		tabFolder.setSelectedItem((getFilterTabByIndex(filterTabIndex) != null)
				? getFilterTabByIndex(filterTabIndex).getIndex() : 0);

		return beanGraphAttributeListImplGroup;
	}

	public HashMap<String, Boolean> updateGroupMap() {
		return beanGraphAttributeListImplGroups.getFilterMap();
	}

	//	public HashMap<String, Boolean> updateEdgeMap() {
	//		return beanGraphAttributeListImplRelations.getFilterMap();
	//	}

	public int getOpenFilterTab() {
		return tabFolder.getSelectedIndex();
	}

	enum FilterTab {

		GROUP(0),
		RELATIONS(1);

		private final int index;

		private FilterTab(final int index) {
			this.index = index;
		}

		public int getIndex() {
			return index;
		}
	}

	public FilterTab getFilterTabByIndex(final int index) {
		for (final FilterTab filterTab : FilterTab.values()) {
			if (filterTab.getIndex() == index) {
				return filterTab;
			}
		}
		return null;
	}
}
