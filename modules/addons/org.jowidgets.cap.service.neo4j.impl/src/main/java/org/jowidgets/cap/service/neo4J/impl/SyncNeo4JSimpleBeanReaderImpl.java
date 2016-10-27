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
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.service.api.bean.IBeanPropertyAccessor;
import org.jowidgets.cap.service.tools.reader.AbstractSimpleBeanReader;
import org.jowidgets.util.Assert;

//TODO MG this implementation is not made for production use
final class SyncNeo4JSimpleBeanReaderImpl<BEAN_TYPE extends IBean, PARAM_TYPE>
		extends AbstractSimpleBeanReader<BEAN_TYPE, PARAM_TYPE> {

	private final Neo4JAllBeansProvider<BEAN_TYPE, PARAM_TYPE> beansProvider;

	SyncNeo4JSimpleBeanReaderImpl(
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId,
		final IBeanPropertyAccessor<BEAN_TYPE> propertyAccessor) {
		this(beanType, beanTypeId, propertyAccessor, null);
	}

	SyncNeo4JSimpleBeanReaderImpl(
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId,
		final IBeanPropertyAccessor<BEAN_TYPE> propertyAccessor,
		final Collection<IFilter> additionalFilters) {
		super(beanType, propertyAccessor, additionalFilters);
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(propertyAccessor, "propertyAccessor");

		this.beansProvider = new Neo4JAllBeansProvider<BEAN_TYPE, PARAM_TYPE>(beanType, beanTypeId);
	}

	@Override
	protected List<BEAN_TYPE> getAllBeans(
		final List<? extends IBeanKey> parentBeans,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {
		return beansProvider.getAllBeans(parentBeans, parameter, executionCallback);
	}
}
