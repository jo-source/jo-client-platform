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

package org.jowidgets.cap.service.api.factory;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.bean.IBeanModifier;
import org.jowidgets.cap.service.api.deleter.IDeleterServiceBuilder;
import org.jowidgets.cap.service.api.entity.IBeanServicesProviderBuilder;
import org.jowidgets.service.api.IServiceRegistry;

public interface IBeanServiceFactory {

	<BEAN_TYPE extends IBean> IBeanAccess<BEAN_TYPE> beanAccess(Class<? extends BEAN_TYPE> beanType);

	<BEAN_TYPE extends IBean> IBeanServicesProviderBuilder beanServicesBuilder(
		IServiceRegistry registry,
		Object entityId,
		Class<? extends BEAN_TYPE> beanType,
		IBeanDtoFactory<BEAN_TYPE> beanDtoFactory,
		IBeanInitializer<BEAN_TYPE> beanInitializer,
		IBeanModifier<BEAN_TYPE> beanModifier);

	<BEAN_TYPE extends IBean> ICreatorService creatorService(
		Class<? extends BEAN_TYPE> beanType,
		IBeanDtoFactory<BEAN_TYPE> beanDtoFactory,
		IBeanInitializer<BEAN_TYPE> beanInitializer);

	<BEAN_TYPE extends IBean, PARAM_TYPE> IReaderService<PARAM_TYPE> readerService(
		Class<? extends BEAN_TYPE> beanType,
		IBeanDtoFactory<BEAN_TYPE> beanDtoFactory);

	<BEAN_TYPE extends IBean> IDeleterServiceBuilder<BEAN_TYPE> deleterServiceBuilder(Class<? extends BEAN_TYPE> beanType);

	IDeleterService deleterService(Class<? extends IBean> beanType, boolean allowDeletedData, boolean allowStaleData);

}
