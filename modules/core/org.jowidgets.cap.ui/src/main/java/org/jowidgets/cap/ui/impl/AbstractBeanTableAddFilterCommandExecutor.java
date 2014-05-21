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

import org.jowidgets.api.command.EnabledState;
import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IEnabledState;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.cap.common.api.filter.IOperator;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.filter.IFilterPanelProvider;
import org.jowidgets.cap.ui.api.filter.IFilterSupport;
import org.jowidgets.cap.ui.api.filter.IIncludingFilterFactory;
import org.jowidgets.cap.ui.api.filter.IUiConfigurableFilter;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IPopupMenuListener;
import org.jowidgets.cap.ui.tools.model.DataModelContextExecutor;
import org.jowidgets.common.widgets.controller.ITableCellPopupEvent;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.tools.command.EnabledChecker;
import org.jowidgets.util.event.IChangeListener;

abstract class AbstractBeanTableAddFilterCommandExecutor implements ICommand, ICommandExecutor, IEnabledChecker {

	private static final IMessage OPERATOR_NOT_INVERTIBLE = Messages.getMessage("AbstractBeanTableAddFilterCommandExecutor.operatorNotInvertibe");

	private final IBeanTableModel<?> model;
	private final int columnIndex;
	private final boolean invert;
	private final EnabledChecker enabledChecker;

	AbstractBeanTableAddFilterCommandExecutor(final IBeanTable<?> table, final int columnIndex, final boolean invert) {
		this.model = table.getModel();
		this.columnIndex = columnIndex;
		this.invert = invert;
		this.enabledChecker = new EnabledChecker();

		enabledChecker.setEnabledState(EnabledState.DISABLED);
		table.addCellMenuListener(new IPopupMenuListener<ITableCellPopupEvent>() {
			@Override
			public void beforeMenuShow(final ITableCellPopupEvent event) {
				if (event.getColumnIndex() == columnIndex) {
					enabledChecker.setEnabledState(checkEnabledState(event.getRowIndex(), columnIndex));
				}
			}
		});
	}

	@Override
	public final void execute(final IExecutionContext executionContext) throws Exception {
		DataModelContextExecutor.executeDataChange(model, new Runnable() {
			@Override
			public void run() {
				doExecution(executionContext);
			}
		});
	}

	abstract void doExecution(IExecutionContext executionContext);

	@Override
	public final IEnabledState getEnabledState() {
		return enabledChecker.getEnabledState();
	}

	@Override
	public final void addChangeListener(final IChangeListener listener) {
		enabledChecker.addChangeListener(listener);
	}

	@Override
	public final void removeChangeListener(final IChangeListener listener) {
		enabledChecker.removeChangeListener(listener);
	}

	@Override
	public final ICommandExecutor getCommandExecutor() {
		return this;
	}

	@Override
	public final IEnabledChecker getEnabledChecker() {
		return this;
	}

	@Override
	public final IExceptionHandler getExceptionHandler() {
		return null;
	}

	final IUiConfigurableFilter<?> getIncludingFilter(final IExecutionContext executionContext) {
		final ITableCellPopupEvent cellPopupEvent = executionContext.getValue(IBeanTable.CELL_POPUP_EVENT_CONTEXT_KEY);
		return getIncludingFilter(cellPopupEvent.getRowIndex(), columnIndex);
	}

	abstract String getDisabledMessageOnNoIncludingFilter(String value);

	private IEnabledState checkEnabledState(final int rowIndex, final int columnIndex) {
		final Object value = model.getValue(rowIndex, columnIndex);
		final IUiConfigurableFilter<?> includingFilter = getIncludingFilter(rowIndex, columnIndex);
		if (includingFilter == null) {
			final IAttribute<Object> attribute = model.getAttribute(columnIndex);
			String valueAsString = attribute.getValueAsString(value);
			if (valueAsString == null) {
				valueAsString = "";
			}
			return EnabledState.disabled(getDisabledMessageOnNoIncludingFilter(valueAsString));
		}
		else if (invert) {
			final IAttribute<Object> attribute = model.getAttribute(columnIndex);
			final IFilterPanelProvider<IOperator> filterPanelProvider = attribute.getFilterPanelProvider(includingFilter.getType());
			if (filterPanelProvider != null) {
				final IOperator operator = includingFilter.getOperator();
				if (!filterPanelProvider.getOperatorProvider().isInvertible(operator)) {
					return EnabledState.disabled(MessageReplacer.replace(OPERATOR_NOT_INVERTIBLE.get(), operator.getLabelLong()));
				}
			}
			else {
				final String valueAsString = attribute.getValueAsString(value);
				EnabledState.disabled(getDisabledMessageOnNoIncludingFilter(valueAsString));
			}
		}
		return EnabledState.ENABLED;
	}

	private IUiConfigurableFilter<?> getIncludingFilter(final int rowIndex, final int columnIndex) {
		final IAttribute<Object> attribute = model.getAttribute(columnIndex);
		if (attribute != null) {
			final Object value = model.getValue(rowIndex, columnIndex);
			final IControlPanelProvider<Object> controlPanel = attribute.getCurrentIncludingFilterControlPanel();
			if (controlPanel != null) {
				final IFilterSupport<Object> filterSupport = controlPanel.getFilterSupport();
				if (filterSupport != null) {
					final IIncludingFilterFactory<Object> includingFilterFactory = filterSupport.getIncludingFilterFactory();
					if (includingFilterFactory != null) {
						return includingFilterFactory.getIncludingFilter(value);
					}
				}
			}
		}
		return null;
	}

}
