/*
 * Copyright (c) 2011, grossmann, Nikolaus Moll
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

package org.jowidgets.cap.ui.impl.widgets;

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IButton;
import org.jowidgets.api.widgets.IComboBox;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.IControl;
import org.jowidgets.api.widgets.IFrame;
import org.jowidgets.api.widgets.IScrollComposite;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.ui.api.attribute.DisplayFormat;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeConfig;
import org.jowidgets.cap.ui.api.attribute.IAttributeGroup;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.table.IBeanTableConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialog;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialogBluePrint;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.common.widgets.controler.IActionListener;
import org.jowidgets.common.widgets.layout.ILayouter;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.widgets.wrapper.WindowWrapper;
import org.jowidgets.util.Assert;

final class BeanTableSettingsDialogImpl extends WindowWrapper implements IBeanTableSettingsDialog {

	private final IBeanTableModel<?> model;
	private final IFrame frame;
	private boolean okPressed;
	private final List<GroupInformation> groups;
	private IBeanTableConfig currentConfig;
	private final ColumnBasedLayoutManager layoutManager;
	private final IScrollComposite scrollComposite;

	BeanTableSettingsDialogImpl(final IFrame frame, final IBeanTableSettingsDialogBluePrint setup) {
		super(frame);
		Assert.paramNotNull(frame, "frame");
		Assert.paramNotNull(setup, "setup");
		Assert.paramNotNull(setup.getModel(), "setup.getModel()");

		this.frame = frame;
		frame.setLayout(Toolkit.getLayoutFactoryProvider().migLayoutBuilder().columnConstraints("[fill]").rowConstraints(
				"[fill]0[fill]10[fill]").build());
		this.model = setup.getModel();
		this.okPressed = false;
		this.layoutManager = new ColumnBasedLayoutManager(7, 10);
		this.groups = getGroupInformation();

		new ColumnHeaderComposite(frame);
		// no horizontal scrollbar
		scrollComposite = frame.add(
				Toolkit.getBluePrintFactory().scrollComposite().setHorizontalBar(false),
				"grow, w 0::, h 0::, wrap");
		scrollComposite.setLayout(new MigLayoutDescriptor());
		for (final GroupInformation groupInformation : groups) {
			for (int index = groupInformation.startIndex; index <= groupInformation.endIndex; index++) {
				new ColumnComposite(scrollComposite, model.getAttribute(index));
			}
		}

		final IComposite buttonBar = frame.add(Toolkit.getBluePrintFactory().composite(), "right, wrap");
		buttonBar.setLayout(Toolkit.getLayoutFactoryProvider().migLayoutBuilder().columnConstraints("[]10[]0").build());
		final IButton ok = buttonBar.add(Toolkit.getBluePrintFactory().button("Ok"), "tag ok");
		final IButton cancel = buttonBar.add(Toolkit.getBluePrintFactory().button("Cancel", "tag cancel, wrap"));

		ok.addActionListener(new IActionListener() {
			@Override
			public void actionPerformed() {
				okPressed = true;
				setVisible(false);

			}
		});

		cancel.addActionListener(new IActionListener() {
			@Override
			public void actionPerformed() {
				setVisible(false);
			}
		});

		layoutManager.calculateLayout();
		//scrollComposite.setMaxSize(new Dimension(32768, 32768));
		//scrollComposite.setPreferredSize(new Dimension(32768, 32768));
		frame.setSize(1000, 700);
	}

	@Override
	public IBeanTableConfig show() {
		currentConfig = model.getConfig();

		okPressed = false;
		frame.setVisible(true);

		final IBeanTableConfig result = currentConfig;
		currentConfig = null;
		return result;
	}

	@Override
	public boolean isOkPressed() {
		return okPressed;
	}

	/******************* Helper methods **************************************************/
	private int getGroupEnd(final int index) {
		final IAttributeGroup group = model.getAttribute(index).getGroup();
		int currentIndex = index + 1;
		while (currentIndex < model.getColumnCount()) {
			final IAttributeGroup currentGroup = model.getAttribute(currentIndex).getGroup();
			if ((group != null) && (currentGroup != null)) {
				if ((group.getId() != null) && (currentGroup.getId() != null)) {
					if (!group.getId().equals(currentGroup.getId())) {
						break;
					}
				}
			}

			currentIndex++;
		}
		return currentIndex - 1;
	}

	private List<GroupInformation> getGroupInformation() {
		final List<GroupInformation> result = new LinkedList<GroupInformation>();
		int currentIndex = 0;
		while (currentIndex < model.getColumnCount()) {
			final int endIndex = getGroupEnd(currentIndex);
			result.add(new GroupInformation(currentIndex, endIndex));
			currentIndex = endIndex + 1;
		}

		return result;
	}

	private enum Visibility {
		NONE,
		PARTIAL,
		ALL
	}

	private final class GroupInformation {
		private final int startIndex;
		private final int endIndex;
		private final IAttributeGroup group;
		@SuppressWarnings("unused")
		private final boolean sortable;
		private final List<DisplayFormat> headerDisplayFormats;
		private final List<String> valueDisplayFormats;

		private GroupInformation(final int startIndex, final int endIndex) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.headerDisplayFormats = new LinkedList<DisplayFormat>();
			this.valueDisplayFormats = new LinkedList<String>();
			this.group = model.getAttribute(startIndex).getGroup();

			int sortableCount = 0;
			boolean hasLongLabel = false;
			for (int index = startIndex; index <= endIndex; index++) {
				final IAttribute<?> attribute = model.getAttribute(index);
				if (attribute.isSortable()) {
					sortableCount++;
				}

				if ((!hasLongLabel) && (attribute.getLabelLong() != null)) {
					hasLongLabel = true;
				}

				final List<?> controlPanels = attribute.getControlPanels();
				for (final Object provider : controlPanels) {
					final IControlPanelProvider<?> controlPanelProvider = (IControlPanelProvider<?>) provider;

					if (!valueDisplayFormats.contains(controlPanelProvider.getDisplayFormatName())) {
						valueDisplayFormats.add(controlPanelProvider.getDisplayFormatName());
					}
				}
			}

			if (hasLongLabel) {
				headerDisplayFormats.add(DisplayFormat.LONG);
				headerDisplayFormats.add(DisplayFormat.SHORT);
			}
			else {
				headerDisplayFormats.add(DisplayFormat.SHORT);
			}

			sortable = (sortableCount > 0);
		}

		@SuppressWarnings("unused")
		private String getId() {
			if (group == null) {
				return null;
			}
			return group.getId();
		}

		/**
		 * Determines the visibility of the group attributes
		 * 
		 * @return visibility
		 */
		@SuppressWarnings("unused")
		private Visibility getVisiblilty() {
			final Visibility result;
			boolean hasVisible = false;
			boolean hasInvisible = false;
			for (int index = startIndex; index <= endIndex; index++) {
				final String propertyName = model.getAttribute(index).getPropertyName();
				final IAttributeConfig config = currentConfig.getAttributeConfigs().get(propertyName);

				if (config.isVisible()) {
					hasVisible = true;
					if (hasInvisible) {
						break;
					}
				}
				else {
					hasInvisible = true;
					if (hasVisible) {
						break;
					}
				}
			}

			if (hasVisible && hasInvisible) {
				result = Visibility.PARTIAL;
			}
			else if (hasVisible) {
				result = Visibility.ALL;
			}
			else {
				result = Visibility.NONE;
			}
			return result;
		}
	}

	private final class ColumnHeaderComposite {
		private final IComposite composite;

		private ColumnHeaderComposite(final IContainer parent) {
			final IBluePrintFactory bpF = Toolkit.getBluePrintFactory();
			this.composite = parent.add(bpF.composite(), "growx, wrap");
			composite.setMaxSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
			composite.setLayout(new ColumnBasedLayouter(composite, layoutManager));

			composite.add(bpF.textLabel("Name"));
			composite.add(bpF.textLabel("Visible"));
			composite.add(bpF.textLabel("Header Display Format"));
			composite.add(bpF.textLabel("Value Display Format"));
			composite.add(bpF.textLabel("Alignment"));
			composite.add(bpF.textLabel("Current sorting"));
			composite.add(bpF.textLabel("Default sorting"));
		}
	}

	private final class ColumnComposite {
		private final IComposite composite;

		private ColumnComposite(final IContainer parent, final IAttribute<?> attribute) {
			final IBluePrintFactory bpF = Toolkit.getBluePrintFactory();
			this.composite = parent.add(bpF.composite(), "growx, w 0:max:max, wrap");
			composite.setMaxSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
			composite.setLayout(new ColumnBasedLayouter(composite, layoutManager));

			// column name
			composite.add(bpF.textLabel(attribute.getLabel()));

			// visible checkbox
			composite.add(bpF.checkBox());

			// column name display format
			final IComboBox<String> headerDisplayFormat = composite.add(bpF.comboBoxSelection("colname"));
			headerDisplayFormat.setMaxSize(new Dimension(300, 20));

			// value display format
			final IComboBox<String> valueDisplayFormat = composite.add(bpF.comboBoxSelection("value"));
			valueDisplayFormat.setMaxSize(new Dimension(300, 20));

			// alignment
			final IComboBox<String> columnAlignment = composite.add(bpF.comboBoxSelection("align"));
			columnAlignment.setMaxSize(new Dimension(300, 20));

			// current sorting
			final IComboBox<String> currentSorting = composite.add(bpF.comboBoxSelection("cur sort"));
			currentSorting.setMaxSize(new Dimension(300, 20));

			// default sorting
			final IComboBox<String> defaultSorting = composite.add(bpF.comboBoxSelection("def sort"));
			defaultSorting.setMaxSize(new Dimension(300, 20));

			// TODO NM allow column spanning
		}
	}

	private final class ColumnBasedLayoutManager {
		private final List<ColumnBasedLayouter> layouters;
		private final int columnCount;
		private final int[] minWidths;
		private final int[] maxWidths;
		private final int[] usedWidths;
		private final int[] gaps;
		private final int rowHeight;
		private int lastFrameWidth;
		private final int[] growPriority;
		private boolean layoutingEnabled;

		private Dimension minSize;
		private final Dimension maxSize;

		private ColumnBasedLayoutManager(final int columnCount, final int gap) {
			this.layouters = new LinkedList<ColumnBasedLayouter>();
			this.columnCount = columnCount;
			this.minWidths = new int[columnCount];
			this.maxWidths = new int[columnCount];
			this.usedWidths = new int[columnCount];
			this.growPriority = new int[columnCount];
			for (int i = 0; i < growPriority.length; i++) {
				growPriority[i] = 1;
			}

			this.gaps = new int[columnCount + 1];
			for (int i = 0; i < gaps.length; i++) {
				gaps[i] = gap;
			}
			this.rowHeight = 30;
			this.maxSize = new Dimension(Integer.MAX_VALUE, rowHeight);
			lastFrameWidth = 0;

			layoutingEnabled = true;
		}

		private Dimension getMinSize() {
			return minSize;
		}

		private Dimension getPreferredSize() {
			return minSize;
		}

		private Dimension getMaxSize() {
			return maxSize;
		}

		private void calculateLayout() {
			for (final ColumnBasedLayouter layouter : layouters) {
				// calculate min, pref, max widths
				for (int column = 0; column < columnCount; column++) {

					final IControl control = layouter.getControl(column);
					if (control != null) {
						minWidths[column] = Math.max(minWidths[column], control.getMinSize().getWidth());
						maxWidths[column] = Math.max(maxWidths[column], control.getMaxSize().getWidth());
					}
				}
			}

			// calculate used width

			int minWidth = gaps[0];
			for (int column = 0; column < columnCount; column++) {
				minWidth = minWidth + minWidths[column] + gaps[column + 1];

				if (minWidths[column] == maxWidths[column]) {
					growPriority[column] = 0;
				}
			}
			minSize = new Dimension(minWidth, rowHeight);
		}

		private void calculateUsedWidths() {
			if (lastFrameWidth == scrollComposite.getSize().getWidth()) {
				return;
			}
			layoutingEnabled = false;
			lastFrameWidth = scrollComposite.getSize().getWidth();

			final int availableWidth = lastFrameWidth - 20;

			final int gapWidth = getArrayWidth(gaps);
			final int minWidth = getArrayWidth(minWidths);
			final int effectiveWidth = availableWidth - gapWidth;
			if (effectiveWidth <= minWidth) {
				// do not shrink
				for (int column = 0; column < columnCount; column++) {
					usedWidths[column] = minWidths[column];
				}
			}

			final int growPrioritySum = getArrayWidth(growPriority);
			final int growWidth = effectiveWidth - minWidth;
			int restWidth = effectiveWidth;
			for (int i = 0; i < columnCount - 1; i++) {
				int additionalWidth = 0;
				if (growPriority[i] > 0) {
					additionalWidth = (int) ((double) (growWidth * growPriority[i]) / growPrioritySum);
				}
				usedWidths[i] = minWidths[i] + additionalWidth;
				restWidth = restWidth - usedWidths[i];
			}
			usedWidths[columnCount - 1] = restWidth;

			frame.setRedrawEnabled(false);
			for (final ColumnBasedLayouter layouter : layouters) {
				layouter.doLayout();
			}
			frame.setRedrawEnabled(true);
			layoutingEnabled = true;
		}

		private int getArrayWidth(final int[] widths) {
			int result = 0;
			for (final int width : widths) {
				result = result + width;
			}
			return result;
		}
	}

	private final class ColumnBasedLayouter implements ILayouter {
		private final ColumnBasedLayoutManager manager;
		private final IContainer parent;

		private ColumnBasedLayouter(final IContainer parent, final ColumnBasedLayoutManager manager) {
			this.parent = parent;
			this.manager = manager;
			manager.layouters.add(this);
		}

		@Override
		public void layout() {
			if (!manager.layoutingEnabled) {
				return;
			}
			manager.calculateUsedWidths();
		}

		private void doLayout() {
			final List<IControl> children = parent.getChildren();
			int index = 0;
			int x = manager.gaps[0];
			for (final IControl child : children) {
				// TODO NM child to index [span]
				final int width = manager.usedWidths[index];

				child.setPosition(x, 0);
				child.setSize(width, child.getPreferredSize().getHeight());
				x = x + width + manager.gaps[index + 1];
				index++;
			}
		}

		@Override
		public Dimension getMinSize() {
			return manager.getMinSize();
		}

		@Override
		public Dimension getPreferredSize() {
			return manager.getPreferredSize();
		}

		@Override
		public Dimension getMaxSize() {
			return manager.getMaxSize();
		}

		@Override
		public void invalidate() {
			//manager.calculateUsedWidths();
		}

		private IControl getControl(final int column) {
			final List<IControl> children = parent.getChildren();
			if (column > children.size()) {
				return null;
			}
			return children.get(column);
		}
	}

}
