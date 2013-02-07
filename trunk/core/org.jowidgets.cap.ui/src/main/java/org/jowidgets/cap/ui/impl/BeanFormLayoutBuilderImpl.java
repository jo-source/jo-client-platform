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
	private final Map<Integer, Integer> controlMinWidths;
	private final Map<Integer, Integer> controlPrefWidths;
	private final Map<Integer, Integer> controlMaxWidths;

	private Integer controlMinWidthDefault;
	private Integer controlPrefWidthDefault;
	private Integer controlMaxWidthDefault;

	private Integer minWidth;
	private Integer width;
	private Integer maxWidth;

	private int validationLabelHeight;

	private int columnCount;

	BeanFormLayoutBuilderImpl() {
		this.groups = new LinkedList<IBeanFormGroup>();
		this.controlMinWidthDefault = Integer.valueOf(60);
		this.validationLabelHeight = 20;
		this.controlMinWidths = new HashMap<Integer, Integer>();
		this.controlPrefWidths = new HashMap<Integer, Integer>();
		this.controlMaxWidths = new HashMap<Integer, Integer>();
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
	public IBeanFormLayoutBuilder setMinWidth(final int minWidth) {
		this.minWidth = Integer.valueOf(minWidth);
		return this;
	}

	@Override
	public IBeanFormLayoutBuilder setPrefWidth(final int width) {
		this.width = Integer.valueOf(width);
		return this;
	}

	@Override
	public IBeanFormLayoutBuilder setMaxWidth(final int maxWidth) {
		this.maxWidth = Integer.valueOf(maxWidth);
		return this;
	}

	@Override
	public IBeanFormLayoutBuilder setControlMinWidthDefault(final int width) {
		this.controlMinWidthDefault = Integer.valueOf(width);
		return this;
	}

	@Override
	public IBeanFormLayoutBuilder setControlPrefWidthDefault(final int width) {
		this.controlPrefWidthDefault = Integer.valueOf(width);
		return this;
	}

	@Override
	public IBeanFormLayoutBuilder setControlMaxWidthDefault(final int width) {
		this.controlMaxWidthDefault = Integer.valueOf(width);
		return this;
	}

	@Override
	public IBeanFormLayoutBuilder setControlMinWidth(final int columnIndex, final int width) {
		controlMinWidths.put(Integer.valueOf(columnIndex), Integer.valueOf(width));
		return this;
	}

	@Override
	public IBeanFormLayoutBuilder setControlPrefWidth(final int columnIndex, final int width) {
		controlPrefWidths.put(Integer.valueOf(columnIndex), Integer.valueOf(width));
		return this;
	}

	@Override
	public IBeanFormLayoutBuilder setControlMaxWidth(final int columnIndex, final int width) {
		controlMaxWidths.put(Integer.valueOf(columnIndex), Integer.valueOf(width));
		return this;
	}

	@Override
	public IBeanFormLayoutBuilder setValidationLabelHeight(final int height) {
		this.validationLabelHeight = height;
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
			if (attribute.isVisible()) {
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
		}
		if (groupBuilder != null) {
			addGroup(groupBuilder);
		}
		return this;
	}

	@Override
	public IBeanFormLayout build() {
		return new BeanFormLayoutImpl(
			columnCount,
			minWidth,
			width,
			maxWidth,
			groups,
			controlMinWidthDefault,
			controlPrefWidthDefault,
			controlMaxWidthDefault,
			controlMinWidths,
			controlPrefWidths,
			controlMaxWidths,
			validationLabelHeight);
	}

	private IBeanFormPropertyBuilder getPropertyBuilder() {
		return CapUiToolkit.beanFormToolkit().propertyBuilder();
	}

}
