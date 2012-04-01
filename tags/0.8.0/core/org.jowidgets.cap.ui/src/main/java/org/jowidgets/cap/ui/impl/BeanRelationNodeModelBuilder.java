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

import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModel;
import org.jowidgets.cap.ui.api.tree.IEntityTypeId;

class BeanRelationNodeModelBuilder<PARENT_BEAN_TYPE, CHILD_BEAN_TYPE, INSTANCE_TYPE> extends
		BeanRelationNodeModelBluePrint<CHILD_BEAN_TYPE, INSTANCE_TYPE> {

	private final IEntityTypeId<PARENT_BEAN_TYPE> parentEntityType;
	private final IEntityTypeId<CHILD_BEAN_TYPE> childEntityType;
	private final IBeanProxy<PARENT_BEAN_TYPE> parentBean;

	BeanRelationNodeModelBuilder(
		final IEntityTypeId<PARENT_BEAN_TYPE> parentEntityType,
		final IBeanProxy<PARENT_BEAN_TYPE> parentBean,
		final IEntityTypeId<CHILD_BEAN_TYPE> childEntityType) {
		super(childEntityType.getEntityId(), childEntityType.getBeanType());
		this.parentEntityType = parentEntityType;
		this.parentBean = parentBean;
		this.childEntityType = childEntityType;
	}

	IBeanRelationNodeModel<PARENT_BEAN_TYPE, CHILD_BEAN_TYPE> build() {
		return new BeanRelationNodeModelImpl<PARENT_BEAN_TYPE, CHILD_BEAN_TYPE>(
			getLabel(),
			parentBean,
			parentEntityType,
			childEntityType,
			getChildRenderer(),
			new LinkedList<IEntityTypeId<Object>>(getChildRelations()),
			getReaderService(),
			getReaderParameterProvider(),
			getCreatorService(),
			getRefreshService(),
			getUpdaterService(),
			getDeleterService(),
			getBeanValidators(),
			getAttributes());
	}
}
