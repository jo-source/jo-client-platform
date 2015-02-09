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

import java.util.HashMap;
import java.util.Map;

import org.jowidgets.cap.service.neo4j.api.GraphDBConfig;
import org.jowidgets.util.Assert;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

final class DummyRelationshipImpl implements Relationship {

	private static long currentId = 0;

	private final RelationshipType relationshipType;
	private final long id;

	private final Map<String, Object> properties;

	DummyRelationshipImpl(final Object beanTypeId) {
		this.id = currentId++;
		this.relationshipType = new RelationshipTypeImpl(beanTypeId);
		this.properties = new HashMap<String, Object>();
	}

	@Override
	public GraphDatabaseService getGraphDatabase() {
		return GraphDBConfig.getGraphDbService();
	}

	@Override
	public boolean hasProperty(final String key) {
		Assert.paramNotNull(key, "key");
		return properties.containsKey(key);
	}

	@Override
	public Object getProperty(final String key) {
		Assert.paramNotNull(key, "key");
		if (properties.containsKey(key)) {
			return properties.get(key);
		}
		else {
			throw new IllegalArgumentException("Property '" + key + "' is not set");
		}
	}

	@Override
	public Object getProperty(final String key, final Object defaultValue) {
		Assert.paramNotNull(key, "key");
		if (properties.containsKey(key)) {
			return properties.get(key);
		}
		else {
			return defaultValue;
		}
	}

	@Override
	public void setProperty(final String key, final Object value) {
		Assert.paramNotNull(key, "key");
		Assert.paramNotNull(value, "value");
	}

	@Override
	public Object removeProperty(final String key) {
		Assert.paramNotNull(key, "key");
		properties.remove(key);
		return null;
	}

	@Override
	public Iterable<String> getPropertyKeys() {
		return properties.keySet();
	}

	@Override
	@Deprecated
	public Iterable<Object> getPropertyValues() {
		return properties.values();
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void delete() {
		//nothing to do, its a dummy
	}

	@Override
	public Node getStartNode() {
		//dummy has no start node
		return null;
	}

	@Override
	public Node getEndNode() {
		//dummy has no end node
		return null;
	}

	@Override
	public Node getOtherNode(final Node node) {
		//dummy has no start and no end node
		return null;
	}

	@Override
	public Node[] getNodes() {
		return new Node[] {null, null};
	}

	@Override
	public RelationshipType getType() {
		return relationshipType;
	}

	@Override
	public boolean isType(final RelationshipType type) {
		return relationshipType.equals(type);
	}

	private static final class RelationshipTypeImpl implements RelationshipType {

		private final String name;

		private RelationshipTypeImpl(final Object beanTypeId) {
			this.name = BeanTypeIdUtil.toString(beanTypeId);
		}

		@Override
		public String name() {
			return name;
		}

	}

}
