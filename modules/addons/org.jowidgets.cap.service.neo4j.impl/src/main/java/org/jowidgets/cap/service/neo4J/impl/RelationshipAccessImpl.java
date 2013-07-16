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

import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.exception.ServiceException;
import org.jowidgets.cap.service.neo4j.api.GraphDBConfig;
import org.jowidgets.cap.service.neo4j.api.IRelationshipAccess;
import org.jowidgets.cap.service.neo4j.api.IdGenerator;
import org.jowidgets.util.Assert;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.UniqueFactory;
import org.neo4j.graphdb.index.UniqueFactory.UniqueRelationshipFactory;

final class RelationshipAccessImpl implements IRelationshipAccess {

	private final Index<Relationship> relationshipIndex;
	private final String beanTypePropertyName;

	RelationshipAccessImpl() {
		this.relationshipIndex = GraphDBConfig.getRelationshipIndex();
		this.beanTypePropertyName = GraphDBConfig.getBeanTypePropertyName();
	}

	@Override
	public Relationship findRelationship(final Object beanTypeId, final Object nodeId) {
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(nodeId, "nodeId");

		Relationship result = null;
		final String beanTypeIdString = BeanTypeIdUtil.toString(beanTypeId);
		for (final Relationship relationship : relationshipIndex.get(IBean.ID_PROPERTY, nodeId)) {
			if (beanTypeIdString.equals(relationship.getProperty(beanTypePropertyName))) {
				if (result == null) {
					result = relationship;
				}
				else {
					throw new ServiceException("More than one relationship found for the id '"
						+ nodeId
						+ "' and the type '"
						+ beanTypeIdString
						+ "'.");
				}
			}
		}
		return result;
	}

	@Override
	public Relationship createRelationship(
		final Object beanTypeId,
		final RelationshipType relationshipType,
		final Node startNode,
		final Node endNode) {

		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(relationshipType, "relationshipType");
		Assert.paramNotNull(startNode, "startNode");
		Assert.paramNotNull(endNode, "endNode");

		final String beanTypeIdString = BeanTypeIdUtil.toString(beanTypeId);
		final UniqueRelationshipFactory factory = new UniqueFactory.UniqueRelationshipFactory(relationshipIndex) {
			@Override
			protected Relationship create(final Map<String, Object> properties) {
				final Relationship created = startNode.createRelationshipTo(endNode, relationshipType);
				created.setProperty(IBean.ID_PROPERTY, properties.get(IBean.ID_PROPERTY));
				return created;
			}
		};

		final Relationship result = factory.getOrCreate(IBean.ID_PROPERTY, IdGenerator.createUniqueId(beanTypeIdString));
		result.setProperty(beanTypePropertyName, beanTypeIdString);
		relationshipIndex.add(result, beanTypePropertyName, beanTypeIdString);
		return result;
	}

	@Override
	public Relationship createDummyRelationship(final Object beanTypeId) {
		return new DummyRelationshipImpl(beanTypeId);
	}

}
