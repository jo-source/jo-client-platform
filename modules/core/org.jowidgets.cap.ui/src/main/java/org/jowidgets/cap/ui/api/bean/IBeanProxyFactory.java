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

package org.jowidgets.cap.ui.api.bean;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.ui.api.attribute.IAttributeSet;

public interface IBeanProxyFactory<BEAN_TYPE> {

	/**
	 * Adds a IBeanPropertyValidatorto the factory
	 * 
	 * @param validator The validator to add, must not be null
	 */
	void addBeanPropertyValidator(IBeanPropertyValidator<BEAN_TYPE> validator);

	/**
	 * Creates a list of bean proxies from bean dtos
	 * 
	 * @param beanDtos The bean dto's to crate the bean proxies for
	 * 
	 * @return A list of created bean proxies
	 */
	List<IBeanProxy<BEAN_TYPE>> createProxies(Collection<? extends IBeanDto> beanDtos);

	/**
	 * Creates a bean proxy from an bean dto
	 * 
	 * @param beanDto The bean dto to crate the bean proxy from
	 * 
	 * @return A bean proxy
	 */
	IBeanProxy<BEAN_TYPE> createProxy(IBeanDto beanDto);

	/**
	 * Creates a transient bean proxy
	 * 
	 * @return The created bean proxy
	 */
	IBeanProxy<BEAN_TYPE> createTransientProxy();

	/**
	 * Creates a transient bean proxy with given default values.
	 * 
	 * Remark: This may override the default values defined for the factory
	 * 
	 * @param defaultValues The default value to use
	 * 
	 * @returnThe created bean proxy
	 */
	IBeanProxy<BEAN_TYPE> createTransientProxy(Map<String, Object> defaultValues);

	/**
	 * Creates a last row dummy bean proxy
	 * 
	 * @return The created bean proxy
	 */
	IBeanProxy<BEAN_TYPE> createLastRowDummyProxy();

	/**
	 * Creates a dummy bean proxy
	 * 
	 * @return The created dummy bean proxy
	 */
	IBeanProxy<BEAN_TYPE> createDummyProxy();

	@Deprecated
	/**
	 * @deprecated replaced by {@link #createProxies(Collection<? extends IBeanDto> beanDtos)}
	 */
	List<IBeanProxy<BEAN_TYPE>> createProxies(Collection<? extends IBeanDto> beanDtos, IAttributeSet attributes);

	@Deprecated
	/**
	 * @deprecated replaced by {@link #createProxy(IBeanDto beanDto)}
	 */
	IBeanProxy<BEAN_TYPE> createProxy(IBeanDto beanDto, IAttributeSet attributes);

	@Deprecated
	/**
	 * @deprecated replaced by {@link #createTransientProxy()}
	 */
	IBeanProxy<BEAN_TYPE> createTransientProxy(IAttributeSet attributes);

	@Deprecated
	/**
	 * @deprecated replaced by {@link #createTransientProxy(Map<String, Object> defaultValues)}
	 */
	IBeanProxy<BEAN_TYPE> createTransientProxy(IAttributeSet attributes, Map<String, Object> defaultValues);

	@Deprecated
	/**
	 * @deprecated replaced by {@link #createLastRowDummyProxy()}
	 */
	IBeanProxy<BEAN_TYPE> createLastRowDummyProxy(IAttributeSet attributes);

	@Deprecated
	/**
	 * @deprecated replaced by {@link #createDummyProxy(IAttributeSet attributes)}
	 */
	IBeanProxy<BEAN_TYPE> createDummyProxy(IAttributeSet attributes);
}
