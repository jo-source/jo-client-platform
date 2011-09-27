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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jowidgets.api.color.Colors;
import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.layout.tablelayout.ITableLayout;
import org.jowidgets.api.layout.tablelayout.ITableLayoutBuilder;
import org.jowidgets.api.layout.tablelayout.ITableLayoutBuilder.Alignment;
import org.jowidgets.api.layout.tablelayout.ITableLayoutBuilder.ColumnMode;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.ICheckBox;
import org.jowidgets.api.widgets.IComboBox;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.IControl;
import org.jowidgets.api.widgets.IScrollComposite;
import org.jowidgets.api.widgets.IToolBar;
import org.jowidgets.api.widgets.IToolBarButton;
import org.jowidgets.api.widgets.blueprint.ITextLabelBluePrint;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.sort.SortOrder;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeConfig;
import org.jowidgets.cap.ui.api.attribute.IAttributeGroup;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.control.DisplayFormat;
import org.jowidgets.cap.ui.api.control.IDisplayFormat;
import org.jowidgets.cap.ui.api.table.IBeanTableConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableConfigBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.tools.sort.SortModelConfigBuilder;
import org.jowidgets.common.color.ColorValue;
import org.jowidgets.common.color.IColorConstant;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.common.types.Markup;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.common.widgets.descriptor.IWidgetDescriptor;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.command.ActionBuilder;
import org.jowidgets.tools.controller.InputObservable;
import org.jowidgets.tools.converter.AbstractConverter;
import org.jowidgets.tools.widgets.wrapper.CheckBoxWrapper;
import org.jowidgets.tools.widgets.wrapper.ComboBoxWrapper;
import org.jowidgets.tools.widgets.wrapper.CompositeWrapper;
import org.jowidgets.tools.widgets.wrapper.ContainerWrapper;
import org.jowidgets.util.NullCompatibleEquivalence;

final class BeanTableAttributeListImpl extends CompositeWrapper {
	private static final String ALL_LABEL_TEXT = Messages.getString("BeanTableAttributeListImpl.all"); //$NON-NLS-1$
	private static final String SEARCH_LABEL_TEXT = Messages.getString("BeanTableAttributeListImpl.search"); //$NON-NLS-1$
	private static final String TEXT_CLEAR_SORTING = Messages.getString("BeanTableAttributeListImpl.no_sorting"); //$NON-NLS-1$
	private static final String DEFAULT_GROUP_NAME = Messages.getString("BeanTableAttributeListImpl.default"); //$NON-NLS-1$
	private static final String ALL_VISIBLE = Messages.getString("BeanTableAttributeListImpl.all_visible"); //$NON-NLS-1$
	private static final String ALL_HIDDEN = Messages.getString("BeanTableAttributeListImpl.all_hidden"); //$NON-NLS-1$
	private static final String VISIBLE_HIDDEN = Messages.getString("BeanTableAttributeListImpl.n_visible_m_hidden"); //$NON-NLS-1$

	private static final boolean USE_AUTO_COMPLETION = false;

	private static final IColorConstant ATTRIBUTE_HEADER_BACKGROUND = new ColorValue(6, 27, 95);
	private static final IColorConstant ATTRIBUTE_GROUP_BACKGROUND = new ColorValue(130, 177, 236);
	private static final IColorConstant[] ALTERNATING_GROUPS = new IColorConstant[] {
			new ColorValue(130, 177, 236), new ColorValue(115, 162, 255)};

	private static final IColorConstant[] ALTERNATING_ATTRIBUTES = new IColorConstant[] {
			Colors.DEFAULT_TABLE_EVEN_BACKGROUND_COLOR, Colors.WHITE};

	private static final ITextLabelBluePrint LABEL_HEADER = Toolkit.getBluePrintFactory().textLabel().setColor(Colors.WHITE).setMarkup(
			Markup.STRONG).alignCenter();
	private static final ITextLabelBluePrint LABEL_GROUPS = Toolkit.getBluePrintFactory().textLabel().setMarkup(Markup.STRONG).setColor(
			Colors.WHITE);
	private static final ITextLabelBluePrint LABEL_ATTRIBUTES = Toolkit.getBluePrintFactory().textLabel();
	private static final ITextLabelBluePrint LABEL_ALL = Toolkit.getBluePrintFactory().textLabel().setMarkup(Markup.STRONG);

	private static final IconsSmall ICON_EXPANDED = IconsSmall.TABLE_SORT_DESC;
	private static final IconsSmall ICON_COLLAPED = IconsSmall.TABLE_SORT_ASC;

	private final List<String> groupNames;
	private final Map<String, AttributeGroupComposite> groups;
	private final Map<String, AttributeComposite> attributeComposites;
	private final ITableLayout attributeLayoutManager;
	private final IScrollComposite attributeScroller;

	private final IDisplayFormat emptyDisplayFormat;

	private final AllAttributesComposite allAttributesComposite;
	private final AttributesFilterComposite attributesFilterComposite;

	private final int maxSortingLength;
	private final SortIndexModel currentSortingModel;
	private final SortIndexModel defaultSortingModel;

	private final IInputListener updateHeadersListener;

	private boolean eventsDisabled;
	private boolean layoutingEnabled;

	BeanTableAttributeListImpl(final IComposite container, final IBeanTableModel<?> model) {
		super(container);
		final AllAttributeInformation allAttributeInformation = new AllAttributeInformation(model);

		this.emptyDisplayFormat = createEmptyDisplayFormat();

		final ITableLayoutBuilder builder = Toolkit.getLayoutFactoryProvider().tableLayoutBuilder();
		builder.columnCount(10);
		builder.gap(20);
		builder.verticalGap(4);

		builder.alignment(2, Alignment.CENTER); // center visibility checkbox
		builder.gapBeforeColumn(0, 10);
		builder.gapAfterColumn(0, 10);
		builder.gapBeforeColumn(7, 2); // reduce gap between in sort order
		builder.gapBeforeColumn(9, 2); // reduce gap between in sort order
		builder.gapAfterColumn(9, 10);
		builder.fixedColumnWidth(7, 50); // fixed width for sort order
		builder.fixedColumnWidth(9, 50); // fixed width for sort order

		if (allAttributeInformation.getHeaderFormats().isEmpty()) {
			builder.gapBeforeColumn(3, 0);
			builder.columnMode(3, ColumnMode.HIDDEN);
		}
		if (allAttributeInformation.getContentFormats().isEmpty()) {
			builder.gapBeforeColumn(4, 0);
			builder.columnMode(4, ColumnMode.HIDDEN);
		}
		if (!allAttributeInformation.isSortable()) {
			builder.gapBeforeColumn(6, 0);
			builder.columnMode(6, ColumnMode.HIDDEN);
			builder.gapBeforeColumn(7, 0);
			builder.columnMode(7, ColumnMode.HIDDEN);
			builder.gapBeforeColumn(7, 0);
			builder.columnMode(8, ColumnMode.HIDDEN);
			builder.gapBeforeColumn(9, 0);
			builder.columnMode(9, ColumnMode.HIDDEN);
		}

		if (isSingleGroup(model)) {
			// hide toolbar column if no groups exist
			builder.columnMode(0, ColumnMode.HIDDEN);
			builder.gapAfterColumn(0, 0); // no gap after hidden column
		}

		this.attributeLayoutManager = builder.build();
		disableLayouting();

		this.groups = new HashMap<String, AttributeGroupComposite>();
		this.attributeComposites = new HashMap<String, AttributeComposite>();

		//Toolkit.getLayoutFactoryProvider().migLayoutBuilder().constraints("hidemode 2").columnConstraints("[grow]").rowConstraints("[]0[]0[]0[grow, 0:500:]").build();

		this.setLayout(new MigLayoutDescriptor("hidemode 2", "[grow]", "[]0[]0[]0[grow, 0:500:]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.maxSortingLength = getMaxSortableLength(model);

		updateHeadersListener = new IInputListener() {
			@Override
			public void inputChanged() {
				updateHeaders();
			}
		};

		currentSortingModel = new SortIndexModel(new ISortIndexModelProvider() {

			@Override
			public ComboBox<ComboBoxSortOrder> getSort(final AbstractListElement<?> composite) {
				return composite.getCurrentSorting();
			}

			@Override
			public SortingIndexComboBox getSortIndex(final AbstractListElement<?> composite) {
				return composite.getCurrentSortingIndex();
			}

		});
		defaultSortingModel = new SortIndexModel(new ISortIndexModelProvider() {

			@Override
			public ComboBox<ComboBoxSortOrder> getSort(final AbstractListElement<?> composite) {
				return composite.getDefaultSorting();
			}

			@Override
			public SortingIndexComboBox getSortIndex(final AbstractListElement<?> composite) {
				return composite.getDefaultSortingIndex();
			}

		});

		final IBluePrintFactory bpF = Toolkit.getBluePrintFactory();

		new AttributeHeaderComposite(add(bpF.composite(), "grow, wrap")); //$NON-NLS-1$
		allAttributesComposite = new AllAttributesComposite(add(bpF.composite(), "grow, wrap"), //$NON-NLS-1$
			attributeLayoutManager,
			model,
			allAttributeInformation);
		attributesFilterComposite = new AttributesFilterComposite(add(bpF.composite(), "grow, wrap"), //$NON-NLS-1$
			attributeLayoutManager,
			model,
			new FilterInformation(allAttributeInformation));
		attributesFilterComposite.setVisible(false);

		attributeScroller = this.add(bpF.scrollComposite().setHorizontalBar(false), "grow, w 0::, h 0::"); //$NON-NLS-1$
		attributeScroller.setLayout(Toolkit.getLayoutFactoryProvider().listLayoutBuilder().build());

		groupNames = new LinkedList<String>();

		AttributeGroupHeader lastHeader = null;
		for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
			final IAttribute<?> attribute = model.getAttribute(columnIndex);

			final String attributeGroupId;
			if (attribute.getGroup() != null) {
				attributeGroupId = attribute.getGroup().getId();
			}
			else {
				attributeGroupId = DEFAULT_GROUP_NAME;
			}

			AttributeGroupComposite groupContainer = groups.get(attributeGroupId);
			if (groupContainer == null) {
				final AttributeGroupInformation information = new AttributeGroupInformation(columnIndex, model);
				groupNames.add(information.getLabelText());
				groupContainer = new AttributeGroupComposite(attributeScroller.add(bpF.composite()), model, information);
				groups.put(attributeGroupId, groupContainer);

				final AttributeGroupHeader header = groupContainer.getHeader();
				if (lastHeader != null) {
					lastHeader.setLayoutConstraints(true);
				}
				else {
					header.setLayoutConstraints(false);
				}
				lastHeader = header;
			}

			// add item
			final AttributeComposite attributeComposite = new AttributeComposite(
				groupContainer.add(bpF.composite()),
				attributeLayoutManager,
				model,
				attribute);
			attributeComposites.put(attribute.getPropertyName(), attributeComposite);
		}
		enableLayouting();

		attributeLayoutManager.validate();
		setSizes();
	}

	private boolean isSingleGroup(final IBeanTableModel<?> model) {
		IAttributeGroup group = null;
		for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
			final IAttribute<?> attribute = model.getAttribute(columnIndex);
			if (columnIndex == 0) {
				group = attribute.getGroup();
			}
			else {
				final IAttributeGroup currentGroup = attribute.getGroup();
				if (group == null && currentGroup == null) {
					continue;
				}
				if (group == null || currentGroup == null) {
					return false;
				}

				if (!group.getId().equals(currentGroup.getId())) {
					return false;
				}
			}
		}

		return true;
	}

