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

import java.io.Serializable;

import org.jowidgets.cap.common.api.bean.Cardinality;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkProperties;
import org.jowidgets.cap.common.api.service.ILinkCreatorService;
import org.jowidgets.cap.common.api.service.ILinkDeleterService;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;

final class EntityLinkDescriptorImpl implements IEntityLinkDescriptor, Serializable {

	private static final long serialVersionUID = -788776636135499105L;

	private final Object linkEntityId;
	private final Object linkedEntityId;
	private final Object linkableEntityId;
	private final Cardinality linkedCardinality;
	private final IEntityLinkProperties sourceProperties;
	private final IEntityLinkProperties destinationProperties;
	private final IServiceId<ILinkCreatorService> creatorServiceId;
	private final IServiceId<ILinkDeleterService> deleterServiceId;

	EntityLinkDescriptorImpl(
		final Object linkTypeId,
		final Object linkedTypeId,
		final Object linkableTypeId,
		final Cardinality linkedCardinality,
		final IEntityLinkProperties sourceProperties,
		final IEntityLinkProperties destinationProperties,
		final IServiceId<ILinkCreatorService> creatorServiceId,
		final IServiceId<ILinkDeleterService> deleterServiceId) {

		Assert.paramNotNull(linkedTypeId, "linkedTypeId");

		this.linkEntityId = linkTypeId;
		this.linkedEntityId = linkedTypeId;
		this.linkableEntityId = linkableTypeId;
		this.linkedCardinality = linkedCardinality;
		this.sourceProperties = sourceProperties;
		this.destinationProperties = destinationProperties;
		this.creatorServiceId = creatorServiceId;
		this.deleterServiceId = deleterServiceId;
	}

	@Override
	public Object getLinkEntityId() {
		return linkEntityId;
	}

	@Override
	public Object getLinkedEntityId() {
		return linkedEntityId;
	}

	@Override
	public Object getLinkableEntityId() {
		return linkableEntityId;
	}

	@Override
	public Cardinality getLinkedCardinality() {
		return linkedCardinality;
	}

	@Override
	public IEntityLinkProperties getSourceProperties() {
		return sourceProperties;
	}

	@Override
	public IEntityLinkProperties getDestinationProperties() {
		return destinationProperties;
	}

	@Override
	public ILinkCreatorService getLinkCreatorService() {
		if (creatorServiceId != null) {
			return ServiceProvider.getService(creatorServiceId);
		}
		else {
			return null;
		}
	}

	@Override
	public ILinkDeleterService getLinkDeleterService() {
		if (deleterServiceId != null) {
			return ServiceProvider.getService(deleterServiceId);
		}
		else {
			return null;
		}
	}

	@Override
	public String toString() {
		return "EntityLinkDescriptorImpl [linkEntityId="
			+ linkEntityId
			+ ", linkedEntityId="
			+ linkedEntityId
			+ ", linkableEntityId="
			+ linkableEntityId
			+ ", sourceProperties="
			+ sourceProperties
			+ ", destinationProperties="
			+ destinationProperties
			+ ", creatorServiceId="
			+ creatorServiceId
			+ ", deleterServiceId="
			+ deleterServiceId
			+ "]";
	}

}
