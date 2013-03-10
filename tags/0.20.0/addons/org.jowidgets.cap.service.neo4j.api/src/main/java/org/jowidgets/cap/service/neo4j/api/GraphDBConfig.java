/*
 * Copyright (c) 2011, grossmann
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

import java.util.Iterator;
import java.util.ServiceLoader;

import org.jowidgets.util.Assert;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;

public final class GraphDBConfig {

	private static IGraphDBConfig instance;

	private GraphDBConfig() {}

	public static synchronized void initialize(final IGraphDBConfig instance) {
		Assert.paramNotNull(instance, "instance");
		if (GraphDBConfig.instance == null) {
			GraphDBConfig.instance = instance;
		}
		else {
			throw new IllegalStateException("The GraphDBConfig is already initialized");
		}
	}

	public static IGraphDBConfig getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	public static GraphDatabaseService getGraphDbService() {
		return getInstance().getGraphDbService();
	}

	public static Index<Node> getNodeIndex() {
		return getInstance().getNodeIndex();
	}

	public static Index<Relationship> getRelationshipIndex() {
		return getInstance().getRelationshipIndex();
	}

	public static IBeanFactory getBeanFactory() {
		return getInstance().getBeanFactory();
	}

	public static String getBeanTypePropertyName() {
		return getInstance().getBeanTypePropertyName();
	}

	public static IIdGenerator getIdGenerator() {
		return getInstance().getIdGenerator();
	}

	public static IGraphDBConfigBuilder builder() {
		return Neo4JServiceToolkit.graphDBConfigBuilder();
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			final ServiceLoader<IGraphDBConfig> serviceLoader = ServiceLoader.load(IGraphDBConfig.class);
			final Iterator<IGraphDBConfig> iterator = serviceLoader.iterator();

			if (!iterator.hasNext()) {
				instance = builder().build();
			}
			else {
				instance = iterator.next();
				if (iterator.hasNext()) {
					throw new IllegalStateException("More than one implementation found for '"
						+ IGraphDBConfig.class.getName()
						+ "'");
				}
			}
		}
	}

}