	private int getMaxSortableLength(final IBeanTableModel<?> model) {
		int result = 0;
		for (int i = 0; i < model.getColumnCount(); i++) {
			final IAttribute<Object> attribute = model.getAttribute(i);
			if (attribute.isSortable()) {
				result++;
			}
		}
		return result;
	}

	void updateValues(final IBeanTableConfig currentConfig) {
		eventsDisabled = true;
		currentSortingModel.clearSortIndices();
		defaultSortingModel.clearSortIndices();

		for (final Entry<String, AttributeComposite> entry : attributeComposites.entrySet()) {
			final AttributeComposite composite = entry.getValue();
			composite.updateValues(currentConfig);
		}

		int index = 0;
		for (final ISort sort : currentConfig.getSortModelConfig().getCurrentSorting()) {
			final AttributeComposite composite = attributeComposites.get(sort.getPropertyName());
			if (composite.isSortable()) {
				composite.getCurrentSorting().setValue(ComboBoxSortOrder.convert(sort.getSortOrder()));

				composite.getCurrentSortingIndex().setIndex(index + 1);
			}

			index++;
		}

		index = 0;
		for (final ISort sort : currentConfig.getSortModelConfig().getDefaultSorting()) {
			final AttributeComposite composite = attributeComposites.get(sort.getPropertyName());
			if (composite.isSortable()) {
				composite.getDefaultSorting().setValue(ComboBoxSortOrder.convert(sort.getSortOrder()));
				composite.getDefaultSortingIndex().setIndex(index + 1);
			}
			index++;
		}
		currentSortingModel.sortingLength = currentConfig.getSortModelConfig().getCurrentSorting().size();
		currentSortingModel.updateSortingIndexRanges();

		defaultSortingModel.sortingLength = currentConfig.getSortModelConfig().getDefaultSorting().size();
		defaultSortingModel.updateSortingIndexRanges();

		eventsDisabled = false;
		updateHeaders();
	}

	void updateHeaders() {
		if (eventsDisabled) {
			return;
		}

		eventsDisabled = true;
		for (final Entry<String, AttributeGroupComposite> entry : groups.entrySet()) {
			final AttributeGroupComposite composite = entry.getValue();
			composite.getHeader().updateValues();
		}

		allAttributesComposite.updateValues();
		attributesFilterComposite.updateValues();
		eventsDisabled = false;
	}

	private final class AttributeGroupComposite extends CompositeWrapper {

		private final AttributeGroupHeader header;
		private final IComposite composite;
		private boolean collapsed;

		public AttributeGroupComposite(
			final IComposite widget,
			final IBeanTableModel<?> model,
			final AttributeGroupInformation information) {
			super(widget);
			setLayout(Toolkit.getLayoutFactoryProvider().listLayoutBuilder().build());
			header = new AttributeGroupHeader(
				super.add(Toolkit.getBluePrintFactory().composite()),
				attributeLayoutManager,
				model,
				information);

			composite = super.add(Toolkit.getBluePrintFactory().composite());
			composite.setLayout(Toolkit.getLayoutFactoryProvider().listLayoutBuilder().backgroundColors(ALTERNATING_ATTRIBUTES).build());
		}

		public AttributeGroupHeader getHeader() {
			return header;
		}

		@Override
		public <WIDGET_TYPE extends IControl> WIDGET_TYPE add(
			final IWidgetDescriptor<? extends WIDGET_TYPE> descriptor,
			final Object layoutConstraints) {
			return composite.add(descriptor, layoutConstraints);
		}

		@Override
		public <WIDGET_TYPE extends IControl> WIDGET_TYPE add(
			final ICustomWidgetCreator<WIDGET_TYPE> creator,
			final Object layoutConstraints) {
			return composite.add(creator, layoutConstraints);
		}

		@Override
		public <WIDGET_TYPE extends IControl> WIDGET_TYPE add(final IWidgetDescriptor<? extends WIDGET_TYPE> descriptor) {
			return composite.add(descriptor);
		}

		@Override
		public <WIDGET_TYPE extends IControl> WIDGET_TYPE add(final ICustomWidgetCreator<WIDGET_TYPE> creator) {
			return composite.add(creator);
		}

		public void setCollapsed(final boolean collapsed) {
			if (collapsed != this.collapsed) {
				this.collapsed = collapsed;
				composite.setLayoutConstraints(!collapsed);
				refreshAttributeLayout(false);
			}
		}

		public boolean isCollapsed() {
			return collapsed;
		}

		public void refreshAttributeLayout(final boolean globalRefresh) {
			if (layoutingEnabled()) {
				if (!collapsed) {
					composite.setVisible(true);
					composite.layoutBegin();
					composite.layoutEnd();
					layoutBegin();
					layoutEnd();
				}
				else if (composite.isVisible()) {
					composite.setVisible(false);
					layoutBegin();
					layoutEnd();
				}

				if (!globalRefresh) {
					getParent().layoutBegin();
					getParent().layoutEnd();
					updateHeaderColors();
				}
			}
		}
	}

	private final class AttributeHeaderComposite extends ContainerWrapper {

