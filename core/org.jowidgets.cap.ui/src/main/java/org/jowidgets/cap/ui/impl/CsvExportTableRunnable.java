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

import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.ICsvExportParameter;
import org.jowidgets.cap.ui.api.table.IExternalReader;
import org.jowidgets.cap.ui.impl.CsvExporter.IBeanProvider;

final class CsvExportTableRunnable implements Runnable {

	private final IBeanTableModel<?> model;
	private final IResultCallback<String> resultCallback;
	private final ICsvExportParameter parameter;
	private final IExecutionCallback executionCallback;

	CsvExportTableRunnable(
		final IBeanTableModel<?> model,
		final IResultCallback<String> resultCallback,
		final ICsvExportParameter parameter,
		final IExecutionCallback executionCallback) {

		this.model = model;
		this.resultCallback = resultCallback;
		this.parameter = parameter;
		this.executionCallback = executionCallback;
	}

	@Override
	public void run() {
		final CsvExporter exporter = new CsvExporter(parameter, model);
		try {
			exporter.export(new BeanProvider(), executionCallback);
		}
		catch (final Exception exception) {
			resultCallback.exception(exception);
		}
	}

	private final class BeanProvider implements IBeanProvider {

		private static final int PAGE_SIZE = 1000;

		private final IExternalReader reader;

		private boolean hasMoreBeans;
		private int currentPage;

		private BeanProvider() {
			this.reader = model.getExternalReader();
			this.hasMoreBeans = true;
			this.currentPage = 0;
		}

		@Override
		public boolean hasMoreBeans() {
			return hasMoreBeans;
		}

		@Override
		public void countBeans(final IResultCallback<Integer> resultCallback) {
			reader.count(resultCallback, executionCallback);
		}

		@Override
		public void getBeans(final IResultCallback<List<IBeanDto>> resultCallback) {
			final IResultCallback<List<IBeanDto>> decoratedResult = new IResultCallback<List<IBeanDto>>() {

				@Override
				public void finished(final List<IBeanDto> result) {
					if (result.size() > PAGE_SIZE) {
						hasMoreBeans = true;
						currentPage++;
					}
					else {
						hasMoreBeans = false;
					}
					resultCallback.finished(result);
				}

				@Override
				public void exception(final Throwable exception) {
					resultCallback.exception(exception);
				}
			};

			hasMoreBeans = false;
			reader.read(decoratedResult, currentPage * PAGE_SIZE, PAGE_SIZE + 1, executionCallback);
		}

		@Override
		public void finished(final String message) {
			resultCallback.finished(message);
		}

		@Override
		public void exception(final Throwable exception) {
			resultCallback.exception(exception);
		}
	}

}
