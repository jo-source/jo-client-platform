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
import org.jowidgets.cap.service.neo4j.api.IGraphDBConfigBuilder;
import org.jowidgets.cap.service.neo4j.api.INodeIdGenerator;
import org.jowidgets.util.Assert;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;

final class GraphDbConfigBuilderImpl implements IGraphDBConfigBuilder {

	private GraphDatabaseService graphDb;
	private String embeddedGraphDbPath;
	private boolean autoShutdown;
	private Index<Node> nodeIndex;
	private String nodeIndexName;
	private IBeanFactory beanFactory;
	private String beanTypePropertyName;
	private INodeIdGenerator idGenerator;

	GraphDbConfigBuilderImpl() {
		this.embeddedGraphDbPath = DEFAULT_EMBEDDED_GRAPH_DB_PATH;
		this.autoShutdown = true;
		this.nodeIndexName = DEFAULT_NODE_INDEX_NAME;
		this.beanTypePropertyName = DEFAULT_BEAN_TYPE_PROPERTY_NAME;
		this.beanFactory = new DefaultBeanFactory();
		this.idGenerator = new DefaultNodeIdGenerator();
	}

	@Override
	public IGraphDBConfigBuilder setGraphDbService(final GraphDatabaseService graphDatabaseService) {
		Assert.paramNotNull(graphDatabaseService, "graphDatabaseService");
		this.embeddedGraphDbPath = null;
		this.graphDb = graphDatabaseService;
		return this;
	}

	@Override
	public IGraphDBConfigBuilder setGraphDbService(final String path, final boolean autoShutdown) {
		Assert.paramNotEmpty(path, "path");
		this.graphDb = null;
		this.embeddedGraphDbPath = path;
		this.autoShutdown = autoShutdown;
		return this;
	}

	@Override
	public IGraphDBConfigBuilder setGraphDbService(final String path) {
		return setGraphDbService(path, true);
	}

	@Override
	public IGraphDBConfigBuilder setNodeIndex(final Index<Node> index) {
		Assert.paramNotNull(index, "index");
		this.nodeIndexName = null;
		this.nodeIndex = index;
		return this;
	}

	@Override
	public IGraphDBConfigBuilder setNodeIndex(final String indexName) {
		Assert.paramNotEmpty(indexName, "indexName");
		this.nodeIndex = null;
		this.nodeIndexName = indexName;
		return this;
	}

	@Override
	public IGraphDBConfigBuilder setBeanFactory(final IBeanFactory beanFactory) {
		Assert.paramNotNull(beanFactory, "beanFactory");
		this.beanFactory = beanFactory;
		return this;
	}

	@Override
	public IGraphDBConfigBuilder setBeanTypePropertyName(final String beanTypePropertyName) {
		Assert.paramNotNull(beanTypePropertyName, "beanTypePropertyName");
		this.beanTypePropertyName = beanTypePropertyName;
		return this;
	}

	@Override
	public IGraphDBConfigBuilder setIdGenerator(final INodeIdGenerator idGenerator) {
		Assert.paramNotNull(idGenerator, "idGenerator");
		this.idGenerator = idGenerator;
		return this;
	}

	private GraphDatabaseService getGraphDbService() {
		if (graphDb != null) {
			return graphDb;
		}
		else {
			return new GraphDatabaseFactory().newEmbeddedDatabase(embeddedGraphDbPath);
		}
	}

	private Index<Node> getNodeIndex(final GraphDatabaseService graphDbService) {
		if (nodeIndex != null) {
			return nodeIndex;
		}
		else {
			return graphDbService.index().forNodes(nodeIndexName);
		}
	}

	@Override
	public IGraphDBConfig build() {
		final GraphDatabaseService graphDbService = getGraphDbService();
		return new GraphDbConfigImpl(
			graphDbService,
			autoShutdown,
			getNodeIndex(graphDbService),
			beanFactory,
			beanTypePropertyName,
			idGenerator);
	}
}
