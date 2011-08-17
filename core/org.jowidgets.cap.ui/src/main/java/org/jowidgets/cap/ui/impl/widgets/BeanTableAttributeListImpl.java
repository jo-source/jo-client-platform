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
import org.jowidgets.cap.ui.api.attribute.DisplayFormat;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeConfig;
import org.jowidgets.cap.ui.api.attribute.IAttributeGroup;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
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
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.command.ActionBuilder;
import org.jowidgets.tools.controller.InputObservable;
import org.jowidgets.tools.widgets.wrapper.CheckBoxWrapper;
import org.jowidgets.tools.widgets.wrapper.ComboBoxWrapper;
import org.jowidgets.tools.widgets.wrapper.CompositeWrapper;
import org.jowidgets.tools.widgets.wrapper.ContainerWrapper;
import org.jowidgets.util.NullCompatibleEquivalence;

final class BeanTableAttributeListImpl extends CompositeWrapper {
	// TODO i18n
	private static final String ALL_LABEL_TEXT = "All";
	private static final String SEARCH_LABEL_TEXT = "Search";

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

	private final Map<String, AttributeGroupHeader> groupHeaders;
	private final Map<String, AttributeGroupContainer> groupContainers;
	private final Map<String, AttributeComposite> attributeComposites;
	private final ITableLayout attributeLayoutManager;
	private final IScrollComposite attributeScroller;

	//private final ListLayout attributeScrollerLayout;

	private final AllAttributesComposite allAttributesComposite;
	private final AttributesFilterComposite attributesFilterComposite;

	private final int maxSortingLength;
	private boolean eventsDisabled;

	private SortIndexModel currentSortingModel;
	private SortIndexModel defaultSortingModel;

	private final IInputListener updateHeadersListener;
	private boolean layoutingEnabled;

	BeanTableAttributeListImpl(final IComposite container, final IBeanTableModel<?> model) {
		super(container);

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

		if (isSingleGroup(model)) {
			// hide toolbar column if no groups exist
			builder.columnMode(0, ColumnMode.HIDDEN);
			builder.gapAfterColumn(0, 0); // no gap after hidden column
		}

		this.attributeLayoutManager = builder.build();
		disableLayouting();

		this.groupHeaders = new HashMap<String, AttributeGroupHeader>();
		this.groupContainers = new HashMap<String, AttributeGroupContainer>();
		this.attributeComposites = new HashMap<String, AttributeComposite>();

		//Toolkit.getLayoutFactoryProvider().migLayoutBuilder().constraints("hidemode 2").columnConstraints("[grow]").rowConstraints("[]0[]0[]0[grow, 0:500:]").build();

		this.setLayout(new MigLayoutDescriptor("hidemode 2", "[grow]", "[]0[]0[]0[grow, 0:500:]"));
		this.maxSortingLength = getMaxSortableLength(model);

		final AllAttributeInformation allAttributeInformation = new AllAttributeInformation(model);

		updateHeadersListener = new IInputListener() {
			@Override
			public void inputChanged() {
				updateHeaders();
			}
		};

		currentSortingModel = new SortIndexModel(new ISortIndexModelProvider() {

			@Override
			public ComboBox getSort(final AbstractListElement<?> composite) {
				return composite.getCurrentSorting();
			}

			@Override
			public SortingIndexComboBox getSortIndex(final AbstractListElement<?> composite) {
				return composite.getCurrentSortingIndex();
			}

		});
		defaultSortingModel = new SortIndexModel(new ISortIndexModelProvider() {

			@Override
			public ComboBox getSort(final AbstractListElement<?> composite) {
				return composite.getDefaultSorting();
			}

			@Override
			public SortingIndexComboBox getSortIndex(final AbstractListElement<?> composite) {
				return composite.getDefaultSortingIndex();
			}

		});

		final IBluePrintFactory bpF = Toolkit.getBluePrintFactory();

		new AttributeHeaderComposite(add(bpF.composite(), "grow, wrap"));
		allAttributesComposite = new AllAttributesComposite(
			add(bpF.composite(), "grow, wrap"),
			attributeLayoutManager,
			model,
			allAttributeInformation);
		attributesFilterComposite = new AttributesFilterComposite(
			add(bpF.composite(), "grow, wrap"),
			attributeLayoutManager,
			model,
			new FilterInformation(allAttributeInformation));
		attributesFilterComposite.setVisible(false);

		attributeScroller = this.add(bpF.scrollComposite().setHorizontalBar(false), "grow, w 0::, h 0::");
		attributeScroller.setLayout(Toolkit.getLayoutFactoryProvider().listLayoutBuilder().backgroundColors(ALTERNATING_GROUPS).build());

		AttributeGroupHeader lastHeader = null;
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
				final AttributeGroupInformation information = new AttributeGroupInformation(columnIndex, model);
				final AttributeGroupHeader header = new AttributeGroupHeader(
					attributeScroller.add(bpF.composite()),
					attributeLayoutManager,
					model,
					information);
				groupHeaders.put(attributeGroupId, header);
				groupContainer = new AttributeGroupContainer(attributeScroller.add(bpF.composite()));
				groupContainers.put(attributeGroupId, groupContainer);

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
				composite.getCurrentSorting().setValue(sort.getSortOrder().getLabel());

				composite.getCurrentSortingIndex().setIndex(index + 1);
			}

