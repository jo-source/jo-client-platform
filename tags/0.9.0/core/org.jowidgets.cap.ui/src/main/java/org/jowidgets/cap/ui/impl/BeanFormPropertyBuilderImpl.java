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

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.blueprint.IValidationResultLabelBluePrint;
import org.jowidgets.api.widgets.descriptor.setup.IValidationLabelSetup;
import org.jowidgets.cap.ui.api.form.IBeanFormProperty;
import org.jowidgets.cap.ui.api.form.IBeanFormPropertyBuilder;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.common.types.AlignmentVertical;
import org.jowidgets.util.Assert;

final class BeanFormPropertyBuilderImpl implements IBeanFormPropertyBuilder {

	private String propertyName;
	private boolean showLabel;
	private int rowSpan;
	private Integer rowCount;
	private int columnSpan;
	private Integer columnCount;

	private Integer height;

	private AlignmentHorizontal labelAlignmentHorizontal;
	private AlignmentHorizontal propertyAlignmentHorizontal;
	private AlignmentVertical labelAlignmentVertical;
	private AlignmentVertical propertyAlignmentVertical;

	private IValidationLabelSetup validationLabel;
	private Integer validationLabelMinSize;

	BeanFormPropertyBuilderImpl() {
		showLabel = true;
		rowSpan = 1;
		columnSpan = 1;
		labelAlignmentHorizontal = AlignmentHorizontal.RIGHT;
		labelAlignmentVertical = AlignmentVertical.CENTER;
		propertyAlignmentHorizontal = AlignmentHorizontal.LEFT;
		propertyAlignmentVertical = AlignmentVertical.CENTER;

		this.validationLabel = createDefaultValidationLabelSetup();
		this.validationLabelMinSize = Integer.valueOf(18);
	}

	@Override
	public IBeanFormPropertyBuilder setPropertyName(final String name) {
		Assert.paramNotEmpty(name, "name");
		this.propertyName = name;
		return this;
	}

	@Override
	public IBeanFormPropertyBuilder setShowLabel(final boolean showLabel) {
		this.showLabel = showLabel;
		return this;
	}

	@Override
	public IBeanFormPropertyBuilder setRowSpan(final int rowSpan) {
		this.rowSpan = rowSpan;
		return this;
	}

	@Override
	public IBeanFormPropertyBuilder setRowCount(final int rowCount) {
		this.rowCount = Integer.valueOf(rowCount);
		return this;
	}

	@Override
	public IBeanFormPropertyBuilder setColumnSpan(final int columnSpan) {
		this.columnSpan = columnSpan;
		return this;
	}

	@Override
	public IBeanFormPropertyBuilder setColumnCount(final int columnCount) {
		this.columnCount = Integer.valueOf(columnCount);
		return this;
	}

	@Override
	public IBeanFormPropertyBuilder setHeight(final int height) {
		this.height = height;
		return this;
	}

	@Override
	public IBeanFormPropertyBuilder setLabelAlignmentHorizontal(final AlignmentHorizontal alignment) {
		Assert.paramNotNull(alignment, "alignment");
		this.labelAlignmentHorizontal = alignment;
		return this;
	}

	@Override
	public IBeanFormPropertyBuilder setPropertyAlignmentHorizontal(final AlignmentHorizontal alignment) {
		Assert.paramNotNull(alignment, "alignment");
		this.propertyAlignmentHorizontal = alignment;
		return this;
	}

	@Override
	public IBeanFormPropertyBuilder setLabelAlignmentVertical(final AlignmentVertical alignment) {
		Assert.paramNotNull(alignment, "alignment");
		this.labelAlignmentVertical = alignment;
		return this;
	}

	@Override
	public IBeanFormPropertyBuilder setPropertyAlignmentVertical(final AlignmentVertical alignment) {
		Assert.paramNotNull(alignment, "alignment");
		this.propertyAlignmentVertical = alignment;
		return this;
	}

	@Override
	public IBeanFormPropertyBuilder setValidationLabel(final IValidationLabelSetup validationLabel) {
		this.validationLabel = validationLabel;
		return this;
	}

	@Override
	public IBeanFormPropertyBuilder setValidationLabel(final boolean validationLabel) {
		if (validationLabel) {
			this.validationLabel = createDefaultValidationLabelSetup();
		}
		else {
			this.validationLabel = null;
		}
		return this;
	}

	private IValidationLabelSetup createDefaultValidationLabelSetup() {
		final IValidationResultLabelBluePrint result = Toolkit.getBluePrintFactory().validationResultLabel();
		result.setShowValidationMessage(false);
		return result;
	}

	@Override
	public IBeanFormPropertyBuilder setValidationLabelMinSize(final int minSize) {
		this.validationLabelMinSize = Integer.valueOf(minSize);
		return this;
	}

	@Override
	public IBeanFormProperty build() {
		return new BeanFormPropertyImpl(
			propertyName,
			showLabel,
			rowSpan,
			rowCount != null ? rowCount : rowSpan,
			columnSpan,
			columnCount != null ? columnCount : columnSpan,
			height,
			labelAlignmentHorizontal,
			propertyAlignmentHorizontal,
			labelAlignmentVertical,
			propertyAlignmentVertical,
			validationLabel,
			validationLabelMinSize);
	}

}
