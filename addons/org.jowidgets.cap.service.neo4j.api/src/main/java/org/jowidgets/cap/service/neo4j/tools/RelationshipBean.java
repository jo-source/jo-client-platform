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

package org.jowidgets.cap.service.neo4j.tools;

import org.jowidgets.cap.common.api.exception.DeletedBeanException;
import org.jowidgets.cap.service.neo4j.api.IRelationshipBean;
import org.jowidgets.cap.service.neo4j.api.NodeAccess;
import org.jowidgets.cap.service.neo4j.api.RelationshipAccess;
import org.jowidgets.util.Assert;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class RelationshipBean extends PropertyContainerBean implements IRelationshipBean {

	private Node tempStartNode;
	private Node tempEndNode;

	public RelationshipBean(final Relationship relationship) {
		super(relationship);
	}

	@Override
	public Relationship getRelationship() {
		return (Relationship) getPropertyContainer();
	}

	protected Object getStartNodeId() {
		return getNodeId(getRelationship().getStartNode());
	}

	protected Object getEndNodeId() {
		return getNodeId(getRelationship().getEndNode());
	}

	protected void setStartNodeId(final Object beanTypeId, final RelationshipType relationshipType, final Object id) {
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(relationshipType, "relationshipType");
		Assert.paramNotNull(id, "id");

		final Node startNode = NodeAccess.findNode(beanTypeId, id);
		if (startNode == null) {
			throw new DeletedBeanException(id);
		}
		final Node endNode = getEndNode();
		if (endNode != null) {
			createRelationship(beanTypeId, relationshipType, startNode, endNode);
			tempStartNode = null;
			tempEndNode = null;
		}
		else {
			tempStartNode = startNode;
		}
	}

	protected void setEndNodeId(final Object beanTypeId, final RelationshipType relationshipType, final Object id) {
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(relationshipType, "relationshipType");
		Assert.paramNotNull(id, "id");

		final Node endNode = NodeAccess.findNode(beanTypeId, id);
		if (endNode == null) {
			throw new DeletedBeanException(id);
		}
		final Node startNode = getStartNode();
		if (startNode != null) {
			createRelationship(beanTypeId, relationshipType, startNode, endNode);
			tempStartNode = null;
			tempEndNode = null;
		}
		else {
			tempEndNode = endNode;
		}
	}

	private Node getStartNode() {
		final Node startNode = getRelationship().getStartNode();
		if (startNode != null) {
			return startNode;
		}
		else {
			return tempStartNode;
		}
	}

	private Node getEndNode() {
		final Node endNode = getRelationship().getEndNode();
		if (endNode != null) {
			return endNode;
		}
		else {
			return tempEndNode;
		}
	}

	private Object getNodeId(final Node node) {
		if (node != null) {
			return new NodeBean(node).getId();
		}
		else {
			return null;
		}
	}

	private void createRelationship(
		final Object beanTypeId,
		final RelationshipType relationshipType,
		final Node startNode,
		final Node endNode) {
		setRelationship(RelationshipAccess.createRelationship(beanTypeId, relationshipType, startNode, endNode));
	}

	private void setRelationship(final Relationship newRelationship) {
		final Relationship oldRelationship = getRelationship();
		for (final String key : oldRelationship.getPropertyKeys()) {
			newRelationship.setProperty(key, oldRelationship.getProperty(key));
		}
		oldRelationship.delete();
		setPropertyContainer(newRelationship);
	}

}
