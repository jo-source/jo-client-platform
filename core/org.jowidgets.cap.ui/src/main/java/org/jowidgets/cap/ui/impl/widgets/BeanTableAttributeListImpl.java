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
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.ICheckBox;
import org.jowidgets.api.widgets.IComboBox;
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
import org.jowidgets.common.types.Rectangle;
import org.jowidgets.common.widgets.controler.IInputListener;
import org.jowidgets.common.widgets.layout.ILayouter;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.command.ActionBuilder;
import org.jowidgets.tools.controler.InputObservable;
import org.jowidgets.tools.widgets.wrapper.ComboBoxWrapper;
import org.jowidgets.tools.widgets.wrapper.ContainerWrapper;
import org.jowidgets.util.NullCompatibleEquivalence;

@SuppressWarnings("unused")
final class BeanTableAttributeListImpl extends ContainerWrapper {

	//private static final IColorConstant ATTRIBUTE_HEADER_BACKGROUND = new ColorValue(40, 90, 255);
	//a little bit darker suggestion
	private static final IColorConstant ATTRIBUTE_HEADER_BACKGROUND = new ColorValue(6, 27, 95);
	private static final IColorConstant ATTRIBUTE_GROUP_BACKGROUND = new ColorValue(130, 177, 236);

	private static final IColorConstant[] NO_COLORS = new IColorConstant[0];
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
	private final AttributeLayoutManager attributeLayoutManager;
	private final IScrollComposite attributeScroller;

	private final ListLayout attributeScrollerLayout;

	private final AllAttributesComposite allAttributesComposite;
	private final AttributesFilterComposite attributesFilterComposite;

	private final int maxSortingLength;
	private boolean eventsDisabled;

	private SortIndexModel currentSortingModel;
	private SortIndexModel defaultSortingModel;

