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

import org.jowidgets.cap.common.api.ordered.IOrderedBean;
import org.jowidgets.cap.service.api.bean.IBeanIdentityResolver;
import org.jowidgets.cap.service.api.bean.IBeanReader;
import org.jowidgets.cap.service.api.crud.ICrudServiceInterceptor;

public interface IOrderedBeanCrudServiceInterceptorBuilder<BEAN_TYPE extends IOrderedBean, GROUP_TYPE> {

	/**
	 * Sets the identity resolver. The resolver is mandatory
	 * 
	 * @param identityResolver The resolver to set
	 * 
	 * @return This builder
	 */
	IOrderedBeanCrudServiceInterceptorBuilder<BEAN_TYPE, GROUP_TYPE> setIdentityResolver(
		IBeanIdentityResolver<? extends BEAN_TYPE> identityResolver);

	/**
	 * Sets the bean reader. The reader is mandatory
	 * 
	 * @param beanReader The reader to set
	 *
	 * @return This builder
	 */
	IOrderedBeanCrudServiceInterceptorBuilder<BEAN_TYPE, GROUP_TYPE> setBeanReader(
		IBeanReader<? extends BEAN_TYPE, Void> beanReader);

	/**
	 * Sets the group mapper. If not set, a default mapper will be used that assmues only one group exists
	 * 
	 * @param mapper The mapper to set
	 *
	 * @return This builder
	 */
	IOrderedBeanCrudServiceInterceptorBuilder<BEAN_TYPE, GROUP_TYPE> setGroupMapper(
		IOrderedBeanGroupMapper<? extends BEAN_TYPE, ? extends GROUP_TYPE> mapper);

	ICrudServiceInterceptor<BEAN_TYPE> build();

}
