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

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptorBuilder;
import org.jowidgets.cap.common.api.entity.IEntityLinkProperties;
import org.jowidgets.cap.common.api.entity.IEntityLinkPropertiesBuilder;
import org.jowidgets.util.Assert;

final class EntityLinkDescriptorBuilderImpl implements IEntityLinkDescriptorBuilder {

	private Object linkTypeId;
	private Object linkedTypeId;
	private Object linkableTypeId;
	private IEntityLinkProperties sourceProperties;
	private IEntityLinkProperties destinationProperties;

	EntityLinkDescriptorBuilderImpl() {}

	@Override
	public IEntityLinkDescriptorBuilder setLinkTypeId(final Object id) {
		Assert.paramNotNull(id, "id");
		this.linkTypeId = id;
		return this;
	}

	@Override
	public IEntityLinkDescriptorBuilder setLinkedTypeId(final Object id) {
		Assert.paramNotNull(id, "id");
		this.linkedTypeId = id;
		return this;
	}

	@Override
	public IEntityLinkDescriptorBuilder setLinkableTypeId(final Object id) {
		this.linkableTypeId = id;
		return this;
	}

	@Override
	public IEntityLinkDescriptorBuilder setSourceProperties(final IEntityLinkProperties properties) {
		Assert.paramNotNull(properties, "properties");
		this.sourceProperties = properties;
		return this;
	}

	@Override
	public IEntityLinkDescriptorBuilder setSourceProperties(final String keyPropertyName, final String foreignKeyPropertyname) {
		return setSourceProperties(createProperties(keyPropertyName, foreignKeyPropertyname));
	}

	@Override
	public IEntityLinkDescriptorBuilder setDestinationProperties(final IEntityLinkProperties properties) {
		this.destinationProperties = properties;
		return this;
	}

	@Override
	public IEntityLinkDescriptorBuilder setDestinationProperties(final String keyPropertyName, final String foreignKeyPropertyname) {
		return setDestinationProperties(createProperties(keyPropertyName, foreignKeyPropertyname));
	}

	@Override
	public IEntityLinkDescriptor build() {
		return new EntityLinkDescriptorImpl(linkTypeId, linkedTypeId, linkableTypeId, sourceProperties, destinationProperties);
	}

	private static IEntityLinkProperties createProperties(final String keyPropertyName, final String foreignKeyPropertyname) {
		Assert.paramNotNull(keyPropertyName, "keyPropertyName");
		Assert.paramNotNull(foreignKeyPropertyname, "foreignKeyPropertyname");
		final IEntityLinkPropertiesBuilder builder = CapCommonToolkit.entityLinkPropertiesBuilder();
		builder.setKeyPropertyName(keyPropertyName).setForeignKeyPropertyName(foreignKeyPropertyname);
		return builder.build();
	}

}
