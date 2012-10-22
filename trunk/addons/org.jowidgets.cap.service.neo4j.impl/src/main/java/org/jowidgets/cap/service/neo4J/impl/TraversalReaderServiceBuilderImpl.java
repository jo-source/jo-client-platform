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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.service.api.adapter.ISyncReaderService;
import org.jowidgets.cap.service.neo4j.api.ITraversalReaderServiceBuilder;
import org.jowidgets.util.Assert;
import org.neo4j.graphdb.traversal.TraversalDescription;

final class TraversalReaderServiceBuilderImpl<BEAN_TYPE extends IBean, PARAM_TYPE> extends
		AbstractNeo4JReaderServiceBuilderImpl<ITraversalReaderServiceBuilder<BEAN_TYPE, PARAM_TYPE>, BEAN_TYPE, PARAM_TYPE> implements
		ITraversalReaderServiceBuilder<BEAN_TYPE, PARAM_TYPE> {

	private Object parentBeanTypeId;
	private final List<TraversalDescription> traversalDescriptions;

	TraversalReaderServiceBuilderImpl() {
		this.traversalDescriptions = new LinkedList<TraversalDescription>();
	}

	@Override
	public ITraversalReaderServiceBuilder<BEAN_TYPE, PARAM_TYPE> setParentBeanTypeId(final Object parentBeanTypeId) {
		this.parentBeanTypeId = parentBeanTypeId;
		return this;
	}

	@Override
	public ITraversalReaderServiceBuilder<BEAN_TYPE, PARAM_TYPE> setTraversalDescriptions(
		final Collection<TraversalDescription> traversalDescriptions) {
		Assert.paramNotNull(traversalDescriptions, "traversalDescriptions");
		this.traversalDescriptions.clear();
		this.traversalDescriptions.addAll(traversalDescriptions);
		return this;
	}

	@Override
	public ITraversalReaderServiceBuilder<BEAN_TYPE, PARAM_TYPE> setTraversalDescriptions(
		final TraversalDescription[] traversalDescriptions) {
		Assert.paramNotNull(traversalDescriptions, "traversalDescriptions");
		final List<TraversalDescription> descriptionsList = Arrays.asList(traversalDescriptions);
		return setTraversalDescriptions(descriptionsList);
	}

	@Override
	public ITraversalReaderServiceBuilder<BEAN_TYPE, PARAM_TYPE> setTraversalDescription(
		final TraversalDescription traversalDescription) {
		Assert.paramNotNull(traversalDescription, "traversalDescription");
		this.traversalDescriptions.clear();
		return addTraversalDescription(traversalDescription);
	}

	@Override
	public ITraversalReaderServiceBuilder<BEAN_TYPE, PARAM_TYPE> addTraversalDescription(
		final TraversalDescription traversalDescription) {
		Assert.paramNotNull(traversalDescription, "traversalDescription");
		this.traversalDescriptions.add(traversalDescription);
		return this;
	}

	@Override
	protected ISyncReaderService<PARAM_TYPE> doBuild() {
		return new SyncNeo4JSimpleTraversalReaderServiceImpl<BEAN_TYPE, PARAM_TYPE>(
			parentBeanTypeId,
			getBeanType(),
			getBeanTypeId(),
			traversalDescriptions,
			getBeanDtoFactory(),
			getFilters());
	}

}
