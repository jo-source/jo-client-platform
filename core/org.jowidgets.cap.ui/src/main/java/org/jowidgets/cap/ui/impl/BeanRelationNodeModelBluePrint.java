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

package org.jowidgets.cap.ui.impl;

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.model.ILabelModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModelBluePrint;
import org.jowidgets.cap.ui.api.tree.IEntityTypeId;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.util.Assert;

class BeanRelationNodeModelBluePrint<CHILD_BEAN_TYPE, INSTANCE_TYPE> extends
		AbstractBeanModelBuilderImpl<CHILD_BEAN_TYPE, INSTANCE_TYPE> implements
		IBeanRelationNodeModelBluePrint<CHILD_BEAN_TYPE, INSTANCE_TYPE> {

	private final List<IEntityTypeId<Object>> childRelations;

	private String text;
	private String description;
	private IImageConstant icon;
	private IBeanProxyLabelRenderer<CHILD_BEAN_TYPE> childRenderer;

	BeanRelationNodeModelBluePrint(final Object entityId, final Class<CHILD_BEAN_TYPE> beanType) {
		super(entityId, beanType);
		this.childRelations = new LinkedList<IEntityTypeId<Object>>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE setText(final String text) {
		this.text = text;
		return (INSTANCE_TYPE) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE setDescription(final String description) {
		this.description = description;
		return (INSTANCE_TYPE) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE setIcon(final IImageConstant icon) {
		this.icon = icon;
		return (INSTANCE_TYPE) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE setChildRenderer(final IBeanProxyLabelRenderer<CHILD_BEAN_TYPE> renderer) {
		return (INSTANCE_TYPE) this;
	}

	@Override
	public INSTANCE_TYPE addChildRelation(final Object entityId, final Class<?> beanType) {
		return addChildRelation(new EntityTypeIdImpl<Object>(entityId, beanType));
	}

	@Override
	public INSTANCE_TYPE addChildRelation(final Class<?> beanType) {
		return addChildRelation(new EntityTypeIdImpl<Object>(beanType, beanType));
	}

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE addChildRelation(final IEntityTypeId<?> entityTypeId) {
		Assert.paramNotNull(entityTypeId, "entityTypeId");
		childRelations.add((IEntityTypeId<Object>) entityTypeId);
		return (INSTANCE_TYPE) this;
	}

	protected ILabelModel getLabel() {
		return new LabelModelImpl(text, description, icon);
	}

	protected List<IEntityTypeId<Object>> getChildRelations() {
		return childRelations;
	}

	protected IBeanProxyLabelRenderer<CHILD_BEAN_TYPE> getChildRenderer() {
		if (childRenderer == null) {
			return new BeanByAttributesRenderer<CHILD_BEAN_TYPE>(getAttributes());
		}
		return childRenderer;
	}

}
