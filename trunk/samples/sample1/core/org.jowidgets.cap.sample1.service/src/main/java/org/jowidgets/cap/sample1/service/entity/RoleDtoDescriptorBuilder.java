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

package org.jowidgets.cap.sample1.service.entity;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.bean.IPropertyBuilder;
import org.jowidgets.cap.sample1.service.datastore.RoleInitializer;

public class RoleDtoDescriptorBuilder {

	private final List<IProperty> properties;

	public RoleDtoDescriptorBuilder() {
		this(RoleInitializer.ALL_PROPERTIES);
	}

	public RoleDtoDescriptorBuilder(final Collection<String> visibleProperties) {

		this.properties = new LinkedList<IProperty>();

		IPropertyBuilder propertyBuilder;
		propertyBuilder = builder();
		propertyBuilder.setName(IBean.ID_PROPERTY);
		propertyBuilder.setLabel(Messages.getString("RoleDtoDescriptorBuilder.id")); //$NON-NLS-1$
		propertyBuilder.setDescription(Messages.getString("RoleDtoDescriptorBuilder.id_description")); //$NON-NLS-1$
		propertyBuilder.setMandatory(true);
		propertyBuilder.setReadonly(true);
		propertyBuilder.setValueType(Long.class);
		propertyBuilder.setVisible(visibleProperties.contains(IBean.ID_PROPERTY));
		properties.add(propertyBuilder.build());

		propertyBuilder = builder();
		propertyBuilder.setName(RoleInitializer.NAME_PROPERTY);
		propertyBuilder.setLabel(Messages.getString("RoleDtoDescriptorBuilder.name")); //$NON-NLS-1$
		propertyBuilder.setDescription(Messages.getString("RoleDtoDescriptorBuilder.name_description")); //$NON-NLS-1$
		propertyBuilder.setMandatory(true);
		propertyBuilder.setVisible(visibleProperties.contains(RoleInitializer.NAME_PROPERTY));
		properties.add(propertyBuilder.build());

		propertyBuilder = builder();
		propertyBuilder.setName(RoleInitializer.DESCRIPTION_PROPERTY);
		propertyBuilder.setLabel(Messages.getString("RoleDtoDescriptorBuilder.description")); //$NON-NLS-1$
		propertyBuilder.setDescription(Messages.getString("RoleDtoDescriptorBuilder.description_description")); //$NON-NLS-1$
		propertyBuilder.setMandatory(false);
		propertyBuilder.setVisible(visibleProperties.contains(RoleInitializer.DESCRIPTION_PROPERTY));
		properties.add(propertyBuilder.build());

		propertyBuilder = builder();
		propertyBuilder.setName(IBean.VERSION_PROPERTY);
		propertyBuilder.setLabel(Messages.getString("RoleDtoDescriptorBuilder.version")); //$NON-NLS-1$
		propertyBuilder.setDescription(Messages.getString("RoleDtoDescriptorBuilder.version_description")); //$NON-NLS-1$
		propertyBuilder.setMandatory(true);
		propertyBuilder.setReadonly(true);
		propertyBuilder.setValueType(long.class);
		propertyBuilder.setVisible(visibleProperties.contains(IBean.VERSION_PROPERTY));
		properties.add(propertyBuilder.build());

	}

	IPropertyBuilder builder() {
		final IPropertyBuilder propertyBuilder = CapCommonToolkit.propertyBuilder();
		propertyBuilder.setValueType(String.class).setSortable(true).setFilterable(true);
		return propertyBuilder;
	}

	IBeanDtoDescriptor build() {
		return CapCommonToolkit.dtoDescriptor(
				properties,
				Messages.getString("RoleDtoDescriptorBuilder.labelSingular"),
				Messages.getString("RoleDtoDescriptorBuilder.labelPlural"));
	}
}
