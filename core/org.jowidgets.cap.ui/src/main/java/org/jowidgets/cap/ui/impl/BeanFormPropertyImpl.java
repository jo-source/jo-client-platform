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

import org.jowidgets.api.widgets.descriptor.setup.IValidationLabelSetup;
import org.jowidgets.cap.ui.api.form.IBeanFormProperty;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.common.types.AlignmentVertical;
import org.jowidgets.util.Assert;

final class BeanFormPropertyImpl implements IBeanFormProperty {

	private final String propertyName;
	private final boolean showLabel;
	private final int rowSpan;
	private final int rowCount;
	private final int columnSpan;
	private final int columnCount;
	private final Integer height;

	private final AlignmentHorizontal labelAlignmentHorizontal;
	private final AlignmentHorizontal propertyAlignmentHorizontal;
	private final AlignmentVertical labelAlignmentVertical;
	private final AlignmentVertical propertyAlignmentVertical;

	private final IValidationLabelSetup validationLabel;
	private final Integer validationLabelMinSize;

	BeanFormPropertyImpl(
		final String propertyName,
		final boolean showLabel,
		final int rowSpan,
		final int rowCount,
		final int columnSpan,
		final int columnCount,
		final Integer height,
		final AlignmentHorizontal labelAlignmentHorizontal,
		final AlignmentHorizontal propertyAlignmentHorizontal,
		final AlignmentVertical labelAlignmentVertical,
		final AlignmentVertical propertyAlignmentVertical,
		final IValidationLabelSetup validationLabel,
		final Integer validationLabelMinSize) {
		Assert.paramNotEmpty(propertyName, "propertyName");
		this.propertyName = propertyName;
		this.showLabel = showLabel;
		this.rowSpan = rowSpan;
		this.rowCount = rowCount;
		this.columnSpan = columnSpan;
		this.columnCount = columnCount;
		this.height = height;
		this.labelAlignmentHorizontal = labelAlignmentHorizontal;
		this.propertyAlignmentHorizontal = propertyAlignmentHorizontal;
		this.labelAlignmentVertical = labelAlignmentVertical;
		this.propertyAlignmentVertical = propertyAlignmentVertical;
		this.validationLabel = validationLabel;
		this.validationLabelMinSize = validationLabelMinSize;
	}

	@Override
	public String getPropertyName() {
		return propertyName;
	}

	@Override
	public boolean showLabel() {
		return showLabel;
	}

	@Override
	public int getRowSpan() {
		return rowSpan;
	}

	@Override
	public int getRowCount() {
		return rowCount;
	}

	@Override
	public int getColumnSpan() {
		return columnSpan;
	}

	@Override
	public int getColumnCount() {
		return columnCount;
	}

	@Override
	public Integer getHeight() {
		return height;
	}

	@Override
	public AlignmentHorizontal getLabelAlignmentHorizontal() {
		return labelAlignmentHorizontal;
	}

	@Override
	public AlignmentHorizontal getPropertyAlignmentHorizontal() {
		return propertyAlignmentHorizontal;
	}

	@Override
	public AlignmentVertical getLabelAlignmentVertical() {
		return labelAlignmentVertical;
	}

	@Override
	public AlignmentVertical getPropertyAlignmentVertical() {
		return propertyAlignmentVertical;
	}

	@Override
	public IValidationLabelSetup getValidationLabel() {
		return validationLabel;
	}

	@Override
	public Integer getValidationLabelMinSize() {
		return validationLabelMinSize;
	}

}
