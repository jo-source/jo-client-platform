/*
 * Copyright (c) 2016, Grossmann
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

import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.AttributeSet;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeSet;

/**
 * Accessor class to create {@link IBeanProxyFactory} instances
 */
public final class BeanProxyFactory {

	private BeanProxyFactory() {}

	/**
	 * Creates a new bean proxy factory
	 * 
	 * @param beanTypeId The bean type id to use for created bean proxies
	 * @param beanType The bean type to use for the created bean proxies
	 * 
	 * @return A new IBeanProxyFactory
	 * 
	 * @deprecated Use {@link #beanProxyFactory(Object, Class, IAttributeSet)}
	 */
	@Deprecated
	public static <BEAN_TYPE> IBeanProxyFactory<BEAN_TYPE> create(
		final Object beanTypeId,
		final Class<? extends BEAN_TYPE> beanType) {
		return CapUiToolkit.beanProxyFactory(beanTypeId, beanType);
	}

	/**
	 * Creates a new bean proxy factory
	 * 
	 * @param beanTypeId The bean type id to use for created bean proxies
	 * @param beanType The bean type to use for the created bean proxies
	 * @param attributeSet The attribute set to use
	 * 
	 * @return A new IBeanProxyFactory
	 */
	public static <BEAN_TYPE> IBeanProxyFactory<BEAN_TYPE> create(
		final Object beanTypeId,
		final Class<? extends BEAN_TYPE> beanType,
		final IAttributeSet attributeSet) {
		return CapUiToolkit.beanProxyFactory(beanTypeId, beanType, attributeSet);
	}

	/**
	 * Creates a new bean proxy factory
	 * 
	 * @param beanTypeId The bean type id to use for created bean proxies
	 * @param beanType The bean type to use for the created bean proxies
	 * @param attributes The attribute to use
	 * 
	 * @return A new IBeanProxyFactory
	 */
	public static <BEAN_TYPE> IBeanProxyFactory<BEAN_TYPE> create(
		final Object beanTypeId,
		final Class<? extends BEAN_TYPE> beanType,
		final Collection<IAttribute<?>> attributes) {
		return CapUiToolkit.beanProxyFactory(beanTypeId, beanType, AttributeSet.create(attributes));
	}

	/**
	 * Creates a new bean proxy factory and configures it by its entity id with help of the entity service
	 * 
	 * @param entityId The entity id to use
	 * 
	 * @return A new IBeanProxyFactory
	 */
	public static <BEAN_TYPE> IBeanProxyFactory<BEAN_TYPE> create(final Object entityId) {
		final IBeanProxyFactoryBuilder<BEAN_TYPE> builder = builder(entityId);
		return builder.build();
	}

	/**
	 * Creates a new bean proxy factory builder and configures it by its entity id with help of the entity service
	 * 
	 * @param entityId The entity id to use
	 * 
	 * @return A new IBeanProxyFactoryBuilder
	 */
	public static <BEAN_TYPE> IBeanProxyFactoryBuilder<BEAN_TYPE> builder(final Object entityId) {
		return CapUiToolkit.beanProxyFactoryBuilder(entityId);
	}

	/**
	 * Creates a new bean proxy factory builder
	 * 
	 * @param beanType The bean type to use for the created bean proxies
	 * 
	 * @return A new IBeanProxyFactoryBuilder
	 */
	public static <BEAN_TYPE> IBeanProxyFactoryBuilder<BEAN_TYPE> builder(final Class<BEAN_TYPE> beanType) {
		return CapUiToolkit.beanProxyFactoryBuilder(beanType);
	}
}
