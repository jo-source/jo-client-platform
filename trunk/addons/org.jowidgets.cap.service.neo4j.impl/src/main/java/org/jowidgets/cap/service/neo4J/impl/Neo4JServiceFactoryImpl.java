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
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.creator.ICreatorServiceBuilder;
import org.jowidgets.cap.service.api.deleter.IDeleterServiceBuilder;
import org.jowidgets.cap.service.neo4j.api.INeo4JServiceFactory;
import org.jowidgets.cap.service.tools.factory.AbstractBeanServiceFactory;
import org.jowidgets.util.IAdapterFactory;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;

final class Neo4JServiceFactoryImpl extends AbstractBeanServiceFactory implements INeo4JServiceFactory {

	@Override
	public <BEAN_TYPE extends IBean> IBeanAccess<BEAN_TYPE> beanAccess(
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId) {
		return new Neo4JBeanAccessImpl<BEAN_TYPE>(beanType, beanTypeId);
	}

	@Override
	public <BEAN_TYPE extends IBean> ICreatorServiceBuilder<BEAN_TYPE> creatorServiceBuilder(
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId) {
		return new Neo4JCreatorServiceBuilderImpl<BEAN_TYPE>(beanType, beanTypeId);
	}

	@Override
	public <BEAN_TYPE extends IBean, PARAM_TYPE> IReaderService<PARAM_TYPE> readerService(
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId,
		final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory) {

		final ISyncReaderService<PARAM_TYPE> result;
		result = new SyncNeo4JSimpleReaderServiceImpl<BEAN_TYPE, PARAM_TYPE>(beanType, beanTypeId, beanDtoFactory);

		final IAdapterFactory<IReaderService<PARAM_TYPE>, ISyncReaderService<PARAM_TYPE>> adapterFactory;
		adapterFactory = CapServiceToolkit.adapterFactoryProvider().reader();
		return adapterFactory.createAdapter(result);
	}

	@Override
	public <BEAN_TYPE extends IBean> IDeleterServiceBuilder<BEAN_TYPE> deleterServiceBuilder(
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId) {
		return new Neo4JDeleterServiceBuilderImpl<BEAN_TYPE>(beanAccess(beanType, beanTypeId));
	}

	@Override
	public <BEAN_TYPE extends IBean, PARAM_TYPE> IReaderService<PARAM_TYPE> readerService(
		final Object parentBeanTypeId,
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId,
		final RelationshipType relationshipType,
		final Direction direction,
		final boolean related,
		final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory) {

		final ISyncReaderService<PARAM_TYPE> result;
		result = new SyncNeo4JSimpleRelatedReaderServiceImpl<BEAN_TYPE, PARAM_TYPE>(
			parentBeanTypeId,
			beanType,
			beanTypeId,
			relationshipType,
			direction,
			related,
			beanDtoFactory);

		final IAdapterFactory<IReaderService<PARAM_TYPE>, ISyncReaderService<PARAM_TYPE>> adapterFactory;
		adapterFactory = CapServiceToolkit.adapterFactoryProvider().reader();
		return adapterFactory.createAdapter(result);
	}

	@Override
	public <BEAN_TYPE extends IBean, PARAM_TYPE> IReaderService<PARAM_TYPE> readerService(
		final Object parentBeanTypeId,
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId,
		final RelationshipType relationshipType,
		final Direction direction,
		final boolean related,
		final Collection<String> propertyNames) {
		return readerService(
				parentBeanTypeId,
				beanType,
				beanTypeId,
				relationshipType,
				direction,
				related,
				CapServiceToolkit.dtoFactory(beanType, propertyNames));
	}

	@Override
	public <BEAN_TYPE extends IBean, PARAM_TYPE> IReaderService<PARAM_TYPE> readerService(
		final Object parentBeanTypeId,
		final Class<? extends BEAN_TYPE> beanType,
		final RelationshipType relationshipType,
		final Direction direction,
		final boolean related,
		final Collection<String> propertyNames) {
		return readerService(parentBeanTypeId, beanType, beanType, relationshipType, direction, related, propertyNames);
	}

}
