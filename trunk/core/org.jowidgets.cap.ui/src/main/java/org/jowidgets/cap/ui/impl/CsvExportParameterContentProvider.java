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
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IButton;
import org.jowidgets.api.widgets.ICheckBox;
import org.jowidgets.api.widgets.IComboBox;
import org.jowidgets.api.widgets.IFileChooser;
import org.jowidgets.api.widgets.IInputComponent;
import org.jowidgets.api.widgets.blueprint.ICheckBoxBluePrint;
import org.jowidgets.api.widgets.blueprint.IComboBoxSelectionBluePrint;
import org.jowidgets.api.widgets.blueprint.IFileChooserBluePrint;
import org.jowidgets.api.widgets.blueprint.ITextLabelBluePrint;
import org.jowidgets.api.widgets.content.IInputContentContainer;
import org.jowidgets.api.widgets.content.IInputContentCreator;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.ICsvExportParameter;
import org.jowidgets.cap.ui.api.table.ICsvExportParameter.ExportType;
import org.jowidgets.common.types.DialogResult;
import org.jowidgets.common.types.FileChooserType;
import org.jowidgets.common.types.IFileChooserFilter;
import org.jowidgets.common.widgets.controller.IActionListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.types.FileChooserFilter;
import org.jowidgets.tools.validation.MandatoryValidator;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.ValidationResult;

final class CsvExportParameterContentProvider<BEAN_TYPE> implements IInputContentCreator<ICsvExportParameter> {

	private IComboBox<String> separatorCmb;
	private IComboBox<ICsvExportParameter.ExportType> exportTypeCmb;
	private IFileChooser fileFC;
	private IInputComponent<String> filenameTL;
	private String filename;
	private IComboBox<String> maskCmb;
	private ICheckBox headerChb;
	private ICheckBox propertiesChb;
	private IComboBox<String> encodingCmb;
	private String filePath;

	private ICsvExportParameter parameter;

	CsvExportParameterContentProvider(final IBeanTableModel<BEAN_TYPE> model) {}

	@Override
	public void setValue(final ICsvExportParameter parameter) {
		this.parameter = parameter;

		if (parameter != null) {
			filenameTL.setValue(parameter.getFilename());
			filename = parameter.getFilename();
			headerChb.setValue(parameter.isExportHeader());
			propertiesChb.setValue(parameter.isExportInvisibleProperties());
			exportTypeCmb.setValue(parameter.getExportType());
			separatorCmb.setValue(String.valueOf(parameter.getSeparator()));
			maskCmb.setValue(String.valueOf(parameter.getMask()));
			encodingCmb.setValue(parameter.getEncoding());
		}
	}

	@Override
	public ICsvExportParameter getValue() {
		if (parameter != null) {
			return new CsvExportParameter(
				exportTypeCmb.getValue(),
				headerChb.getValue(),
				propertiesChb.getValue(),
				separatorCmb.getValue().charAt(0),
				maskCmb.getValue().charAt(0),
				encodingCmb.getValue(),
				//CHECKSTYLE:OFF
				//TODO SP fix later
				filename = (filename == null) ? "default_export" : filename);
			//CHECKSTYLE:On
		}
		else {
			return parameter;
		}
	}

	@Override
	public void createContent(final IInputContentContainer container) {
		final ITextLabelBluePrint textLabelBp = BPF.textLabel().alignRight();

		container.setLayout(new MigLayoutDescriptor("[][grow, 250::]", "[][]"));

		final IValidator<String> fileNameValidator = new IValidator<String>() {
			@Override
			public IValidationResult validate(final String value) {
				if (filePath != null) {
					if (new File(filePath).exists()) {
						return ValidationResult.warning(Messages.getString("CsvExportParameterContentProvider.file_overwritten"));
					}
				}
				return ValidationResult.ok();
			}
		};

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.filename")), "alignx r");
		filenameTL = container.add(BPF.inputFieldString().setEditable(true), "growx,w 0::,split");
		filenameTL.addValidator(fileNameValidator);
		filenameTL.addValidator(new MandatoryValidator<String>());

		final IButton openFileButton = container.add(BPF.button().setIcon(IconsSmall.EDIT), "w 0::25,h 0::25, wrap");
		openFileButton.addActionListener(new IActionListener() {
			@Override
			public void actionPerformed() {
				openFileChooser();
			}
		});

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.header")), "alignx r");
		final ICheckBoxBluePrint headerChbBp = BPF.checkBox();
		headerChb = container.add(headerChbBp, "split 3, w  0::");

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.properties")), "alignx r, growx");
		final ICheckBoxBluePrint propertiesChbBp = BPF.checkBox();
		propertiesChb = container.add(propertiesChbBp, "w 0::, wrap");

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.Export")), "alignx r");
		final IComboBoxSelectionBluePrint<ExportType> exportTypeCmbBp = BPF.comboBoxSelection(ICsvExportParameter.ExportType.values());
		exportTypeCmbBp.setAutoCompletion(false);
		exportTypeCmb = container.add(exportTypeCmbBp, "growx, w 0::, wrap");

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.separator")), "alignx r");
		final IComboBoxSelectionBluePrint<String> separatorCmbBp = BPF.comboBoxSelection(";", ",", "|");
		separatorCmbBp.setValue(";").setAutoCompletion(false);
		separatorCmb = container.add(separatorCmbBp, "growx, w 0::, wrap");

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.mask")), "align r");
		final IComboBoxSelectionBluePrint<String> maskCmbBp = BPF.comboBoxSelection("*", " ");
		maskCmbBp.setValue("*").setAutoCompletion(false);
		maskCmb = container.add(maskCmbBp, "growx, w 0::, wrap");

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.encoding")), "alignx r");
		final IComboBoxSelectionBluePrint<String> encodingCmbBp = BPF.comboBoxSelection("UTF-8", " ");
		encodingCmbBp.setValue("UTF-8").setAutoCompletion(false);
		encodingCmb = container.add(encodingCmbBp, "growx, w 0::");

	}

	private void openFileChooser() {
		final List<IFileChooserFilter> filterList = new LinkedList<IFileChooserFilter>();
		filterList.add(new FileChooserFilter(Messages.getString("CsvExportParameterContentProvider.csv_file"), "csv"));
		filterList.add(new FileChooserFilter(Messages.getString("CsvExportParameterContentProvider.text_file"), "txt"));
		final IFileChooserBluePrint fcBp = BPF.fileChooser(FileChooserType.OPEN_FILE).setFilterList(filterList);
		fileFC = Toolkit.getActiveWindow().createChildWindow(fcBp);
		final DialogResult result = fileFC.open();
		if (result == DialogResult.OK) {
			for (final File file : fileFC.getSelectedFiles()) {
				filePath = file.getPath();
				filenameTL.setValue(file.getName());
				filename = file.getAbsolutePath();

			}
		}
	}
}
