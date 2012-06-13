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
	private final Map<Integer, Integer> minSizes;
	private final Map<Integer, Integer> maxSizes;
	private final Integer minWidth;
	private final Integer width;
	private final Integer maxWidth;
	private final Border contentBorder;

	BeanFormLayoutImpl(
		final int columnCount,
		final Integer minWidth,
		final Integer width,
		final Integer maxWidth,
		final List<IBeanFormGroup> groups,
		final Map<Integer, Integer> minSizes,
		final Map<Integer, Integer> maxSizes,
		final Border contentBorder) {

		this.columnCount = columnCount;
		this.groups = Collections.unmodifiableList(new LinkedList<IBeanFormGroup>(groups));
		this.minWidth = minWidth;
		this.width = width;
		this.maxWidth = maxWidth;
		this.minSizes = new HashMap<Integer, Integer>(minSizes);
		this.maxSizes = new HashMap<Integer, Integer>(maxSizes);
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
	public Integer getWidth() {
		return width;
	}

	@Override
	public Integer getMaxWidth() {
		return maxWidth;
	}

	@Override
	public Integer getControlMinWidth(final int column) {
		return minSizes.get(Integer.valueOf(column));
	}

	@Override
	public Integer getControlMaxWidth(final int column) {
		return maxSizes.get(Integer.valueOf(column));
	}

	@Override
	public List<IBeanFormGroup> getGroups() {
		return groups;
	}

	@Override
	public Border getContentBorder() {
		return contentBorder;
	}

}
