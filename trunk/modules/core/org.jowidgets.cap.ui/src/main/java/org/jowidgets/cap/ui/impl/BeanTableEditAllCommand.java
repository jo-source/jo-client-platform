/*
 * Copyright (c) 2014, MGrossmann
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

import java.util.List;

import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IEnabledState;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.api.widgets.IInputDialog;
import org.jowidgets.api.widgets.blueprint.IInputDialogBluePrint;
import org.jowidgets.api.widgets.content.IInputContentContainer;
import org.jowidgets.api.widgets.content.IInputContentCreator;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.common.types.Markup;
import org.jowidgets.common.widgets.controller.ITableCellPopupEvent;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.validation.MandatoryValidator;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.event.IChangeListener;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.maybe.Nothing;
import org.jowidgets.util.maybe.Some;
import org.jowidgets.validation.IValidator;

final class BeanTableEditAllCommand<BEAN_TYPE> implements ICommand, ICommandExecutor, IEnabledChecker {

	private final IBeanTableModel<BEAN_TYPE> model;
	private final int columnIndex;
	private final String propertyName;

	private final IEnabledChecker enabledChecker;

	BeanTableEditAllCommand(final IBeanTableModel<BEAN_TYPE> model, final int columnIndex) {

		this.model = model;
		this.columnIndex = columnIndex;
		this.propertyName = model.getAttribute(columnIndex).getPropertyName();

		this.enabledChecker = new BeanSelectionProviderEnabledChecker<BEAN_TYPE>(
			model,
			BeanSelectionPolicy.MULTI_SELECTION,
			BeanModificationStatePolicy.ANY_MODIFICATION,
			BeanMessageStatePolicy.NO_ERROR,
			true);
	}

	@Override
	public ICommandExecutor getCommandExecutor() {
		return this;
	}

	@Override
	public IEnabledChecker getEnabledChecker() {
		return this;
	}

	@Override
	public IExceptionHandler getExceptionHandler() {
		return null;
	}

	@Override
	public void execute(final IExecutionContext executionContext) throws Exception {

		final ITableCellPopupEvent event = executionContext.getValue(IBeanTable.CELL_POPUP_EVENT_CONTEXT_KEY);
		final int rowIndex = event.getRowIndex();
		final Object currentValue = model.getValue(rowIndex, columnIndex);

		final IMaybe<Object> valueMaybe = getNewValue(executionContext, currentValue, model.getAttribute(columnIndex));

		if (valueMaybe.isNothing()) {
			return;
		}

		final Object value = valueMaybe.getValue();

		final List<IBeanProxy<BEAN_TYPE>> selectedBeans = model.getSelectedBeans();
		if (model.getSelectedBeans().size() > 1) {
			for (final IBeanProxy<BEAN_TYPE> bean : selectedBeans) {
				if (bean != null && !bean.isDisposed() && !bean.isDummy() && !bean.isLastRowDummy()) {
					bean.setValue(propertyName, value);
				}
			}
		}
		else {
			for (int i = 0; i < model.getSize(); i++) {
				final IBeanProxy<?> bean = model.getBean(i);
				if (bean != null && !bean.isDisposed() && !bean.isDummy() && !bean.isLastRowDummy()) {
					bean.setValue(propertyName, value);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private IMaybe<Object> getNewValue(
		final IExecutionContext executionContext,
		final Object currentValue,
		final IAttribute<Object> attribute) {

		final ICustomWidgetCreator<IInputControl<Object>> widgetCreator = getWidgetCreator(attribute);
		if (widgetCreator == null) {
			//TODO this should not occur, show an error to the user
			return Nothing.getInstance();
		}

		final IInputDialogBluePrint<Object> dialogBp = BPF.inputDialog(new CurrentValueContentCreator(widgetCreator, attribute));
		dialogBp.setExecutionContext(executionContext);
		dialogBp.setAutoDispose(true);
		dialogBp.setMinPackSize(new Dimension(400, 200));
		dialogBp.setMaxPackSize(new Dimension(800, 600));

		final IInputDialog<Object> dialog = Toolkit.getActiveWindow().createChildWindow(dialogBp);
		dialog.setValue(currentValue);
		dialog.setVisible(true);

		if (dialog.isOkPressed()) {
			return new Some<Object>(dialog.getValue());
		}
		else {
			return Nothing.getInstance();
		}
	}

	@SuppressWarnings({"rawtypes"})
	private ICustomWidgetCreator getWidgetCreator(final IAttribute<Object> attribute) {
		final IControlPanelProvider<Object> controlPanel = attribute.getCurrentControlPanel();
		if (controlPanel != null) {
			if (attribute.isCollectionType()) {
				return controlPanel.getCollectionControlCreator();
			}
			else {
				return controlPanel.getControlCreator();
			}
		}
		return null;
	}

	@Override
	public void addChangeListener(final IChangeListener listener) {
		enabledChecker.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(final IChangeListener listener) {
		enabledChecker.removeChangeListener(listener);
	}

	@Override
	public IEnabledState getEnabledState() {
		return enabledChecker.getEnabledState();
	}

	private final class CurrentValueContentCreator implements IInputContentCreator<Object> {

		private final IAttribute<Object> attribute;
		private final ICustomWidgetCreator<IInputControl<Object>> controlCreator;

		private IInputControl<Object> inputControl;

		private CurrentValueContentCreator(
			final ICustomWidgetCreator<IInputControl<Object>> controlCreator,
			final IAttribute<Object> attribute) {

			this.controlCreator = controlCreator;
			this.attribute = attribute;
		}

		@Override
		public void setValue(final Object value) {
			inputControl.setValue(value);
		}

		@Override
		public Object getValue() {
			return inputControl.getValue();
		}

		@Override
		public void createContent(final IInputContentContainer container) {
			container.setLayout(new MigLayoutDescriptor("[][grow]", "[]"));

			container.add(BPF.textLabel(attribute.getCurrentLabel()).setMarkup(Markup.STRONG));
			inputControl = container.add(controlCreator, "growx, w 0::");

			final IValidator<Object> validator = attribute.getValidator();
			if (validator != null) {
				inputControl.addValidator(validator);
			}

			if (attribute.isMandatory()) {
				inputControl.addValidator(new MandatoryValidator<Object>());
			}
		}

	}

}
