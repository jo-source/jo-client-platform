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

package org.jowidgets.cap.ui.impl;

import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IInputDialog;
import org.jowidgets.api.widgets.blueprint.IInputDialogBluePrint;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.api.widgets.content.IInputContentContainer;
import org.jowidgets.api.widgets.content.IInputContentCreator;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.filter.IFilterPanelProvider;
import org.jowidgets.cap.ui.api.filter.IFilterSupport;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.cap.ui.api.filter.IIncludingFilterFactory;
import org.jowidgets.cap.ui.api.filter.IUiConfigurableFilter;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IAttributeFilterControl;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.common.widgets.controller.ITableCellPopupEvent;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.command.ActionBuilder;

final class BeanTableAddCustomFilterActionBuilder extends ActionBuilder {

	BeanTableAddCustomFilterActionBuilder(final IBeanTableModel<?> model, final int columnIndex) {
		super();
		//TODO i18n
		setText("Custom filter ...");
		setToolTipText("Adds an custom filter to this column");
		setIcon(IconsSmall.FILTER);
		setCommand(new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				final IAttribute<Object> attribute = model.getAttribute(columnIndex);

				final ITableCellPopupEvent cellPopupEvent = executionContext.getValue(IBeanTable.CELL_POPUP_EVENT_CONTEXT_KEY);
				final Object cellValue = model.getValue(cellPopupEvent.getRowIndex(), columnIndex);

				final IFilterSupport<Object> filterSupport = attribute.getCurrentControlPanel().getFilterSupport();
				final IIncludingFilterFactory<Object> includingFilterFactory = filterSupport.getIncludingFilterFactory();
				final IUiConfigurableFilter<?> includingFilter = includingFilterFactory.getIncludingFilter(cellValue);

				final IFilterType filterType = includingFilter.getType();

				final IFilterPanelProvider<?> filterPanelProvider = getFilterPanelProvider(filterSupport, filterType);
				if (filterPanelProvider == null) {
					throw new IllegalStateException("No filter panel provider found for the attribute '"
						+ attribute.getPropertyName()
						+ "' and filter type '"
						+ filterType
						+ "'.");
				}
				else {
					openDialog(model, columnIndex, executionContext, filterPanelProvider, includingFilter);
				}
			}
		});
	}

	private IFilterPanelProvider<?> getFilterPanelProvider(
		final IFilterSupport<Object> filterSupport,
		final IFilterType filterType) {

		for (final IFilterPanelProvider<?> filterPanelProvider : filterSupport.getFilterPanels()) {
			if (filterType.getId().equals(filterPanelProvider.getType().getId())) {
				return filterPanelProvider;
			}
		}

		return null;
	}

	private void openDialog(
		final IBeanTableModel<?> model,
		final int columnIndex,
		final IExecutionContext executionContext,
		final IFilterPanelProvider<?> filterPanelProvider,
		final IUiConfigurableFilter<?> includingFilter) {

		final IBluePrintFactory bpf = Toolkit.getBluePrintFactory();
		final ICapApiBluePrintFactory capBpf = CapUiToolkit.bluePrintFactory();

		final IInputDialogBluePrint<IUiConfigurableFilter<? extends Object>> dialogBp = bpf.inputDialog(new IInputContentCreator<IUiConfigurableFilter<? extends Object>>() {

			private IAttributeFilterControl filterControl;

			@Override
			public void setValue(final IUiConfigurableFilter<? extends Object> value) {
				filterControl.setValue(value);
			}

			@Override
			public IUiConfigurableFilter<? extends Object> getValue() {
				return filterControl.getValue();
			}

			@Override
			public void createContent(final IInputContentContainer container) {
				container.setLayout(new MigLayoutDescriptor("0[][grow, 0::]0", "0[grow, 0::]0"));

				final IAttribute<?> attribute = model.getAttribute(columnIndex);
				container.add(Toolkit.getBluePrintFactory().textLabel(attribute.getLabel()));

				filterControl = container.add(capBpf.attributeFilterControl(model.getAttributes()), "growx, w 0::");
				filterControl.setValue(includingFilter);
			}
		});

		dialogBp.setTitle(executionContext.getAction().getText());

		final IInputDialog<IUiConfigurableFilter<?>> dialog = Toolkit.getActiveWindow().createChildWindow(dialogBp);
		dialog.setVisible(true);

		if (dialog.isOkPressed()) {
			model.addFilter(IBeanTableModel.UI_FILTER_ID, dialog.getValue());
			model.load();
		}
	}
	//	private void openDialog(
	//		final IBeanTableModel<?> model,
	//		final int columnIndex,
	//		final IExecutionContext executionContext,
	//		final IFilterPanelProvider<?> filterPanelProvider) {
	//		final IBluePrintFactory bpf = Toolkit.getBluePrintFactory();
	//		final IDialogBluePrint dialogBp = bpf.dialog(executionContext.getAction().getText()).setModal(false);
	//		final IFrame dialog = Toolkit.getActiveWindow().createChildWindow(dialogBp);
	//
	//		dialog.setLayout(new MigLayoutDescriptor("[]0[]", "[]"));
	//
	//		final List<String> properties = new LinkedList<String>();
	//		for (int i = 0; i < model.getColumnCount(); i++) {
	//			final IAttribute<Object> attribute = model.getAttribute(i);
	//			if (attribute.isFilterable()) {
	//				properties.add(attribute.getLabel());
	//			}
	//		}
	//
	//		final IButton button = dialog.add(bpf.button().setIcon(IconsSmall.POPUP_ARROW), "w 22!, h 22!");
	//		final IComboBox<String> comboBox = dialog.add(bpf.comboBoxSelection(properties).setAutoSelectionPolicy(
	//				AutoSelectionPolicy.PREVIOUS_SELECTED_OR_FIRST));
	//
	//		final IPopupMenu popupMenu = button.createPopupMenu();
	//		final IMenuModel menuModel = popupMenu.getModel();
	//		menuModel.addRadioItem("Arithmetic filter").setSelected(true);
	//		menuModel.addRadioItem("Arithmetic property filter");
	//		menuModel.addRadioItem("Boolean filter");
	//
	//		button.addActionListener(new IActionListener() {
	//			@Override
	//			public void actionPerformed() {
	//				popupMenu.show(new Position(0, Math.max(comboBox.getSize().getHeight(), button.getSize().getHeight())));
	//			}
	//		});
	//
	//		dialog.setVisible(true);
	//	}
}