	BeanTableAttributeListImpl(final IContainer container, final IBeanTableModel<?> model) {
		super(container);
		this.attributeLayoutManager = new AttributeLayoutManager(10, 20, 4);
		this.groupHeaders = new HashMap<String, AttributeGroupHeader>();
		this.groupContainers = new HashMap<String, AttributeGroupContainer>();
		this.attributeComposites = new HashMap<String, AttributeComposite>();
		this.setLayout(new MigLayoutDescriptor("hidemode 2", "[grow]", "[]0[]0[]0[grow, 0:500:]"));
		this.maxSortingLength = getMaxSortableLength(model);

		currentSortingModel = new SortIndexModel(new ISortIndexModelProvider() {

			@Override
			public ComboBox getSort(final AbstractAttributeComposite<?> composite) {
				return composite.getCurrentSorting();
			}

			@Override
			public SortingIndexComboBox getSortIndex(final AbstractAttributeComposite<?> composite) {
				return composite.getCurrentSortingIndex();
			}

		});
		defaultSortingModel = new SortIndexModel(new ISortIndexModelProvider() {

			@Override
			public ComboBox getSort(final AbstractAttributeComposite<?> composite) {
				return composite.getDefaultSorting();
			}

			@Override
			public SortingIndexComboBox getSortIndex(final AbstractAttributeComposite<?> composite) {
				return composite.getDefaultSortingIndex();
			}

		});

		final IBluePrintFactory bpF = Toolkit.getBluePrintFactory();

		new AttributeHeaderComposite(add(bpF.composite(), "grow, wrap"));
		allAttributesComposite = new AllAttributesComposite(
			add(bpF.composite(), "grow, wrap"),
			attributeLayoutManager,
			model,
			new AllAttributeInformation(model));
		attributesFilterComposite = new AttributesFilterComposite(
			add(bpF.composite(), "grow, wrap"),
			attributeLayoutManager,
			model,
			new AllAttributeInformation(model));
		attributesFilterComposite.setVisible(false);

		attributeScroller = this.add(bpF.scrollComposite().setHorizontalBar(false), "grow, w 0::, h 0::");
		attributeScrollerLayout = new ListLayout(attributeScroller, ALTERNATING_GROUPS);
		attributeScroller.setLayout(attributeScrollerLayout);

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
					lastHeader.setEnabled(true);
				}
				else {
					header.setEnabled(false);
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

		attributeLayoutManager.modes[2] = FillMode.CENTER; // center visibility checkbox
		attributeLayoutManager.gaps[0] = 10;
		attributeLayoutManager.gaps[1] = 10;
		attributeLayoutManager.gaps[7] = 2; // reduce gap between in sort order
		attributeLayoutManager.gaps[9] = 2; // reduce gap between in sort order
		attributeLayoutManager.gaps[attributeLayoutManager.gaps.length - 1] = 10;

		if (groupHeaders.size() <= 1) {
			// hide toolbar column if no groups exist
			attributeLayoutManager.modes[0] = FillMode.HIDDEN;
			attributeLayoutManager.gaps[1] = 0; // no gap after hidden column
		}

		attributeLayoutManager.calculateLayout();
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

	private final class AttributeGroupContainer extends ContainerWrapper {

		private AttributeGroupContainer(final IContainer container) {
			super(container);
			setLayout(new ListLayout(container, ALTERNATING_ATTRIBUTES));
		}

	}

	private final class AttributeHeaderComposite extends ContainerWrapper {

		public AttributeHeaderComposite(final IContainer container) {
			super(container);
			setBackgroundColor(ATTRIBUTE_HEADER_BACKGROUND);
			setLayout(new AttributeLayout(container, attributeLayoutManager));

			final IBluePrintFactory bpF = Toolkit.getBluePrintFactory();
			// TODO i18n
			add(LABEL_HEADER.setText(""));
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
		private final TYPE information;
		private final ICheckBox visible;
		private final ComboBox headerFormat;
		private final ComboBox contentFormat;
		private final ComboBox columnAlignment;
		private final ComboBox currentSorting;
		private final SortingIndexComboBox currentSortingIndex;
		private final ComboBox defaultSorting;
		private final SortingIndexComboBox defaultSortingIndex;
		private final IToolBarButton collapseButton;

		private AbstractAttributeComposite(
			final IContainer container,
			final AttributeLayoutManager attributeLayoutManager,
			final IBeanTableModel<?> model,
			final TYPE information,
			final ITextLabelBluePrint textLabel) {
			super(container);
			this.information = information;
			final IBluePrintFactory bpF = Toolkit.getBluePrintFactory();
			setLayout(new AttributeLayout(container, attributeLayoutManager));

			if (hasCollapseButton()) {
				final IToolBar toolBar = container.add(bpF.toolBar());
				collapseButton = toolBar.addAction(createCollapseAction());
			}
			else {
				container.add(bpF.textLabel());
				collapseButton = null;
			}

			add(textLabel.setText(getLabelText()));

			visible = add(bpF.checkBox());
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
				// TODO NM add listeners...
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

		protected abstract IInputListener createVisibleChangedListener();

		protected abstract IInputListener createHeaderChangedListener();

		protected abstract IInputListener createContentChangedListener();

		protected abstract IInputListener createAlignmentChangedListener();

		protected abstract IInputListener createCurrentSortingChangedListener();

		protected abstract IInputListener createCurrentSortingIndexChangedListener();

		protected abstract IInputListener createDefaultSortingChangedListener();

		protected abstract IInputListener createDefaultSortingIndexChangedListener();

		protected abstract IAction createCollapseAction();

		protected abstract boolean hasCollapseButton();

		protected abstract String getLabelText();

		protected abstract List<String> getHeaderFormats();

		protected abstract List<String> getContentFormats();

		protected abstract boolean isSortable();

		protected abstract boolean hasSortPriority();

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

		public void updateValues(final String[] propertyNames) {
			int visibleCount = 0;
			int invisibleCount = 0;
			final Set<String> headerFormats = new HashSet<String>();
			final Set<String> contentFormats = new HashSet<String>();
			final Set<String> columnAlignments = new HashSet<String>();
			final Set<String> currentSortings = new HashSet<String>();
			final Set<String> defaultSortings = new HashSet<String>();
			for (final String propertyName : propertyNames) {
				final AttributeComposite attributeComposite = attributeComposites.get(propertyName);
				if (attributeComposite.getVisible().getValue()) {
					visibleCount++;
				}
				else {
					invisibleCount++;
				}

				if (attributeComposite.getHeaderFormat() != null) {
					headerFormats.add(attributeComposite.getHeaderFormat().getValue());
				}
				if (attributeComposite.getContentFormat() != null) {
					contentFormats.add(attributeComposite.getContentFormat().getValue());
				}

				columnAlignments.add(attributeComposite.getColumnAlignment().getValue());

				if (attributeComposite.getCurrentSorting() != null) {
					currentSortings.add(attributeComposite.getCurrentSorting().getValue());
				}
				if (attributeComposite.getDefaultSorting() != null) {
					defaultSortings.add(attributeComposite.getDefaultSorting().getValue());
				}
			}

			setValue(getHeaderFormat(), headerFormats);
			setValue(getContentFormat(), contentFormats);
			setValue(getColumnAlignment(), columnAlignments);

			getVisible().setValue(invisibleCount == 0);

			setValue(getContentFormat(), contentFormats);
			setValue(getCurrentSorting(), currentSortings);
			setValue(getDefaultSorting(), defaultSortings);
		}

	}

	private final class AttributeComposite extends AbstractAttributeComposite<IAttribute<?>> {

		private final String propertyName;
		private final int lastDefaultSortingIndex;

		private AttributeComposite(
			final IContainer container,
			final AttributeLayoutManager attributeLayoutManager,
			final IBeanTableModel<?> model,
			final IAttribute<?> attribute) {
			super(container, attributeLayoutManager, model, attribute, LABEL_ATTRIBUTES);
			propertyName = attribute.getPropertyName();

			lastDefaultSortingIndex = 0;
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

		@Override
		protected boolean hasCollapseButton() {
			return false;
		}

		@Override
		protected IAction createCollapseAction() {
			return null;
		}

		public void updateValues(final IBeanTableConfig currentConfig) {
			final IAttributeConfig attributeConfig = currentConfig.getAttributeConfigs().get(propertyName);
			if (attributeConfig == null) {
				// TODO NM handle this
				return;
			}

			if (getHeaderFormat() != null) {
				String format = getHeaderFormatConfig(attributeConfig);
				if ((format != null)
					&& (!getHeaderFormat().getElements().contains(format))
					&& (format.equals(DisplayFormat.DEFAULT.getName()))) {
					format = DisplayFormat.SHORT.getName();

				}
				setValue(getHeaderFormat(), format);
			}

			if (getContentFormat() != null) {
				final String format = getContentFormatConfig(attributeConfig);
				setValue(getContentFormat(), format);
			}

			getVisible().setValue(attributeConfig.isVisible());

			setValue(getColumnAlignment(), attributeConfig.getTableAlignment().getLabel());

			setValue(getCurrentSorting(), "");
			setValue(getCurrentSortingIndex(), 0);
			setValue(getDefaultSorting(), "");
			setValue(getDefaultSortingIndex(), 0);
		}

		protected String getHeaderFormatConfig(final IAttributeConfig attributeConfig) {
			// TODO NM check if there are several configurations for this attribute available
			// if (!hasShortAndLongLabel(attribute)) {
			//	continue;
			// }

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
			// TODO NM check if there are several configurations for this attribute available
			//if (!hasContentFormatSelection(attribute)) {
			//	continue;
			//}

			final String id = attributeConfig.getDisplayFormatId();
			if (id == null) {
				return null;
			}
			return getDisplayFormatNameById(id);
		}

		@Override
		protected IInputListener createHeaderChangedListener() {
			return null;
		}

		@Override
		protected IInputListener createContentChangedListener() {
			return null;
		}

		@Override
		protected IInputListener createAlignmentChangedListener() {
			return null;
		}

		@Override
		protected IInputListener createVisibleChangedListener() {
			return null;
		}

		@Override
		protected IInputListener createCurrentSortingChangedListener() {
			return createSortingChangedListener(currentSortingModel);
		}

		@Override
		protected IInputListener createCurrentSortingIndexChangedListener() {
			return createSortingIndexChangedListener(currentSortingModel);
		}

		@Override
		protected IInputListener createDefaultSortingChangedListener() {
			return createSortingChangedListener(defaultSortingModel);
		}

		@Override
		protected IInputListener createDefaultSortingIndexChangedListener() {
			return createSortingIndexChangedListener(defaultSortingModel);
		}

		private IInputListener createSortingChangedListener(final SortIndexModel model) {
			return new IInputListener() {
				@Override
				public void inputChanged() {
					final ComboBox comboBox = model.provider.getSort(attributeComposites.get(propertyName));
					final String value = comboBox.getValue();
					final boolean newEnableState = !value.equals("") && comboBox.getLastValue().equals("");
					if (newEnableState) {
						model.insertSortIndex(model.sortingLength + 1, propertyName);
					}
					else {
						model.removeSortIndex(
								model.provider.getSortIndex(attributeComposites.get(propertyName)).getIndex(),
								propertyName);
					}
					model.updateSortingIndexRanges();
				}
			};
		}

		private IInputListener createSortingIndexChangedListener(final SortIndexModel model) {
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

	private abstract class AbstractAttributesGroupComposite<TYPE> extends AbstractAttributeComposite<TYPE> {
		private AbstractAttributesGroupComposite(
			final IContainer container,
			final AttributeLayoutManager attributeLayoutManager,
			final IBeanTableModel<?> model,
			final TYPE information,
			final ITextLabelBluePrint textLabel) {
			super(container, attributeLayoutManager, model, information, textLabel);
			setBackgroundColor(ATTRIBUTE_GROUP_BACKGROUND);
		}

		@Override
		protected boolean hasSortPriority() {
			return false;
		}

		public void updateValues() {
			updateValues(getPropertyNames());
		}

		protected abstract String[] getPropertyNames();

		@Override
		protected IInputListener createVisibleChangedListener() {
			return new IInputListener() {

				@Override
				public void inputChanged() {
					final boolean visible = getVisible().getValue();
					for (final String property : getPropertyNames()) {
						attributeComposites.get(property).getVisible().setValue(visible);
					}
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
						final IComboBox<String> header = attributeComposites.get(property).getHeaderFormat();
						setValue(header, value);
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
						final IComboBox<String> content = attributeComposites.get(property).getContentFormat();
						setValue(content, value);
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
						final IComboBox<String> alignment = attributeComposites.get(property).getColumnAlignment();
						setValue(alignment, value);
					}
				}
			};
		}

		@Override
		protected IInputListener createCurrentSortingChangedListener() {
			return createSortingChangedListener(currentSortingModel);
		}

		@Override
		protected IInputListener createDefaultSortingChangedListener() {
			return createSortingChangedListener(defaultSortingModel);
		}

		private IInputListener createSortingChangedListener(final SortIndexModel model) {
			final AbstractAttributeComposite<?> composite = this;
			return new IInputListener() {

				@Override
				public void inputChanged() {
					final String[] properties = getPropertyNames();
					final String value = model.provider.getSort(composite).getValue();
					final boolean clear = value.equals("");
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
						setValue(sorting, value);
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

		@Override
		protected IInputListener createCurrentSortingIndexChangedListener() {
			return null;
		}

		@Override
		protected IInputListener createDefaultSortingIndexChangedListener() {
			return null;
		}
	}

	private final class AttributeGroupHeader extends AbstractAttributesGroupComposite<AttributeGroupInformation> {

		private final AttributeGroupInformation information;
		private final boolean collapsed;

		private AttributeGroupHeader(
			final IContainer container,
			final AttributeLayoutManager attributeLayoutManager,
			final IBeanTableModel<?> model,
			final AttributeGroupInformation information) {
			super(container, attributeLayoutManager, model, information, LABEL_GROUPS);
			setBackgroundColor(ATTRIBUTE_GROUP_BACKGROUND);
			this.information = information;
			collapsed = false;
		}

		@Override
		protected String getLabelText() {
			return getData().getId();
		}

		@Override
		protected List<String> getHeaderFormats() {
			return getData().headerFormats;
		}

		@Override
		protected List<String> getContentFormats() {
			return getData().contentFormats;
		}

		@Override
		protected boolean isSortable() {
			return getData().sortable;
		}

		@Override
		protected boolean hasCollapseButton() {
			return true;
		}

		@Override
		protected IAction createCollapseAction() {
			final ActionBuilder actionBuilder = new ActionBuilder();
			actionBuilder.setIcon(IconsSmall.TABLE_SORT_DESC);
			actionBuilder.setCommand(new ICommandExecutor() {

				@Override
				public void execute(final IExecutionContext executionContext) throws Exception {
					getData().setCollapsed(!getData().isCollapsed());
				}

			});
			return actionBuilder.build();
		}

		@Override
		protected String[] getPropertyNames() {
			return getData().propertyNames;
		}

	}

	private final class AttributesFilterComposite extends AbstractAttributesGroupComposite<AllAttributeInformation> {

		private boolean collapseState;
		private String[] propertyNames;
		private List<String> headerFormats;
		private List<String> contentFormats;
		private Boolean sortable;

		private AttributesFilterComposite(
			final IContainer container,
			final AttributeLayoutManager attributeLayoutManager,
			final IBeanTableModel<?> model,
			final AllAttributeInformation information) {
			super(container, attributeLayoutManager, model, information, LABEL_ALL);
			setBackgroundColor(Colors.WHITE);
			collapseState = false;
		}

		@Override
		protected String getLabelText() {
			// TODO i18n
			return "Search";
		}

		@Override
		protected List<String> getHeaderFormats() {
			if (headerFormats == null) {
				updateData();
			}
			return headerFormats;
		}

		@Override
		protected List<String> getContentFormats() {
			if (contentFormats == null) {
				updateData();
			}
			return contentFormats;
		}

		@Override
		protected boolean isSortable() {
			if (sortable == null) {
				updateData();
			}
			return sortable.booleanValue();
		}

		@Override
		protected boolean hasCollapseButton() {
			return true;
		}

		@Override
		protected IAction createCollapseAction() {
			final ActionBuilder actionBuilder = new ActionBuilder();
			actionBuilder.setIcon(IconsSmall.TABLE_SORT_DESC);
			actionBuilder.setCommand(new ICommandExecutor() {

				@Override
				public void execute(final IExecutionContext executionContext) throws Exception {
					final IToolBarButton button = (IToolBarButton) executionContext.getSource();
					collapseState = !collapseState;
					// (un)collapse all groups
					attributeLayoutManager.beginLayout();
					for (final AttributeGroupHeader header : groupHeaders.values()) {
						if (!header.isEnabled()) {
							continue;
						}

						if (header.information.collapsed != collapseState) {
							header.information.setCollapsed(collapseState);
						}
					}
					attributeLayoutManager.endLayout();

					if (collapseState) {
						button.setIcon(IconsSmall.TABLE_SORT_ASC);
					}
					else {
						button.setIcon(IconsSmall.TABLE_SORT_DESC);
					}

				}

			});
			return actionBuilder.build();
		}

		@Override
		protected String[] getPropertyNames() {
			if (propertyNames == null) {
				updateData();
			}
			return propertyNames;
		}

		public void updateData() {
			propertyNames = getData().propertyNames;
			headerFormats = getData().headerFormats;
			contentFormats = getData().contentFormats;
			sortable = true;
		}

	}

	private final class AllAttributesComposite extends AbstractAttributesGroupComposite<AllAttributeInformation> {

		private boolean collapseState;

		private AllAttributesComposite(
			final IContainer container,
			final AttributeLayoutManager attributeLayoutManager,
			final IBeanTableModel<?> model,
			final AllAttributeInformation information) {
			super(container, attributeLayoutManager, model, information, LABEL_ALL);
			setBackgroundColor(Colors.WHITE);
			collapseState = false;
		}

		@Override
		protected String getLabelText() {
			// TODO i18n
			return "All";
		}

		@Override
		protected List<String> getHeaderFormats() {
			return getData().headerFormats;
		}

		@Override
		protected List<String> getContentFormats() {
			return getData().contentFormats;
		}

		@Override
		protected boolean isSortable() {
			return getData().sortable;
		}

		@Override
		protected boolean hasCollapseButton() {
			return true;
		}

		@Override
		protected IAction createCollapseAction() {
			final ActionBuilder actionBuilder = new ActionBuilder();
			actionBuilder.setIcon(IconsSmall.TABLE_SORT_DESC);
			actionBuilder.setCommand(new ICommandExecutor() {

				@Override
				public void execute(final IExecutionContext executionContext) throws Exception {
					final IToolBarButton button = (IToolBarButton) executionContext.getSource();
					collapseState = !collapseState;
					// (un)collapse all groups
					attributeLayoutManager.beginLayout();
					for (final AttributeGroupHeader header : groupHeaders.values()) {
						if (header.information.collapsed != collapseState) {
							header.information.setCollapsed(collapseState);
						}
					}
					attributeLayoutManager.endLayout();

					if (collapseState) {
						button.setIcon(IconsSmall.TABLE_SORT_ASC);
					}
					else {
						button.setIcon(IconsSmall.TABLE_SORT_DESC);
					}

				}

			});
			return actionBuilder.build();
		}

		@Override
		protected String[] getPropertyNames() {
			return getData().propertyNames;
		}

	}

	private enum FillMode {
		FILL,
		CENTER,
		HIDDEN
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
		private boolean layouting;

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
			this.layouting = true;
		}

		public void setGroupVisible(final String id, final boolean visible) {
			groupContainers.get(id).setEnabled(visible);
			if (layouting) {
				invalidate();
			}
		}

		private void beginLayout() {
			layouting = false;
		}

		private void endLayout() {
			if (!layouting) {
				layouting = true;
				invalidate();
			}
		}

		private void invalidate() {
			attributeScrollerLayout.invalidate();
			attributeScrollerLayout.layout();
			attributeScroller.layoutBegin();
			attributeScroller.layoutEnd();
		}

		private void calculateLayout() {
			for (int i = 0; i < widths.length; i++) {
				widths[i] = 0;
			}

			int minHeight = 0;
			for (final AttributeLayout layouter : layouters) {
				for (int i = 0; i < columnCount; i++) {
					if (modes[i] == FillMode.HIDDEN) {
						continue;
					}

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

	private static final class AttributeLayout implements ILayouter {
		private final IContainer container;
		private int layoutHashCode;
		private final AttributeLayoutManager attributeLayoutManager;

		private final HashMap<IControl, Dimension> preferredSizes;

		private AttributeLayout(final IContainer container, final AttributeLayoutManager attributeLayoutManager) {
			this.container = container;
			this.preferredSizes = new HashMap<IControl, Dimension>();
			this.layoutHashCode = 0;
			this.attributeLayoutManager = attributeLayoutManager;
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
				else if (attributeLayoutManager.modes[index] == FillMode.CENTER) {
					controlWidth = size.getWidth();
				}
				else if (attributeLayoutManager.modes[index] == FillMode.HIDDEN) {
					controlWidth = 0;
				}
				else {
					throw new IllegalStateException("Unkown fill mode");
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
		public void invalidate() {}

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
				if (size.getHeight() > 0) {
					preferredSizes.put(control, size);
				}
				return size;
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
				final boolean controlVisible = control.isVisible();
				if (!control.isEnabled()) {
					if (controlVisible) {
						control.setVisible(false);
					}
					continue;
				}

				if (!controlVisible) {
					control.setVisible(true);
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
				final boolean controlVisible = control.isVisible();
				if (!control.isEnabled()) {
					if (controlVisible) {
						control.setVisible(false);
					}
					continue;
				}

				if (!controlVisible) {
					control.setVisible(true);
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
				if (size.getHeight() > 0) {
					preferredSizes.put(control, size);
				}
				return size;
			}
			return preferredSizes.get(control);
		}

	}

	private enum Visibility {
		NONE,
		PARTIAL,
		ALL
	}

	private final class AllAttributeInformation {
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

			// TODO NM
			headerFormats.add(DisplayFormat.SHORT.getName());
			headerFormats.add(DisplayFormat.LONG.getName());

			sortable = sortableCount > 0;
		}
	}

	private final class AttributeGroupInformation {
		private final IAttributeGroup group;
		private final int startIndex;
		private final int endIndex;
		private final boolean sortable;
		private final String[] propertyNames;
		private final List<String> headerFormats;
		private final List<String> contentFormats;
		private boolean collapsed;

		private AttributeGroupInformation(final int startIndex, final IBeanTableModel<?> model) {
			this.group = model.getAttribute(startIndex).getGroup();
			this.startIndex = startIndex;
			this.endIndex = getGroupEnd(model, startIndex);
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

		private String getId() {
			if (group == null) {
				return null;
			}
			return group.getId();
		}

		private boolean isCollapsed() {
			return collapsed;
		}

		private void setCollapsed(final boolean collapsed) {
			this.collapsed = collapsed;
			attributeLayoutManager.setGroupVisible(getId(), !collapsed);

			final AttributeGroupHeader header = groupHeaders.get(getId());
			final IToolBarButton button = header.getCollapseButton();
			if (collapsed) {
				button.setIcon(IconsSmall.TABLE_SORT_ASC);
			}
			else {
				button.setIcon(IconsSmall.TABLE_SORT_DESC);
			}
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

	private static boolean hasShortAndLongLabel(final IAttribute<?> attribute) {
		return ((attribute.getLabelLong() != null) && (!attribute.getLabelLong().equals(attribute.getLabel())));
	}

	private static boolean hasContentFormatSelection(final IAttribute<?> attribute) {
		return attribute.getControlPanels().size() > 1;
	}

	private static String getDisplayFormatNameById(final String id) {
		for (final DisplayFormat format : DisplayFormat.values()) {
			if (format.getId().equals(id)) {
				return format.getName();
			}
		}
		return null;
	}

	private static void setValue(final IComboBox<String> comboBox, final Set<String> values) {
		if (values.size() == 1) {
			setValue(comboBox, (String) values.toArray()[0]);
		}
		else {
			setValue(comboBox, (String) null);
		}
	}

	private static void setValue(final SortingIndexComboBox comboBox, final int index) {
		if (comboBox != null) {
			comboBox.setIndex(index);
		}
	}

	private static void setValue(final IComboBox<String> comboBox, final String value) {
		if (comboBox == null) {
			return;
		}
		if ((value != null) && (!comboBox.getElements().contains(value))) {
			return;
		}
		if (NullCompatibleEquivalence.equals(value, comboBox.getValue())) {
			return;
		}
		comboBox.setValue(value);
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
			for (int i = 0; i < range; i++) {
				result.add(String.valueOf(i + 1));
			}
			if ((getIndex() == 0) && (range < maxSortingLength)) {
				result.add(String.valueOf(range + 1));
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
		ComboBox getSort(AbstractAttributeComposite<?> composite);

		SortingIndexComboBox getSortIndex(AbstractAttributeComposite<?> composite);
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
		boolean changed = false;
		final boolean allVisible = (text == null) || (text.equals(""));

		final HashSet<String> visibleList = new HashSet<String>();

		for (final Entry<String, AttributeComposite> entry : attributeComposites.entrySet()) {
			final AttributeComposite attributeComposite = entry.getValue();
			final boolean visible = allVisible || checkFilter(attributeComposite, text);
			changed = changed || (attributeComposite.isEnabled() != visible);
			attributeComposite.setEnabled(visible);

			if (visible) {
				visibleList.add(attributeComposite.propertyName);
			}
		}

		if (attributesFilterComposite.isVisible() != !allVisible) {
			attributesFilterComposite.setVisible(!allVisible);
			layoutBegin();
			layoutEnd();
		}

		if (changed) {
			attributesFilterComposite.updateData();
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

				if (attributeGroupHeader.isEnabled() != headerVisible) {
					attributeGroupHeader.setEnabled(headerVisible);
				}
				if (container.isEnabled() == attributeGroupHeader.getData().collapsed) {
					container.setEnabled(!attributeGroupHeader.getData().collapsed);
				}

				container.layoutBegin();
				container.layoutEnd();
			}
			attributeLayoutManager.invalidate();
		}
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
}
