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

package org.jowidgets.cap.common.api.service;

import java.util.Collection;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.tools.ServiceId;

/**
 * An entity service provides info's for a bunch of entities.
 * 
 * Entities can be different depending on the relation with other entities,
 * so for each relation of an entity to another entity, a own entity id may exist.
 * 
 * E.g. an person entity may have different properties than a subordinate person
 * 
 * @author grossmann
 */
public interface IEntityService {

	/**
	 * The default service id
	 */
	IServiceId<IEntityService> ID = new ServiceId<IEntityService>(IEntityService.class.getName(), IEntityService.class);

	/**
	 * Gets all available entity info's.
	 * 
	 * @return All available entity info's, never null but may be empty
	 */
	Collection<IEntityInfo> getEntityInfos();

	/**
	 * Gets a entity info for a given entity id
	 * 
	 * @param entityId The id to get the info for
	 * 
	 * @return The entity info or null if no info is available
	 */
	IEntityInfo getEntityInfo(Object entityId);

	/**
	 * Gets the bean dto descriptor for a given entity id
	 * 
	 * @param entityId The id to get the descriptor for
	 * 
	 * @return The descriptor or null if no info is available
	 */
	IBeanDtoDescriptor getDescriptor(Object entityId);

	/**
	 * Gets the bean services for a given entity id
	 * 
	 * @param entityId The id to get the bean services for
	 * 
	 * @return The bean services or null if no info is available
	 */
	IBeanServicesProvider getBeanServices(Object entityId);

	/**
	 * Gets the entity links for a given entity id
	 * 
	 * @param entityId The id to get the entity links for
	 * 
	 * @return The entity links or null if no info is available
	 */
	List<IEntityLinkDescriptor> getEntityLinks(Object entityId);

	/**
	 * Clears all cached data if caching is used
	 */
	void clearCache();

}
