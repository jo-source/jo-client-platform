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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeGroup;
import org.jowidgets.cap.ui.api.form.IBeanFormGroup;
import org.jowidgets.cap.ui.api.form.IBeanFormGroupBuilder;
import org.jowidgets.cap.ui.api.form.IBeanFormLayout;
import org.jowidgets.cap.ui.api.form.IBeanFormLayoutBuilder;
import org.jowidgets.cap.ui.api.form.IBeanFormPropertyBuilder;
import org.jowidgets.util.Assert;
import org.jowidgets.util.NullCompatibleEquivalence;

final class BeanFormLayoutBuilderImpl implements IBeanFormLayoutBuilder {

	private final List<IBeanFormGroup> groups;
	private final Map<Integer, Integer> minSizes;
	private final Map<Integer, Integer> maxSizes;
	private final Map<Integer, Integer> rowHeights;

	private int columnCount;

	BeanFormLayoutBuilderImpl() {
		this.groups = new LinkedList<IBeanFormGroup>();
		this.minSizes = new HashMap<Integer, Integer>();
		this.maxSizes = new HashMap<Integer, Integer>();
		this.rowHeights = new HashMap<Integer, Integer>();
		this.columnCount = 1;
	}

	@Override
	public IBeanFormLayoutBuilder setColumnCount(final int columnCount) {
		if (columnCount < 1) {
			throw new IllegalArgumentException("Parameter 'columnCount' must be greater than 0, bust is '" + columnCount + "'");
		}
		this.columnCount = columnCount;
		return this;
	}

	@Override
	public IBeanFormLayoutBuilder setColumnMinSize(final int columnIndex, final int minSize) {
		minSizes.put(Integer.valueOf(columnIndex), Integer.valueOf(minSize));
		return this;
	}

	@Override
	public IBeanFormLayoutBuilder setColumnMaxSize(final int columnIndex, final int maxSize) {
		maxSizes.put(Integer.valueOf(columnIndex), Integer.valueOf(maxSize));
		return this;
	}

	@Override
	public IBeanFormLayoutBuilder setRowHeight(final int rowIndex, final int rowHeight) {
		rowHeights.put(Integer.valueOf(rowIndex), Integer.valueOf(rowHeight));
		return this;
	}

	@Override
	public IBeanFormLayoutBuilder addGroup(final IBeanFormGroup group) {
		Assert.paramNotNull(group, "group");
		groups.add(group);
		return this;
	}

	@Override
	public IBeanFormLayoutBuilder addGroup(final IBeanFormGroupBuilder groupBuilder) {
		Assert.paramNotNull(groupBuilder, "groupBuilder");
		groups.add(groupBuilder.build());
		return this;
	}

	@Override
	public IBeanFormLayoutBuilder addGroups(final Collection<? extends IAttribute<?>> attributes) {
		return addGroups(attributes, getPropertyBuilder());
	}

	@Override
	public IBeanFormLayoutBuilder addGroups(
		final Collection<? extends IAttribute<?>> attributes,
		final IBeanFormPropertyBuilder defaultBuilder) {

		Assert.paramNotNull(attributes, "attributes");
		Assert.paramNotNull(defaultBuilder, "defaultBuilder");

		String oldGroupId = null;
		IBeanFormGroupBuilder groupBuilder = null;
		boolean first = true;
		for (final IAttribute<?> attribute : attributes) {
			final IAttributeGroup group = attribute.getGroup();
			final String newGroupId = group != null ? group.getId() : null;
			if (first || !NullCompatibleEquivalence.equals(oldGroupId, newGroupId)) {
				first = false;
				oldGroupId = newGroupId;
				if (groupBuilder != null) {
					addGroup(groupBuilder);
				}
				final String newGroupLabel = group != null ? group.getLabel() : null;
				groupBuilder = CapUiToolkit.beanFormToolkit().groupBuilder().setLabel(newGroupLabel);
			}
			defaultBuilder.setPropertyName(attribute.getPropertyName());
			groupBuilder.addProperty(defaultBuilder);
		}
		if (groupBuilder != null) {
			addGroup(groupBuilder);
		}
		return this;
	}

	@Override
	public IBeanFormLayout build() {
		return new BeanFormLayoutImpl(columnCount, groups, minSizes, maxSizes, rowHeights);
	}

	private IBeanFormPropertyBuilder getPropertyBuilder() {
		return CapUiToolkit.beanFormToolkit().propertyBuilder();
	}

}
