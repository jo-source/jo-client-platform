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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.entity.IEntityApplicationNode;
import org.jowidgets.i18n.api.IMessage;

final class EntityApplicationNodeImpl implements IEntityApplicationNode, Serializable {

	private static final long serialVersionUID = -2241187136589714302L;

	private final Object id;
	private final IMessage label;
	private final IMessage description;
	private final Object iconDescriptor;
	private final List<IEntityApplicationNode> children;

	EntityApplicationNodeImpl(
		final Object entityId,
		final IMessage label,
		final IMessage description,
		final Object iconDescriptor,
		final Collection<IEntityApplicationNode> children) {
		this.id = entityId;
		this.label = label;
		this.description = description;
		this.iconDescriptor = iconDescriptor;
		this.children = Collections.unmodifiableList(new LinkedList<IEntityApplicationNode>(children));
	}

	@Override
	public Object getEntityId() {
		return id;
	}

	@Override
	public IMessage getLabel() {
		return label;
	}

	@Override
	public IMessage getDescription() {
		return description;
	}

	@Override
	public Object getIconDescriptor() {
		return iconDescriptor;
	}

	@Override
	public List<IEntityApplicationNode> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return "EntityClassImpl [entityId=" + id + ", label=" + label + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final EntityApplicationNodeImpl other = (EntityApplicationNodeImpl) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		}
		else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

}
