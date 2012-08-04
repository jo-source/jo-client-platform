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

package org.jowidgets.cap.service.neo4j.api;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

public interface IGraphDBConfigBuilder {

	String DEFAULT_EMBEDDED_GRAPH_DB_PATH = "NEO4J_DB";
	String DEFAULT_NODE_INDEX_NAME = "nodeIndex";
	String DEFAULT_BEAN_TYPE_PROPERTY_NAME = "type";

	/**
	 * Sets the GraphDatabaseService.
	 * 
	 * @param graphDatabaseService The GraphDatabaseService to set
	 * 
	 * @return This builder
	 */
	IGraphDBConfigBuilder setGraphDbService(GraphDatabaseService graphDatabaseService);

	/**
	 * Sets an embedded GraphDatabaseService
	 * 
	 * @param path The path of the embedded graph database service
	 * @param autoShutdown If set true, the dabase will be shut down when virtual machine will be shut down
	 * 
	 * @return This builder
	 */
	IGraphDBConfigBuilder setGraphDbService(String path, boolean autoShutdown);

	/**
	 * Sets an embedded GraphDatabaseService. AutoShutdown will be enabled
	 * 
	 * @param path The path of the embedded graph database service
	 * 
	 * @return This builder
	 */
	IGraphDBConfigBuilder setGraphDbService(String path);

	/**
	 * Sets the node index that will be used to hold all nodes by id and by type
	 * 
	 * @param index The index to set
	 * 
	 * @return This builder
	 */
	IGraphDBConfigBuilder setNodeIndex(Index<Node> index);

	/**
	 * Sets the node index that will be used to hold all nodes by id and by type
	 * 
	 * @param index The index name to set
	 * 
	 * @return This builder
	 */
	IGraphDBConfigBuilder setNodeIndex(String indexName);

	/**
	 * Sets the bean factory that creates beans form nodes
	 * 
	 * @param beanFactory The bean factory to set
	 * 
	 * @return This builder
	 */
	IGraphDBConfigBuilder setBeanFactory(IBeanFactory beanFactory);

	/**
	 * Sets the name of the property that should be used to set the type of the bean
	 * 
	 * @param beanTypePropertyName The property name to set
	 * 
	 * @return This builder
	 */
	IGraphDBConfigBuilder setBeanTypePropertyName(String beanTypePropertyName);

	/**
	 * Sets the id generator that will be used to generate unique id's for each created node
	 * 
	 * @param idGenerator The id generator to set
	 * 
	 * @return This builder
	 */
	IGraphDBConfigBuilder setIdGenerator(INodeIdGenerator idGenerator);

	/**
	 * @return The newly created graph db config
	 */
	IGraphDBConfig build();

}
