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

package org.jowidgets.cap.service.impl;

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.entity.EntityApplicationNode;
import org.jowidgets.cap.common.api.entity.IEntityApplicationNode;
import org.jowidgets.cap.common.api.service.IEntityApplicationService;
import org.jowidgets.cap.service.api.entity.IEntityApplicationServiceBuilder;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.tools.StaticMessage;
import org.jowidgets.util.Assert;
import org.jowidgets.util.builder.AbstractSingleUseBuilder;

@SuppressWarnings({"rawtypes", "unchecked"})
final class EntityApplicationServiceBuilderImpl extends AbstractSingleUseBuilder<IEntityApplicationService> implements
		IEntityApplicationServiceBuilder {

	private final List<IEntityApplicationNode> nodes;

	private IMessage label;
	private IMessage description;

	EntityApplicationServiceBuilderImpl() {
		this.nodes = new LinkedList();
		this.label = new StaticMessage();
		this.description = new StaticMessage();
	}

	@Override
	public IEntityApplicationServiceBuilder addNode(final IEntityApplicationNode node) {
		Assert.paramNotNull(node, "node");
		checkExhausted();
		nodes.add(node);
		return this;
	}

	@Override
	public IEntityApplicationServiceBuilder addNode(final Object entityId) {
		Assert.paramNotNull(entityId, "entityId");
		return addNode(EntityApplicationNode.builder().setEntityId(entityId).build());
	}

	@Override
	public IEntityApplicationServiceBuilder setLabel(final IMessage label) {
		Assert.paramNotNull(label, "label");
		checkExhausted();
		this.label = label;
		return this;
	}

	@Override
	public IEntityApplicationServiceBuilder setDescription(final IMessage description) {
		Assert.paramNotNull(description, "description");
		checkExhausted();
		this.description = description;
		return this;
	}

	@Override
	public IEntityApplicationServiceBuilder setLabel(final String label) {
		return setLabel(new StaticMessage(label));
	}

	@Override
	public IEntityApplicationServiceBuilder setDescription(final String description) {
		return setDescription(new StaticMessage(description));
	}

	@Override
	protected IEntityApplicationService doBuild() {
		return new EntityApplicationServiceImpl(label, description, nodes);
	}

}
