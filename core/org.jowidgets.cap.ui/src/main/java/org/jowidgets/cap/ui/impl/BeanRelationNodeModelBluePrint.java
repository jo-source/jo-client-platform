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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.model.ILabelModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModelBluePrint;
import org.jowidgets.cap.ui.api.types.IEntityTypeId;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

class BeanRelationNodeModelBluePrint<CHILD_BEAN_TYPE, INSTANCE_TYPE> extends
		AbstractBeanModelBuilderImpl<CHILD_BEAN_TYPE, INSTANCE_TYPE> implements
		IBeanRelationNodeModelBluePrint<CHILD_BEAN_TYPE, INSTANCE_TYPE> {

	private static final int DEFAULT_PAGE_SIZE = 100;

	private final List<IEntityTypeId<Object>> childRelations;
	private final List<ISort> defaultSort;

	private String text;
	private String description;
	private IImageConstant icon;
	private IBeanProxyLabelRenderer<CHILD_BEAN_TYPE> childRenderer;
	private int pageSize;

	BeanRelationNodeModelBluePrint(final Object entityId, final Class<CHILD_BEAN_TYPE> beanType) {
		super(entityId, beanType);
		this.childRelations = new LinkedList<IEntityTypeId<Object>>();
		this.defaultSort = getDefaultSort(entityId);
		this.pageSize = DEFAULT_PAGE_SIZE;
	}

	private static List<ISort> getDefaultSort(final Object entityId) {
		final IBeanDtoDescriptor descriptor = EntityServiceHelper.getDtoDescriptor(entityId);
		if (descriptor != null) {
			return new LinkedList<ISort>(descriptor.getDefaultSorting());
		}
		else {
			return new LinkedList<ISort>();
		}
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
		this.childRenderer = renderer;
		return (INSTANCE_TYPE) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE setPageSize(final int pageSize) {
		this.pageSize = pageSize;
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

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE addDefaultSort(final ISort sort) {
		Assert.paramNotNull(sort, "sort");
		defaultSort.add(sort);
		return (INSTANCE_TYPE) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE setDefaultSort(final ISort... defaultSort) {
		Assert.paramNotNull(defaultSort, "defaultSort");
		this.defaultSort.clear();
		this.defaultSort.addAll(Arrays.asList(defaultSort));
		return (INSTANCE_TYPE) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public INSTANCE_TYPE setDefaultSort(final Collection<? extends ISort> defaultSort) {
		Assert.paramNotNull(defaultSort, "defaultSort");
		this.defaultSort.clear();
		this.defaultSort.addAll(defaultSort);
		return (INSTANCE_TYPE) this;
	}

	@Override
	public IBeanProxyLabelRenderer<CHILD_BEAN_TYPE> getChildRenderer() {
		if (childRenderer == null) {
			final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
			if (entityService != null) {
				final IBeanDtoDescriptor dtoDescriptor = entityService.getDescriptor(getEntityId());
				if (dtoDescriptor != null) {
					final String renderingPattern = dtoDescriptor.getRenderingPattern().get();
					if (!EmptyCheck.isEmpty(renderingPattern)) {
						return new BeanProxyLabelPatternRenderer<CHILD_BEAN_TYPE>(renderingPattern, getAttributes());
					}
				}
			}
			return new BeanByAttributesRenderer<CHILD_BEAN_TYPE>(getAttributes());
		}
		return childRenderer;
	}

	protected ILabelModel getLabel() {
		return new LabelModelImpl(text, description, icon);
	}

	protected List<IEntityTypeId<Object>> getChildRelations() {
		return childRelations;
	}

	protected List<ISort> getDefaultSort() {
		return defaultSort;
	}

	protected int getPageSize() {
		return pageSize;
	}

}
