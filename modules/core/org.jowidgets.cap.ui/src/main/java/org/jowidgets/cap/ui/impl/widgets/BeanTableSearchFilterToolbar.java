/*
 * Copyright (c) 2011, grossmann
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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jowidgets.api.controller.IDisposeListener;
import org.jowidgets.api.convert.IStringObjectConverter;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.model.item.ICheckedItemModel;
import org.jowidgets.api.model.item.ICheckedItemModelBuilder;
import org.jowidgets.api.model.item.IItemModelFactory;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.ITextControl;
import org.jowidgets.api.widgets.IToolBar;
import org.jowidgets.api.widgets.IToolBarButton;
import org.jowidgets.api.widgets.blueprint.IToolBarButtonBluePrint;
import org.jowidgets.cap.common.api.bean.IStaticValueRange;
import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.common.api.filter.BooleanOperator;
import org.jowidgets.cap.common.api.lookup.ILookUpEntry;
import org.jowidgets.cap.common.api.lookup.ILookUpProperty;
import org.jowidgets.cap.common.api.lookup.ILookUpValueRange;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeSet;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.filter.IUiArithmeticFilter;
import org.jowidgets.cap.ui.api.filter.IUiBooleanFilter;
import org.jowidgets.cap.ui.api.filter.IUiBooleanFilterBuilder;
import org.jowidgets.cap.ui.api.filter.IUiFilter;
import org.jowidgets.cap.ui.api.filter.IUiFilterFactory;
import org.jowidgets.cap.ui.api.lookup.ILookUp;
import org.jowidgets.cap.ui.api.lookup.ILookUpAccess;
import org.jowidgets.cap.ui.api.model.DataModelChangeType;
import org.jowidgets.cap.ui.api.model.IChangeResponse;
import org.jowidgets.cap.ui.api.model.IChangeResponse.ResponseType;
import org.jowidgets.cap.ui.api.model.IDataModelContext;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.tools.filter.UiBooleanFilterWrapper;
import org.jowidgets.common.types.VirtualKey;
import org.jowidgets.common.widgets.controller.IActionListener;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.common.widgets.controller.IItemStateListener;
import org.jowidgets.common.widgets.controller.IKeyEvent;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.controller.KeyAdapter;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.ICallback;
import org.jowidgets.util.concurrent.DaemonThreadFactory;
import org.jowidgets.util.event.IChangeListener;

final class BeanTableSearchFilterToolbar<BEAN_TYPE> {

	private static final Pattern TOKENIZE_PATTERN = Pattern.compile("\"([^\"]*)\"|(\\S+)");

	private static final int LISTENER_DELAY = 500;

	private final IComposite composite;
	private final boolean searchFilterToolbarEnabled;
	private final IBeanTableModel<BEAN_TYPE> model;
	private final IAttributeSet attributes;
	private final IComposite toolbar;
	private final ITextControl textField;
	private final IToolBarButton closeButton;
	private final ICheckedItemModel searchFilterItemModel;
	private final IItemStateListener searchFilterItemListener;
	private final String searchFilterItemTooltip;
	private final IInputListener inputListener;
	private final IChangeListener filterChangeListener;

	private FilterResultLoader loader;
	private ScheduledExecutorService executorService;
	private String lastText;

	BeanTableSearchFilterToolbar(
		final IComposite composite,
		final boolean searchFilterToolbarEnabled,
		final BeanTableImpl<BEAN_TYPE> table) {
		this.composite = composite;
		this.searchFilterToolbarEnabled = searchFilterToolbarEnabled;
		this.model = table.getModel();
		this.attributes = model.getAttributeSet();

		this.toolbar = composite.add(0, BPF.composite(), "growx, w 0::");
		toolbar.setLayout(new MigLayoutDescriptor("0[][][grow]0", "0[grow]0[]0"));
		toolbar.setVisible(false);

		final IToolBar toolBar1 = toolbar.add(BPF.toolBar(), "");
		final IToolBarButtonBluePrint closeButtonBp = BPF.toolBarButton().setIcon(IconsSmall.DELETE);
		final String closeButtonTooltip = Messages.getString("BeanTableSearchFilterToolbar.hide_toolbar");
		closeButtonBp.setToolTipText(closeButtonTooltip);
		this.closeButton = toolBar1.addItem(closeButtonBp);

		toolBar1.pack();

		toolbar.add(BPF.textLabel().setText(Messages.getString("BeanTableSearchFilterToolbar.search_filter")), "");
		this.textField = toolbar.add(BPF.textField(), "growx, w 100::, wrap");
		toolbar.add(BPF.separator(), "growx, span 3, w 0::");
		toolbar.layout();

		closeButton.addActionListener(new IActionListener() {
			@Override
			public void actionPerformed() {
				setVisible(false);
			}
		});

		final String disabledTooltip = Messages.getString("BeanTableSearchFilterToolbar.hide_disabled");
		this.inputListener = new IInputListener() {
			@Override
			public void inputChanged() {
				final IDataModelContext context = model.getDataModelContext();
				final String newText = textField.getText();
				final IChangeResponse response = context.permitChange(DataModelChangeType.DATA_CHANGE);
				if (ResponseType.ASYNC == response.getType()) {
					context.permitChangeAsync(response, new ICallback<Boolean>() {
						@Override
						public void call(final Boolean parameter) {
							if (Boolean.TRUE.equals(parameter)) {
								doLoad(newText);
							}
							else {
								textField.removeInputListener(inputListener);
								textField.setText(lastText);
								textField.addInputListener(inputListener);
							}
						}
					});
				}
				else if (ResponseType.NO == response.getType()) {
					textField.removeInputListener(inputListener);
					textField.setText(lastText);
					textField.addInputListener(inputListener);
				}
				else {
					doLoad(newText);
				}
			}

			private void doLoad(final String newText) {
				lastText = newText;
				load(newText, false);
				if (EmptyCheck.isEmpty(newText)) {
					searchFilterItemModel.setEnabled(true);
					searchFilterItemModel.setToolTipText(searchFilterItemTooltip);
					closeButton.setEnabled(true);
					closeButton.setToolTipText(closeButtonTooltip);
				}
				else {
					searchFilterItemModel.setEnabled(false);
					searchFilterItemModel.setToolTipText(disabledTooltip);
					closeButton.setEnabled(false);
					closeButton.setToolTipText(disabledTooltip);
				}
			}
		};
		textField.addInputListener(inputListener);

		textField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(final IKeyEvent event) {
				if (event.getVirtualKey() == VirtualKey.ENTER) {
					final IChangeResponse response = model.getDataModelContext().permitChange(DataModelChangeType.DATA_CHANGE);
					if (ResponseType.YES == response.getType()) {
						load(textField.getText(), true);
					}
				}
			}

		});

		this.searchFilterItemTooltip = Messages.getString("BeanTableSearchFilterToolbar.show_searchfilter_tooltip");
		this.searchFilterItemModel = createSearchFilterItemModel();
		this.searchFilterItemListener = new IItemStateListener() {
			@Override
			public void itemStateChanged() {
				table.setSearchFilterToolbarVisible(searchFilterItemModel.isSelected());
			}
		};
		if (searchFilterToolbarEnabled) {
			searchFilterItemModel.addItemListener(searchFilterItemListener);
		}

		if (!EmptyCheck.isEmpty(textField.getText()) && !toolbar.isVisible()) {
			setVisible(true);
		}

		this.filterChangeListener = new IChangeListener() {
			@Override
			public void changed() {
				onFilterChanged();
			}
		};
		model.addFilterChangeListener(filterChangeListener);

		toolbar.addDisposeListener(new IDisposeListener() {
			@Override
			public void onDispose() {
				if (!model.isDisposed()) {
					model.removeFilterChangeListener(filterChangeListener);
				}
			}
		});

		onFilterChanged();
	}

	private void onFilterChanged() {
		final SearchFilter searchFilter = (SearchFilter) model.getFilter(IBeanTableModel.UI_SEARCH_FILTER_ID);
		if (searchFilter != null) {
			setSearchFilterTextWithoutEvents(searchFilter.getSearchText());
		}
		else {
			setSearchFilterTextWithoutEvents(null);
		}
	}

	private void setSearchFilterTextWithoutEvents(final String searchText) {
		textField.removeInputListener(inputListener);
		textField.setText(searchText);
		textField.addInputListener(inputListener);
		final boolean hasSearchText = !EmptyCheck.isEmpty(searchText);
		closeButton.setEnabled(!hasSearchText);
		if (hasSearchText && !toolbar.isVisible()) {
			toolbar.setVisible(true);
		}
	}

	private ICheckedItemModel createSearchFilterItemModel() {
		final IItemModelFactory modelFactory = Toolkit.getModelFactoryProvider().getItemModelFactory();
		final ICheckedItemModelBuilder builder = modelFactory.checkedItemBuilder();
		final String text = Messages.getString("BeanTableSearchFilterToolbar.show_searchfilter_text");
		builder.setText(text).setToolTipText(searchFilterItemTooltip);
		return builder.build();
	}

	void setVisible(final boolean visible) {
		if (!searchFilterToolbarEnabled) {
			return;
		}
		if (toolbar.isVisible() != visible) {
			composite.layoutBegin();
			toolbar.setVisible(visible);
			composite.layoutEnd();
			searchFilterItemModel.removeItemListener(searchFilterItemListener);
			searchFilterItemModel.setSelected(visible);
			searchFilterItemModel.addItemListener(searchFilterItemListener);
		}
	}

	public void setSearchFilterToolbarText(final String text) {
		textField.setText(text);
	}

	void requestSearchFocus() {
		textField.requestFocus();
	}

	ICheckedItemModel getItemModel() {
		return searchFilterItemModel;
	}

	private void load(final String text, final boolean immediate) {
		if (loader != null) {
			loader.cancel();
		}
		loader = new FilterResultLoader(text);
		if (immediate) {
			loader.loadImediate();
		}
		else {
			loader.loadScheduled();
		}
	}

	private final class FilterResultLoader {

		private final String text;
		private final IUiThreadAccess uiThreadAccess;

		private ScheduledFuture<?> schedule;
		private boolean canceled;

		private FilterResultLoader(final String text) {
			this.text = text;
			this.uiThreadAccess = Toolkit.getUiThreadAccess();
			this.canceled = false;
		}

		void loadScheduled() {
			final Runnable runnable = new Runnable() {
				@Override
				public void run() {
					uiThreadAccess.invokeLater(new Runnable() {
						@Override
						public void run() {
							loadImediate();
						}
					});
				}
			};
			schedule = getExecutorService().schedule(runnable, LISTENER_DELAY, TimeUnit.MILLISECONDS);
		}

		private ScheduledExecutorService getExecutorService() {
			if (executorService == null) {
				executorService = Executors.newSingleThreadScheduledExecutor(
						DaemonThreadFactory.create(BeanTableSearchFilterToolbar.class.getName() + "@" + hashCode()));
			}
			return executorService;
		}

		void loadImediate() {
			final IUiFilter filter;
			try {
				filter = createFilter(text);
			}
			catch (final CancellationException e) {
				return;
			}
			if (!canceled) {
				model.removeFilterChangeListener(filterChangeListener);
				model.setFilter(IBeanTableModel.UI_SEARCH_FILTER_ID, filter);
				model.addFilterChangeListener(filterChangeListener);
				model.load();
			}
			loader = null;
		}

		void cancel() {
			if (!canceled) {
				if (schedule != null) {
					schedule.cancel(false);
				}
				this.canceled = true;
				loader = null;
			}
		}

		void checkCanceled() {
			if (canceled) {
				throw new CancellationException();
			}
		}

		private IUiFilter createFilter(final String text) {
			checkCanceled();
			if (EmptyCheck.isEmpty(text)) {
				return null;
			}

			final Matcher matcher = TOKENIZE_PATTERN.matcher(text);

			final IUiFilterFactory factory = CapUiToolkit.filterToolkit().filterFactory();
			final IUiBooleanFilterBuilder andFilterBuilder = factory.booleanFilterBuilder().setOperator(BooleanOperator.AND);

			while (matcher.find()) {
				checkCanceled();
				final String token;
				if (matcher.group(1) != null) {
					token = matcher.group(1);
				}
				else {
					token = matcher.group(2);
				}
				final IUiBooleanFilterBuilder orFilterBuilder = factory.booleanFilterBuilder().setOperator(BooleanOperator.OR);
				boolean predicateCreated = false;
				for (final IAttribute<Object> attribute : attributes) {
					checkCanceled();
					if (attribute.isSearchable()) {
						final IUiFilter filter;
						if (attribute.getValueRange() instanceof ILookUpValueRange) {
							filter = createLookUpFilter(attribute, token);
						}
						else if (attribute.getValueRange() instanceof IStaticValueRange
							&& !((IStaticValueRange) attribute.getValueRange()).isOpen()) {
							filter = createManifoldTypeFilter(attribute, token);
						}
						else if (String.class.isAssignableFrom(attribute.getElementValueType())) {
							filter = createStringTypeFilter(attribute, token);
						}
						else {
							filter = createManifoldTypeFilter(attribute, token);
						}
						if (filter != null) {
							orFilterBuilder.addFilter(filter);
							predicateCreated = true;
						}
					}
				}
				if (predicateCreated) {
					andFilterBuilder.addFilter(orFilterBuilder.build());
				}
			}
			return new SearchFilter(andFilterBuilder.build(), text);
		}

		private IUiFilter createManifoldTypeFilter(final IAttribute<Object> attribute, final String string) {
			if (attribute.getControlPanels().size() == 1) {
				return createManifoldTypeFilter(attribute, attribute.getControlPanels().get(0), string);
			}
			else if (attribute.getControlPanels().size() > 1) {
				final IUiFilterFactory factory = CapUiToolkit.filterToolkit().filterFactory();
				final IUiBooleanFilterBuilder orFilterBuilder = factory.booleanFilterBuilder().setOperator(BooleanOperator.OR);
				boolean predicateCreated = false;
				for (final IControlPanelProvider<Object> controlPanel : attribute.getControlPanels()) {
					checkCanceled();
					final IUiArithmeticFilter<?> manifoldTypeFilter = createManifoldTypeFilter(attribute, controlPanel, string);
					if (manifoldTypeFilter != null) {
						orFilterBuilder.addFilter(manifoldTypeFilter);
						predicateCreated = true;
					}
				}
				if (predicateCreated) {
					return orFilterBuilder.build();
				}
			}
			return null;
		}

		private IUiArithmeticFilter<?> createManifoldTypeFilter(
			final IAttribute<Object> attribute,
			final IControlPanelProvider<Object> controlPanel,
			final String string) {
			final IStringObjectConverter<Object> converter = controlPanel.getStringObjectConverter();
			if (converter != null) {
				final Object value = converter.convertToObject(string);
				if (value != null) {
					return createAritmeticFilter(attribute, value);
				}
			}
			return null;
		}

		private IUiArithmeticFilter<?> createStringTypeFilter(final IAttribute<Object> attribute, final String string) {
			return createAritmeticFilter(attribute, createMaskedString(string));
		}

		private IUiFilter createLookUpFilter(final IAttribute<Object> attribute, final String string) {
			final ILookUpValueRange valueRange = (ILookUpValueRange) attribute.getValueRange();

			final ILookUpAccess lookUpAccess = CapUiToolkit.lookUpCache().getAccess(valueRange.getLookUpId());
			final ILookUp currentLookUp = lookUpAccess.getCurrentLookUp();
			if (currentLookUp == null) {
				return createManifoldTypeFilter(attribute, string);
			}

			final Set<Object> matchingEntries = new HashSet<Object>();
			final String pattern = createRegex(createMaskedString(string));
			for (final ILookUpProperty lookUpProperty : valueRange.getValueProperties()) {
				checkCanceled();
				matchingEntries.addAll(getMatchingEntries(currentLookUp, lookUpProperty, string, pattern));
			}
			if (!matchingEntries.isEmpty()) {
				final IUiFilterFactory factory = CapUiToolkit.filterToolkit().filterFactory();
				return factory.arithmeticFilter(
						attribute.getPropertyName(),
						ArithmeticOperator.CONTAINS_ANY,
						matchingEntries.toArray());
			}
			return null;
		}

		private Set<Object> getMatchingEntries(
			final ILookUp lookUp,
			final ILookUpProperty lookUpProperty,
			final String string,
			final String pattern) {
			final Set<Object> result = new HashSet<Object>();
			for (final ILookUpEntry lookUpEntry : lookUp.getEntries()) {
				checkCanceled();
				if (matches(lookUpEntry.getValue(lookUpProperty.getName()), pattern)) {
					result.add(lookUpEntry.getKey());
				}
			}
			return result;
		}

		private boolean matches(final Object source, final String pattern) {
			if (source != null && source instanceof String) {
				return ((String) source).toLowerCase().matches(pattern);
			}
			else if (source instanceof String) {
				return pattern == null;
			}
			return false;
		}

		private IUiArithmeticFilter<?> createAritmeticFilter(final IAttribute<Object> attribute, final Object value) {
			final IUiFilterFactory factory = CapUiToolkit.filterToolkit().filterFactory();
			return factory.arithmeticFilter(attribute.getPropertyName(), ArithmeticOperator.EQUAL, value);
		}

		private String createMaskedString(final String string) {
			return string + "*";
		}

		private String createRegex(final String search) {
			final StringBuilder regex = new StringBuilder(search.length());
			for (final char c : search.toLowerCase().toCharArray()) {
				switch (c) {
					case '\\':
						regex.append("\\\\");
						break;

					case '[':
					case ']':
					case '(':
					case ')':
					case '.':
					case '+':
					case '^':
					case '$':
						regex.append('\\');
						regex.append(c);
						break;

					// wild cards
					case '%':
					case '*':
						regex.append(".*");
						break;

					case '_':
						regex.append('.');
						break;

					default:
						regex.append(c);
				}
			}
			return regex.toString();
		}

	}

	private static final class SearchFilter extends UiBooleanFilterWrapper {

		private final String searchText;

		private SearchFilter(final IUiBooleanFilter original, final String searchText) {
			super(original);
			this.searchText = searchText;
		}

		private String getSearchText() {
			return searchText;
		}
	}

}
