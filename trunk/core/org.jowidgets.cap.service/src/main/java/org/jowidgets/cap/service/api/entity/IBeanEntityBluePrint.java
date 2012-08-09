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

import java.util.Collection;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptorBuilder;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.service.api.adapter.ISyncCreatorService;
import org.jowidgets.cap.service.api.adapter.ISyncDeleterService;
import org.jowidgets.cap.service.api.adapter.ISyncReaderService;
import org.jowidgets.cap.service.api.adapter.ISyncRefreshService;
import org.jowidgets.cap.service.api.adapter.ISyncUpdaterService;

public interface IBeanEntityBluePrint {

	/**
	 * Sets the entity id.
	 * 
	 * For each beanTypeId there can exists an arbitrary amount of entityId's.
	 * An entityId describes an beanTypeId more concrete.
	 * 
	 * Example1:
	 * 
	 * BeanType: User.class
	 * BeanTypeId: User.class
	 * EntityIds: USER, USERS_LINKED_WITH_ROLES, USERS_LINKED_WITH_USERS
	 * 
	 * Example2:
	 * 
	 * BeanType: IBeanPropertyMap.class
	 * BeanTypeId: USER
	 * EntityIds: USER, USERS_LINKED_WITH_ROLES, USERS_LINKED_WITH_USERS
	 * 
	 * 
	 * @param entityId The entity id (mandatory)
	 * 
	 * @return This bluePrint
	 */
	IBeanEntityBluePrint setEntityId(Object entityId);

	/**
	 * Sets the bean type.
	 * 
	 * The java type this bean is represented with. This can be a generic type, e.g. IBeanPropertyMap.class,
	 * or a concrete type e.g. Person.class.
	 * 
	 * 
	 * @param beanType The bean type (mandatory)
	 * 
	 * @return This blue print
	 */
	IBeanEntityBluePrint setBeanType(Class<? extends IBean> beanType);

	/**
	 * Sets the beanTypeId id.
	 * 
	 * If each type has its on java type to represent, the beanTypeId and the beanType may be the same.
	 * If the bean type is a generic type, the bean type id describes the bean (e.g. PERSON, ROLE, ...)
	 * 
	 * @param beanTypeId The beanTypeId id (mandatory)
	 * 
	 * @return This bluePrint
	 */
	IBeanEntityBluePrint setBeanTypeId(Object beanTypeId);

	/**
	 * Sets the dto descriptor.
	 * 
	 * @param descriptor The dto descriptor (mandatory)
	 * 
	 * @return This blue print
	 */
	IBeanEntityBluePrint setDtoDescriptor(IBeanDtoDescriptor descriptor);

	/**
	 * Convenience method to set the dto descriptor. This method will invoke
	 * the {@link #setDtoDescriptor(IBeanDtoDescriptor)} method.
	 * 
	 * @param builder The builder that builds the dto descriptor (mandadory if not invoked directly)
	 * 
	 * @return This builder
	 */
	IBeanEntityBluePrint setDtoDescriptor(IBeanDtoDescriptorBuilder builder);

	/**
	 * Sets the properties. If not invoked, the properties of the bean dto descriptor will be used.
	 * 
	 * @param properties The properties to set
	 * 
	 * @return This builder
	 */
	IBeanEntityBluePrint setProperties(Collection<String> properties);

	/**
	 * Sets the reader service. If not invoked, the default reader service will be used
	 * 
	 * @param readerService The reader service
	 * 
	 * @return This builder
	 */
	IBeanEntityBluePrint setReaderService(IReaderService<Void> readerService);

	/**
	 * Sets the reader service. If not invoked, the default reader service will be used
	 * 
	 * @param readerService The reader service
	 * 
	 * @return This builder
	 */
	IBeanEntityBluePrint setReaderService(ISyncReaderService<Void> readerService);

	/**
	 * Sets the creator service. If not invoked, the default creator service will be used
	 * 
	 * @param creatorService The creator service
	 * 
	 * @return This builder
	 */
	IBeanEntityBluePrint setCreatorService(ICreatorService creatorService);

	/**
	 * Sets the creator service. If not invoked, the default creator service will be used
	 * 
	 * @param creatorService The creator service
	 * 
	 * @return This builder
	 */
	IBeanEntityBluePrint setCreatorService(ISyncCreatorService creatorService);

	/**
	 * Sets the refresh service. If not invoked, the default refresh service will be used
	 * 
	 * @param refreshService The refresh service
	 * 
	 * @return This builder
	 */
	IBeanEntityBluePrint setRefreshService(IRefreshService refreshService);

	/**
	 * Sets the refresh service. If not invoked, the default refresh service will be used
	 * 
	 * @param refreshService The refresh service
	 * 
	 * @return This builder
	 */
	IBeanEntityBluePrint setRefreshService(ISyncRefreshService refreshService);

	/**
	 * Sets the updater service. If not invoked, the default updater service will be used
	 * 
	 * @param updaterService The updater service
	 * 
	 * @return This builder
	 */
	IBeanEntityBluePrint setUpdaterService(IUpdaterService updaterService);

	/**
	 * Sets the updater service. If not invoked, the default updater service will be used
	 * 
	 * @param updaterService The updater service
	 * 
	 * @return This builder
	 */
	IBeanEntityBluePrint setUpdaterService(ISyncUpdaterService updaterService);

	/**
	 * Sets the deleter service. If not invoked, the default deleter service will be used
	 * 
	 * @param deleterService The deleter service
	 * 
	 * @return This builder
	 */
	IBeanEntityBluePrint setDeleterService(IDeleterService deleterService);

	/**
	 * Sets the deleter service. If not invoked, the default deleter service will be used
	 * 
	 * @param deleterService The deleter service
	 * 
	 * @return This builder
	 */
	IBeanEntityBluePrint setDeleterService(ISyncDeleterService deleterService);

	/**
	 * Convenience method to set the updater service and creator service to null
	 * 
	 * @return This builder
	 */
	IBeanEntityBluePrint setReadonly();

	/**
	 * Adds a link to the entity. The link can be configured on the returned ILinkEntityBluePrint
	 * 
	 * @return The blueprint to configure the link
	 */
	IBeanEntityLinkBluePrint addLink();
}
