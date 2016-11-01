/*
 * Copyright (c) 2016, grossmann
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

import org.jowidgets.cap.common.api.ordered.IOrderedBean;
import org.jowidgets.cap.service.api.bean.IBeanIdentityResolver;
import org.jowidgets.cap.service.api.bean.IBeanReader;
import org.jowidgets.cap.service.api.crud.ICrudServiceInterceptor;
import org.jowidgets.cap.service.api.ordered.IOrderedBeanCrudServiceInterceptorBuilder;
import org.jowidgets.cap.service.api.ordered.IOrderedBeanGroupMapper;
import org.jowidgets.util.Assert;

final class OrderedBeanCrudInterceptorBuilderImpl<BEAN_TYPE extends IOrderedBean, GROUP_TYPE>
		implements IOrderedBeanCrudServiceInterceptorBuilder<BEAN_TYPE, GROUP_TYPE> {

	private IBeanIdentityResolver<BEAN_TYPE> identityResolver;
	private IBeanReader<BEAN_TYPE, Void> beanReader;
	private IOrderedBeanGroupMapper<BEAN_TYPE, GROUP_TYPE> groupMapper;

	OrderedBeanCrudInterceptorBuilderImpl() {
		this.groupMapper = new DefaultOrderedBeanGroupMapper<BEAN_TYPE, GROUP_TYPE>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public IOrderedBeanCrudServiceInterceptorBuilder<BEAN_TYPE, GROUP_TYPE> setIdentityResolver(
		final IBeanIdentityResolver<? extends BEAN_TYPE> identityResolver) {
		Assert.paramNotNull(identityResolver, "identityResolver");
		this.identityResolver = (IBeanIdentityResolver<BEAN_TYPE>) identityResolver;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IOrderedBeanCrudServiceInterceptorBuilder<BEAN_TYPE, GROUP_TYPE> setBeanReader(
		final IBeanReader<? extends BEAN_TYPE, Void> beanReader) {
		Assert.paramNotNull(identityResolver, "identityResolver");
		this.beanReader = (IBeanReader<BEAN_TYPE, Void>) beanReader;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IOrderedBeanCrudServiceInterceptorBuilder<BEAN_TYPE, GROUP_TYPE> setGroupMapper(
		final IOrderedBeanGroupMapper<? extends BEAN_TYPE, ? extends GROUP_TYPE> groupMapper) {
		Assert.paramNotNull(groupMapper, "groupMapper");
		this.groupMapper = (IOrderedBeanGroupMapper<BEAN_TYPE, GROUP_TYPE>) groupMapper;
		return this;
	}

	IBeanIdentityResolver<BEAN_TYPE> getIdentityResolver() {
		return identityResolver;
	}

	IBeanReader<BEAN_TYPE, Void> getBeanReader() {
		return beanReader;
	}

	IOrderedBeanGroupMapper<BEAN_TYPE, GROUP_TYPE> getGroupMapper() {
		return groupMapper;
	}

	@Override
	public ICrudServiceInterceptor<BEAN_TYPE> build() {
		return new OrderedBeansCrudInterceptorImpl<BEAN_TYPE>(this);
	}

}
