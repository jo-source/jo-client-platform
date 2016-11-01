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

package org.jowidgets.cap.service.api.ordered;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.ordered.IOrderedBean;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanReader;
import org.jowidgets.cap.service.api.crud.ICrudServiceInterceptor;
import org.jowidgets.cap.service.api.factory.IBeanServiceFactory;
import org.jowidgets.util.Assert;

public final class OrderedBeanCrudServiceInterceptor {

	private OrderedBeanCrudServiceInterceptor() {}

	public static <BEAN_TYPE extends IOrderedBean & IBean> ICrudServiceInterceptor<BEAN_TYPE> create(
		final Class<? extends BEAN_TYPE> beanType,
		final IBeanServiceFactory serviceFactory) {

		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(serviceFactory, "serviceFactory");

		final IOrderedBeanCrudServiceInterceptorBuilder<BEAN_TYPE, ?> builder = builder();
		setIdentityResolverAndBeanReaderOnBuilder(builder, beanType, serviceFactory);
		return builder.build();
	}

	public static <BEAN_TYPE extends IOrderedBean & IBean> ICrudServiceInterceptor<BEAN_TYPE> create(
		final Class<? extends BEAN_TYPE> beanType,
		final IBeanServiceFactory serviceFactory,
		final IOrderedBeanGroupMapper<? extends BEAN_TYPE, ?> groupMapper) {

		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(serviceFactory, "serviceFactory");
		Assert.paramNotNull(groupMapper, "groupMapper");

		final IOrderedBeanCrudServiceInterceptorBuilder<BEAN_TYPE, Object> builder = builder();
		setIdentityResolverAndBeanReaderOnBuilder(builder, beanType, serviceFactory);
		builder.setGroupMapper(groupMapper);
		return builder.build();
	}

	public static <BEAN_TYPE extends IOrderedBean, GROUP_TYPE> IOrderedBeanCrudServiceInterceptorBuilder<BEAN_TYPE, GROUP_TYPE> builder() {
		return CapServiceToolkit.orderedBeanCrudInterceptorBuilder();
	}

	private static <BEAN_TYPE extends IOrderedBean & IBean> void setIdentityResolverAndBeanReaderOnBuilder(
		final IOrderedBeanCrudServiceInterceptorBuilder<BEAN_TYPE, ?> builder,
		final Class<? extends BEAN_TYPE> beanType,
		final IBeanServiceFactory serviceFactory) {
		builder.setIdentityResolver(serviceFactory.beanAccess(beanType));
		final IBeanReader<BEAN_TYPE, Void> beanReader = serviceFactory.beanReader(beanType);
		builder.setBeanReader(beanReader);
	}
}
