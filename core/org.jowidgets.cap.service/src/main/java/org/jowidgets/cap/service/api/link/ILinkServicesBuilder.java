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

package org.jowidgets.cap.service.api.link;

import java.util.Collection;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.entity.IEntityLinkProperties;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.ILinkCreatorService;
import org.jowidgets.cap.common.api.service.ILinkDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;

public interface ILinkServicesBuilder<LINKED_BEAN_TYPE extends IBean> {

	ILinkServicesBuilder<LINKED_BEAN_TYPE> setLinkedBeanAccess(IBeanAccess<LINKED_BEAN_TYPE> beanAccess);

	ILinkServicesBuilder<LINKED_BEAN_TYPE> setLinkedDtoFactory(IBeanDtoFactory<LINKED_BEAN_TYPE> dtoFactory);

	ILinkServicesBuilder<LINKED_BEAN_TYPE> setLinkedDtoFactory(
		Class<? extends LINKED_BEAN_TYPE> beanType,
		Collection<String> propertyNames);

	ILinkServicesBuilder<LINKED_BEAN_TYPE> setSourceCreatorService(ICreatorService creatorService);

	ILinkServicesBuilder<LINKED_BEAN_TYPE> setSourceDeleterService(IDeleterService deleterService);

	ILinkServicesBuilder<LINKED_BEAN_TYPE> setAllLinksReaderService(IReaderService<Void> readerService);

	ILinkServicesBuilder<LINKED_BEAN_TYPE> setLinkCreatorService(ICreatorService creatorService);

	ILinkServicesBuilder<LINKED_BEAN_TYPE> setLinkDeleterService(IDeleterService deleterService);

	ILinkServicesBuilder<LINKED_BEAN_TYPE> setLinkedCreatorService(ICreatorService creatorService);

	ILinkServicesBuilder<LINKED_BEAN_TYPE> setLinkedDeleterService(IDeleterService creatorService);

	ILinkServicesBuilder<LINKED_BEAN_TYPE> setSourceProperties(IEntityLinkProperties properties);

	ILinkServicesBuilder<LINKED_BEAN_TYPE> setSourceProperties(String keyPropertyName, String foreignKeyPropertyname);

	ILinkServicesBuilder<LINKED_BEAN_TYPE> setSourceProperties(String foreignKeyPropertyName);

	ILinkServicesBuilder<LINKED_BEAN_TYPE> setDestinationProperties(IEntityLinkProperties properties);

	ILinkServicesBuilder<LINKED_BEAN_TYPE> setDestinationProperties(String keyPropertyName, String foreignKeyPropertyname);

	ILinkServicesBuilder<LINKED_BEAN_TYPE> setDestinationProperties(String foreignKeyPropertyName);

	ILinkCreatorService buildCreatorService();

	/**
	 * Tries to build the creator service. If this is not possible, null will be returned;
	 * 
	 * @return The creator service or null
	 */
	ILinkCreatorService tryBuildCreatorService();

	ILinkDeleterService buildDeleterService();

	/**
	 * Tries to build the deleter service. If this is not possible, null will be returned;
	 * 
	 * @return The deleter service or null
	 */
	ILinkDeleterService tryBuildDeleterService();

}
