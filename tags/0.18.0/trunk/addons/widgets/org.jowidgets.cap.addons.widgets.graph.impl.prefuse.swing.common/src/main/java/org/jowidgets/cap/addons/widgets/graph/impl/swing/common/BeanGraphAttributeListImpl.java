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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jowidgets.api.color.Colors;
import org.jowidgets.api.layout.tablelayout.ITableLayout;
import org.jowidgets.api.layout.tablelayout.ITableLayoutBuilder;
import org.jowidgets.api.layout.tablelayout.ITableLayoutBuilder.Alignment;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.ICheckBox;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.blueprint.ICheckBoxBluePrint;
import org.jowidgets.api.widgets.blueprint.ITextLabelBluePrint;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.common.color.ColorValue;
import org.jowidgets.common.color.IColorConstant;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.common.types.Markup;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.widgets.wrapper.CompositeWrapper;
import org.jowidgets.tools.widgets.wrapper.ContainerWrapper;

import prefuse.Visualization;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.visual.tuple.TableNodeItem;

public class BeanGraphAttributeListImpl extends CompositeWrapper {

	private static final IColorConstant ATTRIBUTE_HEADER_BACKGROUND = new ColorValue(6, 27, 95);
	private static final IColorConstant ATTRIBUTE_GROUP_BACKGROUND = new ColorValue(200, 220, 255);

	private static final ITextLabelBluePrint LABEL_HEADER = Toolkit.getBluePrintFactory().textLabel().setColor(Colors.WHITE).setMarkup(
			Markup.STRONG).alignCenter();
	private static final ITextLabelBluePrint LABEL_ALL = Toolkit.getBluePrintFactory().textLabel().setAlignment(
			AlignmentHorizontal.LEFT).setMarkup(Markup.STRONG);
	private static final ITextLabelBluePrint LABEL_GROUP = Toolkit.getBluePrintFactory().textLabel().setAlignment(
			AlignmentHorizontal.LEFT);
	private static final ICheckBoxBluePrint CHECK_BOCK_PB = Toolkit.getBluePrintFactory().checkBox().alignCenter();

	private final ITableLayout attributeLayoutManager;
	private final Visualization vis;
	private final Map<String, Boolean> groupMap;
	private final HashMap<String, Boolean> edgeMap;
	private final List<ICheckBox> allCheckBoxes = new LinkedList<ICheckBox>();
	private boolean row = true;

	public BeanGraphAttributeListImpl(
		final Visualization vis,
		final IComposite container,
		final Map<String, Boolean> groupMap,
		final HashMap<String, Boolean> edgeVisibilityMap,
		final FilterType type) {
		super(container);

		this.groupMap = groupMap;
		this.edgeMap = edgeVisibilityMap;
		this.vis = vis;

		final ITableLayoutBuilder builder = Toolkit.getLayoutFactoryProvider().tableLayoutBuilder();
		builder.columnCount(2);
		builder.gap(15);
		builder.verticalGap(4);

		builder.alignment(1, Alignment.CENTER);
		builder.fixedColumnWidth(0, 200);
		builder.fixedColumnWidth(1, 50);

		this.attributeLayoutManager = builder.build();
		attributeLayoutManager.beginLayout();

		this.setLayout(new MigLayoutDescriptor("hidemode 2", "[grow, 0::]", "[grow,0::]0[grow,0::]"));

		final IBluePrintFactory bpf = Toolkit.getBluePrintFactory();

		new AttributeHeaderComposite(add(bpf.composite(), "grow, wrap"));

		switch (type) {
			case RELATIONS:
				initializeGroups(this, edgeVisibilityMap, bpf);
				break;
			case GROUPS:
				initializeGroups(this, groupMap, bpf);
				break;
			default:
				break;
		}

		attributeLayoutManager.endLayout();
		attributeLayoutManager.validate();

		//		final Dimension minSize = super.getMinSize();
		//		setMinSize(new Dimension(minSize.getWidth(), minSize.getHeight()));

		final Dimension prefSize = super.getPreferredSize();
		setPreferredSize(new Dimension(prefSize.getWidth(), prefSize.getHeight()));
	}

	private void initializeGroups(
		final BeanGraphAttributeListImpl content,
		final Map<String, Boolean> groupMap,
		final IBluePrintFactory bpf) {
		row = !row;
		for (final Entry<String, Boolean> entry : groupMap.entrySet()) {
			new AttributeGroupComposite(add(bpf.composite(), "grow, wrap"), entry.getKey(), entry.getValue(), row);
			row = !row;
		}
	}

