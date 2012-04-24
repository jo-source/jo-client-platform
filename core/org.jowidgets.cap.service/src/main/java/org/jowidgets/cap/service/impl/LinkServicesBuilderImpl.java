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

package org.jowidgets.cap.service.impl;

import java.util.Collection;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.entity.EntityLinkProperties;
import org.jowidgets.cap.common.api.entity.IEntityLinkProperties;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.ILinkCreatorService;
import org.jowidgets.cap.common.api.service.ILinkDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.link.ILinkServicesBuilder;
import org.jowidgets.util.Assert;

final class LinkServicesBuilderImpl<LINKED_BEAN_TYPE extends IBean> implements ILinkServicesBuilder<LINKED_BEAN_TYPE> {

	private IBeanAccess<LINKED_BEAN_TYPE> linkedBeanAccess;
	private IBeanDtoFactory<LINKED_BEAN_TYPE> linkedDtoFactory;

	private Class<? extends IBean> linkBeanType;

	private ICreatorService sourceCreatorService;
	private ICreatorService linkCreatorService;
	private ICreatorService linkableCreatorService;

	private IReaderService<Void> linkReaderService;
	private IDeleterService sourceDeleterService;
	private IDeleterService linkDeleterService;
	private IDeleterService linkableDeleterService;

	private IEntityLinkProperties sourceProperties;
	private IEntityLinkProperties destinationProperties;

	@Override
	public ILinkServicesBuilder<LINKED_BEAN_TYPE> setLinkBeanType(final Class<? extends IBean> beanType) {
		Assert.paramNotNull(beanType, "beanType");
		this.linkBeanType = beanType;
		return this;
	}

	@Override
	public ILinkServicesBuilder<LINKED_BEAN_TYPE> setLinkedBeanAccess(final IBeanAccess<LINKED_BEAN_TYPE> beanAccess) {
		Assert.paramNotNull(beanAccess, "beanAccess");
		this.linkedBeanAccess = beanAccess;
		return this;
	}

	@Override
	public ILinkServicesBuilder<LINKED_BEAN_TYPE> setLinkedDtoFactory(final IBeanDtoFactory<LINKED_BEAN_TYPE> dtoFactory) {
		Assert.paramNotNull(dtoFactory, "dtoFactory");
		this.linkedDtoFactory = dtoFactory;
		return this;
	}

	@Override
	public ILinkServicesBuilder<LINKED_BEAN_TYPE> setLinkedDtoFactory(
		final Class<? extends LINKED_BEAN_TYPE> beanType,
		final Collection<String> propertyNames) {
		CapServiceToolkit.dtoFactory(beanType, propertyNames);
		return setLinkedDtoFactory(CapServiceToolkit.dtoFactory(beanType, propertyNames));
	}

	@Override
	public ILinkServicesBuilder<LINKED_BEAN_TYPE> setSourceCreatorService(final ICreatorService creatorService) {
		this.sourceCreatorService = creatorService;
		return this;
	}

	@Override
	public ILinkServicesBuilder<LINKED_BEAN_TYPE> setSourceDeleterService(final IDeleterService deleterService) {
		this.sourceDeleterService = deleterService;
		return this;
	}

	@Override
	public ILinkServicesBuilder<LINKED_BEAN_TYPE> setAllLinksReaderService(final IReaderService<Void> readerService) {
		Assert.paramNotNull(readerService, "readerService");
		this.linkReaderService = readerService;
		return this;
	}

	@Override
	public ILinkServicesBuilder<LINKED_BEAN_TYPE> setLinkCreatorService(final ICreatorService creatorService) {
		Assert.paramNotNull(creatorService, "creatorService");
		this.linkCreatorService = creatorService;
		return this;
	}

	@Override
	public ILinkServicesBuilder<LINKED_BEAN_TYPE> setLinkDeleterService(final IDeleterService deleterService) {
		Assert.paramNotNull(deleterService, "deleterService");
		this.linkDeleterService = deleterService;
		return this;
	}

	@Override
	public ILinkServicesBuilder<LINKED_BEAN_TYPE> setLinkableCreatorService(final ICreatorService creatorService) {
		this.linkableCreatorService = creatorService;
		return this;
	}

	@Override
	public ILinkServicesBuilder<LINKED_BEAN_TYPE> setLinkableDeleterService(final IDeleterService deleterService) {
		this.linkableDeleterService = deleterService;
		return this;
	}

	@Override
	public ILinkServicesBuilder<LINKED_BEAN_TYPE> setSourceProperties(final IEntityLinkProperties properties) {
		Assert.paramNotNull(properties, "properties");
		this.sourceProperties = properties;
		return this;
	}

	@Override
	public ILinkServicesBuilder<LINKED_BEAN_TYPE> setSourceProperties(
		final String keyPropertyName,
		final String foreignKeyPropertyname) {
		return setSourceProperties(EntityLinkProperties.create(keyPropertyName, foreignKeyPropertyname));
	}

	@Override
	public ILinkServicesBuilder<LINKED_BEAN_TYPE> setSourceProperties(final String foreignKeyPropertyName) {
		return setSourceProperties(EntityLinkProperties.create(foreignKeyPropertyName));
	}

	@Override
	public ILinkServicesBuilder<LINKED_BEAN_TYPE> setDestinationProperties(final IEntityLinkProperties properties) {
		Assert.paramNotNull(properties, "properties");
		this.destinationProperties = properties;
		return this;
	}

	@Override
	public ILinkServicesBuilder<LINKED_BEAN_TYPE> setDestinationProperties(
		final String keyPropertyName,
		final String foreignKeyPropertyname) {
		return setDestinationProperties(EntityLinkProperties.create(keyPropertyName, foreignKeyPropertyname));
	}

	@Override
	public ILinkServicesBuilder<LINKED_BEAN_TYPE> setDestinationProperties(final String foreignKeyPropertyName) {
		return setDestinationProperties(EntityLinkProperties.create(foreignKeyPropertyName));
	}

	@Override
	public ILinkCreatorService buildCreatorService() {
		return new LinkCreatorServiceImpl<LINKED_BEAN_TYPE>(
			linkBeanType,
			linkedBeanAccess,
			linkedDtoFactory,
			sourceCreatorService,
			linkCreatorService,
			linkableCreatorService,
			sourceProperties,
			destinationProperties);
	}

	@Override
	public ILinkDeleterService buildDeleterService() {
		return new LinkDeleterServiceImpl(
			linkReaderService,
			sourceDeleterService,
			linkDeleterService,
			linkableDeleterService,
			sourceProperties,
			destinationProperties);
	}

	@Override
	public ILinkCreatorService tryBuildCreatorService() {
		try {
			return buildCreatorService();
		}
		catch (final Exception e) {
			return null;
		}
	}

	@Override
	public ILinkDeleterService tryBuildDeleterService() {
		try {
			return buildDeleterService();
		}
		catch (final Exception e) {
			return null;
		}
	}

}