			index++;
		}

		index = 0;
		for (final ISort sort : currentConfig.getSortModelConfig().getDefaultSorting()) {
			final AttributeComposite composite = attributeComposites.get(sort.getPropertyName());
			if (composite.isSortable()) {
				composite.getDefaultSorting().setValue(sort.getSortOrder().getLabel());
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
		for (final Entry<String, AttributeGroupHeader> entry : groupHeaders.entrySet()) {
			final AttributeGroupHeader composite = entry.getValue();
			composite.updateValues();
		}

		allAttributesComposite.updateValues();
		attributesFilterComposite.updateValues();
		eventsDisabled = false;
	}

	private final class AttributeGroupContainer extends CompositeWrapper {

		private AttributeGroupContainer(final IComposite container) {
			super(container);
			setLayout(Toolkit.getLayoutFactoryProvider().listLayoutBuilder().backgroundColors(ALTERNATING_ATTRIBUTES).build());
		}

	}

	private final class AttributeHeaderComposite extends ContainerWrapper {

		public AttributeHeaderComposite(final IContainer container) {
			super(container);
			setBackgroundColor(ATTRIBUTE_HEADER_BACKGROUND);
			setLayout(attributeLayoutManager.rowBuilder().build());

			// TODO i18n
			add(LABEL_HEADER.setText(""));
			add(LABEL_HEADER.setText("Name"));
			add(LABEL_HEADER.setText("Visible"));
			add(LABEL_HEADER.setText("Header format"));
			add(LABEL_HEADER.setText("Content format"));
			add(LABEL_HEADER.setText("Alignment"));
			add(LABEL_HEADER.setText("Current sorting"), "span 2");
			add(LABEL_HEADER.setText("Default sorting"), "span 2");
		}
	}

	private abstract class AbstractListElement<TYPE> extends CompositeWrapper {
		private final TYPE information;
		private final VisibilityCheckBox visible;
		private final ComboBox headerFormat;
		private final ComboBox contentFormat;
		private final ComboBox columnAlignment;
		private final ComboBox currentSorting;
		private final SortingIndexComboBox currentSortingIndex;
		private final ComboBox defaultSorting;
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

			final List<String> headerFormats = getHeaderFormats();
			if (headerFormats.size() > 1) {
				headerFormat = new ComboBox(createComboBox(bpF, headerFormats, 300));
				headerFormat.addInputListener(createHeaderChangedListener());
			}
			else {
				headerFormat = null;
				addNotAvailableLabel(textLabel);
			}

			final List<String> contentFormats = getContentFormats();
			if (contentFormats.size() > 1) {
				contentFormat = new ComboBox(createComboBox(bpF, contentFormats, 300));
				contentFormat.addInputListener(createContentChangedListener());
			}
			else {
				contentFormat = null;
				addNotAvailableLabel(textLabel);
			}

			columnAlignment = new ComboBox(createComboBox(bpF, getAlignments(), 200));
			columnAlignment.addInputListener(createAlignmentChangedListener());

			if (isSortable()) {
				currentSorting = new ComboBox(createComboBox(bpF, getSortOrders(), 250));
				currentSorting.addInputListener(createCurrentSortingChangedListener());
				if (hasSortPriority()) {
					currentSortingIndex = new SortingIndexComboBox(add(bpF.comboBoxSelection()));
					currentSortingIndex.addInputListener(createCurrentSortingIndexChangedListener());
				}
				else {
					currentSortingIndex = null;
					addNotAvailableLabel(textLabel);
				}

				defaultSorting = new ComboBox(createComboBox(bpF, getSortOrders(), 250));
				defaultSorting.addInputListener(createDefaultSortingChangedListener());
				if (hasSortPriority()) {
					defaultSortingIndex = new SortingIndexComboBox(add(bpF.comboBoxSelection()));
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

		protected abstract List<String> getHeaderFormats();

		protected abstract List<String> getContentFormats();

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

		public VisibilityCheckBox getVisible() {
			return visible;
		}

		public ComboBox getHeaderFormat() {
			return headerFormat;
		}

		public ComboBox getContentFormat() {
			return contentFormat;
		}

		public ComboBox getColumnAlignment() {
			return columnAlignment;
		}

		public ComboBox getCurrentSorting() {
			return currentSorting;
		}

		public SortingIndexComboBox getCurrentSortingIndex() {
			return currentSortingIndex;
		}

		public ComboBox getDefaultSorting() {
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
		protected List<String> getHeaderFormats() {
			final List<String> result = new LinkedList<String>();
			if (hasShortAndLongLabel(getAttribute())) {
				result.add(DisplayFormat.SHORT.getName());
				result.add(DisplayFormat.LONG.getName());
			}
			return result;
		}

		@Override
		protected List<String> getContentFormats() {
			final List<String> result = new LinkedList<String>();
			for (final IControlPanelProvider<?> provider : getAttribute().getControlPanels()) {
				result.add(provider.getDisplayFormatName());
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
				// TODO NM ask MG what to do if no config is set for this attribute
				return;
			}

			if (getHeaderFormat() != null) {
				String format = getHeaderFormatConfig(attributeConfig);
				if ((format != null)
					&& (!getHeaderFormat().getElements().contains(format))
					&& (format.equals(DisplayFormat.DEFAULT.getName()))) {
					format = DisplayFormat.SHORT.getName();

				}
				getHeaderFormat().setValue(format);
			}

			if (getContentFormat() != null) {
				final String format = getContentFormatConfig(attributeConfig);
				getContentFormat().setValue(format);
			}

			getVisible().setSelected(attributeConfig.isVisible());

			if (getColumnAlignment() != null) {
				getColumnAlignment().setValue(attributeConfig.getTableAlignment().getLabel());
			}

			if (isSortable()) {
				getCurrentSorting().setValue("");
				getCurrentSortingIndex().setIndex(0);
				getDefaultSorting().setValue("");
				getDefaultSortingIndex().setIndex(0);
			}
		}

		protected String getHeaderFormatConfig(final IAttributeConfig attributeConfig) {
			final DisplayFormat format = attributeConfig.getLabelDisplayFormat();
			if (format == null) {
				return null;
			}
			else {
				if (format.equals(DisplayFormat.DEFAULT)) {
					return DisplayFormat.SHORT.getName();
				}
				return format.getName();
			}
		}

		protected String getContentFormatConfig(final IAttributeConfig attributeConfig) {
			final String id = attributeConfig.getDisplayFormatId();
			if (id == null) {
				return null;
			}
			return getDisplayFormatNameById(id);
		}

		@Override
		protected IInputListener createSortingChangedListener(final SortIndexModel model) {
			return new IInputListener() {
				@Override
				public void inputChanged() {
					final ComboBox comboBox = model.provider.getSort(attributeComposites.get(propertyName));
					final String value = comboBox.getValue();
					final boolean hasToAdd = comboBox.getLastValue().equals("");
					final boolean hasToRemove = value.equals("");
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
						model.provider.getSort(composite).setValue("");
					}
					else if (hasToAdd) {
						model.insertSortIndex(newIndex, propertyName);
						getCurrentSorting().setValue(SortOrder.ASC.getLabel());
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

		private boolean collapsed;
		private Visibility visibility;

		public AttributesHeaderComposite(
			final IComposite container,
			final ITableLayout attributeLayoutManager,
			final IBeanTableModel<?> model,
			final TYPE information,
			final ITextLabelBluePrint textLabel) {
			super(container, attributeLayoutManager, model, information, textLabel);
			setBackgroundColor(ATTRIBUTE_GROUP_BACKGROUND);
			collapsed = false;
		}

		@Override
		public String getLabelText() {
			return getData().getLabelText();
		}

		@Override
		public List<String> getHeaderFormats() {
			return getData().getHeaderFormats();
		}

		@Override
		public List<String> getContentFormats() {
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
				final List<String> formats = getHeaderFormats();
				getHeaderFormat().setElements(formats);
				getHeaderFormat().setEnabled(formats.size() > 1);
			}
			if (getContentFormat() != null) {
				final List<String> formats = getContentFormats();
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
			final Set<String> usedHeaderFormats = new HashSet<String>();
			final Set<String> usedContentFormats = new HashSet<String>();
			final Set<String> usedColumnAlignments = new HashSet<String>();
			final Set<String> usedCurrentSortings = new HashSet<String>();
			final Set<String> usedDefaultSortings = new HashSet<String>();
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

			visibility = Visibility.getValue(visibleCount, invisibleCount);
			getVisible().setState(visibility);

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
					setCollapsed(!isCollapsed());
				}

			});
			return actionBuilder.build();
		}

		public String[] getPropertyNames() {
			return getData().getPropertyNames();
		}

		public boolean isCollapsed() {
			return collapsed;
		}

		public void setCollapsed(final boolean collapsed) {
			if (!getData().isCollapsable()) {
				return;
			}

			this.collapsed = collapsed;
			final String id = getData().getLabelText();
			if (groupContainers.containsKey(id)) {
				groupContainers.get(id).setLayoutConstraints(!collapsed);
				if (layoutingEnabled()) {
					attributeScroller.redraw();
				}
			}

			final IToolBarButton button = getCollapseButton();
			if (collapsed) {
				button.setIcon(IconsSmall.TABLE_SORT_ASC);
			}
			else {
				button.setIcon(IconsSmall.TABLE_SORT_DESC);
			}
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
					final String value = getHeaderFormat().getValue();
					for (final String property : getPropertyNames()) {
						final ComboBox comboBox = attributeComposites.get(property).getHeaderFormat();
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
					final String value = getContentFormat().getValue();
					for (final String property : getPropertyNames()) {
						final ComboBox comboBox = attributeComposites.get(property).getContentFormat();
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
					final String value = getColumnAlignment().getValue();
					for (final String property : getPropertyNames()) {
						final ComboBox comboBox = attributeComposites.get(property).getColumnAlignment();
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
					final String value = model.provider.getSort(composite).getValue();
					final boolean clear = (value == null) || (value.equals(""));
					final List<Integer> shiftList = new ArrayList<Integer>();
					for (int columnIndex = 0; columnIndex < properties.length; columnIndex++) {
						final AttributeComposite attributeComposite = attributeComposites.get(properties[columnIndex]);
						if (!attributeComposite.isSortable()) {
							continue;
						}

						final SortingIndexComboBox sortIndex = model.provider.getSortIndex(attributeComposite);
						final ComboBox sorting = model.provider.getSort(attributeComposite);
						final boolean isEmpty = sorting.getValue().equals("");
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

						sorting.setValue(value);
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

					// (un)collapse all groups
					disableLayouting();
					for (final AttributeGroupHeader header : groupHeaders.values()) {
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

					// (un)collapse all groups
					disableLayouting();
					for (final AttributeGroupHeader header : groupHeaders.values()) {
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

		List<String> getHeaderFormats();

		List<String> getContentFormats();

		boolean isSortable();

		boolean isCollapsable();
	}

	private final class FilterInformation implements IAttributesInformation {
		private String[] propertyNames;
		private final List<String> headerFormats;
		private final List<String> contentFormats;
		private boolean sortable;

		private FilterInformation(final AllAttributeInformation allAttributeInformation) {
			propertyNames = new String[0];
			headerFormats = new LinkedList<String>(allAttributeInformation.getHeaderFormats());
			contentFormats = new LinkedList<String>(allAttributeInformation.getContentFormats());
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
		public List<String> getHeaderFormats() {
			return headerFormats;
		}

		@Override
		public List<String> getContentFormats() {
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

				for (final String format : attributeComposite.getHeaderFormats()) {
					if (headerFormats.contains(format)) {
						continue;
					}
					headerFormats.add(format);
				}

				for (final String format : attributeComposite.getContentFormats()) {
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
		private final List<String> headerFormats;
		private final List<String> contentFormats;

		private AllAttributeInformation(final IBeanTableModel<?> model) {
			this.propertyNames = new String[model.getColumnCount()];
			this.headerFormats = new LinkedList<String>();
			this.contentFormats = new LinkedList<String>();

			int sortableCount = 0;
			for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
				final IAttribute<?> attribute = model.getAttribute(columnIndex);
				propertyNames[columnIndex] = attribute.getPropertyName();
				if (attribute.isSortable()) {
					sortableCount++;
				}

				for (final IControlPanelProvider<?> provider : attribute.getControlPanels()) {
					if (contentFormats.contains(provider.getDisplayFormatName())) {
						continue;
					}

					contentFormats.add(provider.getDisplayFormatName());
				}
			}

			// TODO NM get list correctly
			headerFormats.add(DisplayFormat.SHORT.getName());
			headerFormats.add(DisplayFormat.LONG.getName());

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
		public List<String> getHeaderFormats() {
			return headerFormats;
		}

		@Override
		public List<String> getContentFormats() {
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
		private final List<String> headerFormats;
		private final List<String> contentFormats;

		private AttributeGroupInformation(final int startIndex, final IBeanTableModel<?> model) {
			this.group = model.getAttribute(startIndex).getGroup();
			final int endIndex = getGroupEnd(model, startIndex);
			this.propertyNames = new String[endIndex - startIndex + 1];
			this.headerFormats = new LinkedList<String>();
			this.contentFormats = new LinkedList<String>();

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
				for (final Object provider : controlPanels) {
					final IControlPanelProvider<?> controlPanelProvider = (IControlPanelProvider<?>) provider;

					if (!contentFormats.contains(controlPanelProvider.getDisplayFormatName())) {
						contentFormats.add(controlPanelProvider.getDisplayFormatName());
					}
				}

				propIndex++;
			}

			if (hasLongLabel) {
				headerFormats.add(DisplayFormat.SHORT.getName());
				headerFormats.add(DisplayFormat.LONG.getName());
			}

			sortable = (sortableCount > 0);
		}

		@Override
		public String[] getPropertyNames() {
			return propertyNames;
		}

		@Override
		public List<String> getHeaderFormats() {
			return headerFormats;
		}

		@Override
		public List<String> getContentFormats() {
			return contentFormats;
		}

		@Override
		public String getLabelText() {
			if (group == null) {
				return null;
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
				currentIndex++;
			}
			return currentIndex - 1;
		}
	}

	private static boolean hasShortAndLongLabel(final IAttribute<?> attribute) {
		return ((attribute.getLabelLong() != null) && (!attribute.getLabelLong().equals(attribute.getLabel())));
	}

	private static String getDisplayFormatNameById(final String id) {
		for (final DisplayFormat format : DisplayFormat.values()) {
			if (format.getId().equals(id)) {
				return format.getName();
			}
		}
		return null;
	}

	private class ComboBox extends ComboBoxWrapper<String> {
		private final InputObservable inputObservable = new InputObservable();
		private String lastValue;

		public ComboBox(final IComboBox<String> widget) {
			super(widget);
			lastValue = "";
			super.addInputListener(new IInputListener() {
				@Override
				public void inputChanged() {
					final String value = getValue();
					final boolean changed = !lastValue.equals(value);

					if (changed && !eventsDisabled) {
						eventsDisabled = true;
						inputObservable.fireInputChanged();
						eventsDisabled = false;
						updateHeaders();
					}
					if (value == null) {
						lastValue = "";
					}
					else {
						lastValue = value;
					}
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

		private String getLastValue() {
			return lastValue;
		}

		@Override
		// only set Elements is list has changed
		public void setElements(final List<String> elements) {
			final List<String> currentElements = getElements();
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
		public void setValue(final String value) {
			if ((value != null) && (!getElements().contains(value))) {
				return;
			}
			if (NullCompatibleEquivalence.equals(value, getValue())) {
				return;
			}
			super.setValue(value);
		}

		public void setValue(final Set<String> values) {
			if (values.size() == 1) {
				setValue((String) values.toArray()[0]);
			}
			else {
				final List<String> elements = new LinkedList<String>(getElements());
				final String various = "various";
				if (!elements.contains(various)) {
					if (elements.size() > 1 && "".equals(elements.get(0))) {
						elements.add(1, various);
					}
					else {
						elements.add(0, various);
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

			setElements(String.valueOf(maxSortingLength));

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
			if ((value == null) || (value.equals(""))) {
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
				setValue("");
			}
			else {
				setValue(String.valueOf(index));
			}
		}

		private List<String> createList() {
			final List<String> result = new LinkedList<String>();
			result.add("");
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

		public void setState(final Visibility value) {
			if (Visibility.PARTIAL.equals(value)) {
				// TODO NM set intermediate state
				setSelected(true);
			}
			else {
				setSelected(Visibility.ALL.equals(value));
			}
		}
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
		result.add("");
		for (final SortOrder sortOrder : SortOrder.values()) {
			result.add(sortOrder.getLabel());
		}
		return result;
	}

	private interface ISortIndexModelProvider {
		ComboBox getSort(AbstractListElement<?> composite);

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
			final int size = attributeComposites.size();
			for (final Entry<String, AttributeComposite> entry : attributeComposites.entrySet()) {
				final AttributeComposite composite = entry.getValue();
				if (composite.isSortable()) {
					provider.getSortIndex(composite).setRange(size);
					provider.getSort(composite).setValue("");
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
		final boolean allVisible = (text == null) || (text.equals(""));

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

		if (attributesFilterComposite.isVisible() != !allVisible) {
			attributesFilterComposite.setVisible(!allVisible);
		}

		if (changed) {
			for (final Entry<String, AttributeGroupContainer> entry : groupContainers.entrySet()) {
				final String groupId = entry.getKey();
				final AttributeGroupContainer container = entry.getValue();
				final AttributeGroupHeader attributeGroupHeader = groupHeaders.get(groupId);

				boolean headerVisible = allVisible;

				if (!headerVisible) {
					for (final String propertyName : attributeGroupHeader.getPropertyNames()) {
						if (visibleList.contains(propertyName)) {
							headerVisible = true;
							break;
						}
					}
				}

				if (shouldControlBeVisible(attributeGroupHeader) != headerVisible) {
					if (headerVisible && groupHeaders.size() > 1) {
						attributeGroupHeader.setLayoutConstraints(true);
					}
					else {
						attributeGroupHeader.setLayoutConstraints(false);
					}
				}
				if (shouldControlBeVisible(container) == attributeGroupHeader.isCollapsed()) {
					container.setLayoutConstraints(!attributeGroupHeader.isCollapsed());
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
		final String[] words = filter.toLowerCase().split(" ");

		String currentWord = null;
		for (final String word : words) {
			if (currentWord != null) {
				currentWord = currentWord + " " + word;
			}
			else if (word.startsWith("\"")) {
				currentWord = word;
			}
			else {
				result.add(word);
			}

			if (currentWord != null && currentWord.length() > 1 && currentWord.endsWith("\"")) {
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

	private void appendText(final StringBuilder stringBuilder, final IComboBox<String> comboBox) {
		if (comboBox != null) {
			appendText(stringBuilder, comboBox.getValue());
		}
	}

	public void buildConfig(final IBeanTableConfigBuilder builder) {
		for (final Entry<String, AttributeComposite> entry : attributeComposites.entrySet()) {
			final AttributeComposite attributeComposite = entry.getValue();
			builder.addAttributeConfig(entry.getKey(), new IAttributeConfig() {
				@Override
				public Boolean isVisible() {
					return attributeComposite.getVisible().getValue();
				}

				@Override
				public DisplayFormat getLabelDisplayFormat() {
					if (attributeComposite.getHeaderFormat() != null) {
						final String displayFormat = attributeComposite.getHeaderFormat().getValue();
						if (DisplayFormat.SHORT.getName().equals(displayFormat)) {
							return DisplayFormat.SHORT;
						}
						else if (DisplayFormat.LONG.getName().equals(displayFormat)) {
							return DisplayFormat.LONG;
						}
						else {
							return DisplayFormat.DEFAULT;
						}
					}
					else {
						return null;
					}
				}

				@Override
				public String getDisplayFormatId() {
					if (attributeComposite.getContentFormat() != null) {
						final String displayFormat = attributeComposite.getContentFormat().getValue();
						if (DisplayFormat.SHORT.getName().equals(displayFormat)) {
							return DisplayFormat.SHORT.getId();
						}
						else if (DisplayFormat.LONG.getName().equals(displayFormat)) {
							return DisplayFormat.LONG.getId();
						}
						else {
							return DisplayFormat.DEFAULT.getId();
						}
					}
					else {
						return null;
					}
				}

				@Override
				public AlignmentHorizontal getTableAlignment() {
					final String alignment = attributeComposite.getColumnAlignment().getValue();
					if (AlignmentHorizontal.LEFT.getLabel().equals(alignment)) {
						return AlignmentHorizontal.LEFT;
					}
					else if (AlignmentHorizontal.CENTER.getLabel().equals(alignment)) {
						return AlignmentHorizontal.CENTER;
					}
					else if (AlignmentHorizontal.RIGHT.getLabel().equals(alignment)) {
						return AlignmentHorizontal.RIGHT;
					}
					else {
						throw new IllegalStateException("Unkown Alignment: " + alignment);
					}
				}

				@Override
				public Integer getTableWidth() {
					return null;
				}

			});
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
			final String orderText = currentSortingModel.provider.getSort(attributeComposite).getValue();
			final SortOrder sortOrder = getSortOrder(orderText);
			sortModelConfigBuilder.addCurrentProperty(attributeComposite.propertyName, sortOrder);
		}

		for (final AttributeComposite attributeComposite : defaultSorting) {
			final String orderText = defaultSortingModel.provider.getSort(attributeComposite).getValue();
			final SortOrder sortOrder = getSortOrder(orderText);
			sortModelConfigBuilder.addDefaultProperty(attributeComposite.propertyName, sortOrder);
		}

		builder.setSortModelConfig(sortModelConfigBuilder.build());
	}

	private SortOrder getSortOrder(final String text) {
		final SortOrder sortOrder;
		if (SortOrder.ASC.getLabel().equals(text)) {
			sortOrder = SortOrder.ASC;
		}
		else if (SortOrder.DESC.getLabel().equals(text)) {
			sortOrder = SortOrder.DESC;
		}
		else {
			throw new IllegalStateException("Unkown sort order: " + text);
		}
		return sortOrder;
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
		for (final Entry<String, AttributeGroupContainer> entry : groupContainers.entrySet()) {
			final AttributeGroupContainer container = entry.getValue();
			if (shouldControlBeVisible(container)) {
				container.redraw();
			}
			else {
				container.setVisible(false);
			}
		}
		attributeScroller.redraw();

		if (attributesFilterComposite.isVisible()) {
			attributesFilterComposite.redraw();
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
}
