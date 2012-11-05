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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.ui.api.form.IBeanFormGroup;
import org.jowidgets.cap.ui.api.form.IBeanFormLayout;
import org.jowidgets.common.types.Border;

final class BeanFormLayoutImpl implements IBeanFormLayout {

	private final int columnCount;
	private final List<IBeanFormGroup> groups;
	private final Integer controlMinWidthDefault;
	private final Integer controlPrefWidthDefault;
	private final Integer controlMaxWidthDefault;
	private final Map<Integer, Integer> controlMinWidths;
	private final Map<Integer, Integer> controlPrefWidths;
	private final Map<Integer, Integer> controlMaxWidths;
	private final Integer minWidth;
	private final Integer width;
	private final Integer maxWidth;
	private final int validationLabelHeight;
	private final Border contentBorder;

	BeanFormLayoutImpl(
		final int columnCount,
		final Integer minWidth,
		final Integer width,
		final Integer maxWidth,
		final List<IBeanFormGroup> groups,
		final Integer controlMinWidthDefault,
		final Integer controlPrefWidthDefault,
		final Integer controlMaxWidthDefault,
		final Map<Integer, Integer> controlMinWidths,
		final Map<Integer, Integer> controlPrefWidths,
		final Map<Integer, Integer> controlMaxWidth,
		final int validationLabelHeight,
		final Border contentBorder) {

		this.columnCount = columnCount;
		this.groups = Collections.unmodifiableList(new LinkedList<IBeanFormGroup>(groups));
		this.minWidth = minWidth;
		this.width = width;
		this.maxWidth = maxWidth;
		this.controlMinWidthDefault = controlMinWidthDefault;
		this.controlPrefWidthDefault = controlPrefWidthDefault;
		this.controlMaxWidthDefault = controlMaxWidthDefault;
		this.controlMinWidths = new HashMap<Integer, Integer>(controlMinWidths);
		this.controlPrefWidths = new HashMap<Integer, Integer>(controlPrefWidths);
		this.controlMaxWidths = new HashMap<Integer, Integer>(controlMaxWidth);
		this.validationLabelHeight = validationLabelHeight;
		this.contentBorder = contentBorder;
	}

	@Override
	public int getColumnCount() {
		return columnCount;
	}

	@Override
	public Integer getMinWidth() {
		return minWidth;
	}

	@Override
	public Integer getPrefWidth() {
		return width;
	}

	@Override
	public Integer getMaxWidth() {
		return maxWidth;
	}

	@Override
	public Integer getControlMinWidth(final int column) {
		final Integer result = controlMinWidths.get(Integer.valueOf(column));
		if (result == null) {
			return controlMinWidthDefault;
		}
		else {
			return result;
		}
	}

	@Override
	public Integer getControlPrefWidth(final int column) {
		final Integer result = controlPrefWidths.get(Integer.valueOf(column));
		if (result == null) {
			return controlPrefWidthDefault;
		}
		else {
			return result;
		}
	}

	@Override
	public Integer getControlMaxWidth(final int column) {
		final Integer result = controlMaxWidths.get(Integer.valueOf(column));
		if (result == null) {
			return controlMaxWidthDefault;
		}
		else {
			return result;
		}
	}

	@Override
	public int getValidationLabelHeight() {
		return validationLabelHeight;
	}

	@Override
	public Border getContentBorder() {
		return contentBorder;
	}

	@Override
	public List<IBeanFormGroup> getGroups() {
		return groups;
	}

}
