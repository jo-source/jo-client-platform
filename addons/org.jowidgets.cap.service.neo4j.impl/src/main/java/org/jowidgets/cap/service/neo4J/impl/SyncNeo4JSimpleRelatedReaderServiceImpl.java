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

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.neo4j.api.GraphDBConfig;
import org.jowidgets.cap.service.neo4j.api.IBeanFactory;
import org.jowidgets.cap.service.neo4j.api.NodeAccess;
import org.jowidgets.cap.service.tools.reader.AbstractSimpleReaderService;
import org.jowidgets.util.Assert;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.Index;

//TODO MG this implementation is not made for production use
final class SyncNeo4JSimpleRelatedReaderServiceImpl<BEAN_TYPE extends IBean, PARAM_TYPE> extends
		AbstractSimpleReaderService<BEAN_TYPE, PARAM_TYPE> {

	private final Class<? extends BEAN_TYPE> beanType;
	private final Object parentBeanTypeId;
	private final Object beanTypeId;
	private final String beanTypeIdString;
	private final Index<Node> nodeIndex;
	private final String beanTypePropertyName;
	private final IBeanFactory beanFactory;
	private final RelationshipType relationshipType;
	private final Direction direction;
	private final boolean related;

	SyncNeo4JSimpleRelatedReaderServiceImpl(
		final Object parentBeanTypeId,
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId,
		final RelationshipType relationshipType,
		final Direction direction,
		final boolean related,
		final IBeanDtoFactory<BEAN_TYPE> beanFactory) {

		super(beanFactory);

		Assert.paramNotNull(parentBeanTypeId, "parentBeanTypeId");
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(beanFactory, "beanFactory");
		Assert.paramNotNull(relationshipType, "relationshipType");
		Assert.paramNotNull(direction, "direction");

		this.parentBeanTypeId = parentBeanTypeId;
		this.beanType = beanType;
		this.beanTypeId = beanTypeId;
		this.beanTypeIdString = BeanTypeIdUtil.toString(beanTypeId);
		this.relationshipType = relationshipType;
		this.direction = direction;
		this.related = related;

		this.beanFactory = GraphDBConfig.getBeanFactory();
		this.nodeIndex = GraphDBConfig.getNodeIndex();
		this.beanTypePropertyName = GraphDBConfig.getBeanTypePropertyName();
	}

	@Override
	protected List<? extends BEAN_TYPE> getAllBeans(final List<? extends IBeanKey> parentBeans, final PARAM_TYPE parameter) {
		if (related) {
			return getAllRelatedBeans(parentBeans);
		}
		else {
			return getAllUnrelatedBeans(parentBeans);
		}
	}

	private List<? extends BEAN_TYPE> getAllRelatedBeans(final List<? extends IBeanKey> parentBeans) {
		final Set<BEAN_TYPE> result = new LinkedHashSet<BEAN_TYPE>();

		for (final IBeanKey beanKey : parentBeans) {
			final Node parentNode = NodeAccess.findNode(parentBeanTypeId, beanKey.getId());
			if (parentNode != null) {
				for (final Relationship relationship : parentNode.getRelationships(direction, relationshipType)) {
					result.add(beanFactory.createNodeBean(beanType, beanTypeId, relationship.getOtherNode(parentNode)));
				}
			}
		}

		return new LinkedList<BEAN_TYPE>(result);
	}

	private List<? extends BEAN_TYPE> getAllUnrelatedBeans(final List<? extends IBeanKey> parentBeans) {
		final List<BEAN_TYPE> result = new LinkedList<BEAN_TYPE>();
		if (beanFactory.isNodeBean(beanType, beanTypeId)) {
			for (final Node node : nodeIndex.get(beanTypePropertyName, beanTypeIdString)) {
				if (!isRelatedWith(node, parentBeans)) {
					result.add(beanFactory.createNodeBean(beanType, beanTypeId, node));
				}
			}
		}
		else {
			throw new IllegalStateException("The bean type '" + beanType + "' is not a node bean.");
		}
		return result;
	}

	private boolean isRelatedWith(final Node node, final List<? extends IBeanKey> parentBeans) {
		final Object nodeId = node.getProperty(IBean.ID_PROPERTY);
		for (final IBeanKey beanKey : parentBeans) {
			final Node parentNode = NodeAccess.findNode(parentBeanTypeId, beanKey.getId());
			if (parentNode != null) {
				for (final Relationship relationship : parentNode.getRelationships(direction, relationshipType)) {
					if (nodeId.equals(relationship.getOtherNode(parentNode).getProperty(IBean.ID_PROPERTY))) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