		public AttributeHeaderComposite(final IContainer container) {
			super(container);
			setBackgroundColor(ATTRIBUTE_HEADER_BACKGROUND);
			setLayout(attributeLayoutManager.rowBuilder().build());

			add(LABEL_HEADER.setText("")); //$NON-NLS-1$
			add(LABEL_HEADER.setText(Messages.getString("BeanTableAttributeListImpl.name"))); //$NON-NLS-1$
			add(LABEL_HEADER.setText(Messages.getString("BeanTableAttributeListImpl.visible"))); //$NON-NLS-1$
			add(LABEL_HEADER.setText(Messages.getString("BeanTableAttributeListImpl.header_format"))); //$NON-NLS-1$
			add(LABEL_HEADER.setText(Messages.getString("BeanTableAttributeListImpl.content_format"))); //$NON-NLS-1$
			add(LABEL_HEADER.setText(Messages.getString("BeanTableAttributeListImpl.alignment"))); //$NON-NLS-1$
			add(LABEL_HEADER.setText(Messages.getString("BeanTableAttributeListImpl.current_sorting")), "span 2"); //$NON-NLS-1$ //$NON-NLS-2$
			add(LABEL_HEADER.setText(Messages.getString("BeanTableAttributeListImpl.default_sorting")), "span 2"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private abstract class AbstractListElement<TYPE> extends CompositeWrapper {
		private final TYPE information;
		private final VisibilityCheckBox visible;
		private final ComboBox<IDisplayFormat> headerFormat;
		private final ComboBox<IDisplayFormat> contentFormat;
		private final ComboBox<ComboBoxAlignmentHorizontal> columnAlignment;
		private final ComboBox<ComboBoxSortOrder> currentSorting;
		private final SortingIndexComboBox currentSortingIndex;
		private final ComboBox<ComboBoxSortOrder> defaultSorting;
		private final SortingIndexComboBox defaultSortingIndex;
		private final IToolBarButton collapseButton;

		private AbstractListElement(
			final IComposite container,
			final ITableLayout attributeLayoutManager,
			final IBeanTableModel<?> model,
			final TYPE information,
			final ITextLabelBluePrint textLabel) {
			super(container);
			this.information = information;
			final IBluePrintFactory bpF = Toolkit.getBluePrintFactory();
			setLayout(attributeLayoutManager.rowBuilder().build());

			if (hasCollapseButton()) {
				final IToolBar toolBar = container.add(bpF.toolBar());
				collapseButton = toolBar.addAction(createCollapseAction());
			}
			else {
				container.add(bpF.textLabel());
				collapseButton = null;
			}

			add(textLabel.setText(getLabelText()));

			visible = new VisibilityCheckBox(add(bpF.checkBox()));
			final IInputListener visibleChangedListener = createVisibleChangedListener();
			if (visibleChangedListener != null) {
				visible.addInputListener(visibleChangedListener);
			}

			headerFormat = createHeaderFormatComboBox();
			if (headerFormat != null) {
				setMaxWidth(headerFormat, 300);
				headerFormat.addInputListener(createHeaderChangedListener());
			}
			else {
				addNotAvailableLabel(textLabel);
			}

			contentFormat = createContentFormatComboBox();
			if (contentFormat != null) {
				setMaxWidth(contentFormat, 300);
				contentFormat.addInputListener(createContentChangedListener());
			}
			else {
				addNotAvailableLabel(textLabel);
			}

			columnAlignment = createAlignmentComboBox();
			if (columnAlignment != null) {
				setMaxWidth(columnAlignment, 200);
				columnAlignment.addInputListener(createAlignmentChangedListener());
			}
			else {
				addNotAvailableLabel(textLabel);
			}

			if (isSortable()) {
				currentSorting = createSortOrderComboBox();
				setMaxWidth(currentSorting, 250);
				currentSorting.addInputListener(createCurrentSortingChangedListener());
				if (hasSortPriority()) {
					currentSortingIndex = new SortingIndexComboBox(add(bpF.comboBoxSelection().setAutoCompletion(
							USE_AUTO_COMPLETION)));
					currentSortingIndex.addInputListener(createCurrentSortingIndexChangedListener());
				}
				else {
					currentSortingIndex = null;
					addNotAvailableLabel(textLabel);
				}

				defaultSorting = createSortOrderComboBox();
				setMaxWidth(defaultSorting, 250);
				defaultSorting.addInputListener(createDefaultSortingChangedListener());
				if (hasSortPriority()) {
					defaultSortingIndex = new SortingIndexComboBox(add(bpF.comboBoxSelection().setAutoCompletion(
							USE_AUTO_COMPLETION)));
					defaultSortingIndex.addInputListener(createDefaultSortingIndexChangedListener());
				}
				else {
					defaultSortingIndex = null;
					addNotAvailableLabel(textLabel);
				}
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

		protected ComboBox<IDisplayFormat> createHeaderFormatComboBox() {
			final List<IDisplayFormat> headerFormats = getHeaderFormats();
			if (headerFormats.size() > 1) {
				final ComboBox<IDisplayFormat> result = new ComboBox<IDisplayFormat>(
					add(Toolkit.getBluePrintFactory().comboBoxSelection(new HeaderDisplayFormatConverter()).setAutoCompletion(
							USE_AUTO_COMPLETION)));
				result.setElements(headerFormats);
				return result;
			}
			else {
				return null;
			}
		}

		protected ComboBox<IDisplayFormat> createContentFormatComboBox() {
			final List<IDisplayFormat> contentFormats = getContentFormats();
			if (contentFormats.size() > 1) {
				final ComboBox<IDisplayFormat> result = new ComboBox<IDisplayFormat>(
					add(Toolkit.getBluePrintFactory().comboBoxSelection(new ContentDisplayFormatConverter(contentFormats)).setAutoCompletion(
							USE_AUTO_COMPLETION)));
				result.setElements(contentFormats);
				return result;
			}
			else {
				return null;
			}
		}

		protected ComboBox<ComboBoxAlignmentHorizontal> createAlignmentComboBox() {
			final List<ComboBoxAlignmentHorizontal> alignments = getAlignments();
			final ComboBox<ComboBoxAlignmentHorizontal> result = new ComboBox<ComboBoxAlignmentHorizontal>(
				add(Toolkit.getBluePrintFactory().comboBoxSelection(new AlignmentConverter()).setAutoCompletion(
						USE_AUTO_COMPLETION)));
			result.setElements(alignments);
			return result;
		}

		protected ComboBox<ComboBoxSortOrder> createSortOrderComboBox() {
			final List<ComboBoxSortOrder> sortOrders = getSortOrders();
			final ComboBox<ComboBoxSortOrder> result = new ComboBox<ComboBoxSortOrder>(
				add(Toolkit.getBluePrintFactory().comboBoxSelection(new SortOrderConverter()).setAutoCompletion(
						USE_AUTO_COMPLETION)));
			result.setElements(sortOrders);
			return result;
		}

		protected void setMaxWidth(final ComboBox<?> comboBox, final int width) {
			comboBox.setMaxSize(new Dimension(width, comboBox.getPreferredSize().getHeight()));
		}

		protected TYPE getData() {
			return information;
		}

		protected IInputListener createHeaderChangedListener() {
			return updateHeadersListener;
		}

		protected IInputListener createContentChangedListener() {
			return updateHeadersListener;
		}

		protected IInputListener createAlignmentChangedListener() {
			return updateHeadersListener;
		}

		protected IInputListener createVisibleChangedListener() {
			return updateHeadersListener;
		}

		protected abstract String getLabelText();

		protected abstract List<IDisplayFormat> getHeaderFormats();

		protected abstract List<IDisplayFormat> getContentFormats();

		protected abstract boolean isSortable();

		protected abstract boolean hasSortPriority();

		protected IInputListener createCurrentSortingChangedListener() {
			return createSortingChangedListener(currentSortingModel);
		}

		protected IInputListener createDefaultSortingChangedListener() {
			return createSortingChangedListener(defaultSortingModel);
		}

		protected IInputListener createSortingChangedListener(final SortIndexModel model) {
			return null;
		}

		protected IInputListener createCurrentSortingIndexChangedListener() {
			return createSortingIndexChangedListener(currentSortingModel);
		}

		protected IInputListener createDefaultSortingIndexChangedListener() {
			return createSortingIndexChangedListener(defaultSortingModel);
		}

		protected IInputListener createSortingIndexChangedListener(final SortIndexModel model) {
			return null;
		}

		protected boolean hasCollapseButton() {
			return false;
		}

		protected IAction createCollapseAction() {
			return null;
		}

		protected void addNotAvailableLabel(final ITextLabelBluePrint textLabel) {
			final AlignmentHorizontal alignment = textLabel.getAlignment();
			add(textLabel.alignCenter().setText("-")); //$NON-NLS-1$
			textLabel.setAlignment(alignment);
		}

		public VisibilityCheckBox getVisible() {
			return visible;
		}

		public ComboBox<IDisplayFormat> getHeaderFormat() {
			return headerFormat;
		}

		public ComboBox<IDisplayFormat> getContentFormat() {
			return contentFormat;
		}

		public ComboBox<ComboBoxAlignmentHorizontal> getColumnAlignment() {
			return columnAlignment;
		}

		public ComboBox<ComboBoxSortOrder> getCurrentSorting() {
			return currentSorting;
		}

		public SortingIndexComboBox getCurrentSortingIndex() {
			return currentSortingIndex;
		}

		public ComboBox<ComboBoxSortOrder> getDefaultSorting() {
			return defaultSorting;
		}

		public SortingIndexComboBox getDefaultSortingIndex() {
			return defaultSortingIndex;
		}

		public IToolBarButton getCollapseButton() {
			return collapseButton;
		}

	}

	private final class AttributeComposite extends AbstractListElement<IAttribute<?>> {

		private final String propertyName;

		private AttributeComposite(
			final IComposite container,
			final ITableLayout tableLayout,
			final IBeanTableModel<?> model,
			final IAttribute<?> attribute) {
			super(container, tableLayout, model, attribute, LABEL_ATTRIBUTES);
			propertyName = attribute.getPropertyName();
		}

		private IAttribute<?> getAttribute() {
			return getData();
		}

		@Override
		protected String getLabelText() {
			return getAttribute().getLabel();
		}

		@Override
		protected List<IDisplayFormat> getHeaderFormats() {
			final List<IDisplayFormat> result = new LinkedList<IDisplayFormat>();
			if (hasShortAndLongLabel(getAttribute())) {
				result.add(DisplayFormat.SHORT);
				result.add(DisplayFormat.LONG);
			}
			return result;
		}

		@Override
		protected List<IDisplayFormat> getContentFormats() {
			final List<IDisplayFormat> result = new LinkedList<IDisplayFormat>();
			for (final IControlPanelProvider<?> provider : getAttribute().getControlPanels()) {
				result.add(provider.getDisplayFormat());
			}
			return result;
		}

		@Override
		protected boolean isSortable() {
			return getAttribute().isSortable();
		}

		public void updateValues(final IBeanTableConfig currentConfig) {
			final IAttributeConfig attributeConfig = currentConfig.getAttributeConfigs().get(propertyName);
			if (attributeConfig == null) {
				throw new IllegalStateException("No configuration set for '" + propertyName + "'."); //$NON-NLS-1$ //$NON-NLS-2$
			}

			if (getHeaderFormat() != null) {
				DisplayFormat format = getHeaderFormatConfig(attributeConfig);
				if ((format != null) && (!getHeaderFormat().getElements().contains(format))) {
					format = DisplayFormat.SHORT;

				}
				getHeaderFormat().setValue(format);
			}

			if (getContentFormat() != null) {
				final IDisplayFormat format = getContentFormatConfig(attributeConfig, getAttribute());
				getContentFormat().setValue(format);
			}

			getVisible().setSelected(attributeConfig.isVisible());

			if (getColumnAlignment() != null) {
				getColumnAlignment().setValue(ComboBoxAlignmentHorizontal.convert(attributeConfig.getTableAlignment()));
			}
		}

		protected DisplayFormat getHeaderFormatConfig(final IAttributeConfig attributeConfig) {
			return attributeConfig.getLabelDisplayFormat();
		}

		protected IDisplayFormat getContentFormatConfig(final IAttributeConfig attributeConfig, final IAttribute<?> attribute) {
			return attributeConfig.getDisplayFormat();
		}

		@Override
		protected IInputListener createSortingChangedListener(final SortIndexModel model) {
			return new IInputListener() {
				@Override
				public void inputChanged() {
					final ComboBox<ComboBoxSortOrder> comboBox = model.provider.getSort(attributeComposites.get(propertyName));
					final ComboBoxSortOrder value = comboBox.getValue();
					final boolean hasToAdd = comboBox.getLastValue() == null
						|| comboBox.getLastValue() == ComboBoxSortOrder.NOT_SET;
					final boolean hasToRemove = value == ComboBoxSortOrder.NOT_SET;

					if (hasToAdd) {
						model.insertSortIndex(model.sortingLength + 1, propertyName);
					}
					else if (hasToRemove) {
						model.removeSortIndex(
								model.provider.getSortIndex(attributeComposites.get(propertyName)).getIndex(),
								propertyName);
					}

					if (hasToAdd || hasToRemove) {
						model.updateSortingIndexRanges();
					}
				}
			};
		}

		@Override
		protected IInputListener createSortingIndexChangedListener(final SortIndexModel model) {
			return new IInputListener() {

				@Override
				public void inputChanged() {
					final AttributeComposite composite = attributeComposites.get(propertyName);
					final SortingIndexComboBox comboBox = model.provider.getSortIndex(composite);
					final int previousIndex = comboBox.getPreviousIndex();
					final int newIndex = comboBox.getIndex();
					if (newIndex == previousIndex) {
						return;
					}

					final boolean hasToAdd = (previousIndex == 0);
					final boolean hasToRemove = (newIndex == 0);

					if (hasToRemove) {
						model.removeSortIndex(previousIndex, propertyName);
						model.provider.getSort(composite).setValue(ComboBoxSortOrder.NOT_SET);
					}
					else if (hasToAdd) {
						model.insertSortIndex(newIndex, propertyName);
						model.provider.getSort(composite).setValue(ComboBoxSortOrder.ASC);
					}
					else {
						model.shiftSortingIndex(newIndex, previousIndex, propertyName);
					}
					model.updateSortingIndexRanges();
				}
			};
		}

		@Override
		protected boolean hasSortPriority() {
			return true;
		}

	}

	private class AttributesHeaderComposite<TYPE extends IAttributesInformation> extends AbstractListElement<TYPE> {

		private AttributeGroupComposite groupComposite;

		public AttributesHeaderComposite(
			final IComposite container,
			final ITableLayout attributeLayoutManager,
			final IBeanTableModel<?> model,
			final TYPE information,
			final ITextLabelBluePrint textLabel) {
			super(container, attributeLayoutManager, model, information, textLabel);
			setBackgroundColor(ATTRIBUTE_GROUP_BACKGROUND);
		}

		@Override
		protected HeaderComboBox<IDisplayFormat> createHeaderFormatComboBox() {
			final List<IDisplayFormat> headerFormats = getHeaderFormats();
			if (headerFormats.size() > 1) {
				final HeaderComboBox<IDisplayFormat> result = new HeaderComboBox<IDisplayFormat>(
					add(Toolkit.getBluePrintFactory().comboBoxSelection(new HeaderDisplayFormatConverter()).setAutoCompletion(
							USE_AUTO_COMPLETION)),
					emptyDisplayFormat,
					null);
				result.setElements(headerFormats);
				return result;
			}
			else {
				return null;
			}
		}

		@Override
		protected HeaderComboBox<IDisplayFormat> createContentFormatComboBox() {
			final List<IDisplayFormat> contentFormats = getContentFormats();
			if (contentFormats.size() > 1) {
				final HeaderComboBox<IDisplayFormat> result = new HeaderComboBox<IDisplayFormat>(
					add(Toolkit.getBluePrintFactory().comboBoxSelection(new ContentDisplayFormatConverter(contentFormats)).setAutoCompletion(
							USE_AUTO_COMPLETION)),
					emptyDisplayFormat,
					null);
				result.setElements(contentFormats);
				return result;
			}
			else {
				return null;
			}
		}

		@Override
		protected HeaderComboBox<ComboBoxAlignmentHorizontal> createAlignmentComboBox() {
			final List<ComboBoxAlignmentHorizontal> alignments = getAlignments();
			final HeaderComboBox<ComboBoxAlignmentHorizontal> result = new HeaderComboBox<ComboBoxAlignmentHorizontal>(
				add(Toolkit.getBluePrintFactory().comboBoxSelection(new AlignmentConverter()).setAutoCompletion(
						USE_AUTO_COMPLETION)),
				ComboBoxAlignmentHorizontal.VARIOUS,
				null);
			result.setElements(alignments);
			return result;
		}

		@Override
		protected HeaderComboBox<ComboBoxSortOrder> createSortOrderComboBox() {
			final List<ComboBoxSortOrder> sortOrders = getSortOrders();
			final HeaderComboBox<ComboBoxSortOrder> result = new HeaderComboBox<ComboBoxSortOrder>(
				add(Toolkit.getBluePrintFactory().comboBoxSelection(new SortOrderConverter()).setAutoCompletion(
						USE_AUTO_COMPLETION)),
				ComboBoxSortOrder.NOT_SET,
				ComboBoxSortOrder.CLEAR_SORTING);
			result.setElements(sortOrders);
			return result;
		}

		@Override
		public HeaderComboBox<IDisplayFormat> getHeaderFormat() {
			return (HeaderComboBox<IDisplayFormat>) super.getHeaderFormat();
		}

		@Override
		public HeaderComboBox<IDisplayFormat> getContentFormat() {
			return (HeaderComboBox<IDisplayFormat>) super.getContentFormat();
		}

		@Override
		public HeaderComboBox<ComboBoxAlignmentHorizontal> getColumnAlignment() {
			return (HeaderComboBox<ComboBoxAlignmentHorizontal>) super.getColumnAlignment();
		}

		@Override
		public HeaderComboBox<ComboBoxSortOrder> getCurrentSorting() {
			return (HeaderComboBox<ComboBoxSortOrder>) super.getCurrentSorting();
		}

		@Override
		public HeaderComboBox<ComboBoxSortOrder> getDefaultSorting() {
			return (HeaderComboBox<ComboBoxSortOrder>) super.getDefaultSorting();
		}

		@Override
		public String getLabelText() {
			return getData().getLabelText();
		}

		@Override
		public List<IDisplayFormat> getHeaderFormats() {
			return getData().getHeaderFormats();
		}

		@Override
		public List<IDisplayFormat> getContentFormats() {
			return getData().getContentFormats();
		}

		@Override
		public boolean isSortable() {
			return getData().isSortable();
		}

		@Override
		public boolean hasCollapseButton() {
			return getData().isCollapsable();
		}

		@Override
		public boolean hasSortPriority() {
			return false;
		}

		public void updateValues() {
			final String[] propertyNames = getPropertyNames();

			if (getHeaderFormat() != null) {
				final List<IDisplayFormat> formats = getHeaderFormats();
				getHeaderFormat().setElements(formats);
				getHeaderFormat().setEnabled(formats.size() > 1);
			}
			if (getContentFormat() != null) {
				final List<IDisplayFormat> formats = getContentFormats();
				getContentFormat().setElements(formats);
				getContentFormat().setEnabled(formats.size() > 1);
			}
			if (getCurrentSorting() != null) {
				getCurrentSorting().setElements(getSortOrders());
				getCurrentSorting().setEnabled(isSortable());
			}
			if (getDefaultSorting() != null) {
				getDefaultSorting().setElements(getSortOrders());
				getDefaultSorting().setEnabled(isSortable());
			}
			if (getColumnAlignment() != null) {
				getColumnAlignment().setElements(getAlignments());
				getColumnAlignment().setEnabled(propertyNames.length > 0);
			}
			if (getVisible() != null) {
				getVisible().setEnabled(propertyNames.length > 0);
				if (!getVisible().isEnabled()) {
					getVisible().setValue(false);
				}
			}

			int visibleCount = 0;
			int invisibleCount = 0;
			final Set<IDisplayFormat> usedHeaderFormats = new HashSet<IDisplayFormat>();
			final Set<IDisplayFormat> usedContentFormats = new HashSet<IDisplayFormat>();
			final Set<ComboBoxAlignmentHorizontal> usedColumnAlignments = new HashSet<ComboBoxAlignmentHorizontal>();
			final Set<ComboBoxSortOrder> usedCurrentSortings = new HashSet<ComboBoxSortOrder>();
			final Set<ComboBoxSortOrder> usedDefaultSortings = new HashSet<ComboBoxSortOrder>();
			for (final String propertyName : propertyNames) {
				final AttributeComposite attributeComposite = attributeComposites.get(propertyName);
				if (attributeComposite.getVisible().getValue()) {
					visibleCount++;
				}
				else {
					invisibleCount++;
				}

				if (attributeComposite.getHeaderFormat() != null) {
					usedHeaderFormats.add(attributeComposite.getHeaderFormat().getValue());
				}
				if (attributeComposite.getContentFormat() != null) {
					usedContentFormats.add(attributeComposite.getContentFormat().getValue());
				}

				usedColumnAlignments.add(attributeComposite.getColumnAlignment().getValue());

				if (attributeComposite.getCurrentSorting() != null) {
					usedCurrentSortings.add(attributeComposite.getCurrentSorting().getValue());
				}
				if (attributeComposite.getDefaultSorting() != null) {
					usedDefaultSortings.add(attributeComposite.getDefaultSorting().getValue());
				}
			}

			if (getHeaderFormat() != null) {
				getHeaderFormat().setValue(usedHeaderFormats);
			}
			if (getContentFormat() != null) {
				getContentFormat().setValue(usedContentFormats);
			}
			if (getColumnAlignment() != null) {
				getColumnAlignment().setValue(usedColumnAlignments);
			}

			getVisible().setState(visibleCount, invisibleCount);

			if (getContentFormat() != null) {
				getContentFormat().setValue(usedContentFormats);
			}
			if (getCurrentSorting() != null) {
				getCurrentSorting().setValue(usedCurrentSortings);
			}
			if (getDefaultSorting() != null) {
				getDefaultSorting().setValue(usedDefaultSortings);
			}
		}

		@Override
		protected IAction createCollapseAction() {
			final ActionBuilder actionBuilder = new ActionBuilder();
			actionBuilder.setIcon(IconsSmall.TABLE_SORT_DESC);
			actionBuilder.setCommand(new ICommandExecutor() {

				@Override
				public void execute(final IExecutionContext executionContext) throws Exception {
					toggleCollapseState();
				}

			});
			return actionBuilder.build();
		}

		public String[] getPropertyNames() {
			return getData().getPropertyNames();
		}

		public void toggleCollapseState() {
			setCollapsed(!isCollapsed());
		}

		public boolean isCollapsed() {
			if (getGroupComposite() != null) {
				return getGroupComposite().isCollapsed();
			}
			else {
				final IToolBarButton button = getCollapseButton();
				return ICON_COLLAPED.equals(button.getIcon());
			}
		}

		public void setCollapsed(final boolean collapsed) {
			if (!getData().isCollapsable()) {
				return;
			}

			if (getGroupComposite() != null) {
				getGroupComposite().setCollapsed(collapsed);
			}
			final IToolBarButton button = getCollapseButton();
			if (collapsed) {
				button.setIcon(ICON_COLLAPED);
			}
			else {
				button.setIcon(ICON_EXPANDED);
			}
		}

		private AttributeGroupComposite getGroupComposite() {
			if (groupComposite == null) {
				final String id = getData().getLabelText();
				groupComposite = groups.get(id);
			}
			return groupComposite;
		}

		@Override
		protected IInputListener createVisibleChangedListener() {
			return new IInputListener() {

				@Override
				public void inputChanged() {
					if (eventsDisabled) {
						return;
					}

					eventsDisabled = true;
					final boolean visible = getVisible().getValue();
					for (final String property : getPropertyNames()) {
						attributeComposites.get(property).getVisible().setValue(visible);
					}
					eventsDisabled = false;
					updateHeaders();
				}
			};
		}

		@Override
		protected IInputListener createHeaderChangedListener() {
			return new IInputListener() {

				@Override
				public void inputChanged() {
					final IDisplayFormat value = getHeaderFormat().getValue();
					for (final String property : getPropertyNames()) {
						final ComboBox<IDisplayFormat> comboBox = attributeComposites.get(property).getHeaderFormat();
						if (comboBox != null) {
							comboBox.setValue(value);
						}
					}
				}
			};
		}

		@Override
		protected IInputListener createContentChangedListener() {
			return new IInputListener() {

				@Override
				public void inputChanged() {
					final IDisplayFormat value = getContentFormat().getValue();
					for (final String property : getPropertyNames()) {
						final ComboBox<IDisplayFormat> comboBox = attributeComposites.get(property).getContentFormat();
						if (comboBox != null) {
							comboBox.setValue(value);
						}
					}
				}
			};
		}

		@Override
		protected IInputListener createAlignmentChangedListener() {
			return new IInputListener() {

				@Override
				public void inputChanged() {
					final ComboBoxAlignmentHorizontal value = getColumnAlignment().getValue();
					for (final String property : getPropertyNames()) {
						final ComboBox<ComboBoxAlignmentHorizontal> comboBox = attributeComposites.get(property).getColumnAlignment();
						if (comboBox != null) {
							comboBox.setValue(value);
						}
					}
				}
			};
		}

		@Override
		protected IInputListener createSortingChangedListener(final SortIndexModel model) {
			final AbstractListElement<?> composite = this;
			return new IInputListener() {

				@Override
				public void inputChanged() {
					final String[] properties = getPropertyNames();
					final ComboBoxSortOrder value = model.provider.getSort(composite).getValue();

					final boolean clear = ComboBoxSortOrder.CLEAR_SORTING.equals(value)
						|| ComboBoxSortOrder.NOT_SET.equals(value);
					final List<Integer> shiftList = new ArrayList<Integer>();
					for (int columnIndex = 0; columnIndex < properties.length; columnIndex++) {
						final AttributeComposite attributeComposite = attributeComposites.get(properties[columnIndex]);
						if (!attributeComposite.isSortable()) {
							continue;
						}

						final SortingIndexComboBox sortIndex = model.provider.getSortIndex(attributeComposite);
						final ComboBox<ComboBoxSortOrder> sorting = model.provider.getSort(attributeComposite);
						final boolean isEmpty = sorting.getValue() == null
							|| ComboBoxSortOrder.NOT_SET.equals(sorting.getValue());

						if (clear && !isEmpty) {
							model.sortingLength = model.sortingLength - 1;
							shiftList.add(Integer.valueOf(sortIndex.getIndex()));
							sortIndex.setIndex(0);
						}
						else if (!clear && isEmpty) {
							model.sortingLength = model.sortingLength + 1;
							if (sortIndex.range < model.sortingLength) {
								sortIndex.setRange(attributeComposites.size());
							}
							sortIndex.setIndex(model.sortingLength);
						}

						if (!clear) {
							sorting.setValue(value);
						}
						else {
							sorting.setValue(ComboBoxSortOrder.NOT_SET);
						}
					}

					if (clear && shiftList.size() > 0) {
						for (final Entry<String, AttributeComposite> entry : attributeComposites.entrySet()) {
							final AttributeComposite attributeComposite = entry.getValue();
							if (!attributeComposite.isSortable()) {
								continue;
							}

							final SortingIndexComboBox sortIndex = model.provider.getSortIndex(attributeComposite);
							final int index = sortIndex.getIndex();
							final int shift = getShift(index, shiftList);
							if (shift != 0) {
								sortIndex.setIndex(index + shift);
							}
						}
					}

					model.updateSortingIndexRanges();
				}

				private int getShift(final int index, final List<Integer> list) {
					int result = 0;
					for (final Integer shiftIndex : list) {
						if (index > shiftIndex) {
							result--;
						}
					}
					return result;
				}
			};
		}

	}

	private final class AttributeGroupHeader extends AttributesHeaderComposite<IAttributesInformation> {

		private AttributeGroupHeader(
			final IComposite container,
			final ITableLayout attributeLayoutManager,
			final IBeanTableModel<?> model,
			final IAttributesInformation information) {
			super(container, attributeLayoutManager, model, information, LABEL_GROUPS);
			setBackgroundColor(ATTRIBUTE_GROUP_BACKGROUND);
		}

	}

	private final class AttributesFilterComposite extends AttributesHeaderComposite<FilterInformation> {

		private AttributesFilterComposite(
			final IComposite container,
			final ITableLayout attributeLayoutManager,
			final IBeanTableModel<?> model,
			final FilterInformation information) {
			super(container, attributeLayoutManager, model, information, LABEL_ALL);
			setBackgroundColor(Colors.WHITE);
		}

		@Override
		protected IAction createCollapseAction() {
			final ActionBuilder actionBuilder = new ActionBuilder();
			actionBuilder.setIcon(IconsSmall.TABLE_SORT_DESC);
			actionBuilder.setCommand(new ICommandExecutor() {

				@Override
				public void execute(final IExecutionContext executionContext) throws Exception {
					final boolean collapseState = !isCollapsed();
					setCollapsed(collapseState);

					disableLayouting();
					// (un)collapse all groups
					for (final AttributeGroupComposite composite : groups.values()) {
						final AttributeGroupHeader header = composite.getHeader();
						if (!shouldControlBeVisible(header)) {
							continue;
						}

						if (header.isCollapsed() != collapseState) {
							header.setCollapsed(collapseState);
						}
					}
					enableLayouting();
					refreshLayout();
				}

			});
			return actionBuilder.build();
		}
	}

	private final class AllAttributesComposite extends AttributesHeaderComposite<AllAttributeInformation> {

		private AllAttributesComposite(
			final IComposite container,
			final ITableLayout attributeLayoutManager,
			final IBeanTableModel<?> model,
			final AllAttributeInformation information) {
			super(container, attributeLayoutManager, model, information, LABEL_ALL);
			setBackgroundColor(Colors.WHITE);
		}

		@Override
		protected IAction createCollapseAction() {
			final ActionBuilder actionBuilder = new ActionBuilder();
			actionBuilder.setIcon(IconsSmall.TABLE_SORT_DESC);
			actionBuilder.setCommand(new ICommandExecutor() {

				@Override
				public void execute(final IExecutionContext executionContext) throws Exception {
					final boolean collapseState = !isCollapsed();
					setCollapsed(collapseState);

					disableLayouting();
					// (un)collapse all groups
					for (final AttributeGroupComposite composite : groups.values()) {
						final AttributeGroupHeader header = composite.getHeader();
						if (header.isCollapsed() != collapseState) {
							header.setCollapsed(collapseState);
						}
					}
					enableLayouting();

					refreshLayout();
				}

			});
			return actionBuilder.build();
		}

	}

	private enum Visibility {
		NONE,
		PARTIAL,
		ALL;

		public static Visibility getValue(final int visibleCount, final int invisibleCount) {
			if (visibleCount == 0) {
				return NONE;
			}
			else if (invisibleCount == 0) {
				return ALL;
			}
			else {
				return PARTIAL;
			}
		}
	}

	interface IAttributesInformation {
		String getLabelText();

		String[] getPropertyNames();

		List<IDisplayFormat> getHeaderFormats();

		List<IDisplayFormat> getContentFormats();

		boolean isSortable();

		boolean isCollapsable();
	}

	private final class FilterInformation implements IAttributesInformation {
		private String[] propertyNames;
		private final List<IDisplayFormat> headerFormats;
		private final List<IDisplayFormat> contentFormats;
		private boolean sortable;

		private FilterInformation(final AllAttributeInformation allAttributeInformation) {
			propertyNames = new String[0];
			headerFormats = new LinkedList<IDisplayFormat>(allAttributeInformation.getHeaderFormats());
			contentFormats = new LinkedList<IDisplayFormat>(allAttributeInformation.getContentFormats());
			sortable = true;
		}

		@Override
		public String getLabelText() {
			return SEARCH_LABEL_TEXT;
		}

		@Override
		public String[] getPropertyNames() {
			return propertyNames;
		}

		@Override
		public List<IDisplayFormat> getHeaderFormats() {
			return headerFormats;
		}

		@Override
		public List<IDisplayFormat> getContentFormats() {
			return contentFormats;
		}

		@Override
		public boolean isSortable() {
			return sortable;
		}

		@Override
		public boolean isCollapsable() {
			return true;
		}

		public void setVisibleProperties(final List<String> visiblePropertyNames) {
			propertyNames = new String[visiblePropertyNames.size()];
			int index = 0;
			for (final String propertyName : visiblePropertyNames) {
				propertyNames[index] = propertyName;
				index++;
			}
			headerFormats.clear();
			contentFormats.clear();

			sortable = false;
			for (final String propertyName : propertyNames) {
				final AttributeComposite attributeComposite = attributeComposites.get(propertyName);
				sortable = sortable || attributeComposite.isSortable();

				for (final IDisplayFormat format : attributeComposite.getHeaderFormats()) {
					if (headerFormats.contains(format)) {
						continue;
					}
					headerFormats.add(format);
				}

				for (final IDisplayFormat format : attributeComposite.getContentFormats()) {
					if (contentFormats.contains(format)) {
						continue;
					}
					contentFormats.add(format);
				}
			}
		}

	}

	private final class AllAttributeInformation implements IAttributesInformation {
		private final boolean sortable;
		private final String[] propertyNames;
		private final List<IDisplayFormat> headerFormats;
		private final List<IDisplayFormat> contentFormats;

		private AllAttributeInformation(final IBeanTableModel<?> model) {
			this.propertyNames = new String[model.getColumnCount()];
			this.headerFormats = new LinkedList<IDisplayFormat>();
			this.contentFormats = new LinkedList<IDisplayFormat>();

			int sortableCount = 0;
			boolean hasShortAndLongHeaderFormats = false;
			for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
				final IAttribute<?> attribute = model.getAttribute(columnIndex);
				propertyNames[columnIndex] = attribute.getPropertyName();
				if (attribute.isSortable()) {
					sortableCount++;
				}

				if (attribute.getControlPanels().size() > 1) {
					for (final IControlPanelProvider<?> provider : attribute.getControlPanels()) {
						if (contentFormats.contains(provider.getDisplayFormat())) {
							continue;
						}

						contentFormats.add(provider.getDisplayFormat());
					}
				}

				hasShortAndLongHeaderFormats = hasShortAndLongHeaderFormats || (hasShortAndLongLabel(attribute));
			}

			if (hasShortAndLongHeaderFormats) {
				headerFormats.add(DisplayFormat.SHORT);
				headerFormats.add(DisplayFormat.LONG);
			}

			sortable = sortableCount > 0;
		}

		@Override
		public String getLabelText() {
			return ALL_LABEL_TEXT;
		}

		@Override
		public String[] getPropertyNames() {
			return propertyNames;
		}

		@Override
		public List<IDisplayFormat> getHeaderFormats() {
			return headerFormats;
		}

		@Override
		public List<IDisplayFormat> getContentFormats() {
			return contentFormats;
		}

		@Override
		public boolean isSortable() {
			return sortable;
		}

		@Override
		public boolean isCollapsable() {
			return true;
		}
	}

	private final class AttributeGroupInformation implements IAttributesInformation {
		private final IAttributeGroup group;
		private final boolean sortable;
		private final String[] propertyNames;
		private final List<IDisplayFormat> headerFormats;
		private final List<IDisplayFormat> contentFormats;

		private AttributeGroupInformation(final int startIndex, final IBeanTableModel<?> model) {
			this.group = model.getAttribute(startIndex).getGroup();
			final int endIndex = getGroupEnd(model, startIndex);
			this.propertyNames = new String[endIndex - startIndex + 1];
			this.headerFormats = new LinkedList<IDisplayFormat>();
			this.contentFormats = new LinkedList<IDisplayFormat>();

			int sortableCount = 0;
			int propIndex = 0;
			boolean hasLongLabel = false;
			for (int index = startIndex; index <= endIndex; index++) {
				final IAttribute<?> attribute = model.getAttribute(index);
				propertyNames[propIndex] = attribute.getPropertyName();

				if (attribute.isSortable()) {
					sortableCount++;
				}

				if ((!hasLongLabel) && (hasShortAndLongLabel(attribute))) {
					hasLongLabel = true;
				}

				final List<?> controlPanels = attribute.getControlPanels();
				if (controlPanels.size() > 1) {
					for (final Object provider : controlPanels) {
						final IControlPanelProvider<?> controlPanelProvider = (IControlPanelProvider<?>) provider;

						if (!contentFormats.contains(controlPanelProvider.getDisplayFormat())) {
							contentFormats.add(controlPanelProvider.getDisplayFormat());
						}
					}
				}

				propIndex++;
			}

			if (hasLongLabel) {
				headerFormats.add(DisplayFormat.SHORT);
				headerFormats.add(DisplayFormat.LONG);
			}

			sortable = (sortableCount > 0);
		}

		@Override
		public String[] getPropertyNames() {
			return propertyNames;
		}

		@Override
		public List<IDisplayFormat> getHeaderFormats() {
			return headerFormats;
		}

		@Override
		public List<IDisplayFormat> getContentFormats() {
			return contentFormats;
		}

		@Override
		public String getLabelText() {
			if (group == null) {
				return DEFAULT_GROUP_NAME;
			}
			return group.getId();
		}

		@Override
		public boolean isSortable() {
			return sortable;
		}

		@Override
		public boolean isCollapsable() {
			return true;
		}

		private int getGroupEnd(final IBeanTableModel<?> model, final int index) {
			int currentIndex = index + 1;
			while (currentIndex < model.getColumnCount()) {
				final IAttributeGroup currentGroup = model.getAttribute(currentIndex).getGroup();
				if ((group != null) && (currentGroup != null)) {
					if (!NullCompatibleEquivalence.equals(group.getId(), currentGroup.getId())) {
						break;
					}
				}
				else if (group != null || currentGroup != null) {
					break;
				}

				currentIndex++;
			}
			return currentIndex - 1;
		}
	}

	private static boolean hasShortAndLongLabel(final IAttribute<?> attribute) {
		return ((attribute.getLabelLong() != null) && (!attribute.getLabelLong().equals(attribute.getLabel())));
	}

	//	private static String getDisplayFormatNameById(final Object id, final IAttribute<?> attribute) {
	//		for (final IControlPanelProvider<?> provider : attribute.getControlPanels()) {
	//			if (provider.getDisplayFormat().getId().equals(id)) {
	//				return provider.getDisplayFormat().getName();
	//			}
	//		}
	//		return null;
	//	}

	private class ComboBox<TYPE> extends ComboBoxWrapper<TYPE> {
		private final InputObservable inputObservable = new InputObservable();
		private TYPE lastValue;

		public ComboBox(final IComboBox<TYPE> widget) {
			super(widget);
			lastValue = null;
			super.addInputListener(new IInputListener() {
				@Override
				public void inputChanged() {
					final TYPE value = getValue();
					final boolean changed = (value != null) && (!NullCompatibleEquivalence.equals(lastValue, value));

					if (changed && !eventsDisabled) {
						eventsDisabled = true;
						inputObservable.fireInputChanged();
						eventsDisabled = false;
						updateHeaders();
					}
					lastValue = value;
				}
			});
		}

		@Override
		public void addInputListener(final IInputListener listener) {
			if (listener == null) {
				return;
			}
			inputObservable.addInputListener(listener);
		}

		@Override
		public void removeInputListener(final IInputListener listener) {
			if (listener == null) {
				return;
			}
			inputObservable.removeInputListener(listener);
		}

		private TYPE getLastValue() {
			return lastValue;
		}

		@Override
		// only set Elements is list has changed to avoid flickering
		public void setElements(final List<? extends TYPE> elements) {
			final List<TYPE> currentElements = getElements();
			boolean changed = false;
			if (elements.size() != currentElements.size()) {
				changed = true;
			}
			else {
				for (int i = 0; i < elements.size(); i++) {
					if (!NullCompatibleEquivalence.equals(elements.get(i), currentElements.get(i))) {
						changed = true;
						break;
					}
				}
			}

			if (changed) {
				super.setElements(elements);
			}
		}

		@Override
		public void setValue(final TYPE value) {
			if (!getElements().contains(value)) {
				return;
			}
			if (equalObjects(value, getValue())) {
				return;
			}
			lastValue = value;
			super.setValue(value);
		}

	}

	private class HeaderComboBox<TYPE> extends ComboBox<TYPE> {

		private final TYPE various;
		private final TYPE unset;

		public HeaderComboBox(final IComboBox<TYPE> widget, final TYPE various, final TYPE unset) {
			super(widget);
			this.various = various;
			this.unset = unset;
		}

		@SuppressWarnings("unchecked")
		public void setValue(final Set<TYPE> values) {
			if (values.size() == 0) {
				final List<TYPE> elementCollection = getElements();
				if (!elementCollection.contains(various)) {
					final List<TYPE> elements = new LinkedList<TYPE>(elementCollection);
					elements.add(0, various);
					setElements(elements);
				}
				setValue(various);
			}
			else if (values.size() == 1) {
				setValue((TYPE) values.toArray()[0]);
			}
			else {
				List<TYPE> elements = getElements();
				if (!elements.contains(various)) {
					elements = new LinkedList<TYPE>(elements);
					elements.add(0, various);
					setElements(elements);
				}
				if (unset != null && !elements.contains(unset)) {
					elements = new LinkedList<TYPE>(elements);
					if (elements.size() > 1 && (elements.get(0) == various)) {
						elements.add(1, unset);
					}
					else {
						elements.add(0, unset);
					}
					setElements(elements);
				}
				setValue(various);
			}
		}
	}

	private class SortingIndexComboBox extends ComboBoxWrapper<String> {

		private final InputObservable inputObservable = new InputObservable();
		private int range;
		private int previousIndex;

		public SortingIndexComboBox(final IComboBox<String> widget) {
			super(widget);
			range = -1;

			super.addInputListener(new IInputListener() {
				@Override
				public void inputChanged() {
					final int currentIndex = getIndex();
					if ((currentIndex != previousIndex) && (!eventsDisabled)) {
						eventsDisabled = true;
						inputObservable.fireInputChanged();
						eventsDisabled = false;
						updateHeaders();
					}

					previousIndex = currentIndex;
				}
			});

		}

		public void setRange(final int range) {
			if (this.range == range) {
				return;
			}

			this.range = Math.min(range, maxSortingLength);

			final int index = getIndex();
			setElements(createList());
			setIndex(index);
		}

		public int getIndex() {
			final String value = getValue();
			if (value == null || "".equals(value)) { //$NON-NLS-1$
				return 0;
			}
			return Integer.valueOf(value);
		}

		public int getPreviousIndex() {
			return previousIndex;
		}

		public void setIndex(final int index) {
			if (index > range) {
				setRange(index);
			}
			if (getIndex() == 0) {
				range = -1; // invalidate range
			}

			if (index == 0) {
				setValue(""); //$NON-NLS-1$
			}
			else {
				setValue(String.valueOf(index));
			}
		}

		private List<String> createList() {
			final List<String> result = new LinkedList<String>();
			result.add(""); //$NON-NLS-1$
			int usedRange = range;
			if (getIndex() == 0) {
				usedRange++;
			}
			usedRange = Math.min(usedRange, maxSortingLength);
			for (int i = 0; i < usedRange; i++) {
				result.add(String.valueOf(i + 1));
			}
			return result;
		}

		@Override
		public void addInputListener(final IInputListener listener) {
			if (listener == null) {
				return;
			}
			inputObservable.addInputListener(listener);
		}

		@Override
		public void removeInputListener(final IInputListener listener) {
			if (listener == null) {
				return;
			}
			inputObservable.removeInputListener(listener);
		}
	}

	private class VisibilityCheckBox extends CheckBoxWrapper {

		public VisibilityCheckBox(final ICheckBox widget) {
			super(widget);
		}

		public void setState(final int visibleCount, final int invisibleCount) {
			final Visibility visibility = Visibility.getValue(visibleCount, invisibleCount);
			if (Visibility.PARTIAL.equals(visibility)) {
				setSelected(true);
				setToolTipText(Toolkit.getMessageReplacer().replace(
						VISIBLE_HIDDEN,
						String.valueOf(visibleCount),
						String.valueOf(invisibleCount)));
			}
			else if (Visibility.ALL.equals(visibility)) {
				setSelected(true);
				setToolTipText(ALL_VISIBLE);
			}
			else if (Visibility.NONE.equals(visibility)) {
				setSelected(false);
				setToolTipText(ALL_HIDDEN);
			}
			else {
				throw new IllegalStateException("Unkown visibility '" + visibility + "'."); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	protected List<ComboBoxAlignmentHorizontal> getAlignments() {
		final List<ComboBoxAlignmentHorizontal> result = new LinkedList<ComboBoxAlignmentHorizontal>();
		for (final ComboBoxAlignmentHorizontal alignment : ComboBoxAlignmentHorizontal.values()) {
			if (alignment.getAlignmentHorizontal() != null) {
				result.add(alignment);
			}
		}
		return result;
	}

	protected List<ComboBoxSortOrder> getSortOrders() {
		final List<ComboBoxSortOrder> result = new LinkedList<ComboBoxSortOrder>();
		for (final ComboBoxSortOrder sortOrder : ComboBoxSortOrder.values()) {
			if (sortOrder != ComboBoxSortOrder.CLEAR_SORTING) {
				result.add(sortOrder);
			}
		}
		return result;
	}

	private interface ISortIndexModelProvider {
		ComboBox<ComboBoxSortOrder> getSort(AbstractListElement<?> composite);

		SortingIndexComboBox getSortIndex(AbstractListElement<?> composite);
	}

	private final class SortIndexModel {
		private int sortingLength;
		private final ISortIndexModelProvider provider;

		SortIndexModel(final ISortIndexModelProvider provider) {
			this.provider = provider;
		}

		void updateSortingIndexRanges() {
			for (final Entry<String, AttributeComposite> entry : attributeComposites.entrySet()) {
				final AttributeComposite composite = entry.getValue();
				if (composite.isSortable()) {
					provider.getSortIndex(composite).setRange(sortingLength);
				}
			}
		}

		void clearSortIndices() {
			for (final Entry<String, AttributeComposite> entry : attributeComposites.entrySet()) {
				final AttributeComposite composite = entry.getValue();
				if (composite.isSortable() && (provider.getSortIndex(composite).getIndex() > 0)) {
					provider.getSort(composite).setValue(ComboBoxSortOrder.NOT_SET);
					provider.getSortIndex(composite).setIndex(0);
				}
			}
		}

		void insertSortIndex(final int index, final String propertyName) {
			sortingLength++;
			for (final Entry<String, AttributeComposite> entry : attributeComposites.entrySet()) {
				final AttributeComposite composite = entry.getValue();
				if (!composite.isSortable()) {
					continue;
				}

				final SortingIndexComboBox comboBox = provider.getSortIndex(composite);
				if (entry.getKey().equals(propertyName)) {
					comboBox.setIndex(index);
					continue;
				}

				final int currentIndex = comboBox.getIndex();
				if (currentIndex == 0) {
					continue;
				}
				if (currentIndex >= index) {
					final int newIndex = currentIndex + 1;
					if (comboBox.range < newIndex) {
						comboBox.setRange(attributeComposites.size());
					}
					comboBox.setIndex(newIndex);
				}
			}
		}

		void removeSortIndex(final int index, final String propertyName) {
			sortingLength--;
			for (final Entry<String, AttributeComposite> entry : attributeComposites.entrySet()) {
				final AttributeComposite composite = entry.getValue();
				if (!composite.isSortable()) {
					continue;
				}

				final SortingIndexComboBox comboBox = provider.getSortIndex(composite);
				if (entry.getKey().equals(propertyName)) {
					comboBox.setIndex(0);
					continue;
				}

				final int currentIndex = comboBox.getIndex();
				if (currentIndex == 0) {
					continue;
				}
				if (currentIndex >= index) {
					final int newIndex = currentIndex - 1;
					comboBox.setIndex(newIndex);
				}
			}
		}

		void shiftSortingIndex(final int newIndex, final int previousIndex, final String propertyName) {
			final int delta;
			if (newIndex < previousIndex) {
				delta = 1;
			}
			else {
				delta = -1;
			}

			final int lowestIndex = Math.min(newIndex, previousIndex);
			final int highestIndex = Math.max(newIndex, previousIndex);

			for (final Entry<String, AttributeComposite> entry : attributeComposites.entrySet()) {
				final AttributeComposite composite = entry.getValue();
				if (!composite.isSortable()) {
					continue;
				}

				final SortingIndexComboBox comboBox = provider.getSortIndex(composite);
				if (entry.getKey().equals(propertyName)) {
					comboBox.setIndex(newIndex);
					continue;
				}

				final int index = comboBox.getIndex();
				if ((index >= lowestIndex) && (index <= highestIndex)) {
					comboBox.setIndex(index + delta);
				}
			}
		}
	}

	public void setFilter(final String text) {
		eventsDisabled = true;
		boolean changed = false;
		final boolean allVisible = (text == null) || (text.trim().equals("")); //$NON-NLS-1$

		final List<String> visibleList = new LinkedList<String>();

		disableLayouting();
		final FilterInformation filterInformation = attributesFilterComposite.getData();

		for (final Entry<String, AttributeComposite> entry : attributeComposites.entrySet()) {
			final AttributeComposite attributeComposite = entry.getValue();
			final boolean visible = allVisible || checkFilter(attributeComposite, text);
			changed = changed || (shouldControlBeVisible(attributeComposite) != visible);
			setAttributeVisible(attributeComposite, visible);

			if (visible) {
				visibleList.add(attributeComposite.propertyName);
			}
		}

		filterInformation.setVisibleProperties(visibleList);
		attributesFilterComposite.updateValues();

		if (attributesFilterComposite.isVisible() == allVisible) {
			attributesFilterComposite.setVisible(!allVisible);
			getWidget().layoutBegin();
			getWidget().layoutEnd();
		}

		if (changed) {
			for (final Entry<String, AttributeGroupComposite> entry : groups.entrySet()) {
				final AttributeGroupComposite container = entry.getValue();

				boolean headerVisible = allVisible;

				if (!headerVisible) {
					// header is visible, if at least one attribute is visisble
					for (final String propertyName : container.getHeader().getPropertyNames()) {
						if (visibleList.contains(propertyName)) {
							headerVisible = true;
							break;
						}
					}
				}

				if (shouldControlBeVisible(container.getHeader()) != headerVisible) {
					if (headerVisible && groups.size() > 1) {
						container.getHeader().setLayoutConstraints(true);
					}
					else {
						container.getHeader().setLayoutConstraints(false);
					}
				}
				if (shouldControlBeVisible(container.composite) != !container.getHeader().isCollapsed()) {
					container.composite.setLayoutConstraints(!container.getHeader().isCollapsed());
				}
			}
		}
		enableLayouting();

		if (changed) {
			refreshLayout();
		}
		eventsDisabled = false;
	}

	private boolean checkFilter(final AttributeComposite attributeComposite, final String filter) {
		if (filter == null) {
			return true;
		}

		final IAttribute<?> attribute = attributeComposite.getAttribute();

		if ((attribute.getGroup() != null) && (attribute.getGroup().getLabel() != null)) {
			final String groupLabel = attribute.getGroup().getLabel().toLowerCase();
			if (groupLabel.contains(filter.toLowerCase())) {
				return true;
			}
		}

		final StringBuilder attributeText = new StringBuilder();
		appendText(attributeText, attribute.getLabel());
		appendText(attributeText, attribute.getLabelLong());
		appendText(attributeText, attributeComposite.getColumnAlignment());
		appendText(attributeText, attributeComposite.getHeaderFormat());
		appendText(attributeText, attributeComposite.getContentFormat());

		final List<String> words = getWords(filter);
		for (final String word : words) {
			if (attributeText.indexOf(word) < 0) {
				return false;
			}
		}

		return true;
	}

	private List<String> getWords(final String filter) {
		final List<String> result = new LinkedList<String>();
		final String[] words = filter.toLowerCase().split(" "); //$NON-NLS-1$

		String currentWord = null;
		for (final String word : words) {
			if (currentWord != null) {
				currentWord = currentWord + " " + word; //$NON-NLS-1$
			}
			else if (word.startsWith("\"")) { //$NON-NLS-1$
				currentWord = word;
			}
			else {
				result.add(word);
			}

			if (currentWord != null && currentWord.length() > 1 && currentWord.endsWith("\"")) { //$NON-NLS-1$
				result.add(currentWord.substring(1, currentWord.length() - 1));
				currentWord = null;
			}
		}

		if (currentWord != null) {
			result.add(currentWord.substring(1));
		}

		return result;
	}

	private void appendText(final StringBuilder stringBuilder, final String text) {
		if (text != null) {
			stringBuilder.append('%');
			stringBuilder.append(text.toLowerCase());
			stringBuilder.append('%');
		}
	}

	private void appendText(final StringBuilder stringBuilder, final IComboBox<?> comboBox) {
		if (comboBox != null) {
			appendText(stringBuilder, comboBox.getValue().toString());
		}
	}

	public void buildConfig(final IBeanTableConfigBuilder builder) {
		for (final Entry<String, AttributeComposite> entry : attributeComposites.entrySet()) {
			final AttributeComposite attributeComposite = entry.getValue();

			final boolean visible = attributeComposite.getVisible().getValue();
			final DisplayFormat labelDisplayFormat = (attributeComposite.getHeaderFormat() != null)
					? (DisplayFormat) attributeComposite.getHeaderFormat().getValue() : null;
			final IDisplayFormat displayFormat = (attributeComposite.getContentFormat() != null)
					? attributeComposite.getContentFormat().getValue() : null;
			final AlignmentHorizontal tableAlignment = attributeComposite.getColumnAlignment().getValue().getAlignmentHorizontal();
			final Integer tableWidth = null;

			builder.addAttributeConfig(entry.getKey(), new AttributeConfigImpl(
				visible,
				labelDisplayFormat,
				displayFormat,
				tableAlignment,
				tableWidth));
		}

		final SortModelConfigBuilder sortModelConfigBuilder = new SortModelConfigBuilder();
		final AttributeComposite[] currentSorting = new AttributeComposite[currentSortingModel.sortingLength];
		final AttributeComposite[] defaultSorting = new AttributeComposite[defaultSortingModel.sortingLength];
		for (final Entry<String, AttributeComposite> entry : attributeComposites.entrySet()) {
			final AttributeComposite attributeComposite = entry.getValue();
			if (!attributeComposite.isSortable()) {
				continue;
			}

			final int currentSortingIndex = currentSortingModel.provider.getSortIndex(attributeComposite).getIndex();
			if (currentSortingIndex > 0) {
				currentSorting[currentSortingIndex - 1] = attributeComposite;
			}

			final int defaultSortingIndex = defaultSortingModel.provider.getSortIndex(attributeComposite).getIndex();
			if (defaultSortingIndex > 0) {
				defaultSorting[defaultSortingIndex - 1] = attributeComposite;
			}
		}

		for (final AttributeComposite attributeComposite : currentSorting) {
			final ComboBoxSortOrder order = currentSortingModel.provider.getSort(attributeComposite).getValue();
			if (order.getSortOrder() != null) {
				sortModelConfigBuilder.addCurrentProperty(attributeComposite.propertyName, order.getSortOrder());
			}
		}

		for (final AttributeComposite attributeComposite : defaultSorting) {
			final ComboBoxSortOrder order = defaultSortingModel.provider.getSort(attributeComposite).getValue();
			if (order.getSortOrder() != null) {
				sortModelConfigBuilder.addDefaultProperty(attributeComposite.propertyName, order.getSortOrder());
			}
		}

		builder.setSortModelConfig(sortModelConfigBuilder.build());
	}

	private void setAttributeVisible(final AttributeComposite attributeComposite, final boolean visible) {
		if (shouldControlBeVisible(attributeComposite) != visible) {
			attributeComposite.setLayoutConstraints(visible);
		}
	}

	private boolean shouldControlBeVisible(final IControl control) {
		if (control.getLayoutConstraints() == null) {
			return true;
		}
		if (control.getLayoutConstraints() instanceof Boolean) {
			final Boolean visible = (Boolean) control.getLayoutConstraints();
			return visible.booleanValue();
		}

		return true;
	}

	private void refreshLayout() {
		if (attributesFilterComposite.isVisible()) {
			attributesFilterComposite.layoutBegin();
			attributesFilterComposite.layoutEnd();
		}

		attributeScroller.layoutBegin();
		for (final Entry<String, AttributeGroupComposite> entry : groups.entrySet()) {
			entry.getValue().refreshAttributeLayout(true);
		}
		attributeScroller.layoutEnd();

		updateHeaderColors();
	}

	private void updateHeaderColors() {
		int index = 0;
		for (final String groupName : groupNames) {
			final AttributeGroupComposite attributeGroupComposite = groups.get(groupName);
			attributeGroupComposite.getHeader().setBackgroundColor(ALTERNATING_GROUPS[index]);

			if (attributeGroupComposite.isCollapsed()) {
				index = (index + 1) % ALTERNATING_GROUPS.length;
			}
		}
	}

	private void disableLayouting() {
		layoutingEnabled = false;
		attributeLayoutManager.beginLayout();
	}

	private void enableLayouting() {
		layoutingEnabled = true;
		attributeLayoutManager.endLayout();
	}

	private boolean layoutingEnabled() {
		return layoutingEnabled;
	}

	public void setSizes() {
		final Dimension minSize = super.getMinSize();
		setMinSize(new Dimension(minSize.getWidth() + 30, minSize.getHeight()));

		final Dimension prefSize = super.getPreferredSize();
		setPreferredSize(new Dimension(prefSize.getWidth() + 30, prefSize.getHeight()));
	}

	// TODO NM use existing class or builder instead
	final class AttributeConfigImpl implements IAttributeConfig {

		private final Boolean visible;
		private final DisplayFormat labelDisplayFormat;
		private final IDisplayFormat displayFormat;
		private final AlignmentHorizontal tableAlignment;
		private final Integer tableWidth;

		AttributeConfigImpl(
			final Boolean visible,
			final DisplayFormat labelDisplayFormat,
			final IDisplayFormat displayFormat,
			final AlignmentHorizontal tableAlignment,
			final Integer tableWidth) {

			this.visible = visible;
			this.labelDisplayFormat = labelDisplayFormat;
			this.displayFormat = displayFormat;
			this.tableAlignment = tableAlignment;
			this.tableWidth = tableWidth;
		}

		@Override
		public Boolean isVisible() {
			return visible;
		}

		@Override
		public DisplayFormat getLabelDisplayFormat() {
			return labelDisplayFormat;
		}

		@Override
		public IDisplayFormat getDisplayFormat() {
			return displayFormat;
		}

		@Override
		public AlignmentHorizontal getTableAlignment() {
			return tableAlignment;
		}

		@Override
		public Integer getTableWidth() {
			return tableWidth;
		}

	}

	private static boolean isEmptyObject(final Object object) {
		if (object == null) {
			return true;
		}
		if (object instanceof String) {
			return ("".equals(object)); //$NON-NLS-1$
		}
		return false;
	}

	private static boolean equalObjects(final Object object1, final Object object2) {
		if (isEmptyObject(object1) && (isEmptyObject(object2))) {
			return true;
		}
		else if (isEmptyObject(object1)) {
			return false;
		}
		else {
			return object1.equals(object2);
		}
	}

	private static final class ContentDisplayFormatConverter extends AbstractConverter<IDisplayFormat> {
		private final List<IDisplayFormat> formats;

		private ContentDisplayFormatConverter(final List<IDisplayFormat> formats) {
			this.formats = formats;
		}

		@Override
		public IDisplayFormat convertToObject(final String string) {
			for (final IDisplayFormat displayFormat : formats) {
				if (displayFormat.getName().equals(string)) {
					return displayFormat;
				}
			}
			return null;
		}

		@Override
		public String convertToString(final IDisplayFormat value) {
			return value.getName();
		}

	}

	private static final class HeaderDisplayFormatConverter extends AbstractConverter<IDisplayFormat> {

		@Override
		public IDisplayFormat convertToObject(final String string) {
			if (DisplayFormat.LONG.getName().equals(string)) {
				return DisplayFormat.LONG;
			}
			else {
				return DisplayFormat.SHORT;
			}
		}

		@Override
		public String convertToString(final IDisplayFormat value) {
			return value.getName();
		}

	}

	private static final class AlignmentConverter extends AbstractConverter<ComboBoxAlignmentHorizontal> {

		@Override
		public ComboBoxAlignmentHorizontal convertToObject(final String string) {
			if (ComboBoxAlignmentHorizontal.LEFT.getLabel().equals(string)) {
				return ComboBoxAlignmentHorizontal.LEFT;
			}
			else if (ComboBoxAlignmentHorizontal.CENTER.getLabel().equals(string)) {
				return ComboBoxAlignmentHorizontal.CENTER;
			}
			else if (ComboBoxAlignmentHorizontal.RIGHT.getLabel().equals(string)) {
				return ComboBoxAlignmentHorizontal.RIGHT;
			}
			else if (ComboBoxAlignmentHorizontal.VARIOUS.getLabel().equals(string)) {
				return ComboBoxAlignmentHorizontal.VARIOUS;
			}
			else {
				throw new IllegalArgumentException("Unkown alignment '" + string + "':"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		@Override
		public String convertToString(final ComboBoxAlignmentHorizontal value) {
			return value.getLabel();
		}

	}

	private static enum ComboBoxSortOrder {
		NOT_SET(""), //$NON-NLS-1$
		CLEAR_SORTING(TEXT_CLEAR_SORTING),
		ASC(SortOrder.ASC),
		DESC(SortOrder.DESC);

		private String label;
		private SortOrder sortOrder;

		private ComboBoxSortOrder(final String label) {
			this(label, null);
		}

		private ComboBoxSortOrder(final SortOrder sortOrder) {
			this(sortOrder.getLabel(), sortOrder);
		}

		private ComboBoxSortOrder(final String label, final SortOrder sortOrder) {
			this.label = label;
			this.sortOrder = sortOrder;
		}

		public String getLabel() {
			return label;
		}

		public SortOrder getSortOrder() {
			return sortOrder;
		}

		@Override
		public String toString() {
			return label;
		}

		public static ComboBoxSortOrder convert(final SortOrder sortOrder) {
			for (final ComboBoxSortOrder value : values()) {
				if (value.sortOrder == sortOrder) {
					return value;
				}
			}

			return NOT_SET;
		}
	}

	private static final class SortOrderConverter extends AbstractConverter<ComboBoxSortOrder> {

		@Override
		public ComboBoxSortOrder convertToObject(final String string) {
			if (ComboBoxSortOrder.NOT_SET.equals(string)) {
				return ComboBoxSortOrder.NOT_SET;
			}
			else if (ComboBoxSortOrder.ASC.equals(string)) {
				return ComboBoxSortOrder.ASC;
			}
			else if (ComboBoxSortOrder.DESC.equals(string)) {
				return ComboBoxSortOrder.DESC;
			}
			else {
				throw new IllegalArgumentException("Unkown sort order '" + string + "':"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		@Override
		public String convertToString(final ComboBoxSortOrder value) {
			return value.getLabel();
		}
	}

	private static IDisplayFormat createEmptyDisplayFormat() {
		return new IDisplayFormat() {

			@Override
			public String getName() {
				return ""; //$NON-NLS-1$
			}

			@Override
			public Object getId() {
				return ""; //$NON-NLS-1$
			}

			@Override
			public String getDescription() {
				return ""; //$NON-NLS-1$
			}
		};
	}

	public enum ComboBoxAlignmentHorizontal {

		VARIOUS(""), //$NON-NLS-1$
		LEFT(AlignmentHorizontal.LEFT),
		RIGHT(AlignmentHorizontal.RIGHT),
		CENTER(AlignmentHorizontal.CENTER);

		private String label;
		private AlignmentHorizontal alignmentHorizontal;

		private ComboBoxAlignmentHorizontal(final String label) {
			this(label, null);
		}

		private ComboBoxAlignmentHorizontal(final AlignmentHorizontal alignmentHorizontal) {
			this(alignmentHorizontal.getLabel(), alignmentHorizontal);
		}

		private ComboBoxAlignmentHorizontal(final String label, final AlignmentHorizontal alignmentHorizontal) {
			this.label = label;
			this.alignmentHorizontal = alignmentHorizontal;
		}

		public String getLabel() {
			return label;
		}

		public AlignmentHorizontal getAlignmentHorizontal() {
			return alignmentHorizontal;
		}

		@Override
		public String toString() {
			return label;
		}

		public static ComboBoxAlignmentHorizontal convert(final AlignmentHorizontal tableAlignment) {
			for (final ComboBoxAlignmentHorizontal alignment : values()) {
				if (alignment.getAlignmentHorizontal() == tableAlignment) {
					return alignment;
				}
			}
			return null;
		}

	}
}
