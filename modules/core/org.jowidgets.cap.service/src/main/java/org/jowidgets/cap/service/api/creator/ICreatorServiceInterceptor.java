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

package org.jowidgets.cap.service.api.creator;

import java.util.Collection;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;

public interface ICreatorServiceInterceptor<BEAN_TYPE> {

	/**
	 * Will be invoked before the new beans will be initialized with the bean data values
	 * 
	 * @param parentBeanKeys The keys of the parents
	 * @param beans The new beans
	 * @param beanDataMapper Provides the IBeanData for each new bean
	 * @param executionCallback The execution callback
	 */
	void beforeInitializeForCreation(
		List<IBeanKey> parentBeanKeys,
		Collection<BEAN_TYPE> beans,
		IBeanDataMapper<BEAN_TYPE> beanDataMapper,
		IExecutionCallback executionCallback);

	/**
	 * Will be invoked after the new beans was initialized with the bean data values
	 * 
	 * @param parentBeanKeys The keys of the parents
	 * @param beans The new beans
	 * @param beanDataMapper Provides the IBeanData for each new bean
	 * @param executionCallback The execution callback
	 */
	void afterInitializeForCreation(
		List<IBeanKey> parentBeanKeys,
		Collection<BEAN_TYPE> beans,
		IBeanDataMapper<BEAN_TYPE> beanDataMapper,
		IExecutionCallback executionCallback);

	/**
	 * Will be invoked after the new beans was created and before they will be
	 * converted into IBeanDtos
	 * 
	 * @param parentBeanKeys The keys of the parents
	 * @param beans The new beans
	 * @param beanDataMapper Provides the IBeanData for each new bean
	 * @param executionCallback The execution callback
	 */
	void afterCreation(
		List<IBeanKey> parentBeanKeys,
		Collection<BEAN_TYPE> beans,
		IBeanDataMapper<BEAN_TYPE> beanDataMapper,
		IExecutionCallback executionCallback);

}
