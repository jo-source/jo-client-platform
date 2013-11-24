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

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.neo4j.api.GraphDBConfig;
import org.jowidgets.cap.service.neo4j.api.IBeanFactory;
import org.jowidgets.cap.service.neo4j.api.NodeAccess;
import org.jowidgets.cap.service.neo4j.api.RelationshipAccess;
import org.jowidgets.cap.service.tools.creator.AbstractSyncCreatorServiceImpl;
import org.jowidgets.util.Assert;

final class SyncNeo4JCreatorServiceImpl<BEAN_TYPE extends IBean> extends AbstractSyncCreatorServiceImpl<BEAN_TYPE> {

	private final Class<? extends BEAN_TYPE> beanType;
	private final Object beanTypeId;
	private final IBeanFactory beanFactory;

	SyncNeo4JCreatorServiceImpl(
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId,
		final IBeanDtoFactory<BEAN_TYPE> dtoFactory,
		final IBeanInitializer<BEAN_TYPE> beanInitializer,
		final IExecutableChecker<BEAN_TYPE> executableChecker,
		final IBeanValidator<BEAN_TYPE> beanValidator) {

		super(beanType, dtoFactory, beanInitializer, executableChecker, beanValidator);

		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(beanTypeId, "beanTypeId");

		this.beanType = beanType;
		this.beanTypeId = beanTypeId;

		this.beanFactory = GraphDBConfig.getBeanFactory();
	}

	@Override
	protected BEAN_TYPE createBean(final IExecutionCallback executionCallback) {
		if (beanFactory.isNodeBean(beanType, beanTypeId)) {
			return beanFactory.createNodeBean(beanType, beanTypeId, NodeAccess.createNewNode(beanTypeId));
		}
		else if (beanFactory.isRelationshipBean(beanType, beanTypeId)) {
			return beanFactory.createRelationshipBean(
					beanType,
					beanTypeId,
					RelationshipAccess.createDummyRelationship(beanTypeId));
		}
		else {
			throw new IllegalStateException("The bean type '" + beanType + "' is neither a node bean nor a relationship bean.");
		}
	}

	@Override
	protected void persistBean(final BEAN_TYPE bean, final IExecutionCallback executionCallback) {
		// Nothing to do
	}

}
