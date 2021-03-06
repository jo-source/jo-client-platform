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
import java.util.HashSet;
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
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.traversal.TraversalDescription;

//TODO MG this implementation is not made for production use
final class SyncNeo4JSimpleTraversalReaderServiceImpl<BEAN_TYPE extends IBean, PARAM_TYPE> extends
		AbstractSimpleReaderService<BEAN_TYPE, PARAM_TYPE> {

	private final Class<? extends BEAN_TYPE> beanType;
	private final Object parentBeanTypeId;
	private final Object beanTypeId;
	private final String beanTypeIdString;
	private final String beanTypePropertyName;
	private final Index<Node> nodeIndex;
	private final IBeanFactory beanFactory;
	private final Collection<TraversalDescription> traversalDescriptions;
	private final boolean related;

	SyncNeo4JSimpleTraversalReaderServiceImpl(
		final Object parentBeanTypeId,
		final Class<? extends BEAN_TYPE> beanType,
		final Object beanTypeId,
		final Collection<TraversalDescription> traversalDescriptions,
		final IBeanDtoFactory<BEAN_TYPE> beanFactory,
		final Collection<IFilter> additionalFilters,
		final boolean related) {

		super(beanType, beanFactory, additionalFilters);

		Assert.paramNotNull(parentBeanTypeId, "parentBeanTypeId");
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotNull(beanFactory, "beanFactory");
		Assert.paramNotNull(traversalDescriptions, "traversalDescriptions");

		this.parentBeanTypeId = parentBeanTypeId;
		this.beanType = beanType;
		this.beanTypeId = beanTypeId;
		this.beanTypeIdString = BeanTypeIdUtil.toString(beanTypeId);
		this.traversalDescriptions = new LinkedList<TraversalDescription>(traversalDescriptions);
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
			return new LinkedList<BEAN_TYPE>(getAllRelatedBeans(parentBeans, parameter, executionCallback));
		}
		else {
			return new LinkedList<BEAN_TYPE>(getAllUnrelatedBeans(parentBeans, parameter, executionCallback));
		}
	}

	private Set<? extends BEAN_TYPE> getAllUnrelatedBeans(
		final List<? extends IBeanKey> parentBeans,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {
		if (beanFactory.isNodeBean(beanType, beanTypeId)) {
			final Set<BEAN_TYPE> result = new LinkedHashSet<BEAN_TYPE>();
			final Set<? extends BEAN_TYPE> relatedBeans = getAllRelatedBeans(parentBeans, parameter, executionCallback);
			final Set<Object> parentBeanIds = getParentBeanIds(parentBeans);
			for (final Node node : nodeIndex.get(beanTypePropertyName, beanTypeIdString)) {
				final BEAN_TYPE bean = beanFactory.createNodeBean(beanType, beanTypeId, node);
				CapServiceToolkit.checkCanceled(executionCallback);
				if (!relatedBeans.contains(bean) && !parentBeanIds.contains(bean.getId())) {
					result.add(bean);
				}
			}
			return result;
		}
		else {
			throw new IllegalStateException("The bean type '" + beanType + "' is not a node bean.");
		}
	}

	private Set<Object> getParentBeanIds(final List<? extends IBeanKey> parentBeans) {
		final Set<Object> result = new HashSet<Object>();
		for (final IBeanKey key : parentBeans) {
			result.add(key.getId());
		}
		return result;
	}

	private Set<? extends BEAN_TYPE> getAllRelatedBeans(
		final List<? extends IBeanKey> parentBeans,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {
		final Set<BEAN_TYPE> result = new LinkedHashSet<BEAN_TYPE>();
		for (final IBeanKey beanKey : parentBeans) {
			CapServiceToolkit.checkCanceled(executionCallback);
			final Node parentNode = NodeAccess.findNode(parentBeanTypeId, beanKey.getId());
			if (parentNode != null) {
				for (final TraversalDescription traversalDescription : traversalDescriptions) {
					for (final Node resultNode : traversalDescription.traverse(parentNode).nodes()) {
						CapServiceToolkit.checkCanceled(executionCallback);
						if (resultNode.hasProperty(beanTypePropertyName)
							&& beanTypeIdString.equals(resultNode.getProperty(beanTypePropertyName))) {
							result.add(beanFactory.createNodeBean(beanType, beanTypeId, resultNode));
						}
					}
				}
			}
		}
		return result;
	}

}
