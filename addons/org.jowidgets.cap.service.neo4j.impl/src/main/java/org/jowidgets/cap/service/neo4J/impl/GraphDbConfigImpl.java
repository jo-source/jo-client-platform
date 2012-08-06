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

import org.jowidgets.cap.service.neo4j.api.IBeanFactory;
import org.jowidgets.cap.service.neo4j.api.IGraphDBConfig;
import org.jowidgets.cap.service.neo4j.api.IIdGenerator;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;

final class GraphDbConfigImpl implements IGraphDBConfig {

	private final GraphDatabaseService graphDb;
	private final Index<Node> nodeIndex;
	private final Index<Relationship> relationshipIndex;
	private final IBeanFactory beanFactory;
	private final String beanTypePropertyName;
	private final IIdGenerator idGenerator;

	GraphDbConfigImpl(
		final GraphDatabaseService graphDb,
		final boolean autoShuttdown,
		final Index<Node> nodeIndex,
		final Index<Relationship> relationshipIndex,
		final IBeanFactory beanFactory,
		final String beanTypePropertyName,
		final IIdGenerator idGenerator) {

		this.graphDb = graphDb;
		this.nodeIndex = nodeIndex;
		this.relationshipIndex = relationshipIndex;
		this.beanFactory = beanFactory;
		this.beanTypePropertyName = beanTypePropertyName;
		this.idGenerator = idGenerator;

		if (autoShuttdown) {
			registerShutdownHook(graphDb);
		}
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	@Override
	public GraphDatabaseService getGraphDbService() {
		return graphDb;
	}

	@Override
	public Index<Node> getNodeIndex() {
		return nodeIndex;
	}

	@Override
	public Index<Relationship> getRelationshipIndex() {
		return relationshipIndex;
	}

	@Override
	public IBeanFactory getBeanFactory() {
		return beanFactory;
	}

	@Override
	public String getBeanTypePropertyName() {
		return beanTypePropertyName;
	}

	@Override
	public IIdGenerator getIdGenerator() {
		return idGenerator;
	}

}
