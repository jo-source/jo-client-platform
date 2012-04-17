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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jowidgets.api.convert.IObjectLabelConverter;
import org.jowidgets.api.convert.IObjectStringConverter;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.tools.execution.ResultCallbackAdapter;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.ICsvExportParameter;
import org.jowidgets.tools.message.MessageReplacer;

final class CsvExporter {

	private final ICsvExportParameter parameter;
	private final IBeanTableModel<?> model;
	private final Map<String, IObjectStringConverter<Object>> converter;

	CsvExporter(final ICsvExportParameter parameter, final IBeanTableModel<?> model) {
		this.parameter = parameter;
		this.model = model;
		this.converter = createConverterMap(model, parameter);
	}

	private static Map<String, IObjectStringConverter<Object>> createConverterMap(
		final IBeanTableModel<?> model,
		final ICsvExportParameter parameter) {
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

	void export(final IBeanProvider beanProvider, final IExecutionCallback executionCallback) throws Exception {
		final IResultCallback<Integer> result = new ResultCallbackAdapter<Integer>() {

			@Override
			public void finished(final Integer result) {
				try {
					if (result != null) {
						executionCallback.setTotalStepCount(result.intValue());
					}
					startExportBeans(beanProvider, result, executionCallback);
				}
				catch (final Exception e) {
					beanProvider.exception(e);
				}
			}

		};

		beanProvider.countBeans(result);
	}

	private void startExportBeans(
		final IBeanProvider beanProvider,
		final Integer totalCount,
		final IExecutionCallback executionCallback) throws Exception {

		final File file = new File(parameter.getFilename());
		final FileOutputStream fos = new FileOutputStream(file);
		final PrintStream ps = new PrintStream(fos, true, parameter.getEncoding());

		executionCallback.addExecutionCallbackListener(new IExecutionCallbackListener() {
			@Override
			public void canceled() {
				try {
					closeStream(ps);
				}
				catch (final Exception e) {
				}
			}
		});

		writeHeader(ps);
		exportBeans(beanProvider, ps, 0, totalCount, executionCallback);
	}

	void exportBeans(
		final IBeanProvider beanProvider,
		final PrintStream ps,
		final int exportedCount,
		final Integer totalCount,
		final IExecutionCallback executionCallback) {

		if (beanProvider.hasMoreBeans()) {
			final IResultCallback<List<IBeanDto>> result = new IResultCallback<List<IBeanDto>>() {

				@Override
				public void finished(final List<IBeanDto> result) {
					try {
						final int writtenBeans = writeBeans(result, ps, exportedCount, totalCount, executionCallback);
						exportBeans(beanProvider, ps, writtenBeans, totalCount, executionCallback);
					}
					catch (final Exception e) {
						beanProvider.exception(e);
					}
				}

				@Override
				public void exception(final Throwable exception) {
					try {
						closeStream(ps);
					}
					catch (final Exception e) {
						//a further exception has been occurred
					}
				}
			};
			beanProvider.getBeans(result);
		}
		else {
			beanProvider.finished(MessageReplacer.replace(
					Messages.getString("CsvExporter.exported") + " ' %1 ' %2",
					String.valueOf(exportedCount),
					model.getEntityLabelPlural()));
		}
	}

	private void writeHeader(final PrintStream ps) throws Exception {
		if (parameter.isExportHeader()) {
			final String header = createHeader();
			ps.println(header);
		}
	}

	private int writeBeans(
		final List<IBeanDto> beans,
		final PrintStream ps,
		int exportedCount,
		final Integer totalCount,
		final IExecutionCallback executionCallback) throws Exception {
		for (final IBeanDto bean : beans) {
			if (!executionCallback.isCanceled()) {
				exportedCount++;
				if (totalCount != null) {
					executionCallback.setDescription(MessageReplacer.replace(
							Messages.getString("CsvExporter.Export")
								+ " ' %1 ' "
								+ Messages.getString("CsvExporter.of")
								+ " ' %2 ' %3 ",
							String.valueOf(exportedCount),
							Integer.toString(totalCount),
							model.getEntityLabelPlural()));

					executionCallback.workedOne();
				}
				else {
					executionCallback.setDescription(MessageReplacer.replace(Messages.getString("CsvExporter.Export")
						+ "' %1 ' %2 ", String.valueOf(exportedCount), model.getEntityLabelPlural()));
				}
				ps.println(beanToCsv(bean));
			}
			else {
				return -1;
			}
		}
		return exportedCount;
	}

	private void closeStream(final PrintStream ps) {
		ps.close();
	}

	private String createHeader() {
		final StringBuilder result = new StringBuilder();
		for (final IAttribute<Object> attribute : model.getAttributes()) {
			if (attribute.isVisible() || parameter.isExportInvisibleProperties()) {
				final String label = attribute.getCurrentLabel();
				result.append(label + parameter.getSeparator());
			}
		}
		return result.toString();
	}

	private String beanToCsv(final IBeanDto bean) {
		final StringBuilder result = new StringBuilder();
		for (final Entry<String, IObjectStringConverter<Object>> entry : converter.entrySet()) {
			final Object value = bean.getValue(entry.getKey());
			String propValue;
			if (value instanceof Collection) {
				final StringBuilder cellValue = new StringBuilder();
				cellValue.append(parameter.getMask());
				for (final Object element : (Collection<?>) value) {
					final String elementString = entry.getValue().convertToString(element);
					cellValue.append(elementString + parameter.getSeparator());
				}
				propValue = cellValue.toString().substring(0, cellValue.length() - 1) + parameter.getMask();
				if (propValue.length() == 1) {
					propValue = null;
				}
			}
			else {
				propValue = entry.getValue().convertToString(bean.getValue(entry.getKey()));
			}
			if (propValue == null) {
				propValue = ("");
			}
			result.append(propValue + parameter.getSeparator());
		}
		return result.toString().substring(0, result.length() - 1);
	}

	interface IBeanProvider {

		boolean hasMoreBeans();

		void countBeans(IResultCallback<Integer> resultCallback);

		void getBeans(IResultCallback<List<IBeanDto>> resultCallback);

		void exception(Throwable exception);

		void finished(String message);

	}
}
