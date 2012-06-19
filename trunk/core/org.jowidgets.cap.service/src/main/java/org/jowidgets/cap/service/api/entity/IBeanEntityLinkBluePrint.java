/*
 * Copyright (c) 2012, grossmann
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

package org.jowidgets.cap.service.api.entity;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.entity.IEntityLinkProperties;
import org.jowidgets.cap.common.api.service.ILinkCreatorService;
import org.jowidgets.cap.common.api.service.ILinkDeleterService;
import org.jowidgets.service.api.IServiceId;

public interface IBeanEntityLinkBluePrint {

	/**
	 * The entity id of the link (mandatory)
	 * 
	 * @param id The entity id of the link
	 * 
	 * @return This blue print
	 */
	IBeanEntityLinkBluePrint setLinkEntityId(Object id);

	/**
	 * The link bean type (mandatory, if no entity blue print was created for the entity id)
	 * 
	 * @param beanType The type of the link bean
	 * 
	 * @return This blue print
	 */
	IBeanEntityLinkBluePrint setLinkBeanType(Class<? extends IBean> beanType);

	/**
	 * The entity id of the linked entity (mandatory)
	 * 
	 * @param id The entity id of the linked entity
	 * 
	 * @return This blue print
	 */
	IBeanEntityLinkBluePrint setLinkedEntityId(Object id);

	/**
	 * The entity id of the linkable entitiy (may be null)
	 * 
	 * @param id The entity of the linkable entity
	 * 
	 * @return This blue print
	 */
	IBeanEntityLinkBluePrint setLinkableEntityId(Object id);

	/**
	 * Sets the symmetric property, the default a 'false'
	 * 
	 * The link is symmetric if
	 * 'a' is linked with 'b' implies 'b' is linked with 'a'
	 * 
	 * If the link will be set to symmetric, the default deleter service will also try to delete
	 * the reverse link, e.g. if 'a' should be unlinked with 'b', 'b' will also be unlinked from 'a'.
	 * 
	 * @param symmetric If true the links is symmetric, false otherwise
	 * 
	 * @return This blue print
	 */
	IBeanEntityLinkBluePrint setSymmetric(boolean symmetric);

	IBeanEntityLinkBluePrint setSourceProperties(IEntityLinkProperties properties);

	IBeanEntityLinkBluePrint setSourceProperties(String keyPropertyName, String foreignKeyPropertyname);

	IBeanEntityLinkBluePrint setSourceProperties(String foreignKeyPropertyName);

	IBeanEntityLinkBluePrint setDestinationProperties(IEntityLinkProperties properties);

	IBeanEntityLinkBluePrint setDestinationProperties(String keyPropertyName, String foreignKeyPropertyname);

	IBeanEntityLinkBluePrint setDestinationProperties(String foreignKeyPropertyName);

	IBeanEntityLinkBluePrint setLinkCreatorService(IServiceId<ILinkCreatorService> serviceId);

	IBeanEntityLinkBluePrint setLinkDeleterService(IServiceId<ILinkDeleterService> serviceId);

}
