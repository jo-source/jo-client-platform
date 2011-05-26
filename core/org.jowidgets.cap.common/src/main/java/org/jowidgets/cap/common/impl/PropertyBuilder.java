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

package org.jowidgets.cap.common.impl;

import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.util.Assert;

final class PropertyBuilder {

	private String name;
	private String labelDefault;
	private String labelLongDefault;
	private String descriptionDefault;
	private boolean visibleDefault;
	private boolean mandatoryDefault;
	private Class<?> valueType;
	private Class<?> elementValueType;
	private boolean readonly;
	private boolean sortable;
	private boolean filterable;

	PropertyBuilder() {
		this.visibleDefault = true;
		this.mandatoryDefault = false;
		this.readonly = false;
		this.sortable = true;
		this.filterable = true;
	}

	PropertyBuilder setName(final String name) {
		Assert.paramNotEmpty(name, "name");
		this.name = name;
		return this;
	}

	PropertyBuilder setLabel(final String labelDefault) {
		this.labelDefault = labelDefault;
		return this;
	}

	PropertyBuilder setLabelLong(final String labelLongDefault) {
		this.labelLongDefault = labelLongDefault;
		return this;
	}

	PropertyBuilder setDescription(final String descriptionDefault) {
		this.descriptionDefault = descriptionDefault;
		return this;
	}

	PropertyBuilder setVisible(final boolean visibleDefault) {
		this.visibleDefault = visibleDefault;
		return this;
	}

	PropertyBuilder setMandatory(final boolean mandatoryDefault) {
		this.mandatoryDefault = mandatoryDefault;
		return this;
	}

	PropertyBuilder setValueType(final Class<?> valueType) {
		this.valueType = valueType;
		return this;
	}

	PropertyBuilder setElementValueType(final Class<?> elementValueType) {
		this.elementValueType = elementValueType;
		return this;
	}

	PropertyBuilder setReadonly(final boolean readonly) {
		this.readonly = readonly;
		return this;
	}

	PropertyBuilder setSortable(final boolean sortable) {
		this.sortable = sortable;
		return this;
	}

	PropertyBuilder setFilterable(final boolean filterable) {
		this.filterable = filterable;
		return this;
	}

	private String getLabelDefault() {
		if (labelDefault == null) {
			return name;
		}
		else {
			return labelDefault;
		}
	}

	IProperty build() {
		return new PropertyImpl(
			name,
			getLabelDefault(),
			labelLongDefault,
			descriptionDefault,
			visibleDefault,
			mandatoryDefault,
			valueType,
			elementValueType,
			readonly,
			sortable,
			filterable);
	}

}
