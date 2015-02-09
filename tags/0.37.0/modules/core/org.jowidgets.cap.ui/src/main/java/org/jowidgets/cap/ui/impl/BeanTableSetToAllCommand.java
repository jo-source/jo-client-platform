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
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.common.widgets.controller.ITableCellPopupEvent;
import org.jowidgets.util.event.IChangeListener;

final class BeanTableSetToAllCommand<BEAN_TYPE> implements ICommand, ICommandExecutor, IEnabledChecker {

	private final IBeanTableModel<BEAN_TYPE> model;
	private final int columnIndex;
	private final String propertyName;

	private final IEnabledChecker enabledChecker;

	BeanTableSetToAllCommand(final IBeanTableModel<BEAN_TYPE> model, final int columnIndex) {

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
		final Object value = model.getValue(rowIndex, columnIndex);

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

}
