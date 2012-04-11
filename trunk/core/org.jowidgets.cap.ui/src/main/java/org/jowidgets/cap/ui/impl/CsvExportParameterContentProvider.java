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

import org.jowidgets.api.widgets.IComboBox;
import org.jowidgets.api.widgets.blueprint.IComboBoxSelectionBluePrint;
import org.jowidgets.api.widgets.blueprint.ITextLabelBluePrint;
import org.jowidgets.api.widgets.content.IInputContentContainer;
import org.jowidgets.api.widgets.content.IInputContentCreator;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.ICsvExportParameter;
import org.jowidgets.cap.ui.api.table.ICsvExportParameter.ExportType;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.widgets.blueprint.BPF;

final class CsvExportParameterContentProvider<BEAN_TYPE> implements IInputContentCreator<ICsvExportParameter> {

	private IComboBox<String> separatorCmb;
	private IComboBox<ICsvExportParameter.ExportType> exportTypeCmb;

	private ICsvExportParameter parameter;

	CsvExportParameterContentProvider(final IBeanTableModel<BEAN_TYPE> model) {}

	@Override
	public void setValue(final ICsvExportParameter parameter) {
		this.parameter = parameter;
		if (parameter != null) {
			exportTypeCmb.setValue(parameter.getExportType());
			separatorCmb.setValue(String.valueOf(parameter.getSeparator()));
		}
	}

	@Override
	public ICsvExportParameter getValue() {
		if (parameter != null) {
			return new CsvExportParameter(
				exportTypeCmb.getValue(),
				parameter.isExportHeader(),
				parameter.isExportInvisibleProperties(),
				separatorCmb.getValue().charAt(0),
				parameter.getMask(),
				parameter.getEncoding(),
				parameter.getFilename());
		}
		else {
			return parameter;
		}
	}

	@Override
	public void createContent(final IInputContentContainer container) {
		final ITextLabelBluePrint textLabelBp = BPF.textLabel().alignRight();

		container.setLayout(new MigLayoutDescriptor("[][grow, 0::]", "[][]"));

		//TODO SP i18n
		container.add(textLabelBp.setText("Export"), "alignx r");
		final IComboBoxSelectionBluePrint<ExportType> exportTypeCmbBp = BPF.comboBoxSelection(ICsvExportParameter.ExportType.values());
		exportTypeCmbBp.setAutoCompletion(false);
		exportTypeCmb = container.add(exportTypeCmbBp, "growx, w 0::, wrap");

		//TODO SP i18n
		container.add(textLabelBp.setText("Separator"), "alignx r");
		final IComboBoxSelectionBluePrint<String> separatorCmbBp = BPF.comboBoxSelection(";", ",", "|");
		separatorCmbBp.setValue(";").setAutoCompletion(false);
		separatorCmb = container.add(separatorCmbBp, "growx, w 0::");

	}
}
