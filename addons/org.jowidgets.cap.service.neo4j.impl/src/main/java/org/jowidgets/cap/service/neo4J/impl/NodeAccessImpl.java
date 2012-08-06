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
import org.jowidgets.cap.service.neo4j.api.INodeAccess;
import org.jowidgets.cap.service.neo4j.api.IdGenerator;
import org.jowidgets.util.Assert;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.UniqueFactory;

final class NodeAccessImpl implements INodeAccess {

	private final Index<Node> nodeIndex;
	private final String beanTypePropertyName;

	NodeAccessImpl() {
		this.nodeIndex = GraphDBConfig.getNodeIndex();
		this.beanTypePropertyName = GraphDBConfig.getBeanTypePropertyName();
	}

	@Override
	public Node findNode(final Object beanTypeId, final Object nodeId) {
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(nodeId, "nodeId");

		Node result = null;
		final String beanTypeIdString = BeanTypeIdUtil.toString(beanTypeId);
		for (final Node node : nodeIndex.get(IBean.ID_PROPERTY, nodeId)) {
			if (beanTypeIdString.equals(node.getProperty(beanTypePropertyName))) {
				if (result == null) {
					result = node;
				}
				else {
					throw new ServiceException("More than one node found for the id '"
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
	public Node createNewNode(final Object beanTypeId) {
		Assert.paramNotNull(beanTypeId, "beanTypeId");

		final String beanTypeIdString = BeanTypeIdUtil.toString(beanTypeId);
		final UniqueFactory<Node> factory = new UniqueFactory.UniqueNodeFactory(nodeIndex) {
			@Override
			protected void initialize(final Node created, final Map<String, Object> properties) {
				created.setProperty(IBean.ID_PROPERTY, properties.get(IBean.ID_PROPERTY));
			}
		};
		final Node result = factory.getOrCreate(IBean.ID_PROPERTY, IdGenerator.createUniqueId(beanTypeIdString));
		result.setProperty(beanTypePropertyName, beanTypeIdString);
		nodeIndex.add(result, beanTypePropertyName, beanTypeIdString);
		return result;
	}

}
