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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanData;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.entity.IEntityLinkProperties;
import org.jowidgets.cap.common.api.exception.BeanException;
import org.jowidgets.cap.common.api.exception.DeletedBeanException;
import org.jowidgets.cap.common.api.exception.ServiceException;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.link.ILinkCreation;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.ILinkCreatorService;
import org.jowidgets.cap.common.tools.execution.SyncResultCallback;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class LinkCreatorServiceImpl<SOURCE_BEAN_TYPE extends IBean, LINKED_BEAN_TYPE extends IBean> implements ILinkCreatorService {

	private final IBeanAccess<SOURCE_BEAN_TYPE> sourceBeanAccess;
	private final IBeanDtoFactory<SOURCE_BEAN_TYPE> sourceDtoFactory;
	private final Class<? extends IBean> linkBeanType;
	private final IBeanAccess<LINKED_BEAN_TYPE> linkedBeanAccess;
	private final IBeanDtoFactory<LINKED_BEAN_TYPE> linkedDtoFactory;
	private final ICreatorService sourceCreatorService;
	private final ICreatorService linkCreatorService;
	private final ICreatorService linkableCreatorService;

	private final IEntityLinkProperties sourceProperties;
	private final IEntityLinkProperties destinationProperties;

	LinkCreatorServiceImpl(
		final IBeanAccess<SOURCE_BEAN_TYPE> sourceBeanAccess,
		final IBeanDtoFactory<SOURCE_BEAN_TYPE> sourceDtoFactory,
		final Class<? extends IBean> linkBeanType,
		final IBeanAccess<LINKED_BEAN_TYPE> linkedBeanAccess,
		final IBeanDtoFactory<LINKED_BEAN_TYPE> linkedDtoFactory,
		final ICreatorService sourceCreatorService,
		final ICreatorService linkCreatorService,
		final ICreatorService linkableCreatorService,
		final IEntityLinkProperties sourceProperties,
		final IEntityLinkProperties destinationProperties) {

		Assert.paramNotNull(sourceBeanAccess, "sourceBeanAccess");
		Assert.paramNotNull(sourceDtoFactory, "sourceDtoFactory");
		Assert.paramNotNull(linkBeanType, "linkBeanType");
		Assert.paramNotNull(linkedBeanAccess, "linkedBeanAccess");
		Assert.paramNotNull(linkedDtoFactory, "linkedDtoFactory");
		Assert.paramNotNull(sourceProperties, "sourceProperties");

		this.sourceBeanAccess = sourceBeanAccess;
		this.sourceDtoFactory = sourceDtoFactory;
		this.linkBeanType = linkBeanType;
		this.linkedBeanAccess = linkedBeanAccess;
		this.linkedDtoFactory = linkedDtoFactory;
		this.sourceCreatorService = sourceCreatorService;
		this.linkCreatorService = linkCreatorService;
		this.linkableCreatorService = linkableCreatorService;
		this.sourceProperties = sourceProperties;
		this.destinationProperties = destinationProperties;
	}

	@Override
	public void create(
		final IResultCallback<List<IBeanDto>> linkedBeansResult,
		final Collection<? extends ILinkCreation> links,
		final IExecutionCallback executionCallback) {
		try {
			linkedBeansResult.finished(createSyncImpl(links, executionCallback));
		}
		catch (final Exception exception) {
			linkedBeansResult.exception(exception);
		}
	}

	private List<IBeanDto> createSyncImpl(
		final Collection<? extends ILinkCreation> links,
		final IExecutionCallback executionCallback) {

		final Set<IBeanDto> result = new LinkedHashSet<IBeanDto>();

		for (final ILinkCreation link : links) {
			CapServiceToolkit.checkCanceled(executionCallback);
			result.addAll(createLinks(link, executionCallback));
		}

		return new LinkedList<IBeanDto>(result);
	}

	private List<IBeanDto> createLinks(final ILinkCreation link, final IExecutionCallback executionCallback) {
		final List<IBeanDto> result = new LinkedList<IBeanDto>();

		final List<IBeanDto> sourceBeans = getBeans(link.getSourceBeans(), sourceBeanAccess, sourceDtoFactory, executionCallback);
		sourceBeans.addAll(createBeans(link.getTransientSourceBeans(), sourceCreatorService, executionCallback));

		final List<IBeanDto> linkedBeans = getBeans(
				link.getLinkableBeans(),
				linkedBeanAccess,
				linkedDtoFactory,
				executionCallback);
		linkedBeans.addAll(createBeans(link.getTransientLinkableBeans(), linkableCreatorService, executionCallback));

		for (final IBeanDto sourceBean : sourceBeans) {
			if (EmptyCheck.isEmpty(linkedBeans) && destinationProperties == null) {//direct link
				result.add(createLink(sourceBean, null, link.getAdditionalLinkProperties(), executionCallback));
			}
			else {
				for (final IBeanDto linkedBean : linkedBeans) {
					result.add(createLink(sourceBean, linkedBean, link.getAdditionalLinkProperties(), executionCallback));
				}
			}
		}

		return result;
	}

	private IBeanDto createLink(
		final IBeanDto sourceBean,
		final IBeanDto linkedBean,
		final IBeanData additionalProperties,
		final IExecutionCallback executionCallback) {

		final IBeanData decoratedBeanData = new DecoratedLinkBeanData(additionalProperties, linkedBean, sourceBean);
		final IBeanDto linkBean = createBean(decoratedBeanData, linkCreatorService, executionCallback);

		if (linkBean == null) {
			throw new ServiceException("Can not create link for source: "
				+ sourceBean
				+ ", linked: "
				+ linkedBean
				+ ", additional properties: "
				+ additionalProperties
				+ ".");
		}

		if (linkBeanType.equals(linkedBeanAccess.getBeanType()) && linkedBean == null) {
			return linkBean;
		}
		else {
			return linkedBean;
		}
	}

	private <BEAN_TYPE extends IBean> List<IBeanDto> getBeans(
		final Collection<IBeanKey> beanKeys,
		final IBeanAccess<BEAN_TYPE> beanAccess,
		final IBeanDtoFactory<BEAN_TYPE> dtoFactory,
		final IExecutionCallback executionCallback) {

		final List<IBeanDto> result = new LinkedList<IBeanDto>();
		for (final IBeanKey beanKey : beanKeys) {
			CapServiceToolkit.checkCanceled(executionCallback);
			final List<IBeanKey> singletonList = Collections.singletonList(beanKey);
			final List<BEAN_TYPE> beans = beanAccess.getBeans(singletonList, executionCallback);
			if (!EmptyCheck.isEmpty(beans) && beans.size() == 1) {
				final BEAN_TYPE bean = beans.iterator().next();
				result.add(dtoFactory.createDto(bean));
			}
			else if (!EmptyCheck.isEmpty(beans) && beans.size() > 1) {
				throw new BeanException(beanKey.getId(), "More than one bean found for the key '" + beanKey + "'");
			}
			else {
				throw new DeletedBeanException(beanKey.getId());
			}

		}
		return result;
	}

	private List<IBeanDto> createBeans(
		final Collection<IBeanData> beanDatas,
		final ICreatorService creatorService,
		final IExecutionCallback executionCallback) {
		final List<IBeanDto> result = new LinkedList<IBeanDto>();
		for (final IBeanData beanData : beanDatas) {
			result.add(createBean(beanData, creatorService, executionCallback));
		}
		return result;
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
				CapServiceToolkit.checkCanceled(executionCallback);
				return result.iterator().next();
			}
			else {
				throw new ServiceException("Can not create bean from bean data");
			}
		}

		return null;
	}

	private final class DecoratedLinkBeanData implements IBeanData, Serializable {

		private static final long serialVersionUID = -651044571313906726L;

		private final IBeanData original;

		private final IBeanDto sourceBean;
		private final IBeanDto linkedBean;

		private DecoratedLinkBeanData(final IBeanData original, final IBeanDto createdLinkedBean, final IBeanDto createdSourceBean) {
			this.original = original;
			this.linkedBean = createdLinkedBean;
			this.sourceBean = createdSourceBean;
		}

		@Override
		public Object getValue(final String propertyName) {
			if (sourceProperties.getForeignKeyPropertyName().equals(propertyName)) {
				if (sourceBean != null) {
					return sourceBean.getValue(sourceProperties.getKeyPropertyName());
				}
			}
			if (destinationProperties != null && destinationProperties.getForeignKeyPropertyName().equals(propertyName)) {
				if (linkedBean != null) {
					return linkedBean.getValue(destinationProperties.getKeyPropertyName());
				}
			}
			return original.getValue(propertyName);
		}
	}

}
