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

package org.jowidgets.cap.sample1.ui.attribute;

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.sample1.common.entity.IDynamicPropertiesBean;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeGroup;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;

public class DynamicPropertiesBeanAttributesFactory {

	private final IAttributeToolkit attributeToolkit;

	public DynamicPropertiesBeanAttributesFactory() {
		this.attributeToolkit = CapUiToolkit.getAttributeToolkit();
	}

	public List<IAttribute<String>> tableAttributes() {
		final List<IAttribute<String>> attributes = new LinkedList<IAttribute<String>>();

		final List<String> allProperties = IDynamicPropertiesBean.ALL_PROPERTIES;

		int groupIndex = 0;
		IAttributeGroup group = null;
		for (int columnIndex = 0; columnIndex < allProperties.size(); columnIndex++) {
			final IAttributeBuilder<String> builder = attributeToolkit.createAttributeBuilder(String.class);
			builder.setPropertyName(allProperties.get(columnIndex));
			builder.setLabel("Col " + columnIndex);
			builder.setLabelLong("Column " + columnIndex);
			builder.setDescription("Description of column " + columnIndex);

			if (columnIndex % 8 == 0) {
				final int currentGroupIndex = groupIndex;
				group = new IAttributeGroup() {

					@Override
					public String getId() {
						return "Group " + currentGroupIndex;
					}

					@Override
					public String getLabel() {
						return "Group " + currentGroupIndex;
					}

					@Override
					public String getDescription() {
						return "Description of group " + currentGroupIndex;
					}
				};
				groupIndex++;
			}

			builder.setGroup(group);

			attributes.add(builder.build());
		}

		return attributes;

	}
}
