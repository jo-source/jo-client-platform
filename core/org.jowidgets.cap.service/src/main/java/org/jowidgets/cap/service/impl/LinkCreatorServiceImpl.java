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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanData;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.entity.IEntityLinkProperties;
import org.jowidgets.cap.common.api.exception.DeletedBeanException;
import org.jowidgets.cap.common.api.exception.ServiceException;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.link.ILinkData;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.ILinkCreatorService;
import org.jowidgets.cap.common.tools.bean.BeanKey;
import org.jowidgets.cap.common.tools.execution.SyncResultCallback;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.util.Assert;

final class LinkCreatorServiceImpl<LINKED_BEAN_TYPE extends IBean> implements ILinkCreatorService {

	private final IBeanAccess<LINKED_BEAN_TYPE> linkedBeanAccess;
	private final IBeanDtoFactory<LINKED_BEAN_TYPE> beanDtoFactory;
	private final ICreatorService sourceCreatorService;
	private final ICreatorService linkCreatorService;
	private final ICreatorService linkedCreatorService;

	private final IEntityLinkProperties sourceProperties;
	private final IEntityLinkProperties destinationProperties;

	LinkCreatorServiceImpl(
		final IBeanAccess<LINKED_BEAN_TYPE> linkedBeanAccess,
		final IBeanDtoFactory<LINKED_BEAN_TYPE> beanDtoFactory,
		final ICreatorService sourceCreatorService,
		final ICreatorService linkCreatorService,
		final ICreatorService linkedCreatorService,
		final IEntityLinkProperties sourceProperties,
		final IEntityLinkProperties destinationProperties) {

		Assert.paramNotNull(linkedBeanAccess, "beanAcces");
		Assert.paramNotNull(beanDtoFactory, "beanDtoFactory");
		Assert.paramNotNull(sourceProperties, "sourceProperties");
		Assert.paramNotNull(destinationProperties, "destinationProperties");

		this.linkedBeanAccess = linkedBeanAccess;
		this.beanDtoFactory = beanDtoFactory;
		this.sourceCreatorService = sourceCreatorService;
		this.linkCreatorService = linkCreatorService;
		this.linkedCreatorService = linkedCreatorService;
		this.sourceProperties = sourceProperties;
		this.destinationProperties = destinationProperties;
	}

	@Override
	public void create(
		final IResultCallback<List<IBeanDto>> linkedBeansResult,
		final Collection<? extends ILinkData> links,
		final IExecutionCallback executionCallback) {
		try {
			final List<IBeanDto> linkedBeans = createSyncImpl(links, executionCallback);
			linkedBeansResult.finished(linkedBeans);
		}
		catch (final Exception exception) {
			linkedBeansResult.exception(exception);
		}
	}

	private List<IBeanDto> createSyncImpl(final Collection<? extends ILinkData> links, final IExecutionCallback executionCallback) {

		final List<IBeanDto> result = new LinkedList<IBeanDto>();

		for (final ILinkData link : links) {
			CapServiceToolkit.checkCanceled(executionCallback);
			final IBeanDto createdLinkedBean = createLink(link, executionCallback);
			if (createdLinkedBean != null) {
				result.add(createdLinkedBean);
			}
			else {
				throw new ServiceException("Could not create link for '" + link + "'");
			}
		}

		return result;
	}

	private IBeanDto createLink(final ILinkData link, final IExecutionCallback executionCallback) {

		final IBeanDto createdSourceBean = createBean(link.getSourceData(), sourceCreatorService, executionCallback);
		final IBeanDto createdLinkedBean = createBean(link.getLinkedData(), linkedCreatorService, executionCallback);

		final IBeanDto createdLinkBean;
		final IBeanData linkData = link.getLinkData();
		if (linkData != null) {
			final IBeanData decoratedBeanData = new DecoratedBeanData(linkData, createdLinkedBean, createdSourceBean);
			createdLinkBean = createBean(decoratedBeanData, linkCreatorService, executionCallback);
		}
		else {
			createdLinkBean = null;
		}

		if (createdLinkedBean != null) {
			return createdLinkedBean;
		}
		else if (createdLinkBean != null) {
			return readLinkedBean(createdLinkBean, executionCallback);
		}
		return null;
	}

	private IBeanDto readLinkedBean(final IBeanDto createdLinkBean, final IExecutionCallback executionCallback) {
		CapServiceToolkit.checkCanceled(executionCallback);
		final Object linkedId = createdLinkBean.getValue(destinationProperties.getForeignKeyPropertyName());
		final IBeanKey beanKey = new BeanKey(linkedId, 0);
		final List<LINKED_BEAN_TYPE> beans = linkedBeanAccess.getBeans(Collections.singleton(beanKey), executionCallback);
		if (beans.size() > 0) {
			return beanDtoFactory.createDto(beans.iterator().next());
		}
		else {
			throw new DeletedBeanException(linkedId);
		}
	}

	private IBeanDto createBean(
		final IBeanData beanData,
		final ICreatorService creatorService,
		final IExecutionCallback executionCallback) {

		if (creatorService != null && beanData != null) {
			final SyncResultCallback<List<IBeanDto>> resultCallback = new SyncResultCallback<List<IBeanDto>>();
			creatorService.create(resultCallback, Collections.singleton(beanData), executionCallback);
			final List<IBeanDto> result = resultCallback.getResultSynchronious();
			if (result.size() == 1) {
				return result.iterator().next();
			}
			else {
				throw new ServiceException("Can not create bean from bean data");
			}
		}

		return null;
	}

	private final class DecoratedBeanData implements IBeanData, Serializable {

		private static final long serialVersionUID = -651044571313906726L;

		private final IBeanData original;

		private final Object sourceId;
		private final Object linkedId;
		private final IBeanDto createdLinkedBean;
		private final IBeanDto createdSourceBean;

		private DecoratedBeanData(final IBeanData original, final IBeanDto createdLinkedBean, final IBeanDto createdSourceBean) {
			this.original = original;
			this.sourceId = original.getValue(sourceProperties.getForeignKeyPropertyName());
			this.linkedId = original.getValue(destinationProperties.getForeignKeyPropertyName());
			this.createdLinkedBean = createdLinkedBean;
			this.createdSourceBean = createdSourceBean;
		}

		@Override
		public Object getValue(final String propertyName) {
			if (sourceProperties.getForeignKeyPropertyName().equals(propertyName)) {
				if (sourceId == null && createdSourceBean != null) {
					return createdSourceBean.getValue(sourceProperties.getKeyPropertyName());
				}
			}
			if (destinationProperties.getForeignKeyPropertyName().equals(propertyName)) {
				if (linkedId == null && createdLinkedBean != null) {
					return createdLinkedBean.getValue(destinationProperties.getKeyPropertyName());
				}
			}
			return original.getValue(propertyName);
		}
	}

}
