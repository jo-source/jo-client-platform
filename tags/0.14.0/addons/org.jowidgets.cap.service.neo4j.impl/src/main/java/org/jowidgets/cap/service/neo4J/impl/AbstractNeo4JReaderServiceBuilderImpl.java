/*
 * Copyright (c) 2012, grossmann
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

package org.jowidgets.cap.service.neo4J.impl;

import java.util.Collection;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.adapter.ISyncReaderService;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.neo4j.api.INeo4JReaderServiceBuilder;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IAdapterFactory;

abstract class AbstractNeo4JReaderServiceBuilderImpl<BUILDER_TYPE extends INeo4JReaderServiceBuilder<?, ?, ?>, BEAN_TYPE extends IBean, PARAM_TYPE> implements
		INeo4JReaderServiceBuilder<BUILDER_TYPE, BEAN_TYPE, PARAM_TYPE> {

	private Class<? extends BEAN_TYPE> beanType;
	private Object beanTypeId;
	private IBeanDtoFactory<BEAN_TYPE> beanDtoFactory;
	private Collection<String> beanDtoFactoryProperties;

	abstract ISyncReaderService<PARAM_TYPE> doBuild();

	@SuppressWarnings("unchecked")
	@Override
	public final BUILDER_TYPE setBeanType(final Class<? extends BEAN_TYPE> beanType) {
		this.beanType = beanType;
		return (BUILDER_TYPE) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final BUILDER_TYPE setBeanTypeId(final Object beanTypeId) {
		this.beanTypeId = beanTypeId;
		return (BUILDER_TYPE) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final BUILDER_TYPE setBeanDtoFactory(final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory) {
		Assert.paramNotNull(beanDtoFactory, "beanDtoFactory");
		this.beanDtoFactory = beanDtoFactory;
		this.beanDtoFactoryProperties = null;
		return (BUILDER_TYPE) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final BUILDER_TYPE setBeanDtoFactory(final Collection<String> propertyNames) {
		Assert.paramNotNull(propertyNames, "propertyNames");
		this.beanDtoFactoryProperties = propertyNames;
		return (BUILDER_TYPE) this;
	}

	protected final IBeanDtoFactory<BEAN_TYPE> getBeanDtoFactory() {
		if (beanDtoFactory == null) {
			if (beanDtoFactoryProperties != null && beanType != null) {
				return CapServiceToolkit.dtoFactory(beanType, beanDtoFactoryProperties);
			}
			else {
				return null;
			}
		}
		else {
			return beanDtoFactory;
		}
	}

	protected final Class<? extends BEAN_TYPE> getBeanType() {
		return beanType;
	}

	protected final Object getBeanTypeId() {
		if (beanTypeId != null) {
			return beanTypeId;
		}
		else {
			return beanType;
		}
	}

	@Override
	public final IReaderService<PARAM_TYPE> build() {
		final IAdapterFactory<IReaderService<PARAM_TYPE>, ISyncReaderService<PARAM_TYPE>> adapterFactory;
		adapterFactory = CapServiceToolkit.adapterFactoryProvider().reader();
		return adapterFactory.createAdapter(doBuild());
	}

}
