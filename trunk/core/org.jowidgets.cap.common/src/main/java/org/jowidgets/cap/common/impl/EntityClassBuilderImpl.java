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
import java.util.LinkedList;

import org.jowidgets.cap.common.api.entity.IEntityClass;
import org.jowidgets.cap.common.api.entity.IEntityClassBuilder;
import org.jowidgets.util.Assert;
import org.jowidgets.util.builder.AbstractSingleUseBuilder;

final class EntityClassBuilderImpl extends AbstractSingleUseBuilder<IEntityClass> implements IEntityClassBuilder {

	@SuppressWarnings("rawtypes")
	private final Collection subClasses;

	private Object id;
	private String label;
	private String description;

	EntityClassBuilderImpl() {
		this.subClasses = new LinkedList<IEntityClass>();
	}

	@Override
	public IEntityClassBuilder setId(final Object id) {
		checkExhausted();
		this.id = id;
		return this;
	}

	@Override
	public IEntityClassBuilder setLabel(final String label) {
		checkExhausted();
		this.label = label;
		return this;
	}

	@Override
	public IEntityClassBuilder setDescription(final String description) {
		checkExhausted();
		this.description = description;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IEntityClassBuilder setSubClasses(final Collection<? extends IEntityClass> subClasses) {
		Assert.paramNotNull(subClasses, "subClasses");
		checkExhausted();
		this.subClasses.clear();
		this.subClasses.addAll(subClasses);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IEntityClassBuilder addSubClass(final IEntityClass subClass) {
		Assert.paramNotNull(subClass, "subClass");
		checkExhausted();
		subClasses.add(subClass);
		return this;
	}

	@Override
	protected IEntityClass doBuild() {
		return new EntityClassImpl(id, label, description, subClasses);
	}

}
