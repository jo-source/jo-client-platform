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

package org.jowidgets.cap.common.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.entity.IEntityApplicationNode;
import org.jowidgets.cap.common.api.entity.IEntityApplicationNodeBuilder;
import org.jowidgets.util.Assert;
import org.jowidgets.util.builder.AbstractSingleUseBuilder;

final class EntityApplicationNodeBuilderImpl extends AbstractSingleUseBuilder<IEntityApplicationNode> implements
		IEntityApplicationNodeBuilder {

	private final Collection<IEntityApplicationNode> children;

	private Object id;
	private String label;
	private String description;

	EntityApplicationNodeBuilderImpl() {
		this.children = new LinkedList<IEntityApplicationNode>();
	}

	@Override
	public IEntityApplicationNodeBuilder setEntityId(final Object id) {
		checkExhausted();
		this.id = id;
		return this;
	}

	@Override
	public IEntityApplicationNodeBuilder setLabel(final String label) {
		checkExhausted();
		this.label = label;
		return this;
	}

	@Override
	public IEntityApplicationNodeBuilder setDescription(final String description) {
		checkExhausted();
		this.description = description;
		return this;
	}

	@Override
	public IEntityApplicationNodeBuilder addNode(final IEntityApplicationNode node) {
		Assert.paramNotNull(node, "node");
		checkExhausted();
		children.add(node);
		return this;
	}

	@Override
	public IEntityApplicationNodeBuilder addNode(final Object childEntityId) {
		Assert.paramNotNull(childEntityId, "childEntityId");
		final List<IEntityApplicationNode> nodeChildren = Collections.emptyList();
		children.add(new EntityApplicationNodeImpl(childEntityId, null, null, nodeChildren));
		return this;
	}

	@Override
	protected IEntityApplicationNode doBuild() {
		return new EntityApplicationNodeImpl(id, label, description, children);
	}

}
