/*
 * Copyright (c) 2014, grossmann
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
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.image.Icons;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.types.QuestionResult;
import org.jowidgets.api.widgets.IFileChooser;
import org.jowidgets.api.widgets.IQuestionDialog;
import org.jowidgets.api.widgets.blueprint.IFileChooserBluePrint;
import org.jowidgets.api.widgets.blueprint.IQuestionDialogBluePrint;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.execution.IParameterProvider;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.ICsvExportParameter;
import org.jowidgets.common.types.DialogResult;
import org.jowidgets.common.types.FileChooserType;
import org.jowidgets.common.types.IFileChooserFilter;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.tools.types.FileChooserFilter;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.maybe.Nothing;
import org.jowidgets.util.maybe.Some;

final class CsvExportFileParameterProvider<BEAN_TYPE> implements IParameterProvider<BEAN_TYPE, ICsvExportParameter> {

	private static final String USER_HOME_PATH = System.getProperty("user.home");
	private final List<IFileChooserFilter> filterList;

	private String filename;

	CsvExportFileParameterProvider(final IBeanTableModel<BEAN_TYPE> model) {
		this.filterList = generateFilterList();
		this.filename = USER_HOME_PATH
			+ File.separator
			+ model.getEntityLabelPlural()
			+ "."
			+ this.filterList.get(0).getExtensions().get(0);
	}

	private static List<IFileChooserFilter> generateFilterList() {
		final List<IFileChooserFilter> result = new LinkedList<IFileChooserFilter>();
		result.add(new FileChooserFilter(Messages.getString("CsvExportFileParameterProvider.csv_file"), "csv"));
		return result;
	}

	@Override
	public IMaybe<ICsvExportParameter> getParameter(
		final IExecutionContext executionContext,
		final List<IBeanProxy<BEAN_TYPE>> beans,
		final ICsvExportParameter defaultParameter) throws Exception {

		final IFileChooserBluePrint fileChooserBp = BPF.fileChooser(FileChooserType.SAVE).setFilterList(filterList);
		final IFileChooser fileChooser = Toolkit.getActiveWindow().createChildWindow(fileChooserBp);
		fileChooser.setSelectedFile(new File(filename));
		final DialogResult result = fileChooser.open();
		if (result == DialogResult.OK) {
			for (final File file : fileChooser.getSelectedFiles()) {
				filename = file.getAbsolutePath();
			}
			if (!filename.contains(fileChooser.getSelectedFilter().getExtensions().get(0))) {
				filename = filename + "." + fileChooser.getSelectedFilter().getExtensions().get(0);
			}

			if (EmptyCheck.isEmpty(filename)) {
				Toolkit.getMessagePane().showInfo(
						executionContext,
						Messages.getString("CsvExportFileParameterProvider.no_file_choosen"));
				return Nothing.getInstance();
			}

			if (!checkFileType(filename.substring(filename.length() - 3))) {
				Toolkit.getMessagePane().showError(
						executionContext,
						Messages.getString("CsvExportFileParameterProvider.incompatible_file"));
				return Nothing.getInstance();
			}

			final File file = new File(filename);

			if (file.exists()) {
				final String question = Messages.getString("CsvExportFileParameterProvider.file_overwritten");
				final IQuestionDialogBluePrint dialogBp = BPF.questionDialog();
				dialogBp.setTitle(executionContext.getAction().getText());
				dialogBp.setIcon(executionContext.getAction().getIcon());
				dialogBp.setIcon(Icons.WARNING);
				dialogBp.setText(MessageReplacer.replace(question, file.getName()));
				dialogBp.setDefaultResult(QuestionResult.NO);

				final IQuestionDialog dialog = Toolkit.getActiveWindow().createChildWindow(dialogBp);

				if (!QuestionResult.YES.equals(dialog.question())) {
					return Nothing.getInstance();
				}
			}

			return new Some<ICsvExportParameter>(new CsvExportParameter(
				defaultParameter.getExportType(),
				defaultParameter.isExportHeader(),
				defaultParameter.isExportInvisibleProperties(),
				defaultParameter.getSeparator(),
				defaultParameter.getMask(),
				defaultParameter.getEncoding(),
				filename));
		}
		else {
			return Nothing.getInstance();
		}
	}

	private boolean checkFileType(final String filetype) {
		for (final IFileChooserFilter filter : filterList) {
			if (filetype.equals(filter.getExtensions().get(0))) {
				return true;
			}
		}
		return false;
	}

}
