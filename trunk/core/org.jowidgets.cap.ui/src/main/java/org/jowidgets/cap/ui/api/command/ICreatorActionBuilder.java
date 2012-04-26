/*
 * Copyright (c) 2011, H.Westphal
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

package org.jowidgets.cap.ui.api.command;

import java.util.Collection;
import java.util.List;

import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.service.api.IServiceId;

public interface ICreatorActionBuilder<BEAN_TYPE> extends ICapActionBuilder<ICreatorActionBuilder<BEAN_TYPE>> {

	/**
	 * Sets the entity label (singular).
	 * This will set a proper text with the entity label as a variable.
	 * 
	 * @param label The label to set
	 * 
	 * @return This builder
	 */
	ICreatorActionBuilder<BEAN_TYPE> setEntityLabelSingular(String label);

	ICreatorActionBuilder<BEAN_TYPE> setAttributes(Collection<? extends IAttribute<?>> attributes);

	ICreatorActionBuilder<BEAN_TYPE> setBeanForm(IBeanFormBluePrint<BEAN_TYPE> beanForm);

	ICreatorActionBuilder<BEAN_TYPE> setBeanForm(Collection<? extends IAttribute<?>> attributes);

	ICreatorActionBuilder<BEAN_TYPE> setBeanPropertyValidators(List<? extends IBeanPropertyValidator<BEAN_TYPE>> validators);

	ICreatorActionBuilder<BEAN_TYPE> addBeanPropertyValidator(IBeanPropertyValidator<BEAN_TYPE> validator);

	ICreatorActionBuilder<BEAN_TYPE> setCreatorService(ICreatorService creatorService);

	ICreatorActionBuilder<BEAN_TYPE> setCreatorService(IServiceId<ICreatorService> creatorServiceId);

	ICreatorActionBuilder<BEAN_TYPE> setCreatorService(String creatorServiceId);

	ICreatorActionBuilder<BEAN_TYPE> setAnySelection(boolean anySelection);

	ICreatorActionBuilder<BEAN_TYPE> addEnabledChecker(IEnabledChecker enabledChecker);

	ICreatorActionBuilder<BEAN_TYPE> setExceptionConverter(IBeanExceptionConverter exceptionConverter);

	ICreatorActionBuilder<BEAN_TYPE> addExecutionInterceptor(IExecutionInterceptor<List<IBeanDto>> interceptor);

}
