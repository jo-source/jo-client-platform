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

import org.jowidgets.api.convert.IObjectStringConverter;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IButton;
import org.jowidgets.api.widgets.IComboBox;
import org.jowidgets.api.widgets.IFileChooser;
import org.jowidgets.api.widgets.IInputComponent;
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
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.ValidationResult;

final class CsvExportParameterContentProvider<BEAN_TYPE> implements IInputContentCreator<ICsvExportParameter> {

	private final String userHomePath = System.getProperty("user.home");
	private final IBeanTableModel<BEAN_TYPE> model;

	private IComboBox<String> separatorCmb;
	private IComboBox<ICsvExportParameter.ExportType> exportTypeCmb;
	private IFileChooser fileFC;
	private IInputComponent<String> filenameTf;
	private IComboBox<String> maskCmb;
	private IComboBox<Boolean> headerCmb;
	private IComboBox<Boolean> propertiesCmb;
	private IComboBox<String> encodingCmb;
	private ICsvExportParameter parameter;

	CsvExportParameterContentProvider(final IBeanTableModel<BEAN_TYPE> model) {
		this.model = model;
	}

	@Override
	public void setValue(final ICsvExportParameter parameter) {
		this.parameter = parameter;

		if (parameter != null) {
			headerCmb.setValue(parameter.isExportHeader());
			propertiesCmb.setValue(parameter.isExportInvisibleProperties());
			exportTypeCmb.setValue(parameter.getExportType());
			separatorCmb.setValue(String.valueOf(parameter.getSeparator()));
			maskCmb.setValue(String.valueOf(parameter.getMask()));
			encodingCmb.setValue(parameter.getEncoding());
			if (parameter.getFilename() == null) {
				filenameTf.setValue(userHomePath + File.separator + model.getEntityLabelPlural() + ".csv");
			}
			else {
				filenameTf.setValue(parameter.getFilename());
			}
		}
	}

	@Override
	public ICsvExportParameter getValue() {
		if (parameter != null) {
			return new CsvExportParameter(
				exportTypeCmb.getValue(),
				headerCmb.getValue() != null ? headerCmb.getValue() : true,
				propertiesCmb.getValue() != null ? propertiesCmb.getValue() : true,
				separatorCmb.getValue().charAt(0),
				maskCmb.getValue().charAt(0),
				encodingCmb.getValue(),
				filenameTf.getValue());
		}
		else {
			return parameter;
		}
	}

	@Override
	public void createContent(final IInputContentContainer container) {
		final ITextLabelBluePrint textLabelBp = BPF.textLabel().alignRight();
		container.setLayout(new MigLayoutDescriptor("[][grow, 250::]", "[][]"));

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.filename")), "alignx r");
		filenameTf = container.add(BPF.inputFieldString().setEditable(true), "growx,w 0::,split");
		filenameTf.addValidator(new FileNameValidator());
		createOpenFileButton(container);

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.header")), "alignx r");
		final IComboBoxSelectionBluePrint<Boolean> headerCmbBp = BPF.comboBoxSelectionBoolean().setAutoCompletion(false);
		headerCmb = container.add(headerCmbBp, "growx ,w  0:: , wrap");

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.properties")), "alignx r, growx");
		final IComboBoxSelectionBluePrint<Boolean> propertiesCmbBp = BPF.comboBoxSelection(createExportAttributesConverter());
		propertiesCmbBp.setElements(Boolean.TRUE, Boolean.FALSE).setValue(Boolean.FALSE).setAutoCompletion(false);
		propertiesCmb = container.add(propertiesCmbBp, "growx, w 0::, wrap");

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.Export")), "alignx r");
		final IComboBoxSelectionBluePrint<ExportType> exportTypeCmbBp = BPF.comboBoxSelection(ICsvExportParameter.ExportType.values());
		exportTypeCmbBp.setAutoCompletion(false);
		exportTypeCmb = container.add(exportTypeCmbBp, "growx, w 0::, wrap");

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.separator")), "alignx r");
		final IComboBoxSelectionBluePrint<String> separatorCmbBp = BPF.comboBoxSelection(";", ",", "|");
		separatorCmbBp.setValue(";").setAutoCompletion(false);
		separatorCmb = container.add(separatorCmbBp, "growx, w 0::, wrap");

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.mask")), "align r");
		final IComboBoxSelectionBluePrint<String> maskCmbBp = BPF.comboBoxSelection("*", "\"");
		maskCmbBp.setValue("*").setAutoCompletion(false);
		maskCmb = container.add(maskCmbBp, "growx, w 0::, wrap");

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.encoding")), "alignx r");
		final IComboBoxSelectionBluePrint<String> encodingCmbBp = BPF.comboBoxSelection("UTF-8", "Cp1250", "ISO8859_1");
		encodingCmbBp.setValue("UTF-8").setAutoCompletion(false);
		encodingCmb = container.add(encodingCmbBp, "growx, w 0::");

	}

	private IObjectStringConverter<Boolean> createExportAttributesConverter() {
		return new IObjectStringConverter<Boolean>() {

			@Override
			public String convertToString(final Boolean value) {
				if (value != null) {
					if (value.booleanValue()) {
						return "Alle";
					}
					else {
						return "Sichtbar";
					}
				}
				return "";
			}

			@Override
			public String getDescription(final Boolean value) {
				if (value != null) {
					if (value.booleanValue()) {
						return "Es werden alle Spalten exportiert";
					}
					else {
						return "Es werden nur die sichtbaren Spalten exportiert";
					}
				}
				return "";
			}
		};
	}

	private void createOpenFileButton(final IInputContentContainer container) {
		final IButton openFileButton = container.add(BPF.button().setIcon(IconsSmall.EDIT), "w 0::25,h 0::25, wrap");
		openFileButton.addActionListener(new IActionListener() {
			@Override
			public void actionPerformed() {
				openFileChooser();
			}
		});
	}

	private void openFileChooser() {
		final List<IFileChooserFilter> filterList = new LinkedList<IFileChooserFilter>();
		filterList.add(new FileChooserFilter(Messages.getString("CsvExportParameterContentProvider.csv_file"), "csv"));
		filterList.add(new FileChooserFilter(Messages.getString("CsvExportParameterContentProvider.text_file"), "txt"));
		final IFileChooserBluePrint fcBp = BPF.fileChooser(FileChooserType.OPEN_FILE).setFilterList(filterList);
		fileFC = Toolkit.getActiveWindow().createChildWindow(fcBp);
		fileFC.setSelectedFile(new File(filenameTf.getValue()));
		final DialogResult result = fileFC.open();
		if (result == DialogResult.OK) {
			for (final File file : fileFC.getSelectedFiles()) {
				filenameTf.setValue(file.getAbsolutePath());
			}
		}
	}

	class FileNameValidator implements IValidator<String> {

		@Override
		public IValidationResult validate(final String value) {

			if (EmptyCheck.isEmpty(value)) {
				return ValidationResult.infoError("Choose File");
			}

			if (new File(filenameTf.getValue()).exists()) {
				return ValidationResult.warning(Messages.getString("CsvExportParameterContentProvider.file_overwritten"));
			}

			return ValidationResult.ok();
		}

	}

}
