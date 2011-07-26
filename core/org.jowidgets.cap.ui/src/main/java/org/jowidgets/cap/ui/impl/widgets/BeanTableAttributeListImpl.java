/*
 * Copyright (c) 2011, nimoll
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.api.color.Colors;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.ICheckBox;
import org.jowidgets.api.widgets.IComboBox;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.IControl;
import org.jowidgets.api.widgets.IScrollComposite;
import org.jowidgets.api.widgets.blueprint.ITextLabelBluePrint;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.common.api.sort.SortOrder;
import org.jowidgets.cap.ui.api.attribute.DisplayFormat;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeConfig;
import org.jowidgets.cap.ui.api.attribute.IAttributeGroup;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.table.IBeanTableConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.common.color.ColorValue;
import org.jowidgets.common.color.IColorConstant;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.common.types.Markup;
import org.jowidgets.common.types.Rectangle;
import org.jowidgets.common.widgets.controler.IInputListener;
import org.jowidgets.common.widgets.layout.ILayouter;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.widgets.wrapper.ContainerWrapper;

@SuppressWarnings("unused")
final class BeanTableAttributeListImpl extends ContainerWrapper {

	//private static final IColorConstant ATTRIBUTE_GROUP_BACKGROUND = new ColorValue(150, 197, 226);
	private static final IColorConstant ATTRIBUTE_GROUP_BACKGROUND = new ColorValue(130, 177, 236);

	private static final IColorConstant[] NO_COLORS = new IColorConstant[0];
	private static final IColorConstant[] ALTERNATING_GROUPS = new IColorConstant[] {
			new ColorValue(130, 177, 236), new ColorValue(255, 0, 0)};//new ColorValue(115, 162, 255)};

	private static final IColorConstant[] ALTERNATING_ATTRIBUTES = new IColorConstant[] {
			Colors.DEFAULT_TABLE_EVEN_BACKGROUND_COLOR, Colors.WHITE};

	private static final ITextLabelBluePrint LABEL_HEADER = Toolkit.getBluePrintFactory().textLabel().setColor(Colors.WHITE).setMarkup(
			Markup.STRONG).alignCenter();
	private static final ITextLabelBluePrint LABEL_GROUPS = Toolkit.getBluePrintFactory().textLabel().setMarkup(Markup.STRONG).setColor(
			Colors.WHITE);
	private static final ITextLabelBluePrint LABEL_ATTRIBUTES = Toolkit.getBluePrintFactory().textLabel();
	private static final ITextLabelBluePrint LABEL_ALL = Toolkit.getBluePrintFactory().textLabel().setMarkup(Markup.STRONG);

	private final IBeanTableModel<?> model;
	private final Map<String, AttributeGroupContainer> groupContainers;
	private final Map<IAttribute<?>, AttributeComposite> attributeComposites;
	private final AttributeLayoutManager attributeLayoutManager;
	private final IScrollComposite attributeScroller;
	private final IBeanTableConfig currentConfig;

	private final ListLayout attributeScrollerLayout;

	BeanTableAttributeListImpl(final IContainer container, final IBeanTableModel<?> model) {
		super(container);
		this.model = model;
		currentConfig = model.getConfig();
		this.attributeLayoutManager = new AttributeLayoutManager(9, 10, 4);
		this.groupContainers = new HashMap<String, AttributeGroupContainer>();
		this.attributeComposites = new HashMap<IAttribute<?>, AttributeComposite>();
		this.setLayout(new MigLayoutDescriptor("[grow]", "[]0[]0[grow]"));

		final IBluePrintFactory bpF = Toolkit.getBluePrintFactory();

		new AttributeHeaderComposite(add(bpF.composite(), "grow, wrap"));
		new AllAttributesComposite(add(bpF.composite(), "grow, wrap"), model);

		attributeScroller = this.add(bpF.scrollComposite().setHorizontalBar(false), "wrap, grow, w 0::, h 0::");
		attributeScrollerLayout = new ListLayout(attributeScroller, ALTERNATING_GROUPS);
		attributeScroller.setLayout(attributeScrollerLayout);

		AttributeGroupComposite lastHeader = null;
		for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
			final IAttribute<?> attribute = model.getAttribute(columnIndex);

			final String attributeGroupId;
			if (attribute.getGroup() != null) {
				attributeGroupId = attribute.getGroup().getId();
			}
			else {
				attributeGroupId = null;
			}

			AttributeGroupContainer groupContainer = groupContainers.get(attributeGroupId);
			if (groupContainer == null) {
				final AttributeGroupInformation information = new AttributeGroupInformation(columnIndex);
				final AttributeGroupComposite header = new AttributeGroupComposite(
					attributeScroller.add(bpF.composite()),
					information);
				groupContainer = new AttributeGroupContainer(attributeScroller.add(bpF.composite()));
				groupContainers.put(attributeGroupId, groupContainer);

				if (lastHeader != null) {
					lastHeader.setVisible(true);
				}
				else {
					header.setVisible(false);
				}
				lastHeader = header;
			}

			// add item
			final AttributeComposite attributeComposite = new AttributeComposite(groupContainer.add(bpF.composite()), attribute);
			attributeComposites.put(attribute, attributeComposite);
		}

		attributeLayoutManager.modes[1] = FillMode.CENTER; // center visibility checkbox
		attributeLayoutManager.gaps[4] = 40; // add central gap
		attributeLayoutManager.gaps[6] = 7; // reduce gap between in sort order
		attributeLayoutManager.gaps[8] = 7; // reduce gap between in sort order

		attributeLayoutManager.calculateLayout();
	}

	private final class AttributeGroupContainer extends ContainerWrapper {

		private AttributeGroupContainer(final IContainer container) {
			super(container);
			setLayout(new ListLayout(container, ALTERNATING_ATTRIBUTES));
		}

	}

	private final class AttributeHeaderComposite extends ContainerWrapper {

		public AttributeHeaderComposite(final IContainer container) {
			super(container);
			setBackgroundColor(ATTRIBUTE_GROUP_BACKGROUND);
			setLayout(new AttributeLayout(container));

			final IBluePrintFactory bpF = Toolkit.getBluePrintFactory();
			add(LABEL_HEADER.setText("Name"));
			add(LABEL_HEADER.setText("Visible"));
			add(LABEL_HEADER.setText("Header format"));
			add(LABEL_HEADER.setText("Content format"));
			add(LABEL_HEADER.setText("Alignment"));
			add(LABEL_HEADER.setText("Current sorting"), 2);
			add(LABEL_HEADER.setText("Default sorting"), 2);
		}
	}

	private abstract class AbstractAttributeComposite<TYPE> extends ContainerWrapper {
		private final ICheckBox visible;
		private final IComboBox<String> headerFormat;
		private final IComboBox<String> contentFormat;
		private final IComboBox<String> columnAlignment;
		private final IComboBox<String> currentSorting;
		private final IComboBox<String> currentSortingIndex;
		private final IComboBox<String> defaultSorting;
		private final IComboBox<String> defaultSortingIndex;

		private AbstractAttributeComposite(final IContainer container, final TYPE data, final ITextLabelBluePrint textLabel) {
			super(container);
			final IBluePrintFactory bpF = Toolkit.getBluePrintFactory();
			setLayout(new AttributeLayout(container));

			add(textLabel.setText(getLabelText(data)));

			visible = add(bpF.checkBox());

			final List<String> headerFormats = getHeaderFormats(data);
			if (headerFormats.size() > 1) {
				headerFormat = createComboBox(bpF, headerFormats, 300);
			}
			else {
				headerFormat = null;
				addNotAvailableLabel(textLabel);
			}

			final List<String> contentFormats = getContentFormats(data);
			if (contentFormats.size() > 1) {
				contentFormat = createComboBox(bpF, contentFormats, 300);
			}
			else {
				contentFormat = null;
				addNotAvailableLabel(textLabel);
			}

			columnAlignment = createComboBox(bpF, getAlignments(), 200);

			if (isSortable(data)) {
				currentSorting = createComboBox(bpF, getSortOrders(), 250);
				currentSortingIndex = add(bpF.comboBoxSelection("1"));

				defaultSorting = createComboBox(bpF, getSortOrders(), 250);
				defaultSortingIndex = add(bpF.comboBoxSelection("1"));
			}
			else {
				currentSorting = null;
				currentSortingIndex = null;
				defaultSorting = null;
				defaultSortingIndex = null;
				for (int i = 0; i < 4; i++) {
					addNotAvailableLabel(textLabel);
				}
			}
		}

		protected abstract String getLabelText(final TYPE data);

		protected abstract List<String> getHeaderFormats(final TYPE data);

		protected abstract List<String> getContentFormats(final TYPE data);

		protected abstract boolean isSortable(final TYPE data);

		private void addNotAvailableLabel(final ITextLabelBluePrint textLabel) {
			final AlignmentHorizontal alignment = textLabel.getAlignment();
			add(textLabel.alignCenter().setText("-"));
			textLabel.setAlignment(alignment);
		}

		protected IComboBox<String> createComboBox(
			final IBluePrintFactory bpF,
			final List<String> elements,
			final Integer maxWidth) {
			final IComboBox<String> result = add(bpF.comboBoxSelection());
			result.setElements(elements);
			if (maxWidth != null) {
				result.setMaxSize(new Dimension(maxWidth.intValue(), result.getPreferredSize().getHeight()));
			}
			return result;
		}

		protected List<String> getAlignments() {
			final List<String> result = new LinkedList<String>();
			for (final AlignmentHorizontal alignment : AlignmentHorizontal.values()) {
				result.add(alignment.getLabel());
			}
			return result;
		}

		protected List<String> getSortOrders() {
			final List<String> result = new LinkedList<String>();
			for (final SortOrder sortOrder : SortOrder.values()) {
				result.add(sortOrder.getLabel());
			}
			return result;
		}

		public ICheckBox getVisible() {
			return visible;
		}

		public IComboBox<String> getHeaderFormat() {
			return headerFormat;
		}

		public IComboBox<String> getContentFormat() {
			return contentFormat;
		}

		public IComboBox<String> getColumnAlignment() {
			return columnAlignment;
		}

		public IComboBox<String> getCurrentSorting() {
			return currentSorting;
		}

		public IComboBox<String> getCurrentSortingIndex() {
			return currentSortingIndex;
		}

		public IComboBox<String> getDefaultSorting() {
			return defaultSorting;
		}

		public IComboBox<String> getDefaultSortingIndex() {
			return defaultSortingIndex;
		}

	}

	private final class AttributeComposite extends AbstractAttributeComposite<IAttribute<?>> {

		private AttributeComposite(final IContainer container, final IAttribute<?> attribute) {
			super(container, attribute, LABEL_ATTRIBUTES);
		}

		@Override
		protected String getLabelText(final IAttribute<?> attribute) {
			return attribute.getLabel();
		}

		@Override
		protected List<String> getHeaderFormats(final IAttribute<?> attribute) {
			final List<String> result = new LinkedList<String>();
			if ((attribute.getLabelLong() != null) && (!attribute.getLabel().equals(attribute.getLabelLong()))) {
				result.add(DisplayFormat.SHORT.getName());
				result.add(DisplayFormat.LONG.getName());
			}
			return result;
		}

		@Override
		protected List<String> getContentFormats(final IAttribute<?> attribute) {
			final List<String> result = new LinkedList<String>();
			for (final IControlPanelProvider<?> provider : attribute.getControlPanels()) {
				result.add(provider.getDisplayFormatName());
			}
			return result;
		}

		@Override
		protected boolean isSortable(final IAttribute<?> attribute) {
			return attribute.isSortable();
		}
	}

	private final class AttributeGroupComposite extends AbstractAttributeComposite<AttributeGroupInformation> {

		private AttributeGroupComposite(final IContainer container, final AttributeGroupInformation information) {
			super(container, information, LABEL_GROUPS);
			setBackgroundColor(ATTRIBUTE_GROUP_BACKGROUND);

			getVisible().addInputListener(new IInputListener() {

				@Override
				public void inputChanged() {
					groupContainers.get(information.getId()).setVisible(!getVisible().isSelected());
					attributeScrollerLayout.invalidate();
					attributeScrollerLayout.layout();
					attributeScroller.layoutBegin();
					attributeScroller.layoutEnd();
				}

			});
		}

		@Override
		protected String getLabelText(final AttributeGroupInformation data) {
			return data.getId();
		}

		@Override
		protected List<String> getHeaderFormats(final AttributeGroupInformation data) {
			final List<String> result = new LinkedList<String>();
			// TODO NM
			result.add(DisplayFormat.SHORT.getName());
			result.add(DisplayFormat.LONG.getName());
			return result;
		}

		@Override
		protected List<String> getContentFormats(final AttributeGroupInformation data) {
			return data.contentFormats;
		}

		@Override
		protected boolean isSortable(final AttributeGroupInformation data) {
			return data.sortable;
		}

	}

	private final class AllAttributesComposite extends AbstractAttributeComposite<IBeanTableModel<?>> {
		private AllAttributesComposite(final IContainer container, final IBeanTableModel<?> model) {
			super(container, model, LABEL_ALL);
			setBackgroundColor(Colors.WHITE);
		}

		@Override
		protected String getLabelText(final IBeanTableModel<?> model) {
			// TODO i18n
			return "All";
		}

		@Override
		protected List<String> getHeaderFormats(final IBeanTableModel<?> model) {
			final List<String> result = new LinkedList<String>();
			// TODO NM
			result.add(DisplayFormat.SHORT.getName());
			result.add(DisplayFormat.LONG.getName());
			return result;
		}

		@Override
		protected List<String> getContentFormats(final IBeanTableModel<?> model) {
			final List<String> result = new LinkedList<String>();
			for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
				final IAttribute<?> attribute = model.getAttribute(columnIndex);
				for (final IControlPanelProvider<?> provider : attribute.getControlPanels()) {
					if (result.contains(provider.getDisplayFormatName())) {
						continue;
					}

					result.add(provider.getDisplayFormatName());
				}
			}
			return result;
		}

		@Override
		protected boolean isSortable(final IBeanTableModel<?> model) {
			for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
				if (model.getAttribute(columnIndex).isSortable()) {
					return true;
				}
			}
			return false;
		}
	}

	private enum FillMode {
		FILL,
		CENTER
	}

	private final class AttributeLayoutManager {
		private final int columnCount;
		private final List<AttributeLayout> layouters;
		private final int[] widths;
		private final int[] gaps;
		private final FillMode[] modes;
		private final int verticalGap;

		private Dimension preferredSize;

		private int layoutHashCode;

		private AttributeLayoutManager(final int columnCount, final int defaultGap, final int verticalGap) {
			this.columnCount = columnCount;
			this.layouters = new LinkedList<AttributeLayout>();
			this.verticalGap = verticalGap;

			this.widths = new int[columnCount];
			this.gaps = new int[columnCount + 1];
			for (int i = 0; i < gaps.length; i++) {
				gaps[i] = defaultGap;
			}

			this.modes = new FillMode[columnCount];
			for (int i = 0; i < modes.length; i++) {
				modes[i] = FillMode.FILL;
			}

			this.layoutHashCode = 0;
		}

		private void calculateLayout() {
			for (int i = 0; i < widths.length; i++) {
				widths[i] = 0;
			}

			int minHeight = 0;
			for (final AttributeLayout layouter : layouters) {
				for (int i = 0; i < columnCount; i++) {
					final Dimension size = layouter.getChildSize(i);
					widths[i] = Math.max(widths[i], size.getWidth());
					minHeight = Math.max(minHeight, size.getHeight());
				}
			}

			int minWidth = 0;
			for (final int width : widths) {
				minWidth = minWidth + width;
			}
			for (final int gap : gaps) {
				minWidth = minWidth + gap;
			}
			preferredSize = new Dimension(minWidth, minHeight + 2 * verticalGap);

			layoutHashCode = 17;
			for (int i = 0; i < widths.length; i++) {
				layoutHashCode = 31 * layoutHashCode + widths[i];
			}
			for (int i = 0; i < gaps.length; i++) {
				layoutHashCode = 31 * layoutHashCode + gaps[i];
			}
		}

		public Dimension getPreferredSize() {
			if (preferredSize == null) {
				calculateLayout();
			}
			return preferredSize;
		}
	}

	private final class AttributeLayout implements ILayouter {
		private final IContainer container;
		private int layoutHashCode;

		private final HashMap<IControl, Dimension> preferredSizes;

		private AttributeLayout(final IContainer container) {
			this.container = container;
			this.preferredSizes = new HashMap<IControl, Dimension>();
			this.layoutHashCode = 0;
			attributeLayoutManager.layouters.add(this);
		}

		@Override
		public void layout() {
			if (layoutHashCode == attributeLayoutManager.layoutHashCode) {
				return;
			}
			layoutHashCode = attributeLayoutManager.layoutHashCode;

			final Rectangle clientArea = container.getClientArea();
			int x = attributeLayoutManager.gaps[0];
			int index = 0;
			for (final IControl control : container.getChildren()) {
				final int span = getSpan(control);
				final int width = getSpanWidth(index, span);

				final Dimension size = getPreferredSize(control);
				final int y = clientArea.getY() + (clientArea.getHeight() - size.getHeight()) / 2;

				final int controlWidth;
				if (attributeLayoutManager.modes[index] == FillMode.FILL) {
					controlWidth = width;
				}
				else {
					controlWidth = size.getWidth();
				}
				control.setSize(controlWidth, size.getHeight());
				control.setPosition(x + (width - controlWidth) / 2, y);

				index = index + span;
				x = x + width + attributeLayoutManager.gaps[index];
			}
		}

		@Override
		public Dimension getMinSize() {
			return getPreferredSize();
		}

		@Override
		public Dimension getPreferredSize() {
			return attributeLayoutManager.getPreferredSize();
		}

		@Override
		public Dimension getMaxSize() {
			return getPreferredSize();
		}

		@Override
		public void invalidate() {
			// TODO Auto-generated method stub

		}

		public Dimension getChildSize(final int index) {
			int currentIndex = 0;
			for (final IControl control : container.getChildren()) {
				if (index == currentIndex) {
					return getPreferredSize(control);
				}

				currentIndex = currentIndex + getSpan(control);
			}
			return new Dimension(0, 0);
		}

		private int getSpan(final IControl control) {
			if ((control.getLayoutConstraints() == null) || (!(control.getLayoutConstraints() instanceof Integer))) {
				return 1;
			}

			return (Integer) control.getLayoutConstraints();
		}

		private int getSpanWidth(final int index, final int span) {
			int result = attributeLayoutManager.widths[index];
			for (int i = 1; i < span; i++) {
				result = result + attributeLayoutManager.gaps[index + i] + attributeLayoutManager.widths[index + i];
			}
			return result;
		}

		private Dimension getPreferredSize(final IControl control) {
			if (!preferredSizes.containsKey(control)) {
				final Dimension size = control.getPreferredSize();
				preferredSizes.put(control, size);
			}
			return preferredSizes.get(control);
		}

	}

	private static final class ListLayout implements ILayouter {

		private final IContainer container;
		private final HashMap<IControl, Dimension> preferredSizes;
		private Dimension preferredSize;
		private final IColorConstant[] colors;

		private ListLayout(final IContainer container, final IColorConstant[] colors) {
			this.container = container;
			this.colors = colors;
			this.preferredSizes = new HashMap<IControl, Dimension>();
		}

		@Override
		public void layout() {
			final Rectangle clientArea = container.getClientArea();
			final int x = clientArea.getX();
			int y = clientArea.getY();
			final int width = clientArea.getWidth();

			int groupIndex = 0;

			for (final IControl control : container.getChildren()) {
				if (!control.isVisible()) {
					continue;
				}
				final Dimension size = getPreferredSize(control);

				control.setPosition(x, y);
				control.setSize(width, size.getHeight());

				if (colors.length > 0) {
					control.setBackgroundColor(getAttributeColor(groupIndex));
					groupIndex++;
				}

				y = y + size.getHeight();

			}
		}

		private IColorConstant getAttributeColor(final int index) {
			return colors[index % colors.length];
		}

		@Override
		public Dimension getMinSize() {
			return getPreferredSize();
		}

		@Override
		public Dimension getPreferredSize() {
			if (preferredSize == null) {
				calculateSizes();
			}
			return preferredSize;
		}

		@Override
		public Dimension getMaxSize() {
			return getPreferredSize();
		}

		@Override
		public void invalidate() {
			preferredSizes.clear();
			preferredSize = null;
		}

		private void calculateSizes() {
			//final Rectangle clientArea = container.getClientArea();
			int preferredWidth = 0;
			int preferredHeight = 0;
			for (final IControl control : container.getChildren()) {
				if (!control.isVisible()) {
					continue;
				}

				final Dimension size = getPreferredSize(control);
				preferredWidth = Math.max(preferredWidth, size.getWidth());
				preferredHeight = preferredHeight + size.getHeight();
			}

			preferredSize = container.computeDecoratedSize(new Dimension(preferredWidth, preferredHeight));
		}

		private Dimension getPreferredSize(final IControl control) {
			if (!preferredSizes.containsKey(control)) {
				final Dimension size = control.getPreferredSize();
				preferredSizes.put(control, size);
			}
			return preferredSizes.get(control);
		}

	}

	private enum Visibility {
		NONE,
		PARTIAL,
		ALL
	}

	private final class AttributeGroupInformation {
		private final IAttributeGroup group;
		private final int startIndex;
		private final int endIndex;
		private final boolean sortable;
		//private final List<DisplayFormat> headerDisplayFormats;
		private final List<String> contentFormats;

		private AttributeGroupInformation(final int startIndex) {
			this.group = model.getAttribute(startIndex).getGroup();
			this.startIndex = startIndex;
			this.endIndex = getGroupEnd(model, startIndex);
			//this.headerDisplayFormats = new LinkedList<DisplayFormat>();
			this.contentFormats = new LinkedList<String>();

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

					if (!contentFormats.contains(controlPanelProvider.getDisplayFormatName())) {
						contentFormats.add(controlPanelProvider.getDisplayFormatName());
					}
				}
			}
			sortable = (sortableCount > 0);
		}

		private String getId() {
			if (group == null) {
				return null;
			}
			return group.getId();
		}

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

	private static int getGroupEnd(final IBeanTableModel<?> model, final int index) {
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
}
