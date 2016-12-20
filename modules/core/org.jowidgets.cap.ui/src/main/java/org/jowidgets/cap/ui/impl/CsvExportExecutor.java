/*
 * Copyright (c) 2012, grossmann
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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IWindow;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.execution.IExecutor;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.ICsvExportParameter;
import org.jowidgets.cap.ui.api.table.ICsvExportParameter.ExportType;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.cap.ui.api.widgets.IExecutionTaskDialog;
import org.jowidgets.cap.ui.api.widgets.IExecutionTaskDialogBluePrint;
import org.jowidgets.cap.ui.tools.execution.AbstractUiResultCallback;
import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;
import org.jowidgets.util.concurrent.DaemonThreadFactory;

class CsvExportExecutor<BEAN_TYPE> implements IExecutor<BEAN_TYPE, ICsvExportParameter> {

	private static final ILogger LOGGER = LoggerProvider.get(CsvExportExecutor.class);

	private final IBeanTableModel<BEAN_TYPE> model;

	private final Executor executor;

	CsvExportExecutor(final IBeanTableModel<BEAN_TYPE> model) {
		this.model = model;
		this.executor = Executors.newFixedThreadPool(10, new DaemonThreadFactory());
	}

	@Override
	public void execute(
		final IExecutionContext executionContext,
		final List<IBeanProxy<BEAN_TYPE>> beans,
		final ICsvExportParameter parameter) throws Exception {

		final IExecutionTask executionTask = CapUiToolkit.executionTaskFactory().create(executionContext);
		final IExecutionTaskDialog executionTaskDialog = createExecutionTaskDialog(executionContext, executionTask);
		final IResultCallback<String> resultCallback = new ResultCallback(executionTaskDialog);

		final Runnable exportRunnable;
		if (parameter.getExportType() == ExportType.SELECTION) {
			final List<IBeanDto> beanDtos = createBeanDtos(beans, model.getPropertyNames());
			exportRunnable = new CsvExportSelectedRunnable(model, resultCallback, parameter, beanDtos, executionTask);
		}
		else {
			final List<IBeanDto> addedData = createBeanDtos(model.getAddedData(), model.getPropertyNames());
			exportRunnable = new CsvExportTableRunnable(model, addedData, resultCallback, parameter, executionTask);
		}

		executionTaskDialog.setVisible(true);
		executor.execute(exportRunnable);
	}

	private static IExecutionTaskDialog createExecutionTaskDialog(
		final IExecutionContext executionContext,
		final IExecutionTask executionTask) {

		final ICapApiBluePrintFactory cbpf = CapUiToolkit.bluePrintFactory();
		final IExecutionTaskDialogBluePrint executionTaskDialogBp = cbpf.executionTaskDialog(executionContext, executionTask);
		executionTaskDialogBp.setModal(false);
		final IWindow activeWindow = Toolkit.getActiveWindow();
		return activeWindow.createChildWindow(executionTaskDialogBp);
	}

	private static List<IBeanDto> createBeanDtos(final List<? extends IBeanProxy<?>> beans, final Collection<String> properties) {
		final List<IBeanDto> result = new LinkedList<IBeanDto>();
		for (final IBeanProxy<?> bean : beans) {
			result.add(new BeanDto(bean, properties));
		}
		return result;
	}

	private static final class ResultCallback extends AbstractUiResultCallback<String> {

		private final IExecutionTaskDialog executionTaskDialog;

		private ResultCallback(final IExecutionTaskDialog executionTaskDialog) {
			this.executionTaskDialog = executionTaskDialog;
		}

		@Override
		public void finishedUi(final String result) {
			executionTaskDialog.executionFinished(result);
		}

		@Override
		public void exceptionUi(final Throwable exception) {
			LOGGER.error(exception);
			executionTaskDialog.executionError(exception.getMessage());
		}

	}

	private static final class BeanDto implements IBeanDto {

		private final Object id;
		private final long version;
		private final Map<String, Object> values;

		BeanDto(final IBeanProxy<?> bean, final Collection<String> properties) {

			this.id = bean.getId();
			this.version = bean.getVersion();
			this.values = new HashMap<String, Object>();

			for (final String propertyName : properties) {
				values.put(propertyName, bean.getValue(propertyName));
			}
		}

		@Override
		public Object getValue(final String propertyName) {
			return values.get(propertyName);
		}

		@Override
		public Object getId() {
			return id;
		}

		@Override
		public long getVersion() {
			return version;
		}

	}
}
