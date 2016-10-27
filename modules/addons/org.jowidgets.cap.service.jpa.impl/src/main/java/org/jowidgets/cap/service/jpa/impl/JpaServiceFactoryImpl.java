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

package org.jowidgets.cap.service.jpa.impl;

import java.util.Collection;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanPropertyAccessor;
import org.jowidgets.cap.service.api.bean.IBeanReader;
import org.jowidgets.cap.service.api.creator.ICreatorServiceBuilder;
import org.jowidgets.cap.service.api.deleter.IDeleterServiceBuilder;
import org.jowidgets.cap.service.jpa.api.IJpaServiceFactory;
import org.jowidgets.cap.service.jpa.api.query.ICriteriaQueryCreatorBuilder;
import org.jowidgets.cap.service.jpa.api.query.IQueryCreator;
import org.jowidgets.cap.service.jpa.api.query.JpaQueryToolkit;
import org.jowidgets.cap.service.tools.factory.AbstractBeanServiceFactory;
import org.jowidgets.util.Assert;

public class JpaServiceFactoryImpl extends AbstractBeanServiceFactory implements IJpaServiceFactory {

	@Override
	public <BEAN_TYPE extends IBean> IBeanAccess<BEAN_TYPE> beanAccess(
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId) {
		return new JpaBeanAccessImpl<BEAN_TYPE>(beanType, beanTypeId);
	}

	@Override
	public <BEAN_TYPE extends IBean, PARAM_TYPE> IBeanReader<BEAN_TYPE, PARAM_TYPE> beanReader(
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId,
		final IBeanPropertyAccessor<BEAN_TYPE> propertyAccessor) {
		final ICriteriaQueryCreatorBuilder<PARAM_TYPE> queryCreatorBuilder;
		queryCreatorBuilder = JpaQueryToolkit.criteriaQueryCreatorBuilder(beanType);
		return beanReader(queryCreatorBuilder.build());
	}

	@Override
	public <BEAN_TYPE extends IBean, PARAM_TYPE> IBeanReader<BEAN_TYPE, PARAM_TYPE> beanReader(
		final IQueryCreator<PARAM_TYPE> queryCreator) {
		return new JpaBeanReader<BEAN_TYPE, PARAM_TYPE>(queryCreator);
	}

	@Override
	public <BEAN_TYPE extends IBean> ICreatorServiceBuilder<BEAN_TYPE> creatorServiceBuilder(
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId) {
		Assert.paramNotNull(beanType, "beanType");
		return new JpaCreatorServiceBuilderImpl<BEAN_TYPE>(beanType);
	}

	@Override
	public <BEAN_TYPE extends IBean, PARAM_TYPE> IReaderService<PARAM_TYPE> readerService(
		final IQueryCreator<PARAM_TYPE> queryCreator,
		final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory) {
		final IBeanReader<BEAN_TYPE, PARAM_TYPE> reader = beanReader(queryCreator);
		return readerService(reader, beanDtoFactory);
	}

	@Override
	public <BEAN_TYPE extends IBean> IDeleterServiceBuilder<BEAN_TYPE> deleterServiceBuilder(
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId) {
		return new JpaDeleterServiceBuilderImpl<BEAN_TYPE>(beanAccess(beanType));
	}

	@Override
	public <PARAM_TYPE> IReaderService<PARAM_TYPE> readerService(
		final Class<? extends IBean> beanType,
		final IQueryCreator<PARAM_TYPE> queryCreator,
		final Collection<String> propertyNames) {
		return readerService(queryCreator, CapServiceToolkit.dtoFactory(beanType, propertyNames));
	}

}
