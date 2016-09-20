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

package org.jowidgets.cap.service.api.updater;

import java.util.Collection;

import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.service.api.adapter.ISyncUpdaterService;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanModifier;
import org.jowidgets.cap.service.api.bean.IBeanUpdateInterceptor;
import org.jowidgets.validation.IValidator;

public interface IUpdaterServiceBuilder<BEAN_TYPE> {

	IUpdaterServiceBuilder<BEAN_TYPE> addUpdaterInterceptor(IBeanUpdateInterceptor<BEAN_TYPE> interceptor);

	IUpdaterServiceBuilder<BEAN_TYPE> addBeanValidator(IBeanValidator<? extends BEAN_TYPE> validator);

	IUpdaterServiceBuilder<BEAN_TYPE> addPropertyValidator(String propertyName, IValidator<? extends Object> validator);

	/**
	 * If set to true, validation warnings must be confirmed by the user.
	 * 
	 * The default is false.
	 * 
	 * @param confirm true if warnings should be confirmed, false otherwise
	 * 
	 * @return This builder
	 */
	IUpdaterServiceBuilder<BEAN_TYPE> setConfirmValidationWarnings(boolean confirm);

	IUpdaterServiceBuilder<BEAN_TYPE> addExecutableChecker(IExecutableChecker<? extends BEAN_TYPE> executableChecker);

	IUpdaterServiceBuilder<BEAN_TYPE> setExecutableChecker(IExecutableChecker<? extends BEAN_TYPE> executableChecker);

	IUpdaterServiceBuilder<BEAN_TYPE> setBeanDtoFactory(IBeanDtoFactory<BEAN_TYPE> beanDtoFactory);

	IUpdaterServiceBuilder<BEAN_TYPE> setBeanModifier(IBeanModifier<BEAN_TYPE> beanModifier);

	IUpdaterServiceBuilder<BEAN_TYPE> setBeanDtoFactoryAndBeanModifier(final Collection<String> propertyNames);

	IUpdaterServiceBuilder<BEAN_TYPE> setAllowDeletedBeans(boolean allowDeletedBeans);

	IUpdaterServiceBuilder<BEAN_TYPE> setAllowStaleBeans(boolean allowStaleBeans);

	ISyncUpdaterService buildSyncService();

	IUpdaterService build();

}
