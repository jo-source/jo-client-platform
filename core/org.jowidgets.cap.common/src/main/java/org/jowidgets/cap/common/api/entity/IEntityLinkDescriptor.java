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

package org.jowidgets.cap.common.api.entity;

import org.jowidgets.cap.common.api.service.ILinkCreatorService;
import org.jowidgets.cap.common.api.service.ILinkDeleterService;

public interface IEntityLinkDescriptor {

	/**
	 * Gets the id of the entity that holds the link
	 * 
	 * @return The entity id that hold the link, may be null
	 */
	Object getLinkEntityId();

	/**
	 * Gets the id of the entity that is linked (directly or indirectly) to the source
	 * 
	 * Reading objects of that entity gets all objects that are currently linked with the given source object
	 * (the parent parameter of the loader holds them)
	 * 
	 * Remark: If the object is linked directly (e.g. has no link table), the link entity and the linked entity may be
	 * the same type
	 * 
	 * @return The entity id that is linked, never null
	 */
	Object getLinkedEntityId();

	/**
	 * Gets the id of the entity that is linkable (indirectly) with the source. If the link is a direct link
	 * or if the link is readonly, null will be returned.
	 * 
	 * Reading objects of that type gets all objects that could be potentially linked with the given source object
	 * (e.g. all objects that are not currently linked)
	 * 
	 * @return The entity id that is linkable or null, if the link is directly or readonly
	 */
	Object getLinkableEntityId();

	/**
	 * Gets the link properties of the link source.
	 * 
	 * @return The link properties of the link source or null if link is readonly
	 */
	IEntityLinkProperties getSourceProperties();

	/**
	 * Gets the link properties of the link destination.
	 * 
	 * @return The link properties of the link destination or null for direct links
	 */
	IEntityLinkProperties getDestinationProperties();

	/**
	 * @return The creator service or null, if link is readonly
	 */
	ILinkCreatorService getLinkCreatorService();

	/**
	 * @return The deleter service or null if the link is readonly
	 */
	ILinkDeleterService getLinkDeleterService();
}
