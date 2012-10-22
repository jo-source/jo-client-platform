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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.neo4j.api.GraphDBConfig;
import org.jowidgets.cap.service.neo4j.api.IBeanFactory;
import org.jowidgets.cap.service.neo4j.api.NodeAccess;
import org.jowidgets.cap.service.tools.reader.AbstractSimpleReaderService;
import org.jowidgets.util.Assert;
import org.jowidgets.util.Tuple;
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
	private final Collection<Tuple<RelationshipType, Direction>> path;
	private final boolean related;

	SyncNeo4JSimpleRelatedReaderServiceImpl(
		final Object parentBeanTypeId,
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId,
		final Collection<Tuple<RelationshipType, Direction>> path,
		final boolean related,
		final IBeanDtoFactory<BEAN_TYPE> beanFactory,
		final Collection<IFilter> additionalFilters) {

		super(beanFactory, additionalFilters);

		Assert.paramNotNull(parentBeanTypeId, "parentBeanTypeId");
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(beanFactory, "beanFactory");
		Assert.paramNotNull(path, "relationshipType");

		this.parentBeanTypeId = parentBeanTypeId;
		this.beanType = beanType;
		this.beanTypeId = beanTypeId;
		this.beanTypeIdString = BeanTypeIdUtil.toString(beanTypeId);
		this.path = new LinkedList<Tuple<RelationshipType, Direction>>(path);
		this.related = related;

		this.beanFactory = GraphDBConfig.getBeanFactory();
		this.nodeIndex = GraphDBConfig.getNodeIndex();
		this.beanTypePropertyName = GraphDBConfig.getBeanTypePropertyName();
	}

	@Override
	protected List<? extends BEAN_TYPE> getAllBeans(
		final List<? extends IBeanKey> parentBeans,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {
		if (related) {
			return getAllRelatedBeans(parentBeans, executionCallback);
		}
		else {
			return getAllUnrelatedBeans(parentBeans, executionCallback);
		}
	}

	private List<? extends BEAN_TYPE> getAllRelatedBeans(
		final List<? extends IBeanKey> parentBeans,
		final IExecutionCallback executionCallback) {
		final Set<BEAN_TYPE> result = new LinkedHashSet<BEAN_TYPE>();

		for (final IBeanKey beanKey : parentBeans) {
			CapServiceToolkit.checkCanceled(executionCallback);
			final Node parentNode = NodeAccess.findNode(parentBeanTypeId, beanKey.getId());
			if (parentNode != null) {
				for (final Tuple<Node, Relationship> tuple : getAllRelatedNodes(parentNode, executionCallback)) {
					CapServiceToolkit.checkCanceled(executionCallback);
					result.add(beanFactory.createRelatedNodeBean(beanType, beanTypeId, tuple.getFirst(), tuple.getSecond()));
				}
			}
		}

		return new LinkedList<BEAN_TYPE>(result);
	}

	private List<Tuple<Node, Relationship>> getAllRelatedNodes(final Node parentNode, final IExecutionCallback executionCallback) {
		final List<Tuple<Node, Relationship>> parentNodes = new LinkedList<Tuple<Node, Relationship>>();
		parentNodes.add(new Tuple<Node, Relationship>(parentNode, null));
		return getAllRelatedNodes(parentNodes, path.iterator(), executionCallback);
	}

	private List<Tuple<Node, Relationship>> getAllRelatedNodes(
		final List<Tuple<Node, Relationship>> parentNodes,
		final Iterator<Tuple<RelationshipType, Direction>> iterator,
		final IExecutionCallback executionCallback) {

		if (iterator.hasNext()) {
			final List<Tuple<Node, Relationship>> nodes = getAllRelatedNodes(parentNodes, iterator.next(), executionCallback);
			if (iterator.hasNext()) {
				return getAllRelatedNodes(nodes, iterator, executionCallback);
			}
			else {
				return nodes;
			}
		}

		return Collections.emptyList();
	}

	private List<Tuple<Node, Relationship>> getAllRelatedNodes(
		final List<Tuple<Node, Relationship>> parentNodes,
		final Tuple<RelationshipType, Direction> relation,
		final IExecutionCallback executionCallback) {
		final List<Tuple<Node, Relationship>> result = new LinkedList<Tuple<Node, Relationship>>();
		final RelationshipType relationshipType = relation.getFirst();
		final Direction direction = relation.getSecond();
		for (final Tuple<Node, Relationship> parentNodeTuple : parentNodes) {
			CapServiceToolkit.checkCanceled(executionCallback);
			final Node parentNode = parentNodeTuple.getFirst();
			for (final Relationship relationship : parentNode.getRelationships(direction, relationshipType)) {
				CapServiceToolkit.checkCanceled(executionCallback);
				result.add(new Tuple<Node, Relationship>(relationship.getOtherNode(parentNode), relationship));
			}
		}
		return result;
	}

	private List<? extends BEAN_TYPE> getAllUnrelatedBeans(
		final List<? extends IBeanKey> parentBeans,
		final IExecutionCallback executionCallback) {
		final List<BEAN_TYPE> result = new LinkedList<BEAN_TYPE>();
		if (beanFactory.isNodeBean(beanType, beanTypeId)) {
			for (final Node node : nodeIndex.get(beanTypePropertyName, beanTypeIdString)) {
				CapServiceToolkit.checkCanceled(executionCallback);
				if (!isRelatedWith(node, parentBeans, executionCallback)) {
					result.add(beanFactory.createNodeBean(beanType, beanTypeId, node));
				}
			}
		}
		else {
			throw new IllegalStateException("The bean type '" + beanType + "' is not a node bean.");
		}
		return result;
	}

	private boolean isRelatedWith(
		final Node node,
		final List<? extends IBeanKey> parentBeans,
		final IExecutionCallback executionCallback) {
		final Object nodeId = node.getProperty(IBean.ID_PROPERTY);
		for (final IBeanKey beanKey : parentBeans) {
			final Node parentNode = NodeAccess.findNode(parentBeanTypeId, beanKey.getId());
			if (parentNode != null) {
				for (final Tuple<Node, Relationship> tuple : getAllRelatedNodes(parentNode, executionCallback)) {
					CapServiceToolkit.checkCanceled(executionCallback);
					if (nodeId.equals(tuple.getFirst().getProperty(IBean.ID_PROPERTY))) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
