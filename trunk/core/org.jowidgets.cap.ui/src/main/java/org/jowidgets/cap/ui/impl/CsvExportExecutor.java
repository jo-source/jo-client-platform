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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.convert.IObjectLabelConverter;
import org.jowidgets.api.convert.IObjectStringConverter;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IWindow;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
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
import org.jowidgets.util.concurrent.DaemonThreadFactory;

class CsvExportExecutor<BEAN_TYPE> implements IExecutor<BEAN_TYPE, ICsvExportParameter> {

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

		final IExecutionTask executionTask = CapUiToolkit.executionTaskFactory().create();
		final IExecutionTaskDialog executionTaskDialog = createExecutionTaskDialog(executionContext, executionTask);
		final IResultCallback<String> resultCallback = new ResultCallback(executionTaskDialog);
		final Map<String, IObjectStringConverter<Object>> converters = createConverterMap(parameter);

		final Runnable exportRunnable;
		if (parameter.getExportType() == ExportType.SELECTION) {
			final List<IBeanDto> beanDtos = createBeanDtos(beans, converters.keySet());
			exportRunnable = new SelectedExportRunnable(resultCallback, parameter, beanDtos, executionTask, converters);
		}
		else {
			exportRunnable = new TableExportRunnable(resultCallback, parameter, executionTask, converters);
		}

		executionTaskDialog.setVisible(true);
		executor.execute(exportRunnable);
	}

	private Map<String, IObjectStringConverter<Object>> createConverterMap(final ICsvExportParameter parameter) {
		final Map<String, IObjectStringConverter<Object>> result = new LinkedHashMap<String, IObjectStringConverter<Object>>();
		for (final IAttribute<Object> attribute : model.getAttributes()) {
			final String propertyName = attribute.getPropertyName();
			if (attribute.isVisible() || parameter.isExportInvisibleProperties()) {
				final IControlPanelProvider<Object> controlPanel = attribute.getCurrentControlPanel();
				if (controlPanel != null) {
					final IObjectLabelConverter<Object> converter = controlPanel.getObjectLabelConverter();
					if (converter != null) {
						result.put(propertyName, converter);
					}
				}
			}
		}
		return result;
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

	@SuppressWarnings("unused")
	private String beanToCsv(final IBeanDto bean, final ICsvExportParameter parameter) {
		final StringBuilder result = new StringBuilder();
		for (final IAttribute<Object> attribute : model.getAttributes()) {
			final String propertyName = attribute.getPropertyName();
			if (attribute.isVisible() || parameter.isExportInvisibleProperties()) {
				final IControlPanelProvider<Object> controlPanel = attribute.getCurrentControlPanel();
				if (controlPanel != null) {
					final IObjectLabelConverter<Object> converter = controlPanel.getObjectLabelConverter();
					if (converter != null) {
						final String propValue = converter.convertToString(bean.getValue(propertyName));
						//TODO SP add to result
					}
				}
			}
		}
		return result.toString();
	}

	private final class SelectedExportRunnable implements Runnable {

		private final IResultCallback<String> resultCallback;
		@SuppressWarnings("unused")
		private final ICsvExportParameter parameter;
		@SuppressWarnings("unused")
		private final List<IBeanDto> beans;
		@SuppressWarnings("unused")
		private final IExecutionCallback executionCallback;
		@SuppressWarnings("unused")
		private final Map<String, IObjectStringConverter<Object>> converters;

		private SelectedExportRunnable(
			final IResultCallback<String> resultCallback,
			final ICsvExportParameter parameter,
			final List<IBeanDto> beans,
			final IExecutionCallback executionCallback,
			final Map<String, IObjectStringConverter<Object>> converters) {

			this.resultCallback = resultCallback;
			this.parameter = parameter;
			this.beans = beans;
			this.executionCallback = executionCallback;
			this.converters = converters;
		}

		@Override
		public void run() {
			resultCallback.finished("Exported all");
		}

	}

	private final class TableExportRunnable implements Runnable {

		private final IResultCallback<String> resultCallback;
		@SuppressWarnings("unused")
		private final ICsvExportParameter parameter;
		private final IExecutionCallback executionCallback;
		@SuppressWarnings("unused")
		private final Map<String, IObjectStringConverter<Object>> converters;

		private TableExportRunnable(
			final IResultCallback<String> resultCallback,
			final ICsvExportParameter parameter,
			final IExecutionCallback executionCallback,
			final Map<String, IObjectStringConverter<Object>> converters) {

			this.resultCallback = resultCallback;
			this.parameter = parameter;
			this.executionCallback = executionCallback;
			this.converters = converters;
		}

		@Override
		public void run() {
			final int steps = 1000;
			executionCallback.setTotalStepCount(steps);
			for (int i = 0; i < steps && !executionCallback.isCanceled(); i++) {
				//CHECKSTYLE:OFF
				System.out.println("step: " + i);
				//CHECKSTYLE:ON
				executionCallback.workedOne();
				executionCallback.setDescription("Step: " + i);
				try {
					Thread.sleep(50);
				}
				catch (final InterruptedException e) {
				}
			}
			if (executionCallback.isCanceled()) {
				//CHECKSTYLE:OFF
				System.out.println("CANCELED");
				//CHECKSTYLE:ON
				return;
			}
			else {
				resultCallback.finished("Exported 1000 beans");
			}
		}

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
			executionTaskDialog.executionFinished(exception.getMessage());
		}

	}

	private static final class BeanDto implements IBeanDto {

		private final Object id;
		private final long version;
		private final Map<String, Object> values;

		public BeanDto(final IBeanProxy<?> bean, final Collection<String> properties) {

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