	private void initializeGroups(
		final BeanGraphAttributeListImpl content,
		final HashMap<String, Boolean> edgeVisibilityMap,
		final IBluePrintFactory bpf) {
		row = !row;
		new AllAttributeComposite(content.add(bpf.composite(), "grow, wrap"));
		for (final Entry<String, Boolean> entry : edgeVisibilityMap.entrySet()) {
			new AttributeEdgeComposite(content.add(bpf.composite(), "grow, wrap"), entry.getKey(), entry.getValue(), row);
			row = !row;
		}
	}

	private final class AllAttributeComposite extends ContainerWrapper {

		private final ICheckBox checkBox;

		public AllAttributeComposite(final IContainer container) {
			super(container);
			setBackgroundColor(row ? ATTRIBUTE_GROUP_BACKGROUND : Colors.WHITE);
			row = !row;
			setLayout(attributeLayoutManager.rowBuilder().build());
			add(LABEL_ALL.setText("All"));
			checkBox = add(CHECK_BOCK_PB);
			checkBox.setSelected(true);
			checkBox.addInputListener(new IInputListener() {

				@Override
				public void inputChanged() {
					updateAllCheckBoxes(checkBox.isSelected());
				}

			});
		}
	}

	private void updateAllCheckBoxes(final boolean selected) {
		final Iterator<ICheckBox> it = allCheckBoxes.iterator();
		while (it.hasNext()) {
			synchronized (vis) {
				it.next().setSelected(selected);
				vis.run("filter");
				vis.run("layout");
				vis.run("animate");
				vis.run("color");
			}
		}
	}

	private final class AttributeHeaderComposite extends ContainerWrapper {

		public AttributeHeaderComposite(final IContainer container) {
			super(container);
			setBackgroundColor(ATTRIBUTE_HEADER_BACKGROUND);
			setLayout(attributeLayoutManager.rowBuilder().build());

			add(LABEL_HEADER.setText("name"));
			add(LABEL_HEADER.setText("visible").setAlignment(AlignmentHorizontal.CENTER));
		}
	}

	private final class AttributeGroupComposite extends ContainerWrapper {

		private final ICheckBox checkBox;

		public AttributeGroupComposite(final IContainer container, final String key, final boolean selected, final boolean row) {
			super(container);
			//			setBackgroundColor(row ? ATTRIBUTE_GROUP_BACKGROUND : Colors.WHITE);
			final int[] color = BeanRelationGraphImpl.GROUP_COLOR_MAP.get(key);
			setBackgroundColor(new ColorValue(color[0], color[1], color[2]));
			setLayout(attributeLayoutManager.rowBuilder().build());

			add(LABEL_GROUP.setText(key).setAlignment(AlignmentHorizontal.LEFT));
			checkBox = add(CHECK_BOCK_PB);
			checkBox.setSelected(selected);
			checkBox.addInputListener(new IInputListener() {

				@Override
				public void inputChanged() {

					for (final Entry<String, Boolean> entry : groupMap.entrySet()) {
						if (key.equals(entry.getKey())) {
							entry.setValue(checkBox.isSelected());

							final String query = "beanrelation = '" + entry.getKey() + "'";
							final Predicate predicate = (Predicate) ExpressionParser.parse(query);
							final Iterator<?> iterator = vis.items(BeanRelationGraphImpl.NODES, predicate);
							while (iterator.hasNext()) {
								final TableNodeItem item = (TableNodeItem) iterator.next();
								item.set("filtered", entry.getValue());
							}
						}
					}
					vis.run("filter");
					vis.run("layout");
					vis.run("animate");
					vis.run("color");
				}
			});
		}
	}

	private final class AttributeEdgeComposite extends ContainerWrapper {

		private final ICheckBox checkBox;

		public AttributeEdgeComposite(final IContainer container, final String key, final boolean selected, final boolean row) {
			super(container);
			setBackgroundColor(row ? ATTRIBUTE_GROUP_BACKGROUND : Colors.WHITE);
			setLayout(attributeLayoutManager.rowBuilder().build());

			add(LABEL_GROUP.setText(key).setAlignment(AlignmentHorizontal.LEFT));
			checkBox = add(CHECK_BOCK_PB);
			allCheckBoxes.add(checkBox);
			checkBox.setSelected(selected);
			checkBox.addInputListener(new IInputListener() {

				@Override
				public void inputChanged() {

					synchronized (vis) {

						for (final Entry<String, Boolean> entry : edgeMap.entrySet()) {
							if (key.equals(entry.getKey())) {
								entry.setValue(checkBox.isSelected());
							}
						}
						vis.run("filter");
						vis.run("layout");
						vis.run("animate");
						vis.run("color");
					}
				}
			});
		}
	}

	public Map<String, Boolean> getGroupMap() {
		return groupMap;
	}

	public HashMap<String, Boolean> getEdgeMap() {
		return this.edgeMap;
	}

	enum FilterType {

		RELATIONS,
		GROUPS;
	}

}
