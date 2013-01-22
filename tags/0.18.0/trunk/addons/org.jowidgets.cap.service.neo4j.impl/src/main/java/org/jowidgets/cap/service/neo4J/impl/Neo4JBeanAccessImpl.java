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
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.neo4j.api.GraphDBConfig;
import org.jowidgets.cap.service.neo4j.api.IBeanFactory;
import org.jowidgets.cap.service.neo4j.api.NodeAccess;
import org.jowidgets.cap.service.neo4j.api.RelationshipAccess;
import org.jowidgets.util.Assert;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

final class Neo4JBeanAccessImpl<BEAN_TYPE extends IBean> implements IBeanAccess<BEAN_TYPE> {

	private final Class<BEAN_TYPE> beanType;
	private final Object beanTypeId;
	private final IBeanFactory beanFactory;

	@SuppressWarnings("unchecked")
	Neo4JBeanAccessImpl(final Class<? extends BEAN_TYPE> beanType, final Object beanTypeId) {
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(beanTypeId, "beanTypeId");

		this.beanType = (Class<BEAN_TYPE>) beanType;
		this.beanTypeId = beanTypeId;
		this.beanFactory = GraphDBConfig.getBeanFactory();
	}

	@Override
	public List<BEAN_TYPE> getBeans(final Collection<? extends IBeanKey> keys, final IExecutionCallback executionCallback) {
		Assert.paramNotNull(keys, "keys");

		final List<BEAN_TYPE> result = new LinkedList<BEAN_TYPE>();

		for (final IBeanKey key : keys) {
			CapServiceToolkit.checkCanceled(executionCallback);
			final BEAN_TYPE bean = findBean(key.getId());
			if (bean != null) {
				result.add(bean);
			}
		}

		return result;
	}

	private BEAN_TYPE findBean(final Object id) {
		if (beanFactory.isNodeBean(beanType, beanTypeId)) {
			final Node node = NodeAccess.findNode(beanTypeId, id);
			if (node != null) {
				return beanFactory.createNodeBean(beanType, beanTypeId, node);
			}
			else {
				return null;
			}
		}
		else if (beanFactory.isRelationshipBean(beanType, beanTypeId)) {
			final Relationship relationship = RelationshipAccess.findRelationship(beanTypeId, id);
			if (relationship != null) {
				return beanFactory.createRelationshipBean(beanType, beanTypeId, relationship);
			}
			else {
				return null;
			}
		}
		else {
			throw new IllegalStateException("The bean type '" + beanType + "' is neither a node bean nor a relationship bean.");
		}
	}

	@Override
	public Class<BEAN_TYPE> getBeanType() {
		return beanType;
	}

	@Override
	public Object getBeanTypeId() {
		return beanTypeId;
	}

	@Override
	public void flush() {}

}
